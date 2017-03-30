package chat.willow.warren.extension.sasl

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.irc.message.extension.sasl.AuthenticateMessage
import chat.willow.kale.irc.message.extension.sasl.rpl.Rpl903Message
import chat.willow.kale.irc.message.extension.sasl.rpl.Rpl904Message
import chat.willow.kale.irc.message.extension.sasl.rpl.Rpl905Message
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.AuthCredentials
import chat.willow.warren.state.AuthLifecycle
import chat.willow.warren.state.IStateCapturing

data class SaslState(var shouldAuth: Boolean, var lifecycle: AuthLifecycle, var credentials: AuthCredentials?, var mechanisms: Set<String> = setOf())

class SaslExtension(initialState: SaslState, private val kaleRouter: IKaleRouter<IKaleIrcMessageHandler>, private val capManager: ICapManager, private val sink: IMessageSink) : ICapExtension, IStateCapturing<SaslState> {

    private val LOGGER = loggerFor<SaslExtension>()

    internal var internalState: SaslState = initialState
    @Volatile override var state: SaslState = initialState.copy()

    val authenticateHandler: AuthenticateHandler by lazy { AuthenticateHandler(internalState, sink) }
    val rpl903Handler: Rpl903Handler by lazy { Rpl903Handler(capManager, internalState) }
    val rpl904Handler: Rpl904Handler by lazy { Rpl904Handler(capManager, internalState) }
    val rpl905Handler: Rpl905Handler by lazy { Rpl905Handler(capManager, internalState) }

    override fun captureStateSnapshot() {
        state = internalState.copy()
    }

    override fun setUp() {
        kaleRouter.register(AuthenticateMessage.command, authenticateHandler)
        kaleRouter.register(Rpl903Message.command, rpl903Handler)
        kaleRouter.register(Rpl904Message.command, rpl904Handler)
        kaleRouter.register(Rpl905Message.command, rpl905Handler)

        // If we're enabled without any mechanisms, assume PLAIN is supported
        if (internalState.mechanisms.isEmpty()) {
            internalState.mechanisms += "plain"
        }

        if (internalState.shouldAuth) {
            if (!internalState.mechanisms.contains("plain")) {
                LOGGER.info("SASL enabled, but the server doesn't appear to support PLAIN authentication - not authenticating")
                return
            }

            LOGGER.trace("server acked sasl - starting authentication for user: ${internalState.credentials?.account}")

            internalState.lifecycle = AuthLifecycle.AUTHING

            sink.write(AuthenticateMessage.Command(payload = "PLAIN"))
        }
    }

    override fun tearDown() {
        kaleRouter.unregister(AuthenticateMessage.command)
        kaleRouter.unregister(Rpl903Message.command)
        kaleRouter.unregister(Rpl904Message.command)
        kaleRouter.unregister(Rpl905Message.command)

        internalState.lifecycle = AuthLifecycle.AUTHING
        internalState.mechanisms = setOf()
    }

    override fun valueSet(value: String?) {
        internalState.mechanisms = parseMechanismsFromValue(value).toSet()

        LOGGER.debug("server set new sasl mechanisms, new state: $internalState")
    }

    private fun parseMechanismsFromValue(value: String?): List<String> {
        return (value ?: "").split(',').filterNot(String::isEmpty).map(String::toLowerCase)
    }

}