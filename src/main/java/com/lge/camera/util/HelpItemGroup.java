package com.lge.camera.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraParameters;
import com.lge.media.CamcorderProfileEx;
import java.util.ArrayList;
import java.util.List;

public class HelpItemGroup {
    public static final int PAGE_ORDER_360_PANORAMA = 15;
    public static final int PAGE_ORDER_AI_CAM = 4;
    public static final int PAGE_ORDER_CINEMA = 6;
    public static final int PAGE_ORDER_DUALPOP = 7;
    public static final int PAGE_ORDER_FLASH_JUMPCUT = 12;
    public static final int PAGE_ORDER_FOOD = 9;
    public static final int PAGE_ORDER_LAST = 20;
    public static final int PAGE_ORDER_LG_LENS = 2;
    public static final int PAGE_ORDER_MANUAL = 5;
    public static final int PAGE_ORDER_MULTIVIEW = 20;
    public static final int PAGE_ORDER_NORMAL = 0;
    public static final int PAGE_ORDER_OUTFOCUS = 3;
    public static final int PAGE_ORDER_PANORAMA = 11;
    public static final int PAGE_ORDER_POPOUT = 8;
    public static final int PAGE_ORDER_QUICK_SETTING = 1;
    public static final int PAGE_ORDER_SLOMO = 10;
    public static final int PAGE_ORDER_SNAP = 13;
    public static final int PAGE_ORDER_SQUARE_DUAL = 17;
    public static final int PAGE_ORDER_SQUARE_GRID = 18;
    public static final int PAGE_ORDER_SQUARE_GUIDE = 19;
    public static final int PAGE_ORDER_SQUARE_SNAP = 16;
    public static final int PAGE_ORDER_TIMELAPSE = 14;
    private ArrayList<String> mHelpGuideIdList = null;
    private ArrayList<HelpItem> mHelpListItemList = null;
    private ArrayList<String> mSupportedModeArray = null;
    private DataSetChangeNotifier sDataSetChangeNotifier = null;

    public interface DataSetChangeNotifier {
        void notifyDataSetConstructed();

        void notifyDataSetDestroyed();
    }

    public static class HelpItem {
        public String key;
        public List<Integer> mHelpDescription;
        public int mHelpImageId;
        public int mHelpNum;
        public int mHelpTitleId;

        public HelpItem() {
            this.key = null;
            this.mHelpNum = 0;
            this.mHelpTitleId = -1;
            this.mHelpImageId = 0;
            this.mHelpDescription = null;
            this.mHelpDescription = new ArrayList();
        }

        public HelpItem(String key) {
            this.key = null;
            this.mHelpNum = 0;
            this.mHelpTitleId = -1;
            this.mHelpImageId = 0;
            this.mHelpDescription = null;
            this.key = key;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HelpItem item = (HelpItem) o;
            if (this.key != null) {
                if (this.key.equals(item.key)) {
                    return true;
                }
            } else if (item.key == null) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.key != null ? this.key.hashCode() : -1;
        }
    }

    public void setDataSetChangeNotifier(DataSetChangeNotifier notifier) {
        this.sDataSetChangeNotifier = notifier;
    }

    public void initialize(CameraParameters params, int cameraId, Context context) {
        CamLog.m3d(CameraConstants.TAG, "START TIME = " + System.currentTimeMillis());
        if (this.mHelpListItemList == null) {
            this.mHelpListItemList = new ArrayList();
        }
        this.mHelpListItemList.clear();
        if (this.mSupportedModeArray == null) {
            this.mSupportedModeArray = new ArrayList();
        }
        this.mSupportedModeArray.clear();
        if (this.mHelpGuideIdList == null) {
            this.mHelpGuideIdList = new ArrayList();
        }
        this.mHelpGuideIdList.clear();
        String[] supportedModeList = ConfigurationUtil.sMODE_REAR_SUPPORTED_ITEMS;
        for (Object add : supportedModeList) {
            this.mSupportedModeArray.add(add);
        }
        makeHelpPageItems(context);
        if (this.sDataSetChangeNotifier != null) {
            this.sDataSetChangeNotifier.notifyDataSetConstructed();
        }
        CamLog.m3d(CameraConstants.TAG, "END TIME = " + System.currentTimeMillis());
    }

    protected boolean isOneOfCameraSupportVideoHdr(CameraParameters params) {
        if (params == null) {
            return false;
        }
        boolean[] isVideoHdrSupported = CameraDeviceUtils.isVideoHDRSupported(params);
        if (isVideoHdrSupported == null) {
            return false;
        }
        for (boolean supportVideoHdr : isVideoHdrSupported) {
            if (supportVideoHdr) {
                return true;
            }
        }
        return false;
    }

    public void makeHelpPageItems(Context context) {
        int i = 0;
        while (i <= 20) {
            HelpItem item = new HelpItem();
            if (i == 0) {
                item.key = "mode_normal";
                item.mHelpTitleId = C0088R.string.help_take_photos_title1;
                makeHelpDesc(context, item, i);
                if (!FunctionProperties.isSupportedConeUI()) {
                    item.mHelpImageId = C0088R.drawable.camera_setting_help_image_01_mode_shutter;
                } else if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO)) {
                    item.mHelpImageId = C0088R.drawable.camera_setting_help_image_01_mode_second_screen;
                } else {
                    item.mHelpImageId = C0088R.drawable.camera_help_image_mode_02;
                }
            } else if (i == 2 && FunctionProperties.isSupportedLGLens(context)) {
                item.key = "mode_normal";
                item.mHelpTitleId = C0088R.string.lg_lens;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_20_lg_lens;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_lg_lens_desc));
            } else if ((FunctionProperties.isSupportedRearOutfocus() || FunctionProperties.isSupportedFrontOutfocus()) && i == 3) {
                item.key = CameraConstants.MODE_FRONT_OUTFOCUS;
                item.mHelpTitleId = C0088R.string.portrait_outfocus;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_22_outfocus;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.value_up_front_outfocus_help));
            } else if (i == 4 && FunctionProperties.isSupportedSmartCam(context)) {
                item.key = CameraConstants.MODE_SMART_CAM;
                item.mHelpTitleId = C0088R.string.ai_cam2;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_21_ai_cam;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_ai_cam_desc));
                if (ModelProperties.isJoanRenewal()) {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_once));
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_ai_cam_scene_desc));
                }
            } else if (i == 1) {
                item.key = "mode_normal";
                item.mHelpTitleId = C0088R.string.help_quick_setting_title;
                if (ModelProperties.isLongLCDModel()) {
                    item.mHelpImageId = C0088R.drawable.camera_setting_help_image_19_settings;
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_quick_setting_desc));
                } else {
                    item.mHelpImageId = C0088R.drawable.camera_setting_help_image_19_settings_16_9;
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_quick_setting_16_9_desc));
                }
            } else if (i == 11 && (this.mSupportedModeArray.contains(CameraConstants.MODE_PANORAMA) || this.mSupportedModeArray.contains(CameraConstants.MODE_PANORAMA_LG) || this.mSupportedModeArray.contains(CameraConstants.MODE_PANORAMA_LG_RAW))) {
                item.key = CameraConstants.MODE_PANORAMA;
                item.mHelpTitleId = C0088R.string.shot_mode_panorama;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_02_panorama;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_panorama_desc1));
            } else if (i == 12 && this.mSupportedModeArray.contains(CameraConstants.MODE_FLASH_JUMPCUT)) {
                item.key = CameraConstants.MODE_FLASH_JUMPCUT;
                item.mHelpTitleId = C0088R.string.mode_flash_jump_cut;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_23_flash_jumpcut;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_flash_jump_cut));
            } else if (i == 13 && this.mSupportedModeArray.contains(CameraConstants.MODE_SNAP)) {
                item.key = CameraConstants.SNAP_VIDEO_KEY;
                item.mHelpTitleId = C0088R.string.shot_mode_snap_movie;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_03_snap_video;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_snap_desc1));
            } else if (i == 20 && FunctionProperties.isSupportedMode(CameraConstants.MODE_MULTIVIEW)) {
                item.key = CameraConstants.MULTIVIEW_KEY;
                item.mHelpTitleId = C0088R.string.camera_help_multiview_title;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_04_multiview;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_multi_view_desc1));
            } else if (i == 8 && !FunctionProperties.isSupportedMode(CameraConstants.MODE_DUAL_POP_CAMERA) && this.mSupportedModeArray.contains(CameraConstants.MODE_POPOUT_CAMERA)) {
                item.key = CameraConstants.MODE_POPOUT_CAMERA;
                item.mHelpTitleId = C0088R.string.help_title_popout;
                if (ModelProperties.isMTKChipset()) {
                    item.mHelpImageId = C0088R.drawable.camera_help_image_popout_k7;
                } else {
                    item.mHelpImageId = C0088R.drawable.camera_setting_help_image_05_popout;
                }
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_popout_desc1));
            } else if (i == 7 && FunctionProperties.isSupportedMode(CameraConstants.MODE_DUAL_POP_CAMERA)) {
                item.key = CameraConstants.MODE_DUAL_POP_CAMERA;
                item.mHelpTitleId = C0088R.string.mode_dual_pop_camera;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_05_dualpop;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.mode_dual_pop_camera_help));
            } else if (i == 10 && this.mSupportedModeArray.contains(CameraConstants.MODE_SLOW_MOTION)) {
                item.key = CameraConstants.MODE_SLOW_MOTION;
                item.mHelpTitleId = C0088R.string.help_slow_motion_title;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_06_slomo;
                if (CamcorderProfileEx.hasProfile(0, 10013)) {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_slow_motion_desc3));
                } else if (!CamcorderProfileEx.hasProfile(0, 10017) || CamcorderProfileEx.hasProfile(2, 10017)) {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_slow_motion_desc1));
                } else {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_slow_motion_desc2));
                }
            } else if (i == 14 && this.mSupportedModeArray.contains(CameraConstants.MODE_TIME_LAPSE_VIDEO)) {
                item.key = CameraConstants.MODE_TIME_LAPSE_VIDEO;
                item.mHelpTitleId = C0088R.string.time_lapse_video_title;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_07_timelapse;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_time_lapse_desc));
            } else if (i == 5 && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA)) {
                item.key = CameraConstants.MANUAL_KEY;
                item.mHelpTitleId = C0088R.string.camera_help_manual_mode_title1;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_08_manual_camera;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_manual_desc3));
                if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO)) {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_twice));
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.manual_video_quick_view_mode_button));
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_once));
                    item.mHelpImageId = C0088R.drawable.camera_setting_help_image_08_manual_video;
                    item.mHelpDescription.add(Integer.valueOf(AudioUtil.getNumOfMic(context) >= 3 ? C0088R.string.help_manual_partial_desc2 : C0088R.string.help_manual_partial_desc2_2mic));
                    if (FunctionProperties.isSupportedLogProfile()) {
                        item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_once));
                        item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_manual_partial_desc_lut_1));
                    } else if (FunctionProperties.isSupportedHDR10()) {
                        item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_once));
                        item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_manual_partial_desc_hdr10));
                    }
                }
                if (FunctionProperties.isSupportedGraphy()) {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_twice));
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_manual_partial_desc_graphy));
                }
            } else if (i == 9 && this.mSupportedModeArray.contains("mode_food")) {
                item.key = "mode_food";
                item.mHelpTitleId = C0088R.string.help_food_mode_title;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_11_camera_food;
                item.mHelpDescription.add(Integer.valueOf(FunctionProperties.isSupportedFilmRecording() ? C0088R.string.help_food_mode_description1 : C0088R.string.help_food_mode_description_no_recording));
            } else if (i == 15 && this.mSupportedModeArray.contains(CameraConstants.MODE_PANORAMA_LG_360_PROJ)) {
                item.key = CameraConstants.MODE_PANORAMA_LG_360_PROJ;
                item.mHelpTitleId = C0088R.string.help_360_panorama_title;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_10_camera_360_panorama;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_360_panorama_description));
            } else if (i == 16 && FunctionProperties.isSupportedMode(CameraConstants.MODE_SQUARE)) {
                item.key = CameraConstants.MODE_SQUARE_SNAPSHOT;
                item.mHelpTitleId = C0088R.string.snap_shot_help_title;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_13_snap;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_square_mode_snap_shot_desc));
            } else if (i == 17 && FunctionProperties.isSupportedMode(CameraConstants.MODE_SQUARE_SPLICE)) {
                item.key = CameraConstants.MODE_SQUARE_SPLICE;
                item.mHelpTitleId = C0088R.string.match_shot_help_title;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_14_dual;
                if (FunctionProperties.isSupportedCollageRecording()) {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_square_mode_dual_shot_desc));
                } else {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_square_mode_dual_shot_desc_photo_only));
                }
            } else if (i == 18 && FunctionProperties.isSupportedMode(CameraConstants.MODE_SQUARE)) {
                item.key = CameraConstants.MODE_SQUARE_GRID;
                item.mHelpTitleId = C0088R.string.grid_shot_help_title;
                item.mHelpImageId = Utils.isRTLLanguage() ? C0088R.drawable.camera_setting_help_image_15_grid_rtl : C0088R.drawable.camera_setting_help_image_15_grid;
                if (FunctionProperties.isSupportedCollageRecording()) {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_square_mode_grid_shot_desc));
                } else {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_square_mode_grid_shot_desc_photo_only));
                }
            } else if (i == 19 && FunctionProperties.isSupportedMode(CameraConstants.MODE_SQUARE)) {
                item.key = CameraConstants.MODE_SQUARE_OVERLAP;
                item.mHelpTitleId = C0088R.string.guide_shot_help_title;
                item.mHelpImageId = C0088R.drawable.camera_setting_help_image_16_guide;
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_square_mode_guide_shot_desc2));
            } else {
                if (i == 6 && this.mSupportedModeArray.contains(CameraConstants.MODE_CINEMA)) {
                    item.key = CameraConstants.MODE_CINEMA;
                    item.mHelpTitleId = C0088R.string.shot_mode_cine_video;
                    item.mHelpImageId = C0088R.drawable.camera_setting_help_image_17_cine_filter;
                    if (FunctionProperties.isSupportedHDR10()) {
                        item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_cine_video_hdr10_description));
                        item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_once));
                    }
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_cine_video_description));
                }
                i++;
            }
            item.mHelpNum = i;
            addPageItem(item);
            i++;
        }
    }

    private void makeHelpDesc(Context c, HelpItem item, int pageOrder) {
        boolean isKorean = "ko".equals(c.getResources().getConfiguration().locale.getLanguage());
        if (pageOrder == 0) {
            item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_take_photos_partial_desc));
            if (FunctionProperties.isSupportedFingerPrintMode(c)) {
                if (isKorean) {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_once));
                }
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_take_photos_partial_desc_fingerprint));
            }
            if (FunctionProperties.isSupportedHotKeyRecording(c)) {
                if (isKorean) {
                    item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_once));
                }
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_take_photos_partial_desc_hotkey1));
            }
            if (isKorean) {
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_once));
            } else {
                item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_space_once));
            }
            item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_take_photos_partial_desc_shutterzoom));
            item.mHelpDescription.add(Integer.valueOf(isKorean ? C0088R.string.help_enter_once : C0088R.string.help_enter_twice));
            item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_take_photos_partial_desc_selife));
            item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_twice));
            item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_tips));
            item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_enter_once));
            item.mHelpDescription.add(Integer.valueOf(C0088R.string.help_recording_preview_desc));
        }
    }

    public void addPageItem(HelpItem helpItem) {
        if (this.mHelpListItemList != null) {
            this.mHelpListItemList.add(helpItem);
        }
    }

    public void removeTapShotItem() {
        removePageItem(CameraConstants.TAP_SHOT_KEY);
    }

    public void removePageItem(String key) {
        if (this.mHelpListItemList != null) {
            int itemIndex = this.mHelpListItemList.indexOf(new HelpItem(key));
            if (itemIndex >= 0) {
                this.mHelpListItemList.remove(itemIndex);
            }
        }
    }

    public HelpItem getPageItem(int position) {
        return (HelpItem) this.mHelpListItemList.get(position);
    }

    public int getPageItemPosition(String key) {
        return this.mHelpListItemList.indexOf(new HelpItem(key));
    }

    public void removeUnusedPageItems(int position) {
        HelpItem item = getPageItem(position);
        this.mHelpListItemList.clear();
        this.mHelpListItemList.add(item);
    }

    public int getHelpListSize() {
        return this.mHelpListItemList == null ? 0 : this.mHelpListItemList.size();
    }

    public ArrayList<HelpItem> getHelpItemList() {
        return this.mHelpListItemList == null ? null : this.mHelpListItemList;
    }

    public void close() {
        if (this.mHelpListItemList != null) {
            this.mHelpListItemList.clear();
            this.mHelpListItemList = null;
        }
        if (this.mHelpGuideIdList != null) {
            this.mHelpGuideIdList.clear();
            this.mHelpGuideIdList = null;
        }
        if (this.mSupportedModeArray != null) {
            this.mSupportedModeArray.clear();
            this.mSupportedModeArray = null;
        }
        if (this.sDataSetChangeNotifier != null) {
            this.sDataSetChangeNotifier.notifyDataSetDestroyed();
            this.sDataSetChangeNotifier = null;
        }
        if (this.mHelpGuideIdList != null) {
            this.mHelpGuideIdList.clear();
            this.mHelpGuideIdList = null;
        }
    }

    public void setCameraHelpAppSetting(Context context, boolean set) {
        Editor edit = context.getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        edit.putBoolean(CameraConstants.SYSTEM_HELP_APP_SETTING_STATUS, set);
        edit.apply();
        CamLog.m3d(CameraConstants.TAG, "help-setting setCameraHelpSetting " + set);
    }
}
