package com.lge.camera.dialog;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ViewUtil;

public class StorageFullRotatableDialog extends RotateDialog {
    private int mMessageId = 0;

    /* renamed from: com.lge.camera.dialog.StorageFullRotatableDialog$1 */
    class C07761 implements OnClickListener {
        boolean mHasAvailableStorage = true;

        C07761() {
        }

        public void onClick(View v) {
            int storage = 1;
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            switch (StorageFullRotatableDialog.this.mMessageId) {
                case C0088R.string.storage_full_msg_2_external:
                    StorageFullRotatableDialog.this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_INTERNAL, true);
                    break;
                case C0088R.string.storage_full_msg_2_internal:
                    StorageFullRotatableDialog.this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_EXTERNAL, true);
                    break;
                default:
                    this.mHasAvailableStorage = false;
                    break;
            }
            if (!CameraConstants.STORAGE_NAME_EXTERNAL.equals(StorageFullRotatableDialog.this.mGet.getSettingValue(Setting.KEY_STORAGE))) {
                storage = 0;
            }
            if (this.mHasAvailableStorage) {
                StorageFullRotatableDialog.this.mGet.checkStorage(0, storage);
            }
            StorageFullRotatableDialog.this.onDismiss();
        }
    }

    public boolean onDismiss() {
        CamLog.m3d(CameraConstants.TAG, "onDismiss mMessageId " + this.mMessageId);
        switch (this.mMessageId) {
            case C0088R.string.storage_full_msg_2_external:
                this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_INTERNAL, true);
                break;
            case C0088R.string.storage_full_msg_2_internal:
                this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_EXTERNAL, true);
                break;
        }
        return super.onDismiss();
    }

    public StorageFullRotatableDialog(CamDialogInterface function, int msgId) {
        super(function);
        this.mMessageId = msgId;
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, false, false);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        ((TextView) v.findViewById(C0088R.id.title_text)).setText(C0088R.string.sp_storage_full_popup_ics_title_NORMAL);
        messageText.setText(this.mMessageId);
        messageText.setPaddingRelative(0, 0, 0, 0);
        btnOk.setText(C0088R.string.sp_ok_NORMAL);
        setOneButtonLayout(v, true);
        btnOk.setOnClickListener(new C07761());
        super.create(v, false, true);
    }

    protected void notifyFocusingFinished(final View view) {
        if (ModelProperties.isKeyPadSupported(this.mGet.getAppContext())) {
            this.mGet.getHandler().postDelayed(new Thread(new Runnable() {
                public void run() {
                    StorageFullRotatableDialog.this.mGet.removePostRunnable(this);
                    view.setFocusable(false);
                    Button btnOk = (Button) view.findViewById(C0088R.id.ok_button);
                    btnOk.sendAccessibilityEvent(32768);
                    btnOk.requestFocus();
                }
            }), (long) (ViewUtil.isAccessibilityServiceEnabled(this.mGet.getAppContext()) ? 5000 : 0));
        }
    }

    protected void setNextFocusID(int degree) {
        if (ModelProperties.isKeyPadSupported(this.mGet.getAppContext())) {
            Button btnOk = (Button) this.mGet.findViewById(C0088R.id.ok_button);
            if (btnOk != null) {
                btnOk.setNextFocusLeftId(btnOk.getId());
                btnOk.setNextFocusRightId(btnOk.getId());
                btnOk.setNextFocusUpId(btnOk.getId());
                btnOk.setNextFocusDownId(btnOk.getId());
            }
        }
    }
}
