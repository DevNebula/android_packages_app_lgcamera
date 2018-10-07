package com.lge.octopus.tentacles.ble.peripheral;

public interface PeripheralServiceCallback {
    void onPeripheralResult(String str);

    void onReceiveMessage(byte[] bArr);
}
