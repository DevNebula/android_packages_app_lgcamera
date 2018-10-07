package com.lge.camera.settings;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import java.util.ArrayList;

public class ModePrefMaker {
    public static ListPreference makeModePreference(Context context, PreferenceGroup prefGroup, boolean rear) {
        return makeModePreference(context, prefGroup, Setting.KEY_MODE, C0088R.string.shot_mode, C0088R.array.mode_new_entries, C0088R.array.mode_new_entryValues, rear ? ConfigurationUtil.sMODE_REAR_SUPPORTED_ITEMS : ConfigurationUtil.sMODE_FRONT_SUPPORTED_ITEMS, C0088R.array.mode_new_description, C0088R.array.mode_new_images, context.getResources().getString(C0088R.string.mode_new_default));
    }

    private static ListPreference makeModePreference(Context context, PreferenceGroup prefGroup, String key, int titleId, int entryId, int entryValueId, String[] modeSupported, int descId, int imageId, String defaultValue) {
        if (context == null || prefGroup == null) {
            return null;
        }
        Resources resources = context.getResources();
        String title = resources.getString(titleId);
        String[] entries = resources.getStringArray(entryId);
        String[] entryValues = resources.getStringArray(entryValueId);
        int[] extraInfo4Int = PrefMakerUtil.getIconList(context, descId);
        int[] extraInfo5Int = PrefMakerUtil.getIconList(context, imageId);
        if (title == null || entries == null || entryValues == null || modeSupported == null || extraInfo4Int == null || extraInfo5Int == null || defaultValue == null) {
            return null;
        }
        int i;
        ArrayList<String> modeArrayList = changeSupportedModeList(modeSupported, context);
        modeSupported = (String[]) modeArrayList.toArray(new String[modeArrayList.size()]);
        for (i = 0; i < modeSupported.length; i++) {
            if (modeSupported[i].equals("mode_panorama_360_proj")) {
                Toast.makeText(context, "camera_config should be updated", 0).show();
                modeSupported[i] = CameraConstants.MODE_PANORAMA_LG_360_PROJ;
            } else if (modeSupported[i].equals("mode_panorama_360_normal")) {
                Toast.makeText(context, "camera_config should be updated", 0).show();
                modeSupported[i] = CameraConstants.MODE_PANORAMA_LG_RAW;
            }
        }
        int supportedSize = modeSupported.length;
        int entrySize = entryValues.length;
        int[] supportedDescIcons = new int[supportedSize];
        int[] supportedImageIcons = new int[supportedSize];
        String[] supportedEntries = new String[supportedSize];
        for (i = 0; i < entrySize; i++) {
            for (int j = 0; j < supportedSize; j++) {
                if (modeSupported[j].equals(entryValues[i])) {
                    supportedDescIcons[j] = extraInfo4Int[i];
                    supportedImageIcons[j] = extraInfo5Int[i];
                    supportedEntries[j] = entries[i];
                    break;
                }
            }
        }
        ListPreference listPref = new ListPreference(context, prefGroup.getSharedPreferenceName());
        listPref.setKey(key);
        listPref.setTitle(title);
        listPref.setMenuIconResources(null);
        listPref.setSettingMenuIconResources(null);
        listPref.setEntries(supportedEntries);
        listPref.setEntryValues(modeSupported);
        listPref.setExtraInfos(supportedDescIcons, 4);
        listPref.setExtraInfos(supportedImageIcons, 5);
        listPref.setDefaultValue(defaultValue);
        listPref.setPersist(false);
        return listPref;
    }

    private static ArrayList<String> changeSupportedModeList(String[] modeSupported, Context context) {
        ArrayList<String> modeArrayList = new ArrayList();
        for (Object add : modeSupported) {
            modeArrayList.add(add);
        }
        return modeArrayList;
    }
}
