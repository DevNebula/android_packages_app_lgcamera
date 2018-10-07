package com.lge.voiceshutter.library;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.lge.voiceshutter.library.ISTAudioRecorder.RecorderState;

public class AudioRecogEngine {
    public static final String DUMP_PATH = (Environment.getExternalStorageDirectory().getPath() + "/pcm_dump");
    private static final int PCMDUMP_OFF = 0;
    private static final int PCMDUMP_ON = 1;
    private static final int RECOG_ERROR_INIT_FAIL_MSG = 11;
    private static final int RECOG_RESULT_CALLBACK_MSG = 1;
    private static final int RECOG_RESULT_ENGINESTOP_MSG = 2;
    private static final int SAMPLE_RATE = 16000;
    private static final String TAG = "VoiceShutter";
    public static final int VOICE_ENGINE_ENGLISH = 1;
    public static final int VOICE_ENGINE_JAPAN = 2;
    public static final int VOICE_ENGINE_KOREA = 0;
    private static final int VOICE_ENGINE_START = 1;
    private static final int VOICE_ENGINE_STOP = 0;
    private boolean mAbortThread = false;
    private Callback mCallback = null;
    private Thread mEngThread = null;
    private int mEngineState = 0;
    private Handler mHandler = new C14481();
    private LGKeyRec mLGR = null;
    private int mPCMDump = 0;
    private ISTAudioRecorder mRecorder = null;
    private int mVoiceEngineKind = 0;
    private String recogResult;

    public interface Callback {
        void onAudioEngineStartCallback(int i);

        void onAudioEngineStopCallback(int i);

        void onAudioRecogErrorCallback(int i);

        void onAudioRecogResultCallback(int i);
    }

    /* renamed from: com.lge.voiceshutter.library.AudioRecogEngine$1 */
    class C14481 extends Handler {
        C14481() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AudioRecogEngine.this.mCallback.onAudioRecogResultCallback(1);
                    return;
                case 2:
                    AudioRecogEngine.this.stop();
                    return;
                case 11:
                    AudioRecogEngine.this.mCallback.onAudioRecogErrorCallback(1);
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.lge.voiceshutter.library.AudioRecogEngine$2 */
    class C14492 implements Runnable {
        C14492() {
        }

        public void run() {
            boolean bAdvance = true;
            if (AudioRecogEngine.this.mLGR == null) {
                AudioRecogEngine.this.mAbortThread = true;
                AudioRecogEngine.this.mHandler.sendEmptyMessage(2);
                Log.d("VoiceShutter", "mEngThread stop! mLGR is null. ");
                return;
            }
            while (!AudioRecogEngine.this.mAbortThread) {
                int event;
                if (bAdvance) {
                    event = AudioRecogEngine.this.mLGR.Advance();
                } else {
                    event = 8;
                    bAdvance = true;
                }
                switch (event) {
                    case 5:
                        AudioRecogEngine.this.recogResult = "recognition results: [[type=RAW, text=" + AudioRecogEngine.this.mLGR.GetKeywordResult() + "]]";
                        Log.d("VoiceShutter", AudioRecogEngine.this.recogResult);
                        Log.d("VoiceShutter", "MSG_SHOW_RESULTS_IN_BROWSER");
                        if (AudioRecogEngine.this.mRecorder != null) {
                            if (AudioRecogEngine.this.mPCMDump == 1) {
                                AudioRecogEngine.this.mRecorder.dump(AudioRecogEngine.DUMP_PATH);
                            }
                            AudioRecogEngine.this.mRecorder.stop();
                        }
                        if (AudioRecogEngine.this.mLGR != null) {
                            AudioRecogEngine.this.mLGR.Stop();
                        }
                        AudioRecogEngine.this.mAbortThread = true;
                        if (AudioRecogEngine.this.mHandler == null) {
                            break;
                        }
                        AudioRecogEngine.this.mHandler.sendEmptyMessage(1);
                        AudioRecogEngine.this.mHandler.sendEmptyMessage(2);
                        break;
                    case 8:
                        if (AudioRecogEngine.this.mRecorder == null) {
                            break;
                        }
                        byte[] buf = AudioRecogEngine.this.mRecorder.getBuffer();
                        if (buf != null && AudioRecogEngine.this.mLGR != null) {
                            AudioRecogEngine.this.mLGR.PutAudio(buf, 0, buf.length, false);
                            break;
                        }
                        SystemClock.sleep(12);
                        bAdvance = false;
                        break;
                        break;
                    default:
                        break;
                }
            }
            Log.d("VoiceShutter", "thread break");
        }
    }

    public AudioRecogEngine(Callback callback, int kind) {
        this.mCallback = callback;
        this.mEngineState = 0;
        this.mVoiceEngineKind = kind;
    }

    public void start(int sensitivity) {
        Log.d("VoiceShutter", "AudioRecogEngine-start : mEngineState = " + this.mEngineState + ", mVoiceEngineKind = " + this.mVoiceEngineKind);
        if (this.mEngineState != 1) {
            this.mEngineState = 1;
            if (this.mLGR == null) {
                this.mLGR = new LGKeyRec(this.mVoiceEngineKind, sensitivity);
            }
            if (Initialize()) {
                if (this.mRecorder != null) {
                    this.mRecorder.start();
                }
                if (this.mEngThread != null) {
                    this.mEngThread.start();
                }
                Log.d("VoiceShutter", "AudioRecogEngine : Voice Shutter engine Start.");
                Log.d("VoiceShutter", "MSG_SHOW_LISTENING");
                return;
            }
            Log.d("VoiceShutter", "AudioRecogEngine : Fail Voice Shutter engine initialization");
            if (this.mHandler != null) {
                this.mHandler.sendEmptyMessage(2);
                this.mHandler.sendEmptyMessage(11);
            }
        }
    }

    public void stop() {
        if (this.mEngineState == 1) {
            this.mEngineState = 0;
            if (!this.mAbortThread) {
                this.mAbortThread = true;
            }
            try {
                if (this.mEngThread != null) {
                    this.mEngThread.join();
                    this.mEngThread = null;
                }
            } catch (InterruptedException e) {
                Log.e("VoiceShutter", "AudioRecogEngine : InterruptedException in stop():" + e);
            }
            if (this.mLGR != null) {
                this.mLGR.DestroyRecognizer();
                this.mLGR = null;
            }
            if (this.mRecorder != null) {
                this.mRecorder.release();
                this.mRecorder = null;
            }
            Log.d("VoiceShutter", "AudioRecogEngine : Voice Shutter engine is stopped.");
        }
    }

    public boolean isAudioRecogEngineStarted() {
        return this.mEngineState == 1;
    }

    private boolean Initialize() {
        this.mRecorder = new ISTAudioRecorder(6, SAMPLE_RATE, 16, 2);
        if (this.mRecorder.getRecorderState() == RecorderState.ERROR) {
            this.mRecorder.release();
            this.mRecorder = null;
            Log.d("VoiceShutter", "Fail to open Audio Recorder");
            return false;
        }
        try {
            if (this.mLGR != null) {
                this.mLGR.Initialize();
                this.mLGR.Start();
            }
            this.mAbortThread = false;
            this.mEngThread = new Thread(new C14492());
            return true;
        } catch (IllegalStateException e) {
            Log.e("VoiceShutter", "IllegalStateException  mLGR.Initialize() :" + e);
            return false;
        }
    }

    public String getRecognitionResult() {
        return this.recogResult;
    }
}
