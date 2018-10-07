package com.lge.camera.settings;

import java.util.HashMap;

public class SettingBackup {
    public HashMap<String, String> mBackupSettingMap = new HashMap();

    public SettingBackup() {
        clearBackup();
    }

    public void clearBackup() {
        if (this.mBackupSettingMap != null) {
            this.mBackupSettingMap.clear();
        }
    }

    public void addBackupSetting(String key, String value) {
        if (this.mBackupSettingMap != null && ((String) this.mBackupSettingMap.get(key)) == null) {
            this.mBackupSettingMap.put(key, value);
        }
    }

    public String restoreBackupSetting(String key) {
        if (this.mBackupSettingMap != null) {
            String savedValue = (String) this.mBackupSettingMap.get(key);
            if (savedValue != null) {
                this.mBackupSettingMap.remove(key);
                return savedValue;
            }
        }
        return "";
    }
}
