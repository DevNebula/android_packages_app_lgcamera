package com.lge.gallery.xmp.encoder.model;

import java.io.InputStream;
import java.io.OutputStream;

public interface Model {
    String getData();

    boolean isRaw();

    void readData(InputStream inputStream);

    void writeData(OutputStream outputStream);
}
