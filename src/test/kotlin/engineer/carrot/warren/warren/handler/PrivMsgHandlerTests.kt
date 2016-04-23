package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.warren.state.ChannelTypesState
import org.junit.Before

class PrivMsgHandlerTests {

    lateinit var handler: PrivMsgHandler
    lateinit var channelsState: ChannelTypesState

    @Before fun setUp() {
        channelsState = ChannelTypesState(types = setOf('#', '&'))
        handler = PrivMsgHandler(channelsState)
    }

    //FIXME: Test when it does something verifiable

}