package engineer.carrot.warren.warren.irc.message

import com.google.common.base.Joiner
import engineer.carrot.warren.warren.irc.CharacterCodes

object IrcMessageSerialiser : IIrcMessageSerialiser {

    override fun serialise(message: IrcMessage): String? {
        val builder = StringBuilder()

        if (message.tags.isNotEmpty()) {
            val serialisedTags = mutableListOf<String>()

            for ((key, value) in message.tags) {
                if (value == null) {
                    serialisedTags.add(key)
                } else {
                    serialisedTags.add("${key}${CharacterCodes.EQUALS}${value}")
                }
            }

            builder.append(CharacterCodes.AT)
            builder.append(Joiner.on(CharacterCodes.SEMICOLON).join(serialisedTags))
            builder.append(CharacterCodes.SPACE)
        }

        if (message.prefix != null) {
            builder.append(CharacterCodes.COLON)
            builder.append(message.prefix)
            builder.append(CharacterCodes.SPACE)
        }

        builder.append(message.command)

        if (message.parameters.isNotEmpty()) {
            builder.append(CharacterCodes.SPACE)

            val parametersSize = message.parameters.size

            if (message.parameters.size > 1) {
                for (i in 0 .. parametersSize - 2) {
                    builder.append(message.parameters[i])
                    builder.append(CharacterCodes.SPACE)
                }
            }

            builder.append(CharacterCodes.COLON)
            builder.append(message.parameters[parametersSize - 1])
        }

        val output = builder.toString()
        if (output.length < IrcMessageParser.MIN_LINE_LENGTH || output.length > IrcMessageParser.MAX_LINE_LENGTH) {
            println("serialised message is too long: $output")
            return null
        }

        return output
    }

}