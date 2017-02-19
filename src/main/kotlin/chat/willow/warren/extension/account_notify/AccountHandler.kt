package chat.willow.warren.extension.account_notify

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.account_notify.AccountMessage
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.JoinedChannelsState

class AccountHandler(val channelsState: JoinedChannelsState) : IKaleHandler<AccountMessage> {

    private val LOGGER = loggerFor<AccountHandler>()
    override val messageType = AccountMessage::class.java

    override fun handle(message: AccountMessage, tags: ITagStore) {
        val nick = message.source.nick

        val account: String? = if (message.account == "*") {
            // User logged out

            null
        } else {
            // User changed account

            message.account
        }

        for ((name, channel) in channelsState.all) {
            val user = channel.users[nick]
            if (user != null) {
                channel.users -= nick
                channel.users += user.copy(account = account)
            }
        }

        LOGGER.trace("user changed their account: ${message.source} to $account")
    }

}