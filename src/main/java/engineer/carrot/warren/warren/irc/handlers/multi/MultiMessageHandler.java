package engineer.carrot.warren.warren.irc.handlers.multi;

import javax.annotation.Nullable;

public abstract class MultiMessageHandler<R> implements IMultiMessageHandler<R> {
    private boolean isConstructing = false;

    public void startConstructing() {
        this.isConstructing = true;
        this.initialiseHandler();
    }

    protected abstract void initialiseHandler();

    @Override
    public boolean isConstructing() {
        return this.isConstructing;
    }

    @Override
    @Nullable
    public R finishConstructing() {
        if (!this.isConstructing) {
            return null;
        }

        R result = this.formResult();
        this.isConstructing = false;
        return result;
    }

    protected abstract R formResult();
}
