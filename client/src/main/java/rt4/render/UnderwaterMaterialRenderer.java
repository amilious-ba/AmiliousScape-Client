package rt4.render;

import com.jogamp.opengl.GL2;
import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;
import rt4.util.ColorUtils;
import rt4.render.primitive.GlTile;
import rt4.core.GlobalConfig;

import java.nio.ByteBuffer;

@OriginalClass("client!wg")
public final class UnderwaterMaterialRenderer implements MaterialRenderer {

	@OriginalMember(owner = "client!wg", name = "b", descriptor = "Z")
	public static boolean aBoolean308 = false;
	@OriginalMember(owner = "client!nh", name = "Z", descriptor = "I")
	public static int anInt3241 = 128;
	@OriginalMember(owner = "client!wg", name = "c", descriptor = "I")
	private int anInt5805 = -1;

	@OriginalMember(owner = "client!wg", name = "a", descriptor = "[F")
	private final float[] aFloatArray29 = new float[4];

	@OriginalMember(owner = "client!wg", name = "d", descriptor = "I")
	private int anInt5806 = -1;

	@OriginalMember(owner = "client!wg", name = "<init>", descriptor = "()V")
	public UnderwaterMaterialRenderer() {
		if (GlRenderer.maxTextureUnits >= 2) {
			@Pc(17) int[] local17 = new int[1];
			@Pc(20) byte[] local20 = new byte[8];
			@Pc(22) int local22 = 0;
			while (local22 < 8) {
				local20[local22++] = (byte) (local22 * 159 / 8 + 96);
			}
			GlRenderer.api.glGenTextures(1, local17, 0);
			GlRenderer.api.glBindTexture(GL2.GL_TEXTURE_1D, local17[0]);
			GlRenderer.api.glTexImage1D(GL2.GL_TEXTURE_1D, 0, GL2.GL_ALPHA, 8, 0, GL2.GL_ALPHA, GL2.GL_UNSIGNED_BYTE, ByteBuffer.wrap(local20));
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			this.anInt5805 = local17[0];
			aBoolean308 = GlRenderer.maxTextureUnits > 2 && GlRenderer.extTexture3dSupported;
			this.method4606();
		}
	}

	@OriginalMember(owner = "client!wg", name = "e", descriptor = "()I")
	public static int method4607() {
		return aBoolean308 ? 33986 : 33985;
	}

	@OriginalMember(owner = "client!wg", name = "f", descriptor = "()V")
	public static void method4608() {
		GlRenderer.api.glClientActiveTexture(method4607());
		GlRenderer.api.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		GlRenderer.api.glClientActiveTexture(GL2.GL_TEXTURE0);
	}

	@OriginalMember(owner = "client!wg", name = "g", descriptor = "()V")
	public static void method4609() {
		GlRenderer.api.glClientActiveTexture(method4607());
		GlRenderer.api.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		GlRenderer.api.glClientActiveTexture(GL2.GL_TEXTURE0);
	}

	@OriginalMember(owner = "client!mf", name = "a", descriptor = "()V")
	public static void applyFogFade() {
		GlRenderer.api.glDisableClientState(GL2.GL_COLOR_ARRAY);
		GlRenderer.setLightingEnabled(false);
		GlRenderer.api.glDisable(GL2.GL_DEPTH_TEST);
		GlRenderer.api.glPushAttrib(GL2.GL_FOG_BIT);
		GlRenderer.api.glFogf(GL2.GL_FOG_START, (float) GlobalConfig.VIEW_DISTANCE - (GlobalConfig.VIEW_FADE_DISTANCE * 2.0f));
		GlRenderer.disableDepthMask();
		try {
			for (@Pc(19) int i = 0; i < SceneGraph.surfaceHdTiles[0].length; i++) {
				@Pc(31) GlTile tile = SceneGraph.surfaceHdTiles[0][i];
				if (tile.texture >= 0 && Rasteriser.textureProvider.getMaterialType(tile.texture) == MaterialManager.WATER) {
					GlRenderer.api.glColor4fv(ColorUtils.getRgbFloat(tile.underwaterColor), 0);
					@Pc(57) float f = 201.5F - (tile.blend ? 1.0F : 0.5F);
					tile.method1944(SceneGraph.tiles, f, true);
				}
			}
		} catch (Exception ignored) {
		}
		GlRenderer.api.glEnableClientState(GL2.GL_COLOR_ARRAY);
		GlRenderer.restoreLighting();
		GlRenderer.api.glEnable(GL2.GL_DEPTH_TEST);
		GlRenderer.api.glPopAttrib();
		GlRenderer.enableDepthMask();
	}

	@OriginalMember(owner = "client!wg", name = "d", descriptor = "()V")
	private void method4606() {
		this.anInt5806 = GlRenderer.api.glGenLists(2);
		GlRenderer.api.glNewList(this.anInt5806, GL2.GL_COMPILE);
		GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE1);
		if (aBoolean308) {
			GlRenderer.api.glBindTexture(GL2.GL_TEXTURE_3D, MaterialManager.texture3D);
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_ADD);
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_RGB, GL2.GL_SRC_COLOR);
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_REPLACE);
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_PREVIOUS);
			GlRenderer.api.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
			GlRenderer.api.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
			GlRenderer.api.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
			GlRenderer.api.glTexGeni(GL2.GL_Q, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
			GlRenderer.api.glTexGenfv(GL2.GL_Q, GL2.GL_OBJECT_PLANE, new float[]{0.0F, 0.0F, 0.0F, 1.0F}, 0);
			GlRenderer.api.glEnable(GL2.GL_TEXTURE_GEN_S);
			GlRenderer.api.glEnable(GL2.GL_TEXTURE_GEN_T);
			GlRenderer.api.glEnable(GL2.GL_TEXTURE_GEN_R);
			GlRenderer.api.glEnable(GL2.GL_TEXTURE_GEN_Q);
			GlRenderer.api.glEnable(GL2.GL_TEXTURE_3D);
			GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE2);
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
		}
		GlRenderer.api.glBindTexture(GL2.GL_TEXTURE_1D, this.anInt5805);
		GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_INTERPOLATE);
		GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_CONSTANT);
		GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC2_RGB, GL2.GL_TEXTURE);
		GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_REPLACE);
		GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_PREVIOUS);
		GlRenderer.api.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
		GlRenderer.api.glEnable(GL2.GL_TEXTURE_1D);
		GlRenderer.api.glEnable(GL2.GL_TEXTURE_GEN_S);
		GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE0);
		GlRenderer.api.glEndList();
		GlRenderer.api.glNewList(this.anInt5806 + 1, GL2.GL_COMPILE);
		GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE1);
		if (aBoolean308) {
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_MODULATE);
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_RGB, GL2.GL_SRC_COLOR);
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_MODULATE);
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_TEXTURE);
			GlRenderer.api.glDisable(GL2.GL_TEXTURE_GEN_S);
			GlRenderer.api.glDisable(GL2.GL_TEXTURE_GEN_T);
			GlRenderer.api.glDisable(GL2.GL_TEXTURE_GEN_R);
			GlRenderer.api.glDisable(GL2.GL_TEXTURE_GEN_Q);
			GlRenderer.api.glDisable(GL2.GL_TEXTURE_3D);
			GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE2);
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
		}
		GlRenderer.api.glTexEnvfv(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_COLOR, new float[]{0.0F, 1.0F, 0.0F, 1.0F}, 0);
		GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_MODULATE);
		GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_TEXTURE);
		GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC2_RGB, GL2.GL_CONSTANT);
		GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_MODULATE);
		GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_TEXTURE);
		GlRenderer.api.glDisable(GL2.GL_TEXTURE_1D);
		GlRenderer.api.glDisable(GL2.GL_TEXTURE_GEN_S);
		GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE0);
		GlRenderer.api.glEndList();
	}

	@OriginalMember(owner = "client!wg", name = "b", descriptor = "()V")
	@Override
	public final void bind() {
		@Pc(1) GL2 local1 = GlRenderer.gl;
		GlRenderer.api.glCallList(this.anInt5806);
	}

	@OriginalMember(owner = "client!wg", name = "c", descriptor = "()I")
	@Override
	public final int getFlags() {
		return 0;
	}

	@OriginalMember(owner = "client!wg", name = "a", descriptor = "()V")
	@Override
	public final void unbind() {
		@Pc(1) GL2 local1 = GlRenderer.gl;
		GlRenderer.api.glCallList(this.anInt5806 + 1);
	}

	@OriginalMember(owner = "client!wg", name = "a", descriptor = "(I)V")
	@Override
	public final void setArgument(@OriginalArg(0) int arg0) {
		@Pc(1) GL2 local1 = GlRenderer.gl;
		GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE1);
		if (aBoolean308 || arg0 >= 0) {
			GlRenderer.api.glPushMatrix();
			GlRenderer.api.glLoadIdentity();
			GlRenderer.api.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
			GlRenderer.api.glRotatef((float) MaterialManager.anInt5559 * 360.0F / 2048.0F, 1.0F, 0.0F, 0.0F);
			GlRenderer.api.glRotatef((float) MaterialManager.anInt1815 * 360.0F / 2048.0F, 0.0F, 1.0F, 0.0F);
			GlRenderer.api.glTranslatef((float) -MaterialManager.anInt406, (float) -MaterialManager.anInt4675, (float) -MaterialManager.anInt5158);
			if (aBoolean308) {
				this.aFloatArray29[0] = 0.001F;
				this.aFloatArray29[1] = 9.0E-4F;
				this.aFloatArray29[2] = 0.0F;
				this.aFloatArray29[3] = 0.0F;
				GlRenderer.api.glTexGenfv(GL2.GL_S, GL2.GL_EYE_PLANE, this.aFloatArray29, 0);
				this.aFloatArray29[0] = 0.0F;
				this.aFloatArray29[1] = 9.0E-4F;
				this.aFloatArray29[2] = 0.001F;
				this.aFloatArray29[3] = 0.0F;
				GlRenderer.api.glTexGenfv(GL2.GL_T, GL2.GL_EYE_PLANE, this.aFloatArray29, 0);
				this.aFloatArray29[0] = 0.0F;
				this.aFloatArray29[1] = 0.0F;
				this.aFloatArray29[2] = 0.0F;
				this.aFloatArray29[3] = (float) GlRenderer.anInt5323 * 0.005F;
				GlRenderer.api.glTexGenfv(GL2.GL_R, GL2.GL_EYE_PLANE, this.aFloatArray29, 0);
				GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE2);
			}
			local1.glTexEnvfv(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_COLOR, WaterMaterialRenderer.method2422(), 0);
			if (arg0 >= 0) {
				this.aFloatArray29[0] = 0.0F;
				this.aFloatArray29[1] = 1.0F / (float) anInt3241;
				this.aFloatArray29[2] = 0.0F;
				this.aFloatArray29[3] = (float) arg0 * 1.0F / (float) anInt3241;
				GlRenderer.api.glTexGenfv(GL2.GL_S, GL2.GL_EYE_PLANE, this.aFloatArray29, 0);
				GlRenderer.api.glEnable(GL2.GL_TEXTURE_GEN_S);
			} else {
				GlRenderer.api.glDisable(GL2.GL_TEXTURE_GEN_S);
			}
			GlRenderer.api.glPopMatrix();
		} else {
			GlRenderer.api.glDisable(GL2.GL_TEXTURE_GEN_S);
		}
		GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE0);
	}
}
