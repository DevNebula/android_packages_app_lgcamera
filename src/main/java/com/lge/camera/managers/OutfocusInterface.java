package com.lge.camera.managers;

import com.lge.camera.C0088R;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUpdater;

abstract class OutfocusInterface extends ManagerInterfaceImpl {
    private static double VALID_DISTANCE_FEET = 4.0d;
    protected int mBlurLevel = 0;
    protected int mDefaultBlurLevel = 0;

    public OutfocusInterface(ModuleInterface moduleInterface, int defaultBlurLevel) {
        super(moduleInterface);
        this.mDefaultBlurLevel = defaultBlurLevel;
        this.mBlurLevel = defaultBlurLevel;
    }

    public void setBlurLevel(int level) {
        this.mBlurLevel = level;
    }

    public int getBlurLevel() {
        return this.mBlurLevel;
    }

    public int getBlurLevelForParam() {
        return this.mBlurLevel * 10;
    }

    public void initParamUpdater(ParamUpdater paramUpdater) {
        if (paramUpdater != null) {
            paramUpdater.addRequester(ParamConstants.KEY_OUTFOCUS, "on", false, true);
            paramUpdater.addRequester(ParamConstants.KEY_OUTFOCUS_LEVEL, "" + getBlurLevelForParam(), false, true);
        }
    }

    public void restoreParam(ParamUpdater paramUpdater) {
        if (paramUpdater != null) {
            paramUpdater.setParamValue(ParamConstants.KEY_OUTFOCUS, "off");
            paramUpdater.setParamValue(ParamConstants.KEY_OUTFOCUS_LEVEL, "0");
        }
        CameraProxy device = this.mGet.getCameraDevice();
        if (device != null) {
            CameraParameters params = device.getParameters();
            if (params != null) {
                params.set(ParamConstants.KEY_OUTFOCUS, "off");
                params.set(ParamConstants.KEY_OUTFOCUS_LEVEL, "0");
                device.setParameters(params);
            }
        }
    }

    public void removeParamUpdater(ParamUpdater paramUpdater) {
        if (paramUpdater != null) {
            paramUpdater.removeRequester(ParamConstants.KEY_OUTFOCUS);
            paramUpdater.removeRequester(ParamConstants.KEY_OUTFOCUS_LEVEL);
        }
    }

    public String getErrorMessage(int errorType) {
        boolean isRearOutfocus = this.mGet.isRearCamera();
        int resId = isRearOutfocus ? C0088R.string.outfocus_recognize_error : C0088R.string.outfocus_recognize_error1;
        if (errorType == 0) {
            return null;
        }
        if ((errorType & 8) != 0) {
            resId = C0088R.string.outfocus_lens_covered_error;
        } else if ((errorType & 4) != 0) {
            resId = isRearOutfocus ? C0088R.string.outfocus_low_light_error : C0088R.string.outfocus_low_light_error1;
        } else if ((errorType & 2) != 0) {
            resId = C0088R.string.outfocus_distance_near_error;
        }
        return this.mGet.getActivity().getString(resId);
    }

    public String getLDBExtraString() {
        return "outfocus_blur_level=" + getBlurLevelForParam();
    }
}
