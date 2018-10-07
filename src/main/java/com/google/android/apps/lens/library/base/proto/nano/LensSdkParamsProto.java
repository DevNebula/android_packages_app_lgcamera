package com.google.android.apps.lens.library.base.proto.nano;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.ExtendableMessageNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.NanoEnumValue;
import java.io.IOException;

public abstract class LensSdkParamsProto {

    public static final class LensSdkParams extends ExtendableMessageNano<LensSdkParams> implements Cloneable {
        private static volatile LensSdkParams[] _emptyArray;
        public String agsaVersionName;
        @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
        public int arStickersAvailabilityStatus;
        @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
        public int lensAvailabilityStatus;
        public String lensSdkVersion;

        public interface LensAvailabilityStatus {
            @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
            public static final int LENS_AVAILABILITY_UNKNOWN = -1;
            @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
            public static final int LENS_READY = 0;
            @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
            public static final int LENS_UNAVAILABLE = 1;
            @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
            public static final int LENS_UNAVAILABLE_DEVICE_INCOMPATIBLE = 3;
            @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
            public static final int LENS_UNAVAILABLE_DEVICE_LOCKED = 5;
            @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
            public static final int LENS_UNAVAILABLE_INVALID_CURSOR = 4;
            @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
            public static final int LENS_UNAVAILABLE_LOCALE_NOT_SUPPORTED = 2;
            @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
            public static final int LENS_UNAVAILABLE_UNKNOWN_ERROR_CODE = 6;
        }

        @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
        public static int checkLensAvailabilityStatusOrThrow(int value) {
            if (value >= -1 && value <= 6) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum LensAvailabilityStatus");
        }

        @NanoEnumValue(legacy = false, value = LensAvailabilityStatus.class)
        public static int[] checkLensAvailabilityStatusOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkLensAvailabilityStatusOrThrow(value);
            }
            return copy;
        }

        public static LensSdkParams[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new LensSdkParams[0];
                    }
                }
            }
            return _emptyArray;
        }

        public LensSdkParams() {
            clear();
        }

        public LensSdkParams clear() {
            this.lensSdkVersion = "";
            this.agsaVersionName = "";
            this.lensAvailabilityStatus = -1;
            this.arStickersAvailabilityStatus = -1;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public LensSdkParams clone() {
            try {
                return (LensSdkParams) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.lensSdkVersion == null || this.lensSdkVersion.equals(""))) {
                output.writeString(1, this.lensSdkVersion);
            }
            if (!(this.agsaVersionName == null || this.agsaVersionName.equals(""))) {
                output.writeString(2, this.agsaVersionName);
            }
            if (this.lensAvailabilityStatus != -1) {
                output.writeInt32(3, this.lensAvailabilityStatus);
            }
            if (this.arStickersAvailabilityStatus != -1) {
                output.writeInt32(4, this.arStickersAvailabilityStatus);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!(this.lensSdkVersion == null || this.lensSdkVersion.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.lensSdkVersion);
            }
            if (!(this.agsaVersionName == null || this.agsaVersionName.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(2, this.agsaVersionName);
            }
            if (this.lensAvailabilityStatus != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.lensAvailabilityStatus);
            }
            if (this.arStickersAvailabilityStatus != -1) {
                return size + CodedOutputByteBufferNano.computeInt32Size(4, this.arStickersAvailabilityStatus);
            }
            return size;
        }

        public LensSdkParams mergeFrom(CodedInputByteBufferNano input) throws IOException {
            int initialPos;
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.lensSdkVersion = input.readString();
                        continue;
                    case 18:
                        this.agsaVersionName = input.readString();
                        continue;
                    case 24:
                        initialPos = input.getPosition();
                        try {
                            this.lensAvailabilityStatus = checkLensAvailabilityStatusOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 32:
                        initialPos = input.getPosition();
                        try {
                            this.arStickersAvailabilityStatus = checkLensAvailabilityStatusOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e2) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    default:
                        if (!super.storeUnknownField(input, tag)) {
                            break;
                        }
                        continue;
                }
            }
            return this;
        }

        public static LensSdkParams parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (LensSdkParams) MessageNano.mergeFrom(new LensSdkParams(), data);
        }

        public static LensSdkParams parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new LensSdkParams().mergeFrom(input);
        }
    }

    private LensSdkParamsProto() {
    }
}
