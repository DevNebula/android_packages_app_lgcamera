package com.google.android.apps.gsa.search.shared.service.proto.nano;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.ExtendableMessageNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import java.io.IOException;

public final class ClientEventProto extends ExtendableMessageNano<ClientEventProto> implements Cloneable {
    private static volatile ClientEventProto[] _emptyArray;
    private int bitField0_;
    private int eventId_;

    public static ClientEventProto[] emptyArray() {
        if (_emptyArray == null) {
            synchronized (InternalNano.LAZY_INIT_LOCK) {
                if (_emptyArray == null) {
                    _emptyArray = new ClientEventProto[0];
                }
            }
        }
        return _emptyArray;
    }

    public int getEventId() {
        return this.eventId_;
    }

    public boolean hasEventId() {
        return (this.bitField0_ & 1) != 0;
    }

    public ClientEventProto clearEventId() {
        this.eventId_ = 0;
        this.bitField0_ &= -2;
        return this;
    }

    public ClientEventProto setEventId(int value) {
        this.bitField0_ |= 1;
        this.eventId_ = value;
        return this;
    }

    public ClientEventProto() {
        clear();
    }

    public ClientEventProto clear() {
        this.bitField0_ = 0;
        this.eventId_ = 0;
        this.unknownFieldData = null;
        this.cachedSize = -1;
        return this;
    }

    public ClientEventProto clone() {
        try {
            return (ClientEventProto) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public void writeTo(CodedOutputByteBufferNano output) throws IOException {
        if ((this.bitField0_ & 1) != 0) {
            output.writeInt32(1, this.eventId_);
        }
        super.writeTo(output);
    }

    protected int computeSerializedSize() {
        int size = super.computeSerializedSize();
        if ((this.bitField0_ & 1) != 0) {
            return size + CodedOutputByteBufferNano.computeInt32Size(1, this.eventId_);
        }
        return size;
    }

    public ClientEventProto mergeFrom(CodedInputByteBufferNano input) throws IOException {
        while (true) {
            int tag = input.readTag();
            switch (tag) {
                case 0:
                    break;
                case 8:
                    this.eventId_ = input.readInt32();
                    this.bitField0_ |= 1;
                    continue;
                default:
                    if (!super.storeUnknownField(input, tag)) {
                        break;
                    }
                    continue;
            }
        }
        return this;
    }

    public static ClientEventProto parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
        return (ClientEventProto) MessageNano.mergeFrom(new ClientEventProto(), data);
    }

    public static ClientEventProto parseFrom(CodedInputByteBufferNano input) throws IOException {
        return new ClientEventProto().mergeFrom(input);
    }
}
