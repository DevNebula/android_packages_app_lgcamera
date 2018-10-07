package com.lge.camera.settings;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

public class PrefMakerUtil {
    public static ListPreference makePreference(Context context, PreferenceGroup prefGroup, String key, int titleId, int menuIconResId, int settingMenuResId, int entryId, int entryValueId, int indicatorId, int defaultId, boolean persist, int descId, String settingDescr) {
        return makePreference(context, prefGroup, key, titleId, menuIconResId, settingMenuResId, entryId, entryValueId, indicatorId, defaultId, persist, descId, settingDescr, false);
    }

    public static ListPreference makePreference(Context context, PreferenceGroup prefGroup, String key, int titleId, int menuIconResId, int settingMenuResId, int entryId, int entryValueId, int indicatorId, int defaultId, boolean persist, int descId, String settingDescr, boolean isBulletDividerSetting) {
        if (context == null || prefGroup == null) {
            return null;
        }
        int[] menuIconResources;
        int[] settingMenuResources;
        String[] entries;
        String[] entriyValues;
        String defaultValue;
        String[] desc;
        Resources resources = context.getResources();
        if (menuIconResId == -1) {
            menuIconResources = null;
        } else {
            menuIconResources = getIconList(context, menuIconResId);
        }
        if (settingMenuResId == -1) {
            settingMenuResources = null;
        } else {
            settingMenuResources = getIconList(context, settingMenuResId);
        }
        if (entryId == -1) {
            entries = null;
        } else {
            entries = resources.getStringArray(entryId);
        }
        if (entryValueId == -1) {
            entriyValues = null;
        } else {
            entriyValues = resources.getStringArray(entryValueId);
        }
        if (entryValueId == -1) {
            defaultValue = null;
        } else {
            defaultValue = resources.getString(defaultId);
        }
        if (descId == -1) {
            desc = null;
        } else {
            desc = resources.getStringArray(descId);
        }
        ListPreference listPref = new ListPreference(context, prefGroup.getSharedPreferenceName());
        listPref.setKey(key);
        listPref.setTitle(resources.getString(titleId));
        listPref.setMenuIconResources(menuIconResources);
        listPref.setSettingMenuIconResources(settingMenuResources);
        listPref.setEntries(entries);
        listPref.setEntryValues(entriyValues);
        listPref.setDefaultValue(defaultValue);
        listPref.setPersist(persist);
        listPref.setDescription(desc);
        listPref.setSettingDecription(settingDescr);
        listPref.setIsBulletDividerSetting(isBulletDividerSetting);
        return listPref;
    }

    public static ListPreference makeSupportedPreference(Context context, PreferenceGroup prefGroup, String key, int titleId, int menuIconResId, int settingMenuResId, int entryId, int entryValueId, int supportedEntyValueId, int indicatorId, int defaultId, boolean persist) {
        if (context == null || prefGroup == null) {
            return null;
        }
        int[] menuIcons;
        int[] settingMenuIcons;
        String[] entries;
        String[] entryValues;
        String[] supportedValues;
        String defaultValue;
        Resources resources = context.getResources();
        if (menuIconResId == -1) {
            menuIcons = null;
        } else {
            menuIcons = getIconList(context, menuIconResId);
        }
        if (settingMenuResId == -1) {
            settingMenuIcons = null;
        } else {
            settingMenuIcons = getIconList(context, settingMenuResId);
        }
        if (entryId == -1) {
            entries = null;
        } else {
            entries = resources.getStringArray(entryId);
        }
        if (entryValueId == -1) {
            entryValues = null;
        } else {
            entryValues = resources.getStringArray(entryValueId);
        }
        if (supportedEntyValueId == -1) {
            supportedValues = null;
        } else {
            supportedValues = resources.getStringArray(supportedEntyValueId);
        }
        if (defaultId == -1) {
            defaultValue = null;
        } else {
            defaultValue = resources.getString(defaultId);
        }
        int supportedSize = supportedValues == null ? 0 : supportedValues.length;
        int entrySize = entryValues == null ? 0 : entryValues.length;
        int[] supportedMenuIcons = new int[supportedSize];
        int[] supportedSettingMenuIcons = new int[supportedSize];
        String[] supportedEntries = new String[supportedSize];
        int i = 0;
        int j = 0;
        while (i < entrySize) {
            if (j < supportedSize && supportedValues[j].equals(entryValues[i])) {
                if (menuIcons != null) {
                    supportedMenuIcons[j] = menuIcons[i];
                }
                if (settingMenuIcons != null) {
                    supportedSettingMenuIcons[j] = settingMenuIcons[i];
                }
                if (entries != null) {
                    supportedEntries[j] = entries[i];
                }
                j++;
            }
            i++;
        }
        ListPreference listPref = new ListPreference(context, prefGroup.getSharedPreferenceName());
        listPref.setKey(key);
        listPref.setTitle(resources.getString(titleId));
        listPref.setMenuIconResources(supportedMenuIcons);
        listPref.setSettingMenuIconResources(supportedSettingMenuIcons);
        listPref.setEntries(supportedEntries);
        listPref.setEntryValues(supportedValues);
        listPref.setDefaultValue(defaultValue);
        listPref.setPersist(persist);
        return listPref;
    }

    public static ListPreference makeSupportedPreference(Context context, PreferenceGroup prefGroup, String key, int titleId, int menuIconResId, int settingMenuResId, int entryId, int entryValueId, String[] supportedEntyValue, int indicatorId, String[] defaultValue, boolean persist) {
        if (context == null || prefGroup == null) {
            return null;
        }
        int[] menuIcons;
        int[] settingMenuIcons;
        String[] entries;
        String[] entryValues;
        Resources resources = context.getResources();
        if (menuIconResId == -1) {
            menuIcons = null;
        } else {
            menuIcons = getIconList(context, menuIconResId);
        }
        if (settingMenuResId == -1) {
            settingMenuIcons = null;
        } else {
            settingMenuIcons = getIconList(context, settingMenuResId);
        }
        if (entryId == -1) {
            entries = null;
        } else {
            entries = resources.getStringArray(entryId);
        }
        if (entryValueId == -1) {
            entryValues = null;
        } else {
            entryValues = resources.getStringArray(entryValueId);
        }
        int supportedSize = supportedEntyValue == null ? 0 : supportedEntyValue.length;
        int entrySize = entryValues == null ? 0 : entryValues.length;
        int[] supportedMenuIcons = new int[supportedSize];
        int[] supportedSettingMenuIcons = new int[supportedSize];
        String[] supportedEntries = new String[supportedSize];
        int i = 0;
        int j = 0;
        while (i < entrySize) {
            if (j < supportedSize && supportedEntyValue[j].equals(entryValues[i])) {
                if (menuIcons != null) {
                    supportedMenuIcons[j] = menuIcons[i];
                }
                if (settingMenuIcons != null) {
                    supportedSettingMenuIcons[j] = settingMenuIcons[i];
                }
                if (entries != null) {
                    supportedEntries[j] = entries[i];
                }
                j++;
            }
            i++;
        }
        ListPreference listPref = new ListPreference(context, prefGroup.getSharedPreferenceName());
        listPref.setKey(key);
        listPref.setTitle(resources.getString(titleId));
        listPref.setMenuIconResources(supportedMenuIcons);
        listPref.setSettingMenuIconResources(supportedSettingMenuIcons);
        listPref.setEntries(supportedEntries);
        listPref.setEntryValues(supportedEntyValue);
        listPref.setDefaultValue(defaultValue[0]);
        listPref.setPersist(persist);
        return listPref;
    }

    public static ListPreference makePreferenceWithValue(Context context, PreferenceGroup prefGroup, String key, int titleId, int[] menuIcons, int[] settingMenuIcons, String[] entries, String[] entryValues, int[] indicatorId, String defaultValue, boolean persist, String[] desc, String settingDescription) {
        if (context == null || prefGroup == null) {
            return null;
        }
        ListPreference listPref = new ListPreference(context, prefGroup.getSharedPreferenceName());
        listPref.setKey(key);
        listPref.setTitle(context.getResources().getString(titleId));
        listPref.setMenuIconResources(menuIcons);
        listPref.setSettingMenuIconResources(settingMenuIcons);
        listPref.setEntries(entries);
        listPref.setEntryValues(entryValues);
        listPref.setDefaultValue(defaultValue);
        listPref.setPersist(persist);
        listPref.setDescription(desc);
        listPref.setSettingDecription(settingDescription);
        return listPref;
    }

    public static int[] getIconList(Context context, int resourceID) {
        if (resourceID == -1) {
            return new int[0];
        }
        TypedArray tempTypedArray = context.getResources().obtainTypedArray(resourceID);
        int[] tempIconList = new int[tempTypedArray.length()];
        for (int i = 0; i < tempIconList.length; i++) {
            tempIconList[i] = tempTypedArray.getResourceId(i, 0);
        }
        tempTypedArray.recycle();
        return tempIconList;
    }
}
