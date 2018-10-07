package com.lge.camera.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera.Size;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class Setting extends Observable {
    public static final String KEY_AU_CLOUD = "key_au_cloud";
    public static final String KEY_BEAUTYSHOT = "key_beautyshot";
    public static final String KEY_BINNING = "key_sw_pixel_binning";
    public static final String KEY_CAMERA_ID = "pref_camera_id_key";
    public static final String KEY_CAMERA_PICTURESIZE = "picture-size";
    public static final String KEY_CAMERA_PICTURESIZE_SUB = "picture-size-sub";
    public static final String KEY_COLOR_EFFECT = "key_color_effect";
    public static final String KEY_DUAL_POP_TYPE = "key_dual_pop_type";
    public static final String KEY_FILM_EMULATOR = "key_film_emulator";
    public static final String KEY_FINGER_DETECTION = "key_finger_detection";
    public static final String KEY_FLASH = "flash-mode";
    public static final String KEY_FOCUS = "focus-mode";
    public static final String KEY_FRAME_GRID = "key_frame_grid";
    public static final String KEY_FULLVISION = "key_fullvision";
    public static final String KEY_GRAPHY = "key_graphy";
    public static final String KEY_HDR = "hdr-mode";
    public static final String KEY_HDR10 = "key_video_hdr10";
    public static final String KEY_HISTOGRAM = "key_histogram";
    public static final String KEY_INCLINOMETER = "key_inclinometer";
    public static final String KEY_LENS_SELECTION = "key_lens_selection";
    public static final String KEY_LG_EV_CTRL = "lg_ev_ctrl";
    public static final String KEY_LG_MANUAL_ISO = "key_lg_manual_iso";
    public static final String KEY_LG_WB = "lg-wb";
    public static final String KEY_LIGHTFRAME = "lightframe-mode";
    public static final String KEY_LIVE_PHOTO = "key_live_photo";
    public static final String KEY_MANUAL_FOCUS_STEP = "key_manual_focus_step";
    public static final String KEY_MANUAL_NOISE_REDUCTION = "key_manual_noise_reduction";
    public static final String KEY_MANUAL_VIDEO_AUDIO = "key_manual_video_audio";
    public static final String KEY_MANUAL_VIDEO_BITRATE = "key_video_bitrate";
    public static final String KEY_MANUAL_VIDEO_FRAME_RATE = "key_video_framerate";
    public static final String KEY_MANUAL_VIDEO_LOG = "key_manual_video_log";
    public static final String KEY_MANUAL_VIDEO_SIZE = "key_manual_video_size";
    public static final String KEY_MODE = "key_mode";
    public static final String KEY_MOTION_QUICKVIEWER = "key_motion_quickviewer";
    public static final String KEY_MULTIVIEW_FRAMESHOT = "key_multiview_interval";
    public static final String KEY_MULTIVIEW_LAYOUT = "key_multiview_layout";
    public static final String KEY_PANO_SOUND_REC = "key_panorama_sound_record";
    public static final String KEY_QR = "key_qr";
    public static final String KEY_RAW_PICTURE = "key_raw_picture";
    public static final String KEY_RELIGHTING = "key_relighting";
    public static final String KEY_SAVE_DIRECTION = "key_save_direction";
    public static final String KEY_SHUTTERLESS_SELFIE = "key_shutterless_selfie";
    public static final String KEY_SHUTTER_SPEED = "shutter-speed";
    public static final String KEY_SIGNATURE = "key_signature";
    public static final String KEY_SLOW_MOTION_VIDEO_SIZE = "key_slow_motion_video_size";
    public static final String KEY_SMART_CAM_FILTER = "key_smart_cam_filter";
    public static final String KEY_SQUARE_PICTURE_SIZE = "key_square_picture_size";
    public static final String KEY_SQUARE_VIDEO_SIZE = "key_square_video_size";
    public static final String KEY_STICKER = "key_sticker";
    public static final String KEY_STORAGE = "key_storage";
    public static final String KEY_SWAP_CAMERA = "key_swap";
    public static final String KEY_TAG_LOCATION = "key_tag_location";
    public static final String KEY_TILE_PREVIEW = "key_tile_preview";
    public static final String KEY_TIMER = "key_camera_timer";
    public static final String KEY_TRACKING_AF = "tracking-af";
    public static final String KEY_VIDEO_LG_EV_CTRL = "lg_video_ev_ctrl";
    public static final String KEY_VIDEO_LG_MANUAL_ISO = "key_video_lg_manual_iso";
    public static final String KEY_VIDEO_LG_WB = "lg-video-wb";
    public static final String KEY_VIDEO_MANUAL_FOCUS_STEP = "key_manual_video_focus_step";
    public static final String KEY_VIDEO_RECORDSIZE = "key_video_recordsize";
    public static final String KEY_VIDEO_RECORDSIZE_SUB = "key_video_recordsize_sub";
    public static final String KEY_VIDEO_SHUTTER_SPEED = "video-shutter-speed";
    public static final String KEY_VIDEO_STEADY = "key_video_steady";
    public static final String KEY_VOICESHUTTER = "key_voiceshutter";
    public static final String KEY_ZOOM = "zoom";
    public static final String SETTING_ITEM_HELP = "help";
    public static final String SETTING_SECTION_CAMERA_VIDEO_SIZE = "section_camera";
    public static final String SETTING_SECTION_FUNCTION = "section_function";
    public static final String SETTING_SECTION_GENERAL = "section_general";
    public static final int SETTING_SECTION_INDEX = -100;
    public static final String SETTING_SECTION_MANUAL_CAMERA = "section_manual_camera";
    public static final String SETTING_SECTION_MANUAL_VIDEO = "section_manual_video";
    public static final int SETTING_SINGLE_MENU_INDEX = -101;
    protected String mConfigName;
    public SettingInterface mGet = null;
    protected PreferenceGroup mPreferenceGroup;

    public Setting(SettingInterface function, Context context, String configName, PreferenceGroup prefGroup) {
        this.mGet = function;
        this.mConfigName = configName;
        this.mPreferenceGroup = prefGroup;
    }

    public PreferenceGroup getPreferenceGroup() {
        return this.mPreferenceGroup;
    }

    public void setPreferenceGroup(PreferenceGroup prefGroup) {
        this.mPreferenceGroup = prefGroup;
    }

    public void close() {
        deleteObservers();
    }

    public int getCount() {
        return this.mPreferenceGroup.size();
    }

    private boolean setSetting(ListPreference listPref, String value, boolean save) {
        if (listPref == null) {
            return false;
        }
        if (!save || (this.mGet.isAttachIntent() && (listPref.getKey().contains("picture-size") || listPref.getKey().contains(KEY_VIDEO_RECORDSIZE) || listPref.getKey().contains(KEY_FULLVISION)))) {
            listPref.setSaveSettingEnabled(false);
        } else {
            listPref.setSaveSettingEnabled(true);
        }
        if (listPref.getValue().equals(value)) {
            return false;
        }
        listPref.setValue(value);
        setChanged();
        notifyObservers();
        return true;
    }

    private boolean setFlashSetting(ListPreference listPref, String value, boolean save) {
        if (listPref == null) {
            return false;
        }
        if (save) {
            listPref.setSaveSettingEnabled(true);
        } else {
            listPref.setSaveSettingEnabled(false);
        }
        listPref.setValue(value);
        setChanged();
        notifyObservers();
        return true;
    }

    public boolean setForcedSetting(String key, String value) {
        ListPreference listPref = this.mPreferenceGroup.findPreference(key);
        if (listPref == null) {
            CamLog.m3d(CameraConstants.TAG, "ListPreference is null!!!");
            return false;
        }
        listPref.setSaveSettingEnabled(true);
        listPref.setValue(value);
        return true;
    }

    public boolean setSetting(int index, int value, boolean save) {
        ListPreference pref = this.mPreferenceGroup.getListPreference(index);
        if (pref == null) {
            return false;
        }
        return setSetting(pref, pref.findValueOfIndex(value), save);
    }

    public boolean setSetting(String key, int value, boolean save) {
        ListPreference pref = this.mPreferenceGroup.findPreference(key);
        if (pref == null) {
            return false;
        }
        return setSetting(pref, pref.findValueOfIndex(value), save);
    }

    public boolean setSetting(String key, String value, boolean save) {
        ListPreference pref = this.mPreferenceGroup.findPreference(key);
        if (pref == null) {
            CamLog.m3d(CameraConstants.TAG, "ListPreference is null!!!");
            return false;
        } else if ("flash-mode".equals(key)) {
            return setFlashSetting(pref, value, save);
        } else {
            return setSetting(pref, value, save);
        }
    }

    public int getSettingIndex(String key) {
        try {
            if (this.mPreferenceGroup == null) {
                return 0;
            }
            ListPreference pref = this.mPreferenceGroup.findPreference(key);
            if (pref != null) {
                return pref.findIndexOfValue(pref.getValue());
            }
            return 0;
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "pref error : ", e);
            return 0;
        }
    }

    public String getSettingKey(int index) {
        String key = "";
        try {
            if (this.mPreferenceGroup == null) {
                return key;
            }
            ListPreference pref = this.mPreferenceGroup.getListPreference(index);
            if (pref != null) {
                return pref.getKey();
            }
            return key;
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "pref error : ", e);
            return key;
        }
    }

    public String getSettingValue(String key) {
        ListPreference pref = this.mPreferenceGroup.findPreference(key);
        return pref != null ? pref.getValue() : "not found";
    }

    public ListPreference getListPreference(String key) {
        return this.mPreferenceGroup == null ? null : this.mPreferenceGroup.findPreference(key);
    }

    protected static List<String> sizeListToStringList(List<Size> sizes) {
        ArrayList<String> list = new ArrayList();
        for (Size size : sizes) {
            list.add(String.format("%dx%d", new Object[]{Integer.valueOf(size.width), Integer.valueOf(size.height)}));
        }
        return list;
    }

    public static int readPreferredCameraId(SharedPreferences pref) {
        return Integer.parseInt(pref.getString(KEY_CAMERA_ID, "0"));
    }

    public static void writePreferredCameraId(SharedPreferences pref, int cameraId) {
        Editor editor = pref.edit();
        editor.putString(KEY_CAMERA_ID, Integer.toString(cameraId));
        editor.apply();
    }

    public String getConfigName() {
        return this.mConfigName;
    }
}
