package com.lge.camera.util;

import android.util.Log;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.options.SerializeOptions;
import com.lge.camera.constants.CameraConstants;
import java.util.List;

public class XMPWriter {
    private static final String BURST_PRIMARY = "BurstPrimary";
    private static final String GOOGLE_PHOTO_NAMESPACE = "http://ns.google.com/photos/1.0/camera/";
    private static final char MARKER_SOD = '￡';
    private static final char MARKER_SOI = '￘';
    private static final int MAX_XMP_BUFFER_SIZE = 65502;
    private static final String OUTFOCUS_XMP_BLUR_LEVEL = "<LGBehindImage:BlurLevel>%d</LGBehindImage:BlurLevel>";
    private static final String OUTFOCUS_XMP_EXTRA_OFFSET = "<LGBehindImage:NegativeOffset>%s</LGBehindImage:NegativeOffset>";
    private static final String OUTFOCUS_XMP_FOCUS_ROI = "<LGBehindImage:FocusROI>%s</LGBehindImage:FocusROI>";
    private static final String OUTFOCUS_XMP_ORIGINAL_AND_EXTRA_OFFSET = "<LGBehindImage:NegativeOffsetForOrgImage>%s</LGBehindImage:NegativeOffsetForOrgImage>";
    private static final String OUTFOCUS_XMP_SENSOR_ACTIVE_SIZE = "<LGBehindImage:SensorActiveSize>%s</LGBehindImage:SensorActiveSize>";
    private static final String OUTFOCUS_XMP_TAG_INFO = "<LGBehindImage:TagInfo>%s</LGBehindImage:TagInfo>";
    private static final String PROJECT_ID = "ProjectID";
    private static final String XMP_HEADER = "http://ns.adobe.com/xap/1.0/\u0000";
    private static final int XMP_HEADER_SIZE = 29;
    private static final String sDualPopXmpRawText = "http://ns.adobe.com/xap/1.0/\u0000<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"LGXmpEncoder\"><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"><rdf:Description xmlns:LGDualPic=\"http://ns.lge.com/gallery/1.0/dualpicture/\" xmlns:xmpNote=\"http://ns.adobe.com/xmp/note/\" xmlns:LGBehindImage=\"http://ns.lge.com/gallery/1.0/behindimage/\" rdf:about=\"\"><LGDualPic:Version>1.0</LGDualPic:Version><LGBehindImage:CameraAngle>wide</LGBehindImage:CameraAngle><LGBehindImage:Mime>image/jpg</LGBehindImage:Mime><LGBehindImage:Size>%d</LGBehindImage:Size><LGBehindImage:NegativeOffset>%d</LGBehindImage:NegativeOffset></rdf:Description>:colors koehler</rdf:RDF></x:xmpmeta>";
    private static final String sOutfocusXmpRawText_end = "</rdf:Description></rdf:RDF></x:xmpmeta>";
    private static final String sOutfocusXmpRawText_start = "http://ns.adobe.com/xap/1.0/\u0000<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"LGXmpEncoder\"><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"><rdf:Description xmlns:LGOutFocusPic=\"http://ns.lge.com/gallery/1.0/outfocuspicture/\" xmlns:xmpNote=\"http://ns.adobe.com/xmp/note/\" xmlns:LGBehindImage=\"http://ns.lge.com/gallery/1.0/behindimage/\" rdf:about=\"\"><LGOutFocusPic:Version>1.0</LGOutFocusPic:Version><LGBehindImage:Mime>image/jpg</LGBehindImage:Mime>";
    private static final String sXmpRawText = "http://ns.adobe.com/xap/1.0/\u0000<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"LGXmpEncoder\"><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"><rdf:Description xmlns:LGLivePic=\"http://ns.lge.com/gallery/1.0/livepicture/\" xmlns:xmpNote=\"http://ns.adobe.com/xmp/note/\" xmlns:LGBehindVideo=\"http://ns.lge.com/gallery/1.0/behindvideo/\" rdf:about=\"\"><LGLivePic:Version>1.0</LGLivePic:Version><LGBehindVideo:Mime>video/mp4</LGBehindVideo:Mime><LGBehindVideo:NegativeOffset>%d</LGBehindVideo:NegativeOffset><LGBehindVideo:Size>%d</LGBehindVideo:Size><LGBehindVideo:PresentationTimestampUs>%d</LGBehindVideo:PresentationTimestampUs><LGBehindVideo:CameraType>%d</LGBehindVideo:CameraType></rdf:Description></rdf:RDF></x:xmpmeta>";

    public static byte[] InsertXMPData(byte[] jpegData, String uuid, int bestPic) {
        long start = System.currentTimeMillis();
        CamLog.m3d(CameraConstants.TAG, "uuid : " + uuid + " , bestPic : " + bestPic);
        try {
            XMPMetaFactory.getSchemaRegistry().registerNamespace(GOOGLE_PHOTO_NAMESPACE, "GCamera");
        } catch (XMPException e) {
            e.printStackTrace();
        }
        XMPMeta xmpMeta = createXMPMeta();
        try {
            xmpMeta.setProperty(GOOGLE_PHOTO_NAMESPACE, PROJECT_ID, uuid);
            if (bestPic == 1) {
                xmpMeta.setPropertyInteger(GOOGLE_PHOTO_NAMESPACE, BURST_PRIMARY, bestPic);
            }
        } catch (XMPException e2) {
            e2.printStackTrace();
        }
        jpegData = writeXmpToJpeg(jpegData, xmpMeta);
        Log.d(CameraConstants.TAG, "insert process time : " + (System.currentTimeMillis() - start));
        return jpegData;
    }

    private static byte[] writeXmpToJpeg(byte[] jpegData, XMPMeta xmpMeta) {
        byte[] xmpdata = getXMPSection(xmpMeta);
        int origin_size = jpegData.length;
        byte[] olddata = jpegData;
        if (xmpdata == null) {
            return jpegData;
        }
        jpegData = new byte[((xmpdata.length + origin_size) + 4)];
        jpegData[0] = olddata[0];
        int offset = 0 + 1;
        jpegData[offset] = olddata[offset];
        offset++;
        jpegData[offset] = olddata[offset];
        offset++;
        if ((olddata[offset] & 255) != 225) {
            Log.d(CameraConstants.TAG, "wrong point read");
            return olddata;
        }
        jpegData[offset] = olddata[offset];
        offset++;
        int lh = (byte) (olddata[offset] & 255);
        jpegData[offset] = lh;
        if (lh < 0) {
            lh += 256;
        }
        offset++;
        int ll = (byte) (olddata[offset] & 255);
        jpegData[offset] = ll;
        if (ll < 0) {
            ll += 256;
        }
        offset++;
        int length = (lh << 8) | ll;
        System.arraycopy(olddata, offset, jpegData, offset, length - 2);
        offset = (length - 2) + 6;
        int i = offset;
        int i2 = i + 1;
        jpegData[i] = (byte) -1;
        i = i2 + 1;
        jpegData[i2] = (byte) -31;
        int sectionLength = xmpdata.length + 2;
        int sectionlh = sectionLength >> 8;
        int sectionll = sectionLength & 255;
        i2 = i + 1;
        jpegData[i] = (byte) sectionlh;
        i = i2 + 1;
        jpegData[i2] = (byte) sectionll;
        int xmplength = (sectionlh << 8) | sectionll;
        System.arraycopy(xmpdata, 0, jpegData, i, xmpdata.length);
        System.arraycopy(olddata, offset, jpegData, i + (xmplength - 2), olddata.length - offset);
        return jpegData;
    }

    public static XMPMeta createXMPMeta() {
        return XMPMetaFactory.create();
    }

    public static byte[] getXMPSection(XMPMeta meta) {
        try {
            SerializeOptions options = new SerializeOptions();
            options.setUseCompactFormat(true);
            options.setOmitPacketWrapper(true);
            byte[] buffer = XMPMetaFactory.serializeToBuffer(meta, options);
            if (buffer.length > MAX_XMP_BUFFER_SIZE) {
                return null;
            }
            byte[] xmpdata = new byte[(buffer.length + 29)];
            System.arraycopy(XMP_HEADER.getBytes(), 0, xmpdata, 0, 29);
            System.arraycopy(buffer, 0, xmpdata, 29, buffer.length);
            return xmpdata;
        } catch (XMPException e) {
            Log.d(CameraConstants.TAG, "Serialize xmp failed", e);
            return null;
        }
    }

    public static byte[] insertLivePicXMP(byte[] input, long size, long videoPlayTime, int cameraType) {
        int inIdx;
        int outIdx;
        CamLog.m3d(CameraConstants.TAG, String.format("-Live Photo- insertLivePicXMP, size = %d, videoPlayTime = %d", new Object[]{Long.valueOf(size), Long.valueOf(videoPlayTime)}));
        int outIdx2 = 0;
        int i = 0;
        char SOFMarker = byteToChar(input[0], input[1]);
        char SODMarker = byteToChar(input[2], input[3]);
        int exifLength = byteToChar(input[4], input[5]);
        CamLog.m3d(CameraConstants.TAG, "exifLength = " + exifLength);
        byte[] xmpByteData = getXmpString(size, size, videoPlayTime, cameraType).getBytes();
        int xmpLength = xmpByteData.length;
        byte[] output = new byte[((input.length + xmpLength) + 4)];
        int i2 = 0;
        while (true) {
            inIdx = i;
            outIdx = outIdx2;
            if (i2 >= exifLength + 4) {
                break;
            }
            outIdx2 = outIdx + 1;
            i = inIdx + 1;
            output[outIdx] = input[inIdx];
            i2++;
        }
        writeChar(output, SODMarker, outIdx);
        outIdx2 = outIdx + 2;
        writeChar(output, (char) (xmpLength + 2), outIdx2);
        outIdx2 += 2;
        i2 = 0;
        while (true) {
            outIdx = outIdx2;
            if (i2 >= xmpLength) {
                break;
            }
            outIdx2 = outIdx + 1;
            output[outIdx] = xmpByteData[i2];
            i2++;
        }
        while (true) {
            i = inIdx;
            outIdx2 = outIdx;
            if (outIdx2 >= output.length) {
                return output;
            }
            outIdx = outIdx2 + 1;
            inIdx = i + 1;
            output[outIdx2] = input[i];
        }
    }

    public static byte[] insertDualPopXMP(byte[] input, long size) {
        int inIdx;
        int outIdx;
        int outIdx2 = 0;
        int i = 0;
        char SOFMarker = byteToChar(input[0], input[1]);
        char SODMarker = byteToChar(input[2], input[3]);
        int exifLength = byteToChar(input[4], input[5]);
        CamLog.m3d(CameraConstants.TAG, "exifLength = " + exifLength);
        byte[] xmpByteData = getDualPopXmpString(size, size).getBytes();
        int xmpLength = xmpByteData.length;
        byte[] output = new byte[((input.length + xmpLength) + 4)];
        int i2 = 0;
        while (true) {
            inIdx = i;
            outIdx = outIdx2;
            if (i2 >= exifLength + 4) {
                break;
            }
            outIdx2 = outIdx + 1;
            i = inIdx + 1;
            output[outIdx] = input[inIdx];
            i2++;
        }
        writeChar(output, SODMarker, outIdx);
        outIdx2 = outIdx + 2;
        writeChar(output, (char) (xmpLength + 2), outIdx2);
        i2 = 0;
        outIdx = outIdx2 + 2;
        while (i2 < xmpLength) {
            outIdx2 = outIdx + 1;
            output[outIdx] = xmpByteData[i2];
            i2++;
            outIdx = outIdx2;
        }
        while (true) {
            i = inIdx;
            outIdx2 = outIdx;
            if (outIdx2 >= output.length) {
                return output;
            }
            outIdx = outIdx2 + 1;
            inIdx = i + 1;
            output[outIdx2] = input[i];
        }
    }

    public static byte[] insertOutfocusXMP(byte[] input, String xmpData) {
        int inIdx;
        int outIdx;
        int outIdx2 = 0;
        int i = 0;
        char SOFMarker = byteToChar(input[0], input[1]);
        char SODMarker = byteToChar(input[2], input[3]);
        int exifLength = byteToChar(input[4], input[5]);
        CamLog.m3d(CameraConstants.TAG, "exifLength = " + exifLength);
        byte[] xmpByteData = xmpData.getBytes();
        int xmpLength = xmpByteData.length;
        byte[] output = new byte[((input.length + xmpLength) + 4)];
        int i2 = 0;
        while (true) {
            inIdx = i;
            outIdx = outIdx2;
            if (i2 >= exifLength + 4) {
                break;
            }
            outIdx2 = outIdx + 1;
            i = inIdx + 1;
            output[outIdx] = input[inIdx];
            i2++;
        }
        writeChar(output, SODMarker, outIdx);
        outIdx2 = outIdx + 2;
        writeChar(output, (char) (xmpLength + 2), outIdx2);
        i2 = 0;
        outIdx = outIdx2 + 2;
        while (i2 < xmpLength) {
            outIdx2 = outIdx + 1;
            output[outIdx] = xmpByteData[i2];
            i2++;
            outIdx = outIdx2;
        }
        while (true) {
            i = inIdx;
            outIdx2 = outIdx;
            if (outIdx2 >= output.length) {
                return output;
            }
            outIdx = outIdx2 + 1;
            inIdx = i + 1;
            output[outIdx2] = input[i];
        }
    }

    private static void writeChar(byte[] output, char input, int offset) {
        output[offset] = (byte) ((65280 & input) >> 8);
        output[offset + 1] = (byte) (input & 255);
    }

    private static char byteToChar(byte input1, byte input2) {
        return (char) (((char) (input2 & 255)) | ((char) ((input1 & 255) << 8)));
    }

    private static String getDualPopXmpString(long negativeOffset, long size) {
        return String.format(sDualPopXmpRawText, new Object[]{Long.valueOf(negativeOffset), Long.valueOf(size)});
    }

    private static String getXmpString(long negativeOffset, long size, long videoPlayTime, int cameraType) {
        return String.format(sXmpRawText, new Object[]{Long.valueOf(negativeOffset), Long.valueOf(size), Long.valueOf(videoPlayTime), Integer.valueOf(cameraType)});
    }

    public static String getOutfousXmpString(List<String> itemList, int level) {
        String xmp = sOutfocusXmpRawText_start + String.format(OUTFOCUS_XMP_TAG_INFO, new Object[]{itemList.get(1)}) + String.format(OUTFOCUS_XMP_EXTRA_OFFSET, new Object[]{itemList.get(2)}) + String.format(OUTFOCUS_XMP_ORIGINAL_AND_EXTRA_OFFSET, new Object[]{itemList.get(3)}) + String.format(OUTFOCUS_XMP_BLUR_LEVEL, new Object[]{Integer.valueOf(level)}) + sOutfocusXmpRawText_end;
        CamLog.m3d(CameraConstants.TAG, "[outfocus] xmp : " + xmp);
        return xmp;
    }

    public static String getOutfousXmpString(int tagInfo, int extraOffset, int originalAndExtraOffset, int level, String focusROI, String sensorActiveSize) {
        String xmp = sOutfocusXmpRawText_start + String.format(OUTFOCUS_XMP_TAG_INFO, new Object[]{Integer.valueOf(tagInfo)}) + String.format(OUTFOCUS_XMP_EXTRA_OFFSET, new Object[]{Integer.valueOf(extraOffset)}) + String.format(OUTFOCUS_XMP_ORIGINAL_AND_EXTRA_OFFSET, new Object[]{Integer.valueOf(originalAndExtraOffset)}) + String.format(OUTFOCUS_XMP_BLUR_LEVEL, new Object[]{Integer.valueOf(level)}) + String.format(OUTFOCUS_XMP_FOCUS_ROI, new Object[]{focusROI}) + String.format(OUTFOCUS_XMP_SENSOR_ACTIVE_SIZE, new Object[]{sensorActiveSize}) + sOutfocusXmpRawText_end;
        CamLog.m3d(CameraConstants.TAG, "[outfocus] xmp : " + xmp);
        return xmp;
    }
}
