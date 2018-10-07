package com.lge.camera.managers;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.ICameraCallback.CameraImageMetaCallback;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;

public class LightFrameManager extends ManagerInterfaceImpl {
    protected static int sLightFrameRemainTime = 1332;
    private final int HALF = 2;
    private CameraImageMetaCallback mCameraImageMetaCallback = null;
    private GradientDrawable mGD = null;
    private OnTouchListener mLightFrameBackTouchListener = new C10422();
    private RotateImageView mLightFrameIconView = null;

    /* renamed from: com.lge.camera.managers.LightFrameManager$1 */
    class C10411 implements CameraImageMetaCallback {
        C10411() {
        }

        public void onImageMetaData(TotalCaptureResult result) {
            LightFrameManager.sLightFrameRemainTime = ((int) ((((float) ((Long) result.get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue()) / 1.0E9f) * 20000.0f)) + 300;
        }
    }

    /* renamed from: com.lge.camera.managers.LightFrameManager$2 */
    class C10422 implements OnTouchListener {
        C10422() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            return true;
        }
    }

    public LightFrameManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        View lf = this.mGet.findViewById(C0088R.id.full_light_frame);
        if (lf != null) {
            lf.setOnTouchListener(this.mLightFrameBackTouchListener);
            this.mLightFrameIconView = (RotateImageView) lf.findViewById(C0088R.id.lightFrame_preview_image);
        }
        this.mCameraImageMetaCallback = new C10411();
    }

    public int getLightFrameRemainTime() {
        return sLightFrameRemainTime;
    }

    public void turnOnLightFrame() {
        CamLog.m3d(CameraConstants.TAG, "-light- turn on lightFrame");
    }

    public CameraImageMetaCallback getImageMetaCallback() {
        return this.mCameraImageMetaCallback;
    }

    public void turnOffLightFrame() {
        CamLog.m3d(CameraConstants.TAG, "-light- turn off lightFrame");
    }

    private void loadLightBg() {
        this.mGD = (GradientDrawable) this.mGet.getActivity().getResources().getDrawable(C0088R.drawable.gradation);
        this.mGD.setGradientRadius((float) Utils.getLCDsize(getAppContext(), true)[0]);
    }

    private void unLoadLightBg() {
        if (this.mGD != null) {
            this.mGD = null;
        }
    }

    public void setBacklightToSystemSetting(Activity activity) {
        try {
            LayoutParams params = activity.getWindow().getAttributes();
            params.screenBrightness = -1.0f;
            activity.getWindow().setAttributes(params);
        } catch (Exception e) {
            CamLog.m4d(CameraConstants.TAG, "Fail to backlight control:", e);
        }
    }

    public void setBacklightToMax(Activity activity) {
        try {
            LayoutParams params = activity.getWindow().getAttributes();
            params.screenBrightness = 1.0f;
            activity.getWindow().setAttributes(params);
        } catch (Exception e) {
            CamLog.m4d(CameraConstants.TAG, "Fail to backlight control:", e);
        }
    }

    public boolean isLightFrameMode() {
        return false;
    }

    public int[] changeLightFramePreviewSize(int[] size) {
        int[] previewSize = size;
        previewSize[0] = previewSize[0] / 2;
        previewSize[1] = previewSize[1] / 2;
        return previewSize;
    }

    public void onResumeBefore() {
        CamLog.m3d(CameraConstants.TAG, "onResumeBefore");
        setDegree(getOrientationDegree(), false);
        showInitView();
    }

    public void showInitView() {
        if (!isLightFrameMode() || !CameraConstants.FILM_NONE.equals(this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR))) {
            return;
        }
        if (!FunctionProperties.isSupportedSwitchingAnimation() || !this.mGet.isAnimationShowing()) {
            turnOnLightFrame();
        }
    }

    public void onPauseBefore() {
        CamLog.m3d(CameraConstants.TAG, "onPauseBefore");
        if (FunctionProperties.isSupportedLightFrame()) {
            turnOffLightFrame();
        }
    }

    public void onConfigurationChanged(Configuration config) {
        CamLog.m3d(CameraConstants.TAG, "onConfigurationChanged");
        if (FunctionProperties.isSupportedLightFrame()) {
            super.onConfigurationChanged(config);
        }
    }

    public void setDegree(int degree, boolean animation) {
        if (this.mLightFrameIconView != null) {
            this.mLightFrameIconView.setDegree(degree, animation);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mCameraImageMetaCallback = null;
    }
}
