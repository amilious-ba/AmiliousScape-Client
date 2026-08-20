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

	// ARB vertex/fragment programs (advanced features)
	void glGenProgramsARB(int n, int[] programs, int offset);
	void glBindProgramARB(int target, int program);
	void glProgramStringARB(int target, int format, int len, String string);
	void glProgramLocalParameter4fARB(int target, int index, float x, float y, float z, float w);
	void glProgramLocalParameter4fvARB(int target, int index, float[] params, int offset);
	void glProgramLocalParameter4fvARB(int target, int index, java.nio.FloatBuffer params);
}
