package android.support.p001v7.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.p000v4.content.res.ConfigurationHelper;
import android.support.p000v4.view.ViewConfigurationCompat;
import android.support.p001v7.appcompat.C0082R;
import android.view.ViewConfiguration;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;

@RestrictTo({Scope.GROUP_ID})
/* renamed from: android.support.v7.view.ActionBarPolicy */
public class ActionBarPolicy {
    private Context mContext;

    public static ActionBarPolicy get(Context context) {
        return new ActionBarPolicy(context);
    }

    private ActionBarPolicy(Context context) {
        this.mContext = context;
    }

    public int getMaxActionButtons() {
        Resources res = this.mContext.getResources();
        int widthDp = ConfigurationHelper.getScreenWidthDp(res);
        int heightDp = ConfigurationHelper.getScreenHeightDp(res);
        if (ConfigurationHelper.getSmallestScreenWidthDp(res) > 600 || widthDp > 600 || ((widthDp > CameraConstants.MENU_TYPE_FULL_SCREEN_GUIDE && heightDp > CameraConstantsEx.HD_SCREEN_RESOLUTION) || (widthDp > CameraConstantsEx.HD_SCREEN_RESOLUTION && heightDp > CameraConstants.MENU_TYPE_FULL_SCREEN_GUIDE))) {
            return 5;
        }
        if (widthDp >= 500 || ((widthDp > 640 && heightDp > CameraConstants.SHUTTER_ZOOM_SUPPORTED_MIN_ZOOM_LEVEL) || (widthDp > CameraConstants.SHUTTER_ZOOM_SUPPORTED_MIN_ZOOM_LEVEL && heightDp > 640))) {
            return 4;
        }
        if (widthDp >= 360) {
            return 3;
        }
        return 2;
    }

    public boolean showsOverflowMenuButton() {
        if (VERSION.SDK_INT < 19 && ViewConfigurationCompat.hasPermanentMenuKey(ViewConfiguration.get(this.mContext))) {
            return false;
        }
        return true;
    }

    public int getEmbeddedMenuWidthLimit() {
        return this.mContext.getResources().getDisplayMetrics().widthPixels / 2;
    }

    public boolean hasEmbeddedTabs() {
        return this.mContext.getResources().getBoolean(C0082R.bool.abc_action_bar_embed_tabs);
    }

    public int getTabContainerHeight() {
        TypedArray a = this.mContext.obtainStyledAttributes(null, C0082R.styleable.ActionBar, C0082R.attr.actionBarStyle, 0);
        int height = a.getLayoutDimension(C0082R.styleable.ActionBar_height, 0);
        Resources r = this.mContext.getResources();
        if (!hasEmbeddedTabs()) {
            height = Math.min(height, r.getDimensionPixelSize(C0082R.dimen.abc_action_bar_stacked_max_height));
        }
        a.recycle();
        return height;
    }

    public boolean enableHomeButtonByDefault() {
        return this.mContext.getApplicationInfo().targetSdkVersion < 14;
    }

    public int getStackedTabMaxWidth() {
        return this.mContext.getResources().getDimensionPixelSize(C0082R.dimen.abc_action_bar_stacked_tab_max_width);
    }
}
