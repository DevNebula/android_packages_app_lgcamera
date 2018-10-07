package com.lge.camera.postview;

import android.view.View;
import android.view.ViewGroup;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SystemBarUtil;

public class PostviewNormal extends PostviewBase {
    public PostviewNormal(PostviewBridge postviewBridge) {
        super(postviewBridge);
    }

    public void setupPostviewActionBar() {
        SystemBarUtil.setActionBarAnim(this.mGet.getActivity(), true);
        super.setupActionBar(true, false);
    }

    public void setupLayout() {
        super.setupLayout();
        ViewGroup vg = (ViewGroup) getBaseLayout();
        if (vg != null) {
            vg.addView(this.mGet.inflateView(C0088R.layout.postview_normal));
        }
    }

    public void releaseLayout() {
        ViewGroup vg = (ViewGroup) getBaseLayout();
        if (vg != null) {
            View postView = this.mGet.findViewById(C0088R.id.postview_normal);
            if (postView != null) {
                vg.removeView(postView);
            }
        }
        super.releaseLayout();
    }

    public void postviewShow() {
        CamLog.m3d(CameraConstants.TAG, "TIME_CHECK show()");
        View postView = this.mGet.findViewById(C0088R.id.postview_normal);
        if (postView == null) {
            CamLog.m11w(CameraConstants.TAG, "postviewShow : inflate view fail.");
            return;
        }
        loadSingleCapturedImages();
        startPostviewAnimation(postView, true);
    }

    public int getPostviewType() {
        return 0;
    }
}
