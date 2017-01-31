package chat.willow.warren.extension.cap

import chat.willow.kale.IKale
import chat.willow.kale.irc.message.extension.cap.CapEndMessage
import chat.willow.kale.irc.message.extension.cap.CapLsMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.account_notify.AccountNotifyExtension
import chat.willow.warren.extension.away_notify.AwayNotifyExtension
import chat.willow.warren.extension.cap.handler.*
import chat.willow.warren.extension.extended_join.ExtendedJoinExtension
import chat.willow.warren.extension.sasl.SaslExtension
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.registration.IRegistrationExtension
import chat.willow.warren.registration.IRegistrationManager
import chat.willow.warren.state.AuthLifecycle
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.ChannelsState
import chat.willow.warren.state.IStateCapturing

data class CapState(var lifecycle: CapLifecycle, var negotiate: Set<String>, var server: Map<String, String?>, var accepted: Set<String>, var rejected: Set<String>)

enum class CapLifecycle { NEGOTIATING, NEGOTIATED, FAILED }

interface ICapManager : IStateCapturing<CapState> {

    fun capEnabled(name: String)
    fun capDisabled(name: String)
    fun onRegistrationStateChanged()

}

enum class CapKeys(val key: String) {
    SASL("sasl"),
    ACCOUNT_NOTIFY("account-notify"),
    AWAY_NOTIFY("away-notify"),
    EXTENDED_JOIN("extended-join"),
    MULTI_PREFIX("multi-prefix"),
    CAP_NOTIFY("cap-notify")
}

class CapManager(initialState: CapState, private val kale: IKale, channelsState: ChannelsState, initialSaslState: SaslState, private val sink: IMessageSink, caseMappingState: CaseMappingState, private val registrationManager: IRegistrationManager) : ICapManager, ICapExtension, IRegistrationExtension {

    private val LOGGER = loggerFor<CapManager>()

    internal var internalState: CapState = initialState
    @Volatile override var state: CapState = initialState.copy()

    val sasl = SaslExtension(initialSaslState, kale, this, sink)

    private val capLsHandler: CapLsHandler by lazy { CapLsHandler(internalState, sasl.internalState, sink, this) }
    private val capAckHandler: CapAckHandler by lazy { CapAckHandler(internalState, sasl.internalState, sink, this) }
    private val capNakHandler: CapNakHandler by lazy { CapNakHandler(internalState, sasl.internalState, sink, this) }
    private val capNewHandler: CapNewHandler by lazy { CapNewHandler(internalState, sink) }
    private val capDelHandler: CapDelHandler by lazy { CapDelHandler(internalState, sink, this) }

    private val capExtensions = mapOf(
            CapKeys.SASL.key to sasl,
            CapKeys.ACCOUNT_NOTIFY.key to AccountNotifyExtension(kale, channelsState.joined),
            CapKeys.AWAY_NOTIFY.key to AwayNotifyExtension(kale, channelsState.joined),
            CapKeys.EXTENDED_JOIN.key to ExtendedJoinExtension(kale, channelsState, caseMappingState)
    )

    override fun captureStateSnapshot() {
        state = internalState.copy()

        sasl.captureStateSnapshot()
    }

    override fun capEnabled(name: String) {
        capExtensions[name]?.setUp()
    }

    override fun capDisabled(name: String) {
        capExtensions[name]?.tearDown()
    }

    override fun onRegistrationStateChanged() {
        if (shouldEndCapNegotiation(sasl.internalState, internalState)) {
            endCapNegotiation()
            registrationManager.onExtensionSuccess(this)
        }
    }

    private fun shouldEndCapNegotiation(saslState: SaslState, capState: CapState): Boolean {
        val remainingCaps = capState.negotiate - (capState.accepted + capState.rejected)
        LOGGER.trace("cap end checker: remaining caps to negotiate: $remainingCaps")

        if (remainingCaps.isEmpty()) {
            // TODO: CapManager should ask its extensions if they're done with registration?
            if (saslState.lifecycle == AuthLifecycle.AUTHING && capState.accepted.contains("sasl")) {
                LOGGER.debug("cap end checker: no more remaining caps, but we're still authenticating")
            } else {
                LOGGER.debug("cap end checker: no more remaining caps, SASL not authing - good to end negotiation")

                return true
            }
        }

        return false
    }

    private fun endCapNegotiation() {
        internalState.lifecycle = CapLifecycle.NEGOTIATED

        LOGGER.debug("ending cap negotiation with state: $internalState")

        sink.write(CapEndMessage())
    }


    override fun setUp() {
        kale.register(capLsHandler)
        kale.register(capAckHandler)
        kale.register(capNakHandler)
        kale.register(capNewHandler)
        kale.register(capDelHandler)
    }

    override fun tearDown() {
        kale.unregister(capLsHandler)
        kale.unregister(capAckHandler)
        kale.unregister(capNakHandler)
        kale.unregister(capNewHandler)
        kale.unregister(capDelHandler)
    }

    // IRegistrationExtension

    override fun startRegistration() {
        sink.write(CapLsMessage(caps = mapOf()))
    }

    override fun onRegistrationSucceeded() {
        registrationManager.onExtensionSuccess(this)
    }

    override fun onRegistrationFailed() {
        registrationManager.onExtensionFailure(this)
    }

}