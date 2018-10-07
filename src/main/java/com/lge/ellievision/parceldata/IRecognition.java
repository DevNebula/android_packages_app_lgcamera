package com.lge.ellievision.parceldata;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IRecognition implements Parcelable {
    public static final Creator<IRecognition> CREATOR = new C14371();
    private final Float confidence;
    /* renamed from: id */
    private final String f40id;
    private final RectF location;
    private final String title;

    /* renamed from: com.lge.ellievision.parceldata.IRecognition$1 */
    static class C14371 implements Creator<IRecognition> {
        C14371() {
        }

        public IRecognition createFromParcel(Parcel in) {
            return new IRecognition(in);
        }

        public IRecognition[] newArray(int size) {
            return new IRecognition[size];
        }
    }

    public IRecognition(String id, String title, Float confidence, RectF location) {
        this.f40id = id;
        this.title = title;
        this.confidence = confidence;
        this.location = new RectF(location);
    }

    protected IRecognition(Parcel in) {
        this.f40id = in.readString();
        this.title = in.readString();
        this.confidence = Float.valueOf(in.readFloat());
        this.location = (RectF) in.readParcelable(RectF.class.getClassLoader());
    }

    public String getId() {
        return this.f40id;
    }

    public String getTitle() {
        return this.title;
    }

    public float getConfidence() {
        return this.confidence.floatValue();
    }

    public RectF getLocation() {
        return this.location;
    }

    public String toString() {
        String resultString = "";
        if (this.f40id != null) {
            resultString = resultString + "[" + this.f40id + "] ";
        }
        if (this.title != null) {
            resultString = resultString + this.title + " ";
        }
        if (this.confidence != null) {
            resultString = resultString + String.format("(%.1f%%) ", new Object[]{Float.valueOf(this.confidence.floatValue() * 100.0f)}) + " ";
        }
        if (this.location != null) {
            resultString = resultString + "rect [" + this.location.left + ", " + this.location.top + ", " + this.location.right + ", " + this.location.bottom + "]";
        }
        return resultString.trim();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.f40id);
        dest.writeString(this.title);
        dest.writeFloat(this.confidence.floatValue());
        dest.writeParcelable(this.location, 0);
    }
}
