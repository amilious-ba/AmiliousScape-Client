package rt4.gl.api;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * OpenGL texture operations including binding, generation, parameters,
 * environment settings, coordinate generation, and multitexture.
 */
public interface GlTextureApi {

	// Binding & Generation
	void glGenTextures(int n, int[] textures, int offset);
	void glGenTextures(int n, IntBuffer textures);

	void glBindTexture(int target, int texture);

	void glDeleteTextures(int n, int[] textures, int offset);
	void glDeleteTextures(int n, IntBuffer textures);

	// Image Data
	void glTexImage2D(int target, int level, int internalFormat, int width, int height,
	                  int border, int format, int type, Buffer pixels);

	void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth,
	                  int border, int format, int type, Buffer pixels);

	void glTexImage1D(int target, int level, int internalFormat, int width,
	                  int border, int format, int type, Buffer pixels);

	void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
	                     int format, int type, Buffer pixels);

	// Modern path (optional - may not be available on all systems)
	void glTexStorage2D(int target, int levels, int internalFormat, int width, int height);
	void glGenerateMipmap(int target);

	// Parameters
	void glTexParameteri(int target, int pname, int param);
	void glTexParameterf(int target, int pname, float param);

	// Environment
	void glTexEnvi(int target, int pname, int param);
	void glTexEnvf(int target, int pname, float param);
	void glTexEnvfv(int target, int pname, float[] params, int offset);
	void glTexEnvfv(int target, int pname, FloatBuffer params);

	// Coordinates & Generation
	void glTexCoord2f(float s, float t);
	void glMultiTexCoord2f(int target, float s, float t);

	void glTexGeni(int coord, int pname, int param);
	void glTexGenfv(int coord, int pname, float[] params, int offset);
	void glTexGenfv(int coord, int pname, FloatBuffer params);

	// Multitexture
	void glActiveTexture(int texture);
	void glClientActiveTexture(int texture);
}
