package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.view.Surface;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.ext.VideoPlayer.PlayerCallback;
import com.lge.camera.components.MVRecordOutputInfo;
import com.lge.camera.components.MultiViewLayout;
import com.lge.camera.components.MultiViewRecordInputInfo;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraSecondHolder;
import com.lge.camera.managers.GestureViewManager;
import com.lge.camera.managers.TimerManager.TimerTypeMultiview;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.QuickClipUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.TelephonyUtil;
import com.lge.camera.util.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class MultiViewCameraModule extends MultiViewCameraModuleBase {
    private final int MULTIVIEW_AUDIO_BIT_RATE = CameraConstants.VALUE_VIDEO_BITRATE_MMS;
    private final int MULTIVIEW_AUDIO_CHANNEL_COUNT = 2;
    private final String MULTIVIEW_AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private final int MULTIVIEW_AUDIO_SAMPLE_RATE = 48000;
    private final int MULTIVIEW_VIDEO_BIT_RATE = 17000000;
    private final int MULTIVIEW_VIDEO_FRAME_RATE = 30;
    private final int MULTIVIEW_VIDEO_IFRAME_INTERVAL = 1;
    private final String MULTIVIEW_VIDEO_MIME_TYPE = "video/avc";
    private MVRecordOutputInfo mCollageOutputInfo = null;
    private int[] mFileType;
    protected MultiViewRecordInputInfo mInputInfo = null;
    private long mMaxVideoDuration = Long.MIN_VALUE;
    private int mMaxVideoId = 0;
    private int mNumImageFiles;
    private int mNumTotalFiles;
    private int mNumVideoFile;
    private int mPlayDoneCount = 0;
    private int mPlayStartedCount = 0;
    protected Bitmap[] mPostViewBitmap = new Bitmap[2];
    private String mRecordedFileName = null;
    private Object mSyncFreezePreview = null;

    /* renamed from: com.lge.camera.app.ext.MultiViewCameraModule$3 */
    class C04013 implements Runnable {
        C04013() {
        }

        public void run() {
            MultiViewCameraModule.this.mFrameShotProgress = false;
            if (!CameraConstants.MODE_SQUARE_SPLICE.equals(MultiViewCameraModule.this.getShotMode())) {
                MultiViewCameraModule.this.setUIOnCollageSaving();
                MultiViewCameraModule.this.mQuickClipManager.setAfterShot();
            }
            MultiViewCameraModule.this.mSyncFreezePreview = new Object();
            synchronized (MultiViewCameraModule.this.mSyncFreezePreview) {
                try {
                    CamLog.m3d(CameraConstants.TAG, "Multiview - waitFor freezePreview");
                    MultiViewCameraModule.this.mSyncFreezePreview.wait(CameraConstants.TOAST_LENGTH_SHORT);
                    CamLog.m3d(CameraConstants.TAG, "Multiview - continue freezePreview");
                    DebugUtil.setEndTime("[+] MV: Final FreezePreview is Done");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            MultiViewCameraModule.this.mSyncFreezePreview = null;
            MultiViewRecordInputInfo inputInfo = MultiViewCameraModule.this.makeCollageInputInfo(MultiViewCameraModule.this.mFrameShotFilePath, MultiViewCameraModule.this.mVideoDegree);
            if (inputInfo != null) {
                MultiViewCameraModule.this.mNumTotalFiles = inputInfo.getTotalCount();
                MultiViewCameraModule.this.mNumImageFiles = inputInfo.getImageCount();
                MultiViewCameraModule.this.mNumVideoFile = MultiViewCameraModule.this.mNumTotalFiles - MultiViewCameraModule.this.mNumImageFiles;
                MultiViewCameraModule.this.mFileType = inputInfo.getFileType();
                MultiViewCameraModule.this.mPlayerTimeStamp = new long[MultiViewCameraModule.this.mNumTotalFiles];
                for (int j = 0; j < MultiViewCameraModule.this.mNumTotalFiles; j++) {
                    if (MultiViewCameraModule.this.mFileType[j] == 1) {
                        MultiViewCameraModule.this.mPlayerTimeStamp[j] = Long.MAX_VALUE;
                    } else {
                        MultiViewCameraModule.this.mPlayerTimeStamp[j] = 0;
                    }
                }
                if (MultiViewCameraModule.this.mNumTotalFiles != MultiViewCameraModule.this.mNumImageFiles) {
                    if (CameraConstants.MODE_SQUARE_SPLICE.equals(MultiViewCameraModule.this.getShotMode())) {
                        MultiViewCameraModule.this.mInputInfo = inputInfo;
                        MultiViewCameraModule.this.setPostViewContents();
                        return;
                    }
                    MultiViewCameraModule.this.setMVState(4);
                    MultiViewCameraModule.this.startMakingCollageVideo(inputInfo);
                } else if (CameraConstants.MODE_SQUARE_SPLICE.equals(MultiViewCameraModule.this.getShotMode())) {
                    MultiViewCameraModule.this.mSplicePostViewManager.setPostviewType(0);
                    MultiViewCameraModule.this.setPostViewImage();
                } else {
                    MultiViewCameraModule.this.setMVState(8);
                    MultiViewCameraModule.this.mRenderThread.setCollageImageOnState(true);
                    MultiViewCameraModule.this.mRenderThread.takeGLView();
                }
            }
        }
    }

    class VideoPlayerCallback implements PlayerCallback {
        private static final long MAX_TOLERABLE_DELAY_IN_USEC = 50000;
        private final int mId;

        public VideoPlayerCallback(int id) {
            this.mId = id;
        }

        public void playStarted(long playDuration) {
            synchronized (MultiViewCameraModule.this.mSynchVideoPlayer) {
                MultiViewCameraModule.this.mPlayStartedCount = MultiViewCameraModule.this.mPlayStartedCount + 1;
                CamLog.m3d(CameraConstants.TAG, "duration [" + this.mId + "] = " + playDuration);
                if (playDuration > MultiViewCameraModule.this.mMaxVideoDuration) {
                    MultiViewCameraModule.this.mMaxVideoId = this.mId;
                    MultiViewCameraModule.this.mMaxVideoDuration = playDuration;
                }
                if (MultiViewCameraModule.this.mPlayStartedCount >= MultiViewCameraModule.this.mNumVideoFile) {
                    MultiViewCameraModule.this.mRenderThread.setSyncVideoId(MultiViewCameraModule.this.mMaxVideoId);
                }
                if (MultiViewCameraModule.this.mAudioBuffer == null) {
                    MultiViewCameraModule.this.mAudioBuffer = new LinkedList[MultiViewCameraModule.this.mNumTotalFiles];
                    for (int i = 0; i < MultiViewCameraModule.this.mNumTotalFiles; i++) {
                        MultiViewCameraModule.this.mAudioBuffer[i] = null;
                    }
                }
                MultiViewCameraModule.this.mAudioBuffer[this.mId] = new LinkedList();
            }
        }

        public void preRender(long presentationTimeUsec) {
            if (MultiViewCameraModule.this.mPlayerTimeStamp != null) {
                MultiViewCameraModule.this.mPlayerTimeStamp[this.mId] = presentationTimeUsec;
                long maxSleepTimeUsec = 0;
                for (int i = 0; i < MultiViewCameraModule.this.mNumTotalFiles; i++) {
                    long deltaUsec = presentationTimeUsec - MultiViewCameraModule.this.mPlayerTimeStamp[i];
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

        public void postRender() {
            CamLog.m3d(CameraConstants.TAG, "id = " + this.mId + ", Collage - postRender");
        }

        public void loopReset() {
            CamLog.m3d(CameraConstants.TAG, "id = " + this.mId + ", Collage - loopReset");
        }

        public void playDone() {
            synchronized (MultiViewCameraModule.this.mSynchVideoPlayer) {
                MultiViewCameraModule.this.mPlayDoneCount = MultiViewCameraModule.this.mPlayDoneCount + 1;
                CamLog.m3d(CameraConstants.TAG, "id = " + this.mId + ", Collage - playDone - " + MultiViewCameraModule.this.mPlayDoneCount);
                if (MultiViewCameraModule.this.mPlayDoneCount >= MultiViewCameraModule.this.mNumTotalFiles - MultiViewCameraModule.this.mNumImageFiles) {
                    MultiViewCameraModule.this.mRenderThread.stopRecorderCollage();
                    if (MultiViewCameraModule.this.mCollageOutputInfo != null) {
                        String outFilePath = MultiViewCameraModule.this.mCollageOutputInfo.getOutputDir() + MultiViewCameraModule.this.mCollageOutputInfo.getOutputFileName() + MultiViewCameraModule.this.mCollageOutputInfo.getOutputFileExt();
                        String videoSize = MultiViewCameraModule.this.mCollageOutputInfo.getVideoWidth() + "x" + MultiViewCameraModule.this.mCollageOutputInfo.getVideoHeight();
                        if (outFilePath != null) {
                            File outFile = new File(outFilePath);
                            if (outFile != null) {
                                MultiViewCameraModule.this.mQuickClipManager.setAfterShot();
                                DebugUtil.setEndTime("[2] MV video: all video players are stopped");
                                DebugUtil.setStartTime("[+] MV : updateThumbnail");
                                MultiViewCameraModule.this.mGet.getMediaSaveService().addVideo(MultiViewCameraModule.this.mGet.getAppContext(), MultiViewCameraModule.this.mGet.getAppContext().getContentResolver(), MultiViewCameraModule.this.mCollageOutputInfo.getOutputDir(), MultiViewCameraModule.this.mCollageOutputInfo.getOutputFileName(), videoSize, MultiViewCameraModule.this.getRecDurationTime(outFilePath), outFile.length(), MultiViewCameraModule.this.mLocationServiceManager.getCurrentLocation(), 1, MultiViewCameraModule.this.mOnMediaSavedListener);
                            }
                        }
                    }
                    MultiViewCameraModule.this.collageEnd();
                    synchronized (MultiViewCameraModule.this.mSynchAudioBuffer) {
                        for (int i = 0; i < MultiViewCameraModule.this.mNumVideoFile; i++) {
                            if (MultiViewCameraModule.this.mAudioBuffer[i] != null) {
                                MultiViewCameraModule.this.mAudioBuffer[i].clear();
                                MultiViewCameraModule.this.mAudioBuffer[i] = null;
                            }
                        }
                    }
                    MultiViewCameraModule.this.mAudioBuffer = null;
                    MultiViewCameraModule.this.mMaxVideoDuration = Long.MIN_VALUE;
                }
            }
        }

        /* JADX WARNING: Missing block: B:14:0x00cd, code:
            r8 = new java.nio.ByteBuffer[com.lge.camera.app.ext.MultiViewCameraModule.access$1000(r25.this$0)];
            r17 = 0;
            r9 = 0;
     */
        /* JADX WARNING: Missing block: B:16:0x00ea, code:
            if (r9 >= com.lge.camera.app.ext.MultiViewCameraModule.access$1000(r25.this$0)) goto L_0x010c;
     */
        /* JADX WARNING: Missing block: B:17:0x00ec, code:
            r8[r9] = r4[r9].getData();
            r17 = (int) (((long) r17) + r4[r9].getTimeStamp());
            r9 = r9 + 1;
     */
        /* JADX WARNING: Missing block: B:22:0x010c, code:
            if (r17 == 0) goto L_0x0175;
     */
        /* JADX WARNING: Missing block: B:23:0x010e, code:
            r17 = r17 / com.lge.camera.app.ext.MultiViewCameraModule.access$1000(r25.this$0);
     */
        /* JADX WARNING: Missing block: B:24:0x011a, code:
            com.lge.camera.util.CamLog.m3d(com.lge.camera.constants.CameraConstants.TAG, "timeStamp of mixed audio data = " + java.lang.String.format("%,d", new java.lang.Object[]{java.lang.Integer.valueOf(r17)}));
            r15 = r8[0].limit();
            r7 = java.nio.ByteBuffer.allocate(r15);
            r14 = 0;
            r9 = 0;
     */
        /* JADX WARNING: Missing block: B:25:0x0157, code:
            if (r9 >= r15) goto L_0x0194;
     */
        /* JADX WARNING: Missing block: B:26:0x0159, code:
            r12 = 0;
     */
        /* JADX WARNING: Missing block: B:28:0x0166, code:
            if (r12 >= com.lge.camera.app.ext.MultiViewCameraModule.access$1000(r25.this$0)) goto L_0x0178;
     */
        /* JADX WARNING: Missing block: B:29:0x0168, code:
            r14 = r14 + r8[r12].get(r9);
            r12 = r12 + 1;
     */
        /* JADX WARNING: Missing block: B:30:0x0175, code:
            r17 = 0;
     */
        /* JADX WARNING: Missing block: B:32:0x017c, code:
            if (r14 <= 127) goto L_0x018b;
     */
        /* JADX WARNING: Missing block: B:33:0x017e, code:
            r14 = 127;
     */
        /* JADX WARNING: Missing block: B:34:0x0180, code:
            r13 = (byte) r14;
            r7.position(r9);
            r7.put(r13);
            r14 = 0;
            r9 = r9 + 1;
     */
        /* JADX WARNING: Missing block: B:36:0x018f, code:
            if (r14 >= -128) goto L_0x0180;
     */
        /* JADX WARNING: Missing block: B:37:0x0191, code:
            r14 = -128;
     */
        /* JADX WARNING: Missing block: B:38:0x0194, code:
            r25.this$0.mRenderThread.setAudioData(r7, (long) r17);
     */
        /* JADX WARNING: Missing block: B:53:?, code:
            return;
     */
        public void audioDataAvailable(android.media.MediaCodec r26, int r27, long r28) {
            /*
            r25 = this;
            r18 = "CameraApp";
            r19 = new java.lang.StringBuilder;
            r19.<init>();
            r20 = "audioDataAvailable - Id = ";
            r19 = r19.append(r20);
            r0 = r25;
            r0 = r0.mId;
            r20 = r0;
            r19 = r19.append(r20);
            r20 = ", timeStamp = ";
            r19 = r19.append(r20);
            r0 = r19;
            r1 = r28;
            r19 = r0.append(r1);
            r19 = r19.toString();
            com.lge.camera.util.CamLog.m3d(r18, r19);
            r6 = r26.getOutputBuffers();
            r5 = r6[r27];
            r18 = r5.limit();
            r16 = java.nio.ByteBuffer.allocate(r18);
            r16.clear();
            r0 = r16;
            r0.put(r5);
            r0 = r25;
            r0 = com.lge.camera.app.ext.MultiViewCameraModule.this;
            r18 = r0;
            r0 = r18;
            r0 = r0.mSynchAudioBuffer;
            r19 = r0;
            monitor-enter(r19);
            r0 = r25;
            r0 = com.lge.camera.app.ext.MultiViewCameraModule.this;	 Catch:{ all -> 0x0109 }
            r18 = r0;
            r0 = r18;
            r0 = r0.mAudioBuffer;	 Catch:{ all -> 0x0109 }
            r18 = r0;
            r0 = r25;
            r0 = r0.mId;	 Catch:{ all -> 0x0109 }
            r20 = r0;
            r18 = r18[r20];	 Catch:{ all -> 0x0109 }
            r20 = new com.lge.camera.components.AudioData;	 Catch:{ all -> 0x0109 }
            r0 = r20;
            r1 = r28;
            r3 = r16;
            r0.<init>(r1, r3);	 Catch:{ all -> 0x0109 }
            r0 = r18;
            r1 = r20;
            r0.add(r1);	 Catch:{ all -> 0x0109 }
            r18 = r25.checkAllAudioBufferFilled();	 Catch:{ all -> 0x0109 }
            if (r18 != 0) goto L_0x007d;
        L_0x007b:
            monitor-exit(r19);	 Catch:{ all -> 0x0109 }
        L_0x007c:
            return;
        L_0x007d:
            r0 = r25;
            r0 = com.lge.camera.app.ext.MultiViewCameraModule.this;	 Catch:{ all -> 0x0109 }
            r18 = r0;
            r18 = r18.mNumVideoFile;	 Catch:{ all -> 0x0109 }
            r0 = r18;
            r4 = new com.lge.camera.components.AudioData[r0];	 Catch:{ all -> 0x0109 }
            r10 = 0;
            r9 = 0;
            r11 = r10;
        L_0x008e:
            r0 = r25;
            r0 = com.lge.camera.app.ext.MultiViewCameraModule.this;	 Catch:{ all -> 0x0109 }
            r18 = r0;
            r18 = r18.mNumTotalFiles;	 Catch:{ all -> 0x0109 }
            r0 = r18;
            if (r9 >= r0) goto L_0x00cc;
        L_0x009c:
            r0 = r25;
            r0 = com.lge.camera.app.ext.MultiViewCameraModule.this;	 Catch:{ all -> 0x0109 }
            r18 = r0;
            r18 = r18.mFileType;	 Catch:{ all -> 0x0109 }
            r18 = r18[r9];	 Catch:{ all -> 0x0109 }
            r20 = 2;
            r0 = r18;
            r1 = r20;
            if (r0 != r1) goto L_0x01ae;
        L_0x00b0:
            r10 = r11 + 1;
            r0 = r25;
            r0 = com.lge.camera.app.ext.MultiViewCameraModule.this;	 Catch:{ all -> 0x0109 }
            r18 = r0;
            r0 = r18;
            r0 = r0.mAudioBuffer;	 Catch:{ all -> 0x0109 }
            r18 = r0;
            r18 = r18[r9];	 Catch:{ all -> 0x0109 }
            r18 = r18.remove();	 Catch:{ all -> 0x0109 }
            r18 = (com.lge.camera.components.AudioData) r18;	 Catch:{ all -> 0x0109 }
            r4[r11] = r18;	 Catch:{ all -> 0x0109 }
        L_0x00c8:
            r9 = r9 + 1;
            r11 = r10;
            goto L_0x008e;
        L_0x00cc:
            monitor-exit(r19);	 Catch:{ all -> 0x0109 }
            r0 = r25;
            r0 = com.lge.camera.app.ext.MultiViewCameraModule.this;
            r18 = r0;
            r18 = r18.mNumVideoFile;
            r0 = r18;
            r8 = new java.nio.ByteBuffer[r0];
            r17 = 0;
            r9 = 0;
        L_0x00de:
            r0 = r25;
            r0 = com.lge.camera.app.ext.MultiViewCameraModule.this;
            r18 = r0;
            r18 = r18.mNumVideoFile;
            r0 = r18;
            if (r9 >= r0) goto L_0x010c;
        L_0x00ec:
            r18 = r4[r9];
            r18 = r18.getData();
            r8[r9] = r18;
            r0 = r17;
            r0 = (long) r0;
            r18 = r0;
            r20 = r4[r9];
            r20 = r20.getTimeStamp();
            r18 = r18 + r20;
            r0 = r18;
            r0 = (int) r0;
            r17 = r0;
            r9 = r9 + 1;
            goto L_0x00de;
        L_0x0109:
            r18 = move-exception;
            monitor-exit(r19);	 Catch:{ all -> 0x0109 }
            throw r18;
        L_0x010c:
            if (r17 == 0) goto L_0x0175;
        L_0x010e:
            r0 = r25;
            r0 = com.lge.camera.app.ext.MultiViewCameraModule.this;
            r18 = r0;
            r18 = r18.mNumVideoFile;
            r17 = r17 / r18;
        L_0x011a:
            r18 = "CameraApp";
            r19 = new java.lang.StringBuilder;
            r19.<init>();
            r20 = "timeStamp of mixed audio data = ";
            r19 = r19.append(r20);
            r20 = "%,d";
            r21 = 1;
            r0 = r21;
            r0 = new java.lang.Object[r0];
            r21 = r0;
            r22 = 0;
            r23 = java.lang.Integer.valueOf(r17);
            r21[r22] = r23;
            r20 = java.lang.String.format(r20, r21);
            r19 = r19.append(r20);
            r19 = r19.toString();
            com.lge.camera.util.CamLog.m3d(r18, r19);
            r18 = 0;
            r18 = r8[r18];
            r15 = r18.limit();
            r7 = java.nio.ByteBuffer.allocate(r15);
            r14 = 0;
            r13 = 0;
            r9 = 0;
        L_0x0157:
            if (r9 >= r15) goto L_0x0194;
        L_0x0159:
            r12 = 0;
        L_0x015a:
            r0 = r25;
            r0 = com.lge.camera.app.ext.MultiViewCameraModule.this;
            r18 = r0;
            r18 = r18.mNumVideoFile;
            r0 = r18;
            if (r12 >= r0) goto L_0x0178;
        L_0x0168:
            r18 = r8[r12];
            r0 = r18;
            r18 = r0.get(r9);
            r14 = r14 + r18;
            r12 = r12 + 1;
            goto L_0x015a;
        L_0x0175:
            r17 = 0;
            goto L_0x011a;
        L_0x0178:
            r18 = 127; // 0x7f float:1.78E-43 double:6.27E-322;
            r0 = r18;
            if (r14 <= r0) goto L_0x018b;
        L_0x017e:
            r14 = 127; // 0x7f float:1.78E-43 double:6.27E-322;
        L_0x0180:
            r13 = (byte) r14;
            r7.position(r9);
            r7.put(r13);
            r14 = 0;
            r9 = r9 + 1;
            goto L_0x0157;
        L_0x018b:
            r18 = -128; // 0xffffffffffffff80 float:NaN double:NaN;
            r0 = r18;
            if (r14 >= r0) goto L_0x0180;
        L_0x0191:
            r14 = -128; // 0xffffffffffffff80 float:NaN double:NaN;
            goto L_0x0180;
        L_0x0194:
            r0 = r25;
            r0 = com.lge.camera.app.ext.MultiViewCameraModule.this;
            r18 = r0;
            r0 = r18;
            r0 = r0.mRenderThread;
            r18 = r0;
            r0 = r17;
            r0 = (long) r0;
            r20 = r0;
            r0 = r18;
            r1 = r20;
            r0.setAudioData(r7, r1);
            goto L_0x007c;
        L_0x01ae:
            r10 = r11;
            goto L_0x00c8;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.ext.MultiViewCameraModule.VideoPlayerCallback.audioDataAvailable(android.media.MediaCodec, int, long):void");
        }

        private boolean checkAllAudioBufferFilled() {
            int nonEmptyBufferCnt = 0;
            for (int i = 0; i < MultiViewCameraModule.this.mNumTotalFiles; i++) {
                if (MultiViewCameraModule.this.mAudioBuffer[i] != null) {
                    if (MultiViewCameraModule.this.mAudioBuffer[i].isEmpty()) {
                        return false;
                    }
                    nonEmptyBufferCnt++;
                }
            }
            if (nonEmptyBufferCnt == MultiViewCameraModule.this.mNumVideoFile) {
                return true;
            }
            return false;
        }
    }

    public MultiViewCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public boolean onCameraShutterButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "multiview camera shutter clicked.");
        if (!this.mMultiviewFrameReady) {
            CamLog.m3d(CameraConstants.TAG, "mMultiviewFrameReady = " + this.mMultiviewFrameReady);
            return false;
        } else if ((getMVState() & 16) == 0 && (getMVState() & 32) == 0 && checkMultiviewStorage(1)) {
            return super.onCameraShutterButtonClicked();
        } else {
            return false;
        }
    }

    protected void updateMultiviewShotType(String value) {
        CamLog.m3d(CameraConstants.TAG, "updateMultiviewShotType value = " + value);
        if (isModeMenuVisible()) {
            hideModeMenu(false, true);
        }
        setSetting(Setting.KEY_MULTIVIEW_FRAMESHOT, value, true);
        if (this.mRenderThread != null) {
            this.mRenderThread.setupMultiViewRecorder();
        }
        this.mMultiViewManager.activateFirstGuideLayout();
        MultiViewFrame.resetCapturedTexture();
        if (this.mMultiviewFrame != null) {
            this.mMultiviewFrame.restoreVertex(this.mGet.getOrientationDegree());
        }
        if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            if ("off".equals(value)) {
                cancelImportedImage();
                this.mIsImportedImage = false;
                this.mSpliceViewImageImportManager.setReverseState(false);
                ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).setRotateState(0);
                this.mSpliceViewImageImportManager.showImportButton(false);
                this.mSpliceViewImageImportManager.showSwapAndRotateButton(false);
                this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(0, true);
                this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(1, true);
            } else {
                this.mSpliceViewImageImportManager.showImportButton(true);
                this.mSpliceViewImageImportManager.showSwapAndRotateButton(false);
                this.mSpliceViewImageImportManager.showImportLayout(true);
                this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(0, true);
                this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(1, false);
            }
            this.mSpliceViewImageImportManager.setupCueLayout();
        }
        resetFrameShotProgress();
    }

    private boolean checkMultiviewStorage(int checkFor) {
        int storageType = getCurStorage();
        if (storageType == 0) {
            return checkStorage(checkFor, 0);
        }
        if (checkStorage(4, 0)) {
            return checkStorage(checkFor, storageType);
        }
        return false;
    }

    public boolean onShutterBottomButtonLongClickListener() {
        if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()) || this.mMultiviewFrame.isCapturingPreview() || isProgressDialogVisible() || isRotateDialogVisible() || this.mGet.isModuleChanging() || !checkModuleValidate(223) || (getMVState() & 32) != 0 || !checkMultiviewStorage(1)) {
            return false;
        }
        if (isMultiviewFrameShot()) {
            doIntervalFrameShotMV(true);
        }
        return true;
    }

    public void onShutterTopButtonLongClickListener() {
        if (!CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            CamLog.m3d(CameraConstants.TAG, "onShutterTopButtonLongClickListener");
            if (!this.mMultiviewFrame.isCapturingPreview() && !isProgressDialogVisible() && !isRotateDialogVisible() && !this.mGet.isModuleChanging() && checkModuleValidate(223) && (getMVState() & 16) == 0 && isMultiviewFrameShot() && checkMultiviewStorage(2)) {
                access$400(true);
                if (AudioUtil.isAudioRecording(getAppContext())) {
                    this.mGet.postOnUiThread(new HandlerRunnable(this) {
                        public void handleRun() {
                            if (MultiViewCameraModule.this.mGet != null) {
                                if (AudioUtil.isAudioRecording(MultiViewCameraModule.this.getAppContext())) {
                                    AudioUtil.setAudioAvailability(false);
                                    if (MultiViewCameraModule.this.mToastManager != null) {
                                        MultiViewCameraModule.this.mToastManager.showShortToast(MultiViewCameraModule.this.mGet.getActivity().getString(C0088R.string.error_cannot_start_recording_with_audio));
                                    }
                                    MultiViewCameraModule.this.access$400(false);
                                    return;
                                }
                                AudioUtil.setAudioAvailability(true);
                                if (MultiViewCameraModule.this.isMultiviewFrameShot()) {
                                    MultiViewCameraModule.this.doIntervalFrameShotMV(false);
                                }
                            }
                        }
                    }, 300);
                    if (!AudioUtil.checkAudioAvailabilityBeforeRecording()) {
                    }
                    return;
                }
                AudioUtil.setAudioAvailability(true);
                if (isMultiviewFrameShot()) {
                    doIntervalFrameShotMV(false);
                }
            }
        }
    }

    public void onShutterTopButtonClickListener() {
        CamLog.m3d(CameraConstants.TAG, "-rec- onShutterTopButtonClickListener");
        if ((this.mPostviewManager != null && this.mPostviewManager.isPostviewShowing()) || SystemBarUtil.isSystemUIVisible(getActivity()) || isAnimationShowing() || !checkMultiviewStorage(2) || !checkModuleValidate(95)) {
            return;
        }
        if ((this.mReviewThumbnailManager == null || !this.mReviewThumbnailManager.isQuickViewAniStarted()) && this.mCaptureButtonManager != null) {
            switch (this.mCaptureButtonManager.getShutterButtonMode(1)) {
                case 2:
                    if (!isMultiviewFrameShot()) {
                        onRecordStartButtonClicked();
                    } else if (!this.mCaptureButtonManager.getShutterButtonComp(1).isShutterButtonLongPress()) {
                        if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                            this.mSpliceViewImageImportManager.setCurrentImportState();
                            this.mSpliceViewImageImportManager.processForTimerShot(true);
                        }
                        onRecordStartButtonClicked();
                    }
                    if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                        this.mSpliceViewImageImportManager.showDualCamLayoutAll(false);
                        return;
                    }
                    return;
                case 3:
                    if (this.mRecordingUIManager == null || this.mRecordingUIManager.checkMinRecTime()) {
                        onShutterStopButtonClicked();
                        CamLog.m3d(CameraConstants.TAG, "-rec- VID_STOP_BUTTON");
                        return;
                    }
                    CamLog.m7i(CameraConstants.TAG, "Video recording time is too short.");
                    return;
                case 4:
                    if (checkInterval(1)) {
                        onVideoPauseClicked();
                        return;
                    }
                    return;
                case 5:
                    if (checkInterval(1)) {
                        onVideoResumeClicked();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    protected void doIntervalFrameShotMV(boolean isImage) {
        CamLog.m3d(CameraConstants.TAG, "doIntervalFrameShotMV: " + isImage);
        LdbUtil.setMultiViewIntervalMode(true);
        setQuickClipIcon(true, false);
        this.mFrameShotProgress = true;
        if (this.mFrameShotProgressCount == this.mFrameShotMaxCount) {
            resetFrameShotProgress();
        }
        if (isImage) {
            setMVState(16);
            doStartTimerShot(new TimerTypeMultiview());
        } else {
            setMVState(32);
            this.mGet.enableConeMenuIcon(31, false);
            this.mTimerManager.startTimerShot(2, 1);
            this.mGet.hideModeMenu(false, true);
            doCleanView(true, false, false);
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setThumbnailVisibility(8, true);
        }
        setCaptureButtonEnable(false, 3);
    }

    private boolean isRecordEnableState(ListPreference listPref) {
        if (listPref == null) {
            CamLog.m5e(CameraConstants.TAG, "KEY_VIDEO_RECORDSIZE listPref is null in startRecorder");
            return false;
        } else if (this.mFrameShotCameraIdOrder != null) {
            return true;
        } else {
            CamLog.m3d(CameraConstants.TAG, "-multiview- mFrameShotCameraIdOrder is null");
            return false;
        }
    }

    private String getVideoSize(int cameraId) {
        String videoSize;
        CamLog.m3d(CameraConstants.TAG, "-multiview- isMultiviewFrameShot() = " + isMultiviewFrameShot());
        if (!isMultiviewFrameShot()) {
            videoSize = "1280x720";
        } else if (cameraId == 0 || cameraId == 4) {
            videoSize = "1280x720";
        } else if (FunctionProperties.useWideRearAsDefault() && cameraId == 2) {
            videoSize = "1280x720";
        } else {
            videoSize = "1280x960";
        }
        CamLog.m3d(CameraConstants.TAG, "-rec- videoSize = " + videoSize);
        return videoSize;
    }

    public void onVideoShutterClicked() {
        CamLog.m3d(CameraConstants.TAG, "video shutter clicked");
        playRecordingSound(true);
        if (!checkMultiviewStorage(2)) {
            return;
        }
        if (TelephonyUtil.phoneInCall(this.mGet.getAppContext())) {
            if ((getMVState() & 32) != 0) {
                setMVState(1);
            }
            setQuickButtonEnable(100, true, true);
            setCaptureButtonEnable(true, 2);
            this.mToastManager.showShortToast(this.mGet.getActivity().getString(C0088R.string.error_video_recording_during_call));
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "getMVState = " + getMVState());
        if ((getMVState() & 4) == 0 && (getMVState() & 8) == 0) {
            if (isMultiviewFrameShot()) {
                this.mFrameShotProgress = true;
            } else {
                this.mFrameShotProgress = false;
            }
            if (this.mFrameShotProgressCount == this.mFrameShotMaxCount) {
                resetFrameShotProgress();
            }
            if (this.mFrameShotProgressCount == 0) {
                this.mCameraDegree = getVideoOrientation();
                CamLog.m3d(CameraConstants.TAG, "-multiview- mCameraDegree = " + this.mCameraDegree);
            }
            if (this.mFrameShotCameraIdOrder != null) {
                startRecorder();
                if (CameraSecondHolder.isSecondCameraOpened() && isMultiviewFrameShot() && !this.mIsSingleViewScreenRecording) {
                    CameraSecondHolder.subinstance().getCameraDevice().setRecordSurfaceToTarget(true);
                }
            }
        }
    }

    protected void startRecorder() {
        ListPreference listPref = getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        if (isRecordEnableState(listPref)) {
            String dir;
            String videoSize;
            AudioUtil.setUseBuiltInMicForRecording(getAppContext(), true);
            String extend = ".mp4";
            if (this.mFrameShotProgress) {
                dir = getAppContext().getFilesDir().getAbsolutePath();
            } else {
                dir = getCurDir();
            }
            int storage = getCurStorage();
            if (this.mRecordingUIManager != null) {
                this.mRecordingUIManager.initVideoTime();
            }
            this.mIsMultiviewRecording = true;
            String fileName = makeFileName(1, storage, dir, false, getSettingValue(Setting.KEY_MODE));
            String outFilePath = dir + fileName + extend;
            CamLog.m3d(CameraConstants.TAG, "output file is : " + outFilePath);
            IntentBroadcastUtil.stopVoiceRec(this.mGet.getActivity(), true);
            if (this.mToastManager.isShowing()) {
                this.mToastManager.hideAndResetDisturb(0);
            }
            int cameraId = getCameraDeviceId();
            CamLog.m3d(CameraConstants.TAG, "-rec- cameraId : " + cameraId);
            setVideoLimitSize();
            setCameraState(5);
            if (!CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()) || isMultiviewFrameShot()) {
                videoSize = getVideoSize(cameraId);
            } else {
                videoSize = "2880x1440";
            }
            int i = storage;
            this.mStartRecorderInfo = new StartRecorderInfo(i, fileName, outFilePath, videoSize, 1, 30.0d, 0, CameraDeviceUtils.getVideoFlipType(getVideoOrientation(), this.mCameraId, "off".equals(getSettingValue(Setting.KEY_SAVE_DIRECTION)), false));
            setSingleviewRecordingFlag(cameraId);
            if (isMultiviewFrameShot() && !this.mIsSingleViewScreenRecording) {
                if (cameraId == 0 || cameraId == 2) {
                    setParamForVideoRecord(true, listPref, videoSize);
                } else {
                    setRecordingParamOnFront("true");
                }
            }
            sVideoSize = Utils.sizeStringToArray(listPref.getExtraInfo(2));
            this.mIsFileSizeLimitReached = false;
            this.mIsUHDRecTimeLimitWarningDisplayed = false;
            if (this.mLightFrameManager.isLightFrameMode()) {
                this.mLightFrameManager.turnOffLightFrame();
            }
            if (!isMultiviewFrameShot() || this.mIsSingleViewScreenRecording) {
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        CamLog.m3d(CameraConstants.TAG, "-rec- runStartRecorder : post runnable after 100msec");
                        MultiViewCameraModule.this.runStartRecorder(true);
                    }
                }, 100);
            }
            this.mCameraIdBeforeInAndOutZoom = this.mCameraId;
            return;
        }
        setCameraState(1);
    }

    protected void setSingleviewRecordingFlag(int cameraId) {
        if (!isMultiviewFrameShot()) {
            this.mIsSingleViewScreenRecording = false;
        } else if (cameraId == 4) {
            this.mIsSingleViewScreenRecording = true;
        } else {
            this.mIsSingleViewScreenRecording = false;
        }
        CamLog.m3d(CameraConstants.TAG, "mIsSingleViewScreenRecording = " + this.mIsSingleViewScreenRecording);
    }

    protected void afterLastFrameShot() {
        new Thread(new C04013()).start();
    }

    protected void setPostviewBitmap(boolean isImageVideo) {
        int degree = ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getRotateState();
        Bitmap bm;
        if (isImageVideo) {
            if (this.mSpliceViewImageImportManager.getReverseState()) {
                this.mPostViewBitmap[0] = this.mSplicePostViewManager.getRotatedImage(this.mSpliceViewImageImportManager.getBitmapInPrePostView(1), this.mSplicePostViewManager.getIntDegree(degree));
                this.mPostViewBitmap[1] = createCopiedBitmap(MultiViewFrame.sPreviewBitmap[1]);
                return;
            }
            if (this.mFileType[0] == 2) {
                bm = createCopiedBitmap(MultiViewFrame.sPreviewBitmap[0]);
            } else {
                bm = this.mSpliceViewImageImportManager.getBitmapInPrePostView(0);
            }
            this.mPostViewBitmap[0] = this.mSplicePostViewManager.getRotatedImage(bm, this.mSplicePostViewManager.getIntDegree(degree));
            this.mPostViewBitmap[1] = createCopiedBitmap(MultiViewFrame.sPreviewBitmap[1]);
        } else if (this.mSpliceViewImageImportManager.getReverseState()) {
            this.mPostViewBitmap[1] = this.mSplicePostViewManager.getRotatedImage(this.mSpliceViewImageImportManager.getBitmapInPrePostView(1), this.mSplicePostViewManager.getIntDegree(degree));
            this.mPostViewBitmap[0] = createCopiedBitmap(MultiViewFrame.sPreviewBitmap[1]);
        } else {
            if (this.mFileType[0] == 2) {
                bm = createCopiedBitmap(MultiViewFrame.sPreviewBitmap[0]);
            } else {
                bm = this.mSpliceViewImageImportManager.getBitmapInPrePostView(0);
            }
            this.mPostViewBitmap[0] = this.mSplicePostViewManager.getRotatedImage(bm, this.mSplicePostViewManager.getIntDegree(degree));
            this.mPostViewBitmap[1] = createCopiedBitmap(MultiViewFrame.sPreviewBitmap[1]);
        }
    }

    protected Bitmap createCopiedBitmap(Bitmap src) {
        if (src != null) {
            return Bitmap.createScaledBitmap(src, src.getWidth(), src.getHeight(), false);
        }
        CamLog.m5e(CameraConstants.TAG, "src is null");
        return null;
    }

    protected void setPostViewImage() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                MultiViewCameraModule.this.setButtonsVisibilityForPostView(false);
                MultiViewCameraModule.this.mSplicePostViewManager.initLayout();
                MultiViewCameraModule.this.setPostviewBitmap(false);
                MultiViewCameraModule.this.mSplicePostViewManager.setPostViewContents(MultiViewCameraModule.this.mPostViewBitmap);
                MultiViewCameraModule.this.mSplicePostViewManager.setDegree(MultiViewCameraModule.this.mGet.getOrientationDegree(), false);
                MultiViewCameraModule.this.access$1600(true, true, MultiViewCameraModule.this.getShareContentType());
            }
        });
    }

    protected void setPostViewContents() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                int[] cameraId = ((MultiViewLayout) MultiViewCameraModule.this.mMultiViewLayoutList.get(MultiViewCameraModule.this.getLayoutIndex())).getCurCameraIdArray();
                MultiViewCameraModule.this.setButtonsVisibilityForPostView(false);
                MultiViewCameraModule.this.mSplicePostViewManager.initLayout();
                if (MultiViewCameraModule.this.mNumVideoFile == MultiViewCameraModule.this.mNumTotalFiles) {
                    MultiViewCameraModule.this.setPostviewBitmap(false);
                    MultiViewCameraModule.this.mSplicePostViewManager.setPostViewContents(MultiViewCameraModule.this.mPostViewBitmap, cameraId, MultiViewCameraModule.this.mVideoDegree);
                } else {
                    MultiViewCameraModule.this.setPostviewBitmap(true);
                    MultiViewCameraModule.this.mSplicePostViewManager.setPostViewContents(MultiViewCameraModule.this.mPostViewBitmap, cameraId, MultiViewCameraModule.this.mFileType, MultiViewCameraModule.this.mSpliceViewImageImportManager.getReverseState(), ((MultiViewLayout) MultiViewCameraModule.this.mMultiViewLayoutList.get(MultiViewCameraModule.this.getLayoutIndex())).getRotateState(), MultiViewCameraModule.this.mVideoDegree);
                }
                MultiViewCameraModule.this.mSplicePostViewManager.setDegree(MultiViewCameraModule.this.mGet.getOrientationDegree(), false);
                MultiViewCameraModule.this.access$1600(true, true, MultiViewCameraModule.this.getShareContentType());
            }
        });
    }

    private String getShareContentType() {
        if (this.mFileType == null || this.mFileType.length == 0) {
            return null;
        }
        String contentType = "image/*";
        for (int fileType : this.mFileType) {
            if (fileType == 2) {
                return "video/*";
            }
        }
        return contentType;
    }

    protected void doScreenCapture() {
        CamLog.m3d(CameraConstants.TAG, "-cp- doScreenCapture add imagefile");
        if (this.mFrameShotProgress) {
            String filePath = getAppContext().getFilesDir().getAbsolutePath() + CameraConstants.MULTIVIEW_FILE_NAME + this.mFrameShotProgressCount + ".jpg";
            capturePreview(filePath);
            this.mFrameShotFilePath.add(filePath);
            this.mVideoDegree.add(Integer.valueOf(this.mCurDegree));
            CamLog.m3d(CameraConstants.TAG, "-degree- add degree = " + this.mCurDegree + " videoDegree size = " + this.mVideoDegree.size() + ", filepath = " + filePath);
        }
    }

    protected void capturePreview(String filePath) {
        this.mIsCapturingPreview = true;
        this.mMultiviewFrame.freezePreview(this.mFrameShotProgressCount, filePath);
    }

    protected void startMakingCollageVideo(final MultiViewRecordInputInfo inputInfo) {
        new Thread(new Runnable() {
            public void run() {
                CamLog.m3d(CameraConstants.TAG, "startMakingCollageVideo - started");
                int[] video_size = Utils.sizeStringToArray(CameraConstants.MODE_SQUARE_SPLICE.equals(MultiViewCameraModule.this.getShotMode()) ? "2880x1440" : "1920x1080");
                String outputDir = MultiViewCameraModule.this.getCurDir();
                MultiViewCameraModule.this.mRecordedFileName = MultiViewCameraModule.this.makeFileName(1, MultiViewCameraModule.this.getCurStorage(), outputDir, false, MultiViewCameraModule.this.getSettingValue(Setting.KEY_MODE));
                String outputFileExt = ".mp4";
                CamLog.m3d(CameraConstants.TAG, "outputFileName = " + MultiViewCameraModule.this.mRecordedFileName);
                MultiViewCameraModule.this.mCollageOutputInfo = new MVRecordOutputInfo("video/avc", video_size[0], video_size[1], 17000000, 30, 1, MultiViewCameraModule.this.mCameraDegree, "audio/mp4a-latm", 48000, 2, CameraConstants.VALUE_VIDEO_BITRATE_MMS, outputDir, MultiViewCameraModule.this.mRecordedFileName, ".mp4");
                MultiViewCameraModule.this.mCollageSTLatch = new CountDownLatch(1);
                MultiViewCameraModule.this.mRenderThread.startRecorderCollage(inputInfo, MultiViewCameraModule.this.mCollageOutputInfo);
                MultiViewCameraModule.this.setLayoutForRecording(MultiViewCameraModule.this.getLayoutIndex());
                try {
                    CamLog.m3d(CameraConstants.TAG, "Collage - wait for surface texture ready callback");
                    MultiViewCameraModule.this.mCollageSTLatch.await();
                    CamLog.m3d(CameraConstants.TAG, "Collage - continue after surface texture ready callback");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                VideoPlayerCallback[] mVideoPlayerCallback = new VideoPlayerCallback[MultiViewCameraModule.this.mNumTotalFiles];
                MultiViewCameraModule.this.mPlayStartedCount = 0;
                MultiViewCameraModule.this.mPlayDoneCount = 0;
                for (int i = 0; i < MultiViewCameraModule.this.mNumTotalFiles; i++) {
                    String filePath = (String) MultiViewCameraModule.this.mFrameShotFilePath.get(i);
                    mVideoPlayerCallback[i] = new VideoPlayerCallback(i);
                    if (MultiViewCameraModule.this.mSurfaceTextureCollage[i] != null) {
                        new VideoPlayThread(new File(filePath), new Surface(MultiViewCameraModule.this.mSurfaceTextureCollage[i]), mVideoPlayerCallback[i]).setName("VideoPlayThread(" + i + ")");
                    }
                }
                DebugUtil.setEndTime("[1] MV video: all video players are started");
                DebugUtil.setStartTime("[2] MV video: all video players are stopped");
            }
        }, "CollageThread").start();
    }

    protected void setUIOnCollageSaving() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                MultiViewCameraModule.this.showDialog(132);
                MultiViewCameraModule.this.mGet.enableConeMenuIcon(31, false);
            }
        });
    }

    private MultiViewRecordInputInfo makeCollageInputInfo(ArrayList<String> collageFilePath, ArrayList<Integer> degree) {
        if (collageFilePath == null) {
            CamLog.m5e(CameraConstants.TAG, "collage file path is NULL");
            return null;
        }
        int totalCount = collageFilePath.size();
        int[] fileType = new int[totalCount];
        Iterator<String> it = collageFilePath.iterator();
        int imageCount = 0;
        int i = 0;
        while (it.hasNext()) {
            String fileName = (String) it.next();
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
            CamLog.m3d(CameraConstants.TAG, "extension = " + ext);
            int i2;
            if (QuickClipUtil.VIDEO.equals(ext)) {
                i2 = i + 1;
                fileType[i] = 2;
                i = i2;
            } else {
                i2 = i + 1;
                fileType[i] = 1;
                imageCount++;
                i = i2;
            }
        }
        if (this.mSpliceViewImageImportManager.getReverseState()) {
            int temp = fileType[1];
            fileType[1] = fileType[0];
            fileType[0] = temp;
            Collections.reverse(degree);
            Collections.reverse(collageFilePath);
        }
        return new MultiViewRecordInputInfo(collageFilePath, degree, fileType, totalCount, imageCount);
    }

    private void collageEnd() {
        CamLog.m3d(CameraConstants.TAG, "-th- collageEnd");
        resetFrameShotProgress();
        sendLDBIntentOnAfterStopRecording();
        setMVState(1);
        if (this.mRenderThread != null && this.mRenderThread.isRequiredToFinishAfterSaving()) {
            CamLog.m3d(CameraConstants.TAG, "-th- activity is paused stopThread");
            this.mRenderThread.stopThread();
            release();
        }
        multiviewMakingCollageEnd();
        MultiViewFrame.deleteCapturedTexture();
        if (sLatch2 != null) {
            sLatch2.countDown();
            CamLog.m3d(CameraConstants.TAG, "MultiView - sLatch2 countdown");
        }
        resetImportedImageOnUiThread();
    }

    public void afterPreviewCaptured() {
        CamLog.m3d(CameraConstants.TAG, "-cp- afterPreviewCaptured");
        if (this.mSyncFreezePreview != null) {
            synchronized (this.mSyncFreezePreview) {
                this.mSyncFreezePreview.notify();
            }
        }
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                boolean z = false;
                CamLog.m3d(CameraConstants.TAG, "-cp- Enable shutter button");
                MultiViewCameraModule.this.mSnapShotChecker.releaseSnapShotChecker();
                if (MultiViewCameraModule.this.mCaptureButtonManager != null) {
                    MultiViewCameraModule.this.mCaptureButtonManager.setShutterButtonEnable(true, MultiViewCameraModule.this.getShutterButtonType());
                    if (TelephonyUtil.phoneInCall(MultiViewCameraModule.this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
                        MultiViewCameraModule.this.setCaptureButtonEnable(false, 1);
                    }
                }
                if (MultiViewCameraModule.this.mQuickButtonManager != null) {
                    MultiViewCameraModule.this.mQuickButtonManager.refreshButtonEnable(100, true, true);
                }
                if (MultiViewCameraModule.this.mReviewThumbnailManager != null) {
                    GestureViewManager access$3800 = MultiViewCameraModule.this.mReviewThumbnailManager;
                    if (MultiViewCameraModule.this.getUri() != null) {
                        z = true;
                    }
                    access$3800.setEnabled(z);
                }
                if (MultiViewCameraModule.this.isSupportedQuickClip()) {
                    MultiViewCameraModule.this.mQuickClipManager.rollbackStatus();
                }
            }
        });
        this.mIsTakingPicture = false;
        this.mIsCapturingPreview = false;
    }

    protected void updateRecordingFlashState(boolean isRecordingStart, boolean hasAutoMode) {
    }

    protected void doPhoneStateListenerAction(int state) {
        super.doPhoneStateListenerAction(state);
        if (state != 0) {
            if ((getMVState() & 32) != 0) {
                if (this.mToastManager != null) {
                    this.mToastManager.showShortToast(this.mGet.getActivity().getString(C0088R.string.error_cannot_start_recording_with_audio));
                }
                setMVState(1, true);
            }
            if (this.mCaptureButtonManager != null) {
                this.mCaptureButtonManager.setShutterButtonEnable(true, getShutterButtonType());
                if ((getMVState() & 1) != 0 && 3 != this.mCaptureButtonManager.getShutterButtonMode(1)) {
                    this.mCaptureButtonManager.setShutterButtonEnable(false, 1);
                }
            }
        } else if (this.mLayoutSelectionManager != null) {
            this.mLayoutSelectionManager.setVisibility(true);
        }
    }

    public void stopRecordByCallPopup() {
        if (isMultiviewFrameShot()) {
            cancelRecording();
            if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                this.mSpliceViewImageImportManager.showDualCamLayoutAll(true);
                return;
            }
            return;
        }
        super.stopRecordByCallPopup();
    }

    protected void cancelImportedImage() {
    }

    public boolean isFaceDetectionSupported() {
        return false;
    }
}
