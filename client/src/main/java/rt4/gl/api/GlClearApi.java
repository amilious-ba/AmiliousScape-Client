package rt4.gl.api;

/**
 * OpenGL clear, viewport, and scissor operations.
 */
public interface GlClearApi {

	void glClear(int mask);
	void glClearColor(float red, float green, float blue, float alpha);
	void glClearDepth(double depth);

	void glViewport(int x, int y, int width, int height);
	void glScissor(int x, int y, int width, int height);
}
