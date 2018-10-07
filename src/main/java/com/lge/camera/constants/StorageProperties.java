package com.lge.camera.constants;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class StorageProperties {
    private static String sNasUuid = null;

    public static class StorageInfo {
        public String path = null;
        public String state = "removed";
    }

    public static boolean isAllMemoryMounted(Context c) {
        return (getInternal(c).path == null || getSd(c).path == null) ? false : true;
    }

    public static boolean isMemoryMounted(Context c, int storageType) {
        if (storageType == 0) {
            if (getInternal(c).path != null) {
                return true;
            }
            return false;
        } else if (storageType == 1) {
            if (getSd(c).path == null) {
                return false;
            }
            return true;
        } else if (storageType != 2) {
            return false;
        } else {
            if (getNas(c).path == null) {
                return false;
            }
            return true;
        }
    }

    public static int getNoOfStorageVolumes(Context c) {
        int volumeCount = 0;
        StorageManager sm = (StorageManager) c.getSystemService(LdbConstants.LDB_FEAT_NAME_STORAGE);
        try {
            Class<?> storageManager = Class.forName("android.os.storage.StorageManager");
            Class<?> storageVolume = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeListMethod = storageManager.getMethod("getVolumeList", new Class[0]);
            Method getState = storageVolume.getMethod("getState", new Class[0]);
            Object[] storageVolumes = (Object[]) getVolumeListMethod.invoke(sm, new Object[0]);
            if (storageVolumes != null) {
                for (Object o : storageVolumes) {
                    if ("mounted".equals((String) getState.invoke(o, new Object[0]))) {
                        volumeCount++;
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        } catch (ClassNotFoundException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        }
        return volumeCount;
    }

    public static StorageInfo getInternal(Context c) {
        StorageInfo storageInfo = new StorageInfo();
        if (c == null) {
            CamLog.m3d(CameraConstants.TAG, "fail to get internal info");
        } else {
            StorageManager sm = (StorageManager) c.getApplicationContext().getSystemService(LdbConstants.LDB_FEAT_NAME_STORAGE);
            try {
                Class<?> storageManager = Class.forName("android.os.storage.StorageManager");
                Class<?> storageVolume = Class.forName("android.os.storage.StorageVolume");
                Method getVolumeListMethod = storageManager.getMethod("getVolumeList", new Class[0]);
                Method isPrimaryMethod = storageVolume.getMethod("isPrimary", new Class[0]);
                Method getPathMethod = storageVolume.getMethod("getPath", new Class[0]);
                Method getStateMethod = storageVolume.getMethod("getState", new Class[0]);
                Object[] storageVolumes = (Object[]) getVolumeListMethod.invoke(sm, new Object[0]);
                int length = storageVolumes.length;
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 >= length) {
                        break;
                    }
                    Object o = storageVolumes[i2];
                    if (((Boolean) isPrimaryMethod.invoke(o, new Object[0])).booleanValue()) {
                        String state = (String) getStateMethod.invoke(o, new Object[0]);
                        storageInfo.state = state;
                        if ("mounted".equals(state)) {
                            storageInfo.path = (String) getPathMethod.invoke(o, new Object[0]);
                            break;
                        }
                    }
                    i = i2 + 1;
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e2) {
                e2.printStackTrace();
            } catch (ClassNotFoundException e3) {
                e3.printStackTrace();
            } catch (IllegalAccessException e4) {
                e4.printStackTrace();
            }
        }
        return storageInfo;
    }

    public static StorageInfo getSd(Context c) {
        StorageInfo storageInfo = new StorageInfo();
        if (c == null) {
            CamLog.m3d(CameraConstants.TAG, "fail to get sd info");
        } else {
            try {
                Class<?> storageManager = Class.forName("android.os.storage.StorageManager");
                Class<?> volumeInfo = Class.forName("android.os.storage.VolumeInfo");
                Class<?> diskInfo = Class.forName("android.os.storage.DiskInfo");
                Method getVolumesMethod = storageManager.getMethod("getVolumes", new Class[0]);
                Method isPrimaryMethod = volumeInfo.getMethod("isPrimary", new Class[0]);
                Method getPathMethod = volumeInfo.getMethod("getPath", new Class[0]);
                Method getStateMethod = volumeInfo.getMethod("getState", new Class[0]);
                Method getDiskMethod = volumeInfo.getMethod("getDisk", new Class[0]);
                Method isSdMethod = diskInfo.getMethod("isSd", new Class[0]);
                Method isUsbMethod = diskInfo.getMethod("isUsb", new Class[0]);
                Integer storageState = Integer.valueOf(0);
                for (Object o : (List) getVolumesMethod.invoke((StorageManager) c.getSystemService(LdbConstants.LDB_FEAT_NAME_STORAGE), new Object[0])) {
                    boolean isPrimary = ((Boolean) isPrimaryMethod.invoke(o, new Object[0])).booleanValue();
                    Object diskInfoObject = getDiskMethod.invoke(o, new Object[0]);
                    boolean isSd = false;
                    boolean isUsb = false;
                    if (diskInfoObject != null) {
                        isSd = ((Boolean) isSdMethod.invoke(diskInfoObject, new Object[0])).booleanValue();
                        isUsb = ((Boolean) isUsbMethod.invoke(diskInfoObject, new Object[0])).booleanValue();
                    }
                    if (!(isPrimary || !isSd || isUsb)) {
                        File storage = (File) getPathMethod.invoke(o, new Object[0]);
                        if (storage == null) {
                            CamLog.m3d(CameraConstants.TAG, "skip check storage because it's null");
                        } else {
                            storageInfo.path = storage.getPath();
                            if (((Integer) getStateMethod.invoke(o, new Object[0])).intValue() == 2) {
                                storageInfo.state = "mounted";
                                break;
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (NoSuchMethodException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
        }
        return storageInfo;
    }

    public static StorageInfo getNas(Context c) {
        StorageInfo storageInfo = new StorageInfo();
        if (c == null || !ModelProperties.isLguCloudServiceModel()) {
            CamLog.m3d(CameraConstants.TAG, "fail to get nas info");
        } else {
            StorageManager sm = (StorageManager) c.getSystemService(LdbConstants.LDB_FEAT_NAME_STORAGE);
            try {
                String nasUUID = getNasUuid(sm);
                if (nasUUID != null && nasUUID.length() != 0) {
                    for (StorageVolume volume : sm.getStorageVolumes()) {
                        if (nasUUID.equals(volume.getUuid())) {
                            storageInfo.path = volume.getPath();
                            storageInfo.state = volume.getState();
                            CamLog.m7i(CameraConstants.TAG, "NAS path " + storageInfo.path + " state " + storageInfo.state);
                            break;
                        }
                    }
                    CamLog.m7i(CameraConstants.TAG, "unMount NAS ");
                }
            } catch (Exception e) {
                CamLog.m7i(CameraConstants.TAG, "UUID_CNAS_PHYSICAL : NoSuchFieldException ");
            }
        }
        return storageInfo;
    }

    public static String getNasUuid(StorageManager sm) {
        if (sNasUuid == null) {
            try {
                String nas = (String) sm.getClass().getField("UUID_CNAS_PHYSICAL").get(null);
                if (nas == null) {
                    nas = "";
                }
                sNasUuid = nas;
            } catch (Exception e) {
                CamLog.m7i(CameraConstants.TAG, "UUID_CNAS_PHYSICAL : NoSuchFieldException ");
                sNasUuid = "";
            }
        }
        return sNasUuid;
    }
}
