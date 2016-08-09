package com.vvovsvv.touchmine;

// TODO smooth movement

public class SoftMoveInfo {
    private volatile float mAccumulatedDX = 0.0f;
    private volatile float mAccumulatedDY = 0.0f;

    public volatile float lookAtX = 0.0f;
    public volatile float lookAtY = 0.0f;

    private final float mSlowDownMult = 0.1f;

    // called by main / input thread
    public void addDragDelta(float dx, float dy) {
        mAccumulatedDX += dx;
        mAccumulatedDY += dy;
    }

    // called by rendering thread
    public void integrate() {
        // adjust movement
        final float moveSubX = mAccumulatedDX*mSlowDownMult;
        final float moveSubY = mAccumulatedDY*mSlowDownMult;
        mAccumulatedDX -= moveSubX;
        mAccumulatedDY -= moveSubY;

        lookAtX += moveSubX;
        lookAtY += moveSubY;
    }
}