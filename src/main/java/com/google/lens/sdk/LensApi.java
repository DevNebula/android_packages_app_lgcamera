package com.google.lens.sdk;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardDismissCallback;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.google.android.apps.lens.library.base.LensSdkParamsReader;
import com.google.android.apps.lens.library.base.LensServiceBridge;
import com.google.android.apps.lens.library.base.proto.nano.LensSdkParamsProto.LensSdkParams;

public class LensApi {
    @VisibleForTesting
    static final String LENS_BITMAP_URI_KEY = "LensBitmapUriKey";
    @VisibleForTesting
    static final String LENS_DEEPLINKING_STRING = "googleapp://lens";
    @VisibleForTesting
    static final Uri LENS_DEEPLINKING_URI = Uri.parse(LENS_DEEPLINKING_STRING);
    private static final String TAG = "LensApi";
    private final Context context;
    private final LensSdkParamsReader paramsReader;
    private final LensServiceBridge serviceBridge;

    public interface LensAvailabilityCallback {
        void onAvailabilityStatusFetched(@LensAvailabilityStatus int i);
    }

    public @interface LensAvailabilityStatus {
        public static final int LENS_READY = 0;
        public static final int LENS_UNAVAILABLE = 1;
        public static final int LENS_UNAVAILABLE_DEVICE_INCOMPATIBLE = 3;
        public static final int LENS_UNAVAILABLE_DEVICE_LOCKED = 5;
        public static final int LENS_UNAVAILABLE_LOCALE_NOT_SUPPORTED = 2;
        public static final int LENS_UNAVAILABLE_UNKNOWN_ERROR_CODE = 6;
    }

    public @interface LensFeature {
        public static final int LENS_AR_STICKERS = 1;
        public static final int LENS_CORE = 0;
    }

    private static final class LensSdkParamsCallback implements com.google.android.apps.lens.library.base.LensSdkParamsReader.LensSdkParamsCallback {
        private final LensAvailabilityCallback lensAvailabilityCallback;

        LensSdkParamsCallback(@NonNull LensAvailabilityCallback callback) {
            this.lensAvailabilityCallback = callback;
        }

        public void onLensSdkParamsAvailable(LensSdkParams params) {
            this.lensAvailabilityCallback.onAvailabilityStatusFetched(LensApi.mapInternalLensAvailabilityToExternal(params.lensAvailabilityStatus));
        }
    }

    public LensApi(Context context) {
        this.context = context;
        this.paramsReader = new LensSdkParamsReader(context);
        this.serviceBridge = new LensServiceBridge(context);
    }

    public void onResume() {
        this.serviceBridge.bindService();
    }

    public void onPause() {
        this.serviceBridge.unbindService();
    }

    public void launchLensActivity(final Activity activity) {
        KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService("keyguard");
        if (!keyguardManager.isKeyguardLocked()) {
            startLensActivity(activity);
        } else if (VERSION.SDK_INT >= 26) {
            keyguardManager.requestDismissKeyguard(activity, new KeyguardDismissCallback() {
                public void onDismissSucceeded() {
                    Log.d(LensApi.TAG, "Keyguard successfully dismissed");
                    LensApi.this.startLensActivity(activity);
                }

                public void onDismissError() {
                    Log.e(LensApi.TAG, "Error dismissing keyguard");
                }

                public void onDismissCancelled() {
                    Log.d(LensApi.TAG, "Keyguard dismiss cancelled");
                }
            });
        } else {
            Log.e(TAG, "Cannot start Lens when device is locked with Android " + VERSION.SDK_INT);
        }
    }

    public void launchLensActivity(Activity activity, @LensFeature int feature) {
        switch (feature) {
            case 0:
                launchLensActivity(activity);
                return;
            case 1:
                if (this.paramsReader.getArStickersAvailability() == 0) {
                    Intent intent = new Intent();
                    intent.setClassName("com.google.ar.lens", LensSdkParamsReader.LENS_AR_STICKERS_ACTIVITY);
                    activity.startActivity(intent);
                    return;
                }
                return;
            default:
                Log.w(TAG, "Invalid lens activity: " + feature);
                return;
        }
    }

    private void startLensActivity(Activity activity) {
        if (this.serviceBridge.prewarmLensActivity()) {
            Log.i(TAG, "Lens is pre-warmed.");
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(LENS_DEEPLINKING_STRING));
        activity.startActivityForResult(intent, 0);
    }

    public void launchLensActivityWithBitmapUri(Activity activity, Uri bitmapUri) {
        if (bitmapUri == null) {
            Log.e(TAG, "Image URI is null!");
            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(appendBitmapUri(LENS_DEEPLINKING_URI, bitmapUri));
        activity.startActivityForResult(intent, 0);
    }

    public void checkLensAvailability(LensAvailabilityCallback callback) {
        if (!((KeyguardManager) this.context.getSystemService("keyguard")).isKeyguardLocked() || VERSION.SDK_INT >= 26) {
            this.paramsReader.getParams(new LensSdkParamsCallback(callback));
        } else {
            callback.onAvailabilityStatusFetched(5);
        }
    }

    public void checkArStickersAvailability(LensAvailabilityCallback callback) {
        callback.onAvailabilityStatusFetched(mapInternalLensAvailabilityToExternal(this.paramsReader.getArStickersAvailability()));
    }

    private Uri appendBitmapUri(Uri baseUri, Uri bitmapUri) {
        if (baseUri == null || bitmapUri == null) {
            return baseUri;
        }
        return baseUri.buildUpon().appendQueryParameter(LENS_BITMAP_URI_KEY, bitmapUri.toString()).build();
    }

    @LensAvailabilityStatus
    private static int mapInternalLensAvailabilityToExternal(int internalLensAvailabilityStatus) {
        switch (internalLensAvailabilityStatus) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 5:
            case 6:
                return internalLensAvailabilityStatus;
            default:
                Log.d(TAG, "Internal error code: " + internalLensAvailabilityStatus);
                return 1;
        }
    }
}
