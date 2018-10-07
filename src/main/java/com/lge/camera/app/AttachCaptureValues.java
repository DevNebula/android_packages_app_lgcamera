package com.lge.camera.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.io.File;

public class AttachCaptureValues {
    private String mIntentFrom = null;
    private boolean mRequestSingleImage = false;
    private Uri mTargetUri = null;

    public void setupCaptureParams(Intent intent) {
        if (intent != null) {
            Bundle myExtras = intent.getExtras();
            if (myExtras != null) {
                this.mTargetUri = (Uri) myExtras.getParcelable("output");
                this.mRequestSingleImage = myExtras.getBoolean("sigleimage", false);
                this.mIntentFrom = myExtras.getString("intentFrom");
                CamLog.m7i(CameraConstants.TAG, String.format("mSaveUri: %s, intentFrom:%s", new Object[]{this.mTargetUri, this.mIntentFrom}));
                return;
            }
            CamLog.m7i(CameraConstants.TAG, String.format("no extra values", new Object[0]));
        }
    }

    public Uri getTargetUri() {
        return this.mTargetUri;
    }

    public boolean hasTargetUri() {
        return this.mRequestSingleImage && this.mTargetUri != null;
    }

    public String getIntentFrom() {
        return this.mIntentFrom;
    }

    public boolean isRequestedSingleImage() {
        return this.mRequestSingleImage;
    }

    public boolean isDirectlyGoingToCropGallery() {
        return this.mTargetUri == null && this.mIntentFrom == null;
    }

    public void preProcessSaveUri(Uri savedUri) {
        if (savedUri == null) {
            CamLog.m7i(CameraConstants.TAG, "savedUri is null.");
        } else if (this.mTargetUri == null) {
            CamLog.m7i(CameraConstants.TAG, "TargetUri is null.");
        } else if ("content".equals(this.mTargetUri.getScheme())) {
            File tempFile = new File(savedUri.getPath());
            if (tempFile.exists()) {
                CamLog.m3d(CameraConstants.TAG, "temp file(" + tempFile.getPath() + ") deleted : " + tempFile.delete());
            }
        } else {
            String requestedPath = this.mTargetUri.getPath();
            requestedPath = requestedPath.substring(0, requestedPath.lastIndexOf(47));
            CamLog.m3d(CameraConstants.TAG, "Requested directory:" + requestedPath);
            File requestedDir = new File(requestedPath);
            if (!requestedDir.exists()) {
                CamLog.m3d(CameraConstants.TAG, "Requested directory not exist, make it.");
                requestedDir.mkdirs();
            }
        }
    }
}
