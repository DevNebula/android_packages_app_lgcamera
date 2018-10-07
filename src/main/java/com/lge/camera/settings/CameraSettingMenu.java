package com.lge.camera.settings;

import android.content.Context;
import com.lge.camera.C0088R;
import com.lge.camera.util.SettingKeyWrapper;

public class CameraSettingMenu extends SettingMenu {
    public CameraSettingMenu(SettingInterface function, Context context, Setting setting) {
        super(function);
        this.mSetting = setting;
        this.mSetting.addObserver(this);
        buildSettingMenus(setting);
    }

    protected void buildSettingMenus(Setting setting) {
        SettingMenuItem parentMenu;
        int prefSize = setting.getPreferenceGroup().size();
        for (int i = 0; i < prefSize; i++) {
            ListPreference listPref = setting.getPreferenceGroup().getListPreference(i);
            if (listPref != null) {
                CharSequence[] entries = listPref.getEntries();
                CharSequence[] entryValues = listPref.getEntryValues();
                parentMenu = new SettingMenuItem(i, listPref.getTitle());
                int selectedPos = listPref.findIndexOfValue(listPref.getValue());
                if (checkToggleSettingMenu(entries, entryValues)) {
                    parentMenu.setToggleType(true);
                }
                if (listPref.getSettingDescription() != null) {
                    parentMenu.setGuideText(listPref.getSettingDescription());
                }
                if (selectedPos < 0) {
                    selectedPos = 0;
                }
                parentMenu.setSelectedChildPos(selectedPos);
                if (listPref.getSettingMenuIconResources() != null && listPref.getSettingMenuIconResources().length > 0) {
                    parentMenu.setIconResId(listPref.getSettingMenuIconResources()[0]);
                }
                parentMenu.setCommand(listPref.getCommand());
                parentMenu.setKey(listPref.getKey());
                this.mMenuList.add(parentMenu);
                int j = 0;
                while (j < entries.length && entryValues.length != 0) {
                    if (!(entries[j] == null || entryValues[j] == null)) {
                        SettingMenuItem childMenu = new SettingMenuItem(j, entries[j].toString());
                        childMenu.setValue(entryValues[j].toString());
                        childMenu.setCommand(listPref.getCommand());
                        parentMenu.addChild(childMenu);
                    }
                    j++;
                }
                parentMenu.setBulletDividerType(listPref.isBulletDividerSetting());
            }
        }
        parentMenu = new SettingMenuItem(prefSize + 1, this.mGet.getAppContext().getString(C0088R.string.help));
        parentMenu.setKey(Setting.SETTING_ITEM_HELP);
        this.mMenuList.add(parentMenu);
    }

    private boolean checkToggleSettingMenu(CharSequence[] entries, CharSequence[] entryValues) {
        if (entries.length == 2) {
            if ("off".equals(entryValues[0]) && "on".equals(entryValues[1])) {
                return true;
            }
            if ("on".equals(entryValues[0]) && "off".equals(entryValues[1])) {
                return true;
            }
        }
        return false;
    }

    protected void updatePictureSizeSettingMenu(ListPreference listPref, int selectedPosition, int cameraId) {
        if (listPref != null) {
            if (selectedPosition < 0) {
                selectedPosition = 0;
            }
            SettingMenuItem parentMenu = getMenuItem(SettingKeyWrapper.getPictureSizeKey("mode_normal", cameraId));
            if (parentMenu != null) {
                parentMenu.setSelectedChildPos(selectedPosition);
                CharSequence[] entryValues = listPref.getEntryValues();
                CharSequence[] entry = listPref.getEntries();
                parentMenu.close();
                for (int i = 0; i < entryValues.length; i++) {
                    if (entryValues[i] != null) {
                        SettingMenuItem childMenu = new SettingMenuItem(i, entry[i].toString());
                        childMenu.setValue(entryValues[i].toString());
                        childMenu.setCommand(listPref.getCommand());
                        parentMenu.addChild(childMenu);
                    }
                }
            }
        }
    }

    protected void updateSpecificSettingMenu(String key, String[] entryValues, String[] entreis, int selectedPosition) {
        SettingMenuItem parentMenu = getMenuItem(key);
        if (parentMenu != null) {
            parentMenu.setSelectedChildPos(selectedPosition);
            parentMenu.close();
            for (int i = 0; i < entryValues.length; i++) {
                if (entryValues[i] != null) {
                    SettingMenuItem childMenu = new SettingMenuItem(i, entreis[i].toString());
                    childMenu.setValue(entryValues[i].toString());
                    parentMenu.addChild(childMenu);
                }
            }
        }
    }

    protected void updateGuideTextSettingMenu(String key, String guideText) {
        SettingMenuItem parentMenu = getMenuItem(key);
        if (parentMenu != null) {
            parentMenu.setGuideText(guideText);
        }
    }
}
