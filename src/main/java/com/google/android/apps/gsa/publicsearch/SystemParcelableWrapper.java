package com.google.android.apps.gsa.publicsearch;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class SystemParcelableWrapper implements Parcelable {
    public static final Creator<SystemParcelableWrapper> CREATOR = new C15981();
    private final Parcelable parcelable;

    /* renamed from: com.google.android.apps.gsa.publicsearch.SystemParcelableWrapper$1 */
    class C15981 implements Creator<SystemParcelableWrapper> {
        C15981() {
        }

        public SystemParcelableWrapper createFromParcel(Parcel source) {
            return new SystemParcelableWrapper(source.readParcelable(null));
        }

        public SystemParcelableWrapper[] newArray(int size) {
            return new SystemParcelableWrapper[size];
        }
    }

    public SystemParcelableWrapper(Parcelable parcelable) {
        if (isSystemParcelable(parcelable)) {
            this.parcelable = parcelable;
            return;
        }
        throw new IllegalArgumentException("Only Android system classes can be passed in SystemParcelableWrapper.");
    }

    public Parcelable getParcelable() {
        return this.parcelable;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.parcelable, flags);
    }

    static boolean isSystemParcelable(Parcelable parcelable) {
        return parcelable.getClass().getName().startsWith("android.os.");
    }
}
