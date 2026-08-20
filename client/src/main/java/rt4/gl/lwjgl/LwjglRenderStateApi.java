package rt4.gl.lwjgl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBFragmentProgram;
import org.lwjgl.opengl.ARBVertexProgram;
import org.lwjgl.opengl.GL11;
import rt4.gl.api.GlRenderStateApi;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * LWJGL implementation of rendering state operations.
 * Simple 1:1 mappings to LWJGL GL11.
 */
public final class LwjglRenderStateApi implements GlRenderStateApi {

	@Override
	public void glShadeModel(int mode) {
		GL11.glShadeModel(mode);
	}

	@Override
	public void glCullFace(int mode) {
		GL11.glCullFace(mode);
	}

	@Override
	public void glPolygonMode(int face, int mode) {
		GL11.glPolygonMode(face, mode);
	}

	@Override
	public void glLineWidth(float width) {
		GL11.glLineWidth(width);
	}

	@Override
	public void glPushAttrib(int mask) {
		GL11.glPushAttrib(mask);
	}

	@Override
	public void glPopAttrib() {
		GL11.glPopAttrib();
	}

	@Override
	public void glDrawBuffer(int mode) {
		GL11.glDrawBuffer(mode);
	}

	@Override
	public void glReadBuffer(int mode) {
		GL11.glReadBuffer(mode);
	}

	@Override
	public void glGenProgramsARB(int n, int[] programs, int offset) {
		IntBuffer buffer = LwjglBufferHelper.toIntBuffer(programs, offset, n);
		ARBVertexProgram.glGenProgramsARB(buffer);
		LwjglBufferHelper.fromIntBuffer(buffer, programs, offset, n);
	}

	@Override
	public void glBindProgramARB(int target, int program) {
		ARBVertexProgram.glBindProgramARB(target, program);
	}

	@Override
	public void glProgramStringARB(int target, int format, int len, String string) {
		ByteBuffer buffer = BufferUtils.createByteBuffer(string.length());
		buffer.put(string.getBytes());
		buffer.flip();
		ARBVertexProgram.glProgramStringARB(target, format, buffer);
	}

	@Override
	public void glProgramLocalParameter4fARB(int target, int index, float x, float y, float z, float w) {
		ARBVertexProgram.glProgramLocalParameter4fARB(target, index, x, y, z, w);
	}

	@Override
	public void glProgramLocalParameter4fvARB(int target, int index, float[] params, int offset) {
		FloatBuffer buffer = LwjglBufferHelper.toFloatBuffer(params, offset, 4);
		ARBVertexProgram.glProgramLocalParameter4fvARB(target, index, buffer);
	}

	@Override
	public void glProgramLocalParameter4fvARB(int target, int index, FloatBuffer params) {
		ARBVertexProgram.glProgramLocalParameter4fvARB(target, index, params);
	}
}
