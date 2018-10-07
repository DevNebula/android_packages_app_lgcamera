package com.lge.camera.file;

public class SaveRequest {
    public int countBurstshot = 0;
    public byte[] data = null;
    public long dateTaken = 0;
    public int degree = 0;
    public byte[] extraExif = null;
    public int queueCount = 0;
    public boolean updateThumbnail = false;

    public void unbind() {
        this.data = null;
        this.dateTaken = 0;
        this.degree = 0;
        this.updateThumbnail = false;
        this.queueCount = 0;
        this.countBurstshot = 0;
    }
}
