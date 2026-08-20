package rt4.gl.lwjgl;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.WGLEXTSwapControl;
import rt4.gl.api.GlQueryApi;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * LWJGL implementation of query operations.
 * For array-based queries, we create a temporary buffer, query into it, then extract to array.
 */
public final class LwjglQueryApi implements GlQueryApi {

	@Override
	public void glGetIntegerv(int pname, int[] params, int offset) {
		// JOGL: accepts array + offset
		// LWJGL: requires IntBuffer
		// Create temp buffer, query, extract to array
		IntBuffer buffer = LwjglBufferHelper.toIntBuffer(new int[params.length - offset]);
		GL11.glGetIntegerv(pname, buffer);
		LwjglBufferHelper.fromIntBuffer(buffer, params, offset, params.length - offset);
	}

	@Override
	public void glGetIntegerv(int pname, IntBuffer params) {
		// Already a buffer - ensure it's ready to write
		params.clear();
		GL11.glGetIntegerv(pname, params);
	}

	@Override
	public void glGetFloatv(int pname, float[] params, int offset) {
		// JOGL: accepts array + offset
		// LWJGL: requires FloatBuffer
		// Create temp buffer, query, extract to array
		FloatBuffer buffer = LwjglBufferHelper.toFloatBuffer(new float[params.length - offset]);
		GL11.glGetFloatv(pname, buffer);
		LwjglBufferHelper.fromFloatBuffer(buffer, params, offset, params.length - offset);
	}

	@Override
	public void glGetFloatv(int pname, FloatBuffer params) {
		// Already a buffer - ensure it's ready to write
		params.clear();
		GL11.glGetFloatv(pname, params);
	}

	@Override
	public String glGetString(int name) {
		return GL11.glGetString(name);
	}

	@Override
	public boolean isExtensionAvailable(String extension) {
		// JOGL: GL2.isExtensionAvailable(String)
		// LWJGL 3: GL.getCapabilities().<extension field>
		// For simplicity, check if extension string is in GL_EXTENSIONS
		try {
			return GL.getCapabilities().toString().contains(extension);
		} catch (Exception e) {
			// Fallback: parse GL_EXTENSIONS string
			String extensions = GL11.glGetString(GL11.GL_EXTENSIONS);
			return extensions != null && extensions.contains(extension);
		}
	}

	@Override
	public void setSwapInterval(int interval) {
		// JOGL: gl.setSwapInterval(int)
		// LWJGL: WGLEXTSwapControl.wglSwapIntervalEXT(int) on Windows
		// Note: This is platform-specific. On Windows we use WGL extension.
		// On Linux would need GLX, on Mac would need CGL.
		try {
			WGLEXTSwapControl.wglSwapIntervalEXT(interval);
		} catch (Exception e) {
			// Not supported or not on Windows - ignore
			System.err.println("Warning: setSwapInterval not supported: " + e.getMessage());
		}
	}
}
