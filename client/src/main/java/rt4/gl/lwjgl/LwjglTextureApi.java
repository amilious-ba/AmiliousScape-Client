package rt4.gl.lwjgl;

import org.lwjgl.opengl.ARBTextureStorage;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import rt4.gl.api.GlTextureApi;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * LWJGL implementation of texture operations.
 * This is one of the most complex APIs due to many method variants and buffer handling.
 */
public final class LwjglTextureApi implements GlTextureApi {

	// ============================================================
	// Binding & Generation
	// ============================================================

	@Override
	public void glGenTextures(int n, int[] textures, int offset) {
		// JOGL: accepts array + offset
		// LWJGL: requires IntBuffer
		IntBuffer buffer = LwjglBufferHelper.toIntBuffer(new int[n]);
		GL11.glGenTextures(buffer);
		// Extract generated IDs back to array
		LwjglBufferHelper.fromIntBuffer(buffer, textures, offset, n);
	}

	@Override
	public void glGenTextures(int n, IntBuffer textures) {
		// Already a buffer - ensure it's ready to write
		textures.clear();
		GL11.glGenTextures(textures);
	}

	@Override
	public void glBindTexture(int target, int texture) {
		GL11.glBindTexture(target, texture);
	}

	@Override
	public void glDeleteTextures(int n, int[] textures, int offset) {
		// JOGL: accepts array + offset
		// LWJGL: requires IntBuffer
		GL11.glDeleteTextures(LwjglBufferHelper.toIntBuffer(textures, offset, n));
	}

	@Override
	public void glDeleteTextures(int n, IntBuffer textures) {
		// Already a buffer - prepare for reading
		GL11.glDeleteTextures(LwjglBufferHelper.prepareIntBuffer(textures));
	}

	// ============================================================
	// Image Data
	// ============================================================

	@Override
	public void glTexImage2D(int target, int level, int internalFormat, int width, int height,
	                         int border, int format, int type, Buffer pixels) {
		// LWJGL requires DIRECT buffers for pixel data (not heap buffers)
		if (pixels == null) {
			GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, (ByteBuffer) null);
		} else if (pixels instanceof IntBuffer) {
			// Handle IntBuffer separately - LWJGL has dedicated overload
			IntBuffer intBuffer = (IntBuffer) pixels;
			if (!intBuffer.isDirect()) {
				IntBuffer direct = org.lwjgl.BufferUtils.createIntBuffer(intBuffer.remaining());
				direct.put(intBuffer);
				direct.flip();
				intBuffer.rewind();
				GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, direct);
			} else {
				GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, intBuffer);
			}
		} else if (pixels instanceof ByteBuffer) {
			ByteBuffer byteBuffer = (ByteBuffer) pixels;
			// If it's a heap buffer, convert to direct buffer (LWJGL requirement)
			if (!byteBuffer.isDirect()) {
				ByteBuffer direct = org.lwjgl.BufferUtils.createByteBuffer(byteBuffer.remaining());
				direct.put(byteBuffer);
				direct.flip();
				byteBuffer.rewind(); // Restore original buffer position
				GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, direct);
			} else {
				GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, byteBuffer);
			}
		} else {
			// Fallback for other buffer types
			throw new IllegalArgumentException("Unsupported buffer type: " + pixels.getClass().getName());
		}
	}

	@Override
	public void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth,
	                         int border, int format, int type, Buffer pixels) {
		// LWJGL GL12 for 3D textures - requires direct buffers
		if (pixels == null) {
			GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, (ByteBuffer) null);
		} else if (pixels instanceof ByteBuffer) {
			ByteBuffer byteBuffer = (ByteBuffer) pixels;
			if (!byteBuffer.isDirect()) {
				ByteBuffer direct = org.lwjgl.BufferUtils.createByteBuffer(byteBuffer.remaining());
				direct.put(byteBuffer);
				direct.flip();
				byteBuffer.rewind();
				GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, direct);
			} else {
				GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, byteBuffer);
			}
		} else {
			ByteBuffer byteBuffer = (ByteBuffer) pixels;
			if (!byteBuffer.isDirect()) {
				ByteBuffer direct = org.lwjgl.BufferUtils.createByteBuffer(byteBuffer.remaining());
				direct.put(byteBuffer);
				direct.flip();
				byteBuffer.rewind();
				GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, direct);
			} else {
				GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, byteBuffer);
			}
		}
	}

	@Override
	public void glTexImage1D(int target, int level, int internalFormat, int width,
	                         int border, int format, int type, Buffer pixels) {
		// LWJGL GL11 for 1D textures - requires direct buffers
		if (pixels == null) {
			GL11.glTexImage1D(target, level, internalFormat, width, border, format, type, (ByteBuffer) null);
		} else if (pixels instanceof ByteBuffer) {
			ByteBuffer byteBuffer = (ByteBuffer) pixels;
			if (!byteBuffer.isDirect()) {
				ByteBuffer direct = org.lwjgl.BufferUtils.createByteBuffer(byteBuffer.remaining());
				direct.put(byteBuffer);
				direct.flip();
				byteBuffer.rewind();
				GL11.glTexImage1D(target, level, internalFormat, width, border, format, type, direct);
			} else {
				GL11.glTexImage1D(target, level, internalFormat, width, border, format, type, byteBuffer);
			}
		} else {
			ByteBuffer byteBuffer = (ByteBuffer) pixels;
			if (!byteBuffer.isDirect()) {
				ByteBuffer direct = org.lwjgl.BufferUtils.createByteBuffer(byteBuffer.remaining());
				direct.put(byteBuffer);
				direct.flip();
				byteBuffer.rewind();
				GL11.glTexImage1D(target, level, internalFormat, width, border, format, type, direct);
			} else {
				GL11.glTexImage1D(target, level, internalFormat, width, border, format, type, byteBuffer);
			}
		}
	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
	                            int format, int type, Buffer pixels) {
		// LWJGL requires direct buffers for pixel data
		if (pixels instanceof ByteBuffer) {
			ByteBuffer byteBuffer = (ByteBuffer) pixels;
			if (!byteBuffer.isDirect()) {
				ByteBuffer direct = org.lwjgl.BufferUtils.createByteBuffer(byteBuffer.remaining());
				direct.put(byteBuffer);
				direct.flip();
				byteBuffer.rewind();
				GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, direct);
			} else {
				GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, byteBuffer);
			}
		} else {
			ByteBuffer byteBuffer = (ByteBuffer) pixels;
			if (!byteBuffer.isDirect()) {
				ByteBuffer direct = org.lwjgl.BufferUtils.createByteBuffer(byteBuffer.remaining());
				direct.put(byteBuffer);
				direct.flip();
				byteBuffer.rewind();
				GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, direct);
			} else {
				GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, byteBuffer);
			}
		}
	}

	@Override
	public void glTexStorage2D(int target, int levels, int internalFormat, int width, int height) {
		// Modern OpenGL - may not be available
		// Try ARB extension first
		try {
			ARBTextureStorage.glTexStorage2D(target, levels, internalFormat, width, height);
		} catch (Exception e) {
			// Not supported - silently ignore
			// Game will fall back to glTexImage2D
		}
	}

	@Override
	public void glGenerateMipmap(int target) {
		// Use EXT_framebuffer_object extension
		try {
			EXTFramebufferObject.glGenerateMipmapEXT(target);
		} catch (Exception e) {
			// Not supported - silently ignore
		}
	}

	// ============================================================
	// Parameters
	// ============================================================

	@Override
	public void glTexParameteri(int target, int pname, int param) {
		GL11.glTexParameteri(target, pname, param);
	}

	@Override
	public void glTexParameterf(int target, int pname, float param) {
		GL11.glTexParameterf(target, pname, param);
	}

	// ============================================================
	// Environment
	// ============================================================

	@Override
	public void glTexEnvi(int target, int pname, int param) {
		GL11.glTexEnvi(target, pname, param);
	}

	@Override
	public void glTexEnvf(int target, int pname, float param) {
		GL11.glTexEnvf(target, pname, param);
	}

	@Override
	public void glTexEnvfv(int target, int pname, float[] params, int offset) {
		// JOGL: accepts array + offset
		// LWJGL: requires FloatBuffer
		// Texture environment parameters are typically 4 floats (RGBA color)
		GL11.glTexEnvfv(target, pname, LwjglBufferHelper.toFloatBuffer(params, offset, 4));
	}

	@Override
	public void glTexEnvfv(int target, int pname, FloatBuffer params) {
		// Already a buffer - prepare for reading
		GL11.glTexEnvfv(target, pname, LwjglBufferHelper.prepareFloatBuffer(params));
	}

	// ============================================================
	// Coordinates & Generation
	// ============================================================

	@Override
	public void glTexCoord2f(float s, float t) {
		GL11.glTexCoord2f(s, t);
	}

	@Override
	public void glMultiTexCoord2f(int target, float s, float t) {
		GL13.glMultiTexCoord2f(target, s, t);
	}

	@Override
	public void glTexGeni(int coord, int pname, int param) {
		GL11.glTexGeni(coord, pname, param);
	}

	@Override
	public void glTexGenfv(int coord, int pname, float[] params, int offset) {
		// JOGL: accepts array + offset
		// LWJGL: requires FloatBuffer
		// Texture generation parameters are typically 4 floats (plane equation)
		GL11.glTexGenfv(coord, pname, LwjglBufferHelper.toFloatBuffer(params, offset, 4));
	}

	@Override
	public void glTexGenfv(int coord, int pname, FloatBuffer params) {
		// Already a buffer - prepare for reading
		GL11.glTexGenfv(coord, pname, LwjglBufferHelper.prepareFloatBuffer(params));
	}

	// ============================================================
	// Multitexture
	// ============================================================

	@Override
	public void glActiveTexture(int texture) {
		GL13.glActiveTexture(texture);
	}

	@Override
	public void glClientActiveTexture(int texture) {
		GL13.glClientActiveTexture(texture);
	}
}
