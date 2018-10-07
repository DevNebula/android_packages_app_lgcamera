package com.lge.camera.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import com.lge.R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.TalkBackUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class SettingChildAdapter extends ArrayAdapter<SettingMenuItem> implements Observer {
    protected final WeakReference<Context> mContext;
    protected String mCurParentKey = "";
    protected final WeakReference<SettingMenu> mMenus;

    /* renamed from: com.lge.camera.settings.SettingChildAdapter$1 */
    class C13951 implements Runnable {
        C13951() {
        }

        public void run() {
            SettingChildAdapter.this.notifyDataSetChanged();
        }
    }

    public class ItemViewHolder {
        CheckedTextView mItemTitle;
    }

    public SettingChildAdapter(Context context, int resId, SettingMenu menus, ArrayList<SettingMenuItem> menuItem) {
        super(context, resId, menuItem);
        this.mContext = new WeakReference(context);
        this.mMenus = new WeakReference(menus);
        menus.addObserver(this);
    }

    public void close() {
        SettingMenu settingMenu = (SettingMenu) this.mMenus.get();
        if (settingMenu != null) {
            settingMenu.deleteObserver(this);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewHolder holder;
        Context context = (Context) this.mContext.get();
        View itemView = convertView;
        if (itemView == null) {
            itemView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.dialog_c_6, null);
            if (itemView == null) {
                CamLog.m11w(CameraConstants.TAG, "SettingExpandChildMenuAdapter error. view is null.");
                return null;
            }
            holder = new ItemViewHolder();
            holder.mItemTitle = (CheckedTextView) itemView.findViewById(16908308);
            itemView.setTag(holder);
        } else {
            holder = (ItemViewHolder) itemView.getTag();
        }
        SettingMenuItem menuItem = getItem(position);
        SettingMenu settingMenu = (SettingMenu) this.mMenus.get();
        if (!(menuItem == null || settingMenu == null || itemView == null || context == null)) {
            holder.mItemTitle.setText(menuItem.getName());
            holder.mItemTitle.setContentDescription(TalkBackUtil.makePictureDescription(getContext(), menuItem.getName()));
            holder.mItemTitle.setTextColor(ColorUtil.getTextColorPrimaryFromTheme(context));
            holder.mItemTitle.setAlpha(menuItem.isEnable() ? 1.0f : 0.35f);
            if (menuItem.getName().equals(settingMenu.getCurChildEntry(menuItem.getKey()))) {
                holder.mItemTitle.setChecked(true);
            }
            itemView.setClickable(!menuItem.isEnable());
        }
        return itemView;
    }

    public int getCount() {
        return super.getCount();
    }

    public SettingMenuItem getItem(int position) {
        if (getCount() <= 0) {
            return null;
        }
        return (SettingMenuItem) super.getItem(position);
    }

    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void update(Observable observable, Object obj) {
        update();
    }

    public void update() {
        SettingMenu settingMenu = (SettingMenu) this.mMenus.get();
        if (settingMenu != null && settingMenu.mGet != null) {
            settingMenu.mGet.getHandler().post(new C13951());
        }
    }

    public void setCurParentKey(String key) {
        this.mCurParentKey = key;
    }

    public String getCurParentKey() {
        return this.mCurParentKey;
    }
}
