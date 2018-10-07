package com.lge.camera.managers.ext.sticker;

public class StickerManagerState {
    private State mCurrentState;

    public enum State {
        STATE_NONE,
        STATE_CREATE,
        STATE_LAYOUT_INIT,
        STATE_WAIT_ONE_SHOT,
        STATE_START,
        STATE_STOP,
        STATE_DESTROY
    }

    public StickerManagerState() {
        this.mCurrentState = State.STATE_NONE;
        this.mCurrentState = State.STATE_NONE;
    }

    public synchronized void setState(State state) {
        this.mCurrentState = state;
    }

    public synchronized State getState() {
        return this.mCurrentState;
    }

    public synchronized boolean isStarted() {
        return this.mCurrentState == State.STATE_START;
    }
}
