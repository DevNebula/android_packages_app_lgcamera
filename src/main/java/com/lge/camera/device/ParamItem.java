package com.lge.camera.device;

public class ParamItem {
    protected String mKey = null;
    protected Object mValue = null;

    public ParamItem(String key, Object value) {
        this.mKey = key;
        this.mValue = value;
    }

    public String getKey() {
        return this.mKey;
    }

    public Object getValue() {
        return this.mValue;
    }

    public void setValue(Object value) {
        this.mValue = value;
    }
}
