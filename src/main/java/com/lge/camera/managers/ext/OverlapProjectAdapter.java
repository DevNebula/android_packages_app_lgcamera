package com.lge.camera.managers.ext;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Process;
import android.provider.MediaStore.Images.Media;
import android.support.p000v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.database.OverlapProjectDb;
import com.lge.camera.database.OverlapProjectDefaults;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SquareUtil;
import com.lge.camera.util.Utils;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class OverlapProjectAdapter extends BaseAdapter {
    private static final int COLOR_ADD = -15066598;
    private static final int MAX_DISPLAY_NUM_CNT = 999;
    public static final int MAX_SAMPLE_COUNT = 12;
    private OverlapProjectAdapterInterface mAdapterInterface;
    private Object mAddBitmapLock = new Object();
    private AddBitmapToMemoryThread mAddBitmapToMemoryThread;
    private HashMap<String, String> mBadgeNumber = null;
    private HashMap<String, Bitmap> mBitmapCache = null;
    private Context mContext;
    private int mCount = 0;
    private Bitmap mPlaceHolderBitmap;

    private class AddBitmapToMemoryThread extends Thread {
        private AddBitmapToMemoryThread() {
        }

        /* synthetic */ AddBitmapToMemoryThread(OverlapProjectAdapter x0, C12421 x1) {
            this();
        }

        public void run() {
            Process.setThreadPriority(10);
            synchronized (OverlapProjectAdapter.this.mAddBitmapLock) {
                CamLog.m3d(CameraConstants.TAG, "[Cell]AddBitmapToMemoryThread START");
                for (int i = 0; i < OverlapProjectAdapter.this.mAdapterInterface.onLoadedSamples().size(); i++) {
                    String resIdStr;
                    OverlapProjectDb currProject = (OverlapProjectDb) OverlapProjectAdapter.this.mAdapterInterface.onLoadedSamples().get(i);
                    int preset = currProject.getPreset();
                    if (preset != -1) {
                        resIdStr = String.valueOf(OverlapProjectDefaults.getDefaultSample(preset));
                    } else {
                        resIdStr = SquareUtil.getSampleFilesDir(OverlapProjectAdapter.this.mContext) + currProject.getSamplePath();
                    }
                    if (OverlapProjectAdapter.this.getBitmapFromMemCache(resIdStr) == null) {
                        int height = SquareUtil.getHeight(OverlapProjectAdapter.this.mContext) / 2;
                        OverlapProjectAdapter.this.addBitmapToMemoryCache(resIdStr, SquareUtil.decodeSampledBitmap(OverlapProjectAdapter.this.mContext, resIdStr, height, height));
                    }
                }
                CamLog.m3d(CameraConstants.TAG, "[Cell]AddBitmapToMemoryThread END");
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            this.bitmapWorkerTaskReference = new WeakReference(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return (BitmapWorkerTask) this.bitmapWorkerTaskReference.get();
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private String data = "";
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            this.imageViewReference = new WeakReference(imageView);
        }

        protected Bitmap doInBackground(String... params) {
            this.data = params[0];
            int height = SquareUtil.getHeight(OverlapProjectAdapter.this.mContext) / 2;
            Bitmap bitmap = SquareUtil.decodeSampledBitmap(OverlapProjectAdapter.this.mContext, this.data, height, height);
            OverlapProjectAdapter.this.addBitmapToMemoryCache(params[0], bitmap);
            return bitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {
            if (this.imageViewReference != null && bitmap != null) {
                ImageView imageView = (ImageView) this.imageViewReference.get();
                Drawable drawable = imageView.getDrawable();
                if (imageView != null && (drawable instanceof AsyncDrawable)) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    class CaptureCountWorkerTask extends AsyncTask<String, Void, String> {
        private String data = "";
        private final WeakReference<TextView> textViewReference;

        public CaptureCountWorkerTask(TextView textView) {
            this.textViewReference = new WeakReference(textView);
        }

        protected String doInBackground(String... params) {
            this.data = params[0];
            String captureCntStr = "";
            int capturedCnt = OverlapProjectAdapter.this.getPictureCnt(this.data);
            if (capturedCnt > 0) {
                captureCntStr = String.format("%d", new Object[]{Integer.valueOf(capturedCnt)});
                if (capturedCnt > OverlapProjectAdapter.MAX_DISPLAY_NUM_CNT) {
                    captureCntStr = String.format("%d", new Object[]{Integer.valueOf(OverlapProjectAdapter.MAX_DISPLAY_NUM_CNT)}) + "+";
                }
            } else {
                captureCntStr = String.format("%d", new Object[]{Integer.valueOf(0)});
            }
            if (OverlapProjectAdapter.this.mBadgeNumber != null) {
                OverlapProjectAdapter.this.mBadgeNumber.put(this.data, captureCntStr);
            }
            return captureCntStr;
        }

        protected void onPostExecute(String captureCntStr) {
            if (this.textViewReference != null && captureCntStr != null) {
                TextView textView = (TextView) this.textViewReference.get();
                if (textView != null && !textView.isEnabled()) {
                    textView.setEnabled(true);
                    textView.setText(captureCntStr);
                }
            }
        }
    }

    public class ProjectItemHolder {
        ImageView mAddView;
        ImageView mCntImageView;
        RelativeLayout mCntLayout;
        ImageView mProjectView;
        RelativeLayout mProjectViewLayout;
    }

    public OverlapProjectAdapter(Context c) {
        this.mContext = c;
        this.mPlaceHolderBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), C0088R.drawable.overlap_place_holder);
    }

    public void initAdapter() {
        this.mBitmapCache = new HashMap();
        this.mBadgeNumber = new HashMap();
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (this.mBitmapCache != null && key != null && bitmap != null && !this.mBitmapCache.containsKey(key)) {
            this.mBitmapCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        if (this.mBitmapCache == null || key == null) {
            return null;
        }
        return (Bitmap) this.mBitmapCache.get(key);
    }

    public void removeBitmapFromMemCache(String key) {
        if (this.mBitmapCache != null) {
            Bitmap bitmap = (Bitmap) this.mBitmapCache.remove(key);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    public void removeBadgeNumber(String projectId) {
        if (this.mBadgeNumber != null) {
            this.mBadgeNumber.remove(projectId);
        }
    }

    public void clearCache() {
        if (this.mBitmapCache != null) {
            this.mBitmapCache.clear();
            this.mBitmapCache = null;
        }
        if (this.mBadgeNumber != null) {
            this.mBadgeNumber.clear();
            this.mBadgeNumber = null;
        }
    }

    public void setAdapterInterface(OverlapProjectAdapterInterface adapter) {
        this.mAdapterInterface = adapter;
    }

    public int getCount() {
        int i = 12;
        int samplesSize = this.mAdapterInterface.onLoadedSamples().size();
        if (samplesSize < 12) {
            i = samplesSize + 1;
        }
        this.mCount = i;
        return this.mCount;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.mContext == null) {
            return convertView;
        }
        ProjectItemHolder holder;
        View projectItemView = convertView;
        if (projectItemView == null) {
            projectItemView = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(C0088R.layout.overlap_project_item_view, null);
            if (projectItemView == null) {
                CamLog.m11w(CameraConstants.TAG, "[Cell]projectItemView inflate error");
                return null;
            }
            holder = new ProjectItemHolder();
            holder.mProjectViewLayout = (RelativeLayout) projectItemView.findViewById(C0088R.id.project_view_layout);
            holder.mProjectView = (ImageView) projectItemView.findViewById(C0088R.id.project_view);
            holder.mAddView = (ImageView) projectItemView.findViewById(C0088R.id.add_view);
            holder.mCntLayout = (RelativeLayout) projectItemView.findViewById(C0088R.id.captured_cnt_layout);
            holder.mCntImageView = (ImageView) projectItemView.findViewById(C0088R.id.captured_cnt);
            holder.mProjectViewLayout.setLayoutParams(new LayoutParams(-1, (int) (((float) Utils.getLCDsize(this.mContext, true)[1]) / 2.0f)));
            projectItemView.setTag(holder);
        } else {
            holder = (ProjectItemHolder) projectItemView.getTag();
        }
        setHolderView(projectItemView, holder, position);
        return projectItemView;
    }

    public void addBitmapToMemoryCacheInThread() {
        this.mAddBitmapToMemoryThread = new AddBitmapToMemoryThread(this, null);
        this.mAddBitmapToMemoryThread.start();
    }

    private void setHolderView(View projectItemView, ProjectItemHolder holder, int position) {
        if (position + 1 != this.mCount || this.mAdapterInterface.onLoadedSamples().size() == 12) {
            OverlapProjectDb currProject = (OverlapProjectDb) this.mAdapterInterface.onLoadedSamples().get(position);
            int preset = currProject.getPreset();
            if (preset != -1) {
                loadBitmap(String.valueOf(OverlapProjectDefaults.getDefaultSample(preset)), holder.mProjectView);
            } else {
                loadBitmap(SquareUtil.getSampleFilesDir(this.mContext) + currProject.getSamplePath(), holder.mProjectView);
            }
            holder.mProjectView.setVisibility(0);
            LayoutParams flp = (LayoutParams) holder.mCntLayout.getLayoutParams();
            if (position % 2 == 0) {
                flp.removeRule(20);
                flp.addRule(21);
                holder.mCntLayout.setGravity(GravityCompat.END);
            } else {
                flp.removeRule(21);
                flp.addRule(20);
                holder.mCntLayout.setGravity(GravityCompat.START);
            }
            holder.mCntLayout.setLayoutParams(flp);
            holder.mCntImageView.setImageResource(this.mAdapterInterface.getCurrProjectIndex() == position ? C0088R.drawable.btn_overlap_project_badge_number_sel : C0088R.drawable.btn_overlap_project_badge_number);
            holder.mCntLayout.setVisibility(0);
            holder.mAddView.setVisibility(8);
            projectItemView.setContentDescription(this.mContext.getResources().getString(C0088R.string.sp_Project_NORMAL));
            final int selectedPos = position;
            holder.mCntLayout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    OverlapProjectAdapter.this.mAdapterInterface.onProjectSelected(selectedPos, true, true);
                }
            });
            return;
        }
        setAddViewHolder(projectItemView, holder, position);
    }

    private void setAddViewHolder(View projectItemView, ProjectItemHolder holder, int position) {
        if (projectItemView != null && holder != null) {
            LayoutParams flp = (LayoutParams) holder.mAddView.getLayoutParams();
            if (position % 2 == 0) {
                flp.removeRule(20);
                flp.addRule(21);
            } else {
                flp.removeRule(21);
                flp.addRule(20);
            }
            holder.mAddView.setLayoutParams(flp);
            holder.mProjectViewLayout.setBackgroundColor(COLOR_ADD);
            holder.mProjectView.setVisibility(8);
            holder.mCntLayout.setVisibility(8);
            holder.mAddView.setVisibility(0);
            holder.mAddView.setImageResource(C0088R.drawable.btn_overlap_project_add);
            projectItemView.setContentDescription(this.mContext.getResources().getString(C0088R.string.cell_guide_add_project));
        }
    }

    public int getPictureCnt(String projectId) {
        int result = 0;
        Cursor imageQueryCursor = null;
        try {
            imageQueryCursor = this.mContext.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{"_id", CameraConstants.ORIENTATION, "datetaken", "_display_name"}, "camera_mode='4' AND project_id='" + projectId + "'", null, "datetaken DESC,_id DESC");
            if (imageQueryCursor == null) {
                CamLog.m3d(CameraConstants.TAG, "[Cell]imageQueryCursor null");
                if (imageQueryCursor != null) {
                    imageQueryCursor.close();
                }
                return 0;
            }
            result = imageQueryCursor.getCount();
            if (imageQueryCursor != null) {
                imageQueryCursor.close();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            if (imageQueryCursor != null) {
                imageQueryCursor.close();
            }
        } catch (Throwable th) {
            if (imageQueryCursor != null) {
                imageQueryCursor.close();
            }
        }
    }

    public void loadBitmap(String resId, ImageView imageView) {
        Bitmap bitmap = getBitmapFromMemCache(resId);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else if (cancelPotentialWork(resId, imageView)) {
            BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            imageView.setImageDrawable(new AsyncDrawable(this.mContext.getResources(), this.mPlaceHolderBitmap, task));
            task.execute(new String[]{resId});
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask == null) {
            return true;
        }
        if (bitmapWorkerTask.data.equals(data)) {
            return false;
        }
        bitmapWorkerTask.cancel(true);
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                return ((AsyncDrawable) drawable).getBitmapWorkerTask();
            }
        }
        return null;
    }

    public void loadTextView(String projectId, TextView textView) {
        String textKey = projectId;
        String badge = null;
        if (this.mBadgeNumber != null) {
            badge = (String) this.mBadgeNumber.get(textKey);
        }
        if (badge == null || badge.length() == 0) {
            badge = String.format("%d", new Object[]{Integer.valueOf(0)});
        }
        textView.setText(badge);
        if (cancelPotentialWork(textKey, textView)) {
            CaptureCountWorkerTask task = new CaptureCountWorkerTask(textView);
            textView.setEnabled(false);
            textView.setTag(task);
            task.execute(new String[]{textKey});
        }
    }

    public static boolean cancelPotentialWork(String data, TextView textView) {
        CaptureCountWorkerTask captureCountWorkerTask = getCaptureCountWorkerTask(textView);
        if (captureCountWorkerTask == null) {
            return true;
        }
        if (captureCountWorkerTask.data.equals(data)) {
            return false;
        }
        captureCountWorkerTask.cancel(true);
        return true;
    }

    private static CaptureCountWorkerTask getCaptureCountWorkerTask(TextView textView) {
        if (textView == null || textView.isEnabled()) {
            return null;
        }
        return (CaptureCountWorkerTask) textView.getTag();
    }

    public boolean startSlideShow(Bitmap bitmap, View view, int index) {
        if (this.mBitmapCache == null || view == null) {
            return false;
        }
        ImageView imageView = (ImageView) view.findViewById(C0088R.id.project_view);
        if (imageView == null) {
            return false;
        }
        boolean isNotLoaded = imageView.getDrawable() instanceof AsyncDrawable;
        if (!isNotLoaded) {
            CamLog.m3d(CameraConstants.TAG, "[Cell]startSlideShow " + index + " view " + imageView);
            imageView.setImageBitmap(bitmap);
        }
        if (isNotLoaded) {
            return false;
        }
        return true;
    }

    public void stopSlideShow(View view, int index) {
        if (this.mBitmapCache != null) {
            CamLog.m3d(CameraConstants.TAG, "[Cell]stopSlideShow " + index + " view " + view);
            if (index < this.mAdapterInterface.onLoadedSamples().size() && view != null) {
                ImageView imageView = (ImageView) view.findViewById(C0088R.id.project_view);
                if (imageView != null) {
                    OverlapProjectDb currProject = (OverlapProjectDb) this.mAdapterInterface.onLoadedSamples().get(index);
                    int preset = currProject.getPreset();
                    if (preset != -1) {
                        loadBitmap(String.valueOf(OverlapProjectDefaults.getDefaultSample(preset)), imageView);
                        return;
                    }
                    loadBitmap(SquareUtil.getSampleFilesDir(this.mContext) + currProject.getSamplePath(), imageView);
                }
            }
        }
    }
}
