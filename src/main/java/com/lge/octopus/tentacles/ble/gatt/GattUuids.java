package com.lge.octopus.tentacles.ble.gatt;

import android.os.ParcelUuid;
import java.util.HashMap;
import java.util.UUID;

public class GattUuids {
    public static final ParcelUuid PARCEL_UUID_CHAHR_BEAM_CONTROL = ParcelUuid.fromString(STRING_UUID_CHAHR_BEAM_CONTROL);
    public static final ParcelUuid PARCEL_UUID_CHAHR_BT_CONTROL = ParcelUuid.fromString(STRING_UUID_CHAHR_BT_CONTROL);
    public static final ParcelUuid PARCEL_UUID_CHAHR_WIFI_CONTROL = ParcelUuid.fromString(STRING_UUID_CHAHR_WIFI_CONTROL);
    public static final ParcelUuid PARCEL_UUID_LECCP_ACTION = ParcelUuid.fromString(STRING_UUID_LECCP_ACTION);
    public static final ParcelUuid PARCEL_UUID_LECCP_BT = ParcelUuid.fromString(STRING_UUID_LECCP_BT);
    public static final ParcelUuid PARCEL_UUID_LECCP_HAS_PRIVACY = ParcelUuid.fromString(STRING_UUID_LECCP_HAS_PRIVACY);
    public static final ParcelUuid PARCEL_UUID_LECCP_NAME = ParcelUuid.fromString(STRING_UUID_LECCP_NAME);
    public static final ParcelUuid PARCEL_UUID_LECCP_P2P = ParcelUuid.fromString(STRING_UUID_LECCP_P2P);
    public static final ParcelUuid PARCEL_UUID_LECCP_PHONE_NUMBER = ParcelUuid.fromString(STRING_UUID_LECCP_PHONE_NUMBER);
    public static final ParcelUuid PARCEL_UUID_LECCP_STATUS = ParcelUuid.fromString(STRING_UUID_LECCP_STATUS);
    public static final ParcelUuid PARCEL_UUID_LECCP_UPNP_UUID = ParcelUuid.fromString(STRING_UUID_LECCP_UPNP_UUID);
    public static final ParcelUuid PARCEL_UUID_LECCP_WIFI = ParcelUuid.fromString(STRING_UUID_LECCP_WIFI);
    public static final ParcelUuid PARCEL_UUID_LECCP_WIFI_AP = ParcelUuid.fromString(STRING_UUID_LECCP_WIFI_AP);
    public static final ParcelUuid PARCEL_UUID_SERVICE_BANC_BLE = ParcelUuid.fromString(STRING_UUID_SERVICE_BANC_BLE);
    public static final ParcelUuid PARCEL_UUID_SERVICE_LECCP = ParcelUuid.fromString(STRING_UUID_SERVICE_LECCP);
    public static final ParcelUuid PARCEL_UUID_SERVICE_SAMPLE = ParcelUuid.fromString(STRING_UUID_SERVICE_SAMPLE);
    public static final ParcelUuid PARCEL_UUID_SERVICE_TDS = ParcelUuid.fromString(STRING_UUID_SERVICE_TDS);
    public static final ParcelUuid PARCEL_UUID_TDS_HANDOVER_CONTROL_POINT = ParcelUuid.fromString(STRING_UUID_TDS_HANDOVER_CONTROL_POINT);
    public static final String STRING_UUID16_DEFAULT = "ffa0";
    public static final String STRING_UUID_CHAHR_BEAM_CONTROL = "0000ffa3-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_CHAHR_BT_CONTROL = "0000ffa1-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_CHAHR_WIFI_CONTROL = "0000ffa2-0000-1000-8000-00805f9b34fb";
    private static final String STRING_UUID_FORMAT_PREFIX = "0000";
    private static final String STRING_UUID_FORMAT_SUFFIX = "-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_LECCP_ACTION = "00002ab8-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_LECCP_BT = "00002ab1-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_LECCP_HAS_PRIVACY = "00002abb-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_LECCP_NAME = "00002ab0-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_LECCP_P2P = "00002ab3-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_LECCP_PHONE_NUMBER = "00002ab6-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_LECCP_STATUS = "00002ab7-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_LECCP_UPNP_UUID = "00002ab5-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_LECCP_WIFI = "00002ab2-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_LECCP_WIFI_AP = "00002ab4-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_SERVICE_BANC_BLE = "0000ffa0-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_SERVICE_LECCP = "0000feb9-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_SERVICE_SAMPLE = "000055a0-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_SERVICE_TDS = "00001824-0000-1000-8000-00805f9b34fb";
    public static final String STRING_UUID_TDS_HANDOVER_CONTROL_POINT = "00002ABC-0000-1000-8000-00805f9b34fb";
    public static final UUID UUID_CHAHR_BEAM_CONTROL = UUID.fromString(STRING_UUID_CHAHR_BEAM_CONTROL);
    public static final UUID UUID_CHAHR_BT_CONTROL = UUID.fromString(STRING_UUID_CHAHR_BT_CONTROL);
    public static final UUID UUID_CHAHR_WIFI_CONTROL = UUID.fromString(STRING_UUID_CHAHR_WIFI_CONTROL);
    public static final UUID UUID_LECCP_ACTION = UUID.fromString(STRING_UUID_LECCP_ACTION);
    public static final UUID UUID_LECCP_BT = UUID.fromString(STRING_UUID_LECCP_BT);
    public static final UUID UUID_LECCP_HAS_PRIVACY = UUID.fromString(STRING_UUID_LECCP_HAS_PRIVACY);
    public static final UUID UUID_LECCP_NAME = UUID.fromString(STRING_UUID_LECCP_NAME);
    public static final UUID UUID_LECCP_P2P = UUID.fromString(STRING_UUID_LECCP_P2P);
    public static final UUID UUID_LECCP_PHONE_NUMBER = UUID.fromString(STRING_UUID_LECCP_PHONE_NUMBER);
    public static final UUID UUID_LECCP_SERVICE = UUID.fromString(STRING_UUID_SERVICE_LECCP);
    public static final UUID UUID_LECCP_STATUS = UUID.fromString(STRING_UUID_LECCP_STATUS);
    public static final UUID UUID_LECCP_UPNP_UUID = UUID.fromString(STRING_UUID_LECCP_UPNP_UUID);
    public static final UUID UUID_LECCP_WIFI = UUID.fromString(STRING_UUID_LECCP_WIFI);
    public static final UUID UUID_LECCP_WIFI_AP = UUID.fromString(STRING_UUID_LECCP_WIFI_AP);
    public static final UUID UUID_SERVICE_BANC_BLE = UUID.fromString(STRING_UUID_SERVICE_BANC_BLE);
    public static final UUID UUID_SERVICE_SAMPLE = UUID.fromString(STRING_UUID_SERVICE_SAMPLE);
    public static final UUID UUID_SERVICE_TDS = UUID.fromString(STRING_UUID_SERVICE_TDS);
    public static final UUID UUID_TDS_HANDOVER_CONTROL_POINT = UUID.fromString(STRING_UUID_TDS_HANDOVER_CONTROL_POINT);
    private static HashMap<String, String> attributes = new HashMap();

    static {
        attributes.put(STRING_UUID_SERVICE_BANC_BLE, "Kwon Service Sample");
        attributes.put(STRING_UUID_SERVICE_LECCP, "Leccp Service");
        attributes.put(STRING_UUID_SERVICE_TDS, "TDS Service");
        attributes.put(STRING_UUID_CHAHR_BT_CONTROL, "Bluetooth Control");
        attributes.put(STRING_UUID_CHAHR_WIFI_CONTROL, "WiFi Control");
        attributes.put(STRING_UUID_CHAHR_BEAM_CONTROL, "SmartShare Beam Control");
        attributes.put(STRING_UUID_LECCP_HAS_PRIVACY, "Leccp Has Privacy");
        attributes.put(STRING_UUID_LECCP_NAME, "Leccp Name");
        attributes.put(STRING_UUID_LECCP_BT, "Leccp BT");
        attributes.put(STRING_UUID_LECCP_WIFI, "Leccp WiFi");
        attributes.put(STRING_UUID_LECCP_P2P, "Leccp P2P");
        attributes.put(STRING_UUID_LECCP_WIFI_AP, "Leccp WiFi AP");
        attributes.put(STRING_UUID_LECCP_UPNP_UUID, "Leccp UPNP_UUID");
        attributes.put(STRING_UUID_LECCP_PHONE_NUMBER, "Leccp Phone Number");
        attributes.put(STRING_UUID_LECCP_STATUS, "Leccp Status");
        attributes.put(STRING_UUID_LECCP_ACTION, "Leccp Action");
        attributes.put(STRING_UUID_TDS_HANDOVER_CONTROL_POINT, "TDS Handover Control Point");
    }

    public static ParcelUuid parcelUuidOf(int uuid16) {
        return ParcelUuid.fromString(STRING_UUID_FORMAT_PREFIX + String.format("%04x", new Object[]{Integer.valueOf(uuid16)}) + STRING_UUID_FORMAT_SUFFIX);
    }

    public static String lookup(String uuid, String defaultName) {
        String name = (String) attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
