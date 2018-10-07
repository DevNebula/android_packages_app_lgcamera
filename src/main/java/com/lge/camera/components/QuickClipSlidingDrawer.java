package com.lge.camera.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Space;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.QuickClipManager;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.MemoryUtils;
import com.lge.camera.util.QuickClipSharedItem;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Iterator;

public class QuickClipSlidingDrawer extends MultiDirectionSlidingDrawer {
    public static final int ARROW_VIEW_TAG = 10;
    public static final int DEFAULT_QUICK_CLIP = 1;
    public static final int LEFT_ARROW = 101;
    private static final int MSG_UPDATE_VIEW_TYPE = 1000;
    public static final int NONE_ARROW = 102;
    public static final int QUICK_CLIP_FIRST = 0;
    public static final int QUICK_CLIP_MAX = 5;
    public static final int RIGHT_ARROW = 100;
    private static final int VIEW_CHANGE_OFFSET = 10;
    private static final int VIEW_CHANGE_TIMER = 500;
    View mArrowLayoutView;
    ImageView mArrowView;
    private Context mContext;
    private final Handler mHandler;
    private final ArrayList<RotateImageView> mIconViewList;
    private boolean mIsCircleView;
    private OnClickListener mItemClickedListener;
    private QuickClipManager mQuickClipManager;
    private int mSelectedTag;
    private onTriggerQuickClip mSetOnTriggerQuickClip;
    private int mSkipFrame;
    private int mTouchDownX;
    private int mTouchPadding;

    private class TouchHandler extends Handler {
        private TouchHandler() {
        }

        public void handleMessage(Message m) {
            switch (m.what) {
                case 1000:
                    QuickClipSlidingDrawer.this.notifyViewType(false);
                    return;
                default:
                    return;
            }
        }
    }

    public interface onTriggerQuickClip {
        void onChanged(boolean z);

        void onTouchEvent();
    }

    public QuickClipSlidingDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickClipSlidingDrawer(Context context, AttributeSet attrs, int defstyle) {
        super(context, attrs, defstyle);
        this.mIsCircleView = true;
        this.mTouchDownX = -1;
        this.mTouchPadding = 0;
        this.mSkipFrame = 0;
        this.mArrowView = null;
        this.mArrowLayoutView = null;
        this.mIconViewList = new ArrayList();
        this.mHandler = new TouchHandler();
        this.mContext = context;
        this.mTouchPadding = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_touch_padding);
    }

    public void setOnQuickClipStatus(onTriggerQuickClip onTriggerQuickClip) {
        this.mSetOnTriggerQuickClip = onTriggerQuickClip;
    }

    public void setOnItemClickedListener(OnClickListener onItemClickedListener) {
        this.mItemClickedListener = onItemClickedListener;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void setManager(QuickClipManager qcm) {
        this.mQuickClipManager = qcm;
    }

    public void notifyViewType(boolean isCircleView) {
        this.mIsCircleView = isCircleView;
        if (!(this.mSetOnTriggerQuickClip == null || this.mIsCircleView)) {
            this.mSetOnTriggerQuickClip.onChanged(this.mIsCircleView);
        }
        if (!this.mIsCircleView) {
            this.mHandler.removeMessages(1000);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;
        View handle = this.mHandle;
        int handleWidth = handle.getMeasuredWidth();
        int handleHeight = handle.getMeasuredHeight();
        View content = this.mContent;
        int handleTop = (height - handleHeight) / 2;
        int handleLeft = this.mExpanded ? this.mTopOffset : (width - handleWidth) + this.mBottomOffset;
        content.layout(this.mTopOffset + handleWidth, 0, (this.mTopOffset + handleWidth) + content.getMeasuredWidth(), content.getMeasuredHeight());
        handle.layout(handleLeft, handleTop, handleLeft + handleWidth, handleTop + handleHeight);
        this.mHandleHeight = handle.getHeight();
        this.mHandleWidth = handle.getWidth();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == 0 || heightSpecMode == 0) {
            throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
        }
        View handle = this.mHandle;
        measureChild(handle, widthMeasureSpec, heightMeasureSpec);
        this.mContent.measure(MeasureSpec.makeMeasureSpec(((widthSpecSize - handle.getMeasuredWidth()) - this.mTopOffset) - this.mContentOffset, 1073741824), MeasureSpec.makeMeasureSpec(heightSpecSize, 1073741824));
        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    protected void dispatchDraw(Canvas canvas) {
        long drawingTime = getDrawingTime();
        View handle = this.mHandle;
        View content = this.mContent;
        if (!this.mExpanded && !this.mTracking && !this.mAnimating) {
            drawChild(canvas, handle, drawingTime);
        } else if (this.mIsCircleView) {
            drawChild(canvas, handle, drawingTime);
        } else if (this.mSkipFrame > 0) {
            this.mSkipFrame--;
        } else {
            canvas.save();
            canvas.translate((float) (handle.getLeft() - this.mTopOffset), 0.0f);
            drawChild(canvas, content, drawingTime);
            canvas.restore();
            canvas.save();
            drawChild(canvas, handle, drawingTime);
            canvas.restore();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.mLocked) {
            return false;
        }
        if (this.mSetOnTriggerQuickClip != null) {
            this.mSetOnTriggerQuickClip.onTouchEvent();
        }
        if (this.mIsCircleView && event.getAction() == 0) {
            this.mSkipFrame = 1;
            this.mTouchDownX = (int) event.getX();
            this.mHandler.removeMessages(1000);
            this.mHandler.sendEmptyMessageDelayed(1000, 500);
        }
        return super.onInterceptTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mLocked) {
            return false;
        }
        int action = event.getAction();
        if (this.mTracking && this.mIsCircleView) {
            if (action == 2) {
                if (this.mTouchDownX >= 0 && Math.abs(this.mTouchDownX - ((int) event.getX())) > 10) {
                    CamLog.m3d(CameraConstants.TAG, "MSG_UPDATE_VIEW_TYPE by diff");
                    notifyViewType(false);
                }
            } else if (action == 1 || action == 3) {
                this.mTouchDownX = -1;
                this.mHandler.removeMessages(1000);
            }
        }
        return super.onTouchEvent(event);
    }

    protected boolean isTouchEventOnDrawer(MotionEvent event, Rect frame, Rect mContentFrame) {
        float x = event.getX();
        float y = event.getY();
        if (!this.mExpanded) {
            setTouchPadding(isOpened());
            Rect rect = new Rect();
            rect.top = frame.top;
            rect.bottom = frame.bottom;
            rect.left = frame.left - (this.mTouchPadding * 2);
            rect.right = frame.right + Math.abs(Utils.getPx(this.mContext, C0088R.dimen.quick_clip_circle_bottom_offset));
            if (!(this.mTracking || frame.contains((int) x, (int) y) || rect.contains((int) x, (int) y))) {
                return false;
            }
        } else if (!(this.mTracking || frame.contains((int) x, (int) y) || mContentFrame.contains((int) x, (int) y))) {
            return false;
        }
        return true;
    }

    protected void setPressedItem(MotionEvent event, View handle, View content) {
        if (handle != null && content != null && this.mHandle != null && this.mContent != null && this.mFrame != null && this.mContentFrame != null) {
            Rect frameRect = new Rect(this.mFrame);
            addTouchDelegate(frameRect);
            int handleWidth = this.mFrame.width() + Utils.getPx(this.mContext, C0088R.dimen.quick_clip_top_offset);
            int i;
            View selectedView;
            if (frameRect.contains((int) event.getX(), (int) event.getY())) {
                handle.setPressed(true);
                for (i = 0; i < ((ViewGroup) handle).getChildCount(); i++) {
                    selectedView = ((ViewGroup) handle).getChildAt(i);
                    Rect rect = getTouchRect(selectedView);
                    addFirstClipTouchPadding(rect, i);
                    if (rect.contains((int) event.getX(), (int) event.getY())) {
                        selectedView.setPressed(true);
                        this.mSelectedTag = getTag(selectedView);
                        return;
                    }
                }
            } else if (this.mExpanded && this.mContentFrame.contains((int) event.getX(), (int) event.getY())) {
                for (i = 0; i < ((ViewGroup) content).getChildCount(); i++) {
                    selectedView = ((ViewGroup) content).getChildAt(i);
                    if (getTouchRect(selectedView).contains(((int) event.getX()) - handleWidth, (int) event.getY())) {
                        selectedView.setPressed(true);
                        this.mSelectedTag = getTag(selectedView);
                        return;
                    }
                }
            }
        }
    }

    protected void addFirstClipTouchPadding(Rect rect, int i) {
        if (this.mExpanded && i == 1) {
            rect.left += Utils.getPx(getContext(), C0088R.dimen.quick_clip_touch_padding_first);
            rect.right += Utils.getPx(getContext(), C0088R.dimen.quick_clip_touch_padding_first);
        }
    }

    protected void changeTouchEvent(boolean vertical, int handleTop, int handleLeft, MotionEvent event, float velocity) {
        if (this.mHandle != null && this.mContent != null && this.mFrame != null && this.mContentFrame != null) {
            Rect frameRect = new Rect(this.mFrame);
            addTouchDelegate(frameRect);
            View selectedView = null;
            int x = (int) event.getX();
            int y = (int) event.getY();
            int i;
            View view;
            if (frameRect.contains((int) event.getX(), (int) event.getY())) {
                for (i = 0; i < ((ViewGroup) this.mHandle).getChildCount(); i++) {
                    view = ((ViewGroup) this.mHandle).getChildAt(i);
                    Rect rect = getTouchRect(view);
                    addFirstClipTouchPadding(rect, i);
                    CamLog.m3d(CameraConstants.TAG, "mSelectedTag : " + this.mSelectedTag + " current Tag" + getTag(view));
                    if (rect.contains(x, y) && getTag(view) == this.mSelectedTag) {
                        selectedView = view;
                        break;
                    }
                }
            } else if (this.mExpanded && this.mContentFrame.contains((int) event.getX(), (int) event.getY())) {
                LinearLayout contentLayout = this.mContent;
                int handleWidth = this.mFrame.width() + Utils.getPx(this.mContext, C0088R.dimen.quick_clip_top_offset);
                for (i = 0; i < contentLayout.getChildCount(); i++) {
                    view = contentLayout.getChildAt(i);
                    if (getTouchRect(view).contains(x - handleWidth, y) && getTag(view) == this.mSelectedTag) {
                        selectedView = view;
                        break;
                    }
                }
            }
            if (event.getAction() == 1) {
                this.mHandle.setPressed(false);
                this.mContent.setPressed(false);
            }
            if (selectedView != null) {
                selectedView.performClick();
            } else {
                performFling(handleLeft, velocity, false);
            }
        }
    }

    protected void performFling(int position, float velocity, boolean always) {
        notifyViewType(false);
        super.performFling(position, velocity, always);
    }

    protected void prepareContent() {
        if (!this.mAnimating) {
            View content = this.mContent;
            if (content.isLayoutRequested()) {
                int handleWidth = this.mHandle.getWidth();
                content.measure(MeasureSpec.makeMeasureSpec(((getRight() - getLeft()) - handleWidth) - this.mTopOffset, 1073741824), MeasureSpec.makeMeasureSpec(getBottom() - getTop(), 1073741824));
                content.layout(this.mTopOffset + handleWidth, 0, (this.mTopOffset + handleWidth) + content.getMeasuredWidth(), content.getMeasuredHeight());
            }
        }
    }

    protected void stopTracking() {
        super.stopTracking();
        this.mContent.setPressed(false);
    }

    protected boolean isTapVelocity(float velocity) {
        return true;
    }

    private void addTouchDelegate(Rect rect) {
        setTouchPadding(isOpened());
        rect.top -= this.mTouchPadding;
        rect.bottom += this.mTouchPadding;
        rect.left -= this.mTouchPadding / 2;
        rect.right += this.mTouchPadding;
    }

    public void setCircleView(boolean set) {
        this.mIsCircleView = set;
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mIconViewList != null) {
            Iterator it = this.mIconViewList.iterator();
            while (it.hasNext()) {
                ((RotateImageView) it.next()).setDegree(degree, animation);
            }
        }
    }

    private Rect getTouchRect(View view) {
        if (view.getVisibility() != 0) {
            return new Rect(0, 0, 0, 0);
        }
        Rect rect = new Rect();
        view.getHitRect(rect);
        if (!isOpened()) {
            int[] realPos = new int[2];
            view.getLocationOnScreen(realPos);
            rect.left = realPos[0];
            rect.right = rect.left + view.getWidth();
        }
        if (getTag(view) == 10) {
            setTouchPadding(isOpened());
            rect.left -= this.mTouchPadding * 2;
            rect.right += this.mTouchPadding / 2;
        }
        addTouchDelegate(rect);
        return rect;
    }

    private int getTag(View view) {
        Object tagObject = view.getTag();
        if (tagObject == null) {
            return -1;
        }
        return ((Integer) tagObject).intValue();
    }

    public boolean makeQuickClipList(int width, ArrayList<QuickClipSharedItem> sharedItem) {
        LinearLayout handleView = (LinearLayout) findViewById(C0088R.id.quick_clip_handle_layout);
        LinearLayout menuView = (LinearLayout) findViewById(C0088R.id.quick_clip_menu);
        MemoryUtils.removeChildViews(handleView);
        MemoryUtils.removeChildViews(menuView);
        this.mIconViewList.clear();
        CamLog.m3d(CameraConstants.TAG, "makeQuickClipFrameMenu");
        if (sharedItem == null || sharedItem.size() == 0 || this.mContext == null) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "QuickClipSharedItems size : " + sharedItem.size());
        int imgSize = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_menu_image.width);
        int imgBGSize = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_menu_image_bg.width);
        for (int i = 0; i < sharedItem.size(); i++) {
            RelativeLayout rl = layoutQuickClipItem(i, (QuickClipSharedItem) sharedItem.get(i), imgSize, imgBGSize);
            if (rl == null) {
                return false;
            }
            LayoutParams param1 = new LayoutParams(imgBGSize, imgBGSize);
            if (i == 0) {
                handleView.setBackground(this.mContext.getDrawable(C0088R.drawable.camera_quick_clip_sns_bg_normal));
                ViewGroup.LayoutParams layoutParam = handleView.getLayoutParams();
                RelativeLayout arrowLayout = layoutHandleArrow();
                layoutParam.width = arrowLayout.getLayoutParams().width + imgBGSize;
                handleView.setLayoutParams(layoutParam);
                handleView.addView(arrowLayout);
                handleView.addView(rl, param1);
            } else if (i == 5) {
                param1.setMarginStart(Utils.getPx(this.mContext, C0088R.dimen.quick_clip_content_more_btn_startMargin));
                param1.setMarginEnd(Utils.getPx(this.mContext, C0088R.dimen.quick_clip_content_padding_end));
                menuView.addView(rl, param1);
            } else {
                Space space = new Space(this.mContext);
                LayoutParams spaceParams = new LayoutParams(0, 0);
                spaceParams.weight = 1.0f;
                menuView.addView(space, spaceParams);
                menuView.addView(rl, param1);
            }
        }
        return true;
    }

    private RelativeLayout layoutQuickClipItem(int tag, QuickClipSharedItem item, int imageSize, int bgSize) {
        if (this.mContext == null) {
            return null;
        }
        RelativeLayout rl = new RelativeLayout(this.mContext);
        rl.setLayoutParams(new RelativeLayout.LayoutParams(bgSize, bgSize));
        rl.setTag(Integer.valueOf(tag));
        rl.setClickable(true);
        rl.setBackground(this.mContext.getDrawable(C0088R.drawable.camera_quick_clip_sns_stroke));
        rl.setOnClickListener(this.mItemClickedListener);
        rl.setGravity(17);
        rl.setAddStatesFromChildren(true);
        RotateImageView iv = new RotateImageView(this.mContext);
        iv.setLayoutParams(new RelativeLayout.LayoutParams(imageSize, imageSize));
        iv.setImageDrawable(item.getAppIcon());
        iv.setContentDescription(item.getLabel());
        iv.setScaleType(ScaleType.FIT_XY);
        this.mIconViewList.add(iv);
        rl.setClipChildren(false);
        rl.addView(iv);
        return rl;
    }

    private RelativeLayout layoutHandleArrow() {
        RelativeLayout rl = new RelativeLayout(this.mContext);
        this.mArrowView = new RotateImageView(this.mContext);
        int arrowSize = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_arrow_size);
        int arrowStartMargin = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_left_arrow_padding_start);
        int arrowEndMargine = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_arrow_padding_end);
        RelativeLayout.LayoutParams rLayoutParams = new RelativeLayout.LayoutParams((arrowSize + arrowStartMargin) + arrowEndMargine, -2);
        this.mArrowView.setPadding(arrowStartMargin, 0, arrowEndMargine, 0);
        rl.setTag(Integer.valueOf(10));
        rl.setClickable(true);
        rl.setOnClickListener(this.mItemClickedListener);
        rl.addView(this.mArrowView);
        rLayoutParams.addRule(9);
        rLayoutParams.addRule(15);
        rl.setLayoutParams(rLayoutParams);
        this.mArrowLayoutView = rl;
        this.mArrowLayoutView.setVisibility(8);
        return rl;
    }

    public void updateHandleArrow(int status) {
        LinearLayout handleView = (LinearLayout) findViewById(C0088R.id.quick_clip_handle_layout);
        View handleViewLayout = null;
        if (this.mArrowView != null && handleView != null) {
            int i = 0;
            while (i < handleView.getChildCount()) {
                if (((Integer) handleView.getChildAt(i).getTag()) != null && ((Integer) handleView.getChildAt(i).getTag()).intValue() == 0) {
                    handleViewLayout = handleView.getChildAt(i);
                    break;
                }
                i++;
            }
            if (handleViewLayout != null) {
                int imgBGSize = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_menu_image_bg.width);
                int arrowStartMargin = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_left_arrow_padding_start);
                int arrowStartMarginClose = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_left_arrow_padding_start_close);
                int arrowEndMargine = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_arrow_padding_end);
                int arrowSize = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_arrow_size);
                LayoutParams lLayoutParams = (LayoutParams) handleViewLayout.getLayoutParams();
                if (status == 100) {
                    lLayoutParams.setMarginStart(0);
                    handleViewLayout.setLayoutParams(lLayoutParams);
                    this.mArrowLayoutView.setVisibility(0);
                    this.mArrowView.setImageDrawable(this.mContext.getDrawable(C0088R.drawable.btn_camera_quick_clip_sns_arrow_right));
                    this.mArrowView.setLayoutParams(new RelativeLayout.LayoutParams((arrowSize + arrowStartMargin) + arrowEndMargine, -2));
                    this.mArrowView.setPadding(arrowStartMargin, 0, arrowEndMargine, 0);
                    this.mArrowView.setContentDescription(this.mContext.getString(C0088R.string.quick_clip_talkback_tray_close));
                } else if (status == 101) {
                    lLayoutParams.setMarginStart(0);
                    handleViewLayout.setLayoutParams(lLayoutParams);
                    this.mArrowLayoutView.setVisibility(0);
                    this.mArrowView.setImageDrawable(this.mContext.getDrawable(C0088R.drawable.btn_camera_quick_clip_sns_arrow_left));
                    this.mArrowView.setLayoutParams(new RelativeLayout.LayoutParams((arrowSize + arrowStartMarginClose) + arrowEndMargine, -2));
                    this.mArrowView.setPadding(arrowStartMarginClose, 0, arrowEndMargine, 0);
                    this.mArrowView.setContentDescription(this.mContext.getString(C0088R.string.quick_clip_talkback_tray_click_open));
                } else if (status == 102) {
                    int circleSize = this.mContext.getDrawable(C0088R.drawable.camera_quick_clip_sns_bg_circle_normal).getIntrinsicWidth();
                    int circleStartMargin = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_handle_circle_startMargin);
                    lLayoutParams.setMarginStart((((circleSize - imgBGSize) / 2) + circleStartMargin) - Utils.getPx(this.mContext, C0088R.dimen.quick_clip_handle_circle_item_startMargin));
                    handleViewLayout.setLayoutParams(lLayoutParams);
                    this.mArrowView.setPadding(0, 0, 0, 0);
                    this.mArrowLayoutView.setVisibility(8);
                }
            }
        }
    }

    public boolean updateAppIcon(ArrayList<QuickClipSharedItem> sharedItem) {
        if (sharedItem == null || this.mIconViewList == null || this.mIconViewList.size() == 0) {
            return false;
        }
        try {
            ArrayList<QuickClipSharedItem> sharedItemList = (ArrayList) sharedItem.clone();
            if (sharedItemList == null || sharedItemList.size() == 0) {
                return false;
            }
            int i = 0;
            Iterator it = sharedItemList.iterator();
            while (it.hasNext()) {
                QuickClipSharedItem item = (QuickClipSharedItem) it.next();
                ImageView iv = i >= this.mIconViewList.size() ? null : (RotateImageView) this.mIconViewList.get(i);
                if (iv != null) {
                    iv.setImageDrawable(item.getAppIcon());
                    iv.setContentDescription(item.getLabel());
                }
                i++;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void unregisterListener() {
        CamLog.m7i(CameraConstants.TAG, "unregisterCallback");
        this.mItemClickedListener = null;
        this.mSetOnTriggerQuickClip = null;
        setOnDrawerOpenListener(null);
        setOnDrawerCloseListener(null);
        setOnDrawerScrollListener(null);
    }

    public void releaseView() {
        LinearLayout handleView = (LinearLayout) findViewById(C0088R.id.quick_clip_handle_layout);
        LinearLayout menuView = (LinearLayout) findViewById(C0088R.id.quick_clip_menu);
        this.mHandler.removeCallbacks(null);
        MemoryUtils.removeChildViews(handleView);
        MemoryUtils.removeChildViews(menuView);
        this.mIconViewList.clear();
        unregisterListener();
        this.mContext = null;
    }

    public void close() {
        this.mTracking = false;
        this.mHandle.setPressed(false);
        this.mContent.setPressed(false);
        super.close();
    }

    public void setTouchPadding(boolean isOn) {
        if (isOn) {
            this.mTouchPadding = Utils.getPx(this.mContext, C0088R.dimen.quick_clip_touch_padding);
        } else {
            this.mTouchPadding = 0;
        }
    }

    public void resetAnimating() {
        this.mAnimating = false;
    }
}
