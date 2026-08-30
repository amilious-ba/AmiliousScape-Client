package rt4.amilious.menu;

import rt4.JagString;
import rt4.Packet;

import java.util.HashMap;

/**
 * Opcode 200 fills BY_INDEX. MiniMenu.addNpcEntries merges slots into NpcType.ops.
 */
public final class NpcMenuOverrides {

    public static final int OPCODE = 200;

    public static final class Override {
        public int npcId;
        public String displayName;
        public final String[] slots = new String[5];
    }

    private static final HashMap<Integer, Override> BY_INDEX = new HashMap<Integer, Override>();

    private NpcMenuOverrides() {
    }

    public static void clearAll() {
        BY_INDEX.clear();
    }

    public static void put(int npcIndex, Override o) {
        if (o == null) {
            BY_INDEX.remove(npcIndex);
        } else {
            BY_INDEX.put(npcIndex, o);
        }
    }

    public static Override get(int npcIndex) {
        return BY_INDEX.get(npcIndex);
    }

    public static void read(Packet buf, int payloadLen) {
        if (buf == null || payloadLen < 6) {
            return;
        }
        int start = buf.offset;
        int npcIndex = buf.g2();
        int npcId = buf.g2();
        int flags = buf.g1();
        JagString nameJs = buf.gjstr();
        String name = nameJs == null ? "" : nameJs.toString();

        if ((flags & 1) != 0) {
            BY_INDEX.remove(npcIndex);
            return;
        }

        Override o = new Override();
        o.npcId = npcId;
        o.displayName = name;

        if (buf.offset >= start + payloadLen) {
            BY_INDEX.put(npcIndex, o);
            return;
        }

        int count = buf.g1();
        if (count < 0) count = 0;
        if (count > 5) count = 5;

        for (int i = 0; i < count && buf.offset < start + payloadLen; i++) {
            int slot = buf.g1();
            JagString text = buf.gjstr();
            if (slot >= 0 && slot < o.slots.length && text != null) {
                o.slots[slot] = text.toString();
            }
        }
        BY_INDEX.put(npcIndex, o);
    }

}