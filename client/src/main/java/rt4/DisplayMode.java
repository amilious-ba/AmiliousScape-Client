package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;
import plugin.PluginRepository;

import java.awt.*;

@OriginalClass("client!od")
public final class DisplayMode {

	@OriginalMember(owner = "client!ib", name = "i", descriptor = "[Lclient!od;")
	public static DisplayMode[] aClass114Array1;
	@OriginalMember(owner = "client!rc", name = "M", descriptor = "Z")
	public static boolean aBoolean73 = false;
	@OriginalMember(owner = "client!jk", name = "y", descriptor = "Z")
	public static boolean aBoolean156 = false;
	@OriginalMember(owner = "client!hi", name = "f", descriptor = "J")
	public static long aLong89 = 0L;

	@OriginalMember(owner = "client!od", name = "j", descriptor = "I")
	public int width;

	@OriginalMember(owner = "client!od", name = "k", descriptor = "I")
	public int refreshRate;

	@OriginalMember(owner = "client!od", name = "l", descriptor = "I")
	public int height;

	@OriginalMember(owner = "client!od", name = "m", descriptor = "I")
	public int bitDepth;


	@OriginalMember(owner = "client!c", name = "a", descriptor = "(Ljava/awt/Frame;ZLsignlink!ll;)V")
	public static void exitFullScreen(@OriginalArg(0) Frame arg0, @OriginalArg(2) SignLink arg1) {
		while (true) {
			@Pc(16) PrivilegedRequest local16 = arg1.exitFullScreen(arg0);
			while (local16.status == 0) {
				ThreadUtils.sleep(10L);
			}
			if (local16.status == 1) {
				arg0.setVisible(false);
				arg0.dispose();
				return;
			}
			ThreadUtils.sleep(100L);
		}
	}

	@OriginalMember(owner = "client!th", name = "a", descriptor = "(ZIIII)V")
	public static void setWindowMode(@OriginalArg(0) boolean arg0, @OriginalArg(1) int arg1, @OriginalArg(3) int arg2, @OriginalArg(4) int arg3) {
		aLong89 = 0L;
		@Pc(4) int mode = getWindowMode();
		if (arg1 == 3 || mode == 3) {
			arg0 = true;
		}
		@Pc(44) boolean useHd = mode > 0 != arg1 > 0;
		if (arg0 && arg1 > 0) {
			useHd = true;
		}
		setWindowMode(arg0, arg1, useHd, mode, arg2, arg3);
	}




	@OriginalMember(owner = "client!le", name = "a", descriptor = "(I)I")
	public static int getWindowMode() {
		if (GameShell.fullScreenFrame != null
				|| GameShell.borderlessFullscreenActive
				|| GameShell.exclusiveFullscreenActive) {
			return 3;
		} else if (GlRenderer.enabled && aBoolean156) {
			return 2;
		} else if (GlRenderer.enabled) {
			return 1;
		} else {
			return 0;
		}
	}

	@OriginalMember(owner = "client!pm", name = "a", descriptor = "(ZIZIZII)V")
	public static void setWindowMode(boolean arg0, int arg1, boolean arg2, int mode, int arg4, int arg5) {
		if (arg2) {
			GlRenderer.quit();
		}

		// ----- EXIT fullscreen -----
		boolean leavingFullscreen = (GameShell.fullScreenFrame != null
				|| GameShell.borderlessFullscreenActive
				|| GameShell.exclusiveFullscreenActive)
				&& (arg1 != 3 || arg4 != Preferences.fullScreenWidth || arg5 != Preferences.fullScreenHeight);

		if (leavingFullscreen) {
			if (GameShell.borderlessFullscreenActive) {
				exitBorderlessFullscreen();
			} else if (GameShell.exclusiveFullscreenActive) {
				exitExclusiveFullscreen();
			} else if (GameShell.fullScreenFrame != null && GameShell.fullScreenFrame != GameShell.frame) {
				// legacy second-frame exclusive only
				if (GameShell.frame != null) {
					GameShell.frame.setVisible(false);
				}
				exitFullScreen(GameShell.fullScreenFrame, GameShell.signLink);
				GameShell.fullScreenFrame = null;
			}
		}

		// ----- ENTER fullscreen (mode 3) -----
		if (arg1 == 3 && GameShell.fullScreenFrame == null
				&& !GameShell.borderlessFullscreenActive
				&& !GameShell.exclusiveFullscreenActive) {

			if (GameShell.frame != null && mode != 3 && GameShell.windowedFrameWidth == 0) {
				java.awt.Dimension currentSize = GameShell.frame.getSize();
				java.awt.Insets insets = GameShell.frame.getInsets();
				java.awt.Point location = GameShell.frame.getLocation();
				GameShell.windowedFrameWidth = currentSize.width - insets.left - insets.right;
				GameShell.windowedFrameHeight = currentSize.height - insets.top - insets.bottom;
				GameShell.windowedFrameX = location.x;
				GameShell.windowedFrameY = location.y;
				System.out.println("Saved windowed size: " + GameShell.windowedFrameWidth + "x" + GameShell.windowedFrameHeight
						+ " at " + GameShell.windowedFrameX + "," + GameShell.windowedFrameY);
			}

			boolean useBorderless = GlobalJsonConfig.instance == null
					|| GlobalJsonConfig.instance.borderlessFullscreen;

			if (useBorderless) {
				enterBorderlessFullscreen(arg4, arg5);
			} else {
				enterExclusiveFullscreen(arg4, arg5);
				// Fallback: legacy second-frame exclusive
				if (!GameShell.exclusiveFullscreenActive) {
					GameShell.fullScreenFrame = method3176(0, arg5, arg4, GameShell.signLink);
					if (GameShell.fullScreenFrame != null) {
						Preferences.fullScreenHeight = arg5;
						Preferences.fullScreenWidth = arg4;
						Preferences.write(GameShell.signLink);
					}
				}
			}
		}

		// Fallback if enter failed entirely
		if (arg1 == 3
				&& GameShell.fullScreenFrame == null
				&& !GameShell.borderlessFullscreenActive
				&& !GameShell.exclusiveFullscreenActive) {
			setWindowMode(true, Preferences.favoriteWorlds, true, mode, -1, -1);
			return;
		}

		if (GameShell.replaceCanvas) {
			arg0 = true;
		}

		// ----- Active container -----
		Container local85;
		if (GameShell.fullScreenFrame != null) {
			local85 = GameShell.fullScreenFrame;
		} else if (GameShell.frame == null) {
			local85 = GameShell.signLink.applet;
		} else {
			local85 = GameShell.frame;
		}

		// Restore windowed size when exiting, else use container size
		Insets local109 = null;
		boolean restoredFromSaved =
				GameShell.fullScreenFrame == null
						&& !GameShell.borderlessFullscreenActive
						&& !GameShell.exclusiveFullscreenActive
						&& GameShell.windowedFrameWidth > 0
						&& mode == 3;

		if (restoredFromSaved) {
			// Already client-area size (saved before FS). Do NOT subtract insets again.
			GameShell.frameWidth = GameShell.windowedFrameWidth;
			GameShell.frameHeight = GameShell.windowedFrameHeight;
			System.out.println("Restoring windowed size: " + GameShell.frameWidth + "x" + GameShell.frameHeight);
		} else {
			GameShell.frameWidth = local85.getSize().width;
			GameShell.frameHeight = local85.getSize().height;

			// Outer → client only when we measured getSize()
			if (GameShell.frame == local85
					&& !GameShell.borderlessFullscreenActive
					&& !GameShell.exclusiveFullscreenActive) {
				local109 = GameShell.frame.getInsets();
				GameShell.frameWidth -= local109.right + local109.left;
				GameShell.frameHeight -= local109.bottom + local109.top;
			}
		}


		if (arg1 >= 2) {
			GameShell.canvasWidth = GameShell.frameWidth;
			GameShell.canvasHeight = GameShell.frameHeight;
			GameShell.leftMargin = 0;
			GameShell.topMargin = 0;
		} else {
			GameShell.topMargin = 0;
			GameShell.leftMargin = (GameShell.frameWidth - 765) / 2;
			GameShell.canvasWidth = 765;
			GameShell.canvasHeight = 503;
		}

		if (arg0) {
			Keyboard.stop(GameShell.canvas);
			Mouse.stop(GameShell.canvas);
			if (client.mouseWheel != null) {
				client.mouseWheel.stop(GameShell.canvas);
			}
			client.instance.addCanvas();
			Keyboard.start(GameShell.canvas);
			Mouse.start(GameShell.canvas);
			if (client.mouseWheel != null) {
				client.mouseWheel.start(GameShell.canvas);
			}

			long deadline = System.currentTimeMillis() + 2000;
			while (GameShell.canvas != null
					&& !GameShell.canvas.isDisplayable()
					&& System.currentTimeMillis() < deadline) {
				try {
					GameShell.frame.validate();
					GameShell.canvas.setVisible(true);
					Thread.sleep(50);
				} catch (Exception ignored) {
				}
			}

			System.out.println("Canvas displayable=" + (GameShell.canvas != null && GameShell.canvas.isDisplayable())
					+ " size=" + GameShell.canvasWidth + "x" + GameShell.canvasHeight);

			if (arg1 > 0) {
				try {
					GameShell.canvas.setIgnoreRepaint(true);

					if (!aBoolean73) {
						PrivilegedRequest req = GameShell.signLink.loadGlNatives(client.instance.getClass());
						while (req.status == 0) {
							ThreadUtils.sleep(100L);
						}
						if (req.status == 1) {
							aBoolean73 = true;
						}
						System.out.println("loadGlNatives status=" + req.status);
					}

					if (aBoolean73) {
						int result = GlRenderer.init(GameShell.canvas, Preferences.antiAliasingMode * 2);
						System.out.println("GlRenderer.init result=" + result + " enabled=" + GlRenderer.enabled);
					} else {
						System.out.println("Skipped GlRenderer.init – natives not loaded");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			if (GlRenderer.enabled) {
				GlRenderer.setCanvasSize(GameShell.canvasWidth, GameShell.canvasHeight);
			}
			GameShell.canvas.setSize(GameShell.canvasWidth, GameShell.canvasHeight);
			if (GameShell.frame == local85
					&& !GameShell.borderlessFullscreenActive
					&& !GameShell.exclusiveFullscreenActive) {
				local109 = GameShell.frame.getInsets();
				GameShell.canvas.setLocation(local109.left + GameShell.leftMargin, local109.top + GameShell.topMargin);
			} else {
				GameShell.canvas.setLocation(GameShell.leftMargin, GameShell.topMargin);
			}
		}

		if (arg1 == 0 && mode > 0) {
			GlRenderer.createAndDestroyContext(GameShell.canvas);
		}

		if (arg2 && arg1 > 0) {
			GameShell.canvas.setIgnoreRepaint(true);
			if (!aBoolean73) {
				SceneGraph.clear();
				SoftwareRaster.frameBuffer = null;
				SoftwareRaster.frameBuffer = FrameBuffer.create(GameShell.canvasHeight, GameShell.canvasWidth, GameShell.canvas);
				SoftwareRaster.clear();
				if (client.gameState == 5) {
					LoadingBar.render(true, Fonts.b12Full);
				} else {
					Fonts.drawTextOnScreen(false, LocalizedText.LOADING);
				}
				try {
					Graphics local269 = GameShell.canvas.getGraphics();
					SoftwareRaster.frameBuffer.draw(local269);
				} catch (Exception local277) {
				}
				GameShell.method2704();
				if (mode == 0) {
					SoftwareRaster.frameBuffer = FrameBuffer.create(503, 765, GameShell.canvas);
				} else {
					SoftwareRaster.frameBuffer = null;
				}
				PrivilegedRequest local300 = GameShell.signLink.loadGlNatives(client.instance.getClass());
				while (local300.status == 0) {
					ThreadUtils.sleep(100L);
				}
				if (local300.status == 1) {
					aBoolean73 = true;
				}
			}
			if (aBoolean73) {
				GlRenderer.init(GameShell.canvas, Preferences.antiAliasingMode * 2);
			}
		}

		if (!GlRenderer.enabled && arg1 > 0) {
			setWindowMode(true, 0, true, mode, -1, -1);
			return;
		}

		if (arg1 > 0 && mode == 0) {
			GameShell.thread.setPriority(5);
			SoftwareRaster.frameBuffer = null;
			SoftwareModel.method4580();
			((Js5GlTextureProvider) Rasteriser.textureProvider).method3248(200);
			if (Preferences.highDetailLighting) {
				Rasteriser.setBrightness(0.7F);
			}
			LoginManager.method4637();
		} else if (arg1 == 0 && mode > 0) {
			GameShell.thread.setPriority(1);
			SoftwareRaster.frameBuffer = FrameBuffer.create(503, 765, GameShell.canvas);
			SoftwareModel.method4583();
			ParticleSystem.quit();
			((Js5GlTextureProvider) Rasteriser.textureProvider).method3248(20);
			if (Preferences.highDetailLighting) {
				if (Preferences.brightness == 1) {
					Rasteriser.setBrightness(0.9F);
				}
				if (Preferences.brightness == 2) {
					Rasteriser.setBrightness(0.8F);
				}
				if (Preferences.brightness == 3) {
					Rasteriser.setBrightness(0.7F);
				}
				if (Preferences.brightness == 4) {
					Rasteriser.setBrightness(0.6F);
				}
			}
			GlTile.method1939();
			LoginManager.method4637();
		}

		SceneGraph.aBoolean130 = !SceneGraph.allLevelsAreVisible();
		if (arg2) {
			client.method2721();
		}
		aBoolean156 = arg1 >= 2;
		if (InterfaceList.topLevelInterface != -1) {
			InterfaceList.method3712(true);
		}

		// FS enter/leave rebuilds peers — re-run interface *open* scripts so Graphics
		// radios match getWindowMode() (same path as closing + reopening the panel)
		if (arg1 == 3 || mode == 3) {
			if (InterfaceList.topLevelInterface != -1) {
				InterfaceList.method1626(InterfaceList.topLevelInterface);
			}
			if (InterfaceList.openInterfaces != null) {
				for (rt4.ComponentPointer p = (rt4.ComponentPointer) InterfaceList.openInterfaces.head();
					 p != null;
					 p = (rt4.ComponentPointer) InterfaceList.openInterfaces.next()) {
					InterfaceList.method1626(p.interfaceId);
				}
			}
			for (int i = 0; i < 100; i++) {
				InterfaceList.aBooleanArray100[i] = true;
			}
			GameShell.fullRedraw = true;
		}

		// Amilious: keep 371 on the right parent + pin above chat
		rt4.amilious.TutorialPatch.onWindowModeChanged();

		if (Protocol.socket != null && (client.gameState == 30 || client.gameState == 25)) {
			ClientProt.sendWindowDetails();
		}
		for (int local466 = 0; local466 < 100; local466++) {
			InterfaceList.aBooleanArray100[local466] = true;
		}
		GameShell.fullRedraw = true;
		GameShell.replaceCanvas = false;
		PluginRepository.reloadPlugins();
	}

	/** Enter single-window borderless fullscreen using the existing GameShell.frame. */
	private static void enterBorderlessFullscreen(int width, int height) {
		if (GameShell.frame == null) {
			return;
		}

		try {
			/*GraphicsConfiguration gc = GameShell.frame.getGraphicsConfiguration();
			Rectangle bounds = gc.getBounds();

			int w = (width > 0) ? width : bounds.width;
			int h = (height > 0) ? height : bounds.height;*/

			GraphicsConfiguration gc = GameShell.frame.getGraphicsConfiguration();
			if (gc == null) {
				gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
						.getDefaultScreenDevice()
						.getDefaultConfiguration();
			}
			Rectangle bounds = gc.getBounds();

			// Always use scaled AWT bounds — never native DisplayMode w/h
			int w = bounds.width;
			int h = bounds.height;



			// Detach canvas BEFORE dispose so JOGL isn't holding a dead surface
			if (GameShell.canvas != null) {
				try {
					Keyboard.stop(GameShell.canvas);
					Mouse.stop(GameShell.canvas);
					if (client.mouseWheel != null) {
						client.mouseWheel.stop(GameShell.canvas);
					}
					if (GameShell.canvas.getParent() != null) {
						GameShell.canvas.getParent().remove(GameShell.canvas);
					}
				} catch (Exception ignored) {
				}
			}

			GameShell.frame.setVisible(false);
			GameShell.frame.dispose();

			GameShell.frame.setUndecorated(true);
			GameShell.frame.setBounds(bounds.x, bounds.y, w, h);
			GameShell.frame.setVisible(true);
			GameShell.frame.setAlwaysOnTop(true);  // force above taskba
			GameShell.frame.validate();
			GameShell.frame.toFront();
			GameShell.frame.requestFocus();

			GameShell.borderlessFullscreenActive = true;
			GameShell.fullScreenFrame = GameShell.frame; // marker so getWindowMode / scripts see FS
			GameShell.replaceCanvas = true;
			Preferences.fullScreenWidth = w;
			Preferences.fullScreenHeight = h;
			Preferences.write(GameShell.signLink);

			System.out.println("Entered borderless fullscreen: " + w + "x" + h);
		} catch (Exception e) {
			e.printStackTrace();
			GameShell.borderlessFullscreenActive = false;
		}
	}

	/** Restore the normal decorated window from borderless fullscreen. */
	private static void exitBorderlessFullscreen() {
		if (GameShell.frame == null) {
			GameShell.borderlessFullscreenActive = false;
			return;
		}

		try {
			int w = GameShell.windowedFrameWidth > 0 ? GameShell.windowedFrameWidth : 1024;
			int h = GameShell.windowedFrameHeight > 0 ? GameShell.windowedFrameHeight : 768;

			if (GameShell.canvas != null) {
				try {
					Keyboard.stop(GameShell.canvas);
					Mouse.stop(GameShell.canvas);
					if (client.mouseWheel != null) {
						client.mouseWheel.stop(GameShell.canvas);
					}
					if (GameShell.canvas.getParent() != null) {
						GameShell.canvas.getParent().remove(GameShell.canvas);
					}
				} catch (Exception ignored) {
				}
			}

			GameShell.frame.setVisible(false);
			GameShell.frame.dispose();

			GameShell.frame.setUndecorated(false);
			GameShell.frame.setSize(w + 16, h + 39);
			if (GameShell.windowedFrameX > 0 || GameShell.windowedFrameY > 0) {
				GameShell.frame.setLocation(GameShell.windowedFrameX, GameShell.windowedFrameY);
			} else {
				GameShell.frame.setLocationRelativeTo(null);
			}

			GameShell.frame.setVisible(true);
			GameShell.frame.validate();

			Insets insets = GameShell.frame.getInsets();
			GameShell.frame.setSize(insets.left + w + insets.right, insets.top + h + insets.bottom);
			GameShell.frame.toFront();

			GameShell.borderlessFullscreenActive = false;
			GameShell.fullScreenFrame = null; // clear marker only – do NOT dispose
			GameShell.replaceCanvas = true;
			System.out.println("Exited borderless fullscreen, restored " + w + "x" + h);
		} catch (Exception e) {
			e.printStackTrace();
			GameShell.borderlessFullscreenActive = false;
		}
	}

	private static void enterExclusiveFullscreen(int width, int height) {
		if (GameShell.frame == null) {
			return;
		}

		try {
			if (GameShell.canvas != null) {
				try {
					Keyboard.stop(GameShell.canvas);
					Mouse.stop(GameShell.canvas);
					if (client.mouseWheel != null) {
						client.mouseWheel.stop(GameShell.canvas);
					}
					if (GameShell.canvas.getParent() != null) {
						GameShell.canvas.getParent().remove(GameShell.canvas);
					}
				} catch (Exception ignored) {
				}
			}

			GraphicsConfiguration gc = GameShell.frame.getGraphicsConfiguration();
			GraphicsDevice device = gc.getDevice();
			if (!device.isFullScreenSupported()) {
				System.out.println("[FS] Exclusive not supported on this device");
				return;
			}

			// Native (physical) size of the monitor — what setDisplayMode understands
			java.awt.DisplayMode current = device.getDisplayMode();
			int wantW = current.getWidth();
			int wantH = current.getHeight();

			// Optional: honor a *native* preference if it matches a real mode
			if (width >= 800 && height >= 600) {
				// If prefs look like scaled logical size, map up via transform
				double sx = 1.0, sy = 1.0;
				try {
					java.awt.geom.AffineTransform tx = gc.getDefaultTransform();
					if (tx.getScaleX() > 0) sx = tx.getScaleX();
					if (tx.getScaleY() > 0) sy = tx.getScaleY();
				} catch (Exception ignored) {
				}
				int nativeFromArgsW = (int) Math.round(width * sx);
				int nativeFromArgsH = (int) Math.round(height * sy);

				// Prefer exact native match to current if args are clearly logical
				boolean argsLookLogical = (width < current.getWidth() - 50);
				int candidateW = argsLookLogical ? nativeFromArgsW : width;
				int candidateH = argsLookLogical ? nativeFromArgsH : height;

				for (java.awt.DisplayMode dm : device.getDisplayModes()) {
					if (dm.getWidth() == candidateW && dm.getHeight() == candidateH) {
						wantW = candidateW;
						wantH = candidateH;
						break;
					}
				}
			}

			java.awt.DisplayMode best = null;
			for (java.awt.DisplayMode dm : device.getDisplayModes()) {
				if (dm.getWidth() == wantW && dm.getHeight() == wantH) {
					if (best == null
							|| dm.getBitDepth() > best.getBitDepth()
							|| (dm.getBitDepth() == best.getBitDepth()
							&& dm.getRefreshRate() > best.getRefreshRate())) {
						best = dm;
					}
				}
			}
			if (best == null) {
				best = current;
			}

			// Undecorated while not displayable
			GameShell.frame.setVisible(false);
			try {
				GameShell.frame.dispose();
			} catch (Exception ignored) {
			}
			try {
				GameShell.frame.setUndecorated(true);
			} catch (Exception e) {
				System.out.println("[FS] setUndecorated failed: " + e.getMessage());
			}

			// REQUIRED order: full-screen window first, then display mode
			device.setFullScreenWindow(GameShell.frame);

			try {
				if (best != null && !best.equals(device.getDisplayMode())) {
					device.setDisplayMode(best);
					System.out.println("[FS] Exclusive display mode: "
							+ best.getWidth() + "x" + best.getHeight()
							+ " @" + best.getRefreshRate());
				} else {
					System.out.println("[FS] Exclusive using current mode: "
							+ device.getDisplayMode().getWidth() + "x"
							+ device.getDisplayMode().getHeight());
				}
			} catch (Exception e) {
				System.out.println("[FS] Could not set display mode, using current: " + e.getMessage());
			}

			GameShell.frame.validate();

			GameShell.exclusiveFullscreenActive = true;
			GameShell.borderlessFullscreenActive = false;
			GameShell.fullScreenFrame = GameShell.frame;
			GameShell.replaceCanvas = true;

			// Canvas/layout: use frame size after FS (AWT may still report scaled numbers)
			Rectangle bounds = GameShell.frame.getBounds();
			int storeW = bounds.width > 0 ? bounds.width : wantW;
			int storeH = bounds.height > 0 ? bounds.height : wantH;
			Preferences.fullScreenWidth = storeW;
			Preferences.fullScreenHeight = storeH;
			Preferences.write(GameShell.signLink);

			System.out.println("[FS] Entered exclusive fullscreen: " + storeW + "x" + storeH
					+ " (native target " + wantW + "x" + wantH + ")");
		} catch (Exception e) {
			e.printStackTrace();
			GameShell.exclusiveFullscreenActive = false;
			GameShell.fullScreenFrame = null;
			try {
				GraphicsDevice device = GameShell.frame.getGraphicsConfiguration().getDevice();
				device.setFullScreenWindow(null);
			} catch (Exception ignored) {
			}
		}
	}

	private static void exitExclusiveFullscreen() {
		if (GameShell.frame == null) {
			GameShell.exclusiveFullscreenActive = false;
			GameShell.fullScreenFrame = null;
			return;
		}

		try {
			if (GameShell.canvas != null) {
				try {
					Keyboard.stop(GameShell.canvas);
					Mouse.stop(GameShell.canvas);
					if (client.mouseWheel != null) {
						client.mouseWheel.stop(GameShell.canvas);
					}
					if (GameShell.canvas.getParent() != null) {
						GameShell.canvas.getParent().remove(GameShell.canvas);
					}
				} catch (Exception ignored) {
				}
			}

			// Leave exclusive mode first
			try {
				GraphicsDevice device = GameShell.frame.getGraphicsConfiguration().getDevice();
				device.setFullScreenWindow(null);
			} catch (Exception e) {
				System.out.println("[FS] setFullScreenWindow(null) failed: " + e);
			}

			int w = GameShell.windowedFrameWidth > 0 ? GameShell.windowedFrameWidth : 1024;
			int h = GameShell.windowedFrameHeight > 0 ? GameShell.windowedFrameHeight : 768;

			// Same peer rebuild as borderless exit — required to restore decorations on Windows
			GameShell.frame.setVisible(false);
			GameShell.frame.dispose();

			GameShell.frame.setUndecorated(false);

			// Rough size first so insets become non-zero after show
			GameShell.frame.setSize(w + 16, h + 39);
			if (GameShell.windowedFrameX > 0 || GameShell.windowedFrameY > 0) {
				GameShell.frame.setLocation(GameShell.windowedFrameX, GameShell.windowedFrameY);
			} else {
				GameShell.frame.setLocationRelativeTo(null);
			}

			GameShell.frame.setVisible(true);
			GameShell.frame.validate();

			Insets insets = GameShell.frame.getInsets();
			GameShell.frame.setSize(
					insets.left + w + insets.right,
					insets.top + h + insets.bottom
			);
			if (GameShell.windowedFrameX > 0 || GameShell.windowedFrameY > 0) {
				GameShell.frame.setLocation(GameShell.windowedFrameX, GameShell.windowedFrameY);
			}

			// Force above other apps (toFront alone is ignored a lot on Windows)
			try {
				GameShell.frame.setAlwaysOnTop(true);
				GameShell.frame.toFront();
				GameShell.frame.requestFocus();
				if (GameShell.canvas != null) {
					GameShell.canvas.requestFocus();
				}
				GameShell.frame.setAlwaysOnTop(false);
			} catch (Exception ignored) {}

			//GameShell.frame.toFront();



			GameShell.exclusiveFullscreenActive = false;
			GameShell.fullScreenFrame = null;
			GameShell.replaceCanvas = true;

			System.out.println("[FS] Exited exclusive fullscreen, restored " + w + "x" + h
					+ " at " + GameShell.windowedFrameX + "," + GameShell.windowedFrameY
					+ " undecorated=" + GameShell.frame.isUndecorated());
		} catch (Exception e) {
			e.printStackTrace();
			GameShell.exclusiveFullscreenActive = false;
			GameShell.fullScreenFrame = null;
		}
	}

	/*private static void exitExclusiveFullscreen() {
		if (GameShell.frame == null) {
			GameShell.exclusiveFullscreenActive = false;
			GameShell.fullScreenFrame = null;
			return;
		}

		try {
			if (GameShell.canvas != null) {
				try {
					Keyboard.stop(GameShell.canvas);
					Mouse.stop(GameShell.canvas);
					if (client.mouseWheel != null) {
						client.mouseWheel.stop(GameShell.canvas);
					}
					if (GameShell.canvas.getParent() != null) {
						GameShell.canvas.getParent().remove(GameShell.canvas);
					}
				} catch (Exception ignored) {
				}
			}

			GraphicsDevice device = GameShell.frame.getGraphicsConfiguration().getDevice();
			device.setFullScreenWindow(null);

			int w = GameShell.windowedFrameWidth > 0 ? GameShell.windowedFrameWidth : 1024;
			int h = GameShell.windowedFrameHeight > 0 ? GameShell.windowedFrameHeight : 768;

			GameShell.frame.setSize(w + 16, h + 39);
			if (GameShell.windowedFrameX > 0 || GameShell.windowedFrameY > 0) {
				GameShell.frame.setLocation(GameShell.windowedFrameX, GameShell.windowedFrameY);
			} else {
				GameShell.frame.setLocationRelativeTo(null);
			}
			GameShell.frame.setVisible(true);
			GameShell.frame.validate();

			Insets insets = GameShell.frame.getInsets();
			GameShell.frame.setSize(insets.left + w + insets.right, insets.top + h + insets.bottom);

			GameShell.exclusiveFullscreenActive = false;
			GameShell.fullScreenFrame = null;
			GameShell.replaceCanvas = true;

			System.out.println("[FS] Exited exclusive fullscreen, restored " + w + "x" + h);
		} catch (Exception e) {
			e.printStackTrace();
			GameShell.exclusiveFullscreenActive = false;
			GameShell.fullScreenFrame = null;
		}
	}*/





	@OriginalMember(owner = "client!ab", name = "c", descriptor = "(B)[Lclient!od;")
	public static DisplayMode[] getDisplayModes() {
		if (aClass114Array1 == null) {
			DisplayMode[] local16 = method3558(GameShell.signLink);
			DisplayMode[] local20 = new DisplayMode[local16.length];
			int local22 = 0;

			// Native size of the default screen (cap the list)
			int maxW = Integer.MAX_VALUE;
			int maxH = Integer.MAX_VALUE;
			try {
				java.awt.DisplayMode nativeDm = GraphicsEnvironment
						.getLocalGraphicsEnvironment()
						.getDefaultScreenDevice()
						.getDisplayMode();
				maxW = nativeDm.getWidth();
				maxH = nativeDm.getHeight();
			} catch (Exception ignored) {
			}

			label52:
			for (int local24 = 0; local24 < local16.length; local24++) {
				DisplayMode local32 = local16[local24];

				if (local32.bitDepth > 0 && local32.bitDepth < 24) {
					continue;
				}
				if (local32.width < 1024 || local32.height < 768) {
					continue;
				}
				// no larger than native desktop
				if (local32.width > maxW || local32.height > maxH) {
					continue;
				}

				for (int local52 = 0; local52 < local22; local52++) {
					DisplayMode local59 = local20[local52];
					if (local32.width == local59.width && local59.height == local32.height) {
						if (local32.bitDepth > local59.bitDepth) {
							local20[local52] = local32;
						}
						continue label52;
					}
				}
				local20[local22] = local32;
				local22++;
			}

			aClass114Array1 = new DisplayMode[local22];
			ArrayUtils.copy(local20, 0, aClass114Array1, 0, local22);

			int[] local112 = new int[aClass114Array1.length];
			for (int local114 = 0; local114 < aClass114Array1.length; local114++) {
				DisplayMode local122 = aClass114Array1[local114];
				local112[local114] = local122.height * local122.width;
			}
			ArrayUtils.sort(local112, aClass114Array1);
		}
		return aClass114Array1;
		/*if (aClass114Array1 == null) {
			@Pc(16) DisplayMode[] local16 = method3558(GameShell.signLink);
			@Pc(20) DisplayMode[] local20 = new DisplayMode[local16.length];
			@Pc(22) int local22 = 0;
			label52:
			for (@Pc(24) int local24 = 0; local24 < local16.length; local24++) {
				@Pc(32) DisplayMode local32 = local16[local24];
				// Filter to minimum 1024x768 to avoid mouse coordinate issues and poor quality on modern monitors
				if ((local32.bitDepth <= 0 || local32.bitDepth >= 24) && local32.width >= 1024 && local32.height >= 768) {
					for (@Pc(52) int local52 = 0; local52 < local22; local52++) {
						@Pc(59) DisplayMode local59 = local20[local52];
						if (local32.width == local59.width && local59.height == local32.height) {
							if (local32.bitDepth > local59.bitDepth) {
								local20[local52] = local32;
							}
							continue label52;
						}
					}
					local20[local22] = local32;
					local22++;
				}
			}
			aClass114Array1 = new DisplayMode[local22];
			ArrayUtils.copy(local20, 0, aClass114Array1, 0, local22);
			@Pc(112) int[] local112 = new int[aClass114Array1.length];
			for (@Pc(114) int local114 = 0; local114 < aClass114Array1.length; local114++) {
				@Pc(122) DisplayMode local122 = aClass114Array1[local114];
				local112[local114] = local122.height * local122.width;
			}
			ArrayUtils.sort(local112, aClass114Array1);
		}
		return aClass114Array1;*/
	}

	@OriginalMember(owner = "client!pm", name = "a", descriptor = "(ILsignlink!ll;)[Lclient!od;")
	public static DisplayMode[] method3558(@OriginalArg(1) SignLink arg0) {
		if (!arg0.isFullScreenSupported()) {
			return new DisplayMode[0];
		}
		@Pc(17) PrivilegedRequest local17 = arg0.getDisplayModes();
		while (local17.status == 0) {
			ThreadUtils.sleep(10L);
		}
		if (local17.status == 2) {
			return new DisplayMode[0];
		}
		@Pc(39) int[] local39 = (int[]) local17.result;
		@Pc(45) DisplayMode[] local45 = new DisplayMode[local39.length >> 2];
		for (@Pc(47) int local47 = 0; local47 < local45.length; local47++) {
			@Pc(59) DisplayMode local59 = new DisplayMode();
			local45[local47] = local59;
			local59.width = local39[local47 << 2];
			local59.height = local39[(local47 << 2) + 1];
			local59.bitDepth = local39[(local47 << 2) + 2];
			local59.refreshRate = local39[(local47 << 2) + 3];
		}
		return local45;
	}

	@OriginalMember(owner = "client!nf", name = "a", descriptor = "(IIIIILsignlink!ll;)Ljava/awt/Frame;")
	public static Frame method3176(@OriginalArg(2) int arg0, @OriginalArg(3) int arg1, @OriginalArg(4) int arg2, @OriginalArg(5) SignLink arg3) {
		if (!arg3.isFullScreenSupported()) {
			return null;
		}
		@Pc(20) DisplayMode[] local20 = method3558(arg3);
		if (local20 == null) {
			return null;
		}
		@Pc(27) boolean local27 = false;
		for (@Pc(29) int local29 = 0; local29 < local20.length; local29++) {
			if (arg2 == local20[local29].width && arg1 == local20[local29].height && (!local27 || local20[local29].bitDepth > arg0)) {
				arg0 = local20[local29].bitDepth;
				local27 = true;
			}
		}
		if (!local27) {
			return null;
		}
		@Pc(90) PrivilegedRequest local90 = arg3.enterFullScreen(arg0, arg1, arg2);
		while (local90.status == 0) {
			ThreadUtils.sleep(10L);
		}
		@Pc(103) Frame local103 = (Frame) local90.result;
		if (local103 == null) {
			return null;
		} else if (local90.status == 2) {
			exitFullScreen(local103, arg3);
			return null;
		} else {
			return local103;
		}
	}

}
