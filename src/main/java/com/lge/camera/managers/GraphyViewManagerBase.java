package com.lge.camera.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.p001v7.widget.RecyclerView;
import android.support.p001v7.widget.RecyclerView.Adapter;
import android.support.p001v7.widget.RecyclerView.ItemDecoration;
import android.support.p001v7.widget.RecyclerView.LayoutParams;
import android.support.p001v7.widget.RecyclerView.State;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.managers.ext.DetailViewHandler;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.MathUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import com.lge.graphy.data.GraphyItem;
import java.util.ArrayList;

public abstract class GraphyViewManagerBase extends ManagerInterfaceImpl {
    private final double ITEM_SHOW_COUNT = 5.5d;
    protected double mComparativeIlluminance = 0.0d;
    protected DetailViewHandler mDetailViewHandler;
    protected GraphyAdapter mGraphyAdapter = null;
    protected GraphyInterface mGraphyGet = null;
    protected RecyclerView mGraphyRecyclerView = null;
    protected RelativeLayout mGraphyRecyclerViewWrapper = null;
    protected boolean mIsRTL = false;
    protected int mListViewItemWidth = 0;
    protected int mSelectedPosition = -1;

    class GraphyAdapter extends Adapter<ViewHolder> {
        private ArrayList<GraphyItem> itemList;
        private boolean mIsChangedManualDataByUser = false;
        private boolean mIsLongClick = false;
        private int mLongClickedPosition = -1;
        private long mPreviousClickTime = 0;

        public class ViewHolder extends android.support.p001v7.widget.RecyclerView.ViewHolder {
            public ImageView arrowView;
            public RelativeLayout categoryView;
            public RelativeLayout categoryViewBG;
            public RotateTextView category_text_View;
            public RotateImageView imageView;
            public RelativeLayout image_text_View;
            public ImageView moreView;
            public TextView textView;

            public ViewHolder(View view) {
                super(view);
                this.imageView = null;
                this.image_text_View = null;
                this.category_text_View = null;
                this.textView = null;
                this.categoryView = null;
                this.categoryViewBG = null;
                this.arrowView = null;
                this.moreView = null;
                this.imageView = (RotateImageView) this.itemView.findViewById(C0088R.id.graphy_thumbnail);
                this.image_text_View = (RelativeLayout) this.itemView.findViewById(C0088R.id.graphy_thumbnail_add_text_layout);
                this.category_text_View = (RotateTextView) this.itemView.findViewById(C0088R.id.graphy_category_prefix_text);
                this.categoryViewBG = (RelativeLayout) this.itemView.findViewById(C0088R.id.graphy_category_bg);
                this.categoryView = (RelativeLayout) this.itemView.findViewById(C0088R.id.graphy_category);
                this.textView = (TextView) this.itemView.findViewById(C0088R.id.graphy_category_txt);
                this.arrowView = (ImageView) this.itemView.findViewById(C0088R.id.graphy_category_arrow);
                this.moreView = (ImageView) this.itemView.findViewById(C0088R.id.graphy_category_more);
                GraphyAdapter.this.setOnLongClickListener(this);
                GraphyAdapter.this.setOnTouchListener(this);
                GraphyAdapter.this.setOnClickListener(this);
            }
        }

        public GraphyAdapter() {
            CamLog.m3d(CameraConstants.TAG, "[Graphy] listViewItemWidth : " + GraphyViewManagerBase.this.mListViewItemWidth);
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CamLog.m3d(CameraConstants.TAG, "[Graphy] onCreateViewHolder listViewItemWidth : " + GraphyViewManagerBase.this.mListViewItemWidth);
            View view = LayoutInflater.from(parent.getContext()).inflate(C0088R.layout.graphy_item, parent, false);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            lp.width = GraphyViewManagerBase.this.mListViewItemWidth;
            lp.height = GraphyViewManagerBase.this.mListViewItemWidth + Utils.getPx(GraphyViewManagerBase.this.getAppContext(), C0088R.dimen.graphy_item_padding);
            view.setLayoutParams(lp);
            return new ViewHolder(view);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            if (this.itemList != null && holder != null) {
                CamLog.m3d(CameraConstants.TAG, "[Graphy] onBindViewHolder position : " + position);
                holder.itemView.setBackgroundColor(Color.argb(192, 38, 38, 38));
                GraphyItem item = (GraphyItem) this.itemList.get(position);
                holder.imageView.setBackgroundDrawable(null);
                loadThumbnail(holder, item);
                if (item.getType() != 1) {
                    GraphyViewManagerBase.this.setCategoryItemView(holder, item);
                } else {
                    holder.categoryView.setVisibility(8);
                    holder.categoryViewBG.setVisibility(8);
                }
                if (item.getType() == 1) {
                    holder.imageView.setContentDescription(String.format(GraphyViewManagerBase.this.getAppContext().getString(C0088R.string.talkback_cell_image_selected), new Object[]{Integer.valueOf(position)}));
                } else if (item.getType() == 2) {
                    holder.imageView.setContentDescription(GraphyViewManagerBase.this.getAppContext().getString(C0088R.string.add));
                } else if (item.getType() == 3) {
                    holder.imageView.setContentDescription(null);
                }
                holder.image_text_View.setRotation((float) (-GraphyViewManagerBase.this.mManagerDegree));
                holder.categoryView.setRotation((float) (-GraphyViewManagerBase.this.mManagerDegree));
                if (GraphyViewManagerBase.this.mSelectedPosition == -1) {
                    item.setSelected(false);
                }
                if (!ModelProperties.isJoanRenewal() && !ModelProperties.isFakeMode()) {
                    String categoryPrefix = item.getStringValue(GraphyItem.KEY_CATEGORY_PREFIX);
                    String luxPrefix = item.getStringValue(GraphyItem.KEY_LUX);
                    CamLog.m3d(CameraConstants.TAG, "[graphy] position : " + position + ", mSelectedPosition : " + GraphyViewManagerBase.this.mSelectedPosition + ", item.getSelected() : " + item.getSelected());
                    CamLog.m3d(CameraConstants.TAG, "[graphy] setText : " + item.getCategory(categoryPrefix) + ", luxPrefix : " + luxPrefix + ", item.getGraphyLux : " + item.getGraphyLux(luxPrefix));
                    holder.category_text_View.setText(item.getCategory(categoryPrefix));
                    CamLog.m7i(CameraConstants.TAG, "[graphy][lux] mComparativeIlluminance : " + GraphyViewManagerBase.this.mComparativeIlluminance + ", item getIlluminance : " + item.getDoubleValue(GraphyItem.KEY_ILLUMINANCE));
                    if (Math.abs(GraphyViewManagerBase.this.mComparativeIlluminance - item.getDoubleValue(GraphyItem.KEY_ILLUMINANCE)) > 1.0d) {
                        holder.imageView.setImageResource(0);
                    } else {
                        holder.imageView.setImageResource(C0088R.drawable.ic_graphy_recommend);
                    }
                    holder.category_text_View.setTextColor(GraphyViewManagerBase.this.mGet.getAppContext().getColor(item.getSelected() ? C0088R.color.camera_pressed_txt : C0088R.color.camera_white_txt));
                }
            }
        }

        public int getItemCount() {
            if (this.itemList != null) {
                return this.itemList.size();
            }
            return 0;
        }

        public void setItemList(ArrayList<GraphyItem> itemList) {
            this.itemList = itemList;
        }

        public ArrayList<GraphyItem> getItemList() {
            return this.itemList;
        }

        public boolean isLongClick() {
            return this.mIsLongClick;
        }

        public void setIsLongClick(boolean longclick) {
            this.mIsLongClick = longclick;
        }

        public void setIsChangedManualDataByUser(boolean changed) {
            this.mIsChangedManualDataByUser = changed;
        }

        public int getLongClickedPosition() {
            return this.mLongClickedPosition;
        }

        private void loadThumbnail(ViewHolder holder, GraphyItem item) {
            ThumbnailLoadTask oldLoadTask = (ThumbnailLoadTask) holder.itemView.getTag();
            if (oldLoadTask != null) {
                oldLoadTask.cancel(true);
            }
            ThumbnailLoadTask newLoadTask = new ThumbnailLoadTask(holder, item, 0);
            holder.itemView.setTag(newLoadTask);
            newLoadTask.execute(new Void[0]);
        }

        private void setOnLongClickListener(final ViewHolder holder) {
            if (!holder.itemView.isLongClickable()) {
                holder.itemView.setLongClickable(true);
                holder.itemView.setOnLongClickListener(new OnLongClickListener() {
                    public boolean onLongClick(View view) {
                        if (GraphyViewManagerBase.this.mGet.checkModuleValidate(48)) {
                            GraphyAdapter.this.mLongClickedPosition = holder.getAdapterPosition();
                            CamLog.m3d(CameraConstants.TAG, "[Graphy] long click : " + GraphyAdapter.this.mLongClickedPosition);
                            GraphyItem item = (GraphyItem) GraphyAdapter.this.itemList.get(GraphyAdapter.this.mLongClickedPosition);
                            if (item.getType() == 1) {
                                GraphyAdapter.this.mIsLongClick = true;
                                AudioUtil.performHapticFeedback(holder.itemView, 65585);
                                String wb = item.getStringValue(GraphyItem.KEY_WB_STR);
                                String aperture = item.getStringValue(GraphyItem.KEY_APERTURE);
                                if (aperture != null && aperture.toLowerCase().contains("f/")) {
                                    aperture = aperture.replace("f/", "");
                                }
                                String ss = item.getStringValue(GraphyItem.KEY_SHUTTER_SPEED_STR);
                                String iso = item.getStringValue(GraphyItem.KEY_ISO_STR);
                                String categoryName = item.getCategory(item.getStringValue(GraphyItem.KEY_CATEGORY_PREFIX));
                                if (GraphyAdapter.this.mLongClickedPosition != GraphyViewManagerBase.this.mSelectedPosition) {
                                    item.setSelected(false);
                                    GraphyAdapter.this.notifyItemChanged(GraphyAdapter.this.mLongClickedPosition);
                                }
                                float shutterSpeed = MathUtil.parseStringToFloat(ss);
                                if (shutterSpeed > 1.0f) {
                                    ss = GraphyViewManagerBase.this.convertFormat(shutterSpeed);
                                }
                                CamLog.m3d(CameraConstants.TAG, "[graphy][detail] categoryName : " + categoryName);
                                GraphyViewManagerBase.this.mDetailViewHandler.showGraphyDetailView(GraphyViewManagerBase.this.mManagerDegree, item, categoryName, wb, aperture, iso, ss);
                            }
                        }
                        return false;
                    }
                });
            }
        }

        private void setOnClickListener(final ViewHolder holder) {
            holder.itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!GraphyAdapter.this.mIsLongClick && !GraphyViewManagerBase.this.isListAnimating() && GraphyViewManagerBase.this.mGet.checkModuleValidate(48) && GraphyViewManagerBase.this.mGraphyRecyclerViewWrapper.getVisibility() == 0) {
                        GraphyAdapter.this.OnItemSelected(holder);
                    }
                }
            });
        }

        private void setOnTouchListener(final ViewHolder holder) {
            holder.itemView.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == 1) {
                        CamLog.m3d(CameraConstants.TAG, "[Graphy] touch position : " + holder.getAdapterPosition());
                        if (GraphyAdapter.this.mIsLongClick) {
                            GraphyAdapter.this.mIsLongClick = false;
                            GraphyAdapter.this.mLongClickedPosition = -1;
                            GraphyViewManagerBase.this.hideDetailView();
                            return true;
                        }
                    } else if (event.getAction() == 0) {
                        GraphyViewManagerBase.this.removeInitialGuideLayout();
                    }
                    return false;
                }
            });
        }

        private boolean checkFastClick() {
            long currentTime = SystemClock.uptimeMillis();
            long gap = currentTime - this.mPreviousClickTime;
            this.mPreviousClickTime = currentTime;
            if (gap < 100) {
                return true;
            }
            return false;
        }

        private void OnItemSelected(ViewHolder holder) {
            int newPosition = holder.getAdapterPosition();
            GraphyItem item = (GraphyItem) this.itemList.get(newPosition);
            if (item != null && !checkFastClick()) {
                if (GraphyViewManagerBase.this.mSelectedPosition != newPosition || item.getType() != 1 || this.mIsChangedManualDataByUser) {
                    if (item.getType() == 1) {
                        GraphyViewManagerBase.this.smoothScrollToPosition(holder);
                    }
                    this.mIsChangedManualDataByUser = false;
                    View view = holder.itemView;
                    if (view != null && view.getTag() != null) {
                        CamLog.m3d(CameraConstants.TAG, "[Graphy] itemList.size() : " + this.itemList.size() + ", selected item position : " + GraphyViewManagerBase.this.mSelectedPosition + ", newPosition : " + newPosition + ", SELECT TURE!!!!");
                        if (GraphyViewManagerBase.this.mSelectedPosition >= this.itemList.size()) {
                            GraphyViewManagerBase.this.mSelectedPosition = -1;
                        }
                        if (GraphyViewManagerBase.this.mSelectedPosition != -1) {
                            GraphyItem olditem = (GraphyItem) this.itemList.get(GraphyViewManagerBase.this.mSelectedPosition);
                            item.setSelected(true);
                            olditem.setSelected(false);
                        }
                        notifyItemChanged(GraphyViewManagerBase.this.mSelectedPosition);
                        notifyItemChanged(newPosition);
                        if (item.getType() != 2) {
                            GraphyViewManagerBase.this.mSelectedPosition = newPosition;
                        }
                        GraphyViewManagerBase.this.mGraphyGet.sendSelectedItemToControlManager(item, false);
                    }
                }
            }
        }
    }

    class GraphyDividerItemDecoration extends ItemDecoration {
        private Drawable mDivider;

        public GraphyDividerItemDecoration(Context context) {
            this.mDivider = context.getResources().getDrawable(C0088R.drawable.graphy_divider, null);
        }

        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            super.getItemOffsets(outRect, itemPosition, parent);
            if (itemPosition != parent.getAdapter().getItemCount() - 1) {
                outRect.right = Utils.getPx(GraphyViewManagerBase.this.getAppContext(), C0088R.dimen.graphy_item_padding);
            }
        }

        public void onDrawOver(Canvas c, RecyclerView parent, State state) {
            ViewHolder holder = (ViewHolder) parent.findViewHolderForAdapterPosition(GraphyViewManagerBase.this.mSelectedPosition);
            if (holder != null) {
                View child = holder.itemView;
                if (child != null) {
                    int left = child.getLeft();
                    int right = left + this.mDivider.getIntrinsicHeight();
                    this.mDivider.setBounds(left, child.getTop(), right, child.getHeight());
                    this.mDivider.draw(c);
                    right = child.getRight();
                    this.mDivider.setBounds(right - this.mDivider.getIntrinsicHeight(), child.getTop(), right, child.getHeight());
                    this.mDivider.draw(c);
                    left = child.getLeft();
                    right = child.getRight();
                    int top = child.getTop();
                    this.mDivider.setBounds(left, top, right, top + this.mDivider.getIntrinsicHeight());
                    this.mDivider.draw(c);
                    int bottom = child.getBottom() - this.mDivider.getIntrinsicHeight();
                    top = bottom - this.mDivider.getIntrinsicHeight();
                    this.mDivider.setBounds(child.getLeft(), top, child.getRight(), bottom);
                    this.mDivider.draw(c);
                    holder.category_text_View.setTextColor(GraphyViewManagerBase.this.mGet.getAppContext().getColor(C0088R.color.camera_pressed_txt));
                }
            }
        }
    }

    class ThumbnailLoadTask extends AsyncTask<Void, Void, Bitmap> {
        public static final int LARGE_TYPE = 1;
        public static final int SMALL_TYPE = 0;
        ViewHolder holder = null;
        GraphyItem item = null;
        boolean needAniMation = false;
        int type = 0;

        public ThumbnailLoadTask(ViewHolder holder, GraphyItem item, int type) {
            this.holder = holder;
            this.item = item;
            this.type = type;
        }

        public void setAnimation(boolean needAniMation) {
            this.needAniMation = needAniMation;
        }

        protected Bitmap doInBackground(Void... arg0) {
            Bitmap bitmap;
            int resId = this.item.getIntValue(GraphyItem.KEY_RESOURCE_ID_INT);
            String imagePath = this.item.getStringValue(GraphyItem.KEY_IMAGE_PATH_STR);
            if (resId > 0) {
                bitmap = BitmapManagingUtil.getBitmap(GraphyViewManagerBase.this.getAppContext(), resId);
            } else if (imagePath != null) {
                bitmap = BitmapFactory.decodeFile(imagePath);
            } else {
                bitmap = this.item.getBitmap();
            }
            if (bitmap == null || bitmap.isRecycled()) {
                return null;
            }
            if (this.type == 0) {
                return ThumbnailUtils.extractThumbnail(bitmap, GraphyViewManagerBase.this.mListViewItemWidth, GraphyViewManagerBase.this.mListViewItemWidth, 2);
            }
            if (this.type != 1) {
                return null;
            }
            CamLog.m5e(CameraConstants.TAG, "[detail] LARGE_TYPE.");
            return BitmapManagingUtil.getRoundedCornerBitmap(getResizedBitmap(bitmap), RatioCalcUtil.getSizeCalculatedByPercentage(GraphyViewManagerBase.this.getAppContext(), true, 0.0125f));
        }

        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                return;
            }
            if (this.type == 0) {
                this.holder.imageView.setBackgroundDrawable(new BitmapDrawable(result));
                if (!(ModelProperties.isJoanRenewal() || ModelProperties.isFakeMode())) {
                    String str = CameraConstants.TAG;
                    StringBuilder append = new StringBuilder().append("[graphy] 2. setText : ");
                    GraphyItem graphyItem = this.item;
                    GraphyItem graphyItem2 = this.item;
                    GraphyItem graphyItem3 = this.item;
                    CamLog.m3d(str, append.append(graphyItem.getCategory(graphyItem2.getStringValue(GraphyItem.KEY_CATEGORY_PREFIX))).append(", getSelected : ").append(this.item.getSelected()).toString());
                    RotateTextView rotateTextView = this.holder.category_text_View;
                    GraphyItem graphyItem4 = this.item;
                    graphyItem = this.item;
                    graphyItem2 = this.item;
                    rotateTextView.setText(graphyItem4.getCategory(graphyItem.getStringValue(GraphyItem.KEY_CATEGORY_PREFIX)));
                    this.holder.category_text_View.setVisibility(0);
                }
                this.holder.image_text_View.setVisibility(0);
                return;
            }
            if (!(this.type == 1 && GraphyViewManagerBase.this.mGraphyAdapter != null && GraphyViewManagerBase.this.mGraphyAdapter.isLongClick())) {
            }
        }

        protected void onCancelled(Bitmap result) {
            CamLog.m3d(CameraConstants.TAG, "[Graphy] canceled bitmap : " + result);
        }

        private Bitmap getResizedBitmap(Bitmap bitmap) {
            if (bitmap == null) {
                return null;
            }
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            float ratio = getRatio(bitmapWidth, bitmapHeight);
            Matrix matrix = new Matrix();
            matrix.postRotate((float) (GraphyViewManagerBase.this.mManagerDegree * -1));
            matrix.postScale(ratio, ratio);
            Bitmap resized = null;
            try {
                resized = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            } catch (Exception e) {
                e.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, e.getMessage());
            } finally {
                if (!(bitmap == null || bitmap == resized)) {
                    bitmap.recycle();
                }
            }
            return matrix != null ? resized : resized;
        }

        private float getRatio(int bitmapWidth, int bitmapHeight) {
            int maxWidth;
            int maxHeight;
            if (GraphyViewManagerBase.this.mManagerDegree == 90 || GraphyViewManagerBase.this.mManagerDegree == 270) {
                if (bitmapWidth < bitmapHeight) {
                    return ((float) RatioCalcUtil.getSizeCalculatedByPercentage(GraphyViewManagerBase.this.getAppContext(), false, 0.8889f)) / ((float) bitmapHeight);
                }
                maxWidth = RatioCalcUtil.getSizeCalculatedByPercentage(GraphyViewManagerBase.this.getAppContext(), true, 0.7778f);
                maxHeight = RatioCalcUtil.getSizeCalculatedByPercentage(GraphyViewManagerBase.this.getAppContext(), false, 0.7778f);
                if (((float) maxWidth) / ((float) maxHeight) < ((float) bitmapWidth) / ((float) bitmapHeight)) {
                    return ((float) maxWidth) / ((float) bitmapWidth);
                }
                return ((float) maxHeight) / ((float) bitmapHeight);
            } else if (GraphyViewManagerBase.this.mManagerDegree != 0 && GraphyViewManagerBase.this.mManagerDegree != 180) {
                return 1.0f;
            } else {
                if (bitmapWidth > bitmapHeight) {
                    return ((float) RatioCalcUtil.getSizeCalculatedByPercentage(GraphyViewManagerBase.this.getAppContext(), false, 0.8889f)) / ((float) bitmapWidth);
                }
                maxWidth = RatioCalcUtil.getSizeCalculatedByPercentage(GraphyViewManagerBase.this.getAppContext(), false, 0.7778f);
                maxHeight = RatioCalcUtil.getSizeCalculatedByPercentage(GraphyViewManagerBase.this.getAppContext(), true, 0.7778f);
                if (((float) maxHeight) / ((float) maxWidth) < ((float) bitmapHeight) / ((float) bitmapWidth)) {
                    return ((float) maxHeight) / ((float) bitmapHeight);
                }
                return ((float) maxWidth) / ((float) bitmapWidth);
            }
        }
    }

    public abstract void hideDetailView();

    protected abstract boolean isListAnimating();

    protected abstract void removeInitialGuideLayout();

    protected abstract void setCategoryItemView(ViewHolder viewHolder, GraphyItem graphyItem);

    protected abstract void smoothScrollToPosition(ViewHolder viewHolder);

    public GraphyViewManagerBase(GraphyInterface moduleInterface) {
        super(moduleInterface);
        this.mGraphyGet = moduleInterface;
        this.mListViewItemWidth = (int) ((((double) Utils.getLCDsize(getAppContext(), true)[1]) - (((double) Utils.getPx(getAppContext(), C0088R.dimen.graphy_item_padding)) * Math.ceil(5.5d))) / 5.5d);
        this.mIsRTL = Utils.isRTLLanguage();
    }

    public String convertFormat(float f) {
        if (f == ((float) ((long) f))) {
            return String.format("%d", new Object[]{Long.valueOf((long) f)});
        }
        return String.format("%s", new Object[]{Float.valueOf(f)});
    }

    public int getSelectedPosition() {
        return this.mSelectedPosition;
    }
}
