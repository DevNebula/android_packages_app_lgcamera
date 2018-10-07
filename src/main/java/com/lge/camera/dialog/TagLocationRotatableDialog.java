package com.lge.camera.dialog;

import android.location.LocationManager;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class TagLocationRotatableDialog extends RotateDialog {

    /* renamed from: com.lge.camera.dialog.TagLocationRotatableDialog$1 */
    class C07831 implements OnClickListener {
        C07831() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "yes button click....");
            if (!CheckStatusManager.isSystemSettingUseLocation(TagLocationRotatableDialog.this.mGet.getActivity().getContentResolver())) {
                CheckStatusManager.setSystemSettingUseLocation(TagLocationRotatableDialog.this.mGet.getActivity().getContentResolver(), true);
                TagLocationRotatableDialog.this.mGet.setLocationOnByCamera(true);
            }
            TagLocationRotatableDialog.this.mGet.setSetting(Setting.KEY_TAG_LOCATION, "on", true);
            LocationManager locationManager = (LocationManager) TagLocationRotatableDialog.this.mGet.getAppContext().getSystemService("location");
            if (locationManager.isProviderEnabled("gps") || locationManager.isProviderEnabled("network")) {
                TagLocationRotatableDialog.this.mGet.getHandler().sendEmptyMessage(60);
                SharedPreferenceUtilBase.saveInitialTagLocation(TagLocationRotatableDialog.this.mGet.getAppContext(), 1);
            }
            if (SharedPreferenceUtil.getInitialTagLocation(TagLocationRotatableDialog.this.mGet.getAppContext()) != 0) {
                TagLocationRotatableDialog.this.mGet.showLocationPermissionRequestDialog(true);
            }
            TagLocationRotatableDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.TagLocationRotatableDialog$2 */
    class C07842 implements OnClickListener {
        C07842() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "no button click....");
            SharedPreferenceUtilBase.saveInitialTagLocation(TagLocationRotatableDialog.this.mGet.getAppContext(), 1);
            TagLocationRotatableDialog.this.mGet.setSetting(Setting.KEY_TAG_LOCATION, "off", true);
            TagLocationRotatableDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.TagLocationRotatableDialog$3 */
    class C07853 extends ClickableSpan {
        C07853() {
        }

        public void onClick(View textView) {
            TagLocationRotatableDialog.this.mGet.showDialog(138);
        }

        public void updateDrawState(TextPaint textPaint) {
            super.updateDrawState(textPaint);
            textPaint.setColor(TagLocationRotatableDialog.this.mGet.getAppContext().getResources().getColor(C0088R.color.location_consent_color));
        }
    }

    public TagLocationRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    protected void setView(View dialogView, boolean useCheckBox, boolean isHelpDialog) {
        ScrollView scrView = (ScrollView) dialogView.findViewById(C0088R.id.message_scroll);
        if (scrView != null) {
            TextView messageText;
            int textViewRes = this.mGet.getAppContext().getResources().getIdentifier("dialog_c_1", "layout", "com.lge");
            if (textViewRes <= 0) {
                messageText = new TextView(this.mGet.getAppContext());
                messageText.setId(16908308);
            } else {
                messageText = (TextView) this.mGet.inflateView(textViewRes);
            }
            messageText.setTextDirection(5);
            messageText.setTextAppearance(C0088R.style.type_d03_dp);
            messageText.setPadding(0, 0, 0, 0);
            scrView.addView(messageText);
        }
    }

    public void create(boolean showAnim) {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, false, false);
        TextView titleText = (TextView) v.findViewById(C0088R.id.title_text);
        TextView messageText = (TextView) v.findViewById(16908308);
        Button btnOk = (Button) v.findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        setFocus(btnCancel, btnOk, false);
        titleText.setText(C0088R.string.tag_locations_title2);
        setMessageText(messageText);
        btnOk.setText(C0088R.string.turn_on_button);
        btnCancel.setText(C0088R.string.later_button2);
        super.create(v, false, false, showAnim);
        btnOk.setOnClickListener(new C07831());
        btnCancel.setOnClickListener(new C07842());
    }

    private void setMessageText(TextView messageText) {
        if (messageText != null) {
            if (CheckStatusManager.isSystemSettingUseLocation(this.mGet.getAppContext().getContentResolver())) {
                messageText.setText(C0088R.string.tag_location_message_on1);
            } else if (ModelProperties.getCarrierCode() == 6) {
                messageText.setText(getVZWTagLoationMsg());
                messageText.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                messageText.setText(C0088R.string.tag_location_message_off1);
            }
        }
    }

    private SpannableString getVZWTagLoationMsg() {
        String location_msg = this.mGet.getAppContext().getString(C0088R.string.tag_location_message_vzw_off1);
        String location_consent_msg = this.mGet.getAppContext().getString(C0088R.string.location_consent);
        location_msg = location_msg.replace("(#01#)", location_consent_msg);
        SpannableString spannableString = new SpannableString(location_msg);
        spannableString.setSpan(new C07853(), location_msg.indexOf(location_consent_msg), location_msg.indexOf(location_consent_msg) + location_consent_msg.length(), 33);
        return spannableString;
    }
}
