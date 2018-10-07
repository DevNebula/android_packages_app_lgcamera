package com.lge.camera.app;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.CursorAdapter;
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

public class ThumbnailAdapter extends CursorAdapter {
    private LayoutInflater inflater;
    private int listViewItemWidth;
    private Context mContext;
    int mDegree = 0;
    TilePreviewInterface mGet;
    RotateImageView mIconThumb;
    RotateImageView mIconThumbMicro;
    private boolean mIsRTL = false;
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

    public ThumbnailAdapter(Context context, Cursor c, ThumbnailHelper mThumbnailHelper, TilePreviewInterface mget) {
        super(context, c);
        this.mContext = context;
        this.mThumbnailHelper = mThumbnailHelper;
        this.inflater = LayoutInflater.from(context);
        this.mGet = mget;
        this.listViewItemWidth = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.11112f);
        this.mIsRTL = Utils.isRTLLanguage();
        CamLog.m3d(CameraConstants.TAG, "[Tile] is RTL? : " + this.mIsRTL);
    }

    public void bindView(View view, Context context, Cursor cursor) {
        if (view != null) {
            int color;
            ItemHolder holder = (ItemHolder) view.getTag();
            if (cursor.getPosition() % 2 == 0) {
                color = this.mContext.getColor(C0088R.color.tile_preview_bg_1);
            } else {
                color = this.mContext.getColor(C0088R.color.tile_preview_bg_2);
            }
            view.setBackgroundColor(color);
            ThumbnailListItem item = ThumbnailListItem.fromCursor(cursor);
            int pos = cursor.getPosition();
            int count = cursor.getCount();
            boolean isGoingHome = pos < this.prePos;
            if ((this.mGet == null || this.mGet.getBurstProgress()) && pos == 0) {
                holder.mIconThumbMicro.setVisibility(8);
                holder.mIconThumb.setVisibility(8);
                return;
            }
            holder.mIconThumbMicro.setVisibility(0);
            holder.mIconThumb.setVisibility(0);
            this.mThumbnailHelper.stopLoading(holder.mIconThumbMicro);
            this.mThumbnailHelper.loadThumbnail(item, holder.mIconThumbMicro, 50);
            this.mThumbnailHelper.stopLoading(holder.mIconThumb);
            this.mThumbnailHelper.loadThumbnail(item, holder.mIconThumb, 40);
            int nextPos;
            if (isGoingHome) {
                nextPos = pos - 5;
                if (nextPos >= 0) {
                    this.mThumbnailHelper.loadThumbnail(ThumbnailListItem.fromCursor((Cursor) getItem(nextPos)), null, 30);
                }
            } else {
                nextPos = pos + 5;
                if (nextPos < count) {
                    this.mThumbnailHelper.loadThumbnail(ThumbnailListItem.fromCursor((Cursor) getItem(nextPos)), null, 30);
                }
            }
            this.prePos = pos;
            ImageView imageView = holder.mSelected;
            color = (pos != this.mSel || this.mSel == -1) ? 8 : 0;
            imageView.setVisibility(color);
            setItemLayout(holder, item);
            if (item.mIsImage) {
                view.setContentDescription(String.format(this.mContext.getString(C0088R.string.talkback_cell_image_selected), new Object[]{Integer.valueOf(pos + 1)}));
            } else {
                view.setContentDescription(String.format(this.mContext.getString(C0088R.string.talkback_cell_video_selected), new Object[]{Integer.valueOf(pos + 1)}));
            }
        }
    }

    public void setItemLayout(ItemHolder holder, ThumbnailListItem item) {
        int i = 8;
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

    public View newView(Context arg0, Cursor cursor, ViewGroup arg2) {
        View view = this.inflater.inflate(C0088R.layout.thumbnail_list_item, arg2, false);
        if (view == null) {
            return null;
        }
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        lp.width = this.listViewItemWidth;
        lp.height = this.listViewItemWidth;
        view.setLayoutParams(lp);
        ItemHolder holder = new ItemHolder();
        if (holder != null) {
            holder.mIconThumb = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_item_image);
            holder.mIconThumbMicro = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_item_image_micro);
            holder.mSelected = (ImageView) view.findViewById(C0088R.id.thumbnail_list_item_selected);
            if (this.mIsRTL) {
                holder.mModeColumn = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_mode_rtl);
            } else {
                holder.mModeColumn = (RotateImageView) view.findViewById(C0088R.id.thumbnail_list_mode);
            }
            holder.mPlayIcon = (RotateImageButton) view.findViewById(C0088R.id.thumbnail_list_item_play_icon);
        }
        view.setTag(holder);
        return view;
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

    public void setAutoRequery(boolean flag) {
        this.mAutoRequery = flag;
    }
}
