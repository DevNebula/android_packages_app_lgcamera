package com.lge.camera.settings;

import java.util.ArrayList;

public class SettingMenuItem {
    private ArrayList<SettingMenuItem> mChildList = new ArrayList();
    private String mCommand = "";
    private boolean mEnable = true;
    private String mGuideText = "";
    private int mIconResId = 0;
    private boolean mIsBulletDividerType = false;
    private boolean mIsToggleType = false;
    private String mKey = "";
    private String mMenuItemName = "";
    private int mSelectedChildPos = 0;
    private int mSettingIndex;
    private String mValue = "";

    public SettingMenuItem(int settingIndex, String name) {
        this.mSettingIndex = settingIndex;
        this.mMenuItemName = name;
    }

    public int getSettingIndex() {
        return this.mSettingIndex;
    }

    public String getName() {
        return this.mMenuItemName;
    }

    public String getCommand() {
        return this.mCommand;
    }

    public void setCommand(String command) {
        this.mCommand = command;
    }

    public String getKey() {
        return this.mKey;
    }

    public void setKey(String key) {
        this.mKey = key;
    }

    public String getValue() {
        return this.mValue;
    }

    public void setValue(String value) {
        this.mValue = value;
    }

    public void setSelectedChildPos(int selectedChildPos) {
        this.mSelectedChildPos = selectedChildPos;
    }

    public int getSelectedChildPos() {
        return this.mSelectedChildPos;
    }

    public void setSelectedChildBySettingIndex(int settingIndex) {
        int count = this.mChildList.size();
        for (int i = 0; i < count; i++) {
            if (((SettingMenuItem) this.mChildList.get(i)).getSettingIndex() == settingIndex) {
                this.mSelectedChildPos = i;
            }
        }
    }

    public void setIconResId(int resId) {
        this.mIconResId = resId;
    }

    public int getIconResId() {
        return this.mIconResId;
    }

    public void addChild(SettingMenuItem child) {
        this.mChildList.add(child);
    }

    public SettingMenuItem getChild(int index) {
        return (SettingMenuItem) this.mChildList.get(index);
    }

    public SettingMenuItem getSelectedChild() {
        return (SettingMenuItem) this.mChildList.get(this.mSelectedChildPos);
    }

    public int getChildCount() {
        return this.mChildList.size();
    }

    public int getChildIndex(String value) {
        for (int i = 0; i < getChildCount(); i++) {
            if (value.equals(getChild(i).getValue())) {
                return i;
            }
        }
        return -1;
    }

    public boolean isEnable() {
        return this.mEnable;
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
    }

    public void close() {
        this.mChildList.clear();
    }

    public boolean isToggleType() {
        return this.mIsToggleType;
    }

    public void setToggleType(boolean mIsToggleType) {
        this.mIsToggleType = mIsToggleType;
    }

    public String getGuideText() {
        return this.mGuideText;
    }

    public void setGuideText(String mGuideText) {
        this.mGuideText = mGuideText;
    }

    public ArrayList<SettingMenuItem> getChildList() {
        return this.mChildList;
    }

    public boolean isBulletDividerType() {
        return this.mIsBulletDividerType;
    }

    public void setBulletDividerType(boolean isBullet) {
        this.mIsBulletDividerType = isBullet;
    }
}
