package com.lge.camera.managers;

import android.content.ContentResolver;
import android.location.Location;
import android.media.Image;
import android.media.MediaCodec.CodecException;
import android.os.Handler;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.managers.LivePhotoEncoder.LivePhotoEncoderListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class LivePhotoManagerBase extends ManagerInterfaceImpl {
    public static final int LIVE_PHOTO_CAMERA_TYPE_FRONT = 1;
    public static final int LIVE_PHOTO_CAMERA_TYPE_FRONT_FLIP = 2;
    public static final int LIVE_PHOTO_CAMERA_TYPE_REAR = 0;
    protected final int FPS = 15;
    protected final int KEY_FRAME_PER_SEC = 7;
    protected int[] mFrameSize;
    protected boolean mIsFrontCamera = false;
    protected boolean mIsLivePhotoEnabled;
    protected boolean mIsSavingProgress = false;
    protected long mLastPrevCallbackTime;
    protected Object mLinkedListLock = new Object();
    protected LinkedList<LivePhoto> mLivePhotoList;
    protected int mLogCount = 0;
    protected LinkedList<byte[]> mPrev2SecBuffer;
    protected Handler mPreviewCallbackHandler;
    protected boolean mWindowFocus = true;

    protected class LivePhoto {
        private final int MAX_SIZE;
        private final String TEMP_FILE_POSTFIX = "_temp";
        private LinkedList<byte[]> m3SecBuffer = new LinkedList();
        private int mCameraType = 0;
        private int mDeviceDegree;
        private LivePhotoEncoder mEncoder;
        private int mFrameCount = 0;
        private String mImageFileName;
        private ImageUriInfo mImageUriInfo;
        private byte[] mJpegData;
        private LivePhotoEncoderListener mListener = new C10462();
        private String mSavedFileName;

        /* renamed from: com.lge.camera.managers.LivePhotoManagerBase$LivePhoto$2 */
        class C10462 implements LivePhotoEncoderListener {
            C10462() {
            }

            public void onError(CodecException e) {
                if (LivePhoto.this.mEncoder != null) {
                    LivePhoto.this.mEncoder.close();
                    LivePhoto.this.mEncoder = null;
                }
            }

            /* JADX WARNING: Removed duplicated region for block: B:19:0x00ea  */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0175 A:{SYNTHETIC, Splitter: B:42:0x0175} */
            /* JADX WARNING: Removed duplicated region for block: B:45:0x017a A:{SYNTHETIC, Splitter: B:45:0x017a} */
            /* JADX WARNING: Removed duplicated region for block: B:30:0x013e A:{SYNTHETIC, Splitter: B:30:0x013e} */
            /* JADX WARNING: Removed duplicated region for block: B:33:0x0143 A:{SYNTHETIC, Splitter: B:33:0x0143} */
            /* JADX WARNING: Removed duplicated region for block: B:19:0x00ea  */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x0175 A:{SYNTHETIC, Splitter: B:42:0x0175} */
            /* JADX WARNING: Removed duplicated region for block: B:45:0x017a A:{SYNTHETIC, Splitter: B:45:0x017a} */
            /* JADX WARNING: Removed duplicated region for block: B:30:0x013e A:{SYNTHETIC, Splitter: B:30:0x013e} */
            /* JADX WARNING: Removed duplicated region for block: B:33:0x0143 A:{SYNTHETIC, Splitter: B:33:0x0143} */
            /* JADX WARNING: Removed duplicated region for block: B:19:0x00ea  */
            public void onEncodeAfter(java.lang.String r23) {
                /*
                r22 = this;
                r16 = 0;
                r9 = 0;
                r3 = "CameraApp";
                r4 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x012f }
                r4.<init>();	 Catch:{ IOException -> 0x012f }
                r5 = "-Live Photo- videoFileName = ";
                r4 = r4.append(r5);	 Catch:{ IOException -> 0x012f }
                r0 = r23;
                r4 = r4.append(r0);	 Catch:{ IOException -> 0x012f }
                r5 = ".mp4";
                r4 = r4.append(r5);	 Catch:{ IOException -> 0x012f }
                r4 = r4.toString();	 Catch:{ IOException -> 0x012f }
                com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ IOException -> 0x012f }
                r15 = new java.io.File;	 Catch:{ IOException -> 0x012f }
                r3 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x012f }
                r3.<init>();	 Catch:{ IOException -> 0x012f }
                r0 = r23;
                r3 = r3.append(r0);	 Catch:{ IOException -> 0x012f }
                r4 = ".mp4";
                r3 = r3.append(r4);	 Catch:{ IOException -> 0x012f }
                r3 = r3.toString();	 Catch:{ IOException -> 0x012f }
                r15.<init>(r3);	 Catch:{ IOException -> 0x012f }
                r17 = new java.io.FileInputStream;	 Catch:{ IOException -> 0x012f }
                r0 = r17;
                r0.<init>(r15);	 Catch:{ IOException -> 0x012f }
                r13 = new java.io.File;	 Catch:{ IOException -> 0x01b1, all -> 0x01a8 }
                r3 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x01b1, all -> 0x01a8 }
                r3.<init>();	 Catch:{ IOException -> 0x01b1, all -> 0x01a8 }
                r0 = r22;
                r4 = com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.this;	 Catch:{ IOException -> 0x01b1, all -> 0x01a8 }
                r4 = r4.mSavedFileName;	 Catch:{ IOException -> 0x01b1, all -> 0x01a8 }
                r3 = r3.append(r4);	 Catch:{ IOException -> 0x01b1, all -> 0x01a8 }
                r4 = ".jpg";
                r3 = r3.append(r4);	 Catch:{ IOException -> 0x01b1, all -> 0x01a8 }
                r3 = r3.toString();	 Catch:{ IOException -> 0x01b1, all -> 0x01a8 }
                r13.<init>(r3);	 Catch:{ IOException -> 0x01b1, all -> 0x01a8 }
                r10 = new java.io.FileOutputStream;	 Catch:{ IOException -> 0x01b1, all -> 0x01a8 }
                r10.<init>(r13);	 Catch:{ IOException -> 0x01b1, all -> 0x01a8 }
                r0 = r22;
                r3 = com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.this;	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r3 = r3.mFrameCount;	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r3 = (float) r3;	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r4 = 1097859072; // 0x41700000 float:15.0 double:5.424144515E-315;
                r3 = r3 / r4;
                r4 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
                r14 = r3 - r4;
                r3 = 0;
                r3 = (r14 > r3 ? 1 : (r14 == r3 ? 0 : -1));
                if (r3 >= 0) goto L_0x007f;
            L_0x007e:
                r14 = 0;
            L_0x007f:
                r3 = 1232348160; // 0x49742400 float:1000000.0 double:6.088608896E-315;
                r3 = r3 * r14;
                r6 = (long) r3;	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r0 = r22;
                r3 = com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.this;	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r3 = r3.mJpegData;	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r4 = r15.length();	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r8 = com.lge.camera.file.FileManager.EOFMarker;	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r8 = r8.length;	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r0 = (long) r8;	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r20 = r0;
                r4 = r4 + r20;
                r0 = r22;
                r8 = com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.this;	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r8 = r8.mCameraType;	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r18 = com.lge.camera.util.XMPWriter.insertLivePicXMP(r3, r4, r6, r8);	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r0 = r18;
                com.lge.camera.file.FileManager.copyFileIntoStream(r0, r10);	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                r0 = r17;
                com.lge.camera.file.FileManager.copyFileIntoStream(r0, r10);	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                com.lge.camera.file.FileManager.insertLivePicEOFMarker(r10);	 Catch:{ IOException -> 0x01b6, all -> 0x01ac }
                if (r10 == 0) goto L_0x00b6;
            L_0x00b3:
                r10.close();	 Catch:{ IOException -> 0x011d }
            L_0x00b6:
                if (r17 == 0) goto L_0x00bb;
            L_0x00b8:
                r17.close();	 Catch:{ IOException -> 0x0126 }
            L_0x00bb:
                r3 = new java.lang.StringBuilder;
                r3.<init>();
                r0 = r23;
                r3 = r3.append(r0);
                r4 = ".mp4";
                r3 = r3.append(r4);
                r3 = r3.toString();
                com.lge.camera.file.FileManager.deleteFile(r3);
                r9 = r10;
                r16 = r17;
            L_0x00d6:
                r0 = r22;
                r3 = com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.this;
                r3 = com.lge.camera.managers.LivePhotoManagerBase.this;
                r4 = 0;
                r3.setLivePhotoSavingProgress(r4);
                r0 = r22;
                r3 = com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.this;
                r3 = r3.m3SecBuffer;
                if (r3 == 0) goto L_0x00fd;
            L_0x00ea:
                r0 = r22;
                r3 = com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.this;
                r3 = r3.m3SecBuffer;
                r3.clear();
                r0 = r22;
                r3 = com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.this;
                r4 = 0;
                r3.m3SecBuffer = r4;
            L_0x00fd:
                r0 = r22;
                r3 = com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.this;
                r3 = com.lge.camera.managers.LivePhotoManagerBase.this;
                r3 = r3.mGet;
                r11 = r3.getHandler();
                r3 = 109; // 0x6d float:1.53E-43 double:5.4E-322;
                r12 = r11.obtainMessage(r3);
                r0 = r22;
                r3 = com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.this;
                r3 = r3.mImageUriInfo;
                r12.obj = r3;
                r11.sendMessage(r12);
                return;
            L_0x011d:
                r2 = move-exception;
                r3 = "CameraApp";
                r4 = "ignore close exception";
                com.lge.camera.util.CamLog.m3d(r3, r4);
                goto L_0x00b6;
            L_0x0126:
                r2 = move-exception;
                r3 = "CameraApp";
                r4 = "ignore close exception";
                com.lge.camera.util.CamLog.m3d(r3, r4);
                goto L_0x00bb;
            L_0x012f:
                r2 = move-exception;
            L_0x0130:
                r2.printStackTrace();	 Catch:{ all -> 0x0172 }
                r3 = "CameraApp";
                r4 = r2.getMessage();	 Catch:{ all -> 0x0172 }
                com.lge.camera.util.CamLog.m5e(r3, r4);	 Catch:{ all -> 0x0172 }
                if (r9 == 0) goto L_0x0141;
            L_0x013e:
                r9.close();	 Catch:{ IOException -> 0x0160 }
            L_0x0141:
                if (r16 == 0) goto L_0x0146;
            L_0x0143:
                r16.close();	 Catch:{ IOException -> 0x0169 }
            L_0x0146:
                r3 = new java.lang.StringBuilder;
                r3.<init>();
                r0 = r23;
                r3 = r3.append(r0);
                r4 = ".mp4";
                r3 = r3.append(r4);
                r3 = r3.toString();
                com.lge.camera.file.FileManager.deleteFile(r3);
                goto L_0x00d6;
            L_0x0160:
                r2 = move-exception;
                r3 = "CameraApp";
                r4 = "ignore close exception";
                com.lge.camera.util.CamLog.m3d(r3, r4);
                goto L_0x0141;
            L_0x0169:
                r2 = move-exception;
                r3 = "CameraApp";
                r4 = "ignore close exception";
                com.lge.camera.util.CamLog.m3d(r3, r4);
                goto L_0x0146;
            L_0x0172:
                r3 = move-exception;
            L_0x0173:
                if (r9 == 0) goto L_0x0178;
            L_0x0175:
                r9.close();	 Catch:{ IOException -> 0x0196 }
            L_0x0178:
                if (r16 == 0) goto L_0x017d;
            L_0x017a:
                r16.close();	 Catch:{ IOException -> 0x019f }
            L_0x017d:
                r4 = new java.lang.StringBuilder;
                r4.<init>();
                r0 = r23;
                r4 = r4.append(r0);
                r5 = ".mp4";
                r4 = r4.append(r5);
                r4 = r4.toString();
                com.lge.camera.file.FileManager.deleteFile(r4);
                throw r3;
            L_0x0196:
                r2 = move-exception;
                r4 = "CameraApp";
                r5 = "ignore close exception";
                com.lge.camera.util.CamLog.m3d(r4, r5);
                goto L_0x0178;
            L_0x019f:
                r2 = move-exception;
                r4 = "CameraApp";
                r5 = "ignore close exception";
                com.lge.camera.util.CamLog.m3d(r4, r5);
                goto L_0x017d;
            L_0x01a8:
                r3 = move-exception;
                r16 = r17;
                goto L_0x0173;
            L_0x01ac:
                r3 = move-exception;
                r9 = r10;
                r16 = r17;
                goto L_0x0173;
            L_0x01b1:
                r2 = move-exception;
                r16 = r17;
                goto L_0x0130;
            L_0x01b6:
                r2 = move-exception;
                r9 = r10;
                r16 = r17;
                goto L_0x0130;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.2.onEncodeAfter(java.lang.String):void");
            }
        }

        public LivePhoto(LinkedList<byte[]> prevData, String fileName, int deviceDegree, int cameraType) {
            this.MAX_SIZE = prevData.size() + 15;
            this.m3SecBuffer.addAll(prevData);
            this.mImageFileName = fileName;
            this.mSavedFileName = LivePhotoManagerBase.this.mGet.getCurDir() + this.mImageFileName;
            this.mDeviceDegree = deviceDegree;
            this.mCameraType = cameraType;
            new Thread(new Runnable(LivePhotoManagerBase.this) {
                public void run() {
                    LivePhoto.this.mJpegData = LivePhoto.this.readImageByteBuffer();
                    FileManager.deleteFile(LivePhotoManagerBase.this.mGet.getCurDir() + LivePhoto.this.mImageFileName + ".jpg");
                }
            }).start();
        }

        public void setImageUriInfo(ImageUriInfo info) {
            this.mImageUriInfo = info;
        }

        /* JADX WARNING: Removed duplicated region for block: B:19:0x0061 A:{SYNTHETIC, Splitter: B:19:0x0061} */
        /* JADX WARNING: Removed duplicated region for block: B:35:? A:{SYNTHETIC, RETURN, SKIP} */
        /* JADX WARNING: Removed duplicated region for block: B:10:0x0046  */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0071 A:{SYNTHETIC, Splitter: B:25:0x0071} */
        public byte[] readImageByteBuffer() {
            /*
            r10 = this;
            r7 = new java.lang.StringBuilder;
            r7.<init>();
            r8 = com.lge.camera.managers.LivePhotoManagerBase.this;
            r8 = r8.mGet;
            r8 = r8.getCurDir();
            r7 = r7.append(r8);
            r8 = r10.mImageFileName;
            r7 = r7.append(r8);
            r8 = ".jpg";
            r7 = r7.append(r8);
            r4 = r7.toString();
            r5 = 0;
            r6 = 0;
            r2 = 0;
            r1 = new java.io.File;	 Catch:{ IOException -> 0x0055 }
            r1.<init>(r4);	 Catch:{ IOException -> 0x0055 }
            r3 = new java.io.FileInputStream;	 Catch:{ IOException -> 0x0055 }
            r3.<init>(r1);	 Catch:{ IOException -> 0x0055 }
            r8 = r1.length();	 Catch:{ IOException -> 0x0081, all -> 0x007e }
            r7 = (int) r8;	 Catch:{ IOException -> 0x0081, all -> 0x007e }
            r6 = new byte[r7];	 Catch:{ IOException -> 0x0081, all -> 0x007e }
            r7 = 0;
            r8 = r6.length;	 Catch:{ IOException -> 0x0081, all -> 0x007e }
            r5 = r3.read(r6, r7, r8);	 Catch:{ IOException -> 0x0081, all -> 0x007e }
            r3.close();	 Catch:{ IOException -> 0x0081, all -> 0x007e }
            if (r3 == 0) goto L_0x0043;
        L_0x0040:
            r3.close();	 Catch:{ IOException -> 0x004b }
        L_0x0043:
            r2 = r3;
        L_0x0044:
            if (r6 == 0) goto L_0x004a;
        L_0x0046:
            r7 = r6.length;
            if (r5 == r7) goto L_0x004a;
        L_0x0049:
            r6 = 0;
        L_0x004a:
            return r6;
        L_0x004b:
            r0 = move-exception;
            r7 = "CameraApp";
            r8 = "ignore close exception";
            com.lge.camera.util.CamLog.m3d(r7, r8);
            r2 = r3;
            goto L_0x0044;
        L_0x0055:
            r0 = move-exception;
        L_0x0056:
            r7 = "CameraApp";
            r8 = r0.getMessage();	 Catch:{ all -> 0x006e }
            com.lge.camera.util.CamLog.m5e(r7, r8);	 Catch:{ all -> 0x006e }
            if (r2 == 0) goto L_0x0044;
        L_0x0061:
            r2.close();	 Catch:{ IOException -> 0x0065 }
            goto L_0x0044;
        L_0x0065:
            r0 = move-exception;
            r7 = "CameraApp";
            r8 = "ignore close exception";
            com.lge.camera.util.CamLog.m3d(r7, r8);
            goto L_0x0044;
        L_0x006e:
            r7 = move-exception;
        L_0x006f:
            if (r2 == 0) goto L_0x0074;
        L_0x0071:
            r2.close();	 Catch:{ IOException -> 0x0075 }
        L_0x0074:
            throw r7;
        L_0x0075:
            r0 = move-exception;
            r8 = "CameraApp";
            r9 = "ignore close exception";
            com.lge.camera.util.CamLog.m3d(r8, r9);
            goto L_0x0074;
        L_0x007e:
            r7 = move-exception;
            r2 = r3;
            goto L_0x006f;
        L_0x0081:
            r0 = move-exception;
            r2 = r3;
            goto L_0x0056;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.LivePhotoManagerBase.LivePhoto.readImageByteBuffer():byte[]");
        }

        public boolean put(byte[] newData) {
            if (this.m3SecBuffer.size() < this.MAX_SIZE) {
                this.m3SecBuffer.add(newData);
            }
            return this.m3SecBuffer.size() >= this.MAX_SIZE;
        }

        public void save() {
            int videoDegree = LivePhotoManagerBase.this.getVideoDegree(this.mImageUriInfo.mDegree, this.mDeviceDegree);
            CamLog.m3d(CameraConstants.TAG, "-Live Photo- save(), input frameCount = " + this.m3SecBuffer.size());
            CamLog.m3d(CameraConstants.TAG, "-Live Photo- save(), videoDegree = " + videoDegree);
            this.mFrameCount = this.m3SecBuffer.size();
            this.mEncoder = new LivePhotoEncoder();
            this.mEncoder.setDirPath(LivePhotoManagerBase.this.mGet.getCurDir());
            this.mEncoder.setBuffer(this.m3SecBuffer);
            this.mEncoder.setFileName(this.mImageFileName + "_temp");
            this.mEncoder.setSize(LivePhotoManagerBase.this.mFrameSize);
            this.mEncoder.setKeyFramePerSec(7);
            this.mEncoder.setFPS(15);
            if (this.mImageUriInfo != null) {
                this.mEncoder.setOrientationHint(videoDegree);
            }
            this.mEncoder.setListener(this.mListener);
            this.mEncoder.setColorFormat(2135033992);
            this.mEncoder.start();
        }
    }

    public LivePhotoManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
    }

    public boolean isNeedToEnableLivePhoto() {
        return false;
    }

    protected boolean isLivePhotoSupported() {
        return false;
    }

    public void changePreviewSize(String previewSize) {
        CamLog.m3d(CameraConstants.TAG, "-Live Photo- changePreviewSize : " + previewSize);
        if (!Arrays.equals(Utils.sizeStringToArray(previewSize), this.mFrameSize) && this.mIsLivePhotoEnabled) {
            restartLivePhoto();
        }
    }

    public void restartLivePhoto() {
        if (this.mIsLivePhotoEnabled) {
            disableLivePhoto();
            enableLivePhoto();
        }
    }

    public boolean enableLivePhoto() {
        boolean z = false;
        CamLog.m3d(CameraConstants.TAG, "-Live Photo- enableLivePhoto");
        if (this.mIsLivePhotoEnabled) {
            CamLog.m11w(CameraConstants.TAG, "-Live Photo- already enabled, return");
            return false;
        } else if (this.mWindowFocus) {
            if (this.mPrev2SecBuffer == null) {
                this.mPrev2SecBuffer = new LinkedList();
            }
            if (this.mLivePhotoList == null) {
                this.mLivePhotoList = new LinkedList();
            }
            this.mIsLivePhotoEnabled = true;
            initFrameSize();
            this.mLastPrevCallbackTime = System.currentTimeMillis();
            this.mLogCount = 0;
            if (!CameraDeviceUtils.isRearCamera(this.mGet.getCameraId())) {
                z = true;
            }
            this.mIsFrontCamera = z;
            return true;
        } else {
            CamLog.m3d(CameraConstants.TAG, "-Live Photo- window focus is not granted, return");
            return false;
        }
    }

    protected void initFrameSize() {
        this.mFrameSize = Utils.sizeStringToArray(this.mGet.getCurrentSelectedPreviewSize());
        CamLog.m3d(CameraConstants.TAG, "frameSize size[0] = " + this.mFrameSize[0] + ", size[1] = " + this.mFrameSize[1]);
    }

    public void disableLivePhoto() {
        CamLog.m3d(CameraConstants.TAG, "-Live Photo- disableLivePhoto");
        this.mIsLivePhotoEnabled = false;
        if (this.mPrev2SecBuffer != null) {
            this.mPrev2SecBuffer.clear();
            this.mPrev2SecBuffer = null;
        }
        forceSaveLivePhotos();
        if (this.mLivePhotoList != null && this.mLivePhotoList.size() > 0) {
            this.mLivePhotoList.clear();
            this.mLivePhotoList = null;
        }
    }

    public boolean isLivePhotoEnabled() {
        return this.mIsLivePhotoEnabled;
    }

    private void forceSaveLivePhotos() {
        if (this.mLivePhotoList != null && this.mLivePhotoList.size() != 0) {
            Iterator it = this.mLivePhotoList.iterator();
            while (it.hasNext()) {
                ((LivePhoto) it.next()).save();
            }
            this.mLivePhotoList.clear();
            this.mLivePhotoList = null;
        }
    }

    public void onPreviewFrame(Image image) {
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        if (this.mGet.checkModuleValidate(16)) {
            disableLivePhoto();
        }
    }

    /* JADX WARNING: Missing block: B:25:?, code:
            return;
     */
    public void onImageSaved(java.lang.String r9, int r10) {
        /*
        r8 = this;
        r0 = r8.mIsLivePhotoEnabled;
        if (r0 == 0) goto L_0x000c;
    L_0x0004:
        r0 = r8.mLivePhotoList;
        if (r0 == 0) goto L_0x000c;
    L_0x0008:
        r0 = r8.mPrev2SecBuffer;
        if (r0 != 0) goto L_0x000d;
    L_0x000c:
        return;
    L_0x000d:
        r6 = r8.mLinkedListLock;
        monitor-enter(r6);
        r0 = r8.mIsLivePhotoEnabled;	 Catch:{ all -> 0x001e }
        if (r0 == 0) goto L_0x001c;
    L_0x0014:
        r0 = r8.mLivePhotoList;	 Catch:{ all -> 0x001e }
        if (r0 == 0) goto L_0x001c;
    L_0x0018:
        r0 = r8.mPrev2SecBuffer;	 Catch:{ all -> 0x001e }
        if (r0 != 0) goto L_0x0021;
    L_0x001c:
        monitor-exit(r6);	 Catch:{ all -> 0x001e }
        goto L_0x000c;
    L_0x001e:
        r0 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x001e }
        throw r0;
    L_0x0021:
        r0 = "CameraApp";
        r1 = "-Live Photo- add LivePhoto, fileName = %s, current frame count = %d";
        r2 = 2;
        r2 = new java.lang.Object[r2];	 Catch:{ all -> 0x001e }
        r3 = 0;
        r2[r3] = r9;	 Catch:{ all -> 0x001e }
        r3 = 1;
        r4 = r8.mPrev2SecBuffer;	 Catch:{ all -> 0x001e }
        r4 = r4.size();	 Catch:{ all -> 0x001e }
        r4 = java.lang.Integer.valueOf(r4);	 Catch:{ all -> 0x001e }
        r2[r3] = r4;	 Catch:{ all -> 0x001e }
        r1 = java.lang.String.format(r1, r2);	 Catch:{ all -> 0x001e }
        com.lge.camera.util.CamLog.m3d(r0, r1);	 Catch:{ all -> 0x001e }
        r7 = r8.mLivePhotoList;	 Catch:{ all -> 0x001e }
        r0 = new com.lge.camera.managers.LivePhotoManagerBase$LivePhoto;	 Catch:{ all -> 0x001e }
        r2 = r8.mPrev2SecBuffer;	 Catch:{ all -> 0x001e }
        r1 = r8.mGet;	 Catch:{ all -> 0x001e }
        r1 = r1.getCameraId();	 Catch:{ all -> 0x001e }
        r5 = r8.getLivePhotoCameraType(r1);	 Catch:{ all -> 0x001e }
        r1 = r8;
        r3 = r9;
        r4 = r10;
        r0.<init>(r2, r3, r4, r5);	 Catch:{ all -> 0x001e }
        r7.add(r0);	 Catch:{ all -> 0x001e }
        monitor-exit(r6);	 Catch:{ all -> 0x001e }
        goto L_0x000c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.LivePhotoManagerBase.onImageSaved(java.lang.String, int):void");
    }

    protected int getLivePhotoCameraType(int cameraId) {
        int cameraType;
        if (CameraDeviceUtils.isRearCamera(cameraId)) {
            cameraType = 0;
        } else if ("on".equals(this.mGet.getSettingValue(Setting.KEY_SAVE_DIRECTION))) {
            cameraType = 2;
        } else {
            cameraType = 1;
        }
        CamLog.m3d(CameraConstants.TAG, "-Live photo- camera type = " + cameraType);
        return cameraType;
    }

    public void setImageUriInfo(ContentResolver cr, String directory, String fileName, long dateTaken, Location location, int degree, ExifInterface exif, boolean isBurstshot) {
        if (this.mLivePhotoList != null) {
            LivePhoto livePhoto = (LivePhoto) this.mLivePhotoList.getLast();
            if (livePhoto != null) {
                CamLog.m3d(CameraConstants.TAG, "-Live Photo- setImageUriInfo, fileName = " + fileName);
                livePhoto.setImageUriInfo(new ImageUriInfo(cr, directory, fileName, dateTaken, location, degree, exif, isBurstshot));
            }
        }
    }

    public boolean isAvailableLivePhoto() {
        if (!this.mIsLivePhotoEnabled) {
            return true;
        }
        if (this.mLivePhotoList == null) {
            CamLog.m7i(CameraConstants.TAG, "mLivePhotoList is null");
            return false;
        } else if (this.mLivePhotoList.size() <= 10) {
            return true;
        } else {
            CamLog.m7i(CameraConstants.TAG, "-Live Photo- too much live photos");
            return false;
        }
    }

    protected int getVideoDegree(int exifDegree, int deviceDegree) {
        return exifDegree;
    }

    public boolean isLivePhotoSavingProgress() {
        return this.mIsSavingProgress;
    }

    public void setLivePhotoSavingProgress(boolean saving) {
        this.mIsSavingProgress = saving;
    }

    public void pause() {
    }

    public void resume() {
    }

    public void setHasWindowFocus(boolean hasFocus) {
        this.mWindowFocus = hasFocus;
    }
}
