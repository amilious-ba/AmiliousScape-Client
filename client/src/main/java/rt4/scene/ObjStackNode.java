package rt4.scene;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import rt4.util.SecondaryNode;

@OriginalClass("client!pa")
public final class ObjStackNode extends SecondaryNode {

	@OriginalMember(owner = "client!pa", name = "T", descriptor = "Lclient!uj;")
	public final ObjStack value;

	@OriginalMember(owner = "client!pa", name = "<init>", descriptor = "(Lclient!uj;)V")
	public ObjStackNode(@OriginalArg(0) ObjStack arg0) {
		this.value = arg0;
	}
}
