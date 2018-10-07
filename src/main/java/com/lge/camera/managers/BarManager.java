package com.lge.camera.managers;

import android.app.Activity;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import com.lge.camera.C0088R;
import com.lge.camera.components.BarAction;
import com.lge.camera.components.BarView;
import com.lge.camera.components.BarView.BarManagerListener;
import com.lge.camera.components.BeautyshotBar;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.Utils;

public class BarManager extends ManagerInterfaceImpl implements BarAction {
    private static int[] sArrType = new int[]{1, 2, 4};
    private SparseArray<BarView> mArrBar = new SparseArray();

    public BarManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setBarListener(int type, BarManagerListener listener) {
        BarView currentBarView = getBar(type);
        if (currentBarView != null) {
            currentBarView.setBarListener(listener);
        }
    }

    public BarManagerListener getBarListener(int type) {
        BarView currentBarView = getBar(type);
        if (currentBarView != null) {
            return currentBarView.getBarListener();
        }
        return null;
    }

    public void initBar(int types) {
        CamLog.m3d(CameraConstants.TAG, "[relighting]on type>" + types);
        if (this.mArrBar == null) {
            this.mArrBar = new SparseArray();
        }
        for (int currentType : sArrType) {
            if ((currentType & types) == currentType) {
                BarView currentBarView = createBar(currentType);
                if (currentBarView != null) {
                    this.mArrBar.put(currentType, currentBarView);
                    currentBarView.startRotation(getOrientationDegree(), false);
                }
            }
        }
    }

    public BarView createBar(int type) {
        BeautyshotBar beautybar = (BeautyshotBar) this.mGet.findViewById(C0088R.id.beautyshot_bar);
        if (beautybar == null) {
            return null;
        }
        switch (type) {
            case 1:
                beautybar.setBarSettingKey(Setting.KEY_BEAUTYSHOT);
                beautybar.initBar(this, type);
                return beautybar;
            case 4:
                CamLog.m3d(CameraConstants.TAG, "[relighting] create TYPE_RELIGHTING_BAR");
                beautybar.setBarSettingKey(Setting.KEY_RELIGHTING);
                beautybar.initBar(this, type);
                return beautybar;
            default:
                return beautybar;
        }
    }

    public void setEnable(int types, boolean enable) {
        BarView currentBarView = getBar(types);
        if (currentBarView != null) {
            currentBarView.initCursor();
            currentBarView.setBarEnable(enable);
        }
    }

    public void setVisible(int types, boolean visible, boolean isSingleSet) {
        CamLog.m3d(CameraConstants.TAG, "types = " + types);
        int visibility = visible ? 0 : 4;
        int curDegree = getOrientationDegree();
        BarView currentBarView;
        if (isSingleSet) {
            currentBarView = getBar(types);
            if (currentBarView != null && currentBarView.getVisibility() != visibility) {
                currentBarView.setVisibility(visibility);
                currentBarView.initCursor();
                if (visible) {
                    currentBarView.startRotation(curDegree, false);
                }
            }
        } else if (types == -2) {
            for (int current : sArrType) {
                currentBarView = getBar(current);
                if (currentBarView != null) {
                    currentBarView.setVisibility(visibility);
                    currentBarView.initCursor();
                    if (visible) {
                        currentBarView.startRotation(curDegree, false);
                    }
                }
            }
        } else {
            for (int current2 : sArrType) {
                if ((current2 & types) == current2) {
                    currentBarView = getBar(current2);
                    if (currentBarView != null) {
                        currentBarView.setVisibility(visibility);
                        currentBarView.initCursor();
                        if (visible) {
                            currentBarView.startRotation(curDegree, false);
                        }
                    }
                }
            }
        }
    }

    public BarView getBar(int type) {
        return this.mArrBar == null ? null : (BarView) this.mArrBar.get(type);
    }

    public int getBarValue(int type) {
        BarView currentBarView = getBar(type);
        if (currentBarView != null) {
            return currentBarView.getCursorValue();
        }
        return 0;
    }

    public void setBarMaxValue(int type, int maxValue) {
        BarView currentBarView = getBar(type);
        if (currentBarView != null) {
            currentBarView.setMaxValue(maxValue);
        }
    }

    public void destoryAllBar() {
        if (this.mArrBar != null) {
            for (int i = 0; i < this.mArrBar.size(); i++) {
                ((BarView) this.mArrBar.valueAt(i)).unbind();
            }
            this.mArrBar.clear();
        }
    }

    public void onPauseAfter() {
        setVisible(-2, false, false);
        super.onPauseAfter();
    }

    public void onDestroy() {
        destoryAllBar();
        this.mArrBar = null;
        super.onDestroy();
    }

    public void removePostRunnable(Object object) {
        this.mGet.removePostRunnable(object);
    }

    public void updateAllBars(int types, int value) {
        int i = 0;
        int[] iArr;
        int length;
        BarView currentBarView;
        if (types == -2) {
            iArr = sArrType;
            length = iArr.length;
            while (i < length) {
                currentBarView = getBar(iArr[i]);
                if (currentBarView != null) {
                    currentBarView.setBarValue(value);
                    currentBarView.setCursor(value);
                }
                i++;
            }
            return;
        }
        iArr = sArrType;
        length = iArr.length;
        while (i < length) {
            int current = iArr[i];
            if ((current & types) == current) {
                currentBarView = getBar(current);
                if (currentBarView != null) {
                    currentBarView.setBarValue(value);
                    currentBarView.setCursor(value);
                }
            }
            i++;
        }
    }

    public void rotateSettingBar(int types, int degree, boolean useAnim) {
        int i = 0;
        int[] iArr;
        int length;
        BarView currentBarView;
        if (types == -2) {
            iArr = sArrType;
            length = iArr.length;
            while (i < length) {
                currentBarView = getBar(iArr[i]);
                if (currentBarView != null && currentBarView.getVisibility() == 0) {
                    currentBarView.startRotation(degree, useAnim);
                }
                i++;
            }
            return;
        }
        iArr = sArrType;
        length = iArr.length;
        while (i < length) {
            int current = iArr[i];
            if ((current & types) == current) {
                currentBarView = getBar(current);
                if (currentBarView != null) {
                    currentBarView.startRotation(degree, useAnim);
                }
            }
            i++;
        }
    }

    public void updateBar(int barType, int step) {
        BarView currentBarView = getBar(barType);
        if (currentBarView != null) {
            if (currentBarView.getVisibility() != 0) {
                currentBarView.setVisibility(0);
                currentBarView.startRotation(getOrientationDegree(), false);
            }
            currentBarView.updateBar(step, false, false, false);
        }
    }

    public void updateBarWithValue(int barType, int value) {
        BarView currentBarView = getBar(barType);
        if (currentBarView != null) {
            currentBarView.updateBarWithValue(value, false);
        }
    }

    public void updateExtraInfo(int barType, final String info) {
        final BarView currentBarView = getBar(barType);
        runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (currentBarView != null) {
                    currentBarView.updateExtraInfo(info);
                }
            }
        });
    }

    public boolean isBarVisible(int type) {
        BarView currentBarView = getBar(type);
        if (currentBarView != null) {
            return currentBarView.getVisibility() == 0;
        } else {
            return false;
        }
    }

    public void setBarRotateDegree(int type, int degree, boolean animation) {
        BarView currentBarView = getBar(type);
        if (currentBarView != null) {
            currentBarView.startRotation(degree, animation);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean isPaused() {
        return this.mGet.isPaused();
    }

    public View findViewById(int id) {
        return this.mGet.getActivity().findViewById(id);
    }

    public String getSettingValue(String key) {
        return this.mGet.getSettingValue(key);
    }

    public String getBarSettingValue(String key) {
        return getSettingValue(key);
    }

    public void runOnUiThread(Object action) {
        this.mGet.runOnUiThread(action);
    }

    public void postOnUiThread(Object action, long delay) {
        this.mGet.postOnUiThread(action, delay);
    }

    public boolean setBarSetting(String key, String value, boolean save) {
        int barType;
        if (Setting.KEY_BEAUTYSHOT.equals(key)) {
            barType = 1;
        } else if (Setting.KEY_RELIGHTING.equals(key)) {
            barType = 4;
        } else if ("zoom".equals(key)) {
            barType = 2;
        } else {
            barType = 0;
        }
        BarManagerListener listener = getBarListener(barType);
        if (listener == null) {
            return false;
        }
        return listener.setBarSetting(key, value, save);
    }

    public void resetBarDisappearTimer(int barType, int duration) {
        BarManagerListener listener = getBarListener(barType);
        if (listener != null) {
            listener.resetBarDisappearTimer(barType, duration);
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        rotateSettingBar(-2, degree, animation);
    }

    public Activity getActivity() {
        return this.mGet.getActivity();
    }

    public void switchCamera() {
        if (this.mGet != null && !this.mGet.isCameraChanging() && !this.mGet.isAnimationShowing()) {
            if (FunctionProperties.isSupportedSwitchingAnimation()) {
                this.mGet.setGestureType(1);
                this.mGet.getCurPreviewBlurredBitmap(136, 240, 25, false);
                this.mGet.startCameraSwitchingAnimation(1);
            }
            this.mGet.handleSwitchCamera();
        }
    }

    public void resumeShutterless() {
        this.mGet.resumeShutterless();
    }

    public void pauseShutterless() {
        this.mGet.pauseShutterless();
    }

    public boolean isBarTouching() {
        BarView currentBarView = getBar(1);
        if (currentBarView != null) {
            return currentBarView.isBarTouched();
        }
        BarView currentBarView2 = getBar(4);
        if (currentBarView2 != null) {
            return currentBarView2.isBarTouched();
        }
        return false;
    }

    public boolean isPreview4by3() {
        if (this.mGet == null) {
            return false;
        }
        String previewSize;
        if (this.mGet.getCameraState() == 6 || this.mGet.getCameraState() == 7) {
            previewSize = this.mGet.getCurrentSelectedVideoSize();
        } else {
            previewSize = this.mGet.getCurrentSelectedPreviewSize();
        }
        return Utils.calculate4by3Preview(previewSize);
    }

    public boolean isRearCamera() {
        if (this.mGet == null) {
            return false;
        }
        return this.mGet.isRearCamera();
    }

    public String getShotMode() {
        if (this.mGet == null) {
            return "mode_normal";
        }
        return this.mGet.getShotMode();
    }
}
