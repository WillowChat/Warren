package chat.willow.warren.extension.cap

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.KaleSubcommandHandler
import chat.willow.kale.irc.message.extension.cap.CapMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.account_notify.AccountNotifyExtension
import chat.willow.warren.extension.away_notify.AwayNotifyExtension
import chat.willow.warren.extension.cap.handler.*
import chat.willow.warren.extension.chghost.ChgHostExtension
import chat.willow.warren.extension.extended_join.ExtendedJoinExtension
import chat.willow.warren.extension.invite_notify.InviteNotifyExtension
import chat.willow.warren.extension.monitor.MonitorExtension
import chat.willow.warren.extension.monitor.MonitorState
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
    fun capValueSet(name: String, value: String?)
    fun onRegistrationStateChanged()
    val sasl: IStateCapturing<SaslState>
    val monitor: IStateCapturing<MonitorState>

}

enum class CapKeys(val key: String) {
    SASL("sasl"),
    ACCOUNT_NOTIFY("account-notify"),
    AWAY_NOTIFY("away-notify"),
    EXTENDED_JOIN("extended-join"),
    MULTI_PREFIX("multi-prefix"),
    CAP_NOTIFY("cap-notify"),
    USERHOST_IN_NAMES("userhost-in-names"),
    INVITE_NOTIFY("invite-notify"),
    MONITOR("monitor"),
    CHGHOST("chghost"),
    SERVER_TIME("server-time"),
    ACCOUNT_TAG("account-tag")
}

class CapManager(initialState: CapState, private val kaleRouter: IKaleRouter<IKaleIrcMessageHandler>, channelsState: ChannelsState, initialSaslState: SaslState, initialMonitorState: MonitorState, private val sink: IMessageSink, caseMappingState: CaseMappingState, private val registrationManager: IRegistrationManager, eventDispatcher: IWarrenEventDispatcher) : ICapManager, ICapExtension, IRegistrationExtension {

    private val LOGGER = loggerFor<CapManager>()

    internal var internalState: CapState = initialState
    @Volatile override var state: CapState = initialState.copy()

    override val sasl = SaslExtension(initialSaslState, kaleRouter, this, sink)
    override val monitor = MonitorExtension(initialMonitorState, kaleRouter, sink, eventDispatcher)

    private val capLsHandler: CapLsHandler by lazy { CapLsHandler(internalState, sink, this) }
    private val capAckHandler: CapAckHandler by lazy { CapAckHandler(internalState, sasl.internalState, sink, this) }
    private val capNakHandler: CapNakHandler by lazy { CapNakHandler(internalState, sasl.internalState, sink, this) }
    private val capNewHandler: CapNewHandler by lazy { CapNewHandler(internalState, sink, this) }
    private val capDelHandler: CapDelHandler by lazy { CapDelHandler(internalState, sink, this) }

    val capsToHandlers = mapOf(
            CapMessage.Ls.subcommand to capLsHandler,
            CapMessage.Ack.subcommand to capAckHandler,
            CapMessage.Nak.subcommand to capNakHandler,
            CapMessage.New.subcommand to capNewHandler,
            CapMessage.Del.subcommand to capDelHandler
    )

    private val capHandler = KaleSubcommandHandler(capsToHandlers, subcommandPosition = 1)

    private val capExtensions = mapOf(
            CapKeys.SASL.key to sasl,
            CapKeys.ACCOUNT_NOTIFY.key to AccountNotifyExtension(kaleRouter, channelsState.joined),
            CapKeys.AWAY_NOTIFY.key to AwayNotifyExtension(kaleRouter, channelsState.joined),
            CapKeys.EXTENDED_JOIN.key to ExtendedJoinExtension(kaleRouter, channelsState, caseMappingState),
            CapKeys.INVITE_NOTIFY.key to InviteNotifyExtension(kaleRouter, eventDispatcher),
            CapKeys.MONITOR.key to monitor,
            CapKeys.CHGHOST.key to ChgHostExtension(kaleRouter, channelsState.joined)
    )

    override fun captureStateSnapshot() {
        state = internalState.copy()

        sasl.captureStateSnapshot()
        monitor.captureStateSnapshot()
    }

    override fun capEnabled(name: String) {
        capExtensions[name]?.setUp()
    }

    override fun capDisabled(name: String) {
        capExtensions[name]?.tearDown()
    }

    override fun capValueSet(name: String, value: String?) {
        capExtensions[name]?.valueSet(value)
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

        sink.write(CapMessage.End.Command)
    }


    override fun setUp() {
        kaleRouter.register(CapMessage.command, capHandler)
    }

    override fun tearDown() {
        kaleRouter.unregister(CapMessage.command)
    }

    // IRegistrationExtension

    override fun startRegistration() {
        sink.write(CapMessage.Ls.Command(version = "302"))
    }

    override fun onRegistrationSucceeded() {
        registrationManager.onExtensionSuccess(this)
    }

    override fun onRegistrationFailed() {
        registrationManager.onExtensionFailure(this)
    }

}