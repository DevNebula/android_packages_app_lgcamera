package android.support.p000v4.view;

import android.annotation.TargetApi;
import android.support.annotation.RequiresApi;
import android.view.ViewGroup.MarginLayoutParams;

@TargetApi(17)
@RequiresApi(17)
/* renamed from: android.support.v4.view.MarginLayoutParamsCompatJellybeanMr1 */
class MarginLayoutParamsCompatJellybeanMr1 {
    MarginLayoutParamsCompatJellybeanMr1() {
    }

    public static int getMarginStart(MarginLayoutParams lp) {
        return lp.getMarginStart();
    }

    public static int getMarginEnd(MarginLayoutParams lp) {
        return lp.getMarginEnd();
    }

    public static void setMarginStart(MarginLayoutParams lp, int marginStart) {
        lp.setMarginStart(marginStart);
    }

    public static void setMarginEnd(MarginLayoutParams lp, int marginEnd) {
        lp.setMarginEnd(marginEnd);
    }

    public static boolean isMarginRelative(MarginLayoutParams lp) {
        return lp.isMarginRelative();
    }

    public static int getLayoutDirection(MarginLayoutParams lp) {
        return lp.getLayoutDirection();
    }

    public static void setLayoutDirection(MarginLayoutParams lp, int layoutDirection) {
        lp.setLayoutDirection(layoutDirection);
    }

    public static void resolveLayoutDirection(MarginLayoutParams lp, int layoutDirection) {
        lp.resolveLayoutDirection(layoutDirection);
    }
}
