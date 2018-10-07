package com.lge.ellievision.parceldata;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.ByteArrayOutputStream;

public class IBitmap implements Parcelable {
    public static final Creator<IBitmap> CREATOR = new C14351();
    byte[] imageByte;

    /* renamed from: com.lge.ellievision.parceldata.IBitmap$1 */
    static class C14351 implements Creator<IBitmap> {
        C14351() {
        }

        public IBitmap createFromParcel(Parcel in) {
            byte[] buffer = new byte[in.readInt()];
            in.readByteArray(buffer);
            return new IBitmap().setImage(buffer);
        }

        public IBitmap[] newArray(int size) {
            return new IBitmap[size];
        }
    }

    public IBitmap setImage(Bitmap _bitmap) {
        this.imageByte = bitmapToByteArray(_bitmap);
        return this;
    }

    public IBitmap setImage(byte[] _imageByte) {
        this.imageByte = _imageByte;
        return this;
    }

    public Bitmap getIcon() {
        return byteArrayToBitmap(this.imageByte);
    }

    public byte[] getByteStream() {
        return this.imageByte;
    }

    public byte[] bitmapToByteArray(Bitmap _bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        _bitmap.compress(CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public Bitmap byteArrayToBitmap(byte[] _byteArray) {
        return BitmapFactory.decodeByteArray(_byteArray, 0, _byteArray.length);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.imageByte.length);
        dest.writeByteArray(this.imageByte);
    }
}
