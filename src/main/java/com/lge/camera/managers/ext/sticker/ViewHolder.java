package com.lge.camera.managers.ext.sticker;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageView;

public class ViewHolder extends android.support.p001v7.widget.RecyclerView.ViewHolder {
    public final RelativeLayout mContainer;
    public final RotateImageView mImageView = ((RotateImageView) this.mContainer.findViewById(C0088R.id.sticker_item));
    private OnViewHolderClickListener mListener;
    public final ImageView mSelectedImage = ((ImageView) this.mContainer.findViewById(C0088R.id.item_selected));

    /* renamed from: com.lge.camera.managers.ext.sticker.ViewHolder$1 */
    class C13521 implements OnClickListener {
        C13521() {
        }

        public void onClick(View view) {
            if (ViewHolder.this.mListener != null) {
                ViewHolder.this.mListener.onClicked(ViewHolder.this.getAdapterPosition());
            }
        }
    }

    public interface OnViewHolderClickListener {
        void onClicked(int i);
    }

    public ViewHolder(RelativeLayout iv) {
        super(iv);
        this.mContainer = iv;
        this.itemView.setOnClickListener(new C13521());
    }

    public void setOnViewHolderClickListener(OnViewHolderClickListener listener) {
        if (this.mListener == null) {
            this.mListener = listener;
        }
    }
}
