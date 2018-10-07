package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.SizeF;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.DrawingPanel;
import com.lge.camera.components.DrawingPanel.DrawingPanelListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ICameraCallback.CameraImageCallback;
import com.lge.camera.device.ICameraCallback.CameraImageMetaCallback;
import com.lge.camera.device.ICameraCallback.CameraPreviewDataCallback;
import com.lge.camera.device.PanoramaControl;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.SupportedExif;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorConverter;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;
import com.lge.panorama.BufferCalculator;
import com.lge.panorama.Panorama;
import com.lge.panorama.Panorama.SignatureCallbackInJava;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class PanoramaModuleLGNormal extends PanoramaModuleLGImpl {
    protected static final long FRAME_DUR = 33;
    protected static final int SKIP_MINI_PREV_CNT = 1;
    protected CameraImageCallback fullFrameCbHal3 = new C043612();
    protected BufferCalculator mBufferCalculator = new BufferCalculator();
    protected int[] mLastPos = new int[]{0, 0};
    protected final PanoramaControl mPanoramaController = new PanoramaControl(this);
    private int mPrevResId = 0;
    protected CameraPreviewDataCallback mPreviewFeedCallback = new C04569();
    protected CameraPreviewDataCallback mPreviewMiniDirectCallback = new C04548();
    protected ConcurrentLinkedQueue<BufferId> mQueueBufferId = null;
    protected ConcurrentLinkedQueue<FrameData> mQueueFrame = null;
    protected PictureCallback mRawCallback = new C043310();
    protected int mSkipPrevMiniCnt = 0;
    protected CameraImageMetaCallback metaFullFrameCbHal3 = new C043814();
    protected CameraImageMetaCallback metaPrevCbHal3 = new C043713();
    protected CameraImageCallback previewCbHal3 = new C043411();

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$10 */
    class C043310 implements PictureCallback {
        C043310() {
        }

        /* JADX WARNING: Missing block: B:40:?, code:
            return;
     */
        public void onPictureTaken(byte[] r13, android.hardware.Camera r14) {
            /*
            r12 = this;
            r10 = 0;
            r2 = java.lang.System.currentTimeMillis();
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;
            r4.setFisrtTimestamp(r2, r2);
            r5 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.AddBufferSyncOBJ;
            monitor-enter(r5);
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.mIsFeeding;	 Catch:{ all -> 0x00d2 }
            if (r4 != 0) goto L_0x0027;
        L_0x0014:
            r4 = "CameraApp";
            r6 = "mIsFeeding is false, return picture callback.";
            com.lge.camera.util.CamLog.m11w(r4, r6);	 Catch:{ all -> 0x00d2 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r6 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r6 = r6.mC_s;	 Catch:{ all -> 0x00d2 }
            r6 = r2 - r6;
            r4.mC_dur = r6;	 Catch:{ all -> 0x00d2 }
            monitor-exit(r5);	 Catch:{ all -> 0x00d2 }
        L_0x0026:
            return;
        L_0x0027:
            if (r13 == 0) goto L_0x00fd;
        L_0x0029:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r6 = r4.mC_cnt;	 Catch:{ all -> 0x00d2 }
            r4 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1));
            if (r4 != 0) goto L_0x0035;
        L_0x0031:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4.mC_s = r2;	 Catch:{ all -> 0x00d2 }
        L_0x0035:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r6 = r4.mC_cnt;	 Catch:{ all -> 0x00d2 }
            r8 = 1;
            r6 = r6 + r8;
            r4.mC_cnt = r6;	 Catch:{ all -> 0x00d2 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.isZeroCopy();	 Catch:{ all -> 0x00d2 }
            if (r4 == 0) goto L_0x00d5;
        L_0x0046:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.mFrameHashMap;	 Catch:{ all -> 0x00d2 }
            if (r4 == 0) goto L_0x00ae;
        L_0x004c:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.mQueueBufferId;	 Catch:{ all -> 0x00d2 }
            if (r4 == 0) goto L_0x00ae;
        L_0x0052:
            r1 = r13.hashCode();	 Catch:{ all -> 0x00d2 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.mFrameHashMap;	 Catch:{ all -> 0x00d2 }
            r6 = java.lang.Integer.valueOf(r1);	 Catch:{ all -> 0x00d2 }
            r4 = r4.containsKey(r6);	 Catch:{ all -> 0x00d2 }
            if (r4 == 0) goto L_0x00ae;
        L_0x0064:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.mFrameHashMap;	 Catch:{ all -> 0x00d2 }
            r6 = java.lang.Integer.valueOf(r1);	 Catch:{ all -> 0x00d2 }
            r4 = r4.get(r6);	 Catch:{ all -> 0x00d2 }
            r4 = (java.lang.Integer) r4;	 Catch:{ all -> 0x00d2 }
            r0 = r4.intValue();	 Catch:{ all -> 0x00d2 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.mQueueBufferId;	 Catch:{ all -> 0x00d2 }
            r6 = new com.lge.camera.app.ext.PanoramaModuleLGNormal$BufferId;	 Catch:{ all -> 0x00d2 }
            r7 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r6.<init>(r0, r2);	 Catch:{ all -> 0x00d2 }
            r4.add(r6);	 Catch:{ all -> 0x00d2 }
            r4 = "CameraApp";
            r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d2 }
            r6.<init>();	 Catch:{ all -> 0x00d2 }
            r7 = "onPreviewFrame mQueueBufferId : ";
            r6 = r6.append(r7);	 Catch:{ all -> 0x00d2 }
            r6 = r6.append(r0);	 Catch:{ all -> 0x00d2 }
            r7 = ", mQueueBuffer size : ";
            r6 = r6.append(r7);	 Catch:{ all -> 0x00d2 }
            r7 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r7 = r7.mQueueBufferId;	 Catch:{ all -> 0x00d2 }
            r7 = r7.size();	 Catch:{ all -> 0x00d2 }
            r6 = r6.append(r7);	 Catch:{ all -> 0x00d2 }
            r6 = r6.toString();	 Catch:{ all -> 0x00d2 }
            com.lge.camera.util.CamLog.m3d(r4, r6);	 Catch:{ all -> 0x00d2 }
        L_0x00ae:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.isZeroCopy();	 Catch:{ all -> 0x00d2 }
            if (r4 == 0) goto L_0x0105;
        L_0x00b6:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.mPanorama;	 Catch:{ all -> 0x00d2 }
            if (r4 == 0) goto L_0x0105;
        L_0x00bc:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.mPanorama;	 Catch:{ all -> 0x00d2 }
            r0 = r4.dequeueBuffer();	 Catch:{ all -> 0x00d2 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r6 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.sBUFFER_SIZE;	 Catch:{ all -> 0x00d2 }
            r4 = r4.getRawBufferById(r0, r6);	 Catch:{ all -> 0x00d2 }
            r14.addRawImageCallbackBuffer(r4);	 Catch:{ all -> 0x00d2 }
        L_0x00cf:
            monitor-exit(r5);	 Catch:{ all -> 0x00d2 }
            goto L_0x0026;
        L_0x00d2:
            r4 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x00d2 }
            throw r4;
        L_0x00d5:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r6 = r4.mC_cnt;	 Catch:{ all -> 0x00d2 }
            r4 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1));
            if (r4 < 0) goto L_0x00ae;
        L_0x00dd:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.mQueueFrame;	 Catch:{ all -> 0x00d2 }
            if (r4 == 0) goto L_0x00ae;
        L_0x00e3:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.mQueueFrame;	 Catch:{ all -> 0x00d2 }
            r4 = r4.size();	 Catch:{ all -> 0x00d2 }
            r6 = 2;
            if (r4 > r6) goto L_0x00ae;
        L_0x00ee:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.mQueueFrame;	 Catch:{ all -> 0x00d2 }
            r6 = new com.lge.camera.app.ext.PanoramaModuleLGNormal$FrameData;	 Catch:{ all -> 0x00d2 }
            r7 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r6.<init>(r13, r2);	 Catch:{ all -> 0x00d2 }
            r4.add(r6);	 Catch:{ all -> 0x00d2 }
            goto L_0x00ae;
        L_0x00fd:
            r4 = "CameraApp";
            r6 = "RawCallback data is null.";
            com.lge.camera.util.CamLog.m11w(r4, r6);	 Catch:{ all -> 0x00d2 }
            goto L_0x00ae;
        L_0x0105:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r6 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r6 = r6.mCurBufferId;	 Catch:{ all -> 0x00d2 }
            r6 = r6 + 1;
            r7 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.sBUFFER_SIZE;	 Catch:{ all -> 0x00d2 }
            r6 = r6 % r7;
            r4.mCurBufferId = r6;	 Catch:{ all -> 0x00d2 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00d2 }
            r4 = r4.getRawBuffer();	 Catch:{ all -> 0x00d2 }
            r14.addRawImageCallbackBuffer(r4);	 Catch:{ all -> 0x00d2 }
            goto L_0x00cf;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.ext.PanoramaModuleLGNormal.10.onPictureTaken(byte[], android.hardware.Camera):void");
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$11 */
    class C043411 implements CameraImageCallback {
        C043411() {
        }

        public void onImageData(Image image) {
            if (image != null) {
                try {
                    if (PanoramaModuleLGNormal.this.checkAvailableUpdatingPreview()) {
                        ColorConverter.yuvToRgbWithResizeForYuv420SP(PanoramaModuleLGNormal.this.mPreviewImageRGBBuffer, image.getPlanes()[0].getBuffer(), image.getPlanes()[2].getBuffer(), image.getPlanes()[0].getRowStride(), image.getWidth(), image.getHeight(), PanoramaModuleLGNormal.this.mPreviewImageMini.getWidth(), PanoramaModuleLGNormal.this.mPreviewImageMini.getHeight(), 0);
                        PanoramaModuleLGNormal.this.mPreviewImageMini.copyPixelsFromBuffer(PanoramaModuleLGNormal.this.mPreviewImageRGBBuffer);
                        PanoramaModuleLGNormal.this.mPreviewImageRGBBuffer.rewind();
                        PanoramaModuleLGNormal.this.mGet.runOnUiThread(new HandlerRunnable(PanoramaModuleLGNormal.this) {
                            public void handleRun() {
                                if (PanoramaModuleLGNormal.this.checkAvailableUpdatingPreview()) {
                                    PanoramaModuleLGNormal.this.mPreviewMiniCanvas.drawBitmap(PanoramaModuleLGNormal.this.mPreviewImageMini, null, PanoramaModuleLGNormal.this.mRectPreviewMini, null);
                                    PanoramaModuleLGNormal.this.mPreviewMini.invalidate();
                                    PanoramaModuleLGNormal.this.mPrevDrawStarted = true;
                                    PanoramaModuleLGNormal.this.checkTakePictureByVoiceAssistant();
                                    return;
                                }
                                CamLog.m3d(CameraConstants.TAG, "preview callback update on ui thread return, mState : " + PanoramaModuleLGNormal.this.mState);
                            }
                        });
                    } else {
                        CamLog.m3d(CameraConstants.TAG, "preview callback drawing return, mState : " + PanoramaModuleLGNormal.this.mState);
                        return;
                    }
                } finally {
                    if (image != null) {
                        image.close();
                    }
                }
            } else {
                CamLog.m5e(CameraConstants.TAG, "CameraImageCallback-Preview : Image is null. error.");
            }
            if (image != null) {
                image.close();
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$12 */
    class C043612 implements CameraImageCallback {
        C043612() {
        }

        /* JADX WARNING: Missing block: B:18:0x0050, code:
            if (r19 == null) goto L_?;
     */
        /* JADX WARNING: Missing block: B:19:0x0052, code:
            r19.close();
     */
        /* JADX WARNING: Missing block: B:43:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:44:?, code:
            return;
     */
        public void onImageData(android.media.Image r19) {
            /*
            r18 = this;
            if (r19 == 0) goto L_0x0114;
        L_0x0002:
            r12 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.EngineSynchronizedOBJ;	 Catch:{ all -> 0x010d }
            monitor-enter(r12);	 Catch:{ all -> 0x010d }
            r10 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x010a }
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r2 = r2.mIsFeeding;	 Catch:{ all -> 0x010a }
            if (r2 == 0) goto L_0x0019;
        L_0x0011:
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r2 = r2.mPanorama;	 Catch:{ all -> 0x010a }
            if (r2 != 0) goto L_0x0035;
        L_0x0019:
            r2 = "CameraApp";
            r13 = "mIsFeeding is false, return picture callback.";
            com.lge.camera.util.CamLog.m11w(r2, r13);	 Catch:{ all -> 0x010a }
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r0 = r18;
            r13 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r14 = r13.mC_s;	 Catch:{ all -> 0x010a }
            r14 = r10 - r14;
            r2.mC_dur = r14;	 Catch:{ all -> 0x010a }
            monitor-exit(r12);	 Catch:{ all -> 0x010a }
            if (r19 == 0) goto L_0x0034;
        L_0x0031:
            r19.close();
        L_0x0034:
            return;
        L_0x0035:
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r2 = r2.mState;	 Catch:{ all -> 0x010a }
            r13 = 4;
            if (r2 != r13) goto L_0x0056;
        L_0x003e:
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r2 = r2.checkIdleMoving();	 Catch:{ all -> 0x010a }
            if (r2 != 0) goto L_0x0056;
        L_0x0048:
            r2 = "CameraApp";
            r13 = "STOP feedframe die to idle check during 10s.";
            com.lge.camera.util.CamLog.m11w(r2, r13);	 Catch:{ all -> 0x010a }
            monitor-exit(r12);	 Catch:{ all -> 0x010a }
            if (r19 == 0) goto L_0x0034;
        L_0x0052:
            r19.close();
            goto L_0x0034;
        L_0x0056:
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r14 = r2.mC_cnt;	 Catch:{ all -> 0x010a }
            r16 = 0;
            r2 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1));
            if (r2 != 0) goto L_0x0068;
        L_0x0062:
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r2.mC_s = r10;	 Catch:{ all -> 0x010a }
        L_0x0068:
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r14 = r2.mC_cnt;	 Catch:{ all -> 0x010a }
            r16 = 1;
            r14 = r14 + r16;
            r2.mC_cnt = r14;	 Catch:{ all -> 0x010a }
            r2 = "CameraApp";
            r13 = new java.lang.StringBuilder;	 Catch:{ all -> 0x010a }
            r13.<init>();	 Catch:{ all -> 0x010a }
            r14 = "mDirectFeedFrameCallback, image width : ";
            r13 = r13.append(r14);	 Catch:{ all -> 0x010a }
            r14 = r19.getWidth();	 Catch:{ all -> 0x010a }
            r13 = r13.append(r14);	 Catch:{ all -> 0x010a }
            r14 = ", height : ";
            r13 = r13.append(r14);	 Catch:{ all -> 0x010a }
            r14 = r19.getHeight();	 Catch:{ all -> 0x010a }
            r13 = r13.append(r14);	 Catch:{ all -> 0x010a }
            r13 = r13.toString();	 Catch:{ all -> 0x010a }
            com.lge.camera.util.CamLog.m7i(r2, r13);	 Catch:{ all -> 0x010a }
            r14 = r19.getTimestamp();	 Catch:{ all -> 0x010a }
            r14 = (double) r14;	 Catch:{ all -> 0x010a }
            r16 = 4696837146684686336; // 0x412e848000000000 float:0.0 double:1000000.0;
            r14 = r14 / r16;
            r8 = (long) r14;	 Catch:{ all -> 0x010a }
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r2.setFisrtTimestamp(r10, r8);	 Catch:{ all -> 0x010a }
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r6 = r2.getTimeStamp(r8);	 Catch:{ all -> 0x010a }
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r2.readSensorData();	 Catch:{ all -> 0x010a }
            r2 = r19.getPlanes();	 Catch:{ all -> 0x010a }
            r13 = 0;
            r2 = r2[r13];	 Catch:{ all -> 0x010a }
            r3 = r2.getBuffer();	 Catch:{ all -> 0x010a }
            r2 = r19.getPlanes();	 Catch:{ all -> 0x010a }
            r13 = 2;
            r2 = r2[r13];	 Catch:{ all -> 0x010a }
            r4 = r2.getBuffer();	 Catch:{ all -> 0x010a }
            r2 = r19.getPlanes();	 Catch:{ all -> 0x010a }
            r13 = 0;
            r2 = r2[r13];	 Catch:{ all -> 0x010a }
            r5 = r2.getRowStride();	 Catch:{ all -> 0x010a }
            r2 = r19.getWidth();	 Catch:{ all -> 0x010a }
            if (r2 != r5) goto L_0x0100;
        L_0x00e8:
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r2 = r2.mPanorama;	 Catch:{ all -> 0x010a }
            r2.feedFrame(r3, r4, r6);	 Catch:{ all -> 0x010a }
        L_0x00f1:
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r2.updatePanoramaPreview();	 Catch:{ all -> 0x010a }
            monitor-exit(r12);	 Catch:{ all -> 0x010a }
        L_0x00f9:
            if (r19 == 0) goto L_0x0034;
        L_0x00fb:
            r19.close();
            goto L_0x0034;
        L_0x0100:
            r0 = r18;
            r2 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x010a }
            r2 = r2.mPanorama;	 Catch:{ all -> 0x010a }
            r2.feedFrame(r3, r4, r5, r6);	 Catch:{ all -> 0x010a }
            goto L_0x00f1;
        L_0x010a:
            r2 = move-exception;
            monitor-exit(r12);	 Catch:{ all -> 0x010a }
            throw r2;	 Catch:{ all -> 0x010d }
        L_0x010d:
            r2 = move-exception;
            if (r19 == 0) goto L_0x0113;
        L_0x0110:
            r19.close();
        L_0x0113:
            throw r2;
        L_0x0114:
            r2 = "CameraApp";
            r12 = "CameraImageCallback-Full Frame: Image is null. error.";
            com.lge.camera.util.CamLog.m5e(r2, r12);	 Catch:{ all -> 0x010d }
            goto L_0x00f9;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.ext.PanoramaModuleLGNormal.12.onImageData(android.media.Image):void");
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$13 */
    class C043713 implements CameraImageMetaCallback {
        C043713() {
        }

        public void onImageMetaData(TotalCaptureResult result) {
            if (PanoramaModuleLGNormal.this.mPanoramaController != null) {
                PanoramaModuleLGNormal.this.mPanoramaController.onMetadataCallback(result, false);
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$14 */
    class C043814 implements CameraImageMetaCallback {
        C043814() {
        }

        public void onImageMetaData(TotalCaptureResult result) {
            if (PanoramaModuleLGNormal.this.mPanoramaController != null) {
                PanoramaModuleLGNormal.this.mPanoramaController.onMetadataCallback(result, true);
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$15 */
    class C043915 implements Runnable {
        C043915() {
        }

        public void run() {
            while (PanoramaModuleLGNormal.this.mIsFeeding) {
                PanoramaModuleLGNormal.this.mStart = System.currentTimeMillis();
                if (PanoramaModuleLGNormal.this.mState != 4 || PanoramaModuleLGNormal.this.checkIdleMoving()) {
                    if (PanoramaModuleLGNormal.this.isZeroCopy()) {
                        PanoramaModuleLGNormal.this.doQueueBuffer();
                    } else if (!(PanoramaModuleLGNormal.this.mQueueFrame == null || PanoramaModuleLGNormal.this.mQueueFrame.isEmpty())) {
                        FrameData fData = (FrameData) PanoramaModuleLGNormal.this.mQueueFrame.poll();
                        PanoramaModuleLGNormal.this.doFeedFrame(fData.mFrameData, fData.mTimeStamp);
                    }
                    PanoramaModuleLGNormal.this.mDur = System.currentTimeMillis() - PanoramaModuleLGNormal.this.mStart;
                    CamLog.m7i(CameraConstants.TAG, "startFeedFrameThread mDur : " + PanoramaModuleLGNormal.this.mDur);
                    if (PanoramaModuleLGNormal.this.mDur >= PanoramaModuleLGNormal.FRAME_DUR) {
                        PanoramaModuleLGNormal.this.mDur = 5;
                    } else {
                        PanoramaModuleLGNormal.this.mDur = Math.max(PanoramaModuleLGNormal.FRAME_DUR - PanoramaModuleLGNormal.this.mDur, 5);
                    }
                    try {
                        Thread.sleep(PanoramaModuleLGNormal.this.mDur);
                    } catch (InterruptedException e) {
                        CamLog.m11w(CameraConstants.TAG, "Feed frame thread interrupted.");
                    }
                } else {
                    CamLog.m11w(CameraConstants.TAG, "STOP feedframe die to idle check during 10s.");
                    return;
                }
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$19 */
    class C044319 implements SignatureCallbackInJava {
        C044319() {
        }

        public Bitmap setSignatureParam(int panowidth, int panoheight, int degree) {
            if (PanoramaModuleLGNormal.this.isSignatureEnableCondition()) {
                return PanoramaModuleLGNormal.this.mGet.getSignatureBitmap(panowidth, panoheight, degree);
            }
            return null;
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$3 */
    class C04473 implements AnimationListener {

        /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$3$1 */
        class C04481 implements AnimationListener {
            C04481() {
            }

            public void onAnimationStart(Animation animation) {
                PanoramaModuleLGNormal.this.mPreviewMiniLayout.setVisibility(8);
                PanoramaModuleLGNormal.this.mDrawingPanel.setVisibility(0);
            }

            public void onAnimationEnd(Animation animation) {
                PanoramaModuleLGNormal.this.findViewById(C0088R.id.panorama_preview_mini_border).setBackgroundResource(C0088R.drawable.panorama_white_box);
                PanoramaModuleLGNormal.this.mPreviewMiniLayout.setVisibility(0);
                PanoramaModuleLGNormal.this.mPreviewMiniLayoutArrow.setVisibility(0);
                if (PanoramaModuleLGNormal.this.mState != 5) {
                    CamLog.m7i(CameraConstants.TAG, "panorama is not taking. return");
                    PanoramaModuleLGNormal.this.mDrawingPanel.setVisibility(8);
                    return;
                }
                PanoramaModuleLGNormal.this.findViewById(C0088R.id.panorama_background_preview_layout).setVisibility(4);
                PanoramaModuleLGNormal.this.showPanoramaStopButton();
            }

            public void onAnimationRepeat(Animation animation) {
            }
        }

        C04473() {
        }

        public void onAnimationStart(Animation animation) {
            PanoramaModuleLGNormal.this.setVisibleArrowGuide(false, true, false, false);
            PanoramaModuleLGNormal.this.findViewById(C0088R.id.panorama_preview_mini_border).setBackground(null);
        }

        public void onAnimationEnd(Animation animation) {
            if (PanoramaModuleLGNormal.this.mState != 5) {
                CamLog.m7i(CameraConstants.TAG, "panorama is not taking. return");
                PanoramaModuleLGNormal.this.mPreviewMiniLayoutArrow.setVisibility(0);
                PanoramaModuleLGNormal.this.findViewById(C0088R.id.panorama_preview_mini_border).setBackgroundResource(C0088R.drawable.panorama_white_box);
                return;
            }
            AnimationUtil.startAlphaAnimation(PanoramaModuleLGNormal.this.findViewById(C0088R.id.panorama_background_preview_layout), 1.0f, 0.0f, 400, new C04481());
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$6 */
    class C04516 implements Runnable {
        C04516() {
        }

        public void run() {
            PanoramaModuleLGNormal.this.releaseEngine();
            PanoramaModuleLGNormal.this.mIsOnRecording = false;
            PanoramaModuleLGNormal.this.mState = 0;
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$7 */
    class C04527 implements DrawingPanelListener {
        C04527() {
        }

        public void onCompleted() {
            PanoramaModuleLGNormal.this.mGet.runOnUiThread(new HandlerRunnable(PanoramaModuleLGNormal.this) {
                public void handleRun() {
                    CamLog.m3d(CameraConstants.TAG, "onCompleted() stopPanorama.");
                    PanoramaModuleLGNormal.this.stopPanorama(true, false);
                }
            });
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$8 */
    class C04548 implements CameraPreviewDataCallback {
        C04548() {
        }

        public void onPreviewFrame(byte[] data, CameraProxy camera) {
            synchronized (PanoramaModuleCommonImpl.AddBufferSyncOBJ) {
                if (data == null) {
                    CamLog.m11w(CameraConstants.TAG, "PreviewCallback data is null.");
                } else if (PanoramaModuleLGNormal.this.mSkipPrevMiniCnt >= 1) {
                    PanoramaModuleLGNormal.this.mSkipPrevMiniCnt = 0;
                    final byte[] data_send = data;
                    PanoramaModuleLGNormal.this.mGet.runOnUiThread(new HandlerRunnable(PanoramaModuleLGNormal.this) {
                        public void handleRun() {
                            if (PanoramaModuleLGNormal.this.checkAvailableUpdatingPreview()) {
                                int imageW = PanoramaModuleLGNormal.this.mPreviewImageMini.getWidth();
                                int imageH = PanoramaModuleLGNormal.this.mPreviewImageMini.getHeight();
                                ColorConverter.yuvToRgbWithResize(PanoramaModuleLGNormal.this.mPreviewImageRGBBuffer, data_send, PanoramaModuleLGNormal.this.mPreviewW, PanoramaModuleLGNormal.this.mPreviewW, PanoramaModuleLGNormal.this.mPreviewH, imageW, imageH, 0);
                                PanoramaModuleLGNormal.this.mPreviewImageMini.copyPixelsFromBuffer(PanoramaModuleLGNormal.this.mPreviewImageRGBBuffer);
                                PanoramaModuleLGNormal.this.mPreviewMiniCanvas.drawBitmap(PanoramaModuleLGNormal.this.mPreviewImageMini, null, PanoramaModuleLGNormal.this.mRectPreviewMini, null);
                                PanoramaModuleLGNormal.this.mPreviewImageRGBBuffer.rewind();
                                PanoramaModuleLGNormal.this.mPreviewMini.invalidate();
                                PanoramaModuleLGNormal.this.mPrevDrawStarted = true;
                                PanoramaModuleLGNormal.this.checkTakePictureByVoiceAssistant();
                                return;
                            }
                            CamLog.m3d(CameraConstants.TAG, "preview callback drawing return, mState : " + PanoramaModuleLGNormal.this.mState);
                        }
                    });
                } else {
                    PanoramaModuleLGNormal panoramaModuleLGNormal = PanoramaModuleLGNormal.this;
                    panoramaModuleLGNormal.mSkipPrevMiniCnt++;
                }
                PanoramaModuleLGNormal.this.mCurBufferId = (PanoramaModuleLGNormal.this.mCurBufferId + 1) % PanoramaModuleCommonImpl.sBUFFER_SIZE;
                if (PanoramaModuleLGNormal.this.mDirectCallbackManager != null) {
                    PanoramaModuleLGNormal.this.mDirectCallbackManager.addCallbackBuffer(PanoramaModuleLGNormal.this.getPreviewBuffer());
                }
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGNormal$9 */
    class C04569 implements CameraPreviewDataCallback {
        C04569() {
        }

        /* JADX WARNING: Missing block: B:44:?, code:
            return;
     */
        public void onPreviewFrame(byte[] r13, com.lge.camera.device.CameraManager.CameraProxy r14) {
            /*
            r12 = this;
            r10 = 0;
            r2 = java.lang.System.currentTimeMillis();
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;
            r4.setFisrtTimestamp(r2, r2);
            r5 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.AddBufferSyncOBJ;
            monitor-enter(r5);
            if (r13 == 0) goto L_0x0111;
        L_0x0010:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mIsFeeding;	 Catch:{ all -> 0x00e6 }
            if (r4 != 0) goto L_0x0029;
        L_0x0016:
            r4 = "CameraApp";
            r6 = "mIsFeeding is false, return picture callback.";
            com.lge.camera.util.CamLog.m11w(r4, r6);	 Catch:{ all -> 0x00e6 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r6 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r6 = r6.mC_s;	 Catch:{ all -> 0x00e6 }
            r6 = r2 - r6;
            r4.mC_dur = r6;	 Catch:{ all -> 0x00e6 }
            monitor-exit(r5);	 Catch:{ all -> 0x00e6 }
        L_0x0028:
            return;
        L_0x0029:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mInputType;	 Catch:{ all -> 0x00e6 }
            if (r4 != 0) goto L_0x00b4;
        L_0x002f:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r6 = r4.mC_cnt;	 Catch:{ all -> 0x00e6 }
            r4 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1));
            if (r4 != 0) goto L_0x003b;
        L_0x0037:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4.mC_s = r2;	 Catch:{ all -> 0x00e6 }
        L_0x003b:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r6 = r4.mC_cnt;	 Catch:{ all -> 0x00e6 }
            r8 = 1;
            r6 = r6 + r8;
            r4.mC_cnt = r6;	 Catch:{ all -> 0x00e6 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.isZeroCopy();	 Catch:{ all -> 0x00e6 }
            if (r4 == 0) goto L_0x00e9;
        L_0x004c:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mFrameHashMap;	 Catch:{ all -> 0x00e6 }
            if (r4 == 0) goto L_0x00b4;
        L_0x0052:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mQueueBufferId;	 Catch:{ all -> 0x00e6 }
            if (r4 == 0) goto L_0x00b4;
        L_0x0058:
            r1 = r13.hashCode();	 Catch:{ all -> 0x00e6 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mFrameHashMap;	 Catch:{ all -> 0x00e6 }
            r6 = java.lang.Integer.valueOf(r1);	 Catch:{ all -> 0x00e6 }
            r4 = r4.containsKey(r6);	 Catch:{ all -> 0x00e6 }
            if (r4 == 0) goto L_0x00b4;
        L_0x006a:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mFrameHashMap;	 Catch:{ all -> 0x00e6 }
            r6 = java.lang.Integer.valueOf(r1);	 Catch:{ all -> 0x00e6 }
            r4 = r4.get(r6);	 Catch:{ all -> 0x00e6 }
            r4 = (java.lang.Integer) r4;	 Catch:{ all -> 0x00e6 }
            r0 = r4.intValue();	 Catch:{ all -> 0x00e6 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mQueueBufferId;	 Catch:{ all -> 0x00e6 }
            r6 = new com.lge.camera.app.ext.PanoramaModuleLGNormal$BufferId;	 Catch:{ all -> 0x00e6 }
            r7 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r6.<init>(r0, r2);	 Catch:{ all -> 0x00e6 }
            r4.add(r6);	 Catch:{ all -> 0x00e6 }
            r4 = "CameraApp";
            r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00e6 }
            r6.<init>();	 Catch:{ all -> 0x00e6 }
            r7 = "onPreviewFrame mQueueBufferId : ";
            r6 = r6.append(r7);	 Catch:{ all -> 0x00e6 }
            r6 = r6.append(r0);	 Catch:{ all -> 0x00e6 }
            r7 = ", mQueueBufferId size : ";
            r6 = r6.append(r7);	 Catch:{ all -> 0x00e6 }
            r7 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r7 = r7.mQueueBufferId;	 Catch:{ all -> 0x00e6 }
            r7 = r7.size();	 Catch:{ all -> 0x00e6 }
            r6 = r6.append(r7);	 Catch:{ all -> 0x00e6 }
            r6 = r6.toString();	 Catch:{ all -> 0x00e6 }
            com.lge.camera.util.CamLog.m3d(r4, r6);	 Catch:{ all -> 0x00e6 }
        L_0x00b4:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mDirectCallbackManager;	 Catch:{ all -> 0x00e6 }
            if (r4 == 0) goto L_0x00e3;
        L_0x00bc:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.isZeroCopy();	 Catch:{ all -> 0x00e6 }
            if (r4 == 0) goto L_0x0119;
        L_0x00c4:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mPanorama;	 Catch:{ all -> 0x00e6 }
            if (r4 == 0) goto L_0x0119;
        L_0x00ca:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mPanorama;	 Catch:{ all -> 0x00e6 }
            r0 = r4.dequeueBuffer();	 Catch:{ all -> 0x00e6 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mDirectCallbackManager;	 Catch:{ all -> 0x00e6 }
            r6 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r7 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.sBUFFER_SIZE;	 Catch:{ all -> 0x00e6 }
            r6 = r6.getRawBufferById(r0, r7);	 Catch:{ all -> 0x00e6 }
            r4.addCallbackBuffer(r6);	 Catch:{ all -> 0x00e6 }
        L_0x00e3:
            monitor-exit(r5);	 Catch:{ all -> 0x00e6 }
            goto L_0x0028;
        L_0x00e6:
            r4 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x00e6 }
            throw r4;
        L_0x00e9:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r6 = r4.mC_cnt;	 Catch:{ all -> 0x00e6 }
            r4 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1));
            if (r4 < 0) goto L_0x00b4;
        L_0x00f1:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mQueueFrame;	 Catch:{ all -> 0x00e6 }
            if (r4 == 0) goto L_0x00b4;
        L_0x00f7:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mQueueFrame;	 Catch:{ all -> 0x00e6 }
            r4 = r4.size();	 Catch:{ all -> 0x00e6 }
            r6 = 2;
            if (r4 > r6) goto L_0x00b4;
        L_0x0102:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mQueueFrame;	 Catch:{ all -> 0x00e6 }
            r6 = new com.lge.camera.app.ext.PanoramaModuleLGNormal$FrameData;	 Catch:{ all -> 0x00e6 }
            r7 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r6.<init>(r13, r2);	 Catch:{ all -> 0x00e6 }
            r4.add(r6);	 Catch:{ all -> 0x00e6 }
            goto L_0x00b4;
        L_0x0111:
            r4 = "CameraApp";
            r6 = "PreviewCallback data is null.";
            com.lge.camera.util.CamLog.m11w(r4, r6);	 Catch:{ all -> 0x00e6 }
            goto L_0x00b4;
        L_0x0119:
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r6 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r6 = r6.mCurBufferId;	 Catch:{ all -> 0x00e6 }
            r6 = r6 + 1;
            r7 = com.lge.camera.app.ext.PanoramaModuleCommonImpl.sBUFFER_SIZE;	 Catch:{ all -> 0x00e6 }
            r6 = r6 % r7;
            r4.mCurBufferId = r6;	 Catch:{ all -> 0x00e6 }
            r4 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r4 = r4.mDirectCallbackManager;	 Catch:{ all -> 0x00e6 }
            r6 = com.lge.camera.app.ext.PanoramaModuleLGNormal.this;	 Catch:{ all -> 0x00e6 }
            r6 = r6.getRawBuffer();	 Catch:{ all -> 0x00e6 }
            r4.addCallbackBuffer(r6);	 Catch:{ all -> 0x00e6 }
            goto L_0x00e3;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.ext.PanoramaModuleLGNormal.9.onPreviewFrame(byte[], com.lge.camera.device.CameraManager$CameraProxy):void");
        }
    }

    public class BufferId {
        public int mBufId = 0;
        public long mTimeStamp = 0;

        public BufferId(int id, long time) {
            this.mBufId = id;
            this.mTimeStamp = time;
        }
    }

    public class FrameData {
        public byte[] mFrameData = null;
        public long mTimeStamp = 0;

        public FrameData(byte[] data, long time) {
            this.mFrameData = data;
            this.mTimeStamp = time;
        }
    }

    public PanoramaModuleLGNormal(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void init() {
        super.init();
        this.mExecutor = Executors.newCachedThreadPool();
        this.mPrevDrawStarted = false;
    }

    protected void startPreview(CameraParameters params, SurfaceTexture surfaceTexture) {
        synchronized (LGSF_SYNC_OBJ) {
            if (FunctionProperties.getSupportedHal() == 2) {
                setCameraCallbackAll(-1, null, null, null);
            }
            if (this.mCameraDevice != null) {
                if (params == null) {
                    params = this.mCameraDevice.getParameters();
                }
                if (params != null) {
                    this.mViewAngleH = this.mCameraDevice.getHorizontalViewAngle(params);
                    this.mViewAngleV = this.mCameraDevice.getVerticalViewAngle(params);
                    CamLog.m3d(CameraConstants.TAG, "ViewAngle Horizontal : " + this.mViewAngleH + ", Vertival : " + this.mViewAngleV);
                    this.mMaxAngleRotationX = (double) (360.0f - (this.mViewAngleH / 2.0f));
                    this.mMaxAngleRotationY = (double) (360.0f - (this.mViewAngleV / 2.0f));
                    CamLog.m3d(CameraConstants.TAG, "Param angle H : " + this.mViewAngleH + ", w : " + this.mViewAngleV);
                    this.mFocalLength = this.mCameraDevice.getFocalLength(params);
                    CamLog.m3d(CameraConstants.TAG, "focal length : " + this.mFocalLength);
                    if (this.mInputType == 2 || this.mInputType == 1) {
                        params.set("raw_ppmask_enable", "1");
                    }
                }
            }
            super.startPreview(params, surfaceTexture);
        }
    }

    protected void checkStatusFlow(final int statusCode) {
        CamLog.m3d(CameraConstants.TAG, "checkStatusFlow : " + statusCode + ", mState : " + this.mState + ", mCaptureDirection : " + this.mCaptureDirection);
        switch (statusCode) {
            case 1:
            case 6:
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        CamLog.m3d(CameraConstants.TAG, "checkStatusFlow-[STATUS_WHOLE_AREA_COMPLETE]-stopPanorama statusCode : " + statusCode);
                        PanoramaModuleLGNormal.this.stopPanorama(true, statusCode == 6);
                    }
                });
                return;
            case 2:
                CamLog.m3d(CameraConstants.TAG, "NOTIFY engine saving completed. : please check doing in other thread Thread id : " + Thread.currentThread().getName());
                this.mWaitSavingCompleted = false;
                addImage();
                resetEngine();
                panoramaSaveAfter();
                return;
            case 4:
                if (this.mIsFeeding && this.mPanorama != null && this.mPanorama.isBusy()) {
                    resetPanoramaOnUiThread();
                    return;
                } else {
                    CamLog.m7i(CameraConstants.TAG, "Already panorama stopped.");
                    return;
                }
            case 5:
                if (this.mDrawingPanel == null) {
                    resetPanoramaOnUiThread();
                    return;
                }
                initFirstRotationMatrix();
                this.mDateTaken[0] = System.currentTimeMillis();
                int convertDirection = convertDirectionLG(this.mCaptureDirection);
                Size previewSize = (convertDirection == 21 || convertDirection == 22) ? this.mHoriPanPrevSize : this.mVertPanoPrevSize;
                this.mDrawingPanel.setPreviewParameters(this.mHoriPanPrevSize, this.mVertPanoPrevSize, convertDirection, this.mFrameSize, this.mFrameRotation, this.mBufferCalculator.getHorizontalPanoramaMaxLength(), previewSize, CameraConstants.MODE_PANORAMA_LG_360_PROJ.equals(getShotMode()));
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (PanoramaModuleLGNormal.this.mState != 4) {
                            CamLog.m11w(CameraConstants.TAG, "Started panorama handle - current mState : " + PanoramaModuleLGNormal.this.mState + " and return by wrong state.");
                            return;
                        }
                        PanoramaModuleLGNormal.this.startPanoTakingUI();
                        PanoramaModuleLGNormal.this.mState = 5;
                        CamLog.m3d(CameraConstants.TAG, "START Panorama - mState : " + PanoramaModuleLGNormal.this.mState);
                    }
                });
                return;
            default:
                return;
        }
    }

    private void startPanoTakingUI() {
        boolean z = true;
        View view = this.mPreviewMiniLayout;
        if (this.mCaptureDirection != 1) {
            z = false;
        }
        AnimationUtil.startPanoPreviewAnimation(view, z, new C04473());
    }

    protected void checkSpeedWarningStatusFlow(int statusCode) {
        if (this.mHandlerTaking != null) {
            int resId = 0;
            switch (statusCode) {
                case 120:
                    resId = C0088R.string.panorama_go_down;
                    break;
                case 121:
                    resId = C0088R.string.panorama_go_up;
                    break;
                case 122:
                case 123:
                    resId = C0088R.string.panorama_slow_down;
                    break;
                case 130:
                case 131:
                case 132:
                case 133:
                    resId = C0088R.string.panorama_slow_down;
                    break;
            }
            if (resId > 0) {
                Message msg = new Message();
                msg.arg1 = resId;
                msg.what = 1;
                this.mHandlerTaking.removeMessages(2);
                if (this.mPrevResId != resId) {
                    this.mHandlerTaking.sendEmptyMessage(2);
                    this.mHandlerTaking.sendMessage(msg);
                    this.mHandlerTaking.sendEmptyMessageDelayed(2, 1000);
                } else {
                    this.mHandlerTaking.sendMessage(msg);
                    this.mHandlerTaking.sendEmptyMessageDelayed(2, 1000);
                }
                this.mPrevResId = resId;
            }
        }
    }

    protected void checkStatusWarningError(int statusCode) {
        switch (statusCode) {
            case Panorama.STATUS_ERROR_OUT_OF_MEMORY /*401*/:
            case 500:
                CamLog.m5e(CameraConstants.TAG, "checkStatusWarningError - unknown or OOM.");
                resetPanoramaOnUiThread();
                return;
            case Panorama.STATUS_ERROR_ALIGNMENT_FAILURE /*402*/:
            case Panorama.STATUS_ERROR_WRONG_DIRECTION_UP /*410*/:
            case Panorama.STATUS_ERROR_WRONG_DIRECTION_DOWN /*411*/:
            case Panorama.STATUS_ERROR_WRONG_DIRECTION_LEFT /*412*/:
            case Panorama.STATUS_ERROR_WRONG_DIRECTION_RIGHT /*413*/:
            case Panorama.STATUS_ERROR_TOO_FAR_UP /*420*/:
            case Panorama.STATUS_ERROR_TOO_FAR_DOWN /*421*/:
            case Panorama.STATUS_ERROR_TOO_FAR_LEFT /*422*/:
            case Panorama.STATUS_ERROR_TOO_FAR_RIGHT /*423*/:
            case Panorama.STATUS_ERROR_TOO_FAST /*450*/:
                CamLog.m11w(CameraConstants.TAG, "checkStatusWarningError - direction and far error.");
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (PanoramaModuleLGNormal.this.mToastManager != null) {
                            PanoramaModuleLGNormal.this.mToastManager.showShortToast(PanoramaModuleLGNormal.this.getActivity().getResources().getString(C0088R.string.error_panorama_during_taking));
                        }
                    }
                });
                return;
            case Panorama.STATUS_ERROR_MOVEMENT_DETECTION_INCOMPLETE /*405*/:
                CamLog.m11w(CameraConstants.TAG, "checkStatusWarningError - alignment and incomplete.");
                resetPanoramaOnUiThread();
                return;
            case Panorama.STATUS_ERROR_SAVING_FAILURE /*430*/:
                if (this.mToastManager != null) {
                    this.mToastManager.showShortToast(getActivity().getString(C0088R.string.error_write_file));
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void startEngine() {
        CamLog.m3d(CameraConstants.TAG, "startEngine START");
        if (this.mCameraDevice == null) {
            CamLog.m3d(CameraConstants.TAG, "exit startEngine mCameraDevice=" + this.mCameraDevice);
        } else if (this.mState > 0) {
            CamLog.m3d(CameraConstants.TAG, "exit because already do startEngine");
        } else if (this.mPanorama != null) {
            CamLog.m11w(CameraConstants.TAG, "mPanorama is not null. Engine is working.");
        } else {
            boolean z;
            this.mState = 1;
            initPreviewPictureSize();
            if (isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
                z = false;
            } else {
                z = true;
            }
            createViews(z);
            this.mPanorama = new Panorama(this.mPanoramaListener);
            this.mPanorama.init();
            this.mPanorama.setGainCompensationEnabled(false);
            this.mPanorama.setEnableMinPathSeamFinder360(1);
            this.mPanorama.setJpegQualityFactor(92);
            this.mPanorama.setThumbnailJpegQualityFactor(80);
            this.mPanorama.setThumbnailMaximumWidth(512);
            this.mPanorama.setTooFastProportion(getCameraId() == 2 ? 0.015333333f : 0.023f);
            this.mPanorama.setCoarseAlignmentDownscaleFactor(16.0f);
            this.mPanorama.setCameraFrameDelay(getFrameDelay());
            this.mPanorama.setSensorSampleRate(90.0f);
            this.mPanorama.setSensorObservationWindowTime(2);
            setPanoramaMode360();
            CamLog.m7i(CameraConstants.TAG, "Panorama version : " + Panorama.getLibVersion() + ", ARM version : " + Panorama.getLibArchitecture());
            if (FunctionProperties.getSupportedHal() == 2) {
                setCameraCallbackAll(0, this.previewCbHal3, null, this.metaPrevCbHal3);
            } else {
                setCameraCallbackAll(getPreviewBuffer(), this.mPreviewMiniDirectCallback);
            }
            setVisiblePreviewMini(true, false);
            setVisibleArrowGuide(true, false, false, true);
            if (this.mFocusManager != null) {
                this.mFocusManager.stopAutoFocus(false);
                this.mFocusManager.hideAndCancelAllFocus(false);
                this.mFocusManager.registerCallback(0);
            }
            setCameraState(1);
            this.mState = 2;
            this.mCaptureDirection = 0;
            initFirstRotationMatrix();
            CamLog.m3d(CameraConstants.TAG, "startEngine END");
        }
    }

    protected void setPanoramaMode360() {
        if (this.mPanorama != null) {
            this.mPanorama.setMakePanorama360(0);
            this.mPanorama.setEnableCylindricalStripWarping(false);
            this.mPanorama.setMinimumAnglePanorama360(0.0f);
        }
    }

    public void stopEngine() {
        CamLog.m3d(CameraConstants.TAG, "stopEngine START");
        stopPanorama(false, false);
        if (!isProcessingFinishTask()) {
            releaseEngine();
        }
        this.mState = 0;
        if (FunctionProperties.getSupportedHal() == 2) {
            setCameraCallbackAll(-1, null, null, null);
        } else {
            setCameraCallbackAll(null, null);
        }
        this.mPrevDrawStarted = false;
        releaseByteBuffers(this.mPrevByteBuffer);
        this.mPrevByteBuffer = null;
        hide();
        CamLog.m3d(CameraConstants.TAG, "stopEngine END");
    }

    protected void resetEngine() {
        CamLog.m7i(CameraConstants.TAG, "resetEngine - start");
        if (!this.mShowDialog) {
            showProcessingDialog(false, 0);
        }
        if (isPaused()) {
            this.mGet.getActivity().runOnUiThread(new C04516());
        } else {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    PanoramaModuleLGNormal.this.resetToPreviewState();
                }
            });
        }
    }

    protected void resetToPreviewState() {
        CamLog.m3d(CameraConstants.TAG, "resetToPreviewState");
        releaseEngine();
        super.resetToPreviewState();
    }

    public void releaseEngine() {
        CamLog.m3d(CameraConstants.TAG, "release Engine start");
        synchronized (EngineSynchronizedOBJ) {
            if (this.mPanorama != null) {
                this.mPanorama.release();
                this.mPanorama = null;
            }
        }
    }

    protected void doSwitchCamera() {
        stopEngine();
        super.doSwitchCamera();
    }

    public void startPanorama() {
        CamLog.m3d(CameraConstants.TAG, "startPanorama()");
        if (this.mCameraDevice == null || this.mPanorama == null || isProcessingFinishTask() || this.mState != 2 || this.mSnapShotChecker.getSnapShotState() > 2 || this.mPanorama.isBusy() || isPaused()) {
            CamLog.m3d(CameraConstants.TAG, "exit mStatusShot : " + this.mState + " isProcessing : " + isProcessingFinishTask());
            return;
        }
        setCameraState(3);
        this.mSnapShotChecker.setSnapShotState(3);
        this.mTempParams = null;
        this.mSaveThread = null;
        this.mCurBufferId = 0;
        resetTimestamps();
        this.mSensorEventListener.resetTimestamps();
        startPanoramaExtraProceed();
    }

    public void startPanoramaExtraProceed() {
        startPanoAfterTakePicture();
    }

    public void startPanoAfterTakePicture() {
        if (isPaused()) {
            CamLog.m11w(CameraConstants.TAG, "startPanoAftertakePicture, activity is paused. return.");
            return;
        }
        setFingerDetectionListener(false);
        if (FunctionProperties.getSupportedHal() == 2) {
            setCameraCallbackAll(-1, null, null, null);
        } else {
            setCameraCallbackAll(null, null);
        }
        this.mPrevDrawStarted = false;
        if (this.mFocusManager != null) {
            this.mFocusManager.stopAutoFocus(false);
        }
        if (this.mCameraDevice != null) {
            setStartPanoramaParams(this.mCameraDevice.getParameters());
        }
        if (!(isFastShotAvailable(3) || this.mCameraDevice == null)) {
            this.mCameraDevice.setLongshot(false);
        }
        if (!ModelProperties.isFakeExif()) {
            String str;
            if ("".equals(SystemProperties.get("ro.product.model"))) {
                str = Build.MODEL;
            } else {
                str = SystemProperties.get("ro.product.model");
            }
            this.mModelName = str;
        }
        setSpeedWarningLayout();
        doStartPanoramaJob();
    }

    protected void setStartPanoramaParams(CameraParameters parameters) {
        if (parameters == null || this.mCameraDevice == null) {
            CamLog.m11w(CameraConstants.TAG, "Device or panorama is null. mCameraDevice : " + this.mCameraDevice);
            return;
        }
        if (FunctionProperties.isSupportedFilmEmulator()) {
            parameters.set(ParamConstants.KEY_FILM_ENABLE, "false");
        }
        activateFingerDetection(false, parameters, false);
        parameters.setFlashMode("off");
        parameters.setFocusMode("auto");
        parameters.setAutoExposureLock(true);
        parameters.setAutoWhiteBalanceLock(true);
        this.mCameraDevice.setFaceDetectionCallback(this.mHandler, null);
        this.mCameraDevice.setAutoFocusMoveCallback(this.mHandler, null);
        if (this.mInputType == 0) {
            this.mCameraDevice.stopPreview();
            String prevSize = ModelProperties.getPanoramaPreviewSize(false);
            CamLog.m3d(CameraConstants.TAG, "INPUT_TYPE_PREVIEW prevSize : " + prevSize);
            int[] preSizeInt = Utils.sizeStringToArray(prevSize);
            this.mFrameSize = new Size(preSizeInt[0], preSizeInt[1]);
            parameters.set("frame_repeating_enable", "2");
            parameters.set(ParamConstants.KEY_PREVIEW_SIZE, prevSize);
            this.mCameraDevice.setParameters(parameters);
            this.mCameraDevice.startPreview();
            if (FunctionProperties.getSupportedHal() != 2) {
                createRawBuffer((int) (((double) (preSizeInt[0] * preSizeInt[1])) * 1.5d), sBUFFER_SIZE);
            }
        } else if (this.mInputType == 1 || this.mInputType == 2) {
            Size pictureSize = parameters.getPictureSize();
            this.mFrameSize = new Size(pictureSize.getWidth(), pictureSize.getHeight());
            parameters.set("frame_repeating_enable", "1");
            parameters.set("raw_ppmask_enable", "1");
            this.mCameraDevice.setParameters(parameters);
            if (FunctionProperties.getSupportedHal() != 2) {
                int rawBufferW = this.mFrameSize.getWidth();
                int rawBufferH = this.mFrameSize.getHeight();
                if (this.mInputType == 1) {
                    this.mRawBufSize = (int) (((double) (rawBufferW * rawBufferH)) * 1.5d);
                } else if (this.mInputType == 2) {
                    this.mStride = (rawBufferW + 63) & -64;
                    this.mScanline = (rawBufferH + 63) & -64;
                    this.mRawBufSize = (((((this.mStride * this.mScanline) + 63) & -64) + ((((this.mStride * this.mScanline) / 2) + 63) & -64)) + 4095) & -4096;
                    CamLog.m3d(CameraConstants.TAG, "Raw buffer size mStride : " + this.mStride + ", mScanline : " + this.mScanline + ", Buffer size : " + this.mRawBufSize);
                }
                createRawBuffer(this.mRawBufSize, sBUFFER_SIZE);
            }
        }
        CamLog.m3d(CameraConstants.TAG, "frame(input) size = " + this.mFrameSize);
    }

    protected void doStartPanoramaJob() {
        if (this.mCameraDevice == null || this.mReviewThumbnailManager == null || this.mQuickButtonManager == null || this.mCaptureButtonManager == null || this.mDotIndicatorManager == null) {
            CamLog.m3d(CameraConstants.TAG, "exit doStartPanoramaJob by null object");
            stopPanorama(false, false);
            this.mSnapShotChecker.releaseSnapShotChecker();
            resetToPreviewState();
            return;
        }
        this.mQuickButtonManager.hide(true, false, false);
        this.mDotIndicatorManager.hide();
        access$400(CameraConstants.MENU_TYPE_ALL, false, true);
        this.mReviewThumbnailManager.setThumbnailVisibility(4);
        this.mCaptureButtonManager.changeButtonByMode(7);
        this.mCaptureButtonManager.setShutterButtonEnable(false, getShutterButtonType());
        if (FunctionProperties.isSupportedConeUI()) {
            this.mGet.enableConeMenuIcon(31, false);
        }
        keepScreenOn();
        initDebugFps();
        initFrameDebug();
        this.mDirectory = getCurDir();
        this.mFileName = makeFileName(0, getCurStorage(), getCurDir(), false, CameraConstants.MODE_PANORAMA);
        this.mFileNameWithExt = getPanoramaFileName(false) + ".jpg";
        this.mOutputFileName = getPanoramaFileDir(false) + this.mFileNameWithExt;
        this.mIdleCheckTime = System.currentTimeMillis();
        initFirstRotationMatrix();
        capturePanorama();
        this.mGet.playSound(3, true, 0);
        this.mState = 4;
        showGuideText(true);
        setQuickClipIcon(true, false);
        setVisibleArrowGuide(true, true, true, true);
        doPanoramaJobStartAfter();
    }

    protected void capturePanorama() {
        CamLog.m7i(CameraConstants.TAG, "capture panorama - start");
        if (FunctionProperties.getSupportedHal() == 2) {
            this.mPanoramaController.quickenExposureTimeConstantBrightness();
            setFilterParameter((int) ((long) this.mPanoramaController.getSensorSensitivity()));
            setPanoramaEngineParam();
            startFeedFrameThread(true);
            setCameraCallbackAll(1, null, this.fullFrameCbHal3, this.metaFullFrameCbHal3);
            return;
        }
        setPanoramaEngineParam();
        startFeedFrameThread(false);
        Camera camera = (Camera) this.mCameraDevice.getCamera();
        if (camera == null) {
            CamLog.m5e(CameraConstants.TAG, "Hardware camera is null. error.");
        } else if (this.mInputType == 0 && this.mDirectCallbackManager != null) {
            if (isZeroCopy()) {
                this.mDirectCallbackManager.addCallbackBuffer(getRawBufferById(this.mPanorama.dequeueBuffer(), sBUFFER_SIZE));
            } else {
                this.mDirectCallbackManager.addCallbackBuffer(getRawBuffer());
            }
            this.mDirectCallbackManager.setPreviewDataCallbackWithBuffer(this.mPreviewFeedCallback);
        } else if (this.mInputType == 1 || this.mInputType == 2) {
            if (isZeroCopy()) {
                camera.addRawImageCallbackBuffer(getRawBufferById(this.mPanorama.dequeueBuffer(), sBUFFER_SIZE));
            } else {
                camera.addRawImageCallbackBuffer(getRawBuffer());
            }
            camera.takePicture(null, this.mRawCallback, null);
        }
    }

    protected void setPanoramaEngineParam() {
        if (this.mCameraDevice == null) {
            CamLog.m11w(CameraConstants.TAG, "Camera device is null, setPanoramaEngineParam return.");
            return;
        }
        DisplayMetrics dm = Utils.getWindowRealMatics(getActivity());
        int previewSmallerLength = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 264.0f);
        if (getCameraId() == 2) {
            previewSmallerLength = (int) (((float) previewSmallerLength) * 1.72f);
        }
        int horiPanPrevSizeWidth = dm.widthPixels - (Utils.getPx(getAppContext(), C0088R.dimen.panorama_bar_side_margin) * 2);
        CamLog.m3d(CameraConstants.TAG, "horiPanPrevSizeWidth : " + horiPanPrevSizeWidth + ", previewSmallerLength : " + previewSmallerLength);
        this.mHoriPanPrevSize = new Size(horiPanPrevSizeWidth, previewSmallerLength);
        CamLog.m3d(CameraConstants.TAG, "dm.heightPixels : " + dm.heightPixels + ", previewSmallerLength : " + previewSmallerLength);
        this.mVertPanoPrevSize = new Size(previewSmallerLength, dm.heightPixels);
        this.mPanoramaMaxLength = (int) (((360.0d - ((double) (this.mViewAngleH / 2.0f))) * ((double) this.mFrameSize.getWidth())) / ((double) this.mViewAngleH));
        CamLog.m7i(CameraConstants.TAG, "mPanoramaMaxLength : " + this.mPanoramaMaxLength);
        this.mFrameRotation = 90;
        int cameraSensorRotationDegrees = this.mCameraDevice.getCameraInfo().getCameraOrientation();
        CamLog.m3d(CameraConstants.TAG, "cameraSensorRotationDegrees : " + cameraSensorRotationDegrees);
        int sensorActiveArrayWidth = 0;
        int sensorActiveArrayHeight = 0;
        this.mTempParams = this.mCameraDevice.getParameters();
        if (this.mTempParams != null) {
            Size sensorArraySize = this.mCameraDevice.getSensorActiveArrarySize(this.mTempParams, ModelProperties.isMTKChipset());
            sensorActiveArrayWidth = sensorArraySize.getWidth();
            sensorActiveArrayHeight = sensorArraySize.getHeight();
            CamLog.m3d(CameraConstants.TAG, "sensorActiveArrayWidth : " + sensorActiveArrayWidth + ", sensorActiveArrayHeight : " + sensorActiveArrayHeight);
        }
        SizeF sensorPhysicalSize = this.mCameraDevice.getSensorPhysicalSize(this.mFocalLength, this.mViewAngleH, this.mViewAngleV);
        float sensorWidth = sensorPhysicalSize.getWidth();
        float sensorHeight = sensorPhysicalSize.getHeight();
        if (FunctionProperties.getSupportedHal() != 2 && getCameraId() == 2) {
            float sensorArrayRatio = sensorActiveArrayHeight > 0 ? ((float) sensorActiveArrayWidth) / ((float) sensorActiveArrayHeight) : 1.3333334f;
            float sensorPhysicalRatio = sensorWidth / sensorHeight;
            if (Float.compare(sensorArrayRatio, sensorPhysicalRatio) == -1) {
                sensorWidth = sensorHeight * sensorArrayRatio;
                sensorHeight = sensorWidth / sensorPhysicalRatio;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "sensorWidth : " + sensorWidth + ", sensorHeight : " + sensorHeight);
        setWideDistCorrectionValue(sensorActiveArrayWidth, sensorActiveArrayHeight);
        calcPanoramaBuffer(this.mFocalLength, 0.0f, sensorWidth, sensorHeight, 0.3f);
        float fNumber = this.mCameraDevice.getCameraFnumber(this.mTempParams);
        float maxAperture = fNumber;
        int iso = this.mPanoramaController.getSensorSensitivity();
        long exposureTimeNs = this.mPanoramaController.getExposureTime();
        CamLog.m7i(CameraConstants.TAG, "setPanoramaEngineParam, fNumber : " + fNumber + ", iso : " + iso + ", exposureTimeNs : " + exposureTimeNs);
        this.mPanorama.begin(this.mOutputFileName, this.mFrameSize, this.mBufferCalculator.getHorizontalPanoramaMaxLength(), this.mBufferCalculator.getVerticalPanoramaMaxLength(), this.mBufferCalculator.getmMaximumRotationAngleCoverage(), this.mFrameRotation, cameraSensorRotationDegrees, this.mHoriPanPrevSize, this.mVertPanoPrevSize, Exif.EXIF_STR_MAKE, this.mModelName, this.mFocalLength, fNumber, maxAperture, iso, exposureTimeNs, 0, sensorWidth, sensorHeight, sensorActiveArrayWidth, sensorActiveArrayHeight, 0.0f, 0.0f, 0.0f, false, this.mLensCorrectionMode, this.mPrincipal_point, this.mUndistort_matrix, this.mNRmode, this.mEdgeEnhancementMode, this.mTrackingMode, isZeroCopy() ? this.mByteBufferArray : null, isZeroCopy() ? sBUFFER_SIZE : 0, isZeroCopy() ? this.mByteBufferArray[0].arrayOffset() : 0);
        setSignatureUse();
    }

    public void stopPanorama(boolean needSaving, boolean stop360) {
        CamLog.m3d(CameraConstants.TAG, "stopPanorama START mState : " + this.mState + ", needSaving : " + needSaving + ", stop360 : " + stop360);
        stopFeedFrameThread();
        stopPanoramabyEngine(!needSaving, stop360);
        this.mDateTaken[1] = System.currentTimeMillis();
        synchronized (EngineSynchronizedOBJ) {
            CamLog.m3d(CameraConstants.TAG, "stopPanorama engine sync mState : " + this.mState);
            if (this.mState >= 6) {
                CamLog.m3d(CameraConstants.TAG, "stopPanorama exit by mState=" + this.mState + ", mCameraState=" + this.mCameraState);
            } else if (isProcessingFinishTask() || this.mCameraState != 3) {
                CamLog.m3d(CameraConstants.TAG, "stopPanorama exit by mCameraState=" + this.mCameraState + ", mState=" + this.mState);
                releaseByteBuffers(this.mRawByteBuffer);
                this.mRawByteBuffer = null;
                releaseByteBuffers(this.mByteBufferArray);
                this.mByteBufferArray = null;
            } else {
                boolean isNeedSaving = false;
                if (needSaving) {
                    isNeedSaving = !isPaused();
                    if (isNeedSaving && !this.mShowDialog) {
                        showProcessingDialog(true, 0);
                    }
                }
                stopPanoramaBefore(isNeedSaving);
                if (this.mDrawingPanel != null) {
                    this.mDrawingPanel.setVisibility(4);
                }
                doStopPanoramaJob(isNeedSaving);
                releaseByteBuffers(this.mByteBufferArray);
                this.mByteBufferArray = null;
                releaseFrameHashMap();
                releaseBackupFrame();
                CamLog.m3d(CameraConstants.TAG, "stopPanorama END");
            }
        }
    }

    protected void calcPanoramaBuffer(float focalLength, float focalDistance, float sensorWidth, float sensorHeight, float armDistance) {
        if (this.mTrackingMode == 1) {
            this.mBufferCalculator.setBufferCalculator(this.mViewAngleH, this.mViewAngleV, this.mFrameSize, sensorWidth, sensorHeight, focalLength, focalDistance, 360, 0.0f, false);
        } else {
            this.mBufferCalculator.setBufferCalculator(this.mViewAngleH, this.mViewAngleV, this.mFrameSize, sensorWidth, sensorHeight, focalLength, focalDistance, 360, armDistance, false);
        }
    }

    protected void setDrawingPanelView() {
        this.mDrawingPanel = (DrawingPanel) this.mBaseView.findViewById(C0088R.id.preview_drawingpanel);
        this.mDrawingPanel.setListener(new C04527());
        this.mDrawingPanel.setArrowRes(C0088R.drawable.panorama_guide_arrow_right, C0088R.drawable.panorama_guide_arrow_left, C0088R.drawable.panorama_guide_arrow_up, C0088R.drawable.panorama_guide_arrow_down);
    }

    private void checkTakePictureByVoiceAssistant() {
        if (this.mGet.isVoiceAssistantSpecified() && !this.mGet.getAssistantBoolFlag(CameraConstantsEx.FLAG_CAMERA_OPEN_ONLY, true)) {
            this.mGet.setAssistantFlag(CameraConstantsEx.FLAG_CAMERA_OPEN_ONLY, Boolean.valueOf(onCameraShutterButtonClicked()));
        }
    }

    protected void startFeedFrameThread(boolean onlySetFeeding) {
        this.mIsFeeding = true;
        if (onlySetFeeding) {
            CamLog.m7i(CameraConstants.TAG, "startFeedFrameThread : only set Feeding flag. return.");
        } else {
            this.mExecutor.submit(new C043915());
        }
    }

    protected void stopFeedFrameThread() {
        this.mIsFeeding = false;
        this.mF_dur = System.currentTimeMillis() - this.mF_s;
        if (this.mQueueFrame != null) {
            this.mQueueFrame.clear();
        }
        if (this.mQueueBufferId != null) {
            this.mQueueBufferId.clear();
        }
        this.mCurBufferId = 0;
        this.mIdleCheckTime = 0;
        saveAllFrameDebug();
    }

    protected boolean doFeedFrame(byte[] data, long imageTimeStamp) {
        if (data == null || !this.mIsFeeding) {
            CamLog.m3d(CameraConstants.TAG, "skip feeding : " + data);
            return false;
        }
        if (this.mF_cnt == 0) {
            this.mF_s = System.currentTimeMillis();
        }
        this.mF_cnt++;
        readSensorData();
        synchronized (PanoPreviewSyncOBJ) {
            long timestamp = getTimeStamp(imageTimeStamp);
            Trace.beginSection("pre doFeedFrame");
            if (this.mInputType == 0 || this.mInputType == 1) {
                this.mPanorama.feedFrame(data, timestamp);
            } else if (this.mInputType == 2) {
                this.mPanorama.feedFrame(data, this.mStride, this.mScanline, timestamp);
            }
            Trace.endSection();
        }
        updatePanoramaPreview();
        return true;
    }

    protected boolean doQueueBuffer() {
        if (this.mQueueBufferId == null || this.mQueueBufferId.isEmpty()) {
            return false;
        }
        if (this.mF_cnt == 0) {
            this.mF_s = System.currentTimeMillis();
        }
        this.mF_cnt++;
        BufferId bufId = (BufferId) this.mQueueBufferId.poll();
        int bufferId = bufId.mBufId;
        readSensorData();
        long timestamp = getTimeStamp(bufId.mTimeStamp);
        synchronized (PanoPreviewSyncOBJ) {
            this.mPanorama.queueBuffer(bufferId, timestamp);
        }
        updatePanoramaPreview();
        CamLog.m7i(CameraConstants.TAG, "startFeedFrameThread mPanorama.queueBuffer bufferId : " + bufferId);
        return true;
    }

    protected void updatePanoramaPreview() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (!PanoramaModuleLGNormal.this.isPaused() && PanoramaModuleLGNormal.this.mIsFeeding && PanoramaModuleLGNormal.this.mState == 5 && PanoramaModuleLGNormal.this.mDrawingPanel != null && PanoramaModuleLGNormal.this.mPanorama != null) {
                    CamLog.m3d(CameraConstants.TAG, "updatePanoramaPreview");
                    Bitmap bitmap = PanoramaModuleLGNormal.this.mDrawingPanel.getPanoPrevBitmap();
                    synchronized (PanoramaModuleCommonImpl.PanoPreviewSyncOBJ) {
                        PanoramaModuleLGNormal.this.mPanorama.fillBuffer(bitmap, PanoramaModuleLGNormal.this.mLastPos);
                    }
                    PanoramaModuleLGNormal.this.mDrawingPanel.setArrowPos(PanoramaModuleLGNormal.this.mLastPos[0], PanoramaModuleLGNormal.this.mLastPos[1]);
                    PanoramaModuleLGNormal.this.mDrawingPanel.invalidate();
                }
            }
        });
    }

    protected void initPreviewPictureSize() {
        super.initPreviewPictureSize();
        createPreviewBuffer((int) (((double) (this.mPreviewW * this.mPreviewH)) * 1.5d), sBUFFER_SIZE);
        this.mQueueFrame = new ConcurrentLinkedQueue();
        this.mQueueBufferId = new ConcurrentLinkedQueue();
    }

    protected void resetPanoramaOnUiThread() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                CamLog.m3d(CameraConstants.TAG, "resetPanoramaOnUiThread-send on UI thread");
                PanoramaModuleLGNormal.this.mToastManager.showShortToast(PanoramaModuleLGNormal.this.getActivity().getResources().getString(C0088R.string.error_panorama_during_taking));
                PanoramaModuleLGNormal.this.stopPanorama(false, false);
                PanoramaModuleLGNormal.this.mSnapShotChecker.releaseSnapShotChecker();
                PanoramaModuleLGNormal.this.resetToPreviewState();
            }
        });
    }

    protected int addImage() {
        this.mSavingTime = System.currentTimeMillis() - this.mSavingTime;
        CamLog.m3d(CameraConstants.TAG, "addImage saving time : " + this.mSavingTime);
        CamLog.m3d(CameraConstants.TAG, "addImage output image path : " + this.mOutputFileName);
        SupportedExif supportedExif = new SupportedExif(true, this.mCameraCapabilities.isFlashSupported(), this.mCameraCapabilities.isWBSupported(), this.mCameraCapabilities.isMeteringAreaSupported(), this.mCameraCapabilities.isZoomSupported(), true, true, true);
        ExifInterface exif = readExif();
        int degree = Exif.getOrientation(exif);
        int[] exifSize = setExifSize(exif);
        if (exifSize[0] == 0 || exifSize[1] == 0) {
            exifSize = Exif.getImageSize(exif);
            CamLog.m3d(CameraConstants.TAG, "exifSize[0] : " + exifSize[0] + ", exifSize[1] : " + exifSize[1]);
        }
        try {
            Exif.createExif(exif, this.mOutputFileName, exifSize[0], exifSize[1], this.mTempParams, getCurrentLocation(), degree, -1, supportedExif, (short) 10).forceRewriteExif(this.mOutputFileName);
            Uri resultUri = addXMPMetaData(getPanoramaFileName(false), this.mDateTaken[1], getPanoramaFileDir(false), this.mOutputFileName, degree, exifSize);
            int modeColume = 1;
            if (CameraConstants.MODE_PANORAMA_LG_360_PROJ.equals(getShotMode())) {
                modeColume = 100;
            }
            this.mGet.onNewItemAdded(resultUri, modeColume, null);
            SharedPreferenceUtil.saveLastPicture(getActivity(), resultUri);
            final Uri uri = resultUri;
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    PanoramaModuleLGNormal.this.mReviewThumbnailManager.updateThumbnail(uri, false, false, true, false);
                }
            });
            this.mGet.requestNotifyNewMediaonActivity(resultUri, true);
            this.mTotalSavingTime = System.currentTimeMillis() - this.mTotalSavingTime;
            CamLog.m3d(CameraConstants.TAG, "addImage - end");
            return 0;
        } catch (FileNotFoundException e) {
            CamLog.m5e(CameraConstants.TAG, "Exif force rewrite fail. FileNotFoundException.");
            return 4;
        } catch (IOException e2) {
            CamLog.m5e(CameraConstants.TAG, "Exif force rewrite fail. IOException.");
            return 4;
        }
    }

    protected Uri addXMPMetaData(String file_name, long dateTaken, String directory, String output_img_path, int degree, int[] exifSize) {
        return FileManager.registerImageUri(getActivity().getContentResolver(), this.mDirectory, this.mFileNameWithExt, this.mDateTaken[1], getCurrentLocation(), degree, exifSize, false);
    }

    protected void setFilterParameter(int curISO) {
        if (curISO <= 400) {
            this.mNRmode = 0;
            this.mEdgeEnhancementMode = 0;
        } else if (curISO > 400 && curISO <= 600) {
            this.mNRmode = 1;
        } else if (curISO > 600) {
            this.mNRmode = 2;
        }
        CamLog.m7i(CameraConstants.TAG, "curISO is : " + curISO + ", Filter NR : " + this.mNRmode + ", Sharpen : " + this.mEdgeEnhancementMode);
    }

    protected void setSignaturePanorama(int oriImageWidth, int oriImageHeight, int degree) {
    }

    public void setSignatureUse() {
        if (this.mPanorama == null) {
            CamLog.m7i(CameraConstants.TAG, "setSignatureUse, mPanorama is null.");
        } else if (FunctionProperties.isSignatureSupported(getAppContext())) {
            String signature = this.mGet.getCurSettingValue(Setting.KEY_SIGNATURE);
            if ("on".equals(signature)) {
                CamLog.m3d(CameraConstants.TAG, "SignatureUse : " + signature);
                this.mPanorama.setSignatureCallback(new C044319());
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "Signature is off.");
            this.mPanorama.setSignatureCallback(null);
        } else {
            CamLog.m7i(CameraConstants.TAG, "setSignatureUse, mPanorama is null or signature is not supported.");
            this.mPanorama.setSignatureCallback(null);
        }
    }

    protected void stopPanoramaBefore(final boolean isNeedSaving) {
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (isNeedSaving) {
                    PanoramaModuleLGNormal.this.mGet.playSound(3, false, 0);
                }
            }
        });
    }
}
