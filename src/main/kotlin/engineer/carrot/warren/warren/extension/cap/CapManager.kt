package engineer.carrot.warren.warren.extension.cap

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.irc.message.extension.cap.CapEndMessage
import engineer.carrot.warren.kale.irc.message.extension.cap.CapLsMessage
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.extension.account_notify.AccountNotifyExtension
import engineer.carrot.warren.warren.extension.away_notify.AwayNotifyExtension
import engineer.carrot.warren.warren.extension.extended_join.ExtendedJoinExtension
import engineer.carrot.warren.warren.extension.sasl.SaslExtension
import engineer.carrot.warren.warren.extension.sasl.SaslState
import engineer.carrot.warren.warren.handler.CapAckHandler
import engineer.carrot.warren.warren.handler.CapLsHandler
import engineer.carrot.warren.warren.handler.CapNakHandler
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.registration.IRegistrationExtension
import engineer.carrot.warren.warren.registration.IRegistrationExtensionListener
import engineer.carrot.warren.warren.state.AuthLifecycle
import engineer.carrot.warren.warren.state.CaseMappingState
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.IStateCapturing

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
}

class CapManager(initialState: CapState, private val kale: IKale, channelsState: ChannelsState, initialSaslState: SaslState, private val sink: IMessageSink, caseMappingState: CaseMappingState) : ICapManager, ICapExtension, IRegistrationExtension {

    private val LOGGER = loggerFor<CapManager>()

    internal var internalState: CapState = initialState
    @Volatile override var state: CapState = initialState.copy()

    val sasl = SaslExtension(initialSaslState, kale, this, sink)

    private val capLsHandler: CapLsHandler by lazy { CapLsHandler(internalState, sasl.internalState, sink, this) }
    private val capAckHandler: CapAckHandler by lazy { CapAckHandler(internalState, sasl.internalState, sink, this) }
    private val capNakHandler: CapNakHandler by lazy { CapNakHandler(internalState, sasl.internalState, sink, this) }

    private val capExtensions = mapOf(
            CapKeys.SASL.key to sasl,
            CapKeys.ACCOUNT_NOTIFY.key to AccountNotifyExtension(kale, channelsState.joined),
            CapKeys.AWAY_NOTIFY.key to AwayNotifyExtension(kale, channelsState.joined),
            CapKeys.EXTENDED_JOIN.key to ExtendedJoinExtension(kale, channelsState, caseMappingState)
    )

    override var listener: IRegistrationExtensionListener? = null

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
            listener?.onSuccess()
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
    }

    override fun tearDown() {
        kale.unregister(capLsHandler)
        kale.unregister(capAckHandler)
        kale.unregister(capNakHandler)
    }

    // IRegistrationExtension

    override fun startRegistration() {
        sink.write(CapLsMessage(caps = mapOf()))
    }

    override fun onRegistrationSucceeded() {
        listener?.onSuccess()
    }

    override fun onRegistrationFailed() {
        listener?.onFailure()
    }

}