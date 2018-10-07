package com.google.zxing.integration.android;

public final class IntentResult {
    private final String contents;
    private final String errorCorrectionLevel;
    private final String formatName;
    private final Integer orientation;
    private final byte[] rawBytes;

    IntentResult() {
        this(null, null, null, null, null);
    }

    IntentResult(String contents, String formatName, byte[] rawBytes, Integer orientation, String errorCorrectionLevel) {
        this.contents = contents;
        this.formatName = formatName;
        this.rawBytes = rawBytes;
        this.orientation = orientation;
        this.errorCorrectionLevel = errorCorrectionLevel;
    }

    public String getContents() {
        return this.contents;
    }

    public String getFormatName() {
        return this.formatName;
    }

    public byte[] getRawBytes() {
        return this.rawBytes;
    }

    public Integer getOrientation() {
        return this.orientation;
    }

    public String getErrorCorrectionLevel() {
        return this.errorCorrectionLevel;
    }

    public String toString() {
        return "Format: " + this.formatName + 10 + "Contents: " + this.contents + 10 + "Raw bytes: (" + (this.rawBytes == null ? 0 : this.rawBytes.length) + " bytes)\nOrientation: " + this.orientation + 10 + "EC level: " + this.errorCorrectionLevel + 10;
    }
}
