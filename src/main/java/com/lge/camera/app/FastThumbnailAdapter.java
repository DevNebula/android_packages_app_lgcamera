package com.lge.camera.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.lge.camera.C0088R;
import com.lge.camera.components.FastThumbnailListView;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ThumbnailListItem;
import com.lge.camera.managers.TilePreviewInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;

public class FastThumbnailAdapter extends BaseAdapter {
    public static final int MAX_ITEM_SIZE = 11;
    public static final int VISIBLE_ITEM_SIZE = 5;
    private int listViewItemWidth;
    private Context mContext = null;
    private int mDegree = 0;
    private TilePreviewInterface mGet;
    private LayoutInflater mInflater;
    private boolean mIsRTL = false;
    private ArrayList<ThumbnailListItem> mItemList;
    private final ThumbnailHelper mThumbnailHelper;

    class ItemHolder {
        RotateImageView mIconThumb;
        RotateImageView mIconThumbMicro;
        RotateImageView mModeColumn;
        RotateImageButton mPlayIcon;
        ImageView mSelected;

        ItemHolder() {
        }
    }

    public FastThumbnailAdapter(Context context, TilePreviewInterface mget, ThumbnailHelper thumbnailHelper) {
        this.mContext = context;
        this.mItemList = new ArrayList();
        this.mInflater = LayoutInflater.from(context);
        this.mGet = mget;
        this.mThumbnailHelper = thumbnailHelper;
        this.listViewItemWidth = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.11112f);
        this.mIsRTL = Utils.isRTLLanguage();
        CamLog.m3d(CameraConstants.TAG, "[Tile] is RTL? : " + this.mIsRTL);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int color;
        if (convertView == null) {
            convertView = this.mInflater.inflate(C0088R.layout.thumbnail_list_item, parent, false);
        }
        LayoutParams lp = (LayoutParams) convertView.getLayoutParams();
        lp.width = this.listViewItemWidth;
        lp.height = this.listViewItemWidth;
        convertView.setLayoutParams(lp);
        ItemHolder holder = (ItemHolder) convertView.getTag();
        if (holder == null) {
            holder = setHolder(convertView);
            convertView.setTag(holder);
        }
        if (position % 2 == 0) {
            color = this.mContext.getColor(C0088R.color.tile_preview_bg_1);
        } else {
            color = this.mContext.getColor(C0088R.color.tile_preview_bg_2);
        }
        convertView.setBackgroundColor(color);
        ThumbnailListItem item = getItem(position);
        if (this.mGet == null || item == null || item.mUri == null) {
            holder.mIconThumbMicro.setVisibility(8);
            holder.mIconThumb.setVisibility(8);
        } else {
            holder.mIconThumbMicro.setVisibility(0);
            holder.mIconThumb.setVisibility(0);
            this.mThumbnailHelper.stopLoading(holder.mIconThumbMicro);
            this.mThumbnailHelper.loadThumbnail(item, holder.mIconThumbMicro, 50);
            this.mThumbnailHelper.stopLoading(holder.mIconThumb);
            this.mThumbnailHelper.loadThumbnail(item, holder.mIconThumb, 40);
            if (item.mIsBurstShot) {
                holder.mModeColumn.setImageDrawable(Utils.getTypeDrawable(this.mContext, 30));
                holder.mModeColumn.setVisibility(0);
            } else if (item.mModeColumn != 0) {
                holder.mModeColumn.setImageDrawable(Utils.getTypeDrawable(this.mContext, item.mModeColumn));
                holder.mModeColumn.setVisibility(0);
            } else {
                holder.mModeColumn.setVisibility(8);
            }
            if (item.mIsImage) {
                convertView.setContentDescription(String.format(this.mContext.getString(C0088R.string.talkback_cell_image_selected), new Object[]{Integer.valueOf(position + 1)}));
            } else {
                convertView.setContentDescription(String.format(this.mContext.getString(C0088R.string.talkback_cell_video_selected), new Object[]{Integer.valueOf(position + 1)}));
            }
            RotateImageButton rotateImageButton = holder.mPlayIcon;
            if (item.mMediaType.contains("video")) {
                color = 0;
            } else {
                color = 8;
            }
            rotateImageButton.setVisibility(color);
            holder.mIconThumbMicro.setDegree(this.mDegree, false);
            holder.mIconThumb.setDegree(this.mDegree, false);
            holder.mPlayIcon.setDegree(this.mDegree, false);
            holder.mModeColumn.setDegree(this.mDegree, false);
        }
        return convertView;
    }

    private ItemHolder setHolder(View convertView) {
        ItemHolder holder = new ItemHolder();
        holder.mIconThumb = (RotateImageView) convertView.findViewById(C0088R.id.thumbnail_list_item_image);
        holder.mIconThumbMicro = (RotateImageView) convertView.findViewById(C0088R.id.thumbnail_list_item_image_micro);
        holder.mSelected = (ImageView) convertView.findViewById(C0088R.id.thumbnail_list_item_selected);
        if (this.mIsRTL) {
            holder.mModeColumn = (RotateImageView) convertView.findViewById(C0088R.id.thumbnail_list_mode_rtl);
        } else {
            holder.mModeColumn = (RotateImageView) convertView.findViewById(C0088R.id.thumbnail_list_mode);
        }
        holder.mPlayIcon = (RotateImageButton) convertView.findViewById(C0088R.id.thumbnail_list_item_play_icon);
        return holder;
    }

    public int getCount() {
        if (this.mItemList != null) {
            return this.mItemList.size();
        }
        return 0;
    }

    public ThumbnailListItem getItem(int position) {
        if (this.mItemList == null || this.mItemList.size() <= position) {
            return null;
        }
        return (ThumbnailListItem) this.mItemList.get(position);
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public void setItemList(ArrayList<ThumbnailListItem> itemList) {
        this.mItemList = itemList;
        notifyDataSetChanged();
    }

    public void resetItemList() {
        if (this.mItemList != null) {
            this.mItemList.clear();
        }
        notifyDataSetChanged();
    }

    public void addItem(ThumbnailListItem item) {
        this.mItemList.add(0, item);
        if (this.mItemList.size() > 11) {
            this.mItemList.remove(11);
        }
        notifyDataSetChanged();
    }

    public void addItem(ThumbnailListItem item, int pos) {
        this.mItemList.add(pos, item);
        if (this.mItemList.size() > 11) {
            this.mItemList.remove(11);
        }
        notifyDataSetChanged();
    }

    public void removeItem(long id) {
        if (this.mItemList != null && this.mItemList.size() != 0 && id != 0) {
            int idx = 0;
            Iterator it = this.mItemList.iterator();
            while (it.hasNext()) {
                if (id == ((ThumbnailListItem) it.next()).f33id) {
                    this.mItemList.remove(idx);
                    break;
                }
                idx++;
            }
            notifyDataSetChanged();
        }
    }

    public void setDegree(int degree) {
        if (this.mIsRTL) {
            this.mDegree = (degree + 90) % 360;
        } else {
            this.mDegree = (degree + 270) % 360;
        }
        if (!this.mGet.getBurstProgress()) {
            notifyDataSetChanged();
        }
    }

    public void rotateFastThumbnailListView(int degree, FastThumbnailListView thumbnailList) {
        if (this.mIsRTL) {
            this.mDegree = (degree + 90) % 360;
        } else {
            this.mDegree = (degree + 270) % 360;
        }
        int i = 0;
        while (i < 6) {
            View view = thumbnailList.getChildAt(i);
            if (view != null) {
                RotateImageView modeColumn;
                RotateImageView iconThumbMicro = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_item_image_micro);
                RotateImageView iconThumb = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_item_image);
                RotateImageButton playIcon = (RotateImageButton) view.findViewById(C0088R.id.thumbnail_list_item_play_icon);
                if (this.mIsRTL) {
                    modeColumn = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_mode_rtl);
                } else {
                    modeColumn = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_mode);
                }
                if (iconThumb != null && iconThumbMicro != null && modeColumn != null && playIcon != null) {
                    iconThumbMicro.setDegree(this.mDegree, true);
                    iconThumb.setDegree(this.mDegree, true);
                    playIcon.setDegree(this.mDegree, true);
                    modeColumn.setDegree(this.mDegree, true);
                    i++;
                } else {
                    return;
                }
            }
            return;
        }
    }
}
