package com.lge.camera.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.LinkedList;
import java.util.List;

public class EditableGridView extends GridView {
    private int mAniStartPosition = -1;
    private List<Animator> mAnimatorList = null;
    private Context mContext = null;
    private int mDegree = 0;
    private int mDragPosition = -1;
    private ImageView mDragView = null;
    private boolean mIsAnimating = false;
    private boolean mIsDragging = false;
    private boolean mIsEditMode = false;
    private EditableGridViewListener mListener = null;
    private int mLongClickPosition = -1;
    private int mMoveRawX = 0;
    private int mMoveRawY = 0;
    private int mMoveX = 0;
    private int mMoveY = 0;
    private OnItemLongClickListener mOnItemLongClickListener = null;
    private WindowManager mWindowManager = null;
    private LayoutParams mWindowParams;

    /* renamed from: com.lge.camera.components.EditableGridView$1 */
    class C05391 implements OnItemLongClickListener {
        C05391() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (!EditableGridView.this.mIsEditMode) {
                EditableGridView.this.mIsEditMode = true;
                if (EditableGridView.this.mListener != null) {
                    EditableGridView.this.mListener.setEditMode(true);
                }
            }
            if (EditableGridView.this.mIsDragging) {
                return false;
            }
            View item = EditableGridView.this.getChildAt(position - EditableGridView.this.getFirstVisiblePosition());
            if (item == null) {
                return false;
            }
            item.setDrawingCacheEnabled(true);
            item.buildDrawingCache(true);
            Bitmap bmpCache = item.getDrawingCache();
            if (bmpCache == null) {
                return false;
            }
            EditableGridView.this.startDrag(Bitmap.createBitmap(bmpCache));
            item.destroyDrawingCache();
            CamLog.m3d(CameraConstants.TAG, "[mode] long click : " + position);
            EditableGridView.this.mLongClickPosition = position;
            EditableGridView.this.mDragPosition = position;
            if (EditableGridView.this.mListener != null) {
                EditableGridView.this.mListener.startDragging(position);
            }
            return true;
        }
    }

    /* renamed from: com.lge.camera.components.EditableGridView$2 */
    class C05402 extends AnimatorListenerAdapter {
        C05402() {
        }

        public void onAnimationStart(Animator animation) {
            EditableGridView.this.mIsAnimating = true;
            CamLog.m3d(CameraConstants.TAG, "[mode] onAnimationStart  : " + EditableGridView.this.mAnimatorList.size());
        }

        public void onAnimationEnd(Animator animation) {
            EditableGridView.this.stopAnimation();
            if (EditableGridView.this.mLongClickPosition == -1) {
                EditableGridView.this.mAniStartPosition = -1;
            } else {
                EditableGridView.this.mAniStartPosition = EditableGridView.this.mDragPosition;
            }
            CamLog.m3d(CameraConstants.TAG, "[mode] onAnimationEnd  : " + EditableGridView.this.mAniStartPosition);
        }
    }

    public interface EditableGridViewListener {
        void drag(int i);

        void setEditMode(boolean z);

        void startDragging(int i);

        void stopDragging();
    }

    public void setListener(EditableGridViewListener listener) {
        this.mListener = listener;
    }

    public EditableGridView(Context context) {
        super(context);
        setup(context);
    }

    public EditableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    private void setup(Context context) {
        this.mContext = context;
        if (this.mAnimatorList == null) {
            this.mAnimatorList = new LinkedList();
        }
        setWindowManager();
        setLongClickListener();
    }

    public void setEditMode(boolean isEditMode) {
        this.mIsEditMode = isEditMode;
        if (!this.mIsEditMode && this.mIsDragging) {
            stopDragging();
        }
    }

    private void setWindowManager() {
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        if (this.mWindowParams != null) {
            this.mWindowParams = null;
        }
        this.mWindowParams = new LayoutParams();
        this.mWindowParams.height = -2;
        this.mWindowParams.width = -2;
        this.mWindowParams.flags = 920;
        this.mWindowParams.format = -3;
        this.mWindowParams.windowAnimations = 0;
        this.mWindowParams.gravity = 8388659;
    }

    private void setLongClickListener() {
        if (this.mOnItemLongClickListener == null) {
            this.mOnItemLongClickListener = new C05391();
            setOnItemLongClickListener(this.mOnItemLongClickListener);
        }
    }

    public void onDestroy() {
        if (this.mIsDragging) {
            stopDragging();
        }
        this.mDragView = null;
        this.mContext = null;
        if (this.mAnimatorList != null) {
            this.mAnimatorList.clear();
            this.mAnimatorList = null;
        }
        this.mWindowManager = null;
        this.mWindowParams = null;
        this.mOnItemLongClickListener = null;
    }

    public void setDegree(int degree) {
        this.mDegree = degree;
        if (this.mIsDragging) {
            stopDragging();
        }
    }

    private void startDrag(Bitmap bitmap) {
        if (this.mIsEditMode) {
            this.mIsDragging = true;
            ImageView view = new ImageView(this.mContext);
            view.setPaddingRelative(0, 0, 0, 0);
            view.setImageBitmap(bitmap);
            view.setRotation((float) this.mDegree);
            AnimationSet animSet = new AnimationSet(true);
            ScaleAnimation scale = new ScaleAnimation(0.667f, 1.0f, 0.667f, 1.0f, (float) ((getColumnWidth() * 3) / 4), (float) ((getColumnWidth() * 3) / 4));
            scale.setDuration(150);
            AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.5f);
            alpha.setDuration(150);
            animSet.addAnimation(scale);
            animSet.addAnimation(alpha);
            animSet.setFillEnabled(true);
            animSet.setFillAfter(true);
            this.mWindowManager.addView(view, this.mWindowParams);
            view.clearAnimation();
            view.startAnimation(animSet);
            view.setRotation((float) this.mDegree);
            this.mDragView = view;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        switch (event.getAction()) {
            case 0:
            case 2:
                if (this.mListener != null) {
                    this.mMoveX = (int) event.getX();
                    this.mMoveY = (int) event.getY();
                    this.mMoveRawX = (int) event.getRawX();
                    this.mMoveRawY = (int) event.getRawY();
                    this.mWindowParams.x = this.mMoveRawX - (getColumnWidth() / 2);
                    this.mWindowParams.y = this.mMoveRawY - (getColumnWidth() / 2);
                    this.mWindowParams.screenOrientation = 90;
                    if (this.mIsEditMode && this.mIsDragging) {
                        if (this.mDragView != null) {
                            rotateView(this.mDragView);
                            this.mWindowManager.updateViewLayout(this.mDragView, this.mWindowParams);
                        }
                        int index = pointToPosition(this.mMoveX, this.mMoveY);
                        if (!(index == -1 || this.mDragPosition == index)) {
                            this.mListener.drag(index);
                            if (this.mAniStartPosition != index) {
                                startDragAtPosition(index);
                                setAniSet();
                            }
                            this.mDragPosition = index;
                            break;
                        }
                    }
                }
                return false;
            case 1:
                stopDragging();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void startDragAtPosition(int position) {
        if (this.mLongClickPosition != -1 && this.mDragPosition != -1) {
            if (this.mAniStartPosition == -1) {
                this.mAniStartPosition = this.mLongClickPosition;
            }
            startDragAnimation(this.mAniStartPosition, position);
        }
    }

    private void startDragAnimation(int start, int end) {
        AnimateItem item = new AnimateItem();
        int i = start;
        while (i != end) {
            int index = i;
            long id = getAdapter().getItemId(i);
            item.setIndex(index);
            item.setId(id);
            int columnNum = getNumColumns();
            int rest = index % columnNum;
            int typeX = -1;
            int typeY = -1;
            if (start < end) {
                if (rest == columnNum - 1) {
                    typeX = 2;
                    typeY = 4;
                } else {
                    typeX = 1;
                }
            } else if (start > end) {
                if (rest == 0) {
                    typeX = 1;
                    typeY = 8;
                } else {
                    typeX = 2;
                }
            }
            item.setAniTypeX(typeX);
            item.setAniTypeY(typeY);
            animateReorder(item);
            i = start < end ? i + 1 : i - 1;
        }
    }

    private void animateReorder(AnimateItem item) {
        if (this.mAnimatorList != null && item != null) {
            float startX;
            long id = item.getId();
            int typeX = item.getAniTypeX();
            int typeY = item.getAniTypeY();
            View view = getViewForId(id);
            int imageWidth = view == null ? getColumnWidth() : view.getWidth();
            int imageHeigth = view == null ? getColumnWidth() : view.getHeight();
            float startY = 0.0f;
            switch (typeY) {
                case 4:
                    startY = (float) imageHeigth;
                    if (typeX != 1) {
                        startX = (float) ((-imageWidth) * 2);
                        break;
                    } else {
                        startX = (float) (imageWidth * 2);
                        break;
                    }
                case 8:
                    startY = (float) (-imageHeigth);
                    if (typeX != 1) {
                        startX = (float) ((-imageWidth) * 2);
                        break;
                    } else {
                        startX = (float) (imageWidth * 2);
                        break;
                    }
                default:
                    if (typeX != 1) {
                        startX = (float) (-imageWidth);
                        break;
                    } else {
                        startX = (float) imageWidth;
                        break;
                    }
            }
            ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX", new float[]{startX, 0.0f});
            ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY", new float[]{startY, 0.0f});
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(new Animator[]{animX, animY});
            this.mAnimatorList.add(animSetXY);
        }
    }

    private void setAniSet() {
        if (this.mAnimatorList != null && !this.mAnimatorList.isEmpty() && !this.mIsAnimating) {
            AnimatorSet resultSet = new AnimatorSet();
            resultSet.playTogether(this.mAnimatorList);
            resultSet.setDuration(150);
            resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
            resultSet.addListener(new C05402());
            resultSet.start();
        }
    }

    private void stopAnimation() {
        this.mIsAnimating = false;
        if (this.mAnimatorList != null) {
            this.mAnimatorList.clear();
        }
    }

    public void deleteAnimation(int deletedIndex) {
        startDragAnimation(deletedIndex, getChildCount() - 1);
        setAniSet();
    }

    private int getPositionForID(long itemId) {
        View v = getViewForId(itemId);
        if (v == null) {
            return -1;
        }
        return getPositionForView(v);
    }

    private View getViewForId(long itemId) {
        int firstVisiblePosition = getFirstVisiblePosition();
        ListAdapter adapter = getAdapter();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (adapter.getItemId(firstVisiblePosition + i) == itemId) {
                return v;
            }
        }
        return null;
    }

    private void rotateView(View view) {
        if (view != null) {
            switch (this.mDegree) {
                case 90:
                    view.setRotation(270.0f);
                    return;
                case 270:
                    view.setRotation(90.0f);
                    return;
                default:
                    view.setRotation((float) this.mDegree);
                    return;
            }
        }
    }

    private void stopDragging() {
        if (this.mIsDragging) {
            if (this.mListener != null) {
                this.mListener.stopDragging();
            }
            if (this.mDragView != null) {
                this.mDragView.setVisibility(8);
                this.mWindowManager.removeView(this.mDragView);
                this.mDragView.setImageDrawable(null);
                this.mDragView = null;
            }
            this.mIsDragging = false;
            this.mLongClickPosition = -1;
            this.mAniStartPosition = -1;
            stopAnimation();
        }
    }

    private int myPointToPosition(int x, int y) {
        Rect frame = new Rect();
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child != null) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return getFirstVisiblePosition() + i;
                }
            }
        }
        return -1;
    }
}
