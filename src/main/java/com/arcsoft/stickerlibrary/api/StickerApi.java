package com.arcsoft.stickerlibrary.api;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.p000v4.internal.view.SupportMenu;
import com.arcsoft.stickerlibrary.codec.CodecLog;
import com.arcsoft.stickerlibrary.codec.MediaManager;
import com.arcsoft.stickerlibrary.sticker.StickerMessage;
import com.arcsoft.stickerlibrary.utils.BenchmarkUtil;
import com.arcsoft.stickerlibrary.utils.LogUtil;
import com.arcsoft.stickerlibrary.utils.TemplateItem;
import com.arcsoft.stickerlibrary.utils.XMLParser;
import com.arcsoft.stickerlibrary.utils.XMLParser.AlignParams;
import com.arcsoft.stickerlibrary.utils.XMLParser.BeautyParams;
import com.arcsoft.stickerlibrary.utils.XMLParser.FunnyFaceParams;
import com.arcsoft.stickerlibrary.utils.XMLParser.MaskParams;
import com.arcsoft.stickerlibrary.utils.XMLParser.Sticker2dParams;
import com.arcsoft.stickerlibrary.utils.XMLParser.Sticker3dParams;
import com.arcsoft.stickerlibrary.utils.XMLParser.SuperParams;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class StickerApi {
    public static final int ORIENTATION_HYSTERESIS = 5;
    private static final String TAG = StickerApi.class.getSimpleName();
    private static final int TEMPLATE_QUEUE_SIZE = 2;
    private final int RECORDING_CALLBACK_INTERVAL = 1000000;
    private final int RESULT_BITSIZE = 16;
    private final int RESULT_MASK = SupportMenu.USER_MASK;
    private Context mContext;
    private EGLContext mCurrentContext = EGL14.EGL_NO_CONTEXT;
    private EGLDisplay mCurrentDisplay = EGL14.EGL_NO_DISPLAY;
    private int mCurrentOrientation = -1;
    private String mCurrentResourceName = null;
    private EGLSurface mCurrentSurface = EGL14.EGL_NO_SURFACE;
    private int mDegree;
    private Object mEngineGLLockObj = new Object();
    private Object mEngineInitLockObj = new Object();
    private Object mEngineUILockObj = new Object();
    private String mFaceModelPath = "";
    private int mFrameHeight;
    private int mFrameWidth;
    private String mImageDirectory;
    private boolean mIsEnbaleLog = true;
    private boolean mIsMirror;
    private volatile boolean mIsRequestCapture = false;
    private volatile boolean mIsRequestPauseRecording = false;
    private boolean mIsRequestRecording = false;
    private volatile boolean mIsStickerRunning = false;
    private long mMaxRecordingDuration = 0;
    private long mMaxRecordingFileSize = 0;
    private MediaManager mMediaManager;
    private FaceStatus mOldFaceStatus = new FaceStatus();
    private int mOrientCamera;
    private int mOutlineOrientation = 270;
    private long mPreviewCallbackCountForFPS = 0;
    private long mRecordTimeCallbackCount = 0;
    private StickerRecordingListener mRecordingListener = null;
    private Thread mSetTemplateThread = null;
    private long mStartPreviewCallbackTimeForFPS = 0;
    private StickerCaptureCallback mStickerCaptureCB = null;
    private StickerInfoListener mStickerInfoListener = null;
    private StickerJNI mStickerJNI = null;
    private Queue<TemplateItem> mTemplatesQueue = new LinkedList();
    private String mTrackDataPath = "";
    private Object mVisitQueueMonitor = new Object();
    private Object mVisitXmlListMonitor = new Object();
    ArrayList<SuperParams> mXmlConfigList = null;
    private boolean mbInitFinish = false;

    public StickerApi() {
        LogUtil.setEnabled(this.mIsEnbaleLog);
        CodecLog.enableLog(this.mIsEnbaleLog);
        this.mImageDirectory = null;
    }

    public void enableLog(boolean isEnableLog) {
        this.mIsEnbaleLog = isEnableLog;
        LogUtil.setEnabled(this.mIsEnbaleLog);
        CodecLog.enableLog(this.mIsEnbaleLog);
    }

    public int init(@NonNull Context context, int width, int height, boolean isMirror, int degreeScreen, int orientCamera, String trackDataPath, String faceModelPath, StickerInfoListener infoListener) {
        LogUtil.LogD(TAG, "init in, this=" + this + " ,stickerApi_init_threadId=" + Thread.currentThread().getId() + " ,width = " + width + " ,height = " + height);
        this.mDegree = degreeScreen;
        this.mOrientCamera = orientCamera;
        this.mIsMirror = isMirror;
        this.mContext = context;
        this.mTrackDataPath = trackDataPath;
        this.mFaceModelPath = faceModelPath;
        this.mStickerInfoListener = infoListener;
        LogUtil.LogD(TAG, "init mFrameWidth = " + width + " ,mFrameHeight = " + height + " ,degreeScreen = " + degreeScreen + " ,orientCamera = " + orientCamera + " ,mIsMirror(frontCamera) = " + this.mIsMirror);
        synchronized (this.mEngineUILockObj) {
            BenchmarkUtil.start("StickerApi_init");
            this.mStickerJNI = new StickerJNI(context);
            this.mStickerJNI.init(width, height, this.mIsMirror, this.mDegree, this.mStickerInfoListener);
            this.mFrameWidth = width;
            this.mFrameHeight = height;
            LogUtil.LogD(TAG, "init mFrameWidth = " + width + " ,mFrameHeight = " + height);
            BenchmarkUtil.stop("StickerApi_init");
        }
        synchronized (this.mEngineInitLockObj) {
            this.mbInitFinish = true;
        }
        this.mIsStickerRunning = true;
        startSetTemplateThread();
        LogUtil.LogD(TAG, "init out");
        return 0;
    }

    public int setTemplate(String configXmlPath) {
        LogUtil.LogD(TAG, "setTemplate in, this=" + this + " ,stickerApi_setTemplate_threadId=" + Thread.currentThread().getId());
        int ret = 0;
        synchronized (this.mEngineInitLockObj) {
            if (this.mbInitFinish) {
                synchronized (this.mVisitQueueMonitor) {
                    TemplateItem templateItem = new TemplateItem();
                    templateItem.setConfigList(xmlParser(this.mContext, configXmlPath));
                    templateItem.setConfigPath(configXmlPath);
                    if (this.mTemplatesQueue.size() >= 2) {
                        this.mTemplatesQueue.poll();
                    }
                    this.mTemplatesQueue.offer(templateItem);
                    synchronized (this.mVisitXmlListMonitor) {
                        this.mXmlConfigList = templateItem.getConfigList();
                    }
                    this.mVisitQueueMonitor.notifyAll();
                }
                this.mPreviewCallbackCountForFPS = 0;
                LogUtil.LogD(TAG, "setTemplate out");
            } else {
                LogUtil.LogE(TAG, "startProcess_camera1-> StickerApi is not init.");
                ret = 5;
            }
        }
        return ret;
    }

    /* JADX WARNING: Missing block: B:9:0x0043, code:
            if (r7.mXmlConfigList.isEmpty() != false) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:10:0x0045, code:
            if (r8 > 0) goto L_0x004c;
     */
    /* JADX WARNING: Missing block: B:16:0x004c, code:
            r1 = null;
            r0 = 0;
     */
    /* JADX WARNING: Missing block: B:18:0x0054, code:
            if (r0 >= r7.mXmlConfigList.size()) goto L_0x0064;
     */
    /* JADX WARNING: Missing block: B:19:0x0056, code:
            r1 = r7.mXmlConfigList.toArray()[r0];
     */
    /* JADX WARNING: Missing block: B:20:0x0062, code:
            if (6 != r1.mParamsType) goto L_0x0076;
     */
    /* JADX WARNING: Missing block: B:22:0x0066, code:
            if (6 != r1.mParamsType) goto L_0x0079;
     */
    /* JADX WARNING: Missing block: B:23:0x0068, code:
            r7.mStickerJNI.setFunnyType(r8);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "setFunnyType out");
     */
    /* JADX WARNING: Missing block: B:24:0x0076, code:
            r0 = r0 + 1;
     */
    /* JADX WARNING: Missing block: B:25:0x0079, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "setFunnyType out");
     */
    /* JADX WARNING: Missing block: B:32:?, code:
            return 5;
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:34:?, code:
            return 3;
     */
    private int setFunnyType(int r8) {
        /*
        r7 = this;
        r6 = 6;
        r2 = TAG;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "setFunnyType, this=";
        r3 = r3.append(r4);
        r3 = r3.append(r7);
        r4 = " ,stickerApi_setFunnyType_threadId=";
        r3 = r3.append(r4);
        r4 = java.lang.Thread.currentThread();
        r4 = r4.getId();
        r3 = r3.append(r4);
        r3 = r3.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);
        r3 = r7.mEngineInitLockObj;
        monitor-enter(r3);
        r2 = r7.mbInitFinish;	 Catch:{ all -> 0x0049 }
        if (r2 != 0) goto L_0x003c;
    L_0x0032:
        r2 = TAG;	 Catch:{ all -> 0x0049 }
        r4 = "startProcess_camera2-> StickerApi is not init.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogE(r2, r4);	 Catch:{ all -> 0x0049 }
        r2 = 1;
        monitor-exit(r3);	 Catch:{ all -> 0x0049 }
    L_0x003b:
        return r2;
    L_0x003c:
        monitor-exit(r3);	 Catch:{ all -> 0x0049 }
        r2 = r7.mXmlConfigList;
        r2 = r2.isEmpty();
        if (r2 != 0) goto L_0x0047;
    L_0x0045:
        if (r8 > 0) goto L_0x004c;
    L_0x0047:
        r2 = 5;
        goto L_0x003b;
    L_0x0049:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0049 }
        throw r2;
    L_0x004c:
        r1 = 0;
        r0 = 0;
    L_0x004e:
        r2 = r7.mXmlConfigList;
        r2 = r2.size();
        if (r0 >= r2) goto L_0x0064;
    L_0x0056:
        r2 = r7.mXmlConfigList;
        r2 = r2.toArray();
        r1 = r2[r0];
        r1 = (com.arcsoft.stickerlibrary.utils.XMLParser.SuperParams) r1;
        r2 = r1.mParamsType;
        if (r6 != r2) goto L_0x0076;
    L_0x0064:
        r2 = r1.mParamsType;
        if (r6 != r2) goto L_0x0079;
    L_0x0068:
        r2 = r7.mStickerJNI;
        r2.setFunnyType(r8);
        r2 = TAG;
        r3 = "setFunnyType out";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);
        r2 = 0;
        goto L_0x003b;
    L_0x0076:
        r0 = r0 + 1;
        goto L_0x004e;
    L_0x0079:
        r2 = TAG;
        r3 = "setFunnyType out";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);
        r2 = 3;
        goto L_0x003b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.arcsoft.stickerlibrary.api.StickerApi.setFunnyType(int):int");
    }

    /* JADX WARNING: Missing block: B:8:0x0036, code:
            r9 = r12.mVisitXmlListMonitor;
     */
    /* JADX WARNING: Missing block: B:9:0x0038, code:
            monitor-enter(r9);
     */
    /* JADX WARNING: Missing block: B:12:0x003b, code:
            if (r12.mXmlConfigList == null) goto L_0x011c;
     */
    /* JADX WARNING: Missing block: B:14:0x0043, code:
            if (r12.mXmlConfigList.size() <= 0) goto L_0x011c;
     */
    /* JADX WARNING: Missing block: B:15:0x0045, code:
            r3 = new com.arcsoft.stickerlibrary.sticker.StickerAction();
     */
    /* JADX WARNING: Missing block: B:16:0x004a, code:
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:19:0x0051, code:
            if (r4 >= r12.mXmlConfigList.size()) goto L_0x011b;
     */
    /* JADX WARNING: Missing block: B:20:0x0053, code:
            r5 = (com.arcsoft.stickerlibrary.utils.XMLParser.SuperParams) r12.mXmlConfigList.get(r4);
     */
    /* JADX WARNING: Missing block: B:21:0x005e, code:
            if (r5.mParamsType != 3) goto L_0x0114;
     */
    /* JADX WARNING: Missing block: B:22:0x0060, code:
            r7 = (com.arcsoft.stickerlibrary.utils.XMLParser.Sticker2dParams) r5;
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "_m2dTemplatePath = " + r7.m2dTemplatePath);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "_m2dMouthOpen = " + r7.m2dMouthOpen);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "_m2dEyeBlink = " + r7.m2dEyeBlink);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "_m2dEyebrowRaise = " + r7.m2dEyebrowRaise);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "_m2dHeadNod = " + r7.m2dHeadNod);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "_m2dHeadShake = " + r7.m2dHeadShake);
            r3.mMouthOpen = r7.m2dMouthOpen;
            r3.mEyeBlink = r7.m2dEyeBlink;
            r3.mEyebrowRaise = r7.m2dEyebrowRaise;
            r3.mHeadNod = r7.m2dHeadNod;
            r3.mHeadShake = r7.m2dHeadShake;
     */
    /* JADX WARNING: Missing block: B:23:0x0114, code:
            r4 = r4 + 1;
     */
    /* JADX WARNING: Missing block: B:28:0x011b, code:
            r2 = r3;
     */
    /* JADX WARNING: Missing block: B:30:?, code:
            monitor-exit(r9);
     */
    /* JADX WARNING: Missing block: B:31:0x011d, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "getActionList out");
     */
    /* JADX WARNING: Missing block: B:32:0x0127, code:
            r8 = th;
     */
    /* JADX WARNING: Missing block: B:34:?, code:
            monitor-exit(r9);
     */
    /* JADX WARNING: Missing block: B:35:0x0129, code:
            throw r8;
     */
    /* JADX WARNING: Missing block: B:36:0x012a, code:
            r8 = th;
     */
    /* JADX WARNING: Missing block: B:37:0x012b, code:
            r2 = r3;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            return r2;
     */
    public com.arcsoft.stickerlibrary.sticker.StickerAction getActionList() {
        /*
        r12 = this;
        r8 = TAG;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "getActionList in, this=";
        r9 = r9.append(r10);
        r9 = r9.append(r12);
        r10 = " ,stickerApi_getActionList_threadId=";
        r9 = r9.append(r10);
        r10 = java.lang.Thread.currentThread();
        r10 = r10.getId();
        r9 = r9.append(r10);
        r9 = r9.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r8, r9);
        r2 = 0;
        r9 = r12.mEngineInitLockObj;
        monitor-enter(r9);
        r8 = r12.mbInitFinish;	 Catch:{ all -> 0x0118 }
        if (r8 != 0) goto L_0x0035;
    L_0x0032:
        monitor-exit(r9);	 Catch:{ all -> 0x0118 }
        r3 = r2;
    L_0x0034:
        return r3;
    L_0x0035:
        monitor-exit(r9);	 Catch:{ all -> 0x0118 }
        r9 = r12.mVisitXmlListMonitor;
        monitor-enter(r9);
        r8 = r12.mXmlConfigList;	 Catch:{ all -> 0x0127 }
        if (r8 == 0) goto L_0x011c;
    L_0x003d:
        r8 = r12.mXmlConfigList;	 Catch:{ all -> 0x0127 }
        r8 = r8.size();	 Catch:{ all -> 0x0127 }
        if (r8 <= 0) goto L_0x011c;
    L_0x0045:
        r3 = new com.arcsoft.stickerlibrary.sticker.StickerAction;	 Catch:{ all -> 0x0127 }
        r3.<init>();	 Catch:{ all -> 0x0127 }
        r4 = 0;
    L_0x004b:
        r8 = r12.mXmlConfigList;	 Catch:{ all -> 0x012a }
        r8 = r8.size();	 Catch:{ all -> 0x012a }
        if (r4 >= r8) goto L_0x011b;
    L_0x0053:
        r8 = r12.mXmlConfigList;	 Catch:{ all -> 0x012a }
        r5 = r8.get(r4);	 Catch:{ all -> 0x012a }
        r5 = (com.arcsoft.stickerlibrary.utils.XMLParser.SuperParams) r5;	 Catch:{ all -> 0x012a }
        r6 = r5.mParamsType;	 Catch:{ all -> 0x012a }
        r8 = 3;
        if (r6 != r8) goto L_0x0114;
    L_0x0060:
        r0 = r5;
        r0 = (com.arcsoft.stickerlibrary.utils.XMLParser.Sticker2dParams) r0;	 Catch:{ all -> 0x012a }
        r7 = r0;
        r8 = TAG;	 Catch:{ all -> 0x012a }
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x012a }
        r10.<init>();	 Catch:{ all -> 0x012a }
        r11 = "_m2dTemplatePath = ";
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r11 = r7.m2dTemplatePath;	 Catch:{ all -> 0x012a }
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r10 = r10.toString();	 Catch:{ all -> 0x012a }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r8, r10);	 Catch:{ all -> 0x012a }
        r8 = TAG;	 Catch:{ all -> 0x012a }
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x012a }
        r10.<init>();	 Catch:{ all -> 0x012a }
        r11 = "_m2dMouthOpen = ";
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r11 = r7.m2dMouthOpen;	 Catch:{ all -> 0x012a }
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r10 = r10.toString();	 Catch:{ all -> 0x012a }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r8, r10);	 Catch:{ all -> 0x012a }
        r8 = TAG;	 Catch:{ all -> 0x012a }
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x012a }
        r10.<init>();	 Catch:{ all -> 0x012a }
        r11 = "_m2dEyeBlink = ";
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r11 = r7.m2dEyeBlink;	 Catch:{ all -> 0x012a }
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r10 = r10.toString();	 Catch:{ all -> 0x012a }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r8, r10);	 Catch:{ all -> 0x012a }
        r8 = TAG;	 Catch:{ all -> 0x012a }
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x012a }
        r10.<init>();	 Catch:{ all -> 0x012a }
        r11 = "_m2dEyebrowRaise = ";
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r11 = r7.m2dEyebrowRaise;	 Catch:{ all -> 0x012a }
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r10 = r10.toString();	 Catch:{ all -> 0x012a }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r8, r10);	 Catch:{ all -> 0x012a }
        r8 = TAG;	 Catch:{ all -> 0x012a }
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x012a }
        r10.<init>();	 Catch:{ all -> 0x012a }
        r11 = "_m2dHeadNod = ";
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r11 = r7.m2dHeadNod;	 Catch:{ all -> 0x012a }
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r10 = r10.toString();	 Catch:{ all -> 0x012a }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r8, r10);	 Catch:{ all -> 0x012a }
        r8 = TAG;	 Catch:{ all -> 0x012a }
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x012a }
        r10.<init>();	 Catch:{ all -> 0x012a }
        r11 = "_m2dHeadShake = ";
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r11 = r7.m2dHeadShake;	 Catch:{ all -> 0x012a }
        r10 = r10.append(r11);	 Catch:{ all -> 0x012a }
        r10 = r10.toString();	 Catch:{ all -> 0x012a }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r8, r10);	 Catch:{ all -> 0x012a }
        r8 = r7.m2dMouthOpen;	 Catch:{ all -> 0x012a }
        r3.mMouthOpen = r8;	 Catch:{ all -> 0x012a }
        r8 = r7.m2dEyeBlink;	 Catch:{ all -> 0x012a }
        r3.mEyeBlink = r8;	 Catch:{ all -> 0x012a }
        r8 = r7.m2dEyebrowRaise;	 Catch:{ all -> 0x012a }
        r3.mEyebrowRaise = r8;	 Catch:{ all -> 0x012a }
        r8 = r7.m2dHeadNod;	 Catch:{ all -> 0x012a }
        r3.mHeadNod = r8;	 Catch:{ all -> 0x012a }
        r8 = r7.m2dHeadShake;	 Catch:{ all -> 0x012a }
        r3.mHeadShake = r8;	 Catch:{ all -> 0x012a }
    L_0x0114:
        r4 = r4 + 1;
        goto L_0x004b;
    L_0x0118:
        r8 = move-exception;
        monitor-exit(r9);	 Catch:{ all -> 0x0118 }
        throw r8;
    L_0x011b:
        r2 = r3;
    L_0x011c:
        monitor-exit(r9);	 Catch:{ all -> 0x0127 }
        r8 = TAG;
        r9 = "getActionList out";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r8, r9);
        r3 = r2;
        goto L_0x0034;
    L_0x0127:
        r8 = move-exception;
    L_0x0128:
        monitor-exit(r9);	 Catch:{ all -> 0x0127 }
        throw r8;
    L_0x012a:
        r8 = move-exception;
        r2 = r3;
        goto L_0x0128;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.arcsoft.stickerlibrary.api.StickerApi.getActionList():com.arcsoft.stickerlibrary.sticker.StickerAction");
    }

    /* JADX WARNING: Missing block: B:13:0x005b, code:
            if (0 != r18.mPreviewCallbackCountForFPS) goto L_0x0065;
     */
    /* JADX WARNING: Missing block: B:14:0x005d, code:
            r18.mStartPreviewCallbackTimeForFPS = java.lang.System.currentTimeMillis();
     */
    /* JADX WARNING: Missing block: B:15:0x0065, code:
            r13 = r18.mEngineUILockObj;
     */
    /* JADX WARNING: Missing block: B:16:0x0069, code:
            monitor-enter(r13);
     */
    /* JADX WARNING: Missing block: B:19:0x006e, code:
            if (r18.mStickerJNI == null) goto L_0x021d;
     */
    /* JADX WARNING: Missing block: B:20:0x0070, code:
            com.arcsoft.stickerlibrary.utils.BenchmarkUtil.start("StickerApi_startProcess_cameraTwo_time");
            r6 = r19.getPlanes();
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "IIMAGEP w=" + r19.getWidth() + " ,h=" + r19.getHeight() + " ,size=" + r19.getPlanes().length);
            r5 = 0;
     */
    /* JADX WARNING: Missing block: B:22:0x00b4, code:
            if (r5 >= r6.length) goto L_0x0126;
     */
    /* JADX WARNING: Missing block: B:23:0x00b6, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "IIMAGEP rowStride=" + r6[r5].getRowStride() + " ,pixStride=" + r6[r5].getPixelStride() + " ,sie=" + r6[r5].getBuffer().remaining() + " ,capacity =" + r6[r5].getBuffer().capacity() + " ,limit =" + r6[r5].getBuffer().limit());
     */
    /* JADX WARNING: Missing block: B:24:0x0120, code:
            r5 = r5 + 1;
     */
    /* JADX WARNING: Missing block: B:30:?, code:
            r4 = new com.arcsoft.stickerlibrary.utils.ArcBuff();
            r4.mFormat = 2050;
            r4.mIsContinues = false;
            r4.mWidth = r19.getWidth();
            r4.mHeight = r19.getHeight();
            r8 = java.lang.System.currentTimeMillis();
            r4.yNativeBuff = r18.mStickerJNI.getDirectBufferAddress(r6[0].getBuffer());
            r4.vuNativeBuff = r18.mStickerJNI.getDirectBufferAddress(r6[2].getBuffer());
            r4.mPitches[0] = r6[0].getRowStride();
            r4.mPitches[1] = r6[2].getRowStride();
            r18.mCurrentOrientation = roundOrientation(r20, r18.mCurrentOrientation);
     */
    /* JADX WARNING: Missing block: B:31:0x0190, code:
            if (r18.mIsMirror == false) goto L_0x027c;
     */
    /* JADX WARNING: Missing block: B:32:0x0192, code:
            r18.mOutlineOrientation = ((r18.mOrientCamera - r18.mCurrentOrientation) + 360) % 360;
     */
    /* JADX WARNING: Missing block: B:33:0x01a3, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "outlineOrientation:" + r18.mOutlineOrientation + ",orientCamera=" + r18.mOrientCamera + ",orientSensor=" + r20 + ",mCurrentOrientation=" + r18.mCurrentOrientation);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "Get Address time : " + (java.lang.System.currentTimeMillis() - r8));
            r7 = r18.mStickerJNI.startProcess(r4, r18.mOutlineOrientation);
     */
    /* JADX WARNING: Missing block: B:34:0x0213, code:
            if (r7 != 0) goto L_0x0218;
     */
    /* JADX WARNING: Missing block: B:35:0x0215, code:
            callbackFaceStatus();
     */
    /* JADX WARNING: Missing block: B:36:0x0218, code:
            com.arcsoft.stickerlibrary.utils.BenchmarkUtil.stop("StickerApi_startProcess_cameraTwo_time");
     */
    /* JADX WARNING: Missing block: B:37:0x021d, code:
            monitor-exit(r13);
     */
    /* JADX WARNING: Missing block: B:38:0x021e, code:
            r18.mPreviewCallbackCountForFPS++;
     */
    /* JADX WARNING: Missing block: B:39:0x0231, code:
            if (r18.mPreviewCallbackCountForFPS < 20) goto L_0x0272;
     */
    /* JADX WARNING: Missing block: B:40:0x0233, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "Performance preview callback fps = " + ((1000.0d * ((double) r18.mPreviewCallbackCountForFPS)) / ((double) (java.lang.System.currentTimeMillis() - r18.mStartPreviewCallbackTimeForFPS))));
            r18.mPreviewCallbackCountForFPS = 0;
     */
    /* JADX WARNING: Missing block: B:41:0x0272, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "startProcess out");
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            r18.mOutlineOrientation = ((r18.mCurrentOrientation - r18.mOrientCamera) + 180) % 360;
     */
    /* JADX WARNING: Missing block: B:51:?, code:
            return r7;
     */
    public int startProcess(@android.support.annotation.NonNull android.media.Image r19, int r20) {
        /*
        r18 = this;
        r12 = TAG;
        r13 = new java.lang.StringBuilder;
        r13.<init>();
        r14 = "startProcess in ,this=";
        r13 = r13.append(r14);
        r0 = r18;
        r13 = r13.append(r0);
        r14 = " ,stickerApi_startProcess2_threadId=";
        r13 = r13.append(r14);
        r14 = java.lang.Thread.currentThread();
        r14 = r14.getId();
        r13 = r13.append(r14);
        r13 = r13.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r12, r13);
        r7 = 0;
        r12 = 35;
        r13 = r19.getFormat();
        if (r12 == r13) goto L_0x003d;
    L_0x0035:
        r12 = new java.lang.RuntimeException;
        r13 = "startProcess ,the format of Image must be ImageFormat.YUV_420_888";
        r12.<init>(r13);
        throw r12;
    L_0x003d:
        r0 = r18;
        r13 = r0.mEngineInitLockObj;
        monitor-enter(r13);
        r0 = r18;
        r12 = r0.mbInitFinish;	 Catch:{ all -> 0x0123 }
        if (r12 != 0) goto L_0x0052;
    L_0x0048:
        r12 = TAG;	 Catch:{ all -> 0x0123 }
        r14 = "startProcess_camera2-> StickerApi is not init.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogE(r12, r14);	 Catch:{ all -> 0x0123 }
        r12 = 1;
        monitor-exit(r13);	 Catch:{ all -> 0x0123 }
    L_0x0051:
        return r12;
    L_0x0052:
        monitor-exit(r13);	 Catch:{ all -> 0x0123 }
        r12 = 0;
        r0 = r18;
        r14 = r0.mPreviewCallbackCountForFPS;
        r12 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1));
        if (r12 != 0) goto L_0x0065;
    L_0x005d:
        r12 = java.lang.System.currentTimeMillis();
        r0 = r18;
        r0.mStartPreviewCallbackTimeForFPS = r12;
    L_0x0065:
        r0 = r18;
        r13 = r0.mEngineUILockObj;
        monitor-enter(r13);
        r0 = r18;
        r12 = r0.mStickerJNI;	 Catch:{ all -> 0x028f }
        if (r12 == 0) goto L_0x021d;
    L_0x0070:
        r12 = "StickerApi_startProcess_cameraTwo_time";
        com.arcsoft.stickerlibrary.utils.BenchmarkUtil.start(r12);	 Catch:{ all -> 0x028f }
        r6 = r19.getPlanes();	 Catch:{ all -> 0x028f }
        r12 = TAG;	 Catch:{ all -> 0x028f }
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x028f }
        r14.<init>();	 Catch:{ all -> 0x028f }
        r15 = "IIMAGEP w=";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = r19.getWidth();	 Catch:{ all -> 0x028f }
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = " ,h=";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = r19.getHeight();	 Catch:{ all -> 0x028f }
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = " ,size=";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = r19.getPlanes();	 Catch:{ all -> 0x028f }
        r15 = r15.length;	 Catch:{ all -> 0x028f }
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r14 = r14.toString();	 Catch:{ all -> 0x028f }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r12, r14);	 Catch:{ all -> 0x028f }
        r5 = 0;
    L_0x00b3:
        r12 = r6.length;	 Catch:{ all -> 0x028f }
        if (r5 >= r12) goto L_0x0126;
    L_0x00b6:
        r12 = TAG;	 Catch:{ all -> 0x028f }
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x028f }
        r14.<init>();	 Catch:{ all -> 0x028f }
        r15 = "IIMAGEP rowStride=";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = r6[r5];	 Catch:{ all -> 0x028f }
        r15 = r15.getRowStride();	 Catch:{ all -> 0x028f }
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = " ,pixStride=";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = r6[r5];	 Catch:{ all -> 0x028f }
        r15 = r15.getPixelStride();	 Catch:{ all -> 0x028f }
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = " ,sie=";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = r6[r5];	 Catch:{ all -> 0x028f }
        r15 = r15.getBuffer();	 Catch:{ all -> 0x028f }
        r15 = r15.remaining();	 Catch:{ all -> 0x028f }
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = " ,capacity =";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = r6[r5];	 Catch:{ all -> 0x028f }
        r15 = r15.getBuffer();	 Catch:{ all -> 0x028f }
        r15 = r15.capacity();	 Catch:{ all -> 0x028f }
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = " ,limit =";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = r6[r5];	 Catch:{ all -> 0x028f }
        r15 = r15.getBuffer();	 Catch:{ all -> 0x028f }
        r15 = r15.limit();	 Catch:{ all -> 0x028f }
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r14 = r14.toString();	 Catch:{ all -> 0x028f }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r12, r14);	 Catch:{ all -> 0x028f }
        r5 = r5 + 1;
        goto L_0x00b3;
    L_0x0123:
        r12 = move-exception;
        monitor-exit(r13);	 Catch:{ all -> 0x0123 }
        throw r12;
    L_0x0126:
        r4 = new com.arcsoft.stickerlibrary.utils.ArcBuff;	 Catch:{ all -> 0x028f }
        r4.<init>();	 Catch:{ all -> 0x028f }
        r12 = 2050; // 0x802 float:2.873E-42 double:1.013E-320;
        r4.mFormat = r12;	 Catch:{ all -> 0x028f }
        r12 = 0;
        r4.mIsContinues = r12;	 Catch:{ all -> 0x028f }
        r12 = r19.getWidth();	 Catch:{ all -> 0x028f }
        r4.mWidth = r12;	 Catch:{ all -> 0x028f }
        r12 = r19.getHeight();	 Catch:{ all -> 0x028f }
        r4.mHeight = r12;	 Catch:{ all -> 0x028f }
        r8 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x028f }
        r0 = r18;
        r12 = r0.mStickerJNI;	 Catch:{ all -> 0x028f }
        r14 = 0;
        r14 = r6[r14];	 Catch:{ all -> 0x028f }
        r14 = r14.getBuffer();	 Catch:{ all -> 0x028f }
        r14 = r12.getDirectBufferAddress(r14);	 Catch:{ all -> 0x028f }
        r4.yNativeBuff = r14;	 Catch:{ all -> 0x028f }
        r0 = r18;
        r12 = r0.mStickerJNI;	 Catch:{ all -> 0x028f }
        r14 = 2;
        r14 = r6[r14];	 Catch:{ all -> 0x028f }
        r14 = r14.getBuffer();	 Catch:{ all -> 0x028f }
        r14 = r12.getDirectBufferAddress(r14);	 Catch:{ all -> 0x028f }
        r4.vuNativeBuff = r14;	 Catch:{ all -> 0x028f }
        r12 = r4.mPitches;	 Catch:{ all -> 0x028f }
        r14 = 0;
        r15 = 0;
        r15 = r6[r15];	 Catch:{ all -> 0x028f }
        r15 = r15.getRowStride();	 Catch:{ all -> 0x028f }
        r12[r14] = r15;	 Catch:{ all -> 0x028f }
        r12 = r4.mPitches;	 Catch:{ all -> 0x028f }
        r14 = 1;
        r15 = 2;
        r15 = r6[r15];	 Catch:{ all -> 0x028f }
        r15 = r15.getRowStride();	 Catch:{ all -> 0x028f }
        r12[r14] = r15;	 Catch:{ all -> 0x028f }
        r0 = r18;
        r12 = r0.mCurrentOrientation;	 Catch:{ all -> 0x028f }
        r0 = r18;
        r1 = r20;
        r12 = r0.roundOrientation(r1, r12);	 Catch:{ all -> 0x028f }
        r0 = r18;
        r0.mCurrentOrientation = r12;	 Catch:{ all -> 0x028f }
        r0 = r18;
        r12 = r0.mIsMirror;	 Catch:{ all -> 0x028f }
        if (r12 == 0) goto L_0x027c;
    L_0x0192:
        r0 = r18;
        r12 = r0.mOrientCamera;	 Catch:{ all -> 0x028f }
        r0 = r18;
        r14 = r0.mCurrentOrientation;	 Catch:{ all -> 0x028f }
        r12 = r12 - r14;
        r12 = r12 + 360;
        r12 = r12 % 360;
        r0 = r18;
        r0.mOutlineOrientation = r12;	 Catch:{ all -> 0x028f }
    L_0x01a3:
        r12 = TAG;	 Catch:{ all -> 0x028f }
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x028f }
        r14.<init>();	 Catch:{ all -> 0x028f }
        r15 = "outlineOrientation:";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r0 = r18;
        r15 = r0.mOutlineOrientation;	 Catch:{ all -> 0x028f }
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = ",orientCamera=";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r0 = r18;
        r15 = r0.mOrientCamera;	 Catch:{ all -> 0x028f }
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r15 = ",orientSensor=";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r0 = r20;
        r14 = r14.append(r0);	 Catch:{ all -> 0x028f }
        r15 = ",mCurrentOrientation=";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r0 = r18;
        r15 = r0.mCurrentOrientation;	 Catch:{ all -> 0x028f }
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r14 = r14.toString();	 Catch:{ all -> 0x028f }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r12, r14);	 Catch:{ all -> 0x028f }
        r2 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x028f }
        r12 = TAG;	 Catch:{ all -> 0x028f }
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x028f }
        r14.<init>();	 Catch:{ all -> 0x028f }
        r15 = "Get Address time : ";
        r14 = r14.append(r15);	 Catch:{ all -> 0x028f }
        r16 = r2 - r8;
        r0 = r16;
        r14 = r14.append(r0);	 Catch:{ all -> 0x028f }
        r14 = r14.toString();	 Catch:{ all -> 0x028f }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r12, r14);	 Catch:{ all -> 0x028f }
        r0 = r18;
        r12 = r0.mStickerJNI;	 Catch:{ all -> 0x028f }
        r0 = r18;
        r14 = r0.mOutlineOrientation;	 Catch:{ all -> 0x028f }
        r7 = r12.startProcess(r4, r14);	 Catch:{ all -> 0x028f }
        if (r7 != 0) goto L_0x0218;
    L_0x0215:
        r18.callbackFaceStatus();	 Catch:{ all -> 0x028f }
    L_0x0218:
        r12 = "StickerApi_startProcess_cameraTwo_time";
        com.arcsoft.stickerlibrary.utils.BenchmarkUtil.stop(r12);	 Catch:{ all -> 0x028f }
    L_0x021d:
        monitor-exit(r13);	 Catch:{ all -> 0x028f }
        r0 = r18;
        r12 = r0.mPreviewCallbackCountForFPS;
        r14 = 1;
        r12 = r12 + r14;
        r0 = r18;
        r0.mPreviewCallbackCountForFPS = r12;
        r0 = r18;
        r12 = r0.mPreviewCallbackCountForFPS;
        r14 = 20;
        r12 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1));
        if (r12 < 0) goto L_0x0272;
    L_0x0233:
        r12 = java.lang.System.currentTimeMillis();
        r0 = r18;
        r14 = r0.mStartPreviewCallbackTimeForFPS;
        r10 = r12 - r14;
        r12 = TAG;
        r13 = new java.lang.StringBuilder;
        r13.<init>();
        r14 = "Performance preview callback fps = ";
        r13 = r13.append(r14);
        r14 = 4652007308841189376; // 0x408f400000000000 float:0.0 double:1000.0;
        r0 = r18;
        r0 = r0.mPreviewCallbackCountForFPS;
        r16 = r0;
        r0 = r16;
        r0 = (double) r0;
        r16 = r0;
        r14 = r14 * r16;
        r0 = (double) r10;
        r16 = r0;
        r14 = r14 / r16;
        r13 = r13.append(r14);
        r13 = r13.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r12, r13);
        r12 = 0;
        r0 = r18;
        r0.mPreviewCallbackCountForFPS = r12;
    L_0x0272:
        r12 = TAG;
        r13 = "startProcess out";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r12, r13);
        r12 = r7;
        goto L_0x0051;
    L_0x027c:
        r0 = r18;
        r12 = r0.mCurrentOrientation;	 Catch:{ all -> 0x028f }
        r0 = r18;
        r14 = r0.mOrientCamera;	 Catch:{ all -> 0x028f }
        r12 = r12 - r14;
        r12 = r12 + 180;
        r12 = r12 % 360;
        r0 = r18;
        r0.mOutlineOrientation = r12;	 Catch:{ all -> 0x028f }
        goto L_0x01a3;
    L_0x028f:
        r12 = move-exception;
        monitor-exit(r13);	 Catch:{ all -> 0x028f }
        throw r12;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.arcsoft.stickerlibrary.api.StickerApi.startProcess(android.media.Image, int):int");
    }

    /* JADX WARNING: Missing block: B:13:0x0058, code:
            if (0 != r12.mPreviewCallbackCountForFPS) goto L_0x0060;
     */
    /* JADX WARNING: Missing block: B:14:0x005a, code:
            r12.mStartPreviewCallbackTimeForFPS = java.lang.System.currentTimeMillis();
     */
    /* JADX WARNING: Missing block: B:15:0x0060, code:
            r5 = r12.mEngineUILockObj;
     */
    /* JADX WARNING: Missing block: B:16:0x0062, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            r12.mCurrentOrientation = roundOrientation(r14, r12.mCurrentOrientation);
     */
    /* JADX WARNING: Missing block: B:19:0x006d, code:
            if (r12.mIsMirror == false) goto L_0x0122;
     */
    /* JADX WARNING: Missing block: B:20:0x006f, code:
            r12.mOutlineOrientation = ((r12.mOrientCamera - r12.mCurrentOrientation) + 360) % 360;
     */
    /* JADX WARNING: Missing block: B:21:0x007a, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "outlineOrientation:" + r12.mOutlineOrientation + ",orientCamera=" + r12.mOrientCamera + ",orientSensor=" + r14 + ",mCurrentOrientation=" + r12.mCurrentOrientation);
     */
    /* JADX WARNING: Missing block: B:22:0x00b8, code:
            if (r12.mStickerJNI == null) goto L_0x00d8;
     */
    /* JADX WARNING: Missing block: B:23:0x00ba, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "startProcess Arc_Offscreen x");
            com.arcsoft.stickerlibrary.utils.BenchmarkUtil.start("StickerApi_startProcess_cameraOne_time");
            r0 = r12.mStickerJNI.startProcess(r1, r12.mOutlineOrientation);
     */
    /* JADX WARNING: Missing block: B:24:0x00ce, code:
            if (r0 != 0) goto L_0x00d3;
     */
    /* JADX WARNING: Missing block: B:25:0x00d0, code:
            callbackFaceStatus();
     */
    /* JADX WARNING: Missing block: B:26:0x00d3, code:
            com.arcsoft.stickerlibrary.utils.BenchmarkUtil.stop("StickerApi_startProcess_cameraOne_time");
     */
    /* JADX WARNING: Missing block: B:27:0x00d8, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:28:0x00d9, code:
            r12.mPreviewCallbackCountForFPS++;
     */
    /* JADX WARNING: Missing block: B:29:0x00e6, code:
            if (r12.mPreviewCallbackCountForFPS < 20) goto L_0x0115;
     */
    /* JADX WARNING: Missing block: B:30:0x00e8, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "Performance preview callback fps = " + ((1000.0d * ((double) r12.mPreviewCallbackCountForFPS)) / ((double) (java.lang.System.currentTimeMillis() - r12.mStartPreviewCallbackTimeForFPS))));
            r12.mPreviewCallbackCountForFPS = 0;
     */
    /* JADX WARNING: Missing block: B:31:0x0115, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "startProcess Arc_Offscreen out");
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            r12.mOutlineOrientation = ((r12.mCurrentOrientation - r12.mOrientCamera) + 180) % 360;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            return r0;
     */
    public int startProcess(@android.support.annotation.NonNull byte[] r13, int r14) {
        /*
        r12 = this;
        r10 = 0;
        if (r13 != 0) goto L_0x000c;
    L_0x0004:
        r4 = new java.lang.IllegalArgumentException;
        r5 = "frameData of frame never be null.";
        r4.<init>(r5);
        throw r4;
    L_0x000c:
        r4 = TAG;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "startProcess Arc_Offscreen in ,this=";
        r5 = r5.append(r6);
        r5 = r5.append(r12);
        r6 = " ,stickerApi_startProcess1_threadId=";
        r5 = r5.append(r6);
        r6 = java.lang.Thread.currentThread();
        r6 = r6.getId();
        r5 = r5.append(r6);
        r5 = r5.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r4, r5);
        r1 = new com.arcsoft.stickerlibrary.utils.ArcOffscreen;
        r4 = r12.mFrameWidth;
        r5 = r12.mFrameHeight;
        r6 = 2050; // 0x802 float:2.873E-42 double:1.013E-320;
        r1.<init>(r4, r5, r6, r13);
        r0 = 0;
        r5 = r12.mEngineInitLockObj;
        monitor-enter(r5);
        r4 = r12.mbInitFinish;	 Catch:{ all -> 0x011f }
        if (r4 != 0) goto L_0x0053;
    L_0x0049:
        r4 = TAG;	 Catch:{ all -> 0x011f }
        r6 = "startProcess_camera1-> StickerApi is not init.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogE(r4, r6);	 Catch:{ all -> 0x011f }
        r4 = 1;
        monitor-exit(r5);	 Catch:{ all -> 0x011f }
    L_0x0052:
        return r4;
    L_0x0053:
        monitor-exit(r5);	 Catch:{ all -> 0x011f }
        r4 = r12.mPreviewCallbackCountForFPS;
        r4 = (r10 > r4 ? 1 : (r10 == r4 ? 0 : -1));
        if (r4 != 0) goto L_0x0060;
    L_0x005a:
        r4 = java.lang.System.currentTimeMillis();
        r12.mStartPreviewCallbackTimeForFPS = r4;
    L_0x0060:
        r5 = r12.mEngineUILockObj;
        monitor-enter(r5);
        r4 = r12.mCurrentOrientation;	 Catch:{ all -> 0x012f }
        r4 = r12.roundOrientation(r14, r4);	 Catch:{ all -> 0x012f }
        r12.mCurrentOrientation = r4;	 Catch:{ all -> 0x012f }
        r4 = r12.mIsMirror;	 Catch:{ all -> 0x012f }
        if (r4 == 0) goto L_0x0122;
    L_0x006f:
        r4 = r12.mOrientCamera;	 Catch:{ all -> 0x012f }
        r6 = r12.mCurrentOrientation;	 Catch:{ all -> 0x012f }
        r4 = r4 - r6;
        r4 = r4 + 360;
        r4 = r4 % 360;
        r12.mOutlineOrientation = r4;	 Catch:{ all -> 0x012f }
    L_0x007a:
        r4 = TAG;	 Catch:{ all -> 0x012f }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x012f }
        r6.<init>();	 Catch:{ all -> 0x012f }
        r7 = "outlineOrientation:";
        r6 = r6.append(r7);	 Catch:{ all -> 0x012f }
        r7 = r12.mOutlineOrientation;	 Catch:{ all -> 0x012f }
        r6 = r6.append(r7);	 Catch:{ all -> 0x012f }
        r7 = ",orientCamera=";
        r6 = r6.append(r7);	 Catch:{ all -> 0x012f }
        r7 = r12.mOrientCamera;	 Catch:{ all -> 0x012f }
        r6 = r6.append(r7);	 Catch:{ all -> 0x012f }
        r7 = ",orientSensor=";
        r6 = r6.append(r7);	 Catch:{ all -> 0x012f }
        r6 = r6.append(r14);	 Catch:{ all -> 0x012f }
        r7 = ",mCurrentOrientation=";
        r6 = r6.append(r7);	 Catch:{ all -> 0x012f }
        r7 = r12.mCurrentOrientation;	 Catch:{ all -> 0x012f }
        r6 = r6.append(r7);	 Catch:{ all -> 0x012f }
        r6 = r6.toString();	 Catch:{ all -> 0x012f }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r4, r6);	 Catch:{ all -> 0x012f }
        r4 = r12.mStickerJNI;	 Catch:{ all -> 0x012f }
        if (r4 == 0) goto L_0x00d8;
    L_0x00ba:
        r4 = TAG;	 Catch:{ all -> 0x012f }
        r6 = "startProcess Arc_Offscreen x";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r4, r6);	 Catch:{ all -> 0x012f }
        r4 = "StickerApi_startProcess_cameraOne_time";
        com.arcsoft.stickerlibrary.utils.BenchmarkUtil.start(r4);	 Catch:{ all -> 0x012f }
        r4 = r12.mStickerJNI;	 Catch:{ all -> 0x012f }
        r6 = r12.mOutlineOrientation;	 Catch:{ all -> 0x012f }
        r0 = r4.startProcess(r1, r6);	 Catch:{ all -> 0x012f }
        if (r0 != 0) goto L_0x00d3;
    L_0x00d0:
        r12.callbackFaceStatus();	 Catch:{ all -> 0x012f }
    L_0x00d3:
        r4 = "StickerApi_startProcess_cameraOne_time";
        com.arcsoft.stickerlibrary.utils.BenchmarkUtil.stop(r4);	 Catch:{ all -> 0x012f }
    L_0x00d8:
        monitor-exit(r5);	 Catch:{ all -> 0x012f }
        r4 = r12.mPreviewCallbackCountForFPS;
        r6 = 1;
        r4 = r4 + r6;
        r12.mPreviewCallbackCountForFPS = r4;
        r4 = r12.mPreviewCallbackCountForFPS;
        r6 = 20;
        r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r4 < 0) goto L_0x0115;
    L_0x00e8:
        r4 = java.lang.System.currentTimeMillis();
        r6 = r12.mStartPreviewCallbackTimeForFPS;
        r2 = r4 - r6;
        r4 = TAG;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "Performance preview callback fps = ";
        r5 = r5.append(r6);
        r6 = 4652007308841189376; // 0x408f400000000000 float:0.0 double:1000.0;
        r8 = r12.mPreviewCallbackCountForFPS;
        r8 = (double) r8;
        r6 = r6 * r8;
        r8 = (double) r2;
        r6 = r6 / r8;
        r5 = r5.append(r6);
        r5 = r5.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r4, r5);
        r12.mPreviewCallbackCountForFPS = r10;
    L_0x0115:
        r4 = TAG;
        r5 = "startProcess Arc_Offscreen out";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r4, r5);
        r4 = r0;
        goto L_0x0052;
    L_0x011f:
        r4 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x011f }
        throw r4;
    L_0x0122:
        r4 = r12.mCurrentOrientation;	 Catch:{ all -> 0x012f }
        r6 = r12.mOrientCamera;	 Catch:{ all -> 0x012f }
        r4 = r4 - r6;
        r4 = r4 + 180;
        r4 = r4 % 360;
        r12.mOutlineOrientation = r4;	 Catch:{ all -> 0x012f }
        goto L_0x007a;
    L_0x012f:
        r4 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x012f }
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.arcsoft.stickerlibrary.api.StickerApi.startProcess(byte[], int):int");
    }

    /* JADX WARNING: Missing block: B:8:0x003d, code:
            if (r13 == 0) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:9:0x003f, code:
            if (r14 == 0) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:11:0x0045, code:
            if (r10.length() != 0) goto L_0x004c;
     */
    /* JADX WARNING: Missing block: B:18:0x004e, code:
            if (r9.mMediaManager == null) goto L_0x0058;
     */
    /* JADX WARNING: Missing block: B:20:0x0057, code:
            throw new java.lang.RuntimeException("Recording has been started already.");
     */
    /* JADX WARNING: Missing block: B:21:0x0058, code:
            if (r12 == 0) goto L_0x0085;
     */
    /* JADX WARNING: Missing block: B:23:0x005c, code:
            if (90 == r12) goto L_0x0085;
     */
    /* JADX WARNING: Missing block: B:25:0x0060, code:
            if (180 == r12) goto L_0x0085;
     */
    /* JADX WARNING: Missing block: B:27:0x0064, code:
            if (270 == r12) goto L_0x0085;
     */
    /* JADX WARNING: Missing block: B:29:0x0084, code:
            throw new java.lang.RuntimeException("StickerApi-> startRecording(...) screenOrientation = " + r12 + " is invalid");
     */
    /* JADX WARNING: Missing block: B:30:0x0085, code:
            r9.mRecordingListener = r11;
            r9.mCurrentDisplay = android.opengl.EGL14.eglGetCurrentDisplay();
            r9.mCurrentContext = android.opengl.EGL14.eglGetCurrentContext();
            r9.mCurrentSurface = android.opengl.EGL14.eglGetCurrentSurface(12378);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "default : dispaly=" + r9.mCurrentDisplay + " , context=" + r9.mCurrentContext + " , surface=" + r9.mCurrentSurface);
            r9.mMediaManager = new com.arcsoft.stickerlibrary.codec.MediaManager(r10, r13, r14, r9.mDegree, r9.mIsMirror, r12, r11);
            r9.mMediaManager.setEncoderCount(2);
            r9.mMediaManager.initVideoEncoderWithSharedContext(r9.mCurrentContext);
            r9.mMediaManager.initAudioEncoder();
            r9.mMediaManager.startRecording();
            r9.mIsRequestRecording = true;
            r9.mIsRequestPauseRecording = false;
            com.arcsoft.stickerlibrary.codec.CodecLog.enableLog(r9.mIsEnbaleLog);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "startRecording out");
     */
    /* JADX WARNING: Missing block: B:35:?, code:
            return 2;
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            return 0;
     */
    public int startRecording(@android.support.annotation.NonNull java.lang.String r10, com.arcsoft.stickerlibrary.api.StickerRecordingListener r11, int r12, @android.support.annotation.NonNull int r13, @android.support.annotation.NonNull int r14) {
        /*
        r9 = this;
        r0 = TAG;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "startRecording in , this";
        r1 = r1.append(r2);
        r1 = r1.append(r9);
        r2 = " ,stickerApi_startRecording_threadId=";
        r1 = r1.append(r2);
        r2 = java.lang.Thread.currentThread();
        r2 = r2.getId();
        r1 = r1.append(r2);
        r1 = r1.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r0, r1);
        r8 = 0;
        r1 = r9.mEngineInitLockObj;
        monitor-enter(r1);
        r0 = r9.mbInitFinish;	 Catch:{ all -> 0x0049 }
        if (r0 != 0) goto L_0x003c;
    L_0x0032:
        r0 = TAG;	 Catch:{ all -> 0x0049 }
        r2 = "startRecording-> StickerApi is not init.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogE(r0, r2);	 Catch:{ all -> 0x0049 }
        r8 = 1;
        monitor-exit(r1);	 Catch:{ all -> 0x0049 }
    L_0x003b:
        return r8;
    L_0x003c:
        monitor-exit(r1);	 Catch:{ all -> 0x0049 }
        if (r13 == 0) goto L_0x0047;
    L_0x003f:
        if (r14 == 0) goto L_0x0047;
    L_0x0041:
        r0 = r10.length();
        if (r0 != 0) goto L_0x004c;
    L_0x0047:
        r8 = 2;
        goto L_0x003b;
    L_0x0049:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0049 }
        throw r0;
    L_0x004c:
        r0 = r9.mMediaManager;
        if (r0 == 0) goto L_0x0058;
    L_0x0050:
        r0 = new java.lang.RuntimeException;
        r1 = "Recording has been started already.";
        r0.<init>(r1);
        throw r0;
    L_0x0058:
        if (r12 == 0) goto L_0x0085;
    L_0x005a:
        r0 = 90;
        if (r0 == r12) goto L_0x0085;
    L_0x005e:
        r0 = 180; // 0xb4 float:2.52E-43 double:8.9E-322;
        if (r0 == r12) goto L_0x0085;
    L_0x0062:
        r0 = 270; // 0x10e float:3.78E-43 double:1.334E-321;
        if (r0 == r12) goto L_0x0085;
    L_0x0066:
        r0 = new java.lang.RuntimeException;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "StickerApi-> startRecording(...) screenOrientation = ";
        r1 = r1.append(r2);
        r1 = r1.append(r12);
        r2 = " is invalid";
        r1 = r1.append(r2);
        r1 = r1.toString();
        r0.<init>(r1);
        throw r0;
    L_0x0085:
        r9.mRecordingListener = r11;
        r0 = android.opengl.EGL14.eglGetCurrentDisplay();
        r9.mCurrentDisplay = r0;
        r0 = android.opengl.EGL14.eglGetCurrentContext();
        r9.mCurrentContext = r0;
        r0 = 12378; // 0x305a float:1.7345E-41 double:6.1155E-320;
        r0 = android.opengl.EGL14.eglGetCurrentSurface(r0);
        r9.mCurrentSurface = r0;
        r0 = TAG;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "default : dispaly=";
        r1 = r1.append(r2);
        r2 = r9.mCurrentDisplay;
        r1 = r1.append(r2);
        r2 = " , context=";
        r1 = r1.append(r2);
        r2 = r9.mCurrentContext;
        r1 = r1.append(r2);
        r2 = " , surface=";
        r1 = r1.append(r2);
        r2 = r9.mCurrentSurface;
        r1 = r1.append(r2);
        r1 = r1.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r0, r1);
        r0 = new com.arcsoft.stickerlibrary.codec.MediaManager;
        r4 = r9.mDegree;
        r5 = r9.mIsMirror;
        r1 = r10;
        r2 = r13;
        r3 = r14;
        r6 = r12;
        r7 = r11;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7);
        r9.mMediaManager = r0;
        r0 = r9.mMediaManager;
        r1 = 2;
        r0.setEncoderCount(r1);
        r0 = r9.mMediaManager;
        r1 = r9.mCurrentContext;
        r0.initVideoEncoderWithSharedContext(r1);
        r0 = r9.mMediaManager;
        r0.initAudioEncoder();
        r0 = r9.mMediaManager;
        r0.startRecording();
        r0 = 1;
        r9.mIsRequestRecording = r0;
        r0 = 0;
        r9.mIsRequestPauseRecording = r0;
        r0 = r9.mIsEnbaleLog;
        com.arcsoft.stickerlibrary.codec.CodecLog.enableLog(r0);
        r0 = TAG;
        r1 = "startRecording out";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r0, r1);
        goto L_0x003b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.arcsoft.stickerlibrary.api.StickerApi.startRecording(java.lang.String, com.arcsoft.stickerlibrary.api.StickerRecordingListener, int, int, int):int");
    }

    /* JADX WARNING: Missing block: B:9:0x003e, code:
            if (r6.mMediaManager == null) goto L_0x0057;
     */
    /* JADX WARNING: Missing block: B:11:0x0042, code:
            if (r6.mIsRequestRecording == false) goto L_0x0057;
     */
    /* JADX WARNING: Missing block: B:12:0x0044, code:
            r6.mIsRequestPauseRecording = true;
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "pauseRecording out");
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            return r6.mMediaManager.pauseRecording();
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            return 5;
     */
    public int pauseRecording() {
        /*
        r6 = this;
        r0 = 1;
        r1 = TAG;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "pauseRecording in ,this=";
        r2 = r2.append(r3);
        r2 = r2.append(r6);
        r3 = " ,stickerApi_pauseRecording_threadId=";
        r2 = r2.append(r3);
        r3 = java.lang.Thread.currentThread();
        r4 = r3.getId();
        r2 = r2.append(r4);
        r2 = r2.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r1, r2);
        r1 = r6.mEngineInitLockObj;
        monitor-enter(r1);
        r2 = r6.mbInitFinish;	 Catch:{ all -> 0x0054 }
        if (r2 != 0) goto L_0x003b;
    L_0x0032:
        r2 = TAG;	 Catch:{ all -> 0x0054 }
        r3 = "pauseRecording-> StickerApi is not init.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogE(r2, r3);	 Catch:{ all -> 0x0054 }
        monitor-exit(r1);	 Catch:{ all -> 0x0054 }
    L_0x003a:
        return r0;
    L_0x003b:
        monitor-exit(r1);	 Catch:{ all -> 0x0054 }
        r1 = r6.mMediaManager;
        if (r1 == 0) goto L_0x0057;
    L_0x0040:
        r1 = r6.mIsRequestRecording;
        if (r1 == 0) goto L_0x0057;
    L_0x0044:
        r6.mIsRequestPauseRecording = r0;
        r0 = TAG;
        r1 = "pauseRecording out";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r0, r1);
        r0 = r6.mMediaManager;
        r0 = r0.pauseRecording();
        goto L_0x003a;
    L_0x0054:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0054 }
        throw r0;
    L_0x0057:
        r0 = 5;
        goto L_0x003a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.arcsoft.stickerlibrary.api.StickerApi.pauseRecording():int");
    }

    /* JADX WARNING: Missing block: B:9:0x003e, code:
            if (r6.mMediaManager == null) goto L_0x0058;
     */
    /* JADX WARNING: Missing block: B:11:0x0042, code:
            if (r6.mIsRequestRecording == false) goto L_0x0058;
     */
    /* JADX WARNING: Missing block: B:12:0x0044, code:
            r0 = r6.mMediaManager.resumeRecording();
            r6.mIsRequestPauseRecording = false;
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "resumeRecording out");
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            return 5;
     */
    public int resumeRecording() {
        /*
        r6 = this;
        r1 = TAG;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "resumeRecording in , this=";
        r2 = r2.append(r3);
        r2 = r2.append(r6);
        r3 = " ,stickerApi_resumeRecording_threadId=";
        r2 = r2.append(r3);
        r3 = java.lang.Thread.currentThread();
        r4 = r3.getId();
        r2 = r2.append(r4);
        r2 = r2.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r1, r2);
        r2 = r6.mEngineInitLockObj;
        monitor-enter(r2);
        r1 = r6.mbInitFinish;	 Catch:{ all -> 0x0055 }
        if (r1 != 0) goto L_0x003b;
    L_0x0031:
        r1 = TAG;	 Catch:{ all -> 0x0055 }
        r3 = "resumeRecording-> StickerApi is not init.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogE(r1, r3);	 Catch:{ all -> 0x0055 }
        r0 = 1;
        monitor-exit(r2);	 Catch:{ all -> 0x0055 }
    L_0x003a:
        return r0;
    L_0x003b:
        monitor-exit(r2);	 Catch:{ all -> 0x0055 }
        r1 = r6.mMediaManager;
        if (r1 == 0) goto L_0x0058;
    L_0x0040:
        r1 = r6.mIsRequestRecording;
        if (r1 == 0) goto L_0x0058;
    L_0x0044:
        r1 = r6.mMediaManager;
        r0 = r1.resumeRecording();
        r1 = 0;
        r6.mIsRequestPauseRecording = r1;
        r1 = TAG;
        r2 = "resumeRecording out";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r1, r2);
        goto L_0x003a;
    L_0x0055:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0055 }
        throw r1;
    L_0x0058:
        r0 = 5;
        goto L_0x003a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.arcsoft.stickerlibrary.api.StickerApi.resumeRecording():int");
    }

    public int stopRecording() {
        LogUtil.LogD(TAG, "stopRecording in ,this=" + this + " ,stickerApi_stopRecording_threadId=" + Thread.currentThread().getId());
        int ret = 0;
        synchronized (this.mEngineInitLockObj) {
            if (this.mbInitFinish) {
                if (this.mIsRequestRecording) {
                    if (this.mMediaManager != null) {
                        resumeRecording();
                        this.mMediaManager.stopRecording();
                        this.mMediaManager = null;
                    }
                    this.mIsRequestRecording = false;
                    this.mMaxRecordingDuration = 0;
                    this.mRecordTimeCallbackCount = 0;
                    this.mMaxRecordingFileSize = 0;
                    LogUtil.LogD(TAG, "stopRecording out");
                }
            } else {
                LogUtil.LogE(TAG, "stopRecording-> StickerApi is not init.");
                ret = 1;
            }
        }
        return ret;
    }

    public void capture(StickerCaptureCallback cb) {
        LogUtil.LogD(TAG, "capture in,this=" + this + " ,stickerApi_capture_threadId=" + Thread.currentThread().getId());
        synchronized (this.mEngineInitLockObj) {
            if (this.mbInitFinish) {
                this.mStickerCaptureCB = cb;
                this.mIsRequestCapture = true;
                LogUtil.LogD(TAG, "capture out");
                return;
            }
            LogUtil.LogE(TAG, "capture-> StickerApi is not init.");
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0051, code:
            r24 = r25.mEngineGLLockObj;
     */
    /* JADX WARNING: Missing block: B:9:0x0057, code:
            monitor-enter(r24);
     */
    /* JADX WARNING: Missing block: B:11:?, code:
            com.arcsoft.stickerlibrary.utils.BenchmarkUtil.start("StickerApi_startRender_time");
     */
    /* JADX WARNING: Missing block: B:12:0x0061, code:
            if (r25.mStickerJNI == null) goto L_0x0117;
     */
    /* JADX WARNING: Missing block: B:13:0x0063, code:
            r14 = r25.mStickerJNI.startRender();
            r13 = android.support.p000v4.internal.view.SupportMenu.USER_MASK & r14;
            r22 = r14 >> 16;
            r14 = r13;
     */
    /* JADX WARNING: Missing block: B:14:0x0073, code:
            if (r13 != 0) goto L_0x013f;
     */
    /* JADX WARNING: Missing block: B:15:0x0075, code:
            if (r22 <= 0) goto L_0x013f;
     */
    /* JADX WARNING: Missing block: B:16:0x0077, code:
            r12 = true;
     */
    /* JADX WARNING: Missing block: B:17:0x0078, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "result = " + r13 + " ,textureId=" + r22 + " ,mIsRequestCapture=" + r25.mIsRequestCapture + " , isTetxureIdValid=" + r12);
     */
    /* JADX WARNING: Missing block: B:18:0x00b8, code:
            if (r25.mIsRequestCapture == false) goto L_0x00c1;
     */
    /* JADX WARNING: Missing block: B:19:0x00ba, code:
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "get capture request.");
     */
    /* JADX WARNING: Missing block: B:21:0x00c5, code:
            if (r25.mIsRequestRecording == false) goto L_0x0142;
     */
    /* JADX WARNING: Missing block: B:23:0x00cb, code:
            if (r25.mIsRequestPauseRecording != false) goto L_0x0142;
     */
    /* JADX WARNING: Missing block: B:24:0x00cd, code:
            if (r12 == false) goto L_0x0142;
     */
    /* JADX WARNING: Missing block: B:26:0x00d3, code:
            if (r25.mMediaManager == null) goto L_0x0117;
     */
    /* JADX WARNING: Missing block: B:27:0x00d5, code:
            r25.mMediaManager.makeCurrent();
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "makeCuttent ok.");
            r25.mMediaManager.drawSurfaceWithTextureId(r22);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "drawSurfaceWithTextureId ok.");
            controlRecordingProcess();
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "controlRecordingProcess ok.");
            android.opengl.EGL14.eglMakeCurrent(r25.mCurrentDisplay, r25.mCurrentSurface, r25.mCurrentSurface, r25.mCurrentContext);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "eglMakeCurrent back ok.");
     */
    /* JADX WARNING: Missing block: B:28:0x0117, code:
            monitor-exit(r24);
     */
    /* JADX WARNING: Missing block: B:29:0x0118, code:
            com.arcsoft.stickerlibrary.utils.BenchmarkUtil.stop("StickerApi_startRender_time");
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "startRender out , glGetError=" + android.opengl.GLES20.glGetError());
     */
    /* JADX WARNING: Missing block: B:34:0x013f, code:
            r12 = false;
     */
    /* JADX WARNING: Missing block: B:37:0x0146, code:
            if (r25.mIsRequestCapture == false) goto L_0x0117;
     */
    /* JADX WARNING: Missing block: B:38:0x0148, code:
            if (r12 == false) goto L_0x0117;
     */
    /* JADX WARNING: Missing block: B:39:0x014a, code:
            r16 = java.lang.System.currentTimeMillis();
            r9 = android.opengl.EGL14.eglGetCurrentDisplay();
            r10 = android.opengl.EGL14.eglGetCurrentSurface(12377);
            r23 = new int[1];
            r11 = new int[1];
            android.opengl.EGL14.eglQuerySurface(r9, r10, 12375, r23, 0);
            android.opengl.EGL14.eglQuerySurface(r9, r10, 12374, r11, 0);
            r4 = r23[0];
            r5 = r11[0];
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "StickerApi_capture_getEGLState_time=" + (java.lang.System.currentTimeMillis() - r16));
            r8 = java.nio.ByteBuffer.allocateDirect((r4 * r5) * 4);
            r8.order(java.nio.ByteOrder.LITTLE_ENDIAN);
            r18 = java.lang.System.currentTimeMillis();
            android.opengl.GLES20.glReadPixels(0, 0, r4, r5, 6408, 5121, r8);
            com.arcsoft.stickerlibrary.utils.LogUtil.LogD(TAG, "StickerApi_capture_time=" + (java.lang.System.currentTimeMillis() - r18));
            r25.mIsRequestCapture = false;
            new com.arcsoft.stickerlibrary.api.StickerApi.C16521(r25, "saveImageThread").start();
     */
    /* JADX WARNING: Missing block: B:47:?, code:
            return r14;
     */
    public int startRender() {
        /*
        r25 = this;
        r2 = TAG;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r6 = "startRender in , glGetError=";
        r3 = r3.append(r6);
        r6 = android.opengl.GLES20.glGetError();
        r3 = r3.append(r6);
        r6 = " ,this=";
        r3 = r3.append(r6);
        r0 = r25;
        r3 = r3.append(r0);
        r6 = " ,stickerApi_startRender_threadId=";
        r3 = r3.append(r6);
        r6 = java.lang.Thread.currentThread();
        r6 = r6.getId();
        r3 = r3.append(r6);
        r3 = r3.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);
        r14 = 0;
        r0 = r25;
        r3 = r0.mEngineInitLockObj;
        monitor-enter(r3);
        r0 = r25;
        r2 = r0.mbInitFinish;	 Catch:{ all -> 0x013c }
        if (r2 != 0) goto L_0x0050;
    L_0x0046:
        r2 = TAG;	 Catch:{ all -> 0x013c }
        r6 = "startRender-> StickerApi is not init.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogE(r2, r6);	 Catch:{ all -> 0x013c }
        r2 = 5;
        monitor-exit(r3);	 Catch:{ all -> 0x013c }
    L_0x004f:
        return r2;
    L_0x0050:
        monitor-exit(r3);	 Catch:{ all -> 0x013c }
        r0 = r25;
        r0 = r0.mEngineGLLockObj;
        r24 = r0;
        monitor-enter(r24);
        r2 = "StickerApi_startRender_time";
        com.arcsoft.stickerlibrary.utils.BenchmarkUtil.start(r2);	 Catch:{ all -> 0x01dd }
        r0 = r25;
        r2 = r0.mStickerJNI;	 Catch:{ all -> 0x01dd }
        if (r2 == 0) goto L_0x0117;
    L_0x0063:
        r0 = r25;
        r2 = r0.mStickerJNI;	 Catch:{ all -> 0x01dd }
        r14 = r2.startRender();	 Catch:{ all -> 0x01dd }
        r2 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        r13 = r2 & r14;
        r22 = r14 >> 16;
        r14 = r13;
        if (r13 != 0) goto L_0x013f;
    L_0x0075:
        if (r22 <= 0) goto L_0x013f;
    L_0x0077:
        r12 = 1;
    L_0x0078:
        r2 = TAG;	 Catch:{ all -> 0x01dd }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01dd }
        r3.<init>();	 Catch:{ all -> 0x01dd }
        r6 = "result = ";
        r3 = r3.append(r6);	 Catch:{ all -> 0x01dd }
        r3 = r3.append(r13);	 Catch:{ all -> 0x01dd }
        r6 = " ,textureId=";
        r3 = r3.append(r6);	 Catch:{ all -> 0x01dd }
        r0 = r22;
        r3 = r3.append(r0);	 Catch:{ all -> 0x01dd }
        r6 = " ,mIsRequestCapture=";
        r3 = r3.append(r6);	 Catch:{ all -> 0x01dd }
        r0 = r25;
        r6 = r0.mIsRequestCapture;	 Catch:{ all -> 0x01dd }
        r3 = r3.append(r6);	 Catch:{ all -> 0x01dd }
        r6 = " , isTetxureIdValid=";
        r3 = r3.append(r6);	 Catch:{ all -> 0x01dd }
        r3 = r3.append(r12);	 Catch:{ all -> 0x01dd }
        r3 = r3.toString();	 Catch:{ all -> 0x01dd }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);	 Catch:{ all -> 0x01dd }
        r0 = r25;
        r2 = r0.mIsRequestCapture;	 Catch:{ all -> 0x01dd }
        if (r2 == 0) goto L_0x00c1;
    L_0x00ba:
        r2 = TAG;	 Catch:{ all -> 0x01dd }
        r3 = "get capture request.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);	 Catch:{ all -> 0x01dd }
    L_0x00c1:
        r0 = r25;
        r2 = r0.mIsRequestRecording;	 Catch:{ all -> 0x01dd }
        if (r2 == 0) goto L_0x0142;
    L_0x00c7:
        r0 = r25;
        r2 = r0.mIsRequestPauseRecording;	 Catch:{ all -> 0x01dd }
        if (r2 != 0) goto L_0x0142;
    L_0x00cd:
        if (r12 == 0) goto L_0x0142;
    L_0x00cf:
        r0 = r25;
        r2 = r0.mMediaManager;	 Catch:{ all -> 0x01dd }
        if (r2 == 0) goto L_0x0117;
    L_0x00d5:
        r0 = r25;
        r2 = r0.mMediaManager;	 Catch:{ all -> 0x01dd }
        r2.makeCurrent();	 Catch:{ all -> 0x01dd }
        r2 = TAG;	 Catch:{ all -> 0x01dd }
        r3 = "makeCuttent ok.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);	 Catch:{ all -> 0x01dd }
        r0 = r25;
        r2 = r0.mMediaManager;	 Catch:{ all -> 0x01dd }
        r0 = r22;
        r2.drawSurfaceWithTextureId(r0);	 Catch:{ all -> 0x01dd }
        r2 = TAG;	 Catch:{ all -> 0x01dd }
        r3 = "drawSurfaceWithTextureId ok.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);	 Catch:{ all -> 0x01dd }
        r25.controlRecordingProcess();	 Catch:{ all -> 0x01dd }
        r2 = TAG;	 Catch:{ all -> 0x01dd }
        r3 = "controlRecordingProcess ok.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);	 Catch:{ all -> 0x01dd }
        r0 = r25;
        r2 = r0.mCurrentDisplay;	 Catch:{ all -> 0x01dd }
        r0 = r25;
        r3 = r0.mCurrentSurface;	 Catch:{ all -> 0x01dd }
        r0 = r25;
        r6 = r0.mCurrentSurface;	 Catch:{ all -> 0x01dd }
        r0 = r25;
        r7 = r0.mCurrentContext;	 Catch:{ all -> 0x01dd }
        android.opengl.EGL14.eglMakeCurrent(r2, r3, r6, r7);	 Catch:{ all -> 0x01dd }
        r2 = TAG;	 Catch:{ all -> 0x01dd }
        r3 = "eglMakeCurrent back ok.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);	 Catch:{ all -> 0x01dd }
    L_0x0117:
        monitor-exit(r24);	 Catch:{ all -> 0x01dd }
        r2 = "StickerApi_startRender_time";
        com.arcsoft.stickerlibrary.utils.BenchmarkUtil.stop(r2);
        r2 = TAG;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r6 = "startRender out , glGetError=";
        r3 = r3.append(r6);
        r6 = android.opengl.GLES20.glGetError();
        r3 = r3.append(r6);
        r3 = r3.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);
        r2 = r14;
        goto L_0x004f;
    L_0x013c:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x013c }
        throw r2;
    L_0x013f:
        r12 = 0;
        goto L_0x0078;
    L_0x0142:
        r0 = r25;
        r2 = r0.mIsRequestCapture;	 Catch:{ all -> 0x01dd }
        if (r2 == 0) goto L_0x0117;
    L_0x0148:
        if (r12 == 0) goto L_0x0117;
    L_0x014a:
        r16 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x01dd }
        r9 = android.opengl.EGL14.eglGetCurrentDisplay();	 Catch:{ all -> 0x01dd }
        r2 = 12377; // 0x3059 float:1.7344E-41 double:6.115E-320;
        r10 = android.opengl.EGL14.eglGetCurrentSurface(r2);	 Catch:{ all -> 0x01dd }
        r2 = 1;
        r0 = new int[r2];	 Catch:{ all -> 0x01dd }
        r23 = r0;
        r2 = 1;
        r11 = new int[r2];	 Catch:{ all -> 0x01dd }
        r2 = 12375; // 0x3057 float:1.7341E-41 double:6.114E-320;
        r3 = 0;
        r0 = r23;
        android.opengl.EGL14.eglQuerySurface(r9, r10, r2, r0, r3);	 Catch:{ all -> 0x01dd }
        r2 = 12374; // 0x3056 float:1.734E-41 double:6.1136E-320;
        r3 = 0;
        android.opengl.EGL14.eglQuerySurface(r9, r10, r2, r11, r3);	 Catch:{ all -> 0x01dd }
        r2 = 0;
        r4 = r23[r2];	 Catch:{ all -> 0x01dd }
        r2 = 0;
        r5 = r11[r2];	 Catch:{ all -> 0x01dd }
        r20 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x01dd }
        r2 = TAG;	 Catch:{ all -> 0x01dd }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01dd }
        r3.<init>();	 Catch:{ all -> 0x01dd }
        r6 = "StickerApi_capture_getEGLState_time=";
        r3 = r3.append(r6);	 Catch:{ all -> 0x01dd }
        r6 = r20 - r16;
        r3 = r3.append(r6);	 Catch:{ all -> 0x01dd }
        r3 = r3.toString();	 Catch:{ all -> 0x01dd }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);	 Catch:{ all -> 0x01dd }
        r2 = r4 * r5;
        r2 = r2 * 4;
        r8 = java.nio.ByteBuffer.allocateDirect(r2);	 Catch:{ all -> 0x01dd }
        r2 = java.nio.ByteOrder.LITTLE_ENDIAN;	 Catch:{ all -> 0x01dd }
        r8.order(r2);	 Catch:{ all -> 0x01dd }
        r18 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x01dd }
        r2 = 0;
        r3 = 0;
        r6 = 6408; // 0x1908 float:8.98E-42 double:3.166E-320;
        r7 = 5121; // 0x1401 float:7.176E-42 double:2.53E-320;
        android.opengl.GLES20.glReadPixels(r2, r3, r4, r5, r6, r7, r8);	 Catch:{ all -> 0x01dd }
        r2 = TAG;	 Catch:{ all -> 0x01dd }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01dd }
        r3.<init>();	 Catch:{ all -> 0x01dd }
        r6 = "StickerApi_capture_time=";
        r3 = r3.append(r6);	 Catch:{ all -> 0x01dd }
        r6 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x01dd }
        r6 = r6 - r18;
        r3 = r3.append(r6);	 Catch:{ all -> 0x01dd }
        r3 = r3.toString();	 Catch:{ all -> 0x01dd }
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r2, r3);	 Catch:{ all -> 0x01dd }
        r2 = 0;
        r0 = r25;
        r0.mIsRequestCapture = r2;	 Catch:{ all -> 0x01dd }
        r15 = new com.arcsoft.stickerlibrary.api.StickerApi$1;	 Catch:{ all -> 0x01dd }
        r2 = "saveImageThread";
        r0 = r25;
        r15.<init>(r2, r8);	 Catch:{ all -> 0x01dd }
        r15.start();	 Catch:{ all -> 0x01dd }
        goto L_0x0117;
    L_0x01dd:
        r2 = move-exception;
        monitor-exit(r24);	 Catch:{ all -> 0x01dd }
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.arcsoft.stickerlibrary.api.StickerApi.startRender():int");
    }

    public FaceResult getFaceResult() {
        FaceResult result;
        LogUtil.LogD(TAG, "getFaceResult in, this=" + this + " ,stickerApi_getFaceResult_threadId=" + Thread.currentThread().getId());
        synchronized (this.mEngineInitLockObj) {
            if (this.mbInitFinish) {
                result = null;
                FaceStatus status = getFaceStatus();
                if (status != null) {
                    result = new FaceResult();
                    result.setFaceCount(status.faceCount);
                }
                LogUtil.LogD(TAG, "getFaceResult out");
            } else {
                LogUtil.LogE(TAG, "getFaceResult-> StickerApi is not init.");
                result = null;
            }
        }
        return result;
    }

    public int uninit() {
        LogUtil.LogD(TAG, "uninit in ,glGetError=" + GLES20.glGetError() + " ,this=" + this + " ,stickerApi_uninit_threadId=" + Thread.currentThread().getId());
        BenchmarkUtil.start("StickerApi_uninit");
        this.mIsStickerRunning = false;
        synchronized (this.mEngineInitLockObj) {
            this.mbInitFinish = false;
        }
        stopRecording();
        this.mIsRequestCapture = false;
        synchronized (this.mVisitQueueMonitor) {
            this.mVisitQueueMonitor.notifyAll();
        }
        if (this.mSetTemplateThread != null) {
            try {
                this.mSetTemplateThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.mSetTemplateThread = null;
        }
        synchronized (this.mEngineUILockObj) {
            synchronized (this.mEngineGLLockObj) {
                if (this.mStickerJNI != null) {
                    LogUtil.LogD(TAG, "mStickerJNI.uninit() in");
                    this.mStickerJNI.uninit();
                    this.mStickerJNI = null;
                    LogUtil.LogD(TAG, "mStickerJNI.uninit() out");
                }
            }
        }
        BenchmarkUtil.stop("StickerApi_uninit");
        LogUtil.LogD(TAG, "uninit out , glGetError=" + GLES20.glGetError());
        return 0;
    }

    public int setMaxDuration(long duration) {
        int ret = 0;
        LogUtil.LogD(TAG, "setMaxDuration in ,glGetError=" + GLES20.glGetError() + " ,this=" + this + " ,stickerApi_setMaxDuration_threadId=" + Thread.currentThread().getId());
        if (duration <= 0) {
            ret = 2;
        } else {
            this.mMaxRecordingDuration = duration;
        }
        LogUtil.LogD(TAG, "setMaxDuration out , ret = " + ret + " mMaxRecordingDuration=" + this.mMaxRecordingDuration);
        return ret;
    }

    public int setMaxFileSize(long size) {
        int ret = 0;
        LogUtil.LogD(TAG, "setMaxFileSize in ,glGetError=" + GLES20.glGetError() + " ,this=" + this + " ,stickerApi_setMaxFileSize_threadId=" + Thread.currentThread().getId());
        if (size <= 0) {
            ret = 2;
        } else {
            this.mMaxRecordingFileSize = size;
        }
        LogUtil.LogD(TAG, "setMaxFileSize out , ret = " + ret + " mMaxRecordingFileSize=" + this.mMaxRecordingFileSize);
        return ret;
    }

    /* JADX WARNING: Missing block: B:9:0x003e, code:
            if (r4.mStickerJNI == null) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:10:0x0040, code:
            r4.mStickerJNI.setDefaultBeautyParameter(r5, r6);
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            return 5;
     */
    public int setDefaultBeautyValue(int r5, int r6) {
        /*
        r4 = this;
        r0 = TAG;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "setDefaultBeautyValue in, this=";
        r1 = r1.append(r2);
        r1 = r1.append(r4);
        r2 = " ,setDefaultBeautyValue_threadId=";
        r1 = r1.append(r2);
        r2 = java.lang.Thread.currentThread();
        r2 = r2.getId();
        r1 = r1.append(r2);
        r1 = r1.toString();
        com.arcsoft.stickerlibrary.utils.LogUtil.LogD(r0, r1);
        r1 = r4.mEngineInitLockObj;
        monitor-enter(r1);
        r0 = r4.mbInitFinish;	 Catch:{ all -> 0x0047 }
        if (r0 != 0) goto L_0x003b;
    L_0x0031:
        r0 = TAG;	 Catch:{ all -> 0x0047 }
        r2 = "setDefaultBeautyValue-> StickerApi is not init.";
        com.arcsoft.stickerlibrary.utils.LogUtil.LogE(r0, r2);	 Catch:{ all -> 0x0047 }
        r0 = 1;
        monitor-exit(r1);	 Catch:{ all -> 0x0047 }
    L_0x003a:
        return r0;
    L_0x003b:
        monitor-exit(r1);	 Catch:{ all -> 0x0047 }
        r0 = r4.mStickerJNI;
        if (r0 == 0) goto L_0x004a;
    L_0x0040:
        r0 = r4.mStickerJNI;
        r0.setDefaultBeautyParameter(r5, r6);
        r0 = 0;
        goto L_0x003a;
    L_0x0047:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0047 }
        throw r0;
    L_0x004a:
        r0 = 5;
        goto L_0x003a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.arcsoft.stickerlibrary.api.StickerApi.setDefaultBeautyValue(int, int):int");
    }

    private int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation;
        if (orientationHistory == -1) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            changeOrientation = Math.min(dist, 360 - dist) >= 50;
        }
        if (changeOrientation) {
            return (((orientation + 45) / 90) * 90) % 360;
        }
        return orientationHistory;
    }

    private void startSetTemplateThread() {
        if (this.mSetTemplateThread != null) {
            throw new RuntimeException("setTemplate thread already started.");
        }
        this.mSetTemplateThread = new Thread("setTemplate") {
            public void run() {
                super.run();
                while (StickerApi.this.mIsStickerRunning) {
                    TemplateItem templateItem;
                    synchronized (StickerApi.this.mVisitQueueMonitor) {
                        if (StickerApi.this.mTemplatesQueue.isEmpty()) {
                            try {
                                StickerApi.this.mVisitQueueMonitor.wait();
                            } catch (InterruptedException e) {
                                LogUtil.LogE(StickerApi.TAG, "startSetTemplateThread -> setTempate thread is interrupted.");
                            }
                        }
                        templateItem = (TemplateItem) StickerApi.this.mTemplatesQueue.poll();
                    }
                    if (templateItem != null) {
                        ArrayList<SuperParams> pendingXmlConfigList = new ArrayList();
                        ArrayList<SuperParams> configListFromQueue = templateItem.getConfigList();
                        if (configListFromQueue == null || configListFromQueue.size() == 0) {
                            pendingXmlConfigList = null;
                        } else {
                            Iterator it = configListFromQueue.iterator();
                            while (it.hasNext()) {
                                pendingXmlConfigList.add((SuperParams) it.next());
                            }
                        }
                        String configXmlPath = templateItem.getConfigPath();
                        if (configXmlPath != null) {
                            int lastSlashIndex = configXmlPath.lastIndexOf(File.separator);
                            StickerApi.this.mCurrentResourceName = configXmlPath.substring(configXmlPath.lastIndexOf(File.separator, lastSlashIndex - 1) + 1, lastSlashIndex);
                        }
                        if (StickerApi.this.mStickerJNI != null) {
                            BenchmarkUtil.start("jni_setTemplate_setTemplate");
                            if (!(StickerApi.this.mStickerJNI.setTemplate(pendingXmlConfigList) == 0 || StickerApi.this.mStickerInfoListener == null)) {
                                StickerApi.this.mStickerInfoListener.OnInfoListener(StickerMessage.MSG_STICKER_RESOURCE_BROKEN, StickerApi.this.mCurrentResourceName);
                                LogUtil.LogD(StickerApi.TAG, "startSetTemplateThread_resource_broken");
                            }
                            BenchmarkUtil.stop("jni_setTemplate_setTemplate");
                        }
                    }
                }
            }
        };
        this.mSetTemplateThread.start();
    }

    private FaceStatus getFaceStatus() {
        LogUtil.LogD(TAG, "getFaceStatus in, this=" + this + " ,stickerApi_getFaceStatus_threadId=" + Thread.currentThread().getId());
        FaceStatus faceStatus = null;
        synchronized (this.mEngineGLLockObj) {
            if (this.mStickerJNI != null) {
                faceStatus = this.mStickerJNI.getFaceStatus();
            }
        }
        LogUtil.LogD(TAG, "getFaceStatus out");
        return faceStatus;
    }

    private ArrayList<SuperParams> xmlParser(Context context, String configXmlPath) {
        LogUtil.LogD(TAG, "xmlParser in");
        BenchmarkUtil.start("StickerApixmlParser");
        if (configXmlPath == null) {
            return null;
        }
        XMLParser arcParser = new XMLParser();
        arcParser.load(context, configXmlPath, this.mTrackDataPath, this.mFaceModelPath);
        ArrayList<SuperParams> xmlConfigList = arcParser.getParamsList();
        if (xmlConfigList != null) {
            for (int i = 0; i < xmlConfigList.size(); i++) {
                SuperParams param = (SuperParams) xmlConfigList.get(i);
                int paramType = param.mParamsType;
                if (paramType == 1) {
                    LogUtil.LogD(TAG, "mAlignTrackPath = " + ((AlignParams) param).mAlignTrackPath);
                } else if (paramType == 2) {
                    BeautyParams beautyParams = (BeautyParams) param;
                    LogUtil.LogD(TAG, "mBeautySkinSoften = " + beautyParams.mBeautySkinSoften);
                    LogUtil.LogD(TAG, "mBeautySkinBright = " + beautyParams.mBeautySkinBright);
                } else if (paramType == 6) {
                    FunnyFaceParams funnyFaceParams = (FunnyFaceParams) param;
                    LogUtil.LogD(TAG, "mExpressionType = " + funnyFaceParams.mExpressionType);
                    LogUtil.LogD(TAG, "mIntensity = " + funnyFaceParams.mIntensity);
                } else if (paramType == 3) {
                    Sticker2dParams sticker2dParams = (Sticker2dParams) param;
                    LogUtil.LogD(TAG, "_m2dTemplatePath = " + sticker2dParams.m2dTemplatePath);
                    LogUtil.LogD(TAG, "_m2dMouthOpen = " + sticker2dParams.m2dMouthOpen);
                    LogUtil.LogD(TAG, "_m2dEyeBlink = " + sticker2dParams.m2dEyeBlink);
                    LogUtil.LogD(TAG, "_m2dEyebrowRaise = " + sticker2dParams.m2dEyebrowRaise);
                    LogUtil.LogD(TAG, "_m2dHeadNod = " + sticker2dParams.m2dHeadNod);
                    LogUtil.LogD(TAG, "_m2dHeadShake = " + sticker2dParams.m2dHeadShake);
                } else if (paramType == 4) {
                    Sticker3dParams sticker3dParams = (Sticker3dParams) param;
                    LogUtil.LogD(TAG, "*m3dTemplatePath = " + sticker3dParams.m3dTemplatePath);
                    LogUtil.LogD(TAG, "*m3dTemplateSourcePath = " + sticker3dParams.m3dTemplateSourcePath);
                    LogUtil.LogD(TAG, "*m3dConfigPath = " + sticker3dParams.m3dConfigPath);
                    LogUtil.LogD(TAG, "*m3dFaceHouseDataPath = " + sticker3dParams.m3dFaceHouseDataPath);
                } else if (paramType == 5) {
                    MaskParams maskParams = (MaskParams) param;
                    LogUtil.LogD(TAG, "*mMaskTemplateSourcePath = " + maskParams.mMaskTemplateSourcePath);
                    LogUtil.LogD(TAG, "*mMaskConfigPath = " + maskParams.mMaskConfigPath);
                    LogUtil.LogD(TAG, "*mMaskFaceHouseDataPath = " + maskParams.mMaskFaceHouseDataPath);
                    LogUtil.LogD(TAG, "*mMaskImgPath = " + maskParams.mMaskImgPath);
                }
            }
        }
        BenchmarkUtil.stop("StickerApixmlParser");
        LogUtil.LogD(TAG, "xmlParser out");
        return xmlConfigList;
    }

    private void callbackFaceStatus() {
        LogUtil.LogD(TAG, "callbackFaceStatus in ");
        FaceStatus curStatus = getFaceStatus();
        if (this.mOldFaceStatus.faceCount != curStatus.faceCount) {
            this.mStickerInfoListener.OnInfoListener(17, Integer.valueOf(curStatus.faceCount));
        }
        if (this.mOldFaceStatus.mouth_open[0] != curStatus.mouth_open[0]) {
            this.mStickerInfoListener.OnInfoListener(1, Integer.valueOf(curStatus.mouth_open[0]));
        }
        if (this.mOldFaceStatus.eye_blink[0] != curStatus.eye_blink[0]) {
            this.mStickerInfoListener.OnInfoListener(2, Integer.valueOf(curStatus.eye_blink[0]));
        }
        if (this.mOldFaceStatus.eyebrow_raise[0] != curStatus.eyebrow_raise[0]) {
            this.mStickerInfoListener.OnInfoListener(3, Integer.valueOf(curStatus.eyebrow_raise[0]));
        }
        if (this.mOldFaceStatus.nod_head[0] != curStatus.nod_head[0]) {
            this.mStickerInfoListener.OnInfoListener(4, Integer.valueOf(curStatus.nod_head[0]));
        }
        if (this.mOldFaceStatus.head_pose_lr[0] != curStatus.head_pose_lr[0]) {
            this.mStickerInfoListener.OnInfoListener(5, Integer.valueOf(curStatus.head_pose_lr[0]));
        }
        this.mOldFaceStatus.clone(curStatus);
        LogUtil.LogD(TAG, "callbackFaceStatus out ");
    }

    private long adjustWritingFileSizeSinceCache(long wantedSize) {
        if (wantedSize <= 0) {
            return 0;
        }
        wantedSize -= 512000;
        if (wantedSize <= 0) {
            wantedSize = 1;
        }
        return wantedSize;
    }

    private void controlRecordingProcess() {
        LogUtil.LogD(TAG, "controlRecordingProcess in");
        long timeElapsed = this.mMediaManager.getMuxerTimeElapsed();
        LogUtil.LogD(TAG, "controlRecordingProcess getMuxerTimeElapsed = " + timeElapsed);
        long sizeFile = this.mMediaManager.getMuxerSizeRecorded();
        LogUtil.LogD(TAG, "controlRecordingProcess getMuxerSizeRecorded = " + sizeFile);
        long adjuestedMaxSizle = adjustWritingFileSizeSinceCache(this.mMaxRecordingFileSize);
        LogUtil.LogD(TAG, "controlRecordingProcess adjuestedMaxSizle = " + adjuestedMaxSizle);
        if (timeElapsed / 1000000 > this.mRecordTimeCallbackCount) {
            this.mRecordTimeCallbackCount = timeElapsed / 1000000;
            if (this.mRecordingListener != null) {
                LogUtil.LogD(TAG, "controlRecordingProcess every recording callback interval ");
                if (this.mMaxRecordingDuration > 0) {
                    this.mRecordingListener.onRecordingListener(258, Long.valueOf(timeElapsed));
                }
                if (adjuestedMaxSizle > 0) {
                    this.mRecordingListener.onRecordingListener(260, Long.valueOf(sizeFile));
                }
            }
        }
        LogUtil.LogD(TAG, "controlRecordingProcess callback current time/filesize ok");
        if (this.mMaxRecordingDuration > 0 && timeElapsed > this.mMaxRecordingDuration) {
            LogUtil.LogD(TAG, "controlRecordingProcess reach max duration = " + timeElapsed);
            stopRecording();
            if (this.mRecordingListener != null) {
                this.mRecordingListener.onRecordingListener(257, Long.valueOf(timeElapsed));
            }
        }
        if (adjuestedMaxSizle > 0 && sizeFile > adjuestedMaxSizle) {
            LogUtil.LogD(TAG, "controlRecordingProcess reach max filesize = " + sizeFile);
            stopRecording();
            if (this.mRecordingListener != null) {
                this.mRecordingListener.onRecordingListener(259, Long.valueOf(sizeFile));
            }
        }
        LogUtil.LogD(TAG, "controlRecordingProcess out");
    }
}
