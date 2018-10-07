package com.lge.camera.settings;

import android.content.Context;
import android.content.res.Resources;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class MultiViewPrefMaker {
    public static ListPreference MultiViewPreference(Context context, PreferenceGroup prefGroup) {
        return makeMultiViewPreference(context, prefGroup, Setting.KEY_MULTIVIEW_LAYOUT, C0088R.string.multiview_layout_selection, C0088R.array.multiview_layout_entries, C0088R.array.multiview_layout_entryValues, C0088R.array.multiview_layout_description, C0088R.array.multiview_layout_images, context.getResources().getString(C0088R.string.multiview_layout_default));
    }

    private static ListPreference makeMultiViewPreference(Context context, PreferenceGroup prefGroup, String key, int titleId, int entryId, int entryValueId, int descId, int imageId, String defaultValue) {
        if (context == null || prefGroup == null) {
            return null;
        }
        Resources resources = context.getResources();
        String title = resources.getString(titleId);
        String[] entries = resources.getStringArray(entryId);
        String[] entryValues = resources.getStringArray(entryValueId);
        int[] supportedImageIcons = PrefMakerUtil.getIconList(context, imageId);
        int[] supportedDescIcons = PrefMakerUtil.getIconList(context, descId);
        if (title == null || entries == null || entryValues == null || supportedImageIcons == null || defaultValue == null) {
            return null;
        }
        CamLog.m3d(CameraConstants.TAG, "MultiViewPreference pref name = " + prefGroup.getSharedPreferenceName());
        ListPreference listPref = new ListPreference(context, prefGroup.getSharedPreferenceName());
        listPref.setKey(key);
        listPref.setTitle(title);
        listPref.setMenuIconResources(null);
        listPref.setSettingMenuIconResources(null);
        listPref.setEntries(entries);
        listPref.setEntryValues(entryValues);
        listPref.setExtraInfos(supportedDescIcons, 4);
        listPref.setExtraInfos(supportedImageIcons, 5);
        listPref.setDefaultValue(defaultValue);
        listPref.setPersist(true);
        return listPref;
    }
}
