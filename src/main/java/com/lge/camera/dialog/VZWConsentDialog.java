package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;

public class VZWConsentDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.VZWConsentDialog$1 */
    class C07881 implements OnClickListener {
        C07881() {
        }

        public void onClick(View v) {
            VZWConsentDialog.this.onDismiss();
        }
    }

    public VZWConsentDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_vzw_consent_dialog);
        setView(v, false, false);
        TextView titleText = (TextView) v.findViewById(C0088R.id.title_text);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        setFocus(btnOk, btnOk, false);
        titleText.setText(C0088R.string.location_consent_title);
        messageText.setText(C0088R.string.location_consent_msg);
        messageText.setPaddingRelative(0, 0, 0, 0);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        super.create(v, true, true);
        btnOk.setOnClickListener(new C07881());
    }
}
