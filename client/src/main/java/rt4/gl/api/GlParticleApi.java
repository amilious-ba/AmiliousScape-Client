package rt4.gl.api;

import java.nio.FloatBuffer;

/**
 * OpenGL particle/point sprite operations (ARB extensions).
 */
public interface GlParticleApi {

	void glPointParameterfv(int pname, float[] params, int offset);
	void glPointParameterfv(int pname, FloatBuffer params);

	void glPointParameterf(int pname, float param);
}
