package com.lge.camera.managers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.StrokeTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import java.util.ArrayList;
import java.util.List;

public class ColorEffectAdapter extends BaseAdapter {
    protected Context mContext = null;
    protected int mDegree = 0;
    protected List<ColorItem> mList = null;

    protected class ColorEffectItemHolder {
        ImageView mImageView;
        RotateLayout mRotateLayout;
        StrokeTextView mTextView;

        protected ColorEffectItemHolder() {
        }
    }

    public ColorEffectAdapter(Context context, ArrayList<ColorItem> list, int degree) {
        this.mContext = context;
        this.mList = list;
        this.mDegree = degree;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.mList == null) {
            return convertView;
        }
        ColorEffectItemHolder holder;
        View view = convertView;
        if (view == null) {
            view = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(C0088R.layout.color_effect_grid_view_item, null);
            if (view == null) {
                CamLog.m11w(CameraConstants.TAG, "Color adapter error. view is null.");
                return null;
            }
            holder = new ColorEffectItemHolder();
            holder.mImageView = (ImageView) view.findViewById(C0088R.id.color_effect_item_image_view);
            holder.mRotateLayout = (RotateLayout) view.findViewById(C0088R.id.color_effect_item_text_rotate_layout);
            holder.mTextView = (StrokeTextView) view.findViewById(C0088R.id.color_effect_item_text_view);
            view.setTag(holder);
        } else {
            holder = (ColorEffectItemHolder) view.getTag();
        }
        int[] size = getImageSize();
        LayoutParams lp = view.getLayoutParams();
        if (lp != null) {
            lp.width = size[0];
            lp.height = size[1];
        } else {
            lp = new RelativeLayout.LayoutParams(size[0], size[1]);
        }
        view.setLayoutParams(lp);
        ColorItem curItem = (ColorItem) this.mList.get(position);
        if (curItem != null) {
            setImage(holder, position, curItem);
            setText(holder, position, curItem);
            view.setContentDescription(curItem.getTitle());
        }
        return view;
    }

    public int[] getImageSize() {
        int width = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.311f);
        int height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, true, 0.234f);
        return new int[]{width, height};
    }

    protected void setImage(ColorEffectItemHolder holder, int position, ColorItem curItem) {
        if (holder != null) {
            ImageView image = holder.mImageView;
            if (image != null) {
                Drawable background = this.mContext.getDrawable(curItem.getDrawableId());
                switch (this.mDegree) {
                    case 0:
                        background.setLevel(0);
                        break;
                    case 90:
                        background.setLevel(1);
                        image.setRotation(180.0f);
                        break;
                    case 180:
                        background.setLevel(0);
                        image.setRotation(180.0f);
                        break;
                    case 270:
                        background.setLevel(1);
                        break;
                }
                image.setBackground(background);
                image.setImageDrawable(this.mContext.getResources().getDrawable(curItem.isSelected() ? C0088R.drawable.camera_setting_film_frame_selected : C0088R.drawable.camera_setting_film_frame_normal));
                image.setFocusable(false);
            }
        }
    }

    protected void setText(ColorEffectItemHolder holder, int position, ColorItem curItem) {
        if (holder != null) {
            RotateLayout textLayout = holder.mRotateLayout;
            if (textLayout != null) {
                textLayout.setAngle(this.mDegree);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textLayout.getLayoutParams();
                if (params != null) {
                    switch (this.mDegree) {
                        case 0:
                            params.removeRule(10);
                            params.addRule(12);
                            params.removeRule(20);
                            params.addRule(21);
                            break;
                        case 90:
                            params.removeRule(12);
                            params.addRule(10);
                            params.removeRule(20);
                            params.addRule(21);
                            break;
                        case 180:
                            params.removeRule(12);
                            params.addRule(10);
                            params.removeRule(21);
                            params.addRule(20);
                            break;
                        case 270:
                            params.removeRule(10);
                            params.addRule(12);
                            params.removeRule(21);
                            params.addRule(20);
                            break;
                    }
                    textLayout.setLayoutParams(params);
                    textLayout.setFocusable(false);
                }
            }
            boolean selected = curItem.isSelected();
            StrokeTextView text = holder.mTextView;
            if (text != null) {
                text.setText(curItem.getTitle());
                text.setTextColor(this.mContext.getColor(selected ? C0088R.color.camera_accent_txt : C0088R.color.camera_white_txt));
                text.setSelected(selected);
            }
        }
    }

    public void setDegree(int degree) {
        this.mDegree = degree;
    }

    public int getCount() {
        if (this.mList == null) {
            return 0;
        }
        return this.mList.size();
    }

    public ColorItem getItem(int position) {
        if (this.mList == null) {
            return null;
        }
        return (ColorItem) this.mList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }
}
