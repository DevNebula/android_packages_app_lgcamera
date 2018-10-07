package com.lge.camera.managers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.p000v4.view.ViewPager;
import android.support.p000v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraPictureSizeUtil;
import com.lge.camera.managers.InitialGuideManagerBase.InitPagerAdapter;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class InitialGuideManager extends InitialGuideManagerBase {
    public static final float INIT_CMD_BOTTOM_MARGIN_LANDSCAPE = 0.0132f;
    public static final float INIT_CMD_BOTTOM_MARGIN_PORTRAIT = 0.0715f;
    public static final float INIT_CMD_START_END_MARGIN = 0.0333f;
    public static final float INIT_INDEX_BOTTOM_MARGIN_LANDSCAPE = 0.0694f;
    public static final float INIT_INDEX_BOTTOM_MARGIN_PORTRAIT = 0.1278f;
    private OnPageChangeListener mOnPageChangeListener = new C10322();

    /* renamed from: com.lge.camera.managers.InitialGuideManager$1 */
    class C10311 implements OnTouchListener {
        C10311() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.InitialGuideManager$2 */
    class C10322 implements OnPageChangeListener {
        C10322() {
        }

        public void onPageSelected(int position) {
            InitialGuideManager.this.setIndex(position);
            InitialGuideManager.this.mPagePosition = position;
            InitialGuideManager.this.setButtons();
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    }

    /* renamed from: com.lge.camera.managers.InitialGuideManager$3 */
    class C10333 implements OnClickListener {
        C10333() {
        }

        public void onClick(View view) {
            if (InitialGuideManager.this.mPageList != null) {
                if (InitialGuideManager.this.mPagePosition != (Utils.isRTLLanguage() ? 0 : InitialGuideManager.this.mPageList.size() - 1)) {
                    InitialGuideManager.this.movePagerRight(true);
                    return;
                }
                Handler handler = InitialGuideManager.this.mGet.getHandler();
                if (handler != null) {
                    handler.sendEmptyMessage(97);
                }
                if (view != null) {
                    view.setEnabled(false);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.InitialGuideManager$4 */
    class C10344 implements OnClickListener {
        C10344() {
        }

        public void onClick(View view) {
            InitialGuideManager.this.movePagerLeft(true);
        }
    }

    public InitialGuideManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public boolean showInitialHelp() {
        initializeList();
        if (AppControlUtil.isStartFromOnCreate()) {
            this.mPagePosition = getDefaultPagePosition();
        }
        return showInitPagerHelp();
    }

    private int getDefaultPagePosition() {
        if (this.mPageList != null && Utils.isRTLLanguage()) {
            return this.mPageList.size() - 1;
        }
        return 0;
    }

    private void initializeList() {
        CamLog.m3d(CameraConstants.TAG, "-Photo size help- initializeList");
        if (this.mPageList == null || this.mInitLandLayoutIdList == null || this.mInitLandLayoutIdList == null) {
            this.mPageList = new ArrayList();
            this.mInitLayoutIdList = new ArrayList();
            this.mInitLandLayoutIdList = new ArrayList();
            if (!(Arrays.asList(ConfigurationUtil.sMODE_REAR_SUPPORTED_ITEMS) == null || this.mPageList == null)) {
                float defaultRatio = CameraPictureSizeUtil.getDefaultPictureSizeRatio((ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId())));
                if (((double) defaultRatio) > 0.7d && ((double) defaultRatio) < 0.8d && ModelProperties.isLongLCDModel()) {
                    this.mInitLayoutIdList.add(Integer.valueOf(C0088R.layout.photo_size_help_layout));
                    this.mInitLandLayoutIdList.add(Integer.valueOf(C0088R.layout.photo_size_help_layout_landscape));
                    this.mPageList.add(new InitPhotoSizePage(this.mGet));
                }
            }
            if (Utils.isRTLLanguage()) {
                Collections.reverse(this.mInitLayoutIdList);
                Collections.reverse(this.mInitLandLayoutIdList);
                Collections.reverse(this.mPageList);
                return;
            }
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "-Photo size help- list is already initialized, return");
    }

    private boolean showInitPagerHelp() {
        if (!isNeedToShowInitialHelp()) {
            return false;
        }
        if (this.mInitLayoutIdList == null || this.mInitLandLayoutIdList == null || this.mPageList == null) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "show initial help guide");
        this.mInitialHelpLayout = (RelativeLayout) this.mGet.inflateView(C0088R.layout.initial_help_layout);
        ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.camera_base);
        if (this.mInitialHelpLayout == null || vg == null) {
            return false;
        }
        vg.addView(this.mInitialHelpLayout);
        this.mInitRotateLayout = (RotateLayout) this.mInitialHelpLayout.findViewById(C0088R.id.initial_help_rotate_layout);
        this.mInitViewPager = (ViewPager) this.mInitialHelpLayout.findViewById(C0088R.id.initial_help_viewpager);
        this.mInitPagerAdapter = new InitPagerAdapter();
        this.mInitViewPager.setAdapter(this.mInitPagerAdapter);
        this.mInitViewPager.setOnPageChangeListener(this.mOnPageChangeListener);
        this.mInitViewPager.setCurrentItem(this.mPagePosition, false);
        initButtonLayout();
        initIndexLayout();
        setIndex(this.mPagePosition);
        setButtons();
        if (!(this.mInitialHelpLayout == null || this.mInitRotateLayout == null)) {
            this.mInitialHelpLayout.setVisibility(0);
            this.mInitRotateLayout.setVisibility(0);
            this.mGet.onShowMenu(128);
            this.mInitialHelpLayout.setOnTouchListener(new C10311());
        }
        initPage();
        rotateInitialHelpLayout();
        this.mGet.setPhotoSizeHelpShown(true);
        return true;
    }

    private boolean isNeedToShowInitialHelp() {
        if (SharedPreferenceUtil.getInitialHelpShown(getAppContext()) || isInitialHelpGuideVisible() || this.mGet.isAttachIntent() || ModelProperties.isRetailModeInstalled() || this.mPageList == null || this.mPageList.size() <= 0 || this.mGet.getShotMode() == null) {
            return false;
        }
        return this.mGet.isInitialHelpSupportedModule();
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        if (this.mDegree != degree) {
            this.mDegree = degree;
            if (this.mInitLayoutIdList != null && isInitialHelpGuideVisible()) {
                removeInitialHelpLayout(true);
                this.mGet.setPhotoSizeHelpShown(false);
                showInitPagerHelp();
            }
        }
    }

    public void removeInitialHelpLayout(boolean isRotating) {
        removeInitialPagerLayout(isRotating);
    }

    public void removeInitialPagerLayout(boolean isRotating) {
        CamLog.m3d(CameraConstants.TAG, "- Initial pager help - removeInitialPagerLayout");
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
        if (vg != null) {
            this.mGet.onHideMenu(128);
            vg.removeView(this.mInitialHelpLayout);
        }
        this.mInitialHelpLayout = null;
        this.mInitRotateLayout = null;
        this.mInitViewPager = null;
        if (!isRotating) {
            if (this.mPageList != null) {
                for (int i = 0; i < this.mPageList.size(); i++) {
                    InitHelpPage page = (InitHelpPage) this.mPageList.get(i);
                    if (page != null) {
                        page.onRemove();
                    }
                }
                this.mPageList.clear();
                this.mPageList = null;
            }
            if (this.mInitLayoutIdList != null) {
                this.mInitLayoutIdList.clear();
                this.mInitLayoutIdList = null;
            }
            if (this.mInitLandLayoutIdList != null) {
                this.mInitLandLayoutIdList.clear();
                this.mInitLandLayoutIdList = null;
            }
            SharedPreferenceUtil.saveInitialHelpShown(this.mGet.getAppContext(), true);
            this.mPagePosition = 0;
            this.mGet.setPhotoSizeHelpShown(false);
        }
    }

    private void setIndex(int position) {
        if (this.mIndexLayout != null) {
            Resources res = this.mGet.getAppContext().getResources();
            for (int i = 0; i < this.mIndexLayout.getChildCount(); i++) {
                Drawable drawable;
                ImageView iv = (ImageView) this.mIndexLayout.getChildAt(i);
                int resId;
                if (i == position) {
                    resId = res.getIdentifier("ic_page_view_on", "drawable", "com.lge");
                    if (resId == 0) {
                        resId = C0088R.drawable.ic_page_view_on;
                    }
                    drawable = this.mGet.getAppContext().getDrawable(resId);
                    drawable.setTintList(getColorStateList(this.mGet.getAppContext(), C0088R.color.color_checkbox));
                } else {
                    resId = res.getIdentifier("ic_page_view_off", "drawable", "com.lge");
                    if (resId == 0) {
                        resId = C0088R.drawable.ic_page_view_off;
                    }
                    drawable = res.getDrawable(resId);
                }
                iv.setImageDrawable(drawable);
            }
        }
    }

    private ColorStateList getColorStateList(Context context, int colorResId) {
        return ColorStateList.valueOf(context.getResources().getColor(colorResId));
    }

    private void initIndexLayout() {
        boolean isPort = false;
        this.mIndexLayout = (LinearLayout) this.mInitialHelpLayout.findViewById(C0088R.id.initial_help_index_layout);
        this.mIndexLayoutWrapper = (RelativeLayout) this.mInitialHelpLayout.findViewById(C0088R.id.initial_help_index_layout_wrapper);
        if (this.mPageList == null || this.mPageList.size() > 1) {
            int margin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.square_help_index_interval_margin);
            for (int i = 0; i < this.mInitLayoutIdList.size(); i++) {
                ImageView iv = new ImageView(this.mGet.getAppContext());
                LayoutParams lp = new LayoutParams(-2, -2);
                lp.setMargins(margin, 0, margin, 0);
                lp.setMarginStart(margin);
                lp.setMarginEnd(margin);
                iv.setLayoutParams(lp);
                this.mIndexLayout.addView(iv);
            }
            this.mIndexLayout.setVisibility(0);
            if (this.mDegree == 0 || this.mDegree == 180) {
                isPort = true;
            }
            LayoutParams indexLayoutParam = (LayoutParams) this.mIndexLayoutWrapper.getLayoutParams();
            indexLayoutParam.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, isPort ? 0.1278f : 0.0694f);
            this.mIndexLayoutWrapper.setLayoutParams(indexLayoutParam);
        }
    }

    private void initButtonLayout() {
        boolean isPort;
        View view = this.mInitialHelpLayout.findViewById(C0088R.id.command_layout);
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (this.mDegree == 0 || this.mDegree == 180) {
            isPort = true;
        } else {
            isPort = false;
        }
        int startMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0333f);
        int endMargin = RatioCalcUtil.getNavigationBarHeight(this.mGet.getAppContext());
        int bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, isPort ? 0.0715f : 0.0132f);
        lp.setMarginStart(startMargin);
        if (!isPort) {
            startMargin = endMargin;
        }
        lp.setMarginEnd(startMargin);
        lp.bottomMargin = bottomMargin;
        view.setLayoutParams(lp);
        Button button = (Button) this.mInitialHelpLayout.findViewById(C0088R.id.next_button);
        button.setOnClickListener(new C10333());
        if (this.mPageList != null && this.mPageList.size() <= 1) {
            button.setText(C0088R.string.sp_ok_NORMAL);
        }
        ((Button) this.mInitialHelpLayout.findViewById(C0088R.id.previous_button)).setOnClickListener(new C10344());
    }

    private void movePagerLeft(boolean smoothScroll) {
        if (this.mPageList != null && this.mInitViewPager != null) {
            int i;
            if (Utils.isRTLLanguage()) {
                i = this.mPagePosition + 1;
                this.mPagePosition = i;
                this.mPagePosition = Math.min(i, this.mPageList.size() - 1);
            } else {
                i = this.mPagePosition - 1;
                this.mPagePosition = i;
                this.mPagePosition = Math.max(i, 0);
            }
            this.mInitViewPager.setCurrentItem(this.mPagePosition, smoothScroll);
        }
    }

    private void movePagerRight(boolean smoothScroll) {
        if (this.mPageList != null && this.mInitViewPager != null) {
            int i;
            if (Utils.isRTLLanguage()) {
                i = this.mPagePosition - 1;
                this.mPagePosition = i;
                this.mPagePosition = Math.max(i, 0);
            } else {
                i = this.mPagePosition + 1;
                this.mPagePosition = i;
                this.mPagePosition = Math.min(i, this.mPageList.size() - 1);
            }
            this.mInitViewPager.setCurrentItem(this.mPagePosition, smoothScroll);
        }
    }

    public void onStop() {
        super.onStop();
        if (isInitialHelpGuideVisible()) {
            removeInitialHelpLayout(false);
        }
    }

    public void oneShotPreviewCallbackDone() {
        if (isInitialHelpGuideVisible()) {
            CamLog.m3d(CameraConstants.TAG, "-Photo size help- oneShotPreviewCallbackDone");
            if (this.mPageList != null) {
                for (InitHelpPage page : this.mPageList) {
                    if (page != null) {
                        page.oneShotPreviewCallbackDone();
                    }
                }
            }
        }
    }
}
