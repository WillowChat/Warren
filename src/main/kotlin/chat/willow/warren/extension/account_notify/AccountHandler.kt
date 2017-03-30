package chat.willow.warren.extension.account_notify

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.account_notify.AccountMessage
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.JoinedChannelsState

class AccountHandler(val channelsState: JoinedChannelsState) : KaleHandler<AccountMessage.Message>(AccountMessage.Message.Parser) {

    private val LOGGER = loggerFor<AccountHandler>()

    override fun handle(message: AccountMessage.Message, metadata: IMetadataStore) {
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