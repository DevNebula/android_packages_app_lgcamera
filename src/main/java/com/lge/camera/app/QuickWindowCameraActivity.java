package com.lge.camera.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.Utils;

public class QuickWindowCameraActivity extends CameraActivity {
    protected static final String ACTION_COVERAPP_REGI_EVENT = "com.lge.coverapp.intent.action.regi";
    protected static final String EXTRA_COVERAPP_APPNAME = "com.lge.coverapp.intent.extra.name";
    protected static final String EXTRA_COVERAPP_REGI = "com.lge.coverapp.intent.extra.regi";
    private int mCoverType = 5;
    protected boolean mIsStartedFromQuickCover = false;
    private final Object mLockChangeModule = new Object();

    /* renamed from: com.lge.camera.app.QuickWindowCameraActivity$1 */
    class C03361 implements OnClickListener {
        C03361() {
        }

        public void onClick(View arg0) {
            QuickWindowCameraActivity.this.finish();
        }
    }

    protected void createBroadCastReceiver(int receiverType) {
        if (!AppControlUtil.isQuickTools(getIntent())) {
            receiverType |= 8;
        }
        super.createBroadCastReceiver(receiverType);
    }

    protected void onCreate(Bundle savedInstanceState) {
        this.mCoverType = QuickWindowUtils.getSmartCoverManager(getActivity()).getCoverType();
        Intent intent = getIntent();
        boolean isCameraSwitching = false;
        boolean isSecure = SecureImageUtil.isSecureCameraIntent(intent);
        QuickWindowUtils.setQuickWindowCameraFromIntent(this, intent, isSecure);
        if (QuickWindowUtils.isQuickWindowCameraMode()) {
            this.mIsStartedFromQuickCover = true;
        }
        if (QuickWindowUtils.getCurrentCoverStatus(this) == 2) {
            AppControlUtil.configureWindowFlag(getWindow(), false, isSecure, true, true);
        }
        if (this.mCoverType == 4 && QuickWindowUtils.isQuickWindowCameraMode() && SharedPreferenceUtil.getCameraId(getAppContext()) == 0) {
            isCameraSwitching = true;
            SharedPreferenceUtilBase.setCameraId(getAppContext(), 1);
        }
        super.onCreate(savedInstanceState);
        if (isCameraSwitching) {
            setSetting(Setting.KEY_SWAP_CAMERA, "front", true);
            setupSetting();
        }
    }

    private boolean isSupportedFrontCover() {
        if (this.mCoverType == 4) {
            return true;
        }
        return false;
    }

    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        CamLog.m3d(CameraConstants.TAG, "onResume() mIsCheckingPermission : " + this.mIsCheckingPermission);
        if (this.mIsCheckingPermission) {
            CamLog.m3d(CameraConstants.TAG, "onResume() QuickWindowUtils.isQuickWindowCameraMode() : " + QuickWindowUtils.isQuickWindowCameraMode());
            if (QuickWindowUtils.isQuickWindowCameraMode()) {
                setContentView(C0088R.layout.quick_circle_no_permission);
                initModuleForPermissionCheck();
            }
            super.onResume();
            return;
        }
        if (this.mCurrentModule instanceof AKACoverCameraModule) {
            sendAKACoverUiIntent(true);
        }
        super.onResume();
    }

    public void onPause() {
        resetCoverView();
        super.onPause();
        QuickWindowUtils.setQuickWindowCameraMode(false);
        sendAKACoverUiIntent(false);
    }

    private void initModuleForPermissionCheck() {
        CamLog.m3d(CameraConstants.TAG, "initModuleForPermissionCheck");
        getWindow().addFlags(6815744);
        createBroadCastReceiver(1);
        initPermissionLayout();
    }

    private void initPermissionLayout() {
        int id_width = getActivity().getResources().getIdentifier("config_circle_window_width", "dimen", "com.lge.internal");
        int id_height = getActivity().getResources().getIdentifier("config_circle_window_height", "dimen", "com.lge.internal");
        int id_y_pos = getActivity().getResources().getIdentifier("config_circle_window_y_pos", "dimen", "com.lge.internal");
        int mCoverWidth = getActivity().getResources().getDimensionPixelSize(id_width);
        int mCoverHeight = getActivity().getResources().getDimensionPixelSize(id_height);
        int top_margin = getActivity().getResources().getDimensionPixelSize(id_y_pos);
        if (mCoverHeight == 0) {
            mCoverHeight = Utils.getPx(getAppContext(), C0088R.dimen.quick_circle_width);
        }
        LinearLayout coverView = (LinearLayout) findViewById(C0088R.id.quick_circle_permissions_layout);
        LayoutParams permissionCover = (LayoutParams) coverView.getLayoutParams();
        permissionCover.width = mCoverWidth;
        permissionCover.height = mCoverHeight;
        permissionCover.topMargin = top_margin;
        coverView.setLayoutParams(permissionCover);
        ((ImageView) findViewById(C0088R.id.quick_circle_back_btn)).setOnClickListener(new C03361());
    }

    protected boolean selectOtherModule() {
        if (super.selectOtherModule()) {
            return true;
        }
        Intent intent = getIntent();
        QuickWindowUtils.setQuickWindowCameraFromIntent(this, intent, SecureImageUtil.isSecureCameraIntent(intent));
        if (!QuickWindowUtils.isQuickWindowCameraMode()) {
            return false;
        }
        if (this.mCoverType == 5) {
            this.mCoverType = QuickWindowUtils.getSmartCoverManager(getActivity()).getCoverType();
        }
        if (this.mCoverType == 4) {
            this.mCurrentModule = new AKACoverCameraModule(this);
            this.mModuleMap.put(CameraConstants.MODE_AKA_CAMERA, this.mCurrentModule);
            return true;
        } else if (this.mCoverType == 6) {
            this.mCurrentModule = new DznyCoverCameraModule(this);
            this.mModuleMap.put(CameraConstants.MODE_DISNEY_CAMERA, this.mCurrentModule);
            return true;
        } else {
            this.mCurrentModule = new QuickCircleCameraModule(this);
            this.mModuleMap.put(CameraConstants.MODE_QUICKCIRCLE_CAMERA, this.mCurrentModule);
            return true;
        }
    }

    public void changeModuleByCoverstate() {
        CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] changeModuleByCoverstate");
        synchronized (this.mLockChangeModule) {
            if (QuickWindowUtils.isQuickWindowCaseClosed()) {
                sendAKACoverUiIntent(false);
            } else if (!(this.mCurrentModule instanceof QuickCoverModuleBase)) {
            } else if (AppControlUtil.isGalleryLaunched()) {
            } else {
                super.changeModule();
            }
        }
    }

    public boolean isStartedFromQuickCover() {
        return this.mIsStartedFromQuickCover;
    }

    /* JADX WARNING: Missing block: B:22:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:24:?, code:
            return;
     */
    protected void changeModuleOnResume() {
        /*
        r3 = this;
        r0 = "CameraApp";
        r1 = "-acq- [QUICK COVER] changeModuleOnResume";
        com.lge.camera.util.CamLog.m3d(r0, r1);
        r1 = r3.mLockChangeModule;
        monitor-enter(r1);
        r0 = r3.mCurrentModule;	 Catch:{ all -> 0x0027 }
        if (r0 == 0) goto L_0x0014;
    L_0x000e:
        r0 = r3.isFinishing();	 Catch:{ all -> 0x0027 }
        if (r0 == 0) goto L_0x0016;
    L_0x0014:
        monitor-exit(r1);	 Catch:{ all -> 0x0027 }
    L_0x0015:
        return;
    L_0x0016:
        r0 = com.lge.camera.util.QuickWindowUtils.isQuickWindowCaseClosed();	 Catch:{ all -> 0x0027 }
        if (r0 != 0) goto L_0x0022;
    L_0x001c:
        r0 = r3.mCurrentModule;	 Catch:{ all -> 0x0027 }
        r0 = r0 instanceof com.lge.camera.app.QuickCoverModuleBase;	 Catch:{ all -> 0x0027 }
        if (r0 != 0) goto L_0x002a;
    L_0x0022:
        super.changeModuleOnResume();	 Catch:{ all -> 0x0027 }
        monitor-exit(r1);	 Catch:{ all -> 0x0027 }
        goto L_0x0015;
    L_0x0027:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0027 }
        throw r0;
    L_0x002a:
        r0 = r3.mCurrentModule;	 Catch:{ all -> 0x0027 }
        r0.onDestroy();	 Catch:{ all -> 0x0027 }
        r0 = r3.mCurrentModule;	 Catch:{ all -> 0x0027 }
        r0.restoreSettingMenus();	 Catch:{ all -> 0x0027 }
        r3.selectModule();	 Catch:{ all -> 0x0027 }
        r0 = r3.mCurrentModule;	 Catch:{ all -> 0x0027 }
        if (r0 == 0) goto L_0x0050;
    L_0x003b:
        r0 = r3.mHybridView;	 Catch:{ all -> 0x0027 }
        r2 = r3.mCurrentModule;	 Catch:{ all -> 0x0027 }
        r2 = r2.getClass();	 Catch:{ all -> 0x0027 }
        r0.acquireHybridView(r2);	 Catch:{ all -> 0x0027 }
        r0 = r3.mCurrentModule;	 Catch:{ all -> 0x0027 }
        r0.startCameraDeviceOnCreate();	 Catch:{ all -> 0x0027 }
        r0 = r3.mCurrentModule;	 Catch:{ all -> 0x0027 }
        r0.init();	 Catch:{ all -> 0x0027 }
    L_0x0050:
        monitor-exit(r1);	 Catch:{ all -> 0x0027 }
        goto L_0x0015;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.app.QuickWindowCameraActivity.changeModuleOnResume():void");
    }

    public int getSharedPreferenceCameraId() {
        if ((this.mCurrentModule == null && QuickWindowUtils.isQuickWindowCaseClosed() && !isSupportedFrontCover()) || (this.mCurrentModule instanceof QuickCircleCameraModule)) {
            return 0;
        }
        return SharedPreferenceUtil.getCameraId(getAppContext());
    }

    protected void resetCoverView() {
        TextureView view = getTextureView();
        if (view != null && isSupportedFrontCover()) {
            ImageView coverView = (ImageView) findViewById(C0088R.id.preview_cover_view);
            if (coverView != null && view != null) {
                LayoutParams lpCoverView = (LayoutParams) coverView.getLayoutParams();
                if (lpCoverView != null) {
                    lpCoverView.width = view.getWidth();
                    lpCoverView.height = view.getHeight();
                    lpCoverView.topMargin = view.getTop();
                    lpCoverView.setMarginStart(view.getLeft());
                    coverView.setLayoutParams(lpCoverView);
                }
            }
        }
    }

    private void sendAKACoverUiIntent(boolean regi) {
        Intent intent = new Intent(ACTION_COVERAPP_REGI_EVENT);
        intent.putExtra(EXTRA_COVERAPP_REGI, regi);
        intent.putExtra(EXTRA_COVERAPP_APPNAME, getActivity().getClass().getName());
        intent.addFlags(16777216);
        getActivity().sendBroadcast(intent);
    }
}
