package com.google.protobuf.nano;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.MapEntryLite;
import com.google.protobuf.MapFieldLite;
import com.google.protobuf.Parser;
import java.io.IOException;

public final class CodedInputByteBufferNano {
    private static final int DEFAULT_RECURSION_LIMIT = 64;
    private static final int DEFAULT_SIZE_LIMIT = 67108864;
    private final byte[] buffer;
    private int bufferPos;
    private final int bufferSize;
    private int bufferSizeAfterLimit;
    private final int bufferStart;
    private CodedInputStream codedInputStream;
    private int currentLimit = Integer.MAX_VALUE;
    private int lastTag;
    private int maybeLimitedBufferSize;
    private int recursionDepth;
    private int recursionLimit = 64;
    private int sizeLimit = DEFAULT_SIZE_LIMIT;

    public static CodedInputByteBufferNano newInstance(byte[] buf) {
        return newInstance(buf, 0, buf.length);
    }

    public static CodedInputByteBufferNano newInstance(byte[] buf, int off, int len) {
        return new CodedInputByteBufferNano(buf, off, len);
    }

    public int readTag() throws IOException {
        if (isAtEnd()) {
            this.lastTag = 0;
            return 0;
        }
        this.lastTag = readRawVarint32();
        if (this.lastTag != 0) {
            return this.lastTag;
        }
        throw InvalidProtocolBufferNanoException.invalidTag();
    }

    public void checkLastTagWas(int value) throws InvalidProtocolBufferNanoException {
        if (this.lastTag != value) {
            throw InvalidProtocolBufferNanoException.invalidEndTag();
        }
    }

    public boolean skipField(int tag) throws IOException {
        switch (WireFormatNano.getTagWireType(tag)) {
            case 0:
                readInt32();
                return true;
            case 1:
                readRawLittleEndian64();
                return true;
            case 2:
                skipRawBytes(readRawVarint32());
                return true;
            case 3:
                skipMessage();
                checkLastTagWas(WireFormatNano.makeTag(WireFormatNano.getTagFieldNumber(tag), 4));
                return true;
            case 4:
                return false;
            case 5:
                readRawLittleEndian32();
                return true;
            default:
                throw InvalidProtocolBufferNanoException.invalidWireType();
        }
    }

    public void skipMessage() throws IOException {
        int tag;
        do {
            tag = readTag();
            if (tag == 0) {
                return;
            }
        } while (skipField(tag));
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readRawLittleEndian64());
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readRawLittleEndian32());
    }

    public long readUInt64() throws IOException {
        return readRawVarint64();
    }

    public long readInt64() throws IOException {
        return readRawVarint64();
    }

    public int readInt32() throws IOException {
        return readRawVarint32();
    }

    public long readFixed64() throws IOException {
        return readRawLittleEndian64();
    }

    public int readFixed32() throws IOException {
        return readRawLittleEndian32();
    }

    public boolean readBool() throws IOException {
        return readRawVarint32() != 0;
    }

    public String readString() throws IOException {
        int size = readRawVarint32();
        if (size < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        } else if (size > this.maybeLimitedBufferSize - this.bufferPos) {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        } else {
            String result = new String(this.buffer, this.bufferPos, size, InternalNano.UTF_8);
            this.bufferPos += size;
            return result;
        }
    }

    public void readGroup(MessageNano msg, int fieldNumber) throws IOException {
        if (this.recursionDepth >= this.recursionLimit) {
            throw InvalidProtocolBufferNanoException.recursionLimitExceeded();
        }
        this.recursionDepth++;
        msg.mergeFrom(this);
        checkLastTagWas(WireFormatNano.makeTag(fieldNumber, 4));
        this.recursionDepth--;
    }

    public void readMessage(MessageNano msg) throws IOException {
        int length = readRawVarint32();
        if (this.recursionDepth >= this.recursionLimit) {
            throw InvalidProtocolBufferNanoException.recursionLimitExceeded();
        }
        int oldLimit = pushLimit(length);
        this.recursionDepth++;
        msg.mergeFrom(this);
        checkLastTagWas(0);
        this.recursionDepth--;
        popLimit(oldLimit);
    }

    public byte[] readBytes() throws IOException {
        int size = readRawVarint32();
        if (size < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        } else if (size == 0) {
            return WireFormatNano.EMPTY_BYTES;
        } else {
            if (size > this.maybeLimitedBufferSize - this.bufferPos) {
                throw InvalidProtocolBufferNanoException.truncatedMessage();
            }
            byte[] result = new byte[size];
            System.arraycopy(this.buffer, this.bufferPos, result, 0, size);
            this.bufferPos += size;
            return result;
        }
    }

    public int readUInt32() throws IOException {
        return readRawVarint32();
    }

    public int readEnum() throws IOException {
        return readRawVarint32();
    }

    public int readSFixed32() throws IOException {
        return readRawLittleEndian32();
    }

    public long readSFixed64() throws IOException {
        return readRawLittleEndian64();
    }

    public int readSInt32() throws IOException {
        return decodeZigZag32(readRawVarint32());
    }

    public long readSInt64() throws IOException {
        return decodeZigZag64(readRawVarint64());
    }

    public int readRawVarint32() throws IOException {
        byte tmp = readRawByte();
        if (tmp >= (byte) 0) {
            return tmp;
        }
        int result = tmp & 127;
        tmp = readRawByte();
        if (tmp >= (byte) 0) {
            return result | (tmp << 7);
        }
        result |= (tmp & 127) << 7;
        tmp = readRawByte();
        if (tmp >= (byte) 0) {
            return result | (tmp << 14);
        }
        result |= (tmp & 127) << 14;
        tmp = readRawByte();
        if (tmp >= (byte) 0) {
            return result | (tmp << 21);
        }
        result |= (tmp & 127) << 21;
        tmp = readRawByte();
        result |= tmp << 28;
        if (tmp >= (byte) 0) {
            return result;
        }
        for (int i = 0; i < 5; i++) {
            if (readRawByte() >= (byte) 0) {
                return result;
            }
        }
        throw InvalidProtocolBufferNanoException.malformedVarint();
    }

    public long readRawVarint64() throws IOException {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            byte b = readRawByte();
            result |= ((long) (b & 127)) << shift;
            if ((b & 128) == 0) {
                return result;
            }
        }
        throw InvalidProtocolBufferNanoException.malformedVarint();
    }

    public int readRawLittleEndian32() throws IOException {
        return (((readRawByte() & 255) | ((readRawByte() & 255) << 8)) | ((readRawByte() & 255) << 16)) | ((readRawByte() & 255) << 24);
    }

    public long readRawLittleEndian64() throws IOException {
        return (((((((((long) readRawByte()) & 255) | ((((long) readRawByte()) & 255) << 8)) | ((((long) readRawByte()) & 255) << 16)) | ((((long) readRawByte()) & 255) << 24)) | ((((long) readRawByte()) & 255) << 32)) | ((((long) readRawByte()) & 255) << 40)) | ((((long) readRawByte()) & 255) << 48)) | ((((long) readRawByte()) & 255) << 56);
    }

    public static int decodeZigZag32(int n) {
        return (n >>> 1) ^ (-(n & 1));
    }

    public static long decodeZigZag64(long n) {
        return (n >>> 1) ^ (-(1 & n));
    }

    private CodedInputByteBufferNano(byte[] buffer, int off, int len) {
        this.buffer = buffer;
        this.bufferStart = off;
        int i = off + len;
        this.maybeLimitedBufferSize = i;
        this.bufferSize = i;
        this.bufferPos = off;
    }

    private CodedInputStream getCodedInputStream() throws IOException {
        if (this.codedInputStream == null) {
            this.codedInputStream = CodedInputStream.newInstance(this.buffer, this.bufferStart, this.bufferSize);
        }
        int liteBytesRead = this.codedInputStream.getTotalBytesRead();
        int nanoBytesRead = this.bufferPos - this.bufferStart;
        if (liteBytesRead > nanoBytesRead) {
            throw new IOException(String.format("CodedInputStream read ahead of CodedInputByteBufferNano: %s > %s", new Object[]{Integer.valueOf(liteBytesRead), Integer.valueOf(nanoBytesRead)}));
        }
        this.codedInputStream.skipRawBytes(nanoBytesRead - liteBytesRead);
        this.codedInputStream.setRecursionLimit(this.recursionLimit - this.recursionDepth);
        return this.codedInputStream;
    }

    public <T extends GeneratedMessageLite<T, ?>> T readMessageLite(Parser<T> parser) throws IOException {
        GeneratedMessageLite result = (GeneratedMessageLite) getCodedInputStream().readMessage(parser, ExtensionRegistryLite.getGeneratedRegistry());
        skipField(this.lastTag);
        return result;
    }

    public <T extends GeneratedMessageLite<T, ?>> T readGroupLite(Parser<T> parser, int fieldNumber) throws IOException {
        GeneratedMessageLite result = (GeneratedMessageLite) getCodedInputStream().readGroup(fieldNumber, parser, ExtensionRegistryLite.getGeneratedRegistry());
        skipField(this.lastTag);
        checkLastTagWas(WireFormatNano.makeTag(fieldNumber, 4));
        return result;
    }

    public <K, V> void readMapEntryInto(MapFieldLite<K, V> map, MapEntryLite<K, V> defaultEntry) throws IOException {
        defaultEntry.parseInto(map, getCodedInputStream(), ExtensionRegistryLite.getGeneratedRegistry());
        skipField(this.lastTag);
    }

    public int setRecursionLimit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Recursion limit cannot be negative: " + limit);
        }
        int oldLimit = this.recursionLimit;
        this.recursionLimit = limit;
        return oldLimit;
    }

    public int setSizeLimit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Size limit cannot be negative: " + limit);
        }
        int oldLimit = this.sizeLimit;
        this.sizeLimit = limit;
        return oldLimit;
    }

    public void resetSizeCounter() {
    }

    public int pushLimit(int byteLimit) throws InvalidProtocolBufferNanoException {
        if (byteLimit < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        }
        byteLimit += this.bufferPos;
        int oldLimit = this.currentLimit;
        if (byteLimit > oldLimit) {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
        this.currentLimit = byteLimit;
        recomputeBufferSizeAfterLimit();
        return oldLimit;
    }

    private void recomputeBufferSizeAfterLimit() {
        this.maybeLimitedBufferSize += this.bufferSizeAfterLimit;
        int bufferEnd = this.maybeLimitedBufferSize;
        if (bufferEnd > this.currentLimit) {
            this.bufferSizeAfterLimit = bufferEnd - this.currentLimit;
            this.maybeLimitedBufferSize -= this.bufferSizeAfterLimit;
            return;
        }
        this.bufferSizeAfterLimit = 0;
    }

    public void popLimit(int oldLimit) {
        this.currentLimit = oldLimit;
        recomputeBufferSizeAfterLimit();
    }

    public int getBytesUntilLimit() {
        if (this.currentLimit == Integer.MAX_VALUE) {
            return -1;
        }
        return this.currentLimit - this.bufferPos;
    }

    public boolean isAtEnd() {
        return this.bufferPos == this.maybeLimitedBufferSize;
    }

    public int getPosition() {
        return this.bufferPos - this.bufferStart;
    }

    public byte[] getData(int offset, int length) {
        if (length == 0) {
            return WireFormatNano.EMPTY_BYTES;
        }
        byte[] copy = new byte[length];
        System.arraycopy(this.buffer, this.bufferStart + offset, copy, 0, length);
        return copy;
    }

    public void rewindToPosition(int position) {
        rewindToPositionAndTag(position, this.lastTag);
    }

    void rewindToPositionAndTag(int position, int tag) {
        if (position > this.bufferPos - this.bufferStart) {
            throw new IllegalArgumentException("Position " + position + " is beyond current " + (this.bufferPos - this.bufferStart));
        } else if (position < 0) {
            throw new IllegalArgumentException("Bad position " + position);
        } else {
            this.bufferPos = this.bufferStart + position;
            this.lastTag = tag;
        }
    }

    public byte readRawByte() throws IOException {
        if (this.bufferPos == this.maybeLimitedBufferSize) {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
        byte[] bArr = this.buffer;
        int i = this.bufferPos;
        this.bufferPos = i + 1;
        return bArr[i];
    }

    public byte[] readRawBytes(int size) throws IOException {
        if (size < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        } else if (this.bufferPos + size > this.currentLimit) {
            skipRawBytes(this.currentLimit - this.bufferPos);
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        } else if (size > this.maybeLimitedBufferSize - this.bufferPos) {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        } else {
            byte[] bytes = new byte[size];
            System.arraycopy(this.buffer, this.bufferPos, bytes, 0, size);
            this.bufferPos += size;
            return bytes;
        }
    }

    public void skipRawBytes(int size) throws IOException {
        if (size < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        } else if (this.bufferPos + size > this.currentLimit) {
            skipRawBytes(this.currentLimit - this.bufferPos);
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        } else if (size <= this.maybeLimitedBufferSize - this.bufferPos) {
            this.bufferPos += size;
        } else {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
    }
}
