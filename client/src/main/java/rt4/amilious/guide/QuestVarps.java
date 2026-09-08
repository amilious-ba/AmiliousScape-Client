package rt4.amilious.guide;

import java.util.HashMap;
import java.util.Map;

public final class QuestVarps {

    public static final class Def {
        public final int varp;
        public final int completeAt;

        public Def(int varp, int completeAt) {
            this.varp = varp;
            this.completeAt = completeAt;
        }
    }

    private static final Map<String, Def> BY_NAME = new HashMap<String, Def>();

    static {
        add("Cook's Assistant", 29, 2);
        add("Sheep Shearer", 179, 21);
        add("The Restless Ghost", 107, 5);
        add("Rune Mysteries", 63, 6);
        add("Imp Catcher", 160, 2);
        add("Witch's Potion", 67, 3);
        add("Doric's Quest", 31, 100);
        add("Goblin Diplomacy", 62, 6);
        add("Romeo & Juliet", 144, 100);
        add("Ernest the Chicken", 32, 3);
        add("Prince Ali Rescue", 273, 110);
        add("Black Knights' Fortress", 130, 4);
        add("Demon Slayer", 64, 3);
        add("The Knight's Sword", 122, 7);
        add("Pirate's Treasure", 71, 4);
        add("Shield of Arrav", 145, 7);
        add("Vampire Slayer", 178, 3);
        add("Dragon Slayer", 176, 10);
        add("Druidic Ritual", 80, 4);
        add("Witch's House", 226, 7);
        add("Waterfall Quest", 65, 10);
        add("Tree Gnome Village", 111, 9);
        add("Fight Arena", 17, 14);
        add("The Grand Tree", 150, 160);
        add("Gertrude's Cat", 180, 6);
        add("Plague City", 165, 29);
        add("Biohazard", 68, 16);
        add("Priest in Peril", 302, 61);
        add("Nature Spirit", 307, 110);
        add("Lost City", 147, 6);
        add("Death Plateau", 314, 80);
        add("Merlin's Crystal", 14, 7);
        add("Holy Grail", 5, 10);
        add("Elemental Workshop I", 266,2);
        add("The Golem", 346, 10);
        //add("Shadow of the Storm",,);
        add("One Small Favour",416,20);
        add("Big Chompy Bird Hunting", 293,6);
        add("Tears of Guthix", 451,2);
        //add("Wolf Whistle",,);
        add("Sea Slug",300,12);
    }

    private static void add(String name, int varp, int completeAt) {
        BY_NAME.put(QuestProgress.norm(name), new Def(varp, completeAt));
    }

    public static Def get(String questName) {
        return BY_NAME.get(QuestProgress.norm(questName));
    }

    private QuestVarps() {
    }
}