package com.google.android.apps.gsa.search.shared.service.proto.nano;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.ExtendableMessageNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import java.io.IOException;

public final class LensServiceEventData extends ExtendableMessageNano<LensServiceEventData> implements Cloneable {
    private static volatile LensServiceEventData[] _emptyArray;
    private int bitField0_;
    private int serviceApiVersion_;

    public static LensServiceEventData[] emptyArray() {
        if (_emptyArray == null) {
            synchronized (InternalNano.LAZY_INIT_LOCK) {
                if (_emptyArray == null) {
                    _emptyArray = new LensServiceEventData[0];
                }
            }
        }
        return _emptyArray;
    }

    public int getServiceApiVersion() {
        return this.serviceApiVersion_;
    }

    public boolean hasServiceApiVersion() {
        return (this.bitField0_ & 1) != 0;
    }

    public LensServiceEventData clearServiceApiVersion() {
        this.serviceApiVersion_ = 0;
        this.bitField0_ &= -2;
        return this;
    }

    public LensServiceEventData setServiceApiVersion(int value) {
        this.bitField0_ |= 1;
        this.serviceApiVersion_ = value;
        return this;
    }

    public LensServiceEventData() {
        clear();
    }

    public LensServiceEventData clear() {
        this.bitField0_ = 0;
        this.serviceApiVersion_ = 0;
        this.unknownFieldData = null;
        this.cachedSize = -1;
        return this;
    }

    public LensServiceEventData clone() {
        try {
            return (LensServiceEventData) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public void writeTo(CodedOutputByteBufferNano output) throws IOException {
        if ((this.bitField0_ & 1) != 0) {
            output.writeInt32(1, this.serviceApiVersion_);
        }
        super.writeTo(output);
    }

    protected int computeSerializedSize() {
        int size = super.computeSerializedSize();
        if ((this.bitField0_ & 1) != 0) {
            return size + CodedOutputByteBufferNano.computeInt32Size(1, this.serviceApiVersion_);
        }
        return size;
    }

    public LensServiceEventData mergeFrom(CodedInputByteBufferNano input) throws IOException {
        while (true) {
            int tag = input.readTag();
            switch (tag) {
                case 0:
                    break;
                case 8:
                    this.serviceApiVersion_ = input.readInt32();
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

    public static LensServiceEventData parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
        return (LensServiceEventData) MessageNano.mergeFrom(new LensServiceEventData(), data);
    }

    public static LensServiceEventData parseFrom(CodedInputByteBufferNano input) throws IOException {
        return new LensServiceEventData().mergeFrom(input);
    }
}
