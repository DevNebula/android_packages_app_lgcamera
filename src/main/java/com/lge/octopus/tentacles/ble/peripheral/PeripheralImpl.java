package com.lge.octopus.tentacles.ble.peripheral;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.lge.octopus.tentacles.ble.peripheral.Peripheral.MessageListener;
import com.lge.octopus.tentacles.ble.peripheral.Peripheral.PeripheralCallback;
import com.lge.octopus.tentacles.ble.peripheral.PeripheralService.LocalBinder;

public class PeripheralImpl implements Peripheral, PeripheralServiceCallback {
    private static final String TAG = PeripheralImpl.class.getSimpleName();
    Context mContext;
    private PeripheralCallback mPeripheralCallback;
    private PeripheralService mPeripheralService;
    private final ServiceConnection mServiceConnection = new C16781();
    private MessageListener messageListener;

    /* renamed from: com.lge.octopus.tentacles.ble.peripheral.PeripheralImpl$1 */
    class C16781 implements ServiceConnection {
        C16781() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            PeripheralImpl.this.mPeripheralService = ((LocalBinder) service).getService();
            PeripheralImpl.this.mPeripheralService.setPeripheralServiceCallback(PeripheralImpl.this);
            if (PeripheralImpl.this.mPeripheralService.initialize()) {
                Log.e(PeripheralImpl.TAG, "***************************************************************************");
                Log.i(PeripheralImpl.TAG, "ServiceConnection binded()");
                Log.e(PeripheralImpl.TAG, "***************************************************************************");
                return;
            }
            Log.e(PeripheralImpl.TAG, "Unable to initialize Bluetooth");
        }

        public void onServiceDisconnected(ComponentName name) {
            PeripheralImpl.this.mPeripheralService = null;
        }
    }

    public PeripheralImpl(Context context) {
        this.mContext = context;
    }

    public void initialize() {
        Log.e(TAG, "***************************************************************************");
        Log.i(TAG, "startCentralService()");
        Log.e(TAG, "***************************************************************************");
        this.mContext.bindService(new Intent(this.mContext, PeripheralService.class), this.mServiceConnection, 1);
    }

    public void finish() {
        this.mContext.unbindService(this.mServiceConnection);
        this.mPeripheralService = null;
    }

    public void start(PeripheralCallback peripheralCallback) {
        this.mPeripheralCallback = peripheralCallback;
        this.mPeripheralService.start();
    }

    public void send(byte[] bytes) {
        this.mPeripheralService.send(bytes);
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void onReceiveMessage(byte[] message) {
        this.messageListener.onReceiveMessage(message);
    }

    public void onPeripheralResult(String status) {
        this.mPeripheralCallback.onResult(status);
    }
}
