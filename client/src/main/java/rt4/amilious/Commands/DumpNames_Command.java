package rt4.amilious.Commands;

import rt4.NpcType;
import rt4.NpcTypeList;
import rt4.amilious.DebugConsole;

public class DumpNames_Command extends AbstractCommand {

    public DumpNames_Command() {
        super("::dumpnames");
    }

    @Override
    public boolean execute(String s) {
        DebugConsole.log("Dumping npc names...");
        dumpNpcNames();
        DebugConsole.log("Done.");
        return true;
    }

    public static void dumpNpcNames() {
        try {
            if (NpcTypeList.archive == null) {
                System.out.println("[npc-dump] archive not ready yet");
                return;
            }

            int groups = NpcTypeList.archive.capacity();
            // ids are packed: group = id >>> 7, file = id & 0x7F  → max id ≈ groups * 128
            int maxId = groups * 128;

            java.util.LinkedHashSet<String> names = new java.util.LinkedHashSet<String>();
            int withData = 0;

            for (int id = 0; id < maxId; id++) {
                try {
                    // Skip empty cache slots: no file → still returns a type with default name
                    byte[] data = NpcTypeList.archive.fetchFile(
                            NpcTypeList.getGroupId(id),
                            NpcTypeList.getFileId(id));
                    if (data == null) {
                        continue;
                    }
                    withData++;

                    NpcType t = NpcTypeList.get(id);
                    if (t == null || t.name == null) {
                        continue;
                    }
                    String n = t.name.toString().trim();
                    if (n.isEmpty()) {
                        continue;
                    }
                    // default / junk
                    if (n.equalsIgnoreCase("null") || n.equals("Name")) {
                        continue;
                    }
                    names.add(n);
                } catch (Exception ignored) {
                }
            }

            java.io.File out = new java.io.File(System.getProperty("user.dir"), "npc-names.txt");
            java.io.PrintWriter pw = new java.io.PrintWriter(out, "UTF-8");
            for (String n : names) {
                pw.println(n);
            }
            pw.close();

            System.out.println("[npc-dump] groups=" + groups
                    + " slotsWithData=" + withData
                    + " uniqueNames=" + names.size()
                    + " -> " + out.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
