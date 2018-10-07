package com.lge.camera.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Switch;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.components.SettingVerticalDivider;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.Utils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class SettingParentAdapter extends ArrayAdapter<SettingMenuItem> implements Observer {
    private WeakReference<Context> mContext;
    private int mDegree = 0;
    private int mGuideTextWidth = 0;
    private boolean mIsAvailableUpdate = true;
    private boolean mIsExistGuideText = false;
    private boolean mIsRTLDirection = false;
    private int mItemViewStartMargin = 0;
    private SettingParentInterface mListener;
    private WeakReference<SettingMenu> mMenus;
    private Runnable mNotifyDataSetChangeRunnable = new C14213();
    private int mSelectedIndex = -1;

    public interface SettingParentInterface {
        boolean isOrientationChanged();

        boolean isScrollState();

        void onSettingMenuHide();

        void onSwitchButtonClicked(String str, boolean z, CompoundButton compoundButton, boolean z2);
    }

    /* renamed from: com.lge.camera.settings.SettingParentAdapter$2 */
    class C14202 implements OnTouchListener {
        int mDownX;
        boolean mGestureDetected = false;

        C14202() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    this.mDownX = (int) event.getX();
                    this.mGestureDetected = false;
                    break;
                case 1:
                    if (this.mGestureDetected) {
                        return true;
                    }
                    break;
                case 2:
                    if (((int) event.getX()) - this.mDownX > 300 && SettingParentAdapter.this.mListener != null) {
                        SettingParentAdapter.this.mListener.onSettingMenuHide();
                        this.mGestureDetected = true;
                        break;
                    }
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.settings.SettingParentAdapter$3 */
    class C14213 implements Runnable {
        C14213() {
        }

        public void run() {
            SettingParentAdapter.this.notifyDataSetChanged();
        }
    }

    public class ItemViewHolder {
        ImageView mBulletImage;
        RotateTextView mCurrentSettingValue;
        SettingVerticalDivider mDivider;
        ImageView mSettingArrow;
        TextView mSettingGuideText;
        Switch mSettingOnOffSwitch;
        TextView mSettingTitle;
        LinearLayout mSettingTitleWrapper;
        LinearLayout mSettingValueLayout;
    }

    public class SectionViewHolder {
        RotateTextView mSettingSectionView;
    }

    public SettingParentAdapter(Context context, int resId, SettingMenu menus, ArrayList<SettingMenuItem> menuItem, SettingParentInterface listener) {
        boolean z = true;
        super(context, resId, menuItem);
        this.mContext = new WeakReference(context);
        this.mMenus = new WeakReference(menus);
        menus.addObserver(this);
        this.mListener = listener;
        if (context.getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        this.mIsRTLDirection = z;
    }

    public void close() {
        SettingMenu settingMenu = (SettingMenu) this.mMenus.get();
        if (settingMenu != null) {
            settingMenu.deleteObserver(this);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = (Context) this.mContext.get();
        View itemView = convertView;
        SettingMenu settingMenu = (SettingMenu) this.mMenus.get();
        SettingMenuItem menuItem = getItem(position);
        if (settingMenu == null || menuItem == null) {
            return null;
        }
        this.mItemViewStartMargin = getStartMargin(context);
        if (menuItem.getSettingIndex() == -100) {
            SectionViewHolder sectionViewHolder;
            if (itemView == null || (itemView.getTag() instanceof ItemViewHolder)) {
                itemView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(C0088R.layout.setting_parent_section_view, parent, false);
                if (itemView == null) {
                    CamLog.m11w(CameraConstants.TAG, "SettingParentAdapter error. view is null.");
                    return null;
                }
                sectionViewHolder = new SectionViewHolder();
                sectionViewHolder.mSettingSectionView = (RotateTextView) itemView.findViewById(C0088R.id.settings_list_section_title);
                itemView.setTag(sectionViewHolder);
            } else {
                sectionViewHolder = (SectionViewHolder) itemView.getTag();
            }
            sectionViewHolder.mSettingSectionView.setText(menuItem.getName());
            sectionViewHolder.mSettingSectionView.setContentDescription(menuItem.getName());
        } else {
            ItemViewHolder itemViewHolder;
            if (itemView == null || (itemView.getTag() instanceof SectionViewHolder) || (menuItem.isToggleType() && (this.mListener.isScrollState() || this.mListener.isOrientationChanged()))) {
                itemView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(C0088R.layout.setting_parent_item_view, parent, false);
                if (itemView == null) {
                    CamLog.m11w(CameraConstants.TAG, "SettingMenuAdapter error. view is null.");
                    return null;
                }
                itemViewHolder = new ItemViewHolder();
                initImteViewHolder(itemView, itemViewHolder);
                itemView.setTag(itemViewHolder);
            } else {
                itemViewHolder = (ItemViewHolder) itemView.getTag();
            }
            this.mGuideTextWidth = parent.getWidth() - this.mItemViewStartMargin;
            this.mIsExistGuideText = !"".equals(menuItem.getGuideText());
            boolean isEnable = menuItem.isEnable();
            itemView.setClickable(!isEnable);
            itemView.setEnabled(isEnable);
            setItemView(context, settingMenu, menuItem, itemViewHolder);
            setItemViewEnable(menuItem, itemViewHolder);
        }
        itemView.setPaddingRelative(this.mItemViewStartMargin, 0, 0, 0);
        return itemView;
    }

    private void initImteViewHolder(View itemView, ItemViewHolder itemViewHolder) {
        itemViewHolder.mSettingTitleWrapper = (LinearLayout) itemView.findViewById(C0088R.id.setting_title_guide_wrapper);
        itemViewHolder.mSettingTitle = (TextView) itemView.findViewById(C0088R.id.settings_title_textview);
        itemViewHolder.mSettingGuideText = (TextView) itemView.findViewById(C0088R.id.settings_guide_textview);
        itemViewHolder.mCurrentSettingValue = (RotateTextView) itemView.findViewById(C0088R.id.current_setting_value_text);
        itemViewHolder.mSettingOnOffSwitch = (Switch) itemView.findViewById(C0088R.id.setting_switch);
        itemViewHolder.mSettingValueLayout = (LinearLayout) itemView.findViewById(C0088R.id.multi_select_item_layout);
        itemViewHolder.mSettingArrow = (ImageView) itemView.findViewById(C0088R.id.current_setting_value_arrow);
        itemViewHolder.mBulletImage = (ImageView) itemView.findViewById(C0088R.id.setting_bullet_imageview);
        itemViewHolder.mDivider = (SettingVerticalDivider) itemView.findViewById(C0088R.id.setting_horizontal_divider);
    }

    private void setItemView(Context context, SettingMenu settingMenu, SettingMenuItem menuItem, ItemViewHolder itemViewHolder) {
        if (settingMenu != null && menuItem != null) {
            itemViewHolder.mSettingTitle.setText(menuItem.getName());
            itemViewHolder.mSettingTitle.setContentDescription(menuItem.getName());
            itemViewHolder.mCurrentSettingValue.setText(settingMenu.getCurChildEntry(menuItem.getKey()));
            itemViewHolder.mCurrentSettingValue.setContentDescription(TalkBackUtil.makePictureDescription(getContext(), settingMenu.getCurChildEntry(menuItem.getKey())));
            initBulletSetting(itemViewHolder, menuItem.isBulletDividerType());
            if (menuItem.isToggleType()) {
                setItemViewForToggleType(context, settingMenu, menuItem, itemViewHolder);
            } else {
                itemViewHolder.mSettingOnOffSwitch.setVisibility(8);
                if ("".equals(settingMenu.getCurChildEntry(menuItem.getKey()))) {
                    itemViewHolder.mSettingValueLayout.setVisibility(8);
                    itemViewHolder.mSettingArrow.setVisibility(8);
                } else {
                    itemViewHolder.mSettingArrow.setVisibility(0);
                    itemViewHolder.mSettingValueLayout.setVisibility(0);
                    Drawable d = context.getResources().getDrawable(C0088R.drawable.camera_settings_arrow);
                    if (d != null) {
                        int settingValueMarginEnd = d.getIntrinsicWidth() + Utils.getPx(context, C0088R.dimen.setting_list_item_arrow_marginEnd);
                        LayoutParams lp = (LayoutParams) itemViewHolder.mSettingValueLayout.getLayoutParams();
                        if (lp != null) {
                            lp.setMarginEnd(settingValueMarginEnd);
                            itemViewHolder.mSettingValueLayout.setLayoutParams(lp);
                            if (this.mIsExistGuideText) {
                                itemViewHolder.mSettingValueLayout.measure(0, 0);
                                this.mGuideTextWidth -= Math.min(itemViewHolder.mSettingValueLayout.getMeasuredWidth(), RatioCalcUtil.getSizeCalculatedByPercentage(context, false, 0.25f)) + settingValueMarginEnd;
                            }
                        } else {
                            return;
                        }
                    }
                    return;
                }
            }
            initGuideTextSetting(context, itemViewHolder, menuItem, this.mIsExistGuideText);
        }
    }

    private void initGuideTextSetting(Context context, ItemViewHolder itemViewHolder, SettingMenuItem menuItem, boolean isGuideTextSetting) {
        if (this.mIsExistGuideText) {
            this.mGuideTextWidth -= Utils.getPx(context, C0088R.dimen.setting_list_item_switch_marginEnd);
            LinearLayout.LayoutParams rlp = (LinearLayout.LayoutParams) itemViewHolder.mSettingGuideText.getLayoutParams();
            rlp.width = this.mGuideTextWidth;
            itemViewHolder.mSettingGuideText.setLayoutParams(rlp);
            itemViewHolder.mSettingGuideText.setText(menuItem.getGuideText());
            itemViewHolder.mSettingGuideText.setVisibility(0);
            if (this.mIsRTLDirection) {
                itemViewHolder.mCurrentSettingValue.setTextGravity(64);
                return;
            }
            return;
        }
        itemViewHolder.mSettingGuideText.setVisibility(8);
    }

    private void initBulletSetting(ItemViewHolder itemViewHolder, boolean isBulletSetting) {
        LayoutParams params;
        if (isBulletSetting) {
            params = (LayoutParams) itemViewHolder.mSettingOnOffSwitch.getLayoutParams();
            params.width = -2;
            itemViewHolder.mSettingOnOffSwitch.setFocusable(false);
            itemViewHolder.mSettingOnOffSwitch.setFocusableInTouchMode(false);
            itemViewHolder.mBulletImage.setVisibility(0);
            itemViewHolder.mDivider.setVisibility(0);
            itemViewHolder.mSettingOnOffSwitch.setLayoutParams(params);
            return;
        }
        params = (LayoutParams) itemViewHolder.mSettingOnOffSwitch.getLayoutParams();
        params.width = -1;
        itemViewHolder.mBulletImage.setVisibility(8);
        itemViewHolder.mDivider.setVisibility(8);
        itemViewHolder.mSettingOnOffSwitch.setImportantForAccessibility(2);
        itemViewHolder.mSettingOnOffSwitch.setLayoutParams(params);
    }

    private void setDescForToggleType(SettingMenuItem menuItem, ItemViewHolder itemViewHolder, boolean isGuideTextExist, boolean isOn) {
        String onOff;
        if (isOn) {
            onOff = getContext().getString(C0088R.string.setting_on);
        } else {
            onOff = getContext().getString(C0088R.string.setting_off);
        }
        if (isGuideTextExist) {
            itemViewHolder.mSettingGuideText.setContentDescription(menuItem.getGuideText() + " " + onOff + " " + getContext().getString(C0088R.string.setting_switch));
            return;
        }
        itemViewHolder.mSettingTitle.setContentDescription(menuItem.getName() + " " + onOff + " " + getContext().getString(C0088R.string.setting_switch));
    }

    private void setItemViewForToggleType(Context context, SettingMenu settingMenu, final SettingMenuItem menuItem, final ItemViewHolder itemViewHolder) {
        if (context != null && settingMenu != null && menuItem != null && itemViewHolder != null) {
            if (!context.getResources().getBoolean(C0088R.bool.config_theme_is_additional)) {
                itemViewHolder.mSettingOnOffSwitch.setTrackTintList(context.getColorStateList(C0088R.color.selector_switch_colorlist));
            }
            itemViewHolder.mSettingOnOffSwitch.setVisibility(0);
            itemViewHolder.mSettingValueLayout.setVisibility(8);
            itemViewHolder.mSettingArrow.setVisibility(8);
            itemViewHolder.mSettingOnOffSwitch.setOnCheckedChangeListener(null);
            itemViewHolder.mSettingOnOffSwitch.setOnTouchListener(setSwitchTouchListener());
            itemViewHolder.mSettingOnOffSwitch.setChecked("on".equals(settingMenu.getCurChildValue(menuItem.getKey())));
            if (!context.getResources().getBoolean(C0088R.bool.config_theme_is_additional)) {
                ColorStateList originalColor = itemViewHolder.mSettingOnOffSwitch.getTrackTintList();
                int[][] state = originalColor.getStates();
                int[] colors = originalColor.getColors();
                colors[0] = context.getColor(C0088R.color.switch_color);
                itemViewHolder.mSettingOnOffSwitch.setTrackTintList(new ColorStateList(state, colors));
            }
            itemViewHolder.mSettingOnOffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton button, boolean isChecking) {
                    String description;
                    CamLog.m3d(CameraConstants.TAG, "isChecking : " + isChecking);
                    if (SettingParentAdapter.this.mListener != null) {
                        SettingParentAdapter.this.mListener.onSwitchButtonClicked(menuItem.getKey(), isChecking, button, menuItem.isBulletDividerType());
                    }
                    if (isChecking) {
                        description = SettingParentAdapter.this.getContext().getString(C0088R.string.setting_on);
                    } else {
                        description = SettingParentAdapter.this.getContext().getString(C0088R.string.setting_off);
                    }
                    TalkBackUtil.setTalkbackDescOnDoubleTap(SettingParentAdapter.this.getContext(), description);
                    SettingParentAdapter.this.setDescForToggleType(menuItem, itemViewHolder, !"".equals(menuItem.getGuideText()), isChecking);
                }
            });
            if (this.mIsExistGuideText) {
                itemViewHolder.mSettingOnOffSwitch.measure(0, 0);
                this.mGuideTextWidth -= itemViewHolder.mSettingOnOffSwitch.getMeasuredWidth() + Utils.getPx(context, C0088R.dimen.setting_list_item_switch_marginEnd);
            }
            if (menuItem.isBulletDividerType()) {
                this.mGuideTextWidth -= Utils.getPx(getContext(), C0088R.dimen.setting_vertical_divider.margin) * 2;
            }
            itemViewHolder.mSettingOnOffSwitch.setEnabled(menuItem.isEnable());
            setDescForToggleType(menuItem, itemViewHolder, this.mIsExistGuideText, getContext().getString(C0088R.string.on).equals(itemViewHolder.mCurrentSettingValue.getText()));
        }
    }

    private OnTouchListener setSwitchTouchListener() {
        return new C14202();
    }

    private void setItemViewEnable(SettingMenuItem menuItem, ItemViewHolder itemViewHolder) {
        if (this.mContext != null) {
            Context context = (Context) this.mContext.get();
            if (context != null) {
                itemViewHolder.mSettingTitle.setTextColor(context.getColor(C0088R.color.camera_white_txt));
                itemViewHolder.mSettingGuideText.setTextColor(context.getColor(C0088R.color.secondary_text_default_material_light));
                if (menuItem.isEnable()) {
                    itemViewHolder.mSettingTitle.setAlpha(1.0f);
                    itemViewHolder.mSettingGuideText.setAlpha(1.0f);
                    itemViewHolder.mCurrentSettingValue.setColorFilter(ColorUtil.getNormalColorByAlpha());
                    itemViewHolder.mSettingArrow.setColorFilter(ColorUtil.getNormalColorByAlpha());
                } else {
                    itemViewHolder.mSettingTitle.setAlpha(0.35f);
                    itemViewHolder.mSettingGuideText.setAlpha(0.35f);
                    itemViewHolder.mCurrentSettingValue.setColorFilter(ColorUtil.getDimColorByAlpha());
                    itemViewHolder.mSettingArrow.setColorFilter(ColorUtil.getDimColorByAlpha());
                }
                if (menuItem.isBulletDividerType()) {
                    itemViewHolder.mBulletImage.setEnabled(menuItem.isEnable());
                    itemViewHolder.mDivider.setEnabled(menuItem.isEnable());
                }
            }
        }
    }

    private int getStartMargin(Context context) {
        if (this.mDegree == 0 || this.mDegree == 180) {
            return Utils.getPx(context, C0088R.dimen.setting_list_item_marginStart_portrait);
        }
        return Utils.getPx(context, C0088R.dimen.setting_list_item_marginStart);
    }

    public int getCount() {
        return super.getCount();
    }

    public SettingMenuItem getItem(int position) {
        return (SettingMenuItem) super.getItem(position);
    }

    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public boolean isEnabled(int position) {
        SettingMenuItem ci = getItem(position);
        if (ci != null && ci.getSettingIndex() == -100) {
            return false;
        }
        return true;
    }

    public void setAvailableUpdate(boolean availableUpdate) {
        this.mIsAvailableUpdate = availableUpdate;
    }

    public void update(Observable observable, Object obj) {
        if (this.mIsAvailableUpdate) {
            update();
        }
    }

    public void update() {
        SettingMenu settingMenu = (SettingMenu) this.mMenus.get();
        if (settingMenu != null && settingMenu.mGet != null) {
            settingMenu.mGet.getHandler().removeCallbacks(this.mNotifyDataSetChangeRunnable);
            settingMenu.mGet.getHandler().post(this.mNotifyDataSetChangeRunnable);
        }
    }

    public void setListItemDegree(int degree, boolean useAni) {
        this.mDegree = degree;
    }

    public void setListItemDegree(int degree) {
        this.mDegree = degree;
    }

    public void setSelectedIndex(int index) {
        this.mSelectedIndex = index;
    }

    public int getSelectedIndex() {
        return this.mSelectedIndex;
    }
}
