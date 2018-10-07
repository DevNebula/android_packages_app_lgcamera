package com.lge.camera.managers;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;

public class SquareModeInitGuideManager extends ManagerInterfaceImpl {
    protected static final float CHECK_BOX_LAYOUT_HEIGHT = 0.0666f;
    protected static final float EDGE_MARGIN = 0.01f;
    protected static final float ITEM_HEIGHT_LAND = 0.355f;
    protected static final float ITEM_HEIGHT_PORT = 0.198f;
    protected static final float ITEM_HEIGHT_PORT_LAST_ITEM = 0.2125f;
    protected static final float ITEM_WIDTH_LAND = 0.444f;
    protected static final float SCROLLVIEW_BOTTOM_MARGIN = 0.091f;
    protected static final boolean SHOW_INIT_GUIDE = false;
    private CheckBox mCheckBox;
    private CheckBox mCheckBoxLand;
    private boolean mDoNotShowAgain = true;
    protected RelativeLayout mInitGuideLandscapeView = null;
    protected RelativeLayout mInitGuidePortraitView = null;
    protected RotateLayout mInitGuideView = null;
    int[] mLandscapeItemViewIds = new int[]{C0088R.id.square_mode_init_land_item1, C0088R.id.square_mode_init_land_item2, C0088R.id.square_mode_init_land_item3, C0088R.id.square_mode_init_land_item4};
    private Button mOKButton;
    private Button mOKButtonLand;
    int[] mPortraitItemViewIds = new int[]{C0088R.id.square_mode_init_port_item1, C0088R.id.square_mode_init_port_item2, C0088R.id.square_mode_init_port_item3, C0088R.id.square_mode_init_port_item4};

    /* renamed from: com.lge.camera.managers.SquareModeInitGuideManager$1 */
    class C11661 implements OnTouchListener {
        C11661() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.SquareModeInitGuideManager$2 */
    class C11672 implements OnCheckedChangeListener {
        C11672() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SquareModeInitGuideManager.this.mDoNotShowAgain = isChecked;
            if (SquareModeInitGuideManager.this.mCheckBoxLand != null) {
                SquareModeInitGuideManager.this.mCheckBoxLand.setChecked(SquareModeInitGuideManager.this.mDoNotShowAgain);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.SquareModeInitGuideManager$3 */
    class C11683 implements OnCheckedChangeListener {
        C11683() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SquareModeInitGuideManager.this.mDoNotShowAgain = isChecked;
            if (SquareModeInitGuideManager.this.mCheckBox != null) {
                SquareModeInitGuideManager.this.mCheckBox.setChecked(SquareModeInitGuideManager.this.mDoNotShowAgain);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.SquareModeInitGuideManager$4 */
    class C11694 implements OnClickListener {
        C11694() {
        }

        public void onClick(View arg0) {
            SharedPreferenceUtil.saveSquareModeInitGuide(SquareModeInitGuideManager.this.getAppContext(), !SquareModeInitGuideManager.this.mDoNotShowAgain);
            SquareModeInitGuideManager.this.hideInitGuide();
        }
    }

    /* renamed from: com.lge.camera.managers.SquareModeInitGuideManager$5 */
    class C11705 implements OnClickListener {
        C11705() {
        }

        public void onClick(View arg0) {
            SharedPreferenceUtil.saveSquareModeInitGuide(SquareModeInitGuideManager.this.getAppContext(), !SquareModeInitGuideManager.this.mDoNotShowAgain);
            SquareModeInitGuideManager.this.hideInitGuide();
        }
    }

    public SquareModeInitGuideManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        setupInitGuideView();
    }

    public void setupInitGuideView() {
    }

    protected void setInitGuideLayoutForScreenZoom() {
        View view;
        LayoutParams llp;
        int portHeight = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ITEM_HEIGHT_PORT);
        int landHeight = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, ITEM_HEIGHT_LAND);
        int landWidth = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ITEM_WIDTH_LAND);
        for (int findViewById : this.mPortraitItemViewIds) {
            view = this.mInitGuidePortraitView.findViewById(findViewById);
            llp = (LayoutParams) view.getLayoutParams();
            view.setMinimumHeight(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ITEM_HEIGHT_PORT));
        }
        for (int findViewById2 : this.mLandscapeItemViewIds) {
            view = this.mInitGuideLandscapeView.findViewById(findViewById2);
            llp = (LayoutParams) view.getLayoutParams();
            view.setMinimumHeight(landHeight);
            llp.width = landWidth;
        }
        int paddingValue = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, EDGE_MARGIN);
        this.mInitGuidePortraitView.setPadding(paddingValue, 0, paddingValue, 0);
        this.mInitGuideLandscapeView.setPadding(paddingValue, 0, paddingValue, 0);
        ((RelativeLayout.LayoutParams) this.mInitGuidePortraitView.findViewById(C0088R.id.square_mode_init_guie_portrait_scroll).getLayoutParams()).bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, SCROLLVIEW_BOTTOM_MARGIN);
        ((RelativeLayout.LayoutParams) this.mInitGuideLandscapeView.findViewById(C0088R.id.square_mode_init_guie_landscape_scroll).getLayoutParams()).bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, SCROLLVIEW_BOTTOM_MARGIN);
    }

    protected void setCheckBoxLayout() {
        this.mDoNotShowAgain = true;
        this.mCheckBox = (CheckBox) this.mInitGuideView.findViewById(C0088R.id.square_mode_check_box);
        this.mCheckBox.setOnCheckedChangeListener(new C11672());
        this.mCheckBoxLand = (CheckBox) this.mInitGuideView.findViewById(C0088R.id.square_mode_check_box_land);
        this.mCheckBoxLand.setOnCheckedChangeListener(new C11683());
    }

    protected void setButtonLayout() {
        this.mOKButton = (Button) this.mInitGuideView.findViewById(C0088R.id.square_mode_ok_button);
        this.mOKButton.setOnClickListener(new C11694());
        this.mOKButtonLand = (Button) this.mInitGuideView.findViewById(C0088R.id.square_mode_ok_button_land);
        this.mOKButtonLand.setOnClickListener(new C11705());
    }

    protected void setInitGuideDegree(int degree) {
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        setInitGuideDegree(degree);
    }

    public void onDestroy() {
        super.onDestroy();
        hideInitGuide();
    }

    public void hideInitGuide() {
    }

    public boolean isShowingInitGuide() {
        if (this.mInitGuideView != null && this.mInitGuideView.getVisibility() == 0) {
            return true;
        }
        return false;
    }
}
