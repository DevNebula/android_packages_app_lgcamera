package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class GalleryDeleteReceiver extends CameraBroadCastReceiver {
    public static final String ACTION_DELETE = "com.lge.gallery.action.DELETE";
    public static final String KEY_DELETE_DONE = "lge-delete-done";
    public static final String KEY_DELETE_MEDIA_URI = "lge-delete-media-uri";

    public GalleryDeleteReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        CamLog.m3d(CameraConstants.TAG, "[gallery] onReceive. ACTION_DELETE ? " + ACTION_DELETE.equals(intent.getAction()) + ", DELETE_DONE ? " + intent.getBooleanExtra(KEY_DELETE_DONE, true));
        String deletedUri = intent.getStringExtra(KEY_DELETE_MEDIA_URI);
        if (deletedUri != null && this.mGet != null) {
            CamLog.m3d(CameraConstants.TAG, "[gallery] deleted URI : " + deletedUri);
            this.mGet.setDeletedUriFromGallery(Uri.parse(deletedUri));
        }
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter(ACTION_DELETE);
        intentFilter.addAction(ACTION_DELETE);
        return intentFilter;
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
