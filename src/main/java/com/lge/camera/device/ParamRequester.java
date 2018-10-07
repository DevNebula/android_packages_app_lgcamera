package com.lge.camera.device;

public class ParamRequester {
    private boolean mEnable;
    private String mFixedValue = ParamConstants.USE_CUR_VALUE;
    private String mKey;
    private boolean mUpdateUi;

    public ParamRequester(String key, String fixedValue, boolean updateUi, boolean enable) {
        this.mKey = key;
        this.mFixedValue = fixedValue;
        this.mUpdateUi = updateUi;
        this.mEnable = enable;
    }

    public String getKey() {
        return this.mKey;
    }

    public String getFixedValue() {
        return this.mFixedValue;
    }

    public void setFixedValue(String value) {
        this.mFixedValue = value;
    }

    public boolean isUpdateUi() {
        return this.mUpdateUi;
    }

    public void setUpdateUi(boolean set) {
        this.mUpdateUi = set;
    }

    public boolean isEnable() {
        return this.mEnable;
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
    }
}
