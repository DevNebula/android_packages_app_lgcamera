package com.lge.camera.dialog;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;

public class AuCloudDialog extends RotateDialog {
    private static final String AU_CLOUD_PKG_NAME = "com.kddi.android.auclouduploader";
    private static final String SCHEME = "package";

    /* renamed from: com.lge.camera.dialog.AuCloudDialog$1 */
    class C07241 implements OnClickListener {
        C07241() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            try {
                AuCloudDialog.showInstalledAppDetails(AuCloudDialog.this.mGet.getAppContext(), AuCloudDialog.AU_CLOUD_PKG_NAME);
            } catch (ActivityNotFoundException ex) {
                CamLog.m6e(CameraConstants.TAG, "Au Cloud setting menu open fail", ex);
            }
            AuCloudDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.AuCloudDialog$2 */
    class C07252 implements OnClickListener {
        C07252() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            AuCloudDialog.this.onDismiss();
        }
    }

    public AuCloudDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, false, false);
        TextView titleText = (TextView) v.findViewById(C0088R.id.title_text);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        int messageTextPadding = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_help_dialog_layout.margin);
        titleText.setText(C0088R.string.sp_confirm_NORMAL);
        messageText.setText(C0088R.string.sp_dialog_change_setting_au_cloud);
        messageText.setPaddingRelative(messageTextPadding, 0, messageTextPadding, 0);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        btnCancel.setText(C0088R.string.cancel);
        super.create(v);
        btnOk.setOnClickListener(new C07241());
        btnCancel.setOnClickListener(new C07252());
    }

    public static void showInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        intent.addFlags(268435456);
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts(SCHEME, packageName, null));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, C0088R.string.error_not_exist_app, 0).show();
        }
    }
}
