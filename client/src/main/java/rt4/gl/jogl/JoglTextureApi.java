package rt4.gl.jogl;

import com.jogamp.opengl.GL2;
import rt4.gl.api.GlTextureApi;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * JOGL implementation of texture operations.
 */
public final class JoglTextureApi implements GlTextureApi {

	private final GL2 gl;

	public JoglTextureApi(GL2 gl) {
		this.gl = gl;
	}

	// Binding & Generation

	@Override
	public void glGenTextures(int n, int[] textures, int offset) {
		gl.glGenTextures(n, textures, offset);
	}

	@Override
	public void glGenTextures(int n, IntBuffer textures) {
		gl.glGenTextures(n, textures);
	}

	@Override
	public void glBindTexture(int target, int texture) {
		gl.glBindTexture(target, texture);
	}

	@Override
	public void glDeleteTextures(int n, int[] textures, int offset) {
		gl.glDeleteTextures(n, textures, offset);
	}

	@Override
	public void glDeleteTextures(int n, IntBuffer textures) {
		gl.glDeleteTextures(n, textures);
	}

	// Image Data

	@Override
	public void glTexImage2D(int target, int level, int internalFormat, int width, int height,
	                         int border, int format, int type, Buffer pixels) {
		gl.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
	}

	@Override
	public void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth,
	                         int border, int format, int type, Buffer pixels) {
		gl.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
	}

	@Override
	public void glTexImage1D(int target, int level, int internalFormat, int width,
	                         int border, int format, int type, Buffer pixels) {
		gl.glTexImage1D(target, level, internalFormat, width, border, format, type, pixels);
	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
	                            int format, int type, Buffer pixels) {
		gl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	@Override
	public void glTexStorage2D(int target, int levels, int internalFormat, int width, int height) {
		if (gl.isFunctionAvailable("glTexStorage2D")) {
			gl.glTexStorage2D(target, levels, internalFormat, width, height);
		}
	}

	@Override
	public void glGenerateMipmap(int target) {
		gl.glGenerateMipmap(target);
	}

	// Parameters

	@Override
	public void glTexParameteri(int target, int pname, int param) {
		gl.glTexParameteri(target, pname, param);
	}

	@Override
	public void glTexParameterf(int target, int pname, float param) {
		gl.glTexParameterf(target, pname, param);
	}

	// Environment

	@Override
	public void glTexEnvi(int target, int pname, int param) {
		gl.glTexEnvi(target, pname, param);
	}

	@Override
	public void glTexEnvf(int target, int pname, float param) {
		gl.glTexEnvf(target, pname, param);
	}

	@Override
	public void glTexEnvfv(int target, int pname, float[] params, int offset) {
		gl.glTexEnvfv(target, pname, params, offset);
	}

	@Override
	public void glTexEnvfv(int target, int pname, FloatBuffer params) {
		gl.glTexEnvfv(target, pname, params);
	}

	// Coordinates & Generation

	@Override
	public void glTexCoord2f(float s, float t) {
		gl.glTexCoord2f(s, t);
	}

	@Override
	public void glMultiTexCoord2f(int target, float s, float t) {
		gl.glMultiTexCoord2f(target, s, t);
	}

	@Override
	public void glTexGeni(int coord, int pname, int param) {
		gl.glTexGeni(coord, pname, param);
	}

	@Override
	public void glTexGenfv(int coord, int pname, float[] params, int offset) {
		gl.glTexGenfv(coord, pname, params, offset);
	}

	@Override
	public void glTexGenfv(int coord, int pname, FloatBuffer params) {
		gl.glTexGenfv(coord, pname, params);
	}

	// Multitexture

	@Override
	public void glActiveTexture(int texture) {
		gl.glActiveTexture(texture);
	}

	@Override
	public void glClientActiveTexture(int texture) {
		gl.glClientActiveTexture(texture);
	}
}
