package engineer.carrot.warren.warren.extension.account_notify

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.extension.account_notify.AccountMessage
import engineer.carrot.warren.warren.helper.loggerFor
import engineer.carrot.warren.warren.state.JoinedChannelsState

class AccountHandler(val channelsState: JoinedChannelsState) : IKaleHandler<AccountMessage> {

    private val LOGGER = loggerFor<AccountHandler>()
    override val messageType = AccountMessage::class.java

    override fun handle(message: AccountMessage, tags: Map<String, String?>) {
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