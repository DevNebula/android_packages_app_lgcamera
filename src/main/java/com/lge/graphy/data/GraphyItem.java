package com.lge.graphy.data;

import android.content.Context;
import android.graphics.Bitmap;
import com.lge.camera.C0088R;

public abstract class GraphyItem {
    public static final int BEST_CATEGORY = 1;
    public static final String CATEGORY_CAFE = "004";
    public static final String CATEGORY_CAT = "006";
    public static final String CATEGORY_DAYBREAK = "013";
    public static final String CATEGORY_DOG = "007";
    public static final String CATEGORY_FIREWORKS = "002";
    public static final String CATEGORY_FOOD = "009";
    public static final String CATEGORY_LIGHTING = "011";
    public static final String CATEGORY_MOTIONBLUR = "012";
    public static final String CATEGORY_NIGHTFALL = "014";
    public static final String CATEGORY_NIGHTSKY = "005";
    public static final String CATEGORY_NIGHTVIEW = "001";
    public static final String CATEGORY_PEOPLE = "008";
    public static final String CATEGORY_SCENERY = "015";
    public static final String CATEGORY_SNAPSHOT = "003";
    public static final String CATEGORY_STREET = "010";
    public static final int GRAPHY_CATEGORY = 3;
    public static final String KEY_ANGLE = "_angle";
    public static final String KEY_APERTURE = "_aperture";
    public static final String KEY_CATEGORY_ID_INT = "_category_id";
    public static final String KEY_CATEGORY_NAME_STR = "_category_name";
    public static final String KEY_CATEGORY_PREFIX = "_category_prefix";
    public static final String KEY_ID_INT = "_id";
    public static final String KEY_ILLUMINANCE = "illuminance";
    public static final String KEY_IMAGE_NAME_STR = "_image_name";
    public static final String KEY_IMAGE_PATH_STR = "_image_path";
    public static final String KEY_ISO_STR = "_iso";
    public static final String KEY_LUX = "_Graphy_lux";
    public static final String KEY_MODEL = "_model";
    public static final String KEY_RESOURCE_ID_INT = "_res_id";
    public static final String KEY_SHUTTER_SPEED_STR = "_shutter_speed";
    public static final String KEY_SPREAD = "_spread";
    public static final String KEY_WB_STR = "_wb";
    public static final String KEY_WB_TYPE_STR = "_wb_type";
    public static final String LUX1 = "001";
    public static final String LUX2 = "002";
    public static final String LUX3 = "003";
    public static final String LUX4 = "004";
    public static final String LUX5 = "005";
    public static final String LUX6 = "006";
    public static final String LUX7 = "007";
    public static final int MY_FILTER_CATEGORY = 2;
    public static final int NONE_CATEGORY = 0;
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_GRAPHY = 2;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_NONE = 3;
    public static final String WB_TYPE_AUTO = "auto";
    public static final String WB_TYPE_MANUAL = "manual";
    protected Context mContext = null;
    public boolean mIsSelected = false;
    protected int mType = 0;

    public abstract Bitmap getBitmap();

    public abstract double getDoubleValue(String str);

    public abstract int getIntValue(String str);

    public abstract String getStringValue(String str);

    public abstract void setDoubleValue(String str, double d);

    public abstract void setIntValue(String str, int i);

    public abstract void setStringValue(String str, String str2);

    public GraphyItem(Context context) {
        this.mContext = context;
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public void setSelected(boolean isSelected) {
        this.mIsSelected = isSelected;
    }

    public boolean getSelected() {
        return this.mIsSelected;
    }

    public String getCategory(String prefix) {
        if (this.mContext == null || prefix == null) {
            return "Scenery";
        }
        Object obj = -1;
        switch (prefix.hashCode()) {
            case 47665:
                if (prefix.equals("001")) {
                    obj = null;
                    break;
                }
                break;
            case 47666:
                if (prefix.equals("002")) {
                    obj = 1;
                    break;
                }
                break;
            case 47667:
                if (prefix.equals("003")) {
                    obj = 2;
                    break;
                }
                break;
            case 47668:
                if (prefix.equals("004")) {
                    obj = 3;
                    break;
                }
                break;
            case 47669:
                if (prefix.equals("005")) {
                    obj = 4;
                    break;
                }
                break;
            case 47670:
                if (prefix.equals("006")) {
                    obj = 5;
                    break;
                }
                break;
            case 47671:
                if (prefix.equals("007")) {
                    obj = 6;
                    break;
                }
                break;
            case 47672:
                if (prefix.equals(CATEGORY_PEOPLE)) {
                    obj = 7;
                    break;
                }
                break;
            case 47673:
                if (prefix.equals(CATEGORY_FOOD)) {
                    obj = 8;
                    break;
                }
                break;
            case 47695:
                if (prefix.equals(CATEGORY_STREET)) {
                    obj = 9;
                    break;
                }
                break;
            case 47696:
                if (prefix.equals(CATEGORY_LIGHTING)) {
                    obj = 10;
                    break;
                }
                break;
            case 47697:
                if (prefix.equals(CATEGORY_MOTIONBLUR)) {
                    obj = 11;
                    break;
                }
                break;
            case 47698:
                if (prefix.equals(CATEGORY_DAYBREAK)) {
                    obj = 12;
                    break;
                }
                break;
            case 47699:
                if (prefix.equals(CATEGORY_NIGHTFALL)) {
                    obj = 13;
                    break;
                }
                break;
            case 47700:
                if (prefix.equals(CATEGORY_SCENERY)) {
                    obj = 14;
                    break;
                }
                break;
        }
        switch (obj) {
            case null:
                return this.mContext.getString(C0088R.string.graphy_category_night_view);
            case 1:
                return this.mContext.getString(C0088R.string.graphy_category_fireworks);
            case 2:
                return this.mContext.getString(C0088R.string.graphy_category_snapshot);
            case 3:
                return this.mContext.getString(C0088R.string.graphy_category_cafe);
            case 4:
                return this.mContext.getString(C0088R.string.graphy_category_night_sky);
            case 5:
                return this.mContext.getString(C0088R.string.graphy_category_cat);
            case 6:
                return this.mContext.getString(C0088R.string.graphy_category_dog);
            case 7:
                return this.mContext.getString(C0088R.string.graphy_category_people);
            case 8:
                return this.mContext.getString(C0088R.string.graphy_category_food);
            case 9:
                return this.mContext.getString(C0088R.string.graphy_category_street);
            case 10:
                return this.mContext.getString(C0088R.string.graphy_category_lighting);
            case 11:
                return this.mContext.getString(C0088R.string.graphy_category_motion_blur);
            case 12:
                return this.mContext.getString(C0088R.string.graphy_category_daybreak);
            case 13:
                return this.mContext.getString(C0088R.string.graphy_category_night_fall);
            default:
                return this.mContext.getString(C0088R.string.graphy_category_scenery);
        }
    }

    public int getGraphyLux(java.lang.String r7) {
        /*
        r6 = this;
        r4 = 4;
        r3 = 3;
        r2 = 2;
        r1 = 1;
        r0 = -1;
        if (r7 != 0) goto L_0x0008;
    L_0x0007:
        return r0;
    L_0x0008:
        r5 = r7.hashCode();
        switch(r5) {
            case 47665: goto L_0x0016;
            case 47666: goto L_0x0020;
            case 47667: goto L_0x002a;
            case 47668: goto L_0x0034;
            case 47669: goto L_0x003e;
            case 47670: goto L_0x0048;
            case 47671: goto L_0x0052;
            default: goto L_0x000f;
        };
    L_0x000f:
        r5 = r0;
    L_0x0010:
        switch(r5) {
            case 0: goto L_0x0014;
            case 1: goto L_0x005c;
            case 2: goto L_0x005e;
            case 3: goto L_0x0060;
            case 4: goto L_0x0062;
            case 5: goto L_0x0064;
            case 6: goto L_0x0066;
            default: goto L_0x0013;
        };
    L_0x0013:
        goto L_0x0007;
    L_0x0014:
        r0 = r1;
        goto L_0x0007;
    L_0x0016:
        r5 = "001";
        r5 = r7.equals(r5);
        if (r5 == 0) goto L_0x000f;
    L_0x001e:
        r5 = 0;
        goto L_0x0010;
    L_0x0020:
        r5 = "002";
        r5 = r7.equals(r5);
        if (r5 == 0) goto L_0x000f;
    L_0x0028:
        r5 = r1;
        goto L_0x0010;
    L_0x002a:
        r5 = "003";
        r5 = r7.equals(r5);
        if (r5 == 0) goto L_0x000f;
    L_0x0032:
        r5 = r2;
        goto L_0x0010;
    L_0x0034:
        r5 = "004";
        r5 = r7.equals(r5);
        if (r5 == 0) goto L_0x000f;
    L_0x003c:
        r5 = r3;
        goto L_0x0010;
    L_0x003e:
        r5 = "005";
        r5 = r7.equals(r5);
        if (r5 == 0) goto L_0x000f;
    L_0x0046:
        r5 = r4;
        goto L_0x0010;
    L_0x0048:
        r5 = "006";
        r5 = r7.equals(r5);
        if (r5 == 0) goto L_0x000f;
    L_0x0050:
        r5 = 5;
        goto L_0x0010;
    L_0x0052:
        r5 = "007";
        r5 = r7.equals(r5);
        if (r5 == 0) goto L_0x000f;
    L_0x005a:
        r5 = 6;
        goto L_0x0010;
    L_0x005c:
        r0 = r2;
        goto L_0x0007;
    L_0x005e:
        r0 = r3;
        goto L_0x0007;
    L_0x0060:
        r0 = r4;
        goto L_0x0007;
    L_0x0062:
        r0 = 5;
        goto L_0x0007;
    L_0x0064:
        r0 = 6;
        goto L_0x0007;
    L_0x0066:
        r0 = 7;
        goto L_0x0007;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.graphy.data.GraphyItem.getGraphyLux(java.lang.String):int");
    }
}
