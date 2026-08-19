package rt4.gl.api;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * OpenGL query operations for state retrieval and extension checking.
 */
public interface GlQueryApi {

	void glGetIntegerv(int pname, int[] params, int offset);
	void glGetIntegerv(int pname, IntBuffer params);

	void glGetFloatv(int pname, float[] params, int offset);
	void glGetFloatv(int pname, FloatBuffer params);

	String glGetString(int name);

	boolean isExtensionAvailable(String extension);

	/**
	 * Set swap interval (VSync control).
	 * 0 = off, 1 = vsync on, -1 = adaptive (if supported)
	 */
	void setSwapInterval(int interval);
}
