package com.lge.camera.file;

import android.util.SparseIntArray;
import java.io.IOException;

public interface ExifBridge {
    public static final int CHECKALLOW_TAG_EXIF_IFD = 0;
    public static final int CHECKALLOW_TAG_GPS_IFD = 1;
    public static final int CHECKALLOW_TAG_INTEROPERABILITY_IFD = 2;
    public static final int CHECKALLOW_TAG_JPEG_INTERCHANGE_FORMAT = 3;
    public static final int CHECKALLOW_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = 4;
    public static final int CHECKALLOW_TAG_STRIP_BYTE_COUNTS = 6;
    public static final int CHECKALLOW_TAG_STRIP_OFFSETS = 5;

    Object buildUninitializedTag(int i);

    int calculateAllOffset(Object obj);

    boolean checkAllowedIfd(short s, int i, int i2);

    void createRequiredIfdAndTag(Object obj) throws IOException;

    SparseIntArray getTagInfo();

    boolean isExifOffsetTag(short s);
}
