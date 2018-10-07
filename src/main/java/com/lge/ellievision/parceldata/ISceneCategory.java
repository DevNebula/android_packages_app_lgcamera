package com.lge.ellievision.parceldata;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ISceneCategory implements Parcelable {
    public static final String CATEGORY_DISPLAY_ANIMAL = "Animal";
    public static final String CATEGORY_DISPLAY_AUTO = "Auto";
    public static final String CATEGORY_DISPLAY_BABY = "Baby";
    public static final String CATEGORY_DISPLAY_BEACH = "Beach";
    public static final String CATEGORY_DISPLAY_BEVERAGE = "beverage";
    public static final String CATEGORY_DISPLAY_CITY = "City";
    public static final String CATEGORY_DISPLAY_CLOSEUP_FLOWER = "Flower";
    public static final String CATEGORY_DISPLAY_FIREWORK = "Firework";
    public static final String CATEGORY_DISPLAY_FOOD = "Food";
    public static final String CATEGORY_DISPLAY_FRUIT = "Fruit";
    public static final String CATEGORY_DISPLAY_GOP = "People";
    public static final String CATEGORY_DISPLAY_LANDSCAPE = "Landscape";
    public static final String CATEGORY_DISPLAY_LOW_ANGLE = "Low angle";
    public static final String CATEGORY_DISPLAY_LOW_LIGHT = "Low light";
    public static final String CATEGORY_DISPLAY_PET = "Pet";
    public static final String CATEGORY_DISPLAY_PORTRAIT = "Person";
    public static final String CATEGORY_DISPLAY_SILHOUETTE = "Silhouette";
    public static final String CATEGORY_DISPLAY_SKY = "Sky";
    public static final String CATEGORY_DISPLAY_SNOW = "Snow";
    public static final String CATEGORY_DISPLAY_STAR = "Star";
    public static final String CATEGORY_DISPLAY_SUNSET_SUNRISE = "Sunset";
    public static final String CATEGORY_DISPLAY_TEXT = "Text";
    public static final String CATEGORY_ID_ANIMAL = "animal";
    public static final String CATEGORY_ID_AUTO = "auto";
    public static final String CATEGORY_ID_BABY = "baby";
    public static final String CATEGORY_ID_BEACH = "beach";
    public static final String CATEGORY_ID_BEVERAGE = "beverage";
    public static final String CATEGORY_ID_CITY = "city";
    public static final String CATEGORY_ID_CLOSEUP = "close-up";
    public static final String CATEGORY_ID_FIREWORK = "firework";
    public static final String CATEGORY_ID_FLOWER = "flower";
    public static final String CATEGORY_ID_FOOD = "food";
    public static final String CATEGORY_ID_FRUIT = "fruit";
    public static final String CATEGORY_ID_GOP = "group of people";
    public static final String CATEGORY_ID_LAKE = "lake";
    public static final String CATEGORY_ID_LOW_ANGLE = "low angle";
    public static final String CATEGORY_ID_LOW_LIGHT = "low light";
    public static final String CATEGORY_ID_PET = "pet";
    public static final String CATEGORY_ID_PORTRAIT = "portrait";
    public static final String CATEGORY_ID_SEA = "sea";
    public static final String CATEGORY_ID_SILHOUETTE = "silhouette";
    public static final String CATEGORY_ID_SKY = "sky";
    public static final String CATEGORY_ID_SNOW = "snow";
    public static final String CATEGORY_ID_STAR = "star";
    public static final String CATEGORY_ID_SUNSET_SUNRISE = "sunset/sunrise";
    public static final String CATEGORY_ID_TEXT = "text";
    public static final String CATEGORY_NOT_DEFIND = "NotDefind";
    public static final String CATEGORY_UNKNOWN = "Unknown";
    public static final Creator<ISceneCategory> CREATOR = new C14391();
    public String category;
    public String displayName;

    /* renamed from: com.lge.ellievision.parceldata.ISceneCategory$1 */
    static class C14391 implements Creator<ISceneCategory> {
        C14391() {
        }

        public ISceneCategory createFromParcel(Parcel source) {
            ISceneCategory r = new ISceneCategory();
            r.category = source.readString();
            r.displayName = source.readString();
            return r;
        }

        public ISceneCategory[] newArray(int size) {
            return new ISceneCategory[size];
        }
    }

    public ISceneCategory(String category, String displayName) {
        this.category = category;
        this.displayName = displayName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.category);
        dest.writeString(this.displayName);
    }
}
