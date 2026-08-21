package rt4.amilious.input.action;

import rt4.amilious.input.InputMode;

/**
 * Maps a logical frame button to an Action, optionally only in one mode.
 */
public final class Binding {
    public final Action action;
    public final int buttonId;
    /** null = valid in any mode */
    public final InputMode modeFilter;

    public Binding(Action action, int buttonId, InputMode modeFilter) {
        this.action = action;
        this.buttonId = buttonId;
        this.modeFilter = modeFilter;
    }

    public Binding(Action action, int buttonId) {
        this(action, buttonId, null);
    }
}