package com.lge.camera.managers.ext;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.ValueSeekBar;
import com.lge.camera.components.ValueSeekBar.OnValueSeekBarChangeListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

public class CinemaFilterManager extends CinemaFilterManagerBase {
    private static final int DEFAULT_LUT_VALUE = 100;
    private static final int DEFAULT_VIGNETTE_VALUE = 0;
    public static final int LUT_BAR = 0;
    public static final int NONE_FILTER_NUM = 1;
    private static final String NONE_FILTER_VALUE = "1-100";
    private static final String NONE_VIGNETTE_VALUE = "1-0";
    public static final int VIGNETTE_BAR = 1;
    private int EFFECT_CHANGE_DELAY = 200;
    private final double ITEM_SHOW_COUNT = 5.5d;
    private int MIN_INTERVAL = 100;
    private final int VIGNETTE_DIVIDE_VALUE = 10;
    private ValueSeekBar[] mCinemaSeekBars;
    private View mCover;
    private LayoutParams mCoverLp;
    private DetailViewHandler mDetailViewHandler;
    private CinemaLUTListViewAdapter mLUTAdapter;
    private RotateLayout mLUTListViewRotateLayout;
    private OnValueSeekBarChangeListener mLUTStrengthBarChangeListener = new C11924();
    private HandlerRunnable mLUTStrengthRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            CinemaFilterManager.this.setCinemaLUT();
        }
    };
    private int mListItemTextSize = 0;
    private int mListItemTopOffset = 0;

    /* renamed from: com.lge.camera.managers.ext.CinemaFilterManager$1 */
    class C11891 implements OnItemClickListener {
        C11891() {
        }

        public void onItemClick(final AdapterView<?> listview, View itemView, final int index, long arg3) {
            if (CinemaFilterManager.this.mCurrentLUTIndex == index || CinemaFilterManager.this.mLUTAdapter == null || CinemaFilterManager.this.mCinemaSeekBars == null || CinemaFilterManager.this.mCover == null || CinemaFilterManager.this.mCoverLp == null) {
                CamLog.m7i(CameraConstants.TAG, "some view is null! return");
                return;
            }
            int[] location = new int[2];
            itemView.getLocationInWindow(location);
            CinemaFilterManager.this.mCoverLp.setMarginStart(CinemaFilterManager.this.isRTL ? location[0] - itemView.getWidth() : location[0]);
            CinemaFilterManager.this.mCoverLp.topMargin = CinemaFilterManager.this.isRTL ? (location[1] - CinemaFilterManager.this.mCoverLp.width) + itemView.getHeight() : location[1] - CinemaFilterManager.this.mCoverLp.width;
            CinemaFilterManager.this.mCover.setVisibility(0);
            CinemaFilterManager.this.mCover.setLayoutParams(CinemaFilterManager.this.mCoverLp);
            AnimationUtil.startCineEffectAnimiation(CinemaFilterManager.this.mCover);
            CinemaFilterManager.this.mGet.postOnUiThread(new HandlerRunnable(CinemaFilterManager.this) {
                public void handleRun() {
                    if (CinemaFilterManager.this.mGet.isRecordingState() || listview == null || CinemaFilterManager.this.mCinemaSeekBars == null || CinemaFilterManager.this.mLUTAdapter == null) {
                        CamLog.m7i(CameraConstants.TAG, "do not change cine effect return. isRecordingState = " + CinemaFilterManager.this.mGet.isRecordingState());
                        return;
                    }
                    CinemaFilterManager.this.mCurrentLUTIndex = index;
                    ((ListView) listview).smoothScrollToPositionFromTop(index, CinemaFilterManager.this.mListItemTopOffset);
                    CinemaFilterManager.this.mCinemaSeekBars[0].setProgress(100);
                    CinemaFilterManager.this.mCinemaSeekBars[1].setProgress(0);
                    CinemaFilterManager.this.setCinemaLUT();
                    CinemaFilterManager.this.mCineFilterGet.setCinemaQuickButtonIcon();
                    CinemaFilterManager.this.mLUTAdapter.notifyDataSetChanged();
                    for (ValueSeekBar bar : CinemaFilterManager.this.mCinemaSeekBars) {
                        int i;
                        if (index == 0) {
                            i = 4;
                        } else {
                            i = 0;
                        }
                        bar.setVisibility(i);
                    }
                }
            }, (long) CinemaFilterManager.this.EFFECT_CHANGE_DELAY);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.CinemaFilterManager$2 */
    class C11902 implements OnItemLongClickListener {
        C11902() {
        }

        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            CamLog.m3d(CameraConstants.TAG, "longClick Cinema filter, position = " + position);
            if (position == 0) {
                return false;
            }
            CinemaLUTItem item = (CinemaLUTItem) parent.getItemAtPosition(position);
            if (!(CinemaFilterManager.this.mDetailViewHandler == null || item == null)) {
                StringBuilder info = new StringBuilder();
                if (item.mHashTagSet != null) {
                    Iterator it = item.mHashTagSet.iterator();
                    while (it.hasNext()) {
                        info.append("#" + ((String) it.next()) + " ");
                    }
                }
                CinemaFilterManager.this.mDetailViewHandler.showCineDetailView(item.mLargeDrawableId, info.toString());
            }
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.CinemaFilterManager$3 */
    class C11913 implements OnTouchListener {
        C11913() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == 1) {
                CinemaFilterManager.this.hideDetailView();
            } else if (event.getAction() == 0) {
                CinemaFilterManager.this.hideBubbleHelp();
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.CinemaFilterManager$4 */
    class C11924 implements OnValueSeekBarChangeListener {
        private long mLastSetLUTTime = 0;
        private long mSetLUTTimeDiff = 0;

        C11924() {
        }

        public void onProgressChanged(ValueSeekBar valueSeekBar, int progress, boolean fromUser, boolean valueChanged) {
            if (valueSeekBar != null && CinemaFilterManager.this.mCinemaSeekBars != null && CinemaFilterManager.this.mLUTStrengthRunnable != null) {
                valueSeekBar.setTextVisibility(0);
                valueSeekBar.removeTextDelayed(1500);
                if (valueSeekBar.getType() == 1 && valueChanged) {
                    CinemaFilterManager.this.setCinemaLUT();
                    return;
                }
                this.mSetLUTTimeDiff = System.currentTimeMillis() - this.mLastSetLUTTime;
                if (this.mSetLUTTimeDiff < ((long) CinemaFilterManager.this.MIN_INTERVAL)) {
                    CinemaFilterManager.this.mCineFilterGet.postOnUiThread(CinemaFilterManager.this.mLUTStrengthRunnable, Math.max(0, ((long) CinemaFilterManager.this.MIN_INTERVAL) - this.mSetLUTTimeDiff));
                    return;
                }
                CinemaFilterManager.this.mCineFilterGet.runOnUiThread(CinemaFilterManager.this.mLUTStrengthRunnable);
                this.mSetLUTTimeDiff = 0;
                this.mLastSetLUTTime = System.currentTimeMillis();
            }
        }

        public void onStartTrackingTouch(ValueSeekBar valueSeekBar) {
            this.mSetLUTTimeDiff = 0;
            this.mLastSetLUTTime = 0;
        }

        public void onStopTrackingTouch(ValueSeekBar valueSeekBar) {
        }
    }

    class CinemaLUTItem {
        public HashSet<String> mHashTagSet = new HashSet();
        public int mLargeDrawableId;
        public int mName;
        public int mThumbDrawableId;

        public CinemaLUTItem(int thumbDrawableId, int largeDrawableId, int name, int[] hashTags) {
            this.mThumbDrawableId = thumbDrawableId;
            this.mLargeDrawableId = largeDrawableId;
            this.mName = name;
            if (hashTags != null) {
                for (int id : hashTags) {
                    this.mHashTagSet.add(CinemaFilterManager.this.getAppContext().getString(id));
                }
            }
        }
    }

    class CinemaLUTListViewAdapter extends ArrayAdapter<CinemaLUTItem> {
        private int mDegree = -1;
        private int mJapanTextWidth;
        private boolean mLocaleJapan = false;
        private int mResource;

        public CinemaLUTListViewAdapter(Context context, int resource, CinemaLUTItem[] objects) {
            super(context, resource, objects);
            this.mResource = resource;
            this.mLocaleJapan = Locale.JAPAN.equals(Locale.getDefault());
            this.mJapanTextWidth = CinemaFilterManager.this.mListItemWidth - (Utils.getPx(CinemaFilterManager.this.getAppContext(), C0088R.dimen.cinema_listView_padding) * 2);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            boolean z = false;
            if (convertView == null) {
                convertView = CinemaFilterManager.this.mCineFilterGet.inflateView(this.mResource);
                convertView.setLayoutParams(new ViewGroup.LayoutParams(CinemaFilterManager.this.mListItemWidth, CinemaFilterManager.this.mListItemWidth));
            }
            RotateImageButton itemView = (RotateImageButton) convertView.findViewById(C0088R.id.cinema_listview_item_view);
            itemView.setImageResource(position == CinemaFilterManager.this.mCurrentLUTIndex ? C0088R.drawable.camera_selfie_filter_selected : 0);
            itemView.setBackgroundResource(position == 0 ? C0088R.color.tile_preview_bg_2 : ((CinemaLUTItem) getItem(position)).mThumbDrawableId);
            itemView.setDegree(CinemaFilterManager.this.isRTL ? this.mDegree + 90 : this.mDegree - 90, false);
            itemView.setText("");
            itemView.setTextSize(CinemaFilterManager.this.mListItemTextSize);
            if (this.mLocaleJapan) {
                itemView.setSingleLine(true);
                itemView.setTextWidth(this.mJapanTextWidth);
                itemView.initButtonText(CinemaFilterManager.this.getAppContext().getString(((CinemaLUTItem) getItem(position)).mName));
            } else {
                itemView.setText(CinemaFilterManager.this.getAppContext().getString(((CinemaLUTItem) getItem(position)).mName));
            }
            itemView.setTextGravity(position == 0 ? 17 : 4);
            itemView.setTextPaddingBottom(position == 0 ? 0 : Utils.getPx(CinemaFilterManager.this.getAppContext(), C0088R.dimen.advanced_selfie_text_bottomMargin));
            itemView.setContentDescription(CinemaFilterManager.this.getAppContext().getString(((CinemaLUTItem) getItem(position)).mName));
            if (position == CinemaFilterManager.this.mCurrentLUTIndex) {
                z = true;
            }
            itemView.setSelected(z);
            return convertView;
        }

        public void setDegree(int degree) {
            if (this.mDegree != degree) {
                this.mDegree = degree;
                notifyDataSetChanged();
            }
        }
    }

    public CinemaFilterManager(CinemaFilterInterface moduleInterface) {
        super(moduleInterface);
        this.mCineFilterGet = moduleInterface;
        this.mCurrentLUTIndex = SharedPreferenceUtil.getCinemaLUTIndex(getAppContext());
        if (FunctionProperties.isSupportedHDR10()) {
            this.EFFECT_CHANGE_DELAY = 100;
        }
    }

    public void init() {
        ViewGroup baseParent = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (baseParent == null) {
            CamLog.m3d(CameraConstants.TAG, "baseParent is null");
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "init Cine filter");
        this.mGet.inflateView(C0088R.layout.cinema_lut_controller, baseParent);
        this.mLUTControllerLayout = baseParent.findViewById(C0088R.id.cinema_lut_controller);
        this.mLUTListViewRotateLayout = (RotateLayout) baseParent.findViewById(C0088R.id.cinema_lut_listview_rotateLayout);
        this.mCover = baseParent.findViewById(C0088R.id.cine_effect_cover);
        this.mCoverLp = (LayoutParams) this.mCover.getLayoutParams();
        if (this.mCineFilterGet.isVoiceAssistantSpecified()) {
            String subMode = this.mCineFilterGet.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_SUB_MODE, null);
            int subModeIndex = this.mCurrentLUTIndex;
            String[] subModeArray = new String[]{"romantic", "beauty", "summer_blockbuster", "romantic_comedy", "documentary", "scenery", "drama", "historical", "melodramatic", "mystery", "noir", "classic", "thriller", "flashback", "pop_art"};
            for (int i = 0; i < subModeArray.length; i++) {
                if (subModeArray[i].equalsIgnoreCase(subMode)) {
                    CamLog.m3d(CameraConstants.TAG, "subMode = " + subMode + ", index = " + (i + 1) + ", mCurrentLUTIndex = " + this.mCurrentLUTIndex);
                    subModeIndex = i + 1;
                    break;
                }
            }
            if (this.mCurrentLUTIndex != subModeIndex) {
                this.mCurrentLUTIndex = subModeIndex;
                setCinemaLUT();
            }
        }
        initStrengthBarUI();
        initLUTListUI();
        this.mDetailViewHandler = new DetailViewHandler(getAppContext(), (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls));
        this.mDetailViewHandler.initCineDetailView();
        super.init();
    }

    private void initLUTListUI() {
        if (this.mLUTListViewRotateLayout != null) {
            this.mLUTListView = (ListView) this.mLUTListViewRotateLayout.findViewById(C0088R.id.cinema_lut_listview);
            if (this.mLUTListView != null) {
                int[] lcdSize = Utils.getLCDsize(getAppContext(), false);
                this.mListItemWidth = (int) ((((double) lcdSize[1]) - (((double) Utils.getPx(getAppContext(), C0088R.dimen.cinema_listView_padding)) * Math.ceil(5.5d))) / 5.5d);
                this.mListItemTextSize = (int) (((float) this.mListItemWidth) * 0.145f);
                this.mListItemTopOffset = (lcdSize[1] - this.mListItemWidth) / 2;
                RelativeLayout.LayoutParams listViewLp = (RelativeLayout.LayoutParams) this.mLUTListView.getLayoutParams();
                RelativeLayout.LayoutParams rotateLp = (RelativeLayout.LayoutParams) this.mLUTListViewRotateLayout.getLayoutParams();
                if (listViewLp != null && rotateLp != null) {
                    listViewLp.width = this.mListItemWidth + (Utils.getPx(getAppContext(), C0088R.dimen.cinema_listView_padding) * 2);
                    rotateLp.setMargins(0, 0, 0, (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.336f) - this.mListItemWidth) - (Utils.getPx(getAppContext(), C0088R.dimen.cinema_listView_padding) * 2));
                    this.mLUTListView.setLayoutParams(listViewLp);
                    this.mLUTListViewRotateLayout.setLayoutParams(rotateLp);
                    makeLUTAdapter();
                    this.mLUTListView.setAdapter(this.mLUTAdapter);
                    this.mLUTListView.setSelectionFromTop(this.mCurrentLUTIndex, this.mListItemTopOffset);
                    setListViewListener(this.mLUTListView);
                    this.mLUTListViewRotateLayout.rotateLayout(this.isRTL ? 270 : 90);
                }
            }
        }
    }

    private void setListViewListener(ListView listView) {
        if (listView == null) {
            CamLog.m7i(CameraConstants.TAG, "listView is null return");
            return;
        }
        listView.setOnItemClickListener(new C11891());
        setListViewLongClickAndTouchListener(listView);
    }

    private void setListViewLongClickAndTouchListener(ListView listView) {
        listView.setOnItemLongClickListener(new C11902());
        listView.setOnTouchListener(new C11913());
    }

    private void makeLUTAdapter() {
        if (this.mLUTAdapter == null) {
            cineLUTList = new CinemaLUTItem[17];
            cineLUTList[1] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_01, C0088R.drawable.camera_filter_long_press_sample_01, C0088R.string.advanced_selfie_filter_romantic, new int[]{C0088R.string.cine_filter_tag_beauty, C0088R.string.cine_filter_tag_going_out, C0088R.string.advanced_selfie_filter_romantic, C0088R.string.cine_filter_tag_bright, C0088R.string.cine_filter_tag_couple, C0088R.string.cine_filter_tag_pastel});
            cineLUTList[2] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_02, C0088R.drawable.camera_filter_long_press_sample_02, C0088R.string.cine_filter_tag_beauty, new int[]{C0088R.string.cine_filter_tag_beauty, C0088R.string.cine_filter_tag_traveling, C0088R.string.cine_filter_tag_happy, C0088R.string.cine_filter_tag_shiny, C0088R.string.cine_filter_tag_baby, C0088R.string.cine_filter_tag_pink});
            cineLUTList[3] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_03, C0088R.drawable.camera_filter_long_press_sample_03, C0088R.string.cine_filter_tag_summer_blockbuster, new int[]{C0088R.string.cine_filter_tag_summer_blockbuster, C0088R.string.cine_filter_tag_dynamic, C0088R.string.cine_filter_tag_strong, C0088R.string.cine_filter_tag_bright, C0088R.string.cine_filter_tag_modern_city, C0088R.string.cine_filter_tag_high_contrast});
            cineLUTList[4] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_04, C0088R.drawable.camera_filter_long_press_sample_04, C0088R.string.cine_filter_tag_romantic_comedy, new int[]{C0088R.string.cine_filter_tag_comedy, C0088R.string.cine_filter_tag_beauty, C0088R.string.cine_filter_tag_soft, C0088R.string.cine_filter_tag_north_europe, C0088R.string.cine_filter_tag_young_woman, C0088R.string.cine_filter_tag_pink_original});
            cineLUTList[5] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_05, C0088R.drawable.camera_filter_long_press_sample_05, C0088R.string.cine_filter_tag_documentary, new int[]{C0088R.string.cine_filter_tag_documentary, C0088R.string.cine_filter_tag_natural, C0088R.string.cine_filter_tag_conservative, C0088R.string.cine_filter_tag_conventional, C0088R.string.cine_filter_tag_middle_aged_gentleman, C0088R.string.cine_filter_tag_khaki_brown});
            cineLUTList[6] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_06, C0088R.drawable.camera_filter_long_press_sample_06, C0088R.string.cine_filter_tag_scenery, new int[]{C0088R.string.cine_filter_tag_scenery, C0088R.string.cine_filter_tag_dazzling, C0088R.string.cine_filter_tag_landscape, C0088R.string.cine_filter_tag_light_saver});
            cineLUTList[7] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_07, C0088R.drawable.camera_filter_long_press_sample_07, C0088R.string.cine_filter_tag_drama, new int[]{C0088R.string.advanced_selfie_filter_romantic, C0088R.string.cine_filter_tag_day_off, C0088R.string.cine_filter_tag_warm, C0088R.string.cine_filter_tag_indoor, C0088R.string.cine_filter_tag_child, C0088R.string.cine_filter_tag_yellow});
            cineLUTList[8] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_08, C0088R.drawable.camera_filter_long_press_sample_08, C0088R.string.cine_filter_tag_historical, new int[]{C0088R.string.cine_filter_tag_historic, C0088R.string.cine_filter_tag_memory, C0088R.string.cine_filter_tag_fading, C0088R.string.cine_filter_tag_gothic, C0088R.string.cine_filter_tag_old_city, C0088R.string.cine_filter_tag_faded_color});
            cineLUTList[9] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_09, C0088R.drawable.camera_filter_long_press_sample_09, C0088R.string.cine_filter_tag_melo_drama, new int[]{C0088R.string.cine_filter_tag_melo_drama, C0088R.string.cine_filter_tag_sultriness, C0088R.string.cine_filter_tag_elegance, C0088R.string.cine_filter_tag_sunset, C0088R.string.cine_filter_tag_people, C0088R.string.cine_filter_tag_gold});
            cineLUTList[10] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_10, C0088R.drawable.camera_filter_long_press_sample_10, C0088R.string.cine_filter_tag_mystery, new int[]{C0088R.string.cine_filter_tag_mystery, C0088R.string.cine_filter_tag_cold, C0088R.string.cine_filter_tag_modern, C0088R.string.cine_filter_tag_fit_to_any_day, C0088R.string.cine_filter_tag_young_people, C0088R.string.cine_filter_tag_blue});
            cineLUTList[11] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_11, C0088R.drawable.camera_filter_long_press_sample_11, C0088R.string.cine_filter_tag_noir, new int[]{C0088R.string.cine_filter_tag_thriller, C0088R.string.cine_filter_tag_chasing, C0088R.string.cine_filter_tag_dark, C0088R.string.cine_filter_tag_siberia, C0088R.string.cine_filter_tag_outdoor, C0088R.string.cine_filter_tag_high_contrast});
            cineLUTList[12] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_12, C0088R.drawable.camera_filter_long_press_sample_12, C0088R.string.cine_filter_tag_classic, new int[]{C0088R.string.cine_filter_tag_classical, C0088R.string.cine_filter_tag_soft, C0088R.string.cine_filter_tag_moody, C0088R.string.cine_filter_tag_black_white, C0088R.string.cine_filter_tag_potrait, C0088R.string.cine_filter_tag_gray});
            cineLUTList[13] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_13, C0088R.drawable.camera_filter_long_press_sample_13, C0088R.string.cine_filter_tag_thriller, new int[]{C0088R.string.cine_filter_tag_day_for_night, C0088R.string.cine_filter_tag_rainy, C0088R.string.cine_filter_tag_gloomy, C0088R.string.cine_filter_tag_dawn, C0088R.string.cine_filter_tag_nature, C0088R.string.cine_filter_tag_dark_blue});
            cineLUTList[14] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_14, C0088R.drawable.camera_filter_long_press_sample_14, C0088R.string.cine_filter_tag_flash_back, new int[]{C0088R.string.cine_filter_tag_sepia, C0088R.string.cine_filter_tag_in_1930, C0088R.string.cine_filter_tag_flash_back, C0088R.string.cine_filter_tag_autumn, C0088R.string.cine_filter_tag_people, C0088R.string.cine_filter_tag_brown});
            cineLUTList[15] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_15, C0088R.drawable.camera_filter_long_press_sample_15, C0088R.string.cine_filter_tag_pop_art, new int[]{C0088R.string.cine_filter_tag_pop_art, C0088R.string.cine_filter_tag_unique, C0088R.string.cine_filter_tag_experimental, C0088R.string.cine_filter_tag_infrared, C0088R.string.cine_filter_tag_artistic, C0088R.string.cine_filter_tag_red});
            cineLUTList[16] = new CinemaLUTItem(C0088R.drawable.camera_filter_cine_sample_16, C0088R.drawable.camera_filter_long_press_sample_16, C0088R.string.cine_filter_tag_red_popout, new int[]{C0088R.string.cine_filter_tag_red, C0088R.string.cine_filter_tag_point, C0088R.string.cine_filter_tag_overwhelming, C0088R.string.cine_filter_tag_black_white, C0088R.string.cine_filter_tag_sensational, C0088R.string.cine_filter_tag_emphasize});
            this.mLUTAdapter = new CinemaLUTListViewAdapter(getAppContext(), C0088R.layout.cinema_listview_item, cineLUTList);
        }
        this.mLUTAdapter.setDegree(getOrientationDegree());
    }

    private void initStrengthBarUI() {
        Drawable cursor = getAppContext().getDrawable(C0088R.drawable.ic_camera_cursor);
        if (this.mLUTControllerLayout == null || cursor == null) {
            CamLog.m5e(CameraConstants.TAG, "mLUTControllerLayout or cursor is null, mLUTControllerLayout : " + this.mLUTControllerLayout);
            return;
        }
        this.mCinemaSeekBars = new ValueSeekBar[2];
        this.mCinemaSeekBars[0] = new ValueSeekBar(getAppContext(), 0, (ViewGroup) this.mLUTControllerLayout);
        this.mCinemaSeekBars[0].setProgress(100);
        this.mCinemaSeekBars[0].setBarMargins(0, 0, 0, RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.358f));
        this.mCinemaSeekBars[0].setSeekBarDescription(getAppContext().getString(C0088R.string.strength));
        this.mCinemaSeekBars[0].setLabel(getAppContext().getString(C0088R.string.strength));
        this.mCinemaSeekBars[1] = new ValueSeekBar(getAppContext(), 1, (ViewGroup) this.mLUTControllerLayout);
        this.mCinemaSeekBars[1].setProgress(0);
        this.mCinemaSeekBars[1].setBarMargins(0, 0, 0, RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.372f) + cursor.getIntrinsicHeight());
        this.mCinemaSeekBars[1].setDivideValue(10);
        this.mCinemaSeekBars[1].setSeekBarDescription(getAppContext().getString(C0088R.string.popout_vignetting));
        this.mCinemaSeekBars[1].setLabel(getAppContext().getString(C0088R.string.popout_vignetting));
        for (ValueSeekBar bar : this.mCinemaSeekBars) {
            bar.setOnValueSeekBarChangeListener(this.mLUTStrengthBarChangeListener);
            bar.setRotation(getOrientationDegree());
        }
    }

    private void setCinemaLUT() {
        if (!this.mCineFilterGet.isRecordingState() && this.mCineFilterGet.getCameraDevice() != null) {
            CameraParameters parameters = this.mCineFilterGet.getCameraDevice().getParameters();
            if (parameters == null) {
                return;
            }
            if (getCurrentLUTNum() == 1) {
                this.mCineFilterGet.setParamUpdater(parameters, ParamConstants.KEY_CINEMA_LUT, "1-100");
                this.mCineFilterGet.setParamUpdater(parameters, ParamConstants.KEY_CINEMA_VIGNETTE, "1-0");
                CamLog.m3d(CameraConstants.TAG, "set cinema lut none");
                this.mCineFilterGet.getCameraDevice().setParameters(parameters);
                return;
            }
            this.mCineFilterGet.setParamUpdater(parameters, ParamConstants.KEY_CINEMA_LUT, getCurrentLUTNum() + "-" + getCurrentLUTStrength(0));
            this.mCineFilterGet.setParamUpdater(parameters, ParamConstants.KEY_CINEMA_VIGNETTE, "1-" + getCurrentLUTStrength(1));
            CamLog.m3d(CameraConstants.TAG, "set cinema lut number = " + getCurrentLUTNum() + ", strength = " + getCurrentLUTStrength(0) + ", vignette = " + getCurrentLUTStrength(1));
            this.mCineFilterGet.getCameraDevice().setParameters(parameters);
        }
    }

    public int getCurrentLUTNum() {
        return this.mCurrentLUTIndex + 1;
    }

    public int getCurrentLUTStrength(int type) {
        if (this.mCinemaSeekBars == null || this.mCinemaSeekBars[type] == null) {
            return type == 0 ? 100 : 0;
        } else {
            return this.mCinemaSeekBars[type].getValue();
        }
    }

    private void hideDetailView() {
        if (this.mDetailViewHandler != null) {
            this.mDetailViewHandler.hide();
        }
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        hideDetailView();
    }

    public void onDestroy() {
        super.onDestroy();
        SharedPreferenceUtil.setCinemaLUTIndex(getAppContext(), this.mCurrentLUTIndex);
        if (this.mDetailViewHandler != null) {
            this.mDetailViewHandler.onDestroy();
            this.mDetailViewHandler = null;
        }
        ViewGroup baseParent = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (!(baseParent == null || this.mLUTControllerLayout == null)) {
            baseParent.removeView(this.mLUTControllerLayout);
        }
        this.mLUTControllerLayout = null;
        this.mCinemaSeekBars = null;
        this.mLUTListViewRotateLayout = null;
        this.mLUTListView = null;
        this.mLUTAdapter = null;
        this.mBubbleBox = null;
        this.mBubbleTextLayout = null;
        this.mCover = null;
        this.mCoverLp = null;
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        if (this.mLUTAdapter != null) {
            this.mLUTAdapter.setDegree(degree);
        }
        if (this.mCinemaSeekBars != null) {
            for (ValueSeekBar bar : this.mCinemaSeekBars) {
                bar.setRotation(degree);
            }
        }
        if (this.mDetailViewHandler != null) {
            this.mDetailViewHandler.rotateLayout(degree);
        }
        setBubbleTextLayout();
    }

    public boolean hideCinemaLUTLayout() {
        if (this.mLUTControllerLayout == null || this.mLUTControllerLayout.getVisibility() != 0) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "hide Cinema filter");
        this.mLUTControllerLayout.setVisibility(8);
        hideDetailView();
        hideBubbleHelp();
        return true;
    }

    public boolean showCinemaLUTLayout() {
        if (this.mLUTControllerLayout == null || this.mLUTControllerLayout.getVisibility() == 0 || this.mCinemaSeekBars == null || this.mCover == null) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "show Cinema filter");
        for (ValueSeekBar bar : this.mCinemaSeekBars) {
            int i;
            if (this.mCurrentLUTIndex == 0) {
                i = 4;
            } else {
                i = 0;
            }
            bar.setVisibility(i);
        }
        this.mCover.clearAnimation();
        this.mCover.setVisibility(8);
        this.mLUTControllerLayout.setVisibility(0);
        showBubbleHelp();
        return true;
    }
}
