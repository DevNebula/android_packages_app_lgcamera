package com.lge.voiceshutter.library;

import android.media.AudioRecord;
import android.os.Process;
import android.util.Log;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ISTAudioRecorder {
    public static final int JAVA_BUFFER_INTERVAL = 4000;
    public static final int NUM_JAVA_BUFFER = 160;
    private static final String TAG = ISTAudioRecorder.class.getName();
    public static final int TIMER_INTERVAL = 25;
    private List<ByteArray> BackupPool = null;
    private List<ByteArray> BufferPool = null;
    private int ChannelConfig;
    private int RecorderBufferSize;
    private int aFormat;
    private AudioRecord aRecorder = null;
    private int aSource;
    private short bSamples;
    public byte[] buffer;
    private int framePeriod;
    private RecorderState mState;
    private Thread mThread = null;
    private short nChannels;
    private int sRate;
    private final Object syncObj = new Object();

    /* renamed from: com.lge.voiceshutter.library.ISTAudioRecorder$1 */
    class C14501 extends Thread {
        C14501() {
        }

        public void run() {
            Process.setThreadPriority(-19);
            try {
                ISTAudioRecorder.this.recording();
            } catch (Exception e) {
                Log.e(ISTAudioRecorder.TAG, e.getMessage());
            }
        }
    }

    public enum RecorderState {
        INITIALIZING,
        READY,
        RECORDING,
        ERROR,
        STOPPED
    }

    public RecorderState getRecorderState() {
        return this.mState;
    }

    public ISTAudioRecorder(int audioSource, int sampleRate, int channelConfig, int audioFormat) {
        if (audioFormat == 2) {
            try {
                this.bSamples = (short) 16;
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    Log.e(TAG, e.getMessage());
                } else {
                    Log.e(TAG, "Unknown error occured while initializing recording");
                }
                this.mState = RecorderState.ERROR;
                return;
            }
        }
        this.bSamples = (short) 8;
        this.ChannelConfig = channelConfig;
        if (channelConfig == 16) {
            this.nChannels = (short) 1;
        } else {
            this.nChannels = (short) 2;
        }
        this.aSource = audioSource;
        this.sRate = sampleRate;
        this.aFormat = audioFormat;
        this.framePeriod = (sampleRate * 25) / 1000;
        this.RecorderBufferSize = (((this.framePeriod * 160) * this.bSamples) / 8) * this.nChannels;
        if (this.RecorderBufferSize < AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)) {
            this.RecorderBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            this.framePeriod = this.RecorderBufferSize / (((this.bSamples * 160) / 8) * this.nChannels);
            Log.w(TAG, "Increasing buffer size to " + Integer.toString(this.RecorderBufferSize));
        }
        this.aRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, this.RecorderBufferSize);
        if (this.aRecorder.getState() != 1) {
            this.aRecorder = null;
            throw new Exception("AudioRecord initialization failed");
        }
        this.buffer = new byte[(this.RecorderBufferSize / 160)];
        this.BufferPool = Collections.synchronizedList(new LinkedList());
        this.BackupPool = Collections.synchronizedList(new LinkedList());
        this.mState = RecorderState.READY;
    }

    public byte[] getBuffer() {
        if (this.BufferPool == null || this.BufferPool.size() <= 0) {
            return null;
        }
        ByteArray buf = (ByteArray) this.BufferPool.remove(0);
        this.BackupPool.add(buf);
        if (this.BackupPool.size() > 160) {
            this.BackupPool.remove(0);
        }
        return buf.array();
    }

    public void release() {
        if (this.mState == RecorderState.RECORDING) {
            stop();
        }
        if (this.aRecorder != null) {
            this.aRecorder.stop();
            this.aRecorder.release();
            this.aRecorder = null;
        }
    }

    public void reset() {
        try {
            if (this.mState != RecorderState.ERROR) {
                release();
                this.aRecorder = new AudioRecord(this.aSource, this.sRate, this.ChannelConfig, this.aFormat, this.RecorderBufferSize);
                this.mState = RecorderState.READY;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            this.mState = RecorderState.ERROR;
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0013, code:
            if (r8.aRecorder == null) goto L_0x0022;
     */
    /* JADX WARNING: Missing block: B:10:0x0015, code:
            r8.aRecorder.stop();
            r8.aRecorder.release();
            r8.aRecorder = null;
     */
    /* JADX WARNING: Missing block: B:16:0x0037, code:
            if (r8.aRecorder == null) goto L_0x0045;
     */
    /* JADX WARNING: Missing block: B:17:0x0039, code:
            r2 = r8.aRecorder.read(r8.buffer, 0, r8.buffer.length);
     */
    /* JADX WARNING: Missing block: B:18:0x0045, code:
            if (r2 <= 0) goto L_0x0003;
     */
    /* JADX WARNING: Missing block: B:19:0x0047, code:
            r8.BufferPool.add(new com.lge.voiceshutter.library.ByteArray(r8.buffer, r2));
            r0 = r8.BufferPool.size();
     */
    /* JADX WARNING: Missing block: B:20:0x005b, code:
            if (r0 <= 160) goto L_0x0003;
     */
    /* JADX WARNING: Missing block: B:21:0x005d, code:
            android.util.Log.e(TAG, "BufferPool overflow: " + r0);
            r1 = (com.lge.voiceshutter.library.ByteArray) r8.BufferPool.remove(0);
     */
    private void recording() {
        /*
        r8 = this;
        r7 = 0;
        r2 = 0;
        r0 = 0;
    L_0x0003:
        r4 = 12;
        android.os.SystemClock.sleep(r4);	 Catch:{ all -> 0x0082 }
        r4 = r8.syncObj;	 Catch:{ all -> 0x0082 }
        monitor-enter(r4);	 Catch:{ all -> 0x0082 }
        r3 = r8.mState;	 Catch:{ all -> 0x007f }
        r5 = com.lge.voiceshutter.library.ISTAudioRecorder.RecorderState.RECORDING;	 Catch:{ all -> 0x007f }
        if (r3 == r5) goto L_0x0034;
    L_0x0011:
        r3 = r8.aRecorder;	 Catch:{ all -> 0x007f }
        if (r3 == 0) goto L_0x0022;
    L_0x0015:
        r3 = r8.aRecorder;	 Catch:{ all -> 0x007f }
        r3.stop();	 Catch:{ all -> 0x007f }
        r3 = r8.aRecorder;	 Catch:{ all -> 0x007f }
        r3.release();	 Catch:{ all -> 0x007f }
        r3 = 0;
        r8.aRecorder = r3;	 Catch:{ all -> 0x007f }
    L_0x0022:
        monitor-exit(r4);	 Catch:{ all -> 0x007f }
        r3 = r8.aRecorder;
        if (r3 == 0) goto L_0x0033;
    L_0x0027:
        r3 = r8.aRecorder;
        r3.stop();
        r3 = r8.aRecorder;
        r3.release();
        r8.aRecorder = r7;
    L_0x0033:
        return;
    L_0x0034:
        monitor-exit(r4);	 Catch:{ all -> 0x007f }
        r3 = r8.aRecorder;	 Catch:{ all -> 0x0082 }
        if (r3 == 0) goto L_0x0045;
    L_0x0039:
        r3 = r8.aRecorder;	 Catch:{ all -> 0x0082 }
        r4 = r8.buffer;	 Catch:{ all -> 0x0082 }
        r5 = 0;
        r6 = r8.buffer;	 Catch:{ all -> 0x0082 }
        r6 = r6.length;	 Catch:{ all -> 0x0082 }
        r2 = r3.read(r4, r5, r6);	 Catch:{ all -> 0x0082 }
    L_0x0045:
        if (r2 <= 0) goto L_0x0003;
    L_0x0047:
        r1 = new com.lge.voiceshutter.library.ByteArray;	 Catch:{ all -> 0x0082 }
        r3 = r8.buffer;	 Catch:{ all -> 0x0082 }
        r1.<init>(r3, r2);	 Catch:{ all -> 0x0082 }
        r3 = r8.BufferPool;	 Catch:{ all -> 0x0082 }
        r3.add(r1);	 Catch:{ all -> 0x0082 }
        r3 = r8.BufferPool;	 Catch:{ all -> 0x0082 }
        r0 = r3.size();	 Catch:{ all -> 0x0082 }
        r3 = 160; // 0xa0 float:2.24E-43 double:7.9E-322;
        if (r0 <= r3) goto L_0x0003;
    L_0x005d:
        r3 = TAG;	 Catch:{ all -> 0x0082 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0082 }
        r4.<init>();	 Catch:{ all -> 0x0082 }
        r5 = "BufferPool overflow: ";
        r4 = r4.append(r5);	 Catch:{ all -> 0x0082 }
        r4 = r4.append(r0);	 Catch:{ all -> 0x0082 }
        r4 = r4.toString();	 Catch:{ all -> 0x0082 }
        android.util.Log.e(r3, r4);	 Catch:{ all -> 0x0082 }
        r3 = r8.BufferPool;	 Catch:{ all -> 0x0082 }
        r4 = 0;
        r1 = r3.remove(r4);	 Catch:{ all -> 0x0082 }
        r1 = (com.lge.voiceshutter.library.ByteArray) r1;	 Catch:{ all -> 0x0082 }
        goto L_0x0003;
    L_0x007f:
        r3 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x007f }
        throw r3;	 Catch:{ all -> 0x0082 }
    L_0x0082:
        r3 = move-exception;
        r4 = r8.aRecorder;
        if (r4 == 0) goto L_0x0093;
    L_0x0087:
        r4 = r8.aRecorder;
        r4.stop();
        r4 = r8.aRecorder;
        r4.release();
        r8.aRecorder = r7;
    L_0x0093:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.voiceshutter.library.ISTAudioRecorder.recording():void");
    }

    public void start() {
        if (this.mState == RecorderState.READY) {
            this.mState = RecorderState.RECORDING;
            if (this.aRecorder != null) {
                this.aRecorder.startRecording();
            }
            this.mThread = new C14501();
            this.mThread.start();
            return;
        }
        Log.e(TAG, "start() called on illegal state:" + this.mState);
        this.mState = RecorderState.ERROR;
    }

    public void stop() {
        if (this.mState == RecorderState.RECORDING) {
            synchronized (this.syncObj) {
                this.mState = RecorderState.STOPPED;
            }
            try {
                if (this.mThread != null) {
                    this.mThread.join();
                    this.mThread = null;
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "abrupt thread" + e.getMessage());
                this.mState = RecorderState.ERROR;
            }
        } else {
            Log.e(TAG, "stop() called on illegal state");
            this.mState = RecorderState.ERROR;
        }
        if (this.aRecorder != null) {
            this.aRecorder.stop();
            this.aRecorder.release();
            this.aRecorder = null;
        }
        if (this.BufferPool != null) {
            this.BufferPool.clear();
            this.BufferPool = null;
        }
        if (this.BackupPool != null) {
            this.BackupPool.clear();
            this.BackupPool = null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x00dd A:{SYNTHETIC, Splitter: B:30:0x00dd} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00cf A:{SYNTHETIC, Splitter: B:22:0x00cf} */
    public void dump(java.lang.String r13) {
        /*
        r12 = this;
        r4 = new java.io.File;
        r4.<init>(r13);
        r7 = r4.exists();
        if (r7 != 0) goto L_0x000e;
    L_0x000b:
        r4.mkdirs();
    L_0x000e:
        r5 = java.util.Calendar.getInstance();
        r7 = java.util.Locale.US;
        r8 = "%04d_%02d_%02d_%02d_%02d_%02d.pcm";
        r9 = 6;
        r9 = new java.lang.Object[r9];
        r10 = 0;
        r11 = 1;
        r11 = r5.get(r11);
        r11 = java.lang.Integer.valueOf(r11);
        r9[r10] = r11;
        r10 = 1;
        r11 = 2;
        r11 = r5.get(r11);
        r11 = r11 + 1;
        r11 = java.lang.Integer.valueOf(r11);
        r9[r10] = r11;
        r10 = 2;
        r11 = 5;
        r11 = r5.get(r11);
        r11 = java.lang.Integer.valueOf(r11);
        r9[r10] = r11;
        r10 = 3;
        r11 = 11;
        r11 = r5.get(r11);
        r11 = java.lang.Integer.valueOf(r11);
        r9[r10] = r11;
        r10 = 4;
        r11 = 12;
        r11 = r5.get(r11);
        r11 = java.lang.Integer.valueOf(r11);
        r9[r10] = r11;
        r10 = 5;
        r11 = 13;
        r11 = r5.get(r11);
        r11 = java.lang.Integer.valueOf(r11);
        r9[r10] = r11;
        r6 = java.lang.String.format(r7, r8, r9);
        r2 = 0;
        r3 = new java.io.FileOutputStream;	 Catch:{ Exception -> 0x00b4 }
        r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00b4 }
        r7.<init>();	 Catch:{ Exception -> 0x00b4 }
        r7 = r7.append(r13);	 Catch:{ Exception -> 0x00b4 }
        r8 = "/";
        r7 = r7.append(r8);	 Catch:{ Exception -> 0x00b4 }
        r7 = r7.append(r6);	 Catch:{ Exception -> 0x00b4 }
        r7 = r7.toString();	 Catch:{ Exception -> 0x00b4 }
        r3.<init>(r7);	 Catch:{ Exception -> 0x00b4 }
    L_0x0087:
        r7 = r12.BackupPool;	 Catch:{ Exception -> 0x00eb, all -> 0x00e8 }
        r7 = r7.isEmpty();	 Catch:{ Exception -> 0x00eb, all -> 0x00e8 }
        if (r7 != 0) goto L_0x00a7;
    L_0x008f:
        r7 = r12.BackupPool;	 Catch:{ Exception -> 0x00eb, all -> 0x00e8 }
        r8 = 0;
        r0 = r7.remove(r8);	 Catch:{ Exception -> 0x00eb, all -> 0x00e8 }
        r0 = (com.lge.voiceshutter.library.ByteArray) r0;	 Catch:{ Exception -> 0x00eb, all -> 0x00e8 }
        r7 = r0.array();	 Catch:{ Exception -> 0x00eb, all -> 0x00e8 }
        r8 = 0;
        r9 = r0.array();	 Catch:{ Exception -> 0x00eb, all -> 0x00e8 }
        r9 = r9.length;	 Catch:{ Exception -> 0x00eb, all -> 0x00e8 }
        r3.write(r7, r8, r9);	 Catch:{ Exception -> 0x00eb, all -> 0x00e8 }
        r0 = 0;
        goto L_0x0087;
    L_0x00a7:
        if (r3 == 0) goto L_0x00ee;
    L_0x00a9:
        r3.close();	 Catch:{ IOException -> 0x00af }
    L_0x00ac:
        r2 = 0;
    L_0x00ad:
        r4 = 0;
    L_0x00ae:
        return;
    L_0x00af:
        r1 = move-exception;
        r1.printStackTrace();
        goto L_0x00ac;
    L_0x00b4:
        r1 = move-exception;
    L_0x00b5:
        r7 = TAG;	 Catch:{ all -> 0x00da }
        r8 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00da }
        r8.<init>();	 Catch:{ all -> 0x00da }
        r9 = " FileOutputStream error:";
        r8 = r8.append(r9);	 Catch:{ all -> 0x00da }
        r8 = r8.append(r1);	 Catch:{ all -> 0x00da }
        r8 = r8.toString();	 Catch:{ all -> 0x00da }
        android.util.Log.e(r7, r8);	 Catch:{ all -> 0x00da }
        if (r2 == 0) goto L_0x00d3;
    L_0x00cf:
        r2.close();	 Catch:{ IOException -> 0x00d5 }
    L_0x00d2:
        r2 = 0;
    L_0x00d3:
        r4 = 0;
        goto L_0x00ae;
    L_0x00d5:
        r1 = move-exception;
        r1.printStackTrace();
        goto L_0x00d2;
    L_0x00da:
        r7 = move-exception;
    L_0x00db:
        if (r2 == 0) goto L_0x00e1;
    L_0x00dd:
        r2.close();	 Catch:{ IOException -> 0x00e3 }
    L_0x00e0:
        r2 = 0;
    L_0x00e1:
        r4 = 0;
        throw r7;
    L_0x00e3:
        r1 = move-exception;
        r1.printStackTrace();
        goto L_0x00e0;
    L_0x00e8:
        r7 = move-exception;
        r2 = r3;
        goto L_0x00db;
    L_0x00eb:
        r1 = move-exception;
        r2 = r3;
        goto L_0x00b5;
    L_0x00ee:
        r2 = r3;
        goto L_0x00ad;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.voiceshutter.library.ISTAudioRecorder.dump(java.lang.String):void");
    }
}
