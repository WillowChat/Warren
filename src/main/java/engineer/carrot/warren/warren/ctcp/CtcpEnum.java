package engineer.carrot.warren.warren.ctcp;

public enum CtcpEnum {
    NONE,
    UNKNOWN,
    ACTION;

    // Expects message in the form: "<identifier><space><rest of message>"
    //  CTCP prefix is tolerated
    public static CtcpEnum parseFromMessage(String message) {
        if (message.startsWith(CtcpHelper.CTCP)) {
            message = message.substring(1);
        }

        if (message.startsWith("ACTION ")) {
            return ACTION;
        }

        return UNKNOWN;
    }
}
