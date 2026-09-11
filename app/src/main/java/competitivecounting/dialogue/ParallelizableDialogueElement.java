package competitivecounting.dialogue;

import java.util.concurrent.atomic.AtomicReference;

public abstract class ParallelizableDialogueElement extends DialogueElement implements Finishable {
    protected boolean isFinished = false;
    protected final Finishable parentLock;
    public ParallelizableDialogueElement(Finishable parentLock) {
        this.parentLock = parentLock == null ? this : parentLock;
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    protected void setFinished() {
        isFinished = true;
    }

    protected Finishable getParentLock() {
        return parentLock;
    }
}
