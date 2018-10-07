package com.lge.camera.database;

public class OverlapProjectDb {
    private long mId;
    private int mListIndex;
    private String mName;
    private int mPreset;
    private String mProjectId;
    private String mSamplePath;
    private String mUri;
    private int mUserSampleCnt;

    public OverlapProjectDb() {
        this.mId = 0;
        this.mProjectId = "";
        this.mName = "";
        this.mUri = "";
        this.mPreset = -1;
        this.mSamplePath = "";
        this.mListIndex = 0;
        this.mUserSampleCnt = 0;
    }

    public OverlapProjectDb(String projectId, String name, String uri, int preset, String samplePath, int listIndex, int usampleCnt) {
        this(0, projectId, name, uri, preset, samplePath, listIndex, usampleCnt);
    }

    public OverlapProjectDb(long id, String projectId, String name, String uri, int preset, String samplePath, int listIndex, int usampleCnt) {
        this.mId = 0;
        this.mProjectId = "";
        this.mName = "";
        this.mUri = "";
        this.mPreset = -1;
        this.mSamplePath = "";
        this.mListIndex = 0;
        this.mUserSampleCnt = 0;
        this.mId = id;
        this.mProjectId = projectId;
        this.mName = name;
        this.mUri = uri;
        this.mPreset = preset;
        this.mSamplePath = samplePath;
        this.mListIndex = listIndex;
        this.mUserSampleCnt = usampleCnt;
    }

    public long getId() {
        return this.mId;
    }

    public String getProjectId() {
        return this.mProjectId;
    }

    public String getName() {
        return this.mName;
    }

    public String getUri() {
        return this.mUri;
    }

    public int getPreset() {
        return this.mPreset;
    }

    public String getSamplePath() {
        return this.mSamplePath;
    }

    public int getListIndex() {
        return this.mListIndex;
    }

    public int getUserSampleCnt() {
        return this.mUserSampleCnt;
    }
}
