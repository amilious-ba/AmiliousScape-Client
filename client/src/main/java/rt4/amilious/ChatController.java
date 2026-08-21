package rt4.amilious;

import org.openrs2.deob.annotation.OriginalMember;
import rt4.render.primitive.GlRaster;
import rt4.render.primitive.SoftwareRaster;
import rt4.ui.Keyboard;

/**
 * Controls chat input focus and keyboard event routing.
 * When chat is not focused, keyboard input is blocked from the chat system,
 * allowing keys to be used for other bindings.
 */
public final class ChatController {

	/**
	 * Whether the ChatController system is enabled
	 */
	@OriginalMember(owner = "client!ChatController", name = "enabled", descriptor = "Z")
	public static boolean enabled = true;

	/**
	 * Whether the chat is currently focused and accepting input
	 */
	@OriginalMember(owner = "client!ChatController", name = "focused", descriptor = "Z")
	public static boolean focused = false;

	/**
	 * Tracks the previous state of the Enter key to detect key press (not hold)
	 */
	@OriginalMember(owner = "client!ChatController", name = "prevEnterPressed", descriptor = "Z")
	private static boolean prevEnterPressed = false;

	/**
	 * Whether an Enter key press should be consumed (to prevent it from submitting chat)
	 */
	@OriginalMember(owner = "client!ChatController", name = "shouldConsumeEnter", descriptor = "Z")
	private static boolean shouldConsumeEnter = false;

	/**
	 * Tracks frames since message was submitted to prevent quick chat opening
	 */
	@OriginalMember(owner = "client!ChatController", name = "framesSinceSubmit", descriptor = "I")
	private static int framesSinceSubmit = 0;

	/**
	 * True if we just submitted a message this frame (need to let Enter through)
	 */
	@OriginalMember(owner = "client!ChatController", name = "justSubmitted", descriptor = "Z")
	private static boolean justSubmitted = false;

	/**
	 * Debug flag to enable component ID logging when clicking
	 */
	@OriginalMember(owner = "client!ChatController", name = "debugShowTextId", descriptor = "Z")
	public static boolean debugShowTextId = false;

	/**
	 * Updates the chat focus state. Should be called each game tick.
	 * Toggles focus when Enter is pressed (not held).
	 */
	@OriginalMember(owner = "client!ChatController", name = "update", descriptor = "()V")
	public static void update() {
		if (!enabled) {
			focused = true; // Always focused when disabled
			shouldConsumeEnter = false;
			prevEnterPressed = false;
			framesSinceSubmit = 10; // Prevent quick chat
			justSubmitted = false;
			return;
		}

		boolean enterPressed = Keyboard.pressedKeys[Keyboard.KEY_ENTER];

		// Toggle focus on Enter key press (rising edge detection)
		if (enterPressed && !prevEnterPressed) {
			if (focused) {
				// Chat is focused - unfocus (Enter will submit the message)
				focused = false;
			} else {
				// Chat not focused - focus it
				focused = true;
			}
		}

		prevEnterPressed = enterPressed;
	}

	/**
	 * Checks if the chat is currently focused and accepting input.
	 * If ChatController is disabled, always returns true.
	 * @return true if chat is focused, false otherwise
	 */
	@OriginalMember(owner = "client!ChatController", name = "isFocused", descriptor = "()Z")
	public static boolean isFocused() {
		if (!enabled) {
			return true;
		}
		return focused;
	}

	/**
	 * Checks if an Enter key press should be consumed (blocked from the game).
	 * Used to prevent Enter from opening quick chat when focusing chat.
	 * @return true if Enter should be consumed, false otherwise
	 */
	@OriginalMember(owner = "client!ChatController", name = "shouldConsumeEnter", descriptor = "()Z")
	public static boolean shouldConsumeEnter() {
		return enabled && shouldConsumeEnter;
	}

	/**
	 * Checks if quick chat should be blocked (prevents Enter from opening quick chat after submit).
	 * @return true if quick chat should be blocked, false otherwise
	 */
	@OriginalMember(owner = "client!ChatController", name = "shouldBlockQuickChat", descriptor = "()Z")
	public static boolean shouldBlockQuickChat() {
		return enabled && framesSinceSubmit > 0; // -1 doesn't block, only positive values
	}

	/**
	 * Checks if we just submitted a message this frame (need to let keys through).
	 * @return true if we just submitted, false otherwise
	 */
	@OriginalMember(owner = "client!ChatController", name = "justSubmitted", descriptor = "()Z")
	public static boolean justSubmitted() {
		return enabled && justSubmitted;
	}

	/**
	 * Sets the chat focus state
	 * @param newFocused true to focus chat, false to unfocus
	 */
	@OriginalMember(owner = "client!ChatController", name = "setFocused", descriptor = "(Z)V")
	public static void setFocused(boolean newFocused) {
		focused = newFocused;
	}

	/**
	 * Unfocuses the chat
	 */
	@OriginalMember(owner = "client!ChatController", name = "unfocus", descriptor = "()V")
	public static void unfocus() {
		focused = false;
	}

	/**
	 * Focuses the chat
	 */
	@OriginalMember(owner = "client!ChatController", name = "focus", descriptor = "()V")
	public static void focus() {
		focused = true;
	}

	/**
	 * Enables the ChatController system
	 */
	@OriginalMember(owner = "client!ChatController", name = "enable", descriptor = "()V")
	public static void enable() {
		enabled = true;
	}

	/**
	 * Disables the ChatController system. When disabled, chat input always works normally.
	 */
	@OriginalMember(owner = "client!ChatController", name = "disable", descriptor = "()V")
	public static void disable() {
		enabled = false;
		focused = true; // Ensure chat works when disabled
	}

	/**
	 * Checks if the ChatController system is enabled
	 * @return true if enabled, false otherwise
	 */
	@OriginalMember(owner = "client!ChatController", name = "isEnabled", descriptor = "()Z")
	public static boolean isEnabled() {
		return enabled;
	}

	/**
	 * Gets a color value that can be used to indicate chat focus state.
	 * Returns a grayed-out color when chat is not focused and ChatController is enabled.
	 *
	 * @param originalColor the original color value
	 * @return grayed-out color if chat not focused and enabled, original color otherwise
	 */
	@OriginalMember(owner = "client!ChatController", name = "getChatColor", descriptor = "(I)I")
	public static int getChatColor(int originalColor) {
		if (!enabled || focused) {
			return originalColor;
		}
		// Gray out the color by reducing saturation and brightness
		// Extract RGB components
		int r = (originalColor >> 16) & 0xFF;
		int g = (originalColor >> 8) & 0xFF;
		int b = originalColor & 0xFF;

		// Convert to grayscale (simple average method) and darken
		int gray = (r + g + b) / 3;
		gray = gray / 2; // Make it darker

		// Reconstruct color
		return (gray << 16) | (gray << 8) | gray;
	}

	/**
	 * Gets the alpha transparency value for chat elements based on focus state.
	 * Returns reduced alpha when chat is not focused and ChatController is enabled.
	 *
	 * @param originalAlpha the original alpha value (0-255)
	 * @return reduced alpha if chat not focused and enabled, original alpha otherwise
	 */
	@OriginalMember(owner = "client!ChatController", name = "getChatAlpha", descriptor = "(I)I")
	public static int getChatAlpha(int originalAlpha) {
		if (!enabled || focused) {
			return originalAlpha;
		}
		// Reduce alpha to 40% when not focused
		return (originalAlpha * 40) / 100;
	}

	/**
	 * Draws the chat focus indicator rectangle when chat is not focused.
	 * Should be called before drawing the chat text component.
	 *
	 * @param x X position of the component
	 * @param y Y position of the component
	 * @param width Width of the component
	 * @param height Height of the component
	 * @param glEnabled Whether OpenGL rendering is enabled
	 */
	@OriginalMember(owner = "client!ChatController", name = "drawChatFocusIndicator", descriptor = "(IIIIZ)V")
	public static void drawChatFocusIndicator(int x, int y, int width, int height, boolean glEnabled) {
		if (!enabled || focused) {
			return; // Don't draw when disabled or focused
		}

		int padding = 2;
		int rectX = x - padding;
		int rectY = y;
		int rectWidth = width + (padding * 2);
		int rectHeight = height;

		if (glEnabled) {
			GlRaster.fillRectAlpha(rectX, rectY, rectWidth, rectHeight, 0x000000, 128);
		} else {
			SoftwareRaster.fillRectAlpha(rectX, rectY, rectWidth, rectHeight, 0x000000, 128);
		}
	}
}
