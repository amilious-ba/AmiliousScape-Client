package rt4.data;

import org.openrs2.deob.annotation.OriginalMember;
import rt4.util.SoftLruHashTable;

public class HitBarList {
	@OriginalMember(owner = "client!fm", name = "S", descriptor = "Lclient!n;")
	public static final SoftLruHashTable hitBars = new SoftLruHashTable(4);
}
