package com.lge.octopus.tentacles.ble.central;

import android.os.Bundle;

public interface Central {
    public static final String ACTION_DATA_AVAILABLE = "com.lge.octopus.tentacles.ble.ACTION_DATA_AVAILABLE";
    public static final String ACTION_GATT_CALLBACK_TURN_ON_AP = "com.lge.octopus.tentacles.ble.ACTION_GATT_CALLBACK_TURN_ON_AP";
    public static final String ACTION_GATT_CONNECTED = "com.lge.octopus.tentacles.ble.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.lge.octopus.tentacles.ble.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.lge.octopus.tentacles.ble.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String EXTRA_DATA = "com.lge.octopus.tentacles.ble.EXTRA_DATA";
    public static final String EXTRA_UUID = "com.lge.octopus.tentacles.ble.EXTRA_UUID";
    public static final int GATT_STATE_CONNECTED = 2;
    public static final int GATT_STATE_CONNECTING = 1;
    public static final int GATT_STATE_DISCONNECTED = 0;
    public static final int GATT_STATE_DISCONNECTING = 3;
    public static final int GATT_SUCCESS = 0;
    public static final String LE_ACTION_SCANRESULT = "com.lge.octopus.tentacles.ble.LE_ACTION_SCANRESULT";
    public static final byte LE_ACTIVATE_TRANSPORT = (byte) 1;
    public static final String LE_BLE_ADDRESS = "com.lge.octopus.tentacles.ble.LE_BLE_ADDRESS";
    public static final String LE_BT_ADDRESS = "com.lge.octopus.tentacles.ble.LE_BT_ADDRESS";
    public static final String LE_BT_ON = "com.lge.octopus.tentacles.ble.LE_BT_ON";
    public static final String LE_CAMERA_FACTORY_MODE = "com.lge.octopus.tentacles.ble.LE_CAMERA_FACTORY_MODE";
    public static final String LE_CAMERA_PROTOCOL_OSC = "com.lge.octopus.tentacles.ble.LE_CAMERA_PROTOCOL_OSC";
    public static final String LE_DEVICE_SERIAL = "com.lge.octopus.tentacles.ble.LE_DEVICE_SERIAL";
    public static final String LE_DEV_NAME = "com.lge.octopus.tentacles.ble.LE_DEV_NAME";
    public static final byte LE_ORG_ID_BT_SIG = (byte) 1;
    public static final byte LE_ORG_ID_LG_WIFI = (byte) -2;
    public static final String LE_SERVICE_UUID = "com.lge.octopus.tentacles.ble.LE_SERVICE_UUID";
    public static final byte LE_SERVICE_UUID_128BIT = (byte) 3;
    public static final byte LE_SERVICE_UUID_16BIT = (byte) 1;
    public static final byte LE_SERVICE_UUID_32BIT = (byte) 2;
    public static final String LE_SERVICE_UUID_TYPE = "com.lge.octopus.tentacles.ble.LE_SERVICE_UUID_TYPE";
    public static final byte LE_SERVICE_UUID_UNKNOWN = (byte) 0;
    public static final String LE_WIFI_CONNECTED_COUNT = "com.lge.octopus.tentacles.ble.LE_WIFI_CONNECTED_COUNT";
    public static final String LE_WIFI_MODE = "com.lge.octopus.tentacles.ble.LE_WIFI_MODE";
    public static final String LE_WIFI_STATE = "com.lge.octopus.tentacles.ble.LE_WIFI_STATE";
    public static final String PREFIX = "com.lge.octopus.tentacles.ble";

    public static class CBOPCODE {
        public static final byte WIFI_OVER_AP_OFF = (byte) 3;
        public static final byte WIFI_OVER_AP_ON = (byte) 4;
        public static final byte WIFI_SOFT_AP_OFF = (byte) 1;
        public static final byte WIFI_SOFT_AP_ON = (byte) 2;
        public static final String[] toString = new String[]{"", "WIFI_SOFT_AP_OFF", "WIFI_SOFT_AP_ON", "WIFI_OVER_AP_OFF", "WIFI_OVER_AP_ON"};

        private CBOPCODE() {
        }
    }

    public interface CentralScanCallback {
        void onScanFailed(String str);

        void onScanResult(Bundle bundle);
    }

    public interface ConnectCallback {
        void onConnectionChange(int i);
    }

    public static class MODE {
        public static final int OVER_AP = 1;
        public static final int SOFT_AP = 0;
        public static final String[] toString = new String[]{"SOFT_AP", "OVER_AP"};

        private MODE() {
        }
    }

    public static abstract class MessageListener {
        public void onReceiveMessage(byte[] message) {
        }
    }

    public static class OPCODE {
        public static final byte OVER_AP_ON = (byte) 2;
        public static final byte SOFT_AP_ON = (byte) 1;
        public static final byte SOFT_AP_PW = (byte) 3;
        public static final String[] toString = new String[]{"", "SOFT_AP_ON", "OVER_AP_ON", "SOFT_AP_PW"};

        private OPCODE() {
        }
    }

    public static class STATE {
        public static final int WIFI_BUSY = 16;
        public static final int WIFI_OFF = 0;
        public static final int WIFI_ON = 8;
        public static final String[] toString = new String[]{"WIFI_OFF", "WIFI_ON", "WIFI_BUSY"};

        private STATE() {
        }
    }

    void connect(String str, ConnectCallback connectCallback);

    void disconnect();

    void finish();

    void initialize();

    void send(byte[] bArr);

    void startLeScan(CentralScanCallback centralScanCallback);

    void startLeScan(CentralScanCallback centralScanCallback, String str, String... strArr);

    void stopLeScan();
}
