package rt4.scene;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import rt4.util.SecondaryNode;

@OriginalClass("client!bk")
public final class SpotAnimNode extends SecondaryNode {

	@OriginalMember(owner = "client!bk", name = "M", descriptor = "Lclient!bh;")
	public final SpotAnim aClass8_Sub2_1;

	@OriginalMember(owner = "client!bk", name = "<init>", descriptor = "(Lclient!bh;)V")
	public SpotAnimNode(@OriginalArg(0) SpotAnim arg0) {
		this.aClass8_Sub2_1 = arg0;
	}
}
