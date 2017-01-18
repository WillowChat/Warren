package chat.willow.warren.handler

import chat.willow.kale.irc.CharacterCodes

enum class CtcpEnum {
    NONE,
    UNKNOWN,
    ACTION;

    companion object {

        fun from(rawMessage: String): CtcpEnum {
            val message = if (rawMessage.startsWith(CharacterCodes.CTCP)) {
                rawMessage.substring(1)
            } else {
                return NONE
            }

            if (message.startsWith("ACTION ")) {
                return ACTION
            }

            return UNKNOWN
        }
    }
}

object CtcpHelper {

    val CTCP: String = Character.toString(CharacterCodes.CTCP)

    fun isMessageCTCP(message: String): Boolean = (message.startsWith(CTCP) && message.endsWith(CTCP))

    // Trims <ctcp><identifier><space><rest of message><ctcp> to <rest of message>
    fun trimCTCP(rawMessage: String): String {
        var message = rawMessage
        if (message.startsWith(CTCP)) {
            message = message.substring(1)
        }

        if (message.endsWith(CTCP)) {
            message = message.substring(0, message.length - 1)
        }

        val spacePosition = message.indexOf(CharacterCodes.SPACE)
        if (spacePosition > 0) {
            message = message.substring(spacePosition + 1, message.length)
        }

        return message
    }

}