package com.lge.camera.managers.ext.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.managers.ext.sticker.solutions.ContentsInformation;
import com.lge.camera.managers.ext.sticker.solutions.SolutionBase;
import com.lge.camera.managers.ext.sticker.solutions.SolutionBase.ContentsTakenCallback;
import com.lge.camera.managers.ext.sticker.solutions.SolutionBase.InfoListener;
import com.lge.camera.managers.ext.sticker.solutions.StickerSolutionFactory;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class StickerGLSurfaceView extends GLSurfaceView implements Renderer, ContentsTakenCallback, InfoListener {
    private static final String TAG = "StickerGLSurfaceView";
    private boolean isAway = false;
    private CameraReadyListener mCameraReadyListener;
    private Context mCtx;
    private int mCurrentDegree = 0;
    private StickerInformationDataClass mLastSticker = null;
    private AtomicBoolean mShow = new AtomicBoolean(false);
    private SolutionBase mStickerSolution;
    private int mViewportHeight = 0;
    private int mViewportWidth = 0;
    private boolean skipUninitEngine = false;

    /* renamed from: com.lge.camera.managers.ext.sticker.StickerGLSurfaceView$2 */
    class C13382 implements Runnable {
        C13382() {
        }

        public void run() {
            if (StickerGLSurfaceView.this.mStickerSolution != null && !StickerGLSurfaceView.this.skipUninitEngine) {
                StickerGLSurfaceView.this.mStickerSolution.uninit();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.sticker.StickerGLSurfaceView$4 */
    class C13404 implements Runnable {
        C13404() {
        }

        public void run() {
            StickerGLSurfaceView.this.mStickerSolution.uninit();
            StickerGLSurfaceView.this.mStickerSolution = null;
        }
    }

    public interface CameraReadyListener {
        int currentCameraId();

        int getOrientation();

        int[] getPreviewSize();

        int[] getVideoSize();

        void infoListener(int i, Object obj);

        boolean initWithVideoSize();

        void onContentTaken(ContentsInformation contentsInformation);

        void setStickerDrawing(boolean z);
    }

    public void onContentTaken(ContentsInformation ci) {
        if (this.mCameraReadyListener != null) {
            this.mCameraReadyListener.onContentTaken(ci);
        }
    }

    public void onInfoListener(int action, Object value) {
        if (this.mCameraReadyListener != null) {
            this.mCameraReadyListener.infoListener(action, value);
        }
    }

    public StickerGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public StickerGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context ctx) {
        CamLog.m3d(TAG, "init");
        this.mCtx = ctx;
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(-3);
        setRenderer(this);
        setRenderMode(0);
        setZOrderMediaOverlay(true);
    }

    public void setCameraReadyListener(CameraReadyListener listener) {
        this.mCameraReadyListener = listener;
    }

    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        CamLog.m3d(TAG, "onSurfaceCreated called");
        if (this.mStickerSolution == null) {
            this.mStickerSolution = new StickerSolutionFactory().create(this.mCtx, 2);
            this.mStickerSolution.setContentsTakenCallback(this);
            this.mStickerSolution.setInfoListener(this);
            this.mStickerSolution.phoneOrientationDegree(this.mCurrentDegree);
        }
        if (this.mStickerSolution != null) {
            this.mStickerSolution.onSurfaceCreated(gl10, eglConfig);
        }
    }

    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        CamLog.m3d(TAG, "onSurfaceChanged called : " + width + " _ " + height);
        if (width == this.mViewportWidth && height == this.mViewportHeight) {
            GLES20.glViewport(0, 0, this.mViewportWidth, this.mViewportHeight);
            if (this.mStickerSolution != null) {
                config();
                this.mStickerSolution.onSurfaceChanged(gl10, width, height);
            }
        }
    }

    public void onDrawFrame(GL10 gl10) {
        if (!isShowing()) {
            return;
        }
        if (this.mStickerSolution != null) {
            this.mStickerSolution.onDrawFrame(gl10);
        } else {
            CamLog.m5e(TAG, "mStickerSolution is null");
        }
    }

    public void show() {
        CamLog.m3d(TAG, "");
        this.mShow.set(true);
        setVisibility(0);
    }

    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        CamLog.m3d(TAG, "onVisibilityChanged visibility = " + visibility);
        if (visibility == 0) {
            onResume();
        }
    }

    public void hide() {
        CamLog.m3d(TAG, "");
        this.mShow.set(false);
        if (this.mStickerSolution != null) {
            this.mStickerSolution.setDrawingInformation(new int[]{0, 0}, new int[]{0, 0}, 0, -1, false);
        }
        setVisibility(8);
    }

    public void justHide() {
        CamLog.m3d(TAG, "");
        this.mShow.set(false);
        setVisibility(8);
    }

    public boolean isShowing() {
        return this.mShow.get();
    }

    public void feedFrame(Image image) {
        if (isShowing() && image != null && this.mStickerSolution != null && this.mStickerSolution.isInited()) {
            this.mStickerSolution.process(image, this.mCurrentDegree);
            requestRender();
        }
    }

    public void feedFrame(byte[] datas) {
        if (isShowing() && datas != null && this.mStickerSolution != null && this.mStickerSolution.isInited()) {
            this.mStickerSolution.process(datas, this.mCurrentDegree);
            requestRender();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0051  */
    public boolean changeSticker(com.lge.camera.managers.ext.sticker.StickerInformationDataClass r4) {
        /*
        r3 = this;
        r1 = 1;
        r0 = "StickerGLSurfaceView";
        r2 = "";
        com.lge.camera.util.CamLog.m3d(r0, r2);
        r0 = r3.mCameraReadyListener;
        if (r0 == 0) goto L_0x0014;
    L_0x000c:
        r2 = r3.mCameraReadyListener;
        if (r4 == 0) goto L_0x0036;
    L_0x0010:
        r0 = r1;
    L_0x0011:
        r2.setStickerDrawing(r0);
    L_0x0014:
        if (r4 == 0) goto L_0x0038;
    L_0x0016:
        r0 = r3.mLastSticker;
        if (r0 == 0) goto L_0x0044;
    L_0x001a:
        r0 = r3.mLastSticker;
        r0 = r0.configFile;
        r2 = r4.configFile;
        r0 = r0.equals(r2);
        if (r0 == 0) goto L_0x0044;
    L_0x0026:
        r0 = r3.mStickerSolution;
        r0 = r0.isStickerDrawing();
        if (r0 == 0) goto L_0x0044;
    L_0x002e:
        r0 = "StickerGLSurfaceView";
        r2 = "skip setSticker via same content and drawing";
        com.lge.camera.util.CamLog.m3d(r0, r2);
    L_0x0035:
        return r1;
    L_0x0036:
        r0 = 0;
        goto L_0x0011;
    L_0x0038:
        r0 = r3.mLastSticker;
        if (r0 != 0) goto L_0x0044;
    L_0x003c:
        r0 = "StickerGLSurfaceView";
        r2 = "skip setSticker via same null";
        com.lge.camera.util.CamLog.m3d(r0, r2);
        goto L_0x0035;
    L_0x0044:
        r3.mLastSticker = r4;
        r0 = r3.mLastSticker;
        if (r0 == 0) goto L_0x004d;
    L_0x004a:
        r3.bringItAndShow();
    L_0x004d:
        r0 = r3.mStickerSolution;
        if (r0 == 0) goto L_0x0035;
    L_0x0051:
        r0 = r3.mStickerSolution;
        r2 = r3.mLastSticker;
        r0.setSticker(r2);
        goto L_0x0035;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.sticker.StickerGLSurfaceView.changeSticker(com.lge.camera.managers.ext.sticker.StickerInformationDataClass):boolean");
    }

    public void onCameraSwitchingStart() {
        CamLog.m3d(TAG, "");
        this.skipUninitEngine = true;
        hide();
    }

    public boolean setLayoutAndPosition(int width, int height, int topy) {
        CamLog.m3d(TAG, "");
        LayoutParams pam = (LayoutParams) getLayoutParams();
        if (pam.height == height && pam.width == width && pam.topMargin == topy) {
            return false;
        }
        int bufferw = width;
        int bufferh = height;
        if (width >= CameraConstantsEx.FHD_SCREEN_RESOLUTION) {
            bufferw = CameraConstantsEx.FHD_SCREEN_RESOLUTION;
            bufferh = (int) (((float) height) * (((float) 1080) / ((float) width)));
        }
        getHolder().setFixedSize(bufferw, bufferh);
        this.mViewportHeight = bufferh;
        this.mViewportWidth = bufferw;
        pam.height = height;
        pam.width = width;
        pam.topMargin = topy;
        setLayoutParams(pam);
        return true;
    }

    public boolean onCameraSwitchingEnd() {
        CamLog.m3d(TAG, "");
        this.skipUninitEngine = false;
        if (this.mLastSticker == null) {
            return false;
        }
        bringItAndShow();
        return true;
    }

    public void clearSticker() {
        changeSticker(null);
    }

    public void takePicture(final boolean needFlip, final Bitmap signature) {
        queueEvent(new Runnable() {
            public void run() {
                if (StickerGLSurfaceView.this.mStickerSolution != null) {
                    StickerGLSurfaceView.this.mStickerSolution.takePicture(StickerGLSurfaceView.this.mViewportWidth, StickerGLSurfaceView.this.mViewportHeight, needFlip, signature);
                }
            }
        });
    }

    public void setLastSticker() {
        changeSticker(this.mLastSticker);
    }

    public void reResumeEngine() {
        CamLog.m3d(TAG, "");
        if (this.mStickerSolution != null) {
            config();
            checkLastStickerValid();
        }
    }

    public boolean isStickerDrawing() {
        if (this.mStickerSolution == null || !this.mStickerSolution.isStickerDrawing()) {
            CamLog.m3d(TAG, "isStickerDrawing = false");
            return false;
        }
        CamLog.m3d(TAG, "isStickerDrawing = true");
        return true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        CamLog.m3d(TAG, "surfaceDestroyed");
        queueEvent(new C13382());
        super.surfaceDestroyed(holder);
    }

    public void startRecording(String dir, String filename, long maxFileSize, long maxDuration) {
        CamLog.m3d(TAG, "");
        final String str = dir;
        final String str2 = filename;
        final long j = maxFileSize;
        final long j2 = maxDuration;
        queueEvent(new Runnable() {
            public void run() {
                if (StickerGLSurfaceView.this.mStickerSolution != null) {
                    StickerGLSurfaceView.this.mStickerSolution.startRecording(str, str2, j, j2);
                }
            }
        });
    }

    public ContentsInformation stopRecording() {
        CamLog.m3d(TAG, "");
        ContentsInformation ci = null;
        if (this.mStickerSolution != null && this.mStickerSolution.isRecording()) {
            ci = this.mStickerSolution.stopRecording(this);
        }
        CamLog.m3d(TAG, "stopRecording ci = " + ci);
        if (ci != null && ci.isVideoError()) {
            onContentTaken(ci);
        } else if (ci == null) {
            onContentTaken(new ContentsInformation());
        }
        return ci;
    }

    public void pauseRecording() {
        CamLog.m3d(TAG, "");
        if (this.mStickerSolution != null) {
            this.mStickerSolution.pauseRecording();
        }
    }

    public void resumeRecording() {
        CamLog.m3d(TAG, "");
        if (this.mStickerSolution != null) {
            this.mStickerSolution.resumeRecording();
        }
    }

    private void config() {
        if (this.mStickerSolution != null) {
            this.mStickerSolution.setDrawingInformation(this.mCameraReadyListener.getPreviewSize(), this.mCameraReadyListener.getVideoSize(), this.mCameraReadyListener.getOrientation(), this.mCameraReadyListener.currentCameraId(), this.mCameraReadyListener.initWithVideoSize());
        }
    }

    public String[] getActionStringList() {
        if (this.mStickerSolution != null) {
            return this.mStickerSolution.getActionStringList();
        }
        return null;
    }

    public boolean isRecording() {
        if (this.mStickerSolution != null) {
            return this.mStickerSolution.isRecording();
        }
        return false;
    }

    public void destroy() {
        if (this.mStickerSolution != null) {
            queueEvent(new C13404());
        }
    }

    public void setPhoneOrientationDegree(int degree) {
        this.mCurrentDegree = degree;
        if (this.mStickerSolution != null) {
            this.mStickerSolution.phoneOrientationDegree(degree);
        }
    }

    public void clearLastSticker() {
        this.mLastSticker = null;
    }

    public boolean hasSticker() {
        return this.mLastSticker != null;
    }

    public String getCurrentStickerConfigPath() {
        if (this.mLastSticker != null) {
            return this.mLastSticker.configFile;
        }
        return null;
    }

    public String getLDBInfo() {
        StringBuilder sb = new StringBuilder();
        if (this.mLastSticker == null) {
            return null;
        }
        sb.append(LdbConstants.LDB_FEAT_NAME_STICKER_SOLUTION);
        sb.append("=");
        sb.append(this.mLastSticker.solution_type);
        sb.append(";");
        sb.append(LdbConstants.LDB_FEAT_NAME_STICKER_NAME);
        sb.append("=");
        sb.append(this.mLastSticker.sticker_name);
        sb.append(";");
        return sb.toString();
    }

    public int[] getViewportSize() {
        return new int[]{this.mViewportWidth, this.mViewportHeight};
    }

    public void sendAway(int distance) {
        if (distance != 0 && !this.isAway) {
            setTranslationY((float) distance);
            this.isAway = true;
        }
    }

    public void bringItAndShow() {
        if (this.isAway) {
            setTranslationY(0.0f);
            this.isAway = false;
        }
        show();
    }

    private void checkLastStickerValid() {
        if (this.mLastSticker != null && !new File(this.mLastSticker.configFile).exists()) {
            this.mLastSticker = null;
        }
    }

    public boolean isStickerLoadCompleted() {
        if (this.mLastSticker == null || this.mStickerSolution == null) {
            return false;
        }
        return this.mStickerSolution.isStickerLoadCompleted();
    }
}
