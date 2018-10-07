package com.lge.camera.managers;

import android.content.res.Configuration;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.AdvancedSelfieFilterListItem;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.PrefMakerUtil;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdvancedSelfieManager extends FilmEmulatorManager {
    public static boolean sFilterOptionVisible;
    public final float ADVANCED_SELFIE_BUTTON_VIEW_BOTTOM_MARGIN_RATIO = 0.15f;
    public final float ADVANCED_SELFIE_BUTTON_VIEW_BOTTOM_MARGIN_RATIO_LONG_LCD = 0.279f;
    public final float ADVANCED_SELFIE_BUTTON_VIEW_RIGHT_MARGIN_RATIO_TABLET = 0.1f;
    public final float ADVANCED_SELFIE_BUTTON_VIEW_WIDTH_RATIO = 0.611f;
    public final float ADVANCED_SELFIE_BUTTON_VIEW_WIDTH_RATIO_TABLET = 1.0f;
    public final float ADVANCED_SELFIE_FILTER_VIEW_BOTTOM_MARGIN_RATIO = 0.21f;
    protected RotateImageButton mFilterButton = null;
    private String[] mFilterIndexList;
    protected List<AdvancedSelfieFilterListItem> mFilterItemList;
    private String[] mFilterNameList;
    private int mFilterScrollOffset;
    private boolean mFilterVisible = false;
    private boolean mHideTransient = false;
    protected OnItemClickListener mListClickListener = new C08012();
    protected RotateImageButton mRelightingButton = null;
    protected int mRotationDegree = 0;
    protected int mSelectedMenuType = 0;
    protected View mSelfieFilterLayout = null;
    private FilterScrollAdapter mSelfieFilterListAdapter = null;
    protected ListView mSelfieFilterListview = null;
    protected SeekBar mSelfieFilterStrengthBar = null;
    protected View mSelfieFilterStrengthBarView = null;
    protected TextView mSelfieFilterStrengthLabelLand = null;
    protected TextView mSelfieFilterStrengthLabelPort = null;
    protected TextView mSelfieFilterStrengthLevelText = null;
    private LayoutParams mSelfieFilterStrengthLevelTextLp;
    private HandlerRunnable mSelfieFilterStrengthLevelTextRemoveRunnable = null;
    private LayoutParams mSelfieFilterStrengthLevelTextSizeLp;
    protected RotateLayout mSelfieFilterStrengthTextRotateView = null;
    protected RotateLayout mSelfieFilterStrengthView = null;
    protected View mSelfieFilterView = null;
    protected View mSelfieMenuView = null;
    protected View mSelfieMenulayout = null;
    private int mSelfieStrengthMargin = -1;
    protected RotateImageButton mSkinToneButton = null;
    private int mStrengthBarBottomMargin = -1;

    /* renamed from: com.lge.camera.managers.AdvancedSelfieManager$2 */
    class C08012 implements OnItemClickListener {
        C08012() {
        }

        public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
            CamLog.m3d(CameraConstants.TAG, "[filter] onItemClick called position : " + position + ", mFilterScrollOffset = " + AdvancedSelfieManager.this.mFilterScrollOffset);
            if (AdvancedSelfieManager.this.mSelfieFilterListAdapter != null && !AdvancedSelfieManager.this.checkBlockingFilterState() && !AdvancedSelfieManager.this.mGet.isGIFEncoding() && AdvancedSelfieManager.this.mSelfieFilterStrengthView != null && AdvancedSelfieManager.this.mSelfieFilterStrengthLevelText != null && position != AdvancedSelfieManager.this.mCurLutNumber) {
                if (AdvancedSelfieManager.this.mGet.isCropZoomStarting()) {
                    CamLog.m3d(CameraConstants.TAG, "[filter] Not change filter effect because crop zoom is started.");
                    return;
                }
                AdvancedSelfieManager.this.updateListViewItem(position);
                String filterValue = ((AdvancedSelfieFilterListItem) AdvancedSelfieManager.this.mFilterItemList.get(position)).getFilterValue();
                AdvancedSelfieManager.this.mSelfieFilterListview.smoothScrollToPositionFromTop(position, AdvancedSelfieManager.this.mFilterScrollOffset);
                if (AdvancedSelfieManager.this.mListener != null) {
                    AdvancedSelfieManager.this.mListener.onFilmEffectChanged(filterValue);
                }
                AdvancedSelfieManager.this.resetFilterStrength();
                AdvancedSelfieManager.this.updateSelfieFilterStrengthText();
                AdvancedSelfieManager.this.mSelfieFilterStrengthView.setVisibility(CameraConstants.FILM_NONE.equals(filterValue) ? 4 : 0);
                if (AdvancedSelfieManager.this.mSelfieFilterStrengthTextRotateView != null) {
                    AdvancedSelfieManager.this.mSelfieFilterStrengthTextRotateView.setVisibility(4);
                }
                AdvancedSelfieManager.this.mCurLutNumber = ((AdvancedSelfieFilterListItem) AdvancedSelfieManager.this.mFilterItemList.get(position)).getFilterIndex();
                AdvancedSelfieManager.this.setFilterButtonType();
                AdvancedSelfieManager.this.setMenuButtonSelected(2, true);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.AdvancedSelfieManager$3 */
    class C08023 implements OnClickListener {
        C08023() {
        }

        public void onClick(View v) {
            AdvancedSelfieManager.this.onClickSkinToneButton();
        }
    }

    /* renamed from: com.lge.camera.managers.AdvancedSelfieManager$4 */
    class C08034 implements OnClickListener {
        C08034() {
        }

        public void onClick(View v) {
            AdvancedSelfieManager.this.onClickRelightingButton();
        }
    }

    /* renamed from: com.lge.camera.managers.AdvancedSelfieManager$5 */
    class C08045 implements OnClickListener {
        C08045() {
        }

        public void onClick(View v) {
            AdvancedSelfieManager.this.onClickFilterButton();
        }
    }

    /* renamed from: com.lge.camera.managers.AdvancedSelfieManager$6 */
    class C08056 implements OnSeekBarChangeListener {
        C08056() {
        }

        public void onStopTrackingTouch(SeekBar arg0) {
            if (AdvancedSelfieManager.this.mSelfieFilterStrengthLevelTextRemoveRunnable != null) {
                AdvancedSelfieManager.this.mGet.postOnUiThread(AdvancedSelfieManager.this.mSelfieFilterStrengthLevelTextRemoveRunnable, CameraConstants.TOAST_LENGTH_SHORT);
            }
        }

        public void onStartTrackingTouch(SeekBar arg0) {
            AdvancedSelfieManager.this.mGet.removePostRunnable(AdvancedSelfieManager.this.mSelfieFilterStrengthLevelTextRemoveRunnable);
            if (AdvancedSelfieManager.this.mSelfieFilterStrengthTextRotateView != null) {
                AdvancedSelfieManager.this.mSelfieFilterStrengthTextRotateView.setVisibility(0);
            }
        }

        public void onProgressChanged(SeekBar bar, int level, boolean arg2) {
            if (AdvancedSelfieManager.this.mSelfieFilterStrengthView != null && AdvancedSelfieManager.this.mSelfieFilterStrengthView.getVisibility() == 0) {
                AdvancedSelfieManager.this.mFilmStrength = ((float) level) / 100.0f;
                if (AdvancedSelfieManager.this.mSelfieFilterStrengthLevelText != null) {
                    AdvancedSelfieManager.this.mSelfieFilterStrengthLevelText.setText(Integer.toString(level));
                }
                AdvancedSelfieManager.this.updateSelfieFilterStrengthText();
            }
        }
    }

    public class FilterScrollAdapter extends BaseAdapter {
        int[] img = PrefMakerUtil.getIconList(AdvancedSelfieManager.this.mGet.getAppContext(), C0088R.array.advanced_selfie_filter_icons);
        LayoutInflater inf;
        int layout;
        private int mSelectedItemPos = -1;

        public FilterScrollAdapter(int layout) {
            this.layout = layout;
            this.inf = (LayoutInflater) AdvancedSelfieManager.this.mGet.getAppContext().getSystemService("layout_inflater");
        }

        public void setDegree(int degree) {
            AdvancedSelfieManager.this.mRotationDegree = (degree + 270) % 360;
        }

        public void setSelectedItem(int position) {
            this.mSelectedItemPos = position;
        }

        public int getCount() {
            return this.img.length;
        }

        public Object getItem(int position) {
            return AdvancedSelfieManager.this.mFilterItemList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.inf.inflate(this.layout, null);
            }
            RotateImageButton iv = (RotateImageButton) convertView.findViewById(C0088R.id.imageView1);
            iv.setBackgroundResource(this.img[position]);
            iv.setAdjustViewBounds(true);
            iv.setText(((AdvancedSelfieFilterListItem) AdvancedSelfieManager.this.mFilterItemList.get(position)).getFilterName());
            iv.setContentDescription(AdvancedSelfieManager.this.mFilterNameList[position]);
            iv.setDegree(AdvancedSelfieManager.this.mRotationDegree, false);
            if (AdvancedSelfieManager.this.mCurLutNumber == position) {
                iv.setImageResource(C0088R.drawable.camera_selfie_filter_selected);
                iv.setSelected(true);
            } else {
                iv.setImageResource(0);
                iv.setSelected(false);
            }
            return convertView;
        }
    }

    public AdvancedSelfieManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    protected FilterScrollAdapter createListAdapter() {
        return new FilterScrollAdapter(C0088R.layout.advanced_filter_row);
    }

    public void init() {
        if (isSelfieUIShowingCondition()) {
            sFilterOptionVisible = true;
            createFilterList();
            initSelfieMenuButtonLayout();
            setSelfieMenuButtonListener();
            if (AppControlUtil.isStartFromOnCreate()) {
                setSelfieMenuVisibility(true);
                return;
            }
            return;
        }
        super.init();
    }

    public void resetFilmStrength() {
        this.mFilmStrength = 1.0f;
        if (this.mGet.isRearCamera()) {
            SharedPreferenceUtilBase.saveRearFilterStrength(getAppContext(), 100);
        } else {
            SharedPreferenceUtilBase.saveFrontFilterStrength(getAppContext(), 100);
        }
    }

    public int getFilmStrengthValue() {
        return (int) (this.mFilmStrength * 100.0f);
    }

    public void initSelfieMenuButtonLayout() {
        CamLog.m3d(CameraConstants.TAG, "[filter] initSelfieButtonLayout");
        if (isSelfieUIShowingCondition()) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            this.mSelfieMenulayout = this.mGet.inflateView(C0088R.layout.advanced_selfie_beauty);
            if (vg != null && this.mSelfieMenulayout != null) {
                vg.removeView(this.mSelfieMenulayout);
                if (this.mSelfieMenulayout != null) {
                    vg.addView(this.mSelfieMenulayout, 0, new LayoutParams(-1, -1));
                    this.mSelfieMenuView = this.mSelfieMenulayout.findViewById(C0088R.id.advanced_beauty_layout);
                    changeMenuLayoutParam();
                    createMenuButton();
                    return;
                }
                return;
            }
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "[filter] ButtonLayout not set because RearCamera");
    }

    private void changeMenuLayoutParam() {
        if (this.mSelfieMenulayout != null) {
            int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
            LayoutParams advancedMenulayout = (LayoutParams) this.mSelfieMenulayout.getLayoutParams();
            advancedMenulayout.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ModelProperties.isLongLCDModel() ? 0.279f : 0.15f);
            if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                advancedMenulayout.bottomMargin = lcdSize[1];
            }
            if ("on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW))) {
                advancedMenulayout.bottomMargin += RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.008f);
            }
        }
    }

    protected void createMenuButton() {
        LinearLayout.LayoutParams menuLayout = (LinearLayout.LayoutParams) this.mSelfieMenulayout.findViewById(C0088R.id.advanced_selfie_menu_layout).getLayoutParams();
        menuLayout.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.611f);
        ArrayList<Integer> idList = new ArrayList();
        if (FunctionProperties.isSupportedBeautyShot()) {
            this.mSkinToneButton = (RotateImageButton) this.mSelfieMenulayout.findViewById(C0088R.id.beauty_skin_tone_button);
        } else {
            idList.add(Integer.valueOf(C0088R.id.beauty_skin_tone_button_layout));
        }
        if (FunctionProperties.isSupportedRelighting()) {
            this.mRelightingButton = (RotateImageButton) this.mSelfieMenulayout.findViewById(C0088R.id.beauty_relighting_button);
            this.mRelightingButton.initButtonText(this.mGet.getActivity().getResources().getString(C0088R.string.Advanced_beauty_relighting));
        } else {
            idList.add(Integer.valueOf(C0088R.id.beauty_relighting_button_layout));
        }
        if (FunctionProperties.isSupportedFilmEmulator()) {
            this.mFilterButton = (RotateImageButton) this.mSelfieMenulayout.findViewById(C0088R.id.beauty_filter_button);
            this.mFilterButton.initButtonText(this.mGet.getActivity().getResources().getString(C0088R.string.Advanced_beauty_filter));
            initSelfieFilterLayout();
            setSelfieFilterStrengthBarListener();
            if (!FunctionProperties.isSupportedBeautyShot() && ModelProperties.isTablet(getAppContext())) {
                menuLayout.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 1.0f);
                menuLayout.rightMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.1f) + Utils.getPx(getAppContext(), C0088R.dimen.gif_button_rightMargin);
            }
        } else if (!this.mGet.isColorEffectSupported()) {
            idList.add(Integer.valueOf(C0088R.id.beauty_filter_button_layout));
        }
        menuLayout.width -= (menuLayout.width / 3) * idList.size();
        Iterator it = idList.iterator();
        while (it.hasNext()) {
            RelativeLayout layout = (RelativeLayout) this.mSelfieMenulayout.findViewById(((Integer) it.next()).intValue());
            if (layout != null) {
                layout.setVisibility(8);
            }
        }
    }

    public void initSelfieFilterLayout() {
        if (isSelfieUIShowingCondition()) {
            int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            this.mSelfieFilterLayout = this.mGet.inflateView(C0088R.layout.advanced_selfie_scrollview);
            if (!(vg == null || this.mSelfieFilterLayout == null)) {
                vg.removeView(this.mSelfieFilterLayout);
                if (this.mSelfieFilterLayout != null) {
                    CamLog.m3d(CameraConstants.TAG, "[filter] initSelfieOptionLayout");
                    vg.addView(this.mSelfieFilterLayout, 0, new LayoutParams(-1, -1));
                    this.mSelfieFilterView = this.mSelfieFilterLayout.findViewById(C0088R.id.selfie_filter_layout);
                    this.mSelfieFilterStrengthView = (RotateLayout) this.mSelfieFilterLayout.findViewById(C0088R.id.advanced_selfie_film_strength_rotate_layout);
                    this.mSelfieFilterStrengthBarView = this.mSelfieFilterLayout.findViewById(C0088R.id.advanced_selfie_film_strength_seekbar_layout);
                    if (this.mSelfieFilterView != null && this.mSelfieFilterStrengthView != null && this.mSelfieFilterStrengthBarView != null) {
                        int sizeCalculatedByPercentage;
                        this.mSelfieFilterStrengthBar = (SeekBar) this.mSelfieFilterStrengthView.findViewById(C0088R.id.selfie_film_strength_seekbar);
                        this.mSelfieFilterStrengthLevelText = (TextView) this.mSelfieFilterLayout.findViewById(C0088R.id.selfie_film_strength_seekbar_level);
                        this.mSelfieFilterStrengthTextRotateView = (RotateLayout) this.mSelfieFilterLayout.findViewById(C0088R.id.advanced_selfie_film_strength_text_rotate);
                        this.mSelfieFilterStrengthLabelPort = (TextView) this.mSelfieFilterLayout.findViewById(C0088R.id.selfie_film_strength_seekBar_label_port);
                        this.mSelfieFilterStrengthLabelLand = (TextView) this.mSelfieFilterLayout.findViewById(C0088R.id.selfie_film_strength_seekBar_label_land);
                        this.mSelfieFilterStrengthLevelTextRemoveRunnable = new HandlerRunnable(this) {
                            public void handleRun() {
                                CamLog.m3d(CameraConstants.TAG, "[FilmStrength] Run mSelfieFilterStrengthLevelTextRemoveRunnable. Remove level text!");
                                if (AdvancedSelfieManager.this.mSelfieFilterStrengthTextRotateView != null) {
                                    AdvancedSelfieManager.this.mSelfieFilterStrengthTextRotateView.setVisibility(4);
                                }
                            }
                        };
                        setSelfieFilterImage();
                        resetFilterStrength();
                        setSelfieStrengthLabel();
                        LayoutParams filterLp = (LayoutParams) this.mSelfieFilterView.getLayoutParams();
                        if (ModelProperties.isLongLCDModel()) {
                            sizeCalculatedByPercentage = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.21f);
                        } else {
                            sizeCalculatedByPercentage = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.15f) + this.mGet.getAppContext().getDrawable(C0088R.drawable.ic_camera_selfie_tone_normal).getIntrinsicHeight();
                        }
                        filterLp.bottomMargin = sizeCalculatedByPercentage;
                        if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                            filterLp.bottomMargin = getAppContext().getDrawable(C0088R.drawable.ic_camera_selfie_tone_normal).getIntrinsicHeight() + lcdSize[1];
                        }
                        initSelfieStrengthBarLayout();
                    } else {
                        return;
                    }
                }
            }
            setDegree(this.mGet.getOrientationDegree(), false);
            setFilterButtonType();
            if (!CameraConstants.FILM_NONE.equals(this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR))) {
                setMenuButtonSelected(2, false);
                return;
            }
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "[filter] Filter Layout not set because RearCamera");
    }

    private void initSelfieStrengthBarLayout() {
        if (this.mSelfieFilterView != null && this.mSelfieFilterStrengthBarView != null && this.mSelfieFilterStrengthView != null && this.mSelfieFilterStrengthLevelText != null && this.mSelfieFilterStrengthTextRotateView != null) {
            LayoutParams filterLp = (LayoutParams) this.mSelfieFilterView.getLayoutParams();
            LayoutParams strengthLp = (LayoutParams) this.mSelfieFilterStrengthView.getLayoutParams();
            ((LayoutParams) this.mSelfieFilterStrengthBarView.getLayoutParams()).width = (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.655f) + Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.filter_strength_seekBar_paddingStart)) + Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.filter_strength_seekBar_paddingEnd);
            this.mStrengthBarBottomMargin = (filterLp.bottomMargin + filterLp.height) + Utils.getPx(getAppContext(), C0088R.dimen.filter_strength_seekBar_marginBottom);
            strengthLp.bottomMargin = this.mStrengthBarBottomMargin;
            this.mSelfieFilterStrengthLevelTextSizeLp = new LayoutParams(Utils.getPx(getAppContext(), C0088R.dimen.filter_strength_seekBar_text_height), Utils.getPx(getAppContext(), C0088R.dimen.filter_strength_seekBar_text_height));
            this.mSelfieFilterStrengthLevelText.setLayoutParams(this.mSelfieFilterStrengthLevelTextSizeLp);
            this.mSelfieFilterStrengthLevelTextLp = new LayoutParams(-2, -2);
            this.mSelfieFilterStrengthLevelTextLp.leftMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.7375f);
            this.mSelfieFilterStrengthTextRotateView.setLayoutParams(this.mSelfieFilterStrengthLevelTextLp);
        }
    }

    private void setSelfieFilterImage() {
        if (this.mSelfieFilterView != null) {
            this.mSelfieFilterListview = (ListView) this.mSelfieFilterView.findViewById(C0088R.id.selfie_filter_listView);
        }
        this.mSelfieFilterListAdapter = createListAdapter();
        this.mSelfieFilterListview.setAdapter(this.mSelfieFilterListAdapter);
        this.mSelfieFilterListview.setOnItemClickListener(this.mListClickListener);
        this.mFilterScrollOffset = (Utils.getLCDsize(getAppContext(), true)[1] / 2) - Utils.getPx(getAppContext(), C0088R.dimen.advanced_selfie_option_scrollOffset);
    }

    private void createFilterList() {
        this.mFilterNameList = this.mGet.getAppContext().getResources().getStringArray(ModelProperties.isJapanModel() ? C0088R.array.advanced_selfie_filter_entries_japan : C0088R.array.advanced_selfie_filter_entries);
        this.mFilterIndexList = this.mGet.getAppContext().getResources().getStringArray(C0088R.array.advanced_selfie_filter_entriyValues);
        this.mFilterItemList = new ArrayList();
        for (int i = 0; i < this.mFilterNameList.length; i++) {
            AdvancedSelfieFilterListItem filterItem = new AdvancedSelfieFilterListItem();
            filterItem.initFilterItem(this.mGet.getAppContext(), this.mFilterNameList[i], i, this.mFilterIndexList[i], getOrientationDegree());
            this.mFilterItemList.add(i, filterItem);
        }
    }

    private void resetFilterStrength() {
        this.mFilmStrength = 1.0f;
        if (this.mSelfieFilterStrengthBar != null) {
            this.mSelfieFilterStrengthBar.setProgress(100);
        }
        if (this.mSelfieFilterStrengthLevelText != null) {
            this.mSelfieFilterStrengthLevelText.setText(Integer.toString(100));
        }
    }

    public void setSelfieMenuButtonListener() {
        if (this.mSkinToneButton != null) {
            this.mSkinToneButton.setOnClickListener(new C08023());
        }
        if (this.mRelightingButton != null) {
            this.mRelightingButton.setOnClickListener(new C08034());
        }
        if (this.mFilterButton != null) {
            this.mFilterButton.setOnClickListener(new C08045());
        }
    }

    private boolean checkMenuButtonClickable() {
        if (!this.mGet.isCropZoomStarting() && !this.mGet.isGIFEncoding() && this.mGet.checkModuleValidate(192) && !this.mGet.isMultishotState(7)) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "[selfie] checkMenuButtonClickable return false");
        return false;
    }

    public void onClickSkinToneButton() {
        if (checkMenuButtonClickable() && !this.mGet.isMenuShowing(1003)) {
            CamLog.m3d(CameraConstants.TAG, "[filter] mBeautySkinToneButton clicked and Beauty Bar show!");
            if (this.mSelectedMenuType == 1) {
                setSelfieOptionVisibility(false, true);
                this.mGet.onHideBeautyMenu();
                return;
            }
            setSelfieOptionVisibility(false, true);
            if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                this.mGet.showDoubleCamera(true);
            }
            setMenuType(1);
            setMenuButtonSelected(1, true);
            setMenuButtonSelected(3, false);
            setMenuButtonSelected(2, false);
            this.mGet.initBeautyBar(this.mSelectedMenuType);
            this.mGet.setBarVisible(1, true, true);
            this.mGet.onShowBeautyMenu(this.mSelectedMenuType);
            this.mGet.pauseShutterless();
            sFilterOptionVisible = false;
        }
    }

    public void onClickRelightingButton() {
        if (checkMenuButtonClickable()) {
            CamLog.m3d(CameraConstants.TAG, "[filter] mRelightingButton clicked and Beauty Bar show!");
            if (this.mSelectedMenuType == 3) {
                setSelfieOptionVisibility(false, true);
                this.mGet.onHideBeautyMenu();
                return;
            }
            setSelfieOptionVisibility(false, true);
            if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                this.mGet.showDoubleCamera(true);
            }
            setMenuType(3);
            setMenuButtonSelected(3, true);
            setMenuButtonSelected(1, false);
            setMenuButtonSelected(2, false);
            this.mGet.initBeautyBar(this.mSelectedMenuType);
            this.mGet.setBarVisible(4, true, true);
            this.mGet.onShowBeautyMenu(this.mSelectedMenuType);
            this.mGet.pauseShutterless();
            sFilterOptionVisible = false;
        }
    }

    private boolean checkBlockingFilterState() {
        if (this.mGet.getFilmState() == 1 || this.mGet.getFilmState() == 5 || this.mGet.getCameraState() <= 0 || this.mGet.isSnapShotProcessing() || !this.mGet.checkModuleValidate(48) || this.mGet.isVolumeKeyPressed()) {
            return true;
        }
        return false;
    }

    public void onClickFilterButton() {
        if (checkMenuButtonClickable() && !checkBlockingFilterState() && this.mGet.checkFocusStateForChangingSetting()) {
            CamLog.m3d(CameraConstants.TAG, "[filter] mBeautyFilterButton clicked and Beauty Bar invisible");
            if (this.mSelectedMenuType == 2) {
                setSelfieOptionVisibility(false, true);
                this.mGet.onHideBeautyMenu();
                if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                    this.mGet.showDoubleCamera(true);
                    return;
                }
                return;
            }
            if (!isRunningFilmEmulator()) {
                runFilmEmulator(CameraConstants.FILM_NONE);
            }
            setSelfieOptionVisibility(true, true);
            if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                this.mGet.showDoubleCamera(false);
            }
            setMenuType(2);
            setMenuButtonSelected(2, true);
            setMenuButtonSelected(1, false);
            setMenuButtonSelected(3, false);
            this.mGet.setBarVisible(1, false, false);
            sFilterOptionVisible = true;
            this.mGet.onShowBeautyMenu(this.mSelectedMenuType);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "[Filter] Film engine is releasing or preview state is unavailable, return");
    }

    public void setFilterButtonType() {
        if (this.mFilterButton != null) {
            int filterId;
            if (CameraConstants.FILM_NONE.equals(this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR))) {
                filterId = C0088R.drawable.btn_front_filter_off;
            } else {
                filterId = C0088R.drawable.btn_front_filter_on;
            }
            this.mFilterButton.setImageResource(filterId);
        }
    }

    private RotateImageButton getMenuButton(int type) {
        switch (type) {
            case 1:
                return this.mSkinToneButton;
            case 2:
                return this.mFilterButton;
            case 3:
                return this.mRelightingButton;
            default:
                return null;
        }
    }

    public void setMenuButtonSelected(int type, boolean selected) {
        RotateImageButton menuButton = getMenuButton(type);
        if (menuButton != null) {
            menuButton.setSelected(selected);
        }
    }

    public void setSelfieMenuVisibility(boolean visible, boolean isForced) {
        boolean z = true;
        CamLog.m3d(CameraConstants.TAG, "[selfie] setMenuVisibility visible ? " + visible);
        if ((isSelfieUIShowingCondition() && !this.mGet.isRecordingPriorityMode()) || isForced) {
            if (!visible || this.mGet.checkModuleValidate(192)) {
                int visibility;
                if (visible) {
                    visibility = 0;
                } else {
                    visibility = 8;
                }
                changeMenuLayoutParam();
                if (this.mSelfieMenuView != null) {
                    this.mSelfieMenuView.setVisibility(visibility);
                }
                if (this.mSkinToneButton != null) {
                    boolean z2;
                    if (this.mGet.isVideoCaptureMode()) {
                        z2 = false;
                    } else {
                        z2 = true;
                    }
                    setMenuButtonEnable(1, z2);
                    this.mSkinToneButton.setVisibility(visibility);
                }
                if (this.mRelightingButton != null) {
                    if (this.mGet.isVideoCaptureMode()) {
                        z = false;
                    }
                    setMenuButtonEnable(3, z);
                    this.mRelightingButton.setVisibility(visibility);
                }
                if (this.mFilterButton != null) {
                    this.mFilterButton.setVisibility(8);
                }
            }
        }
    }

    public void hideSelfieMenuTransient() {
        if (this.mSelfieMenuView != null && this.mSelfieMenuView.getVisibility() == 0) {
            setSelfieMenuVisibility(false);
            this.mHideTransient = true;
        }
    }

    public void restoreSelfieMenuVisibility() {
        if (this.mHideTransient && this.mSelfieMenuView != null && this.mSelfieMenuView.getVisibility() != 0) {
            setSelfieMenuVisibility(true);
            this.mHideTransient = false;
        }
    }

    public void setSelfieMenuVisibility(boolean visible) {
        setSelfieMenuVisibility(visible, false);
    }

    public boolean isSelfieMenuVisible() {
        if (this.mSelfieMenuView != null && this.mSelfieMenuView.getVisibility() == 0) {
            return true;
        }
        return false;
    }

    public int isSelectedMenuType() {
        return this.mSelectedMenuType;
    }

    public void setMenuType(int type) {
        this.mSelectedMenuType = type;
    }

    public void setMenuEnable(boolean enable) {
        setMenuButtonEnable(1, enable);
        setMenuButtonEnable(3, enable);
        setMenuButtonEnable(2, enable);
    }

    public void setMenuButtonEnable(int type, boolean enable) {
        if (isSelfieUIShowingCondition() && this.mSelfieMenuView != null) {
            RotateImageButton menuButton = getMenuButton(type);
            ColorFilter cf = enable ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
            if (menuButton != null) {
                menuButton.setEnabled(enable);
                menuButton.setColorFilter(cf);
                menuButton.getBackground().setColorFilter(cf);
            }
        }
    }

    public void setSelfieOptionVisibility(boolean visible, boolean isRestart, boolean isForced) {
        if (!((!FunctionProperties.isSupportedFilmEmulator() && !this.mGet.isColorEffectSupported() && !FunctionProperties.isSupportedBeautyShot()) || this.mGet.isRearCamera() || CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode())) || isForced) {
            CamLog.m3d(CameraConstants.TAG, "[filter] setOptionVisibility = " + visible + ", isRestart = " + isRestart + ", mFilterScrollOffset = " + this.mFilterScrollOffset);
            if (!visible) {
                setMenuType(0);
                setMenuButtonSelected(1, false);
                setMenuButtonSelected(3, false);
                this.mGet.setBarVisible(1, false, false);
                this.mGet.setExtraPreviewButtonSelected(2, false);
                this.mGet.setExtraPreviewButtonSelected(3, false);
                if (!FunctionProperties.isSupportedFilmEmulator()) {
                    return;
                }
                if (this.mSelfieFilterView == null || this.mSelfieFilterStrengthView == null || this.mSelfieFilterStrengthTextRotateView == null) {
                    CamLog.m3d(CameraConstants.TAG, "[filter] mSelfieFilterView is null.");
                    return;
                }
                setMenuButtonSelected(2, false);
                this.mFilterVisible = false;
                this.mSelfieFilterView.setVisibility(4);
                this.mSelfieFilterStrengthTextRotateView.setVisibility(4);
                this.mSelfieFilterStrengthView.setVisibility(4);
                this.mGet.resumeShutterless();
            } else if (this.mSelfieFilterListview == null || this.mSelfieFilterView == null || this.mSelfieFilterStrengthView == null || this.mSelfieFilterStrengthBar == null || this.mSelfieFilterStrengthLevelText == null) {
                CamLog.m3d(CameraConstants.TAG, "[filter] mSelfieFilterView is null.");
            } else {
                this.mSelfieFilterListview.setSelectionFromTop(this.mCurLutNumber, this.mFilterScrollOffset);
                this.mGet.pauseShutterless();
                this.mSelfieFilterView.setVisibility(0);
                if (!CameraConstants.FILM_NONE.equals(this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR))) {
                    int currentStrength = (int) (this.mFilmStrength * 100.0f);
                    this.mSelfieFilterStrengthBar.setProgress(currentStrength);
                    this.mSelfieFilterStrengthLevelText.setText(Integer.toString(currentStrength));
                    updateSelfieFilterStrengthText();
                    CamLog.m3d(CameraConstants.TAG, "[filmStrength] currentStrength : " + currentStrength);
                    this.mSelfieFilterStrengthView.setVisibility(0);
                }
                this.mFilterVisible = true;
            }
        }
    }

    public boolean getSelfieFilterVisibility() {
        CamLog.m3d(CameraConstants.TAG, "[filter] mFilterVisible = " + this.mFilterVisible);
        return this.mFilterVisible;
    }

    public void setSelfieOptionVisibility(boolean visible, boolean isRestart) {
        setSelfieOptionVisibility(visible, isRestart, false);
    }

    public int getFilterViewVisibility() {
        if (this.mSelfieFilterView == null) {
            return 8;
        }
        return this.mSelfieFilterView.getVisibility();
    }

    public boolean isSelfieOptionVisible() {
        if (this.mGet.isBarVisible(1)) {
            return true;
        }
        if (this.mSelfieFilterView == null || !this.mSelfieFilterView.isShown()) {
            return false;
        }
        return true;
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        if (!this.mGet.isRearCamera()) {
            rotateSelfieButton(degree, animation);
            int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
            if (this.mSelfieFilterView != null && this.mSelfieFilterStrengthView != null && this.mSelfieFilterStrengthBarView != null && this.mSelfieFilterStrengthLevelTextLp != null && this.mSelfieFilterStrengthTextRotateView != null) {
                LayoutParams strengthLp = (LayoutParams) this.mSelfieFilterStrengthView.getLayoutParams();
                LayoutParams strengthBarLp = (LayoutParams) this.mSelfieFilterStrengthBarView.getLayoutParams();
                Utils.resetLayoutParameter(strengthLp);
                Utils.resetLayoutParameter(this.mSelfieFilterStrengthLevelTextLp);
                this.mSelfieFilterStrengthBarView.measure(0, 0);
                switch (degree) {
                    case 0:
                    case 270:
                        strengthBarLp.addRule(2, this.mSelfieFilterView.getId());
                        this.mSelfieFilterStrengthView.rotateLayout(0);
                        this.mSelfieFilterStrengthTextRotateView.rotateLayout(0);
                        this.mSelfieFilterStrengthLevelTextLp.addRule(12);
                        this.mSelfieFilterStrengthLevelTextLp.topMargin = 0;
                        this.mSelfieFilterStrengthLevelTextLp.bottomMargin = degree == 0 ? (int) (((double) this.mStrengthBarBottomMargin) + (((double) this.mSelfieFilterStrengthBarView.getMeasuredHeight()) / 2.24d)) : (int) (((double) this.mStrengthBarBottomMargin) + (((double) this.mSelfieFilterStrengthBarView.getMeasuredHeight()) / 1.5d));
                        break;
                    case 90:
                        strengthLp.removeRule(12);
                        strengthLp.addRule(10);
                        strengthLp.topMargin = (lcdSize[0] - this.mStrengthBarBottomMargin) - this.mSelfieFilterStrengthBarView.getMeasuredHeight();
                        this.mSelfieFilterStrengthView.rotateLayout(180);
                        this.mSelfieFilterStrengthTextRotateView.rotateLayout(180);
                        this.mSelfieFilterStrengthLevelTextLp.bottomMargin = 0;
                        this.mSelfieFilterStrengthLevelTextLp.topMargin = (lcdSize[0] - this.mStrengthBarBottomMargin) - ((int) (((double) this.mSelfieFilterStrengthBarView.getMeasuredHeight()) * 1.6d));
                        break;
                    case 180:
                        strengthLp.removeRule(12);
                        strengthLp.addRule(10);
                        this.mSelfieFilterStrengthView.rotateLayout(180);
                        this.mSelfieFilterStrengthTextRotateView.rotateLayout(180);
                        strengthLp.topMargin = ((lcdSize[0] - this.mStrengthBarBottomMargin) - ((int) (((double) this.mSelfieFilterStrengthBarView.getMeasuredHeight()) / 1.5d))) - ((int) (((double) this.mSelfieFilterStrengthLevelTextSizeLp.width) / 1.2d));
                        this.mSelfieFilterStrengthLevelTextLp.addRule(2, this.mSelfieFilterView.getId());
                        this.mSelfieFilterStrengthLevelTextLp.bottomMargin = 0;
                        this.mSelfieFilterStrengthLevelTextLp.topMargin = (lcdSize[0] - this.mStrengthBarBottomMargin) - ((int) (((double) this.mSelfieFilterStrengthBarView.getMeasuredHeight()) * 1.6d));
                        break;
                }
                rotateSelfieStrengthLabel(degree);
            }
        }
    }

    private void rotateSelfieButton(int degree, boolean animation) {
        if (this.mSelfieFilterView != null) {
            ((RotateLayout) this.mSelfieFilterView.findViewById(C0088R.id.selfie_filter_rotate)).rotateLayout(90);
            this.mFilterButton.setDegree(degree, animation);
            this.mSelfieFilterListAdapter.setDegree(degree);
            this.mSelfieFilterListAdapter.notifyDataSetChanged();
        }
        if (this.mSkinToneButton != null) {
            this.mSkinToneButton.setDegree(degree, animation);
        }
        if (this.mRelightingButton != null) {
            this.mRelightingButton.setDegree(degree, animation);
        }
    }

    private void rotateSelfieStrengthLabel(int degree) {
        if (this.mSelfieFilterStrengthLabelLand != null && this.mSelfieFilterStrengthLabelPort != null && this.mSelfieFilterStrengthLevelText != null) {
            updateSelfieFilterStrengthText();
            if (degree == 90 || degree == 270) {
                this.mSelfieFilterStrengthLabelLand.setVisibility(0);
                this.mSelfieFilterStrengthLabelPort.setVisibility(4);
                this.mSelfieFilterStrengthLabelLand.setRotation(90.0f);
                this.mSelfieFilterStrengthLevelText.setRotation(90.0f);
                return;
            }
            this.mSelfieFilterStrengthLabelLand.setVisibility(4);
            this.mSelfieFilterStrengthLabelPort.setVisibility(0);
            this.mSelfieFilterStrengthLevelText.setRotation(0.0f);
        }
    }

    private void updateSelfieFilterStrengthText() {
        if (!this.mGet.isRearCamera() && this.mSelfieFilterStrengthTextRotateView != null && this.mSelfieFilterStrengthLevelTextLp != null && this.mSelfieFilterLayout != null && this.mSelfieFilterStrengthBar != null && this.mSelfieFilterStrengthLevelTextSizeLp != null) {
            int calculatedWidth = (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.655f) + Utils.getPx(getAppContext(), C0088R.dimen.filter_strength_seekBar_paddingStart)) - RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0555f);
            this.mSelfieStrengthMargin = (((this.mSelfieFilterLayout.getWidth() - calculatedWidth) / 2) + ((this.mSelfieFilterStrengthBar.getProgress() * calculatedWidth) / 100)) - (this.mSelfieFilterStrengthLevelTextSizeLp.width / 2);
            this.mSelfieFilterStrengthLevelTextLp.setMarginStart(getOrientationDegree() % 270 == 0 ? this.mSelfieStrengthMargin : (Utils.getLCDsize(getAppContext(), true)[1] - this.mSelfieStrengthMargin) - this.mSelfieFilterStrengthLevelTextSizeLp.width);
            this.mSelfieFilterStrengthTextRotateView.setLayoutParams(this.mSelfieFilterStrengthLevelTextLp);
        }
    }

    public void setSelfieStrengthLabel() {
        Drawable cursor = this.mGet.getAppContext().getDrawable(C0088R.drawable.camera_preview_setting_cursor_normal);
        if (this.mSelfieFilterStrengthLabelPort != null && this.mSelfieFilterStrengthLabelLand != null && this.mSelfieFilterStrengthBarView != null) {
            LayoutParams labelLp = (LayoutParams) this.mSelfieFilterStrengthLabelPort.getLayoutParams();
            labelLp.topMargin = (Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.filter_strength_seekBar_label_marginTop_port) + (cursor.getIntrinsicHeight() / 2)) + Utils.getPx(getAppContext(), C0088R.dimen.filter_strength_seekBar_label_marginBottom_land);
            labelLp.addRule(6, this.mSelfieFilterStrengthBarView.getId());
            labelLp.addRule(18, this.mSelfieFilterStrengthBarView.getId());
            this.mSelfieFilterStrengthLabelPort.setLayoutParams(labelLp);
            this.mSelfieFilterStrengthLabelLand.measure(0, 0);
            int labelMarginLandCompensationValue = (this.mSelfieFilterStrengthLabelLand.getMeasuredHeight() - this.mSelfieFilterStrengthLabelLand.getMeasuredWidth()) / 2;
            labelLp = (LayoutParams) this.mSelfieFilterStrengthLabelLand.getLayoutParams();
            labelLp.topMargin = ((cursor.getIntrinsicHeight() - this.mSelfieFilterStrengthLabelLand.getMeasuredHeight()) / 2) + Utils.getPx(getAppContext(), C0088R.dimen.filter_strength_seekBar_label_marginBottom_land);
            labelLp.setMarginEnd(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.filter_strength_seekBar_label_marginEnd_land) + labelMarginLandCompensationValue);
            labelLp.addRule(16, this.mSelfieFilterStrengthBarView.getId());
            labelLp.addRule(6, this.mSelfieFilterStrengthBarView.getId());
            this.mSelfieFilterStrengthLabelLand.setLayoutParams(labelLp);
        }
    }

    public void onCameraSwitchingStart() {
        super.onCameraSwitchingStart();
        if (!this.mGet.isRearCamera()) {
            CamLog.m3d(CameraConstants.TAG, "[switch] onCameraSwitchingStart");
            setSelfieOptionVisibility(false, true);
            setSelfieMenuVisibility(false);
        }
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        setSelfieOptionVisibility(false, true);
        this.mGet.removePostRunnable(this.mSelfieFilterStrengthLevelTextRemoveRunnable);
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        if (isSelfieUIShowingCondition()) {
            CamLog.m3d(CameraConstants.TAG, "[filter] onResumeBefore");
            sFilterOptionVisible = true;
            setFilterButtonType();
            if (!CameraConstants.FILM_NONE.equals(this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR))) {
                setMenuButtonSelected(2, false);
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (this.mSelfieMenulayout != null) {
            vg.removeView(this.mSelfieMenulayout);
            this.mSelfieMenulayout = null;
            this.mSelfieMenuView = null;
            this.mFilterButton = null;
            this.mSkinToneButton = null;
            this.mRelightingButton = null;
        }
        sFilterOptionVisible = false;
        if (this.mSelfieFilterLayout != null) {
            vg.removeView(this.mSelfieFilterLayout);
            this.mSelfieFilterLayout = null;
            this.mSelfieFilterView = null;
            this.mSelfieFilterStrengthView = null;
            this.mSelfieFilterStrengthBar = null;
            this.mSelfieFilterStrengthBarView = null;
            this.mSelfieFilterStrengthLevelText = null;
            this.mSelfieFilterStrengthTextRotateView = null;
            this.mSelfieFilterStrengthLabelPort = null;
            this.mSelfieFilterStrengthLabelLand = null;
            this.mSelfieFilterStrengthLevelTextLp = null;
            this.mSelfieFilterStrengthLevelTextSizeLp = null;
            this.mSelfieFilterStrengthLevelTextRemoveRunnable = null;
        }
    }

    public void onConfigurationChanged(Configuration config) {
        CamLog.m3d(CameraConstants.TAG, "[selfie] onConfigurationChanged");
        onDestroy();
        init();
        setSelfieMenuVisibility(true);
        super.onConfigurationChanged(config);
    }

    private void updateListViewItem(int position) {
        if (this.mSelfieFilterListAdapter != null) {
            this.mSelfieFilterListAdapter.setSelectedItem(position);
            this.mSelfieFilterListAdapter.notifyDataSetChanged();
        }
    }

    private boolean isSelfieUIShowingCondition() {
        return false;
    }

    public void setSelfieFilterStrengthBarListener() {
        if (!this.mGet.isRearCamera() && this.mSelfieFilterStrengthBar != null) {
            this.mSelfieFilterStrengthBar.setOnSeekBarChangeListener(new C08056());
        }
    }
}
