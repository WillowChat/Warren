package chat.willow.warren.extension.sasl

import engineer.carrot.warren.kale.IKale
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.state.AuthCredentials
import chat.willow.warren.state.AuthLifecycle
import chat.willow.warren.state.IStateCapturing

data class SaslState(var shouldAuth: Boolean, var lifecycle: AuthLifecycle, var credentials: AuthCredentials?)

class SaslExtension(initialState: SaslState, private val kale: IKale, private val capManager: ICapManager, private val sink: IMessageSink) : ICapExtension, IStateCapturing<SaslState> {

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
        kale.register(authenticateHandler)
        kale.register(rpl903Handler)
        kale.register(rpl904Handler)
        kale.register(rpl905Handler)
    }

    override fun tearDown() {
        kale.unregister(authenticateHandler)
        kale.unregister(rpl903Handler)
        kale.unregister(rpl904Handler)
        kale.unregister(rpl905Handler)
    }

}