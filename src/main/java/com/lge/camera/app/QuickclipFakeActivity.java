package com.lge.camera.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.p000v4.internal.view.SupportMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.database.OverlapProjectDbAdapter;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.QuickClipUtil;
import com.lge.camera.util.Utils;

public class QuickclipFakeActivity extends Activity {
    String mActivityName = null;
    String mMimeType = null;
    String mPackageName = null;
    String mUri = null;

    /* renamed from: com.lge.camera.app.QuickclipFakeActivity$1 */
    class C03371 implements OnClickListener {
        C03371() {
        }

        public void onClick(View v) {
            LdbUtil.sendLDBIntent(QuickclipFakeActivity.this.getApplicationContext(), LdbConstants.LDB_FEATURE_NAME_QUICKCLIP_SHAREBYMORE);
            QuickClipUtil.reportActivitySelected(QuickclipFakeActivity.this.getApplicationContext(), QuickclipFakeActivity.this.mActivityName);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.SEND");
            intent.setType(QuickclipFakeActivity.this.mMimeType);
            intent.putExtra("android.intent.extra.STREAM", Uri.parse(QuickclipFakeActivity.this.mUri));
            intent.setComponent(new ComponentName(QuickclipFakeActivity.this.mPackageName, QuickclipFakeActivity.this.mActivityName));
            QuickclipFakeActivity.this.startActivity(intent);
        }
    }

    /* renamed from: com.lge.camera.app.QuickclipFakeActivity$2 */
    class C03382 implements OnClickListener {
        C03382() {
        }

        public void onClick(View v) {
            QuickclipFakeActivity.this.finish();
        }
    }

    /* renamed from: com.lge.camera.app.QuickclipFakeActivity$3 */
    class C03393 implements OnClickListener {
        C03393() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "fake button click....");
            QuickClipUtil.setQuickClipFakeMode();
            QuickclipFakeActivity.this.finish();
        }
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0088R.layout.quick_clip_rotate_dialog);
        Intent intent = getIntent();
        this.mMimeType = intent.getStringExtra("mimeType");
        this.mUri = intent.getStringExtra(OverlapProjectDbAdapter.KEY_URI);
        this.mPackageName = intent.getStringExtra("packageName");
        this.mActivityName = intent.getStringExtra("activityName");
        CamLog.m3d(CameraConstants.TAG, "mimeType : " + this.mMimeType + " uri : " + this.mUri + " packageName : " + this.mPackageName + " activityName" + this.mActivityName);
        TextView titleText = (TextView) findViewById(C0088R.id.title_text);
        setMessageText();
        Button btnOk = (Button) findViewById(C0088R.id.ok_button);
        Button btnCancel = (Button) findViewById(C0088R.id.cancel_button);
        titleText.setText(C0088R.string.quick_clip_pop_up_share_title);
        btnOk.setText(C0088R.string.yes);
        btnCancel.setText(C0088R.string.no);
        Button fakeButton = (Button) findViewById(C0088R.id.fake_button);
        ((RelativeLayout) findViewById(C0088R.id.quickclip_dialog_layout)).setVisibility(0);
        btnOk.setOnClickListener(new C03371());
        btnCancel.setOnClickListener(new C03382());
        fakeButton.setOnClickListener(new C03393());
    }

    private void setMessageText() {
        TextView messageText;
        ScrollView scrView = (ScrollView) findViewById(C0088R.id.message_scroll);
        int textViewRes = getApplicationContext().getResources().getIdentifier("dialog_c_1", "layout", "com.lge");
        if (textViewRes <= 0) {
            messageText = new TextView(getApplicationContext());
            messageText.setId(16908308);
        } else {
            messageText = (TextView) getLayoutInflater().inflate(textViewRes, null);
        }
        int messageTextPadding = Utils.getPx(getApplicationContext(), C0088R.dimen.rotate_help_dialog_layout.margin);
        int messageTextPaddingLayout = Utils.getPx(getApplicationContext(), C0088R.dimen.rotate_dialog_message_text.padding);
        messageText.setMinHeight(Utils.getPx(getApplicationContext(), C0088R.dimen.rotate_dialog_title_layout.minHeight));
        messageText.setMinWidth(Utils.getPx(getApplicationContext(), C0088R.dimen.rotate_dialog_minWidth));
        messageText.setPaddingRelative(messageTextPaddingLayout, messageTextPaddingLayout, messageTextPaddingLayout, messageTextPaddingLayout);
        messageText.setTextDirection(5);
        messageText.setTextAppearance(getApplicationContext(), C0088R.style.type_d03_sp);
        scrView.addView(messageText);
        messageText.setTextColor(SupportMenu.CATEGORY_MASK);
        messageText.setText(C0088R.string.quick_clip_pop_up_share_message);
        messageText.setPaddingRelative(messageTextPadding, 0, messageTextPadding, 0);
    }
}
