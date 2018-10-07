package com.lge.ellievision.parceldata;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IProduct implements Parcelable {
    public static final Creator<IProduct> CREATOR = new C14361();
    public String name;
    public String price;
    public String url;

    /* renamed from: com.lge.ellievision.parceldata.IProduct$1 */
    static class C14361 implements Creator<IProduct> {
        C14361() {
        }

        public IProduct createFromParcel(Parcel source) {
            IProduct p = new IProduct();
            p.name = source.readString();
            p.price = source.readString();
            p.url = source.readString();
            return p;
        }

        public IProduct[] newArray(int size) {
            return new IProduct[size];
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.price);
        dest.writeString(this.url);
    }
}
