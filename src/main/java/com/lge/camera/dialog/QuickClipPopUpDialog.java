package com.lge.camera.dialog;

import android.support.p000v4.internal.view.SupportMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.QuickClipUtil;
import com.lge.camera.util.Utils;

public class QuickClipPopUpDialog extends RotateDialog {
    private QuickClipPopupInterface onClipPopupInterface;

    /* renamed from: com.lge.camera.dialog.QuickClipPopUpDialog$1 */
    class C07601 implements OnClickListener {
        C07601() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "ok button click....");
            QuickClipPopUpDialog.this.onClipPopupInterface.isUpload(true);
            QuickClipPopUpDialog.this.onDismiss();
            QuickClipPopUpDialog.this.onClipPopupInterface.resetClickedFlag();
        }
    }

    /* renamed from: com.lge.camera.dialog.QuickClipPopUpDialog$2 */
    class C07612 implements OnClickListener {
        C07612() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            QuickClipPopUpDialog.this.onClipPopupInterface.isUpload(false);
            QuickClipPopUpDialog.this.onDismiss();
            QuickClipPopUpDialog.this.onClipPopupInterface.resetClickedFlag();
        }
    }

    /* renamed from: com.lge.camera.dialog.QuickClipPopUpDialog$3 */
    class C07623 implements OnClickListener {
        C07623() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "fake button click....");
            QuickClipUtil.setQuickClipFakeMode();
            QuickClipPopUpDialog.this.onDismiss();
            QuickClipPopUpDialog.this.onClipPopupInterface.resetClickedFlag();
        }
    }

    public interface QuickClipPopupInterface {
        void isUpload(boolean z);

        void resetClickedFlag();
    }

    public QuickClipPopUpDialog(CamDialogInterface function, QuickClipPopupInterface onClipPopupInterface) {
        super(function);
        this.onClipPopupInterface = onClipPopupInterface;
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.quick_clip_rotate_dialog);
        setView(v, true, false);
        TextView titleText = (TextView) v.findViewById(C0088R.id.title_text);
        TextView messageText = (TextView) v.findViewById(16908308);
        messageText.setTextColor(SupportMenu.CATEGORY_MASK);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        Button fakeButton = (Button) v.findViewById(C0088R.id.fake_button);
        int messageTextPadding = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_help_dialog_layout.margin);
        titleText.setText(C0088R.string.quick_clip_pop_up_share_title);
        messageText.setText(C0088R.string.quick_clip_pop_up_share_message);
        messageText.setPaddingRelative(messageTextPadding, 0, messageTextPadding, 0);
        btnOk.setText(C0088R.string.yes);
        btnCancel.setText(C0088R.string.no);
        super.create(v, true, false);
        btnOk.setOnClickListener(new C07601());
        btnCancel.setOnClickListener(new C07612());
        fakeButton.setOnClickListener(new C07623());
    }
}
