package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.warren.state.ChannelTypesState
import org.junit.Before

class NoticeHandlerTests {

    lateinit var handler: NoticeHandler
    lateinit var channelsState: ChannelTypesState

    @Before fun setUp() {
        channelsState = ChannelTypesState(types = setOf('#', '&'))
        handler = NoticeHandler(channelsState)
    }

    //FIXME: Test when it does something verifiable

}