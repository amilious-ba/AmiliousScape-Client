package rt4.gl.lwjgl;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Helper for converting Java arrays to NIO buffers required by LWJGL.
 *
 * JOGL accepts Java arrays with offsets: glLightfv(light, pname, float[], offset)
 * LWJGL requires NIO buffers: glLight(light, pname, FloatBuffer)
 *
 * This helper bridges that gap efficiently by:
 * - Converting arrays to buffers on demand
 * - Reusing thread-local buffers for common sizes
 * - Handling the buffer.flip() operation automatically
 */
public final class LwjglBufferHelper {

	// Thread-local reusable buffers for common sizes
	// This avoids constant allocation/GC pressure
	private static final ThreadLocal<FloatBuffer> FLOAT_4 = ThreadLocal.withInitial(() -> BufferUtils.createFloatBuffer(4));
	private static final ThreadLocal<FloatBuffer> FLOAT_16 = ThreadLocal.withInitial(() -> BufferUtils.createFloatBuffer(16));
	private static final ThreadLocal<IntBuffer> INT_1 = ThreadLocal.withInitial(() -> BufferUtils.createIntBuffer(1));
	private static final ThreadLocal<IntBuffer> INT_16 = ThreadLocal.withInitial(() -> BufferUtils.createIntBuffer(16));

	private LwjglBufferHelper() {
		// Utility class - no instantiation
	}

	// ============================================================
	// Float array → FloatBuffer conversion
	// ============================================================

	/**
	 * Convert float array to FloatBuffer, reusing cached buffer if possible.
	 * Buffer is ready to read (position=0, limit=count).
	 *
	 * @param array Source array
	 * @param offset Starting index in array
	 * @param count Number of floats to copy
	 * @return FloatBuffer ready for use with LWJGL
	 */
	public static FloatBuffer toFloatBuffer(float[] array, int offset, int count) {
		FloatBuffer buffer;

		// Reuse thread-local buffers for common sizes
		if (count == 4) {
			buffer = FLOAT_4.get();
			buffer.clear();
		} else if (count == 16) {
			buffer = FLOAT_16.get();
			buffer.clear();
		} else {
			// Uncommon size - allocate new buffer
			buffer = BufferUtils.createFloatBuffer(count);
		}

		// Copy data
		buffer.put(array, offset, count);
		buffer.flip();
		return buffer;
	}

	/**
	 * Convert entire float array to FloatBuffer.
	 */
	public static FloatBuffer toFloatBuffer(float[] array) {
		return toFloatBuffer(array, 0, array.length);
	}

	// ============================================================
	// Int array → IntBuffer conversion
	// ============================================================

	/**
	 * Convert int array to IntBuffer, reusing cached buffer if possible.
	 * Buffer is ready to read (position=0, limit=count).
	 *
	 * @param array Source array
	 * @param offset Starting index in array
	 * @param count Number of ints to copy
	 * @return IntBuffer ready for use with LWJGL
	 */
	public static IntBuffer toIntBuffer(int[] array, int offset, int count) {
		IntBuffer buffer;

		// Reuse thread-local buffers for common sizes
		if (count == 1) {
			buffer = INT_1.get();
			buffer.clear();
		} else if (count <= 16) {
			buffer = INT_16.get();
			buffer.clear();
		} else {
			// Uncommon size - allocate new buffer
			buffer = BufferUtils.createIntBuffer(count);
		}

		// Copy data
		buffer.put(array, offset, count);
		buffer.flip();
		return buffer;
	}

	/**
	 * Convert entire int array to IntBuffer.
	 */
	public static IntBuffer toIntBuffer(int[] array) {
		return toIntBuffer(array, 0, array.length);
	}

	// ============================================================
	// Special cases for single-value operations
	// ============================================================

	/**
	 * Get a reusable 1-element IntBuffer containing the given value.
	 * Useful for glGenTextures(1, buffer) style calls.
	 */
	public static IntBuffer getSingleIntBuffer(int value) {
		IntBuffer buffer = INT_1.get();
		buffer.clear();
		buffer.put(value);
		buffer.flip();
		return buffer;
	}

	/**
	 * Get a reusable 4-element FloatBuffer from individual floats.
	 * Common for glLight, glFog, glColor4f operations.
	 */
	public static FloatBuffer get4FloatBuffer(float f0, float f1, float f2, float f3) {
		FloatBuffer buffer = FLOAT_4.get();
		buffer.clear();
		buffer.put(f0).put(f1).put(f2).put(f3);
		buffer.flip();
		return buffer;
	}

	// ============================================================
	// Buffer → Array extraction (for query operations)
	// ============================================================

	/**
	 * Extract data from IntBuffer into Java array.
	 * Used for glGetIntegerv and similar query operations.
	 *
	 * @param buffer Source buffer (position will be reset to 0)
	 * @param array Destination array
	 * @param offset Starting index in destination array
	 * @param count Number of ints to extract
	 */
	public static void fromIntBuffer(IntBuffer buffer, int[] array, int offset, int count) {
		buffer.position(0);
		buffer.get(array, offset, count);
	}

	/**
	 * Extract data from FloatBuffer into Java array.
	 * Used for glGetFloatv and similar query operations.
	 *
	 * @param buffer Source buffer (position will be reset to 0)
	 * @param array Destination array
	 * @param offset Starting index in destination array
	 * @param count Number of floats to extract
	 */
	public static void fromFloatBuffer(FloatBuffer buffer, float[] array, int offset, int count) {
		buffer.position(0);
		buffer.get(array, offset, count);
	}

	/**
	 * Get single int value from IntBuffer.
	 * Used after glGenTextures(1, buffer) to extract the generated ID.
	 */
	public static int getInt(IntBuffer buffer) {
		return buffer.get(0);
	}

	// ============================================================
	// Direct buffer wrapping (zero-copy when possible)
	// ============================================================

	/**
	 * Wrap existing FloatBuffer for LWJGL use.
	 * No conversion needed - just ensure it's ready to read.
	 */
	public static FloatBuffer prepareFloatBuffer(FloatBuffer buffer) {
		if (buffer.position() != 0) {
			buffer.flip();
		}
		return buffer;
	}

	/**
	 * Wrap existing IntBuffer for LWJGL use.
	 * No conversion needed - just ensure it's ready to read.
	 */
	public static IntBuffer prepareIntBuffer(IntBuffer buffer) {
		if (buffer.position() != 0) {
			buffer.flip();
		}
		return buffer;
	}
}
