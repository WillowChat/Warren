package chat.willow.warren.handler

import chat.willow.warren.state.ChannelTypesState
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