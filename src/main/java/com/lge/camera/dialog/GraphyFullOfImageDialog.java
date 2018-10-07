package com.lge.camera.dialog;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class GraphyFullOfImageDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.GraphyFullOfImageDialog$1 */
    class C07441 implements OnClickListener {
        C07441() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            GraphyFullOfImageDialog.this.mGet.selectMyFilterItem();
            GraphyFullOfImageDialog.this.saveGraphyItemLimitDoNotShowSaveNote();
            GraphyFullOfImageDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.GraphyFullOfImageDialog$2 */
    class C07452 implements OnClickListener {
        C07452() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            GraphyFullOfImageDialog.this.mGet.requeryGraphyItems();
            GraphyFullOfImageDialog.this.saveGraphyItemLimitDoNotShowSaveNote();
            GraphyFullOfImageDialog.this.onDismiss();
        }
    }

    public GraphyFullOfImageDialog(CamDialogInterface function) {
        super(function);
    }

    public void create(boolean showAnim) {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, true, false);
        TextView titleText = (TextView) v.findViewById(C0088R.id.title_text);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        setFocus(btnOk, btnCancel, true);
        titleText.setText(C0088R.string.graphy_label_full_of_image_title);
        messageText.setText(C0088R.string.graphy_label_full_of_image_guide);
        messageText.setPaddingRelative(0, 0, 0, 0);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        btnCancel.setText(C0088R.string.cancel);
        this.mDoNotShowAgain = true;
        setCheckBox(v, true);
        super.create(v, false, false, showAnim);
        btnOk.setOnClickListener(new C07441());
        btnCancel.setOnClickListener(new C07452());
    }

    private void saveGraphyItemLimitDoNotShowSaveNote() {
        CheckBox userCheck = (CheckBox) this.mGet.findViewById(16908289);
        SharedPreferences pref = this.mGet.getActivity().getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
        if (userCheck != null && pref != null) {
            Editor edit = pref.edit();
            edit.putBoolean(CameraConstantsEx.GRAPHY_ITEM_LIMIT_DO_NOT_SHOW_SAVE_NOTE, userCheck.isChecked());
            edit.apply();
        }
    }
}
