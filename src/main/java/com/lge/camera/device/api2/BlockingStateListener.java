package com.lge.camera.device.api2;

import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.os.SystemClock;
import android.util.Log;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingStateListener extends StateCallback {
    public static final int STATE_ACTIVE = 3;
    public static final int STATE_BUSY = 4;
    public static final int STATE_CLOSED = 5;
    public static final int STATE_DISCONNECTED = 6;
    public static final int STATE_ERROR = 7;
    public static final int STATE_IDLE = 2;
    public static final int STATE_OPENED = 0;
    public static final int STATE_UNCONFIGURED = 1;
    public static final int STATE_UNINITIALIZED = -1;
    private static final String TAG = "CameraAppBlocking";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private static final String[] mStateNames = new String[]{"STATE_UNINITIALIZED", "STATE_OPENED", "STATE_UNCONFIGURED", "STATE_IDLE", "STATE_ACTIVE", "STATE_BUSY", "STATE_CLOSED", "STATE_DISCONNECTED", "STATE_ERROR"};
    private final Object mLock;
    private final StateCallback mProxy;
    private final LinkedBlockingQueue<Integer> mRecentStates;
    private boolean mWaiting;

    private void setCurrentState(int state) {
        if (VERBOSE) {
            Log.v(TAG, "Camera device state now " + stateToString(state));
        }
        try {
            this.mRecentStates.put(Integer.valueOf(state));
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to set device state", e);
        }
    }

    public BlockingStateListener() {
        this.mLock = new Object();
        this.mWaiting = false;
        this.mRecentStates = new LinkedBlockingQueue();
        this.mProxy = null;
    }

    public BlockingStateListener(StateCallback callback) {
        this.mLock = new Object();
        this.mWaiting = false;
        this.mRecentStates = new LinkedBlockingQueue();
        this.mProxy = callback;
    }

    public void onOpened(CameraDevice camera) {
        setCurrentState(0);
        if (this.mProxy != null) {
            this.mProxy.onOpened(camera);
        }
    }

    public void onDisconnected(CameraDevice camera) {
        setCurrentState(6);
        if (this.mProxy != null) {
            this.mProxy.onDisconnected(camera);
        }
    }

    public void onError(CameraDevice camera, int error) {
        setCurrentState(7);
        if (this.mProxy != null) {
            this.mProxy.onError(camera, error);
        }
    }

    public void onClosed(CameraDevice camera) {
        setCurrentState(5);
        if (this.mProxy != null) {
            this.mProxy.onClosed(camera);
        }
    }

    public void waitForState(int state, long timeout) {
        waitForAnyOfStates(Arrays.asList(new Integer[]{Integer.valueOf(state)}), timeout);
    }

    public int waitForAnyOfStates(Collection<Integer> states, long timeout) {
        StringBuilder s;
        Integer nextState;
        synchronized (this.mLock) {
            if (this.mWaiting) {
                throw new IllegalStateException("Only one waiter allowed at a time");
            }
            this.mWaiting = true;
        }
        if (VERBOSE) {
            s = new StringBuilder("Waiting for state(s) ");
            appendStates(s, states);
            Log.v(TAG, s.toString());
        }
        long timeoutLeft = timeout;
        long startMs = SystemClock.elapsedRealtime();
        while (true) {
            try {
                nextState = (Integer) this.mRecentStates.poll(timeoutLeft, TimeUnit.MILLISECONDS);
                if (nextState == null) {
                    break;
                }
                if (VERBOSE) {
                    Log.v(TAG, "  Saw transition to " + stateToString(nextState.intValue()));
                }
                if (states.contains(nextState)) {
                    break;
                }
                long endMs = SystemClock.elapsedRealtime();
                timeoutLeft -= endMs - startMs;
                startMs = endMs;
            } catch (InterruptedException e) {
                throw new UnsupportedOperationException("Does not support interrupts on waits", e);
            }
        }
        synchronized (this.mLock) {
            this.mWaiting = false;
        }
        if (states.contains(nextState)) {
            return nextState.intValue();
        }
        s = new StringBuilder("Timed out after ");
        s.append(timeout);
        s.append(" ms waiting for state(s) ");
        appendStates(s, states);
        throw new TimeoutRuntimeException(s.toString());
    }

    public static String stateToString(int state) {
        return mStateNames[state + 1];
    }

    public static void appendStates(StringBuilder s, Collection<Integer> states) {
        boolean start = true;
        for (Integer state : states) {
            if (!start) {
                s.append(" ");
            }
            s.append(stateToString(state.intValue()));
            start = false;
        }
    }
}
