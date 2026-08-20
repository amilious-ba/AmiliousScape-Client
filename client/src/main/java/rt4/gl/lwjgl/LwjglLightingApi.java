package rt4.gl.lwjgl;

import org.lwjgl.opengl.GL11;
import rt4.gl.api.GlLightingApi;

import java.nio.FloatBuffer;

/**
 * LWJGL implementation of lighting, material, and fog operations.
 * Uses LwjglBufferHelper for array→buffer conversion.
 */
public final class LwjglLightingApi implements GlLightingApi {

	// ============================================================
	// Lighting
	// ============================================================

	@Override
	public void glLightfv(int light, int pname, float[] params, int offset) {
		// JOGL: accepts array + offset
		// LWJGL: requires FloatBuffer
		// Light parameters are typically 4 floats (position, ambient, diffuse, specular)
		GL11.glLightfv(light, pname, LwjglBufferHelper.toFloatBuffer(params, offset, 4));
	}

	@Override
	public void glLightfv(int light, int pname, FloatBuffer params) {
		// Already a buffer - just ensure it's ready to read
		GL11.glLightfv(light, pname, LwjglBufferHelper.prepareFloatBuffer(params));
	}

	@Override
	public void glLightf(int light, int pname, float param) {
		GL11.glLightf(light, pname, param);
	}

	@Override
	public void glLightModelfv(int pname, float[] params, int offset) {
		// Light model parameters are typically 4 floats
		GL11.glLightModelfv(pname, LwjglBufferHelper.toFloatBuffer(params, offset, 4));
	}

	@Override
	public void glLightModelfv(int pname, FloatBuffer params) {
		GL11.glLightModelfv(pname, LwjglBufferHelper.prepareFloatBuffer(params));
	}

	// ============================================================
	// Material
	// ============================================================

	@Override
	public void glColorMaterial(int face, int mode) {
		GL11.glColorMaterial(face, mode);
	}

	// ============================================================
	// Fog
	// ============================================================

	@Override
	public void glFogi(int pname, int param) {
		GL11.glFogi(pname, param);
	}

	@Override
	public void glFogf(int pname, float param) {
		GL11.glFogf(pname, param);
	}

	@Override
	public void glFogfv(int pname, float[] params, int offset) {
		// Fog color is 4 floats (RGBA)
		GL11.glFogfv(pname, LwjglBufferHelper.toFloatBuffer(params, offset, 4));
	}

	@Override
	public void glFogfv(int pname, FloatBuffer params) {
		GL11.glFogfv(pname, LwjglBufferHelper.prepareFloatBuffer(params));
	}

	@Override
	public void glHint(int target, int mode) {
		GL11.glHint(target, mode);
	}
}
