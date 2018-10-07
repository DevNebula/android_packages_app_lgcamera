package com.lge.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class SettingListView extends ListView {
    private int mDownX = 0;
    private int mDownY = 0;
    private SettingListViewInterface mListener;

    public interface SettingListViewInterface {
        int getDegree();

        void onSettingMenuHide();
    }

    public SettingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingListView(Context context) {
        super(context);
    }

    public void setSettingListViewInterface(SettingListViewInterface listener) {
        this.mListener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean retVal = true;
        switch (event.getAction()) {
            case 0:
                this.mDownX = (int) event.getX();
                this.mDownY = (int) event.getY();
                retVal = true;
                break;
            case 2:
                int diffY = ((int) event.getY()) - this.mDownY;
                if (((int) event.getX()) - this.mDownX > 300 && diffY < 100) {
                    if (this.mListener != null) {
                        this.mListener.onSettingMenuHide();
                        retVal = false;
                        break;
                    }
                }
                retVal = true;
                break;
                break;
        }
        if (retVal) {
            super.onTouchEvent(event);
        }
        return true;
    }
}
