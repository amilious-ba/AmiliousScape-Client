package rt4.render;

import com.jogamp.opengl.GL2;
import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;
import rt4.core.Preferences;

import java.nio.ByteBuffer;

@OriginalClass("client!vm")
public final class SpecularMaterialRenderer implements MaterialRenderer {

	@OriginalMember(owner = "client!vm", name = "a", descriptor = "I")
	private int anInt5777 = -1;

	@OriginalMember(owner = "client!vm", name = "c", descriptor = "[I")
	private int[] anIntArray519 = null;

	@OriginalMember(owner = "client!vm", name = "b", descriptor = "Z")
	private boolean aBoolean301 = false;

	@OriginalMember(owner = "client!vm", name = "<init>", descriptor = "()V")
	public SpecularMaterialRenderer() {
		if (GlRenderer.arbTextureCubeMapSupported && GlRenderer.maxTextureUnits >= 2) {
			this.method4536();
			GlRenderer.api.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, this.anIntArray519[0]);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_EDGE);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
			GlRenderer.api.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, this.anIntArray519[1]);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_EDGE);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
			GlRenderer.api.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, this.anIntArray519[2]);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP_TO_EDGE);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			GlRenderer.api.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
			this.aBoolean301 = GlRenderer.maxTextureUnits < 3;
		}
		this.method4535();
	}

	@OriginalMember(owner = "client!vm", name = "d", descriptor = "()V")
	private void method4535() {
		this.anInt5777 = GlRenderer.api.glGenLists(2);
		GlRenderer.api.glNewList(this.anInt5777, GL2.GL_COMPILE);
		if (this.anIntArray519 == null) {
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_PRIMARY_COLOR);
		} else {
			GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE1);
			GlRenderer.api.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);
			GlRenderer.api.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);
			GlRenderer.api.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);
			GlRenderer.api.glEnable(GL2.GL_TEXTURE_GEN_S);
			GlRenderer.api.glEnable(GL2.GL_TEXTURE_GEN_T);
			GlRenderer.api.glEnable(GL2.GL_TEXTURE_GEN_R);
			GlRenderer.api.glEnable(GL2.GL_TEXTURE_CUBE_MAP);
			GlRenderer.api.glMatrixMode(GL2.GL_TEXTURE);
			GlRenderer.api.glLoadIdentity();
			GlRenderer.api.glRotatef(22.5F, 1.0F, 0.0F, 0.0F);
			GlRenderer.api.glMatrixMode(GL2.GL_MODELVIEW);
			if (this.aBoolean301) {
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_ADD);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_RGB, GL2.GL_SRC_ALPHA);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_REPLACE);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_PRIMARY_COLOR);
			} else {
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_REPLACE);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_PREVIOUS);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_MODULATE);
				GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE2);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_ADD);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_PREVIOUS);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC1_RGB, GL2.GL_PREVIOUS);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND1_RGB, GL2.GL_SRC_ALPHA);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_REPLACE);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_PRIMARY_COLOR);
				GlRenderer.api.glBindTexture(GL2.GL_TEXTURE_2D, GlRenderer.anInt5328);
				GlRenderer.api.glEnable(GL2.GL_TEXTURE_2D);
			}
			GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE0);
		}
		GlRenderer.api.glEndList();
		GlRenderer.api.glNewList(this.anInt5777 + 1, GL2.GL_COMPILE);
		if (this.anIntArray519 == null) {
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_TEXTURE);
		} else {
			GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE1);
			GlRenderer.api.glDisable(GL2.GL_TEXTURE_GEN_S);
			GlRenderer.api.glDisable(GL2.GL_TEXTURE_GEN_T);
			GlRenderer.api.glDisable(GL2.GL_TEXTURE_GEN_R);
			GlRenderer.api.glDisable(GL2.GL_TEXTURE_CUBE_MAP);
			GlRenderer.api.glMatrixMode(GL2.GL_TEXTURE);
			GlRenderer.api.glLoadIdentity();
			GlRenderer.api.glMatrixMode(GL2.GL_MODELVIEW);
			if (this.aBoolean301) {
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_MODULATE);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND0_RGB, GL2.GL_SRC_COLOR);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_MODULATE);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_TEXTURE);
			} else {
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_MODULATE);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_TEXTURE);
				GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE2);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_MODULATE);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_TEXTURE);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_OPERAND1_RGB, GL2.GL_SRC_COLOR);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_MODULATE);
				GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_TEXTURE);
				GlRenderer.api.glDisable(GL2.GL_TEXTURE_2D);
			}
			GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE0);
		}
		GlRenderer.api.glEndList();
	}

	@OriginalMember(owner = "client!vm", name = "a", descriptor = "()V")
	@Override
	public final void unbind() {
		if (Preferences.highDetailLighting) {
			GlRenderer.api.glCallList(this.anInt5777 + 1);
		} else {
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_TEXTURE);
		}
	}

	@OriginalMember(owner = "client!vm", name = "c", descriptor = "()I")
	@Override
	public final int getFlags() {
		return 4;
	}

	@OriginalMember(owner = "client!vm", name = "b", descriptor = "()V")
	@Override
	public final void bind() {
		GlRenderer.setTextureCombineAlphaMode(1);
		if (Preferences.highDetailLighting) {
			GlRenderer.api.glCallList(this.anInt5777);
		} else {
			GlRenderer.api.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_PRIMARY_COLOR);
		}
	}

	@OriginalMember(owner = "client!vm", name = "a", descriptor = "(I)V")
	@Override
	public final void setArgument(@OriginalArg(0) int arg0) {
		if (Preferences.highDetailLighting && this.anIntArray519 != null) {
			GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE1);
			GlRenderer.api.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, this.anIntArray519[arg0 - 1]);
			GlRenderer.api.glActiveTexture(GL2.GL_TEXTURE0);
		}
	}

	@OriginalMember(owner = "client!vm", name = "e", descriptor = "()V")
	private void method4536() {
		if (this.anIntArray519 == null) {
			this.anIntArray519 = new int[3];
			GlRenderer.api.glGenTextures(3, this.anIntArray519, 0);
		}
		@Pc(19) byte[] local19 = new byte[4096];
		@Pc(22) byte[] local22 = new byte[4096];
		@Pc(25) byte[] local25 = new byte[4096];
		for (@Pc(27) int local27 = 0; local27 < 6; local27++) {
			@Pc(32) int local32 = 0;
			for (@Pc(34) int local34 = 0; local34 < 64; local34++) {
				for (@Pc(39) int local39 = 0; local39 < 64; local39++) {
					@Pc(51) float local51 = (float) local39 * 2.0F / 64.0F - 1.0F;
					@Pc(60) float local60 = (float) local34 * 2.0F / 64.0F - 1.0F;
					@Pc(75) float local75 = (float) (1.0D / Math.sqrt(local51 * local51 + local60 * local60 + 1.0F));
					@Pc(79) float local79 = local51 * local75;
					@Pc(83) float local83 = local60 * local75;
					@Pc(88) float local88;
					if (local27 == 0) {
						local88 = -local79;
					} else if (local27 == 1) {
						local88 = local79;
					} else if (local27 == 2) {
						local88 = local83;
					} else if (local27 == 3) {
						local88 = -local83;
					} else if (local27 == 4) {
						local88 = local75;
					} else {
						local88 = -local75;
					}
					@Pc(129) int local129;
					@Pc(137) int local137;
					@Pc(145) int local145;
					if (local88 > 0.0F) {
						local129 = (int) (Math.pow(local88, 96.0D) * 255.0D);
						local137 = (int) (Math.pow(local88, 36.0D) * 255.0D);
						local145 = (int) (Math.pow(local88, 12.0D) * 255.0D);
					} else {
						local145 = 0;
						local137 = 0;
						local129 = 0;
					}
					if (GlRenderer.maxTextureUnits < 3) {
						local129 /= 5;
						local137 /= 5;
						local145 /= 5;
					} else {
						local129 /= 2;
						local137 /= 2;
						local145 /= 2;
					}
					local22[local32] = (byte) local129;
					local25[local32] = (byte) local137;
					local19[local32] = (byte) local145;
					local32++;
				}
			}
			GlRenderer.api.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, this.anIntArray519[0]);
			GlRenderer.api.glTexImage2D(local27 + GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL2.GL_ALPHA, 64, 64, 0, GL2.GL_ALPHA, GL2.GL_UNSIGNED_BYTE, ByteBuffer.wrap(local22));
			GlRenderer.api.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, this.anIntArray519[1]);
			GlRenderer.api.glTexImage2D(local27 + GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL2.GL_ALPHA, 64, 64, 0, GL2.GL_ALPHA, GL2.GL_UNSIGNED_BYTE, ByteBuffer.wrap(local25));
			GlRenderer.api.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, this.anIntArray519[2]);
			GlRenderer.api.glTexImage2D(local27 + GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL2.GL_ALPHA, 64, 64, 0, GL2.GL_ALPHA, GL2.GL_UNSIGNED_BYTE, ByteBuffer.wrap(local19));
			GlCleaner.onCardTexture += 12288;
		}
	}
}
