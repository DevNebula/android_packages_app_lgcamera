package com.lge.camera.managers;

import android.content.ContentValues;
import android.os.Environment;
import android.os.StatFs;
import android.os.UserHandle;
import android.provider.MediaStore.Files;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.constants.StorageProperties;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.FileNamer;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtilPersist;
import com.lge.camera.util.StorageUtil;
import com.lge.camera.util.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class PhoneStorageManager extends ManagerInterfaceImpl {
    public static final String DCF_DIRECTORY = "/DCIM/100LGDSC/";
    private static final String GUESTMODE_DIRECTORY = "/DCIM/Guest album/";
    private static final String NORMAL_DIRECTORY = "/DCIM/Camera/";
    public static final String SNAP_DIRECTORY = "/DCIM/.snap/";
    private static final String TEMP_DIRECTORY = "/DCIM/.thumbnails/";
    public static final int USER_ID = UserHandle.myUserId();
    private static String sCurShotMode = "mode_normal";
    private static String sExternalStorageDir = null;
    private static String sInternalStorageDir = null;
    private static String sNasStorageDir = null;
    private final int SNAP_SAVING_DAMPER = 5242880;
    private long mRemainingPictureCount = 0;
    private int mSnapBitrate = 0;
    private int mSnapDurationMillis = 0;

    public PhoneStorageManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        updateStorageDir();
    }

    private void updateStorageDir() {
        sInternalStorageDir = StorageProperties.getInternal(getAppContext()).path;
        sExternalStorageDir = StorageProperties.getSd(getAppContext()).path;
        sNasStorageDir = StorageProperties.getNas(getAppContext()).path;
    }

    public static String getInternalStorageDir() {
        return sInternalStorageDir;
    }

    public void onResumeAfter() {
        selectStorageBySystemSetting(false);
        if (SharedPreferenceUtilPersist.getDataCleared(getAppContext()) == 1) {
            FileManager.deleteAllFiles(getStorageDir(0) + SNAP_DIRECTORY);
        }
    }

    public String getDir(int storageType) {
        return getStorageDir(storageType) + getSaveDirectory();
    }

    public ArrayList<String> getAllDir(boolean includeCNAS) {
        ArrayList<String> storagePathList = new ArrayList();
        ArrayList<Integer> storageList = new ArrayList();
        storageList.add(Integer.valueOf(0));
        storageList.add(Integer.valueOf(1));
        if (ModelProperties.isLguCloudServiceModel() && includeCNAS) {
            storageList.add(Integer.valueOf(2));
        }
        Iterator it = storageList.iterator();
        while (it.hasNext()) {
            storagePathList.add(getDir(((Integer) it.next()).intValue()));
        }
        return storagePathList;
    }

    public String getTempDir(int storageType) {
        String targetDir = TEMP_DIRECTORY;
        if (CameraConstants.MODE_SNAP.equals(sCurShotMode)) {
            return sInternalStorageDir + SNAP_DIRECTORY;
        }
        if (CameraConstants.MODE_FLASH_JUMPCUT.equals(sCurShotMode)) {
            return "/data/user/" + USER_ID + "/com.lge.camera/cache/";
        }
        return getStorageDir(storageType) + targetDir;
    }

    public String getStorageDir(int storageType) {
        switch (storageType) {
            case 0:
                if (sInternalStorageDir == null) {
                    CamLog.m3d(CameraConstants.TAG, "Internal storage paths are null");
                    updateStorageDir();
                }
                return sInternalStorageDir;
            case 1:
                if (sExternalStorageDir == null) {
                    CamLog.m3d(CameraConstants.TAG, "External storage paths are null");
                    updateStorageDir();
                }
                return sExternalStorageDir;
            case 2:
                if (sNasStorageDir == null && ModelProperties.isLguCloudServiceModel()) {
                    CamLog.m3d(CameraConstants.TAG, "Internal storage paths are null");
                    updateStorageDir();
                }
                return sNasStorageDir;
            default:
                return sExternalStorageDir;
        }
    }

    public boolean checkStorage(int checkFor, int storageType, int durationMillis, int bitrate) {
        this.mSnapDurationMillis = durationMillis;
        this.mSnapBitrate = bitrate;
        return checkStorage(checkFor, storageType);
    }

    public boolean easyCheckStorage(int checkFor, int storageType, int queueCount) {
        boolean storageFullNear;
        if (this.mRemainingPictureCount <= ((long) (queueCount + 100))) {
            storageFullNear = true;
        } else {
            storageFullNear = false;
        }
        if (storageFullNear) {
            return checkStorage(checkFor, storageType);
        }
        if (this.mRemainingPictureCount <= ((long) queueCount)) {
            return false;
        }
        return true;
    }

    public boolean checkStorage(int checkFor, int storageType) {
        boolean available = false;
        CamLog.m7i(CameraConstants.TAG, String.format("checkstorage(%d, %d) - start", new Object[]{Integer.valueOf(checkFor), Integer.valueOf(storageType)}));
        int storageState = checkStorageState(checkFor, storageType, checkStorageDeviceState(storageType));
        confirmRemainingPictureCountByState(storageState);
        FileNamer.get().setStorageState(this.mGet.getAppContext(), storageType, getDir(storageType), storageState);
        if (!this.mGet.isPaused()) {
            available = checkStorageAvailable(checkFor, storageState);
            CamLog.m3d(CameraConstants.TAG, "checkStorage type=" + storageType + " available = " + available);
            if (!available) {
                if ((!CameraConstants.MODE_SNAP.equals(sCurShotMode) || checkFor == 3) && !((CameraConstants.MODE_MULTIVIEW.equals(sCurShotMode) || CameraConstants.MODE_SQUARE_SPLICE.equals(sCurShotMode)) && checkFor == 4)) {
                    doHandleStorageFull(checkFor, storageType, true);
                } else {
                    this.mGet.postOnUiThread(new HandlerRunnable(this) {
                        public void handleRun() {
                            PhoneStorageManager.this.mGet.showDialog(134);
                        }
                    }, 0);
                }
            }
            CamLog.m3d(CameraConstants.TAG, "checkStorage. - end");
        }
        return available;
    }

    public boolean checkCNasCacheStorage(int checkFor, boolean showDialog) {
        long backup = this.mRemainingPictureCount;
        boolean available = checkStorageAvailable(checkFor, checkStorageState(checkFor, 0, checkStorageDeviceState(0)));
        if (getFreeSpace(0) - CameraConstants.MEMORY_SAFE_FREE_CNAS_CACHE_SPACE < 0) {
            available = false;
        }
        if (!available && showDialog) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (PhoneStorageManager.this.mGet.getDialogID() != 145) {
                        PhoneStorageManager.this.mGet.showDialog(145);
                    }
                }
            }, 0);
        }
        this.mRemainingPictureCount = backup;
        return available;
    }

    public int getRawSize() {
        int rawSize = 0;
        if (FunctionProperties.isSupportedRAWPictureSaving() && FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA) && this.mGet != null && this.mGet.getCameraDevice() != null && ManualUtil.isManualCameraMode(this.mGet.getShotMode()) && !"off".equals(this.mGet.getSettingValue(Setting.KEY_RAW_PICTURE))) {
            String dngSize = this.mGet.getCameraDevice().getParameters().get(ParamConstants.KEY_RAW_SIZE);
            try {
                rawSize = Integer.parseInt(dngSize);
            } catch (NumberFormatException e) {
                CamLog.m3d(CameraConstants.TAG, "NumberFormatException dngSize=" + dngSize);
                rawSize = 0;
            }
            CamLog.m3d(CameraConstants.TAG, "-raw- getRawSize mRawSize=" + rawSize);
        }
        return rawSize;
    }

    public boolean isAvailableTakePicture(int queueCount) {
        if (this.mGet.isAttachIntent()) {
            if (this.mRemainingPictureCount - 1 > ((long) queueCount)) {
                return true;
            }
            return false;
        } else if (this.mRemainingPictureCount <= ((long) queueCount)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean checkStorage() {
        return checkStorage(0, getCurrentStorage());
    }

    private int checkStorageState(int checkFor, int storageType, String stateStorageDevice) {
        long j;
        boolean isFreeSpaceEnoughToSavePicture = false;
        boolean isFreeSpaceEnoughToSaveVideo = false;
        long externalFreeSpace = -1;
        CamLog.m7i(CameraConstants.TAG, String.format("storage(%d) state: %s", new Object[]{Integer.valueOf(storageType), stateStorageDevice}));
        if (StorageUtil.isStorageReady(stateStorageDevice) && checkFsWritable(storageType)) {
            long freeSpaceForVideo;
            externalFreeSpace = getFreeSpace(storageType);
            if (checkFor == 3) {
                isFreeSpaceEnoughToSavePicture = false;
            } else {
                isFreeSpaceEnoughToSavePicture = checkFreeSpaceEnoughToSavePicture(externalFreeSpace);
            }
            if (checkFor == 3) {
                freeSpaceForVideo = (externalFreeSpace - ((long) getVideoEstimateSize(this.mSnapDurationMillis, this.mSnapBitrate))) - CameraConstants.VIDEO_RECORD_LOOPING_THRESHOLD;
            } else {
                freeSpaceForVideo = externalFreeSpace;
            }
            isFreeSpaceEnoughToSaveVideo = checkFreeSpaceEnoughToSaveVideo(freeSpaceForVideo);
        }
        if (checkFor == 3) {
            j = 0;
        } else {
            j = getRemainingPictureCount(externalFreeSpace, storageType);
        }
        this.mRemainingPictureCount = j;
        return checkStorageState(storageType, isFreeSpaceEnoughToSavePicture, isFreeSpaceEnoughToSaveVideo, this.mRemainingPictureCount, externalFreeSpace, stateStorageDevice);
    }

    private static int getVideoEstimateSize(int durationMillis, int bitrate) {
        int result = (bitrate / 8) * (durationMillis / 1000);
        CamLog.m3d(CameraConstants.TAG, "estimated size = " + result + " (" + durationMillis + ", " + bitrate + ")");
        return result;
    }

    private boolean isEnoughToUse(int checkFor, int storageType) {
        int storageState = checkStorageState(checkFor, storageType, checkStorageDeviceState(storageType));
        CamLog.m3d(CameraConstants.TAG, "storageState=" + storageState);
        boolean available = checkStorageAvailable(checkFor, storageState);
        CamLog.m3d(CameraConstants.TAG, "isEnoughToUse type=" + storageType + " available = " + available);
        return available;
    }

    public boolean isStorageFull(int storageType) {
        return !isEnoughToUse(0, storageType);
    }

    public boolean checkStorageAvailable(int checkMode, int storageState) {
        if (storageState == -2 || storageState == -1 || storageState == 0) {
            return false;
        }
        int result = storageState & 17;
        boolean available = checkMode == 1 ? 1 == (result & 1) : checkMode == 2 ? 16 == (result & 16) : result != 0;
        CamLog.m3d(CameraConstants.TAG, "checkStorageAvailable result=" + available);
        return available;
    }

    private long getExpectedAverageVideoSize() {
        int[] size = Utils.sizeStringToArray(this.mGet.getSettingValue(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId())));
        return (getAverageSpaceForVideo(size[0], size[1]) + CameraConstants.VIDEO_LOW_STORAGE_THRESHOLD) + CameraConstants.VIDEO_SAFE_MAX_FILE_SIZE_DAMPER;
    }

    private long getAverageSpaceForVideo(int width, int height) {
        long resolution = (long) (width * height);
        if (resolution >= 2073600) {
            return 3 * resolution;
        }
        if (resolution >= 307200) {
            return 4 * resolution;
        }
        if (resolution >= 76800) {
            return 13 * resolution;
        }
        return 35 * resolution;
    }

    private void confirmRemainingPictureCountByState(int storageState) {
        if ((storageState & 256) == 256) {
            this.mRemainingPictureCount = 0;
        } else if (storageState == -2 || storageState == -1) {
            this.mRemainingPictureCount = -1;
        }
        CamLog.m3d(CameraConstants.TAG, String.format("confrim picture count remained : %s", new Object[]{Long.valueOf(this.mRemainingPictureCount)}));
    }

    private long getRemainingPictureCount(long externalFreeSpace, int storageType) {
        int[] size;
        String sizeString = this.mGet.getSettingValue(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
        if (this.mGet.getSettingValue(Setting.KEY_MODE).contains(CameraConstants.MODE_PANORAMA)) {
            size = Utils.sizeStringToArray(sizeString);
            size[0] = size[0] * 10;
        } else {
            size = Utils.sizeStringToArray(sizeString);
        }
        if (size == null) {
            return 0;
        }
        long pictureRemaining = calculateRemainPictureCount(size[0], size[1], externalFreeSpace, storageType) - this.mGet.getSavingQueueCount();
        if (pictureRemaining > 0) {
            return pictureRemaining;
        }
        return 0;
    }

    public void setRemainingPictureCount(int count) {
        this.mRemainingPictureCount = (long) count;
    }

    private String checkStorageDeviceState(int storageType) {
        String state = "removed";
        if (storageType == 0) {
            return Environment.getExternalStorageState();
        }
        if (storageType == 2) {
            return StorageProperties.getNas(getAppContext()).state;
        }
        return StorageProperties.getSd(getAppContext()).state;
    }

    public boolean isStorageRemoved(int storageType) {
        return !"mounted".equals(checkStorageDeviceState(storageType));
    }

    private boolean checkFsWritable(int storageType) {
        File directory = new File(getDir(storageType).split(getSaveDirectory())[0]);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        CamLog.m3d(CameraConstants.TAG, "checkFsWritable : " + directory.canWrite());
        return directory.canWrite();
    }

    public long getFreeSpace(int storageType) {
        try {
            String strStorageDirectory = getDir(storageType);
            File file = new File(strStorageDirectory);
            if (!file.exists()) {
                file.mkdirs();
                CamLog.m3d(CameraConstants.TAG, "make directory : " + file.toString());
                if (this.mGet != null) {
                    int lastSlash = strStorageDirectory.lastIndexOf(47);
                    if (lastSlash == strStorageDirectory.length() - 1) {
                        strStorageDirectory = strStorageDirectory.substring(0, lastSlash);
                    }
                    ContentValues values = new ContentValues();
                    values.put("_data", strStorageDirectory);
                    values.put("format", Integer.valueOf(12289));
                    values.put("_size", Integer.valueOf(0));
                    this.mGet.getAppContext().getContentResolver().insert(Files.getContentUri(CameraConstants.STORAGE_NAME_EXTERNAL), values);
                }
            }
            StatFs stat = new StatFs(strStorageDirectory);
            long freeSpace = (stat.getAvailableBlocksLong() * stat.getBlockSizeLong()) - 20971520;
            if (freeSpace < 0) {
                freeSpace = 0;
            }
            return freeSpace;
        } catch (RuntimeException ex) {
            CamLog.m5e(CameraConstants.TAG, " error :" + ex.getMessage());
            return -2;
        }
    }

    private boolean checkMemoryFullCondition(long remainingPictureCount) {
        if (remainingPictureCount <= 0) {
            return true;
        }
        if (!this.mGet.isAttachIntent() || remainingPictureCount >= 2) {
            return false;
        }
        return true;
    }

    private int checkStorageState(int storageType, boolean isFreeSpaceEnoughToSavePicture, boolean isFreeSpaceEnoughToSaveVideo, long remainingPictureCount, long externalFreeSpace, String state) {
        if (isFreeSpaceEnoughToSavePicture || isFreeSpaceEnoughToSaveVideo) {
            int storageState;
            if (!isFreeSpaceEnoughToSavePicture) {
                storageState = 256;
            } else if (checkMemoryFullCondition(remainingPictureCount)) {
                storageState = 256;
            } else {
                storageState = 1;
            }
            if (!isFreeSpaceEnoughToSaveVideo) {
                return storageState | 4096;
            }
            if (externalFreeSpace > getExpectedAverageVideoSize()) {
                return storageState | 16;
            }
            return storageState | 4096;
        } else if (externalFreeSpace != -1) {
            return 4352;
        } else {
            if (isStorageReadOnly(state, storageType)) {
                return -2;
            }
            if (state.equals("mounted")) {
                return -1;
            }
            return -2;
        }
    }

    private static String getSaveDirectory() {
        return AppControlUtil.isGuestMode() ? GUESTMODE_DIRECTORY : ModelProperties.useDCFRule() ? DCF_DIRECTORY : NORMAL_DIRECTORY;
    }

    public int getCurrentStorage() {
        return StorageUtil.convertStorageNameToType(this.mGet.getSettingValue(Setting.KEY_STORAGE));
    }

    private boolean isStorageReadOnly(String state, int storageType) {
        return state.equals("mounted_ro") || (state.equals("mounted") && !checkFsWritable(storageType));
    }

    private long calculateRemainPictureCount(int width, int height, long freeSpace, int storageType) {
        long remain = (long) Math.floor(((double) (freeSpace - ((long) (1048576 + getRawSize())))) / getAverageSpaceForPicture(width, height));
        CamLog.m3d(CameraConstants.TAG, String.format("picture count remained : %s", new Object[]{Long.valueOf(remain)}));
        return remain;
    }

    private double getAverageSpaceForPicture(int width, int height) {
        return ((((double) (width * height)) * 0.3d) * MultimediaProperties.getPictureSizeScale(this.mGet.getCameraId(), this.mGet.getSettingValue(Setting.KEY_MODE), 0, this.mGet.getSettingValue(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId())))) + ((double) getRawSize());
    }

    private boolean checkFreeSpaceEnoughToSavePicture(long externalFreeSpace) {
        boolean isFreeSpaceEnoughToSavePicture = false;
        int expectedSize = 1048576 + getRawSize();
        if (this.mGet.getSavingQueueCount() > 0) {
            expectedSize = (int) (((long) expectedSize) * this.mGet.getSavingQueueCount());
        }
        if (externalFreeSpace > ((long) expectedSize)) {
            isFreeSpaceEnoughToSavePicture = true;
        }
        CamLog.m3d(CameraConstants.TAG, "mExternalFreeSpace=" + externalFreeSpace + " mIsHaveEnoughFreeSpace=" + isFreeSpaceEnoughToSavePicture);
        return isFreeSpaceEnoughToSavePicture;
    }

    private boolean checkFreeSpaceEnoughToSaveVideo(long mExternalFreeSpace) {
        boolean isFreeSpaceEnoughToSaveVideo = false;
        if (mExternalFreeSpace > 1572864) {
            isFreeSpaceEnoughToSaveVideo = true;
        }
        CamLog.m3d(CameraConstants.TAG, "mExternalFreeSpace=" + mExternalFreeSpace + " mIsHaveEnoughFreeSpace=" + isFreeSpaceEnoughToSaveVideo);
        return isFreeSpaceEnoughToSaveVideo;
    }

    public void selectStorageBySystemSetting(boolean showDialog) {
        String curStorageName = this.mGet.getSettingValue(Setting.KEY_STORAGE);
        int curStorageType = StorageUtil.convertStorageNameToType(curStorageName);
        String selectedStorage = CameraConstants.STORAGE_NAME_INTERNAL;
        if (StorageUtil.isExternalStorageType(curStorageType) && canUseStorage(curStorageType)) {
            if (isEnoughToUse(0, curStorageType)) {
                selectedStorage = curStorageName;
            } else {
                doHandleStorageFull(0, curStorageType, showDialog);
                return;
            }
        }
        this.mGet.setSetting(Setting.KEY_STORAGE, selectedStorage, true);
        CamLog.m3d(CameraConstants.TAG, "storage is " + selectedStorage);
        if (CameraConstants.STORAGE_NAME_INTERNAL.equals(selectedStorage)) {
            checkStorage();
        }
    }

    private boolean canUseStorage(int storageType) {
        int storageState = checkStorageState(0, storageType, checkStorageDeviceState(storageType));
        if (storageState == -2 || storageState == -1 || storageState == 0) {
            return false;
        }
        return true;
    }

    public void doHandleStorageFull(int checkFor, int storageType, boolean useDialog) {
        CamLog.m3d(CameraConstants.TAG, "doHandleStorageFull, checkFor : " + checkFor + ", storageType : " + storageType + ", useDialog : " + useDialog);
        int dialogId = 128;
        if (storageType == 2) {
            dialogId = doHandleNasStorageFull(checkFor, storageType);
        } else if (StorageProperties.isAllMemoryMounted(this.mGet.getAppContext())) {
            boolean isEnoughInternal = isEnoughToUse(checkFor, 0);
            boolean isEnoughExternal = isEnoughToUse(checkFor, 1);
            if (!isEnoughInternal && !isEnoughExternal) {
                dialogId = 128;
            } else if (storageType == 0 && isEnoughExternal) {
                dialogId = 124;
                this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_EXTERNAL, true);
            } else if (storageType == 1 && isEnoughInternal) {
                dialogId = 125;
                this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_INTERNAL, true);
            }
        } else if (StorageProperties.getNoOfStorageVolumes(this.mGet.getAppContext()) != 0) {
            dialogId = storageType == 1 ? 123 : 122;
        } else {
            return;
        }
        if (useDialog) {
            final int finalId = dialogId;
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (PhoneStorageManager.this.mGet.getDialogID() != finalId) {
                        PhoneStorageManager.this.mGet.showDialog(finalId);
                    }
                }
            }, 0);
        }
    }

    private int doHandleNasStorageFull(int checkFor, int storageType) {
        if (isEnoughStorage(checkFor, 0)) {
            this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_INTERNAL, true);
            return 126;
        } else if (!isEnoughStorage(checkFor, 1)) {
            return 128;
        } else {
            this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_EXTERNAL, true);
            return 127;
        }
    }

    private boolean isEnoughStorage(int checkFor, int storageType) {
        if (StorageProperties.isMemoryMounted(this.mGet.getAppContext(), storageType)) {
            return isEnoughToUse(checkFor, storageType);
        }
        return false;
    }

    public static void setShotMode(String shotMode) {
        sCurShotMode = shotMode;
    }

    public void onPauseAfter() {
        SharedPreferenceUtilPersist.saveDataCleared(getAppContext(), 0);
        super.onPauseAfter();
    }
}
