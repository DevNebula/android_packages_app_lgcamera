package com.lge.camera.managers;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import java.util.ArrayList;
import java.util.List;

public class ColorEffectManager extends ManagerInterfaceImpl implements OnRemoveHandler {
    protected ColorEffectAdapter mColorAdapter = null;
    protected ColorEffectInterface mColorInterface = null;
    protected OnItemClickListener mColorItemClickListener = null;
    protected ArrayList<ColorItem> mColorList = null;
    protected int mDegree = 0;
    protected AnimationListener mHideMenuAniListener = null;
    protected HandlerRunnable mHideMenuRunnable = null;
    protected View mMenuView = null;
    protected int mSelectedPosition = -1;
    protected AnimationListener mShowMenuAniListener = null;

    public interface ColorEffectInterface {
        List<String> getSupportedColorEffects();

        void setBeautyButtonSelected(int i, boolean z);

        boolean setColorEffect(String str);

        void setMenuType(int i);

        void updateColorEffectQuickButton();
    }

    /* renamed from: com.lge.camera.managers.ColorEffectManager$2 */
    class C08742 implements AnimationListener {
        C08742() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (ColorEffectManager.this.mGet != null && !ColorEffectManager.this.mGet.isPaused() && ColorEffectManager.this.mMenuView != null) {
                ColorEffectManager.this.mMenuView.setVisibility(0);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ColorEffectManager$3 */
    class C08753 implements AnimationListener {
        C08753() {
        }

        public void onAnimationStart(Animation animation) {
            if (ColorEffectManager.this.mGet != null) {
                ColorEffectManager.this.mGet.setQuickButtonIndex(C0088R.id.quick_button_color_effect, 0);
            }
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (ColorEffectManager.this.mGet != null) {
                ColorEffectManager.this.mGet.postOnUiThread(ColorEffectManager.this.mHideMenuRunnable, 0);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ColorEffectManager$4 */
    class C08764 implements OnItemClickListener {
        C08764() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            ColorEffectManager.this.onColorItemClick(position);
        }
    }

    public ColorEffectManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setInterface(ColorEffectInterface colorInterface) {
        this.mColorInterface = colorInterface;
    }

    public void init() {
        super.init();
        this.mDegree = getOrientationDegree();
        makeItemList(this.mColorInterface.getSupportedColorEffects());
        if (this.mHideMenuRunnable == null) {
            this.mHideMenuRunnable = new HandlerRunnable(this) {
                public void handleRun() {
                    ColorEffectManager.this.hide();
                }
            };
        }
        makeAnimationListener();
        makeItemClickListener();
    }

    private void makeAnimationListener() {
        if (this.mShowMenuAniListener == null) {
            this.mShowMenuAniListener = new C08742();
        }
        if (this.mHideMenuAniListener == null) {
            this.mHideMenuAniListener = new C08753();
        }
    }

    private void makeItemClickListener() {
        if (this.mColorItemClickListener == null) {
            this.mColorItemClickListener = new C08764();
        }
    }

    protected void onColorItemClick(int position) {
        CamLog.m3d(CameraConstants.TAG, "[color] item click : " + position);
        if (this.mGet.isModuleChanging() || this.mGet.isPaused() || !this.mGet.checkModuleValidate(207) || this.mColorList == null || this.mColorAdapter == null) {
            CamLog.m3d(CameraConstants.TAG, "[color] menu click - return.");
            return;
        }
        ColorItem curItem = (ColorItem) this.mColorList.get(position);
        if (curItem != null) {
            ColorItem prevItem = (ColorItem) this.mColorList.get(this.mSelectedPosition);
            if (prevItem != null) {
                prevItem.setSelected(false);
            }
            curItem.setSelected(true);
            this.mSelectedPosition = position;
            if (this.mColorInterface != null) {
                this.mColorInterface.setColorEffect(curItem.getValue());
            }
            if (this.mColorAdapter != null) {
                this.mColorAdapter.notifyDataSetChanged();
            }
        }
    }

    private void makeItemList(List<String> list) {
        CamLog.m3d(CameraConstants.TAG, "[color] device supported list : " + list.toString());
        if (this.mColorList == null) {
            this.mColorList = new ArrayList();
        }
        String curSetting = this.mGet.getSettingValue(Setting.KEY_COLOR_EFFECT);
        CamLog.m3d(CameraConstants.TAG, "[color] cur setting : " + curSetting);
        ListPreference listPref = (ListPreference) this.mGet.getListPreference(Setting.KEY_COLOR_EFFECT);
        if (listPref != null && this.mColorList != null && list != null) {
            this.mColorList.clear();
            Context context = this.mGet.getAppContext();
            CharSequence[] entryValues = listPref.getEntryValues();
            int size = entryValues.length;
            CharSequence[] entries = new CharSequence[size];
            int[] menuIconResources = new int[size];
            int index = -1;
            for (int i = 0; i < entryValues.length; i++) {
                String value = entryValues[i].toString();
                boolean isSelected = false;
                if (list.contains(value)) {
                    index++;
                    if (curSetting.equals(value)) {
                        this.mSelectedPosition = index;
                        CamLog.m3d(CameraConstants.TAG, "[color] selected index : " + this.mSelectedPosition);
                        isSelected = true;
                    }
                    ColorItem item = new ColorItem(context, value, isSelected);
                    if (item != null) {
                        this.mColorList.add(index, item);
                        entries[i] = item.getTitle();
                        menuIconResources[i] = item.getDrawableId();
                    }
                }
            }
            listPref.setEntries(entries);
            listPref.setMenuIconResources(menuIconResources);
        }
    }

    public void onCameraSwitchingStart() {
        super.onCameraSwitchingStart();
        hide();
    }

    public void showMenu(boolean showAni) {
        show();
        if (showAni) {
            AnimationUtil.startShowingAnimation(this.mMenuView, true, 300, this.mShowMenuAniListener);
        }
    }

    protected void show() {
    }

    protected void hide() {
    }

    public void hideMenu(boolean showAni) {
        if (showAni) {
            AnimationUtil.startShowingAnimation(this.mMenuView, false, 150, this.mHideMenuAniListener);
        } else {
            hide();
        }
    }

    public boolean isMenuVisible() {
        if (this.mMenuView != null) {
            return this.mMenuView.isShown();
        }
        return false;
    }

    public void setRotateDegree(int degree, boolean animation) {
        this.mDegree = degree;
        super.setRotateDegree(degree, animation);
    }

    public void onPauseBefore() {
        hideMenu(false);
        super.onPauseBefore();
    }

    public void onDestroy() {
        if (this.mMenuView != null) {
            this.mMenuView.setVisibility(8);
            this.mMenuView = null;
        }
        this.mColorAdapter = null;
        if (this.mColorList != null) {
            this.mColorList.clear();
            this.mColorList = null;
        }
        this.mHideMenuRunnable = null;
        this.mShowMenuAniListener = null;
        this.mHideMenuAniListener = null;
        this.mColorItemClickListener = null;
        super.onDestroy();
    }

    public void setButtonEnabled(boolean selected) {
    }
}
