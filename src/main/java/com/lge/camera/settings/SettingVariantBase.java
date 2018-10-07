package com.lge.camera.settings;

import android.content.Context;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;

public class SettingVariantBase {
    public void makePreferenceVariant(Context context, PreferenceGroup prefGroup) {
        if (context != null && prefGroup != null) {
            try {
                addPictureSizePreference(context, prefGroup);
                addPictureSizePreferenceForSubCamera(context, prefGroup);
                addVideoSizePreference(context, prefGroup);
                addVideoSizePreferenceForSubCamera(context, prefGroup);
                addTimerKeyMenu(context, prefGroup);
                addCheeseShutterMenu(context, prefGroup);
                addFocusMenu(context, prefGroup);
                addTrackingAFMenu(context, prefGroup);
                addLgWbMenu(context, prefGroup);
                addSaveDirectionMenu(context, prefGroup);
                addTagLocationMenu(context, prefGroup);
                addStorageMenu(context, prefGroup);
                addTilePreviewMenu(context, prefGroup);
                addAuCloudMenu(context, prefGroup);
                addFlashMenu(context, prefGroup);
                addModeMenu(context, prefGroup);
                addSwapMenu(context, prefGroup);
                addHDRMenu(context, prefGroup);
                addFrameGridMenu(context, prefGroup);
                addLightFrameMenu(context, prefGroup);
                addBeautyShotMenu(context, prefGroup);
                addMotionQuickViewerMenu(context, prefGroup);
                addShutterlessSelfieMenu(context, prefGroup);
                addShutterSpeedMenu(context, prefGroup);
                addLGEVCtrlMenu(context, prefGroup);
                addLGISOMenu(context, prefGroup);
                addSavingRAWPictureMenu(context, prefGroup);
                addHistogramMenu(context, prefGroup);
                addInclometerMenu(context, prefGroup);
                addManualFocusStepMenu(context, prefGroup);
                addManualVideoRatioMenu(context, prefGroup);
                addManualVideoFramerateMenu(context, prefGroup);
                addManualVideoBitrateMenu(context, prefGroup);
                addManualVideoSteadyMenu(context, prefGroup);
                addManualVideoAudioMenu(context, prefGroup);
                addVideoLgWbMenu(context, prefGroup);
                addVideoLGEVCtrlMenu(context, prefGroup);
                addVideoManualFocusStepMenu(context, prefGroup);
                addVideoLGISOMenu(context, prefGroup);
                addVideoShutterSpeedMenu(context, prefGroup);
                addMultiviewMenu(context, prefGroup);
                addMultiviewIntervalMenu(context, prefGroup);
                addFilmEmulatorMenu(context, prefGroup);
                if (FunctionProperties.isSignatureSupported(context)) {
                    addSignatureMenu(context, prefGroup);
                }
                addPanoramaSoundRecordwMenu(context, prefGroup);
                addSquarePictureSizeMenu(context, prefGroup);
                addSquareVideoSizeMenu(context, prefGroup);
                addSlowMotionVideoSizeMenu(context, prefGroup);
                addStickerMenu(context, prefGroup);
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "Preference add exception : ", e);
            }
        }
    }

    public void addCheeseShutterMenu(Context context, PreferenceGroup prefGroup) {
        if (!FunctionProperties.isSupportedVoiceShutter()) {
            return;
        }
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference(Setting.KEY_VOICESHUTTER) == null) {
            prefGroup.addChild(PrefMaker.makeCheeseShutterPreference(context, prefGroup));
        }
    }

    public void addPictureSizePreference(Context context, PreferenceGroup prefGroup) {
        if (prefGroup.findPreference("picture-size") == null) {
            ListPreference listPref;
            if ("rear".equals(prefGroup.getSharedPreferenceName())) {
                listPref = SizePrefMaker.makeRearPictureSizePreference(context, prefGroup);
            } else {
                listPref = SizePrefMaker.makeFrontPictureSizePreference(context, prefGroup);
            }
            prefGroup.addChild(listPref);
        }
    }

    public void addPictureSizePreferenceForSubCamera(Context context, PreferenceGroup prefGroup) {
        if (prefGroup.findPreference(Setting.KEY_CAMERA_PICTURESIZE_SUB) == null) {
            ListPreference listPref;
            if ("rear".equals(prefGroup.getSharedPreferenceName())) {
                listPref = SizePrefMaker.makeRearPictureSizePreferenceForSub(context, prefGroup);
            } else {
                listPref = SizePrefMaker.makeFrontPictureSizePreferenceForSub(context, prefGroup);
            }
            prefGroup.addChild(listPref);
        }
    }

    public void addVideoSizePreference(Context context, PreferenceGroup prefGroup) {
        if (prefGroup.findPreference(Setting.KEY_VIDEO_RECORDSIZE) == null) {
            ListPreference listPref;
            if ("rear".equals(prefGroup.getSharedPreferenceName())) {
                listPref = SizePrefMaker.makeRearVideoSizePreference(context, prefGroup);
            } else {
                listPref = SizePrefMaker.makeFrontVideoSizePreference(context, prefGroup);
            }
            prefGroup.addChild(listPref);
        }
    }

    public void addVideoSizePreferenceForSubCamera(Context context, PreferenceGroup prefGroup) {
        if (prefGroup.findPreference(Setting.KEY_VIDEO_RECORDSIZE_SUB) == null) {
            ListPreference listPref;
            if ("rear".equals(prefGroup.getSharedPreferenceName())) {
                listPref = SizePrefMaker.makeRearVideoSizePreferenceForSub(context, prefGroup);
            } else {
                listPref = SizePrefMaker.makeFrontVideoSizePreferenceForSub(context, prefGroup);
            }
            prefGroup.addChild(listPref);
        }
    }

    public void addFocusMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && prefGroup.findPreference("focus-mode") == null) {
            prefGroup.addChild(PrefMaker.makeFocusPreference(context, prefGroup));
        }
    }

    public void addTrackingAFMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference("tracking-af") == null) {
            prefGroup.addChild(PrefMaker.makeTrackingAFPreference(context, prefGroup));
        }
    }

    public void addLgWbMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference("lg-wb") == null) {
            prefGroup.addChild(PrefMaker.makeLgWbPreference(context, prefGroup, "lg-wb"));
        }
    }

    public void addSaveDirectionMenu(Context context, PreferenceGroup prefGroup) {
        if ("front".equals(prefGroup.getSharedPreferenceName()) && prefGroup.findPreference(Setting.KEY_SAVE_DIRECTION) == null) {
            prefGroup.addChild(PrefMaker.makeSaveDirectionPreference(context, prefGroup));
        }
    }

    public void addTagLocationMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference(Setting.KEY_TAG_LOCATION) == null) {
            prefGroup.addChild(PrefMaker.makeTaglocationPreference(context, prefGroup));
        }
    }

    public void addTilePreviewMenu(Context context, PreferenceGroup prefGroup) {
        if (!FunctionProperties.isSupportedCameraRoll()) {
            return;
        }
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference(Setting.KEY_TILE_PREVIEW) == null) {
            prefGroup.addChild(PrefMaker.makeTilePreviewPreference(context, prefGroup));
        }
    }

    public void addStorageMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference(Setting.KEY_STORAGE) == null) {
            prefGroup.addChild(PrefMaker.makeStoragePreference(context, prefGroup));
        }
    }

    public void addAuCloudMenu(Context context, PreferenceGroup prefGroup) {
        if (ModelProperties.getCarrierCode() == 7) {
            FunctionProperties.checkAuClude(context);
            if (FunctionProperties.isSupportedAuClude() && prefGroup.findPreference(Setting.KEY_AU_CLOUD) == null) {
                prefGroup.addChild(PrefMaker.makeAuCloudPreference(context, prefGroup));
            }
        }
    }

    public void addFlashMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference("flash-mode") == null) {
            prefGroup.addChild(PrefMaker.makeFlashPreference(context, prefGroup));
        }
    }

    public void addLightFrameMenu(Context context, PreferenceGroup prefGroup) {
        if ("front".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedLightFrame() && prefGroup.findPreference(Setting.KEY_LIGHTFRAME) == null) {
            prefGroup.addChild(PrefMaker.makeLightFrameFlashPreference(context, prefGroup));
        }
    }

    public void addModeMenu(Context context, PreferenceGroup prefGroup) {
        if (prefGroup.findPreference(Setting.KEY_MODE) == null) {
            prefGroup.addChild(ModePrefMaker.makeModePreference(context, prefGroup, "rear".equals(prefGroup.getSharedPreferenceName())));
        }
    }

    public void addMultiviewMenu(Context context, PreferenceGroup prefGroup) {
        CamLog.m3d(CameraConstants.TAG, "addMultiviewMenu");
        if ((FunctionProperties.isSupportedMode(CameraConstants.MODE_MULTIVIEW) || FunctionProperties.isSupportedMode(CameraConstants.MODE_SQUARE)) && prefGroup.findPreference(Setting.KEY_MULTIVIEW_LAYOUT) == null) {
            ListPreference listPref = MultiViewPrefMaker.MultiViewPreference(context, prefGroup);
            CamLog.m3d(CameraConstants.TAG, "MultiViewPreference listPref");
            prefGroup.addChild(listPref);
        }
    }

    public void addMultiviewIntervalMenu(Context context, PreferenceGroup prefGroup) {
        if ((FunctionProperties.isSupportedMode(CameraConstants.MODE_MULTIVIEW) || FunctionProperties.isSupportedMode(CameraConstants.MODE_SQUARE)) && prefGroup.findPreference(Setting.KEY_MULTIVIEW_FRAMESHOT) == null) {
            prefGroup.addChild(PrefMaker.makeMultiviewIntervalPreference(context, prefGroup));
        }
    }

    public void addSwapMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference(Setting.KEY_SWAP_CAMERA) == null) {
            prefGroup.addChild(PrefMaker.makeSwapPreference(context, prefGroup));
        }
    }

    public void addHDRMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference("hdr-mode") == null) {
            ListPreference listPref;
            if (FunctionProperties.isSupportedHDR("rear".equals(prefGroup.getSharedPreferenceName())) == 3) {
                listPref = PrefMaker.makeHDRAutoPreference(context, prefGroup);
            } else {
                listPref = PrefMaker.makeHDRPreference(context, prefGroup);
            }
            prefGroup.addChild(listPref);
        }
    }

    public void addFrameGridMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference(Setting.KEY_FRAME_GRID) == null) {
            prefGroup.addChild(PrefMaker.makeFrameGridPreference(context, prefGroup));
        }
    }

    public void addTimerKeyMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference(Setting.KEY_TIMER) == null) {
            prefGroup.addChild(PrefMaker.makeTimerPreference(context, prefGroup));
        }
    }

    public void addFullVisionMenu(Context context, PreferenceGroup prefGroup) {
        if (!ModelProperties.isLongLCDModel()) {
            return;
        }
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference(Setting.KEY_FULLVISION) == null) {
            prefGroup.addChild(PrefMaker.makeFullVisionPreference(context, prefGroup));
        }
    }

    public void addSignatureMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference(Setting.KEY_SIGNATURE) == null) {
            prefGroup.addChild(PrefMaker.makeSignaturePreference(context, prefGroup));
        }
    }

    public void addBeautyShotMenu(Context context, PreferenceGroup prefGroup) {
        if ("front".equals(prefGroup.getSharedPreferenceName())) {
            if (prefGroup.findPreference(Setting.KEY_BEAUTYSHOT) == null) {
                prefGroup.addChild(PrefMaker.makeBeautyshotPreference(context, prefGroup));
            }
            if (prefGroup.findPreference(Setting.KEY_RELIGHTING) == null) {
                prefGroup.addChild(PrefMaker.makeBeautyRelightingPreference(context, prefGroup));
            }
        }
    }

    public void addMotionQuickViewerMenu(Context context, PreferenceGroup prefGroup) {
        if ("front".equals(prefGroup.getSharedPreferenceName()) && prefGroup.findPreference(Setting.KEY_MOTION_QUICKVIEWER) == null) {
            prefGroup.addChild(PrefMaker.makeMotionQuickViewPreference(context, prefGroup));
        }
    }

    public void addShutterlessSelfieMenu(Context context, PreferenceGroup prefGroup) {
        if ("front".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isShutterlessSupported(context) && prefGroup.findPreference(Setting.KEY_SHUTTERLESS_SELFIE) == null) {
            prefGroup.addChild(PrefMaker.makeShutterlessSelfiePreference(context, prefGroup));
        }
    }

    public void addShutterSpeedMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && prefGroup.findPreference("shutter-speed") == null) {
            prefGroup.addChild(PrefMaker.makeShutterSpeedPreference(context, prefGroup, "shutter-speed"));
        }
    }

    public void addLGEVCtrlMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && prefGroup.findPreference(Setting.KEY_LG_EV_CTRL) == null) {
            prefGroup.addChild(PrefMaker.makeEVCtrlPreference(context, prefGroup, Setting.KEY_LG_EV_CTRL));
        }
    }

    public void addLGISOMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && prefGroup.findPreference(Setting.KEY_LG_MANUAL_ISO) == null) {
            prefGroup.addChild(PrefMaker.makeLGISOPreference(context, prefGroup, Setting.KEY_LG_MANUAL_ISO));
        }
    }

    public void addSavingRAWPictureMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedRAWPictureSaving() && prefGroup.findPreference(Setting.KEY_RAW_PICTURE) == null) {
            prefGroup.addChild(PrefMaker.makeSavingRAWPicture(context, prefGroup));
        }
    }

    public void addHistogramMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && prefGroup.findPreference(Setting.KEY_HISTOGRAM) == null) {
            prefGroup.addChild(PrefMaker.makeHistogramPreference(context, prefGroup));
        }
    }

    public void addInclometerMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && prefGroup.findPreference(Setting.KEY_INCLINOMETER) == null) {
            prefGroup.addChild(PrefMaker.makeInclinometerPreference(context, prefGroup));
        }
    }

    public void addManualFocusStepMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && prefGroup.findPreference(Setting.KEY_MANUAL_FOCUS_STEP) == null) {
            prefGroup.addChild(PrefMaker.makeManualFocusStepPreference(context, prefGroup, Setting.KEY_MANUAL_FOCUS_STEP));
        }
    }

    public void addManualVideoRatioMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) && prefGroup.findPreference(Setting.KEY_MANUAL_VIDEO_SIZE) == null) {
            prefGroup.addChild(PrefMaker.makeManualVideoRatioPreference(context, prefGroup));
        }
    }

    public void addManualVideoFramerateMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) && prefGroup.findPreference(Setting.KEY_MANUAL_VIDEO_FRAME_RATE) == null) {
            prefGroup.addChild(PrefMaker.makeManualVideoFrameRatePreference(context, prefGroup));
        }
    }

    public void addManualVideoBitrateMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) && prefGroup.findPreference(Setting.KEY_MANUAL_VIDEO_BITRATE) == null) {
            prefGroup.addChild(PrefMaker.makeManualVideoBitratePreference(context, prefGroup));
        }
    }

    public void addManualVideoSteadyMenu(Context context, PreferenceGroup prefGroup) {
        if (FunctionProperties.isSupportedSteadyCamera(true) && prefGroup.findPreference(Setting.KEY_VIDEO_STEADY) == null) {
            prefGroup.addChild(PrefMaker.makeManualVideoSteadyCamPreference(context, prefGroup));
        }
    }

    public void addManualVideoAudioMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) && prefGroup.findPreference(Setting.KEY_MANUAL_VIDEO_AUDIO) == null) {
            prefGroup.addChild(PrefMaker.makeManualVideoAudioPreference(context, prefGroup));
        }
    }

    public void addVideoLgWbMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) && prefGroup.findPreference(Setting.KEY_VIDEO_LG_WB) == null) {
            prefGroup.addChild(PrefMaker.makeLgWbPreference(context, prefGroup, Setting.KEY_VIDEO_LG_WB));
        }
    }

    public void addVideoLGEVCtrlMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) && prefGroup.findPreference(Setting.KEY_VIDEO_LG_EV_CTRL) == null) {
            prefGroup.addChild(PrefMaker.makeEVCtrlPreference(context, prefGroup, Setting.KEY_VIDEO_LG_EV_CTRL));
        }
    }

    public void addVideoManualFocusStepMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) && prefGroup.findPreference(Setting.KEY_VIDEO_MANUAL_FOCUS_STEP) == null) {
            prefGroup.addChild(PrefMaker.makeManualFocusStepPreference(context, prefGroup, Setting.KEY_VIDEO_MANUAL_FOCUS_STEP));
        }
    }

    public void addVideoLGISOMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) && prefGroup.findPreference(Setting.KEY_VIDEO_LG_MANUAL_ISO) == null) {
            prefGroup.addChild(PrefMaker.makeLGISOPreference(context, prefGroup, Setting.KEY_VIDEO_LG_MANUAL_ISO));
        }
    }

    public void addVideoShutterSpeedMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO) && prefGroup.findPreference(Setting.KEY_VIDEO_SHUTTER_SPEED) == null) {
            prefGroup.addChild(PrefMaker.makeShutterSpeedPreference(context, prefGroup, Setting.KEY_VIDEO_SHUTTER_SPEED));
        }
    }

    public void addFilmEmulatorMenu(Context context, PreferenceGroup prefGroup) {
        if (("rear".equals(prefGroup.getSharedPreferenceName()) || "front".equals(prefGroup.getSharedPreferenceName())) && prefGroup.findPreference(Setting.KEY_FILM_EMULATOR) == null) {
            prefGroup.addChild(PrefMaker.makeSelfieFilterPreference(context, prefGroup));
        }
    }

    public void addPanoramaSoundRecordwMenu(Context context, PreferenceGroup prefGroup) {
        if ("rear".equals(prefGroup.getSharedPreferenceName()) && prefGroup.findPreference(Setting.KEY_PANO_SOUND_REC) == null) {
            prefGroup.addChild(PrefMaker.makePanoramaSoundRecordPreference(context, prefGroup));
        }
    }

    public void addSquarePictureSizeMenu(Context context, PreferenceGroup prefGroup) {
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_SQUARE) && prefGroup.findPreference(Setting.KEY_SQUARE_PICTURE_SIZE) == null) {
            ListPreference listPref;
            if ("rear".equals(prefGroup.getSharedPreferenceName())) {
                listPref = SizePrefMaker.makeSquareRearPictureSizePreference(context, prefGroup);
            } else {
                listPref = SizePrefMaker.makeSquareFrontPictureSizePreference(context, prefGroup);
            }
            prefGroup.addChild(listPref);
        }
    }

    public void addSquareVideoSizeMenu(Context context, PreferenceGroup prefGroup) {
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_SQUARE) && prefGroup.findPreference(Setting.KEY_SQUARE_VIDEO_SIZE) == null) {
            ListPreference listPref;
            if ("rear".equals(prefGroup.getSharedPreferenceName())) {
                listPref = SizePrefMaker.makeSquareRearVideoSizePreference(context, prefGroup);
            } else {
                listPref = SizePrefMaker.makeSquareFrontVideoSizePreference(context, prefGroup);
            }
            prefGroup.addChild(listPref);
        }
    }

    public void addSlowMotionVideoSizeMenu(Context context, PreferenceGroup prefGroup) {
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_SLOW_MOTION) && FunctionProperties.enableSlowMotionVideoSizeMenu() && !"front".equals(prefGroup.getSharedPreferenceName()) && prefGroup.findPreference(Setting.KEY_SLOW_MOTION_VIDEO_SIZE) == null) {
            prefGroup.addChild(SizePrefMaker.makeSlowMotionVideoSizePreference(context, prefGroup));
        }
    }

    public void addStickerMenu(Context context, PreferenceGroup prefGroup) {
        if (FunctionProperties.isSupportedSticker() && prefGroup.findPreference(Setting.KEY_STICKER) == null) {
            prefGroup.addChild(PrefMaker.makeStickerPreference(context, prefGroup));
        }
    }
}
