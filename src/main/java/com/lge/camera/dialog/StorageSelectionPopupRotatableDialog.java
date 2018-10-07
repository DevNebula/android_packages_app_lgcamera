package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class StorageSelectionPopupRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.StorageSelectionPopupRotatableDialog$1 */
    class C07811 implements OnClickListener {
        C07811() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            StorageSelectionPopupRotatableDialog.this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_EXTERNAL, true);
            SharedPreferenceUtilBase.saveNeedShowStorageInitDialog(StorageSelectionPopupRotatableDialog.this.mGet.getAppContext(), 1);
            StorageSelectionPopupRotatableDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.StorageSelectionPopupRotatableDialog$2 */
    class C07822 implements OnClickListener {
        C07822() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            StorageSelectionPopupRotatableDialog.this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_INTERNAL, true);
            SharedPreferenceUtilBase.saveNeedShowStorageInitDialog(StorageSelectionPopupRotatableDialog.this.mGet.getAppContext(), 1);
            StorageSelectionPopupRotatableDialog.this.onDismiss();
        }
    }

    public StorageSelectionPopupRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, false, false);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        ((TextView) v.findViewById(C0088R.id.title_text)).setText(C0088R.string.sp_storage_NORMAL);
        messageText.setText(C0088R.string.sp_sdcard_inserted_noti);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        btnCancel.setText(C0088R.string.cancel);
        btnOk.setOnClickListener(new C07811());
        btnCancel.setOnClickListener(new C07822());
        super.create(v, false, true);
    }
}
