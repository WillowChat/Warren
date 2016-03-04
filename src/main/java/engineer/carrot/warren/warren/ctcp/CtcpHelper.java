package engineer.carrot.warren.warren.ctcp;

import engineer.carrot.warren.warren.irc.JavaCharacterCodes;

public class CtcpHelper {
    public static final String CTCP = Character.toString(JavaCharacterCodes.CTCP);

    public static boolean isMessageCTCP(String message) {
        if (message.startsWith(CTCP) && message.endsWith(CTCP)) {
            return true;
        }

        return false;
    }

    // Trims <ctcp><identifier><space><rest of message><ctcp> to <rest of message>
    public static String trimCTCP(String message) {
        if (message.startsWith(CTCP)) {
            message = message.substring(1);
        }

        if (message.endsWith(CTCP)) {
            message = message.substring(0, message.length() - 1);
        }

        int spacePosition = message.indexOf(JavaCharacterCodes.SPACE);
        if (spacePosition > 0) {
            message = message.substring(spacePosition + 1, message.length());
        }

        return message;
    }
}
