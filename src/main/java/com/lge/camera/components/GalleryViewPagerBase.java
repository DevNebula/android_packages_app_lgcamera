package com.lge.camera.components;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.p000v4.util.ArrayMap;
import android.support.p000v4.view.PagerAdapter;
import android.support.p000v4.view.ViewPager;
import android.support.p000v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import com.lge.camera.managers.GalleryPagerAdapter;
import com.lge.camera.util.Utils;
import java.util.Map;

public class GalleryViewPagerBase extends ViewPager {
    private RevalidateIndicesOnContentChange mDataSetObserver;
    private final Map<OnPageChangeListener, ReverseOnPageChangeListener> mReverseOnPageChangeListeners = new ArrayMap(1);
    private boolean mSuppressOnPageChangeListeners;

    private class RevalidateIndicesOnContentChange extends DataSetObserver {
        private GalleryPagerAdapter mGalleryPagerAdapter;

        private RevalidateIndicesOnContentChange(PagerAdapter adapter) {
            this.mGalleryPagerAdapter = (GalleryPagerAdapter) adapter;
        }

        public void onChanged() {
            super.onChanged();
            revalidateIndices();
        }

        public void revalidateIndices() {
            if (Utils.isRTLLanguage()) {
                int newCount = this.mGalleryPagerAdapter.getCount();
                int lastCount = this.mGalleryPagerAdapter.getLastCount();
                if (newCount != lastCount) {
                    GalleryViewPagerBase.this.setCurrentItemWithoutNotification(Math.max(0, lastCount - 1));
                    this.mGalleryPagerAdapter.setLastCount(newCount);
                }
            }
        }
    }

    private class ReverseOnPageChangeListener implements OnPageChangeListener {
        private final OnPageChangeListener mOriginal;
        private int mPagerPosition;

        private ReverseOnPageChangeListener(OnPageChangeListener original) {
            this.mPagerPosition = -1;
            this.mOriginal = original;
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (!GalleryViewPagerBase.this.mSuppressOnPageChangeListeners) {
                if (positionOffset == 0.0f && positionOffsetPixels == 0) {
                    this.mPagerPosition = reverse(position);
                } else {
                    this.mPagerPosition = reverse(position + 1);
                }
                OnPageChangeListener onPageChangeListener = this.mOriginal;
                int i = this.mPagerPosition;
                if (positionOffset > 0.0f) {
                    positionOffset = 1.0f - positionOffset;
                }
                onPageChangeListener.onPageScrolled(i, positionOffset, positionOffsetPixels);
            }
        }

        public void onPageSelected(int position) {
            if (!GalleryViewPagerBase.this.mSuppressOnPageChangeListeners) {
                this.mOriginal.onPageSelected(reverse(position));
            }
        }

        public void onPageScrollStateChanged(int state) {
            if (!GalleryViewPagerBase.this.mSuppressOnPageChangeListeners) {
                this.mOriginal.onPageScrollStateChanged(state);
            }
        }

        private int reverse(int position) {
            PagerAdapter adapter = GalleryViewPagerBase.this.getAdapter();
            if (adapter == null) {
                return 0;
            }
            int convertPosition = (adapter.getCount() - position) - 1;
            if (convertPosition < 0) {
                return 0;
            }
            return convertPosition;
        }
    }

    public GalleryViewPagerBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GalleryViewPagerBase(Context context) {
        super(context);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerRtlDataSetObserver(super.getAdapter());
    }

    protected void onDetachedFromWindow() {
        unregisterRtlDataSetObserver();
        super.onDetachedFromWindow();
    }

    private void registerRtlDataSetObserver(PagerAdapter adapter) {
        if (adapter != null && this.mDataSetObserver == null) {
            this.mDataSetObserver = new RevalidateIndicesOnContentChange(adapter);
            adapter.registerDataSetObserver(this.mDataSetObserver);
            adapter.notifyDataSetChanged();
        }
    }

    private void unregisterRtlDataSetObserver() {
        PagerAdapter adapter = super.getAdapter();
        if (adapter != null && this.mDataSetObserver != null) {
            adapter.unregisterDataSetObserver(this.mDataSetObserver);
            this.mDataSetObserver = null;
        }
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(convert(item), smoothScroll);
    }

    public int getCurrentItem() {
        return convert(super.getCurrentItem());
    }

    public void setCurrentItem(int item) {
        super.setCurrentItem(convert(item));
    }

    private int convert(int position) {
        if (position < 0 || !Utils.isRTLLanguage()) {
            return position;
        }
        if (getAdapter() == null) {
            return 0;
        }
        int convertPosition = (getAdapter().getCount() - position) - 1;
        if (convertPosition < 0) {
            return 0;
        }
        return convertPosition;
    }

    public void setAdapter(PagerAdapter adapter) {
        boolean rtlReady;
        if (adapter == null || !Utils.isRTLLanguage()) {
            rtlReady = false;
        } else {
            rtlReady = true;
        }
        if (rtlReady) {
            unregisterRtlDataSetObserver();
            registerRtlDataSetObserver(adapter);
        }
        super.setAdapter(adapter);
        if (rtlReady) {
            setCurrentItemWithoutNotification(0);
        }
    }

    public void fakeDragBy(float xOffset) {
        if (!Utils.isRTLLanguage()) {
            xOffset = -xOffset;
        }
        super.fakeDragBy(xOffset);
    }

    private void setCurrentItemWithoutNotification(int index) {
        this.mSuppressOnPageChangeListeners = true;
        setCurrentItem(index, false);
        this.mSuppressOnPageChangeListeners = false;
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        if (Utils.isRTLLanguage()) {
            if (listener == null) {
                this.mReverseOnPageChangeListeners.clear();
            } else {
                ReverseOnPageChangeListener reverseListener = new ReverseOnPageChangeListener(listener);
                this.mReverseOnPageChangeListeners.put(listener, reverseListener);
                Object listener2 = reverseListener;
            }
        }
        super.setOnPageChangeListener(listener2);
    }
}
