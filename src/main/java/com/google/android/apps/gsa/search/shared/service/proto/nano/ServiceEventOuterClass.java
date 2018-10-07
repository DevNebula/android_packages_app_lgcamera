package com.google.android.apps.gsa.search.shared.service.proto.nano;

import com.google.protobuf.nano.NanoEnumValue;

public abstract class ServiceEventOuterClass {
    private ServiceEventOuterClass() {
    }

    @NanoEnumValue(legacy = false, value = ServiceEventId.class)
    public static int checkServiceEventIdOrThrow(int value) {
        if ((value >= 1 && value <= 10) || ((value >= 12 && value <= 18) || ((value >= 20 && value <= 22) || ((value >= 24 && value <= 27) || ((value >= 29 && value <= 39) || ((value >= 42 && value <= 51) || ((value >= 56 && value <= 85) || ((value >= 87 && value <= 89) || ((value >= 91 && value <= 91) || ((value >= 94 && value <= 108) || ((value >= 110 && value <= 111) || ((value >= 113 && value <= 113) || ((value >= 115 && value <= 123) || ((value >= 126 && value <= 146) || ((value >= 148 && value <= 158) || ((value >= 160 && value <= 160) || ((value >= 162 && value <= 168) || ((value >= 170 && value <= 172) || ((value >= 174 && value <= 174) || ((value >= 176 && value <= 176) || ((value >= 178 && value <= 189) || ((value >= 191 && value <= 207) || (value >= 210 && value <= 240))))))))))))))))))))))) {
            return value;
        }
        throw new IllegalArgumentException(value + " is not a valid enum ServiceEventId");
    }

    @NanoEnumValue(legacy = false, value = ServiceEventId.class)
    public static int[] checkServiceEventIdOrThrow(int[] values) {
        int[] copy = (int[]) values.clone();
        for (int value : copy) {
            checkServiceEventIdOrThrow(value);
        }
        return copy;
    }
}
