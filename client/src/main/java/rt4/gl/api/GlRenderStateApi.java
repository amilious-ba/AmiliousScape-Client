package rt4.gl.api;

/**
 * OpenGL rendering state operations including shading, culling, and attributes.
 */
public interface GlRenderStateApi {

	void glShadeModel(int mode);
	void glCullFace(int mode);
	void glPolygonMode(int face, int mode);
	void glLineWidth(float width);

	void glPushAttrib(int mask);
	void glPopAttrib();

	void glDrawBuffer(int mode);
	void glReadBuffer(int mode);
}
