package com.lge.camera.managers.ext.sticker.contents.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler;
import com.lge.camera.managers.ext.sticker.contents.scheduler.DeleteContentScheduler;
import com.lge.camera.util.CamLog;

public class StickerContentsReceiver extends BroadcastReceiver {
    public static final String ACTION_CONTENTS_DELETE_COMPLETED = "com.lge.camera.sticker.CONTENT_DELETE_COMPLETE";
    public static final String ACTION_CONTENTS_DOWNLOAD_COMPLETE = "com.lge.camera.sticker.CONTENT_DOWNLOAD_COMPLETE";
    public static final String TAG = "StickerContentsReceiver";

    public void onReceive(Context context, Intent intent) {
        CamLog.m3d(TAG, "StickerDonwloadReceiver intent received = " + intent.getAction());
        if (ACTION_CONTENTS_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            CamLog.m5e(TAG, "sticker Downloded!!!!!");
            DecompressScheduler ds = DecompressScheduler.getInstance(context);
            if (ds != null) {
                ds.excuteJob(intent.getData(), context.getFilesDir().getAbsolutePath());
            }
        } else if (ACTION_CONTENTS_DELETE_COMPLETED.equals(intent.getAction())) {
            CamLog.m5e(TAG, "sticker Deleted!!!!!!!");
            DeleteContentScheduler dcs = DeleteContentScheduler.getInstance(context);
            if (dcs != null) {
                dcs.excuteJob(intent.getData());
            }
        }
    }
}
