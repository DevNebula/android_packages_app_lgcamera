package com.google.android.apps.gsa.search.shared.service.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.FieldType;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.GeneratedMessageLite.DefaultInstanceBasedParser;
import com.google.protobuf.GeneratedMessageLite.ExtendableBuilder;
import com.google.protobuf.GeneratedMessageLite.ExtendableMessage;
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
public final class ClientEventProto extends ExtendableMessage<ClientEventProto, Builder> implements ClientEventProtoOrBuilder {
    private static final ClientEventProto DEFAULT_INSTANCE = new ClientEventProto();
    public static final int EVENT_ID_FIELD_NUMBER = 1;
    private static volatile Parser<ClientEventProto> PARSER;
    @ProtoPresenceBits(id = 0)
    private int bitField0_;
    @ProtoField(fieldNumber = 1, isRequired = false, type = FieldType.INT32)
    @ProtoPresenceCheckedField(mask = 1, presenceBitsId = 0)
    private int eventId_;
    private byte memoizedIsInitialized = (byte) 2;

    /* renamed from: com.google.android.apps.gsa.search.shared.service.proto.ClientEventProto$1 */
    static /* synthetic */ class C16001 {
        /* renamed from: $SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke */
        static final /* synthetic */ int[] f55xa1df5c61 = new int[MethodToInvoke.values().length];

        static {
            try {
                f55xa1df5c61[MethodToInvoke.NEW_MUTABLE_INSTANCE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                f55xa1df5c61[MethodToInvoke.NEW_BUILDER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                f55xa1df5c61[MethodToInvoke.BUILD_MESSAGE_INFO.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                f55xa1df5c61[MethodToInvoke.GET_DEFAULT_INSTANCE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                f55xa1df5c61[MethodToInvoke.GET_PARSER.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                f55xa1df5c61[MethodToInvoke.GET_MEMOIZED_IS_INITIALIZED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                f55xa1df5c61[MethodToInvoke.SET_MEMOIZED_IS_INITIALIZED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    public static final class Builder extends ExtendableBuilder<ClientEventProto, Builder> implements ClientEventProtoOrBuilder {
        /* synthetic */ Builder(C16001 x0) {
            this();
        }

        private Builder() {
            super(ClientEventProto.DEFAULT_INSTANCE);
        }

        public boolean hasEventId() {
            return ((ClientEventProto) this.instance).hasEventId();
        }

        public int getEventId() {
            return ((ClientEventProto) this.instance).getEventId();
        }

        public Builder setEventId(int value) {
            copyOnWrite();
            ((ClientEventProto) this.instance).setEventId(value);
            return this;
        }

        public Builder clearEventId() {
            copyOnWrite();
            ((ClientEventProto) this.instance).clearEventId();
            return this;
        }
    }

    private ClientEventProto() {
    }

    public boolean hasEventId() {
        return (this.bitField0_ & 1) == 1;
    }

    public int getEventId() {
        return this.eventId_;
    }

    private void setEventId(int value) {
        this.bitField0_ |= 1;
        this.eventId_ = value;
    }

    private void clearEventId() {
        this.bitField0_ &= -2;
        this.eventId_ = 0;
    }

    public static ClientEventProto parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
        return (ClientEventProto) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
    }

    public static ClientEventProto parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return (ClientEventProto) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
    }

    public static ClientEventProto parseFrom(ByteString data) throws InvalidProtocolBufferException {
        return (ClientEventProto) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
    }

    public static ClientEventProto parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return (ClientEventProto) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
    }

    public static ClientEventProto parseFrom(byte[] data) throws InvalidProtocolBufferException {
        return (ClientEventProto) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data);
    }

    public static ClientEventProto parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        return (ClientEventProto) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, data, extensionRegistry);
    }

    public static ClientEventProto parseFrom(InputStream input) throws IOException {
        return (ClientEventProto) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
    }

    public static ClientEventProto parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
        return (ClientEventProto) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
    }

    public static ClientEventProto parseDelimitedFrom(InputStream input) throws IOException {
        return (ClientEventProto) parseDelimitedFrom(DEFAULT_INSTANCE, input);
    }

    public static ClientEventProto parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
        return (ClientEventProto) parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
    }

    public static ClientEventProto parseFrom(CodedInputStream input) throws IOException {
        return (ClientEventProto) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input);
    }

    public static ClientEventProto parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
        return (ClientEventProto) GeneratedMessageLite.parseFrom(DEFAULT_INSTANCE, input, extensionRegistry);
    }

    public static Builder newBuilder() {
        return (Builder) DEFAULT_INSTANCE.createBuilder();
    }

    public static Builder newBuilder(ClientEventProto prototype) {
        return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
    }

    protected final Object dynamicMethod(MethodToInvoke method, Object arg0, Object arg1) {
        Throwable th;
        int i = 0;
        switch (C16001.f55xa1df5c61[method.ordinal()]) {
            case 1:
                return new ClientEventProto();
            case 2:
                return new Builder();
            case 3:
                Object[] objects = new Object[]{"bitField0_", "eventId_"};
                return newMessageInfo(DEFAULT_INSTANCE, "\u0001\u0001\u0000\u0001\u0001\u0001\u0001\u0002\u0000\u0000\u0000\u0001\u0004\u0000", objects);
            case 4:
                return DEFAULT_INSTANCE;
            case 5:
                Object parser = PARSER;
                if (parser != null) {
                    return parser;
                }
                synchronized (ClientEventProto.class) {
                    try {
                        parser = PARSER;
                        if (parser == null) {
                            Parser<ClientEventProto> parser2 = new DefaultInstanceBasedParser(DEFAULT_INSTANCE);
                            try {
                                PARSER = parser2;
                                parser = parser2;
                            } catch (Throwable th2) {
                                th = th2;
                                Parser<ClientEventProto> parser3 = parser2;
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
                return Byte.valueOf(this.memoizedIsInitialized);
            case 7:
                if (arg0 != null) {
                    i = 1;
                }
                this.memoizedIsInitialized = (byte) i;
                return null;
            default:
                throw new UnsupportedOperationException();
        }
    }

    static {
        GeneratedMessageLite.registerDefaultInstance(ClientEventProto.class, DEFAULT_INSTANCE);
    }

    public static ClientEventProto getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static Parser<ClientEventProto> parser() {
        return DEFAULT_INSTANCE.getParserForType();
    }
}
