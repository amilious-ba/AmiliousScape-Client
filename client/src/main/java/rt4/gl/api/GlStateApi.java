package rt4.gl.api;

/**
 * OpenGL state management operations.
 * Includes enable/disable for capabilities and client state arrays.
 */
public interface GlStateApi {

	void glEnable(int cap);
	void glDisable(int cap);

	void glEnableClientState(int array);
	void glDisableClientState(int array);
}
