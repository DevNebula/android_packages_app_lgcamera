package com.lge.camera.file;

public class ExifInvalidFormatException extends Exception {
    private static final long serialVersionUID = 1;

    public ExifInvalidFormatException(String meg) {
        super(meg);
    }
}
