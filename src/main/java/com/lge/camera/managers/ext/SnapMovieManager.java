package com.lge.camera.managers.ext;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.file.DBScanManager.DBScanInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.FileNamer;
import com.lge.camera.managers.MediaEditorManager;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class SnapMovieManager extends SnapMovieManagerBase implements DBScanInterface {
    protected int mCountShutterClicked = 0;
    protected boolean mIsRec3sec = false;
    private MediaEditorManager mMediaEditorManager = null;
    protected SaveSnapMovieClipsTask mSaveSnapMovieClipsTask;
    protected int mTakenDegree = 0;
    private final float ratio = 1.7777778f;

    protected class SaveSnapMovieClipsTask extends AsyncTask<Void, Integer, Integer> {
        protected SaveSnapMovieClipsTask() {
        }

        protected void onPreExecute() {
            CamLog.m3d(CameraConstants.TAG, "PreExecute START");
            SnapMovieManager.this.setStatus(3);
            if (SnapMovieManager.this.mGet != null) {
                SnapMovieManager.this.mGet.showSavingDialog(true, 0);
                if (FunctionProperties.isSupportedConeUI()) {
                    SnapMovieManager.this.mGet.enableConeMenuIcon(31, false);
                }
            }
            CamLog.m3d(CameraConstants.TAG, "PreExecute END");
        }

        protected Integer doInBackground(Void... params) {
            CamLog.m3d(CameraConstants.TAG, "doInBackground START");
            if (SnapMovieManager.this.mMediaEditorManager != null && SnapMovieManager.this.mMediaEditorManager.mergeVideoClips()) {
                CamLog.m3d(CameraConstants.TAG, "snap movie - save video clips");
                while (isSaveProcessing()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                CamLog.m3d(CameraConstants.TAG, "doInBackground END");
            }
            return null;
        }

        private boolean isSaveProcessing() {
            return SnapMovieManager.this.mGet.getSaveResult() == 0;
        }

        private boolean isSaveCompleted() {
            boolean completed = true;
            if (SnapMovieManager.this.mGet == null) {
                return false;
            }
            if (SnapMovieManager.this.mGet.getSaveResult() != 1) {
                completed = false;
            }
            CamLog.m3d(CameraConstants.TAG, "snap movie - save completed : " + completed);
            return completed;
        }

        protected void onPostExecute(Integer result) {
            CamLog.m3d(CameraConstants.TAG, "onPostExecute START : " + isSaveCompleted());
            SnapMovieManager.this.resetTask(isSaveCompleted());
            CamLog.m3d(CameraConstants.TAG, "onPostExecute END");
        }
    }

    private class ThreadMergeVideos extends Thread {
        private ThreadMergeVideos() {
        }

        /* synthetic */ ThreadMergeVideos(SnapMovieManager x0, C12821 x1) {
            this();
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "merge video START : " + SnapMovieManager.this.mStatus);
            if (SnapMovieManager.this.mGet != null) {
                String dir = SnapMovieManager.this.mGet.getCurDir();
                int storage = SnapMovieManager.this.mGet.getCurStorage();
                int maxBitrate = SnapMovieManager.this.mShotList.getMaxBitrate();
                if (SnapMovieManager.this.mGet.checkModuleValidate(15) && SnapMovieManager.this.mGet.checkStorage(3, storage, SnapMovieManager.this.mShotList.getCurrentTime(), maxBitrate, true)) {
                    final ArrayList<String> clipPathList = new ArrayList();
                    for (int i = 0; SnapMovieManager.this.mShotList.size() > i; i++) {
                        ShotItem item = (ShotItem) SnapMovieManager.this.mShotList.valueAt(i);
                        if (item != null) {
                            clipPathList.add(item.getFilePath());
                        }
                    }
                    String fileName = SnapMovieManager.this.getFileName(1, storage, dir);
                    if (fileName != null) {
                        String savePath = dir + fileName + ".mp4";
                        SnapMovieManager.this.mGet.setSavePath(savePath);
                        SnapMovieManager.this.mMediaEditorManager = new MediaEditorManager(SnapMovieManager.this.mGet, savePath, clipPathList, maxBitrate);
                        CamLog.m3d(CameraConstants.TAG, "create VideoTrimManager");
                        SnapMovieManager.this.mGet.runOnUiThread(new HandlerRunnable(SnapMovieManager.this) {
                            public void handleRun() {
                                if (clipPathList == null || clipPathList.size() <= 0 || SnapMovieManager.this.mMediaEditorManager == null) {
                                    SnapMovieManager.this.resetTask(false);
                                } else {
                                    SnapMovieManager.this.initMediaEditorCompleted(true);
                                }
                            }
                        });
                        CamLog.m3d(CameraConstants.TAG, "merge video END : " + SnapMovieManager.this.mStatus);
                        super.run();
                        return;
                    }
                    return;
                }
                SnapMovieManager.this.setStatus(1);
                SnapMovieManager.this.mGet.runOnUiThread(new HandlerRunnable(SnapMovieManager.this) {
                    public void handleRun() {
                        SnapMovieManager.this.mGet.setQuickButtonEnable(100, true, true);
                    }
                });
            }
        }
    }

    public SnapMovieManager(SnapMovieInterface snapMovieInterface) {
        super(snapMovieInterface);
    }

    public void setCountShutterClicked(int count) {
        this.mCountShutterClicked = count;
    }

    public void doRecodingJob(boolean isStart) {
        if (this.mGet != null) {
            setVisibleButton(!isStart);
            this.mIsRecording = isStart;
            if (isStart) {
                setVisibleGuideText(false);
                setVisibleThumbLayout(false);
                setVisibleThumb(false);
                setVisibleHandler(false);
                setVisibleDuration(false);
                if (this.mBar != null) {
                    this.mBar.setMode(2);
                    this.mBar.setVisibility(0);
                }
                this.mTakenDegree = this.mGet.getOrientationDegree();
                if (!this.mOrientationFixed) {
                    this.mFixedDegree = this.mTakenDegree;
                    SharedPreferenceUtil.saveSnapMovieOrientation(getAppContext(), this.mTakenDegree);
                    setOrientationFixed(true);
                }
                saveThumbnail(null, null, true, 1000);
                return;
            }
            if (this.mBar != null) {
                this.mBar.setMode(0);
                this.mBar.setSelectedIndex(this.mShotList.getLastIndex());
            }
            this.mGet.hideZoomBar();
            updateBar();
            setVisibleHandler(true);
            setVisibleDuration(true);
            setVisibleBar(true);
        }
    }

    public void increaseShotCountAndTime(int useType, boolean isRec3Sec) {
        boolean z = true;
        if (this.mGet != null && this.mShotList != null) {
            boolean z2;
            if (!this.mOrientationFixed && this.mShotList.size() > 0) {
                this.mFixedDegree = this.mTakenDegree;
                SharedPreferenceUtil.saveSnapMovieOrientation(getAppContext(), this.mTakenDegree);
                rotateView(this.mTakenDegree, false);
                setOrientationFixed(true);
            }
            if (useType == 0) {
                addShotTime(3000);
            } else {
                addShotTime(updateVideoLastItem());
            }
            int currentTime = this.mShotList.getCurrentTime();
            updateSnapDurationTime(currentTime);
            if (currentTime > SnapMovieInterfaceImpl.SHOT_TIME_MAX || SnapMovieInterfaceImpl.SHOT_TIME_MAX - currentTime < 500) {
                setBarMaxTime(currentTime);
            }
            int size = this.mShotList.size();
            if (size > 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            setVisibleButton(z2);
            if (size <= 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            setVisibleGuideText(z2);
            if (size <= 0) {
                z = false;
            }
            setVisibleDuration(z);
        }
    }

    public void decreaseShotCountAndTime(int useType) {
        boolean z = true;
        CamLog.m3d(CameraConstants.TAG, "remove type : " + useType);
        if (this.mShotList != null && this.mShotList.size() >= 1) {
            boolean z2;
            if (useType == 0) {
                this.mShotList.remove(this.mShotList.getLastIndex());
                addShotTime(-3000);
            } else {
                int duration = this.mShotList.getLastItem().getDuration();
                this.mShotList.remove(this.mShotList.getLastIndex());
                if (duration != -1) {
                    addShotTime(-duration);
                }
            }
            updateSnapDurationTime(this.mShotList.getCurrentTime());
            setBarSelectedIndex(this.mShotList.getLastIndex());
            int size = this.mShotList.size();
            if (size > 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            setVisibleButton(z2);
            if (size > 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            setVisibleThumbLayout(z2);
            if (size <= 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            setVisibleGuideText(z2);
            if (size <= 0) {
                z = false;
            }
            setVisibleDuration(z);
        }
    }

    public int loadShotList() {
        boolean z = true;
        if (this.mGet == null) {
            return 0;
        }
        String dirPath = this.mGet.getCurTempDir();
        if (dirPath == null || this.mShotList == null || this.mBar == null) {
            return 0;
        }
        boolean z2;
        this.mShotList.clear();
        this.mBar.setCurrentTime(0);
        this.mShotList = getAvaiableFileList(dirPath);
        int currentTime = this.mShotList.getCurrentTime();
        if (currentTime > SnapMovieInterfaceImpl.SHOT_TIME_MAX || SnapMovieInterfaceImpl.SHOT_TIME_MAX - currentTime < 500) {
            this.mBar.setMaxTime(currentTime);
        }
        updateSnapDurationTime(currentTime);
        this.mBar.setCurrentTime(currentTime);
        int size = this.mShotList.size();
        setVisibleButton(size > 0);
        if (size > 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        setVisibleThumbLayout(z2);
        if (size <= 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        setVisibleGuideText(z2);
        if (size <= 0) {
            z = false;
        }
        setVisibleDuration(z);
        this.mBar.setSelectedIndex(size - 1);
        if (SecureImageUtil.isSecureCamera() && SecureImageUtil.useSecureLockImage() && !this.mGet.isStartedFromQuickCover() && !this.mGet.isModuleChanging()) {
            if (SecureImageUtil.get().getSnapLockedSize() <= -1) {
                SecureImageUtil.get().setSnapLocked(size, currentTime);
            }
            this.mBar.setDisabledWidth(SecureImageUtil.get().getSnapLockedSize(), SecureImageUtil.get().getSnapLockedTime());
        }
        return size;
    }

    private void setBarSelectedIndex(final int index) {
        setVisibleThumbLayout(false);
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (SnapMovieManager.this.mBar != null) {
                    SnapMovieManager.this.mBar.setSelectedIndex(index);
                }
                SnapMovieManager.this.updateBar();
                SnapMovieManager.this.setVisibleThumbLayout(true);
            }
        });
    }

    private void resetVariables() {
        if (this.mShotList != null) {
            this.mShotList.clear();
        }
        if (this.mBar != null) {
            this.mBar.setCurrentTime(0);
        }
        updateSnapDurationTime(0);
        setVisibleThumb(false);
        setVisibleThumbLayout(false);
        setVisibleHandler(false);
        setOrientationFixed(false);
        setVisibleButton(false);
        if (!(this.mGet == null || this.mGet.getAppContext() == null || this.mGuideText == null)) {
            this.mGuideText.setText(makeSpannableGuideString(this.mGet.getAppContext(), this.mGet.getAppContext().getString(C0088R.string.snap_init_guide2)));
            this.mGuideText.append("\n ");
        }
        setVisibleDuration(false);
        this.mCountShutterClicked = 0;
    }

    public void deleteAllShot(boolean isShowToast) {
        if (this.mGet != null) {
            setVisibleThumb(false);
            setVisibleThumbLayout(false);
            setVisibleHandler(false);
            setVisibleButton(false);
            setVisibleDuration(false);
            setBarMaxTime(SnapMovieInterfaceImpl.SHOT_TIME_MAX);
            FileManager.deleteAllFiles(this.mGet.getCurTempDir());
            if (isShowToast) {
                this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.popup_delete_done), CameraConstants.TOAST_LENGTH_SHORT);
            }
            resetVariables();
            setVisibleGuideText(true);
            this.mFixedDegree = -1;
            SharedPreferenceUtil.saveSnapMovieOrientation(getAppContext(), -1);
            rotateView(this.mGet.getOrientationDegree(), false);
        }
    }

    public void addShotTime(int time) {
        int shotTime = this.mShotList == null ? 0 : this.mShotList.getCurrentTime();
        if (shotTime < 0) {
            shotTime = 0;
        } else if (shotTime >= SnapMovieInterfaceImpl.SHOT_TIME_MAX) {
            showGuideLimitWarning();
        }
        setBarTime(shotTime, true);
        if (this.mBar == null) {
            return;
        }
        if (time < 0) {
            this.mBar.removeLastSeparator();
        } else {
            this.mBar.addSeparator(time);
        }
    }

    protected void showGuideLimitWarning() {
        int stringId = (SecureImageUtil.isSecureCamera() && SecureImageUtil.useSecureLockImage()) ? C0088R.string.snap_guide_tap_to_save_secure : C0088R.string.snap_completed_and_save;
        if (this.mGet != null) {
            this.mGet.showToast(getAppContext().getString(stringId), CameraConstants.TOAST_LENGTH_SHORT);
        }
    }

    protected int updateVideoLastItem() {
        if (this.mShotList == null) {
            return 0;
        }
        int recDuration = 0;
        ShotItem item = this.mShotList.getLastItem();
        if (!(item == null || item.getFilePath() == null)) {
            recDuration = FileUtil.getDurationFromFilePath(this.mGet.getAppContext(), item.getFilePath());
        }
        if (item == null || item.getDuration() != -1) {
            return recDuration;
        }
        this.mShotList.getLastItem().setDuration(recDuration);
        return recDuration;
    }

    public void setBarTime(int time, boolean isSeperator) {
        if (this.mBar != null) {
            this.mBar.setCurrentTime(time);
        }
    }

    public void launchVideoStudio(String type) {
        if (this.mShotList.size() < 1) {
            this.mGet.showToast("no saved file", CameraConstants.TOAST_LENGTH_SHORT);
            return;
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.lge.videostudio", "com.lge.videostudio.VEMainActivity"));
        ArrayList<String> extraValueInputPath = new ArrayList();
        for (int i = 0; i < this.mShotList.size(); i++) {
            extraValueInputPath.add(((ShotItem) this.mShotList.valueAt(i)).getFilePath());
        }
        intent.putStringArrayListExtra(type, extraValueInputPath);
        if (SnapMovieInterfaceImpl.EXTRA_NAME_VIDEO_EDITOR_TYPE_STYLE.equals(type)) {
            setStatus(3);
            String dir = this.mGet.getCurDir();
            String fileName = getFileName(1, this.mGet.getCurStorage(), dir);
            if (fileName != null) {
                intent.putExtra("save_path", dir + fileName + ".mp4");
            } else {
                return;
            }
        }
        if (IntentBroadcastUtil.isIntentAvailable(getActivity().getPackageManager(), intent)) {
            try {
                if (SnapMovieInterfaceImpl.EXTRA_NAME_VIDEO_EDITOR_TYPE_STYLE.equals(type)) {
                    this.mGet.getActivity().startActivityForResult(intent, 1);
                    return;
                } else {
                    this.mGet.getActivity().startActivity(intent);
                    return;
                }
            } catch (ActivityNotFoundException e) {
                this.mGet.showToast(getAppContext().getString(C0088R.string.error_not_exist_app), CameraConstants.TOAST_LENGTH_SHORT);
                return;
            }
        }
        this.mGet.showToast(getAppContext().getString(C0088R.string.error_not_exist_app), CameraConstants.TOAST_LENGTH_SHORT);
    }

    public ShotItemList getAvaiableFileList(String dirPath) {
        ShotItemList result = new ShotItemList();
        File dir = new File(dirPath);
        if (dir != null && dir.exists()) {
            String[] children = dir.list();
            if (children != null) {
                ArrayList<String> sList = new ArrayList();
                for (String child : children) {
                    if (!child.contains("_thumb")) {
                        sList.add(child);
                    }
                }
                Collections.sort(sList);
                addFileList(result, dir, sList);
            }
        }
        return result;
    }

    public ShotItemList addFileList(ShotItemList result, File dir, ArrayList<String> sList) {
        Iterator it = sList.iterator();
        while (it.hasNext()) {
            File file = new File(dir, (String) it.next());
            if (file != null && file.exists()) {
                String fileName = file.getName();
                boolean isPicture = fileName.contains(".jpg");
                boolean isVideo = fileName.contains(".mp4");
                if (isPicture || isVideo) {
                    String[] name = fileName.split("\\.");
                    int duration = isPicture ? 3000 : FileUtil.getDurationFromFilePath(getAppContext(), file.getPath());
                    CamLog.m3d(CameraConstants.TAG, "duration = " + duration);
                    if (name.length > 1 && duration > 0) {
                        try {
                            String path = file.getPath();
                            result.put(Integer.valueOf(name[0]).intValue(), new ShotItem(path, duration));
                            this.mBar.addSeparator(duration);
                            CamLog.m3d(CameraConstants.TAG, "add list = " + path + " (duration=" + duration);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return result;
    }

    public void setStatus(int value) {
        CamLog.m3d(CameraConstants.TAG, "setStatus " + value);
        this.mStatus = value;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public int getRemainShotTime(boolean isRec3Sec) {
        int recTime = 3000;
        if (this.mShotList == null) {
            return 0;
        }
        int remain = SnapMovieInterfaceImpl.SHOT_TIME_MAX - this.mShotList.getCurrentTime();
        if (remain < 500) {
            return 0;
        }
        if (remain >= 3000 || this.mShotList.getCurrentTime() >= SnapMovieInterfaceImpl.SHOT_TIME_MAX) {
            setBarMaxTime(SnapMovieInterfaceImpl.SHOT_TIME_MAX);
        } else if (isRec3Sec) {
            if (remain <= SHOT_TIME_VIDEO_MIN) {
                recTime = 2000;
            }
            setBarMaxTime((SnapMovieInterfaceImpl.SHOT_TIME_MAX_NO_DAMPER + recTime) - remain);
            return recTime;
        } else if (remain > SHOT_TIME_VIDEO_MIN) {
            setBarMaxTime(SnapMovieInterfaceImpl.SHOT_TIME_MAX);
        } else {
            setBarMaxTime((SHOT_TIME_VIDEO_MIN + SnapMovieInterfaceImpl.SHOT_TIME_MAX_NO_DAMPER) - remain);
            return SHOT_TIME_VIDEO_MIN;
        }
        if (!isRec3Sec || remain <= 3000) {
            return remain;
        }
        int retTimeWithDamper = 3000;
        int curSize = this.mShotList.size();
        if (curSize >= 0 && curSize < 3000) {
            retTimeWithDamper = 3000 + (20 - curSize);
        }
        return retTimeWithDamper;
    }

    public int getShotTime() {
        return this.mShotList.getCurrentTime();
    }

    public void putShotList(int count, String filePath, int duration) {
        if (this.mShotList != null) {
            this.mShotList.put(count, new ShotItem(filePath, duration));
        }
    }

    public String getFileName(int useType, int storage, String dir) {
        String result;
        if (this.mStatus == 3) {
            FileNamer.get().markTakeTime(CameraConstants.MODE_SNAP);
            result = FileNamer.get().getFileNewName(getAppContext(), useType, storage, dir, false, CameraConstants.MODE_SNAP);
        } else {
            int shotCount = 0;
            if (this.mShotList != null && this.mShotList.size() > 0) {
                shotCount = this.mShotList.keyAt(this.mShotList.getLastIndex()) + 1;
            }
            result = convertDigitNameFormat(shotCount);
            String filePath = this.mGet.getCurTempDir();
            if (filePath == null) {
                return null;
            }
            int shotTime;
            if (!FileManager.isFileExist(filePath)) {
                new File(filePath).mkdirs();
            }
            if (useType == 0) {
                filePath = filePath + result + ".jpg";
                shotTime = 3000;
            } else {
                filePath = filePath + result + ".mp4";
                shotTime = -1;
            }
            putShotList(shotCount, filePath, shotTime);
            if (this.mShotList != null) {
                CamLog.m3d(CameraConstants.TAG, "file name : " + result + " (" + this.mShotList.getCurrentTime() + "/" + SnapMovieInterfaceImpl.SHOT_TIME_MAX + ")");
            }
        }
        return result;
    }

    public void saveThumbnail(final String path, final Bitmap bitmap, boolean isUseThread, final int delay) {
        if (this.mGet != null) {
            if (isUseThread) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep((long) delay);
                        } catch (InterruptedException e) {
                            CamLog.m3d(CameraConstants.TAG, e.toString());
                        }
                        SnapMovieManager.this.saveThumbnail(path, bitmap);
                    }
                }).start();
            } else {
                saveThumbnail(path, bitmap);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x004f A:{SYNTHETIC, Splitter: B:21:0x004f} */
    /* JADX WARNING: Removed duplicated region for block: B:55:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x005e A:{SYNTHETIC, Splitter: B:29:0x005e} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x006a A:{SYNTHETIC, Splitter: B:35:0x006a} */
    private void saveThumbnailFile(java.lang.String r8, android.graphics.Bitmap r9) {
        /*
        r7 = this;
        r4 = "CameraApp";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "thumb file name : ";
        r5 = r5.append(r6);
        r5 = r5.append(r8);
        r5 = r5.toString();
        com.lge.camera.util.CamLog.m3d(r4, r5);
        r4 = r7.mGet;
        if (r4 != 0) goto L_0x001d;
    L_0x001c:
        return;
    L_0x001d:
        if (r8 == 0) goto L_0x001c;
    L_0x001f:
        if (r9 == 0) goto L_0x001c;
    L_0x0021:
        r4 = r9.isRecycled();
        if (r4 != 0) goto L_0x001c;
    L_0x0027:
        r1 = new java.io.File;
        r1.<init>(r8);
        r2 = 0;
        r3 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x0049, IOException -> 0x0058 }
        r3.<init>(r1);	 Catch:{ FileNotFoundException -> 0x0049, IOException -> 0x0058 }
        r4 = android.graphics.Bitmap.CompressFormat.JPEG;	 Catch:{ FileNotFoundException -> 0x0079, IOException -> 0x0076, all -> 0x0073 }
        r5 = 85;
        r9.compress(r4, r5, r3);	 Catch:{ FileNotFoundException -> 0x0079, IOException -> 0x0076, all -> 0x0073 }
        r3.flush();	 Catch:{ FileNotFoundException -> 0x0079, IOException -> 0x0076, all -> 0x0073 }
        if (r3 == 0) goto L_0x007c;
    L_0x003e:
        r3.close();	 Catch:{ IOException -> 0x0043 }
        r2 = r3;
        goto L_0x001c;
    L_0x0043:
        r0 = move-exception;
        r0.printStackTrace();
        r2 = r3;
        goto L_0x001c;
    L_0x0049:
        r0 = move-exception;
    L_0x004a:
        r0.printStackTrace();	 Catch:{ all -> 0x0067 }
        if (r2 == 0) goto L_0x001c;
    L_0x004f:
        r2.close();	 Catch:{ IOException -> 0x0053 }
        goto L_0x001c;
    L_0x0053:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x001c;
    L_0x0058:
        r0 = move-exception;
    L_0x0059:
        r0.printStackTrace();	 Catch:{ all -> 0x0067 }
        if (r2 == 0) goto L_0x001c;
    L_0x005e:
        r2.close();	 Catch:{ IOException -> 0x0062 }
        goto L_0x001c;
    L_0x0062:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x001c;
    L_0x0067:
        r4 = move-exception;
    L_0x0068:
        if (r2 == 0) goto L_0x006d;
    L_0x006a:
        r2.close();	 Catch:{ IOException -> 0x006e }
    L_0x006d:
        throw r4;
    L_0x006e:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x006d;
    L_0x0073:
        r4 = move-exception;
        r2 = r3;
        goto L_0x0068;
    L_0x0076:
        r0 = move-exception;
        r2 = r3;
        goto L_0x0059;
    L_0x0079:
        r0 = move-exception;
        r2 = r3;
        goto L_0x004a;
    L_0x007c:
        r2 = r3;
        goto L_0x001c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.SnapMovieManager.saveThumbnailFile(java.lang.String, android.graphics.Bitmap):void");
    }

    private synchronized void saveThumbnail(String path, Bitmap bitmap) {
        CamLog.m3d(CameraConstants.TAG, "saveThumbnail START");
        if (!this.mGet.checkModuleValidate(15) || this.mGet.getCameraDevice() == null) {
            CamLog.m3d(CameraConstants.TAG, "EXIT saveThumbnail");
        } else {
            if (path == null) {
                String dirPath = this.mGet.getCurTempDir();
                if (dirPath != null) {
                    path = dirPath + convertDigitNameFormat(this.mShotList.keyAt(this.mShotList.getLastIndex())) + "_thumb" + ".jpg";
                }
            }
            String finalPath = path;
            CamLog.m3d(CameraConstants.TAG, "saveThumbnail finalPath=" + finalPath);
            if (bitmap == null) {
                Rect r = new Rect();
                int[] lcd_size = Utils.getLCDsize(getAppContext(), true);
                r.left = 0;
                r.right = lcd_size[1];
                int len = (int) (((float) lcd_size[1]) * 1.7777778f);
                r.top = (lcd_size[0] - len) / 2;
                r.bottom = r.top + len;
                bitmap = BitmapManagingUtil.getRotatedImage(Utils.getScreenShot(lcd_size[1], (int) (((float) lcd_size[1]) * 1.7777778f), true, r), this.mFixedDegree, false);
                if (bitmap != null) {
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 4, bitmap.getHeight() / 4, true);
                    bitmap.recycle();
                    if (this.mGet.isRearCamera()) {
                        saveThumbnailFile(finalPath, resizedBitmap);
                    } else {
                        Matrix m = new Matrix();
                        m.setScale(-1.0f, 1.0f);
                        saveThumbnailFile(finalPath, Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight(), m, false));
                        resizedBitmap.recycle();
                    }
                }
            } else {
                saveThumbnailFile(finalPath, rotateThumbBitmap(this.mGet.getCameraDevice(), bitmap));
            }
            CamLog.m3d(CameraConstants.TAG, "saveThumbnail END");
        }
    }

    protected Bitmap rotateThumbBitmap(CameraProxy camera, Bitmap bitmap) {
        CamLog.m3d(CameraConstants.TAG, "saveThumbnail get color converted bitmap");
        int orientationHint = CameraDeviceUtils.getOrientationHint((this.mGet.getDisplayOrientation() + this.mFixedDegree) % 360, this.mGet.getCameraId());
        int thumbDegree = orientationHint;
        boolean isFlip = this.mGet.isNeedFlip();
        if (isFlip && (thumbDegree == 90 || thumbDegree == 270)) {
            thumbDegree = (thumbDegree + 180) % 360;
        }
        CamLog.m3d(CameraConstants.TAG, "thumbDegree=" + thumbDegree + " orientationHint=" + orientationHint + " fixedDegree=" + this.mFixedDegree + " isFlip=" + isFlip);
        return BitmapManagingUtil.getRotatedImage(bitmap, thumbDegree, this.mGet.isNeedFlip());
    }

    protected String convertDigitNameFormat(int input) {
        String output = "00" + input;
        return output.substring(output.length() - 3);
    }

    private void resetTask(boolean isSuccess) {
        CamLog.m3d(CameraConstants.TAG, "snap movie - reset task :" + isSuccess);
        setStatus(1);
        if (this.mGet != null) {
            this.mGet.setQuickButtonEnable(100, true, true);
            this.mGet.showSavingDialog(false, 0);
            if (isSuccess) {
                SharedPreferences pref = this.mGet.getActivity().getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
                if (pref != null && pref.getBoolean(CameraConstants.SNAP_DO_NOT_SHOW_SAVE_NOTE, false)) {
                    this.mGet.showToastConstant(this.mGet.getActivity().getString(C0088R.string.msg_saved_to_gallery));
                }
                deleteAllShot(false);
                this.mGet.sendLDBIntentOnAfterStopRecording();
            } else {
                this.mGet.showToastConstant(this.mGet.getActivity().getString(C0088R.string.error_write_file));
            }
            if (this.mMediaEditorManager != null) {
                this.mMediaEditorManager.release();
                this.mMediaEditorManager = null;
            }
            if (FunctionProperties.isSupportedConeUI()) {
                this.mGet.enableConeMenuIcon(31, true);
            }
        }
    }

    protected void saveVideoClips() {
        CamLog.m3d(CameraConstants.TAG, "saveVideoClips : " + this.mStatus);
        if (this.mGet == null || this.mShotList == null || this.mShotList.size() == 0 || this.mStatus >= 3) {
            CamLog.m3d(CameraConstants.TAG, "EXIT saveVideoClips : " + this.mStatus);
            return;
        }
        setStatus(3);
        setVisibleThumb(false);
        this.mGet.setQuickButtonEnable(100, false, true);
        new ThreadMergeVideos(this, null).start();
    }

    public void startScan(String filePath) {
    }

    public void initMediaEditorCompleted(boolean isSuccess) {
        CamLog.m3d(CameraConstants.TAG, "initMediaEditorCompleted START");
        if (this.mGet.getCameraDevice() == null || !isSuccess) {
            this.mGet.showSavingDialog(false, 0);
            if (FunctionProperties.isSupportedConeUI()) {
                this.mGet.enableConeMenuIcon(31, true);
            }
            setStatus(1);
            return;
        }
        this.mGet.updateSaveResult(0);
        this.mSaveSnapMovieClipsTask = new SaveSnapMovieClipsTask();
        this.mSaveSnapMovieClipsTask.execute(new Void[0]);
        CamLog.m3d(CameraConstants.TAG, "initMediaEditorCompleted END");
    }

    public void onPauseAfter() {
        setVisibleBar(false, true);
        setVisibleGuideText(false);
        resetVariables();
        this.mGet.getReviewThumbnailManager().setHideMode(false);
        super.onPauseAfter();
    }

    protected int getTextGuideBottomMargin(Context mContext, TextView mTextView, int maxWidth, boolean portrait) {
        if (mTextView == null || "".equals(mTextView.getText().toString())) {
            return 0;
        }
        if (portrait) {
            return RatioCalcUtil.getSizeCalculatedByPercentage(mContext, true, 0.117f);
        }
        return Utils.getPx(mContext, C0088R.dimen.snap_movie_guide_text_marginBottom_land);
    }

    public void onDBScanCompleted(String path, Uri uri) {
        SharedPreferenceUtil.saveLastThumbnailUri(getAppContext(), uri);
    }
}
