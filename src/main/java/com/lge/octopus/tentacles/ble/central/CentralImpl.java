package com.lge.octopus.tentacles.ble.central;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanFilter.Builder;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.p000v4.content.LocalBroadcastManager;
import com.lge.octopus.tentacles.ble.central.Central.CentralScanCallback;
import com.lge.octopus.tentacles.ble.central.Central.ConnectCallback;
import com.lge.octopus.tentacles.ble.central.CentralGattService.LocalBinder;
import com.lge.octopus.tentacles.ble.utils.Logging;
import com.lge.octopus.tentacles.ble.utils.ParseAdvertiseData;
import com.lge.octopus.tentacles.ble.utils.TdsAdvInfo;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CentralImpl implements Central {
    private static final String TAG = CentralImpl.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothManager mBluetoothManager;
    private CentralGattService mCentralGattService;
    private CentralScanCallback mCentralScanCallback;
    private Context mContext;
    private List mExceptFilter;
    private String mLeAddress;
    private ScanCallback mScanCallback = new C16772();
    private AtomicBoolean mScanning = new AtomicBoolean(false);
    private final ServiceConnection mServiceConnection = new C16761();
    private WeakReference<ParseAdvertiseData> weakAdData;
    private WeakReference<TdsAdvInfo> weakTDS;

    /* renamed from: com.lge.octopus.tentacles.ble.central.CentralImpl$1 */
    class C16761 implements ServiceConnection {
        C16761() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            CentralImpl.this.mCentralGattService = ((LocalBinder) service).getService();
            if (CentralImpl.this.mCentralGattService.initialize()) {
                Logging.m45e(CentralImpl.TAG, "***************************************************************************");
                Logging.m46i(CentralImpl.TAG, "ServiceConnection binded()");
                Logging.m45e(CentralImpl.TAG, "***************************************************************************");
                CentralImpl.this.mCentralGattService.connect(CentralImpl.this.mLeAddress);
                return;
            }
            Logging.m45e(CentralImpl.TAG, "Unable to initialize Bluetooth");
        }

        public void onServiceDisconnected(ComponentName name) {
            CentralImpl.this.mCentralGattService = null;
        }
    }

    /* renamed from: com.lge.octopus.tentacles.ble.central.CentralImpl$2 */
    class C16772 extends ScanCallback {
        C16772() {
        }

        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            String error_string = "";
            Logging.m44d(CentralImpl.TAG, "onScanFailed: errorCode = " + errorCode);
            switch (errorCode) {
                case 1:
                    error_string = "SCAN_FAILED_ALREADY_STARTED";
                    break;
                case 2:
                    error_string = "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED";
                    break;
                case 3:
                    error_string = "SCAN_FAILED_INTERNAL_ERROR";
                    break;
                case 4:
                    error_string = "SCAN_FAILED_FEATURE_UNSUPPORTED";
                    break;
            }
            CentralImpl.this.mCentralScanCallback.onScanFailed(error_string);
        }

        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Logging.m44d(CentralImpl.TAG, "onBatchScanResults: results = " + results);
        }

        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            CentralImpl.this.parseScanResult(callbackType, result);
        }
    }

    public CentralImpl(Context context) {
        this.mContext = context;
    }

    public void initialize() {
        initBluetooth();
    }

    public void finish() {
        disconnect();
    }

    public boolean initBluetooth() {
        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager) this.mContext.getSystemService("bluetooth");
            if (this.mBluetoothManager == null) {
                Logging.m45e(TAG, "initialize() : Unable to initialize BluetoothManager.");
                return false;
            }
        }
        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if (this.mBluetoothAdapter == null) {
            Logging.m45e(TAG, "initialize() : Unable to obtain a BluetoothAdapter.");
            return false;
        }
        this.mBluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
        if (this.mBluetoothLeScanner != null) {
            return true;
        }
        Logging.m45e(TAG, "initialize() : Unable to obtain a BluetoothLeScanner.");
        return false;
    }

    public void startLeScan(CentralScanCallback centralScancallback) {
        startLeScan(centralScancallback, null, null);
    }

    public void startLeScan(CentralScanCallback centralScancallback, String filter, String... exceptFilter) {
        if (initBluetooth()) {
            this.mCentralScanCallback = centralScancallback;
            if (this.mScanning.getAndSet(true)) {
                Logging.m44d(TAG, "ble : already Scanning...  >  return");
                return;
            }
            if (exceptFilter != null && exceptFilter.length > 0) {
                Logging.m44d(TAG, "ble : exceptFilter : " + exceptFilter.toString());
                this.mExceptFilter = Arrays.asList(exceptFilter);
            }
            this.mBluetoothLeScanner.startScan(buildScanFilters(filter), buildScanSettings(), this.mScanCallback);
            Logging.m44d(TAG, "Bluetooth is currently mScanning...");
        }
    }

    public void stopLeScan() {
        if (this.mScanning.getAndSet(false)) {
            this.mBluetoothLeScanner.stopScan(this.mScanCallback);
            Logging.m44d(TAG, "Scanning has been stopped");
            return;
        }
        Logging.m44d(TAG, "ble : already stopped");
        this.mCentralScanCallback = null;
    }

    public void connect(String address, ConnectCallback connectCallback) {
        this.mLeAddress = address;
        this.mContext.bindService(new Intent(this.mContext, CentralGattService.class), this.mServiceConnection, 1);
    }

    public void disconnect() {
        if (this.mCentralGattService != null) {
            this.mCentralGattService.disconnect();
            this.mContext.unbindService(this.mServiceConnection);
        }
        this.mCentralGattService = null;
        stopLeScan();
    }

    public void send(byte[] message) {
        if (this.mCentralGattService != null) {
            this.mCentralGattService.send(message);
        }
    }

    private List<ScanFilter> buildScanFilters(String... args) {
        List<ScanFilter> filterList = new ArrayList();
        Builder builder = new Builder();
        if (args != null && args.length > 0) {
            builder.setDeviceName(args[0]);
        }
        filterList.add(builder.build());
        return filterList;
    }

    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(2);
        return builder.build();
    }

    private void broadcastUpdate(String action, Bundle scanResult) {
        Intent intent = new Intent(action);
        if (scanResult != null) {
            intent.putExtras(scanResult);
        }
        LocalBroadcastManager.getInstance(this.mContext.getApplicationContext()).sendBroadcast(intent);
    }

    private synchronized void parseScanResult(int callbackType, ScanResult result) {
        String modelName = result.getScanRecord().getDeviceName();
        if (result.getScanRecord() == null) {
            Logging.m45e(TAG, "ERROR!!! > result.getScanRecord() is null");
        } else {
            byte[] data = result.getScanRecord().getBytes();
            if (data != null) {
                this.weakAdData = new WeakReference(new ParseAdvertiseData(data));
                ParseAdvertiseData adData = (ParseAdvertiseData) this.weakAdData.get();
                if (adData != null) {
                    byte[] tdsData = adData.getTransportDataByte();
                    Bundle scanResult;
                    if (tdsData != null) {
                        this.weakTDS = new WeakReference(new TdsAdvInfo(tdsData));
                        scanResult = getTDSBundle(adData, (TdsAdvInfo) this.weakTDS.get(), result.getDevice().getAddress());
                        broadcastUpdate(Central.LE_ACTION_SCANRESULT, scanResult);
                        this.mCentralScanCallback.onScanResult(scanResult);
                    } else if (this.mExceptFilter == null || !this.mExceptFilter.contains(modelName)) {
                        Logging.m45e(TAG, "************************************ NOT MY DEVICE");
                    } else {
                        Logging.m45e(TAG, "************************************ NOT TDS BUT MINE");
                        scanResult = getExceptBundle(result);
                        broadcastUpdate(Central.LE_ACTION_SCANRESULT, scanResult);
                        this.mCentralScanCallback.onScanResult(scanResult);
                    }
                }
            }
        }
    }

    private Bundle getTDSBundle(ParseAdvertiseData adData, TdsAdvInfo tds, String bleAddress) {
        Bundle scanResult = new Bundle();
        scanResult.putString(Central.LE_DEV_NAME, adData.getNameString());
        scanResult.putString(Central.LE_BT_ADDRESS, adData.getAddressString());
        scanResult.putString(Central.LE_BLE_ADDRESS, bleAddress);
        scanResult.putString(Central.LE_SERVICE_UUID, adData.getServiceUuidString());
        scanResult.putString(Central.LE_DEVICE_SERIAL, tds.getSerial());
        scanResult.putInt(Central.LE_SERVICE_UUID_TYPE, adData.getServiceUuidInt());
        scanResult.putInt(Central.LE_WIFI_MODE, tds.getWifiMode());
        scanResult.putInt(Central.LE_WIFI_CONNECTED_COUNT, tds.getWifiConnCount());
        scanResult.putInt(Central.LE_WIFI_STATE, tds.getWifiState());
        scanResult.putBoolean(Central.LE_BT_ON, tds.getBtState());
        scanResult.putBoolean(Central.LE_CAMERA_PROTOCOL_OSC, tds.isSupportOSC());
        scanResult.putBoolean(Central.LE_CAMERA_FACTORY_MODE, tds.getFactoryMode());
        Logging.m45e(TAG, "****************************************************");
        Logging.m46i(TAG, "ble address : " + bleAddress);
        Logging.m46i(TAG, "name : " + adData.getNameString());
        Logging.m46i(TAG, tds.toString());
        Logging.m45e(TAG, "****************************************************");
        return scanResult;
    }

    private Bundle getExceptBundle(ScanResult result) {
        Bundle scanResult = new Bundle();
        scanResult.putString(Central.LE_DEV_NAME, result.getScanRecord().getDeviceName());
        scanResult.putString(Central.LE_BT_ADDRESS, result.getDevice().getAddress());
        scanResult.putString(Central.LE_BLE_ADDRESS, result.getDevice().getAddress());
        scanResult.putBoolean(Central.LE_CAMERA_FACTORY_MODE, false);
        return scanResult;
    }
}
