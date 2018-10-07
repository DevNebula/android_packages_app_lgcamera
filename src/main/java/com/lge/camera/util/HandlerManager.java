package com.lge.camera.util;

import android.os.Handler;
import com.lge.camera.constants.CameraConstants;
import java.util.ArrayList;
import java.util.Iterator;

public class HandlerManager {
    private Handler mHandler;
    private Object mPostRunnableLock;
    private ArrayList<HandlerRunnable> mPostRunnables;
    private int mUiThreadHashCode;

    public HandlerManager() {
        this.mUiThreadHashCode = 0;
        this.mHandler = new Handler();
        this.mPostRunnables = new ArrayList();
        this.mPostRunnableLock = new Object();
        this.mUiThreadHashCode = Thread.currentThread().hashCode();
    }

    public void runOnUiThread(HandlerRunnable action) {
        if (this.mHandler != null) {
            synchronized (this.mPostRunnableLock) {
                this.mPostRunnables.add(action);
            }
            if (this.mUiThreadHashCode == Thread.currentThread().hashCode()) {
                action.run();
            } else {
                this.mHandler.post(action);
            }
        }
    }

    public void postOnUiThread(HandlerRunnable action) {
        if (this.mHandler != null) {
            synchronized (this.mPostRunnableLock) {
                this.mPostRunnables.add(action);
            }
            this.mHandler.post(action);
        }
    }

    public void postOnUiThread(HandlerRunnable action, long delay) {
        if (this.mHandler != null) {
            synchronized (this.mPostRunnableLock) {
                this.mPostRunnables.add(action);
            }
            this.mHandler.postDelayed(action, delay);
        }
    }

    public void removePostRunnable(Object object) {
        if (this.mPostRunnables == null) {
            CamLog.m3d(CameraConstants.TAG, "mPostRunnables is null");
        } else if (this.mPostRunnables.size() > 0) {
            synchronized (this.mPostRunnableLock) {
                int index = this.mPostRunnables.indexOf(object);
                if (index >= 0) {
                    removeCallbacks((Runnable) this.mPostRunnables.get(index));
                    do {
                    } while (this.mPostRunnables.remove(object));
                }
            }
        }
    }

    public boolean hasRunnable(Object object) {
        if (this.mPostRunnables == null || this.mPostRunnables.size() <= 0 || this.mPostRunnables.indexOf(object) < 0) {
            return false;
        }
        return true;
    }

    public void removePostAllRunnables() {
        if (this.mPostRunnables == null) {
            CamLog.m3d(CameraConstants.TAG, "mPostRunnables is null");
            return;
        }
        synchronized (this.mPostRunnableLock) {
            Iterator it = this.mPostRunnables.iterator();
            while (it.hasNext()) {
                removeCallbacks((Runnable) it.next());
            }
            this.mPostRunnables.clear();
        }
    }

    public void removeCallbacks(Runnable r) {
        if (this.mHandler != null) {
            this.mHandler.removeCallbacks(r);
        }
    }

    public Handler getHandler() {
        return this.mHandler;
    }
}
