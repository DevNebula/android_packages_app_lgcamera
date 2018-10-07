package com.lge.camera.util;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;

public class StorageUtil {
    public static String convertStorageTypeToName(int storageType) {
        String storageName = CameraConstants.STORAGE_NAME_INTERNAL;
        if (storageType == 1) {
            return CameraConstants.STORAGE_NAME_EXTERNAL;
        }
        if (ModelProperties.isLguCloudServiceModel() && storageType == 2) {
            return CameraConstants.STORAGE_NAME_NAS;
        }
        return storageName;
    }

    public static int convertStorageNameToType(String storageName) {
        if (CameraConstants.STORAGE_NAME_EXTERNAL.equals(storageName)) {
            return 1;
        }
        if (ModelProperties.isLguCloudServiceModel() && CameraConstants.STORAGE_NAME_NAS.equals(storageName)) {
            return 2;
        }
        return 0;
    }

    public static boolean isExternalStorageType(int storageType) {
        return storageType == 1 || storageType == 2;
    }

    public static boolean isExternalStorageName(String storageName) {
        return !CameraConstants.STORAGE_NAME_INTERNAL.equals(storageName);
    }

    public static boolean isStorageReady(String storageState) {
        if (storageState.equals("bad_removal") || storageState.equals("removed") || storageState.equals("unmounted") || storageState.equals("shared") || storageState.equals("unmountable") || storageState.equals("mounted_ro")) {
            return false;
        }
        return true;
    }
}
