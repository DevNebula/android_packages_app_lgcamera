package com.lge.camera.managers.ext.sticker;

import android.support.p001v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageView;

public class TabViewHolder extends ViewHolder {
    public View mBottomLine;
    public RotateImageView mDeleteIcon;
    public RotateImageView mIcon;
    public boolean mIsSelected;
    private OnViewHolderClickListener mListener;

    public interface OnViewHolderClickListener {
        void onClicked(int i);
    }

    /* renamed from: com.lge.camera.managers.ext.sticker.TabViewHolder$1 */
    class C13511 implements OnClickListener {
        C13511() {
        }

        public void onClick(View view) {
            if (TabViewHolder.this.mListener != null) {
                TabViewHolder.this.mListener.onClicked(TabViewHolder.this.getAdapterPosition());
            }
        }
    }

    public TabViewHolder(View v) {
        super(v);
        this.mIsSelected = false;
        this.mBottomLine = this.itemView.findViewById(C0088R.id.bottom_selector);
        this.mIcon = (RotateImageView) this.itemView.findViewById(C0088R.id.tab_icon);
        this.mDeleteIcon = (RotateImageView) this.itemView.findViewById(C0088R.id.delete_icon);
        this.mIsSelected = false;
        this.itemView.setOnClickListener(new C13511());
    }

    public void setOnViewHolderClickListener(OnViewHolderClickListener listener) {
        if (this.mListener == null) {
            this.mListener = listener;
        }
    }
}
