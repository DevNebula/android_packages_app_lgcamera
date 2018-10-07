package com.google.protobuf.nano.nano;

import com.google.protobuf.nano.DescriptorProtos.EnumOptions;
import com.google.protobuf.nano.DescriptorProtos.EnumValueOptions;
import com.google.protobuf.nano.DescriptorProtos.FieldOptions;
import com.google.protobuf.nano.DescriptorProtos.FileOptions;
import com.google.protobuf.nano.DescriptorProtos.MessageOptions;
import com.google.protobuf.nano.Extension;
import com.google.protobuf.nano.NanoEnumValue;

public abstract class NanoDescriptor {
    public static final Extension<EnumValueOptions, Boolean> emeritus = Extension.createPrimitiveTyped(8, Boolean.class, 1307892264);
    public static final Extension<FileOptions, byte[]> encodedMungee = Extension.createPrimitiveTyped(12, byte[].class, 1257962002);
    public static final Extension<EnumOptions, Boolean> enumAsLite = Extension.createPrimitiveTyped(8, Boolean.class, 1195355736);
    public static final Extension<EnumOptions, String> enumUnmungedFileDescriptorName = Extension.createPrimitiveTyped(9, String.class, 1522362506);
    public static final Extension<FileOptions, Boolean> fileAsLite = Extension.createPrimitiveTyped(8, Boolean.class, 1445185760);
    public static final Extension<FieldOptions, Integer> generateAs = Extension.createPrimitiveTyped(14, Integer.class, 1437615632);
    public static final Extension<EnumOptions, Boolean> legacyEnum = Extension.createPrimitiveTyped(8, Boolean.class, 1308211224);
    public static final Extension<MessageOptions, Boolean> legacyOneof = Extension.createPrimitiveTyped(8, Boolean.class, 1180950304);
    public static final Extension<MessageOptions, Boolean> messageAsLite = Extension.createPrimitiveTyped(8, Boolean.class, 1195348696);
    public static final Extension<MessageOptions, String> messageUnmungedFileDescriptorName = Extension.createPrimitiveTyped(9, String.class, 1522304402);
    public static final Extension<FileOptions, Integer> munger = Extension.createPrimitiveTyped(14, Integer.class, 1243722024);
    public static final Extension<EnumValueOptions, Boolean> unmungedDepsCompliant = Extension.createPrimitiveTyped(8, Boolean.class, 1362093848);
    public static final Extension<EnumValueOptions, Boolean> watermarkCompliant = Extension.createPrimitiveTyped(8, Boolean.class, 1301621224);
    public static final Extension<EnumValueOptions, String[]> whitelisted = Extension.createRepeatedPrimitiveTyped(9, String[].class, 1432768322, 1432768322, 0);

    public interface GenerateAs {
        @NanoEnumValue(legacy = false, value = GenerateAs.class)
        public static final int INVALID = 0;
        @NanoEnumValue(legacy = false, value = GenerateAs.class)
        public static final int LITE = 2;
        @NanoEnumValue(legacy = false, value = GenerateAs.class)
        public static final int NANO = 1;
        @NanoEnumValue(legacy = false, value = GenerateAs.class)
        public static final int OMIT = 3;
    }

    public interface Munger {
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int ADS_EXPRESS_MOBILEAPP = 10;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int AGSA_PROTO = 2;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int ANDROID_UTIL_CONVERT_TO_NANO_PROTOS = 7;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int CLOCKWORK_PROTO = 15;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int COMMERCE_PAYMENTS_INSTORE_TOOLS_CONVERT_TO_NANO_PROTOS = 6;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int DOTS = 18;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int FIXED_CORRECT_PROTO_PLAY_COMMON = 14;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int FIXED_CORRECT_PROTO_PLAY_ENTERTAINMENT = 3;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int FIXED_CORRECT_PROTO_PLAY_STORE = 4;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int GOGGLES_PROTO = 16;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int HANGOUTS = 12;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int INNERTUBE = 11;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int KIDS_MANAGEMENT = 9;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int NONE = 0;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int REDUCED_NANO_PROTO = 1;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int S3_SPEECH_PROTO = 17;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int SIMUX = 8;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int TESTING_NOT_WATERMARK_COMPLIANT = 100;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int TESTING_WATERMARK_COMPLIANT = 101;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int TVSEARCH = 13;
        @NanoEnumValue(legacy = false, value = Munger.class)
        public static final int WASP = 5;
    }

    private NanoDescriptor() {
    }

    @NanoEnumValue(legacy = false, value = GenerateAs.class)
    public static int checkGenerateAsOrThrow(int value) {
        if (value >= 0 && value <= 3) {
            return value;
        }
        throw new IllegalArgumentException(value + " is not a valid enum GenerateAs");
    }

    @NanoEnumValue(legacy = false, value = GenerateAs.class)
    public static int[] checkGenerateAsOrThrow(int[] values) {
        int[] copy = (int[]) values.clone();
        for (int value : copy) {
            checkGenerateAsOrThrow(value);
        }
        return copy;
    }

    @NanoEnumValue(legacy = false, value = Munger.class)
    public static int checkMungerOrThrow(int value) {
        if ((value >= 0 && value <= 18) || (value >= 100 && value <= 101)) {
            return value;
        }
        throw new IllegalArgumentException(value + " is not a valid enum Munger");
    }

    @NanoEnumValue(legacy = false, value = Munger.class)
    public static int[] checkMungerOrThrow(int[] values) {
        int[] copy = (int[]) values.clone();
        for (int value : copy) {
            checkMungerOrThrow(value);
        }
        return copy;
    }
}
