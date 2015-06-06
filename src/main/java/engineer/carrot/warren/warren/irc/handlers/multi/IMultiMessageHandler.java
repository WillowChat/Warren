package engineer.carrot.warren.warren.irc.handlers.multi;

import javax.annotation.Nullable;

public interface IMultiMessageHandler<R> {
    void startConstructing();

    boolean isConstructing();

    @Nullable
    R finishConstructing();
}