package chat.willow.warren

import chat.willow.kale.irc.tag.extension.AccountTag
import chat.willow.warren.event.ChannelMessageEvent
import chat.willow.warren.extension.cap.CapKeys
import chat.willow.warren.extension.monitor.UserOnlineEvent
import chat.willow.warren.helper.loggerFor

object WarrenRunner {

    private val LOGGER = loggerFor<WarrenRunner>()

    @JvmStatic fun main(args: Array<String>) {
        val argHost = args[0]
        val argPort = args[1].toInt()
        val argNickname = args[2]
        val argPassword = args.getOrNull(3)

        val client = WarrenClient.build {
            server(argHost) {
                port = argPort
                useTLS = (port != 6667)
            }

            user(argNickname) {
                if (argPassword != null) {
                    sasl(argNickname to argPassword)
                }
            }

            channel("#carrot" to "butts")
            channel("#botdev")

            events {
                fireIncomingLineEvent = true
            }

            extensions {
                monitor("carrot", "someone-else")
                disable(CapKeys.CHGHOST, CapKeys.AWAY_NOTIFY)
            }
        }

        client.events.onAny {
            LOGGER.info("event: $it")
        }

        client.events.on(ChannelMessageEvent::class) {
            LOGGER.info("channel message: $it")

            val account = it.metadata[AccountTag::class]?.account

            val saidRabbitParty by lazy { it.message.equals("rabbit party", ignoreCase = true) }
            val accountIsCarrot by lazy { account == "carrot" }
            val nickIsCarrot by lazy { it.user.nick == "carrot" }
            val userIsOp by lazy { 'o' in it.user.modes }

            if (accountIsCarrot && saidRabbitParty) {
                it.user.send("🐰🎉✨")
            } else if (nickIsCarrot && saidRabbitParty) {
                it.user.send("🐰🎉")
            }

            if (!userIsOp || !nickIsCarrot) {
                return@on
            }

            if (!it.message.startsWith("🥕")) {
                return@on
            }

            val splitMessage = it.message.split(" ", limit = 2)

            val command = splitMessage.getOrNull(0)?.removePrefix("🥕")
            if (command != null) {
                when (command) {
                    null -> Unit

                    "ping" -> it.channel.send("pong")

                    "say" -> {
                        val message = splitMessage.getOrNull(1) ?: "nothing to say"
                        it.channel.send(message)
                    }
                }
            }
        }

        client.events.on(UserOnlineEvent::class) {
            LOGGER.info("user online: ${it.prefix.nick}")
        }

        client.start()
    }

}