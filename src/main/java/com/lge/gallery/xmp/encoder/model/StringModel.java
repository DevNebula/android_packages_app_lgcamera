package com.lge.gallery.xmp.encoder.model;

import java.io.InputStream;
import java.io.OutputStream;

public class StringModel implements Model {
    private final String mValue;

    public StringModel(String val) {
        this.mValue = val;
    }

    public String getData() {
        return this.mValue;
    }

    public void readData(InputStream is) {
    }

    public void writeData(OutputStream os) {
    }

    public boolean isRaw() {
        return false;
    }
}
