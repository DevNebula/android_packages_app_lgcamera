package com.lge.camera.managers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.StrokeTextView;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;

public class ColorEffectFrontAdapter extends ColorEffectAdapter {
    private final float COLOR_EFFECT_LISTVIEW_COLUMN_WIDTH = 0.178f;
    private final float TEXT_VIEW_PADDING = 3.0f;

    public ColorEffectFrontAdapter(Context context, ArrayList<ColorItem> list, int degree) {
        super(context, list, degree);
    }

    public int[] getImageSize() {
        int width = RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, false, 0.178f);
        return new int[]{width, width};
    }

    protected void setImage(ColorEffectItemHolder holder, int position, ColorItem curItem) {
        if (holder != null) {
            ImageView image = holder.mImageView;
            if (image != null) {
                switch (this.mDegree) {
                    case 0:
                        image.setRotation(90.0f);
                        break;
                    case 90:
                        image.setRotation(0.0f);
                        break;
                    case 180:
                        image.setRotation(270.0f);
                        break;
                    case 270:
                        image.setRotation(180.0f);
                        break;
                }
                Drawable background = this.mContext.getDrawable(curItem.getDrawableId());
                background.setLevel(2);
                image.setBackground(background);
                image.setImageDrawable(this.mContext.getResources().getDrawable(curItem.isSelected() ? C0088R.drawable.camera_setting_film_frame_front_selected : C0088R.drawable.camera_setting_film_frame_front_normal));
            }
        }
    }

    protected void setText(ColorEffectItemHolder holder, int position, ColorItem curItem) {
        if (holder != null) {
            RotateLayout textLayout = holder.mRotateLayout;
            if (textLayout != null) {
                LayoutParams params = (LayoutParams) textLayout.getLayoutParams();
                if (params != null) {
                    Utils.resetLayoutParameter(params);
                    switch (this.mDegree) {
                        case 0:
                            textLayout.setAngle(270);
                            params.addRule(15);
                            break;
                        case 90:
                            textLayout.setAngle(0);
                            params.addRule(12);
                            params.addRule(14);
                            break;
                        case 180:
                            textLayout.setAngle(90);
                            params.addRule(21);
                            params.addRule(15);
                            break;
                        case 270:
                            textLayout.setAngle(180);
                            params.addRule(14);
                            break;
                    }
                    textLayout.setLayoutParams(params);
                }
                StrokeTextView text = holder.mTextView;
                if (text != null) {
                    int padding = (int) Utils.dpToPx(this.mContext, 3.0f);
                    text.setPadding(padding, 0, padding, padding);
                    text.setText(curItem.getTitle());
                    text.setTextAppearance(C0088R.style.color_effect_list_view_text_stroke);
                    boolean selected = curItem.isSelected();
                    text.setTextColor(this.mContext.getColor(selected ? C0088R.color.camera_accent_txt : C0088R.color.camera_white_txt));
                    text.setSelected(selected);
                }
            }
        }
    }
}
