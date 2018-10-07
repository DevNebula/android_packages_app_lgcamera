package com.lge.gallery.xmp.encoder;

public class XmpDataWrapper {
    public final byte[] data;
    public final boolean isExtension;

    public XmpDataWrapper(byte[] array, boolean isExt) {
        this.data = array;
        this.isExtension = isExt;
    }
}
