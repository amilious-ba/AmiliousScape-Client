package rt4.render.primitive;

import com.jogamp.opengl.GL2;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;
import rt4.util.SecondaryHashTable;
import rt4.network.Buffer;
import rt4.render.GlRenderer;

import java.nio.FloatBuffer;

@OriginalClass("client!ga")
public final class ParticleSystem extends ParticleNode {

	static {
		new SecondaryHashTable(8);
		new Buffer(131056);
	}

	@OriginalMember(owner = "client!ga", name = "a", descriptor = "()V")
	public static void load() {
		if (GlRenderer.api.isExtensionAvailable("GL_ARB_point_parameters")) {
			@Pc(20) float[] coefficients = new float[]{1.0F, 0.0F, 5.0E-4F};
			GlRenderer.api.glPointParameterfv(GL2.GL_POINT_DISTANCE_ATTENUATION, coefficients, 0);
			@Pc(28) FloatBuffer buffer = FloatBuffer.allocate(1);
			GlRenderer.api.glGetFloatv(GL2.GL_POINT_SIZE_MAX, buffer);
			@Pc(36) float pointSizeMax = buffer.get(0);
			if (pointSizeMax > 1024.0F) {
				pointSizeMax = 1024.0F;
			}
			GlRenderer.api.glPointParameterf(GL2.GL_POINT_SIZE_MIN, 1.0F);
			GlRenderer.api.glPointParameterf(GL2.GL_POINT_SIZE_MAX, pointSizeMax);
		}
		if (GlRenderer.api.isExtensionAvailable("GL_ARB_point_sprite")) {
		}
	}

	@OriginalMember(owner = "client!ga", name = "b", descriptor = "()V")
	public static void quit() {
	}

	@OriginalMember(owner = "client!ga", name = "d", descriptor = "()V")
	public final void method1646() {
	}
}
