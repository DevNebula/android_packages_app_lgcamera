package com.lge.camera.postview;

public interface PostViewBarListener {
    void onCursorMoving(boolean z);

    void onCursorUpdated(int i);

    void postOnUiThread(Object obj, long j);

    void removePostRunnable(Object obj);
}
