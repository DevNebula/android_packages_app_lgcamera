package com.lge.camera.app;

import android.app.Activity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import com.lge.camera.C0088R;
import com.lge.camera.app.ext.PopoutActivity;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class CreatePopoutShortcutActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        CamLog.m3d(CameraConstants.TAG, "onCreate");
        super.onCreate(savedInstanceState);
        ShortcutIconResource icon = ShortcutIconResource.fromContext(this, C0088R.mipmap.popout_picture);
        Intent intent = new Intent();
        intent.putExtra("android.intent.extra.shortcut.INTENT", new Intent(this, PopoutActivity.class));
        intent.putExtra("android.intent.extra.shortcut.NAME", getString(C0088R.string.mode_popout_camera));
        intent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", icon);
        intent.putExtra("duplicate", false);
        setResult(-1, intent);
        finish();
    }
}
