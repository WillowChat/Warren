package engineer.carrot.warren.warren.irc.handlers.core;

import engineer.carrot.warren.warren.UserManager;
import engineer.carrot.warren.warren.event.UserChangedNicknameEvent;
import engineer.carrot.warren.warren.irc.User;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.core.ChangeNicknameMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeNicknameHandler extends MessageHandler<ChangeNicknameMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(ChangeNicknameMessage.class);

    @Override
    public void handleMessage(ChangeNicknameMessage message) {
        // Prefix is set if a person formerly known to the server changed nicknames
        //  Otherwise the person is new to the server

        if (message.fromUser == null) {
            LOGGER.warn("Unsupported NICK without a prefix ignored for user '{}'", message.nickname);
            return;
        }

        UserManager manager = this.botDelegate.getUserManager();
        User user = manager.getOrCreateUser(message.fromUser);
        String oldNickname = user.getNameWithoutAccess();
        manager.renameUser(oldNickname, message.nickname);

        this.postEvent(new UserChangedNicknameEvent(user, oldNickname));
    }
}
