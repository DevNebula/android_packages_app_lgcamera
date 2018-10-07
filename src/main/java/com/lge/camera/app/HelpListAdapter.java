package com.lge.camera.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HelpItemGroup.HelpItem;
import com.lge.camera.util.Utils;
import com.lge.media.CamcorderProfileEx;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HelpListAdapter extends BaseAdapter {
    private WeakReference<Context> mContext;
    private int mDegree = 0;
    private WeakReference<ArrayList<HelpItem>> mHelpItems;

    public class HelpListItemHolder {
        LinearLayout mHelpDescWrapper;
        ImageView mHelpItemImage;
        TextView mHelpItemTitle;
        boolean mIsPort;
    }

    public HelpListAdapter(Context context, ArrayList<HelpItem> items) {
        this.mContext = new WeakReference(context);
        this.mHelpItems = new WeakReference(items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.mContext == null) {
            return convertView;
        }
        Context context = (Context) this.mContext.get();
        if (context == null) {
            return convertView;
        }
        HelpListItemHolder holder;
        View helpItemView = convertView;
        boolean isPort = this.mDegree == 0 || this.mDegree == 180;
        if (helpItemView == null || ((HelpListItemHolder) helpItemView.getTag()).mIsPort != isPort) {
            helpItemView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(isPort ? C0088R.layout.help_list_item_view : C0088R.layout.help_list_item_view_land, null);
            if (helpItemView == null) {
                CamLog.m11w(CameraConstants.TAG, "HelpListAdapter error. view is null.");
                return null;
            }
            holder = new HelpListItemHolder();
            holder.mHelpItemTitle = (TextView) helpItemView.findViewById(C0088R.id.help_item_title);
            holder.mHelpItemImage = (ImageView) helpItemView.findViewById(C0088R.id.help_item_image);
            holder.mHelpDescWrapper = (LinearLayout) helpItemView.findViewById(C0088R.id.help_desc_wrapper);
            holder.mIsPort = isPort;
            helpItemView.setTag(holder);
        } else {
            holder = (HelpListItemHolder) helpItemView.getTag();
        }
        setResourceOnHolder(position, holder);
        return helpItemView;
    }

    private void setResourceOnHolder(int position, HelpListItemHolder holder) {
        HelpItem item = getItem(position);
        if (holder != null && holder.mHelpItemTitle != null && item != null) {
            if (item.mHelpTitleId != -1) {
                holder.mHelpItemTitle.setText(item.mHelpTitleId);
                holder.mHelpItemTitle.setTextAppearance((Context) this.mContext.get(), C0088R.style.help_title);
            }
            if (item.mHelpImageId != 0) {
                holder.mHelpItemImage.setImageResource(item.mHelpImageId);
            }
            if (!item.mHelpDescription.isEmpty()) {
                setHelpDesc(holder, item);
            }
        }
    }

    private void setHelpDesc(HelpListItemHolder holder, HelpItem item) {
        if (item != null && holder != null && holder.mHelpDescWrapper != null && item.mHelpDescription != null) {
            holder.mHelpDescWrapper.removeAllViews();
            String desc = "";
            for (Integer descId : item.mHelpDescription) {
                String string = ((Context) this.mContext.get()).getString(descId.intValue());
                if (CameraConstants.MODE_SQUARE_OVERLAP.equals(item.key)) {
                    string = String.format(string, new Object[]{Integer.valueOf(12)});
                }
                if (descId.intValue() == C0088R.string.help_slow_motion_desc1 && CamcorderProfileEx.hasProfile(2, 10017)) {
                    string = string.replace("120", "240");
                }
                desc = desc + string;
            }
            Utils.addTabToNumberedDescription(holder.mHelpDescWrapper, desc, (Context) this.mContext.get(), false, C0088R.style.help_desc);
            holder.mHelpDescWrapper.setContentDescription(((Context) this.mContext.get()).getString(item.mHelpTitleId) + "\n\n" + desc);
        }
    }

    public void setHelpListDegree(int degree) {
        this.mDegree = degree;
    }

    public int getCount() {
        if (this.mHelpItems == null) {
            return 0;
        }
        ArrayList<HelpItem> itemList = (ArrayList) this.mHelpItems.get();
        if (itemList != null) {
            return itemList.size();
        }
        return 0;
    }

    public HelpItem getItem(int position) {
        if (this.mHelpItems == null) {
            return null;
        }
        ArrayList<HelpItem> itemList = (ArrayList) this.mHelpItems.get();
        if (itemList == null || itemList.size() <= 0) {
            return null;
        }
        return (HelpItem) itemList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public void unbind() {
        if (this.mHelpItems != null) {
            this.mHelpItems.clear();
            this.mHelpItems = null;
        }
        this.mContext = null;
    }
}
