package engineer.carrot.warren.warren.irc.message

import com.google.common.base.Splitter
import engineer.carrot.warren.warren.irc.CharacterCodes

object IrcMessageParser: IIrcMessageParser {

    private val CRLF_LENGTH = 2
    val MAX_LINE_LENGTH = 1024 - CRLF_LENGTH
    val MIN_LINE_LENGTH = 5 - CRLF_LENGTH

    override fun parse(line: String): IrcMessage? {
        if (line.length > MAX_LINE_LENGTH || line.length < MIN_LINE_LENGTH) {
            return null
        }

        var (tags, endOfTags) = parseTags(line, 0) ?: return null

        if (endOfTags >= line.length) {
            // tags but no more
            return null
        }

        var (prefix, endOfPrefix) = parsePrefix(line, endOfTags) ?: return null

        if (endOfPrefix >= line.length) {
            // prefix but no more
            return null
        }

        var (command, endOfCommand) = parseCommand(line, endOfPrefix) ?: return null

        if (command.isEmpty()) {
            return null
        }

        if (endOfCommand >= line.length) {
            return IrcMessage(tags, prefix, command)
        }

        var (parameters, endOfParameters) = parseParameters(line, endOfCommand) ?: return null

        return IrcMessage(tags, prefix, command, parameters)
    }

    private fun parseTags(line: String, fromPosition: Int): Pair<Map<String, String?>, Int>? {
        // TODO: Unescape keys and values

        var position = fromPosition

        if (line[position] == CharacterCodes.AT) {
            position++

            val nextSpace = findNext(line, position, CharacterCodes.SPACE) ?: return null
            if (nextSpace <= 0) {
                // @ but no tags
                return null
            }

            val tags = hashMapOf<String, String?>()

            val unparsedTags = Splitter.on(CharacterCodes.SEMICOLON).split(line.substring(position, nextSpace))
            for (tag in unparsedTags) {
                val nextEquals = findNext(tag, 0, CharacterCodes.EQUALS)
                if (nextEquals != null) {
                    if (nextEquals + 1 >= tag.length) {
                        // key but no value
                        return null
                    }

                    val key = tag.substring(0, nextEquals)
                    val value = tag.substring(nextEquals + 1, tag.length)

                    tags.put(key, value)
                } else {
                    tags.put(tag, null)
                }
            }

            position = skipSpaces(line, nextSpace + 1)
            return Pair(tags, position)
        }

        return Pair(emptyMap(), position)
    }

    private fun parsePrefix(line: String, fromPosition: Int): Pair<String?, Int>? {
        var position = fromPosition

        if (line[position] == CharacterCodes.COLON) {
            position++

            val nextSpace = findNext(line, position, CharacterCodes.SPACE) ?: return null
            if (nextSpace < position + 1) {
                // : but nothing else
                return null
            }

            val prefix = line.substring(position, nextSpace)

            position = skipSpaces(line, nextSpace + 1)

            return Pair(prefix, position)
        }

        return Pair(null, position)
    }

    private fun parseCommand(line: String, fromPosition: Int): Pair<String, Int>? {
        var position = fromPosition

        val nextSpace = findNext(line, position, CharacterCodes.SPACE)

        val command: String

        if (nextSpace == null) {
            command = line.substring(position)
            if (command.length <= 0) {
                return null
            }

            position += command.length
        } else {
            if (nextSpace < position + 1) {
                return null
            }

            command = line.substring(position, nextSpace)

            position = skipSpaces(line, nextSpace + 1)
        }

        return Pair(command, position)
    }

    private fun parseParameters(line: String, fromPosition: Int): Pair<List<String>, Int>? {
        var position = fromPosition
        var parameters = mutableListOf<String>()

        while (position < line.length) {
            if (line[position] == CharacterCodes.COLON) {
                position++

                if (position >= line.length) {
                    parameters.add("")
                } else {
                    parameters.add(line.substring(position))
                }

                return Pair(parameters, line.length)
            }

            val nextSpace = findNext(line, position, CharacterCodes.SPACE)
            if (nextSpace != null) {
                val parameter = line.substring(position, nextSpace)
                parameters.add(parameter)

                position = skipSpaces(line, nextSpace + 1)
            } else {
                val parameter = line.substring(position)
                parameters.add(parameter)

                position = line.length
            }
        }

        return Pair(parameters, position)
    }

    private fun findNext(line: String, fromPosition: Int, character: Char): Int? {
        val nextSpacePosition = line.indexOf(character, fromPosition)

        if (nextSpacePosition >= 0) {
            return nextSpacePosition
        } else {
            return null
        }
    }

    private fun skipSpaces(line: String, fromPosition: Int): Int {
        var position = fromPosition

        while (position < line.length && line[position] == CharacterCodes.SPACE) {
            position++
        }

        return position
    }

}