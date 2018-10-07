package com.lge.camera.file;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

class ExifReader {
    private static final String TAG = "ExifReader";

    ExifReader() {
    }

    protected ExifData read(InputStream inputStream, ExifBridge iRef) throws ExifInvalidFormatException, IOException {
        ExifParser parser = ExifParser.parse(inputStream, iRef);
        ExifData exifData = new ExifData(parser.getByteOrder(), iRef);
        for (int event = parser.next(); event != 5; event = parser.next()) {
            ExifTag tag;
            IfdData ifdData;
            byte[] buf;
            switch (event) {
                case 0:
                    exifData.addIfdData(new IfdData(parser.getCurrentIfd(), iRef));
                    break;
                case 1:
                    tag = parser.getTag();
                    if (tag != null) {
                        if (!tag.hasValue()) {
                            parser.registerForTagValue(tag);
                            break;
                        }
                        ifdData = exifData.getIfdData(tag.getIfd());
                        if (ifdData == null) {
                            break;
                        }
                        ifdData.setTag(tag);
                        break;
                    }
                    break;
                case 2:
                    tag = parser.getTag();
                    if (tag != null) {
                        if (tag.getDataType() == (short) 7) {
                            parser.readFullTagValue(tag);
                        }
                        ifdData = exifData.getIfdData(tag.getIfd());
                        if (ifdData == null) {
                            break;
                        }
                        ifdData.setTag(tag);
                        break;
                    }
                    break;
                case 3:
                    buf = new byte[parser.getCompressedImageSize()];
                    if (buf.length != parser.read(buf)) {
                        Log.w(TAG, "Failed to read the compressed thumbnail");
                        break;
                    }
                    exifData.setCompressedThumbnail(buf);
                    break;
                case 4:
                    buf = new byte[parser.getStripSize()];
                    if (buf.length != parser.read(buf)) {
                        Log.w(TAG, "Failed to read the strip bytes");
                        break;
                    }
                    exifData.setStripBytes(parser.getStripIndex(), buf);
                    break;
                default:
                    break;
            }
        }
        return exifData;
    }
}
