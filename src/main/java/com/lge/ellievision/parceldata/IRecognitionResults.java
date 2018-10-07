package com.lge.ellievision.parceldata;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.List;

public class IRecognitionResults implements Parcelable {
    public static final Creator<IRecognitionResults> CREATOR = new C14381();
    private List<String> folder;
    private List<IRecognition> iRecognition;

    /* renamed from: com.lge.ellievision.parceldata.IRecognitionResults$1 */
    static class C14381 implements Creator<IRecognitionResults> {
        C14381() {
        }

        public IRecognitionResults createFromParcel(Parcel in) {
            return new IRecognitionResults(in);
        }

        public IRecognitionResults[] newArray(int size) {
            return new IRecognitionResults[size];
        }
    }

    public IRecognitionResults(List<IRecognition> iRecognition, List<String> folder) {
        this.iRecognition = iRecognition;
        this.folder = folder;
    }

    protected IRecognitionResults(Parcel in) {
        this.iRecognition = in.createTypedArrayList(IRecognition.CREATOR);
        this.folder = in.createStringArrayList();
    }

    public List<IRecognition> getIRecognitions() {
        return this.iRecognition;
    }

    public List<String> getFolders() {
        return this.folder;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.iRecognition);
        dest.writeStringList(this.folder);
    }
}
