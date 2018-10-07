package com.lge.camera.file;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExifTagBase {
    protected static final long LONG_MAX = 2147483647L;
    protected static final long LONG_MIN = -2147483648L;
    static final int SIZE_UNDEFINED = 0;
    protected static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy:MM:dd kk:mm:ss", Locale.US);
    public static final short TYPE_ASCII = (short) 2;
    public static final short TYPE_LONG = (short) 9;
    public static final short TYPE_RATIONAL = (short) 10;
    protected static final int[] TYPE_TO_SIZE_MAP = new int[11];
    public static final short TYPE_UNDEFINED = (short) 7;
    public static final short TYPE_UNSIGNED_BYTE = (short) 1;
    public static final short TYPE_UNSIGNED_LONG = (short) 4;
    public static final short TYPE_UNSIGNED_RATIONAL = (short) 5;
    public static final short TYPE_UNSIGNED_SHORT = (short) 3;
    protected static final long UNSIGNED_LONG_MAX = 4294967295L;
    protected static final int UNSIGNED_SHORT_MAX = 65535;
    protected static final Charset US_ASCII = Charset.forName("US-ASCII");
    protected int mComponentCountActual;
    protected final short mDataType;
    protected boolean mHasDefinedDefaultComponentCount;
    protected int mIfd;
    protected int mOffset;
    protected final short mTagId;
    protected Object mValue = null;

    static {
        TYPE_TO_SIZE_MAP[1] = 1;
        TYPE_TO_SIZE_MAP[2] = 1;
        TYPE_TO_SIZE_MAP[3] = 2;
        TYPE_TO_SIZE_MAP[4] = 4;
        TYPE_TO_SIZE_MAP[5] = 8;
        TYPE_TO_SIZE_MAP[7] = 1;
        TYPE_TO_SIZE_MAP[9] = 4;
        TYPE_TO_SIZE_MAP[10] = 8;
    }

    public static boolean isValidIfd(int ifdId) {
        return ifdId == 0 || ifdId == 1 || ifdId == 2 || ifdId == 3 || ifdId == 4;
    }

    public static boolean isValidType(short type) {
        return type == (short) 1 || type == (short) 2 || type == (short) 3 || type == (short) 4 || type == (short) 5 || type == (short) 7 || type == (short) 9 || type == (short) 10;
    }

    ExifTagBase(short tagId, short type, int componentCount, int ifd, boolean hasDefinedComponentCount) {
        this.mTagId = tagId;
        this.mDataType = type;
        this.mComponentCountActual = componentCount;
        this.mHasDefinedDefaultComponentCount = hasDefinedComponentCount;
        this.mIfd = ifd;
    }

    protected boolean checkBadComponentCount(int count) {
        if (!this.mHasDefinedDefaultComponentCount || this.mComponentCountActual == count) {
            return false;
        }
        return true;
    }

    protected static String convertTypeToString(short type) {
        switch (type) {
            case (short) 1:
                return "UNSIGNED_BYTE";
            case (short) 2:
                return "ASCII";
            case (short) 3:
                return "UNSIGNED_SHORT";
            case (short) 4:
                return "UNSIGNED_LONG";
            case (short) 5:
                return "UNSIGNED_RATIONAL";
            case (short) 7:
                return "UNDEFINED";
            case (short) 9:
                return "LONG";
            case (short) 10:
                return "RATIONAL";
            default:
                return "";
        }
    }

    protected int getOffset() {
        return this.mOffset;
    }

    protected void setOffset(int offset) {
        this.mOffset = offset;
    }

    protected void setHasDefinedCount(boolean d) {
        this.mHasDefinedDefaultComponentCount = d;
    }

    protected boolean hasDefinedCount() {
        return this.mHasDefinedDefaultComponentCount;
    }

    protected boolean checkOverflowForUnsignedShort(int[] value) {
        for (int v : value) {
            if (v > 65535 || v < 0) {
                return true;
            }
        }
        return false;
    }

    protected boolean checkOverflowForUnsignedLong(long[] value) {
        for (long v : value) {
            if (v < 0 || v > 4294967295L) {
                return true;
            }
        }
        return false;
    }

    protected boolean checkOverflowForUnsignedLong(int[] value) {
        for (int v : value) {
            if (v < 0) {
                return true;
            }
        }
        return false;
    }

    protected boolean checkOverflowForUnsignedRational(Rational[] value) {
        for (Rational v : value) {
            if (v.getNumerator() < 0 || v.getDenominator() < 0 || v.getNumerator() > 4294967295L || v.getDenominator() > 4294967295L) {
                return true;
            }
        }
        return false;
    }

    protected boolean checkOverflowForRational(Rational[] value) {
        for (Rational v : value) {
            if (v.getNumerator() < LONG_MIN || v.getDenominator() < LONG_MIN || v.getNumerator() > LONG_MAX || v.getDenominator() > LONG_MAX) {
                return true;
            }
        }
        return false;
    }
}
