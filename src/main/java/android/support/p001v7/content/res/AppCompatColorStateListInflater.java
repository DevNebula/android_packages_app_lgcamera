package android.support.p001v7.content.res;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.p000v4.graphics.ColorUtils;
import android.support.p001v7.appcompat.C0082R;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.Xml;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* renamed from: android.support.v7.content.res.AppCompatColorStateListInflater */
final class AppCompatColorStateListInflater {
    private static final int DEFAULT_COLOR = -65536;

    private AppCompatColorStateListInflater() {
    }

    @NonNull
    public static ColorStateList createFromXml(@NonNull Resources r, @NonNull XmlPullParser parser, @Nullable Theme theme) throws XmlPullParserException, IOException {
        int type;
        AttributeSet attrs = Xml.asAttributeSet(parser);
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type == 2) {
            return AppCompatColorStateListInflater.createFromXmlInner(r, parser, attrs, theme);
        }
        throw new XmlPullParserException("No start tag found");
    }

    @NonNull
    private static ColorStateList createFromXmlInner(@NonNull Resources r, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Theme theme) throws XmlPullParserException, IOException {
        String name = parser.getName();
        if (name.equals("selector")) {
            return AppCompatColorStateListInflater.inflate(r, parser, attrs, theme);
        }
        throw new XmlPullParserException(parser.getPositionDescription() + ": invalid color state list tag " + name);
    }

    private static ColorStateList inflate(@NonNull Resources r, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Theme theme) throws XmlPullParserException, IOException {
        int innerDepth = parser.getDepth() + 1;
        int[][] stateSpecList = new int[20][];
        int[] colorList = new int[stateSpecList.length];
        int listSize = 0;
        while (true) {
            int type = parser.next();
            if (type == 1) {
                break;
            }
            int depth = parser.getDepth();
            if (depth < innerDepth && type == 3) {
                break;
            } else if (type == 2 && depth <= innerDepth && parser.getName().equals("item")) {
                int j;
                TypedArray a = AppCompatColorStateListInflater.obtainAttributes(r, theme, attrs, C0082R.styleable.ColorStateListItem);
                int baseColor = a.getColor(C0082R.styleable.ColorStateListItem_android_color, -65281);
                float alphaMod = 1.0f;
                if (a.hasValue(C0082R.styleable.ColorStateListItem_android_alpha)) {
                    alphaMod = a.getFloat(C0082R.styleable.ColorStateListItem_android_alpha, 1.0f);
                } else if (a.hasValue(C0082R.styleable.ColorStateListItem_alpha)) {
                    alphaMod = a.getFloat(C0082R.styleable.ColorStateListItem_alpha, 1.0f);
                }
                a.recycle();
                int j2 = 0;
                int numAttrs = attrs.getAttributeCount();
                int[] stateSpec = new int[numAttrs];
                int i = 0;
                while (true) {
                    j = j2;
                    if (i >= numAttrs) {
                        break;
                    }
                    int stateResId = attrs.getAttributeNameResource(i);
                    if (stateResId == 16843173 || stateResId == 16843551 || stateResId == C0082R.attr.alpha) {
                        j2 = j;
                    } else {
                        j2 = j + 1;
                        if (!attrs.getAttributeBooleanValue(i, false)) {
                            stateResId = -stateResId;
                        }
                        stateSpec[j] = stateResId;
                    }
                    i++;
                }
                Object stateSpec2 = StateSet.trimStateSet(stateSpec, j);
                int color = AppCompatColorStateListInflater.modulateColorAlpha(baseColor, alphaMod);
                if (listSize == 0 || stateSpec2.length == 0) {
                    int defaultColor = color;
                }
                colorList = GrowingArrayUtils.append(colorList, listSize, color);
                stateSpecList = (int[][]) GrowingArrayUtils.append((Object[]) stateSpecList, listSize, stateSpec2);
                listSize++;
            }
        }
        int[] colors = new int[listSize];
        Object stateSpecs = new int[listSize][];
        System.arraycopy(colorList, 0, colors, 0, listSize);
        System.arraycopy(stateSpecList, 0, stateSpecs, 0, listSize);
        return new ColorStateList(stateSpecs, colors);
    }

    private static TypedArray obtainAttributes(Resources res, Theme theme, AttributeSet set, int[] attrs) {
        if (theme == null) {
            return res.obtainAttributes(set, attrs);
        }
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    }

    private static int modulateColorAlpha(int color, float alphaMod) {
        return ColorUtils.setAlphaComponent(color, Math.round(((float) Color.alpha(color)) * alphaMod));
    }
}
