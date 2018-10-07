package com.lge.camera.managers.ext.sticker.solutions;

public class ContentsInformation implements Cloneable {
    private static final int TYPE_NONE = 0;
    private static final int TYPE_PICTURE = 1;
    private static final int TYPE_VIDEO = 2;
    private static final int TYPE_VIDEO_ERROR = -1;
    private static final int TYPE_VIDEO_RECORDING_END = 3;
    int mJpepOrientation;
    byte[] mPictureData;
    int[] mPictureSize;
    int mType;
    String mVidedoFileDir;
    long mVideoDuration;
    String mVideoFileFullPath;
    long mVideoFileLength;
    String mVideoFileName;
    String mVideoResolution;

    public ContentsInformation() {
        this.mType = 0;
        this.mPictureSize = new int[2];
        this.mType = -1;
    }

    public ContentsInformation(boolean successRecordViaReached) {
        this.mType = 0;
        this.mPictureSize = new int[2];
        if (successRecordViaReached) {
            this.mType = 3;
        } else {
            this.mType = -1;
        }
    }

    public ContentsInformation(byte[] data, int resW, int resH, int jori) {
        this.mType = 0;
        this.mPictureSize = new int[2];
        this.mType = 1;
        this.mPictureData = (byte[]) data.clone();
        this.mPictureSize[0] = resW;
        this.mPictureSize[1] = resH;
        this.mJpepOrientation = jori;
    }

    public ContentsInformation(String dir, String name, String extention, long length, int resW, int resH, long duration) {
        this.mType = 0;
        this.mPictureSize = new int[2];
        this.mType = 2;
        this.mVidedoFileDir = dir;
        this.mVideoFileFullPath = dir + name + extention;
        this.mVideoFileName = name;
        this.mVideoFileLength = length;
        this.mVideoResolution = String.format("%dx%d", new Object[]{Integer.valueOf(resW), Integer.valueOf(resH)});
        this.mVideoDuration = duration;
    }

    public boolean isPicture() {
        return this.mType == 1;
    }

    public boolean isVideo() {
        return this.mType == 2;
    }

    public boolean isVideoError() {
        return this.mType == -1;
    }

    public byte[] getPictureData() {
        if (this.mType == 1) {
            return this.mPictureData;
        }
        return new byte[1];
    }

    public int[] getPictureSize() {
        if (this.mType == 1) {
            return (int[]) this.mPictureSize.clone();
        }
        return new int[1];
    }

    public String getVideoFileFullPath() {
        if (this.mType == 2) {
            return this.mVideoFileFullPath;
        }
        return "";
    }

    public String getVidedoFileDir() {
        if (this.mType == 2) {
            return this.mVidedoFileDir;
        }
        return "";
    }

    public String getVideoFileName() {
        if (this.mType == 2) {
            return this.mVideoFileName;
        }
        return "";
    }

    public long getVideoFileLength() {
        if (this.mType == 2) {
            return this.mVideoFileLength;
        }
        return -1;
    }

    public String getVideoResolution() {
        if (this.mType == 2) {
            return this.mVideoResolution;
        }
        return "";
    }

    public int getJpepOrientation() {
        if (this.mType == 1) {
            return this.mJpepOrientation;
        }
        return 0;
    }

    public boolean isRecordinEndAndWaitSave() {
        if (this.mType == 3) {
            return true;
        }
        return false;
    }

    public long getVideoDuration() {
        if (this.mType == 2 || this.mType == 3) {
            return this.mVideoDuration;
        }
        return 0;
    }

    public String toString() {
        StringBuilder dump = new StringBuilder();
        dump.append("TYPE = ").append(this.mType);
        if (this.mType == 1) {
            if (this.mPictureData != null) {
                dump.append("PICTURE_DATA = ").append(this.mPictureData.hashCode()).append("\n");
                dump.append("PICTURE_SIZE = ").append(String.format("%dx%d", new Object[]{Integer.valueOf(this.mPictureSize[0]), Integer.valueOf(this.mPictureSize[1])})).append("\n");
                dump.append("PICTURE_JPEG_ORIENTATION = ").append(this.mJpepOrientation).append("\n");
            } else {
                dump.append("PICTUREDATA = null").append("\n");
            }
        } else if (this.mType == 2) {
            dump.append("FILE_LENGHT = ").append(this.mVideoFileLength).append("\n");
            dump.append("DIR = ").append(this.mVidedoFileDir).append("\n");
            dump.append("FILE_NAME = ").append(this.mVideoFileName).append("\n");
            dump.append("FILE_FULL_PATH = ").append(this.mVideoFileFullPath).append("\n");
            dump.append("RESOLUTION = ").append(this.mVideoResolution).append("\n");
            dump.append("DURATION = ").append(this.mVideoDuration).append("\n");
        }
        return dump.toString();
    }

    public ContentsInformation clone() {
        ContentsInformation ci = new ContentsInformation();
        ci.mType = this.mType;
        ci.mVidedoFileDir = this.mVidedoFileDir;
        ci.mVideoFileFullPath = this.mVideoFileFullPath;
        ci.mVideoFileLength = this.mVideoFileLength;
        ci.mVideoResolution = this.mVideoResolution;
        ci.mVideoFileName = this.mVideoFileName;
        ci.mVideoDuration = this.mVideoDuration;
        return ci;
    }
}
