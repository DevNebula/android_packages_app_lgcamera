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
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.ViewUtil;

public class StorageInitRotatableDialog extends RotateDialog {
    Button mCancelBtn = null;
    Button mOkBtn = null;

    /* renamed from: com.lge.camera.dialog.StorageInitRotatableDialog$1 */
    class C07781 implements OnClickListener {
        C07781() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            StorageInitRotatableDialog.this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_EXTERNAL, true);
            SharedPreferenceUtilBase.saveNeedShowStorageInitDialog(StorageInitRotatableDialog.this.mGet.getAppContext(), 1);
            StorageInitRotatableDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.StorageInitRotatableDialog$2 */
    class C07792 implements OnClickListener {
        C07792() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            StorageInitRotatableDialog.this.mGet.setSetting(Setting.KEY_STORAGE, CameraConstants.STORAGE_NAME_INTERNAL, true);
            SharedPreferenceUtilBase.saveNeedShowStorageInitDialog(StorageInitRotatableDialog.this.mGet.getAppContext(), 1);
            StorageInitRotatableDialog.this.onDismiss();
        }
    }

    public StorageInitRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    public void create(boolean showAnim) {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, false, false);
        TextView titleText = (TextView) v.findViewById(C0088R.id.title_text);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        this.mCancelBtn = btnCancel;
        this.mOkBtn = btnOk;
        titleText.setText(C0088R.string.camera_storage_init_dialog_title);
        messageText.setText(C0088R.string.sp_save_to_sdcard_NORMAL1);
        messageText.setPaddingRelative(0, 0, 0, 0);
        btnOk.setText(C0088R.string.change);
        btnCancel.setText(C0088R.string.later_button2);
        btnOk.setOnClickListener(new C07781());
        btnCancel.setOnClickListener(new C07792());
        super.create(v, false, false, showAnim);
    }

    public boolean onDismiss() {
        this.mGet.doInitSettingOrder();
        this.mGet.setSettingMenuEnable(Setting.KEY_STORAGE, true);
        if (this.mOkBtn != null) {
            this.mOkBtn = null;
        }
        if (this.mCancelBtn != null) {
            this.mCancelBtn = null;
        }
        return super.onDismiss();
    }

    protected void notifyFocusingFinished(final View view) {
        if (ModelProperties.isKeyPadSupported(this.mGet.getAppContext())) {
            this.mGet.getHandler().postDelayed(new Thread(new Runnable() {
                public void run() {
                    StorageInitRotatableDialog.this.mGet.removePostRunnable(this);
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
            this.mCancelBtn.setNextFocusLeftId(this.mCancelBtn.getId());
            this.mCancelBtn.setNextFocusRightId(this.mCancelBtn.getId());
            this.mCancelBtn.setNextFocusUpId(this.mCancelBtn.getId());
            this.mCancelBtn.setNextFocusDownId(this.mCancelBtn.getId());
            this.mOkBtn.setNextFocusLeftId(this.mOkBtn.getId());
            this.mOkBtn.setNextFocusRightId(this.mOkBtn.getId());
            this.mOkBtn.setNextFocusUpId(this.mOkBtn.getId());
            this.mOkBtn.setNextFocusDownId(this.mOkBtn.getId());
            switch (degree) {
                case 0:
                    this.mCancelBtn.setNextFocusRightId(this.mOkBtn.getId());
                    this.mOkBtn.setNextFocusLeftId(this.mCancelBtn.getId());
                    return;
                case 90:
                    this.mCancelBtn.setNextFocusUpId(this.mOkBtn.getId());
                    this.mOkBtn.setNextFocusDownId(this.mCancelBtn.getId());
                    return;
                case 180:
                    this.mCancelBtn.setNextFocusLeftId(this.mOkBtn.getId());
                    this.mOkBtn.setNextFocusRightId(this.mCancelBtn.getId());
                    return;
                case 270:
                    this.mCancelBtn.setNextFocusDownId(this.mOkBtn.getId());
                    this.mOkBtn.setNextFocusUpId(this.mCancelBtn.getId());
                    return;
                default:
                    return;
            }
        }
    }
}
