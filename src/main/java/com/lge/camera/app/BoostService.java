package com.lge.camera.app;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.util.Log;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.file.FileManager;
import com.lge.camera.managers.PhoneStorageManager;
import com.lge.camera.managers.ShortcutListManager;
import com.lge.camera.managers.SoundLoader;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.SharedPreferenceUtilPersist;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.OsManager;

public class BoostService extends Service {
    private static final int SOUNDLOAD_WAIT_TIME_MILLIS = 5000;
    private static boolean sIsInit = false;
    public static ConditionVariable sSoundLoadWait = new ConditionVariable();
    private BroadcastReceiver mDataClearedEventReceiver = null;
    private Thread mGuarantedExecutionThread = new Thread(new C02972());
    private SoundLoader mSoundLoader = null;
    private IBinder mTokenForBind;

    /* renamed from: com.lge.camera.app.BoostService$1 */
    class C02961 extends BroadcastReceiver {
        C02961() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("com.lge.camera".equals(intent.getData().getSchemeSpecificPart())) {
                Log.i(CameraConstants.TAG, " mDataClearedEventReceiver");
                SharedPreferenceUtilPersist.resetAllPreference(BoostService.this.getApplicationContext(), FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_CAMERA));
                FileManager.deleteAllFiles(PhoneStorageManager.getInternalStorageDir() + PhoneStorageManager.SNAP_DIRECTORY);
                Log.i(CameraConstants.TAG, " mDataClearedEventReceiver-e");
            }
        }
    }

    /* renamed from: com.lge.camera.app.BoostService$2 */
    class C02972 implements Runnable {
        C02972() {
        }

        public void run() {
            Log.i(CameraConstants.TAG, "Guaranteed execution - start");
            BoostService.this.mTokenForBind = BoostService.this.makePersistent(BoostService.this.getApplicationContext());
            long startTime = System.currentTimeMillis();
            BoostService.sSoundLoadWait.close();
            BoostService.this.loadSoundFile();
            BoostService.sSoundLoadWait.block(CameraConstants.TOAST_LENGTH_LONG);
            BoostService.this.makeNonPersistent(BoostService.this.getApplication(), BoostService.this.mTokenForBind);
            Log.i(CameraConstants.TAG, "Guaranteed execution - stop(time = " + (System.currentTimeMillis() - startTime) + "ms)");
        }
    }

    static boolean isServiceLoaded() {
        return sIsInit;
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return 1;
    }

    public void onCreate() {
        Log.i(CameraConstants.TAG, "Camera Boost Service onCreate");
        super.onCreate();
        sIsInit = true;
        this.mGuarantedExecutionThread.start();
        registerListener();
        loadConfigFile();
        FunctionProperties.checkFuntionProperty(getApplicationContext());
        MDMUtil.loadMDMInstance();
        if (FunctionProperties.isSupportedShortcut()) {
            ShortcutListManager.initShortcutList(getApplicationContext());
        }
    }

    public void onDestroy() {
        Log.i(CameraConstants.TAG, "Camera Boost Service onDestroy");
        sIsInit = false;
        unloadSoundFile();
        unRegisterListener();
        makeNonPersistent(getApplicationContext(), this.mTokenForBind);
        this.mTokenForBind = null;
    }

    private void loadSoundFile() {
        if (this.mSoundLoader == null) {
            this.mSoundLoader = SoundLoader.getSoundLoader(getApplicationContext(), null);
            this.mSoundLoader.setLockedSoundLoader(this, true);
        }
    }

    private void unloadSoundFile() {
        if (this.mSoundLoader != null) {
            this.mSoundLoader.setLockedSoundLoader(this, false);
            this.mSoundLoader.releaseSoundLoader(null);
        }
        this.mSoundLoader = null;
    }

    private void loadConfigFile() {
        ConfigurationUtil.setConfiguration(getApplicationContext());
    }

    private void registerListener() {
        if (ModelProperties.isPersistentOn(getApplicationContext())) {
            if (this.mDataClearedEventReceiver == null) {
                this.mDataClearedEventReceiver = new C02961();
            }
            IntentFilter iFilter = new IntentFilter("android.intent.action.PACKAGE_DATA_CLEARED");
            iFilter.addDataScheme("package");
            registerReceiver(this.mDataClearedEventReceiver, iFilter);
        }
    }

    private void unRegisterListener() {
        if (this.mDataClearedEventReceiver != null) {
            unregisterReceiver(this.mDataClearedEventReceiver);
            this.mDataClearedEventReceiver = null;
        }
    }

    private IBinder makePersistent(Context context) {
        OsManager osManager = (OsManager) new LGContext(context).getLGSystemService("osservice");
        IBinder token = null;
        if (osManager != null) {
            try {
                token = osManager.makePersistent(getApplicationContext().getPackageName());
                if (token == null) {
                    makeNonPersistent(context, token);
                }
            } catch (SecurityException e) {
                Log.w(CameraConstants.TAG, "Not installed in the priv app");
            }
            Log.d(CameraConstants.TAG, "binder get: " + token);
        }
        return token;
    }

    private void makeNonPersistent(Context context, IBinder token) {
        OsManager osManager = (OsManager) new LGContext(context).getLGSystemService("osservice");
        if (osManager != null) {
            Log.d(CameraConstants.TAG, "binder release: " + token);
            if (token != null) {
                try {
                    osManager.makeNonPersistent(token);
                    return;
                } catch (SecurityException e) {
                    Log.w(CameraConstants.TAG, "Not installed in the priv app");
                    return;
                }
            }
            osManager.makeNonPersistentUsingPackageName(getApplicationContext().getPackageName());
        }
    }
}
