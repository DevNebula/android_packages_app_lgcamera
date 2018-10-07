package com.google.android.apps.gsa.search.shared.service.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.FieldType;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.GeneratedMessageLite.DefaultInstanceBasedParser;
import com.google.protobuf.GeneratedMessageLite.MethodToInvoke;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import com.google.protobuf.ProtoField;
import com.google.protobuf.ProtoMessage;
import com.google.protobuf.ProtoPresenceBits;
import com.google.protobuf.ProtoPresenceCheckedField;
import com.google.protobuf.ProtoSyntax;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@ProtoMessage(checkInitialized = {}, messageSetWireFormat = false, protoSyntax = ProtoSyntax.PROTO2)
public final class LensServiceEventData extends GeneratedMessageLite<LensServiceEventData, Builder> implements LensServiceEventDataOrBuilder {
    private static final LensServiceEventData DEFAULT_INSTANCE = new LensServiceEventData();
    private static volatile Parser<LensServiceEventData> PARSER = null;
    public static final int SERVICE_API_VERSION_FIELD_NUMBER = 1;
    @ProtoPresenceBits(id = 0)
    private int bitField0_;
    @ProtoField(fieldNumber = 1, isRequired = false, type = FieldType.INT32)
    @ProtoPresenceCheckedField(mask = 1, presenceBitsId = 0)
    private int serviceApiVersion_;

    /* renamed from: com.google.android.apps.gsa.search.shared.service.proto.LensServiceEventData$1 */
    static /* synthetic */ class C16021 {
        /* renamed from: $SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke */
        static final /* synthetic */ int[] f57xa1df5c61 = new int[MethodToInvoke.values().length];

        static {
            try {
                f57xa1df5c61[MethodToInvoke.NEW_MUTABLE_INSTANCE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                f57xa1df5c61[MethodToInvoke.NEW_BUILDER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                f57xa1df5c61[MethodToInvoke.BUILD_MESSAGE_INFO.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                f57xa1df5c61[MethodToInvoke.GET_DEFAULT_INSTANCE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                f57xa1df5c61[MethodToInvoke.GET_PARSER.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                f57xa1df5c61[MethodToInvoke.GET_MEMOIZED_IS_INITIALIZED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                f57xa1df5c61[MethodToInvoke.SET_MEMOIZED_IS_INITIALIZED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    public static final class Builder extends com.google.protobuf.GeneratedMessageLite.Builder<LensServiceEventData, Builder> implements LensServiceEventDataOrBuilder {
        /* synthetic */ Builder(C16021 x0) {
            this();
        }

        private Builder() {
            super(LensServiceEventData.DEFAULT_INSTANCE);
        }

        public boolean hasServiceApiVersion() {
            return ((LensServiceEventData) this.instance).hasServiceApiVersion();
        }

        public int getServiceApiVersion() {
            return ((LensServiceEventData) this.instance).getServiceApiVersion();
        }

        public Builder setServiceApiVersion(int value) {
            copyOnWrite();
            ((LensServiceEventData) this.instance).setServiceApiVersion(value);
            return this;
        }

        public Builder clearServiceApiVersion() {
            copyOnWrite();
            ((LensServiceEventData) this.instance).clearServiceApiVersion();
            return this;
        }
    }

    private LensServiceEventData() {
    }

    public boolean hasServiceApiVersion() {
        return (this.bitField0_ & 1) == 1;
    }

    public int getServiceApiVersion() {
        return this.serviceApiVersion_;
    }

    private void setServiceApiVersion(int value) {
        this.bitField0_ |= 1;
        this.serviceApiVersion_ = value;
    }

    private void clearServiceApiVersion() {
        this.bitField0_ &= -2;
        this.serviceApiVersion_ = 0;
    }

    public static LensServiceEventData parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
        return (LensServiceEventData) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
    }

    public static LensServiceEventData parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return (LensServiceEventData) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
    }

    public static LensServiceEventData parseFrom(ByteString data) throws InvalidProtocolBufferException {
        return (LensServiceEventData) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
    }

    public static LensServiceEventData parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return (LensServiceEventData) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
    }

    public static LensServiceEventData parseFrom(byte[] data) throws InvalidProtocolBufferException {
        return (LensServiceEventData) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
    }

    public static LensServiceEventData parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return (LensServiceEventData) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
    }

    public static LensServiceEventData parseFrom(InputStream input) throws IOException {
        return (LensServiceEventData) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
    }

    public static LensServiceEventData parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
        return (LensServiceEventData) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
    }

    public static LensServiceEventData parseDelimitedFrom(InputStream input) throws IOException {
        return (LensServiceEventData) parseDelimitedFrom(DEFAULT_INSTANCE, input);
    }

    public static LensServiceEventData parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
        return (LensServiceEventData) parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
    }

    public static LensServiceEventData parseFrom(CodedInputStream input) throws IOException {
        return (LensServiceEventData) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
    }

    public static LensServiceEventData parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
        return (LensServiceEventData) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
    }

    public static Builder newBuilder() {
        return (Builder) DEFAULT_INSTANCE.createBuilder();
    }

    public static Builder newBuilder(LensServiceEventData prototype) {
        return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
    }

    protected final Object dynamicMethod(MethodToInvoke method, Object arg0, Object arg1) {
        Throwable th;
        switch (C16021.f57xa1df5c61[method.ordinal()]) {
            case 1:
                return new LensServiceEventData();
            case 2:
                return new Builder();
            case 3:
                Object[] objects = new Object[]{"bitField0_", "serviceApiVersion_"};
                return newMessageInfo(DEFAULT_INSTANCE, "\u0001\u0001\u0000\u0001\u0001\u0001\u0001\u0002\u0000\u0000\u0000\u0001\u0004\u0000", objects);
            case 4:
                return DEFAULT_INSTANCE;
            case 5:
                Object parser = PARSER;
                if (parser != null) {
                    return parser;
                }
                synchronized (LensServiceEventData.class) {
                    try {
                        parser = PARSER;
                        if (parser == null) {
                            Parser<LensServiceEventData> parser2 = new DefaultInstanceBasedParser(DEFAULT_INSTANCE);
                            try {
                                PARSER = parser2;
                                parser = parser2;
                            } catch (Throwable th2) {
                                th = th2;
                                Parser<LensServiceEventData> parser3 = parser2;
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
        GeneratedMessageLite.registerDefaultInstance(LensServiceEventData.class, DEFAULT_INSTANCE);
    }

    public static LensServiceEventData getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static Parser<LensServiceEventData> parser() {
        return DEFAULT_INSTANCE.getParserForType();
    }
}
