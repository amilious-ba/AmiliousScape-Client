package rt4.gl.lwjgl;

import rt4.gl.GlApi;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * LWJGL implementation of GlApi.
 *
 * This facade delegates to specialized sub-implementations for each API category.
 * Each sub-implementation wraps LWJGL's GL11/GL12/GL13/GL14/ARB extension calls
 * and handles buffer conversions via LwjglBufferHelper.
 *
 * This modular design mirrors the JOGL implementation, making it easy to maintain
 * and debug individual API categories independently.
 */
public final class LwjglGlApi implements GlApi {

	private final LwjglStateApi state;
	private final LwjglClearApi clear;
	private final LwjglMatrixApi matrix;
	private final LwjglTextureApi texture;
	private final LwjglBufferApi buffer;
	private final LwjglDrawApi draw;
	private final LwjglLightingApi lighting;
	private final LwjglColorApi color;
	private final LwjglRenderStateApi renderState;
	private final LwjglQueryApi query;
	private final LwjglParticleApi particle;

	public LwjglGlApi() {
		// No GL2 parameter needed - LWJGL uses static methods
		this.state = new LwjglStateApi();
		this.clear = new LwjglClearApi();
		this.matrix = new LwjglMatrixApi();
		this.texture = new LwjglTextureApi();
		this.buffer = new LwjglBufferApi();
		this.draw = new LwjglDrawApi();
		this.lighting = new LwjglLightingApi();
		this.color = new LwjglColorApi();
		this.renderState = new LwjglRenderStateApi();
		this.query = new LwjglQueryApi();
		this.particle = new LwjglParticleApi();
	}

	// ============================================================
	// State Management
	// ============================================================

	@Override
	public void glEnable(int cap) {
		state.glEnable(cap);
	}

	@Override
	public void glDisable(int cap) {
		state.glDisable(cap);
	}

	@Override
	public void glEnableClientState(int array) {
		state.glEnableClientState(array);
	}

	@Override
	public void glDisableClientState(int array) {
		state.glDisableClientState(array);
	}

	// ============================================================
	// Clear / Viewport / Scissor
	// ============================================================

	@Override
	public void glClear(int mask) {
		clear.glClear(mask);
	}

	@Override
	public void glClearColor(float red, float green, float blue, float alpha) {
		clear.glClearColor(red, green, blue, alpha);
	}

	@Override
	public void glClearDepth(double depth) {
		clear.glClearDepth(depth);
	}

	@Override
	public void glViewport(int x, int y, int width, int height) {
		clear.glViewport(x, y, width, height);
	}

	@Override
	public void glScissor(int x, int y, int width, int height) {
		clear.glScissor(x, y, width, height);
	}

	// ============================================================
	// Matrix Operations
	// ============================================================

	@Override
	public void glMatrixMode(int mode) {
		matrix.glMatrixMode(mode);
	}

	@Override
	public void glLoadIdentity() {
		matrix.glLoadIdentity();
	}

	@Override
	public void glLoadMatrixf(float[] m, int offset) {
		matrix.glLoadMatrixf(m, offset);
	}

	@Override
	public void glLoadMatrixf(FloatBuffer m) {
		matrix.glLoadMatrixf(m);
	}

	@Override
	public void glPushMatrix() {
		matrix.glPushMatrix();
	}

	@Override
	public void glPopMatrix() {
		matrix.glPopMatrix();
	}

	@Override
	public void glTranslatef(float x, float y, float z) {
		matrix.glTranslatef(x, y, z);
	}

	@Override
	public void glRotatef(float angle, float x, float y, float z) {
		matrix.glRotatef(angle, x, y, z);
	}

	@Override
	public void glScalef(float x, float y, float z) {
		matrix.glScalef(x, y, z);
	}

	@Override
	public void glOrtho(double left, double right, double bottom, double top, double zNear, double zFar) {
		matrix.glOrtho(left, right, bottom, top, zNear, zFar);
	}

	// ============================================================
	// Texture Operations
	// ============================================================

	@Override
	public void glGenTextures(int n, int[] textures, int offset) {
		texture.glGenTextures(n, textures, offset);
	}

	@Override
	public void glGenTextures(int n, IntBuffer textures) {
		texture.glGenTextures(n, textures);
	}

	@Override
	public void glBindTexture(int target, int texture) {
		this.texture.glBindTexture(target, texture);
	}

	@Override
	public void glDeleteTextures(int n, int[] textures, int offset) {
		texture.glDeleteTextures(n, textures, offset);
	}

	@Override
	public void glDeleteTextures(int n, IntBuffer textures) {
		texture.glDeleteTextures(n, textures);
	}

	@Override
	public void glTexImage2D(int target, int level, int internalFormat, int width, int height,
	                         int border, int format, int type, Buffer pixels) {
		texture.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
	}

	@Override
	public void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth,
	                         int border, int format, int type, Buffer pixels) {
		texture.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
	}

	@Override
	public void glTexImage1D(int target, int level, int internalFormat, int width,
	                         int border, int format, int type, Buffer pixels) {
		texture.glTexImage1D(target, level, internalFormat, width, border, format, type, pixels);
	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
	                            int format, int type, Buffer pixels) {
		texture.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	@Override
	public void glTexStorage2D(int target, int levels, int internalFormat, int width, int height) {
		texture.glTexStorage2D(target, levels, internalFormat, width, height);
	}

	@Override
	public void glGenerateMipmap(int target) {
		texture.glGenerateMipmap(target);
	}

	@Override
	public void glTexParameteri(int target, int pname, int param) {
		texture.glTexParameteri(target, pname, param);
	}

	@Override
	public void glTexParameterf(int target, int pname, float param) {
		texture.glTexParameterf(target, pname, param);
	}

	@Override
	public void glTexEnvi(int target, int pname, int param) {
		texture.glTexEnvi(target, pname, param);
	}

	@Override
	public void glTexEnvf(int target, int pname, float param) {
		texture.glTexEnvf(target, pname, param);
	}

	@Override
	public void glTexEnvfv(int target, int pname, float[] params, int offset) {
		texture.glTexEnvfv(target, pname, params, offset);
	}

	@Override
	public void glTexEnvfv(int target, int pname, FloatBuffer params) {
		texture.glTexEnvfv(target, pname, params);
	}

	@Override
	public void glTexCoord2f(float s, float t) {
		texture.glTexCoord2f(s, t);
	}

	@Override
	public void glMultiTexCoord2f(int target, float s, float t) {
		texture.glMultiTexCoord2f(target, s, t);
	}

	@Override
	public void glTexGeni(int coord, int pname, int param) {
		texture.glTexGeni(coord, pname, param);
	}

	@Override
	public void glTexGenfv(int coord, int pname, float[] params, int offset) {
		texture.glTexGenfv(coord, pname, params, offset);
	}

	@Override
	public void glTexGenfv(int coord, int pname, FloatBuffer params) {
		texture.glTexGenfv(coord, pname, params);
	}

	@Override
	public void glActiveTexture(int texture) {
		this.texture.glActiveTexture(texture);
	}

	@Override
	public void glClientActiveTexture(int texture) {
		this.texture.glClientActiveTexture(texture);
	}

	// ============================================================
	// Buffer Operations
	// ============================================================

	@Override
	public void glGenBuffers(int n, int[] buffers, int offset) {
		buffer.glGenBuffers(n, buffers, offset);
	}

	@Override
	public void glGenBuffers(int n, IntBuffer buffers) {
		buffer.glGenBuffers(n, buffers);
	}

	@Override
	public void glBindBuffer(int target, int buffer) {
		this.buffer.glBindBuffer(target, buffer);
	}

	@Override
	public void glBufferData(int target, long size, Buffer data, int usage) {
		buffer.glBufferData(target, size, data, usage);
	}

	@Override
	public void glBufferSubData(int target, long offset, long size, Buffer data) {
		buffer.glBufferSubData(target, offset, size, data);
	}

	@Override
	public void glDeleteBuffers(int n, int[] buffers, int offset) {
		buffer.glDeleteBuffers(n, buffers, offset);
	}

	@Override
	public void glDeleteBuffers(int n, IntBuffer buffers) {
		buffer.glDeleteBuffers(n, buffers);
	}

	@Override
	public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
		buffer.glVertexPointer(size, type, stride, pointer);
	}

	@Override
	public void glVertexPointer(int size, int type, int stride, long offset) {
		buffer.glVertexPointer(size, type, stride, offset);
	}

	@Override
	public void glColorPointer(int size, int type, int stride, Buffer pointer) {
		buffer.glColorPointer(size, type, stride, pointer);
	}

	@Override
	public void glColorPointer(int size, int type, int stride, long offset) {
		buffer.glColorPointer(size, type, stride, offset);
	}

	@Override
	public void glNormalPointer(int type, int stride, Buffer pointer) {
		buffer.glNormalPointer(type, stride, pointer);
	}

	@Override
	public void glNormalPointer(int type, int stride, long offset) {
		buffer.glNormalPointer(type, stride, offset);
	}

	@Override
	public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
		buffer.glTexCoordPointer(size, type, stride, pointer);
	}

	@Override
	public void glTexCoordPointer(int size, int type, int stride, long offset) {
		buffer.glTexCoordPointer(size, type, stride, offset);
	}

	@Override
	public void glInterleavedArrays(int format, int stride, Buffer pointer) {
		buffer.glInterleavedArrays(format, stride, pointer);
	}

	@Override
	public void glInterleavedArrays(int format, int stride, long offset) {
		buffer.glInterleavedArrays(format, stride, offset);
	}

	// ============================================================
	// Drawing Operations
	// ============================================================

	@Override
	public void glBegin(int mode) {
		draw.glBegin(mode);
	}

	@Override
	public void glEnd() {
		draw.glEnd();
	}

	@Override
	public void glVertex2f(float x, float y) {
		draw.glVertex2f(x, y);
	}

	@Override
	public void glVertex3f(float x, float y, float z) {
		draw.glVertex3f(x, y, z);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, Buffer indices) {
		draw.glDrawElements(mode, count, type, indices);
	}

	@Override
	public void glDrawElements(int mode, int count, int type, long offset) {
		draw.glDrawElements(mode, count, type, offset);
	}

	@Override
	public void glDrawPixels(int width, int height, int format, int type, Buffer pixels) {
		draw.glDrawPixels(width, height, format, type, pixels);
	}

	@Override
	public int glGenLists(int range) {
		return draw.glGenLists(range);
	}

	@Override
	public void glNewList(int list, int mode) {
		draw.glNewList(list, mode);
	}

	@Override
	public void glEndList() {
		draw.glEndList();
	}

	@Override
	public void glCallList(int list) {
		draw.glCallList(list);
	}

	@Override
	public void glDeleteLists(int list, int range) {
		draw.glDeleteLists(list, range);
	}

	@Override
	public void glRasterPos2i(int x, int y) {
		draw.glRasterPos2i(x, y);
	}

	@Override
	public void glPixelZoom(float xfactor, float yfactor) {
		draw.glPixelZoom(xfactor, yfactor);
	}

	@Override
	public void glCopyPixels(int x, int y, int width, int height, int type) {
		draw.glCopyPixels(x, y, width, height, type);
	}

	// ============================================================
	// Lighting
	// ============================================================

	@Override
	public void glLightfv(int light, int pname, float[] params, int offset) {
		lighting.glLightfv(light, pname, params, offset);
	}

	@Override
	public void glLightfv(int light, int pname, FloatBuffer params) {
		lighting.glLightfv(light, pname, params);
	}

	@Override
	public void glLightf(int light, int pname, float param) {
		lighting.glLightf(light, pname, param);
	}

	@Override
	public void glLightModelfv(int pname, float[] params, int offset) {
		lighting.glLightModelfv(pname, params, offset);
	}

	@Override
	public void glLightModelfv(int pname, FloatBuffer params) {
		lighting.glLightModelfv(pname, params);
	}

	@Override
	public void glColorMaterial(int face, int mode) {
		lighting.glColorMaterial(face, mode);
	}

	@Override
	public void glFogi(int pname, int param) {
		lighting.glFogi(pname, param);
	}

	@Override
	public void glFogf(int pname, float param) {
		lighting.glFogf(pname, param);
	}

	@Override
	public void glFogfv(int pname, float[] params, int offset) {
		lighting.glFogfv(pname, params, offset);
	}

	@Override
	public void glFogfv(int pname, FloatBuffer params) {
		lighting.glFogfv(pname, params);
	}

	@Override
	public void glHint(int target, int mode) {
		lighting.glHint(target, mode);
	}

	// ============================================================
	// Color & Blending
	// ============================================================

	@Override
	public void glColor3ub(byte red, byte green, byte blue) {
		color.glColor3ub(red, green, blue);
	}

	@Override
	public void glColor4ub(byte red, byte green, byte blue, byte alpha) {
		color.glColor4ub(red, green, blue, alpha);
	}

	@Override
	public void glColor4f(float red, float green, float blue, float alpha) {
		color.glColor4f(red, green, blue, alpha);
	}

	@Override
	public void glColor4fv(float[] v, int offset) {
		color.glColor4fv(v, offset);
	}

	@Override
	public void glColor4fv(FloatBuffer v) {
		color.glColor4fv(v);
	}

	@Override
	public void glBlendFunc(int sfactor, int dfactor) {
		color.glBlendFunc(sfactor, dfactor);
	}

	@Override
	public void glAlphaFunc(int func, float ref) {
		color.glAlphaFunc(func, ref);
	}

	@Override
	public void glDepthFunc(int func) {
		color.glDepthFunc(func);
	}

	@Override
	public void glDepthMask(boolean flag) {
		color.glDepthMask(flag);
	}

	// ============================================================
	// Rendering State
	// ============================================================

	@Override
	public void glShadeModel(int mode) {
		renderState.glShadeModel(mode);
	}

	@Override
	public void glCullFace(int mode) {
		renderState.glCullFace(mode);
	}

	@Override
	public void glPolygonMode(int face, int mode) {
		renderState.glPolygonMode(face, mode);
	}

	@Override
	public void glLineWidth(float width) {
		renderState.glLineWidth(width);
	}

	@Override
	public void glPushAttrib(int mask) {
		renderState.glPushAttrib(mask);
	}

	@Override
	public void glPopAttrib() {
		renderState.glPopAttrib();
	}

	@Override
	public void glDrawBuffer(int mode) {
		renderState.glDrawBuffer(mode);
	}

	@Override
	public void glReadBuffer(int mode) {
		renderState.glReadBuffer(mode);
	}

	@Override
	public void glGenProgramsARB(int n, int[] programs, int offset) {
		renderState.glGenProgramsARB(n, programs, offset);
	}

	@Override
	public void glBindProgramARB(int target, int program) {
		renderState.glBindProgramARB(target, program);
	}

	@Override
	public void glProgramStringARB(int target, int format, int len, String string) {
		renderState.glProgramStringARB(target, format, len, string);
	}

	@Override
	public void glProgramLocalParameter4fARB(int target, int index, float x, float y, float z, float w) {
		renderState.glProgramLocalParameter4fARB(target, index, x, y, z, w);
	}

	@Override
	public void glProgramLocalParameter4fvARB(int target, int index, float[] params, int offset) {
		renderState.glProgramLocalParameter4fvARB(target, index, params, offset);
	}

	@Override
	public void glProgramLocalParameter4fvARB(int target, int index, FloatBuffer params) {
		renderState.glProgramLocalParameter4fvARB(target, index, params);
	}

	// ============================================================
	// Query Operations
	// ============================================================

	@Override
	public void glGetIntegerv(int pname, int[] params, int offset) {
		query.glGetIntegerv(pname, params, offset);
	}

	@Override
	public void glGetIntegerv(int pname, IntBuffer params) {
		query.glGetIntegerv(pname, params);
	}

	@Override
	public void glGetFloatv(int pname, float[] params, int offset) {
		query.glGetFloatv(pname, params, offset);
	}

	@Override
	public void glGetFloatv(int pname, FloatBuffer params) {
		query.glGetFloatv(pname, params);
	}

	@Override
	public String glGetString(int name) {
		return query.glGetString(name);
	}

	@Override
	public boolean isExtensionAvailable(String extension) {
		return query.isExtensionAvailable(extension);
	}

	@Override
	public void setSwapInterval(int interval) {
		query.setSwapInterval(interval);
	}

	// ============================================================
	// Particles (ARB Extensions)
	// ============================================================

	@Override
	public void glPointParameterfv(int pname, float[] params, int offset) {
		particle.glPointParameterfv(pname, params, offset);
	}

	@Override
	public void glPointParameterfv(int pname, FloatBuffer params) {
		particle.glPointParameterfv(pname, params);
	}

	@Override
	public void glPointParameterf(int pname, float param) {
		particle.glPointParameterf(pname, param);
	}
}
