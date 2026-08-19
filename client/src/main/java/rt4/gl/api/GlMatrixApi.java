package rt4.gl.api;

import java.nio.FloatBuffer;

/**
 * OpenGL matrix operations for transformations.
 * Fixed-function pipeline matrix stack management.
 */
public interface GlMatrixApi {

	void glMatrixMode(int mode);
	void glLoadIdentity();
	void glLoadMatrixf(float[] m, int offset);
	void glLoadMatrixf(FloatBuffer m);

	void glPushMatrix();
	void glPopMatrix();

	void glTranslatef(float x, float y, float z);
	void glRotatef(float angle, float x, float y, float z);
	void glScalef(float x, float y, float z);

	void glOrtho(double left, double right, double bottom, double top, double zNear, double zFar);
}
