package com.itspazaz.rt4;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.GLBuffers;
import rt4.core.DisplayMode;
import rt4.*;
import rt4.core.Preferences;
import rt4.data.StructTypeList;
import rt4.data.VarpTypeList;
import rt4.model.Model;
import rt4.network.Js5QuickChatCommandDecoder;
import rt4.render.Rasteriser;
import rt4.render.primitive.Sprites;
import rt4.scene.Npc;
import rt4.ui.TitleScreen;
import rt4.util.JagString;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.KeyEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Playground extends rt4.core.GameShell {
	public static Playground instance;

	public static rt4.network.BufferedFile cacheMasterIndex;
	public static rt4.network.BufferedFile cacheData;
	public static rt4.network.BufferedFile uid;
	public static rt4.network.BufferedFile[] cacheIndexes = new rt4.network.BufferedFile[28];

	public static rt4.network.Cache[] cacheArchives = new rt4.network.Cache[28];
	public static rt4.network.Cache masterCache;

	public static rt4.network.Js5MasterIndex js5MasterIndex;
	public static rt4.network.Js5CachedResourceProvider[] js5Providers = new rt4.network.Js5CachedResourceProvider[28];
	public static rt4.network.Js5[] archives = new rt4.network.Js5[28];
	public static rt4.network.Js5NetQueue js5NetQueue;
	public static rt4.network.Js5CacheQueue js5CacheQueue;
	private static PrivilegedRequest js5SocketRequest;
	private static rt4.network.BufferedSocket js5Socket;
	private static long js5ConnectTime;

	public static void main(String[] args) {
		instance = new Playground();
		instance.startApplication(32, "runescape");
		rt4.core.GameShell.frame.setLocation(40, 40);
	}

	@Override
	public void init() {
		instance = this;
		this.startApplet(32);
	}

	@Override
	protected void mainInit() {
		rt4.ui.Keyboard.init();
		rt4.ui.Keyboard.start(rt4.core.GameShell.canvas);
		rt4.ui.Mouse.start(rt4.core.GameShell.canvas);
		rt4.render.primitive.SoftwareRaster.frameBuffer.makeTarget();

		try {
			if (rt4.core.GameShell.signLink.cacheData != null) {
				cacheData = new rt4.network.BufferedFile(rt4.core.GameShell.signLink.cacheData, 5200, 0);
				for (int i = 0; i < 28; i++) {
					cacheIndexes[i] = new rt4.network.BufferedFile(rt4.core.GameShell.signLink.cacheIndexes[i], 6000, 0);
					cacheArchives[i] = new rt4.network.Cache(i, cacheData, cacheIndexes[i], 1000000);
				}
				cacheMasterIndex = new rt4.network.BufferedFile(rt4.core.GameShell.signLink.cacheMasterIndex, 6000, 0);
				masterCache = new rt4.network.Cache(255, cacheData, cacheMasterIndex, 500000);
				uid = new rt4.network.BufferedFile(rt4.core.GameShell.signLink.uid, 24, 0);
				rt4.core.GameShell.signLink.cacheIndexes = null;
				rt4.core.GameShell.signLink.cacheMasterIndex = null;
				rt4.core.GameShell.signLink.uid = null;
				rt4.core.GameShell.signLink.cacheData = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		js5NetQueue = new rt4.network.Js5NetQueue();
		js5CacheQueue = new rt4.network.Js5CacheQueue();

		rt4.core.Preferences.characterShadowsOn = false;
		Preferences.highDetailLighting = false;
	}

	public int percentage = 0;
	public int state = 0;

	public rt4.data.NpcType npcType;
	public rt4.scene.Npc npc;

	public void stateLoop() {
		if (state == 0) {
			rt4.ui.LoadingBarAwt.render(null, true, rt4.util.JagString.parse("Connecting to update server"), 1);

			if (js5MasterIndex == null) {
				js5MasterIndex = new rt4.network.Js5MasterIndex(js5NetQueue, js5CacheQueue);
			}

			if (js5MasterIndex.isReady()) {
				for (int i = 0; i < 28; i++) {
					js5Providers[i] = js5MasterIndex.getResourceProvider(i, masterCache, cacheArchives[i]);
					archives[i] = new rt4.network.Js5(js5Providers[i], false, false);
				}
				state++;
			}
		} else if (state == 1) {
			rt4.ui.LoadingBarAwt.render(null, true, rt4.util.JagString.parse("Initializing sprites"), 1);
			percentage = 0;
			for (int i = 0; i < 28; i++) {
				percentage += js5Providers[i].getIndexPercentageComplete() * rt4.core.client.JS5_ARCHIVE_WEIGHTS[i] / 100;
			}
			if (percentage == 100) {
				rt4.render.primitive.Sprites.init(archives[8]);
				rt4.ui.TitleScreen.init(archives[8]);
				rt4.ui.Flames.init(archives[8]);
				state++;
			}
		} else if (state == 2) {
			rt4.ui.LoadingBarAwt.render(null, true, rt4.util.JagString.parse("Preparing fonts"), 1);
			int ready = rt4.ui.Fonts.getReady(archives[8], archives[13]);
			int total = rt4.ui.Fonts.getTotal();
			if (ready >= total) {
				state++;
			}
		} else if (state == 3) {
			rt4.ui.LoadingBarAwt.render(null, true, rt4.util.JagString.parse("Preparing title screen"), 1);
			int ready = rt4.ui.TitleScreen.getReady(archives[8]);
			int total = TitleScreen.getTotal();
			if (ready >= total) {
				state++;
			}
		} else if (state == 4) {
			rt4.ui.LoadingBarAwt.render(null, true, rt4.util.JagString.parse("Loading fonts"), 1);
			rt4.ui.Fonts.load(archives[13], archives[8]);
			state++;
		} else if (state == 5) {
			rt4.ui.LoadingBarAwt.render(null, true, rt4.util.JagString.parse("Loading configs"), 1);
//            percentage = 0;
//            for (int i = 0; i < 28; ++i) {
//                archives[i].fetchAll();
//                percentage += archives[i].getPercentageComplete();
//            }
//            if (percentage > 2700) {
			rt4.data.ParamTypeList.init(archives[2]);
			rt4.data.FloTypeList.init(archives[2]);
			rt4.data.FluTypeList.init(archives[2]);
			rt4.data.IdkTypeList.init(archives[7], archives[2]);
			rt4.data.LocTypeList.init(archives[16], archives[7]);
			rt4.data.NpcTypeList.init(archives[7], archives[18]);
			rt4.data.ObjTypeList.init(archives[19], rt4.ui.Fonts.p11FullSoftware, archives[7]);
			StructTypeList.init(archives[2]);
			rt4.data.SeqTypeList.init(archives[1], archives[20], archives[0]);
			rt4.data.BasTypeList.init(archives[2]);
			rt4.data.SpotAnimTypeList.init(archives[7], archives[21]);
			rt4.data.VarbitTypeList.init(archives[22]);
			VarpTypeList.init(archives[2]);
			rt4.ui.InterfaceList.init(archives[13], archives[8], archives[3], archives[7]);
			rt4.data.InvTypeList.init(archives[2]);
			rt4.data.EnumTypeList.init(archives[17]);
			rt4.data.QuickChatPhraseTypeList.init(archives[25], archives[24], new Js5QuickChatCommandDecoder());
			rt4.data.QuickChatCatTypeList.init(archives[25], archives[24]);
			rt4.data.LightTypeList.init(archives[2]);
			rt4.ui.CursorTypeList.init(archives[2], archives[8]);
			rt4.data.MsiTypeList.init(archives[2], archives[8]);
			rt4.data.Equipment.init();
			state++;
//            }
		} else if (state == 6) {
			rt4.ui.LoadingBarAwt.render(null, true, JagString.parse("Loading sprites"), 1);
			int ready = rt4.render.primitive.Sprites.getReady(archives[8]);
			int total = rt4.render.primitive.Sprites.total();
			if (ready >= total) {
				Sprites.load(archives[8]);
				state++;
			}
		} else if (state == 7) {
			if (useGl) {
				initGl();
			}
			state++;
		} else if (state == 8) {
			rt4.network.Js5GlTextureProvider textureProvider = new rt4.network.Js5GlTextureProvider(archives[9], archives[26], archives[8], 20, false);
			rt4.render.Rasteriser.unpackTextures(textureProvider);
			rt4.render.Rasteriser.setBrightness(0.8F);
			rt4.render.Rasteriser.setBounds(rt4.core.GameShell.canvasWidth, rt4.core.GameShell.canvasHeight);
			rt4.render.Rasteriser.prepare();
			Rasteriser.prepareOffsets();
			loadItem(995, 10000);
			try {
				//loadNpc(exportCounter);
			} catch (Exception ex) {
				npc = null;
				npcType = null;
			}
			state++;
		}
	}

	rt4.render.primitive.Sprite sprite;

	public void loadItem(int id, int count) {
		sprite = rt4.data.Inv.getObjectSprite(0, id, false, count, 0);

		try {
			Files.write(
				Paths.get("items.csv"),
				"id,name,cost\n".getBytes(),
				StandardOpenOption.CREATE_NEW);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		;
		for (int i = 0; i < 14657; ++i) {
			try {
				rt4.data.ObjType obj = rt4.data.ObjTypeList.get(i);
				if (obj == null) {
					break;
				}
				Files.write(
					Paths.get("items.csv"),
					(i + "," + obj.name + "," + obj.cost + "\n").getBytes(),
					StandardOpenOption.APPEND);
			} catch (Exception ex) {
				break;
			}
		}
	}

	public void loadNpc(int id) {
		npcType = rt4.data.NpcTypeList.get(id);
		npc = new Npc();
		rt4.data.BasType basType = rt4.data.BasTypeList.get(npcType.bastypeid);
		npc.seqId = basType.idleAnimationId;
		npc.setNpcType(npcType);
		rt4.core.GameShell.frame.setTitle(npcType.name + " - " + id);
	}

	float yaw = 0.4f;
	float pitch = -0.4f;
	int zoom2d = 471;
	int zoom3d = 471;

	public void initGl() {
		rt4.render.GlRenderer.init(rt4.core.GameShell.canvas, 0);
		if (rt4.render.GlRenderer.enabled) {
			rt4.render.GlRenderer.setCanvasSize(rt4.core.GameShell.canvasWidth, rt4.core.GameShell.canvasHeight);
			rt4.render.GlRenderer.restoreLighting();
			float yaw1 = yaw * 360.0F / 6.2831855F;
			float pitch1 = pitch * 360.0F / 6.2831855F;
			rt4.render.GlRenderer.method4171(0, 0, rt4.core.GameShell.canvasWidth, rt4.core.GameShell.canvasHeight, rt4.core.GameShell.canvasWidth / 2, rt4.core.GameShell.canvasHeight / 2, yaw1, pitch1, zoom2d, zoom3d);
			rt4.render.GlRenderer.setViewportBounds(0, 0, rt4.core.GameShell.canvasWidth, rt4.core.GameShell.canvasHeight);
			rt4.render.GlRenderer.setDepthTestEnabled(true);
			rt4.render.GlRenderer.enableDepthMask();
			rt4.render.GlRenderer.setFogEnabled(true);
			DisplayMode.setWindowMode(false, 2, rt4.core.GameShell.canvasWidth, rt4.core.GameShell.canvasHeight);
			orientation = 292;
			x = 100;
			z = 218;
			y = 236;
			if (rt4.core.GameShell.canvasWidth >= 2500) {
				x = 56;
				z = 176;
				y = 120;
				orientation = 128;
			}
		}
	}

	public static boolean useGl = false;

	private void exportGlImage(String filename) {
		GL2 gl = GLContext.getCurrentGL().getGL2();
		ByteBuffer buffer = GLBuffers.newDirectByteBuffer(rt4.core.GameShell.canvasWidth * rt4.core.GameShell.canvasHeight * 4);

		gl.glReadBuffer(GL2.GL_BACK);
		gl.glReadPixels(0, 0, rt4.core.GameShell.canvasWidth, rt4.core.GameShell.canvasHeight, GL2.GL_BGRA, GL2.GL_UNSIGNED_BYTE, buffer);

		int[] pixels = new int[rt4.core.GameShell.canvasWidth * rt4.core.GameShell.canvasHeight];
		for (int y = rt4.core.GameShell.canvasHeight - 1; y > 0; --y) {
			for (int x = 0; x < rt4.core.GameShell.canvasWidth; ++x) {
				int r = buffer.get() & 0xFF;
				int g = buffer.get() & 0xFF;
				int b = buffer.get() & 0xFF;
				buffer.get();
				int a = 0xFF;
				if (r == 0x33 && g == 0x33 && b == 0x33) {
					a = 0x7F;
				}
				pixels[x + y * rt4.core.GameShell.canvasWidth] = r | (g << 8) | (b << 16) | (a << 24);
			}
		}
		// erase first line (black)
		for (int x = 0; x < rt4.core.GameShell.canvasWidth; ++x) {
			pixels[x] = 0x7F000000;
		}

		exportImage(pixels, filename);
	}

	private void exportImage(int[] pixels, String filename) {
		byte[] raw = new byte[pixels.length * 4];
		int offset = 0;
		for (int rgb : pixels) {
			raw[offset++] = (byte) (rgb >> 16); // red
			raw[offset++] = (byte) (rgb >> 8); // green
			raw[offset++] = (byte) (rgb); // blue

			// set transparency for background color
			if (rgb >> 24 == 0x7F) {
				raw[offset++] = (byte) 0;
			} else {
				raw[offset++] = (byte) 0xFF;
			}
		}

		try {
			DataBuffer buffer = new DataBufferByte(raw, raw.length);
			int samplesPerPixel = 4;
			int[] bandOffsets = {0, 1, 2, 3};
			ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
			WritableRaster raster = Raster.createInterleavedRaster(buffer, rt4.core.GameShell.canvasWidth, rt4.core.GameShell.canvasHeight, samplesPerPixel * rt4.core.GameShell.canvasWidth, samplesPerPixel, bandOffsets, null);
			BufferedImage image = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
			ImageIO.write(image, "PNG", new File(filename + ".png"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

    /*  NPC_NORMAL.xan2d = 96;
        NPC_NORMAL.yan2d = 128;

        CHATHEAD.zoom2d = 796;
        CHATHEAD.zoom3d = 512;
        CHATHEAD.xan2d = 40;
        CHATHEAD.yan2d = 1882;*/

	int exportCounter = 8105;
	int orientation = 378;
	int x = 112;
	int z = 180;
	int y = 116;
	int modifier = 2;
	int chatheadOrientation = 1882;
	int chatheadX = -34;
	int chatheadZ = 97;
	int chatheadY = 592;

	boolean renderHead = false;
	boolean perspectiveChanged = false;

	long lastInputTime = 0;

	public void inputLoop() {
		if (rt4.ui.Keyboard.getKey(KeyEvent.VK_W)) {
			y += modifier;
		} else if (rt4.ui.Keyboard.getKey(KeyEvent.VK_S)) {
			y -= modifier;
		}

		if (rt4.ui.Keyboard.getKey(KeyEvent.VK_Q)) {
			z += modifier;
		} else if (rt4.ui.Keyboard.getKey(KeyEvent.VK_E)) {
			z -= modifier;
		}

		if (rt4.ui.Keyboard.getKey(KeyEvent.VK_SHIFT) && rt4.ui.Keyboard.getKey(KeyEvent.VK_A)) {
			orientation += modifier;
			orientation &= 2047;
		} else if (rt4.ui.Keyboard.getKey(KeyEvent.VK_SHIFT) && rt4.ui.Keyboard.getKey(KeyEvent.VK_D)) {
			orientation -= modifier;
			orientation &= 2047;
		} else if (rt4.ui.Keyboard.getKey(KeyEvent.VK_A)) {
			x -= modifier;
		} else if (rt4.ui.Keyboard.getKey(KeyEvent.VK_D)) {
			x += modifier;
		}

		if (rt4.ui.Keyboard.getKey(KeyEvent.VK_UP)) {
			yaw -= 0.01f;
			perspectiveChanged = true;
		} else if (rt4.ui.Keyboard.getKey(KeyEvent.VK_DOWN)) {
			yaw += 0.01f;
			perspectiveChanged = true;
		}

		if (rt4.ui.Keyboard.getKey(KeyEvent.VK_LEFT)) {
			pitch += 0.01f;
			perspectiveChanged = true;
		} else if (rt4.ui.Keyboard.getKey(KeyEvent.VK_RIGHT)) {
			pitch -= 0.01f;
			perspectiveChanged = true;
		}

		if (rt4.ui.Keyboard.getKey(KeyEvent.VK_F)) {
			zoom2d -= modifier;
			perspectiveChanged = true;
		} else if (rt4.ui.Keyboard.getKey(KeyEvent.VK_G)) {
			zoom2d += modifier;
			perspectiveChanged = true;
		}

		if (rt4.ui.Keyboard.getKey(KeyEvent.VK_OPEN_BRACKET)) {
			modifier--;
		} else if (rt4.ui.Keyboard.getKey(KeyEvent.VK_CLOSE_BRACKET)) {
			modifier++;
		}

		// rate limited input events
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastInputTime < 100) {
			return;
		}
		lastInputTime = currentTime;

		if (rt4.ui.Keyboard.getKey(KeyEvent.VK_H)) {
			renderHead = !renderHead;
			if (renderHead) {
				x = chatheadX;
				z = chatheadZ;
				y = chatheadY;
				orientation = chatheadOrientation;
				yaw = 0.17f;
				pitch = 0.09f;
				perspectiveChanged = true;
				zoom2d = 796;
				zoom3d = 512;
			} else {
				x = 56;
				z = 176;
				y = 120;
				orientation = 128;
				yaw = 0.4f;
				pitch = -0.4f;
				perspectiveChanged = true;
				zoom2d = 471;
				zoom3d = 471;
			}
		}

		if (rt4.ui.Keyboard.getKey(KeyEvent.VK_BACK_SLASH)) {
			if (rt4.render.GlRenderer.enabled) {
				exportGlImage("dump/" + exportCounter++);
			} else {
				exportImage(rt4.render.primitive.SoftwareRaster.pixels, "dump/" + exportCounter++);
			}
		}

		if (rt4.ui.Keyboard.getKey(KeyEvent.VK_1)) {
			exportCounter--;
		} else if (rt4.ui.Keyboard.getKey(KeyEvent.VK_2)) {
			exportCounter++;
		}

		if (rt4.ui.Keyboard.getKey(KeyEvent.VK_P)) {
			System.out.println("cam: " + orientation + ", " + x + ", " + z + ", " + y);
			System.out.println("per: " + yaw + ", " + pitch);
			System.out.println("zoom: " + zoom2d + ", " + zoom3d);
			System.out.println();
		}
	}

	int lastExportCounter = -1;

	@Override
	protected void mainLoop() {
		rt4.ui.Keyboard.loop();
		rt4.ui.Mouse.loop();

		js5NetLoop();
		stateLoop();
		inputLoop();

		if (state == 9) {
			if (lastExportCounter != exportCounter) {
				try {
					//loadNpc(exportCounter);
				} catch (Exception ex) {
					npc = null;
					npcType = null;
				}
			}
		}
	}

	public int js5ConnectState = 0;

	public void js5NetLoop() {
		boolean idle = js5NetQueue.loop();
		if (!idle) {
			js5Connect();
		}
	}

	public void js5Connect() {
		try {
			if (js5ConnectState == 0) {
				js5SocketRequest = rt4.core.GameShell.signLink.openSocket(rt4.core.GlobalConfig.DEFAULT_HOSTNAME, rt4.core.GlobalConfig.DEFAULT_PORT + 1);
				js5ConnectState++;
			}
			if (js5ConnectState == 1) {
				if (js5SocketRequest.status == 2) {
					setJs5Response(1000);
					return;
				}
				if (js5SocketRequest.status == 1) {
					js5ConnectState++;
				}
			}
			if (js5ConnectState == 2) {
				js5Socket = new rt4.network.BufferedSocket((Socket) js5SocketRequest.result, rt4.core.GameShell.signLink);
				rt4.network.Buffer buffer = new rt4.network.Buffer(5);
				buffer.p1(15);
				buffer.p4(530);
				js5Socket.write(buffer.data, 5);
				js5ConnectState++;
				js5ConnectTime = MonotonicClock.currentTimeMillis();
			}
			if (js5ConnectState == 3) {
				if (js5Socket.available() > 0) {
					int response = js5Socket.read();
					if (response != 0) {
						setJs5Response(response);
						return;
					}
					js5ConnectState++;
				} else if (MonotonicClock.currentTimeMillis() - js5ConnectTime > rt4.core.GlobalConfig.JS5_RESPONSE_TIMEOUT) {
					setJs5Response(1001);
					return;
				}
			}
			if (js5ConnectState == 4) {
				js5NetQueue.start(true, js5Socket);
				js5SocketRequest = null;
				js5ConnectState = 0;
				js5Socket = null;
			}
		} catch (IOException ex) {
			setJs5Response(1002);
		}
	}

	private void setJs5Response(int response) {
		js5NetQueue.response = response;
		js5Socket = null;
		js5NetQueue.errors++;
		js5ConnectState = 0;
		js5SocketRequest = null;
	}

	@Override
	protected void mainRedraw() {
		try {
			if (state == 9) {
				if (!rt4.render.GlRenderer.enabled) {
					rt4.render.primitive.SoftwareRaster.clear(0x7F666666);
				} else {
					rt4.render.GlRenderer.clearColorAndDepthBuffers(0x333333);
				}

				if (perspectiveChanged) {
					float yaw1 = yaw * 360.0F / 6.2831855F;
					float pitch1 = pitch * 360.0F / 6.2831855F;
					rt4.render.GlRenderer.method4171(0, 0, rt4.core.GameShell.canvasWidth, rt4.core.GameShell.canvasHeight, rt4.core.GameShell.canvasWidth / 2, rt4.core.GameShell.canvasHeight / 2, yaw1, pitch1, zoom2d, zoom2d);
					perspectiveChanged = false;
				}

				if (npc != null) {
					rt4.data.SeqType seqType = rt4.data.SeqTypeList.get(9804);
					Model head = npcType.getHeadModel(seqType, 0, 0, 0);
					if (renderHead && head != null) {
						head.render(orientation, 25079, 60547, -44308, 48222, x, z, y, 0L, 0, null);
					} else {
						npc.render(orientation, 25079, 60547, -44308, 48222, x, z, y, 0L, 0, null);
					}
				}

				if (sprite != null) {
					sprite.render(canvasWidth / 2 - 144, canvasHeight / 2 - 128);
				}

				if (!rt4.render.GlRenderer.enabled) {
					rt4.render.primitive.SoftwareRaster.frameBuffer.draw(rt4.core.GameShell.canvas.getGraphics());
				} else {
					rt4.render.GlRenderer.draw();
					rt4.render.GlRenderer.swapBuffers();
				}

				if (lastExportCounter != exportCounter) {
					//                exportGlImage("dump/" + exportCounter);
					lastExportCounter = exportCounter;
					//                exportCounter++;
				}
			}
		} catch (Exception ex) {
		}
	}

	@Override
	protected void mainQuit() {
		rt4.ui.Keyboard.stop(rt4.core.GameShell.canvas);
		rt4.ui.Mouse.stop(rt4.core.GameShell.canvas);
		rt4.ui.Keyboard.quit();
		rt4.ui.Mouse.quit();

		if (rt4.render.GlRenderer.enabled) {
			rt4.render.GlRenderer.quit();
		}

		if (rt4.core.GameShell.signLink != null) {
			rt4.core.GameShell.signLink.unloadGlNatives(this.getClass());
		}
	}

	@Override
	protected void reset() {
	}
}
