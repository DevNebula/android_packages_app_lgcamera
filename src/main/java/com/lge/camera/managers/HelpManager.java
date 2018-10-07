package com.lge.camera.managers;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import com.lge.camera.C0088R;
import com.lge.camera.app.HelpListAdapter;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.HelpItemGroup;
import com.lge.camera.util.HelpItemGroup.HelpItem;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;

public class HelpManager implements OnRemoveHandler {
    private final String QHELP_CATEGORY_APP_HELP = "com.lge.qhelp.intent.category.help";
    private final String QHELP_CATEGORY_LOAD_URL = "com.lge.qhelp.intent.category.connect.url";
    private final String QHELP_PACKAGE_NAME = "com.lge.qhelp";
    private final String QHELP_URI = "content://com.lge.qhelp";
    private final String VALID_LANGUAGE = "valid_language";
    protected HelpInterface mGet = null;
    private HelpItemGroup mHelpItemGroup;
    protected ArrayList<HelpItem> mHelpItemList = new ArrayList();
    private HelpListAdapter mHelpListAdapter = null;
    protected LinearLayout mHelpListLayout = null;
    protected RotateLayout mHelpListRotateLayout = null;
    private ListView mHelpListView = null;
    private int mQuickButtonId = -1;

    /* renamed from: com.lge.camera.managers.HelpManager$1 */
    class C09911 implements OnTouchListener {
        C09911() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            return true;
        }
    }

    public HelpManager(HelpInterface helpInterface) {
        this.mGet = helpInterface;
    }

    public void onHelpButtonClicked(int quickButtonId) {
        if (!checkQHelpPreInstalled("com.lge.qhelp")) {
            CamLog.m3d(CameraConstants.TAG, "QHelp not installed");
            this.mQuickButtonId = quickButtonId;
            callLocalHelp();
        } else if (checkSupportLanguage()) {
            makeIntentToQHelpAppHelp();
        } else {
            CamLog.m3d(CameraConstants.TAG, "QHelp not supported language");
            this.mQuickButtonId = quickButtonId;
            callLocalHelp();
        }
    }

    private String getMetaData() {
        String metaData = this.mGet.getShotMode();
        if (metaData != null && !metaData.contains(CameraConstants.MODE_BEAUTY)) {
            return metaData;
        }
        CamLog.m3d(CameraConstants.TAG, "QHelp metaData is null or beauty");
        return "mode_normal";
    }

    private void callLocalHelp() {
        this.mGet.setReturnFromHelp(true);
        this.mGet.onCallLocalHelp();
        LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_HELP);
    }

    private boolean checkQHelpPreInstalled(String packageName) {
        try {
            this.mGet.getActivity().getPackageManager().getPackageInfo(packageName, 1);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private boolean checkSupportLanguage() {
        boolean isValid = false;
        Cursor cursor = null;
        try {
            cursor = this.mGet.getActivity().getContentResolver().query(Uri.parse("content://com.lge.qhelp"), null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                isValid = cursor.getInt(cursor.getColumnIndex("valid_language")) > 0;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isValid;
    }

    private void makeIntentToQHelpAppHelp() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.putExtra("packagename", this.mGet.getAppContext().getPackageName());
        String metaData = getMetaData();
        if ("mode_normal".equals(metaData)) {
            intent.addCategory("com.lge.qhelp.intent.category.help");
        } else {
            intent.addCategory("com.lge.qhelp.intent.category.connect.url");
        }
        intent.putExtra("keyword", metaData);
        CamLog.m3d(CameraConstants.TAG, "QHelp jump to " + metaData + " page");
        this.mGet.getActivity().startActivity(intent);
    }

    private HelpListAdapter createHelpListAdapter() {
        return new HelpListAdapter(this.mGet.getActivity(), this.mHelpItemList);
    }

    protected void initHelpListLayout() {
        initializeHelpItemGroup();
        if (this.mHelpItemList == null || this.mHelpItemList.size() == 0) {
            CamLog.m11w(CameraConstants.TAG, "Item List is null or empty.");
            return;
        }
        if (this.mHelpListLayout == null) {
            this.mHelpListLayout = (LinearLayout) this.mGet.getActivity().getLayoutInflater().inflate(C0088R.layout.help_list_layout, null);
        }
        ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.contents_base);
        if (!(vg == null || this.mHelpListLayout == null)) {
            vg.addView(this.mHelpListLayout);
            this.mHelpListRotateLayout = (RotateLayout) vg.findViewById(C0088R.id.help_rotate_layout);
            this.mHelpListView = (ListView) vg.findViewById(C0088R.id.help_list_view);
            RatioCalcUtil.setLengthSameWithQuickButtonWidth(this.mGet.getAppContext(), this.mHelpListLayout.findViewById(C0088R.id.help_top_margin_view), false, false);
        }
        this.mHelpListAdapter = createHelpListAdapter();
        if (this.mHelpListView != null && this.mHelpListLayout != null && this.mHelpListRotateLayout != null) {
            this.mHelpListView.setAdapter(this.mHelpListAdapter);
            LayoutParams lp = (LayoutParams) this.mHelpListRotateLayout.getLayoutParams();
            if (lp != null) {
                lp.height = (Utils.getLCDsize(this.mGet.getAppContext(), true)[1] * 4) / 3;
                this.mHelpListRotateLayout.setLayoutParams(lp);
                setLayoutDegree(this.mGet.getOrientationDegree(), false);
                this.mHelpListRotateLayout.setOnTouchListener(new C09911());
                this.mHelpListLayout.setVisibility(0);
                CamLog.m3d(CameraConstants.TAG, "displayHelpListLayout - end");
            }
        }
    }

    private void removeHelpListView() {
        if (this.mHelpListLayout != null) {
            this.mGet.setQuickButtonSelected(this.mQuickButtonId, false);
            this.mHelpListLayout.setVisibility(8);
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
            if (!(vg == null || this.mHelpListLayout == null)) {
                vg.removeView(this.mHelpListLayout);
            }
            this.mQuickButtonId = -1;
        }
        this.mHelpListLayout = null;
    }

    public void onConfigurationChanged(Configuration config) {
        if (this.mHelpListRotateLayout != null) {
            this.mHelpListRotateLayout.requestLayout();
            onOrientationChanged(this.mGet.getOrientationDegree());
        }
    }

    public void onOrientationChanged(int degree) {
        if (checkComponents()) {
            setLayoutDegree(degree, true);
        }
    }

    private void setLayoutDegree(int degree, boolean animation) {
        if (this.mHelpListRotateLayout != null && this.mHelpListAdapter != null && this.mHelpListView != null) {
            int convertDegree = degree % 360;
            this.mHelpListRotateLayout.rotateLayout(convertDegree);
            this.mHelpListAdapter.setHelpListDegree(convertDegree);
        }
    }

    public void hide(boolean showAnimation) {
        if (this.mHelpListLayout != null) {
            CamLog.m3d(CameraConstants.TAG, "hideHelpList, animation : " + showAnimation);
            if (this.mGet.isPaused() || !showAnimation) {
                removeHelpListView();
            } else {
                helpListAnimation(this.mHelpListLayout, false);
            }
        }
    }

    public void showHelpList(boolean useAnim) {
        CamLog.m3d(CameraConstants.TAG, "showHelpList, animation : " + useAnim);
        initHelpListLayout();
        if (useAnim) {
            helpListAnimation(this.mHelpListLayout, true);
        } else {
            this.mHelpListLayout.setVisibility(0);
            moveToPosition();
        }
        this.mGet.setQuickButtonSelected(this.mQuickButtonId, true);
    }

    private void moveToPosition() {
        int helpPage = getHelpPage();
        if (this.mHelpItemList != null && this.mHelpListView != null && helpPage != 0) {
            Iterator it = this.mHelpItemList.iterator();
            while (it.hasNext()) {
                HelpItem item = (HelpItem) it.next();
                if (item.mHelpNum == helpPage) {
                    this.mHelpListView.setSelection(this.mHelpItemList.indexOf(item));
                    return;
                }
            }
        }
    }

    private int getHelpPage() {
        String shotMode = this.mGet.getShotMode();
        if (shotMode == null) {
            return 0;
        }
        if (this.mGet.isManualMode()) {
            return 5;
        }
        if (CameraConstants.MODE_PANORAMA_LG_360_PROJ.equals(shotMode)) {
            return 15;
        }
        if (shotMode.contains(CameraConstants.MODE_PANORAMA)) {
            return 11;
        }
        if (CameraConstants.MODE_FLASH_JUMPCUT.equals(shotMode)) {
            return 12;
        }
        if ("mode_food".equals(shotMode)) {
            return 9;
        }
        if (CameraConstants.MODE_MULTIVIEW.equals(shotMode)) {
            return 20;
        }
        if (CameraConstants.MODE_POPOUT_CAMERA.equals(shotMode)) {
            return 8;
        }
        if (CameraConstants.MODE_SLOW_MOTION.equals(shotMode)) {
            return 10;
        }
        if (shotMode.contains(CameraConstants.MODE_SNAP)) {
            return 13;
        }
        if (CameraConstants.MODE_TIME_LAPSE_VIDEO.equals(shotMode)) {
            return 14;
        }
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(shotMode)) {
            return 16;
        }
        if (CameraConstants.MODE_SQUARE_SPLICE.equals(shotMode)) {
            return 17;
        }
        if (CameraConstants.MODE_SQUARE_GRID.equals(shotMode)) {
            return 18;
        }
        if (CameraConstants.MODE_SQUARE_OVERLAP.equals(shotMode)) {
            return 19;
        }
        if (CameraConstants.MODE_CINEMA.equals(shotMode)) {
            return 6;
        }
        if (CameraConstants.MODE_DUAL_POP_CAMERA.equals(shotMode)) {
            return 7;
        }
        if (shotMode.contains(CameraConstants.MODE_SMART_CAM)) {
            return 4;
        }
        if (CameraConstants.MODE_REAR_OUTFOCUS.equals(shotMode) || CameraConstants.MODE_FRONT_OUTFOCUS.equals(shotMode)) {
            return 3;
        }
        return 0;
    }

    private boolean initializeHelpItemGroup() {
        if (this.mHelpItemList == null) {
            return false;
        }
        this.mHelpItemList.clear();
        this.mHelpItemGroup = new HelpItemGroup();
        CameraParameters params = null;
        if (this.mGet.getCameraDevice() != null) {
            params = this.mGet.getCameraDevice().getParameters();
        }
        this.mHelpItemGroup.initialize(params, 0, this.mGet.getAppContext());
        this.mHelpItemList = this.mHelpItemGroup.getHelpItemList();
        return true;
    }

    public boolean isVisible() {
        return this.mHelpListLayout != null && this.mHelpListLayout.isShown();
    }

    private void helpListAnimation(final View aniView, final boolean show) {
        CamLog.m3d(CameraConstants.TAG, "HelpListAnimation-start : show = " + show);
        AnimationUtil.startShowingAnimation(aniView, show, show ? 300 : 150, new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                HelpManager.this.mGet.setQuickButtonSelected(HelpManager.this.mQuickButtonId, show);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (aniView == null) {
                    return;
                }
                if (show) {
                    aniView.setVisibility(0);
                    HelpManager.this.moveToPosition();
                    return;
                }
                HelpManager.this.mGet.postOnUiThread(new HandlerRunnable(HelpManager.this) {
                    public void handleRun() {
                        HelpManager.this.removeHelpListView();
                    }
                }, 0);
            }
        });
    }

    public void onResume() {
    }

    public void onPause() {
        CamLog.m3d(CameraConstants.TAG, "onPause-start");
        if (isVisible()) {
            hide(true);
        }
        CamLog.m3d(CameraConstants.TAG, "onPause-end");
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "onDestroy");
        this.mHelpListRotateLayout = null;
        this.mHelpListView = null;
        this.mHelpListAdapter = null;
        this.mHelpItemList = null;
        this.mHelpItemGroup = null;
        this.mHelpListLayout = null;
    }

    public boolean isNeedHelpItem() {
        return getHelpListSize() > 1;
    }

    private int getHelpListSize() {
        if (this.mHelpItemGroup == null && !initializeHelpItemGroup()) {
            return 0;
        }
        if (this.mHelpItemList != null && this.mHelpItemList.size() != 0) {
            return this.mHelpItemGroup.getHelpListSize();
        }
        CamLog.m11w(CameraConstants.TAG, "Item List is null or empty.");
        return 0;
    }

    private boolean checkComponents() {
        return (this.mHelpListRotateLayout == null || this.mHelpListAdapter == null || this.mHelpListView == null) ? false : true;
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }
}
