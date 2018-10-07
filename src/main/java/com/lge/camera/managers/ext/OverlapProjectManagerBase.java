package com.lge.camera.managers.ext;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.database.OverlapProjectDb;
import com.lge.camera.database.OverlapProjectDbAdapter;
import com.lge.camera.database.OverlapProjectDefaults;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SquareUtil;
import com.lge.camera.util.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class OverlapProjectManagerBase extends ManagerInterfaceImpl implements OverlapProjectAdapterInterface {
    private static final int SLIDESHOW_IMG_H = 720;
    private static final int SLIDESHOW_IMG_W = 720;
    private static final String SLIDESHOW_NUM = "6";
    private static final int SLIDESHOW_PLAY_CNT = 6;
    private static final int SLIDESHOW_PLAY_TIME = 3000;
    private static final int SLIDESHOW_TIMER = 500;
    private AddSlideShowBitmapThread mAddSlideShowBitmapThread;
    private int mCount = 0;
    private int mCurrIndexOfSlideShow = 0;
    private int mCurrProjectIndex = 0;
    protected GridView mGridView;
    protected OverlapProjectManagerInterface mListener;
    private int mLongClickIndex = 0;
    protected RelativeLayout mOverlapProjectLayout;
    protected RotateLayout mOverlapProjectRotateLayout;
    private int mPlayedCntOfSlideShow = 0;
    private ArrayList<OverlapProjectDb> mProjectArray = new ArrayList();
    protected OverlapProjectDbAdapter mProjectDb = new OverlapProjectDbAdapter(this.mGet.getAppContext());
    protected OverlapProjectAdapter mProjectImgAdapter = new OverlapProjectAdapter(this.mGet.getAppContext());
    private int mProjectSlideIndex = 0;
    protected ArrayList<Bitmap> mSlideShowBitmapList = new ArrayList();
    private Object mSlideShowLock = new Object();
    protected HandlerRunnable mSlideShowRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (OverlapProjectManagerBase.this.mSlideShowBitmapList.size() != 0 && OverlapProjectManagerBase.this.mListener.getOverlapCaptureMode() != 1 && OverlapProjectManagerBase.this.mOverlapProjectLayout != null) {
                if (OverlapProjectManagerBase.this.mPlayedCntOfSlideShow == 6) {
                    OverlapProjectManagerBase.this.removeSlideShow();
                    return;
                }
                if (OverlapProjectManagerBase.this.mCurrIndexOfSlideShow + 1 == OverlapProjectManagerBase.this.mSlideShowBitmapList.size()) {
                    OverlapProjectManagerBase.this.mCurrIndexOfSlideShow = 0;
                } else {
                    OverlapProjectManagerBase.this.mCurrIndexOfSlideShow = OverlapProjectManagerBase.this.mCurrIndexOfSlideShow + 1;
                }
                CamLog.m3d(CameraConstants.TAG, "[Cell] mCurrIndexOfSlideShow " + OverlapProjectManagerBase.this.mCurrIndexOfSlideShow);
                if (OverlapProjectManagerBase.this.mGridView.getFirstVisiblePosition() <= OverlapProjectManagerBase.this.mCurrProjectIndex && OverlapProjectManagerBase.this.mGridView.getLastVisiblePosition() >= OverlapProjectManagerBase.this.mCurrProjectIndex) {
                    try {
                        Bitmap bmp = (Bitmap) OverlapProjectManagerBase.this.mSlideShowBitmapList.get(OverlapProjectManagerBase.this.mCurrIndexOfSlideShow);
                        if (bmp != null) {
                            OverlapProjectManagerBase.this.mProjectSlideIndex = OverlapProjectManagerBase.this.mCurrProjectIndex;
                            OverlapProjectManagerBase.this.mProjectImgAdapter.startSlideShow(bmp, OverlapProjectManagerBase.this.mGridView.getChildAt(OverlapProjectManagerBase.this.mProjectSlideIndex - OverlapProjectManagerBase.this.mGridView.getFirstVisiblePosition()), OverlapProjectManagerBase.this.mProjectSlideIndex);
                        } else {
                            return;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        CamLog.m3d(CameraConstants.TAG, "[Cell] SlideShow IndexOutOfBoundsException ");
                        return;
                    }
                }
                OverlapProjectManagerBase.this.mPlayedCntOfSlideShow = OverlapProjectManagerBase.this.mPlayedCntOfSlideShow + 1;
                OverlapProjectManagerBase.this.mGet.postOnUiThread(this, 500);
            }
        }
    };
    protected View mTouchBlockCoverView;

    /* renamed from: com.lge.camera.managers.ext.OverlapProjectManagerBase$1 */
    class C12431 implements OnItemClickListener {
        C12431() {
        }

        public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
            OverlapProjectManagerBase.this.mListener.onProjectSelected(position, false, true);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.OverlapProjectManagerBase$2 */
    class C12442 implements OnItemLongClickListener {
        C12442() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
            if (!OverlapProjectManagerBase.this.mListener.isProjectSelectable() || position == OverlapProjectManagerBase.this.mProjectArray.size()) {
                return false;
            }
            OverlapProjectManagerBase.this.mGet.showDialog(142);
            OverlapProjectManagerBase.this.mLongClickIndex = position;
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.OverlapProjectManagerBase$4 */
    class C12464 implements OnTouchListener {
        C12464() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            return true;
        }
    }

    private class AddSlideShowBitmapThread extends Thread {
        private AddSlideShowBitmapThread() {
        }

        /* synthetic */ AddSlideShowBitmapThread(OverlapProjectManagerBase x0, C12431 x1) {
            this();
        }

        /* JADX WARNING: Missing block: B:31:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:32:?, code:
            return;
     */
        public void run() {
            /*
            r14 = this;
            r1 = 0;
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;
            r10 = r9.mSlideShowLock;
            monitor-enter(r10);
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r9 = r9.mOverlapProjectLayout;	 Catch:{ all -> 0x00f4 }
            if (r9 == 0) goto L_0x0014;
        L_0x000e:
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r9 = r9.mSlideShowBitmapList;	 Catch:{ all -> 0x00f4 }
            if (r9 != 0) goto L_0x0016;
        L_0x0014:
            monitor-exit(r10);	 Catch:{ all -> 0x00f4 }
        L_0x0015:
            return;
        L_0x0016:
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r9 = r9.mSlideShowBitmapList;	 Catch:{ all -> 0x00f4 }
            r9.clear();	 Catch:{ all -> 0x00f4 }
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r7 = r9.mCurrProjectIndex;	 Catch:{ all -> 0x00f4 }
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r9 = r9.mProjectArray;	 Catch:{ all -> 0x00f4 }
            r11 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r11 = r11.mCurrProjectIndex;	 Catch:{ all -> 0x00f4 }
            r2 = r9.get(r11);	 Catch:{ all -> 0x00f4 }
            r2 = (com.lge.camera.database.OverlapProjectDb) r2;	 Catch:{ all -> 0x00f4 }
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r11 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r12 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r12 = r12.mGet;	 Catch:{ all -> 0x00f4 }
            r12 = r12.getCurDir();	 Catch:{ all -> 0x00f4 }
            r11 = r11.getBucketId(r12);	 Catch:{ all -> 0x00f4 }
            r12 = r2.getProjectId();	 Catch:{ all -> 0x00f4 }
            r13 = "6";
            r8 = r9.getMostRecentContent(r11, r12, r13);	 Catch:{ all -> 0x00f4 }
            if (r8 != 0) goto L_0x008c;
        L_0x0051:
            r9 = "CameraApp";
            r11 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00f4 }
            r11.<init>();	 Catch:{ all -> 0x00f4 }
            r12 = "[Cell]startIndex  startIndex  uri size ";
            r11 = r11.append(r12);	 Catch:{ all -> 0x00f4 }
            r11 = r11.append(r1);	 Catch:{ all -> 0x00f4 }
            r11 = r11.toString();	 Catch:{ all -> 0x00f4 }
            com.lge.camera.util.CamLog.m3d(r9, r11);	 Catch:{ all -> 0x00f4 }
            r6 = r2.getPreset();	 Catch:{ all -> 0x00f4 }
            r5 = r2.getSamplePath();	 Catch:{ all -> 0x00f4 }
            r9 = -1;
            if (r6 == r9) goto L_0x0091;
        L_0x0074:
            r3 = 1;
            r4 = 0;
        L_0x0076:
            if (r4 >= r3) goto L_0x00a3;
        L_0x0078:
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r11 = com.lge.camera.util.SquareUtil.getOverlapSampleUri(r6, r4, r5);	 Catch:{ all -> 0x00f4 }
            r0 = r9.getSlideShowBitmap(r11);	 Catch:{ all -> 0x00f4 }
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r9 = r9.mSlideShowBitmapList;	 Catch:{ all -> 0x00f4 }
            r9.add(r0);	 Catch:{ all -> 0x00f4 }
            r4 = r4 + 1;
            goto L_0x0076;
        L_0x008c:
            r1 = r8.size();	 Catch:{ all -> 0x00f4 }
            goto L_0x0051;
        L_0x0091:
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r11 = 0;
            r11 = com.lge.camera.util.SquareUtil.getOverlapSampleUri(r6, r11, r5);	 Catch:{ all -> 0x00f4 }
            r0 = r9.getSlideShowBitmap(r11);	 Catch:{ all -> 0x00f4 }
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r9 = r9.mSlideShowBitmapList;	 Catch:{ all -> 0x00f4 }
            r9.add(r0);	 Catch:{ all -> 0x00f4 }
        L_0x00a3:
            r4 = 0;
        L_0x00a4:
            if (r4 >= r1) goto L_0x00bc;
        L_0x00a6:
            r11 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r9 = r8.get(r4);	 Catch:{ all -> 0x00f4 }
            r9 = (android.net.Uri) r9;	 Catch:{ all -> 0x00f4 }
            r0 = r11.getSlideShowBitmap(r9);	 Catch:{ all -> 0x00f4 }
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r9 = r9.mSlideShowBitmapList;	 Catch:{ all -> 0x00f4 }
            r9.add(r0);	 Catch:{ all -> 0x00f4 }
            r4 = r4 + 1;
            goto L_0x00a4;
        L_0x00bc:
            r9 = "CameraApp";
            r11 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00f4 }
            r11.<init>();	 Catch:{ all -> 0x00f4 }
            r12 = "[Cell]size ";
            r11 = r11.append(r12);	 Catch:{ all -> 0x00f4 }
            r12 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r12 = r12.mSlideShowBitmapList;	 Catch:{ all -> 0x00f4 }
            r12 = r12.size();	 Catch:{ all -> 0x00f4 }
            r11 = r11.append(r12);	 Catch:{ all -> 0x00f4 }
            r11 = r11.toString();	 Catch:{ all -> 0x00f4 }
            com.lge.camera.util.CamLog.m3d(r9, r11);	 Catch:{ all -> 0x00f4 }
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r9 = r9.mCurrProjectIndex;	 Catch:{ all -> 0x00f4 }
            if (r7 != r9) goto L_0x00f7;
        L_0x00e4:
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r9 = r9.mGet;	 Catch:{ all -> 0x00f4 }
            r11 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r11 = r11.mSlideShowRunnable;	 Catch:{ all -> 0x00f4 }
            r12 = 0;
            r9.postOnUiThread(r11, r12);	 Catch:{ all -> 0x00f4 }
        L_0x00f1:
            monitor-exit(r10);	 Catch:{ all -> 0x00f4 }
            goto L_0x0015;
        L_0x00f4:
            r9 = move-exception;
            monitor-exit(r10);	 Catch:{ all -> 0x00f4 }
            throw r9;
        L_0x00f7:
            r9 = "CameraApp";
            r11 = "[Cell]slideshow different index ";
            com.lge.camera.util.CamLog.m3d(r9, r11);	 Catch:{ all -> 0x00f4 }
            r9 = com.lge.camera.managers.ext.OverlapProjectManagerBase.this;	 Catch:{ all -> 0x00f4 }
            r9 = r9.mSlideShowBitmapList;	 Catch:{ all -> 0x00f4 }
            r9.clear();	 Catch:{ all -> 0x00f4 }
            goto L_0x00f1;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.OverlapProjectManagerBase.AddSlideShowBitmapThread.run():void");
        }
    }

    public OverlapProjectManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setProjectManagerInterface(OverlapProjectManagerInterface listener) {
        this.mListener = listener;
    }

    public void init() {
        super.init();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
        this.mOverlapProjectLayout = (RelativeLayout) this.mGet.inflateView(C0088R.layout.overlap_project_layout);
        if (vg != null && this.mOverlapProjectLayout != null) {
            View preview = this.mGet.findViewById(C0088R.id.preview_layout);
            int previewIndex = 0;
            if (preview != null) {
                previewIndex = ((RelativeLayout) preview.getParent()).indexOfChild(preview);
            }
            vg.addView(this.mOverlapProjectLayout, previewIndex + 3);
            int height = (int) (((float) Utils.getLCDsize(getAppContext(), true)[0]) / 2.0f);
            LayoutParams flp = (LayoutParams) this.mOverlapProjectLayout.getLayoutParams();
            flp.height = height;
            flp.width = height;
            flp.addRule(12);
            this.mOverlapProjectRotateLayout = (RotateLayout) this.mOverlapProjectLayout.findViewById(C0088R.id.overlap_project_rotate_layout);
            this.mOverlapProjectRotateLayout.rotateLayout(getOrientationDegree());
            this.mGridView = (GridView) this.mOverlapProjectLayout.findViewById(C0088R.id.overlap_project_gridview);
            this.mProjectImgAdapter.setAdapterInterface(this);
            this.mProjectImgAdapter.initAdapter();
            this.mGridView.setAdapter(this.mProjectImgAdapter);
            this.mGridView.setFocusable(false);
            this.mGridView.setOnItemClickListener(new C12431());
            this.mGridView.setOnItemLongClickListener(new C12442());
            this.mGridView.setVisibility(0);
            this.mTouchBlockCoverView = this.mOverlapProjectLayout.findViewById(C0088R.id.overlap_project_black_cover);
        }
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        removeSlideShow();
    }

    public void onDestroy() {
        super.onDestroy();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
        if (!(vg == null || this.mOverlapProjectLayout == null)) {
            vg.removeView(this.mOverlapProjectLayout);
        }
        this.mOverlapProjectLayout = null;
        this.mProjectImgAdapter.clearCache();
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        LayoutParams rlp = (LayoutParams) this.mGridView.getLayoutParams();
        int height = Utils.getLCDsize(getAppContext(), true)[1];
        if (degree == 0 || degree == 180) {
            rlp.height = height;
            rlp.width = height;
        } else {
            rlp.height = height;
            rlp.width = height;
        }
        this.mGridView.setLayoutParams(rlp);
        int beforeDegree = this.mOverlapProjectRotateLayout.getAngle();
        this.mOverlapProjectRotateLayout.rotateLayout(degree);
        if (this.mOverlapProjectLayout.getVisibility() == 0) {
            AnimationUtil.startRotateAnimationForRotateLayout(this.mOverlapProjectRotateLayout, beforeDegree, degree, false, 300, null);
        }
    }

    public OverlapProjectAdapter getProjectAdapter() {
        return this.mProjectImgAdapter;
    }

    public void notifyProjectAdaper() {
        this.mProjectImgAdapter.notifyDataSetChanged();
    }

    public View getProjectLayout() {
        return this.mOverlapProjectLayout;
    }

    public void setVisible(boolean show) {
        if (this.mOverlapProjectLayout != null) {
            if (show) {
                this.mOverlapProjectLayout.setVisibility(0);
            } else {
                this.mOverlapProjectLayout.setVisibility(8);
            }
        }
    }

    public boolean isShow() {
        return this.mOverlapProjectLayout.getVisibility() == 0;
    }

    public ArrayList<OverlapProjectDb> onLoadedSamples() {
        return this.mProjectArray;
    }

    public OverlapProjectDb openDatabase() {
        CamLog.m3d(CameraConstants.TAG, "[Cell]openDatabase()");
        this.mProjectDb.open();
        OverlapProjectDb currProject = insertDefaultProjects();
        makeDirectory(SquareUtil.getSampleFilesDir(getAppContext()));
        return currProject;
    }

    private File makeDirectory(String dir_path) {
        File dir = new File(dir_path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0034 A:{SYNTHETIC, Splitter: B:22:0x0034} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x005e  */
    /* JADX WARNING: Removed duplicated region for block: B:66:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0034 A:{SYNTHETIC, Splitter: B:22:0x0034} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:66:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x005e  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0063 A:{SYNTHETIC, Splitter: B:46:0x0063} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0075  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0063 A:{SYNTHETIC, Splitter: B:46:0x0063} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0075  */
    public boolean copyFile(java.io.File r13, java.lang.String r14) throws java.io.IOException {
        /*
        r12 = this;
        r9 = 1;
        r8 = 0;
        if (r13 == 0) goto L_0x0077;
    L_0x0004:
        r10 = r13.exists();
        if (r10 == 0) goto L_0x0077;
    L_0x000a:
        r2 = 0;
        r5 = 0;
        r4 = 0;
        r3 = new java.io.FileInputStream;	 Catch:{ Exception -> 0x0080 }
        r3.<init>(r13);	 Catch:{ Exception -> 0x0080 }
        r6 = new java.io.FileOutputStream;	 Catch:{ Exception -> 0x0082, all -> 0x0079 }
        r6.<init>(r14);	 Catch:{ Exception -> 0x0082, all -> 0x0079 }
        r7 = 0;
        r10 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r0 = new byte[r10];	 Catch:{ Exception -> 0x002b, all -> 0x007c }
    L_0x001c:
        r10 = 0;
        r11 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r7 = r3.read(r0, r10, r11);	 Catch:{ Exception -> 0x002b, all -> 0x007c }
        r10 = -1;
        if (r7 == r10) goto L_0x003f;
    L_0x0026:
        r10 = 0;
        r6.write(r0, r10, r7);	 Catch:{ Exception -> 0x002b, all -> 0x007c }
        goto L_0x001c;
    L_0x002b:
        r1 = move-exception;
        r5 = r6;
        r2 = r3;
    L_0x002e:
        r4 = 1;
        r1.printStackTrace();	 Catch:{ all -> 0x0060 }
        if (r2 == 0) goto L_0x0037;
    L_0x0034:
        r2.close();	 Catch:{ all -> 0x0057 }
    L_0x0037:
        if (r5 == 0) goto L_0x003c;
    L_0x0039:
        r5.close();
    L_0x003c:
        if (r4 == 0) goto L_0x005e;
    L_0x003e:
        return r8;
    L_0x003f:
        if (r3 == 0) goto L_0x0044;
    L_0x0041:
        r3.close();	 Catch:{ all -> 0x004e }
    L_0x0044:
        if (r6 == 0) goto L_0x0049;
    L_0x0046:
        r6.close();
    L_0x0049:
        if (r4 == 0) goto L_0x0055;
    L_0x004b:
        r5 = r6;
        r2 = r3;
        goto L_0x003e;
    L_0x004e:
        r9 = move-exception;
        if (r6 == 0) goto L_0x0054;
    L_0x0051:
        r6.close();
    L_0x0054:
        throw r9;
    L_0x0055:
        r8 = r9;
        goto L_0x004b;
    L_0x0057:
        r9 = move-exception;
        if (r5 == 0) goto L_0x005d;
    L_0x005a:
        r5.close();
    L_0x005d:
        throw r9;
    L_0x005e:
        r8 = r9;
        goto L_0x003e;
    L_0x0060:
        r10 = move-exception;
    L_0x0061:
        if (r2 == 0) goto L_0x0066;
    L_0x0063:
        r2.close();	 Catch:{ all -> 0x006e }
    L_0x0066:
        if (r5 == 0) goto L_0x006b;
    L_0x0068:
        r5.close();
    L_0x006b:
        if (r4 == 0) goto L_0x0075;
    L_0x006d:
        throw r10;
    L_0x006e:
        r9 = move-exception;
        if (r5 == 0) goto L_0x0074;
    L_0x0071:
        r5.close();
    L_0x0074:
        throw r9;
    L_0x0075:
        r8 = r9;
        goto L_0x006d;
    L_0x0077:
        r8 = 0;
        goto L_0x003e;
    L_0x0079:
        r10 = move-exception;
        r2 = r3;
        goto L_0x0061;
    L_0x007c:
        r10 = move-exception;
        r5 = r6;
        r2 = r3;
        goto L_0x0061;
    L_0x0080:
        r1 = move-exception;
        goto L_0x002e;
    L_0x0082:
        r1 = move-exception;
        r2 = r3;
        goto L_0x002e;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.OverlapProjectManagerBase.copyFile(java.io.File, java.lang.String):boolean");
    }

    public boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        file.delete();
        return true;
    }

    public void closeDatabase() {
        CamLog.m3d(CameraConstants.TAG, "[Cell]closeDatabase()");
        this.mProjectDb.close();
    }

    public int getProjectCnt() {
        return this.mCount;
    }

    public int getUserSampleCnt() {
        return this.mProjectDb.getUSampleCnt();
    }

    public OverlapProjectDb insertDefaultProjects() {
        CamLog.m3d(CameraConstants.TAG, "[Cell]insertDefaultProjects " + SharedPreferenceUtil.getSquareOverlapDefaultAdded(getAppContext()) + " " + this.mProjectDb.getCount());
        if (this.mProjectDb.getCount() == 0 && !SharedPreferenceUtil.getSquareOverlapDefaultAdded(getAppContext())) {
            String uuid = UUID.randomUUID().toString();
            OverlapProjectDb project00 = new OverlapProjectDb(uuid + "1", "name", OverlapProjectDbAdapter.KEY_URI, 0, "path", 0, 0);
            OverlapProjectDb project01 = new OverlapProjectDb(uuid + "2", "name", OverlapProjectDbAdapter.KEY_URI, 1, "path", 1, 0);
            OverlapProjectDb project02 = new OverlapProjectDb(uuid + "3", "name", OverlapProjectDbAdapter.KEY_URI, 2, "path", 2, 0);
            this.mProjectDb.insert(project00);
            this.mProjectDb.insert(project01);
            this.mProjectDb.insert(project02);
            SharedPreferenceUtil.saveSquareOverlapDefaultAdded(getAppContext(), true);
        }
        this.mProjectArray.clear();
        this.mProjectDb.getProjectLists(this.mProjectArray);
        this.mCount = this.mProjectArray.size();
        for (int i = 0; i < this.mProjectArray.size(); i++) {
            OverlapProjectDb tempProject = (OverlapProjectDb) this.mProjectArray.get(i);
            CamLog.m3d(CameraConstants.TAG, "[Cell]projectId : " + tempProject.getProjectId() + " preset : " + tempProject.getPreset());
        }
        this.mCurrProjectIndex = SharedPreferenceUtil.getSquareOverlapProject(getAppContext());
        if (this.mCurrProjectIndex >= this.mProjectArray.size()) {
            this.mCurrProjectIndex = 0;
            SharedPreferenceUtil.saveSquareOverlapProject(getAppContext(), 0);
        }
        if (this.mCurrProjectIndex == -1) {
            this.mGridView.setSelection(0);
            return null;
        }
        this.mGridView.setSelection(this.mCurrProjectIndex);
        return (OverlapProjectDb) this.mProjectArray.get(this.mCurrProjectIndex);
    }

    public OverlapProjectDb insertNewUserProject(String sampleName, int usampleCnt) {
        if (this.mProjectDb.getCount() >= 12) {
            return null;
        }
        OverlapProjectDb project = new OverlapProjectDb(UUID.randomUUID().toString(), "circle", OverlapProjectDbAdapter.KEY_URI, -1, sampleName, this.mProjectArray.size(), usampleCnt);
        this.mProjectDb.insert(project);
        this.mProjectArray.clear();
        this.mProjectDb.getProjectLists(this.mProjectArray);
        this.mCount = this.mProjectArray.size();
        CamLog.m3d(CameraConstants.TAG, "[Cell]insertNewUserProject " + this.mCount);
        return project;
    }

    public void setCurrProjectIndex(int index) {
        this.mCurrProjectIndex = index;
        SharedPreferenceUtil.saveSquareOverlapProject(getAppContext(), this.mCurrProjectIndex);
    }

    public int getCurrProjectIndex() {
        return this.mCurrProjectIndex;
    }

    public void changeProjectListIndex(int selArrIndex) {
        OverlapProjectDb currProject = (OverlapProjectDb) this.mProjectArray.get(selArrIndex);
        int listIndex = currProject.getListIndex();
        for (int i = 0; i < listIndex; i++) {
            OverlapProjectDb tempProject = (OverlapProjectDb) this.mProjectArray.get(i);
            this.mProjectDb.updateListIndex(tempProject.getId(), tempProject.getListIndex() + 1);
        }
        this.mProjectDb.updateListIndex(currProject.getId(), 0);
        this.mProjectArray.clear();
        this.mProjectDb.getProjectLists(this.mProjectArray);
        this.mCount = this.mProjectArray.size();
        this.mProjectImgAdapter.notifyDataSetChanged();
        this.mGridView.smoothScrollToPosition(0);
        addSlideShow();
    }

    public OverlapProjectDb deleteProject() {
        int i;
        OverlapProjectDb tempProject;
        String resIdStr;
        removeSlideShow();
        for (i = this.mLongClickIndex + 1; i < this.mProjectArray.size(); i++) {
            tempProject = (OverlapProjectDb) this.mProjectArray.get(i);
            this.mProjectDb.updateListIndex(tempProject.getId(), tempProject.getListIndex() - 1);
        }
        if (this.mCurrProjectIndex > this.mLongClickIndex) {
            this.mCurrProjectIndex--;
        }
        tempProject = (OverlapProjectDb) this.mProjectArray.get(this.mLongClickIndex);
        int preset = tempProject.getPreset();
        if (preset != -1) {
            resIdStr = String.valueOf(OverlapProjectDefaults.getDefaultSample(preset));
        } else {
            resIdStr = SquareUtil.getSampleFilesDir(getAppContext()) + tempProject.getSamplePath();
            deleteFile(new File(resIdStr));
        }
        this.mProjectDb.delete(tempProject.getId());
        this.mProjectImgAdapter.removeBitmapFromMemCache(resIdStr);
        this.mProjectArray.clear();
        this.mProjectDb.getProjectLists(this.mProjectArray);
        this.mCount = this.mProjectArray.size();
        for (i = 0; i < this.mProjectArray.size(); i++) {
            OverlapProjectDb project = (OverlapProjectDb) this.mProjectArray.get(i);
            CamLog.m3d(CameraConstants.TAG, "[Cell]preset : " + project.getPreset() + " sample path : " + project.getSamplePath());
        }
        if (this.mCurrProjectIndex == this.mProjectArray.size()) {
            this.mCurrProjectIndex--;
        }
        this.mProjectImgAdapter.notifyDataSetChanged();
        this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.popup_delete_done), CameraConstants.TOAST_LENGTH_SHORT);
        SharedPreferenceUtil.saveSquareOverlapProject(getAppContext(), this.mCurrProjectIndex);
        if (this.mCurrProjectIndex < 0) {
            return null;
        }
        return (OverlapProjectDb) this.mProjectArray.get(this.mCurrProjectIndex);
    }

    public boolean isSlideShowing() {
        if (this.mSlideShowBitmapList != null) {
            return this.mSlideShowBitmapList.size() > 0;
        } else {
            return false;
        }
    }

    public void addSlideShow() {
        if (this.mListener.getOverlapCaptureMode() != 1 && this.mOverlapProjectLayout != null) {
            removeSlideShow();
            CamLog.m3d(CameraConstants.TAG, "[Cell]addSlideShow : " + this.mCurrProjectIndex);
            this.mAddSlideShowBitmapThread = new AddSlideShowBitmapThread(this, null);
            this.mAddSlideShowBitmapThread.start();
        }
    }

    public void removeSlideShow() {
        if (this.mOverlapProjectLayout != null) {
            CamLog.m3d(CameraConstants.TAG, "[Cell]removeSlideShow : " + this.mCurrProjectIndex);
            this.mProjectImgAdapter.stopSlideShow(this.mGridView.getChildAt(this.mProjectSlideIndex - this.mGridView.getFirstVisiblePosition()), this.mProjectSlideIndex);
            this.mCurrIndexOfSlideShow = 0;
            this.mPlayedCntOfSlideShow = 0;
            this.mGet.removePostRunnable(this.mSlideShowRunnable);
            try {
                if (this.mSlideShowBitmapList != null) {
                    for (int i = 0; i < this.mSlideShowBitmapList.size(); i++) {
                        Bitmap removeBmp = (Bitmap) this.mSlideShowBitmapList.get(i);
                        if (removeBmp != null) {
                            removeBmp.recycle();
                        }
                    }
                    this.mSlideShowBitmapList.clear();
                }
            } catch (IndexOutOfBoundsException e) {
                CamLog.m3d(CameraConstants.TAG, "[Cell] SlideShow IndexOutOfBoundsException ");
            }
        }
    }

    public Bitmap getSlideShowBitmap(Uri uri) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] getBitmap - start");
        if (uri.toString().contains(OverlapProjectDbAdapter.URI_OVERLAP)) {
            return SquareUtil.getOverlapSampleBitmap(this.mGet.getAppContext(), uri, CameraConstantsEx.HD_SCREEN_RESOLUTION, CameraConstantsEx.HD_SCREEN_RESOLUTION);
        }
        String filePath = FileUtil.getRealPathFromURI(this.mGet.getAppContext(), uri);
        if (filePath == null) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] filePath is null");
            return null;
        }
        Bitmap bmp;
        ExifInterface exif = Exif.readExif(filePath);
        int degree = Exif.getOrientation(exif);
        if (exif.getThumbnailBitmap() == null) {
            bmp = BitmapManagingUtil.getThumbnailFromUri(getActivity(), uri, 1);
        } else {
            bmp = BitmapManagingUtil.getRotatedImage(exif.getThumbnailBitmap(), degree, false);
        }
        CamLog.m3d(CameraConstants.TAG, "[Cell] getBitmap - end");
        return bmp;
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    public java.util.ArrayList<android.net.Uri> getMostRecentContent(java.lang.String r17, java.lang.String r18, java.lang.String r19) {
        /*
        r16 = this;
        r12 = new java.util.ArrayList;
        r12.<init>();
        r3 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        r2 = "6";
        r0 = r19;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x0028;
    L_0x0011:
        r2 = r3.buildUpon();
        r6 = "limit";
        r0 = r19;
        r2 = r2.appendQueryParameter(r6, r0);
        r3 = r2.build();
        r2 = "CameraApp";
        r6 = "[Cell] slidshow num 6";
        com.lge.camera.util.CamLog.m3d(r2, r6);
    L_0x0028:
        r2 = 4;
        r4 = new java.lang.String[r2];
        r2 = 0;
        r6 = "_id";
        r4[r2] = r6;
        r2 = 1;
        r6 = "orientation";
        r4[r2] = r6;
        r2 = 2;
        r6 = "datetaken";
        r4[r2] = r6;
        r2 = 3;
        r6 = "_display_name";
        r4[r2] = r6;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r6 = "camera_mode='4' AND project_id='";
        r2 = r2.append(r6);
        r0 = r18;
        r2 = r2.append(r0);
        r6 = "'";
        r2 = r2.append(r6);
        r5 = r2.toString();
        r7 = "datetaken DESC,_id DESC";
        r10 = 0;
        r2 = r16.getActivity();
        if (r2 != 0) goto L_0x0065;
    L_0x0063:
        r12 = 0;
    L_0x0064:
        return r12;
    L_0x0065:
        r0 = r16;
        r2 = r0.mGet;	 Catch:{ Exception -> 0x00e9 }
        r2 = r2.getActivity();	 Catch:{ Exception -> 0x00e9 }
        r2 = r2.getContentResolver();	 Catch:{ Exception -> 0x00e9 }
        r6 = 0;
        r10 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x00e9 }
        if (r10 != 0) goto L_0x0087;
    L_0x0078:
        r2 = "CameraApp";
        r6 = "[Cell]imageQueryCursor null";
        com.lge.camera.util.CamLog.m3d(r2, r6);	 Catch:{ Exception -> 0x00e9 }
        r12 = 0;
        if (r10 == 0) goto L_0x0064;
    L_0x0082:
        r10.close();
        r10 = 0;
        goto L_0x0064;
    L_0x0087:
        r11 = 0;
        r14 = -1;
        r9 = java.lang.Long.valueOf(r14);	 Catch:{ Exception -> 0x00e9 }
        r2 = "CameraApp";
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00e9 }
        r6.<init>();	 Catch:{ Exception -> 0x00e9 }
        r13 = "[Cell]imageQueryCursor.getCount() ";
        r6 = r6.append(r13);	 Catch:{ Exception -> 0x00e9 }
        r13 = r10.getCount();	 Catch:{ Exception -> 0x00e9 }
        r6 = r6.append(r13);	 Catch:{ Exception -> 0x00e9 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x00e9 }
        com.lge.camera.util.CamLog.m3d(r2, r6);	 Catch:{ Exception -> 0x00e9 }
        r2 = r10.getCount();	 Catch:{ Exception -> 0x00e9 }
        if (r2 != 0) goto L_0x00b8;
    L_0x00b0:
        r12 = 0;
        if (r10 == 0) goto L_0x0064;
    L_0x00b3:
        r10.close();
        r10 = 0;
        goto L_0x0064;
    L_0x00b8:
        r2 = r10.getCount();	 Catch:{ Exception -> 0x00e9 }
        if (r2 == 0) goto L_0x00e1;
    L_0x00be:
        r10.moveToFirst();	 Catch:{ Exception -> 0x00e9 }
    L_0x00c1:
        r11 = r3;
        r2 = "_id";
        r2 = r10.getColumnIndexOrThrow(r2);	 Catch:{ Exception -> 0x00e9 }
        r14 = r10.getLong(r2);	 Catch:{ Exception -> 0x00e9 }
        r9 = java.lang.Long.valueOf(r14);	 Catch:{ Exception -> 0x00e9 }
        r14 = r9.longValue();	 Catch:{ Exception -> 0x00e9 }
        r2 = android.content.ContentUris.withAppendedId(r11, r14);	 Catch:{ Exception -> 0x00e9 }
        r12.add(r2);	 Catch:{ Exception -> 0x00e9 }
        r2 = r10.moveToNext();	 Catch:{ Exception -> 0x00e9 }
        if (r2 != 0) goto L_0x00c1;
    L_0x00e1:
        if (r10 == 0) goto L_0x0064;
    L_0x00e3:
        r10.close();
        r10 = 0;
        goto L_0x0064;
    L_0x00e9:
        r8 = move-exception;
        r8.printStackTrace();	 Catch:{ all -> 0x00f6 }
        if (r10 == 0) goto L_0x00f3;
    L_0x00ef:
        r10.close();
        r10 = 0;
    L_0x00f3:
        r12 = 0;
        goto L_0x0064;
    L_0x00f6:
        r2 = move-exception;
        if (r10 == 0) goto L_0x00fd;
    L_0x00f9:
        r10.close();
        r10 = 0;
    L_0x00fd:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.OverlapProjectManagerBase.getMostRecentContent(java.lang.String, java.lang.String, java.lang.String):java.util.ArrayList<android.net.Uri>");
    }

    public String getBucketId(String currentDir) {
        String currentStorage = currentDir;
        if (currentStorage.length() <= 0) {
            return null;
        }
        return String.valueOf(currentStorage.substring(0, currentStorage.length() - 1).toLowerCase(Locale.US).hashCode());
    }

    public void setGridViewMoveToPosition(int position, boolean useAnim) {
        if (useAnim) {
            this.mGridView.smoothScrollToPosition(this.mCount);
        } else {
            this.mGridView.setSelection(this.mCount);
        }
    }

    public void showTouchBlockCoverView(boolean show) {
        if (this.mTouchBlockCoverView == null) {
            CamLog.m11w(CameraConstants.TAG, "[Cell] return");
        } else if (show) {
            this.mTouchBlockCoverView.setVisibility(0);
            this.mTouchBlockCoverView.setOnTouchListener(new C12464());
        } else {
            this.mTouchBlockCoverView.setVisibility(8);
            this.mTouchBlockCoverView.setOnTouchListener(null);
        }
    }

    public void onProjectSelected(int selectedPos, boolean isMoveToSnap, boolean isUserClick) {
        this.mListener.onProjectSelected(selectedPos, isMoveToSnap, isUserClick);
    }
}
