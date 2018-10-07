package com.lge.camera.managers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.graphics.Bitmap;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import com.lge.media.GifEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GifManager extends ManagerInterfaceImpl implements Callback {
    public static final int DEFAULT_SPEED_IN_MS = 200;
    private static final int DO_ENCODING = 0;
    private static final int DO_FINISH_ENCODING = 2;
    private static final int DO_UNLOCK = 1;
    private static final String GIF_FILE_EXTENSION = ".gif";
    private static final String GIF_FILE_NAME = "_GIF";
    private static final String GIF_FILE_NAME_FOR_SNAP = "_SnapGIF";
    private static int sCompleteBurstCount;
    private static int sGifHeight;
    private static int sGifWidth;
    public static String sSaveFileName;
    public final float GIF_VIEW_BOTTOM_MARGIN_RATIO = 0.22f;
    public final float GIF_VIEW_BOTTOM_MARGIN_RATIO_LONG_LCD = 0.289f;
    public final float GIF_VIEW_BOTTOM_MARGIN_RATIO_LONG_LCD_NOTCH = 0.317f;
    public final float GIF_VIEW_BOTTOM_MARGIN_RATIO_TABLET = 0.14f;
    private int mBitmapCount = 0;
    private ExecutorService mBitmapTaskPool = null;
    public ArrayList<Uri> mGifArrayList = null;
    protected RotateImageButton mGifButton = null;
    private boolean mGifCancelState = false;
    protected GifEncoder mGifEncoder = null;
    protected View mGifMakerView = null;
    protected View mGifMakerlayout = null;
    private boolean mHideTransient = false;
    private boolean mIsBurstShotVisibleState = false;
    private boolean mIsGifEncoding = false;
    private int mQuotient = 0;
    private long mStartTime = 0;
    private MakeGIFTask makeGifTask;

    /* renamed from: com.lge.camera.managers.GifManager$1 */
    class C09721 implements OnClickListener {
        C09721() {
        }

        public void onClick(View v) {
            CamLog.m7i(CameraConstants.TAG, "[GIF] gif button is clicked. start GIF maker.");
            if (GifManager.this.mGifArrayList == null || GifManager.this.mGifArrayList.size() == 0) {
                CamLog.m5e(CameraConstants.TAG, "[GIF] gifArrayList == null!!!");
            } else if (GifManager.this.mGet.isGifButtonAvailable()) {
                GifManager.this.mIsGifEncoding = true;
                GifManager.this.mGet.setSelfieOptionVisibility(false, true);
                GifManager.this.mStartTime = SystemClock.uptimeMillis();
                GifManager.this.mGet.stopMotionEngine();
                if (GifManager.sCompleteBurstCount > GifManager.this.mGifArrayList.size()) {
                    GifManager.this.mGet.showSavingDialog(true, 0);
                    GifManager.this.mGet.setWaitSavingDialogType(4);
                } else {
                    GifManager.this.executeGifMake();
                }
                GifManager.this.setGifVisibility(false);
                GifManager.this.mGet.setQuickClipIcon(true, false);
            }
        }
    }

    class BitmapTask implements Runnable {
        private SparseArray<Bitmap> mFrame;
        private int mIndex;
        private int mSize;
        private Uri mUri;

        public BitmapTask(SparseArray<Bitmap> frame, Uri uri, int size, int index) {
            this.mFrame = frame;
            this.mUri = uri;
            this.mIndex = index;
            this.mSize = size;
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "[GIF] index:" + this.mIndex + " uri:" + this.mUri + "-start");
            Bitmap bitmap = GifManager.this.getBitmap(this.mUri);
            if (!(bitmap == null || this.mFrame == null)) {
                this.mFrame.put(this.mIndex, bitmap);
            }
            CamLog.m3d(CameraConstants.TAG, "[GIF] index:" + this.mIndex + " uri:" + this.mUri + "-end");
            if (this.mFrame != null && this.mFrame.size() == this.mSize && GifManager.this.mBitmapTaskPool != null) {
                GifManager.this.mBitmapTaskPool.shutdown();
            }
        }
    }

    private class MakeGIFTask extends AsyncTask<Void, Void, Void> {
        private MakeGIFTask() {
        }

        /* synthetic */ MakeGIFTask(GifManager x0, C09721 x1) {
            this();
        }

        protected Void doInBackground(Void... arg0) {
            if (GifManager.this.mGifArrayList == null || GifManager.this.mGifArrayList.size() == 0) {
                CamLog.m5e(CameraConstants.TAG, "[GIF] gifArrayList == null!!!");
                return null;
            }
            CamLog.m7i(CameraConstants.TAG, "[GIF_Time] doInBackground start");
            GifManager.this.setVariableForGif();
            GifManager.this.startGifEncoder(GifManager.this.makeGifTargetPath());
            Bitmap firstBitmap = GifManager.this.getBitmap(GifManager.this.mGifArrayList == null ? null : (Uri) GifManager.this.mGifArrayList.get(0));
            if (!(GifManager.this.mGifEncoder == null || firstBitmap == null)) {
                CamLog.m7i(CameraConstants.TAG, "[GIF_Time] Starting encoding. Get bitmap and Call addFrame.");
                GifManager.this.mGifEncoder.addFrame(firstBitmap);
                HandlerThread handlerThread = new HandlerThread("GifHandlerThread");
                handlerThread.start();
                Handler handler = new Handler(handlerThread.getLooper(), GifManager.this);
                int increase = 0;
                for (int i = 1; i < GifManager.this.mBitmapCount; i++) {
                    int usedSize;
                    GifManager.this.mBitmapTaskPool = Executors.newFixedThreadPool(3);
                    SparseArray<Bitmap> frame = new SparseArray(3);
                    int remainder = (GifManager.this.mBitmapCount - increase) - 1;
                    if (remainder >= 3) {
                        usedSize = 3;
                    } else {
                        usedSize = remainder;
                    }
                    boolean waitTask = false;
                    if (GifManager.this.mGifCancelState || isCancelled()) {
                        CamLog.m5e(CameraConstants.TAG, "[GIF] gif encoding stop. because it is cancelled");
                        GifManager.this.recycleBitmap(frame, frame.size());
                        handler.obtainMessage(2).sendToTarget();
                        handlerThread.quitSafely();
                        return null;
                    }
                    for (int j = 0; j < usedSize; j++) {
                        increase++;
                        if (GifManager.this.mQuotient * increase >= GifManager.this.mGet.getCompleteBurstCount() || GifManager.this.mGifArrayList == null || GifManager.this.mGifArrayList.get(GifManager.this.mQuotient * increase) == null) {
                            CamLog.m3d(CameraConstants.TAG, "[GIF] gifEncoding return false. Uri list is over burstCount OR Uri list is null");
                            GifManager.this.recycleBitmap(frame, frame.size());
                            handler.obtainMessage(2).sendToTarget();
                            handlerThread.quitSafely();
                            return null;
                        }
                        if (!(GifManager.this.mBitmapTaskPool == null || GifManager.this.mBitmapTaskPool.isShutdown())) {
                            GifManager.this.mBitmapTaskPool.execute(new BitmapTask(frame, (Uri) GifManager.this.mGifArrayList.get(GifManager.this.mQuotient * increase), usedSize, j));
                            waitTask = true;
                        }
                    }
                    doEncoding(handler, frame, waitTask);
                }
                CamLog.m7i(CameraConstants.TAG, "[GIF_Time] gifEncoding done result = " + waitEncoding(handler) + ", bitmapCount = " + increase + ", duration = " + (((float) (SystemClock.uptimeMillis() - GifManager.this.mStartTime)) / 1000.0f));
                handlerThread.quitSafely();
            }
            GifManager.this.mGet.setQuickShareAfterGifMaking();
            GifManager.this.finishGifEncoder();
            return null;
        }

        private void doEncoding(Handler handler, SparseArray<Bitmap> frame, boolean waitTask) {
            if (waitTask) {
                try {
                    if (GifManager.this.mBitmapTaskPool.awaitTermination(5, TimeUnit.SECONDS)) {
                        handler.obtainMessage(0, frame).sendToTarget();
                        GifManager.this.mBitmapTaskPool = null;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean waitEncoding(Handler handler) {
            ConditionVariable lock = new ConditionVariable();
            handler.obtainMessage(1, lock).sendToTarget();
            return lock.block(60000);
        }

        protected void onCancelled() {
            CamLog.m7i(CameraConstants.TAG, "[GIF] onCancelled");
            GifManager.this.mGet.showProcessingDialog(false, 0);
            GifManager.this.mIsGifEncoding = false;
        }

        protected void onPostExecute(Void result) {
            CamLog.m3d(CameraConstants.TAG, "[GIF] onPostExecute : hide ProcessingDialog");
            GifManager.this.mGet.showProcessingDialog(false, 0);
            GifManager.this.mIsGifEncoding = false;
        }

        protected void onPreExecute() {
            CamLog.m3d(CameraConstants.TAG, "[GIF] onPreExecute : show ProcessingDialog");
            GifManager.this.mIsGifEncoding = true;
            GifManager.this.mGet.setWaitSavingDialogType(5);
            GifManager.this.mGet.showProcessingDialog(true, 0);
        }
    }

    public GifManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void onGIFButtonListener() {
        if (this.mGifButton != null) {
            this.mGifButton.setOnClickListener(new C09721());
        }
    }

    public void executeGifMake() {
        if (this.mGet.isGifButtonAvailable()) {
            this.makeGifTask = new MakeGIFTask(this, null);
            if (this.makeGifTask != null) {
                this.makeGifTask.execute(new Void[0]);
                return;
            }
            return;
        }
        CamLog.m5e(CameraConstants.TAG, "[GIF] executeGifMake return.");
        this.mIsGifEncoding = false;
    }

    private String makeGifTargetPath() {
        String fileName = this.mGet.makeFileName(0, this.mGet.getCurStorage(), this.mGet.getCurDir(), false, CameraConstants.MODE_GIF);
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(this.mGet.getShotMode())) {
            fileName = fileName + GIF_FILE_NAME_FOR_SNAP;
        } else {
            fileName = fileName + GIF_FILE_NAME;
        }
        sSaveFileName = this.mGet.getCurDir() + fileName + GIF_FILE_EXTENSION;
        return sSaveFileName;
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 0) {
            SparseArray<Bitmap> obj = msg.obj;
            if (obj == null || !(obj instanceof SparseArray)) {
                return false;
            }
            SparseArray<Bitmap> frame = obj;
            int size = frame.size();
            switch (size) {
                case 1:
                    this.mGifEncoder.addFrame((Bitmap) frame.get(0));
                    break;
                case 2:
                    this.mGifEncoder.addFrameDouble((Bitmap) frame.get(0), (Bitmap) frame.get(1));
                    break;
                case 3:
                    this.mGifEncoder.addFrameTriple((Bitmap) frame.get(0), (Bitmap) frame.get(1), (Bitmap) frame.get(2));
                    break;
            }
            recycleBitmap(frame, size);
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
        int tempSavedArraySize = this.mGifArrayList.size();
        sCompleteBurstCount = this.mGet.getCompleteBurstCount();
        this.mQuotient = sCompleteBurstCount % 20 == 0 ? sCompleteBurstCount / 20 : (sCompleteBurstCount / 20) + 1;
        int[] size = Utils.sizeStringToArray(this.mGet.getCurrentSelectedPreviewSize());
        float ratio = ((float) size[0]) / ((float) size[1]);
        if (ModelProperties.isLongLCDModel() && ratio >= 2.05f) {
            sGifHeight = 2268;
            sGifWidth = CameraConstantsEx.FHD_SCREEN_RESOLUTION;
        } else if (ModelProperties.isLongLCDModel() && ratio >= 2.0f) {
            sGifHeight = 2160;
            sGifWidth = CameraConstantsEx.FHD_SCREEN_RESOLUTION;
        } else if (ratio >= 1.7f) {
            sGifHeight = 1920;
            sGifWidth = CameraConstantsEx.FHD_SCREEN_RESOLUTION;
        } else if (ratio >= 1.3f) {
            sGifHeight = 1920;
            sGifWidth = CameraConstantsEx.QHD_SCREEN_RESOLUTION;
        } else if (ratio >= 1.0f) {
            sGifHeight = CameraConstantsEx.FHD_SCREEN_RESOLUTION;
            sGifWidth = CameraConstantsEx.FHD_SCREEN_RESOLUTION;
        }
        int i = 0;
        while (i < this.mGet.getCompleteBurstCount()) {
            this.mBitmapCount++;
            i += this.mQuotient;
        }
        CamLog.m3d(CameraConstants.TAG, "[GIF] mCompleteBurstCount = " + sCompleteBurstCount + ", gifArrayList.size() = " + this.mGifArrayList.size() + ", tempSavingSize = " + tempSavedArraySize + ", quotient = " + this.mQuotient + ", bitmapCount = " + this.mBitmapCount);
    }

    private void startGifEncoder(String targetPath) {
        CamLog.m7i(CameraConstants.TAG, "[GIF_time] gifEncoding start");
        this.mGifEncoder = new GifEncoder();
        this.mGifEncoder.setDelay(200);
        this.mGifEncoder.start(targetPath);
    }

    private void finishGifEncoder() {
        CamLog.m7i(CameraConstants.TAG, "[GIF] finishGifEncoder");
        if (!this.mGifCancelState) {
            Uri uri = FileUtil.getUriFromPath(this.mGet.getAppContext(), sSaveFileName, true);
            this.mGet.callMediaSave(uri);
            updateContentValue(uri);
        }
        this.mIsBurstShotVisibleState = false;
        this.mBitmapCount = 0;
        this.mGifArrayList = null;
        this.mGifEncoder.finish();
        this.mGifEncoder = null;
    }

    private void updateContentValue(Uri uri) {
        int[] sizes = BitmapManagingUtil.getImageSize(getAppContext(), uri);
        if (sizes != null && sizes.length >= 2) {
            int width = sizes[0];
            int height = sizes[1];
            ContentResolver cr = getAppContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put("width", Integer.valueOf(width));
            values.put("height", Integer.valueOf(height));
            cr.update(uri, values, null, null);
        }
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

    public void onConfigurationChanged(Configuration config) {
        CamLog.m3d(CameraConstants.TAG, "[gif] onConfigurationChanged");
        if (!this.mGet.isAttachIntent()) {
            removeGIFView();
            setGifIconLayout();
        }
        super.onConfigurationChanged(config);
    }

    public void onResumeBefore() {
        CamLog.m3d(CameraConstants.TAG, "[GIF] sIsGifEncoding onResume");
        if (!this.mGet.isAttachIntent()) {
            setGifIconLayout();
        }
        setDegree(getOrientationDegree(), false);
        setGifVisibility(false);
        setGifVisibleStatus(false);
        this.mGifCancelState = false;
        super.onResumeBefore();
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        removeGIFView();
    }

    public void onDestroy() {
        super.onDestroy();
        CamLog.m3d(CameraConstants.TAG, "[GIF] onDestroy : Array list set null.");
        this.mGifArrayList = null;
        this.mIsBurstShotVisibleState = false;
        this.mIsGifEncoding = false;
        try {
            if (this.makeGifTask.getStatus() == Status.RUNNING) {
                this.mGifCancelState = true;
                this.makeGifTask.cancel(true);
            }
        } catch (Exception e) {
            CamLog.m3d(CameraConstants.TAG, "[GIF] onDestory : during cancelling makeGifTask");
        }
    }

    public void removeGIFView() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (this.mGifMakerlayout != null) {
            vg.removeView(this.mGifMakerlayout);
            this.mGifMakerlayout = null;
            this.mGifButton = null;
        }
    }

    public void onCameraSwitchingStart() {
        super.onCameraSwitchingStart();
        this.mIsBurstShotVisibleState = false;
        setGifVisibility(false);
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        if (this.mGifButton != null) {
            this.mGifButton.setDegree(degree, animation);
        }
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        if (this.mGifButton != null) {
            this.mGifButton.setDegree(degree, animation);
        }
    }

    public void setGifIconLayout() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (this.mGifMakerlayout == null) {
            this.mGifMakerlayout = this.mGet.inflateView(C0088R.layout.gif_maker);
        }
        if (!(vg == null || this.mGifMakerlayout == null)) {
            CamLog.m3d(CameraConstants.TAG, "[GIF] addView!!!!");
            vg.removeView(this.mGifMakerlayout);
            vg.addView(this.mGifMakerlayout, 0, new LayoutParams(-1, -1));
            this.mGifButton = (RotateImageButton) this.mGifMakerlayout.findViewById(C0088R.id.gif_icon_button);
            LayoutParams buttonParam = (LayoutParams) this.mGifMakerlayout.getLayoutParams();
            buttonParam.rightMargin = Utils.getPx(getAppContext(), C0088R.dimen.adjust_filter_gif_button_rightMargin);
            if (ModelProperties.isTablet(getAppContext())) {
                buttonParam.rightMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.07f) + Utils.getPx(getAppContext(), C0088R.dimen.adjust_filter_gif_button_rightMargin);
            }
            changeGifLayoutParam();
        }
        onGIFButtonListener();
    }

    public void changeGifLayoutParam() {
        if (this.mGifMakerlayout != null) {
            LayoutParams buttonParam = (LayoutParams) this.mGifMakerlayout.getLayoutParams();
            buttonParam.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.22f);
            if (ModelProperties.isLongLCDModel()) {
                buttonParam.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ModelProperties.getLCDType() == 2 ? 0.317f : 0.289f);
            } else if (ModelProperties.isTablet(getAppContext())) {
                buttonParam.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.14f);
            }
        }
    }

    public void hideTransient() {
        if (this.mGifMakerlayout != null && this.mGifMakerlayout.getVisibility() == 0) {
            this.mGifMakerlayout.setVisibility(4);
            this.mHideTransient = true;
        }
    }

    public void restoreVisibility() {
        if (this.mHideTransient && this.mGifMakerlayout != null && this.mGifMakerlayout.getVisibility() != 0) {
            this.mGifMakerlayout.setVisibility(0);
            this.mHideTransient = false;
        }
    }

    public void setHideTransient(boolean set) {
        this.mHideTransient = set;
    }

    public void setGifVisibility(boolean visible) {
        if (this.mGifMakerlayout != null) {
            String str = CameraConstants.TAG;
            StringBuilder append = new StringBuilder().append("[GIF] setVisibility : ");
            int i = (visible && isGifShowingCondition()) ? 0 : 4;
            CamLog.m3d(str, append.append(i).toString());
            changeGifLayoutParam();
            if (visible && isGifShowingCondition()) {
                this.mIsGifEncoding = false;
                sCompleteBurstCount = this.mGet.getCompleteBurstCount();
                this.mGifMakerlayout.setVisibility(0);
                this.mGet.setQuickClipIcon(false, false);
                return;
            }
            if (this.mGifMakerlayout.getVisibility() == 0) {
                this.mGifMakerlayout.setVisibility(4);
                this.mIsBurstShotVisibleState = false;
            }
            if (!this.mGet.isIntervalShotProgress()) {
                this.mGet.setQuickClipIcon(false, true);
            }
        }
    }

    private boolean isGifShowingCondition() {
        return (!this.mIsBurstShotVisibleState || this.mGifArrayList == null || this.mGifArrayList.isEmpty()) ? false : true;
    }

    public void createGifUriList() {
        if (this.mGifArrayList == null) {
            this.mGifArrayList = new ArrayList();
        } else {
            this.mGifArrayList.clear();
        }
    }

    public void addUri(Uri uri) {
        if (uri != null && this.mGifArrayList != null) {
            this.mGifArrayList.add(uri);
        }
    }

    public Bitmap getBitmap(Uri uri) {
        int exifDegree = 0;
        if (uri == null) {
            return null;
        }
        boolean isImageType;
        Bitmap reviewBmp;
        String filePath = FileUtil.getRealPathFromURI(getAppContext(), uri);
        String mediaType = getAppContext().getContentResolver().getType(uri);
        if (mediaType == null || !mediaType.startsWith(CameraConstants.MIME_TYPE_IMAGE)) {
            isImageType = false;
        } else {
            isImageType = true;
        }
        if (isImageType) {
            ExifInterface exif = Exif.readExif(filePath);
            if (isImageType) {
                exifDegree = Exif.getOrientation(exif);
            }
            reviewBmp = BitmapManagingUtil.loadScaledandRotatedBitmap(getAppContext().getContentResolver(), uri.toString(), sGifWidth, sGifHeight, exifDegree);
        } else {
            reviewBmp = BitmapManagingUtil.getThumbnailFromUri(getActivity(), uri, 1);
        }
        if (reviewBmp != null) {
            return reviewBmp;
        }
        CamLog.m3d(CameraConstants.TAG, "[GIF] reviewBmp is null.");
        return null;
    }

    public void setGifVisibleStatus(boolean isBurstDone) {
        this.mIsBurstShotVisibleState = isBurstDone;
    }

    public boolean getGifVisibleStatus() {
        return this.mIsBurstShotVisibleState;
    }

    public int getWaitSavingDialogType() {
        return this.mGet.getWaitSavingDialogType();
    }

    public boolean isGIFEncoding() {
        CamLog.m3d(CameraConstants.TAG, "[gif] mIsGifEncoding : " + this.mIsGifEncoding);
        return this.mIsGifEncoding;
    }

    public void setGifEncoding(boolean isGifEncoding) {
        this.mIsGifEncoding = isGifEncoding;
    }
}
