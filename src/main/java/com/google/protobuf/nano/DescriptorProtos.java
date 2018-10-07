package com.google.protobuf.nano;

import java.io.IOException;
import java.util.Arrays;

public abstract class DescriptorProtos {

    public static final class DescriptorProto extends ExtendableMessageNano<DescriptorProto> {
        private static volatile DescriptorProto[] _emptyArray;
        public EnumDescriptorProto[] enumType;
        public FieldDescriptorProto[] extension;
        public ExtensionRange[] extensionRange;
        public FieldDescriptorProto[] field;
        public String name;
        public DescriptorProto[] nestedType;
        public OneofDescriptorProto[] oneofDecl;
        public MessageOptions options;
        public String[] reservedName;
        public ReservedRange[] reservedRange;

        public static final class ExtensionRange extends ExtendableMessageNano<ExtensionRange> {
            private static volatile ExtensionRange[] _emptyArray;
            public int end;
            public ExtensionRangeOptions options;
            public int start;

            public static ExtensionRange[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new ExtensionRange[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public ExtensionRange() {
                clear();
            }

            public ExtensionRange clear() {
                this.start = 0;
                this.end = 0;
                this.options = null;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (this.start != 0) {
                    output.writeInt32(1, this.start);
                }
                if (this.end != 0) {
                    output.writeInt32(2, this.end);
                }
                if (this.options != null) {
                    output.writeMessage(3, this.options);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (this.start != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, this.start);
                }
                if (this.end != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, this.end);
                }
                if (this.options != null) {
                    return size + CodedOutputByteBufferNano.computeMessageSize(3, this.options);
                }
                return size;
            }

            public ExtensionRange mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            break;
                        case 8:
                            this.start = input.readInt32();
                            continue;
                        case 16:
                            this.end = input.readInt32();
                            continue;
                        case 26:
                            if (this.options == null) {
                                this.options = new ExtensionRangeOptions();
                            }
                            input.readMessage(this.options);
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

            public static ExtensionRange parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (ExtensionRange) MessageNano.mergeFrom(new ExtensionRange(), data);
            }

            public static ExtensionRange parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new ExtensionRange().mergeFrom(input);
            }
        }

        public static final class ReservedRange extends ExtendableMessageNano<ReservedRange> {
            private static volatile ReservedRange[] _emptyArray;
            public int end;
            public int start;

            public static ReservedRange[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new ReservedRange[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public ReservedRange() {
                clear();
            }

            public ReservedRange clear() {
                this.start = 0;
                this.end = 0;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (this.start != 0) {
                    output.writeInt32(1, this.start);
                }
                if (this.end != 0) {
                    output.writeInt32(2, this.end);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (this.start != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, this.start);
                }
                if (this.end != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, this.end);
                }
                return size;
            }

            public ReservedRange mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            break;
                        case 8:
                            this.start = input.readInt32();
                            continue;
                        case 16:
                            this.end = input.readInt32();
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

            public static ReservedRange parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (ReservedRange) MessageNano.mergeFrom(new ReservedRange(), data);
            }

            public static ReservedRange parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new ReservedRange().mergeFrom(input);
            }
        }

        public static DescriptorProto[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new DescriptorProto[0];
                    }
                }
            }
            return _emptyArray;
        }

        public DescriptorProto() {
            clear();
        }

        public DescriptorProto clear() {
            this.name = "";
            this.field = FieldDescriptorProto.emptyArray();
            this.extension = FieldDescriptorProto.emptyArray();
            this.nestedType = emptyArray();
            this.enumType = EnumDescriptorProto.emptyArray();
            this.extensionRange = ExtensionRange.emptyArray();
            this.oneofDecl = OneofDescriptorProto.emptyArray();
            this.options = null;
            this.reservedRange = ReservedRange.emptyArray();
            this.reservedName = WireFormatNano.EMPTY_STRING_ARRAY;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.name == null || this.name.equals(""))) {
                output.writeString(1, this.name);
            }
            if (this.field != null && this.field.length > 0) {
                for (FieldDescriptorProto element : this.field) {
                    if (element != null) {
                        output.writeMessage(2, element);
                    }
                }
            }
            if (this.nestedType != null && this.nestedType.length > 0) {
                for (DescriptorProto element2 : this.nestedType) {
                    if (element2 != null) {
                        output.writeMessage(3, element2);
                    }
                }
            }
            if (this.enumType != null && this.enumType.length > 0) {
                for (EnumDescriptorProto element3 : this.enumType) {
                    if (element3 != null) {
                        output.writeMessage(4, element3);
                    }
                }
            }
            if (this.extensionRange != null && this.extensionRange.length > 0) {
                for (ExtensionRange element4 : this.extensionRange) {
                    if (element4 != null) {
                        output.writeMessage(5, element4);
                    }
                }
            }
            if (this.extension != null && this.extension.length > 0) {
                for (FieldDescriptorProto element5 : this.extension) {
                    if (element5 != null) {
                        output.writeMessage(6, element5);
                    }
                }
            }
            if (this.options != null) {
                output.writeMessage(7, this.options);
            }
            if (this.oneofDecl != null && this.oneofDecl.length > 0) {
                for (OneofDescriptorProto element6 : this.oneofDecl) {
                    if (element6 != null) {
                        output.writeMessage(8, element6);
                    }
                }
            }
            if (this.reservedRange != null && this.reservedRange.length > 0) {
                for (ReservedRange element7 : this.reservedRange) {
                    if (element7 != null) {
                        output.writeMessage(9, element7);
                    }
                }
            }
            if (this.reservedName != null && this.reservedName.length > 0) {
                for (String element8 : this.reservedName) {
                    if (element8 != null) {
                        output.writeString(10, element8);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!(this.name == null || this.name.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (this.field != null && this.field.length > 0) {
                for (FieldDescriptorProto element : this.field) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                    }
                }
            }
            if (this.nestedType != null && this.nestedType.length > 0) {
                for (DescriptorProto element2 : this.nestedType) {
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element2);
                    }
                }
            }
            if (this.enumType != null && this.enumType.length > 0) {
                for (EnumDescriptorProto element3 : this.enumType) {
                    if (element3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element3);
                    }
                }
            }
            if (this.extensionRange != null && this.extensionRange.length > 0) {
                for (ExtensionRange element4 : this.extensionRange) {
                    if (element4 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(5, element4);
                    }
                }
            }
            if (this.extension != null && this.extension.length > 0) {
                for (FieldDescriptorProto element5 : this.extension) {
                    if (element5 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(6, element5);
                    }
                }
            }
            if (this.options != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(7, this.options);
            }
            if (this.oneofDecl != null && this.oneofDecl.length > 0) {
                for (OneofDescriptorProto element6 : this.oneofDecl) {
                    if (element6 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(8, element6);
                    }
                }
            }
            if (this.reservedRange != null && this.reservedRange.length > 0) {
                for (ReservedRange element7 : this.reservedRange) {
                    if (element7 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(9, element7);
                    }
                }
            }
            if (this.reservedName == null || this.reservedName.length <= 0) {
                return size;
            }
            int dataCount = 0;
            int dataSize = 0;
            for (String element8 : this.reservedName) {
                if (element8 != null) {
                    dataCount++;
                    dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element8);
                }
            }
            return (size + dataSize) + (dataCount * 1);
        }

        public DescriptorProto mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                FieldDescriptorProto[] newArray;
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.name = input.readString();
                        continue;
                    case 18:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                        if (this.field == null) {
                            i = 0;
                        } else {
                            i = this.field.length;
                        }
                        newArray = new FieldDescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.field, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new FieldDescriptorProto();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new FieldDescriptorProto();
                        input.readMessage(newArray[i]);
                        this.field = newArray;
                        continue;
                    case 26:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                        if (this.nestedType == null) {
                            i = 0;
                        } else {
                            i = this.nestedType.length;
                        }
                        DescriptorProto[] newArray2 = new DescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.nestedType, 0, newArray2, 0, i);
                        }
                        while (i < newArray2.length - 1) {
                            newArray2[i] = new DescriptorProto();
                            input.readMessage(newArray2[i]);
                            input.readTag();
                            i++;
                        }
                        newArray2[i] = new DescriptorProto();
                        input.readMessage(newArray2[i]);
                        this.nestedType = newArray2;
                        continue;
                    case 34:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        if (this.enumType == null) {
                            i = 0;
                        } else {
                            i = this.enumType.length;
                        }
                        EnumDescriptorProto[] newArray3 = new EnumDescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.enumType, 0, newArray3, 0, i);
                        }
                        while (i < newArray3.length - 1) {
                            newArray3[i] = new EnumDescriptorProto();
                            input.readMessage(newArray3[i]);
                            input.readTag();
                            i++;
                        }
                        newArray3[i] = new EnumDescriptorProto();
                        input.readMessage(newArray3[i]);
                        this.enumType = newArray3;
                        continue;
                    case 42:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                        if (this.extensionRange == null) {
                            i = 0;
                        } else {
                            i = this.extensionRange.length;
                        }
                        ExtensionRange[] newArray4 = new ExtensionRange[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.extensionRange, 0, newArray4, 0, i);
                        }
                        while (i < newArray4.length - 1) {
                            newArray4[i] = new ExtensionRange();
                            input.readMessage(newArray4[i]);
                            input.readTag();
                            i++;
                        }
                        newArray4[i] = new ExtensionRange();
                        input.readMessage(newArray4[i]);
                        this.extensionRange = newArray4;
                        continue;
                    case 50:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                        if (this.extension == null) {
                            i = 0;
                        } else {
                            i = this.extension.length;
                        }
                        newArray = new FieldDescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.extension, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new FieldDescriptorProto();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new FieldDescriptorProto();
                        input.readMessage(newArray[i]);
                        this.extension = newArray;
                        continue;
                    case 58:
                        if (this.options == null) {
                            this.options = new MessageOptions();
                        }
                        input.readMessage(this.options);
                        continue;
                    case 66:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 66);
                        if (this.oneofDecl == null) {
                            i = 0;
                        } else {
                            i = this.oneofDecl.length;
                        }
                        OneofDescriptorProto[] newArray5 = new OneofDescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.oneofDecl, 0, newArray5, 0, i);
                        }
                        while (i < newArray5.length - 1) {
                            newArray5[i] = new OneofDescriptorProto();
                            input.readMessage(newArray5[i]);
                            input.readTag();
                            i++;
                        }
                        newArray5[i] = new OneofDescriptorProto();
                        input.readMessage(newArray5[i]);
                        this.oneofDecl = newArray5;
                        continue;
                    case 74:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 74);
                        if (this.reservedRange == null) {
                            i = 0;
                        } else {
                            i = this.reservedRange.length;
                        }
                        ReservedRange[] newArray6 = new ReservedRange[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.reservedRange, 0, newArray6, 0, i);
                        }
                        while (i < newArray6.length - 1) {
                            newArray6[i] = new ReservedRange();
                            input.readMessage(newArray6[i]);
                            input.readTag();
                            i++;
                        }
                        newArray6[i] = new ReservedRange();
                        input.readMessage(newArray6[i]);
                        this.reservedRange = newArray6;
                        continue;
                    case 82:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 82);
                        i = this.reservedName == null ? 0 : this.reservedName.length;
                        String[] newArray7 = new String[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.reservedName, 0, newArray7, 0, i);
                        }
                        while (i < newArray7.length - 1) {
                            newArray7[i] = input.readString();
                            input.readTag();
                            i++;
                        }
                        newArray7[i] = input.readString();
                        this.reservedName = newArray7;
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

        public static DescriptorProto parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DescriptorProto) MessageNano.mergeFrom(new DescriptorProto(), data);
        }

        public static DescriptorProto parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DescriptorProto().mergeFrom(input);
        }
    }

    public static final class EnumDescriptorProto extends ExtendableMessageNano<EnumDescriptorProto> {
        private static volatile EnumDescriptorProto[] _emptyArray;
        public String name;
        public EnumOptions options;
        public String[] reservedName;
        public EnumReservedRange[] reservedRange;
        public EnumValueDescriptorProto[] value;

        public static final class EnumReservedRange extends ExtendableMessageNano<EnumReservedRange> {
            private static volatile EnumReservedRange[] _emptyArray;
            public int end;
            public int start;

            public static EnumReservedRange[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new EnumReservedRange[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public EnumReservedRange() {
                clear();
            }

            public EnumReservedRange clear() {
                this.start = 0;
                this.end = 0;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (this.start != 0) {
                    output.writeInt32(1, this.start);
                }
                if (this.end != 0) {
                    output.writeInt32(2, this.end);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (this.start != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, this.start);
                }
                if (this.end != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, this.end);
                }
                return size;
            }

            public EnumReservedRange mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            break;
                        case 8:
                            this.start = input.readInt32();
                            continue;
                        case 16:
                            this.end = input.readInt32();
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

            public static EnumReservedRange parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (EnumReservedRange) MessageNano.mergeFrom(new EnumReservedRange(), data);
            }

            public static EnumReservedRange parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new EnumReservedRange().mergeFrom(input);
            }
        }

        public static EnumDescriptorProto[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new EnumDescriptorProto[0];
                    }
                }
            }
            return _emptyArray;
        }

        public EnumDescriptorProto() {
            clear();
        }

        public EnumDescriptorProto clear() {
            this.name = "";
            this.value = EnumValueDescriptorProto.emptyArray();
            this.options = null;
            this.reservedRange = EnumReservedRange.emptyArray();
            this.reservedName = WireFormatNano.EMPTY_STRING_ARRAY;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.name == null || this.name.equals(""))) {
                output.writeString(1, this.name);
            }
            if (this.value != null && this.value.length > 0) {
                for (EnumValueDescriptorProto element : this.value) {
                    if (element != null) {
                        output.writeMessage(2, element);
                    }
                }
            }
            if (this.options != null) {
                output.writeMessage(3, this.options);
            }
            if (this.reservedRange != null && this.reservedRange.length > 0) {
                for (EnumReservedRange element2 : this.reservedRange) {
                    if (element2 != null) {
                        output.writeMessage(4, element2);
                    }
                }
            }
            if (this.reservedName != null && this.reservedName.length > 0) {
                for (String element3 : this.reservedName) {
                    if (element3 != null) {
                        output.writeString(5, element3);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!(this.name == null || this.name.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (this.value != null && this.value.length > 0) {
                for (EnumValueDescriptorProto element : this.value) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                    }
                }
            }
            if (this.options != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(3, this.options);
            }
            if (this.reservedRange != null && this.reservedRange.length > 0) {
                for (EnumReservedRange element2 : this.reservedRange) {
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element2);
                    }
                }
            }
            if (this.reservedName == null || this.reservedName.length <= 0) {
                return size;
            }
            int dataCount = 0;
            int dataSize = 0;
            for (String element3 : this.reservedName) {
                if (element3 != null) {
                    dataCount++;
                    dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element3);
                }
            }
            return (size + dataSize) + (dataCount * 1);
        }

        public EnumDescriptorProto mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.name = input.readString();
                        continue;
                    case 18:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                        if (this.value == null) {
                            i = 0;
                        } else {
                            i = this.value.length;
                        }
                        EnumValueDescriptorProto[] newArray = new EnumValueDescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.value, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new EnumValueDescriptorProto();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new EnumValueDescriptorProto();
                        input.readMessage(newArray[i]);
                        this.value = newArray;
                        continue;
                    case 26:
                        if (this.options == null) {
                            this.options = new EnumOptions();
                        }
                        input.readMessage(this.options);
                        continue;
                    case 34:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        if (this.reservedRange == null) {
                            i = 0;
                        } else {
                            i = this.reservedRange.length;
                        }
                        EnumReservedRange[] newArray2 = new EnumReservedRange[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.reservedRange, 0, newArray2, 0, i);
                        }
                        while (i < newArray2.length - 1) {
                            newArray2[i] = new EnumReservedRange();
                            input.readMessage(newArray2[i]);
                            input.readTag();
                            i++;
                        }
                        newArray2[i] = new EnumReservedRange();
                        input.readMessage(newArray2[i]);
                        this.reservedRange = newArray2;
                        continue;
                    case 42:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                        i = this.reservedName == null ? 0 : this.reservedName.length;
                        String[] newArray3 = new String[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.reservedName, 0, newArray3, 0, i);
                        }
                        while (i < newArray3.length - 1) {
                            newArray3[i] = input.readString();
                            input.readTag();
                            i++;
                        }
                        newArray3[i] = input.readString();
                        this.reservedName = newArray3;
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

        public static EnumDescriptorProto parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (EnumDescriptorProto) MessageNano.mergeFrom(new EnumDescriptorProto(), data);
        }

        public static EnumDescriptorProto parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new EnumDescriptorProto().mergeFrom(input);
        }
    }

    public static final class EnumOptions extends ExtendableMessageNano<EnumOptions> {
        private static volatile EnumOptions[] _emptyArray;
        public boolean allowAlias;
        public boolean deprecated;
        public String proto1Name;
        public UninterpretedOption[] uninterpretedOption;

        public static EnumOptions[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new EnumOptions[0];
                    }
                }
            }
            return _emptyArray;
        }

        public EnumOptions() {
            clear();
        }

        public EnumOptions clear() {
            this.proto1Name = "";
            this.allowAlias = false;
            this.deprecated = false;
            this.uninterpretedOption = UninterpretedOption.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.proto1Name == null || this.proto1Name.equals(""))) {
                output.writeString(1, this.proto1Name);
            }
            if (this.allowAlias) {
                output.writeBool(2, this.allowAlias);
            }
            if (this.deprecated) {
                output.writeBool(3, this.deprecated);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        output.writeMessage(999, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!(this.proto1Name == null || this.proto1Name.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.proto1Name);
            }
            if (this.allowAlias) {
                size += CodedOutputByteBufferNano.computeBoolSize(2, this.allowAlias);
            }
            if (this.deprecated) {
                size += CodedOutputByteBufferNano.computeBoolSize(3, this.deprecated);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(999, element);
                    }
                }
            }
            return size;
        }

        public EnumOptions mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.proto1Name = input.readString();
                        continue;
                    case 16:
                        this.allowAlias = input.readBool();
                        continue;
                    case 24:
                        this.deprecated = input.readBool();
                        continue;
                    case 7994:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 7994);
                        if (this.uninterpretedOption == null) {
                            i = 0;
                        } else {
                            i = this.uninterpretedOption.length;
                        }
                        UninterpretedOption[] newArray = new UninterpretedOption[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.uninterpretedOption, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new UninterpretedOption();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new UninterpretedOption();
                        input.readMessage(newArray[i]);
                        this.uninterpretedOption = newArray;
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

        public static EnumOptions parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (EnumOptions) MessageNano.mergeFrom(new EnumOptions(), data);
        }

        public static EnumOptions parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new EnumOptions().mergeFrom(input);
        }
    }

    public static final class EnumValueDescriptorProto extends ExtendableMessageNano<EnumValueDescriptorProto> {
        private static volatile EnumValueDescriptorProto[] _emptyArray;
        public String name;
        public int number;
        public EnumValueOptions options;

        public static EnumValueDescriptorProto[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new EnumValueDescriptorProto[0];
                    }
                }
            }
            return _emptyArray;
        }

        public EnumValueDescriptorProto() {
            clear();
        }

        public EnumValueDescriptorProto clear() {
            this.name = "";
            this.number = 0;
            this.options = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.name == null || this.name.equals(""))) {
                output.writeString(1, this.name);
            }
            if (this.number != 0) {
                output.writeInt32(2, this.number);
            }
            if (this.options != null) {
                output.writeMessage(3, this.options);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!(this.name == null || this.name.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (this.number != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.number);
            }
            if (this.options != null) {
                return size + CodedOutputByteBufferNano.computeMessageSize(3, this.options);
            }
            return size;
        }

        public EnumValueDescriptorProto mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.name = input.readString();
                        continue;
                    case 16:
                        this.number = input.readInt32();
                        continue;
                    case 26:
                        if (this.options == null) {
                            this.options = new EnumValueOptions();
                        }
                        input.readMessage(this.options);
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

        public static EnumValueDescriptorProto parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (EnumValueDescriptorProto) MessageNano.mergeFrom(new EnumValueDescriptorProto(), data);
        }

        public static EnumValueDescriptorProto parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new EnumValueDescriptorProto().mergeFrom(input);
        }
    }

    public static final class EnumValueOptions extends ExtendableMessageNano<EnumValueOptions> {
        private static volatile EnumValueOptions[] _emptyArray;
        public boolean deprecated;
        public UninterpretedOption[] uninterpretedOption;

        public static EnumValueOptions[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new EnumValueOptions[0];
                    }
                }
            }
            return _emptyArray;
        }

        public EnumValueOptions() {
            clear();
        }

        public EnumValueOptions clear() {
            this.deprecated = false;
            this.uninterpretedOption = UninterpretedOption.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.deprecated) {
                output.writeBool(1, this.deprecated);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        output.writeMessage(999, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.deprecated) {
                size += CodedOutputByteBufferNano.computeBoolSize(1, this.deprecated);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(999, element);
                    }
                }
            }
            return size;
        }

        public EnumValueOptions mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 8:
                        this.deprecated = input.readBool();
                        continue;
                    case 7994:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 7994);
                        if (this.uninterpretedOption == null) {
                            i = 0;
                        } else {
                            i = this.uninterpretedOption.length;
                        }
                        UninterpretedOption[] newArray = new UninterpretedOption[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.uninterpretedOption, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new UninterpretedOption();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new UninterpretedOption();
                        input.readMessage(newArray[i]);
                        this.uninterpretedOption = newArray;
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

        public static EnumValueOptions parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (EnumValueOptions) MessageNano.mergeFrom(new EnumValueOptions(), data);
        }

        public static EnumValueOptions parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new EnumValueOptions().mergeFrom(input);
        }
    }

    public static final class ExtensionRangeOptions extends ExtendableMessageNano<ExtensionRangeOptions> {
        private static volatile ExtensionRangeOptions[] _emptyArray;
        public UninterpretedOption[] uninterpretedOption;

        public static ExtensionRangeOptions[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ExtensionRangeOptions[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ExtensionRangeOptions() {
            clear();
        }

        public ExtensionRangeOptions clear() {
            this.uninterpretedOption = UninterpretedOption.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        output.writeMessage(999, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(999, element);
                    }
                }
            }
            return size;
        }

        public ExtensionRangeOptions mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 7994:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 7994);
                        if (this.uninterpretedOption == null) {
                            i = 0;
                        } else {
                            i = this.uninterpretedOption.length;
                        }
                        UninterpretedOption[] newArray = new UninterpretedOption[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.uninterpretedOption, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new UninterpretedOption();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new UninterpretedOption();
                        input.readMessage(newArray[i]);
                        this.uninterpretedOption = newArray;
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

        public static ExtensionRangeOptions parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ExtensionRangeOptions) MessageNano.mergeFrom(new ExtensionRangeOptions(), data);
        }

        public static ExtensionRangeOptions parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ExtensionRangeOptions().mergeFrom(input);
        }
    }

    public static final class FieldDescriptorProto extends ExtendableMessageNano<FieldDescriptorProto> {
        private static volatile FieldDescriptorProto[] _emptyArray;
        public String defaultValue;
        public String extendee;
        public String jsonName;
        @NanoEnumValue(legacy = false, value = Label.class)
        public int label;
        public String name;
        public int number;
        public int oneofIndex;
        public FieldOptions options;
        @NanoEnumValue(legacy = false, value = Type.class)
        public int type;
        public String typeName;

        public interface Label {
            @NanoEnumValue(legacy = false, value = Label.class)
            public static final int LABEL_OPTIONAL = 1;
            @NanoEnumValue(legacy = false, value = Label.class)
            public static final int LABEL_REPEATED = 3;
            @NanoEnumValue(legacy = false, value = Label.class)
            public static final int LABEL_REQUIRED = 2;
        }

        public interface Type {
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_BOOL = 8;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_BYTES = 12;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_DOUBLE = 1;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_ENUM = 14;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_FIXED32 = 7;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_FIXED64 = 6;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_FLOAT = 2;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_GROUP = 10;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_INT32 = 5;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_INT64 = 3;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_MESSAGE = 11;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_SFIXED32 = 15;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_SFIXED64 = 16;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_SINT32 = 17;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_SINT64 = 18;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_STRING = 9;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_UINT32 = 13;
            @NanoEnumValue(legacy = false, value = Type.class)
            public static final int TYPE_UINT64 = 4;
        }

        @NanoEnumValue(legacy = false, value = Type.class)
        public static int checkTypeOrThrow(int value) {
            if (value >= 1 && value <= 18) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum Type");
        }

        @NanoEnumValue(legacy = false, value = Type.class)
        public static int[] checkTypeOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkTypeOrThrow(value);
            }
            return copy;
        }

        @NanoEnumValue(legacy = false, value = Label.class)
        public static int checkLabelOrThrow(int value) {
            if (value >= 1 && value <= 3) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum Label");
        }

        @NanoEnumValue(legacy = false, value = Label.class)
        public static int[] checkLabelOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkLabelOrThrow(value);
            }
            return copy;
        }

        public static FieldDescriptorProto[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new FieldDescriptorProto[0];
                    }
                }
            }
            return _emptyArray;
        }

        public FieldDescriptorProto() {
            clear();
        }

        public FieldDescriptorProto clear() {
            this.name = "";
            this.number = 0;
            this.label = 1;
            this.type = 1;
            this.typeName = "";
            this.extendee = "";
            this.defaultValue = "";
            this.oneofIndex = 0;
            this.jsonName = "";
            this.options = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.name == null || this.name.equals(""))) {
                output.writeString(1, this.name);
            }
            if (!(this.extendee == null || this.extendee.equals(""))) {
                output.writeString(2, this.extendee);
            }
            if (this.number != 0) {
                output.writeInt32(3, this.number);
            }
            if (this.label != 1) {
                output.writeInt32(4, this.label);
            }
            if (this.type != 1) {
                output.writeInt32(5, this.type);
            }
            if (!(this.typeName == null || this.typeName.equals(""))) {
                output.writeString(6, this.typeName);
            }
            if (!(this.defaultValue == null || this.defaultValue.equals(""))) {
                output.writeString(7, this.defaultValue);
            }
            if (this.options != null) {
                output.writeMessage(8, this.options);
            }
            if (this.oneofIndex != 0) {
                output.writeInt32(9, this.oneofIndex);
            }
            if (!(this.jsonName == null || this.jsonName.equals(""))) {
                output.writeString(10, this.jsonName);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!(this.name == null || this.name.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (!(this.extendee == null || this.extendee.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(2, this.extendee);
            }
            if (this.number != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.number);
            }
            if (this.label != 1) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.label);
            }
            if (this.type != 1) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.type);
            }
            if (!(this.typeName == null || this.typeName.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(6, this.typeName);
            }
            if (!(this.defaultValue == null || this.defaultValue.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(7, this.defaultValue);
            }
            if (this.options != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(8, this.options);
            }
            if (this.oneofIndex != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(9, this.oneofIndex);
            }
            if (this.jsonName == null || this.jsonName.equals("")) {
                return size;
            }
            return size + CodedOutputByteBufferNano.computeStringSize(10, this.jsonName);
        }

        public FieldDescriptorProto mergeFrom(CodedInputByteBufferNano input) throws IOException {
            int initialPos;
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.name = input.readString();
                        continue;
                    case 18:
                        this.extendee = input.readString();
                        continue;
                    case 24:
                        this.number = input.readInt32();
                        continue;
                    case 32:
                        initialPos = input.getPosition();
                        try {
                            this.label = checkLabelOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 40:
                        initialPos = input.getPosition();
                        try {
                            this.type = checkTypeOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e2) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 50:
                        this.typeName = input.readString();
                        continue;
                    case 58:
                        this.defaultValue = input.readString();
                        continue;
                    case 66:
                        if (this.options == null) {
                            this.options = new FieldOptions();
                        }
                        input.readMessage(this.options);
                        continue;
                    case 72:
                        this.oneofIndex = input.readInt32();
                        continue;
                    case 82:
                        this.jsonName = input.readString();
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

        public static FieldDescriptorProto parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (FieldDescriptorProto) MessageNano.mergeFrom(new FieldDescriptorProto(), data);
        }

        public static FieldDescriptorProto parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new FieldDescriptorProto().mergeFrom(input);
        }
    }

    public static final class FieldOptions extends ExtendableMessageNano<FieldOptions> {
        private static volatile FieldOptions[] _emptyArray;
        @NanoEnumValue(legacy = false, value = CType.class)
        public int ctype;
        public boolean deprecated;
        public boolean deprecatedRawMessage;
        public boolean enforceUtf8;
        @NanoEnumValue(legacy = false, value = JSType.class)
        public int jstype;
        public boolean lazy;
        public boolean packed;
        public UninterpretedOption[] uninterpretedOption;
        public UpgradedOption[] upgradedOption;
        public boolean weak;

        public interface CType {
            @NanoEnumValue(legacy = false, value = CType.class)
            public static final int CORD = 1;
            @NanoEnumValue(legacy = false, value = CType.class)
            public static final int STRING = 0;
            @NanoEnumValue(legacy = false, value = CType.class)
            public static final int STRING_PIECE = 2;
        }

        public interface JSType {
            @NanoEnumValue(legacy = false, value = JSType.class)
            public static final int JS_NORMAL = 0;
            @NanoEnumValue(legacy = false, value = JSType.class)
            public static final int JS_NUMBER = 2;
            @NanoEnumValue(legacy = false, value = JSType.class)
            public static final int JS_STRING = 1;
        }

        public static final class UpgradedOption extends ExtendableMessageNano<UpgradedOption> {
            private static volatile UpgradedOption[] _emptyArray;
            public String name;
            public String value;

            public static UpgradedOption[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new UpgradedOption[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public UpgradedOption() {
                clear();
            }

            public UpgradedOption clear() {
                this.name = "";
                this.value = "";
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (!(this.name == null || this.name.equals(""))) {
                    output.writeString(1, this.name);
                }
                if (!(this.value == null || this.value.equals(""))) {
                    output.writeString(2, this.value);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (!(this.name == null || this.name.equals(""))) {
                    size += CodedOutputByteBufferNano.computeStringSize(1, this.name);
                }
                if (this.value == null || this.value.equals("")) {
                    return size;
                }
                return size + CodedOutputByteBufferNano.computeStringSize(2, this.value);
            }

            public UpgradedOption mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            break;
                        case 10:
                            this.name = input.readString();
                            continue;
                        case 18:
                            this.value = input.readString();
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

            public static UpgradedOption parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (UpgradedOption) MessageNano.mergeFrom(new UpgradedOption(), data);
            }

            public static UpgradedOption parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new UpgradedOption().mergeFrom(input);
            }
        }

        @NanoEnumValue(legacy = false, value = CType.class)
        public static int checkCTypeOrThrow(int value) {
            if (value >= 0 && value <= 2) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum CType");
        }

        @NanoEnumValue(legacy = false, value = CType.class)
        public static int[] checkCTypeOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkCTypeOrThrow(value);
            }
            return copy;
        }

        @NanoEnumValue(legacy = false, value = JSType.class)
        public static int checkJSTypeOrThrow(int value) {
            if (value >= 0 && value <= 2) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum JSType");
        }

        @NanoEnumValue(legacy = false, value = JSType.class)
        public static int[] checkJSTypeOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkJSTypeOrThrow(value);
            }
            return copy;
        }

        public static FieldOptions[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new FieldOptions[0];
                    }
                }
            }
            return _emptyArray;
        }

        public FieldOptions() {
            clear();
        }

        public FieldOptions clear() {
            this.ctype = 0;
            this.packed = false;
            this.jstype = 0;
            this.lazy = false;
            this.deprecated = false;
            this.weak = false;
            this.upgradedOption = UpgradedOption.emptyArray();
            this.deprecatedRawMessage = false;
            this.enforceUtf8 = true;
            this.uninterpretedOption = UninterpretedOption.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.ctype != 0) {
                output.writeInt32(1, this.ctype);
            }
            if (this.packed) {
                output.writeBool(2, this.packed);
            }
            if (this.deprecated) {
                output.writeBool(3, this.deprecated);
            }
            if (this.lazy) {
                output.writeBool(5, this.lazy);
            }
            if (this.jstype != 0) {
                output.writeInt32(6, this.jstype);
            }
            if (this.weak) {
                output.writeBool(10, this.weak);
            }
            if (this.upgradedOption != null && this.upgradedOption.length > 0) {
                for (UpgradedOption element : this.upgradedOption) {
                    if (element != null) {
                        output.writeMessage(11, element);
                    }
                }
            }
            if (this.deprecatedRawMessage) {
                output.writeBool(12, this.deprecatedRawMessage);
            }
            if (!this.enforceUtf8) {
                output.writeBool(13, this.enforceUtf8);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element2 : this.uninterpretedOption) {
                    if (element2 != null) {
                        output.writeMessage(999, element2);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.ctype != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.ctype);
            }
            if (this.packed) {
                size += CodedOutputByteBufferNano.computeBoolSize(2, this.packed);
            }
            if (this.deprecated) {
                size += CodedOutputByteBufferNano.computeBoolSize(3, this.deprecated);
            }
            if (this.lazy) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, this.lazy);
            }
            if (this.jstype != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, this.jstype);
            }
            if (this.weak) {
                size += CodedOutputByteBufferNano.computeBoolSize(10, this.weak);
            }
            if (this.upgradedOption != null && this.upgradedOption.length > 0) {
                for (UpgradedOption element : this.upgradedOption) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(11, element);
                    }
                }
            }
            if (this.deprecatedRawMessage) {
                size += CodedOutputByteBufferNano.computeBoolSize(12, this.deprecatedRawMessage);
            }
            if (!this.enforceUtf8) {
                size += CodedOutputByteBufferNano.computeBoolSize(13, this.enforceUtf8);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element2 : this.uninterpretedOption) {
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(999, element2);
                    }
                }
            }
            return size;
        }

        public FieldOptions mergeFrom(CodedInputByteBufferNano input) throws IOException {
            int initialPos;
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                switch (tag) {
                    case 0:
                        break;
                    case 8:
                        initialPos = input.getPosition();
                        try {
                            this.ctype = checkCTypeOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 16:
                        this.packed = input.readBool();
                        continue;
                    case 24:
                        this.deprecated = input.readBool();
                        continue;
                    case 40:
                        this.lazy = input.readBool();
                        continue;
                    case 48:
                        initialPos = input.getPosition();
                        try {
                            this.jstype = checkJSTypeOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e2) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 80:
                        this.weak = input.readBool();
                        continue;
                    case 90:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 90);
                        if (this.upgradedOption == null) {
                            i = 0;
                        } else {
                            i = this.upgradedOption.length;
                        }
                        UpgradedOption[] newArray = new UpgradedOption[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.upgradedOption, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new UpgradedOption();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new UpgradedOption();
                        input.readMessage(newArray[i]);
                        this.upgradedOption = newArray;
                        continue;
                    case 96:
                        this.deprecatedRawMessage = input.readBool();
                        continue;
                    case 104:
                        this.enforceUtf8 = input.readBool();
                        continue;
                    case 7994:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 7994);
                        if (this.uninterpretedOption == null) {
                            i = 0;
                        } else {
                            i = this.uninterpretedOption.length;
                        }
                        UninterpretedOption[] newArray2 = new UninterpretedOption[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.uninterpretedOption, 0, newArray2, 0, i);
                        }
                        while (i < newArray2.length - 1) {
                            newArray2[i] = new UninterpretedOption();
                            input.readMessage(newArray2[i]);
                            input.readTag();
                            i++;
                        }
                        newArray2[i] = new UninterpretedOption();
                        input.readMessage(newArray2[i]);
                        this.uninterpretedOption = newArray2;
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

        public static FieldOptions parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (FieldOptions) MessageNano.mergeFrom(new FieldOptions(), data);
        }

        public static FieldOptions parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new FieldOptions().mergeFrom(input);
        }
    }

    public static final class FileDescriptorProto extends ExtendableMessageNano<FileDescriptorProto> {
        private static volatile FileDescriptorProto[] _emptyArray;
        public String[] dependency;
        public EnumDescriptorProto[] enumType;
        public FieldDescriptorProto[] extension;
        public DescriptorProto[] messageType;
        public String name;
        public FileOptions options;
        public String package_;
        public int[] publicDependency;
        public ServiceDescriptorProto[] service;
        public SourceCodeInfo sourceCodeInfo;
        public String syntax;
        public int[] weakDependency;

        public static FileDescriptorProto[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new FileDescriptorProto[0];
                    }
                }
            }
            return _emptyArray;
        }

        public FileDescriptorProto() {
            clear();
        }

        public FileDescriptorProto clear() {
            this.name = "";
            this.package_ = "";
            this.dependency = WireFormatNano.EMPTY_STRING_ARRAY;
            this.publicDependency = WireFormatNano.EMPTY_INT_ARRAY;
            this.weakDependency = WireFormatNano.EMPTY_INT_ARRAY;
            this.messageType = DescriptorProto.emptyArray();
            this.enumType = EnumDescriptorProto.emptyArray();
            this.service = ServiceDescriptorProto.emptyArray();
            this.extension = FieldDescriptorProto.emptyArray();
            this.options = null;
            this.sourceCodeInfo = null;
            this.syntax = "";
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.name == null || this.name.equals(""))) {
                output.writeString(1, this.name);
            }
            if (!(this.package_ == null || this.package_.equals(""))) {
                output.writeString(2, this.package_);
            }
            if (this.dependency != null && this.dependency.length > 0) {
                for (String element : this.dependency) {
                    if (element != null) {
                        output.writeString(3, element);
                    }
                }
            }
            if (this.messageType != null && this.messageType.length > 0) {
                for (DescriptorProto element2 : this.messageType) {
                    if (element2 != null) {
                        output.writeMessage(4, element2);
                    }
                }
            }
            if (this.enumType != null && this.enumType.length > 0) {
                for (EnumDescriptorProto element3 : this.enumType) {
                    if (element3 != null) {
                        output.writeMessage(5, element3);
                    }
                }
            }
            if (this.service != null && this.service.length > 0) {
                for (ServiceDescriptorProto element4 : this.service) {
                    if (element4 != null) {
                        output.writeMessage(6, element4);
                    }
                }
            }
            if (this.extension != null && this.extension.length > 0) {
                for (FieldDescriptorProto element5 : this.extension) {
                    if (element5 != null) {
                        output.writeMessage(7, element5);
                    }
                }
            }
            if (this.options != null) {
                output.writeMessage(8, this.options);
            }
            if (this.sourceCodeInfo != null) {
                output.writeMessage(9, this.sourceCodeInfo);
            }
            if (this.publicDependency != null && this.publicDependency.length > 0) {
                for (int writeInt32 : this.publicDependency) {
                    output.writeInt32(10, writeInt32);
                }
            }
            if (this.weakDependency != null && this.weakDependency.length > 0) {
                for (int writeInt322 : this.weakDependency) {
                    output.writeInt32(11, writeInt322);
                }
            }
            if (!(this.syntax == null || this.syntax.equals(""))) {
                output.writeString(12, this.syntax);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int dataSize;
            int size = super.computeSerializedSize();
            if (!(this.name == null || this.name.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (!(this.package_ == null || this.package_.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(2, this.package_);
            }
            if (this.dependency != null && this.dependency.length > 0) {
                int dataCount = 0;
                dataSize = 0;
                for (String element : this.dependency) {
                    if (element != null) {
                        dataCount++;
                        dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element);
                    }
                }
                size = (size + dataSize) + (dataCount * 1);
            }
            if (this.messageType != null && this.messageType.length > 0) {
                for (DescriptorProto element2 : this.messageType) {
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element2);
                    }
                }
            }
            if (this.enumType != null && this.enumType.length > 0) {
                for (EnumDescriptorProto element3 : this.enumType) {
                    if (element3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(5, element3);
                    }
                }
            }
            if (this.service != null && this.service.length > 0) {
                for (ServiceDescriptorProto element4 : this.service) {
                    if (element4 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(6, element4);
                    }
                }
            }
            if (this.extension != null && this.extension.length > 0) {
                for (FieldDescriptorProto element5 : this.extension) {
                    if (element5 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(7, element5);
                    }
                }
            }
            if (this.options != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(8, this.options);
            }
            if (this.sourceCodeInfo != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(9, this.sourceCodeInfo);
            }
            if (this.publicDependency != null && this.publicDependency.length > 0) {
                dataSize = 0;
                for (int element6 : this.publicDependency) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element6);
                }
                size = (size + dataSize) + (this.publicDependency.length * 1);
            }
            if (this.weakDependency != null && this.weakDependency.length > 0) {
                dataSize = 0;
                for (int element62 : this.weakDependency) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element62);
                }
                size = (size + dataSize) + (this.weakDependency.length * 1);
            }
            if (this.syntax == null || this.syntax.equals("")) {
                return size;
            }
            return size + CodedOutputByteBufferNano.computeStringSize(12, this.syntax);
        }

        public FileDescriptorProto mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                int[] newArray;
                int limit;
                int startPos;
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.name = input.readString();
                        continue;
                    case 18:
                        this.package_ = input.readString();
                        continue;
                    case 26:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                        i = this.dependency == null ? 0 : this.dependency.length;
                        String[] newArray2 = new String[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.dependency, 0, newArray2, 0, i);
                        }
                        while (i < newArray2.length - 1) {
                            newArray2[i] = input.readString();
                            input.readTag();
                            i++;
                        }
                        newArray2[i] = input.readString();
                        this.dependency = newArray2;
                        continue;
                    case 34:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        if (this.messageType == null) {
                            i = 0;
                        } else {
                            i = this.messageType.length;
                        }
                        DescriptorProto[] newArray3 = new DescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.messageType, 0, newArray3, 0, i);
                        }
                        while (i < newArray3.length - 1) {
                            newArray3[i] = new DescriptorProto();
                            input.readMessage(newArray3[i]);
                            input.readTag();
                            i++;
                        }
                        newArray3[i] = new DescriptorProto();
                        input.readMessage(newArray3[i]);
                        this.messageType = newArray3;
                        continue;
                    case 42:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                        if (this.enumType == null) {
                            i = 0;
                        } else {
                            i = this.enumType.length;
                        }
                        EnumDescriptorProto[] newArray4 = new EnumDescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.enumType, 0, newArray4, 0, i);
                        }
                        while (i < newArray4.length - 1) {
                            newArray4[i] = new EnumDescriptorProto();
                            input.readMessage(newArray4[i]);
                            input.readTag();
                            i++;
                        }
                        newArray4[i] = new EnumDescriptorProto();
                        input.readMessage(newArray4[i]);
                        this.enumType = newArray4;
                        continue;
                    case 50:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                        if (this.service == null) {
                            i = 0;
                        } else {
                            i = this.service.length;
                        }
                        ServiceDescriptorProto[] newArray5 = new ServiceDescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.service, 0, newArray5, 0, i);
                        }
                        while (i < newArray5.length - 1) {
                            newArray5[i] = new ServiceDescriptorProto();
                            input.readMessage(newArray5[i]);
                            input.readTag();
                            i++;
                        }
                        newArray5[i] = new ServiceDescriptorProto();
                        input.readMessage(newArray5[i]);
                        this.service = newArray5;
                        continue;
                    case 58:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 58);
                        if (this.extension == null) {
                            i = 0;
                        } else {
                            i = this.extension.length;
                        }
                        FieldDescriptorProto[] newArray6 = new FieldDescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.extension, 0, newArray6, 0, i);
                        }
                        while (i < newArray6.length - 1) {
                            newArray6[i] = new FieldDescriptorProto();
                            input.readMessage(newArray6[i]);
                            input.readTag();
                            i++;
                        }
                        newArray6[i] = new FieldDescriptorProto();
                        input.readMessage(newArray6[i]);
                        this.extension = newArray6;
                        continue;
                    case 66:
                        if (this.options == null) {
                            this.options = new FileOptions();
                        }
                        input.readMessage(this.options);
                        continue;
                    case 74:
                        if (this.sourceCodeInfo == null) {
                            this.sourceCodeInfo = new SourceCodeInfo();
                        }
                        input.readMessage(this.sourceCodeInfo);
                        continue;
                    case 80:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 80);
                        i = this.publicDependency == null ? 0 : this.publicDependency.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.publicDependency, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.publicDependency = newArray;
                        continue;
                    case 82:
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.publicDependency == null ? 0 : this.publicDependency.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.publicDependency, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.publicDependency = newArray;
                        input.popLimit(limit);
                        continue;
                    case 88:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 88);
                        i = this.weakDependency == null ? 0 : this.weakDependency.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.weakDependency, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.weakDependency = newArray;
                        continue;
                    case 90:
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.weakDependency == null ? 0 : this.weakDependency.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.weakDependency, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.weakDependency = newArray;
                        input.popLimit(limit);
                        continue;
                    case 98:
                        this.syntax = input.readString();
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

        public static FileDescriptorProto parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (FileDescriptorProto) MessageNano.mergeFrom(new FileDescriptorProto(), data);
        }

        public static FileDescriptorProto parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new FileDescriptorProto().mergeFrom(input);
        }
    }

    public static final class FileDescriptorSet extends ExtendableMessageNano<FileDescriptorSet> {
        private static volatile FileDescriptorSet[] _emptyArray;
        public FileDescriptorProto[] file;

        public static FileDescriptorSet[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new FileDescriptorSet[0];
                    }
                }
            }
            return _emptyArray;
        }

        public FileDescriptorSet() {
            clear();
        }

        public FileDescriptorSet clear() {
            this.file = FileDescriptorProto.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.file != null && this.file.length > 0) {
                for (FileDescriptorProto element : this.file) {
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.file != null && this.file.length > 0) {
                for (FileDescriptorProto element : this.file) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                }
            }
            return size;
        }

        public FileDescriptorSet mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                        if (this.file == null) {
                            i = 0;
                        } else {
                            i = this.file.length;
                        }
                        FileDescriptorProto[] newArray = new FileDescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.file, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new FileDescriptorProto();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new FileDescriptorProto();
                        input.readMessage(newArray[i]);
                        this.file = newArray;
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

        public static FileDescriptorSet parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (FileDescriptorSet) MessageNano.mergeFrom(new FileDescriptorSet(), data);
        }

        public static FileDescriptorSet parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new FileDescriptorSet().mergeFrom(input);
        }
    }

    public static final class FileOptions extends ExtendableMessageNano<FileOptions> {
        private static volatile FileOptions[] _emptyArray;
        public int ccApiVersion;
        public boolean ccEnableArenas;
        public boolean ccGenericServices;
        public boolean ccUtf8Verification;
        public String csharpNamespace;
        public boolean deprecated;
        public String goPackage;
        public String javaAltApiPackage;
        public int javaApiVersion;
        public boolean javaEnableDualGenerateMutableApi;
        public boolean javaGenericServices;
        public boolean javaJava5Enums;
        public boolean javaMultipleFiles;
        public String javaMultipleFilesMutablePackage;
        public boolean javaMutableApi;
        public String javaOuterClassname;
        public String javaPackage;
        public boolean javaStringCheckUtf8;
        public boolean javaUseJavaproto2;
        public boolean javaUseJavastrings;
        public String javascriptPackage;
        public String objcClassPrefix;
        @NanoEnumValue(legacy = false, value = OptimizeMode.class)
        public int optimizeFor;
        public String phpClassPrefix;
        public boolean phpGenericServices;
        public String phpNamespace;
        public int pyApiVersion;
        public boolean pyGenericServices;
        public String swiftPrefix;
        public int szlApiVersion;
        public UninterpretedOption[] uninterpretedOption;

        public interface CompatibilityLevel {
            @NanoEnumValue(legacy = false, value = CompatibilityLevel.class)
            public static final int NO_COMPATIBILITY = 0;
            @NanoEnumValue(legacy = false, value = CompatibilityLevel.class)
            public static final int PROTO1_COMPATIBLE = 100;
        }

        public interface OptimizeMode {
            @NanoEnumValue(legacy = false, value = OptimizeMode.class)
            public static final int CODE_SIZE = 2;
            @NanoEnumValue(legacy = false, value = OptimizeMode.class)
            public static final int LITE_RUNTIME = 3;
            @NanoEnumValue(legacy = false, value = OptimizeMode.class)
            public static final int SPEED = 1;
        }

        @NanoEnumValue(legacy = false, value = CompatibilityLevel.class)
        public static int checkCompatibilityLevelOrThrow(int value) {
            if ((value >= 0 && value <= 0) || (value >= 100 && value <= 100)) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum CompatibilityLevel");
        }

        @NanoEnumValue(legacy = false, value = CompatibilityLevel.class)
        public static int[] checkCompatibilityLevelOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkCompatibilityLevelOrThrow(value);
            }
            return copy;
        }

        @NanoEnumValue(legacy = false, value = OptimizeMode.class)
        public static int checkOptimizeModeOrThrow(int value) {
            if (value >= 1 && value <= 3) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum OptimizeMode");
        }

        @NanoEnumValue(legacy = false, value = OptimizeMode.class)
        public static int[] checkOptimizeModeOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkOptimizeModeOrThrow(value);
            }
            return copy;
        }

        public static FileOptions[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new FileOptions[0];
                    }
                }
            }
            return _emptyArray;
        }

        public FileOptions() {
            clear();
        }

        public FileOptions clear() {
            this.ccApiVersion = 2;
            this.ccUtf8Verification = true;
            this.javaPackage = "";
            this.pyApiVersion = 2;
            this.javaApiVersion = 2;
            this.javaUseJavaproto2 = true;
            this.javaJava5Enums = true;
            this.javaUseJavastrings = false;
            this.javaAltApiPackage = "";
            this.javaEnableDualGenerateMutableApi = false;
            this.javaOuterClassname = "";
            this.javaMultipleFiles = false;
            this.javaStringCheckUtf8 = false;
            this.javaMutableApi = false;
            this.javaMultipleFilesMutablePackage = "";
            this.optimizeFor = 1;
            this.goPackage = "";
            this.javascriptPackage = "";
            this.szlApiVersion = 1;
            this.ccGenericServices = false;
            this.javaGenericServices = false;
            this.pyGenericServices = false;
            this.phpGenericServices = false;
            this.deprecated = false;
            this.ccEnableArenas = false;
            this.objcClassPrefix = "";
            this.csharpNamespace = "";
            this.swiftPrefix = "";
            this.phpClassPrefix = "";
            this.phpNamespace = "";
            this.uninterpretedOption = UninterpretedOption.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.javaPackage == null || this.javaPackage.equals(""))) {
                output.writeString(1, this.javaPackage);
            }
            if (this.ccApiVersion != 2) {
                output.writeInt32(2, this.ccApiVersion);
            }
            if (this.pyApiVersion != 2) {
                output.writeInt32(4, this.pyApiVersion);
            }
            if (this.javaApiVersion != 2) {
                output.writeInt32(5, this.javaApiVersion);
            }
            if (!this.javaUseJavaproto2) {
                output.writeBool(6, this.javaUseJavaproto2);
            }
            if (!this.javaJava5Enums) {
                output.writeBool(7, this.javaJava5Enums);
            }
            if (!(this.javaOuterClassname == null || this.javaOuterClassname.equals(""))) {
                output.writeString(8, this.javaOuterClassname);
            }
            if (this.optimizeFor != 1) {
                output.writeInt32(9, this.optimizeFor);
            }
            if (this.javaMultipleFiles) {
                output.writeBool(10, this.javaMultipleFiles);
            }
            if (!(this.goPackage == null || this.goPackage.equals(""))) {
                output.writeString(11, this.goPackage);
            }
            if (!(this.javascriptPackage == null || this.javascriptPackage.equals(""))) {
                output.writeString(12, this.javascriptPackage);
            }
            if (this.szlApiVersion != 1) {
                output.writeInt32(14, this.szlApiVersion);
            }
            if (this.ccGenericServices) {
                output.writeBool(16, this.ccGenericServices);
            }
            if (this.javaGenericServices) {
                output.writeBool(17, this.javaGenericServices);
            }
            if (this.pyGenericServices) {
                output.writeBool(18, this.pyGenericServices);
            }
            if (!(this.javaAltApiPackage == null || this.javaAltApiPackage.equals(""))) {
                output.writeString(19, this.javaAltApiPackage);
            }
            if (this.javaUseJavastrings) {
                output.writeBool(21, this.javaUseJavastrings);
            }
            if (this.deprecated) {
                output.writeBool(23, this.deprecated);
            }
            if (!this.ccUtf8Verification) {
                output.writeBool(24, this.ccUtf8Verification);
            }
            if (this.javaEnableDualGenerateMutableApi) {
                output.writeBool(26, this.javaEnableDualGenerateMutableApi);
            }
            if (this.javaStringCheckUtf8) {
                output.writeBool(27, this.javaStringCheckUtf8);
            }
            if (this.javaMutableApi) {
                output.writeBool(28, this.javaMutableApi);
            }
            if (!(this.javaMultipleFilesMutablePackage == null || this.javaMultipleFilesMutablePackage.equals(""))) {
                output.writeString(29, this.javaMultipleFilesMutablePackage);
            }
            if (this.ccEnableArenas) {
                output.writeBool(31, this.ccEnableArenas);
            }
            if (!(this.objcClassPrefix == null || this.objcClassPrefix.equals(""))) {
                output.writeString(36, this.objcClassPrefix);
            }
            if (!(this.csharpNamespace == null || this.csharpNamespace.equals(""))) {
                output.writeString(37, this.csharpNamespace);
            }
            if (!(this.swiftPrefix == null || this.swiftPrefix.equals(""))) {
                output.writeString(39, this.swiftPrefix);
            }
            if (!(this.phpClassPrefix == null || this.phpClassPrefix.equals(""))) {
                output.writeString(40, this.phpClassPrefix);
            }
            if (!(this.phpNamespace == null || this.phpNamespace.equals(""))) {
                output.writeString(41, this.phpNamespace);
            }
            if (this.phpGenericServices) {
                output.writeBool(42, this.phpGenericServices);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        output.writeMessage(999, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!(this.javaPackage == null || this.javaPackage.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.javaPackage);
            }
            if (this.ccApiVersion != 2) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.ccApiVersion);
            }
            if (this.pyApiVersion != 2) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.pyApiVersion);
            }
            if (this.javaApiVersion != 2) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.javaApiVersion);
            }
            if (!this.javaUseJavaproto2) {
                size += CodedOutputByteBufferNano.computeBoolSize(6, this.javaUseJavaproto2);
            }
            if (!this.javaJava5Enums) {
                size += CodedOutputByteBufferNano.computeBoolSize(7, this.javaJava5Enums);
            }
            if (!(this.javaOuterClassname == null || this.javaOuterClassname.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(8, this.javaOuterClassname);
            }
            if (this.optimizeFor != 1) {
                size += CodedOutputByteBufferNano.computeInt32Size(9, this.optimizeFor);
            }
            if (this.javaMultipleFiles) {
                size += CodedOutputByteBufferNano.computeBoolSize(10, this.javaMultipleFiles);
            }
            if (!(this.goPackage == null || this.goPackage.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(11, this.goPackage);
            }
            if (!(this.javascriptPackage == null || this.javascriptPackage.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(12, this.javascriptPackage);
            }
            if (this.szlApiVersion != 1) {
                size += CodedOutputByteBufferNano.computeInt32Size(14, this.szlApiVersion);
            }
            if (this.ccGenericServices) {
                size += CodedOutputByteBufferNano.computeBoolSize(16, this.ccGenericServices);
            }
            if (this.javaGenericServices) {
                size += CodedOutputByteBufferNano.computeBoolSize(17, this.javaGenericServices);
            }
            if (this.pyGenericServices) {
                size += CodedOutputByteBufferNano.computeBoolSize(18, this.pyGenericServices);
            }
            if (!(this.javaAltApiPackage == null || this.javaAltApiPackage.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(19, this.javaAltApiPackage);
            }
            if (this.javaUseJavastrings) {
                size += CodedOutputByteBufferNano.computeBoolSize(21, this.javaUseJavastrings);
            }
            if (this.deprecated) {
                size += CodedOutputByteBufferNano.computeBoolSize(23, this.deprecated);
            }
            if (!this.ccUtf8Verification) {
                size += CodedOutputByteBufferNano.computeBoolSize(24, this.ccUtf8Verification);
            }
            if (this.javaEnableDualGenerateMutableApi) {
                size += CodedOutputByteBufferNano.computeBoolSize(26, this.javaEnableDualGenerateMutableApi);
            }
            if (this.javaStringCheckUtf8) {
                size += CodedOutputByteBufferNano.computeBoolSize(27, this.javaStringCheckUtf8);
            }
            if (this.javaMutableApi) {
                size += CodedOutputByteBufferNano.computeBoolSize(28, this.javaMutableApi);
            }
            if (!(this.javaMultipleFilesMutablePackage == null || this.javaMultipleFilesMutablePackage.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(29, this.javaMultipleFilesMutablePackage);
            }
            if (this.ccEnableArenas) {
                size += CodedOutputByteBufferNano.computeBoolSize(31, this.ccEnableArenas);
            }
            if (!(this.objcClassPrefix == null || this.objcClassPrefix.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(36, this.objcClassPrefix);
            }
            if (!(this.csharpNamespace == null || this.csharpNamespace.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(37, this.csharpNamespace);
            }
            if (!(this.swiftPrefix == null || this.swiftPrefix.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(39, this.swiftPrefix);
            }
            if (!(this.phpClassPrefix == null || this.phpClassPrefix.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(40, this.phpClassPrefix);
            }
            if (!(this.phpNamespace == null || this.phpNamespace.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(41, this.phpNamespace);
            }
            if (this.phpGenericServices) {
                size += CodedOutputByteBufferNano.computeBoolSize(42, this.phpGenericServices);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(999, element);
                    }
                }
            }
            return size;
        }

        public FileOptions mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.javaPackage = input.readString();
                        continue;
                    case 16:
                        this.ccApiVersion = input.readInt32();
                        continue;
                    case 32:
                        this.pyApiVersion = input.readInt32();
                        continue;
                    case 40:
                        this.javaApiVersion = input.readInt32();
                        continue;
                    case 48:
                        this.javaUseJavaproto2 = input.readBool();
                        continue;
                    case 56:
                        this.javaJava5Enums = input.readBool();
                        continue;
                    case 66:
                        this.javaOuterClassname = input.readString();
                        continue;
                    case 72:
                        int initialPos = input.getPosition();
                        try {
                            this.optimizeFor = checkOptimizeModeOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 80:
                        this.javaMultipleFiles = input.readBool();
                        continue;
                    case 90:
                        this.goPackage = input.readString();
                        continue;
                    case 98:
                        this.javascriptPackage = input.readString();
                        continue;
                    case 112:
                        this.szlApiVersion = input.readInt32();
                        continue;
                    case 128:
                        this.ccGenericServices = input.readBool();
                        continue;
                    case 136:
                        this.javaGenericServices = input.readBool();
                        continue;
                    case 144:
                        this.pyGenericServices = input.readBool();
                        continue;
                    case 154:
                        this.javaAltApiPackage = input.readString();
                        continue;
                    case 168:
                        this.javaUseJavastrings = input.readBool();
                        continue;
                    case 184:
                        this.deprecated = input.readBool();
                        continue;
                    case 192:
                        this.ccUtf8Verification = input.readBool();
                        continue;
                    case 208:
                        this.javaEnableDualGenerateMutableApi = input.readBool();
                        continue;
                    case 216:
                        this.javaStringCheckUtf8 = input.readBool();
                        continue;
                    case 224:
                        this.javaMutableApi = input.readBool();
                        continue;
                    case 234:
                        this.javaMultipleFilesMutablePackage = input.readString();
                        continue;
                    case 248:
                        this.ccEnableArenas = input.readBool();
                        continue;
                    case 290:
                        this.objcClassPrefix = input.readString();
                        continue;
                    case 298:
                        this.csharpNamespace = input.readString();
                        continue;
                    case 314:
                        this.swiftPrefix = input.readString();
                        continue;
                    case 322:
                        this.phpClassPrefix = input.readString();
                        continue;
                    case 330:
                        this.phpNamespace = input.readString();
                        continue;
                    case 336:
                        this.phpGenericServices = input.readBool();
                        continue;
                    case 7994:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 7994);
                        if (this.uninterpretedOption == null) {
                            i = 0;
                        } else {
                            i = this.uninterpretedOption.length;
                        }
                        UninterpretedOption[] newArray = new UninterpretedOption[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.uninterpretedOption, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new UninterpretedOption();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new UninterpretedOption();
                        input.readMessage(newArray[i]);
                        this.uninterpretedOption = newArray;
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

        public static FileOptions parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (FileOptions) MessageNano.mergeFrom(new FileOptions(), data);
        }

        public static FileOptions parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new FileOptions().mergeFrom(input);
        }
    }

    public static final class GeneratedCodeInfo extends ExtendableMessageNano<GeneratedCodeInfo> {
        private static volatile GeneratedCodeInfo[] _emptyArray;
        public Annotation[] annotation;

        public static final class Annotation extends ExtendableMessageNano<Annotation> {
            private static volatile Annotation[] _emptyArray;
            public int begin;
            public int end;
            public int[] path;
            public String sourceFile;

            public static Annotation[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new Annotation[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public Annotation() {
                clear();
            }

            public Annotation clear() {
                this.path = WireFormatNano.EMPTY_INT_ARRAY;
                this.sourceFile = "";
                this.begin = 0;
                this.end = 0;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (this.path != null && this.path.length > 0) {
                    int dataSize = 0;
                    for (int element : this.path) {
                        dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                    }
                    output.writeRawVarint32(10);
                    output.writeRawVarint32(dataSize);
                    for (int writeInt32NoTag : this.path) {
                        output.writeInt32NoTag(writeInt32NoTag);
                    }
                }
                if (!(this.sourceFile == null || this.sourceFile.equals(""))) {
                    output.writeString(2, this.sourceFile);
                }
                if (this.begin != 0) {
                    output.writeInt32(3, this.begin);
                }
                if (this.end != 0) {
                    output.writeInt32(4, this.end);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (this.path != null && this.path.length > 0) {
                    int dataSize = 0;
                    for (int element : this.path) {
                        dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                    }
                    size = ((size + dataSize) + 1) + CodedOutputByteBufferNano.computeRawVarint32Size(dataSize);
                }
                if (!(this.sourceFile == null || this.sourceFile.equals(""))) {
                    size += CodedOutputByteBufferNano.computeStringSize(2, this.sourceFile);
                }
                if (this.begin != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(3, this.begin);
                }
                if (this.end != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(4, this.end);
                }
                return size;
            }

            public Annotation mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    int arrayLength;
                    int i;
                    int[] newArray;
                    switch (tag) {
                        case 0:
                            break;
                        case 8:
                            arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 8);
                            i = this.path == null ? 0 : this.path.length;
                            newArray = new int[(i + arrayLength)];
                            if (i != 0) {
                                System.arraycopy(this.path, 0, newArray, 0, i);
                            }
                            while (i < newArray.length - 1) {
                                newArray[i] = input.readInt32();
                                input.readTag();
                                i++;
                            }
                            newArray[i] = input.readInt32();
                            this.path = newArray;
                            continue;
                        case 10:
                            int limit = input.pushLimit(input.readRawVarint32());
                            arrayLength = 0;
                            int startPos = input.getPosition();
                            while (input.getBytesUntilLimit() > 0) {
                                input.readInt32();
                                arrayLength++;
                            }
                            input.rewindToPosition(startPos);
                            i = this.path == null ? 0 : this.path.length;
                            newArray = new int[(i + arrayLength)];
                            if (i != 0) {
                                System.arraycopy(this.path, 0, newArray, 0, i);
                            }
                            while (i < newArray.length) {
                                newArray[i] = input.readInt32();
                                i++;
                            }
                            this.path = newArray;
                            input.popLimit(limit);
                            continue;
                        case 18:
                            this.sourceFile = input.readString();
                            continue;
                        case 24:
                            this.begin = input.readInt32();
                            continue;
                        case 32:
                            this.end = input.readInt32();
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

            public static Annotation parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (Annotation) MessageNano.mergeFrom(new Annotation(), data);
            }

            public static Annotation parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new Annotation().mergeFrom(input);
            }
        }

        public static GeneratedCodeInfo[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new GeneratedCodeInfo[0];
                    }
                }
            }
            return _emptyArray;
        }

        public GeneratedCodeInfo() {
            clear();
        }

        public GeneratedCodeInfo clear() {
            this.annotation = Annotation.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.annotation != null && this.annotation.length > 0) {
                for (Annotation element : this.annotation) {
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.annotation != null && this.annotation.length > 0) {
                for (Annotation element : this.annotation) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                }
            }
            return size;
        }

        public GeneratedCodeInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                        if (this.annotation == null) {
                            i = 0;
                        } else {
                            i = this.annotation.length;
                        }
                        Annotation[] newArray = new Annotation[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.annotation, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new Annotation();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new Annotation();
                        input.readMessage(newArray[i]);
                        this.annotation = newArray;
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

        public static GeneratedCodeInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (GeneratedCodeInfo) MessageNano.mergeFrom(new GeneratedCodeInfo(), data);
        }

        public static GeneratedCodeInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new GeneratedCodeInfo().mergeFrom(input);
        }
    }

    public static final class MessageOptions extends ExtendableMessageNano<MessageOptions> {
        private static volatile MessageOptions[] _emptyArray;
        public boolean deprecated;
        public String[] experimentalJavaBuilderInterface;
        public String[] experimentalJavaInterfaceExtends;
        public String[] experimentalJavaMessageInterface;
        public boolean mapEntry;
        public boolean messageSetWireFormat;
        public boolean noStandardDescriptorAccessor;
        public UninterpretedOption[] uninterpretedOption;

        public static MessageOptions[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new MessageOptions[0];
                    }
                }
            }
            return _emptyArray;
        }

        public MessageOptions() {
            clear();
        }

        public MessageOptions clear() {
            this.experimentalJavaMessageInterface = WireFormatNano.EMPTY_STRING_ARRAY;
            this.experimentalJavaBuilderInterface = WireFormatNano.EMPTY_STRING_ARRAY;
            this.experimentalJavaInterfaceExtends = WireFormatNano.EMPTY_STRING_ARRAY;
            this.messageSetWireFormat = false;
            this.noStandardDescriptorAccessor = false;
            this.deprecated = false;
            this.mapEntry = false;
            this.uninterpretedOption = UninterpretedOption.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.messageSetWireFormat) {
                output.writeBool(1, this.messageSetWireFormat);
            }
            if (this.noStandardDescriptorAccessor) {
                output.writeBool(2, this.noStandardDescriptorAccessor);
            }
            if (this.deprecated) {
                output.writeBool(3, this.deprecated);
            }
            if (this.experimentalJavaMessageInterface != null && this.experimentalJavaMessageInterface.length > 0) {
                for (String element : this.experimentalJavaMessageInterface) {
                    if (element != null) {
                        output.writeString(4, element);
                    }
                }
            }
            if (this.experimentalJavaBuilderInterface != null && this.experimentalJavaBuilderInterface.length > 0) {
                for (String element2 : this.experimentalJavaBuilderInterface) {
                    if (element2 != null) {
                        output.writeString(5, element2);
                    }
                }
            }
            if (this.experimentalJavaInterfaceExtends != null && this.experimentalJavaInterfaceExtends.length > 0) {
                for (String element22 : this.experimentalJavaInterfaceExtends) {
                    if (element22 != null) {
                        output.writeString(6, element22);
                    }
                }
            }
            if (this.mapEntry) {
                output.writeBool(7, this.mapEntry);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element3 : this.uninterpretedOption) {
                    if (element3 != null) {
                        output.writeMessage(999, element3);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int dataCount;
            int dataSize;
            int size = super.computeSerializedSize();
            if (this.messageSetWireFormat) {
                size += CodedOutputByteBufferNano.computeBoolSize(1, this.messageSetWireFormat);
            }
            if (this.noStandardDescriptorAccessor) {
                size += CodedOutputByteBufferNano.computeBoolSize(2, this.noStandardDescriptorAccessor);
            }
            if (this.deprecated) {
                size += CodedOutputByteBufferNano.computeBoolSize(3, this.deprecated);
            }
            if (this.experimentalJavaMessageInterface != null && this.experimentalJavaMessageInterface.length > 0) {
                dataCount = 0;
                dataSize = 0;
                for (String element : this.experimentalJavaMessageInterface) {
                    if (element != null) {
                        dataCount++;
                        dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element);
                    }
                }
                size = (size + dataSize) + (dataCount * 1);
            }
            if (this.experimentalJavaBuilderInterface != null && this.experimentalJavaBuilderInterface.length > 0) {
                dataCount = 0;
                dataSize = 0;
                for (String element2 : this.experimentalJavaBuilderInterface) {
                    if (element2 != null) {
                        dataCount++;
                        dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element2);
                    }
                }
                size = (size + dataSize) + (dataCount * 1);
            }
            if (this.experimentalJavaInterfaceExtends != null && this.experimentalJavaInterfaceExtends.length > 0) {
                dataCount = 0;
                dataSize = 0;
                for (String element22 : this.experimentalJavaInterfaceExtends) {
                    if (element22 != null) {
                        dataCount++;
                        dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element22);
                    }
                }
                size = (size + dataSize) + (dataCount * 1);
            }
            if (this.mapEntry) {
                size += CodedOutputByteBufferNano.computeBoolSize(7, this.mapEntry);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element3 : this.uninterpretedOption) {
                    if (element3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(999, element3);
                    }
                }
            }
            return size;
        }

        public MessageOptions mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                String[] newArray;
                switch (tag) {
                    case 0:
                        break;
                    case 8:
                        this.messageSetWireFormat = input.readBool();
                        continue;
                    case 16:
                        this.noStandardDescriptorAccessor = input.readBool();
                        continue;
                    case 24:
                        this.deprecated = input.readBool();
                        continue;
                    case 34:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        i = this.experimentalJavaMessageInterface == null ? 0 : this.experimentalJavaMessageInterface.length;
                        newArray = new String[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.experimentalJavaMessageInterface, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readString();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readString();
                        this.experimentalJavaMessageInterface = newArray;
                        continue;
                    case 42:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                        i = this.experimentalJavaBuilderInterface == null ? 0 : this.experimentalJavaBuilderInterface.length;
                        newArray = new String[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.experimentalJavaBuilderInterface, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readString();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readString();
                        this.experimentalJavaBuilderInterface = newArray;
                        continue;
                    case 50:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                        i = this.experimentalJavaInterfaceExtends == null ? 0 : this.experimentalJavaInterfaceExtends.length;
                        newArray = new String[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.experimentalJavaInterfaceExtends, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readString();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readString();
                        this.experimentalJavaInterfaceExtends = newArray;
                        continue;
                    case 56:
                        this.mapEntry = input.readBool();
                        continue;
                    case 7994:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 7994);
                        if (this.uninterpretedOption == null) {
                            i = 0;
                        } else {
                            i = this.uninterpretedOption.length;
                        }
                        UninterpretedOption[] newArray2 = new UninterpretedOption[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.uninterpretedOption, 0, newArray2, 0, i);
                        }
                        while (i < newArray2.length - 1) {
                            newArray2[i] = new UninterpretedOption();
                            input.readMessage(newArray2[i]);
                            input.readTag();
                            i++;
                        }
                        newArray2[i] = new UninterpretedOption();
                        input.readMessage(newArray2[i]);
                        this.uninterpretedOption = newArray2;
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

        public static MessageOptions parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (MessageOptions) MessageNano.mergeFrom(new MessageOptions(), data);
        }

        public static MessageOptions parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new MessageOptions().mergeFrom(input);
        }
    }

    public static final class MethodDescriptorProto extends ExtendableMessageNano<MethodDescriptorProto> {
        private static volatile MethodDescriptorProto[] _emptyArray;
        public boolean clientStreaming;
        public String inputType;
        public String name;
        public MethodOptions options;
        public String outputType;
        public boolean serverStreaming;

        public static MethodDescriptorProto[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new MethodDescriptorProto[0];
                    }
                }
            }
            return _emptyArray;
        }

        public MethodDescriptorProto() {
            clear();
        }

        public MethodDescriptorProto clear() {
            this.name = "";
            this.inputType = "";
            this.outputType = "";
            this.options = null;
            this.clientStreaming = false;
            this.serverStreaming = false;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.name == null || this.name.equals(""))) {
                output.writeString(1, this.name);
            }
            if (!(this.inputType == null || this.inputType.equals(""))) {
                output.writeString(2, this.inputType);
            }
            if (!(this.outputType == null || this.outputType.equals(""))) {
                output.writeString(3, this.outputType);
            }
            if (this.options != null) {
                output.writeMessage(4, this.options);
            }
            if (this.clientStreaming) {
                output.writeBool(5, this.clientStreaming);
            }
            if (this.serverStreaming) {
                output.writeBool(6, this.serverStreaming);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!(this.name == null || this.name.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (!(this.inputType == null || this.inputType.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(2, this.inputType);
            }
            if (!(this.outputType == null || this.outputType.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(3, this.outputType);
            }
            if (this.options != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(4, this.options);
            }
            if (this.clientStreaming) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, this.clientStreaming);
            }
            if (this.serverStreaming) {
                return size + CodedOutputByteBufferNano.computeBoolSize(6, this.serverStreaming);
            }
            return size;
        }

        public MethodDescriptorProto mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.name = input.readString();
                        continue;
                    case 18:
                        this.inputType = input.readString();
                        continue;
                    case 26:
                        this.outputType = input.readString();
                        continue;
                    case 34:
                        if (this.options == null) {
                            this.options = new MethodOptions();
                        }
                        input.readMessage(this.options);
                        continue;
                    case 40:
                        this.clientStreaming = input.readBool();
                        continue;
                    case 48:
                        this.serverStreaming = input.readBool();
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

        public static MethodDescriptorProto parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (MethodDescriptorProto) MessageNano.mergeFrom(new MethodDescriptorProto(), data);
        }

        public static MethodDescriptorProto parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new MethodDescriptorProto().mergeFrom(input);
        }
    }

    public static final class MethodOptions extends ExtendableMessageNano<MethodOptions> {
        private static volatile MethodOptions[] _emptyArray;
        public int clientLogging;
        public boolean clientStreaming;
        public double deadline;
        public boolean deprecated;
        public boolean duplicateSuppression;
        public boolean endUserCredsRequested;
        public boolean failFast;
        public boolean goLegacyChannelApi;
        @NanoEnumValue(legacy = false, value = IdempotencyLevel.class)
        public int idempotencyLevel;
        public long legacyClientInitialTokens;
        public String legacyResultType;
        public long legacyServerInitialTokens;
        public String legacyStreamType;
        @NanoEnumValue(legacy = false, value = TokenUnit.class)
        public int legacyTokenUnit;
        @NanoEnumValue(legacy = false, value = LogLevel.class)
        public int logLevel;
        @NanoEnumValue(legacy = false, value = Protocol.class)
        public int protocol;
        @NanoEnumValue(legacy = false, value = Format.class)
        public int requestFormat;
        @NanoEnumValue(legacy = false, value = Format.class)
        public int responseFormat;
        public String securityLabel;
        @NanoEnumValue(legacy = false, value = SecurityLevel.class)
        public int securityLevel;
        public int serverLogging;
        public boolean serverStreaming;
        public String streamType;
        public UninterpretedOption[] uninterpretedOption;

        public interface Format {
            @NanoEnumValue(legacy = false, value = Format.class)
            public static final int UNCOMPRESSED = 0;
            @NanoEnumValue(legacy = false, value = Format.class)
            public static final int ZIPPY_COMPRESSED = 1;
        }

        public interface IdempotencyLevel {
            @NanoEnumValue(legacy = false, value = IdempotencyLevel.class)
            public static final int IDEMPOTENCY_UNKNOWN = 0;
            @NanoEnumValue(legacy = false, value = IdempotencyLevel.class)
            public static final int IDEMPOTENT = 2;
            @NanoEnumValue(legacy = false, value = IdempotencyLevel.class)
            public static final int NO_SIDE_EFFECTS = 1;
        }

        public interface LogLevel {
            @NanoEnumValue(legacy = false, value = LogLevel.class)
            public static final int LOG_HEADER_AND_FILTERED_PAYLOAD = 3;
            @NanoEnumValue(legacy = false, value = LogLevel.class)
            public static final int LOG_HEADER_AND_NON_PRIVATE_PAYLOAD_INTERNAL = 2;
            @NanoEnumValue(legacy = false, value = LogLevel.class)
            public static final int LOG_HEADER_AND_PAYLOAD = 4;
            @NanoEnumValue(legacy = false, value = LogLevel.class)
            public static final int LOG_HEADER_ONLY = 1;
            @NanoEnumValue(legacy = false, value = LogLevel.class)
            public static final int LOG_NONE = 0;
        }

        public interface Protocol {
            @NanoEnumValue(legacy = false, value = Protocol.class)
            public static final int TCP = 0;
            @NanoEnumValue(legacy = false, value = Protocol.class)
            public static final int UDP = 1;
        }

        public interface SecurityLevel {
            @NanoEnumValue(legacy = false, value = SecurityLevel.class)
            public static final int INTEGRITY = 1;
            @NanoEnumValue(legacy = false, value = SecurityLevel.class)
            public static final int NONE = 0;
            @NanoEnumValue(legacy = false, value = SecurityLevel.class)
            public static final int PRIVACY_AND_INTEGRITY = 2;
            @NanoEnumValue(legacy = false, value = SecurityLevel.class)
            public static final int STRONG_PRIVACY_AND_INTEGRITY = 3;
        }

        public interface TokenUnit {
            @NanoEnumValue(legacy = false, value = TokenUnit.class)
            public static final int BYTE = 1;
            @NanoEnumValue(legacy = false, value = TokenUnit.class)
            public static final int MESSAGE = 0;
        }

        @NanoEnumValue(legacy = false, value = Protocol.class)
        public static int checkProtocolOrThrow(int value) {
            if (value >= 0 && value <= 1) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum Protocol");
        }

        @NanoEnumValue(legacy = false, value = Protocol.class)
        public static int[] checkProtocolOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkProtocolOrThrow(value);
            }
            return copy;
        }

        @NanoEnumValue(legacy = false, value = SecurityLevel.class)
        public static int checkSecurityLevelOrThrow(int value) {
            if (value >= 0 && value <= 3) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum SecurityLevel");
        }

        @NanoEnumValue(legacy = false, value = SecurityLevel.class)
        public static int[] checkSecurityLevelOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkSecurityLevelOrThrow(value);
            }
            return copy;
        }

        @NanoEnumValue(legacy = false, value = Format.class)
        public static int checkFormatOrThrow(int value) {
            if (value >= 0 && value <= 1) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum Format");
        }

        @NanoEnumValue(legacy = false, value = Format.class)
        public static int[] checkFormatOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkFormatOrThrow(value);
            }
            return copy;
        }

        @NanoEnumValue(legacy = false, value = LogLevel.class)
        public static int checkLogLevelOrThrow(int value) {
            if (value >= 0 && value <= 4) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum LogLevel");
        }

        @NanoEnumValue(legacy = false, value = LogLevel.class)
        public static int[] checkLogLevelOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkLogLevelOrThrow(value);
            }
            return copy;
        }

        @NanoEnumValue(legacy = false, value = TokenUnit.class)
        public static int checkTokenUnitOrThrow(int value) {
            if (value >= 0 && value <= 1) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum TokenUnit");
        }

        @NanoEnumValue(legacy = false, value = TokenUnit.class)
        public static int[] checkTokenUnitOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkTokenUnitOrThrow(value);
            }
            return copy;
        }

        @NanoEnumValue(legacy = false, value = IdempotencyLevel.class)
        public static int checkIdempotencyLevelOrThrow(int value) {
            if (value >= 0 && value <= 2) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum IdempotencyLevel");
        }

        @NanoEnumValue(legacy = false, value = IdempotencyLevel.class)
        public static int[] checkIdempotencyLevelOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkIdempotencyLevelOrThrow(value);
            }
            return copy;
        }

        public static MethodOptions[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new MethodOptions[0];
                    }
                }
            }
            return _emptyArray;
        }

        public MethodOptions() {
            clear();
        }

        public MethodOptions clear() {
            this.protocol = 0;
            this.deadline = -1.0d;
            this.duplicateSuppression = false;
            this.failFast = false;
            this.endUserCredsRequested = false;
            this.clientLogging = 256;
            this.serverLogging = 256;
            this.securityLevel = 0;
            this.responseFormat = 0;
            this.requestFormat = 0;
            this.streamType = "";
            this.securityLabel = "";
            this.clientStreaming = false;
            this.serverStreaming = false;
            this.legacyStreamType = "";
            this.legacyResultType = "";
            this.goLegacyChannelApi = false;
            this.legacyClientInitialTokens = -1;
            this.legacyServerInitialTokens = -1;
            this.legacyTokenUnit = 1;
            this.logLevel = 2;
            this.deprecated = false;
            this.idempotencyLevel = 0;
            this.uninterpretedOption = UninterpretedOption.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.protocol != 0) {
                output.writeInt32(7, this.protocol);
            }
            if (Double.doubleToLongBits(this.deadline) != Double.doubleToLongBits(-1.0d)) {
                output.writeDouble(8, this.deadline);
            }
            if (this.duplicateSuppression) {
                output.writeBool(9, this.duplicateSuppression);
            }
            if (this.failFast) {
                output.writeBool(10, this.failFast);
            }
            if (this.clientLogging != 256) {
                output.writeSInt32(11, this.clientLogging);
            }
            if (this.serverLogging != 256) {
                output.writeSInt32(12, this.serverLogging);
            }
            if (this.securityLevel != 0) {
                output.writeInt32(13, this.securityLevel);
            }
            if (this.responseFormat != 0) {
                output.writeInt32(15, this.responseFormat);
            }
            if (this.requestFormat != 0) {
                output.writeInt32(17, this.requestFormat);
            }
            if (!(this.streamType == null || this.streamType.equals(""))) {
                output.writeString(18, this.streamType);
            }
            if (!(this.securityLabel == null || this.securityLabel.equals(""))) {
                output.writeString(19, this.securityLabel);
            }
            if (this.clientStreaming) {
                output.writeBool(20, this.clientStreaming);
            }
            if (this.serverStreaming) {
                output.writeBool(21, this.serverStreaming);
            }
            if (!(this.legacyStreamType == null || this.legacyStreamType.equals(""))) {
                output.writeString(22, this.legacyStreamType);
            }
            if (!(this.legacyResultType == null || this.legacyResultType.equals(""))) {
                output.writeString(23, this.legacyResultType);
            }
            if (this.legacyClientInitialTokens != -1) {
                output.writeInt64(24, this.legacyClientInitialTokens);
            }
            if (this.legacyServerInitialTokens != -1) {
                output.writeInt64(25, this.legacyServerInitialTokens);
            }
            if (this.endUserCredsRequested) {
                output.writeBool(26, this.endUserCredsRequested);
            }
            if (this.logLevel != 2) {
                output.writeInt32(27, this.logLevel);
            }
            if (this.legacyTokenUnit != 1) {
                output.writeInt32(28, this.legacyTokenUnit);
            }
            if (this.goLegacyChannelApi) {
                output.writeBool(29, this.goLegacyChannelApi);
            }
            if (this.deprecated) {
                output.writeBool(33, this.deprecated);
            }
            if (this.idempotencyLevel != 0) {
                output.writeInt32(34, this.idempotencyLevel);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        output.writeMessage(999, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.protocol != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, this.protocol);
            }
            if (Double.doubleToLongBits(this.deadline) != Double.doubleToLongBits(-1.0d)) {
                size += CodedOutputByteBufferNano.computeDoubleSize(8, this.deadline);
            }
            if (this.duplicateSuppression) {
                size += CodedOutputByteBufferNano.computeBoolSize(9, this.duplicateSuppression);
            }
            if (this.failFast) {
                size += CodedOutputByteBufferNano.computeBoolSize(10, this.failFast);
            }
            if (this.clientLogging != 256) {
                size += CodedOutputByteBufferNano.computeSInt32Size(11, this.clientLogging);
            }
            if (this.serverLogging != 256) {
                size += CodedOutputByteBufferNano.computeSInt32Size(12, this.serverLogging);
            }
            if (this.securityLevel != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(13, this.securityLevel);
            }
            if (this.responseFormat != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(15, this.responseFormat);
            }
            if (this.requestFormat != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(17, this.requestFormat);
            }
            if (!(this.streamType == null || this.streamType.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(18, this.streamType);
            }
            if (!(this.securityLabel == null || this.securityLabel.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(19, this.securityLabel);
            }
            if (this.clientStreaming) {
                size += CodedOutputByteBufferNano.computeBoolSize(20, this.clientStreaming);
            }
            if (this.serverStreaming) {
                size += CodedOutputByteBufferNano.computeBoolSize(21, this.serverStreaming);
            }
            if (!(this.legacyStreamType == null || this.legacyStreamType.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(22, this.legacyStreamType);
            }
            if (!(this.legacyResultType == null || this.legacyResultType.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(23, this.legacyResultType);
            }
            if (this.legacyClientInitialTokens != -1) {
                size += CodedOutputByteBufferNano.computeInt64Size(24, this.legacyClientInitialTokens);
            }
            if (this.legacyServerInitialTokens != -1) {
                size += CodedOutputByteBufferNano.computeInt64Size(25, this.legacyServerInitialTokens);
            }
            if (this.endUserCredsRequested) {
                size += CodedOutputByteBufferNano.computeBoolSize(26, this.endUserCredsRequested);
            }
            if (this.logLevel != 2) {
                size += CodedOutputByteBufferNano.computeInt32Size(27, this.logLevel);
            }
            if (this.legacyTokenUnit != 1) {
                size += CodedOutputByteBufferNano.computeInt32Size(28, this.legacyTokenUnit);
            }
            if (this.goLegacyChannelApi) {
                size += CodedOutputByteBufferNano.computeBoolSize(29, this.goLegacyChannelApi);
            }
            if (this.deprecated) {
                size += CodedOutputByteBufferNano.computeBoolSize(33, this.deprecated);
            }
            if (this.idempotencyLevel != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(34, this.idempotencyLevel);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(999, element);
                    }
                }
            }
            return size;
        }

        public MethodOptions mergeFrom(CodedInputByteBufferNano input) throws IOException {
            int initialPos;
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 56:
                        initialPos = input.getPosition();
                        try {
                            this.protocol = checkProtocolOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 65:
                        this.deadline = input.readDouble();
                        continue;
                    case 72:
                        this.duplicateSuppression = input.readBool();
                        continue;
                    case 80:
                        this.failFast = input.readBool();
                        continue;
                    case 88:
                        this.clientLogging = input.readSInt32();
                        continue;
                    case 96:
                        this.serverLogging = input.readSInt32();
                        continue;
                    case 104:
                        initialPos = input.getPosition();
                        try {
                            this.securityLevel = checkSecurityLevelOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e2) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 120:
                        initialPos = input.getPosition();
                        try {
                            this.responseFormat = checkFormatOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e3) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 136:
                        initialPos = input.getPosition();
                        try {
                            this.requestFormat = checkFormatOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e4) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 146:
                        this.streamType = input.readString();
                        continue;
                    case 154:
                        this.securityLabel = input.readString();
                        continue;
                    case 160:
                        this.clientStreaming = input.readBool();
                        continue;
                    case 168:
                        this.serverStreaming = input.readBool();
                        continue;
                    case 178:
                        this.legacyStreamType = input.readString();
                        continue;
                    case 186:
                        this.legacyResultType = input.readString();
                        continue;
                    case 192:
                        this.legacyClientInitialTokens = input.readInt64();
                        continue;
                    case 200:
                        this.legacyServerInitialTokens = input.readInt64();
                        continue;
                    case 208:
                        this.endUserCredsRequested = input.readBool();
                        continue;
                    case 216:
                        initialPos = input.getPosition();
                        try {
                            this.logLevel = checkLogLevelOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e5) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 224:
                        initialPos = input.getPosition();
                        try {
                            this.legacyTokenUnit = checkTokenUnitOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e6) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 232:
                        this.goLegacyChannelApi = input.readBool();
                        continue;
                    case 264:
                        this.deprecated = input.readBool();
                        continue;
                    case 272:
                        initialPos = input.getPosition();
                        try {
                            this.idempotencyLevel = checkIdempotencyLevelOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e7) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 7994:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 7994);
                        if (this.uninterpretedOption == null) {
                            i = 0;
                        } else {
                            i = this.uninterpretedOption.length;
                        }
                        UninterpretedOption[] newArray = new UninterpretedOption[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.uninterpretedOption, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new UninterpretedOption();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new UninterpretedOption();
                        input.readMessage(newArray[i]);
                        this.uninterpretedOption = newArray;
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

        public static MethodOptions parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (MethodOptions) MessageNano.mergeFrom(new MethodOptions(), data);
        }

        public static MethodOptions parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new MethodOptions().mergeFrom(input);
        }
    }

    public static final class OneofDescriptorProto extends ExtendableMessageNano<OneofDescriptorProto> {
        private static volatile OneofDescriptorProto[] _emptyArray;
        public String name;
        public OneofOptions options;

        public static OneofDescriptorProto[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new OneofDescriptorProto[0];
                    }
                }
            }
            return _emptyArray;
        }

        public OneofDescriptorProto() {
            clear();
        }

        public OneofDescriptorProto clear() {
            this.name = "";
            this.options = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.name == null || this.name.equals(""))) {
                output.writeString(1, this.name);
            }
            if (this.options != null) {
                output.writeMessage(2, this.options);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!(this.name == null || this.name.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (this.options != null) {
                return size + CodedOutputByteBufferNano.computeMessageSize(2, this.options);
            }
            return size;
        }

        public OneofDescriptorProto mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.name = input.readString();
                        continue;
                    case 18:
                        if (this.options == null) {
                            this.options = new OneofOptions();
                        }
                        input.readMessage(this.options);
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

        public static OneofDescriptorProto parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (OneofDescriptorProto) MessageNano.mergeFrom(new OneofDescriptorProto(), data);
        }

        public static OneofDescriptorProto parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new OneofDescriptorProto().mergeFrom(input);
        }
    }

    public static final class OneofOptions extends ExtendableMessageNano<OneofOptions> {
        private static volatile OneofOptions[] _emptyArray;
        public UninterpretedOption[] uninterpretedOption;

        public static OneofOptions[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new OneofOptions[0];
                    }
                }
            }
            return _emptyArray;
        }

        public OneofOptions() {
            clear();
        }

        public OneofOptions clear() {
            this.uninterpretedOption = UninterpretedOption.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        output.writeMessage(999, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(999, element);
                    }
                }
            }
            return size;
        }

        public OneofOptions mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 7994:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 7994);
                        if (this.uninterpretedOption == null) {
                            i = 0;
                        } else {
                            i = this.uninterpretedOption.length;
                        }
                        UninterpretedOption[] newArray = new UninterpretedOption[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.uninterpretedOption, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new UninterpretedOption();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new UninterpretedOption();
                        input.readMessage(newArray[i]);
                        this.uninterpretedOption = newArray;
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

        public static OneofOptions parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (OneofOptions) MessageNano.mergeFrom(new OneofOptions(), data);
        }

        public static OneofOptions parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new OneofOptions().mergeFrom(input);
        }
    }

    public static final class ServiceDescriptorProto extends ExtendableMessageNano<ServiceDescriptorProto> {
        private static volatile ServiceDescriptorProto[] _emptyArray;
        public MethodDescriptorProto[] method;
        public String name;
        public ServiceOptions options;
        public StreamDescriptorProto[] stream;

        public static ServiceDescriptorProto[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ServiceDescriptorProto[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ServiceDescriptorProto() {
            clear();
        }

        public ServiceDescriptorProto clear() {
            this.name = "";
            this.method = MethodDescriptorProto.emptyArray();
            this.stream = StreamDescriptorProto.emptyArray();
            this.options = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.name == null || this.name.equals(""))) {
                output.writeString(1, this.name);
            }
            if (this.method != null && this.method.length > 0) {
                for (MethodDescriptorProto element : this.method) {
                    if (element != null) {
                        output.writeMessage(2, element);
                    }
                }
            }
            if (this.options != null) {
                output.writeMessage(3, this.options);
            }
            if (this.stream != null && this.stream.length > 0) {
                for (StreamDescriptorProto element2 : this.stream) {
                    if (element2 != null) {
                        output.writeMessage(4, element2);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!(this.name == null || this.name.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (this.method != null && this.method.length > 0) {
                for (MethodDescriptorProto element : this.method) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                    }
                }
            }
            if (this.options != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(3, this.options);
            }
            if (this.stream != null && this.stream.length > 0) {
                for (StreamDescriptorProto element2 : this.stream) {
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element2);
                    }
                }
            }
            return size;
        }

        public ServiceDescriptorProto mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.name = input.readString();
                        continue;
                    case 18:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                        if (this.method == null) {
                            i = 0;
                        } else {
                            i = this.method.length;
                        }
                        MethodDescriptorProto[] newArray = new MethodDescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.method, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new MethodDescriptorProto();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new MethodDescriptorProto();
                        input.readMessage(newArray[i]);
                        this.method = newArray;
                        continue;
                    case 26:
                        if (this.options == null) {
                            this.options = new ServiceOptions();
                        }
                        input.readMessage(this.options);
                        continue;
                    case 34:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        if (this.stream == null) {
                            i = 0;
                        } else {
                            i = this.stream.length;
                        }
                        StreamDescriptorProto[] newArray2 = new StreamDescriptorProto[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.stream, 0, newArray2, 0, i);
                        }
                        while (i < newArray2.length - 1) {
                            newArray2[i] = new StreamDescriptorProto();
                            input.readMessage(newArray2[i]);
                            input.readTag();
                            i++;
                        }
                        newArray2[i] = new StreamDescriptorProto();
                        input.readMessage(newArray2[i]);
                        this.stream = newArray2;
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

        public static ServiceDescriptorProto parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ServiceDescriptorProto) MessageNano.mergeFrom(new ServiceDescriptorProto(), data);
        }

        public static ServiceDescriptorProto parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ServiceDescriptorProto().mergeFrom(input);
        }
    }

    public static final class ServiceOptions extends ExtendableMessageNano<ServiceOptions> {
        private static volatile ServiceOptions[] _emptyArray;
        public boolean deprecated;
        public double failureDetectionDelay;
        public boolean multicastStub;
        public UninterpretedOption[] uninterpretedOption;

        public static ServiceOptions[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ServiceOptions[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ServiceOptions() {
            clear();
        }

        public ServiceOptions clear() {
            this.multicastStub = false;
            this.failureDetectionDelay = -1.0d;
            this.deprecated = false;
            this.uninterpretedOption = UninterpretedOption.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (Double.doubleToLongBits(this.failureDetectionDelay) != Double.doubleToLongBits(-1.0d)) {
                output.writeDouble(16, this.failureDetectionDelay);
            }
            if (this.multicastStub) {
                output.writeBool(20, this.multicastStub);
            }
            if (this.deprecated) {
                output.writeBool(33, this.deprecated);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        output.writeMessage(999, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (Double.doubleToLongBits(this.failureDetectionDelay) != Double.doubleToLongBits(-1.0d)) {
                size += CodedOutputByteBufferNano.computeDoubleSize(16, this.failureDetectionDelay);
            }
            if (this.multicastStub) {
                size += CodedOutputByteBufferNano.computeBoolSize(20, this.multicastStub);
            }
            if (this.deprecated) {
                size += CodedOutputByteBufferNano.computeBoolSize(33, this.deprecated);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(999, element);
                    }
                }
            }
            return size;
        }

        public ServiceOptions mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 129:
                        this.failureDetectionDelay = input.readDouble();
                        continue;
                    case 160:
                        this.multicastStub = input.readBool();
                        continue;
                    case 264:
                        this.deprecated = input.readBool();
                        continue;
                    case 7994:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 7994);
                        if (this.uninterpretedOption == null) {
                            i = 0;
                        } else {
                            i = this.uninterpretedOption.length;
                        }
                        UninterpretedOption[] newArray = new UninterpretedOption[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.uninterpretedOption, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new UninterpretedOption();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new UninterpretedOption();
                        input.readMessage(newArray[i]);
                        this.uninterpretedOption = newArray;
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

        public static ServiceOptions parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ServiceOptions) MessageNano.mergeFrom(new ServiceOptions(), data);
        }

        public static ServiceOptions parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ServiceOptions().mergeFrom(input);
        }
    }

    public static final class SourceCodeInfo extends ExtendableMessageNano<SourceCodeInfo> {
        private static volatile SourceCodeInfo[] _emptyArray;
        public Location[] location;

        public static final class Location extends ExtendableMessageNano<Location> {
            private static volatile Location[] _emptyArray;
            public String leadingComments;
            public String[] leadingDetachedComments;
            public int[] path;
            public int[] span;
            public String trailingComments;

            public static Location[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new Location[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public Location() {
                clear();
            }

            public Location clear() {
                this.path = WireFormatNano.EMPTY_INT_ARRAY;
                this.span = WireFormatNano.EMPTY_INT_ARRAY;
                this.leadingComments = "";
                this.trailingComments = "";
                this.leadingDetachedComments = WireFormatNano.EMPTY_STRING_ARRAY;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int dataSize;
                if (this.path != null && this.path.length > 0) {
                    dataSize = 0;
                    for (int element : this.path) {
                        dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                    }
                    output.writeRawVarint32(10);
                    output.writeRawVarint32(dataSize);
                    for (int writeInt32NoTag : this.path) {
                        output.writeInt32NoTag(writeInt32NoTag);
                    }
                }
                if (this.span != null && this.span.length > 0) {
                    dataSize = 0;
                    for (int element2 : this.span) {
                        dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element2);
                    }
                    output.writeRawVarint32(18);
                    output.writeRawVarint32(dataSize);
                    for (int writeInt32NoTag2 : this.span) {
                        output.writeInt32NoTag(writeInt32NoTag2);
                    }
                }
                if (!(this.leadingComments == null || this.leadingComments.equals(""))) {
                    output.writeString(3, this.leadingComments);
                }
                if (!(this.trailingComments == null || this.trailingComments.equals(""))) {
                    output.writeString(4, this.trailingComments);
                }
                if (this.leadingDetachedComments != null && this.leadingDetachedComments.length > 0) {
                    for (String element3 : this.leadingDetachedComments) {
                        if (element3 != null) {
                            output.writeString(6, element3);
                        }
                    }
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int dataSize;
                int size = super.computeSerializedSize();
                if (this.path != null && this.path.length > 0) {
                    dataSize = 0;
                    for (int element : this.path) {
                        dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                    }
                    size = ((size + dataSize) + 1) + CodedOutputByteBufferNano.computeRawVarint32Size(dataSize);
                }
                if (this.span != null && this.span.length > 0) {
                    dataSize = 0;
                    for (int element2 : this.span) {
                        dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element2);
                    }
                    size = ((size + dataSize) + 1) + CodedOutputByteBufferNano.computeRawVarint32Size(dataSize);
                }
                if (!(this.leadingComments == null || this.leadingComments.equals(""))) {
                    size += CodedOutputByteBufferNano.computeStringSize(3, this.leadingComments);
                }
                if (!(this.trailingComments == null || this.trailingComments.equals(""))) {
                    size += CodedOutputByteBufferNano.computeStringSize(4, this.trailingComments);
                }
                if (this.leadingDetachedComments == null || this.leadingDetachedComments.length <= 0) {
                    return size;
                }
                int dataCount = 0;
                dataSize = 0;
                for (String element3 : this.leadingDetachedComments) {
                    if (element3 != null) {
                        dataCount++;
                        dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element3);
                    }
                }
                return (size + dataSize) + (dataCount * 1);
            }

            public Location mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    int arrayLength;
                    int i;
                    int[] newArray;
                    int limit;
                    int startPos;
                    switch (tag) {
                        case 0:
                            break;
                        case 8:
                            arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 8);
                            i = this.path == null ? 0 : this.path.length;
                            newArray = new int[(i + arrayLength)];
                            if (i != 0) {
                                System.arraycopy(this.path, 0, newArray, 0, i);
                            }
                            while (i < newArray.length - 1) {
                                newArray[i] = input.readInt32();
                                input.readTag();
                                i++;
                            }
                            newArray[i] = input.readInt32();
                            this.path = newArray;
                            continue;
                        case 10:
                            limit = input.pushLimit(input.readRawVarint32());
                            arrayLength = 0;
                            startPos = input.getPosition();
                            while (input.getBytesUntilLimit() > 0) {
                                input.readInt32();
                                arrayLength++;
                            }
                            input.rewindToPosition(startPos);
                            i = this.path == null ? 0 : this.path.length;
                            newArray = new int[(i + arrayLength)];
                            if (i != 0) {
                                System.arraycopy(this.path, 0, newArray, 0, i);
                            }
                            while (i < newArray.length) {
                                newArray[i] = input.readInt32();
                                i++;
                            }
                            this.path = newArray;
                            input.popLimit(limit);
                            continue;
                        case 16:
                            arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 16);
                            i = this.span == null ? 0 : this.span.length;
                            newArray = new int[(i + arrayLength)];
                            if (i != 0) {
                                System.arraycopy(this.span, 0, newArray, 0, i);
                            }
                            while (i < newArray.length - 1) {
                                newArray[i] = input.readInt32();
                                input.readTag();
                                i++;
                            }
                            newArray[i] = input.readInt32();
                            this.span = newArray;
                            continue;
                        case 18:
                            limit = input.pushLimit(input.readRawVarint32());
                            arrayLength = 0;
                            startPos = input.getPosition();
                            while (input.getBytesUntilLimit() > 0) {
                                input.readInt32();
                                arrayLength++;
                            }
                            input.rewindToPosition(startPos);
                            i = this.span == null ? 0 : this.span.length;
                            newArray = new int[(i + arrayLength)];
                            if (i != 0) {
                                System.arraycopy(this.span, 0, newArray, 0, i);
                            }
                            while (i < newArray.length) {
                                newArray[i] = input.readInt32();
                                i++;
                            }
                            this.span = newArray;
                            input.popLimit(limit);
                            continue;
                        case 26:
                            this.leadingComments = input.readString();
                            continue;
                        case 34:
                            this.trailingComments = input.readString();
                            continue;
                        case 50:
                            arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                            i = this.leadingDetachedComments == null ? 0 : this.leadingDetachedComments.length;
                            String[] newArray2 = new String[(i + arrayLength)];
                            if (i != 0) {
                                System.arraycopy(this.leadingDetachedComments, 0, newArray2, 0, i);
                            }
                            while (i < newArray2.length - 1) {
                                newArray2[i] = input.readString();
                                input.readTag();
                                i++;
                            }
                            newArray2[i] = input.readString();
                            this.leadingDetachedComments = newArray2;
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

            public static Location parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (Location) MessageNano.mergeFrom(new Location(), data);
            }

            public static Location parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new Location().mergeFrom(input);
            }
        }

        public static SourceCodeInfo[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new SourceCodeInfo[0];
                    }
                }
            }
            return _emptyArray;
        }

        public SourceCodeInfo() {
            clear();
        }

        public SourceCodeInfo clear() {
            this.location = Location.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.location != null && this.location.length > 0) {
                for (Location element : this.location) {
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.location != null && this.location.length > 0) {
                for (Location element : this.location) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                }
            }
            return size;
        }

        public SourceCodeInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                        if (this.location == null) {
                            i = 0;
                        } else {
                            i = this.location.length;
                        }
                        Location[] newArray = new Location[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.location, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new Location();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new Location();
                        input.readMessage(newArray[i]);
                        this.location = newArray;
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

        public static SourceCodeInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (SourceCodeInfo) MessageNano.mergeFrom(new SourceCodeInfo(), data);
        }

        public static SourceCodeInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new SourceCodeInfo().mergeFrom(input);
        }
    }

    public static final class StreamDescriptorProto extends ExtendableMessageNano<StreamDescriptorProto> {
        private static volatile StreamDescriptorProto[] _emptyArray;
        public String clientMessageType;
        public String name;
        public StreamOptions options;
        public String serverMessageType;

        public static StreamDescriptorProto[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new StreamDescriptorProto[0];
                    }
                }
            }
            return _emptyArray;
        }

        public StreamDescriptorProto() {
            clear();
        }

        public StreamDescriptorProto clear() {
            this.name = "";
            this.clientMessageType = "";
            this.serverMessageType = "";
            this.options = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!(this.name == null || this.name.equals(""))) {
                output.writeString(1, this.name);
            }
            if (!(this.clientMessageType == null || this.clientMessageType.equals(""))) {
                output.writeString(2, this.clientMessageType);
            }
            if (!(this.serverMessageType == null || this.serverMessageType.equals(""))) {
                output.writeString(3, this.serverMessageType);
            }
            if (this.options != null) {
                output.writeMessage(4, this.options);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!(this.name == null || this.name.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (!(this.clientMessageType == null || this.clientMessageType.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(2, this.clientMessageType);
            }
            if (!(this.serverMessageType == null || this.serverMessageType.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(3, this.serverMessageType);
            }
            if (this.options != null) {
                return size + CodedOutputByteBufferNano.computeMessageSize(4, this.options);
            }
            return size;
        }

        public StreamDescriptorProto mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 10:
                        this.name = input.readString();
                        continue;
                    case 18:
                        this.clientMessageType = input.readString();
                        continue;
                    case 26:
                        this.serverMessageType = input.readString();
                        continue;
                    case 34:
                        if (this.options == null) {
                            this.options = new StreamOptions();
                        }
                        input.readMessage(this.options);
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

        public static StreamDescriptorProto parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (StreamDescriptorProto) MessageNano.mergeFrom(new StreamDescriptorProto(), data);
        }

        public static StreamDescriptorProto parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new StreamDescriptorProto().mergeFrom(input);
        }
    }

    public static final class StreamOptions extends ExtendableMessageNano<StreamOptions> {
        private static volatile StreamOptions[] _emptyArray;
        public long clientInitialTokens;
        public int clientLogging;
        public double deadline;
        public boolean deprecated;
        public boolean endUserCredsRequested;
        public boolean failFast;
        @NanoEnumValue(legacy = false, value = LogLevel.class)
        public int logLevel;
        public String securityLabel;
        @NanoEnumValue(legacy = false, value = SecurityLevel.class)
        public int securityLevel;
        public long serverInitialTokens;
        public int serverLogging;
        @NanoEnumValue(legacy = false, value = TokenUnit.class)
        public int tokenUnit;
        public UninterpretedOption[] uninterpretedOption;

        public interface TokenUnit {
            @NanoEnumValue(legacy = false, value = TokenUnit.class)
            public static final int BYTE = 1;
            @NanoEnumValue(legacy = false, value = TokenUnit.class)
            public static final int MESSAGE = 0;
        }

        @NanoEnumValue(legacy = false, value = TokenUnit.class)
        public static int checkTokenUnitOrThrow(int value) {
            if (value >= 0 && value <= 1) {
                return value;
            }
            throw new IllegalArgumentException(value + " is not a valid enum TokenUnit");
        }

        @NanoEnumValue(legacy = false, value = TokenUnit.class)
        public static int[] checkTokenUnitOrThrow(int[] values) {
            int[] copy = (int[]) values.clone();
            for (int value : copy) {
                checkTokenUnitOrThrow(value);
            }
            return copy;
        }

        public static StreamOptions[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new StreamOptions[0];
                    }
                }
            }
            return _emptyArray;
        }

        public StreamOptions() {
            clear();
        }

        public StreamOptions clear() {
            this.clientInitialTokens = -1;
            this.serverInitialTokens = -1;
            this.tokenUnit = 0;
            this.securityLevel = 0;
            this.securityLabel = "";
            this.clientLogging = 256;
            this.serverLogging = 256;
            this.deadline = -1.0d;
            this.failFast = false;
            this.endUserCredsRequested = false;
            this.logLevel = 2;
            this.deprecated = false;
            this.uninterpretedOption = UninterpretedOption.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.clientInitialTokens != -1) {
                output.writeInt64(1, this.clientInitialTokens);
            }
            if (this.serverInitialTokens != -1) {
                output.writeInt64(2, this.serverInitialTokens);
            }
            if (this.tokenUnit != 0) {
                output.writeInt32(3, this.tokenUnit);
            }
            if (this.securityLevel != 0) {
                output.writeInt32(4, this.securityLevel);
            }
            if (!(this.securityLabel == null || this.securityLabel.equals(""))) {
                output.writeString(5, this.securityLabel);
            }
            if (this.clientLogging != 256) {
                output.writeInt32(6, this.clientLogging);
            }
            if (this.serverLogging != 256) {
                output.writeInt32(7, this.serverLogging);
            }
            if (Double.doubleToLongBits(this.deadline) != Double.doubleToLongBits(-1.0d)) {
                output.writeDouble(8, this.deadline);
            }
            if (this.failFast) {
                output.writeBool(9, this.failFast);
            }
            if (this.endUserCredsRequested) {
                output.writeBool(10, this.endUserCredsRequested);
            }
            if (this.logLevel != 2) {
                output.writeInt32(11, this.logLevel);
            }
            if (this.deprecated) {
                output.writeBool(33, this.deprecated);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        output.writeMessage(999, element);
                    }
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.clientInitialTokens != -1) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.clientInitialTokens);
            }
            if (this.serverInitialTokens != -1) {
                size += CodedOutputByteBufferNano.computeInt64Size(2, this.serverInitialTokens);
            }
            if (this.tokenUnit != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.tokenUnit);
            }
            if (this.securityLevel != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.securityLevel);
            }
            if (!(this.securityLabel == null || this.securityLabel.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(5, this.securityLabel);
            }
            if (this.clientLogging != 256) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, this.clientLogging);
            }
            if (this.serverLogging != 256) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, this.serverLogging);
            }
            if (Double.doubleToLongBits(this.deadline) != Double.doubleToLongBits(-1.0d)) {
                size += CodedOutputByteBufferNano.computeDoubleSize(8, this.deadline);
            }
            if (this.failFast) {
                size += CodedOutputByteBufferNano.computeBoolSize(9, this.failFast);
            }
            if (this.endUserCredsRequested) {
                size += CodedOutputByteBufferNano.computeBoolSize(10, this.endUserCredsRequested);
            }
            if (this.logLevel != 2) {
                size += CodedOutputByteBufferNano.computeInt32Size(11, this.logLevel);
            }
            if (this.deprecated) {
                size += CodedOutputByteBufferNano.computeBoolSize(33, this.deprecated);
            }
            if (this.uninterpretedOption != null && this.uninterpretedOption.length > 0) {
                for (UninterpretedOption element : this.uninterpretedOption) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(999, element);
                    }
                }
            }
            return size;
        }

        public StreamOptions mergeFrom(CodedInputByteBufferNano input) throws IOException {
            int initialPos;
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 8:
                        this.clientInitialTokens = input.readInt64();
                        continue;
                    case 16:
                        this.serverInitialTokens = input.readInt64();
                        continue;
                    case 24:
                        initialPos = input.getPosition();
                        try {
                            this.tokenUnit = checkTokenUnitOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 32:
                        initialPos = input.getPosition();
                        try {
                            this.securityLevel = MethodOptions.checkSecurityLevelOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e2) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 42:
                        this.securityLabel = input.readString();
                        continue;
                    case 48:
                        this.clientLogging = input.readInt32();
                        continue;
                    case 56:
                        this.serverLogging = input.readInt32();
                        continue;
                    case 65:
                        this.deadline = input.readDouble();
                        continue;
                    case 72:
                        this.failFast = input.readBool();
                        continue;
                    case 80:
                        this.endUserCredsRequested = input.readBool();
                        continue;
                    case 88:
                        initialPos = input.getPosition();
                        try {
                            this.logLevel = MethodOptions.checkLogLevelOrThrow(input.readInt32());
                            continue;
                        } catch (IllegalArgumentException e3) {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            break;
                        }
                    case 264:
                        this.deprecated = input.readBool();
                        continue;
                    case 7994:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 7994);
                        if (this.uninterpretedOption == null) {
                            i = 0;
                        } else {
                            i = this.uninterpretedOption.length;
                        }
                        UninterpretedOption[] newArray = new UninterpretedOption[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.uninterpretedOption, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new UninterpretedOption();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new UninterpretedOption();
                        input.readMessage(newArray[i]);
                        this.uninterpretedOption = newArray;
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

        public static StreamOptions parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (StreamOptions) MessageNano.mergeFrom(new StreamOptions(), data);
        }

        public static StreamOptions parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new StreamOptions().mergeFrom(input);
        }
    }

    public static final class UninterpretedOption extends ExtendableMessageNano<UninterpretedOption> {
        private static volatile UninterpretedOption[] _emptyArray;
        public String aggregateValue;
        public double doubleValue;
        public String identifierValue;
        public NamePart[] name;
        public long negativeIntValue;
        public long positiveIntValue;
        public byte[] stringValue;

        public static final class NamePart extends ExtendableMessageNano<NamePart> {
            private static volatile NamePart[] _emptyArray;
            public boolean isExtension;
            public String namePart;

            public static NamePart[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new NamePart[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public NamePart() {
                clear();
            }

            public NamePart clear() {
                this.namePart = "";
                this.isExtension = false;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                output.writeString(1, this.namePart);
                output.writeBool(2, this.isExtension);
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                return (super.computeSerializedSize() + CodedOutputByteBufferNano.computeStringSize(1, this.namePart)) + CodedOutputByteBufferNano.computeBoolSize(2, this.isExtension);
            }

            public NamePart mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            break;
                        case 10:
                            this.namePart = input.readString();
                            continue;
                        case 16:
                            this.isExtension = input.readBool();
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

            public static NamePart parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (NamePart) MessageNano.mergeFrom(new NamePart(), data);
            }

            public static NamePart parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new NamePart().mergeFrom(input);
            }
        }

        public static UninterpretedOption[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new UninterpretedOption[0];
                    }
                }
            }
            return _emptyArray;
        }

        public UninterpretedOption() {
            clear();
        }

        public UninterpretedOption clear() {
            this.name = NamePart.emptyArray();
            this.identifierValue = "";
            this.positiveIntValue = 0;
            this.negativeIntValue = 0;
            this.doubleValue = 0.0d;
            this.stringValue = WireFormatNano.EMPTY_BYTES;
            this.aggregateValue = "";
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.name != null && this.name.length > 0) {
                for (NamePart element : this.name) {
                    if (element != null) {
                        output.writeMessage(2, element);
                    }
                }
            }
            if (!(this.identifierValue == null || this.identifierValue.equals(""))) {
                output.writeString(3, this.identifierValue);
            }
            if (this.positiveIntValue != 0) {
                output.writeUInt64(4, this.positiveIntValue);
            }
            if (this.negativeIntValue != 0) {
                output.writeInt64(5, this.negativeIntValue);
            }
            if (Double.doubleToLongBits(this.doubleValue) != Double.doubleToLongBits(0.0d)) {
                output.writeDouble(6, this.doubleValue);
            }
            if (!Arrays.equals(this.stringValue, WireFormatNano.EMPTY_BYTES)) {
                output.writeBytes(7, this.stringValue);
            }
            if (!(this.aggregateValue == null || this.aggregateValue.equals(""))) {
                output.writeString(8, this.aggregateValue);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.name != null && this.name.length > 0) {
                for (NamePart element : this.name) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element);
                    }
                }
            }
            if (!(this.identifierValue == null || this.identifierValue.equals(""))) {
                size += CodedOutputByteBufferNano.computeStringSize(3, this.identifierValue);
            }
            if (this.positiveIntValue != 0) {
                size += CodedOutputByteBufferNano.computeUInt64Size(4, this.positiveIntValue);
            }
            if (this.negativeIntValue != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, this.negativeIntValue);
            }
            if (Double.doubleToLongBits(this.doubleValue) != Double.doubleToLongBits(0.0d)) {
                size += CodedOutputByteBufferNano.computeDoubleSize(6, this.doubleValue);
            }
            if (!Arrays.equals(this.stringValue, WireFormatNano.EMPTY_BYTES)) {
                size += CodedOutputByteBufferNano.computeBytesSize(7, this.stringValue);
            }
            if (this.aggregateValue == null || this.aggregateValue.equals("")) {
                return size;
            }
            return size + CodedOutputByteBufferNano.computeStringSize(8, this.aggregateValue);
        }

        public UninterpretedOption mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        break;
                    case 18:
                        int i;
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                        if (this.name == null) {
                            i = 0;
                        } else {
                            i = this.name.length;
                        }
                        NamePart[] newArray = new NamePart[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.name, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new NamePart();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new NamePart();
                        input.readMessage(newArray[i]);
                        this.name = newArray;
                        continue;
                    case 26:
                        this.identifierValue = input.readString();
                        continue;
                    case 32:
                        this.positiveIntValue = input.readUInt64();
                        continue;
                    case 40:
                        this.negativeIntValue = input.readInt64();
                        continue;
                    case 49:
                        this.doubleValue = input.readDouble();
                        continue;
                    case 58:
                        this.stringValue = input.readBytes();
                        continue;
                    case 66:
                        this.aggregateValue = input.readString();
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

        public static UninterpretedOption parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (UninterpretedOption) MessageNano.mergeFrom(new UninterpretedOption(), data);
        }

        public static UninterpretedOption parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new UninterpretedOption().mergeFrom(input);
        }
    }

    private DescriptorProtos() {
    }
}
