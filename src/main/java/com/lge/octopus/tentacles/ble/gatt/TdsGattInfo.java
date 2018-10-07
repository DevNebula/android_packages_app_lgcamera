package com.lge.octopus.tentacles.ble.gatt;

public class TdsGattInfo {
    public static final byte ACTIVATE_TRANSPORT = (byte) 1;
    public static final byte ORG_ID_BT_SIG = (byte) 1;
    public static final byte ORG_ID_LG_WIFI = (byte) -2;
    public static final byte ROLE_FLAG = (byte) -64;
    public static final byte ROLE_PROVIDER_ONLY = Byte.MIN_VALUE;
    public static final byte ROLE_SEEKER_AND_PROVIDER = (byte) -64;
    public static final byte ROLE_SEEKER_ONLY = (byte) 64;
    public static final byte WIFI_OVER_AP_OFF = (byte) 3;
    public static final byte WIFI_OVER_AP_ON = (byte) 4;
    public static final byte WIFI_SOFT_AP_OFF = (byte) 1;
    public static final byte WIFI_SOFT_AP_ON = (byte) 2;
}
