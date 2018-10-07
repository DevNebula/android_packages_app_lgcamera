package com.lge.octopus.tentacles.ble.central;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.p000v4.content.LocalBroadcastManager;
import com.lge.octopus.tentacles.ble.central.Central.CBOPCODE;
import com.lge.octopus.tentacles.ble.central.Central.OPCODE;
import com.lge.octopus.tentacles.ble.central.LocalHandler.IHandleMessage;
import com.lge.octopus.tentacles.ble.gatt.GattUuids;
import com.lge.octopus.tentacles.ble.utils.Logging;
import com.lge.octopus.tentacles.ble.utils.Util;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CentralGattService extends Service implements IHandleMessage {
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_DISCONNECTED = 0;
    private static final String TAG = CentralGattService.class.getSimpleName();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private long TIMEOUT = TimeUnit.SECONDS.toMillis(10);
    private final IBinder mBinder = new LocalBinder();
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothManager mBluetoothManager;
    private int mConnectionState = 0;
    private Context mContext;
    private final BluetoothGattCallback mGattCallback = new C16751();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList();
    private LocalHandler mLocalHandler;

    /* renamed from: com.lge.octopus.tentacles.ble.central.CentralGattService$1 */
    class C16751 extends BluetoothGattCallback {
        C16751() {
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Logging.m44d(CentralGattService.TAG, "onConnectionStateChange(): status=" + status + ", newState=" + newState);
            if (newState == 2) {
                CentralGattService.this.mConnectionState = 2;
                CentralGattService.this.broadcastUpdate(Central.ACTION_GATT_CONNECTED);
                Logging.m46i(CentralGattService.TAG, "onConnectionStateChange(): Connected to GATT server.");
                Logging.m46i(CentralGattService.TAG, "onConnectionStateChange(): Attempting to start service discovery:" + CentralGattService.this.mBluetoothGatt.discoverServices());
                CentralGattService.this.mLocalHandler.removeMessages(10);
                CentralGattService.this.mLocalHandler.sendEmptyMessageDelayed(10, CentralGattService.this.TIMEOUT);
            } else if (newState == 0 && CentralGattService.this.mConnectionState != 0) {
                CentralGattService.this.mConnectionState = 0;
                CentralGattService.this.broadcastUpdate(Central.ACTION_GATT_DISCONNECTED);
                Logging.m46i(CentralGattService.TAG, "onConnectionStateChange(): Disconnected from GATT server.");
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Logging.m44d(CentralGattService.TAG, "onServicesDiscovered(): name = " + gatt.getDevice().getName() + ",  status = " + status);
            if (status == 0) {
                CentralGattService.this.displayGattServices(CentralGattService.this.mBluetoothGatt.getServices());
                CentralGattService.this.broadcastUpdate(Central.ACTION_GATT_SERVICES_DISCOVERED);
                return;
            }
            Logging.m44d(CentralGattService.TAG, "onServicesDiscovered(): status = " + status);
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Logging.m44d(CentralGattService.TAG, "onCharacteristicChanged(): received: " + characteristic);
            Logging.m45e(CentralGattService.TAG, "onCharacteristicChanged(): uuid = " + characteristic.getUuid());
            byte[] value = characteristic.getValue();
            String message = CentralGattService.this.getData(characteristic.getValue());
            if (characteristic.getUuid().equals(GattUuids.UUID_TDS_HANDOVER_CONTROL_POINT)) {
                message = CBOPCODE.toString[value[3]];
                Logging.m45e(CentralGattService.TAG, "onCharacteristicChanged(): message = " + message);
            }
            CentralGattService.this.broadcastUpdate(Central.ACTION_DATA_AVAILABLE, message, characteristic.getUuid().toString());
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Logging.m45e(CentralGattService.TAG, "onCharacteristicRead(): uuid = " + characteristic.getUuid());
            if (status == 0) {
                Logging.m45e(CentralGattService.TAG, "onCharacteristicRead(): data = " + CentralGattService.this.getData(characteristic.getValue()));
            }
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Logging.m45e(CentralGattService.TAG, "onCharacteristicWrite(): uuid = " + characteristic.getUuid());
            if (status == 0) {
                Logging.m45e(CentralGattService.TAG, "onCharacteristicWrite(): data = " + CentralGattService.this.getData(characteristic.getValue()));
            }
        }
    }

    public class LocalBinder extends Binder {
        public CentralGattService getService() {
            return CentralGattService.this;
        }
    }

    public boolean initialize() {
        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager) getSystemService("bluetooth");
            if (this.mBluetoothManager == null) {
                Logging.m45e(TAG, "initialize() : Unable to initialize BluetoothManager.");
                return false;
            }
        }
        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if (this.mBluetoothAdapter != null) {
            return true;
        }
        Logging.m45e(TAG, "initialize() : Unable to obtain a BluetoothAdapter.");
        return false;
    }

    public void onCreate() {
        Logging.m45e(TAG, "***************************************************************************");
        Logging.m46i(TAG, "CentralGattService onCreate()");
        Logging.m45e(TAG, "***************************************************************************");
        super.onCreate();
        this.mContext = getApplicationContext();
        this.mLocalHandler = new LocalHandler(this);
    }

    public void onDestroy() {
        Logging.m45e(TAG, "***************************************************************************");
        Logging.m46i(TAG, "CentralGattService onDestroy()");
        Logging.m45e(TAG, "***************************************************************************");
        super.onDestroy();
        this.mLocalHandler.removeCallbacksAndMessages(null);
        this.mLocalHandler = null;
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        Logging.m45e(TAG, "***************************************************************************");
        Logging.m46i(TAG, "CentralGattService onBind()");
        Logging.m45e(TAG, "***************************************************************************");
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        Logging.m45e(TAG, "***************************************************************************");
        Logging.m46i(TAG, "CentralGattService onUnbind()");
        Logging.m45e(TAG, "***************************************************************************");
        close();
        return super.onUnbind(intent);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 10:
                broadcastUpdate(Central.ACTION_GATT_DISCONNECTED, "LocalHandler.LE_CONNECT_TIMEOUT");
                return;
            default:
                return;
        }
    }

    public void connect(String address) {
        if (this.mBluetoothAdapter == null || address == null) {
            Logging.m45e(TAG, "connect() : BluetoothAdapter not initialized or unspecified address.");
            broadcastUpdate(Central.ACTION_GATT_DISCONNECTED, "BluetoothAdapter not initialized or unspecified address.");
            return;
        }
        if (!(this.mBluetoothDeviceAddress == null || !address.equals(this.mBluetoothDeviceAddress) || this.mBluetoothGatt == null)) {
            Logging.m44d(TAG, "connect() : Trying to use an existing mBluetoothGatt for connection.");
            if (this.mBluetoothGatt.connect()) {
                Logging.m44d(TAG, "try to reconnect : addr = " + address);
                this.mConnectionState = 1;
            } else {
                broadcastUpdate(Central.ACTION_GATT_DISCONNECTED, "Fail to Connect back to remote device");
                return;
            }
        }
        Logging.m45e(TAG, " >>>>>> try to connect GATT");
        Logging.m44d(TAG, "connectGattByAddr :: addr : " + address);
        BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Logging.m45e(TAG, "connect() : Device not found.  Unable to connect.");
            broadcastUpdate(Central.ACTION_GATT_DISCONNECTED, "Device not found.  Unable to connect.");
            return;
        }
        this.mBluetoothGatt = device.connectGatt(this, false, this.mGattCallback);
        this.mBluetoothDeviceAddress = address;
        this.mConnectionState = 1;
        this.mLocalHandler.removeCallbacksAndMessages(null);
        this.mLocalHandler.sendEmptyMessageDelayed(10, this.TIMEOUT);
    }

    public void disconnect() {
        this.mLocalHandler.removeCallbacksAndMessages(null);
        if (this.mConnectionState == 0) {
            Logging.m44d(TAG, "GATT already disconnected...");
            return;
        }
        this.mConnectionState = 0;
        if (this.mBluetoothGatt == null) {
            Logging.m48w(TAG, "BluetoothAdapter not initialized");
        } else {
            this.mBluetoothGatt.disconnect();
        }
    }

    public void close() {
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
        }
    }

    private String getData(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        String charData = "";
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            if (data == null || data.length <= 0) {
                return charData;
            }
            StringBuilder stringBuilder = new StringBuilder(data.length);
            int len$ = data.length;
            for (int i$ = 0; i$ < len$; i$++) {
                stringBuilder.append(String.format("%02X ", new Object[]{Byte.valueOf(arr$[i$])}));
            }
            return stringBuilder.toString();
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices != null) {
            String unknownServiceString = "unknown_service";
            String unknownCharaString = "unknown_characteristic";
            ArrayList<HashMap<String, String>> gattServiceData = new ArrayList();
            ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList();
            this.mGattCharacteristics = new ArrayList();
            for (BluetoothGattService gattService : gattServices) {
                HashMap<String, String> currentServiceData = new HashMap();
                String uuid = gattService.getUuid().toString();
                currentServiceData.put("NAME", GattUuids.lookup(uuid, unknownServiceString));
                currentServiceData.put("UUID", uuid);
                gattServiceData.add(currentServiceData);
                Logging.m44d(TAG, "######################################################");
                Logging.m44d(TAG, " uuid : " + uuid);
                ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList();
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> charas = new ArrayList();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    charas.add(gattCharacteristic);
                    HashMap<String, String> currentCharaData = new HashMap();
                    uuid = gattCharacteristic.getUuid().toString();
                    currentCharaData.put("NAME", GattUuids.lookup(uuid, unknownCharaString));
                    currentCharaData.put("UUID", uuid);
                    gattCharacteristicGroupData.add(currentCharaData);
                    Logging.m44d(TAG, " gattCharacteristic uuid : " + uuid);
                }
                this.mGattCharacteristics.add(charas);
                gattCharacteristicData.add(gattCharacteristicGroupData);
                Logging.m44d(TAG, "######################################################");
            }
        }
    }

    public void send(byte[] message) {
        List<BluetoothGattService> gattServices = getSupportedGattServices();
        if (gattServices != null) {
            BluetoothGattCharacteristic gattCharacteristic = null;
            for (BluetoothGattService gattService : gattServices) {
                Logging.m46i(TAG, "Gatt Service UUID:   " + gattService.getUuid());
                if (gattService.getUuid().equals(GattUuids.UUID_SERVICE_TDS)) {
                    Logging.m45e(TAG, "#####  Found TDS Service");
                    gattCharacteristic = gattService.getCharacteristic(GattUuids.UUID_TDS_HANDOVER_CONTROL_POINT);
                    break;
                }
            }
            if (gattCharacteristic == null) {
                Logging.m45e(TAG, "gattCharacteristic is null");
                return;
            }
            this.mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
            boolean result = false;
            Logging.m45e(TAG, "#####  Found Handover Control Point Characteristic:  getProperties =  " + gattCharacteristic.getProperties());
            String opcode = OPCODE.toString[message[3]];
            if ((gattCharacteristic.getProperties() & 8) != 0) {
                Logging.m45e(TAG, "#####  Write Handover Control Point Characteristic:  " + opcode);
                gattCharacteristic.setValue(message);
                result = this.mBluetoothGatt.writeCharacteristic(gattCharacteristic);
            }
            if (result) {
                String passwd = getPassword(message);
                broadcastUpdate(Central.ACTION_GATT_CALLBACK_TURN_ON_AP, passwd == null ? opcode : opcode + "=" + passwd);
            }
        }
    }

    private String getPassword(byte[] message) {
        if (message.length > 3) {
            return new String(Util.subbytes(message, 3));
        }
        return null;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (this.mBluetoothGatt == null) {
            return null;
        }
        return this.mBluetoothGatt.getServices();
    }

    private void broadcastUpdate(String action) {
        broadcastUpdate(action, null);
    }

    private void broadcastUpdate(String action, String data) {
        broadcastUpdate(action, data, null);
    }

    private void broadcastUpdate(String action, String data, String uuid) {
        Intent intent = new Intent(action);
        if (data != null) {
            intent.putExtra(Central.EXTRA_DATA, data);
        }
        if (uuid != null) {
            intent.putExtra(Central.EXTRA_UUID, uuid);
        }
        LocalBroadcastManager.getInstance(this.mContext.getApplicationContext()).sendBroadcast(intent);
    }
}
