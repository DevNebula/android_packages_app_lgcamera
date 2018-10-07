package com.lge.camera.app;

import android.app.Activity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class CreateSquareCameraShortcutActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        CamLog.m3d(CameraConstants.TAG, "onCreate");
        super.onCreate(savedInstanceState);
        ShortcutIconResource icon = ShortcutIconResource.fromContext(this, C0088R.mipmap.lg_iconframe_camera_square);
        Intent intent = new Intent();
        intent.putExtra("android.intent.extra.shortcut.INTENT", new Intent(this, SquareCameraActivity.class));
        intent.putExtra("android.intent.extra.shortcut.NAME", getString(C0088R.string.square_camera));
        intent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", icon);
        intent.putExtra("duplicate", false);
        setResult(-1, intent);
        finish();
    }
}
