package com.lge.gallery.xmp.encoder;

import java.io.OutputStream;

public interface XmpWriter {
    boolean write(OutputStream outputStream, XmpMetadata xmpMetadata);
}
