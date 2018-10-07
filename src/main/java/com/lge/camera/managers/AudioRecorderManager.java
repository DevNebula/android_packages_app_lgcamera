package com.lge.camera.managers;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioRecorderManager extends ManagerInterfaceImpl {
    public static final int MAX_DUR = 30000;
    public static Object sSynchRecordStop = new Object();
    public float mAudioDuration = 0.0f;
    public long mDuration = 0;
    public File mFile = null;
    public String mFirstDate = "";
    public String mLastDate = "";
    public AudioRecorderManagerListener mListener = null;
    public MediaRecorder mRecorder = null;
    public String mSndFileName = "";
    public Thread mStartAudioRecordThread = null;
    public Thread mStopAudioRecordThread = null;

    public interface AudioRecorderManagerListener extends ModuleInterface {
        boolean afterAudioRecordingStopped(boolean z);
    }

    /* renamed from: com.lge.camera.managers.AudioRecorderManager$1 */
    class C08271 implements Runnable {

        /* renamed from: com.lge.camera.managers.AudioRecorderManager$1$1 */
        class C08251 implements OnInfoListener {
            C08251() {
            }

            public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
                AudioRecorderManager.this.audioRecorderOnInfo(arg0, arg1, arg2);
            }
        }

        /* renamed from: com.lge.camera.managers.AudioRecorderManager$1$2 */
        class C08262 implements OnErrorListener {
            C08262() {
            }

            public void onError(MediaRecorder arg0, int arg1, int arg2) {
                AudioRecorderManager.this.recorderOnError(arg0, arg1, arg2);
            }
        }

        C08271() {
        }

        public void run() {
            AudioRecorderManager.this.releaseRecorder(true);
            CamLog.m3d(CameraConstants.TAG, "Media recorder start - start");
            synchronized (AudioRecorderManager.sSynchRecordStop) {
                if (AudioUtil.isAudioRecording(AudioRecorderManager.this.getAppContext())) {
                    CamLog.m11w(CameraConstants.TAG, "Currently, audio recorded in somewhere.");
                    return;
                }
                AudioRecorderManager.this.mRecorder = new MediaRecorder();
                AudioRecorderManager.this.mRecorder.setAudioSource(1);
                AudioRecorderManager.this.mRecorder.setOutputFormat(2);
                AudioRecorderManager.this.mRecorder.setOutputFile(AudioRecorderManager.this.mSndFileName);
                AudioRecorderManager.this.mRecorder.setAudioEncoder(3);
                AudioRecorderManager.this.mRecorder.setMaxDuration(30000);
                AudioRecorderManager.this.mRecorder.setOnInfoListener(new C08251());
                AudioRecorderManager.this.mRecorder.setOnErrorListener(new C08262());
                try {
                    AudioRecorderManager.this.mRecorder.prepare();
                } catch (IOException e) {
                    CamLog.m5e(CameraConstants.TAG, "prepare() failed");
                }
                AudioRecorderManager.this.mRecorder.start();
                AudioRecorderManager.this.mDuration = System.currentTimeMillis();
                AudioRecorderManager.this.mFirstDate = AudioRecorderManager.this.getCurrentTimeStamp();
                CamLog.m3d(CameraConstants.TAG, "Media recorder start - end");
            }
        }
    }

    public AudioRecorderManager(AudioRecorderManagerListener listener) {
        super(listener);
        this.mListener = listener;
    }

    public void setListener(AudioRecorderManagerListener listener) {
        this.mListener = listener;
    }

    public void release() {
        releaseRecorder(true);
        if (this.mStartAudioRecordThread != null && this.mStartAudioRecordThread.isAlive()) {
            this.mStartAudioRecordThread.interrupt();
            try {
                this.mStartAudioRecordThread.join();
            } catch (InterruptedException e) {
                CamLog.m6e(CameraConstants.TAG, "mStartAudioRecordThread wait exception : ", e);
            }
            this.mStartAudioRecordThread = null;
        }
        if (this.mStopAudioRecordThread != null && this.mStopAudioRecordThread.isAlive()) {
            this.mStopAudioRecordThread.interrupt();
            try {
                this.mStopAudioRecordThread.join();
            } catch (InterruptedException e2) {
                CamLog.m6e(CameraConstants.TAG, "mStartAudioRecordThread wait exception : ", e2);
            }
            this.mStopAudioRecordThread = null;
        }
        this.mListener = null;
    }

    public boolean initAudioRecorder(String fileName) {
        this.mSndFileName = fileName;
        return true;
    }

    public boolean startAudioRecord() {
        this.mStartAudioRecordThread = new Thread(new C08271());
        this.mStartAudioRecordThread.start();
        return true;
    }

    public boolean stopAudioRecord(final boolean stopping) {
        final boolean audRec = this.mRecorder != null;
        this.mStopAudioRecordThread = new Thread(new Runnable() {
            public void run() {
                CamLog.m3d(CameraConstants.TAG, "Media recorder stop - start");
                AudioRecorderManager.this.releaseRecorder(stopping);
                if (AudioRecorderManager.this.mListener != null) {
                    AudioRecorderManager.this.mListener.afterAudioRecordingStopped(audRec);
                }
                CamLog.m3d(CameraConstants.TAG, "Media recorder stop - end");
            }
        });
        this.mStopAudioRecordThread.start();
        return true;
    }

    public boolean waitReleaseRecording() {
        if (this.mStopAudioRecordThread == null) {
            return false;
        }
        int waitCnt = 0;
        while (this.mStopAudioRecordThread.isAlive() && waitCnt < 100) {
            try {
                Thread.sleep(10);
                waitCnt++;
            } catch (InterruptedException e) {
                CamLog.m6e(CameraConstants.TAG, "InterruptedException : ", e);
                return false;
            }
        }
        if (waitCnt >= 100) {
            CamLog.m5e(CameraConstants.TAG, "Too long to wait over 1000ms. It's problem on MediaRecoder.");
            return false;
        }
        CamLog.m7i(CameraConstants.TAG, "Media recorder release done!!!");
        return true;
    }

    public synchronized void releaseRecorder(boolean stopping) {
        if (this.mRecorder != null) {
            synchronized (sSynchRecordStop) {
                CamLog.m3d(CameraConstants.TAG, "releaseRecorder - start");
                this.mDuration = System.currentTimeMillis() - this.mDuration;
                this.mLastDate = getCurrentTimeStamp();
                if (stopping) {
                    try {
                        this.mRecorder.stop();
                    } catch (Exception e) {
                        CamLog.m5e(CameraConstants.TAG, "##### mediaRecorder stop Exception : " + e);
                        this.mRecorder.setOnInfoListener(null);
                        this.mRecorder.setOnErrorListener(null);
                        this.mRecorder.release();
                        this.mRecorder = null;
                    } catch (Throwable th) {
                        this.mRecorder.setOnInfoListener(null);
                        this.mRecorder.setOnErrorListener(null);
                        this.mRecorder.release();
                        this.mRecorder = null;
                    }
                }
                this.mRecorder.setOnInfoListener(null);
                this.mRecorder.setOnErrorListener(null);
                this.mRecorder.release();
                this.mRecorder = null;
            }
            CamLog.m3d(CameraConstants.TAG, "releaseRecorder - end");
        }
    }

    protected void audioRecorderOnInfo(MediaRecorder mr, int what, int extra) {
        CamLog.m3d(CameraConstants.TAG, "audioRecorderOnInfo what = " + what + " / extra = " + extra);
        if (what != 1003) {
            if (what == CameraConstantsEx.GOOGLE_ASSISTANT_TAKE_CMD_DELAY) {
                if (this.mRecorder != null) {
                    stopAudioRecord(true);
                }
                this.mDuration = 30000;
            } else if (what == 801 && this.mRecorder != null) {
                stopAudioRecord(true);
            }
        }
    }

    protected void recorderOnError(MediaRecorder mr, int what, int extra) {
        CamLog.m3d(CameraConstants.TAG, "MediaRecorder onError what = " + what + " / extra = " + extra);
        if (what == 1 || what == 2 || what == 100) {
            stopAudioRecord(false);
            if (extra == -1007) {
            }
            if (!this.mGet.getActivity().isFinishing()) {
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                    }
                });
            }
        }
    }

    public String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

    /* JADX WARNING: Removed duplicated region for block: B:95:0x0168 A:{SYNTHETIC, Splitter: B:95:0x0168} */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x016d A:{SYNTHETIC, Splitter: B:98:0x016d} */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0172 A:{SYNTHETIC, Splitter: B:101:0x0172} */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0195 A:{SYNTHETIC, Splitter: B:113:0x0195} */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x019a A:{SYNTHETIC, Splitter: B:116:0x019a} */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x019f A:{SYNTHETIC, Splitter: B:119:0x019f} */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x01b7 A:{SYNTHETIC, Splitter: B:129:0x01b7} */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x01bc A:{SYNTHETIC, Splitter: B:132:0x01bc} */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x01c1 A:{SYNTHETIC, Splitter: B:135:0x01c1} */
    public boolean addXmpAudioMetaData(java.lang.String r23, java.lang.String r24) {
        /*
        r22 = this;
        r14 = 0;
        r7 = 0;
        r9 = 0;
        r12 = 0;
        r0 = r22;
        r0 = r0.mSndFileName;	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r16 = r0;
        if (r16 == 0) goto L_0x0027;
    L_0x000c:
        r13 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r0 = r22;
        r0 = r0.mSndFileName;	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r16 = r0;
        r0 = r16;
        r13.<init>(r0);	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        if (r13 == 0) goto L_0x00d6;
    L_0x001b:
        r16 = r13.exists();	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        if (r16 == 0) goto L_0x00d6;
    L_0x0021:
        r15 = new java.io.FileInputStream;	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r15.<init>(r13);	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r14 = r15;
    L_0x0027:
        if (r23 == 0) goto L_0x003e;
    L_0x0029:
        r5 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r0 = r23;
        r5.<init>(r0);	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        if (r5 == 0) goto L_0x00fe;
    L_0x0032:
        r16 = r5.exists();	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        if (r16 == 0) goto L_0x00fe;
    L_0x0038:
        r8 = new java.io.FileInputStream;	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r8.<init>(r5);	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r7 = r8;
    L_0x003e:
        r16 = "CameraApp";
        r17 = "save - addxmp";
        com.lge.camera.util.CamLog.m3d(r16, r17);	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        if (r14 == 0) goto L_0x0126;
    L_0x0047:
        if (r7 == 0) goto L_0x0126;
    L_0x0049:
        r6 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r0 = r24;
        r6.<init>(r0);	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r10 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r10.<init>(r6);	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r11 = new com.lge.gallery.xmp.encoder.XmpMetadata;	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r16 = com.lge.gallery.xmp.encoder.prop.LgPictureMode.SOUNDPICTURE;	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r0 = r16;
        r11.<init>(r0);	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r16 = "Version";
        r17 = "1.0";
        r0 = r16;
        r1 = r17;
        r11.addProperty(r0, r1);	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r16 = "audio/mpeg";
        r0 = r22;
        r0 = r0.mDuration;	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r18 = r0;
        r20 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r18 = r18 / r20;
        r0 = r18;
        r0 = (double) r0;	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r18 = r0;
        r18 = java.lang.Math.floor(r18);	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r0 = r18;
        r0 = (int) r0;	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r17 = r0;
        r0 = r16;
        r1 = r17;
        r11.addAudio(r14, r0, r1);	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r4 = new com.lge.gallery.xmp.encoder.JpegMetadataEncoder;	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r0 = r22;
        r0 = r0.mGet;	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r16 = r0;
        r16 = r16.getAppContext();	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r0 = r16;
        r4.<init>(r0, r11);	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r4.encodeStream(r7, r10);	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r10.flush();	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r16 = "CameraApp";
        r17 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r17.<init>();	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r18 = "save, , audio dur = ";
        r17 = r17.append(r18);	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r0 = r22;
        r0 = r0.mDuration;	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r18 = r0;
        r17 = r17.append(r18);	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r17 = r17.toString();	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        com.lge.camera.util.CamLog.m3d(r16, r17);	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r22.clearTempFiles();	 Catch:{ FileNotFoundException -> 0x01da, IOException -> 0x01d7, all -> 0x01d4 }
        r12 = 1;
        if (r14 == 0) goto L_0x00c8;
    L_0x00c5:
        r14.close();	 Catch:{ IOException -> 0x0147 }
    L_0x00c8:
        if (r7 == 0) goto L_0x00cd;
    L_0x00ca:
        r7.close();	 Catch:{ IOException -> 0x014d }
    L_0x00cd:
        if (r10 == 0) goto L_0x01de;
    L_0x00cf:
        r10.close();	 Catch:{ IOException -> 0x0153 }
        r9 = r10;
    L_0x00d3:
        r16 = r12;
    L_0x00d5:
        return r16;
    L_0x00d6:
        r16 = "CameraApp";
        r17 = "Sound file does not exist.";
        com.lge.camera.util.CamLog.m5e(r16, r17);	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r16 = 0;
        if (r14 == 0) goto L_0x00e4;
    L_0x00e1:
        r14.close();	 Catch:{ IOException -> 0x00f4 }
    L_0x00e4:
        if (r7 == 0) goto L_0x00e9;
    L_0x00e6:
        r7.close();	 Catch:{ IOException -> 0x00f9 }
    L_0x00e9:
        if (r9 == 0) goto L_0x00d5;
    L_0x00eb:
        r9.close();	 Catch:{ IOException -> 0x00ef }
        goto L_0x00d5;
    L_0x00ef:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x00d5;
    L_0x00f4:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x00e4;
    L_0x00f9:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x00e9;
    L_0x00fe:
        r16 = "CameraApp";
        r17 = "Input panorama picture does not exist.";
        com.lge.camera.util.CamLog.m5e(r16, r17);	 Catch:{ FileNotFoundException -> 0x015a, IOException -> 0x0187 }
        r16 = 0;
        if (r14 == 0) goto L_0x010c;
    L_0x0109:
        r14.close();	 Catch:{ IOException -> 0x011c }
    L_0x010c:
        if (r7 == 0) goto L_0x0111;
    L_0x010e:
        r7.close();	 Catch:{ IOException -> 0x0121 }
    L_0x0111:
        if (r9 == 0) goto L_0x00d5;
    L_0x0113:
        r9.close();	 Catch:{ IOException -> 0x0117 }
        goto L_0x00d5;
    L_0x0117:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x00d5;
    L_0x011c:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x010c;
    L_0x0121:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x0111;
    L_0x0126:
        r16 = 0;
        if (r14 == 0) goto L_0x012d;
    L_0x012a:
        r14.close();	 Catch:{ IOException -> 0x013d }
    L_0x012d:
        if (r7 == 0) goto L_0x0132;
    L_0x012f:
        r7.close();	 Catch:{ IOException -> 0x0142 }
    L_0x0132:
        if (r9 == 0) goto L_0x00d5;
    L_0x0134:
        r9.close();	 Catch:{ IOException -> 0x0138 }
        goto L_0x00d5;
    L_0x0138:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x00d5;
    L_0x013d:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x012d;
    L_0x0142:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x0132;
    L_0x0147:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x00c8;
    L_0x014d:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x00cd;
    L_0x0153:
        r2 = move-exception;
        r2.printStackTrace();
        r9 = r10;
        goto L_0x00d3;
    L_0x015a:
        r2 = move-exception;
    L_0x015b:
        r16 = "CameraApp";
        r17 = "addXmpMetaData error occurred.";
        r0 = r16;
        r1 = r17;
        com.lge.camera.util.CamLog.m6e(r0, r1, r2);	 Catch:{ all -> 0x01b4 }
        if (r14 == 0) goto L_0x016b;
    L_0x0168:
        r14.close();	 Catch:{ IOException -> 0x017d }
    L_0x016b:
        if (r7 == 0) goto L_0x0170;
    L_0x016d:
        r7.close();	 Catch:{ IOException -> 0x0182 }
    L_0x0170:
        if (r9 == 0) goto L_0x00d3;
    L_0x0172:
        r9.close();	 Catch:{ IOException -> 0x0177 }
        goto L_0x00d3;
    L_0x0177:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x00d3;
    L_0x017d:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x016b;
    L_0x0182:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x0170;
    L_0x0187:
        r3 = move-exception;
    L_0x0188:
        r16 = "CameraApp";
        r17 = "addXmpMetaData error occurred.";
        r0 = r16;
        r1 = r17;
        com.lge.camera.util.CamLog.m6e(r0, r1, r3);	 Catch:{ all -> 0x01b4 }
        if (r14 == 0) goto L_0x0198;
    L_0x0195:
        r14.close();	 Catch:{ IOException -> 0x01aa }
    L_0x0198:
        if (r7 == 0) goto L_0x019d;
    L_0x019a:
        r7.close();	 Catch:{ IOException -> 0x01af }
    L_0x019d:
        if (r9 == 0) goto L_0x00d3;
    L_0x019f:
        r9.close();	 Catch:{ IOException -> 0x01a4 }
        goto L_0x00d3;
    L_0x01a4:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x00d3;
    L_0x01aa:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x0198;
    L_0x01af:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x019d;
    L_0x01b4:
        r16 = move-exception;
    L_0x01b5:
        if (r14 == 0) goto L_0x01ba;
    L_0x01b7:
        r14.close();	 Catch:{ IOException -> 0x01c5 }
    L_0x01ba:
        if (r7 == 0) goto L_0x01bf;
    L_0x01bc:
        r7.close();	 Catch:{ IOException -> 0x01ca }
    L_0x01bf:
        if (r9 == 0) goto L_0x01c4;
    L_0x01c1:
        r9.close();	 Catch:{ IOException -> 0x01cf }
    L_0x01c4:
        throw r16;
    L_0x01c5:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x01ba;
    L_0x01ca:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x01bf;
    L_0x01cf:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x01c4;
    L_0x01d4:
        r16 = move-exception;
        r9 = r10;
        goto L_0x01b5;
    L_0x01d7:
        r3 = move-exception;
        r9 = r10;
        goto L_0x0188;
    L_0x01da:
        r2 = move-exception;
        r9 = r10;
        goto L_0x015b;
    L_0x01de:
        r9 = r10;
        goto L_0x00d3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.AudioRecorderManager.addXmpAudioMetaData(java.lang.String, java.lang.String):boolean");
    }

    public void clearTempFiles() {
        if (this.mSndFileName != null) {
            File sndFile = new File(this.mSndFileName);
            if (sndFile != null && sndFile.exists()) {
                sndFile.delete();
                CamLog.m3d(CameraConstants.TAG, "delete sound file.");
            }
        }
    }
}
