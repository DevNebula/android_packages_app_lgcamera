package com.lge.camera.managers.ext.sticker.utils;

import android.content.Context;
import android.content.res.Resources;
import com.lge.camera.util.CamLog;
import java.io.InputStream;

public class ResourceLoader {
    private static String TAG = "ResourceLoader";

    public class RemoteRawResource {
        private final String mPackageName;
        private final String mResId;

        public RemoteRawResource(String packageName, String name) {
            this.mPackageName = packageName;
            this.mResId = packageName + ":raw/" + name;
        }

        public String getId() {
            return this.mResId;
        }

        public InputStream getRawResource(Context context) {
            InputStream inputStream = null;
            try {
                Resources remoteResource = context.getPackageManager().getResourcesForApplication(this.mPackageName);
                return remoteResource.openRawResource(remoteResource.getIdentifier(this.mResId, null, null));
            } catch (Throwable e) {
                CamLog.m11w(ResourceLoader.TAG, "fail to get raw resource(" + this.mResId + ") : " + e);
                return inputStream;
            }
        }
    }
}
