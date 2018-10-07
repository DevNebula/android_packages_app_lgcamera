package com.lge.octopus.tentacles.ble.utils;

public class Util {
    public static String byteArrayToString(byte[] data) {
        String ret = "";
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                ret = ret + String.format("%02x  ", new Object[]{Byte.valueOf(data[i])});
            }
        }
        return ret;
    }

    public static byte[] subbytes(byte[] source, int srcBegin) {
        return subbytes(source, srcBegin, source.length);
    }

    public static byte[] subbytes(byte[] source, int srcBegin, int srcEnd) {
        byte[] destination = new byte[(srcEnd - srcBegin)];
        getBytes(source, srcBegin, srcEnd, destination, 0);
        return destination;
    }

    public static void getBytes(byte[] source, int srcBegin, int srcEnd, byte[] destination, int dstBegin) {
        System.arraycopy(source, srcBegin, destination, dstBegin, srcEnd - srcBegin);
    }
}
