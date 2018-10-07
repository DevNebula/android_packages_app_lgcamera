package com.lge.camera.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.OsManager;
import java.util.ArrayList;

public class ViewUtil {
    public static void clearImageViewDrawableOnly(ImageView imageView) {
        if (imageView != null) {
            try {
                Drawable drawable = imageView.getDrawable();
                if (drawable != null) {
                    imageView.setImageDrawable(null);
                    drawable.setCallback(null);
                }
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "clearImageViewDrawable Exception ", e);
            }
        }
    }

    public static void clearImageViewBackgroundDrawable(ImageView imageView) {
        if (imageView != null) {
            try {
                if (imageView.getBackground() != null) {
                    imageView.getBackground().setCallback(null);
                    imageView.setBackground(null);
                }
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "clearImageViewDrawable Exception ", e);
            }
        }
    }

    public static ArrayList<View> traverseViewGroup(ViewGroup viewGroup) {
        ArrayList<View> returnList = new ArrayList();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                if (viewGroup.getChildAt(i).getVisibility() == 0) {
                    ArrayList<View> subList = traverseViewGroup((ViewGroup) viewGroup.getChildAt(i));
                    for (int j = 0; j < subList.size(); j++) {
                        returnList.add(subList.get(j));
                    }
                }
            } else if (viewGroup.getChildAt(i).getVisibility() == 0) {
                if (viewGroup.getChildAt(i) instanceof TextView) {
                    returnList.add(viewGroup.getChildAt(i));
                } else if (viewGroup.getChildAt(i) instanceof Button) {
                    returnList.add(viewGroup.getChildAt(i));
                }
            }
        }
        return returnList;
    }

    public static void setContentDescriptionForAccessibility(Context context, ViewGroup viewGroup) {
        ArrayList<View> group = traverseViewGroup(viewGroup);
        CharSequence contentDescription = "";
        for (int i = 0; i < group.size(); i++) {
            if (group.get(i) instanceof TextView) {
                CharSequence msg = ((TextView) group.get(i)).getContentDescription();
                if (msg == null) {
                    msg = ((TextView) group.get(i)).getText();
                }
                contentDescription = contentDescription.toString() + msg;
            } else if (group.get(i) instanceof Button) {
                contentDescription = contentDescription.toString() + ((Button) group.get(i)).getText();
            } else {
            }
            contentDescription = contentDescription.toString() + "\n\n";
        }
        viewGroup.setContentDescription(contentDescription);
    }

    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService("accessibility");
        if (am.isEnabled() && am.isTouchExplorationEnabled()) {
            return true;
        }
        return false;
    }

    public static void setPatialUpdate(Context context, boolean on) {
        CamLog.m3d(CameraConstants.TAG, "setPatialUpdate is on? " + on);
        try {
            ((OsManager) new LGContext(context).getLGSystemService("osservice")).setSystemProperty("sys.pu.disable", on ? "0" : "1");
        } catch (SecurityException e) {
            CamLog.m11w(CameraConstants.TAG, "setPatialUpdate : SecurityException.");
        }
    }
}
