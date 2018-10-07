package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;

public class GraphyOnDialog extends RotateDialog {
    private boolean mIsFullOfItem = false;

    /* renamed from: com.lge.camera.dialog.GraphyOnDialog$1 */
    class C07481 implements OnClickListener {
        C07481() {
        }

        public void onClick(View v) {
            if (GraphyOnDialog.this.mIsFullOfItem) {
                GraphyOnDialog.this.mGet.showDialog(16);
            } else {
                GraphyOnDialog.this.mGet.selectMyFilterItem();
            }
            GraphyOnDialog.this.onDismiss();
        }
    }

    public GraphyOnDialog(CamDialogInterface function) {
        super(function);
    }

    public void create(boolean showAnim) {
        this.mIsFullOfItem = showAnim;
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, false, false);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOK = (Button) v.findViewById(C0088R.id.ok_button);
        ((TextView) v.findViewById(C0088R.id.title_text)).setText(C0088R.string.graphy_label_auto_on_title);
        messageText.setText(C0088R.string.graphy_label_auto_on);
        messageText.setPaddingRelative(0, 0, 0, 0);
        btnOK.setText(C0088R.string.sp_ok_NORMAL);
        setOneButtonLayout(v, true);
        super.create(v, false, false, showAnim);
        btnOK.setOnClickListener(new C07481());
    }
}
