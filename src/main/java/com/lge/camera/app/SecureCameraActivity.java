package com.lge.camera.app;

import com.lge.camera.C0088R;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.managers.SecureThumbnailListManager;
import com.lge.camera.managers.ThumbnailListManager;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.AppControlUtilBase;
import com.lge.camera.util.SecureImageUtil;

public class SecureCameraActivity extends QuickWindowCameraActivity {
    private boolean mPausedBySecureCamera = true;

    protected void changeThemeForActionBar() {
        setTheme(C0088R.style.SecureThemeActionBar);
    }

    protected void initModuleOnCreate() {
        if (FunctionProperties.isSupportedCameraRoll()) {
            SecureImageUtil.setSecureCamera(this);
            this.mThumbnailListManager = new SecureThumbnailListManager(this);
        }
        super.initModuleOnCreate();
    }

    public void onResume() {
        if (FunctionProperties.isSupportedCameraRoll()) {
            boolean isSecure = SecureImageUtil.useSecureLockImage();
            if (this.mThumbnailListManager == null || isSecure != this.mPausedBySecureCamera) {
                if (isSecure) {
                    this.mThumbnailListManager = new SecureThumbnailListManager(this);
                } else {
                    this.mThumbnailListManager = new ThumbnailListManager(this);
                }
            } else if (!(!isSecure || AppControlUtil.isGalleryLaunched() || AppControlUtilBase.isVideoLaunched())) {
                this.mThumbnailListManager.closeDetailViewBySecureUnlock();
                SecureImageUtil.get().release();
            }
        }
        super.onResume();
    }

    public void onPause() {
        if (SecureImageUtil.useSecureLockImage()) {
            this.mPausedBySecureCamera = true;
        } else {
            this.mPausedBySecureCamera = false;
        }
        super.onPause();
    }
}
