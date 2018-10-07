package com.lge.camera.managers;

import android.content.Context;
import android.location.LocationManager;
import android.support.p000v4.view.PagerAdapter;
import android.support.p000v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.Utils;
import java.util.List;

public class InitialGuideManagerBase extends ManagerInterfaceImpl {
    protected int mDegree = 0;
    protected LinearLayout mIndexLayout = null;
    protected RelativeLayout mIndexLayoutWrapper = null;
    protected List<Integer> mInitLandLayoutIdList = null;
    protected List<Integer> mInitLayoutIdList = null;
    protected InitPagerAdapter mInitPagerAdapter = null;
    protected RotateLayout mInitRotateLayout = null;
    protected ViewPager mInitViewPager = null;
    private int mInitialGuideStep = 0;
    private View mInitialGuideView = null;
    protected RelativeLayout mInitialHelpLayout = null;
    private InitialGuideListener mListener = null;
    protected List<InitHelpPage> mPageList = null;
    protected int mPagePosition = 0;
    protected String mPhotoSizeDefaultString = null;
    protected boolean mPhotoSizeIsChecked = true;
    protected String mPhotoSizeMaxString = null;
    protected RotateLayout mPhotoSizeRotateLayout = null;

    public interface InitialGuideListener {
        void onInitialGuideConfirm(int i);
    }

    /* renamed from: com.lge.camera.managers.InitialGuideManagerBase$1 */
    class C10351 implements AnimationListener {
        C10351() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            InitialGuideManagerBase.this.mGet.postOnUiThread(new HandlerRunnable(InitialGuideManagerBase.this) {
                public void handleRun() {
                    InitialGuideManagerBase.this.removeInitialGuideView();
                }
            }, 0);
        }
    }

    /* renamed from: com.lge.camera.managers.InitialGuideManagerBase$2 */
    class C10372 implements OnClickListener {
        C10372() {
        }

        public void onClick(View v) {
            InitialGuideManagerBase.this.mInitialGuideStep = InitialGuideManagerBase.this.mInitialGuideStep + 1;
            SharedPreferenceUtil.saveInitialGuideStep(InitialGuideManagerBase.this.getAppContext(), InitialGuideManagerBase.this.mInitialGuideStep);
            if (InitialGuideManagerBase.this.mInitialGuideStep < 2) {
                InitialGuideManagerBase.this.hideInitialGuide();
                if (InitialGuideManagerBase.this.mListener != null) {
                    InitialGuideManagerBase.this.mListener.onInitialGuideConfirm(InitialGuideManagerBase.this.mInitialGuideStep);
                    return;
                }
                return;
            }
            InitialGuideManagerBase.this.initialGuideCompleted();
        }
    }

    public class InitPagerAdapter extends PagerAdapter {
        public Object instantiateItem(View pager, int position) {
            Object v = null;
            if (!(InitialGuideManagerBase.this.mInitLayoutIdList == null || pager == null)) {
                List<Integer> list;
                if (InitialGuideManagerBase.this.mDegree == 0 || InitialGuideManagerBase.this.mDegree == 180) {
                    list = InitialGuideManagerBase.this.mInitLayoutIdList;
                } else {
                    list = InitialGuideManagerBase.this.mInitLandLayoutIdList;
                }
                v = InitialGuideManagerBase.this.mGet.getActivity().getLayoutInflater().inflate(((Integer) list.get(position)).intValue(), null);
                if (list != null && ((((Integer) list.get(position)).intValue() == C0088R.layout.init_help_graphy || ((Integer) list.get(position)).intValue() == C0088R.layout.init_help_graphy_land) && !FunctionProperties.isGraphyDefaultOn())) {
                    TextView desc = (TextView) v.findViewById(C0088R.id.desc_text_view);
                    if (desc != null) {
                        desc.setText(C0088R.string.graphy_init_guide_desc3);
                    }
                }
                if (InitialGuideManagerBase.this.mPageList != null && position < InitialGuideManagerBase.this.mPageList.size()) {
                    ((InitHelpPage) InitialGuideManagerBase.this.mPageList.get(position)).initUI(v, InitialGuideManagerBase.this.mDegree);
                }
                ((ViewPager) pager).addView(v);
            }
            return v;
        }

        public int getCount() {
            return InitialGuideManagerBase.this.mPageList == null ? 0 : InitialGuideManagerBase.this.mPageList.size();
        }

        public boolean isViewFromObject(View pager, Object obj) {
            return pager == obj;
        }

        public void destroyItem(View pager, int position, Object view) {
            ((ViewPager) pager).removeView((View) view);
        }
    }

    public InitialGuideManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setInitialGuideListener(InitialGuideListener listener) {
        this.mListener = listener;
    }

    public void onResumeAfter() {
        if (this.mGet != null) {
            this.mInitialGuideStep = 2;
            if (this.mInitialGuideStep < 2) {
                ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
                this.mInitialGuideView = this.mGet.inflateView(C0088R.layout.initial_guide);
                if (vg != null && this.mInitialGuideView != null) {
                    vg.addView(this.mInitialGuideView);
                    showInitialGuide(this.mInitialGuideStep);
                    setButtonListener();
                }
            } else if (!this.mGet.isPostviewShowing() && !this.mGet.getPhotoSizeHelpShown() && !isInitialHelpGuideVisible() && !showInitialHelp() && !this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_FULL_SCREEN_GUIDE)) {
                showInitDialog();
            }
        }
    }

    public boolean showInitialHelp() {
        return true;
    }

    public void showInitDialog() {
        if (!(QuickWindowUtils.isQuickWindowCameraMode() || SecureImageUtil.isSecureCamera() || ModelProperties.isKeyPadSupported(this.mGet.getAppContext()))) {
            if (SharedPreferenceUtil.getInitialTagLocation(this.mGet.getAppContext()) == 0 && !"on".equals(this.mGet.getSettingValue(Setting.KEY_TAG_LOCATION)) && CheckStatusManager.checkEnterApplication(getActivity(), true)) {
                this.mGet.showDialog(7, false);
                return;
            } else if (checkLocationPermissionCondition() && checkLocationService()) {
                this.mGet.showLocationPermissionRequestDialog(true);
                return;
            }
        }
        if (!showDialogInitStorage()) {
            showManualModeInitDialog();
        }
    }

    private boolean showManualModeInitDialog() {
        if (!FunctionProperties.isNeedToShowInitialManualModeDialog(this.mGet.getAppContext()) || !CameraConstants.MODE_MANUAL_CAMERA.equals(this.mGet.getShotMode())) {
            return false;
        }
        this.mGet.showDialog(148, true);
        return true;
    }

    public void setButtons() {
        if (this.mPageList != null) {
            setPreviousButton(this.mPagePosition);
            setNextButton(this.mPagePosition);
        }
    }

    protected void setPreviousButton(int position) {
        if (this.mPageList != null && this.mInitialHelpLayout != null) {
            View view = this.mInitialHelpLayout.findViewById(C0088R.id.previous_button);
            if (view != null) {
                int startPosition;
                if (Utils.isRTLLanguage()) {
                    startPosition = this.mPageList.size() - 1;
                } else {
                    startPosition = 0;
                }
                if (position == startPosition) {
                    view.setVisibility(8);
                } else {
                    view.setVisibility(0);
                }
            }
        }
    }

    protected void setNextButton(int position) {
        if (this.mPageList != null && this.mInitialHelpLayout != null) {
            Button button = (Button) this.mInitialHelpLayout.findViewById(C0088R.id.next_button);
            if (button != null) {
                if (position == (Utils.isRTLLanguage() ? 0 : this.mPageList.size() - 1)) {
                    int textId;
                    if (this.mPageList == null || this.mPageList.size() > 1) {
                        textId = C0088R.string.sp_done_NORMAL;
                    } else {
                        textId = C0088R.string.sp_ok_NORMAL;
                    }
                    button.setText(this.mGet.getAppContext().getString(textId));
                    return;
                }
                button.setText(this.mGet.getAppContext().getString(C0088R.string.next));
            }
        }
    }

    protected void initPage() {
        if (this.mPageList != null) {
            for (int i = 0; i < this.mPageList.size(); i++) {
                InitHelpPage page = (InitHelpPage) this.mPageList.get(i);
                if (page != null) {
                    page.init();
                }
            }
        }
    }

    public void rotateInitialHelpLayout() {
        if (this.mInitRotateLayout != null) {
            this.mInitRotateLayout.rotateLayout(this.mDegree);
        }
    }

    public boolean isInitialHelpGuideVisible() {
        return this.mInitialHelpLayout == null ? false : this.mInitialHelpLayout.isShown();
    }

    public boolean checkLocationPermissionCondition() {
        if (!"on".equals(this.mGet.getSettingValue(Setting.KEY_TAG_LOCATION)) || this.mGet.getActivity().checkSelfPermission(CameraConstants.ACCESS_FINE_LOCATION) == 0 || !CheckStatusManager.checkEnterApplication(getActivity(), true) || AppControlUtil.isGuestMode()) {
            return false;
        }
        return true;
    }

    private boolean checkLocationService() {
        LocationManager locationManager = (LocationManager) this.mGet.getAppContext().getSystemService("location");
        if (locationManager.isProviderEnabled("gps") || locationManager.isProviderEnabled("network")) {
            return true;
        }
        return false;
    }

    public boolean showDialogInitStorage() {
        if (SharedPreferenceUtil.getPastSDInsertionStatus(this.mGet.getAppContext()) == 0 && !this.mGet.isStorageRemoved(1) && !this.mGet.isRequestedSingleImage()) {
            this.mGet.showDialog(8, false);
            return true;
        } else if (SharedPreferenceUtil.getNeedShowStorageInitDialog(this.mGet.getAppContext()) != 0 || this.mGet.isStorageRemoved(1) || this.mGet.isRequestedSingleImage()) {
            return false;
        } else {
            this.mGet.showDialog(8, false);
            return true;
        }
    }

    public void onPauseAfter() {
        int i;
        removeInitialGuideView();
        Context appContext = this.mGet.getAppContext();
        if (this.mGet.isStorageRemoved(1)) {
            i = 0;
        } else {
            i = 1;
        }
        SharedPreferenceUtilBase.savePastSDInsertionStatus(appContext, i);
        this.mPhotoSizeIsChecked = true;
        this.mDegree = 0;
        super.onPauseAfter();
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        if (this.mGet.isPaused() && isInitialHelpGuideVisible() && this.mPageList != null) {
            for (InitHelpPage page : this.mPageList) {
                if (page != null) {
                    page.onPause();
                }
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mInitialGuideView = null;
        this.mListener = null;
    }

    public void showInitialGuide(int guideStep) {
        if (this.mInitialGuideView != null) {
            SharedPreferenceUtil.saveInitialGuideStep(getAppContext(), guideStep);
            ImageView image = (ImageView) this.mInitialGuideView.findViewById(C0088R.id.initial_guide_image);
            TextView text = (TextView) this.mInitialGuideView.findViewById(C0088R.id.initial_guide_text);
            switch (guideStep) {
                case 0:
                    image.setImageResource(C0088R.drawable.initial_guide_gesture);
                    text.setText(C0088R.string.initial_guide_gesture);
                    break;
                case 1:
                    image.setImageResource(C0088R.drawable.initial_guide_capture);
                    text.setText(C0088R.string.initial_guide_capture);
                    break;
                default:
                    removeInitialGuideView();
                    return;
            }
            AnimationUtil.startShowingAnimation(this.mInitialGuideView, true, 300, null);
        }
    }

    public void hideInitialGuide() {
        if (this.mInitialGuideView != null) {
            AnimationUtil.startShowingAnimation(this.mInitialGuideView, false, 300, null);
        }
    }

    private void initialGuideCompleted() {
        if (this.mInitialGuideView != null) {
            AnimationUtil.startShowingAnimation(this.mInitialGuideView, false, 300, new C10351());
        }
    }

    public void setButtonListener() {
        if (this.mInitialGuideView != null) {
            ((Button) this.mInitialGuideView.findViewById(C0088R.id.initial_guide_button)).setOnClickListener(new C10372());
        }
    }

    private void removeInitialGuideView() {
        if (this.mInitialGuideView != null) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
            if (vg != null) {
                vg.removeView(this.mInitialGuideView);
            }
            this.mInitialGuideView = null;
        }
    }
}
