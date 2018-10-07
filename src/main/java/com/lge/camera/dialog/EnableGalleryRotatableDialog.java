package com.lge.camera.dialog;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;

public class EnableGalleryRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.EnableGalleryRotatableDialog$1 */
    class C07381 implements OnClickListener {
        C07381() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            EnableGalleryRotatableDialog.this.mGet.getActivity().startActivity(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:com.android.gallery3d")));
            EnableGalleryRotatableDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.EnableGalleryRotatableDialog$2 */
    class C07392 implements OnClickListener {
        C07392() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            EnableGalleryRotatableDialog.this.onDismiss();
        }
    }

    public EnableGalleryRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        String appName = "";
        try {
            appName = (String) this.mGet.getActivity().getPackageManager().getApplicationLabel(this.mGet.getActivity().getPackageManager().getApplicationInfo(CameraConstants.PACKAGE_GALLERY, 8192));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, false, false);
        ((ImageView) v.findViewById(C0088R.id.title_icon)).setVisibility(8);
        TextView titleText = (TextView) v.findViewById(C0088R.id.title_text);
        TextView messageText = (TextView) v.findViewById(16908308);
        int messageTextPadding = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_help_dialog_layout.margin);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        titleText.setText(C0088R.string.sp_note_dialog_title_NORMAL);
        messageText.setText(String.format(this.mGet.getActivity().getString(C0088R.string.sp_enable_app_msg_NORMAL), new Object[]{appName}));
        messageText.setPaddingRelative(messageTextPadding, 0, messageTextPadding, 0);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        btnCancel.setText(C0088R.string.cancel);
        super.create(v);
        btnOk.setOnClickListener(new C07381());
        btnCancel.setOnClickListener(new C07392());
    }
}
