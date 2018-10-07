package com.lge.octopus.tentacles.ble.gatt;

import java.util.UUID;

public class GATTProfile {

    public static class Characteristic {
        public static final UUID CHARACTERISTIC_1 = UUID.fromString("ac451fd3-4206-4ccf-8580-2d0ca43a3a9b");
        public static final UUID CHARACTERISTIC_RANDOM = UUID.randomUUID();
    }

    public static class Descriptor {
        public static final UUID DESCRIPTOR_1 = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    public static class Service {
        public static final UUID SERVICE_UUID_1 = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    }
}
