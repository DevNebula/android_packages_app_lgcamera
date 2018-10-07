package com.google.protobuf.nano;

public final class FieldArray implements Cloneable {
    private static final FieldData DELETED = new FieldData();
    private FieldData[] mData;
    private int[] mFieldNumbers;
    private boolean mGarbage;
    private int mSize;

    FieldArray() {
        this(10);
    }

    FieldArray(int initialCapacity) {
        this.mGarbage = false;
        initialCapacity = idealIntArraySize(initialCapacity);
        this.mFieldNumbers = new int[initialCapacity];
        this.mData = new FieldData[initialCapacity];
        this.mSize = 0;
    }

    FieldData get(int fieldNumber) {
        int i = binarySearch(fieldNumber);
        if (i < 0 || this.mData[i] == DELETED) {
            return null;
        }
        return this.mData[i];
    }

    public final int getFieldNumbersSize() {
        return this.mFieldNumbers.length;
    }

    public final int getFieldNumberAt(int index) {
        return this.mFieldNumbers[index];
    }

    void remove(int fieldNumber) {
        int i = binarySearch(fieldNumber);
        if (i >= 0 && this.mData[i] != DELETED) {
            this.mData[i] = DELETED;
            this.mGarbage = true;
        }
    }

    /* renamed from: gc */
    private void m23gc() {
        int n = this.mSize;
        int o = 0;
        int[] keys = this.mFieldNumbers;
        FieldData[] values = this.mData;
        for (int i = 0; i < n; i++) {
            FieldData val = values[i];
            if (val != DELETED) {
                if (i != o) {
                    keys[o] = keys[i];
                    values[o] = val;
                    values[i] = null;
                }
                o++;
            }
        }
        this.mGarbage = false;
        this.mSize = o;
    }

    void put(int fieldNumber, FieldData data) {
        int i = binarySearch(fieldNumber);
        if (i >= 0) {
            this.mData[i] = data;
            return;
        }
        i ^= -1;
        if (i >= this.mSize || this.mData[i] != DELETED) {
            if (this.mGarbage && this.mSize >= this.mFieldNumbers.length) {
                m23gc();
                i = binarySearch(fieldNumber) ^ -1;
            }
            if (this.mSize >= this.mFieldNumbers.length) {
                int n = idealIntArraySize(this.mSize + 1);
                int[] nkeys = new int[n];
                FieldData[] nvalues = new FieldData[n];
                System.arraycopy(this.mFieldNumbers, 0, nkeys, 0, this.mFieldNumbers.length);
                System.arraycopy(this.mData, 0, nvalues, 0, this.mData.length);
                this.mFieldNumbers = nkeys;
                this.mData = nvalues;
            }
            if (this.mSize - i != 0) {
                System.arraycopy(this.mFieldNumbers, i, this.mFieldNumbers, i + 1, this.mSize - i);
                System.arraycopy(this.mData, i, this.mData, i + 1, this.mSize - i);
            }
            this.mFieldNumbers[i] = fieldNumber;
            this.mData[i] = data;
            this.mSize++;
            return;
        }
        this.mFieldNumbers[i] = fieldNumber;
        this.mData[i] = data;
    }

    int size() {
        if (this.mGarbage) {
            m23gc();
        }
        return this.mSize;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    FieldData dataAt(int index) {
        if (this.mGarbage) {
            m23gc();
        }
        return this.mData[index];
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof FieldArray)) {
            return false;
        }
        FieldArray other = (FieldArray) o;
        if (size() != other.size()) {
            return false;
        }
        if (arrayEquals(this.mFieldNumbers, other.mFieldNumbers, this.mSize) && arrayEquals(this.mData, other.mData, this.mSize)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (this.mGarbage) {
            m23gc();
        }
        int result = 17;
        for (int i = 0; i < this.mSize; i++) {
            result = (((result * 31) + this.mFieldNumbers[i]) * 31) + this.mData[i].hashCode();
        }
        return result;
    }

    private int idealIntArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }

    private int idealByteArraySize(int need) {
        for (int i = 4; i < 32; i++) {
            if (need <= (1 << i) - 12) {
                return (1 << i) - 12;
            }
        }
        return need;
    }

    private int binarySearch(int value) {
        int lo = 0;
        int hi = this.mSize - 1;
        while (lo <= hi) {
            int i = (lo + hi) >>> 1;
            int midVal = this.mFieldNumbers[i];
            if (midVal < value) {
                lo = i + 1;
            } else if (midVal <= value) {
                return i;
            } else {
                hi = i - 1;
            }
        }
        return lo ^ -1;
    }

    private boolean arrayEquals(int[] a, int[] b, int size) {
        for (int i = 0; i < size; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean arrayEquals(FieldData[] a, FieldData[] b, int size) {
        for (int i = 0; i < size; i++) {
            if (!a[i].equals(b[i])) {
                return false;
            }
        }
        return true;
    }

    public final FieldArray clone() {
        int size = size();
        FieldArray clone = new FieldArray(size);
        System.arraycopy(this.mFieldNumbers, 0, clone.mFieldNumbers, 0, size);
        for (int i = 0; i < size; i++) {
            if (this.mData[i] != null) {
                clone.mData[i] = this.mData[i].clone();
            }
        }
        clone.mSize = size;
        return clone;
    }
}
