package com.adobe.xmp.options;

import com.adobe.xmp.XMPException;
import com.arcsoft.stickerlibrary.utils.Constant;

public final class AliasOptions extends Options {
    public static final int PROP_ARRAY = 512;
    public static final int PROP_ARRAY_ALTERNATE = 2048;
    public static final int PROP_ARRAY_ALT_TEXT = 4096;
    public static final int PROP_ARRAY_ORDERED = 1024;
    public static final int PROP_DIRECT = 0;

    public AliasOptions(int options) throws XMPException {
        super(options);
    }

    public boolean isSimple() {
        return getOptions() == 0;
    }

    public boolean isArray() {
        return getOption(512);
    }

    public AliasOptions setArray(boolean value) {
        setOption(512, value);
        return this;
    }

    public boolean isArrayOrdered() {
        return getOption(1024);
    }

    public AliasOptions setArrayOrdered(boolean value) {
        setOption(1536, value);
        return this;
    }

    public boolean isArrayAlternate() {
        return getOption(2048);
    }

    public AliasOptions setArrayAlternate(boolean value) {
        setOption(3584, value);
        return this;
    }

    public boolean isArrayAltText() {
        return getOption(4096);
    }

    public AliasOptions setArrayAltText(boolean value) {
        setOption(Constant.PHOTO_EFFECT_ID_SKETCH, value);
        return this;
    }

    public PropertyOptions toPropertyOptions() throws XMPException {
        return new PropertyOptions(getOptions());
    }

    protected String defineOptionName(int option) {
        switch (option) {
            case 0:
                return "PROP_DIRECT";
            case 512:
                return "ARRAY";
            case 1024:
                return "ARRAY_ORDERED";
            case 2048:
                return "ARRAY_ALTERNATE";
            case 4096:
                return "ARRAY_ALT_TEXT";
            default:
                return null;
        }
    }

    protected int getValidOptions() {
        return Constant.PHOTO_EFFECT_ID_SKETCH;
    }
}
