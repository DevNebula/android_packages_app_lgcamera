package com.lge.octopus.tentacles.ble.utils;

import com.lge.ellievision.parceldata.ISceneCategory;

public class ParseAdvertiseData {
    private static final byte AD_TYPE_ADDRESS = (byte) 27;
    private static final byte AD_TYPE_FLAGS = (byte) 1;
    private static final byte AD_TYPE_LOCAL_NAME_COMPLETE = (byte) 9;
    private static final byte AD_TYPE_LOCAL_NAME_SHORT = (byte) 8;
    private static final byte AD_TYPE_MANUFACTURER_SPECIFIC_DATA = (byte) -1;
    private static final byte AD_TYPE_SERVICE_DATA = (byte) 22;
    private static final byte AD_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = (byte) 7;
    private static final byte AD_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = (byte) 6;
    private static final byte AD_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = (byte) 3;
    private static final byte AD_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = (byte) 2;
    private static final byte AD_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = (byte) 5;
    private static final byte AD_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = (byte) 4;
    private static final byte AD_TYPE_TRANSPORT_DATA = (byte) 38;
    private static final byte AD_TYPE_TX_POWER_LEVEL = (byte) 10;
    private static final byte SERVICE_UUID_TYPE_128_BIT = (byte) 3;
    private static final byte SERVICE_UUID_TYPE_16_BIT = (byte) 1;
    private static final byte SERVICE_UUID_TYPE_32_BIT = (byte) 2;
    private static final String TAG = ParseAdvertiseData.class.getSimpleName();
    private byte[] mAddress;
    private int mAdvDataSize = 0;
    private byte[] mFlag;
    private int[] mIntFlags;
    private byte[] mManufacturer;
    private byte[] mName;
    private byte[] mServiceUuid;
    private int mServiceUuidType = 0;
    private String[] mStrFlags;
    private boolean mTdsNewType = false;
    private byte[] mTransportData;
    private byte[] mUnknownAdTypes;
    private byte[] mUserData;

    public ParseAdvertiseData(byte[] scanRecord) {
        parseData(scanRecord);
    }

    public void parseData(byte[] scanRecord) {
        int currentPos = 0;
        int unknownAdTypes_Pos = 0;
        Logging.m44d(TAG, "called...   scanRecord.length = " + scanRecord.length + ", scanRecord = " + scanRecord);
        if (scanRecord == null) {
            Logging.m45e(TAG, "Because scanRecord == null,   return null");
            return;
        }
        while (currentPos < scanRecord.length) {
            int currentPos2 = currentPos + 1;
            int length = scanRecord[currentPos] & 255;
            Logging.m44d(TAG, "currentPos = " + currentPos2 + ", length = " + length);
            if (length == 0) {
                Logging.m45e(TAG, "Because length == 0,   break");
                currentPos = currentPos2;
                return;
            }
            int dataLength = length - 1;
            currentPos = currentPos2 + 1;
            byte fieldType = scanRecord[currentPos2];
            Logging.m44d(TAG, "dataLength = " + dataLength + ", fieldType = " + fieldType);
            switch (fieldType) {
                case (byte) -1:
                    Logging.m44d(TAG, "case AD_TYPE_MANUFACTURER_SPECIFIC_DATA");
                    this.mManufacturer = extractBytes(scanRecord, currentPos, dataLength);
                    int manufacturerId = ((scanRecord[currentPos + 1] & 255) << 8) + (scanRecord[currentPos] & 255);
                    Logging.m44d(TAG, "manufacturerId = " + manufacturerId);
                    if (manufacturerId != 196) {
                        break;
                    }
                    Logging.m44d(TAG, "scanRecord[currentPos + 2] = " + scanRecord[currentPos + 2]);
                    if (scanRecord[currentPos + 2] != AD_TYPE_TRANSPORT_DATA) {
                        break;
                    }
                    this.mTransportData = extractBytes(scanRecord, currentPos + 2, dataLength);
                    this.mTdsNewType = true;
                    Logging.m46i(TAG, "mTransportData = " + Util.byteArrayToString(this.mTransportData));
                    break;
                case (byte) 1:
                    Logging.m44d(TAG, "case AD_TYPE_FLAGS");
                    this.mFlag = extractBytes(scanRecord, currentPos, dataLength);
                    break;
                case (byte) 2:
                case (byte) 3:
                    if (fieldType == (byte) 2) {
                        Logging.m44d(TAG, "case AD_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL");
                    } else {
                        Logging.m44d(TAG, "case AD_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE");
                    }
                    this.mServiceUuid = extractBytes(scanRecord, currentPos, dataLength);
                    this.mServiceUuidType = 1;
                    break;
                case (byte) 4:
                case (byte) 5:
                    if (fieldType == (byte) 4) {
                        Logging.m44d(TAG, "case AD_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL");
                    } else {
                        Logging.m44d(TAG, "case AD_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE");
                    }
                    this.mServiceUuid = extractBytes(scanRecord, currentPos, dataLength);
                    this.mServiceUuidType = 2;
                    break;
                case (byte) 6:
                case (byte) 7:
                    if (fieldType == AD_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL) {
                        Logging.m44d(TAG, "case AD_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL");
                    } else {
                        Logging.m44d(TAG, "case AD_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE");
                    }
                    this.mServiceUuid = extractBytes(scanRecord, currentPos, dataLength);
                    this.mServiceUuidType = 3;
                    break;
                case (byte) 8:
                case (byte) 9:
                    if (fieldType == AD_TYPE_LOCAL_NAME_SHORT) {
                        Logging.m44d(TAG, "case AD_TYPE_LOCAL_NAME_SHORT");
                    } else {
                        Logging.m44d(TAG, "case AD_TYPE_LOCAL_NAME_COMPLETE");
                    }
                    this.mName = extractBytes(scanRecord, currentPos, dataLength);
                    break;
                case (byte) 10:
                    Logging.m44d(TAG, "case AD_TYPE_TX_POWER_LEVEL");
                    break;
                case (byte) 22:
                    Logging.m44d(TAG, "case AD_TYPE_SERVICE_DATA");
                    break;
                case (byte) 27:
                    Logging.m44d(TAG, "case AD_TYPE_ADDRESS");
                    this.mAddress = extractBytes(scanRecord, currentPos, dataLength);
                    break;
                case (byte) 38:
                    this.mTransportData = extractBytes(scanRecord, currentPos, dataLength);
                    this.mTdsNewType = false;
                    Logging.m46i(TAG, "case AD_TYPE_TRANSPORT_DATA");
                    Logging.m46i(TAG, "mTransportData = " + Util.byteArrayToString(this.mTransportData) + ", mTdsNewType = false");
                    break;
                default:
                    Logging.m44d(TAG, "default - fieldType = " + fieldType);
                    if (fieldType != (byte) 0) {
                        if (this.mUnknownAdTypes == null) {
                            this.mUnknownAdTypes = new byte[length];
                        } else {
                            this.mUnknownAdTypes[unknownAdTypes_Pos] = fieldType;
                            unknownAdTypes_Pos++;
                        }
                    }
                    this.mUserData = extractBytes(scanRecord, currentPos, dataLength);
                    break;
            }
            currentPos += dataLength;
        }
    }

    private static byte[] extractBytes(byte[] scanRecord, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }

    private boolean isUserDataType(int type) {
        if (type != 1 && type != 2 && type != 3 && type != 4 && type != 5 && type != 6 && type != 7 && type != 8 && type != 9 && type != 10 && type != 22 && type != -1 && type != 27) {
            return true;
        }
        Logging.m45e(TAG, "This type(0x" + Integer.toHexString(type) + ") is not the USER DATA Type !!!");
        return false;
    }

    public byte[] getNameByte() {
        return this.mName;
    }

    public byte[] getFlagByte() {
        return this.mFlag;
    }

    public byte[] getManufacturerByte() {
        return this.mManufacturer;
    }

    public byte[] getServiceUuidByte() {
        return this.mServiceUuid;
    }

    public int getServiceUuidInt() {
        return this.mServiceUuidType;
    }

    public byte[] getAddressByte() {
        return this.mAddress;
    }

    public byte[] getUserDataByte() {
        return this.mUserData;
    }

    public byte[] getTransportDataByte() {
        return this.mTransportData;
    }

    public byte[] getUnknownAdTypesByte() {
        return this.mUnknownAdTypes;
    }

    public int getAdvDataSize() {
        return this.mAdvDataSize;
    }

    public boolean getTdsNewType() {
        return this.mTdsNewType;
    }

    public String getNameString() {
        String ret = "";
        if (this.mName == null) {
            return ret + String.format("  Not Support\n", new Object[0]);
        }
        for (byte b : this.mName) {
            ret = ret + ((char) b);
        }
        return ret;
    }

    public String getFlagString() {
        String ret = "";
        if (this.mFlag != null) {
            for (int i = 0; i < this.mFlag.length; i++) {
                ret = ret + String.format("%02x  ", new Object[]{Byte.valueOf(this.mFlag[i])});
            }
        }
        return ret;
    }

    public String getFlagListString() {
        String ret = "";
        if (this.mFlag != null) {
            int flag = this.mFlag[0] & 31;
            Logging.m47v(TAG, "flag:  " + String.format("0x%02x", new Object[]{Integer.valueOf(flag)}));
            boolean first = true;
            for (int i = 0; i < this.mIntFlags.length; i++) {
                if ((this.mIntFlags[i] & flag) != 0) {
                    if (!first) {
                        ret = ret + "\n";
                    }
                    first = false;
                    ret = ret + this.mStrFlags[i];
                }
            }
        }
        Logging.m47v(TAG, ret);
        return ret;
    }

    public String getManufacturerString() {
        String ret = "";
        if (this.mManufacturer == null) {
            return ret + String.format("  Not Support\n", new Object[0]);
        }
        for (int i = 0; i < this.mManufacturer.length; i++) {
            ret = ret + String.format("%02x  ", new Object[]{Byte.valueOf(this.mManufacturer[i])});
        }
        return ret;
    }

    public String getServiceUuidString() {
        String ret = "";
        if (this.mServiceUuid == null) {
            return ret + String.format("  Not Support\n", new Object[0]);
        }
        for (int i = this.mServiceUuid.length - 1; i >= 0; i--) {
            ret = ret + String.format("%02x", new Object[]{Byte.valueOf(this.mServiceUuid[i])});
        }
        return ret;
    }

    public String getServiceUuidTypeString() {
        String ret = "";
        switch (this.mServiceUuidType) {
            case 1:
                return "16";
            case 2:
                return "32";
            case 3:
                return "128";
            default:
                return ISceneCategory.CATEGORY_UNKNOWN;
        }
    }

    public String getAddressString() {
        String ret = "";
        if (this.mAddress == null) {
            return ret + String.format("  Not Support\n", new Object[0]);
        }
        int i = 0;
        while (i < this.mAddress.length) {
            StringBuilder append = new StringBuilder().append(ret);
            String str = "%02x%s";
            Object[] objArr = new Object[2];
            objArr[0] = Byte.valueOf(this.mAddress[i]);
            objArr[1] = i < this.mAddress.length + -1 ? ":" : "";
            ret = append.append(String.format(str, objArr)).toString();
            i++;
        }
        return ret;
    }

    public String getUserDataString() {
        String ret = "";
        if (this.mUserData == null) {
            return ret + String.format("  Not Support\n", new Object[0]);
        }
        for (int i = 0; i < this.mUserData.length; i++) {
            ret = ret + String.format("%02x  ", new Object[]{Byte.valueOf(this.mUserData[i])});
        }
        return ret;
    }

    public String getServiceUuidList(byte[] data) {
        String ret = "";
        if (data == null) {
            return ret + String.format("  Not Support\n", new Object[0]);
        }
        int i = 0;
        while (i < data.length) {
            StringBuilder append = new StringBuilder().append(ret);
            r5 = new Object[2];
            int i2 = i + 1;
            r5[0] = Byte.valueOf(data[i]);
            i = i2 + 1;
            r5[1] = Byte.valueOf(data[i2]);
            ret = append.append(String.format("0x%02x%02x  ", r5)).toString();
            i++;
        }
        return ret;
    }

    public String getTransportDataString() {
        String ret = "";
        if (this.mTransportData == null) {
            return ret + String.format("  Not Support\n", new Object[0]);
        }
        for (int i = 0; i < this.mTransportData.length; i++) {
            ret = ret + String.format("%02x  ", new Object[]{Byte.valueOf(this.mTransportData[i])});
        }
        return ret;
    }

    public String getWifiSsid(byte[] data) {
        String ret = "";
        if (data != null) {
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 >= 8) {
                    break;
                }
                StringBuilder append = new StringBuilder().append(ret);
                r5 = new Object[2];
                i = i2 + 1;
                r5[0] = Byte.valueOf(data[i2]);
                i2 = i + 1;
                r5[1] = Byte.valueOf(data[i]);
                ret = append.append(String.format("%c%c", r5)).toString();
                i = i2 + 1;
            }
        }
        return ret;
    }

    public String getUnknownAdTypesString() {
        String ret = "";
        if (this.mUnknownAdTypes != null) {
            for (int i = 0; i < this.mUnknownAdTypes.length; i++) {
                ret = ret + String.format("%02x  ", new Object[]{Byte.valueOf(this.mUnknownAdTypes[i])});
            }
        }
        return ret;
    }

    private void swapByteArray(byte[] array) {
        if (array != null) {
            byte[] temp = new byte[array.length];
            for (int i = 0; i < array.length; i++) {
                temp[i] = array[i];
            }
        }
    }
}
