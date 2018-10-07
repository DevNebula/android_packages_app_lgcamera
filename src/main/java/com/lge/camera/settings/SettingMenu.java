package com.lge.camera.settings;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

public abstract class SettingMenu extends Observable implements Observer {
    protected int mCurMenuIndex = 0;
    public SettingInterface mGet = null;
    protected ArrayList<SettingMenuItem> mMenuList = new ArrayList();
    protected Setting mSetting;

    public SettingMenu(SettingInterface function) {
        this.mGet = function;
    }

    public Setting getSetting() {
        return this.mSetting;
    }

    public int getMenuCount() {
        return this.mMenuList.size();
    }

    public SettingMenuItem getMenuItem(int index) {
        return (SettingMenuItem) this.mMenuList.get(index);
    }

    public SettingMenuItem getMenuItem(String key) {
        if (key == null) {
            return null;
        }
        int index = getMenuIndex(key);
        if (index >= 0) {
            return getMenuItem(index);
        }
        return null;
    }

    public SettingMenuItem getCurrentMenu() {
        return getMenuItem(this.mCurMenuIndex);
    }

    public int getMenuIndex(String key) {
        int index = -1;
        if (key == null) {
            return -1;
        }
        int menuSize = this.mMenuList.size();
        for (int i = 0; i < menuSize; i++) {
            if (key.equals(((SettingMenuItem) this.mMenuList.get(i)).getKey())) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getCurMenuIndex() {
        return this.mCurMenuIndex;
    }

    public boolean setCurMenuIndex(int index) {
        if (this.mCurMenuIndex == index) {
            return false;
        }
        this.mCurMenuIndex = index;
        setChanged();
        notifyObservers();
        return true;
    }

    public boolean setCurMenuIndexOnly(int index) {
        if (this.mCurMenuIndex == index) {
            return false;
        }
        this.mCurMenuIndex = index;
        return true;
    }

    public SettingMenuItem getCurSelectedChildMenu() {
        return ((SettingMenuItem) this.mMenuList.get(this.mCurMenuIndex)).getChild(getSelectedChildIndex(this.mCurMenuIndex));
    }

    public int getCurChildIndex(String value) {
        return ((SettingMenuItem) this.mMenuList.get(this.mCurMenuIndex)).getChildIndex(value);
    }

    public int getSelectedChildIndex(int index) {
        return ((SettingMenuItem) this.mMenuList.get(index)).getSelectedChildPos();
    }

    public int getSelectedChildIndex() {
        return ((SettingMenuItem) this.mMenuList.get(this.mCurMenuIndex)).getSelectedChildPos();
    }

    public int getSelectedChildIndex(String key) {
        if (key == null) {
            return -1;
        }
        int index = getMenuIndex(key);
        if (index >= 0) {
            return ((SettingMenuItem) this.mMenuList.get(index)).getSelectedChildPos();
        }
        return -1;
    }

    public boolean setSelectedChild(int menuIndex, int index, boolean saveSetting) {
        SettingMenuItem currentMenu = (SettingMenuItem) this.mMenuList.get(menuIndex);
        if (currentMenu.getSelectedChildPos() == index) {
            return false;
        }
        currentMenu.setSelectedChildPos(index);
        this.mSetting.setSetting(currentMenu.getKey(), index, saveSetting);
        setChanged();
        notifyObservers();
        return true;
    }

    public boolean setSelectedChild(String key, int index, boolean saveSetting) {
        return setSelectedChild(getMenuIndex(key), index, saveSetting);
    }

    public boolean setSelectedChild(String key, String value, boolean saveSetting) {
        int parentMenuIndex = getMenuIndex(key);
        ListPreference listPref = this.mSetting.getListPreference(key);
        if (listPref == null) {
            return false;
        }
        return setSelectedChild(parentMenuIndex, listPref.findIndexOfValue(value), saveSetting);
    }

    public void setParentMenuEnable(String key, boolean enable) {
        if (this.mMenuList != null) {
            int parentMenuIndex = getMenuIndex(key);
            int menuSize = this.mMenuList.size();
            if (parentMenuIndex >= 0 && parentMenuIndex < menuSize) {
                SettingMenuItem parentMenuItem = (SettingMenuItem) this.mMenuList.get(parentMenuIndex);
                if (parentMenuItem != null && parentMenuItem.isEnable() != enable) {
                    parentMenuItem.setEnable(enable);
                    setChanged();
                    notifyObservers();
                }
            }
        }
    }

    public boolean getParentMenuEnable(String key) {
        if (this.mMenuList != null) {
            int parentMenuIndex = getMenuIndex(key);
            int menuSize = this.mMenuList.size();
            if (parentMenuIndex >= 0 && parentMenuIndex < menuSize) {
                SettingMenuItem parentMenuItem = (SettingMenuItem) this.mMenuList.get(parentMenuIndex);
                if (parentMenuItem != null) {
                    return parentMenuItem.isEnable();
                }
            }
        }
        return false;
    }

    public void setAllSettingMenuEnable(boolean enable) {
        if (this.mMenuList != null) {
            Iterator it = this.mMenuList.iterator();
            while (it.hasNext()) {
                ((SettingMenuItem) it.next()).setEnable(enable);
            }
            setChanged();
            notifyObservers();
        }
    }

    public void setChildMenuEnable(String key, String value, boolean enable) {
        int parentMenuIndex = getMenuIndex(key);
        ListPreference listPref = this.mSetting.getListPreference(key);
        if (listPref != null) {
            SettingMenuItem parentMenuItem = (SettingMenuItem) this.mMenuList.get(parentMenuIndex);
            int childMenuIndex = listPref.findIndexOfValue(value);
            if (childMenuIndex < 0) {
                return;
            }
            if (childMenuIndex >= parentMenuItem.getChildCount()) {
                CamLog.m7i(CameraConstants.TAG, "index error childMenuIndex : " + childMenuIndex + " / parentMenuItem.getChildCount() : " + parentMenuItem.getChildCount());
                return;
            }
            SettingMenuItem childMenuItem = parentMenuItem.getChild(childMenuIndex);
            if (childMenuItem.isEnable() != enable) {
                childMenuItem.setEnable(enable);
                setChanged();
                notifyObservers();
            }
        }
    }

    public void setAllChildMenuEnable(String key, boolean enable) {
        ListPreference listPref = this.mSetting.getListPreference(key);
        if (listPref != null) {
            CharSequence[] entryValues = listPref.getEntryValues();
            if (entryValues != null) {
                for (CharSequence charSequence : entryValues) {
                    setChildMenuEnable(key, charSequence.toString(), enable);
                }
            }
        }
    }

    public void update(Observable arg0, Object arg1) {
        int menuSize = this.mMenuList.size();
        for (int i = 0; i < menuSize; i++) {
            SettingMenuItem parentMenu = (SettingMenuItem) this.mMenuList.get(i);
            parentMenu.setSelectedChildPos(this.mSetting.getSettingIndex(parentMenu.getKey()));
        }
        setChanged();
        notifyObservers();
    }

    public String getCurChildValue(int menuIndex) {
        if (this.mSetting == null || this.mMenuList == null || this.mMenuList.get(menuIndex) == null) {
            return "";
        }
        return getCurChildValue(((SettingMenuItem) this.mMenuList.get(menuIndex)).getKey());
    }

    public String getCurChildValue(String key) {
        if (this.mSetting == null || this.mMenuList == null) {
            return "";
        }
        ListPreference listPref = this.mSetting.getListPreference(key);
        if (listPref == null) {
            return "";
        }
        String entryValue = listPref.getValue();
        if (entryValue == null || "".equals(entryValue)) {
            entryValue = this.mSetting.getSettingValue(key);
        }
        if (Setting.KEY_TAG_LOCATION.equals(key) && "on".equals(entryValue) && !CheckStatusManager.isSystemSettingUseLocation(this.mGet.getAppContext().getContentResolver())) {
            return "off";
        }
        return entryValue;
    }

    public String getCurChildEntry(String key) {
        if (this.mSetting == null || this.mMenuList == null) {
            return "";
        }
        ListPreference listPref = this.mSetting.getListPreference(key);
        if (listPref == null) {
            return "";
        }
        String entryValue = listPref.getEntry();
        if (entryValue == null || "".equals(entryValue)) {
            return this.mSetting.getSettingValue(key);
        }
        return entryValue;
    }

    public int getCurMenuIcon(String key) {
        if (this.mSetting == null || this.mMenuList == null) {
            return -1;
        }
        ListPreference listPref = this.mSetting.getListPreference(key);
        if (listPref == null) {
            return -1;
        }
        int iconIndex = listPref.findIndexOfValue(listPref.getValue());
        int[] menuIconRes = listPref.getMenuIconResources();
        if (menuIconRes != null) {
            return iconIndex == -1 ? menuIconRes[0] : menuIconRes[iconIndex];
        } else {
            return -1;
        }
    }

    public int getCurMenuFocusIcon(String key) {
        if (this.mSetting == null || this.mMenuList == null) {
            return -1;
        }
        ListPreference listPref = this.mSetting.getListPreference(key);
        if (listPref == null) {
            return -1;
        }
        int iconIndex = listPref.findIndexOfValue(listPref.getValue());
        int[] menuIconRes = listPref.getSettingMenuIconResources();
        if (menuIconRes == null) {
            return -1;
        }
        if (menuIconRes.length <= iconIndex) {
            iconIndex = -1;
        }
        return iconIndex == -1 ? menuIconRes[0] : menuIconRes[iconIndex];
    }

    public int[] getCurSettingMenuIcons(String key) {
        if (this.mSetting == null || this.mMenuList == null) {
            return null;
        }
        ListPreference listPref = this.mSetting.getListPreference(key);
        if (listPref != null) {
            return listPref.getSettingMenuIconResources();
        }
        return null;
    }

    public String getCurDescription(String key) {
        if (this.mSetting == null || this.mMenuList == null) {
            return "";
        }
        ListPreference listPref = this.mSetting.getListPreference(key);
        if (listPref != null) {
            return listPref.getDesc();
        }
        return "";
    }

    public CharSequence[] getDescrptionList(String key) {
        if (this.mSetting == null || this.mMenuList == null) {
            return null;
        }
        ListPreference listPref = this.mSetting.getListPreference(key);
        if (listPref != null) {
            return listPref.getDescList();
        }
        return null;
    }

    public void close() {
        for (int i = this.mMenuList.size() - 1; i >= 0; i--) {
            ((SettingMenuItem) this.mMenuList.get(i)).close();
            this.mMenuList.set(i, null);
            this.mMenuList.remove(i);
        }
        this.mMenuList = null;
        this.mSetting.deleteObserver(this);
        this.mSetting = null;
    }
}
