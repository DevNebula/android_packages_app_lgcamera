package com.lge.octopus.tentacles.ble.peripheral;

public interface Peripheral {

    public static abstract class MessageListener {
        public void onReceiveMessage(byte[] message) {
        }
    }

    public interface PeripheralCallback {
        void onResult(String str);
    }

    void finish();

    void initialize();

    void send(byte[] bArr);

    void setMessageListener(MessageListener messageListener);

    void start(PeripheralCallback peripheralCallback);
}
