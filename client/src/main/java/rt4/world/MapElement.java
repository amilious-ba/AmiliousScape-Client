package rt4.world;

import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import rt4.util.Node;

@OriginalClass("client!oj")
public final class MapElement extends Node {

	@OriginalMember(owner = "client!oj", name = "q", descriptor = "I")
	public int anInt4307;

	@OriginalMember(owner = "client!oj", name = "r", descriptor = "I")
	public int id;

	@OriginalMember(owner = "client!oj", name = "A", descriptor = "I")
	public int anInt4314;
}
