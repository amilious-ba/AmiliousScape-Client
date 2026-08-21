package rt4.amilious;

import org.openrs2.deob.annotation.OriginalMember;
import rt4.*;
import rt4.core.Protocol;
import rt4.network.LoginManager;
import rt4.social.LocalizedText;
import rt4.util.Cheat;
import rt4.util.JagString;
import rt4.util.WordPack;

/**
 * Helper class for chat operations
 */
public final class ChatHelper {

	/**
	 * Sends a public chat message with the given text.
	 * This is the same logic as opcode 5008 in ScriptRunner.
	 *
	 * @param message the message to send
	 */
	@OriginalMember(owner = "client!ChatHelper", name = "sendPublicMessage", descriptor = "(Lclient!na;)V")
	public static void sendPublicMessage(JagString message) {
		if (message == null || message.length() == 0) {
			return;
		}

		// Don't send if starts with "::" (command)
		if (message.startsWith(JagString.parse("::"))) {
			Cheat.execute(message);
			return;
		}

		// Check permissions
		if (LoginManager.staffModLevel == 0 && (LoginManager.playerUnderage && !LoginManager.parentalChatConsent || LoginManager.mapQuickChat)) {
			return;
		}

		JagString str1 = message.toLowerCase();
		byte color = 0;
		JagString processedMessage = message;

		// Parse color codes
		if (str1.startsWith(LocalizedText.STABLE_CHATCOL0)) {
			color = 0;
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL0.length());
		} else if (str1.startsWith(LocalizedText.STABLE_CHATCOL1)) {
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL1.length());
			color = 1;
		} else if (str1.startsWith(LocalizedText.STABLE_CHATCOL2)) {
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL2.length());
			color = 2;
		} else if (str1.startsWith(LocalizedText.STABLE_CHATCOL3)) {
			color = 3;
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL3.length());
		} else if (str1.startsWith(LocalizedText.STABLE_CHATCOL4)) {
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL4.length());
			color = 4;
		} else if (str1.startsWith(LocalizedText.STABLE_CHATCOL5)) {
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL5.length());
			color = 5;
		} else if (str1.startsWith(LocalizedText.STABLE_CHATCOL6)) {
			color = 6;
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL6.length());
		} else if (str1.startsWith(LocalizedText.STABLE_CHATCOL7)) {
			color = 7;
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL7.length());
		} else if (str1.startsWith(LocalizedText.STABLE_CHATCOL8)) {
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL8.length());
			color = 8;
		} else if (str1.startsWith(LocalizedText.STABLE_CHATCOL9)) {
			color = 9;
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL9.length());
		} else if (str1.startsWith(LocalizedText.STABLE_CHATCOL10)) {
			color = 10;
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL10.length());
		} else if (str1.startsWith(LocalizedText.STABLE_CHATCOL11)) {
			processedMessage = message.substring(LocalizedText.STABLE_CHATCOL11.length());
			color = 11;
		}

		byte effect = 0;
		str1 = processedMessage.toLowerCase();

		// Parse effect codes
		if (str1.startsWith(LocalizedText.STABLE_CHATEFFECT1)) {
			processedMessage = processedMessage.substring(LocalizedText.STABLE_CHATEFFECT1.length());
			effect = 1;
		} else if (str1.startsWith(LocalizedText.STABLE_CHATEFFECT2)) {
			effect = 2;
			processedMessage = processedMessage.substring(LocalizedText.STABLE_CHATEFFECT2.length());
		} else if (str1.startsWith(LocalizedText.STABLE_CHATEFFECT3)) {
			processedMessage = processedMessage.substring(LocalizedText.STABLE_CHATEFFECT3.length());
			effect = 3;
		} else if (str1.startsWith(LocalizedText.STABLE_CHATEFFECT4)) {
			effect = 4;
			processedMessage = processedMessage.substring(LocalizedText.STABLE_CHATEFFECT4.length());
		} else if (str1.startsWith(LocalizedText.STABLE_CHATEFFECTC5)) {
			effect = 5;
			processedMessage = processedMessage.substring(LocalizedText.STABLE_CHATEFFECTC5.length());
		}

		// Send the message
		Protocol.outboundBuffer.p1isaac(237);
		Protocol.outboundBuffer.p1(0);
		int offset = Protocol.outboundBuffer.offset;
		Protocol.outboundBuffer.p1(color);
		Protocol.outboundBuffer.p1(effect);
		WordPack.encode(Protocol.outboundBuffer, processedMessage);
		Protocol.outboundBuffer.psize1(Protocol.outboundBuffer.offset - offset);
	}

	/**
	 * Reference to the current chat input text (to be set by the chat interface)
	 */
	@OriginalMember(owner = "client!ChatHelper", name = "currentChatInput", descriptor = "Lclient!na;")
	public static JagString currentChatInput = JagString.EMPTY;

	/**
	 * Sends the current chat input message and clears it
	 */
	@OriginalMember(owner = "client!ChatHelper", name = "submitCurrentChat", descriptor = "()V")
	public static void submitCurrentChat() {
		if (currentChatInput != null && currentChatInput.length() > 0) {
			sendPublicMessage(currentChatInput);
			currentChatInput = JagString.EMPTY;
		}
	}
}
