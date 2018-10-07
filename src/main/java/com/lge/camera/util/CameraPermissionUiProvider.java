package com.lge.camera.util;

import android.content.Context;
import com.lge.app.permission.DefaultUiProvider;
import com.lge.camera.C0088R;

public class CameraPermissionUiProvider extends DefaultUiProvider {
    public CharSequence getReasonForRequestingPermissions(Context context, String[] permissions) {
        return context.getResources().getString(C0088R.string.requeset_camera_permissions);
    }
}
