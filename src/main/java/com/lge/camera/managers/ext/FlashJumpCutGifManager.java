package com.lge.camera.managers.ext;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.SparseArray;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.file.Exif;
import com.lge.camera.file.FileManager;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.Utils;
import com.lge.media.GifEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FlashJumpCutGifManager extends ManagerInterfaceImpl implements Callback {
    private static final int DO_ENCODING = 0;
    private static final int DO_FINISH_ENCODING = 2;
    private static final int DO_UNLOCK = 1;
    public static final int FLASH_JUMPCUT_SPEED_IN_MS = 200;
    private static final String GIF_FILE_EXTENSION = ".gif";
    private static final String GIF_FILE_NAME = "_GIF";
    private static int sGifBitMapHeight;
    private static int sGifBitMapWidth;
    public static String sSaveFileName;
    public ArrayList<String> mBitMapFileList = null;
    private int mBitmapCount = 0;
    private ExecutorService mBitmapTaskPool = null;
    public int mCompleteJumpCutCount;
    private boolean mGifCancelState = false;
    protected GifEncoder mGifEncoder = null;
    private long mStartTime = 0;
    private MakeGIFTask makeGifTask;

    class BitmapTask implements Runnable {
        private String mFilePath;
        private SparseArray<Bitmap> mFrame;
        private int mIndex;
        private int mSize;

        public BitmapTask(SparseArray<Bitmap> frame, String filePath, int size, int index) {
            this.mFrame = frame;
            this.mFilePath = filePath;
            this.mIndex = index;
            this.mSize = size;
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "[GIF] index:" + this.mIndex + " mFilePath :" + this.mFilePath + "-start");
            Bitmap bitmap = FlashJumpCutGifManager.this.getBitmap(this.mFilePath);
            if (!(bitmap == null || this.mFrame == null)) {
                this.mFrame.put(this.mIndex, bitmap);
            }
            CamLog.m3d(CameraConstants.TAG, "[GIF] index:" + this.mIndex + " mFilePath:" + this.mFilePath + "-end");
            if (this.mFrame != null && this.mFrame.size() == this.mSize && FlashJumpCutGifManager.this.mBitmapTaskPool != null) {
                FlashJumpCutGifManager.this.mBitmapTaskPool.shutdown();
            }
        }
    }

    private class MakeGIFTask extends AsyncTask<Void, Void, Void> {
        private MakeGIFTask() {
        }

        protected Void doInBackground(Void... arg0) {
            if (FlashJumpCutGifManager.this.mBitMapFileList == null || FlashJumpCutGifManager.this.mBitMapFileList.size() == 0) {
                CamLog.m5e(CameraConstants.TAG, "[JumpCut][GIF] mByteMap == null!!!");
                return null;
            }
            CamLog.m7i(CameraConstants.TAG, "[JumpCut][GIF_Time] doInBackground start");
            FlashJumpCutGifManager.this.setVariableForGif();
            FlashJumpCutGifManager.this.startGifEncoder(FlashJumpCutGifManager.this.makeGifTargetPath());
            Bitmap firstBitmap = FlashJumpCutGifManager.this.getBitmap(FlashJumpCutGifManager.this.mBitMapFileList == null ? null : (String) FlashJumpCutGifManager.this.mBitMapFileList.get(0));
            if (!(FlashJumpCutGifManager.this.mGifEncoder == null || firstBitmap == null)) {
                CamLog.m7i(CameraConstants.TAG, "[JumpCut][GIF_Time] Starting encoding. Get bitmap and Call addFrame.");
                FlashJumpCutGifManager.this.mGifEncoder.addFrame(firstBitmap);
                HandlerThread handlerThread = new HandlerThread("GifHandlerThread");
                handlerThread.start();
                Handler handler = new Handler(handlerThread.getLooper(), FlashJumpCutGifManager.this);
                int increase = 0;
                for (int i = 1; i < FlashJumpCutGifManager.this.mBitmapCount; i++) {
                    int usedSize;
                    SparseArray<Bitmap> frame = new SparseArray(3);
                    int remainder = (FlashJumpCutGifManager.this.mBitmapCount - increase) - 1;
                    if (remainder >= 3) {
                        usedSize = 3;
                    } else {
                        usedSize = remainder;
                    }
                    if (FlashJumpCutGifManager.this.mGifCancelState || isCancelled()) {
                        CamLog.m5e(CameraConstants.TAG, "[JumpCut][GIF] gif encoding stop. because it is cancelled");
                        FlashJumpCutGifManager.this.recycleBitmap(frame, frame.size());
                        handler.obtainMessage(2).sendToTarget();
                        handlerThread.quitSafely();
                        return null;
                    }
                    boolean waitTask = false;
                    FlashJumpCutGifManager.this.mBitmapTaskPool = Executors.newFixedThreadPool(3);
                    for (int j = 0; j < usedSize; j++) {
                        increase++;
                        if (increase >= FlashJumpCutGifManager.this.mCompleteJumpCutCount || FlashJumpCutGifManager.this.mBitMapFileList == null || FlashJumpCutGifManager.this.mBitMapFileList.get(increase) == null) {
                            CamLog.m3d(CameraConstants.TAG, "[JumpCut][GIF] gifEncoding return false. Uri list is over burstCount OR Uri list is null");
                            FlashJumpCutGifManager.this.recycleBitmap(frame, frame.size());
                            handler.obtainMessage(2).sendToTarget();
                            handlerThread.quitSafely();
                            return null;
                        }
                        if (!(FlashJumpCutGifManager.this.mBitmapTaskPool == null || FlashJumpCutGifManager.this.mBitmapTaskPool.isShutdown())) {
                            FlashJumpCutGifManager.this.mBitmapTaskPool.execute(new BitmapTask(frame, (String) FlashJumpCutGifManager.this.mBitMapFileList.get(increase), usedSize, j));
                            waitTask = true;
                        }
                    }
                    doEncoding(handler, frame, waitTask);
                }
                CamLog.m7i(CameraConstants.TAG, "[Jumpcut][GIF_Time] gifEncoding done result = " + waitEncoding(handler) + ", bitmapCount = " + increase + ", duration = " + (((float) (SystemClock.uptimeMillis() - FlashJumpCutGifManager.this.mStartTime)) / 1000.0f));
                handlerThread.quitSafely();
            }
            FlashJumpCutGifManager.this.mGet.setQuickShareAfterGifMaking();
            FlashJumpCutGifManager.this.finishGifEncoder();
            return null;
        }

        private void doEncoding(Handler handler, SparseArray<Bitmap> frame, boolean waitTask) {
            if (waitTask) {
                try {
                    if (FlashJumpCutGifManager.this.mBitmapTaskPool.awaitTermination(5, TimeUnit.SECONDS)) {
                        handler.obtainMessage(0, frame).sendToTarget();
                        FlashJumpCutGifManager.this.mBitmapTaskPool = null;
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            CamLog.m11w(CameraConstants.TAG, "[jumpcut][gif] return doEncoding");
        }

        private boolean waitEncoding(Handler handler) {
            ConditionVariable lock = new ConditionVariable();
            handler.obtainMessage(1, lock).sendToTarget();
            return lock.block(60000);
        }

        protected void onCancelled() {
            CamLog.m7i(CameraConstants.TAG, "[JumpCut][GIF] onCancelled");
            FlashJumpCutGifManager.this.mGet.showProcessingDialog(false, 0);
            FlashJumpCutGifManager.this.mGet.setGifEncoding(false);
        }

        protected void onPostExecute(Void result) {
            CamLog.m3d(CameraConstants.TAG, "[JumpCut][GIF] onPostExecute : hide ProcessingDialog");
            FlashJumpCutGifManager.this.mGet.showProcessingDialog(false, 0);
            FlashJumpCutGifManager.this.mGet.setGifEncoding(false);
        }

        protected void onPreExecute() {
            CamLog.m3d(CameraConstants.TAG, "[JumpCut][GIF] onPreExecute : show ProcessingDialog");
            FlashJumpCutGifManager.this.mGet.setGifEncoding(true);
            FlashJumpCutGifManager.this.mGet.setWaitSavingDialogType(5);
            FlashJumpCutGifManager.this.mGet.showProcessingDialog(true, 0);
        }
    }

    public FlashJumpCutGifManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void executeGifMake() {
        this.mStartTime = SystemClock.uptimeMillis();
        if (this.mCompleteJumpCutCount > this.mBitMapFileList.size()) {
            CamLog.m7i(CameraConstants.TAG, "[gif] return!! during add list size");
            this.mGet.showSavingDialog(true, 0);
            this.mGet.setWaitSavingDialogType(4);
        } else if (this.mBitMapFileList != null && !this.mBitMapFileList.isEmpty()) {
            this.makeGifTask = new MakeGIFTask();
            if (this.makeGifTask != null) {
                this.makeGifTask.execute(new Void[0]);
            }
        }
    }

    private String makeGifTargetPath() {
        sSaveFileName = this.mGet.getCurDir() + (this.mGet.makeFileName(0, this.mGet.getCurStorage(), this.mGet.getCurDir(), false, CameraConstants.MODE_GIF) + GIF_FILE_NAME) + GIF_FILE_EXTENSION;
        return sSaveFileName;
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 0) {
            SparseArray<Bitmap> obj = msg.obj;
            if (obj == null || !(obj instanceof SparseArray)) {
                CamLog.m3d(CameraConstants.TAG, "[jumpcut][gif] DO_ENCODING return");
                return false;
            }
            SparseArray<Bitmap> bitmapFrame = obj;
            int size = bitmapFrame.size();
            switch (size) {
                case 1:
                    CamLog.m7i(CameraConstants.TAG, "[jumpcut][gif] addFrame");
                    this.mGifEncoder.addFrame((Bitmap) bitmapFrame.get(0));
                    break;
                case 2:
                    CamLog.m7i(CameraConstants.TAG, "[jumpcut][gif] addFrameDouble");
                    this.mGifEncoder.addFrameDouble((Bitmap) bitmapFrame.get(0), (Bitmap) bitmapFrame.get(1));
                    break;
                case 3:
                    CamLog.m7i(CameraConstants.TAG, "[jumpcut][gif] addFrameTriple");
                    this.mGifEncoder.addFrameTriple((Bitmap) bitmapFrame.get(0), (Bitmap) bitmapFrame.get(1), (Bitmap) bitmapFrame.get(2));
                    break;
                default:
                    CamLog.m7i(CameraConstants.TAG, "[jumpcut][gif] bitmap frame size is not 1,2,3");
                    break;
            }
            recycleBitmap(bitmapFrame, size);
        } else if (msg.what == 1) {
            ConditionVariable obj2 = msg.obj;
            if (obj2 != null && (obj2 instanceof ConditionVariable)) {
                obj2.open();
            }
        } else if (msg.what == 2) {
            finishGifEncoder();
        }
        return true;
    }

    private void setVariableForGif() {
        CamLog.m3d(CameraConstants.TAG, "[jumpcut][gif] mCompleteJumpCutCount : " + this.mCompleteJumpCutCount + ", mBitmapCount : " + this.mBitmapCount);
        int[] size = Utils.sizeStringToArray(this.mGet.getCurrentSelectedPreviewSize());
        float ratio = ((float) size[0]) / ((float) size[1]);
        if (ModelProperties.isLongLCDModel() && ratio >= 2.05f) {
            sGifBitMapHeight = 2268;
            sGifBitMapWidth = CameraConstantsEx.FHD_SCREEN_RESOLUTION;
        } else if (ModelProperties.isLongLCDModel() && ratio >= 2.0f) {
            sGifBitMapHeight = 2160;
            sGifBitMapWidth = CameraConstantsEx.FHD_SCREEN_RESOLUTION;
        } else if (ratio >= 1.7f) {
            sGifBitMapHeight = 1920;
            sGifBitMapWidth = CameraConstantsEx.FHD_SCREEN_RESOLUTION;
        } else if (ratio >= 1.3f) {
            sGifBitMapHeight = 1920;
            sGifBitMapWidth = CameraConstantsEx.QHD_SCREEN_RESOLUTION;
        } else if (ratio >= 1.0f) {
            sGifBitMapHeight = CameraConstantsEx.FHD_SCREEN_RESOLUTION;
            sGifBitMapWidth = CameraConstantsEx.FHD_SCREEN_RESOLUTION;
        }
        for (int i = 0; i < this.mCompleteJumpCutCount; i++) {
            this.mBitmapCount++;
        }
        CamLog.m3d(CameraConstants.TAG, "[JumpCut][GIF] preview size[0] : " + size[0] + ", size[1] : " + size[1] + ", sGifBitMapHeight : " + sGifBitMapHeight + ", sGifBitMapWidth : " + sGifBitMapWidth);
        CamLog.m3d(CameraConstants.TAG, "[JumpCut][GIF] mCompleteBurstCount = " + this.mCompleteJumpCutCount + ", mBitMapFileList.size() = " + this.mBitMapFileList.size() + ", bitmapCount = " + this.mBitmapCount);
    }

    private void startGifEncoder(String targetPath) {
        CamLog.m7i(CameraConstants.TAG, "[JumpCut][GIF_time] gifEncoding start");
        this.mGifEncoder = new GifEncoder();
        this.mGifEncoder.setDelay(200);
        this.mGifEncoder.start(targetPath);
    }

    private void finishGifEncoder() {
        CamLog.m7i(CameraConstants.TAG, "[JumpCut][GIF] finishGifEncoder");
        if (!this.mGifCancelState) {
            Uri uri = FileUtil.getUriFromPath(this.mGet.getAppContext(), sSaveFileName, true);
            this.mGet.callMediaSave(uri);
            updateContentValue(uri);
        }
        this.mBitmapCount = 0;
        this.mCompleteJumpCutCount = 0;
        FileManager.deleteAllFileInPath(getAppContext(), this.mGet.getTempDir());
        resetGifFileListArray();
        if (this.mGifEncoder != null) {
            this.mGifEncoder.finish();
            this.mGifEncoder = null;
        }
    }

    private void updateContentValue(Uri uri) {
        int[] imageSizes = BitmapManagingUtil.getImageSize(getAppContext(), uri);
        if (imageSizes != null && imageSizes.length >= 2) {
            ContentResolver cr = getAppContext().getContentResolver();
            ContentValues values = new ContentValues();
            int imageHeight = imageSizes[1];
            int imageWidth = imageSizes[0];
            values.put("height", Integer.valueOf(imageHeight));
            values.put("width", Integer.valueOf(imageWidth));
            cr.update(uri, values, null, null);
        }
    }

    public void onResumeBefore() {
        CamLog.m3d(CameraConstants.TAG, "[JumpCut][GIF] sIsGifEncoding onResume. isGIFEncoding ? " + this.mGet.isGIFEncoding());
        this.mGifCancelState = false;
        if (!this.mGet.isGIFEncoding()) {
            FileManager.deleteAllFileInPath(getAppContext(), this.mGet.getTempDir());
        }
        super.onResumeBefore();
    }

    private void recycleBitmap(SparseArray<Bitmap> frame, int frameSize) {
        if (frame != null) {
            for (int i = 0; i < frameSize; i++) {
                Bitmap bitmap = (Bitmap) frame.get(i);
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
            frame.clear();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        CamLog.m3d(CameraConstants.TAG, "[Jumpcut][GIF] onDestroy.");
        this.mBitMapFileList = null;
        try {
            if (this.makeGifTask.getStatus() == Status.RUNNING) {
                this.mGifCancelState = true;
                this.makeGifTask.cancel(true);
            }
        } catch (Exception e) {
            CamLog.m3d(CameraConstants.TAG, "[JumpCut][GIF] onDestory : during cancelling makeGifTask");
        }
    }

    public void setGifShotCount(int count) {
        CamLog.m7i(CameraConstants.TAG, "[jumpcut][gif] mCompleteJumpCutCount : " + count);
        this.mCompleteJumpCutCount = count;
    }

    public void addFilePath(String filePath) {
        if (filePath != null && this.mBitMapFileList != null) {
            CamLog.m7i(CameraConstants.TAG, "[GIF] list size : " + this.mBitMapFileList.size() + ", addFilePath : " + filePath);
            this.mBitMapFileList.add(filePath);
        }
    }

    public void createGifFileListArray() {
        if (this.mBitMapFileList == null) {
            this.mBitMapFileList = new ArrayList();
        } else {
            this.mBitMapFileList.clear();
        }
    }

    public void resetGifFileListArray() {
        if (this.mBitMapFileList != null) {
            this.mBitMapFileList.clear();
            this.mBitMapFileList = null;
        }
    }

    public boolean isGifListEmpty() {
        return this.mBitMapFileList == null || this.mBitMapFileList.isEmpty();
    }

    public Bitmap getBitmap(String filePath) {
        if (this.mBitMapFileList == null || filePath == null) {
            CamLog.m11w(CameraConstants.TAG, "[JumpCut][Gif] mByteMap null! getBitmap return.");
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        if (bitmap == null) {
            return bitmap;
        }
        int exifDegree = Exif.getOrientation(Exif.readExif(filePath));
        Matrix matrix = new Matrix();
        if (!(bitmap.getWidth() == sGifBitMapWidth && bitmap.getHeight() == sGifBitMapHeight)) {
            float s1;
            float s1x = ((float) sGifBitMapWidth) / ((float) bitmap.getWidth());
            float s1y = ((float) sGifBitMapHeight) / ((float) bitmap.getHeight());
            if (s1x < s1y) {
                s1 = s1x;
            } else {
                s1 = s1y;
            }
            matrix.postScale(s1, s1);
        }
        matrix.postRotate((float) exifDegree);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (resizedBitmap == null) {
            return null;
        }
        CamLog.m3d(CameraConstants.TAG, "[jumpcut][GIF] exifDegree : " + exifDegree + ", resizedBitmap.getWidth() : " + resizedBitmap.getWidth() + ", resizedBitmap.getHeight() : " + resizedBitmap.getHeight());
        if (resizedBitmap != bitmap) {
            bitmap.recycle();
        }
        if (matrix != null) {
        }
        return resizedBitmap;
    }

    public int getWaitSavingDialogType() {
        return this.mGet.getWaitSavingDialogType();
    }
}
