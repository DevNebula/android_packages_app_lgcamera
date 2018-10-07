package com.google.android.apps.gsa.search.shared.service.proto.nano;

import com.google.protobuf.nano.NanoEnumValue;

public abstract class ClientEventOuterClass {
    private ClientEventOuterClass() {
    }

    @NanoEnumValue(legacy = false, value = ClientEventId.class)
    public static int checkClientEventIdOrThrow(int value) {
        if ((value >= 0 && value <= 7) || ((value >= 9 && value <= 12) || ((value >= 14 && value <= 18) || ((value >= 23 && value <= 25) || ((value >= 27 && value <= 27) || ((value >= 29 && value <= 29) || ((value >= 31 && value <= 49) || ((value >= 51 && value <= 55) || ((value >= 57 && value <= 90) || ((value >= 92 && value <= 100) || ((value >= 103 && value <= 103) || ((value >= 105 && value <= 108) || ((value >= 110 && value <= 158) || ((value >= 160 && value <= 163) || ((value >= 166 && value <= 171) || ((value >= 173 && value <= 175) || ((value >= 178 && value <= 192) || ((value >= 194 && value <= 200) || ((value >= 203 && value <= 216) || ((value >= 218 && value <= 228) || ((value >= 230 && value <= 233) || ((value >= 235 && value <= 251) || ((value >= 253 && value <= 265) || ((value >= 267 && value <= 309) || (value >= 311 && value <= 348))))))))))))))))))))))))) {
            return value;
        }
        throw new IllegalArgumentException(value + " is not a valid enum ClientEventId");
    }

    @NanoEnumValue(legacy = false, value = ClientEventId.class)
    public static int[] checkClientEventIdOrThrow(int[] values) {
        int[] copy = (int[]) values.clone();
        for (int value : copy) {
            checkClientEventIdOrThrow(value);
        }
        return copy;
    }
}
