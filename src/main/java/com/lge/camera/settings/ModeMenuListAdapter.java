package com.lge.camera.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SecureImageUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ModeMenuListAdapter extends BaseAdapter {
    private final float TEXT_PADDING_TOP = 0.51f;
    private final float TEXT_SIZE = 0.027f;
    private final float TEXT_WIDTH = 0.93f;
    private WeakReference<Context> mContext;
    private int mDegree = 0;
    private boolean mIsEditMode = false;
    private WeakReference<ArrayList<ModeItem>> mMenus;

    public class ModeMenuViewHolder {
        View mDeleteButton;
        RotateImageButton mRotateImageButton;
    }

    public ModeMenuListAdapter(Context context, ArrayList<ModeItem> menus) {
        this.mContext = new WeakReference(context);
        this.mMenus = new WeakReference(menus);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.mContext == null) {
            return convertView;
        }
        Context context = (Context) this.mContext.get();
        if (context == null) {
            return convertView;
        }
        ModeMenuViewHolder holder;
        View modeItemView = convertView;
        if (modeItemView == null) {
            modeItemView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(C0088R.layout.mode_menu_list_item_view, null);
            if (modeItemView == null) {
                return null;
            }
            holder = new ModeMenuViewHolder();
            holder.mRotateImageButton = (RotateImageButton) modeItemView.findViewById(C0088R.id.mode_menu_item_icon);
            holder.mDeleteButton = modeItemView.findViewById(C0088R.id.mode_delete_layout);
            modeItemView.setTag(holder);
            int[] size = getViewSize();
            LayoutParams params = (LayoutParams) modeItemView.getLayoutParams();
            if (params != null) {
                params.width = size[0];
                params.height = size[1];
            } else {
                params = new LayoutParams(size[0], size[1]);
            }
            modeItemView.setLayoutParams(params);
        } else {
            holder = (ModeMenuViewHolder) modeItemView.getTag();
        }
        RotateImageButton imageButton = holder.mRotateImageButton;
        if (imageButton != null) {
            setImage(context, imageButton, position);
        }
        setDeleteImage(holder, position);
        return modeItemView;
    }

    protected int[] getViewSize() {
        Context context = (Context) this.mContext.get();
        if (context == null) {
            return new int[]{0, 0};
        }
        int width = RatioCalcUtil.getSizeCalculatedByPercentage(context, false, 0.1654f);
        return new int[]{width, width};
    }

    protected void setImage(Context context, RotateImageButton imageButton, int position) {
        ModeItem item = getItem(position);
        if (item == null) {
            imageButton.setText(null);
            imageButton.setImageDrawable(null);
            return;
        }
        boolean z;
        if (item.getImageResourceId() != 0) {
            imageButton.setText(null);
            imageButton.setImageResource(item.getImageResourceId());
        }
        boolean isSelected = item.isSelected();
        int textWidth = (int) (((float) getViewSize()[0]) * 0.93f);
        int paddingTop = (int) (((float) getViewSize()[1]) * 0.51f);
        imageButton.setTextSize(RatioCalcUtil.getSizeCalculatedByPercentage(context, false, 0.027f));
        imageButton.setTextPaddingTop(paddingTop);
        imageButton.setTextWidth(textWidth);
        imageButton.setText(item.getTitle());
        imageButton.setTextColor(context.getColor(isSelected ? C0088R.color.camera_pressed_txt : C0088R.color.camera_white_txt));
        imageButton.setImageLevel(getImageLevel(item.getValue(), isSelected));
        imageButton.setAlpha(1.0f);
        imageButton.setImageAlpha(isEnabled(position) ? 255 : 89);
        if (isSelected) {
            z = true;
        } else {
            z = false;
        }
        imageButton.setSelected(z);
        imageButton.setEnabled(isEnabled(position));
        imageButton.setContentDescription(item.getTitle());
    }

    public void setEditMode(boolean isEditMode) {
        this.mIsEditMode = isEditMode;
        notifyDataSetChanged();
    }

    private void setDeleteImage(ModeMenuViewHolder holder, int position) {
        int i = 0;
        View deleteButton = holder.mDeleteButton;
        if (deleteButton != null) {
            ModeItem item = getItem(position);
            if (item == null) {
                deleteButton.setVisibility(8);
                return;
            }
            boolean deletable;
            if (item.isDeletable() && this.mIsEditMode) {
                deletable = true;
            } else {
                deletable = false;
            }
            if (!deletable) {
                i = 8;
            }
            deleteButton.setVisibility(i);
        }
    }

    public int getImageLevel(String mode, boolean selected) {
        int i = 1;
        if (mode != null) {
            boolean isPort = this.mDegree == 0 || this.mDegree == 180;
            if (mode.contains(CameraConstants.MODE_SQUARE) && isPort) {
                return selected ? 3 : 2;
            }
        }
        if (!selected) {
            i = 0;
        }
        return i;
    }

    public boolean isEnabled(int position) {
        ModeItem item = getItem(position);
        if (item == null) {
            return false;
        }
        String mode = item.getValue();
        if (mode == null) {
            return false;
        }
        if (CameraConstants.MODE_SQUARE_OVERLAP.equals(mode) && SecureImageUtil.useSecureLockImage()) {
            return false;
        }
        return super.isEnabled(position);
    }

    public int getListItemDegree() {
        return this.mDegree;
    }

    public void setListItemDegree(int degree) {
        CamLog.m3d(CameraConstants.TAG, "[mode] degree : " + degree);
        this.mDegree = degree;
    }

    public int getCount() {
        if (this.mMenus == null) {
            return 0;
        }
        ArrayList<ModeItem> itemList = (ArrayList) this.mMenus.get();
        if (itemList != null) {
            return itemList.size();
        }
        return 0;
    }

    public ModeItem getItem(int position) {
        if (this.mMenus == null) {
            return null;
        }
        ArrayList<ModeItem> itemList = (ArrayList) this.mMenus.get();
        if (itemList == null || itemList.size() <= 0) {
            return null;
        }
        return (ModeItem) itemList.get(position);
    }

    public int getItemIndex(String modeName) {
        ArrayList<ModeItem> list = (ArrayList) this.mMenus.get();
        if (list == null) {
            return -1;
        }
        for (int i = 0; i < list.size(); i++) {
            ModeItem modeItem = (ModeItem) list.get(i);
            if (modeItem != null && modeItem.getValue().equals(modeName)) {
                return i;
            }
        }
        return -1;
    }

    public long getItemId(int position) {
        return (long) position;
    }
}
