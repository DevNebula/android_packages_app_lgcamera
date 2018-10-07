package com.lge.camera.settings;

import android.content.Context;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.ParamConstants;
import java.util.ArrayList;

public class PrefMaker {
    public static ListPreference makeCheeseShutterPreference(Context context, PreferenceGroup prefGroup) {
        String guideText;
        int titleId = ModelProperties.useCheeseShutterTitle() ? C0088R.string.sp_cheeseshutter_NORMAL : C0088R.string.sp_voiceshutter_NORMAL;
        int descId = ModelProperties.useCheeseShutterTitle() ? C0088R.array.cheeseshutter_description : C0088R.array.voiceshutter_description;
        String wordCheese = context.getString(C0088R.string.sp_voiceshutter_sound_cheese_NORMAL);
        String wordSmile = context.getString(C0088R.string.sp_voiceshutter_sound_smile_NORMAL);
        String wordWhisky = context.getString(C0088R.string.sp_voiceshutter_sound_whisky_NORMAL);
        String wordKimchi = context.getString(C0088R.string.sp_voiceshutter_sound_kimchi_NORMAL);
        String wordLg = context.getString(C0088R.string.sp_voiceshutter_sound_LG_NORMAL);
        String wordTorimasu = context.getString(C0088R.string.sp_voiceshutter_sound_torimasu_NORMAL);
        if (ModelProperties.isSupportVoiceShutterJapanese()) {
            guideText = String.format(context.getString(C0088R.string.setting_guide_cheese_shutter_3items), new Object[]{wordCheese, wordSmile, wordTorimasu});
        } else if (ModelProperties.isSupportVoiceShutterAME()) {
            guideText = String.format(context.getString(C0088R.string.setting_guide_cheese_shutter_4items), new Object[]{wordCheese, wordSmile, wordKimchi, wordLg});
        } else {
            guideText = String.format(context.getString(C0088R.string.setting_guide_cheese_shutter_NORMAL), new Object[]{wordCheese, wordSmile, wordWhisky, wordKimchi, wordLg});
        }
        return PrefMakerUtil.makePreference(context, prefGroup, Setting.KEY_VOICESHUTTER, titleId, -1, -1, C0088R.array.cheeseshutter_entries, C0088R.array.cheeseshutter_entryValues, -1, C0088R.string.cheeseshutter_default, true, descId, guideText);
    }

    public static ListPreference makeFocusPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreference(context, prefGroup, "focus-mode", C0088R.string.focus, -1, -1, C0088R.array.focus_entries, C0088R.array.focus_entryValues, -1, C0088R.string.focus_default, true, -1, null);
    }

    public static ListPreference makeTrackingAFPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreference(context, prefGroup, "tracking-af", C0088R.string.tracking_af, -1, -1, C0088R.array.tracking_af_entries, C0088R.array.tracking_af_entryValues, -1, C0088R.string.tracking_af_default, true, C0088R.array.tracking_af_description, context.getString(C0088R.string.setting_guide_tracking_AF_auto));
    }

    public static ListPreference makeLgWbPreference(Context context, PreferenceGroup prefGroup, String key) {
        String defaultValue = "0";
        if (key == null) {
            key = "lg-wb";
        }
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, key, C0088R.string.white_balance, null, null, null, null, null, defaultValue, true, null, null);
    }

    public static ListPreference makeTimerPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreference(context, prefGroup, Setting.KEY_TIMER, C0088R.string.timer, -1, -1, C0088R.array.timer_entries, C0088R.array.timer_entryValues, C0088R.array.timer_indicatorIcons, C0088R.string.timer_default, false, C0088R.array.timer_description, null);
    }

    public static ListPreference makeTaglocationPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreference(context, prefGroup, Setting.KEY_TAG_LOCATION, C0088R.string.tag_locations_title, -1, -1, C0088R.array.taglocation_entries, C0088R.array.taglocation_entryValues, -1, C0088R.string.taglocation_default, true, -1, null);
    }

    public static ListPreference makeSignaturePreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreference(context, prefGroup, Setting.KEY_SIGNATURE, C0088R.string.setting_signature_title, -1, -1, C0088R.array.signature_entries, C0088R.array.signature_entryValues, -1, C0088R.string.signature_default, true, -1, null, true);
    }

    public static ListPreference makeFullVisionPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreference(context, prefGroup, Setting.KEY_FULLVISION, C0088R.string.quick_setting_title_fullvision, -1, -1, C0088R.array.fullvision_entries, C0088R.array.fullvision_entryValues, -1, C0088R.string.fullvision_default, true, -1, null);
    }

    public static ListPreference makeTilePreviewPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        String[] entriyValues = new String[]{"on", "off"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        String guideText = context.getString(C0088R.string.camera_roll_description);
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_TILE_PREVIEW, C0088R.string.camera_roll, null, null, entries, entriyValues, null, "off", true, desc, guideText);
    }

    public static ListPreference makeStoragePreference(Context context, PreferenceGroup prefGroup) {
        ArrayList<String> entryList = new ArrayList();
        entryList.add(context.getResources().getString(C0088R.string.sp_external_memory_NORMAL));
        entryList.add(context.getResources().getString(C0088R.string.sp_internal_storage_NORMAL));
        if (ModelProperties.isLguCloudServiceModel()) {
            entryList.add(context.getResources().getString(C0088R.string.sp_external_uplus_cloud_NORMAL1));
        }
        String[] entries = (String[]) entryList.toArray(new String[entryList.size()]);
        ArrayList<String> entriyValueList = new ArrayList();
        entriyValueList.add(CameraConstants.STORAGE_NAME_EXTERNAL);
        entriyValueList.add(CameraConstants.STORAGE_NAME_INTERNAL);
        if (ModelProperties.isLguCloudServiceModel()) {
            entriyValueList.add(CameraConstants.STORAGE_NAME_NAS);
        }
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_STORAGE, C0088R.string.sp_storage_NORMAL, null, null, entries, (String[]) entriyValueList.toArray(new String[entriyValueList.size()]), null, CameraConstants.STORAGE_NAME_EXTERNAL, true, (String[]) entryList.toArray(new String[entryList.size()]), null);
    }

    public static ListPreference makeSaveDirectionPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_SAVE_DIRECTION, C0088R.string.sp_save_as_flipped_NORMAL, null, null, new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on)}, new String[]{"off", "on"}, null, FunctionProperties.getFrontFlipDefaultValue(), true, new String[]{context.getResources().getString(C0088R.string.save_as_fliped_off), context.getResources().getString(C0088R.string.save_as_fliped_on)}, null);
    }

    public static ListPreference makeAuCloudPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_AU_CLOUD, C0088R.string.sp_auto_upload_NORMAL, null, null, null, null, null, null, false, null, null);
    }

    public static ListPreference makeFlashPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, "flash-mode", C0088R.string.flash, new int[]{C0088R.drawable.setting_integrate_parent_flash_off_button, C0088R.drawable.setting_integrate_parent_flash_on_button, C0088R.drawable.setting_integrate_parent_flash_auto_button, C0088R.drawable.setting_integrate_parent_flash_rear_on_button}, null, new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.auto), context.getResources().getString(C0088R.string.rear_on)}, new String[]{"off", "on", "auto", ParamConstants.FLASH_MODE_REAR_ON}, null, ModelProperties.getDefaultFlashValue(), true, new String[]{context.getResources().getString(C0088R.string.flash_off), context.getResources().getString(C0088R.string.flash_on), context.getResources().getString(C0088R.string.flash_auto), context.getResources().getString(C0088R.string.rear_on)}, null);
    }

    public static ListPreference makeLightFrameFlashPreference(Context context, PreferenceGroup prefGroup) {
        int[] menuIcons = new int[]{C0088R.drawable.setting_integrate_parent_flash_off_button, C0088R.drawable.setting_integrate_parent_flash_on_button};
        String[] entries = new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on)};
        String[] entriyValues = new String[]{"off", "on"};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_LIGHTFRAME, C0088R.string.flash, menuIcons, null, entries, entriyValues, null, "off", true, null, null);
    }

    public static ListPreference makeSwapPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.sp_rear_NORMAL), context.getResources().getString(C0088R.string.sp_front_NORMAL)};
        String[] entriyValues = new String[]{"rear", "front"};
        int[] menuIcon = new int[]{C0088R.drawable.btn_quickbutton_swap_button, C0088R.drawable.btn_quickbutton_swap_button};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.switch_to_front), context.getResources().getString(C0088R.string.switch_to_back)};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_SWAP_CAMERA, C0088R.string.dual_camera_select, menuIcon, null, entries, entriyValues, null, "rear", true, desc, null);
    }

    public static ListPreference makeHDRAutoPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, "hdr-mode", C0088R.string.title_hdr, null, null, new String[]{context.getResources().getString(C0088R.string.auto), context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.not_use)}, new String[]{"2", "1", "0"}, null, ModelProperties.getAppTier() >= 3 ? "2" : "0", true, new String[]{context.getResources().getString(C0088R.string.hdr_auto), context.getResources().getString(C0088R.string.hdr_on), context.getResources().getString(C0088R.string.hdr_off)}, context.getString(C0088R.string.setting_guide_text_hdr_PrDdefault));
    }

    public static ListPreference makeManualVideoSteadyCamPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        String[] entriyValues = new String[]{"on", "off"};
        String[] descValues = new String[]{context.getResources().getString(C0088R.string.steady_record_on), context.getResources().getString(C0088R.string.steady_record_off)};
        String guideText = context.getString(C0088R.string.setting_guide_steady_recording1);
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_VIDEO_STEADY, C0088R.string.steady_recording_help_title, null, null, entries, entriyValues, null, "on", true, descValues, guideText);
    }

    public static ListPreference makeHDRPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, "hdr-mode", C0088R.string.title_hdr, null, null, new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on)}, new String[]{"0", "1"}, null, "0", true, new String[]{context.getResources().getString(C0088R.string.hdr_off), context.getResources().getString(C0088R.string.hdr_on)}, context.getString(C0088R.string.setting_guide_text_hdr_PrDdefault));
    }

    public static ListPreference makeFrameGridPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        String[] entriyValues = new String[]{"on", "off"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.framegrid_on), context.getResources().getString(C0088R.string.framegrid_off)};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_FRAME_GRID, C0088R.string.setting_grid, null, null, entries, entriyValues, null, "off", true, desc, null);
    }

    public static ListPreference makeBeautyshotPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_BEAUTYSHOT, C0088R.string.beauty, null, null, null, null, null, "" + ModelProperties.getBeautyDefaultLevel(), true, null, null);
    }

    public static ListPreference makeBeautyRelightingPreference(Context context, PreferenceGroup prefGroup) {
        int relightingDefaultLevel;
        String str = Setting.KEY_RELIGHTING;
        StringBuilder append = new StringBuilder().append("");
        if (FunctionProperties.isSupportedRelighting()) {
            relightingDefaultLevel = ModelProperties.getRelightingDefaultLevel();
        } else {
            relightingDefaultLevel = 0;
        }
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, str, C0088R.string.beauty, null, null, null, null, null, append.append(relightingDefaultLevel).toString(), true, null, null);
    }

    public static ListPreference makeMotionQuickViewPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on)};
        String[] entriyValues = new String[]{"off", "on"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.setting_gesture_view_off), context.getResources().getString(C0088R.string.setting_gesture_view_on)};
        String guideText = context.getString(C0088R.string.setting_guide_gesture_view);
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_MOTION_QUICKVIEWER, C0088R.string.camera_help_activity_gesture_view, null, null, entries, entriyValues, null, "off", true, desc, guideText);
    }

    public static ListPreference makeShutterlessSelfiePreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.setting_gestureshot_on), context.getResources().getString(C0088R.string.auto_selfie_help_title)};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_SHUTTERLESS_SELFIE, C0088R.string.selfie_shot, null, null, entries, new String[]{CameraConstants.GESTURESHOT, "Shutterless"}, null, CameraConstants.GESTURESHOT, true, entries, context.getString(C0088R.string.setting_guide_shutterless_shot));
    }

    public static ListPreference makeShutterSpeedPreference(Context context, PreferenceGroup prefGroup, String key) {
        String defaultValue = "0";
        if (key == null) {
            key = "shutter-speed";
        }
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, key, C0088R.string.shutter_key, null, null, null, null, null, defaultValue, true, null, null);
    }

    public static ListPreference makeEVCtrlPreference(Context context, PreferenceGroup prefGroup, String key) {
        String defaultValue = "0";
        if (key == null) {
            key = Setting.KEY_LG_EV_CTRL;
        }
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, key, C0088R.string.shutter_key, null, null, null, null, null, defaultValue, true, null, null);
    }

    public static ListPreference makeLGISOPreference(Context context, PreferenceGroup prefGroup, String key) {
        String defaultValue = "auto";
        if (key == null) {
            key = Setting.KEY_LG_MANUAL_ISO;
        }
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, key, C0088R.string.iso, null, null, null, null, null, defaultValue, true, null, null);
    }

    public static ListPreference makeSavingRAWPicture(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on)};
        String[] entryValues = new String[]{"off", "on"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on)};
        String guideText = context.getString(C0088R.string.setting_guide_raw);
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_RAW_PICTURE, C0088R.string.camera_help_activity_raw, null, null, entries, entryValues, null, "off", true, desc, guideText);
    }

    public static ListPreference makeHistogramPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        String[] entryValues = new String[]{"on", "off"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.camera_hisogram_on), context.getResources().getString(C0088R.string.camera_hisogram_off)};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_HISTOGRAM, C0088R.string.camera_hisogram, null, null, entries, entryValues, null, "on", true, desc, null);
    }

    public static ListPreference makeInclinometerPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.camera_level), context.getResources().getString(C0088R.string.setting_grid), context.getResources().getString(C0088R.string.off)};
        String[] entryValues = new String[]{"on", "grid", "off"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.camera_level), context.getResources().getString(C0088R.string.setting_grid), context.getResources().getString(C0088R.string.frame_grid_and_level_off)};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_INCLINOMETER, C0088R.string.guide, null, null, entries, entryValues, null, "on", true, desc, null);
    }

    public static ListPreference makeGraphyPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on)};
        String[] entriyValues = new String[]{"off", "on"};
        String defaultValue = "on";
        if (!FunctionProperties.isGraphyDefaultOn()) {
            defaultValue = "off";
        }
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_GRAPHY, C0088R.string.graphy_setting_title, null, null, entries, entriyValues, null, defaultValue, true, null, context.getString(C0088R.string.graphy_label_setting_description1));
    }

    public static ListPreference makeManualFocusStepPreference(Context context, PreferenceGroup prefGroup, String key) {
        String defaultValue = "-1";
        if (key == null) {
            key = Setting.KEY_MANUAL_FOCUS_STEP;
        }
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, key, C0088R.string.manual_focus, null, null, null, null, null, defaultValue, false, null, null);
    }

    public static ListPreference makeManualVideoRatioPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_MANUAL_VIDEO_SIZE, C0088R.string.video_resolution, null, null, null, null, null, "1920x1080", true, null, null);
    }

    public static ListPreference makeManualVideoFrameRatePreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_MANUAL_VIDEO_FRAME_RATE, C0088R.string.video_framerate, null, null, null, null, null, "30", true, null, null);
    }

    public static ListPreference makeManualVideoBitratePreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_MANUAL_VIDEO_BITRATE, C0088R.string.video_bitrate, null, null, context.getResources().getStringArray(C0088R.array.manual_video_bitrate_entrys), null, null, "Medium", true, context.getResources().getStringArray(C0088R.array.manual_video_bitrate_entryValues), null);
    }

    public static ListPreference makeManualVideoLogPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        String[] entryValues = new String[]{"on", "off"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.manual_video_log_profile_on_1), context.getResources().getString(C0088R.string.manual_video_log_profile_off_1)};
        String guideText = context.getString(C0088R.string.save_as_log_guide_text_2);
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_MANUAL_VIDEO_LOG, C0088R.string.save_as_log_2, null, null, entries, entryValues, null, "off", true, desc, guideText);
    }

    public static ListPreference makeManualVideoHDR10Preference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        String[] entryValues = new String[]{"on", "off"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.manual_video_hdr10_on), context.getResources().getString(C0088R.string.manual_video_hdr10_off)};
        String guideText = context.getString(C0088R.string.hdr10_guide_text);
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_HDR10, C0088R.string.save_as_hdr10, null, null, entries, entryValues, null, "off", true, desc, guideText);
    }

    public static ListPreference makeMultiviewIntervalPreference(Context context, PreferenceGroup prefGroup) {
        int[] menuIcons = new int[]{C0088R.drawable.setting_integrate_parent_multiview_sequential, C0088R.drawable.setting_integrate_parent_multiview_same_time};
        String[] entries = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        String[] entryValues = new String[]{"on", "off"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_MULTIVIEW_FRAMESHOT, C0088R.string.multiview_interval_icon_titleid, menuIcons, null, entries, entryValues, null, "on", true, desc, null);
    }

    public static ListPreference makeFilmEmulatorPreference(Context context, PreferenceGroup prefGroup) {
        int i;
        String str = Setting.KEY_FILM_EMULATOR;
        if (ModelProperties.isJapanModel()) {
            i = C0088R.array.film_emulator_entries_japan;
        } else {
            i = C0088R.array.film_emulator_entries;
        }
        return PrefMakerUtil.makePreference(context, prefGroup, str, C0088R.string.mode_film_emulator, C0088R.array.film_emulator_icons, -1, i, C0088R.array.film_emulator_entriyValues, -1, C0088R.string.film_emulator_default, true, C0088R.string.mode_film_emulator, null);
    }

    public static ListPreference makeSelfieFilterPreference(Context context, PreferenceGroup prefGroup) {
        int i;
        String str = Setting.KEY_FILM_EMULATOR;
        if (ModelProperties.isJapanModel()) {
            i = C0088R.array.advanced_selfie_filter_entries_japan;
        } else {
            i = C0088R.array.advanced_selfie_filter_entries;
        }
        return PrefMakerUtil.makePreference(context, prefGroup, str, C0088R.string.Advanced_beauty_filter, C0088R.array.advanced_selfie_filter_icons, -1, i, C0088R.array.advanced_selfie_filter_entriyValues, -1, C0088R.string.advanced_selfie_filter_default, true, C0088R.string.Advanced_beauty_filter, null);
    }

    public static ListPreference makeColorEffectPreference(Context context, PreferenceGroup prefGroup, boolean isRearCamera) {
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_COLOR_EFFECT, C0088R.string.Advanced_beauty_filter, null, null, null, context.getResources().getStringArray(isRearCamera ? C0088R.array.color_effect_entry_values : C0088R.array.color_effect_entry_values_front), null, context.getString(C0088R.string.color_effect_default), true, null, null);
    }

    public static ListPreference makeManualVideoAudioPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.hifi), context.getResources().getString(C0088R.string.hifi)};
        String[] entryValues = new String[]{"off", "on"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.hifi), context.getResources().getString(C0088R.string.hifi)};
        String guideText = context.getString(C0088R.string.setting_guide_HIFI1);
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_MANUAL_VIDEO_AUDIO, C0088R.string.hifi, null, null, entries, entryValues, null, "off", true, desc, guideText);
    }

    public static ListPreference makePanoramaSoundRecordPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        String[] entriyValues = new String[]{"on", "off"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.panorama_sound_record_on_desc), context.getResources().getString(C0088R.string.panorama_sound_record_off_desc)};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_PANO_SOUND_REC, C0088R.string.panorama_sound_record, null, null, entries, entriyValues, null, "off", true, desc, null);
    }

    public static ListPreference makeManualNoiseReductionPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on)};
        String[] entryValues = new String[]{"off", "on"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on)};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_MANUAL_NOISE_REDUCTION, C0088R.string.noise_reduction, null, null, entries, entryValues, null, "off", true, desc, null);
    }

    public static ListPreference makeFingerDetectionPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on)};
        String[] entriyValues = new String[]{"off", "on"};
        String description = context.getString(C0088R.string.finger_detection_descript);
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_FINGER_DETECTION, C0088R.string.finger_detection_title, null, null, entries, entriyValues, null, "off", true, null, description);
    }

    public static ListPreference makeSmartCamPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreference(context, prefGroup, Setting.KEY_SMART_CAM_FILTER, C0088R.string.Advanced_beauty_filter, -1, -1, C0088R.array.smartcam_filter_entries, C0088R.array.smartcam_filter_entriyValues, -1, C0088R.string.smartcam_filter_default, true, C0088R.string.Advanced_beauty_filter, null);
    }

    public static ListPreference makeQRPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.off), context.getResources().getString(C0088R.string.on)};
        String[] entriyValues = new String[]{"off", "on"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.qr_off), context.getResources().getString(C0088R.string.qr_on)};
        String description = context.getString(C0088R.string.sp_lglens_scan_qrcode_desc);
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_QR, C0088R.string.sp_lglens_scan_qrcode, null, null, entries, entriyValues, null, "off", true, desc, description);
    }

    public static ListPreference makeLivePhotoPreference(Context context, PreferenceGroup prefGroup) {
        return PrefMakerUtil.makePreference(context, prefGroup, Setting.KEY_LIVE_PHOTO, C0088R.string.setting_title_live_photo, -1, -1, C0088R.array.live_photo_entries, C0088R.array.live_photo_entryValues, -1, C0088R.string.live_photo_default, true, -1, context.getString(C0088R.string.setting_desc_live_photo));
    }

    public static ListPreference makeDualPopTypePreference(Context context, PreferenceGroup prefGroup) {
        int[] menuIcons = new int[]{C0088R.drawable.setting_dualpop_defalut_mode, C0088R.drawable.setting_dualpop_popout_mode};
        String[] entries = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        String[] entryValues = new String[]{"on", "off"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.mode_dual_pop_dual_capture), context.getResources().getString(C0088R.string.mode_dual_pop_dual_capture_off)};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_DUAL_POP_TYPE, C0088R.string.multiview_interval_icon_titleid, menuIcons, null, entries, entryValues, null, "on", true, desc, null);
    }

    public static ListPreference makeBinningPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.setting_bright_mode_manual), context.getResources().getString(C0088R.string.setting_bright_mode_auto), context.getResources().getString(C0088R.string.off)};
        String[] entriyValues = new String[]{"manual", "on", "off"};
        String defaultValue = ModelProperties.isLDUModel() ? "on" : "manual";
        String description = context.getString(C0088R.string.setting_bright_mode_normal_only_desc);
        if (FunctionProperties.isSupportedBinning(0) && FunctionProperties.isSupportedBinning(2)) {
            description = context.getString(C0088R.string.setting_bright_mode_all_desc);
        } else if (FunctionProperties.isSupportedBinning(0)) {
            description = context.getString(C0088R.string.setting_bright_mode_normal_only_desc);
        }
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_BINNING, FunctionProperties.isUseSuperBright() ? C0088R.string.setting_super_bright_camera : C0088R.string.setting_bright_mode, null, null, entries, entriyValues, null, defaultValue, true, null, description);
    }

    public static ListPreference makeLensSelectionMenu(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.lg_lens), context.getResources().getString(C0088R.string.google_lens)};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_LENS_SELECTION, C0088R.string.vision_search_title, null, null, entries, new String[]{CameraConstants.QLENS, CameraConstants.GOOGLELENS}, null, FunctionProperties.isGoogleLensDefault() ? CameraConstants.GOOGLELENS : CameraConstants.QLENS, true, entries, context.getString(C0088R.string.setting_visual_search_desc_rev2));
    }

    public static ListPreference makeStickerPreference(Context context, PreferenceGroup prefGroup) {
        String[] entries = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        String[] entryValues = new String[]{"on", "off"};
        String[] desc = new String[]{context.getResources().getString(C0088R.string.on), context.getResources().getString(C0088R.string.off)};
        return PrefMakerUtil.makePreferenceWithValue(context, prefGroup, Setting.KEY_STICKER, C0088R.string.sticker_title_text, null, null, entries, entryValues, null, "off", true, null, null);
    }
}
