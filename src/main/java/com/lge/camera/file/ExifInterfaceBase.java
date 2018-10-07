package com.lge.camera.file;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.support.p000v4.internal.view.SupportMenu;
import android.util.SparseIntArray;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ExifInterfaceBase implements ExifBridge {
    public static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    public static final int DEFINITION_NULL = 0;
    protected static final int[] IFD_ARRAY = new int[]{0, 1, 2, 3, 4};
    public static final int IFD_NULL = -1;
    public static final int TAG_APERTURE_VALUE = defineTag(2, (short) -28158);
    public static final int TAG_ARTIST = defineTag(0, (short) 315);
    public static final int TAG_BITS_PER_SAMPLE = defineTag(0, (short) 258);
    public static final int TAG_BRIGHTNESS_VALUE = defineTag(2, (short) -28157);
    public static final int TAG_CFA_PATTERN = defineTag(2, (short) -23806);
    public static final int TAG_COLOR_SPACE = defineTag(2, (short) -24575);
    public static final int TAG_COMPONENTS_CONFIGURATION = defineTag(2, (short) -28415);
    public static final int TAG_COMPRESSED_BITS_PER_PIXEL = defineTag(2, (short) -28414);
    public static final int TAG_COMPRESSION = defineTag(0, (short) 259);
    public static final int TAG_COMPRESSION_IFD1 = defineTag(1, (short) 259);
    public static final int TAG_CONTRAST = defineTag(2, (short) -23544);
    public static final int TAG_COPYRIGHT = defineTag(0, (short) -32104);
    public static final int TAG_CUSTOM_RENDERED = defineTag(2, (short) -23551);
    public static final int TAG_DATE_TIME = defineTag(0, (short) 306);
    public static final int TAG_DATE_TIME_DIGITIZED = defineTag(2, (short) -28668);
    public static final int TAG_DATE_TIME_ORIGINAL = defineTag(2, (short) -28669);
    public static final int TAG_DEVICE_SETTING_DESCRIPTION = defineTag(2, (short) -23541);
    public static final int TAG_DIGITAL_ZOOM_RATIO = defineTag(2, (short) -23548);
    public static final int TAG_EXIF_IFD = defineTag(0, (short) -30871);
    public static final int TAG_EXIF_VERSION = defineTag(2, (short) -28672);
    public static final int TAG_EXPOSURE_BIAS_VALUE = defineTag(2, (short) -28156);
    public static final int TAG_EXPOSURE_INDEX = defineTag(2, (short) -24043);
    public static final int TAG_EXPOSURE_MODE = defineTag(2, (short) -23550);
    public static final int TAG_EXPOSURE_PROGRAM = defineTag(2, (short) -30686);
    public static final int TAG_EXPOSURE_TIME = defineTag(2, (short) -32102);
    public static final int TAG_FILE_SOURCE = defineTag(2, (short) -23808);
    public static final int TAG_FLASH = defineTag(2, (short) -28151);
    public static final int TAG_FLASHPIX_VERSION = defineTag(2, (short) -24576);
    public static final int TAG_FLASH_ENERGY = defineTag(2, (short) -24053);
    public static final int TAG_FOCAL_LENGTH = defineTag(2, (short) -28150);
    public static final int TAG_FOCAL_LENGTH_IN_35_MM_FILE = defineTag(2, (short) -23547);
    public static final int TAG_FOCAL_PLANE_RESOLUTION_UNIT = defineTag(2, (short) -24048);
    public static final int TAG_FOCAL_PLANE_X_RESOLUTION = defineTag(2, (short) -24050);
    public static final int TAG_FOCAL_PLANE_Y_RESOLUTION = defineTag(2, (short) -24049);
    public static final int TAG_F_NUMBER = defineTag(2, (short) -32099);
    public static final int TAG_GAIN_CONTROL = defineTag(2, (short) -23545);
    public static final int TAG_GPS_ALTITUDE = defineTag(4, (short) 6);
    public static final int TAG_GPS_ALTITUDE_REF = defineTag(4, (short) 5);
    public static final int TAG_GPS_AREA_INFORMATION = defineTag(4, (short) 28);
    public static final int TAG_GPS_DATE_STAMP = defineTag(4, (short) 29);
    public static final int TAG_GPS_DEST_BEARING = defineTag(4, (short) 24);
    public static final int TAG_GPS_DEST_BEARING_REF = defineTag(4, (short) 23);
    public static final int TAG_GPS_DEST_DISTANCE = defineTag(4, (short) 26);
    public static final int TAG_GPS_DEST_DISTANCE_REF = defineTag(4, (short) 25);
    public static final int TAG_GPS_DEST_LATITUDE = defineTag(4, (short) 20);
    public static final int TAG_GPS_DEST_LATITUDE_REF = defineTag(4, (short) 19);
    public static final int TAG_GPS_DEST_LONGITUDE = defineTag(4, (short) 22);
    public static final int TAG_GPS_DEST_LONGITUDE_REF = defineTag(4, (short) 21);
    public static final int TAG_GPS_DIFFERENTIAL = defineTag(4, (short) 30);
    public static final int TAG_GPS_DOP = defineTag(4, (short) 11);
    public static final int TAG_GPS_IFD = defineTag(0, (short) -30683);
    public static final int TAG_GPS_IMG_DIRECTION = defineTag(4, (short) 17);
    public static final int TAG_GPS_IMG_DIRECTION_REF = defineTag(4, (short) 16);
    public static final int TAG_GPS_LATITUDE = defineTag(4, (short) 2);
    public static final int TAG_GPS_LATITUDE_REF = defineTag(4, (short) 1);
    public static final int TAG_GPS_LONGITUDE = defineTag(4, (short) 4);
    public static final int TAG_GPS_LONGITUDE_REF = defineTag(4, (short) 3);
    public static final int TAG_GPS_MAP_DATUM = defineTag(4, (short) 18);
    public static final int TAG_GPS_MEASURE_MODE = defineTag(4, (short) 10);
    public static final int TAG_GPS_PROCESSING_METHOD = defineTag(4, (short) 27);
    public static final int TAG_GPS_SATTELLITES = defineTag(4, (short) 8);
    public static final int TAG_GPS_SPEED = defineTag(4, (short) 13);
    public static final int TAG_GPS_SPEED_REF = defineTag(4, (short) 12);
    public static final int TAG_GPS_STATUS = defineTag(4, (short) 9);
    public static final int TAG_GPS_TIME_STAMP = defineTag(4, (short) 7);
    public static final int TAG_GPS_TRACK = defineTag(4, (short) 15);
    public static final int TAG_GPS_TRACK_REF = defineTag(4, (short) 14);
    public static final int TAG_GPS_VERSION_ID = defineTag(4, (short) 0);
    public static final int TAG_IMAGE_DESCRIPTION = defineTag(0, (short) 270);
    public static final int TAG_IMAGE_LENGTH = defineTag(0, (short) 257);
    public static final int TAG_IMAGE_UNIQUE_ID = defineTag(2, (short) -23520);
    public static final int TAG_IMAGE_WIDTH = defineTag(0, (short) 256);
    public static final int TAG_INTEROPERABILITY_IFD = defineTag(2, (short) -24571);
    public static final int TAG_INTEROPERABILITY_INDEX = defineTag(3, (short) 1);
    public static final int TAG_INTEROPERABILITY_VERSION = defineTag(3, (short) 2);
    public static final int TAG_ISO_SPEED_RATINGS = defineTag(2, (short) -30681);
    public static final int TAG_JPEG_INTERCHANGE_FORMAT = defineTag(1, (short) 513);
    public static final int TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = defineTag(1, (short) 514);
    public static final int TAG_LIGHT_SOURCE = defineTag(2, (short) -28152);
    public static final int TAG_MAKE = defineTag(0, (short) 271);
    public static final int TAG_MAKER_NOTE = defineTag(2, (short) -28036);
    public static final int TAG_MAX_APERTURE_VALUE = defineTag(2, (short) -28155);
    public static final int TAG_METERING_MODE = defineTag(2, (short) -28153);
    public static final int TAG_MODEL = defineTag(0, (short) 272);
    public static final int TAG_NULL = -1;
    public static final int TAG_OECF = defineTag(2, (short) -30680);
    public static final int TAG_ORIENTATION = defineTag(0, (short) 274);
    public static final int TAG_ORIENTATION_IFD1 = defineTag(1, (short) 274);
    public static final int TAG_PHOTOMETRIC_INTERPRETATION = defineTag(0, (short) 262);
    public static final int TAG_PIXEL_X_DIMENSION = defineTag(2, (short) -24574);
    public static final int TAG_PIXEL_Y_DIMENSION = defineTag(2, (short) -24573);
    public static final int TAG_PLANAR_CONFIGURATION = defineTag(0, (short) 284);
    public static final int TAG_PRIMARY_CHROMATICITIES = defineTag(0, (short) 319);
    public static final int TAG_REFERENCE_BLACK_WHITE = defineTag(0, (short) 532);
    public static final int TAG_RELATED_SOUND_FILE = defineTag(2, (short) -24572);
    public static final int TAG_RESOLUTION_UNIT = defineTag(0, (short) 296);
    public static final int TAG_RESOLUTION_UNIT_IFD1 = defineTag(1, (short) 296);
    public static final int TAG_ROWS_PER_STRIP = defineTag(0, (short) 278);
    public static final int TAG_SAMPLES_PER_PIXEL = defineTag(0, (short) 277);
    public static final int TAG_SATURATION = defineTag(2, (short) -23543);
    public static final int TAG_SCENE_CAPTURE_TYPE = defineTag(2, (short) -23546);
    public static final int TAG_SCENE_TYPE = defineTag(2, (short) -23807);
    public static final int TAG_SENSING_METHOD = defineTag(2, (short) -24041);
    public static final int TAG_SHARPNESS = defineTag(2, (short) -23542);
    public static final int TAG_SHUTTER_SPEED_VALUE = defineTag(2, (short) -28159);
    private static final short TAG_SIZE = (short) 12;
    public static final int TAG_SOFTWARE = defineTag(0, (short) 305);
    public static final int TAG_SPATIAL_FREQUENCY_RESPONSE = defineTag(2, (short) -24052);
    public static final int TAG_SPECTRAL_SENSITIVITY = defineTag(2, (short) -30684);
    public static final int TAG_STRIP_BYTE_COUNTS = defineTag(0, (short) 279);
    public static final int TAG_STRIP_OFFSETS = defineTag(0, (short) 273);
    public static final int TAG_SUBJECT_AREA = defineTag(2, (short) -28140);
    public static final int TAG_SUBJECT_DISTANCE = defineTag(2, (short) -28154);
    public static final int TAG_SUBJECT_DISTANCE_RANGE = defineTag(2, (short) -23540);
    public static final int TAG_SUBJECT_LOCATION = defineTag(2, (short) -24044);
    public static final int TAG_SUB_SEC_TIME = defineTag(2, (short) -28016);
    public static final int TAG_SUB_SEC_TIME_DIGITIZED = defineTag(2, (short) -28014);
    public static final int TAG_SUB_SEC_TIME_ORIGINAL = defineTag(2, (short) -28015);
    public static final int TAG_TRANSFER_FUNCTION = defineTag(0, (short) 301);
    public static final int TAG_USER_COMMENT = defineTag(2, (short) -28026);
    public static final int TAG_WHITE_BALANCE = defineTag(2, (short) -23549);
    public static final int TAG_WHITE_POINT = defineTag(0, (short) 318);
    public static final int TAG_X_RESOLUTION = defineTag(0, (short) 282);
    public static final int TAG_X_RESOLUTION_IFD1 = defineTag(1, (short) 282);
    public static final int TAG_Y_CB_CR_COEFFICIENTS = defineTag(0, (short) 529);
    public static final int TAG_Y_CB_CR_POSITIONING = defineTag(0, (short) 531);
    public static final int TAG_Y_CB_CR_SUB_SAMPLING = defineTag(0, (short) 530);
    public static final int TAG_Y_RESOLUTION = defineTag(0, (short) 283);
    public static final int TAG_Y_RESOLUTION_IFD1 = defineTag(1, (short) 283);
    private static final short TIFF_HEADER_SIZE = (short) 8;
    protected static HashSet<Short> sBannedDefines = new HashSet(sOffsetTags);
    protected static HashSet<Short> sOffsetTags = new HashSet();
    protected ExifData mData = new ExifData(DEFAULT_BYTE_ORDER, this);
    protected SparseIntArray mTagInfo = null;

    static {
        sOffsetTags.add(Short.valueOf(getTrueTagKey(TAG_GPS_IFD)));
        sOffsetTags.add(Short.valueOf(getTrueTagKey(TAG_EXIF_IFD)));
        sOffsetTags.add(Short.valueOf(getTrueTagKey(TAG_JPEG_INTERCHANGE_FORMAT)));
        sOffsetTags.add(Short.valueOf(getTrueTagKey(TAG_INTEROPERABILITY_IFD)));
        sOffsetTags.add(Short.valueOf(getTrueTagKey(TAG_STRIP_OFFSETS)));
        sBannedDefines.add(Short.valueOf(getTrueTagKey(-1)));
        sBannedDefines.add(Short.valueOf(getTrueTagKey(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)));
        sBannedDefines.add(Short.valueOf(getTrueTagKey(TAG_STRIP_BYTE_COUNTS)));
    }

    public static int defineTag(int ifdId, short tagId) {
        return (SupportMenu.USER_MASK & tagId) | (ifdId << 16);
    }

    public static short getTrueTagKey(int tag) {
        return (short) tag;
    }

    public static int getTrueIfd(int tag) {
        return tag >>> 16;
    }

    public List<ExifTag> getAllTags() {
        return this.mData.getAllTags();
    }

    public List<ExifTag> getTagsForTagId(short tagId) {
        return this.mData.getAllTagsForTagId(tagId);
    }

    public List<ExifTag> getTagsForIfdId(int ifdId) {
        return this.mData.getAllTagsForIfd(ifdId);
    }

    public ExifTag getTag(int tagId, int ifdId) {
        if (ExifTagBase.isValidIfd(ifdId)) {
            return this.mData.getTag(getTrueTagKey(tagId), ifdId);
        }
        return null;
    }

    public ExifTag getTag(int tagId) {
        return getTag(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public boolean isTagCountDefined(int tagId) {
        int info = getTagInfo().get(tagId);
        if (info == 0 || getComponentCountFromInfo(info) == 0) {
            return false;
        }
        return true;
    }

    public int getDefinedTagCount(int tagId) {
        int info = getTagInfo().get(tagId);
        if (info == 0) {
            return 0;
        }
        return getComponentCountFromInfo(info);
    }

    public int getActualTagCount(int tagId, int ifdId) {
        ExifTag t = getTag(tagId, ifdId);
        if (t == null) {
            return 0;
        }
        return t.getComponentCount();
    }

    public int getDefinedTagDefaultIfd(int tagId) {
        if (getTagInfo().get(tagId) == 0) {
            return -1;
        }
        return getTrueIfd(tagId);
    }

    public short getDefinedTagType(int tagId) {
        int info = getTagInfo().get(tagId);
        if (info == 0) {
            return (short) -1;
        }
        return getTypeFromInfo(info);
    }

    public ExifTag buildTag(int tagId, int ifdId, Object val) {
        int info = getTagInfo().get(tagId);
        if (info == 0 || val == null) {
            return null;
        }
        short type = getTypeFromInfo(info);
        int definedCount = getComponentCountFromInfo(info);
        boolean hasDefinedCount = definedCount != 0;
        if (!isIfdAllowed(info, ifdId)) {
            return null;
        }
        ExifTag t = new ExifTag(getTrueTagKey(tagId), type, definedCount, ifdId, hasDefinedCount);
        if (t.setValue(val)) {
            return t;
        }
        return null;
    }

    public ExifTag buildTag(int tagId, Object val) {
        return buildTag(tagId, getTrueIfd(tagId), val);
    }

    public ExifTag buildUninitializedTag(int tagId) {
        int info = getTagInfo().get(tagId);
        if (info == 0) {
            return null;
        }
        short type = getTypeFromInfo(info);
        int definedCount = getComponentCountFromInfo(info);
        return new ExifTag(getTrueTagKey(tagId), type, definedCount, getTrueIfd(tagId), definedCount != 0);
    }

    public boolean setTagValue(int tagId, int ifdId, Object val) {
        ExifTag t = getTag(tagId, ifdId);
        if (t == null) {
            return false;
        }
        return t.setValue(val);
    }

    public boolean setTagValue(int tagId, Object val) {
        return setTagValue(tagId, getDefinedTagDefaultIfd(tagId), val);
    }

    public ExifTag setTag(ExifTag tag) {
        return this.mData.addTag(tag);
    }

    public void setTags(Collection<ExifTag> tags) {
        if (tags != null) {
            for (ExifTag t : tags) {
                setTag(t);
            }
        }
    }

    public void deleteTag(int tagId, int ifdId) {
        this.mData.removeTag(getTrueTagKey(tagId), ifdId);
    }

    public void deleteTag(int tagId) {
        deleteTag(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public int setTagDefinition(short tagId, int defaultIfd, short tagType, short defaultComponentCount, int[] allowedIfds) {
        if (sBannedDefines.contains(Short.valueOf(tagId))) {
            return -1;
        }
        if (!ExifTagBase.isValidType(tagType) || !ExifTagBase.isValidIfd(defaultIfd)) {
            return -1;
        }
        int tagDef = defineTag(defaultIfd, tagId);
        if (tagDef == -1) {
            return -1;
        }
        int[] otherDefs = getTagDefinitionsForTagId(tagId);
        SparseIntArray infos = getTagInfo();
        boolean defaultCheck = false;
        for (int i : allowedIfds) {
            if (defaultIfd == i) {
                defaultCheck = true;
            }
            if (!ExifTagBase.isValidIfd(i)) {
                return -1;
            }
        }
        if (!defaultCheck) {
            return -1;
        }
        int ifdFlags = getFlagsFromAllowedIfds(allowedIfds);
        if (otherDefs != null) {
            for (int def : otherDefs) {
                if ((ifdFlags & getAllowedIfdFlagsFromInfo(infos.get(def))) != 0) {
                    return -1;
                }
            }
        }
        getTagInfo().put(tagDef, ((ifdFlags << 24) | (tagType << 16)) | defaultComponentCount);
        return tagDef;
    }

    protected int[] getTagDefinitionsForTagId(short tagId) {
        int[] ifds = IFD_ARRAY;
        int[] defs = new int[ifds.length];
        SparseIntArray infos = getTagInfo();
        int length = ifds.length;
        int i = 0;
        int counter = 0;
        while (i < length) {
            int counter2;
            int def = defineTag(ifds[i], tagId);
            if (infos.get(def) != 0) {
                counter2 = counter + 1;
                defs[counter] = def;
            } else {
                counter2 = counter;
            }
            i++;
            counter = counter2;
        }
        if (counter == 0) {
            return null;
        }
        return Arrays.copyOfRange(defs, 0, counter);
    }

    protected int getTagDefinitionForTag(ExifTag tag) {
        return getTagDefinitionForTag(tag.getTagId(), tag.getDataType(), tag.getComponentCount(), tag.getIfd());
    }

    protected int getTagDefinitionForTag(short tagId, short type, int count, int ifd) {
        int[] defs = getTagDefinitionsForTagId(tagId);
        if (defs == null) {
            return -1;
        }
        SparseIntArray infos = getTagInfo();
        for (int i : defs) {
            int info = infos.get(i);
            short def_type = getTypeFromInfo(info);
            int def_count = getComponentCountFromInfo(info);
            int[] def_ifds = getAllowedIfdsFromInfo(info);
            boolean valid_ifd = false;
            if (def_ifds != null) {
                for (int j : def_ifds) {
                    if (j == ifd) {
                        valid_ifd = true;
                        break;
                    }
                }
            }
            if (valid_ifd && type == def_type && (count == def_count || def_count == 0)) {
                return i;
            }
        }
        return -1;
    }

    public void removeTagDefinition(int tagId) {
        getTagInfo().delete(tagId);
    }

    public void resetTagDefinitions() {
        this.mTagInfo = null;
    }

    public SparseIntArray getTagInfo() {
        if (this.mTagInfo == null) {
            this.mTagInfo = new SparseIntArray();
            initTagInfo();
        }
        return this.mTagInfo;
    }

    private void initTagInfo() {
        int ifdFlags = getFlagsFromAllowedIfds(new int[]{0, 1}) << 24;
        this.mTagInfo.put(TAG_MAKE, (131072 | ifdFlags) | 0);
        this.mTagInfo.put(TAG_IMAGE_WIDTH, (262144 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_IMAGE_LENGTH, (262144 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_BITS_PER_SAMPLE, (196608 | ifdFlags) | 3);
        this.mTagInfo.put(TAG_COMPRESSION, (196608 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_PHOTOMETRIC_INTERPRETATION, (196608 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_ORIENTATION, (196608 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_SAMPLES_PER_PIXEL, (196608 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_PLANAR_CONFIGURATION, (196608 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_Y_CB_CR_SUB_SAMPLING, (196608 | ifdFlags) | 2);
        this.mTagInfo.put(TAG_Y_CB_CR_POSITIONING, (196608 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_X_RESOLUTION, (327680 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_Y_RESOLUTION, (327680 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_RESOLUTION_UNIT, (196608 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_STRIP_OFFSETS, (262144 | ifdFlags) | 0);
        this.mTagInfo.put(TAG_ROWS_PER_STRIP, (262144 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_STRIP_BYTE_COUNTS, (262144 | ifdFlags) | 0);
        this.mTagInfo.put(TAG_TRANSFER_FUNCTION, (196608 | ifdFlags) | 768);
        this.mTagInfo.put(TAG_WHITE_POINT, (327680 | ifdFlags) | 2);
        this.mTagInfo.put(TAG_PRIMARY_CHROMATICITIES, (327680 | ifdFlags) | 6);
        this.mTagInfo.put(TAG_Y_CB_CR_COEFFICIENTS, (327680 | ifdFlags) | 3);
        this.mTagInfo.put(TAG_REFERENCE_BLACK_WHITE, (327680 | ifdFlags) | 6);
        this.mTagInfo.put(TAG_DATE_TIME, (131072 | ifdFlags) | 20);
        this.mTagInfo.put(TAG_IMAGE_DESCRIPTION, (131072 | ifdFlags) | 0);
        this.mTagInfo.put(TAG_MAKE, (131072 | ifdFlags) | 0);
        this.mTagInfo.put(TAG_MODEL, (131072 | ifdFlags) | 0);
        this.mTagInfo.put(TAG_SOFTWARE, (131072 | ifdFlags) | 0);
        this.mTagInfo.put(TAG_ARTIST, (131072 | ifdFlags) | 0);
        this.mTagInfo.put(TAG_COPYRIGHT, (131072 | ifdFlags) | 0);
        this.mTagInfo.put(TAG_EXIF_IFD, (262144 | ifdFlags) | 1);
        this.mTagInfo.put(TAG_GPS_IFD, (262144 | ifdFlags) | 1);
        int ifdFlags1 = getFlagsFromAllowedIfds(new int[]{1}) << 24;
        this.mTagInfo.put(TAG_COMPRESSION_IFD1, (196608 | ifdFlags1) | 1);
        this.mTagInfo.put(TAG_ORIENTATION_IFD1, (196608 | ifdFlags1) | 1);
        this.mTagInfo.put(TAG_X_RESOLUTION_IFD1, (327680 | ifdFlags1) | 1);
        this.mTagInfo.put(TAG_Y_RESOLUTION_IFD1, (327680 | ifdFlags1) | 1);
        this.mTagInfo.put(TAG_RESOLUTION_UNIT_IFD1, (196608 | ifdFlags1) | 1);
        this.mTagInfo.put(TAG_JPEG_INTERCHANGE_FORMAT, (262144 | ifdFlags1) | 1);
        this.mTagInfo.put(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, (262144 | ifdFlags1) | 1);
        int exifFlags = getFlagsFromAllowedIfds(new int[]{2}) << 24;
        this.mTagInfo.put(TAG_EXIF_VERSION, (458752 | exifFlags) | 4);
        this.mTagInfo.put(TAG_FLASHPIX_VERSION, (458752 | exifFlags) | 4);
        this.mTagInfo.put(TAG_COLOR_SPACE, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_COMPONENTS_CONFIGURATION, (458752 | exifFlags) | 4);
        this.mTagInfo.put(TAG_COMPRESSED_BITS_PER_PIXEL, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_PIXEL_X_DIMENSION, (262144 | exifFlags) | 1);
        this.mTagInfo.put(TAG_PIXEL_Y_DIMENSION, (262144 | exifFlags) | 1);
        this.mTagInfo.put(TAG_MAKER_NOTE, (458752 | exifFlags) | 0);
        this.mTagInfo.put(TAG_USER_COMMENT, (458752 | exifFlags) | 0);
        this.mTagInfo.put(TAG_RELATED_SOUND_FILE, (131072 | exifFlags) | 13);
        this.mTagInfo.put(TAG_DATE_TIME_ORIGINAL, (131072 | exifFlags) | 20);
        this.mTagInfo.put(TAG_DATE_TIME_DIGITIZED, (131072 | exifFlags) | 20);
        this.mTagInfo.put(TAG_SUB_SEC_TIME, (131072 | exifFlags) | 0);
        this.mTagInfo.put(TAG_SUB_SEC_TIME_ORIGINAL, (131072 | exifFlags) | 0);
        this.mTagInfo.put(TAG_SUB_SEC_TIME_DIGITIZED, (131072 | exifFlags) | 0);
        this.mTagInfo.put(TAG_IMAGE_UNIQUE_ID, (131072 | exifFlags) | 33);
        this.mTagInfo.put(TAG_EXPOSURE_TIME, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_F_NUMBER, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_EXPOSURE_PROGRAM, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_SPECTRAL_SENSITIVITY, (131072 | exifFlags) | 0);
        this.mTagInfo.put(TAG_ISO_SPEED_RATINGS, (196608 | exifFlags) | 0);
        this.mTagInfo.put(TAG_OECF, (458752 | exifFlags) | 0);
        this.mTagInfo.put(TAG_SHUTTER_SPEED_VALUE, (655360 | exifFlags) | 1);
        this.mTagInfo.put(TAG_APERTURE_VALUE, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_BRIGHTNESS_VALUE, (655360 | exifFlags) | 1);
        this.mTagInfo.put(TAG_EXPOSURE_BIAS_VALUE, (655360 | exifFlags) | 1);
        this.mTagInfo.put(TAG_MAX_APERTURE_VALUE, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_SUBJECT_DISTANCE, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_METERING_MODE, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_LIGHT_SOURCE, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_FLASH, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_FOCAL_LENGTH, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_SUBJECT_AREA, (196608 | exifFlags) | 0);
        this.mTagInfo.put(TAG_FLASH_ENERGY, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_SPATIAL_FREQUENCY_RESPONSE, (458752 | exifFlags) | 0);
        this.mTagInfo.put(TAG_FOCAL_PLANE_X_RESOLUTION, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_FOCAL_PLANE_Y_RESOLUTION, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_FOCAL_PLANE_RESOLUTION_UNIT, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_SUBJECT_LOCATION, (196608 | exifFlags) | 2);
        this.mTagInfo.put(TAG_EXPOSURE_INDEX, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_SENSING_METHOD, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_FILE_SOURCE, (458752 | exifFlags) | 1);
        this.mTagInfo.put(TAG_SCENE_TYPE, (458752 | exifFlags) | 1);
        this.mTagInfo.put(TAG_CFA_PATTERN, (458752 | exifFlags) | 0);
        this.mTagInfo.put(TAG_CUSTOM_RENDERED, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_EXPOSURE_MODE, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_WHITE_BALANCE, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_DIGITAL_ZOOM_RATIO, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_FOCAL_LENGTH_IN_35_MM_FILE, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_SCENE_CAPTURE_TYPE, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_GAIN_CONTROL, (327680 | exifFlags) | 1);
        this.mTagInfo.put(TAG_CONTRAST, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_SATURATION, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_SHARPNESS, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_DEVICE_SETTING_DESCRIPTION, (458752 | exifFlags) | 0);
        this.mTagInfo.put(TAG_SUBJECT_DISTANCE_RANGE, (196608 | exifFlags) | 1);
        this.mTagInfo.put(TAG_INTEROPERABILITY_IFD, (262144 | exifFlags) | 1);
        int gpsFlags = getFlagsFromAllowedIfds(new int[]{4}) << 24;
        this.mTagInfo.put(TAG_GPS_VERSION_ID, (65536 | gpsFlags) | 4);
        this.mTagInfo.put(TAG_GPS_LATITUDE_REF, (131072 | gpsFlags) | 2);
        this.mTagInfo.put(TAG_GPS_LONGITUDE_REF, (131072 | gpsFlags) | 2);
        this.mTagInfo.put(TAG_GPS_LATITUDE, (655360 | gpsFlags) | 3);
        this.mTagInfo.put(TAG_GPS_LONGITUDE, (655360 | gpsFlags) | 3);
        this.mTagInfo.put(TAG_GPS_ALTITUDE_REF, (65536 | gpsFlags) | 1);
        this.mTagInfo.put(TAG_GPS_ALTITUDE, (327680 | gpsFlags) | 1);
        this.mTagInfo.put(TAG_GPS_TIME_STAMP, (327680 | gpsFlags) | 3);
        this.mTagInfo.put(TAG_GPS_SATTELLITES, (131072 | gpsFlags) | 0);
        this.mTagInfo.put(TAG_GPS_STATUS, (131072 | gpsFlags) | 2);
        this.mTagInfo.put(TAG_GPS_MEASURE_MODE, (131072 | gpsFlags) | 2);
        this.mTagInfo.put(TAG_GPS_DOP, (327680 | gpsFlags) | 1);
        this.mTagInfo.put(TAG_GPS_SPEED_REF, (131072 | gpsFlags) | 2);
        this.mTagInfo.put(TAG_GPS_SPEED, (327680 | gpsFlags) | 1);
        this.mTagInfo.put(TAG_GPS_TRACK_REF, (131072 | gpsFlags) | 2);
        this.mTagInfo.put(TAG_GPS_TRACK, (327680 | gpsFlags) | 1);
        this.mTagInfo.put(TAG_GPS_IMG_DIRECTION_REF, (131072 | gpsFlags) | 2);
        this.mTagInfo.put(TAG_GPS_IMG_DIRECTION, (327680 | gpsFlags) | 1);
        this.mTagInfo.put(TAG_GPS_MAP_DATUM, (131072 | gpsFlags) | 0);
        this.mTagInfo.put(TAG_GPS_DEST_LATITUDE_REF, (131072 | gpsFlags) | 2);
        this.mTagInfo.put(TAG_GPS_DEST_LATITUDE, (327680 | gpsFlags) | 1);
        this.mTagInfo.put(TAG_GPS_DEST_BEARING_REF, (131072 | gpsFlags) | 2);
        this.mTagInfo.put(TAG_GPS_DEST_BEARING, (327680 | gpsFlags) | 1);
        this.mTagInfo.put(TAG_GPS_DEST_DISTANCE_REF, (131072 | gpsFlags) | 2);
        this.mTagInfo.put(TAG_GPS_DEST_DISTANCE, (327680 | gpsFlags) | 1);
        this.mTagInfo.put(TAG_GPS_PROCESSING_METHOD, (458752 | gpsFlags) | 0);
        this.mTagInfo.put(TAG_GPS_AREA_INFORMATION, (458752 | gpsFlags) | 0);
        this.mTagInfo.put(TAG_GPS_DATE_STAMP, (131072 | gpsFlags) | 11);
        this.mTagInfo.put(TAG_GPS_DIFFERENTIAL, (196608 | gpsFlags) | 11);
        int interopFlags = getFlagsFromAllowedIfds(new int[]{3}) << 24;
        this.mTagInfo.put(TAG_INTEROPERABILITY_INDEX, (131072 | interopFlags) | 0);
        this.mTagInfo.put(TAG_INTEROPERABILITY_VERSION, (458752 | interopFlags) | 0);
    }

    protected static int getAllowedIfdFlagsFromInfo(int info) {
        return info >>> 24;
    }

    protected static int[] getAllowedIfdsFromInfo(int info) {
        int ifdFlags = getAllowedIfdFlagsFromInfo(info);
        int[] ifds = IFD_ARRAY;
        ArrayList<Integer> l = new ArrayList();
        for (int i = 0; i < 5; i++) {
            if (((ifdFlags >> i) & 1) == 1) {
                l.add(Integer.valueOf(ifds[i]));
            }
        }
        if (l.size() <= 0) {
            return null;
        }
        int[] ret = new int[l.size()];
        int j = 0;
        Iterator it = l.iterator();
        while (it.hasNext()) {
            int j2 = j + 1;
            ret[j] = ((Integer) it.next()).intValue();
            j = j2;
        }
        return ret;
    }

    protected static boolean isIfdAllowed(int info, int ifd) {
        int[] ifds = IFD_ARRAY;
        int ifdFlags = getAllowedIfdFlagsFromInfo(info);
        int i = 0;
        while (i < ifds.length) {
            if (ifd == ifds[i] && ((ifdFlags >> i) & 1) == 1) {
                return true;
            }
            i++;
        }
        return false;
    }

    protected static int getFlagsFromAllowedIfds(int[] allowedIfds) {
        if (allowedIfds == null || allowedIfds.length == 0) {
            return 0;
        }
        int flags = 0;
        int[] ifds = IFD_ARRAY;
        for (int i = 0; i < 5; i++) {
            for (int j : allowedIfds) {
                if (ifds[i] == j) {
                    flags |= 1 << i;
                    break;
                }
            }
        }
        return flags;
    }

    protected static short getTypeFromInfo(int info) {
        return (short) ((info >> 16) & 255);
    }

    protected static int getComponentCountFromInfo(int info) {
        return SupportMenu.USER_MASK & info;
    }

    public boolean isExifOffsetTag(short tag) {
        return sOffsetTags.contains(Short.valueOf(tag));
    }

    public void createRequiredIfdAndTag(Object exifDataObject) throws IOException {
        ExifData exifData = (ExifData) exifDataObject;
        IfdData ifd0 = exifData.getIfdData(0);
        if (ifd0 == null) {
            ifd0 = new IfdData(0, this);
            exifData.addIfdData(ifd0);
        }
        ExifTag exifOffsetTag = buildUninitializedTag(TAG_EXIF_IFD);
        if (exifOffsetTag == null) {
            throw new IOException("No definition for crucial exif tag: " + TAG_EXIF_IFD);
        }
        ifd0.setTag(exifOffsetTag);
        IfdData exifIfd = exifData.getIfdData(2);
        if (exifIfd == null) {
            exifIfd = new IfdData(2, this);
            exifData.addIfdData(exifIfd);
        }
        if (exifData.getIfdData(4) != null) {
            ExifTag gpsOffsetTag = buildUninitializedTag(TAG_GPS_IFD);
            if (gpsOffsetTag == null) {
                throw new IOException("No definition for crucial exif tag: " + TAG_GPS_IFD);
            }
            ifd0.setTag(gpsOffsetTag);
        }
        if (exifData.getIfdData(3) != null) {
            ExifTag interOffsetTag = buildUninitializedTag(TAG_INTEROPERABILITY_IFD);
            if (interOffsetTag == null) {
                throw new IOException("No definition for crucial exif tag: " + TAG_INTEROPERABILITY_IFD);
            }
            exifIfd.setTag(interOffsetTag);
        }
        IfdData ifd1 = exifData.getIfdData(1);
        ExifTag offsetTag;
        ExifTag lengthTag;
        if (exifData.hasCompressedThumbnail()) {
            if (ifd1 == null) {
                ifd1 = new IfdData(1, this);
                exifData.addIfdData(ifd1);
            }
            offsetTag = buildUninitializedTag(TAG_JPEG_INTERCHANGE_FORMAT);
            if (offsetTag == null) {
                throw new IOException("No definition for crucial exif tag: " + TAG_JPEG_INTERCHANGE_FORMAT);
            }
            ifd1.setTag(offsetTag);
            lengthTag = buildUninitializedTag(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
            if (lengthTag == null) {
                throw new IOException("No definition for crucial exif tag: " + TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
            }
            lengthTag.setValue(exifData.getCompressedThumbnail().length);
            ifd1.setTag(lengthTag);
            ifd1.removeTag(getTrueTagKey(TAG_STRIP_OFFSETS));
            ifd1.removeTag(getTrueTagKey(TAG_STRIP_BYTE_COUNTS));
        } else if (exifData.hasUncompressedStrip()) {
            if (ifd1 == null) {
                ifd1 = new IfdData(1, this);
                exifData.addIfdData(ifd1);
            }
            int stripCount = exifData.getStripCount();
            offsetTag = buildUninitializedTag(TAG_STRIP_OFFSETS);
            if (offsetTag == null) {
                throw new IOException("No definition for crucial exif tag: " + TAG_STRIP_OFFSETS);
            }
            lengthTag = buildUninitializedTag(TAG_STRIP_BYTE_COUNTS);
            if (lengthTag == null) {
                throw new IOException("No definition for crucial exif tag: " + TAG_STRIP_BYTE_COUNTS);
            }
            long[] lengths = new long[stripCount];
            for (int i = 0; i < exifData.getStripCount(); i++) {
                lengths[i] = (long) exifData.getStrip(i).length;
            }
            lengthTag.setValue(lengths);
            ifd1.setTag(offsetTag);
            ifd1.setTag(lengthTag);
            ifd1.removeTag(getTrueTagKey(TAG_JPEG_INTERCHANGE_FORMAT));
            ifd1.removeTag(getTrueTagKey(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH));
        } else if (ifd1 != null) {
            ifd1.removeTag(getTrueTagKey(TAG_STRIP_OFFSETS));
            ifd1.removeTag(getTrueTagKey(TAG_STRIP_BYTE_COUNTS));
            ifd1.removeTag(getTrueTagKey(TAG_JPEG_INTERCHANGE_FORMAT));
            ifd1.removeTag(getTrueTagKey(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH));
        }
    }

    private int calculateOffsetOfIfd(IfdData ifd, int offset) {
        offset += ((ifd.getTagCount() * 12) + 2) + 4;
        for (ExifTag tag : ifd.getAllTags()) {
            if (tag.getDataSize() > 4) {
                tag.setOffset(offset);
                offset += tag.getDataSize();
            }
        }
        return offset;
    }

    public int calculateAllOffset(Object exifDataObject) {
        ExifData exifData = (ExifData) exifDataObject;
        int offset = 8;
        IfdData ifd0 = exifData.getIfdData(0);
        if (ifd0 != null) {
            offset = calculateOffsetOfIfd(ifd0, 8);
            ifd0.getTag(getTrueTagKey(TAG_EXIF_IFD)).setValue(offset);
            IfdData exifIfd = exifData.getIfdData(2);
            if (exifIfd != null) {
                offset = calculateOffsetOfIfd(exifIfd, offset);
                IfdData interIfd = exifData.getIfdData(3);
                if (interIfd != null) {
                    exifIfd.getTag(getTrueTagKey(TAG_INTEROPERABILITY_IFD)).setValue(offset);
                    offset = calculateOffsetOfIfd(interIfd, offset);
                }
            }
            IfdData gpsIfd = exifData.getIfdData(4);
            if (gpsIfd != null) {
                ifd0.getTag(getTrueTagKey(TAG_GPS_IFD)).setValue(offset);
                offset = calculateOffsetOfIfd(gpsIfd, offset);
            }
            IfdData ifd1 = exifData.getIfdData(1);
            if (ifd1 != null) {
                ifd0.setOffsetToNextIfd(offset);
                offset = calculateOffsetOfIfd(ifd1, offset);
                if (exifData.hasCompressedThumbnail()) {
                    ifd1.getTag(getTrueTagKey(TAG_JPEG_INTERCHANGE_FORMAT)).setValue(offset);
                    int thumbnailSize = exifData.getCompressedThumbnail().length;
                    CamLog.m3d(CameraConstants.TAG, "exif thumbnail size = " + thumbnailSize);
                    offset += thumbnailSize;
                } else if (exifData.hasUncompressedStrip()) {
                    long[] offsets = new long[exifData.getStripCount()];
                    for (int i = 0; i < exifData.getStripCount(); i++) {
                        offsets[i] = (long) offset;
                        offset += exifData.getStrip(i).length;
                    }
                    ifd1.getTag(getTrueTagKey(TAG_STRIP_OFFSETS)).setValue(offsets);
                }
            }
        }
        CamLog.m3d(CameraConstants.TAG, "exif size = " + offset);
        return offset;
    }

    public boolean checkAllowedIfd(short tid, int checkAllowIfd, int ifd) {
        int tagId = -1;
        switch (checkAllowIfd) {
            case 0:
                tagId = TAG_EXIF_IFD;
                break;
            case 1:
                tagId = TAG_GPS_IFD;
                break;
            case 2:
                tagId = TAG_INTEROPERABILITY_IFD;
                break;
            case 3:
                tagId = TAG_JPEG_INTERCHANGE_FORMAT;
                break;
            case 4:
                tagId = TAG_JPEG_INTERCHANGE_FORMAT_LENGTH;
                break;
            case 5:
                tagId = TAG_STRIP_OFFSETS;
                break;
            case 6:
                tagId = TAG_STRIP_BYTE_COUNTS;
                break;
        }
        if (tagId != -1 && tid == getTrueTagKey(tagId)) {
            int info = getTagInfo().get(tagId);
            if (info != 0) {
                return isIfdAllowed(info, ifd);
            }
        }
        return false;
    }

    public Bitmap getThumbnailBitmap() {
        if (this.mData.hasCompressedThumbnail()) {
            byte[] thumb = this.mData.getCompressedThumbnail();
            return BitmapFactory.decodeByteArray(thumb, 0, thumb.length);
        }
        if (this.mData.hasUncompressedStrip()) {
        }
        return null;
    }

    public byte[] getThumbnailBytes() {
        if (this.mData.hasCompressedThumbnail()) {
            return this.mData.getCompressedThumbnail();
        }
        if (this.mData.hasUncompressedStrip()) {
        }
        return null;
    }

    public byte[] getThumbnail() {
        return this.mData.getCompressedThumbnail();
    }

    public boolean isThumbnailCompressed() {
        return this.mData.hasCompressedThumbnail();
    }

    public boolean hasThumbnail() {
        return this.mData.hasCompressedThumbnail();
    }

    public boolean setCompressedThumbnail(byte[] thumb) {
        this.mData.clearThumbnailAndStrips();
        this.mData.setCompressedThumbnail(thumb);
        return true;
    }

    public boolean setCompressedThumbnail(Bitmap thumb, int quality) {
        if (thumb == null) {
            return false;
        }
        ByteArrayOutputStream thumbnail = new ByteArrayOutputStream();
        if (thumb.compress(CompressFormat.JPEG, quality, thumbnail)) {
            return setCompressedThumbnail(thumbnail.toByteArray());
        }
        return false;
    }

    public void removeCompressedThumbnail() {
        this.mData.setCompressedThumbnail(null);
    }
}
