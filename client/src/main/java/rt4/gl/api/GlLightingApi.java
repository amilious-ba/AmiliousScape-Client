package rt4.gl.api;

import java.nio.FloatBuffer;

/**
 * OpenGL lighting, material, and fog operations.
 * Fixed-function pipeline lighting model.
 */
public interface GlLightingApi {

	// Lighting
	void glLightfv(int light, int pname, float[] params, int offset);
	void glLightfv(int light, int pname, FloatBuffer params);

	void glLightf(int light, int pname, float param);

	void glLightModelfv(int pname, float[] params, int offset);
	void glLightModelfv(int pname, FloatBuffer params);

	// Material
	void glColorMaterial(int face, int mode);

	// Fog
	void glFogi(int pname, int param);
	void glFogf(int pname, float param);
	void glFogfv(int pname, float[] params, int offset);
	void glFogfv(int pname, FloatBuffer params);

	void glHint(int target, int mode);
}
