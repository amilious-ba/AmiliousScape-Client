package rt4.script;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import rt4.util.JagString;

@OriginalClass("client!of")
public interface QuickChatCommandDecoder {

	@OriginalMember(owner = "client!of", name = "a", descriptor = "(I[IIJ)Lclient!na;")
    JagString decode(@OriginalArg(0) int arg0, @OriginalArg(1) int[] arg1, @OriginalArg(3) long arg2);
}
