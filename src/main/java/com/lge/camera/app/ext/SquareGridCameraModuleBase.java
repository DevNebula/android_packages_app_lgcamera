package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.view.Surface;
import android.view.TextureView;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.SquareSnapCameraModule;
import com.lge.camera.app.ext.GridViewRenderer.STAvailableListenerCollage;
import com.lge.camera.app.ext.VideoPlayer.PlayerCallback;
import com.lge.camera.components.AudioData;
import com.lge.camera.components.MVRecordOutputInfo;
import com.lge.camera.components.MultiViewRecordInputInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.FileNamer;
import com.lge.camera.file.MediaSaveService.OnLocalSaveListener;
import com.lge.camera.file.SupportedExif;
import com.lge.camera.managers.GestureViewManager;
import com.lge.camera.managers.ext.GridCameraPostviewManagerBase.GridContentsInfo;
import com.lge.camera.managers.ext.GridCameraPostviewManagerExpand;
import com.lge.camera.managers.ext.GridPostViewListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.Utils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class SquareGridCameraModuleBase extends SquareSnapCameraModule implements GridPostViewListener {
    private static final String GRIDVIEW_FILE_NAME = "gridview_";
    private static final String GRIDVIEW_FILE_NAME_RETAKE = "retake_gridview_";
    protected static final String GRID_PICTURE_SIZE = "1440x1440";
    protected static final int MAX_CAPTURE_COUNT = 4;
    private final int MULTIVIEW_AUDIO_BIT_RATE = CameraConstants.VALUE_VIDEO_BITRATE_MMS;
    private final int MULTIVIEW_AUDIO_CHANNEL_COUNT = 2;
    private final String MULTIVIEW_AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private final int MULTIVIEW_AUDIO_SAMPLE_RATE = 48000;
    private final int MULTIVIEW_VIDEO_BIT_RATE = 17000000;
    private final int MULTIVIEW_VIDEO_FRAME_RATE = 30;
    private final int MULTIVIEW_VIDEO_IFRAME_INTERVAL = 1;
    private final String MULTIVIEW_VIDEO_MIME_TYPE = "video/avc";
    private LinkedList<AudioData>[] mAudioBuffer = null;
    protected int mCameraDegree = 0;
    protected int mCapturedCount = 0;
    private MVRecordOutputInfo mCollageOutputInfo = null;
    protected CountDownLatch mCollageSTLatch;
    protected GridContentsInfo[] mContentsInfo;
    protected int mCurDegree = 0;
    private int[] mFileType;
    protected GridCameraPostviewManagerExpand mGridPostviewManager = new GridCameraPostviewManagerExpand(this);
    protected MultiViewRecordInputInfo mInputInfo = null;
    protected boolean mIsCollageStarted = false;
    protected boolean mIsRecordButtonClicked = false;
    protected boolean mIsRecordingCanceled = false;
    protected boolean mIsRendererStarting = false;
    protected boolean mIsRetakeMode = false;
    protected boolean mIsSaveBtnClicked = false;
    protected boolean mIsSavingImage = false;
    private boolean mIsShutterButtonpressed = false;
    private long mMaxVideoDuration = Long.MIN_VALUE;
    private int mMaxVideoId = 0;
    private int mNumImageFiles;
    private int mNumTotalFiles;
    private int mNumVideoFile;
    private int mPlayDoneCount = 0;
    private int mPlayStartedCount = 0;
    private long[] mPlayerTimeStamp = null;
    private String mRecordedFileName = null;
    protected GridViewRenderer mRenderThread = null;
    protected int mRetakeViewIndex = -1;
    private STAvailableListenerCollage mSTAvailableListenerCollage = new C05046();
    protected SurfaceTexture[] mSurfaceTextureCollage;
    protected Object mSyncFreezePreview = null;
    private Object mSynchAudioBuffer = new Object();
    private Object mSynchVideoPlayer = new Object();
    protected String mTempFilePath = "";
    protected String mTempFilePathRetake = "";
    protected boolean mVisibleForPostView = false;

    /* renamed from: com.lge.camera.app.ext.SquareGridCameraModuleBase$6 */
    class C05046 implements STAvailableListenerCollage {
        C05046() {
        }

        public void onSurfaceTextureReady(SurfaceTexture[] st) {
            CamLog.m3d(CameraConstants.TAG, "gridview - onSurfaceTextureReady - Collage");
            SquareGridCameraModuleBase.this.mSurfaceTextureCollage = st;
            if (SquareGridCameraModuleBase.this.mCollageSTLatch != null) {
                SquareGridCameraModuleBase.this.mCollageSTLatch.countDown();
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.SquareGridCameraModuleBase$7 */
    class C05057 implements Runnable {
        C05057() {
        }

        public void run() {
            SquareGridCameraModuleBase.this.setUIOnCollageSaving();
            SquareGridCameraModuleBase.this.mSyncFreezePreview = new Object();
            synchronized (SquareGridCameraModuleBase.this.mSyncFreezePreview) {
                try {
                    CamLog.m3d(CameraConstants.TAG, "gridview - waitFor freezePreview");
                    SquareGridCameraModuleBase.this.mSyncFreezePreview.wait(CameraConstants.TOAST_LENGTH_SHORT);
                    CamLog.m3d(CameraConstants.TAG, "gridview - continue freezePreview");
                    DebugUtil.setEndTime("[+] MV: Final FreezePreview is Done");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            SquareGridCameraModuleBase.this.mSyncFreezePreview = null;
            MultiViewRecordInputInfo inputInfo = SquareGridCameraModuleBase.this.makeCollageInputInfo();
            if (inputInfo != null) {
                SquareGridCameraModuleBase.this.mNumTotalFiles = inputInfo.getTotalCount();
                SquareGridCameraModuleBase.this.mNumImageFiles = inputInfo.getImageCount();
                SquareGridCameraModuleBase.this.mNumVideoFile = SquareGridCameraModuleBase.this.mNumTotalFiles - SquareGridCameraModuleBase.this.mNumImageFiles;
                SquareGridCameraModuleBase.this.mFileType = inputInfo.getFileType();
                SquareGridCameraModuleBase.this.mPlayerTimeStamp = new long[SquareGridCameraModuleBase.this.mNumTotalFiles];
                for (int j = 0; j < SquareGridCameraModuleBase.this.mNumTotalFiles; j++) {
                    if (SquareGridCameraModuleBase.this.mFileType[j] == 1) {
                        SquareGridCameraModuleBase.this.mPlayerTimeStamp[j] = Long.MAX_VALUE;
                    } else {
                        SquareGridCameraModuleBase.this.mPlayerTimeStamp[j] = 0;
                    }
                }
                DebugUtil.setStartTime("[1] MV video: all video players are started");
                SquareGridCameraModuleBase.this.startMakingCollageVideo(inputInfo);
            }
        }
    }

    class VideoPlayerCallback implements PlayerCallback {
        private static final long MAX_TOLERABLE_DELAY_IN_USEC = 50000;
        private final int mId;

        public VideoPlayerCallback(int id) {
            this.mId = id;
        }

        public void postRender() {
            CamLog.m3d(CameraConstants.TAG, "id = " + this.mId + ", Collage - postRender");
        }

        public void loopReset() {
            CamLog.m3d(CameraConstants.TAG, "id = " + this.mId + ", Collage - loopReset");
        }

        public void playStarted(long playDuration) {
            synchronized (SquareGridCameraModuleBase.this.mSynchVideoPlayer) {
                SquareGridCameraModuleBase.this.mPlayStartedCount = SquareGridCameraModuleBase.this.mPlayStartedCount + 1;
                if (playDuration > SquareGridCameraModuleBase.this.mMaxVideoDuration) {
                    SquareGridCameraModuleBase.this.mMaxVideoId = this.mId;
                    SquareGridCameraModuleBase.this.mMaxVideoDuration = playDuration;
                }
                CamLog.m3d(CameraConstants.TAG, "duration [" + this.mId + "] = " + playDuration);
                if (SquareGridCameraModuleBase.this.mPlayStartedCount >= SquareGridCameraModuleBase.this.mNumVideoFile) {
                    SquareGridCameraModuleBase.this.mRenderThread.setSyncVideoId(SquareGridCameraModuleBase.this.mMaxVideoId);
                }
                if (SquareGridCameraModuleBase.this.mAudioBuffer == null) {
                    SquareGridCameraModuleBase.this.mAudioBuffer = new LinkedList[SquareGridCameraModuleBase.this.mNumTotalFiles];
                    for (int i = 0; i < SquareGridCameraModuleBase.this.mNumTotalFiles; i++) {
                        SquareGridCameraModuleBase.this.mAudioBuffer[i] = null;
                    }
                }
                SquareGridCameraModuleBase.this.mAudioBuffer[this.mId] = new LinkedList();
            }
        }

        public void playDone() {
            synchronized (SquareGridCameraModuleBase.this.mSynchVideoPlayer) {
                SquareGridCameraModuleBase.this.mPlayDoneCount = SquareGridCameraModuleBase.this.mPlayDoneCount + 1;
                if (SquareGridCameraModuleBase.this.mPlayDoneCount >= SquareGridCameraModuleBase.this.mNumTotalFiles - SquareGridCameraModuleBase.this.mNumImageFiles) {
                    SquareGridCameraModuleBase.this.mRenderThread.stopRecorderCollage();
                    if (SquareGridCameraModuleBase.this.mCollageOutputInfo != null) {
                        String outFilePath = SquareGridCameraModuleBase.this.mCollageOutputInfo.getOutputDir() + SquareGridCameraModuleBase.this.mCollageOutputInfo.getOutputFileName() + SquareGridCameraModuleBase.this.mCollageOutputInfo.getOutputFileExt();
                        String videoSize = SquareGridCameraModuleBase.this.mCollageOutputInfo.getVideoWidth() + "x" + SquareGridCameraModuleBase.this.mCollageOutputInfo.getVideoHeight();
                        if (outFilePath != null) {
                            File outFile = new File(outFilePath);
                            if (outFile != null) {
                                SquareGridCameraModuleBase.this.mQuickClipManager.setAfterShot();
                                SquareGridCameraModuleBase.this.mGet.getMediaSaveService().addVideo(SquareGridCameraModuleBase.this.mGet.getAppContext(), SquareGridCameraModuleBase.this.mGet.getAppContext().getContentResolver(), SquareGridCameraModuleBase.this.mCollageOutputInfo.getOutputDir(), SquareGridCameraModuleBase.this.mCollageOutputInfo.getOutputFileName(), videoSize, SquareGridCameraModuleBase.this.getRecDurationTime(outFilePath), outFile.length(), SquareGridCameraModuleBase.this.mLocationServiceManager.getCurrentLocation(), 1, SquareGridCameraModuleBase.this.mOnMediaSavedListener);
                            }
                        }
                    }
                    CamLog.m3d(CameraConstants.TAG, "-th- collageEnd");
                    SquareGridCameraModuleBase.this.access$3700();
                    if (SquareGridCameraModuleBase.this.mRenderThread != null && SquareGridCameraModuleBase.this.mRenderThread.isRequiredToFinishAfterSaving()) {
                        CamLog.m3d(CameraConstants.TAG, "-th- activity is paused stopThread");
                        SquareGridCameraModuleBase.this.mRenderThread.stopThread();
                        SquareGridCameraModuleBase.this.release();
                    }
                    SquareGridCameraModuleBase.this.gridviewMakingCollageEnd();
                    synchronized (SquareGridCameraModuleBase.this.mSynchAudioBuffer) {
                        for (int i = 0; i < SquareGridCameraModuleBase.this.mNumVideoFile; i++) {
                            if (SquareGridCameraModuleBase.this.mAudioBuffer[i] != null) {
                                SquareGridCameraModuleBase.this.mAudioBuffer[i].clear();
                                SquareGridCameraModuleBase.this.mAudioBuffer[i] = null;
                            }
                        }
                    }
                    SquareGridCameraModuleBase.this.mAudioBuffer = null;
                    SquareGridCameraModuleBase.this.mMaxVideoDuration = Long.MIN_VALUE;
                }
                CamLog.m3d(CameraConstants.TAG, "id = " + this.mId + ", Collage - playDone - " + SquareGridCameraModuleBase.this.mPlayDoneCount);
            }
        }

        public void preRender(long presentationTimeUsec) {
            if (SquareGridCameraModuleBase.this.mPlayerTimeStamp != null) {
                long maxSleepTimeUsec = 0;
                SquareGridCameraModuleBase.this.mPlayerTimeStamp[this.mId] = presentationTimeUsec;
                for (int i = 0; i < SquareGridCameraModuleBase.this.mNumTotalFiles; i++) {
                    long deltaUsec = presentationTimeUsec - SquareGridCameraModuleBase.this.mPlayerTimeStamp[i];
                    if (maxSleepTimeUsec < deltaUsec) {
                        maxSleepTimeUsec = deltaUsec;
                    }
                }
                if (maxSleepTimeUsec > MAX_TOLERABLE_DELAY_IN_USEC) {
                    try {
                        Thread.sleep(maxSleepTimeUsec / 1000, ((int) (maxSleepTimeUsec % 1000)) * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private boolean checkAllAudioBufferFilled() {
            int nonEmptyBufferCnt = 0;
            for (int i = 0; i < SquareGridCameraModuleBase.this.mNumTotalFiles; i++) {
                if (SquareGridCameraModuleBase.this.mAudioBuffer[i] != null) {
                    if (SquareGridCameraModuleBase.this.mAudioBuffer[i].isEmpty()) {
                        return false;
                    }
                    nonEmptyBufferCnt++;
                }
            }
            if (nonEmptyBufferCnt == SquareGridCameraModuleBase.this.mNumVideoFile) {
                return true;
            }
            return false;
        }

        /* JADX WARNING: Missing block: B:14:0x00a1, code:
            r8 = new java.nio.ShortBuffer[com.lge.camera.app.ext.SquareGridCameraModuleBase.access$2000(r27.this$0)];
            r19 = 0;
            r9 = 0;
     */
        /* JADX WARNING: Missing block: B:16:0x00be, code:
            if (r9 >= com.lge.camera.app.ext.SquareGridCameraModuleBase.access$2000(r27.this$0)) goto L_0x00e0;
     */
        /* JADX WARNING: Missing block: B:17:0x00c0, code:
            r8[r9] = r4[r9].getShortData();
            r19 = (int) (((long) r19) + r4[r9].getTimeStamp());
            r9 = r9 + 1;
     */
        /* JADX WARNING: Missing block: B:22:0x00e0, code:
            if (r19 == 0) goto L_0x0124;
     */
        /* JADX WARNING: Missing block: B:23:0x00e2, code:
            r19 = r19 / com.lge.camera.app.ext.SquareGridCameraModuleBase.access$2000(r27.this$0);
     */
        /* JADX WARNING: Missing block: B:24:0x00ee, code:
            r16 = r8[0].limit();
            r7 = java.nio.ByteBuffer.allocate(r16 * 2);
            r7.position(0);
            r9 = 0;
     */
        /* JADX WARNING: Missing block: B:26:0x0106, code:
            if (r9 >= r16) goto L_0x0171;
     */
        /* JADX WARNING: Missing block: B:27:0x0108, code:
            r12 = 0;
     */
        /* JADX WARNING: Missing block: B:29:0x0115, code:
            if (r12 >= com.lge.camera.app.ext.SquareGridCameraModuleBase.access$2000(r27.this$0)) goto L_0x0127;
     */
        /* JADX WARNING: Missing block: B:30:0x0117, code:
            r15 = r15 + r8[r12].get(r9);
            r12 = r12 + 1;
     */
        /* JADX WARNING: Missing block: B:31:0x0124, code:
            r19 = 0;
     */
        /* JADX WARNING: Missing block: B:33:0x0137, code:
            if (com.lge.camera.app.ext.SquareGridCameraModuleBase.access$2000(r27.this$0) <= 1) goto L_0x013b;
     */
        /* JADX WARNING: Missing block: B:34:0x0139, code:
            r15 = r15 >> 1;
     */
        /* JADX WARNING: Missing block: B:36:0x013f, code:
            if (r15 <= 32767) goto L_0x0168;
     */
        /* JADX WARNING: Missing block: B:37:0x0141, code:
            r15 = 32767;
     */
        /* JADX WARNING: Missing block: B:38:0x0143, code:
            r17 = (short) r15;
            r13 = (byte) ((r17 >> 8) & 255);
            r7.put((byte) (r17 & 255));
            r7.put(r13);
            r15 = 0;
            r9 = r9 + 1;
     */
        /* JADX WARNING: Missing block: B:40:0x016c, code:
            if (r15 >= -32768) goto L_0x0143;
     */
        /* JADX WARNING: Missing block: B:41:0x016e, code:
            r15 = -32768;
     */
        /* JADX WARNING: Missing block: B:42:0x0171, code:
            r27.this$0.mRenderThread.setAudioData(r7, (long) r19);
     */
        /* JADX WARNING: Missing block: B:57:?, code:
            return;
     */
        public void audioDataAvailable(android.media.MediaCodec r28, int r29, long r30) {
            /*
            r27 = this;
            r6 = r28.getOutputBuffers();
            r21 = r6[r29];
            r5 = r21.asShortBuffer();
            r15 = 0;
            r14 = 0;
            r21 = r5.limit();
            r18 = java.nio.ShortBuffer.allocate(r21);
            r18.clear();
            r0 = r18;
            r0.put(r5);
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;
            r21 = r0;
            r22 = r21.mSynchAudioBuffer;
            monitor-enter(r22);
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;	 Catch:{ all -> 0x00dd }
            r21 = r0;
            r21 = r21.mAudioBuffer;	 Catch:{ all -> 0x00dd }
            r0 = r27;
            r0 = r0.mId;	 Catch:{ all -> 0x00dd }
            r23 = r0;
            r21 = r21[r23];	 Catch:{ all -> 0x00dd }
            r23 = new com.lge.camera.components.AudioData;	 Catch:{ all -> 0x00dd }
            r0 = r23;
            r1 = r30;
            r3 = r18;
            r0.<init>(r1, r3);	 Catch:{ all -> 0x00dd }
            r0 = r21;
            r1 = r23;
            r0.add(r1);	 Catch:{ all -> 0x00dd }
            r21 = r27.checkAllAudioBufferFilled();	 Catch:{ all -> 0x00dd }
            if (r21 != 0) goto L_0x0053;
        L_0x0051:
            monitor-exit(r22);	 Catch:{ all -> 0x00dd }
        L_0x0052:
            return;
        L_0x0053:
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;	 Catch:{ all -> 0x00dd }
            r21 = r0;
            r21 = r21.mNumVideoFile;	 Catch:{ all -> 0x00dd }
            r0 = r21;
            r4 = new com.lge.camera.components.AudioData[r0];	 Catch:{ all -> 0x00dd }
            r10 = 0;
            r9 = 0;
            r11 = r10;
        L_0x0064:
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;	 Catch:{ all -> 0x00dd }
            r21 = r0;
            r21 = r21.mNumTotalFiles;	 Catch:{ all -> 0x00dd }
            r0 = r21;
            if (r9 >= r0) goto L_0x00a0;
        L_0x0072:
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;	 Catch:{ all -> 0x00dd }
            r21 = r0;
            r21 = r21.mFileType;	 Catch:{ all -> 0x00dd }
            r21 = r21[r9];	 Catch:{ all -> 0x00dd }
            r23 = 2;
            r0 = r21;
            r1 = r23;
            if (r0 != r1) goto L_0x018b;
        L_0x0086:
            r10 = r11 + 1;
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;	 Catch:{ all -> 0x00dd }
            r21 = r0;
            r21 = r21.mAudioBuffer;	 Catch:{ all -> 0x00dd }
            r21 = r21[r9];	 Catch:{ all -> 0x00dd }
            r21 = r21.remove();	 Catch:{ all -> 0x00dd }
            r21 = (com.lge.camera.components.AudioData) r21;	 Catch:{ all -> 0x00dd }
            r4[r11] = r21;	 Catch:{ all -> 0x00dd }
        L_0x009c:
            r9 = r9 + 1;
            r11 = r10;
            goto L_0x0064;
        L_0x00a0:
            monitor-exit(r22);	 Catch:{ all -> 0x00dd }
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;
            r21 = r0;
            r21 = r21.mNumVideoFile;
            r0 = r21;
            r8 = new java.nio.ShortBuffer[r0];
            r19 = 0;
            r9 = 0;
        L_0x00b2:
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;
            r21 = r0;
            r21 = r21.mNumVideoFile;
            r0 = r21;
            if (r9 >= r0) goto L_0x00e0;
        L_0x00c0:
            r21 = r4[r9];
            r21 = r21.getShortData();
            r8[r9] = r21;
            r0 = r19;
            r0 = (long) r0;
            r22 = r0;
            r21 = r4[r9];
            r24 = r21.getTimeStamp();
            r22 = r22 + r24;
            r0 = r22;
            r0 = (int) r0;
            r19 = r0;
            r9 = r9 + 1;
            goto L_0x00b2;
        L_0x00dd:
            r21 = move-exception;
            monitor-exit(r22);	 Catch:{ all -> 0x00dd }
            throw r21;
        L_0x00e0:
            if (r19 == 0) goto L_0x0124;
        L_0x00e2:
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;
            r21 = r0;
            r21 = r21.mNumVideoFile;
            r19 = r19 / r21;
        L_0x00ee:
            r21 = 0;
            r21 = r8[r21];
            r16 = r21.limit();
            r21 = r16 * 2;
            r7 = java.nio.ByteBuffer.allocate(r21);
            r21 = 0;
            r0 = r21;
            r7.position(r0);
            r9 = 0;
        L_0x0104:
            r0 = r16;
            if (r9 >= r0) goto L_0x0171;
        L_0x0108:
            r12 = 0;
        L_0x0109:
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;
            r21 = r0;
            r21 = r21.mNumVideoFile;
            r0 = r21;
            if (r12 >= r0) goto L_0x0127;
        L_0x0117:
            r21 = r8[r12];
            r0 = r21;
            r21 = r0.get(r9);
            r15 = r15 + r21;
            r12 = r12 + 1;
            goto L_0x0109;
        L_0x0124:
            r19 = 0;
            goto L_0x00ee;
        L_0x0127:
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;
            r21 = r0;
            r21 = r21.mNumVideoFile;
            r22 = 1;
            r0 = r21;
            r1 = r22;
            if (r0 <= r1) goto L_0x013b;
        L_0x0139:
            r15 = r15 >> 1;
        L_0x013b:
            r21 = 32767; // 0x7fff float:4.5916E-41 double:1.6189E-319;
            r0 = r21;
            if (r15 <= r0) goto L_0x0168;
        L_0x0141:
            r15 = 32767; // 0x7fff float:4.5916E-41 double:1.6189E-319;
        L_0x0143:
            r0 = (short) r15;
            r17 = r0;
            r0 = r17;
            r0 = r0 & 255;
            r21 = r0;
            r0 = r21;
            r0 = (byte) r0;
            r20 = r0;
            r21 = r17 >> 8;
            r0 = r21;
            r0 = r0 & 255;
            r21 = r0;
            r0 = r21;
            r13 = (byte) r0;
            r0 = r20;
            r7.put(r0);
            r7.put(r13);
            r15 = 0;
            r9 = r9 + 1;
            goto L_0x0104;
        L_0x0168:
            r21 = -32768; // 0xffffffffffff8000 float:NaN double:NaN;
            r0 = r21;
            if (r15 >= r0) goto L_0x0143;
        L_0x016e:
            r15 = -32768; // 0xffffffffffff8000 float:NaN double:NaN;
            goto L_0x0143;
        L_0x0171:
            r0 = r27;
            r0 = com.lge.camera.app.ext.SquareGridCameraModuleBase.this;
            r21 = r0;
            r0 = r21;
            r0 = r0.mRenderThread;
            r21 = r0;
            r0 = r19;
            r0 = (long) r0;
            r22 = r0;
            r0 = r21;
            r1 = r22;
            r0.setAudioData(r7, r1);
            goto L_0x0052;
        L_0x018b:
            r10 = r11;
            goto L_0x009c;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.ext.SquareGridCameraModuleBase.VideoPlayerCallback.audioDataAvailable(android.media.MediaCodec, int, long):void");
        }
    }

    public SquareGridCameraModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void saveImage(byte[] data, byte[] extraExif, boolean useDB, boolean needCrop) {
        if (this.mCameraDevice == null && !this.mGet.isPaused()) {
            CamLog.m11w(CameraConstants.TAG, "gridview - mCameraDevice is null, so return");
        } else if (this.mGet.getMediaSaveService() == null || !this.mGet.getMediaSaveService().isQueueFull()) {
            String dir = getCurDir();
            final byte[] bArr = data;
            final byte[] bArr2 = extraExif;
            final boolean z = useDB;
            final boolean z2 = needCrop;
            this.mGet.getMediaSaveService().processLocal(new OnLocalSaveListener() {
                public void onPreExecute() {
                }

                public void onPostExecute(Uri uri) {
                    if (uri != null) {
                        SquareGridCameraModuleBase.this.mGet.requestNotifyNewMediaonActivity(uri, SquareGridCameraModuleBase.this.checkModuleValidate(128));
                        SquareGridCameraModuleBase.this.mGet.onNewItemAdded(uri, 6, null);
                    }
                }

                public Uri onLocalSave(String dir, String fileName) {
                    return SquareGridCameraModuleBase.this.saveImageInBackground(bArr, bArr2, z, z2);
                }
            }, dir, getFileName(dir));
        }
    }

    protected int getSaveImageDegree() {
        int degree = CameraDeviceUtils.getJpegOrientation(this.mGet.getActivity(), Utils.restoreDegree(this.mGet.getAppContext().getResources(), getOrientationDegree()), this.mCameraId);
        if (isRearCamera()) {
            return degree;
        }
        if (degree == 90 || degree == 270) {
            return (degree + 180) % 360;
        }
        return degree;
    }

    public void onSaveContents(GridContentsInfo[] contentsInfo, boolean isSaveBtnClicked) {
        CamLog.m3d(CameraConstants.TAG, "collage save contents in module");
        this.mIsCollageStarted = true;
        this.mContentsInfo = contentsInfo;
        startCollageThread();
        this.mQuickClipManager.setAfterShot();
        this.mIsSaveBtnClicked = isSaveBtnClicked;
    }

    public void onSaveImageContents(boolean isShare) {
        this.mQuickClipManager.setAfterShot();
    }

    protected void setUIOnCollageSaving() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                SquareGridCameraModuleBase.this.showDialog(132);
                SquareGridCameraModuleBase.this.mGet.enableConeMenuIcon(31, false);
            }
        });
    }

    public void onSaveContents(Bitmap bm, boolean isSaveBtnClicked) {
        this.mIsSavingImage = true;
        this.mIsSaveBtnClicked = isSaveBtnClicked;
        Bitmap previewBitmap = BitmapManagingUtil.getRotatedImage(bm, Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()) ? 0 : 270, false);
        if (isSignatureEnableCondition()) {
            previewBitmap = this.mGet.composeSignatureImage(previewBitmap, this.mCameraDegree);
        }
        if (previewBitmap != null) {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            previewBitmap.compress(CompressFormat.JPEG, 100, byteArray);
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    SquareGridCameraModuleBase.this.showDialog(132);
                }
            });
            saveImage(byteArray.toByteArray(), null, true, false);
            try {
                byteArray.close();
            } catch (Exception e) {
                CamLog.m3d(CameraConstants.TAG, "byteArray close failed");
            }
            previewBitmap.recycle();
            bm.recycle();
        }
        this.mGridPostviewManager.resetImageViews();
        this.mGridPostviewManager.setToFirstIndexHighlightView();
    }

    protected void launchQuickClipSharedApp(Uri uri) {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                SquareGridCameraModuleBase.this.mQuickClipManager.doClickQuickClip();
            }
        });
    }

    public void setButtonsVisibilityForPostView(boolean visible) {
        if (checkModuleValidate(8)) {
            this.mVisibleForPostView = visible;
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    int visibility;
                    boolean z;
                    boolean z2;
                    if (SquareGridCameraModuleBase.this.mVisibleForPostView) {
                        visibility = 0;
                    } else {
                        visibility = 8;
                    }
                    if (!SquareGridCameraModuleBase.this.mVisibleForPostView && SquareGridCameraModuleBase.this.isMenuShowing(4)) {
                        SquareGridCameraModuleBase.this.access$400(4, false, true);
                    }
                    SquareGridCameraModuleBase.this.mQuickButtonManager.setVisibility(100, visibility, false);
                    GestureViewManager access$600 = SquareGridCameraModuleBase.this.mReviewThumbnailManager;
                    if (SquareGridCameraModuleBase.this.getUri() == null) {
                        z = false;
                    } else {
                        z = true;
                    }
                    access$600.setEnabled(z);
                    if (visibility == 8 || !SquareGridCameraModuleBase.this.mGridPostviewManager.getQuickClipDrawerOpend()) {
                        SquareGridCameraModuleBase.this.mCaptureButtonManager.setShutterButtonVisibility(visibility, SquareGridCameraModuleBase.this.getShutterButtonType(), false);
                        SquareGridCameraModuleBase.this.mReviewThumbnailManager.setThumbnailVisibility(visibility, false, true);
                    }
                    if (SquareGridCameraModuleBase.this.mVisibleForPostView) {
                        SquareGridCameraModuleBase.this.mBackButtonManager.show();
                    } else {
                        SquareGridCameraModuleBase.this.mBackButtonManager.hide();
                    }
                    SquareGridCameraModuleBase.this.setFilmStrengthButtonVisibility(SquareGridCameraModuleBase.this.mVisibleForPostView, false);
                    GridCameraPostviewManagerExpand gridCameraPostviewManagerExpand = SquareGridCameraModuleBase.this.mGridPostviewManager;
                    if (SquareGridCameraModuleBase.this.mVisibleForPostView) {
                        z2 = false;
                    } else {
                        z2 = true;
                    }
                    gridCameraPostviewManagerExpand.setPostviewButtonsVisibility(z2);
                    gridCameraPostviewManagerExpand = SquareGridCameraModuleBase.this.mGridPostviewManager;
                    if (SquareGridCameraModuleBase.this.mVisibleForPostView) {
                        z2 = false;
                    } else {
                        z2 = true;
                    }
                    gridCameraPostviewManagerExpand.setPostviewPreviewDummyVisibility(z2);
                    if ((SquareGridCameraModuleBase.this.mIsRetakeMode && SquareGridCameraModuleBase.this.mCapturedCount == 4 && SquareGridCameraModuleBase.this.mIsRecordButtonClicked) || SquareGridCameraModuleBase.this.isTimerShotCountdown()) {
                        SquareGridCameraModuleBase.this.mGridPostviewManager.setPostviewPreviewDummyVisibility(false);
                    }
                    SquareGridCameraModuleBase.this.mDoubleCameraManager.showDualViewControl(SquareGridCameraModuleBase.this.mVisibleForPostView);
                    SquareGridCameraModuleBase.this.setDoubleCameraEnable(true);
                    if (SquareGridCameraModuleBase.this.mCameraId == 1) {
                        if (SquareGridCameraModuleBase.this.mExtraPrevewUIManager != null) {
                            SquareGridCameraModuleBase.this.mExtraPrevewUIManager.changeButtonState(0, SquareGridCameraModuleBase.this.isMenuShowing(4));
                        }
                        SquareGridCameraModuleBase.this.showExtraPreviewUI(SquareGridCameraModuleBase.this.mVisibleForPostView, false, SquareGridCameraModuleBase.this.isMenuShowing(4), true);
                        SquareGridCameraModuleBase.this.mAdvancedFilmManager.setSelfieMenuVisibility(SquareGridCameraModuleBase.this.mVisibleForPostView, true);
                        SquareGridCameraModuleBase.this.mAdvancedFilmManager.setSelfieOptionVisibility(false, false, true);
                    }
                    SquareGridCameraModuleBase.this.setBatteryIndicatorVisibility(SquareGridCameraModuleBase.this.mVisibleForPostView);
                    SquareGridCameraModuleBase.this.setTimerIndicatorVisibility(SquareGridCameraModuleBase.this.mVisibleForPostView);
                    SquareGridCameraModuleBase.this.setDeviceStatus(SquareGridCameraModuleBase.this.mVisibleForPostView);
                }
            });
            return;
        }
        CamLog.m11w(CameraConstants.TAG, "setButtonsVisibilityForPostView : swap camera");
    }

    protected void setDeviceStatus(boolean enable) {
        boolean z = false;
        if (enable) {
            String hdrValue = getSettingValue("hdr-mode");
            if ("2".equals(hdrValue)) {
                setHDRMetaDataCallback(hdrValue);
            }
            if (isFocusLock() || isAELock()) {
                this.mFocusManager.showAEAFText();
                this.mFocusManager.showFocus();
                this.mFocusManager.showAEControlBar(false);
            }
            setFlashMetaDataCallback(getSettingValue("flash-mode"));
        } else {
            setHDRMetaDataCallback(null);
            setFlashMetaDataCallback(null);
            if (!(isTimerShotCountdown() && (isFocusLock() || isAELock()))) {
                this.mFocusManager.hideAEAFText();
                this.mFocusManager.hideFocus();
            }
            if (this.mIndicatorManager != null) {
                this.mIndicatorManager.hideAllIndicator();
            }
            hideZoomBar();
        }
        if (!enable) {
            z = true;
        }
        setFocusInVisible(z);
    }

    public void onCancel() {
        if (this.mGridPostviewManager != null) {
            this.mGridPostviewManager.resetImageViews();
            this.mCapturedCount = 0;
            this.mRetakeViewIndex = -1;
            this.mIsRetakeMode = false;
            this.mGridPostviewManager.setQuickClipDrawerOpened(false);
            setButtonsVisibilityForPostView(true);
            setQuickClipIcon(true, true);
            this.mIsCollageShared = false;
            this.mGridPostviewManager.setToFirstIndexHighlightView();
            this.mGridPostviewManager.resetSaveButtonClickedFlag();
            this.mIsSaveBtnClicked = false;
            setDeviceStatus(true);
            startSelfieEngine();
            this.mWaitSavingDialogType = 0;
            this.mNeedProgressDuringCapture = 0;
        }
        if (this.mQuickClipManager != null) {
            this.mQuickClipManager.setSharedUri(getUri());
        }
        AudioUtil.setAudioFocus(this.mGet.getActivity(), false);
    }

    public void onShutterTopButtonFocus(boolean pressed) {
        this.mIsShutterButtonpressed = pressed;
    }

    public void setRetakeMode(int viewIndex, boolean nextRetakeState) {
        if (!nextRetakeState && this.mIsShutterButtonpressed) {
            CamLog.m11w(CameraConstants.TAG, "thisis bug root. so prevent");
        } else if (nextRetakeState) {
            this.mIsRetakeMode = true;
            this.mRetakeViewIndex = viewIndex;
            if (this.mCapturedCount == 4) {
                setButtonsVisibilityForPostView(true);
                startSelfieEngine();
                for (int i = 0; i < 4; i++) {
                    this.mGridPostviewManager.releaseMediaPlayer(i);
                    this.mGridPostviewManager.removeTextureView(i);
                }
            }
        } else {
            doBackKey();
        }
    }

    public boolean isRetakeMode() {
        return this.mIsRetakeMode;
    }

    protected Uri saveImageInBackground(byte[] data, byte[] extraExif, boolean useDB, boolean needCrop) {
        Uri uri = null;
        String dir = getCurDir();
        long curTime = System.currentTimeMillis();
        int storage = getCurStorage();
        String fileName = makeFileName(0, storage, dir, false, getSettingValue(Setting.KEY_MODE)) + ".jpg";
        FileNamer.get().addFileNameInSaving(dir + fileName);
        SupportedExif supportedExif = new SupportedExif(this.mCameraCapabilities.isFocusAreaSupported(), this.mCameraCapabilities.isFlashSupported(), this.mCameraCapabilities.isWBSupported(), this.mCameraCapabilities.isMeteringAreaSupported(), this.mCameraCapabilities.isZoomSupported(), true, true, true);
        byte[] convertJpeg = data;
        if (this.mCameraDevice != null || this.mGet.isPaused()) {
            CameraParameters parameters;
            if (this.mCameraDevice != null) {
                parameters = this.mCameraDevice.getParameters();
                if (this.mCameraCapabilities.isFlashSupported()) {
                    parameters.setFlashMode("off");
                }
            } else {
                parameters = null;
            }
            String picSize = GRID_PICTURE_SIZE;
            int cameraDegree = this.mCameraDegree;
            int[] pictureSize = Utils.sizeStringToArray(picSize);
            ExifInterface exif = Exif.createExif(convertJpeg, pictureSize[0], pictureSize[1], parameters, this.mLocationServiceManager.getCurrentLocation(), cameraDegree, -1, supportedExif, (short) 15);
            updateThumbnail(exif, cameraDegree, false);
            if (!saveThumb(data, extraExif, convertJpeg, dir, fileName, exif, pictureSize)) {
                return null;
            }
            if (needCrop) {
                BitmapManagingUtil.saveCroppedImage(dir, fileName, exif);
            }
            if (useDB) {
                String str = dir;
                String str2 = fileName;
                uri = FileManager.registerImageUri(this.mGet.getAppContext().getContentResolver(), str, str2, curTime, this.mLocationServiceManager.getCurrentLocation(), cameraDegree, Exif.getExifSize(exif), false);
            }
            FileNamer.get().removeFileNameInSaving(dir + fileName);
            CamLog.m3d(CameraConstants.TAG, "Jpeg uri = " + uri);
            checkStorage();
            return uri;
        }
        CamLog.m11w(CameraConstants.TAG, "mCameraDevice is null, so return");
        return null;
    }

    public boolean doBackKey() {
        CamLog.m3d(CameraConstants.TAG, "doBackKey");
        if (isMenuShowing(4)) {
            CamLog.m3d(CameraConstants.TAG, "rear film menu");
            return super.doBackKey();
        } else if (this.mSnapShotChecker.checkMultiShotState(2)) {
            super.doBackKey();
            CamLog.m3d(CameraConstants.TAG, "do back key interval stop and start selfie");
            if (this.mIsRetakeMode) {
                this.mIsRetakeMode = false;
                this.mGridPostviewManager.setHighlightView(this.mCapturedCount, false);
            }
            this.mGestureShutterManager.resetGestureCaptureType();
            return true;
        } else if (processFullContentsBackKey() || processNormalStateBackKey()) {
            return true;
        } else {
            if (!this.mGridPostviewManager.checkReocrdingState()) {
                return super.doBackKey();
            }
            if (!isTimerShotCountdown() || this.mCapturedCount != 0) {
                return true;
            }
            super.doBackKey();
            this.mGestureShutterManager.resetGestureCaptureType();
            return true;
        }
    }

    protected boolean processFullContentsBackKey() {
        if (this.mCapturedCount != 4) {
            return false;
        }
        if (this.mGridPostviewManager.checkReocrdingState() && !isTimerShotCountdown()) {
            return true;
        }
        if (this.mIsRetakeMode) {
            setButtonsVisibilityForPostView(false);
            this.mIsRetakeMode = false;
            this.mGridPostviewManager.resetSaveButtonClickedFlag();
            this.mGridPostviewManager.hideAllHighrightViews();
            this.mGridPostviewManager.setVideoContents();
            CamLog.m3d(CameraConstants.TAG, "max count, retake");
            stopSelfieEngin();
            this.mGridPostviewManager.setPostviewPreviewDummyVisibility(true);
            if (!isTimerShotCountdown()) {
                return true;
            }
            super.doBackKey();
            CamLog.m3d(CameraConstants.TAG, "max count, retake, timer");
            stopSelfieEngin();
            this.mGridPostviewManager.setPostviewPreviewDummyVisibility(true);
            return true;
        }
        onCancel();
        return true;
    }

    protected boolean processNormalStateBackKey() {
        if (this.mCapturedCount >= 4 || this.mCapturedCount <= 0) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "capture progress");
        if (isTimerShotCountdown()) {
            if (this.mIsRetakeMode) {
                this.mGridPostviewManager.setHighlightView(this.mCapturedCount, false);
                this.mIsRetakeMode = false;
            }
            super.doBackKey();
            return true;
        } else if (this.mIsRetakeMode) {
            CamLog.m3d(CameraConstants.TAG, "capture progress - retake");
            this.mGridPostviewManager.setHighlightView(this.mCapturedCount, false);
            this.mIsRetakeMode = false;
            return true;
        } else {
            CamLog.m3d(CameraConstants.TAG, "capture progress - normal");
            onCancel();
            return true;
        }
    }

    protected boolean processBackKeyOnRecording() {
        if (this.mCameraState != 5 && this.mCameraState != 6) {
            return false;
        }
        if (!this.mRecordingUIManager.checkMinRecTime(1000)) {
            return true;
        }
        onVideoStopClicked(true, false);
        this.mIsRecordingCanceled = true;
        this.mGet.enableConeMenuIcon(31, true);
        if (this.mIsRetakeMode) {
            if (this.mCapturedCount == 4) {
                this.mGridPostviewManager.hideAllHighrightViews();
                setButtonsVisibilityForPostView(false);
                this.mGridPostviewManager.setVideoContents();
            } else {
                this.mGridPostviewManager.setHighlightView(this.mCapturedCount, false);
            }
            this.mIsRetakeMode = false;
            this.mRetakeViewIndex = -1;
        }
        return true;
    }

    public int getCurrentShotCount() {
        CamLog.m3d(CameraConstants.TAG, "capture count" + this.mCapturedCount);
        return this.mCapturedCount;
    }

    public String makeFileName(int useType, int storage, String dir, boolean useThread, String shotMode) {
        String filePath;
        if (this.mCapturedCount < 4 && useType == 1 && !this.mIsRetakeMode) {
            filePath = GRIDVIEW_FILE_NAME + this.mCapturedCount;
            this.mTempFilePath = dir + filePath + ".mp4";
            CamLog.m3d(CameraConstants.TAG, "makeFileName add video file : " + this.mTempFilePath);
            this.mCurDegree = getOrientationDegree();
            return filePath;
        } else if (!this.mIsRetakeMode || useType != 1) {
            return FileNamer.get().getFileNewName(getAppContext(), useType, storage, dir, false, shotMode);
        } else {
            filePath = GRIDVIEW_FILE_NAME_RETAKE + this.mRetakeViewIndex;
            this.mTempFilePathRetake = dir + filePath + ".mp4";
            CamLog.m3d(CameraConstants.TAG, "re-take makeFileName add video file : " + this.mTempFilePathRetake);
            this.mCurDegree = getOrientationDegree();
            return filePath;
        }
    }

    public void startRenderer(TextureView textureView, SurfaceTexture surfaceTexture) {
        if (this.mRenderThread != null) {
            try {
                CamLog.m3d(CameraConstants.TAG, "-th- waiting for finishing previous render thread");
                this.mIsRendererStarting = true;
                this.mRenderThread.join();
                CamLog.m3d(CameraConstants.TAG, "-th- finishing previous render thread is done");
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        this.mIsRendererStarting = false;
        this.mRenderThread = new GridViewRenderer(surfaceTexture, getShotMode());
        CamLog.m3d(CameraConstants.TAG, "-th- new renderer thread is created");
        this.mRenderThread.init(textureView, this.mSTAvailableListenerCollage);
        this.mRenderThread.setName("GridViewRenderThread");
        this.mRenderThread.start();
    }

    private void startCollageThread() {
        new Thread(new C05057()).start();
    }

    private MultiViewRecordInputInfo makeCollageInputInfo() {
        if (this.mContentsInfo == null) {
            CamLog.m5e(CameraConstants.TAG, "collage file path is NULL");
            return null;
        }
        int totalCount = this.mContentsInfo.length;
        int[] fileType = new int[totalCount];
        int imageCount = 0;
        ArrayList<String> collageFilePath = new ArrayList();
        ArrayList<Integer> degree = new ArrayList();
        for (int i = 0; i < totalCount; i++) {
            if (this.mContentsInfo[i].contentsType == 1) {
                fileType[i] = 1;
                imageCount++;
                collageFilePath.add("");
                degree.add(Integer.valueOf(0));
            } else {
                fileType[i] = 2;
                collageFilePath.add(this.mContentsInfo[i].filePath);
                degree.add(Integer.valueOf(this.mContentsInfo[i].degree));
            }
        }
        return new MultiViewRecordInputInfo(collageFilePath, degree, fileType, totalCount, imageCount);
    }

    protected void startMakingCollageVideo(final MultiViewRecordInputInfo inputInfo) {
        new Thread(new Runnable() {
            public void run() {
                int i;
                CamLog.m3d(CameraConstants.TAG, "startMakingCollageVideo - started");
                int[] video_size = Utils.sizeStringToArray(SquareGridCameraModuleBase.GRID_PICTURE_SIZE);
                String outputDir = SquareGridCameraModuleBase.this.getCurDir();
                SquareGridCameraModuleBase.this.mRecordedFileName = SquareGridCameraModuleBase.this.makeFileName(1, SquareGridCameraModuleBase.this.getCurStorage(), outputDir, false, SquareGridCameraModuleBase.this.getSettingValue(Setting.KEY_MODE));
                String outputFileExt = ".mp4";
                CamLog.m3d(CameraConstants.TAG, "outputFileName = " + SquareGridCameraModuleBase.this.mRecordedFileName);
                SquareGridCameraModuleBase.this.mCollageOutputInfo = new MVRecordOutputInfo("video/avc", video_size[0], video_size[1], 17000000, 30, 1, SquareGridCameraModuleBase.this.mCameraDegree, "audio/mp4a-latm", 48000, 2, CameraConstants.VALUE_VIDEO_BITRATE_MMS, outputDir, SquareGridCameraModuleBase.this.mRecordedFileName, ".mp4");
                Bitmap[] bm = new Bitmap[4];
                for (i = 0; i < 4; i++) {
                    bm[i] = SquareGridCameraModuleBase.this.mContentsInfo[i].f34bm;
                }
                int[] cameraId = new int[4];
                i = 0;
                while (i < 4) {
                    cameraId[i] = SquareGridCameraModuleBase.this.mContentsInfo[i].isRearCam ? 0 : 1;
                    if (SquareGridCameraModuleBase.this.mContentsInfo[i].isFilmVideo && !SquareGridCameraModuleBase.this.mContentsInfo[i].isRearCam) {
                        cameraId[i] = SquareGridCameraModuleBase.this.mGridPostviewManager.getFilmVideoLocalCamId(SquareGridCameraModuleBase.this.mContentsInfo[i].degree);
                    }
                    i++;
                }
                SquareGridCameraModuleBase.this.mRenderThread.setCameraId(cameraId);
                SquareGridCameraModuleBase.this.mRenderThread.setCollageImageBitmap(bm);
                SquareGridCameraModuleBase.this.mCollageSTLatch = new CountDownLatch(1);
                SquareGridCameraModuleBase.this.mRenderThread.startRecorderCollage(inputInfo, SquareGridCameraModuleBase.this.mCollageOutputInfo);
                try {
                    CamLog.m3d(CameraConstants.TAG, "Collage - wait for surface texture ready callback");
                    SquareGridCameraModuleBase.this.mCollageSTLatch.await();
                    CamLog.m3d(CameraConstants.TAG, "Collage - continue after surface texture ready callback");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                VideoPlayerCallback[] mVideoPlayerCallback = new VideoPlayerCallback[SquareGridCameraModuleBase.this.mNumTotalFiles];
                SquareGridCameraModuleBase.this.mPlayStartedCount = 0;
                SquareGridCameraModuleBase.this.mPlayDoneCount = 0;
                for (i = 0; i < SquareGridCameraModuleBase.this.mNumTotalFiles; i++) {
                    String filePath = (String) inputInfo.getFilePath().get(i);
                    mVideoPlayerCallback[i] = new VideoPlayerCallback(i);
                    if (SquareGridCameraModuleBase.this.mSurfaceTextureCollage[i] != null) {
                        new GridVideoPlayThread(new File(filePath), new Surface(SquareGridCameraModuleBase.this.mSurfaceTextureCollage[i]), mVideoPlayerCallback[i]).setName("VideoPlayThread(" + i + ")");
                    }
                }
                DebugUtil.setEndTime("[1] MV video: all video players are started");
                DebugUtil.setStartTime("[2] MV video: all video players are stopped");
            }
        }, "CollageThread").start();
    }

    private void collageEnd() {
        CamLog.m3d(CameraConstants.TAG, "-th- collageEnd");
        access$3700();
        if (this.mRenderThread != null && this.mRenderThread.isRequiredToFinishAfterSaving()) {
            CamLog.m3d(CameraConstants.TAG, "-th- activity is paused stopThread");
            this.mRenderThread.stopThread();
            release();
        }
        gridviewMakingCollageEnd();
    }

    public void release() {
        CamLog.m3d(CameraConstants.TAG, "gridview - release");
        if (this.mRenderThread != null) {
            this.mRenderThread.stopThread();
        }
        this.mRenderThread = null;
    }

    private boolean saveThumb(byte[] data, byte[] extraExif, byte[] convertJpeg, String dir, String fileName, ExifInterface exif, int[] pictureSize) {
        int thumbRewriteCount = 0;
        boolean isThumbWrote = false;
        int quality = 70;
        while (!isThumbWrote) {
            thumbRewriteCount++;
            if (thumbRewriteCount > 3) {
                try {
                    this.mToastManager.showShortToast(this.mGet.getAppContext().getResources().getString(C0088R.string.saving_failure));
                    return false;
                } catch (IOException e) {
                    quality -= 20;
                    if (quality < 0) {
                        quality = 0;
                    }
                    switch (thumbRewriteCount) {
                        case 1:
                            Exif.updateThumbnail(exif, convertJpeg, pictureSize[0], pictureSize[1], quality);
                            break;
                        case 2:
                            exif.removeCompressedThumbnail();
                            break;
                        default:
                            return false;
                    }
                }
            }
            isThumbWrote = FileManager.writeJpegImageToFile(data, extraExif, dir, fileName, exif);
        }
        return true;
    }

    protected void gridviewMakingCollageEnd() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                CamLog.m3d(CameraConstants.TAG, "-cp- gridviewMakingCollageEnd");
                SquareGridCameraModuleBase.this.mGridPostviewManager.resetImageViews();
                SquareGridCameraModuleBase.this.setButtonsVisibilityForPostView(true);
                SquareGridCameraModuleBase.this.mGet.enableConeMenuIcon(31, true);
                SquareGridCameraModuleBase.this.access$4000(500);
                SquareGridCameraModuleBase.this.mGridPostviewManager.setToFirstIndexHighlightView();
            }
        });
    }

    public int getRetakeCurrentIndex() {
        return this.mRetakeViewIndex;
    }

    public boolean isCountDown() {
        return isTimerShotCountdown();
    }

    public boolean isSavingOnPause() {
        if (this.mRenderThread == null) {
            return false;
        }
        return this.mRenderThread.isRequiredToFinishAfterSaving();
    }

    protected boolean isPauseWaitDuringShot() {
        return false;
    }
}
