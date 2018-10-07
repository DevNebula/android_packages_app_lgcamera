package com.lge.camera.postview;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SystemBarUtil;

public class PostviewAttach extends PostviewBase {
    private OnClickListener mCancelListener = new C13693();
    private OnClickListener mOkListener = new C13682();
    private OnClickListener mPlayListener = new C13704();

    /* renamed from: com.lge.camera.postview.PostviewAttach$2 */
    class C13682 implements OnClickListener {
        C13682() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "Attach button clicked.");
            Uri uri = null;
            if (PostviewAttach.this.mParam.getUriList() != null) {
                uri = (Uri) PostviewAttach.this.mParam.getUriList().get(0);
            }
            if (PostviewAttach.this.mGet.attatchMediaOnPostview(uri, PostviewAttach.this.mParam.getContentType())) {
                v.setOnClickListener(null);
            }
        }
    }

    /* renamed from: com.lge.camera.postview.PostviewAttach$3 */
    class C13693 implements OnClickListener {
        C13693() {
        }

        public void onClick(View v) {
            PostviewAttach.this.startPostviewAnimation(PostviewAttach.this.mGet.findViewById(C0088R.id.postview_attach), false);
            v.setOnClickListener(null);
        }
    }

    /* renamed from: com.lge.camera.postview.PostviewAttach$4 */
    class C13704 implements OnClickListener {
        C13704() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "mPlayButtonListener clicked.");
            Uri uri = null;
            if (PostviewAttach.this.mParam.getUriList() != null) {
                uri = (Uri) PostviewAttach.this.mParam.getUriList().get(0);
            }
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(uri, MultimediaProperties.VIDEO_MIME_TYPE);
            intent.putExtra("mimeType", MultimediaProperties.VIDEO_MIME_TYPE);
            intent.putExtra("android.intent.extra.finishOnCompletion", true);
            try {
                PostviewAttach.this.mGet.getActivity().startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                CamLog.m6e(CameraConstants.TAG, "ActivityNotFoundException : ", ex);
                PostviewAttach.this.mGet.showToast(PostviewAttach.this.mGet.getAppContext().getString(C0088R.string.error_not_exist_app), CameraConstants.TOAST_LENGTH_SHORT);
            }
        }
    }

    public PostviewAttach(PostviewBridge postviewBridge) {
        super(postviewBridge);
    }

    public void setupPostviewActionBar() {
        SystemBarUtil.setActionBarVisible(this.mGet.getActivity(), false);
        SystemBarUtil.hideSystemUIonPostview(this.mGet.getActivity());
    }

    public void setupLayout() {
        super.setupLayout();
        ViewGroup vg = (ViewGroup) getBaseLayout();
        if (vg != null) {
            this.mGet.getActivity().getLayoutInflater().inflate(C0088R.layout.postview_attach, vg);
        }
        setDegree(getOrientationDegree(), false);
        setUpAttachMenu();
    }

    public void releaseLayout() {
        ViewGroup vg = (ViewGroup) getBaseLayout();
        if (vg != null) {
            View postView = this.mGet.findViewById(C0088R.id.postview_attach);
            if (postView != null) {
                vg.removeView(postView);
            }
        }
        super.releaseLayout();
    }

    public void postviewShow() {
        CamLog.m3d(CameraConstants.TAG, "TIME_CHECK show()");
        View postView = this.mGet.findViewById(C0088R.id.postview_attach);
        if (postView == null) {
            CamLog.m11w(CameraConstants.TAG, "postviewShow : inflate view fail.");
            return;
        }
        if (ModelProperties.isKeyPadSupported(getAppContext())) {
            this.mGet.findViewById(C0088R.id.done_button).setFocusable(true);
            this.mGet.findViewById(C0088R.id.done_button).requestFocus();
        }
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                View btnOk = PostviewAttach.this.mGet.findViewById(C0088R.id.done_button);
                if (btnOk != null) {
                    btnOk.sendAccessibilityEvent(8);
                }
            }
        }, 0);
        loadSingleCapturedImages();
        postView.setVisibility(0);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                startPostviewAnimation(this.mGet.findViewById(C0088R.id.postview_attach), false);
                return true;
            default:
                return false;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 4:
                startPostviewAnimation(this.mGet.findViewById(C0088R.id.postview_attach), false);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void setUpAttachMenu() {
        Button okBtn = (Button) this.mGet.findViewById(C0088R.id.done_button);
        Button cancelBtn = (Button) this.mGet.findViewById(C0088R.id.btn_cancel);
        ImageButton playBtn = (ImageButton) this.mGet.findViewById(C0088R.id.btn_play);
        if (this.mParam.getContentType() == 0) {
            playBtn.setVisibility(8);
        } else {
            playBtn.setVisibility(0);
        }
        boolean focusable = ModelProperties.isKeyPadSupported(getAppContext());
        okBtn.setFocusable(focusable);
        cancelBtn.setFocusable(focusable);
        playBtn.setFocusable(focusable);
        okBtn.setOnClickListener(this.mOkListener);
        cancelBtn.setOnClickListener(this.mCancelListener);
        playBtn.setEnabled(true);
        playBtn.setOnClickListener(this.mPlayListener);
    }

    public int getPostviewType() {
        return 1;
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        initCommandLayout(degree);
        if (ModelProperties.isKeyPadSupported(getAppContext())) {
            Button okBtn = (Button) this.mGet.findViewById(C0088R.id.done_button);
            Button cancelBtn = (Button) this.mGet.findViewById(C0088R.id.btn_cancel);
            ImageButton playBtn = (ImageButton) this.mGet.findViewById(C0088R.id.btn_play);
            okBtn.setNextFocusLeftId(okBtn.getId());
            okBtn.setNextFocusRightId(okBtn.getId());
            okBtn.setNextFocusUpId(okBtn.getId());
            okBtn.setNextFocusDownId(okBtn.getId());
            cancelBtn.setNextFocusLeftId(cancelBtn.getId());
            cancelBtn.setNextFocusRightId(cancelBtn.getId());
            cancelBtn.setNextFocusUpId(cancelBtn.getId());
            cancelBtn.setNextFocusDownId(cancelBtn.getId());
            playBtn.setNextFocusLeftId(playBtn.getId());
            playBtn.setNextFocusRightId(playBtn.getId());
            playBtn.setNextFocusUpId(playBtn.getId());
            playBtn.setNextFocusDownId(playBtn.getId());
            switch (degree) {
                case 0:
                    okBtn.setNextFocusLeftId(cancelBtn.getId());
                    cancelBtn.setNextFocusRightId(okBtn.getId());
                    okBtn.setNextFocusUpId(playBtn.getId());
                    cancelBtn.setNextFocusUpId(playBtn.getId());
                    playBtn.setNextFocusDownId(okBtn.getId());
                    return;
                case 90:
                    okBtn.setNextFocusDownId(cancelBtn.getId());
                    cancelBtn.setNextFocusUpId(okBtn.getId());
                    okBtn.setNextFocusLeftId(playBtn.getId());
                    cancelBtn.setNextFocusLeftId(playBtn.getId());
                    playBtn.setNextFocusRightId(okBtn.getId());
                    return;
                case 180:
                    okBtn.setNextFocusRightId(cancelBtn.getId());
                    cancelBtn.setNextFocusLeftId(okBtn.getId());
                    okBtn.setNextFocusDownId(playBtn.getId());
                    cancelBtn.setNextFocusDownId(playBtn.getId());
                    playBtn.setNextFocusUpId(okBtn.getId());
                    return;
                case 270:
                    okBtn.setNextFocusUpId(cancelBtn.getId());
                    cancelBtn.setNextFocusDownId(okBtn.getId());
                    okBtn.setNextFocusRightId(playBtn.getId());
                    cancelBtn.setNextFocusRightId(playBtn.getId());
                    playBtn.setNextFocusLeftId(okBtn.getId());
                    return;
                default:
                    return;
            }
        }
    }

    private void initCommandLayout(int degree) {
        boolean isPortrait;
        LinearLayout commandLayout = (LinearLayout) this.mGet.findViewById(C0088R.id.command_button_layout);
        LayoutParams rlp = (LayoutParams) commandLayout.getLayoutParams();
        LinearLayout.LayoutParams buttonLp = (LinearLayout.LayoutParams) ((LinearLayout) this.mGet.findViewById(C0088R.id.button_wrapper)).getLayoutParams();
        if (degree == 0 || degree == 180) {
            isPortrait = true;
        } else {
            isPortrait = false;
        }
        int navibarHeight = RatioCalcUtil.getNavigationBarHeight(this.mGet.getAppContext());
        if (isPortrait) {
            rlp.setMarginStart(0);
            rlp.setMarginEnd(0);
            if (degree != 0) {
                navibarHeight = 0;
            }
            rlp.bottomMargin = navibarHeight;
        } else {
            rlp.bottomMargin = 0;
            if (degree == 270) {
                rlp.setMarginStart(0);
                rlp.setMarginEnd(navibarHeight);
            } else {
                rlp.setMarginStart(navibarHeight);
                rlp.setMarginEnd(0);
            }
        }
        commandLayout.setLayoutParams(rlp);
    }

    public void requestPostViewButtonFocus() {
        if (this.mParam.getContentType() == 0) {
            ((Button) this.mGet.findViewById(C0088R.id.done_button)).requestFocus();
        } else {
            ((ImageButton) this.mGet.findViewById(C0088R.id.btn_play)).requestFocus();
        }
    }
}
