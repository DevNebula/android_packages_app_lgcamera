package com.lge.camera.dialog;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class EnablePackageRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.EnablePackageRotatableDialog$2 */
    class C07412 implements OnClickListener {
        C07412() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            EnablePackageRotatableDialog.this.onDismiss();
        }
    }

    public EnablePackageRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    public void create(final String packageName) {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, false, false);
        ((ImageView) v.findViewById(C0088R.id.title_icon)).setVisibility(0);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(C0088R.id.title_text)).setText(C0088R.string.sp_note_dialog_title_NORMAL);
        messageText.setText(this.mGet.getActivity().getString(C0088R.string.sp_enable_app_msg_google_photos));
        messageText.setPaddingRelative(0, 0, 0, 0);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        btnCancel.setText(C0088R.string.cancel);
        super.create(v);
        btnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CamLog.m3d(CameraConstants.TAG, "ok button click....");
                EnablePackageRotatableDialog.this.mGet.getActivity().startActivity(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + packageName)));
                EnablePackageRotatableDialog.this.onDismiss();
            }
        });
        btnCancel.setOnClickListener(new C07412());
    }
}
