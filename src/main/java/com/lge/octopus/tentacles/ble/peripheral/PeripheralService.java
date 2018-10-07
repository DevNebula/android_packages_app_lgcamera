package com.lge.octopus.tentacles.ble.peripheral;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData.Builder;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;
import com.lge.octopus.tentacles.ble.gatt.GATTProfile;
import com.lge.octopus.tentacles.ble.gatt.GATTProfile.Characteristic;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PeripheralService extends Service {
    private static final String TAG = PeripheralService.class.getSimpleName();
    private AdvertiseCallback advertiseCallback = new C16791();
    private boolean advertising;
    private ArrayList<BluetoothGattService> advertisingServices;
    private BluetoothGattServer gattServer;
    public BluetoothGattServerCallback gattServerCallback = new C16802();
    private final IBinder mBinder = new LocalBinder();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothManager mBluetoothManager;
    Context mContext;
    PeripheralServiceCallback peripheralServiceCallback;
    private UUID serviceOneCharUuid;
    private List<ParcelUuid> serviceUuids;
    private BluetoothGattCharacteristic temp_characteristic;
    private BluetoothDevice temp_device;

    /* renamed from: com.lge.octopus.tentacles.ble.peripheral.PeripheralService$1 */
    class C16791 extends AdvertiseCallback {
        C16791() {
        }

        public void onStartSuccess(AdvertiseSettings advertiseSettings) {
            String successMsg = "Advertise Success";
            Log.d(PeripheralService.TAG, "advertiseCallback ss:" + successMsg);
            PeripheralService.this.peripheralServiceCallback.onPeripheralResult(successMsg);
        }

        public void onStartFailure(int i) {
            String failMsg = "Advertisement command attempt failed: " + i;
            Log.e(PeripheralService.TAG, "advertiseCallback fail:" + failMsg);
            PeripheralService.this.peripheralServiceCallback.onPeripheralResult(failMsg);
        }
    }

    /* renamed from: com.lge.octopus.tentacles.ble.peripheral.PeripheralService$2 */
    class C16802 extends BluetoothGattServerCallback {
        C16802() {
        }

        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d(PeripheralService.TAG, "onConnectionStateChange status=" + status + "->" + newState);
            if (newState == 2) {
                Log.d(PeripheralService.TAG, "BluetoothGattServer.STATE_CONNECTED");
            }
            if (newState == 0) {
                PeripheralService.this.peripheralServiceCallback.onPeripheralResult("BluetoothGattServer.STATE_CONNECTED");
            }
        }

        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.d(PeripheralService.TAG, "onServiceAdded");
        }

        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(PeripheralService.TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);
            Log.d(PeripheralService.TAG, "onCharacteristicReadRequest characteristic:" + characteristic);
            PeripheralService.this.temp_device = device;
            PeripheralService.this.temp_characteristic = characteristic;
            if (characteristic.getUuid().equals(PeripheralService.this.serviceOneCharUuid)) {
                Log.d(PeripheralService.TAG, "SERVICE_UUID_1");
                characteristic.setValue("Edgar is nice man");
                PeripheralService.this.getGattServer().sendResponse(device, requestId, 0, offset, characteristic.getValue());
            }
        }

        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.d(PeripheralService.TAG, "onCharacteristicWriteRequest requestId=" + requestId + " preparedWrite=" + Boolean.toString(preparedWrite) + " responseNeeded=" + Boolean.toString(responseNeeded) + " offset=" + offset);
            PeripheralService.this.temp_device = device;
            PeripheralService.this.temp_characteristic = characteristic;
            if (responseNeeded) {
                PeripheralService.this.getGattServer().sendResponse(device, requestId, 0, 0, value);
            }
            try {
                if (PeripheralService.this.peripheralServiceCallback != null) {
                    PeripheralService.this.peripheralServiceCallback.onReceiveMessage(value);
                }
                Log.d(PeripheralService.TAG, "onCharacteristicWriteRequest value : " + new String(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public class LocalBinder extends Binder {
        public PeripheralService getService() {
            return PeripheralService.this;
        }
    }

    public boolean initialize() {
        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager) getSystemService("bluetooth");
            if (this.mBluetoothManager == null) {
                Log.e(TAG, "initialize() : Unable to initialize BluetoothManager.");
                return false;
            }
        }
        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if (this.mBluetoothAdapter == null) {
            Log.e(TAG, "initialize() : Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if (this.mBluetoothLeAdvertiser == null) {
            BluetoothAdapter mBluetoothAdapter = this.mBluetoothManager.getAdapter();
            if (mBluetoothAdapter != null) {
                this.mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            } else {
                Log.e(TAG, "ERROR: Bluetooth object null");
                return false;
            }
        }
        return true;
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void onCreate() {
        super.onCreate();
        this.mContext = getApplicationContext();
        this.advertisingServices = new ArrayList();
        this.serviceUuids = new ArrayList();
        addServiceToGattServer();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void start() {
        startAdvertise(this.mContext);
    }

    public void setPeripheralServiceCallback(PeripheralServiceCallback peripheralServiceCallback) {
        this.peripheralServiceCallback = peripheralServiceCallback;
    }

    public void send(byte[] bytes) {
        sendDataToCentral(bytes);
    }

    public boolean getAdvertising() {
        return this.advertising;
    }

    private void startGattServer(Context context) {
        Log.e(TAG, "startGattServer");
        this.gattServer = this.mBluetoothManager.openGattServer(context, this.gattServerCallback);
        if (this.gattServer == null) {
            Log.e(TAG, "gattServer is null");
            return;
        }
        for (int i = 0; i < this.advertisingServices.size(); i++) {
            this.gattServer.addService((BluetoothGattService) this.advertisingServices.get(i));
        }
    }

    public void startAdvertise(Context context) {
        Log.i(TAG, "startAdvertise");
        if (!getAdvertising()) {
            if (this.mBluetoothAdapter.isEnabled()) {
                startGattServer(context);
                Builder dataBuilder = new Builder();
                AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
                dataBuilder.setIncludeTxPowerLevel(false);
                dataBuilder.setIncludeDeviceName(true);
                for (ParcelUuid uuid : this.serviceUuids) {
                    dataBuilder.addServiceUuid(uuid);
                    Log.i(TAG, "uuid :" + uuid);
                }
                settingsBuilder.setAdvertiseMode(1);
                settingsBuilder.setTxPowerLevel(3);
                settingsBuilder.setConnectable(true);
                if (this.mBluetoothLeAdvertiser != null) {
                    Log.i(TAG, "startAdvertising");
                    this.mBluetoothLeAdvertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), this.advertiseCallback);
                    this.advertising = true;
                    return;
                }
                Log.i(TAG, "bluetoothLeAdvertiser : is null");
                this.peripheralServiceCallback.onPeripheralResult("bluetoothLeAdvertiser : is null");
                return;
            }
            Log.e(TAG, "startAdvertise fail : bt enable fail");
            this.peripheralServiceCallback.onPeripheralResult("startAdvertise fail : bt enable fail");
        }
    }

    public void cleanUp() {
        if (getAdvertising()) {
            stopAdvertise();
        }
        if (this.gattServer != null) {
            this.gattServer.close();
        }
    }

    public void stopAdvertise() {
        if (getAdvertising()) {
            this.mBluetoothLeAdvertiser.stopAdvertising(this.advertiseCallback);
            this.gattServer.clearServices();
            this.gattServer.close();
            this.advertisingServices.clear();
            this.advertising = false;
        }
    }

    public BluetoothGattServer getGattServer() {
        return this.gattServer;
    }

    private void addServiceToGattServer() {
        this.serviceOneCharUuid = Characteristic.CHARACTERISTIC_1;
        Log.e(TAG, "addServiceToGattServer serviceOneCharUuid :" + this.serviceOneCharUuid.toString());
        BluetoothGattService firstService = new BluetoothGattService(GATTProfile.Service.SERVICE_UUID_1, 0);
        firstService.addCharacteristic(new BluetoothGattCharacteristic(this.serviceOneCharUuid, 27, 17));
        addService(firstService);
    }

    public void addService(BluetoothGattService service) {
        this.advertisingServices.add(service);
        this.serviceUuids.add(new ParcelUuid(service.getUuid()));
    }

    public void sendDataToCentral(byte[] data) {
        if (data == null) {
            Log.e(TAG, "value is null");
            return;
        }
        Log.e(TAG, "temp_characteristic : " + this.temp_characteristic.getUuid());
        if (getGattServer() == null || this.temp_characteristic == null) {
            Log.e(TAG, "getGattServer is null");
            return;
        }
        Log.e(TAG, "try server write");
        this.temp_characteristic.setValue(data);
        Log.e(TAG, "notifyCharacteristicChanged : " + getGattServer().notifyCharacteristicChanged(this.temp_device, this.temp_characteristic, false));
    }
}
