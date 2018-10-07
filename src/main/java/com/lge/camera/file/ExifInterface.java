package com.lge.camera.file;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;

public class ExifInterface extends ExifInterfaceBase {
    private static final String DATETIME_FORMAT_STR = "yyyy:MM:dd HH:mm:ss";
    private static final String GPS_DATE_FORMAT_STR = "yyyy:MM:dd";
    public static final String INTEROP_INDEX_STR = "R98";
    public static final String INTEROP_VER = "0100";
    private static final String NULL_ARGUMENT_STRING = "Argument is null";
    public static final long X_RESOL_DPI = 72;
    public static final long Y_RESOL_DPI = 72;
    private final DateFormat mDateTimeStampFormat = new SimpleDateFormat(DATETIME_FORMAT_STR, Locale.US);
    private final DateFormat mGPSDateStampFormat = new SimpleDateFormat(GPS_DATE_FORMAT_STR, Locale.US);
    private final Calendar mGPSTimeStampCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    public interface ColorSpace {
        public static final short SRGB = (short) 1;
        public static final short UNCALIBRATED = (short) -1;
    }

    public interface ComponentsConfiguration {
        /* renamed from: B */
        public static final short f20B = (short) 6;
        /* renamed from: CB */
        public static final short f21CB = (short) 2;
        /* renamed from: CR */
        public static final short f22CR = (short) 3;
        /* renamed from: G */
        public static final short f23G = (short) 5;
        public static final short NOT_EXIST = (short) 0;
        /* renamed from: R */
        public static final short f24R = (short) 4;
        /* renamed from: Y */
        public static final short f25Y = (short) 1;
    }

    public interface Compression {
        public static final short JPEG = (short) 6;
        public static final short UNCOMPRESSION = (short) 1;
    }

    public interface Contrast {
        public static final short HARD = (short) 2;
        public static final short NORMAL = (short) 0;
        public static final short SOFT = (short) 1;
    }

    public interface ExposureMode {
        public static final short AUTO_BRACKET = (short) 2;
        public static final short AUTO_EXPOSURE = (short) 0;
        public static final short MANUAL_EXPOSURE = (short) 1;
    }

    public interface ExposureProgram {
        public static final short ACTION_PROGRAM = (short) 6;
        public static final short APERTURE_PRIORITY = (short) 3;
        public static final short CREATIVE_PROGRAM = (short) 5;
        public static final short LANDSCAPE_MODE = (short) 8;
        public static final short MANUAL = (short) 1;
        public static final short NORMAL_PROGRAM = (short) 2;
        public static final short NOT_DEFINED = (short) 0;
        public static final short PROTRAIT_MODE = (short) 7;
        public static final short SHUTTER_PRIORITY = (short) 4;
    }

    public interface FileSource {
        public static final short DSC = (short) 3;
    }

    public interface Flash {
        public static final short DID_NOT_FIRED = (short) 0;
        public static final short FIRED = (short) 1;
        public static final short FUNCTION_NO_FUNCTION = (short) 32;
        public static final short FUNCTION_PRESENT = (short) 0;
        public static final short MODE_AUTO_MODE = (short) 24;
        public static final short MODE_COMPULSORY_FLASH_FIRING = (short) 8;
        public static final short MODE_COMPULSORY_FLASH_SUPPRESSION = (short) 16;
        public static final short MODE_UNKNOWN = (short) 0;
        public static final short RED_EYE_REDUCTION_NO_OR_UNKNOWN = (short) 0;
        public static final short RED_EYE_REDUCTION_SUPPORT = (short) 64;
        public static final short RETURN_NO_STROBE_RETURN_DETECTION_FUNCTION = (short) 0;
        public static final short RETURN_STROBE_RETURN_LIGHT_DETECTED = (short) 6;
        public static final short RETURN_STROBE_RETURN_LIGHT_NOT_DETECTED = (short) 4;
    }

    public interface GainControl {
        public static final short HIGH_DOWN = (short) 4;
        public static final short HIGH_UP = (short) 2;
        public static final short LOW_DOWN = (short) 3;
        public static final short LOW_UP = (short) 1;
        public static final short NONE = (short) 0;
    }

    public interface GpsAltitudeRef {
        public static final short SEA_LEVEL = (short) 0;
        public static final short SEA_LEVEL_NEGATIVE = (short) 1;
    }

    public interface GpsDifferential {
        public static final short DIFFERENTIAL_CORRECTION_APPLIED = (short) 1;
        public static final short WITHOUT_DIFFERENTIAL_CORRECTION = (short) 0;
    }

    public interface GpsLatitudeRef {
        public static final String NORTH = "N";
        public static final String SOUTH = "S";
    }

    public interface GpsLongitudeRef {
        public static final String EAST = "E";
        public static final String WEST = "W";
    }

    public interface GpsMeasureMode {
        public static final String MODE_2_DIMENSIONAL = "2";
        public static final String MODE_3_DIMENSIONAL = "3";
    }

    public interface GpsSpeedRef {
        public static final String KILOMETERS = "K";
        public static final String KNOTS = "N";
        public static final String MILES = "M";
    }

    public interface GpsStatus {
        public static final String INTEROPERABILITY = "V";
        public static final String IN_PROGRESS = "A";
    }

    public interface GpsTrackRef {
        public static final String MAGNETIC_DIRECTION = "M";
        public static final String TRUE_DIRECTION = "T";
    }

    public interface LightSource {
        public static final short CLOUDY_WEATHER = (short) 10;
        public static final short COOL_WHITE_FLUORESCENT = (short) 14;
        public static final short D50 = (short) 23;
        public static final short D55 = (short) 20;
        public static final short D65 = (short) 21;
        public static final short D75 = (short) 22;
        public static final short DAYLIGHT = (short) 1;
        public static final short DAYLIGHT_FLUORESCENT = (short) 12;
        public static final short DAY_WHITE_FLUORESCENT = (short) 13;
        public static final short FINE_WEATHER = (short) 9;
        public static final short FLASH = (short) 4;
        public static final short FLUORESCENT = (short) 2;
        public static final short ISO_STUDIO_TUNGSTEN = (short) 24;
        public static final short OTHER = (short) 255;
        public static final short SHADE = (short) 11;
        public static final short STANDARD_LIGHT_A = (short) 17;
        public static final short STANDARD_LIGHT_B = (short) 18;
        public static final short STANDARD_LIGHT_C = (short) 19;
        public static final short TUNGSTEN = (short) 3;
        public static final short UNKNOWN = (short) 0;
        public static final short WHITE_FLUORESCENT = (short) 15;
    }

    public interface MeteringMode {
        public static final short AVERAGE = (short) 1;
        public static final short CENTER_WEIGHTED_AVERAGE = (short) 2;
        public static final short MULTISPOT = (short) 4;
        public static final short OTHER = (short) 255;
        public static final short PARTAIL = (short) 6;
        public static final short PATTERN = (short) 5;
        public static final short SPOT = (short) 3;
        public static final short UNKNOWN = (short) 0;
    }

    public interface Orientation {
        public static final short BOTTOM_LEFT = (short) 3;
        public static final short BOTTOM_RIGHT = (short) 4;
        public static final short LEFT_BOTTOM = (short) 7;
        public static final short LEFT_TOP = (short) 5;
        public static final short RIGHT_BOTTOM = (short) 8;
        public static final short RIGHT_TOP = (short) 6;
        public static final short TOP_LEFT = (short) 1;
        public static final short TOP_RIGHT = (short) 2;
    }

    public interface PhotometricInterpretation {
        public static final short RGB = (short) 2;
        public static final short YCBCR = (short) 6;
    }

    public interface PlanarConfiguration {
        public static final short CHUNKY = (short) 1;
        public static final short PLANAR = (short) 2;
    }

    public interface ResolutionUnit {
        public static final short CENTIMETERS = (short) 3;
        public static final short INCHES = (short) 2;
    }

    public interface Saturation {
        public static final short HIGH = (short) 2;
        public static final short LOW = (short) 1;
        public static final short NORMAL = (short) 0;
    }

    public interface SceneCapture {
        public static final short DUAL_POP_NORMAL = (short) 22;
        public static final short DUAL_POP_WIDE = (short) 23;
        public static final short FOOD = (short) 16;
        public static final short GRID = (short) 15;
        public static final short LANDSCAPE = (short) 1;
        public static final short LIVE_PHOTO = (short) 20;
        public static final short MULTI_VIEW = (short) 11;
        public static final short NIGHT_SCENE = (short) 3;
        public static final short OUTFOCUS_DUAL = (short) 24;
        public static final short OUTFOCUS_SINGLE = (short) 21;
        public static final short OVERLAP = (short) 13;
        public static final short PANORAMA = (short) 10;
        public static final short POPOUT = (short) 12;
        public static final short PROTRAIT = (short) 2;
        public static final short SNAP_SHOT = (short) 17;
        public static final short SPLICE = (short) 14;
        public static final short STANDARD = (short) 0;
    }

    public interface SceneType {
        public static final short DIRECT_PHOTOGRAPHED = (short) 1;
    }

    public interface SensingMethod {
        public static final short COLOR_SEQUENTIAL_AREA = (short) 5;
        public static final short COLOR_SEQUENTIAL_LINEAR = (short) 8;
        public static final short NOT_DEFINED = (short) 1;
        public static final short ONE_CHIP_COLOR = (short) 2;
        public static final short THREE_CHIP_COLOR = (short) 4;
        public static final short TRILINEAR = (short) 7;
        public static final short TWO_CHIP_COLOR = (short) 3;
    }

    public interface Sharpness {
        public static final short HARD = (short) 2;
        public static final short NORMAL = (short) 0;
        public static final short SOFT = (short) 1;
    }

    public interface SubjectDistance {
        public static final short CLOSE_VIEW = (short) 2;
        public static final short DISTANT_VIEW = (short) 3;
        public static final short MACRO = (short) 1;
        public static final short UNKNOWN = (short) 0;
    }

    public interface WhiteBalance {
        public static final short AUTO = (short) 0;
        public static final short MANUAL = (short) 1;
    }

    public interface YCbCrPositioning {
        public static final short CENTERED = (short) 1;
        public static final short CO_SITED = (short) 2;
    }

    public ExifInterface() {
        this.mGPSDateStampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void readExif(byte[] jpeg) throws IOException {
        readExif(new ByteArrayInputStream(jpeg));
    }

    public void readExif(InputStream inStream) throws IOException {
        if (inStream == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
        try {
            this.mData = new ExifReader().read(inStream, this);
        } catch (ExifInvalidFormatException e) {
            throw new IOException("Invalid exif format : " + e);
        }
    }

    public void readExif(String inFileName) throws FileNotFoundException, IOException {
        IOException e;
        if (inFileName == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
        InputStream is = null;
        try {
            InputStream is2 = new BufferedInputStream(new FileInputStream(inFileName));
            try {
                readExif(is2);
                is2.close();
            } catch (IOException e2) {
                e = e2;
                is = is2;
                closeSilently(is);
                throw e;
            }
        } catch (IOException e3) {
            e = e3;
            closeSilently(is);
            throw e;
        }
    }

    public void setExif(Collection<ExifTag> tags) {
        clearExif();
        setTags(tags);
    }

    public void clearExif() {
        this.mData = new ExifData(DEFAULT_BYTE_ORDER, this);
    }

    public void writeExif(byte[] jpeg, OutputStream exifOutStream) throws IOException {
        if (jpeg == null || exifOutStream == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
        OutputStream s = getExifWriterStream(exifOutStream);
        s.write(jpeg, 0, jpeg.length);
        s.flush();
    }

    public void writeExif(Bitmap bmap, OutputStream exifOutStream) throws IOException {
        if (bmap == null || exifOutStream == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
        OutputStream s = getExifWriterStream(exifOutStream);
        bmap.compress(CompressFormat.JPEG, 90, s);
        s.flush();
    }

    public void writeExif(InputStream jpegStream, OutputStream exifOutStream) throws IOException {
        if (jpegStream == null || exifOutStream == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
        OutputStream s = getExifWriterStream(exifOutStream);
        doExifStreamIO(jpegStream, s);
        s.flush();
    }

    public void writeExif(byte[] jpeg, String exifOutFileName) throws FileNotFoundException, IOException {
        IOException e;
        if (jpeg == null || exifOutFileName == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
        OutputStream out = null;
        OutputStream s = null;
        try {
            OutputStream out2 = new FileOutputStream(exifOutFileName);
            try {
                s = getExifWriterStream(out2);
                s.write(jpeg, 0, jpeg.length);
                s.flush();
                out2.close();
                s.close();
            } catch (IOException e2) {
                e = e2;
                out = out2;
                closeSilently(out);
                closeSilently(s);
                throw e;
            }
        } catch (IOException e3) {
            e = e3;
            closeSilently(out);
            closeSilently(s);
            throw e;
        }
    }

    public void writeExif(Bitmap bmap, String exifOutFileName) throws FileNotFoundException, IOException {
        IOException e;
        if (bmap == null || exifOutFileName == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
        OutputStream out = null;
        OutputStream s = null;
        try {
            OutputStream out2 = new FileOutputStream(exifOutFileName);
            try {
                s = getExifWriterStream(out2);
                bmap.compress(CompressFormat.JPEG, 90, s);
                s.flush();
                out2.close();
                s.close();
            } catch (IOException e2) {
                e = e2;
                out = out2;
                closeSilently(out);
                closeSilently(s);
                throw e;
            }
        } catch (IOException e3) {
            e = e3;
            closeSilently(out);
            closeSilently(s);
            throw e;
        }
    }

    public void writeExif(InputStream jpegStream, String exifOutFileName) throws FileNotFoundException, IOException {
        IOException e;
        if (jpegStream == null || exifOutFileName == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
        OutputStream out = null;
        OutputStream s = null;
        try {
            OutputStream out2 = new FileOutputStream(exifOutFileName);
            try {
                s = getExifWriterStream(out2);
                doExifStreamIO(jpegStream, s);
                s.flush();
                out2.close();
                s.close();
            } catch (IOException e2) {
                e = e2;
                out = out2;
                closeSilently(out);
                closeSilently(s);
                throw e;
            }
        } catch (IOException e3) {
            e = e3;
            closeSilently(out);
            closeSilently(s);
            throw e;
        }
    }

    public void writeExif(String jpegFileName, String exifOutFileName) throws FileNotFoundException, IOException {
        IOException e;
        if (jpegFileName == null || exifOutFileName == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(jpegFileName);
            try {
                writeExif(is2, exifOutFileName);
                is2.close();
            } catch (IOException e2) {
                e = e2;
                is = is2;
                closeSilently(is);
                throw e;
            }
        } catch (IOException e3) {
            e = e3;
            closeSilently(is);
            throw e;
        }
    }

    public OutputStream getExifWriterStream(OutputStream outStream) {
        if (outStream == null) {
            throw new IllegalArgumentException(NULL_ARGUMENT_STRING);
        }
        ExifOutputStream eos = new ExifOutputStream(outStream, this);
        eos.setExifData(this.mData);
        return eos;
    }

    public boolean rewriteExif(String filename, Collection<ExifTag> tags) throws FileNotFoundException, IOException {
        IOException e;
        Throwable th;
        RandomAccessFile file = null;
        InputStream is = null;
        try {
            File file2 = new File(filename);
            InputStream is2 = new BufferedInputStream(new FileInputStream(file2));
            try {
                long exifSize = (long) ExifParser.parse(is2, this).getOffsetToExifEndFromSOF();
                is2.close();
                is = null;
                RandomAccessFile file3 = new RandomAccessFile(file2, "rw");
                try {
                    if (file3.length() < exifSize) {
                        closeSilently(file3);
                        throw new IOException("Filesize changed during operation");
                    }
                    boolean ret = rewriteExif(file3.getChannel().map(MapMode.READ_WRITE, 0, exifSize), (Collection) tags);
                    closeSilently(null);
                    file3.close();
                    return ret;
                } catch (IOException e2) {
                    e = e2;
                    file = file3;
                    try {
                        closeSilently(file);
                        throw e;
                    } catch (Throwable th2) {
                        th = th2;
                        closeSilently(is);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    file = file3;
                    closeSilently(is);
                    throw th;
                }
            } catch (ExifInvalidFormatException e3) {
                throw new IOException("Invalid exif format : ", e3);
            } catch (IOException e4) {
                e = e4;
                is = is2;
                closeSilently(file);
                throw e;
            } catch (Throwable th4) {
                th = th4;
                is = is2;
                closeSilently(is);
                throw th;
            }
        } catch (IOException e5) {
            e = e5;
            closeSilently(file);
            throw e;
        }
    }

    public boolean rewriteExif(ByteBuffer buf, Collection<ExifTag> tags) throws IOException {
        ExifInvalidFormatException e;
        try {
            ExifModifier mod = new ExifModifier(buf, this);
            if (tags != null) {
                try {
                    for (ExifTag t : tags) {
                        mod.modifyTag(t);
                    }
                } catch (ExifInvalidFormatException e2) {
                    e = e2;
                    ExifModifier exifModifier = mod;
                }
            }
            return mod.commit();
        } catch (ExifInvalidFormatException e3) {
            e = e3;
            throw new IOException("Invalid exif format : " + e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x003d  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x003d  */
    public void forceRewriteExif(java.lang.String r10, java.util.Collection<com.lge.camera.file.ExifTag> r11) throws java.io.FileNotFoundException, java.io.IOException {
        /*
        r9 = this;
        r7 = r9.rewriteExif(r10, r11);
        if (r7 != 0) goto L_0x0034;
    L_0x0006:
        r6 = r9.mData;
        r7 = new com.lge.camera.file.ExifData;
        r8 = DEFAULT_BYTE_ORDER;
        r7.<init>(r8, r9);
        r9.mData = r7;
        r4 = 0;
        r0 = 0;
        r5 = new java.io.FileInputStream;	 Catch:{ IOException -> 0x0035 }
        r5.<init>(r10);	 Catch:{ IOException -> 0x0035 }
        r1 = new java.io.ByteArrayOutputStream;	 Catch:{ IOException -> 0x004a, all -> 0x0043 }
        r1.<init>();	 Catch:{ IOException -> 0x004a, all -> 0x0043 }
        r9.doExifStreamIO(r5, r1);	 Catch:{ IOException -> 0x004d, all -> 0x0046 }
        r3 = r1.toByteArray();	 Catch:{ IOException -> 0x004d, all -> 0x0046 }
        r9.readExif(r3);	 Catch:{ IOException -> 0x004d, all -> 0x0046 }
        r9.setTags(r11);	 Catch:{ IOException -> 0x004d, all -> 0x0046 }
        r9.writeExif(r3, r10);	 Catch:{ IOException -> 0x004d, all -> 0x0046 }
        if (r5 == 0) goto L_0x0032;
    L_0x002f:
        r5.close();
    L_0x0032:
        r9.mData = r6;
    L_0x0034:
        return;
    L_0x0035:
        r2 = move-exception;
    L_0x0036:
        closeSilently(r4);	 Catch:{ all -> 0x003a }
        throw r2;	 Catch:{ all -> 0x003a }
    L_0x003a:
        r7 = move-exception;
    L_0x003b:
        if (r4 == 0) goto L_0x0040;
    L_0x003d:
        r4.close();
    L_0x0040:
        r9.mData = r6;
        throw r7;
    L_0x0043:
        r7 = move-exception;
        r4 = r5;
        goto L_0x003b;
    L_0x0046:
        r7 = move-exception;
        r0 = r1;
        r4 = r5;
        goto L_0x003b;
    L_0x004a:
        r2 = move-exception;
        r4 = r5;
        goto L_0x0036;
    L_0x004d:
        r2 = move-exception;
        r0 = r1;
        r4 = r5;
        goto L_0x0036;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.file.ExifInterface.forceRewriteExif(java.lang.String, java.util.Collection):void");
    }

    public void forceRewriteExif(String filename) throws FileNotFoundException, IOException {
        forceRewriteExif(filename, getAllTags());
    }

    public Object getTagValue(int tagId, int ifdId) {
        ExifTag t = getTag(tagId, ifdId);
        return t == null ? null : t.getValue();
    }

    public Object getTagValue(int tagId) {
        return getTagValue(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public String getTagStringValue(int tagId, int ifdId) {
        ExifTag t = getTag(tagId, ifdId);
        if (t == null) {
            return null;
        }
        return t.getValueAsString();
    }

    public String getTagStringValue(int tagId) {
        return getTagStringValue(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public Long getTagLongValue(int tagId, int ifdId) {
        long[] l = getTagLongValues(tagId, ifdId);
        if (l == null || l.length <= 0) {
            return null;
        }
        return Long.valueOf(l[0]);
    }

    public Long getTagLongValue(int tagId) {
        return getTagLongValue(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public Integer getTagIntValue(int tagId, int ifdId) {
        int[] l = getTagIntValues(tagId, ifdId);
        if (l == null || l.length <= 0) {
            return null;
        }
        return Integer.valueOf(l[0]);
    }

    public Integer getTagIntValue(int tagId) {
        return getTagIntValue(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public Byte getTagByteValue(int tagId, int ifdId) {
        byte[] l = getTagByteValues(tagId, ifdId);
        if (l == null || l.length <= 0) {
            return null;
        }
        return Byte.valueOf(l[0]);
    }

    public Byte getTagByteValue(int tagId) {
        return getTagByteValue(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public Rational getTagRationalValue(int tagId, int ifdId) {
        Rational[] l = getTagRationalValues(tagId, ifdId);
        if (l == null || l.length == 0) {
            return null;
        }
        return new Rational(l[0]);
    }

    public Rational getTagRationalValue(int tagId) {
        return getTagRationalValue(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public long[] getTagLongValues(int tagId, int ifdId) {
        ExifTag t = getTag(tagId, ifdId);
        if (t == null) {
            return null;
        }
        return t.getValueAsLongs();
    }

    public long[] getTagLongValues(int tagId) {
        return getTagLongValues(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public int[] getTagIntValues(int tagId, int ifdId) {
        ExifTag t = getTag(tagId, ifdId);
        if (t == null) {
            return null;
        }
        return t.getValueAsInts();
    }

    public int[] getTagIntValues(int tagId) {
        return getTagIntValues(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public byte[] getTagByteValues(int tagId, int ifdId) {
        ExifTag t = getTag(tagId, ifdId);
        if (t == null) {
            return null;
        }
        return t.getValueAsBytes();
    }

    public byte[] getTagByteValues(int tagId) {
        return getTagByteValues(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public Rational[] getTagRationalValues(int tagId, int ifdId) {
        ExifTag t = getTag(tagId, ifdId);
        if (t == null) {
            return null;
        }
        return t.getValueAsRationals();
    }

    public Rational[] getTagRationalValues(int tagId) {
        return getTagRationalValues(tagId, getDefinedTagDefaultIfd(tagId));
    }

    public String getUserComment(short tagId) {
        return this.mData.getUserComment(tagId);
    }

    public static short getOrientationValueForRotation(int degrees) {
        degrees %= 360;
        if (degrees < 0) {
            degrees += 360;
        }
        if (degrees < 90) {
            return (short) 1;
        }
        if (degrees < 180) {
            return (short) 6;
        }
        if (degrees < 270) {
            return (short) 3;
        }
        return (short) 8;
    }

    public static int getRotationForOrientationValue(short orientation) {
        switch (orientation) {
            case (short) 3:
                return 180;
            case (short) 6:
                return 90;
            case (short) 8:
                return 270;
            default:
                return 0;
        }
    }

    public static double convertLatOrLongToDouble(Rational[] coordinate, String reference) {
        try {
            double result = ((coordinate[1].toDouble() / 60.0d) + coordinate[0].toDouble()) + (coordinate[2].toDouble() / 3600.0d);
            if (reference.equals(GpsLatitudeRef.SOUTH) || reference.equals(GpsLongitudeRef.WEST)) {
                return -result;
            }
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }
    }

    public double[] getLatLongAsDoubles() {
        Rational[] latitude = getTagRationalValues(TAG_GPS_LATITUDE);
        String latitudeRef = getTagStringValue(TAG_GPS_LATITUDE_REF);
        Rational[] longitude = getTagRationalValues(TAG_GPS_LONGITUDE);
        String longitudeRef = getTagStringValue(TAG_GPS_LONGITUDE_REF);
        if (latitude == null || longitude == null || latitudeRef == null || longitudeRef == null || latitude.length < 3 || longitude.length < 3) {
            return null;
        }
        return new double[]{convertLatOrLongToDouble(latitude, latitudeRef), convertLatOrLongToDouble(longitude, longitudeRef)};
    }

    public boolean addDateTimeStampTag(int tagId, long timestamp, TimeZone timezone) {
        if (tagId != TAG_DATE_TIME && tagId != TAG_DATE_TIME_DIGITIZED && tagId != TAG_DATE_TIME_ORIGINAL) {
            return false;
        }
        this.mDateTimeStampFormat.setTimeZone(timezone);
        ExifTag t = buildTag(tagId, this.mDateTimeStampFormat.format(Long.valueOf(timestamp)));
        if (t == null) {
            return false;
        }
        setTag(t);
        return true;
    }

    public boolean addGpsTags(double latitude, double longitude) {
        ExifTag latTag = buildTag(TAG_GPS_LATITUDE, toExifLatLong(latitude));
        ExifTag longTag = buildTag(TAG_GPS_LONGITUDE, toExifLatLong(longitude));
        ExifTag latRefTag = buildTag(TAG_GPS_LATITUDE_REF, latitude >= 0.0d ? "N" : GpsLatitudeRef.SOUTH);
        ExifTag longRefTag = buildTag(TAG_GPS_LONGITUDE_REF, longitude >= 0.0d ? GpsLongitudeRef.EAST : GpsLongitudeRef.WEST);
        if (latTag == null || longTag == null || latRefTag == null || longRefTag == null) {
            return false;
        }
        setTag(latTag);
        setTag(longTag);
        setTag(latRefTag);
        setTag(longRefTag);
        return true;
    }

    public boolean addGpsDateTimeStampTag(long timestamp) {
        ExifTag t = buildTag(TAG_GPS_DATE_STAMP, this.mGPSDateStampFormat.format(Long.valueOf(timestamp)));
        if (t == null) {
            return false;
        }
        setTag(t);
        this.mGPSTimeStampCalendar.setTimeInMillis(timestamp);
        t = buildTag(TAG_GPS_TIME_STAMP, new Rational[]{new Rational((long) this.mGPSTimeStampCalendar.get(11), 1), new Rational((long) this.mGPSTimeStampCalendar.get(12), 1), new Rational((long) this.mGPSTimeStampCalendar.get(13), 1)});
        if (t == null) {
            return false;
        }
        setTag(t);
        return true;
    }

    private static Rational[] toExifLatLong(double value) {
        value = Math.abs(value);
        value = (value - ((double) ((int) value))) * 60.0d;
        int seconds = (int) ((value - ((double) ((int) value))) * 6000.0d);
        return new Rational[]{new Rational((long) degrees, 1), new Rational((long) ((int) value), 1), new Rational((long) seconds, 100)};
    }

    private void doExifStreamIO(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[1024];
        int ret = is.read(buf, 0, 1024);
        while (ret != -1) {
            os.write(buf, 0, ret);
            ret = is.read(buf, 0, 1024);
        }
    }

    protected static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable th) {
            }
        }
    }
}
