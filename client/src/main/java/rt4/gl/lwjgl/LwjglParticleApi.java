package rt4.gl.lwjgl;

import org.lwjgl.opengl.GL14;
import rt4.gl.api.GlParticleApi;

import java.nio.FloatBuffer;

/**
 * LWJGL implementation of particle/point sprite operations (ARB extensions).
 * Uses LwjglBufferHelper for array→buffer conversion.
 *
 * Note: Point parameters are in GL14 (OpenGL 1.4) in LWJGL.
 */
public final class LwjglParticleApi implements GlParticleApi {

	@Override
	public void glPointParameterfv(int pname, float[] params, int offset) {
		// JOGL: accepts array + offset
		// LWJGL: requires FloatBuffer
		// Point parameters are typically 3 floats (distance attenuation: constant, linear, quadratic)
		GL14.glPointParameterfv(pname, LwjglBufferHelper.toFloatBuffer(params, offset, 3));
	}

	@Override
	public void glPointParameterfv(int pname, FloatBuffer params) {
		// Already a buffer - just ensure it's ready to read
		GL14.glPointParameterfv(pname, LwjglBufferHelper.prepareFloatBuffer(params));
	}

	@Override
	public void glPointParameterf(int pname, float param) {
		GL14.glPointParameterf(pname, param);
	}
}
