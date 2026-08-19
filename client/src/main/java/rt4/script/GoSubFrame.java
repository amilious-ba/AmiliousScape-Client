package rt4.script;

import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import rt4.util.JagString;

@OriginalClass("client!hj")
public final class GoSubFrame {

	@OriginalMember(owner = "client!hj", name = "b", descriptor = "[I")
	public int[] intLocals;

	@OriginalMember(owner = "client!hj", name = "f", descriptor = "[Lclient!na;")
	public JagString[] stringLocals;

	@OriginalMember(owner = "client!hj", name = "h", descriptor = "Lclient!qc;")
	public ClientScript script;

	@OriginalMember(owner = "client!hj", name = "k", descriptor = "I")
	public int pc = -1;
}
