package com.lge.camera.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.lge.camera.C0088R;

public abstract class CameraPreference {
    private Context mContext;
    private String mSharedPreferenceName;
    private SharedPreferences mSharedPreferences;

    public abstract void reloadValue();

    public CameraPreference(Context context, AttributeSet attrs) {
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, C0088R.styleable.CameraPreference, 0, 0);
        this.mSharedPreferenceName = a.getString(0);
        a.recycle();
    }

    public CameraPreference(Context context, String prefName) {
        this.mContext = context;
        this.mSharedPreferenceName = prefName;
    }

    public void setSharedPreferenceName(String name) {
        this.mSharedPreferenceName = name;
    }

    public String getSharedPreferenceName() {
        return this.mSharedPreferenceName;
    }

    public SharedPreferences getSharedPreferences() {
        if (this.mSharedPreferences == null) {
            this.mSharedPreferences = this.mContext.getSharedPreferences(this.mSharedPreferenceName, 0);
        }
        return this.mSharedPreferences;
    }

    public void close() {
        this.mContext = null;
        this.mSharedPreferences = null;
    }
}
