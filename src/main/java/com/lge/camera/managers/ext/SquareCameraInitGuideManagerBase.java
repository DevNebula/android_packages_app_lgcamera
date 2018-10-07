package com.lge.camera.managers.ext;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SystemBarUtil;

public class SquareCameraInitGuideManagerBase extends ManagerInterfaceImpl {
    protected final float DUAL_ITEM_START_END_MARGIN_LAND = 0.132f;
    protected final float DUAL_ITEM_START_END_MARGIN_PORT = 0.083f;
    protected final float SQUARE_HELP_ONE_ITEM_END_MARGIN_LAND = 0.132f;
    protected final float SQUARE_HELP_ONE_ITEM_HEIGHT_LAND = 0.236f;
    protected final float SQUARE_HELP_ONE_ITEM_START_MARGIN_LAND = 0.167f;
    protected final float TITLE_HEIGHT_LAND = 0.144f;
    protected final float TITLE_HEIGHT_PORT = 0.094f;
    private boolean mDoNotShowAgain = true;
    protected View mGridInitView = null;
    protected RotateLayout mInitGuideRotateLayout = null;
    protected CheckBox mLandCheckBox = null;
    protected RelativeLayout mLandGuideLayout = null;
    protected Button mLandOkBtn = null;
    protected TextView mLandTitleText = null;
    protected InitGuideListener mListener = null;
    protected CheckBox mPortCheckBox = null;
    protected RelativeLayout mPortGuideLayout = null;
    protected Button mPortOkBtn = null;
    protected TextView mPortTitleText = null;

    public interface InitGuideListener {
        void onOkButtonClicked(boolean z);
    }

    /* renamed from: com.lge.camera.managers.ext.SquareCameraInitGuideManagerBase$1 */
    class C12001 implements OnTouchListener {
        C12001() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SquareCameraInitGuideManagerBase$2 */
    class C12012 implements OnClickListener {
        C12012() {
        }

        public void onClick(View arg0) {
            SquareCameraInitGuideManagerBase.this.doOkButtonClicked();
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SquareCameraInitGuideManagerBase$3 */
    class C12023 implements OnClickListener {
        C12023() {
        }

        public void onClick(View arg0) {
            SquareCameraInitGuideManagerBase.this.doOkButtonClicked();
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SquareCameraInitGuideManagerBase$4 */
    class C12034 implements OnCheckedChangeListener {
        C12034() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SquareCameraInitGuideManagerBase.this.mDoNotShowAgain = isChecked;
            if (SquareCameraInitGuideManagerBase.this.mLandCheckBox != null) {
                SquareCameraInitGuideManagerBase.this.mLandCheckBox.setChecked(SquareCameraInitGuideManagerBase.this.mDoNotShowAgain);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SquareCameraInitGuideManagerBase$5 */
    class C12045 implements OnCheckedChangeListener {
        C12045() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SquareCameraInitGuideManagerBase.this.mDoNotShowAgain = isChecked;
            if (SquareCameraInitGuideManagerBase.this.mPortCheckBox != null) {
                SquareCameraInitGuideManagerBase.this.mPortCheckBox.setChecked(SquareCameraInitGuideManagerBase.this.mDoNotShowAgain);
            }
        }
    }

    public SquareCameraInitGuideManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setInitGuideListener(InitGuideListener listener) {
        this.mListener = listener;
    }

    public void onDestroy() {
        super.onDestroy();
        removeViews();
    }

    public void initLayout() {
        removeViews();
        CamLog.m3d(CameraConstants.TAG, "-init guide- init layout");
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
        this.mGridInitView = inflateGuideViewLayout();
        if (!(vg == null || this.mGridInitView == null)) {
            vg.addView(this.mGridInitView);
            this.mInitGuideRotateLayout = (RotateLayout) this.mGridInitView.findViewById(C0088R.id.square_grid_init_guide_view);
            this.mPortGuideLayout = (RelativeLayout) this.mGridInitView.findViewById(C0088R.id.square_grid_init_guide_portrait_view);
            this.mLandGuideLayout = (RelativeLayout) this.mGridInitView.findViewById(C0088R.id.square_grid_init_guide_landscape_view);
            this.mPortOkBtn = (Button) this.mGridInitView.findViewById(C0088R.id.grid_ok_button);
            this.mLandOkBtn = (Button) this.mGridInitView.findViewById(C0088R.id.grid_ok_button_land);
            this.mPortCheckBox = (CheckBox) this.mGridInitView.findViewById(C0088R.id.square_mode_check_box);
            this.mLandCheckBox = (CheckBox) this.mGridInitView.findViewById(C0088R.id.square_mode_check_box_land);
            this.mPortTitleText = (TextView) this.mGridInitView.findViewById(C0088R.id.square_help_title);
            this.mLandTitleText = (TextView) this.mGridInitView.findViewById(C0088R.id.square_help_title_land);
        }
        setupAdditionalView();
        setListeners();
        setupCheckBox();
        setInitGuideLayoutForScreenZoom();
        if (this.mInitGuideRotateLayout != null) {
            this.mGet.onShowMenu(64);
            this.mInitGuideRotateLayout.setVisibility(0);
        }
        setDegree(this.mGet.getOrientationDegree(), false);
    }

    protected View inflateGuideViewLayout() {
        return null;
    }

    protected void setupAdditionalView() {
    }

    private void setListeners() {
        this.mInitGuideRotateLayout.setOnTouchListener(new C12001());
        this.mPortOkBtn.setOnClickListener(new C12012());
        this.mLandOkBtn.setOnClickListener(new C12023());
    }

    protected void setupCheckBox() {
        this.mDoNotShowAgain = true;
        this.mPortCheckBox.setOnCheckedChangeListener(new C12034());
        this.mLandCheckBox.setOnCheckedChangeListener(new C12045());
    }

    private void doOkButtonClicked() {
        if (!isSystemBarVisible() && this.mListener != null) {
            this.mListener.onOkButtonClicked(this.mDoNotShowAgain);
        }
    }

    public void removeViews() {
        if (this.mInitGuideRotateLayout != null && this.mGridInitView != null) {
            this.mInitGuideRotateLayout.setVisibility(8);
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
            if (vg != null && this.mGridInitView != null) {
                vg.removeView(this.mGridInitView);
                this.mGet.onHideMenu(64);
            }
        }
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, false);
        if (this.mInitGuideRotateLayout != null && this.mPortGuideLayout != null && this.mLandGuideLayout != null) {
            this.mInitGuideRotateLayout.rotateLayout(degree);
            if (degree == 0 || degree == 180) {
                this.mPortGuideLayout.setVisibility(0);
                this.mLandGuideLayout.setVisibility(8);
                int naviHeight = RatioCalcUtil.getNavigationBarHeight(this.mGet.getAppContext());
                if (degree == 0) {
                    this.mPortGuideLayout.setPadding(0, 0, 0, naviHeight);
                    return;
                } else {
                    this.mPortGuideLayout.setPadding(0, naviHeight, 0, 0);
                    return;
                }
            }
            this.mPortGuideLayout.setVisibility(8);
            this.mLandGuideLayout.setVisibility(0);
        }
    }

    public boolean getGuideLayouVisiblity() {
        if (this.mInitGuideRotateLayout != null) {
            return this.mInitGuideRotateLayout.getVisibility() == 0;
        } else {
            return false;
        }
    }

    protected boolean isSystemBarVisible() {
        return SystemBarUtil.isSystemUIVisible(getActivity());
    }

    protected void setInitGuideLayoutForScreenZoom() {
    }
}
