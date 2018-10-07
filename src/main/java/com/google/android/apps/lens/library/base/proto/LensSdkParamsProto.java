package com.google.android.apps.lens.library.base.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.FieldType;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.GeneratedMessageLite.DefaultInstanceBasedParser;
import com.google.protobuf.GeneratedMessageLite.MethodToInvoke;
import com.google.protobuf.Internal.EnumLite;
import com.google.protobuf.Internal.EnumLiteMap;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLiteOrBuilder;
import com.google.protobuf.Parser;
import com.google.protobuf.ProtoField;
import com.google.protobuf.ProtoMessage;
import com.google.protobuf.ProtoPresenceBits;
import com.google.protobuf.ProtoPresenceCheckedField;
import com.google.protobuf.ProtoSyntax;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class LensSdkParamsProto {

    /* renamed from: com.google.android.apps.lens.library.base.proto.LensSdkParamsProto$1 */
    static /* synthetic */ class C16061 {
        /* renamed from: $SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke */
        static final /* synthetic */ int[] f61xa1df5c61 = new int[MethodToInvoke.values().length];

        static {
            try {
                f61xa1df5c61[MethodToInvoke.NEW_MUTABLE_INSTANCE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                f61xa1df5c61[MethodToInvoke.NEW_BUILDER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                f61xa1df5c61[MethodToInvoke.BUILD_MESSAGE_INFO.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                f61xa1df5c61[MethodToInvoke.GET_DEFAULT_INSTANCE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                f61xa1df5c61[MethodToInvoke.GET_PARSER.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                f61xa1df5c61[MethodToInvoke.GET_MEMOIZED_IS_INITIALIZED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                f61xa1df5c61[MethodToInvoke.SET_MEMOIZED_IS_INITIALIZED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    public interface LensSdkParamsOrBuilder extends MessageLiteOrBuilder {
        String getAgsaVersionName();

        ByteString getAgsaVersionNameBytes();

        LensAvailabilityStatus getArStickersAvailabilityStatus();

        LensAvailabilityStatus getLensAvailabilityStatus();

        String getLensSdkVersion();

        ByteString getLensSdkVersionBytes();

        boolean hasAgsaVersionName();

        boolean hasArStickersAvailabilityStatus();

        boolean hasLensAvailabilityStatus();

        boolean hasLensSdkVersion();
    }

    @ProtoMessage(checkInitialized = {}, messageSetWireFormat = false, protoSyntax = ProtoSyntax.PROTO2)
    public static final class LensSdkParams extends GeneratedMessageLite<LensSdkParams, Builder> implements LensSdkParamsOrBuilder {
        public static final int AGSA_VERSION_NAME_FIELD_NUMBER = 2;
        public static final int AR_STICKERS_AVAILABILITY_STATUS_FIELD_NUMBER = 4;
        private static final LensSdkParams DEFAULT_INSTANCE = new LensSdkParams();
        public static final int LENS_AVAILABILITY_STATUS_FIELD_NUMBER = 3;
        public static final int LENS_SDK_VERSION_FIELD_NUMBER = 1;
        private static volatile Parser<LensSdkParams> PARSER;
        @ProtoField(fieldNumber = 2, isEnforceUtf8 = false, isRequired = false, type = FieldType.STRING)
        @ProtoPresenceCheckedField(mask = 2, presenceBitsId = 0)
        private String agsaVersionName_ = "";
        @ProtoField(fieldNumber = 4, isRequired = false, type = FieldType.ENUM)
        @ProtoPresenceCheckedField(mask = 8, presenceBitsId = 0)
        private int arStickersAvailabilityStatus_ = -1;
        @ProtoPresenceBits(id = 0)
        private int bitField0_;
        @ProtoField(fieldNumber = 3, isRequired = false, type = FieldType.ENUM)
        @ProtoPresenceCheckedField(mask = 4, presenceBitsId = 0)
        private int lensAvailabilityStatus_ = -1;
        @ProtoField(fieldNumber = 1, isEnforceUtf8 = false, isRequired = false, type = FieldType.STRING)
        @ProtoPresenceCheckedField(mask = 1, presenceBitsId = 0)
        private String lensSdkVersion_ = "";

        public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<LensSdkParams, Builder> implements LensSdkParamsOrBuilder {
            /* synthetic */ Builder(C16061 x0) {
                this();
            }

            private Builder() {
                super(LensSdkParams.DEFAULT_INSTANCE);
            }

            public boolean hasLensSdkVersion() {
                return ((LensSdkParams) this.instance).hasLensSdkVersion();
            }

            public String getLensSdkVersion() {
                return ((LensSdkParams) this.instance).getLensSdkVersion();
            }

            public ByteString getLensSdkVersionBytes() {
                return ((LensSdkParams) this.instance).getLensSdkVersionBytes();
            }

            public Builder setLensSdkVersion(String value) {
                copyOnWrite();
                ((LensSdkParams) this.instance).setLensSdkVersion(value);
                return this;
            }

            public Builder clearLensSdkVersion() {
                copyOnWrite();
                ((LensSdkParams) this.instance).clearLensSdkVersion();
                return this;
            }

            public Builder setLensSdkVersionBytes(ByteString value) {
                copyOnWrite();
                ((LensSdkParams) this.instance).setLensSdkVersionBytes(value);
                return this;
            }

            public boolean hasAgsaVersionName() {
                return ((LensSdkParams) this.instance).hasAgsaVersionName();
            }

            public String getAgsaVersionName() {
                return ((LensSdkParams) this.instance).getAgsaVersionName();
            }

            public ByteString getAgsaVersionNameBytes() {
                return ((LensSdkParams) this.instance).getAgsaVersionNameBytes();
            }

            public Builder setAgsaVersionName(String value) {
                copyOnWrite();
                ((LensSdkParams) this.instance).setAgsaVersionName(value);
                return this;
            }

            public Builder clearAgsaVersionName() {
                copyOnWrite();
                ((LensSdkParams) this.instance).clearAgsaVersionName();
                return this;
            }

            public Builder setAgsaVersionNameBytes(ByteString value) {
                copyOnWrite();
                ((LensSdkParams) this.instance).setAgsaVersionNameBytes(value);
                return this;
            }

            public boolean hasLensAvailabilityStatus() {
                return ((LensSdkParams) this.instance).hasLensAvailabilityStatus();
            }

            public LensAvailabilityStatus getLensAvailabilityStatus() {
                return ((LensSdkParams) this.instance).getLensAvailabilityStatus();
            }

            public Builder setLensAvailabilityStatus(LensAvailabilityStatus value) {
                copyOnWrite();
                ((LensSdkParams) this.instance).setLensAvailabilityStatus(value);
                return this;
            }

            public Builder clearLensAvailabilityStatus() {
                copyOnWrite();
                ((LensSdkParams) this.instance).clearLensAvailabilityStatus();
                return this;
            }

            public boolean hasArStickersAvailabilityStatus() {
                return ((LensSdkParams) this.instance).hasArStickersAvailabilityStatus();
            }

            public LensAvailabilityStatus getArStickersAvailabilityStatus() {
                return ((LensSdkParams) this.instance).getArStickersAvailabilityStatus();
            }

            public Builder setArStickersAvailabilityStatus(LensAvailabilityStatus value) {
                copyOnWrite();
                ((LensSdkParams) this.instance).setArStickersAvailabilityStatus(value);
                return this;
            }

            public Builder clearArStickersAvailabilityStatus() {
                copyOnWrite();
                ((LensSdkParams) this.instance).clearArStickersAvailabilityStatus();
                return this;
            }
        }

        public enum LensAvailabilityStatus implements EnumLite {
            LENS_AVAILABILITY_UNKNOWN(-1),
            LENS_READY(0),
            LENS_UNAVAILABLE(1),
            LENS_UNAVAILABLE_LOCALE_NOT_SUPPORTED(2),
            LENS_UNAVAILABLE_DEVICE_INCOMPATIBLE(3),
            LENS_UNAVAILABLE_INVALID_CURSOR(4),
            LENS_UNAVAILABLE_DEVICE_LOCKED(5),
            LENS_UNAVAILABLE_UNKNOWN_ERROR_CODE(6);
            
            public static final int LENS_AVAILABILITY_UNKNOWN_VALUE = -1;
            public static final int LENS_READY_VALUE = 0;
            public static final int LENS_UNAVAILABLE_DEVICE_INCOMPATIBLE_VALUE = 3;
            public static final int LENS_UNAVAILABLE_DEVICE_LOCKED_VALUE = 5;
            public static final int LENS_UNAVAILABLE_INVALID_CURSOR_VALUE = 4;
            public static final int LENS_UNAVAILABLE_LOCALE_NOT_SUPPORTED_VALUE = 2;
            public static final int LENS_UNAVAILABLE_UNKNOWN_ERROR_CODE_VALUE = 6;
            public static final int LENS_UNAVAILABLE_VALUE = 1;
            private static final EnumLiteMap<LensAvailabilityStatus> internalValueMap = null;
            private final int value;

            /* renamed from: com.google.android.apps.lens.library.base.proto.LensSdkParamsProto$LensSdkParams$LensAvailabilityStatus$1 */
            class C16071 implements EnumLiteMap<LensAvailabilityStatus> {
                C16071() {
                }

                public LensAvailabilityStatus findValueByNumber(int number) {
                    return LensAvailabilityStatus.forNumber(number);
                }
            }

            static {
                internalValueMap = new C16071();
            }

            public final int getNumber() {
                return this.value;
            }

            public static LensAvailabilityStatus forNumber(int value) {
                switch (value) {
                    case -1:
                        return LENS_AVAILABILITY_UNKNOWN;
                    case 0:
                        return LENS_READY;
                    case 1:
                        return LENS_UNAVAILABLE;
                    case 2:
                        return LENS_UNAVAILABLE_LOCALE_NOT_SUPPORTED;
                    case 3:
                        return LENS_UNAVAILABLE_DEVICE_INCOMPATIBLE;
                    case 4:
                        return LENS_UNAVAILABLE_INVALID_CURSOR;
                    case 5:
                        return LENS_UNAVAILABLE_DEVICE_LOCKED;
                    case 6:
                        return LENS_UNAVAILABLE_UNKNOWN_ERROR_CODE;
                    default:
                        return null;
                }
            }

            public static EnumLiteMap<LensAvailabilityStatus> internalGetValueMap() {
                return internalValueMap;
            }

            private LensAvailabilityStatus(int value) {
                this.value = value;
            }
        }

        private LensSdkParams() {
        }

        public boolean hasLensSdkVersion() {
            return (this.bitField0_ & 1) == 1;
        }

        public String getLensSdkVersion() {
            return this.lensSdkVersion_;
        }

        public ByteString getLensSdkVersionBytes() {
            return ByteString.copyFromUtf8(this.lensSdkVersion_);
        }

        private void setLensSdkVersion(String value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.bitField0_ |= 1;
            this.lensSdkVersion_ = value;
        }

        private void clearLensSdkVersion() {
            this.bitField0_ &= -2;
            this.lensSdkVersion_ = getDefaultInstance().getLensSdkVersion();
        }

        private void setLensSdkVersionBytes(ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.bitField0_ |= 1;
            this.lensSdkVersion_ = value.toStringUtf8();
        }

        public boolean hasAgsaVersionName() {
            return (this.bitField0_ & 2) == 2;
        }

        public String getAgsaVersionName() {
            return this.agsaVersionName_;
        }

        public ByteString getAgsaVersionNameBytes() {
            return ByteString.copyFromUtf8(this.agsaVersionName_);
        }

        private void setAgsaVersionName(String value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.bitField0_ |= 2;
            this.agsaVersionName_ = value;
        }

        private void clearAgsaVersionName() {
            this.bitField0_ &= -3;
            this.agsaVersionName_ = getDefaultInstance().getAgsaVersionName();
        }

        private void setAgsaVersionNameBytes(ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.bitField0_ |= 2;
            this.agsaVersionName_ = value.toStringUtf8();
        }

        public boolean hasLensAvailabilityStatus() {
            return (this.bitField0_ & 4) == 4;
        }

        public LensAvailabilityStatus getLensAvailabilityStatus() {
            LensAvailabilityStatus result = LensAvailabilityStatus.forNumber(this.lensAvailabilityStatus_);
            return result == null ? LensAvailabilityStatus.LENS_AVAILABILITY_UNKNOWN : result;
        }

        private void setLensAvailabilityStatus(LensAvailabilityStatus value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.bitField0_ |= 4;
            this.lensAvailabilityStatus_ = value.getNumber();
        }

        private void clearLensAvailabilityStatus() {
            this.bitField0_ &= -5;
            this.lensAvailabilityStatus_ = -1;
        }

        public boolean hasArStickersAvailabilityStatus() {
            return (this.bitField0_ & 8) == 8;
        }

        public LensAvailabilityStatus getArStickersAvailabilityStatus() {
            LensAvailabilityStatus result = LensAvailabilityStatus.forNumber(this.arStickersAvailabilityStatus_);
            return result == null ? LensAvailabilityStatus.LENS_AVAILABILITY_UNKNOWN : result;
        }

        private void setArStickersAvailabilityStatus(LensAvailabilityStatus value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.bitField0_ |= 8;
            this.arStickersAvailabilityStatus_ = value.getNumber();
        }

        private void clearArStickersAvailabilityStatus() {
            this.bitField0_ &= -9;
            this.arStickersAvailabilityStatus_ = -1;
        }

        public static LensSdkParams parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return (LensSdkParams) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static LensSdkParams parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (LensSdkParams) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static LensSdkParams parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (LensSdkParams) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static LensSdkParams parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (LensSdkParams) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static LensSdkParams parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (LensSdkParams) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
        }

        public static LensSdkParams parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (LensSdkParams) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
        }

        public static LensSdkParams parseFrom(InputStream input) throws IOException {
            return (LensSdkParams) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static LensSdkParams parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (LensSdkParams) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static LensSdkParams parseDelimitedFrom(InputStream input) throws IOException {
            return (LensSdkParams) parseDelimitedFrom(DEFAULT_INSTANCE, input);
        }

        public static LensSdkParams parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (LensSdkParams) parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static LensSdkParams parseFrom(CodedInputStream input) throws IOException {
            return (LensSdkParams) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
        }

        public static LensSdkParams parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (LensSdkParams) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return (Builder) DEFAULT_INSTANCE.createBuilder();
        }

        public static Builder newBuilder(LensSdkParams prototype) {
            return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
        }

        protected final Object dynamicMethod(MethodToInvoke method, Object arg0, Object arg1) {
            Throwable th;
            switch (C16061.f61xa1df5c61[method.ordinal()]) {
                case 1:
                    return new LensSdkParams();
                case 2:
                    return new Builder();
                case 3:
                    Object[] objects = new Object[]{"bitField0_", "lensSdkVersion_", "agsaVersionName_", "lensAvailabilityStatus_", LensAvailabilityStatus.internalGetValueMap(), "arStickersAvailabilityStatus_", LensAvailabilityStatus.internalGetValueMap()};
                    return newMessageInfo(DEFAULT_INSTANCE, "\u0001\u0004\u0000\u0001\u0001\u0004\u0004\u0005\u0000\u0000\u0000\u0001\b\u0000\u0002\b\u0001\u0003\f\u0002\u0004\f\u0003", objects);
                case 4:
                    return DEFAULT_INSTANCE;
                case 5:
                    Object parser = PARSER;
                    if (parser != null) {
                        return parser;
                    }
                    synchronized (LensSdkParams.class) {
                        try {
                            parser = PARSER;
                            if (parser == null) {
                                Parser<LensSdkParams> parser2 = new DefaultInstanceBasedParser(DEFAULT_INSTANCE);
                                try {
                                    PARSER = parser2;
                                    parser = parser2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    Parser<LensSdkParams> parser3 = parser2;
                                    throw th;
                                }
                            }
                            return parser;
                        } catch (Throwable th3) {
                            th = th3;
                            throw th;
                        }
                    }
                case 6:
                    return Byte.valueOf((byte) 1);
                case 7:
                    return null;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        static {
            GeneratedMessageLite.registerDefaultInstance(LensSdkParams.class, DEFAULT_INSTANCE);
        }

        public static LensSdkParams getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<LensSdkParams> parser() {
            return DEFAULT_INSTANCE.getParserForType();
        }
    }

    private LensSdkParamsProto() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite registry) {
    }
}
