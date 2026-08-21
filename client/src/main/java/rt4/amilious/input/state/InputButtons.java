package rt4.amilious.input.state;

/**
 * Logical button indices inside InputFrame.
 * Not AWT keycodes — KeyboardDevice will map hardware → these later.
 */
public final class InputButtons {

    private InputButtons() {
    }

    public static final int ENTER = 0;
    public static final int ESCAPE = 1;
    public static final int BACKSPACE = 2;
    public static final int TAB = 3;
    public static final int SPACE = 4;

    public static final int W = 10;
    public static final int A = 11;
    public static final int S = 12;
    public static final int D = 13;

    public static final int UP = 20;
    public static final int DOWN = 21;
    public static final int LEFT = 22;
    public static final int RIGHT = 23;

    public static final int F1 = 30;
    public static final int F2 = 31;
    public static final int F3 = 32;
    public static final int F4 = 33;
    public static final int F5 = 34;
    public static final int F6 = 35;
    public static final int F7 = 36;
    public static final int F8 = 37;
    public static final int F9 = 38;
    public static final int F10 = 39;
    public static final int F11 = 40;
    public static final int F12 = 41;

    public static final int DIGIT_1 = 50;
    public static final int DIGIT_2 = 51;
    public static final int DIGIT_3 = 52;
    public static final int DIGIT_4 = 53;
    public static final int DIGIT_5 = 54;
    public static final int DIGIT_6 = 55;
    public static final int DIGIT_7 = 56;
    public static final int DIGIT_8 = 57;
    public static final int DIGIT_9 = 58;
    public static final int DIGIT_0 = 59;

    // Gamepad face / shoulders (for later)
    public static final int GP_A = 100;
    public static final int GP_B = 101;
    public static final int GP_X = 102;
    public static final int GP_Y = 103;
    public static final int GP_LB = 104;
    public static final int GP_RB = 105;
    public static final int GP_START = 106;
    public static final int GP_BACK = 107;

    public static final int BUTTON_COUNT = 128;

    // Axes
    public static final int AXIS_LEFT_X = 0;
    public static final int AXIS_LEFT_Y = 1;
    public static final int AXIS_RIGHT_X = 2;
    public static final int AXIS_RIGHT_Y = 3;
    public static final int AXIS_COUNT = 8;
}
