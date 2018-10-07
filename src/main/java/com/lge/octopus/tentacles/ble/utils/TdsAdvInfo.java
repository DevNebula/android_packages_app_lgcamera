package com.lge.octopus.tentacles.ble.utils;

import com.lge.octopus.tentacles.ble.central.Central.MODE;

public class TdsAdvInfo {
    private static final byte AVAILABILITY_OFFSET = (byte) 4;
    public static final int LG_ELECTRONICS_ID = 196;
    private static final byte ORG_ID_BT_SIG = (byte) 1;
    private static final byte ORG_ID_LG_WIFI = (byte) -2;
    private static final byte SERVICE_UUID_128BIT = (byte) 3;
    private static final byte SERVICE_UUID_16BIT = (byte) 1;
    private static final byte SERVICE_UUID_32BIT = (byte) 2;
    private static final byte STATE_BUSY = (byte) 16;
    private static final byte STATE_FLAG = (byte) 24;
    private static final byte STATE_OFF = (byte) 0;
    private static final byte STATE_ON = (byte) 8;
    private static final String TAG = TdsAdvInfo.class.getSimpleName();
    private static final byte WIFI_FLAG_CONNECTION_CNT = (byte) 28;
    private static final byte WIFI_FLAG_FACTORY_MODE = Byte.MIN_VALUE;
    private static final byte WIFI_FLAG_OSC = (byte) 32;
    private static final byte WIFI_FLAG_OVER_AP = (byte) 64;
    private static final byte WIFI_FLAG_SOFT_AP = (byte) 0;
    private static final byte WIFI_MODE_OVER_AP = (byte) 1;
    private static final byte WIFI_MODE_SOFT_AP = (byte) 0;
    private String mDeviceSerial = null;
    private byte[] service_uuid_list = null;
    private byte service_uuid_type = (byte) 0;
    private byte td_len_bt = (byte) 0;
    private byte td_len_wifi = (byte) 0;
    private byte td_sub_len_bt = (byte) 0;
    private byte tds_flags_bt = (byte) 0;
    private byte tds_flags_wifi = (byte) 0;
    private int wifi_conn_count;
    private byte wifi_flags;

    public TdsAdvInfo(byte[] data) {
        parse(data);
    }

    public void parse(byte[] data) {
        if (data == null) {
            Logging.m45e(TAG, "ERROR!!! > TDS data is null, fix me!!!");
            return;
        }
        int i = 0;
        while (i < data.length) {
            int i2;
            switch (data[i]) {
                case (byte) -2:
                    Logging.m46i(TAG, "parse() : case ORG_ID_LG_WIFI");
                    i++;
                    i2 = i + 1;
                    this.tds_flags_wifi = data[i];
                    Logging.m44d(TAG, "parse() : tds_flags_wifi = " + this.tds_flags_wifi);
                    i = i2 + 1;
                    this.td_len_wifi = data[i2];
                    Logging.m44d(TAG, "parse() : td_len_wifi = " + this.td_len_wifi);
                    if (this.td_len_wifi >= (byte) 9) {
                        i2 = i + 1;
                        Logging.m44d(TAG, "parse() : transport_data_type = " + data[i]);
                        i = i2 + 1;
                        Logging.m44d(TAG, "parse() : transport_data_device_type = " + data[i2]);
                        i2 = i + 1;
                        this.wifi_flags = data[i];
                        Logging.m44d(TAG, "parse() : wifi_flags = " + String.format("0x%02x", new Object[]{Byte.valueOf(this.wifi_flags)}));
                        if ((this.wifi_flags & -128) == -128) {
                            Logging.m44d(TAG, "parse() : FACTORY_MODE");
                        }
                        if ((this.wifi_flags & 64) == 64) {
                            Logging.m44d(TAG, "parse() : OVER_AP");
                        } else if ((this.wifi_flags & 0) == 0) {
                            Logging.m44d(TAG, "parse() : SOFT_AP");
                        }
                        if ((this.wifi_flags & 32) == 32) {
                            Logging.m44d(TAG, "parse() : OSC");
                        }
                        Logging.m44d(TAG, "parse() : wifi_conn_count = " + ((this.wifi_flags & 28) >> 2));
                        r6 = new Object[6];
                        i = i2 + 1;
                        r6[0] = Byte.valueOf(data[i2]);
                        i2 = i + 1;
                        r6[1] = Byte.valueOf(data[i]);
                        i = i2 + 1;
                        r6[2] = Byte.valueOf(data[i2]);
                        i2 = i + 1;
                        r6[3] = Byte.valueOf(data[i]);
                        i = i2 + 1;
                        r6[4] = Byte.valueOf(data[i2]);
                        i2 = i + 1;
                        r6[5] = Byte.valueOf(data[i]);
                        this.mDeviceSerial = String.format("%c%c%c%c%c%c", r6);
                        Logging.m44d(TAG, "parse() : mDeviceSerial = " + this.mDeviceSerial);
                        i = i2;
                        break;
                    }
                    Logging.m45e(TAG, "parse() : TDS (for WiFi) Packet Error");
                    break;
                case (byte) 1:
                    Logging.m46i(TAG, "parse() : case ORG_ID_BT_SIG");
                    i++;
                    i2 = i + 1;
                    this.tds_flags_bt = data[i];
                    Logging.m44d(TAG, "parse() : tds_flags_bt = " + this.tds_flags_bt);
                    this.td_len_bt = data[i2];
                    Logging.m44d(TAG, "parse() : td_len_bt = " + this.td_len_bt);
                    if (this.td_len_bt != (byte) 0) {
                        if (this.td_len_bt >= (byte) 4) {
                            i = i2 + 1;
                            i2 = i + 1;
                            this.td_sub_len_bt = data[i];
                            Logging.m44d(TAG, "parse() : td_sub_len_bt = " + this.td_sub_len_bt);
                            i = i2 + 1;
                            this.service_uuid_type = data[i2];
                            Logging.m44d(TAG, "parse() : service_uuid_type = " + this.service_uuid_type);
                            int j;
                            switch (this.service_uuid_type) {
                                case (byte) 1:
                                    this.service_uuid_list = new byte[(this.td_sub_len_bt - 1)];
                                    for (j = 0; j < this.td_sub_len_bt - 1; j++) {
                                        i += j;
                                        this.service_uuid_list[j] = data[i];
                                    }
                                    Logging.m44d(TAG, "parse() : case SERVICE_UUID_16BIT = " + this.service_uuid_list);
                                    break;
                                case (byte) 2:
                                    this.service_uuid_list = new byte[(this.td_sub_len_bt - 1)];
                                    for (j = 0; j < this.td_sub_len_bt - 1; j++) {
                                        i += j;
                                        this.service_uuid_list[j] = data[i];
                                    }
                                    Logging.m44d(TAG, "parse() : case SERVICE_UUID_32BIT = " + this.service_uuid_list);
                                    break;
                                case (byte) 3:
                                    this.service_uuid_list = new byte[(this.td_sub_len_bt - 1)];
                                    for (j = 0; j < this.td_sub_len_bt - 1; j++) {
                                        i += j;
                                        this.service_uuid_list[j] = data[i];
                                    }
                                    Logging.m44d(TAG, "parse() : case SERVICE_UUID_128BIT = " + this.service_uuid_list);
                                    break;
                                case (byte) 4:
                                    Logging.m44d(TAG, "parse() : case AVAILABILITY_OFFSET");
                                    break;
                                default:
                                    Logging.m44d(TAG, "parse() : default");
                                    break;
                            }
                        }
                        Logging.m45e(TAG, "parse() : TDS BLE Packet Error");
                        i = i2;
                        break;
                    }
                    Logging.m46i(TAG, "parse() : Remote device is running only BLE");
                    i = i2;
                    break;
                default:
                    break;
            }
            i++;
        }
    }

    public boolean getBtState() {
        return (this.tds_flags_bt & 24) == 8;
    }

    public String getSerial() {
        return this.mDeviceSerial;
    }

    public int getWifiState() {
        return this.tds_flags_wifi & 24;
    }

    public boolean getFactoryMode() {
        return (this.wifi_flags & -128) == -128;
    }

    public int getWifiMode() {
        return (this.wifi_flags & 64) == 64 ? 1 : 0;
    }

    public boolean isSupportOSC() {
        return (this.wifi_flags & 32) == 32;
    }

    public int getWifiConnCount() {
        return (this.wifi_flags & 28) >> 2;
    }

    public String toString() {
        return (((((("" + "factory mode : " + getFactoryMode() + "\n") + "bluetooth state : " + getBtState() + "\n") + "device serial : " + getSerial() + "\n") + "wifi state(0:off,8:on,16:busy) : " + getWifiState() + "\n") + "wifi mode : " + MODE.toString[getWifiMode()] + "\n") + "wifi connect count : " + getWifiConnCount() + "\n") + "support OSC : " + isSupportOSC() + "\n";
    }
}
