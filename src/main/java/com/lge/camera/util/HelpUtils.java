package com.lge.camera.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import com.lge.camera.components.ShiftImageSpan;
import com.lge.camera.managers.ext.SpliceInitGuideManager.SpannableItem;
import java.util.List;

public class HelpUtils {
    public static SpannableString makeSpannableString(Context c, List<SpannableItem> spannableArray, String string) {
        if (spannableArray == null) {
            return null;
        }
        SpannableString ss = new SpannableString(string);
        for (int i = 0; i < spannableArray.size(); i++) {
            SpannableItem item = (SpannableItem) spannableArray.get(i);
            String toBeReplace = item.toBeReplace;
            int spanStartIndex = string.indexOf(toBeReplace);
            int spanEndIndex = spanStartIndex + toBeReplace.length();
            if (spanStartIndex >= 0 && spanEndIndex <= string.length()) {
                while (true) {
                    Drawable drawable = c.getDrawable(item.drawableId);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        ss.setSpan(new ShiftImageSpan(c, drawable, 1), spanStartIndex, spanEndIndex, 17);
                        spanStartIndex = string.indexOf(toBeReplace, spanEndIndex);
                        spanEndIndex = spanStartIndex + toBeReplace.length();
                        if (spanStartIndex < 0 || spanEndIndex > string.length()) {
                            break;
                        }
                    }
                }
            }
        }
        return ss;
    }

    public static String getSpannableDescription(Context c, List<SpannableItem> spannableArray, String string, String componentType) {
        for (int i = 0; i < spannableArray.size(); i++) {
            SpannableItem item = (SpannableItem) spannableArray.get(i);
            String toBeReplace = item.toBeReplace;
            String desc = c.getString(item.descId) + componentType;
            if (!(toBeReplace == null || desc == null)) {
                string = string.replaceAll(toBeReplace, desc);
            }
        }
        return string;
    }
}
