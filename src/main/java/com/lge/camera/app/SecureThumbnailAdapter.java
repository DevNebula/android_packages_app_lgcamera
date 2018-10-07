package com.lge.camera.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.components.ThumbnailListView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ThumbnailListItem;
import com.lge.camera.managers.ThumbnailListPagerAdapter;
import com.lge.camera.managers.TilePreviewInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;

public class SecureThumbnailAdapter extends BaseAdapter {
    public static final int MAX_ITEM_SIZE = 200;
    private int listViewItemWidth;
    private Context mContext;
    int mDegree = 0;
    TilePreviewInterface mGet;
    RotateImageView mIconThumb;
    RotateImageView mIconThumbMicro;
    private LayoutInflater mInflater;
    private boolean mIsRTL = false;
    private ArrayList<ThumbnailListItem> mItemList;
    RotateImageView mModeColumn;
    ThumbnailListPagerAdapter mPagerAdapter;
    RotateImageButton mPlayIcon;
    private int mSel = -1;
    ImageView mSelected;
    private final ThumbnailHelper mThumbnailHelper;
    private int prePos = 0;

    class ItemHolder {
        RotateImageView mIconThumb;
        RotateImageView mIconThumbMicro;
        RotateImageView mModeColumn;
        RotateImageButton mPlayIcon;
        ImageView mSelected;

        ItemHolder() {
        }
    }

    public SecureThumbnailAdapter(Context context, ThumbnailHelper mThumbnailHelper, TilePreviewInterface mget) {
        this.mContext = context;
        this.mThumbnailHelper = mThumbnailHelper;
        this.mInflater = LayoutInflater.from(context);
        this.mGet = mget;
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
            holder = setDefaultHolder(convertView);
            convertView.setTag(holder);
        }
        if (position % 2 == 0) {
            color = this.mContext.getColor(C0088R.color.tile_preview_bg_1);
        } else {
            color = this.mContext.getColor(C0088R.color.tile_preview_bg_2);
        }
        convertView.setBackgroundColor(color);
        ThumbnailListItem item = getItem(position);
        int count = getCount();
        boolean isGoingHome = position < this.prePos;
        if ((this.mGet == null || this.mGet.getBurstProgress()) && position == 0) {
            holder.mIconThumbMicro.setVisibility(8);
            holder.mIconThumb.setVisibility(8);
        } else {
            holder.mIconThumbMicro.setVisibility(0);
            holder.mIconThumb.setVisibility(0);
            if (this.mThumbnailHelper != null) {
                this.mThumbnailHelper.stopLoading(holder.mIconThumbMicro);
                this.mThumbnailHelper.loadThumbnail(item, holder.mIconThumbMicro, 50);
                this.mThumbnailHelper.stopLoading(holder.mIconThumb);
                this.mThumbnailHelper.loadThumbnail(item, holder.mIconThumb, 40);
                int nextPos;
                if (isGoingHome) {
                    nextPos = position - 5;
                    if (nextPos >= 0) {
                        this.mThumbnailHelper.loadThumbnail(getItem(nextPos), null, 30);
                    }
                } else {
                    nextPos = position + 5;
                    if (nextPos < count) {
                        this.mThumbnailHelper.loadThumbnail(getItem(nextPos), null, 30);
                    }
                }
            }
            this.prePos = position;
            setHolder(holder, item, position);
        }
        return convertView;
    }

    private void setHolder(ItemHolder holder, ThumbnailListItem item, int position) {
        int i = 8;
        if (holder != null && item != null) {
            ImageView imageView = holder.mSelected;
            int i2 = (position != this.mSel || this.mSel == -1) ? 8 : 0;
            imageView.setVisibility(i2);
            if (item.mIsBurstShot) {
                holder.mModeColumn.setImageDrawable(Utils.getTypeDrawable(this.mContext, 30));
                holder.mModeColumn.setVisibility(0);
            } else if (item.mModeColumn != 0) {
                holder.mModeColumn.setImageDrawable(Utils.getTypeDrawable(this.mContext, item.mModeColumn));
                holder.mModeColumn.setVisibility(0);
            } else {
                holder.mModeColumn.setVisibility(8);
            }
            RotateImageButton rotateImageButton = holder.mPlayIcon;
            if (item.mMediaType.contains("video")) {
                i = 0;
            }
            rotateImageButton.setVisibility(i);
            holder.mIconThumbMicro.setDegree(this.mDegree, false);
            holder.mIconThumb.setDegree(this.mDegree, false);
            holder.mPlayIcon.setDegree(this.mDegree, false);
            holder.mModeColumn.setDegree(this.mDegree, false);
        }
    }

    private ItemHolder setDefaultHolder(View convertView) {
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

    public ArrayList<ThumbnailListItem> getItemList() {
        return (ArrayList) this.mItemList.clone();
    }

    public void addItem(ThumbnailListItem item) {
        this.mItemList.add(0, item);
        if (this.mItemList.size() > 200) {
            this.mItemList.remove(200);
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

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (this.mPagerAdapter != null) {
            this.mPagerAdapter.notifyDataSetChanged();
        }
    }

    public void notifyDataSetChanged(boolean onlyListView) {
        super.notifyDataSetChanged();
    }

    public void setPagerAdapter(ThumbnailListPagerAdapter pagerAdapter) {
        this.mPagerAdapter = pagerAdapter;
    }

    public void setSelectedItem(int position) {
        CamLog.m7i(CameraConstants.TAG, "[Tile] setSelectedItem position : " + position);
        this.mSel = position;
        notifyDataSetChanged(true);
    }

    public void setDegree(int degree) {
        if (this.mIsRTL) {
            this.mDegree = (degree + 90) % 360;
        } else {
            this.mDegree = (degree + 270) % 360;
        }
        CamLog.m3d(CameraConstants.TAG, "[Tile] mDegree : " + this.mDegree);
        if (!this.mGet.getBurstProgress()) {
            notifyDataSetChanged();
        }
    }

    public void rotateThumbnailListView(int degree, ThumbnailListView thumbnailList) {
        if (this.mIsRTL) {
            this.mDegree = (degree + 90) % 360;
        } else {
            this.mDegree = (degree + 270) % 360;
        }
        CamLog.m3d(CameraConstants.TAG, "[Tile] mDegree : " + this.mDegree);
        int i = 0;
        while (i < 6) {
            View view = thumbnailList.getChildAt(i);
            if (view != null) {
                RotateImageView modeColumn;
                RotateImageView iconThumb = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_item_image);
                RotateImageView iconThumbMicro = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_item_image_micro);
                if (this.mIsRTL) {
                    modeColumn = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_mode_rtl);
                } else {
                    modeColumn = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_mode);
                }
                RotateImageButton playIcon = (RotateImageButton) view.findViewById(C0088R.id.thumbnail_list_item_play_icon);
                if (iconThumb != null && iconThumbMicro != null && modeColumn != null && playIcon != null) {
                    iconThumb.setDegree(this.mDegree, true);
                    iconThumbMicro.setDegree(this.mDegree, true);
                    modeColumn.setDegree(this.mDegree, true);
                    playIcon.setDegree(this.mDegree, true);
                    i++;
                } else {
                    return;
                }
            }
            return;
        }
    }
}
