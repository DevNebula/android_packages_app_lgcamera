package com.google.protobuf.nano;

import java.io.IOException;

public abstract class ExtendableMessageNano<M extends ExtendableMessageNano<M>> extends MessageNano {
    protected FieldArray unknownFieldData;

    protected int computeSerializedSize() {
        int size = 0;
        if (this.unknownFieldData != null) {
            for (int i = 0; i < this.unknownFieldData.size(); i++) {
                size += this.unknownFieldData.dataAt(i).computeSerializedSize();
            }
        }
        return size;
    }

    protected int computeSerializedSizeAsMessageSet() {
        int size = 0;
        if (this.unknownFieldData != null) {
            for (int i = 0; i < this.unknownFieldData.size(); i++) {
                size += this.unknownFieldData.dataAt(i).computeSerializedSizeAsMessageSet();
            }
        }
        return size;
    }

    public void writeTo(CodedOutputByteBufferNano output) throws IOException {
        if (this.unknownFieldData != null) {
            for (int i = 0; i < this.unknownFieldData.size(); i++) {
                this.unknownFieldData.dataAt(i).writeTo(output);
            }
        }
    }

    protected void writeAsMessageSetTo(CodedOutputByteBufferNano output) throws IOException {
        if (this.unknownFieldData != null) {
            for (int i = 0; i < this.unknownFieldData.size(); i++) {
                this.unknownFieldData.dataAt(i).writeAsMessageSetTo(output);
            }
        }
    }

    public final boolean hasExtension(Extension<M, ?> extension) {
        if (this.unknownFieldData == null || this.unknownFieldData.get(WireFormatNano.getTagFieldNumber(extension.tag)) == null) {
            return false;
        }
        return true;
    }

    public final <T> T getExtension(Extension<M, T> extension) {
        if (this.unknownFieldData == null) {
            return null;
        }
        FieldData field = this.unknownFieldData.get(WireFormatNano.getTagFieldNumber(extension.tag));
        if (field != null) {
            return field.getValue(extension);
        }
        return null;
    }

    public final <T> M setExtension(Extension<M, T> extension, T value) {
        int fieldNumber = WireFormatNano.getTagFieldNumber(extension.tag);
        if (value != null) {
            FieldData field = null;
            if (this.unknownFieldData == null) {
                this.unknownFieldData = new FieldArray();
            } else {
                field = this.unknownFieldData.get(fieldNumber);
            }
            if (field == null) {
                this.unknownFieldData.put(fieldNumber, new FieldData(extension, value));
            } else {
                field.setValue(extension, value);
            }
        } else if (this.unknownFieldData != null) {
            this.unknownFieldData.remove(fieldNumber);
            if (this.unknownFieldData.isEmpty()) {
                this.unknownFieldData = null;
            }
        }
        return this;
    }

    protected final boolean storeUnknownField(CodedInputByteBufferNano input, int tag) throws IOException {
        int startPos = input.getPosition();
        if (!input.skipField(tag)) {
            return false;
        }
        storeUnknownFieldData(WireFormatNano.getTagFieldNumber(tag), new UnknownFieldData(tag, input.getData(startPos, input.getPosition() - startPos)));
        return true;
    }

    private void storeUnknownFieldData(int fieldNumber, UnknownFieldData unknownField) throws IOException {
        FieldData field = null;
        if (this.unknownFieldData == null) {
            this.unknownFieldData = new FieldArray();
        } else {
            field = this.unknownFieldData.get(fieldNumber);
        }
        if (field == null) {
            field = new FieldData();
            this.unknownFieldData.put(fieldNumber, field);
        }
        field.addUnknownField(unknownField);
    }

    protected final boolean storeUnknownFieldAsMessageSet(CodedInputByteBufferNano input, int maybeMessageSetItemTag) throws IOException {
        if (maybeMessageSetItemTag != WireFormatNano.MESSAGE_SET_ITEM_TAG) {
            return storeUnknownField(input, maybeMessageSetItemTag);
        }
        int typeId = 0;
        byte[] rawBytes = null;
        while (true) {
            int tag = input.readTag();
            if (tag == 0) {
                break;
            } else if (tag == WireFormatNano.MESSAGE_SET_TYPE_ID_TAG) {
                typeId = input.readUInt32();
            } else if (tag == WireFormatNano.MESSAGE_SET_MESSAGE_TAG) {
                int startPos = input.getPosition();
                input.skipField(tag);
                rawBytes = input.getData(startPos, input.getPosition() - startPos);
            } else if (!input.skipField(tag)) {
                break;
            }
        }
        input.checkLastTagWas(WireFormatNano.MESSAGE_SET_ITEM_END_TAG);
        if (!(rawBytes == null || typeId == 0)) {
            storeUnknownFieldData(typeId, new UnknownFieldData(typeId, rawBytes));
        }
        return true;
    }

    public M clone() throws CloneNotSupportedException {
        ExtendableMessageNano cloned = (ExtendableMessageNano) super.clone();
        InternalNano.cloneUnknownFieldData(this, cloned);
        return cloned;
    }

    public final FieldArray getUnknownFieldArray() {
        return this.unknownFieldData;
    }
}
