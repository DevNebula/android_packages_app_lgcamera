package com.lge.camera.managers;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.file.FileManager;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class UndoManager extends ManagerInterfaceImpl {
    public static final int DELAY_TIME = 5000;
    public static final int DELETING = 4;
    public static final int NONE = 1;
    public static final int SHOW = 2;
    private String mBurstId;
    private Runnable mDeleteRunnable;
    private Handler mHandler;
    private UndoInterface mListener;
    private RotateLayout mRotateLayout;
    private int mState = 1;
    private boolean mUndo = false;
    private Uri mUri = null;

    /* renamed from: com.lge.camera.managers.UndoManager$1 */
    class C11821 extends Handler {
        C11821() {
        }
    }

    /* renamed from: com.lge.camera.managers.UndoManager$3 */
    class C11843 implements OnClickListener {
        C11843() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] undoButton onClicked");
            if (UndoManager.this.mHandler == null) {
                CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] mHandler is return");
                return;
            }
            UndoManager.this.mHandler.removeCallbacks(UndoManager.this.mDeleteRunnable);
            UndoManager.this.mDeleteRunnable = null;
            UndoManager.this.mUndo = true;
            UndoManager.this.onDismiss();
            if (UndoManager.this.mListener != null) {
                UndoManager.this.mListener.onUndoClicked();
            }
        }
    }

    protected class DeleteBurstTask extends AsyncTask<Void, Void, Void> {
        private Cursor mBurstShotCursor;
        private String mBurstShotId;
        private int mResId = 0;

        public DeleteBurstTask(String burstId) {
            this.mBurstShotId = burstId;
        }

        protected void onPreExecute() {
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] DeleteBurstTask - onPreExecute");
            UndoManager.this.setState(4);
            UndoManager.this.mGet.showDialog(141);
            super.onPreExecute();
        }

        protected Void doInBackground(Void... params) {
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] DeleteBurstTask - doInBackground");
            UndoManager.this.mGet.stopBurstSaving();
            this.mBurstShotCursor = UndoManager.this.getBurstShotCursor(this.mBurstShotId);
            if (this.mBurstShotCursor != null && this.mBurstShotCursor.getCount() > 0) {
                this.mBurstShotCursor.moveToFirst();
                while (true) {
                    Uri uri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, this.mBurstShotCursor.getLong(this.mBurstShotCursor.getColumnIndexOrThrow("_id")));
                    Context context = UndoManager.this.mGet.getAppContext();
                    if (!(uri == null || context == null)) {
                        this.mResId = FileManager.deleteFile(context, uri);
                        if (this.mResId != 0) {
                            break;
                        }
                    }
                    if (this.mBurstShotCursor.isLast()) {
                        break;
                    }
                    this.mBurstShotCursor.moveToNext();
                }
            }
            UndoManager.this.mGet.checkStorage();
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] DeleteBurstTask - onPostExecute");
            if (this.mBurstShotCursor != null) {
                this.mBurstShotCursor.close();
                this.mBurstShotCursor = null;
            }
            if (UndoManager.this.mListener != null) {
                UndoManager.this.mListener.onDeleteComplete(true, this.mResId);
            }
            UndoManager.this.mGet.removeRotateDialog();
            UndoManager.this.removeState(4);
            if (UndoManager.this.mGet.getGifVisibleStatus()) {
                UndoManager.this.mGet.setGifVisibleStatus(false);
                UndoManager.this.mGet.setGIFVisibility(false);
            }
        }
    }

    protected class DeleteFileTask extends AsyncTask<Void, Void, Void> {
        private Uri singleUri = null;

        public DeleteFileTask(Uri uri) {
            this.singleUri = uri;
        }

        protected void onPreExecute() {
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] DeleteFileTask - onPreExecute");
            UndoManager.this.setState(4);
            super.onPreExecute();
        }

        protected Void doInBackground(Void... params) {
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] DeleteFileTask - doInBackground");
            Context context = UndoManager.this.mGet.getAppContext();
            if (!(this.singleUri == null || context == null)) {
                FileManager.deleteFile(context, this.singleUri);
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] DeleteFileTask - onPostExecute");
            super.onPostExecute(aVoid);
            if (UndoManager.this.mListener != null) {
                UndoManager.this.mListener.onDeleteComplete(false, 0);
            }
            UndoManager.this.removeState(4);
        }
    }

    public UndoManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        setSnackBarLayout();
        this.mHandler = new C11821();
    }

    public void deleteOrUndo(Uri uri, String burstId, UndoInterface listener) {
        CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] deleteOrUndo uri = " + uri + ", burstId = " + burstId);
        if (checkCurrentState(2)) {
            deleteImmediately();
        }
        this.mUri = uri;
        this.mBurstId = burstId;
        this.mListener = listener;
        if (burstId != null) {
            this.mGet.showDialog(144, false);
            setState(2);
            return;
        }
        this.mGet.hideAllToast();
        showSnackbarLayout(true);
        postDeleteRunnable(uri);
        this.mUndo = false;
    }

    public void deleteImmediately() {
        if (this.mHandler != null && this.mDeleteRunnable != null && this.mUri != null) {
            this.mHandler.removeCallbacks(this.mDeleteRunnable);
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] delete mUri : " + this.mUri);
            new DeleteFileTask(this.mUri).execute(new Void[0]);
            onDismiss();
        }
    }

    private void postDeleteRunnable(final Uri uri) {
        this.mDeleteRunnable = new Runnable() {
            public void run() {
                if (!(UndoManager.this.mUndo && uri == UndoManager.this.mUri)) {
                    new DeleteFileTask(uri).execute(new Void[0]);
                }
                UndoManager.this.onDismiss();
            }
        };
        if (this.mHandler != null) {
            this.mHandler.postDelayed(this.mDeleteRunnable, CameraConstants.TOAST_LENGTH_LONG);
        }
    }

    private void showSnackbarLayout(boolean show) {
        if (this.mRotateLayout != null) {
            this.mRotateLayout.setVisibility(show ? 0 : 8);
        }
        if (show) {
            setState(2);
            setRotateDegree(this.mGet.getOrientationDegree(), false);
            return;
        }
        removeState(2);
    }

    private void setSnackBarLayout() {
        this.mRotateLayout = (RotateLayout) this.mGet.inflateView(C0088R.layout.rotate_undo_layout);
        if (this.mRotateLayout == null) {
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] Exit because mRotateLayout is null");
            return;
        }
        Button undoButton = (Button) this.mRotateLayout.findViewById(C0088R.id.undo_action);
        ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.camera_base);
        if (undoButton == null || vg == null) {
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] Exit because undoButton or vg is null");
            return;
        }
        vg.addView(this.mRotateLayout);
        showSnackbarLayout(false);
        this.mRotateLayout.setVisibility(8);
        undoButton.setOnClickListener(new C11843());
    }

    private void onDismiss() {
        CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] onDismiss");
        showSnackbarLayout(false);
        this.mUri = null;
    }

    public void onPauseBefore() {
        deleteImmediately();
        super.onPauseBefore();
    }

    public void onDestroy() {
        super.onDestroy();
        ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.camera_base);
        if (vg != null) {
            vg.removeView(this.mRotateLayout);
        }
        this.mRotateLayout = null;
        this.mDeleteRunnable = null;
        this.mHandler = null;
        this.mListener = null;
        this.mUri = null;
    }

    public void setRotateDegree(int degree, boolean animation) {
        boolean z = true;
        if (this.mRotateLayout != null && checkCurrentState(2)) {
            this.mRotateLayout.rotateLayout(degree);
            LayoutParams lp = (LayoutParams) this.mRotateLayout.getLayoutParams();
            View innerLayout = this.mRotateLayout.findViewById(C0088R.id.undo_inner_layout);
            Button button = (Button) this.mRotateLayout.findViewById(C0088R.id.undo_action);
            if (lp != null && button != null && innerLayout != null) {
                if (degree == 0 || degree == 180) {
                    lp.width = -1;
                    lp.height = -2;
                    lp.setMarginStart(0);
                    lp.bottomMargin = this.mGet.getQuickClipTopPosition() + RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.004f);
                    button.setMaxWidth(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.77f));
                    innerLayout.setPaddingRelative(0, 0, 0, 0);
                } else {
                    lp.width = -2;
                    if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                        lp.height = Utils.getLCDsize(getAppContext(), true)[1];
                    } else {
                        lp.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.88888f);
                    }
                    lp.setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.195f));
                    lp.bottomMargin = 0;
                    Context appContext = getAppContext();
                    if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                        z = false;
                    }
                    button.setMaxWidth(RatioCalcUtil.getSizeCalculatedByPercentage(appContext, z, 0.77f));
                    int navigationBarHeight = RatioCalcUtil.getNavigationBarHeight(this.mGet.getAppContext());
                    if (degree == 90) {
                        innerLayout.setPaddingRelative(navigationBarHeight, 0, 0, 0);
                    } else {
                        innerLayout.setPaddingRelative(0, 0, navigationBarHeight, 0);
                    }
                }
                lp.addRule(12);
                this.mRotateLayout.setLayoutParams(lp);
            }
        }
    }

    public void doDeleteOnUndoDialog() {
        CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] doDeleteOnUndoDialog");
        new DeleteBurstTask(this.mBurstId).execute(new Void[0]);
    }

    private void setState(int state) {
        this.mState |= state;
        CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] add state = " + state + ", current mState = " + this.mState);
    }

    private void removeState(int state) {
        this.mState &= state ^ -1;
        CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] remove state = " + state + ", current mState = " + this.mState);
    }

    public boolean checkCurrentState(int state) {
        return (this.mState & state) != 0;
    }

    private Cursor getBurstShotCursor(String burstId) {
        CamLog.m7i(CameraConstants.TAG, "[DeleteOrUndo] getBurstShotCursor()");
        Uri burstShotQuery = Files.getContentUri(CameraConstants.STORAGE_NAME_EXTERNAL).buildUpon().appendQueryParameter("limit", CameraConstants.FPS_100).build();
        String[] burstShotProjection = new String[]{"_id", "burst_id"};
        String burstShotSelection = "(burst_id='" + burstId + "')";
        CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] burstShotSelection : " + burstShotSelection);
        Cursor burstShotQueryCursor = null;
        try {
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] getContentResolver start");
            burstShotQueryCursor = this.mGet.getAppContext().getContentResolver().query(burstShotQuery, burstShotProjection, burstShotSelection, null, null);
            if (burstShotQueryCursor == null) {
                return null;
            }
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] burstShotQueryCursor count : " + burstShotQueryCursor.getCount());
            CamLog.m3d(CameraConstants.TAG, "[DeleteOrUndo] getContentResolver end");
            if (burstShotQueryCursor.getCount() == 0) {
                burstShotQueryCursor.close();
                return null;
            }
            return burstShotQueryCursor;
        } catch (SQLiteException e) {
            CamLog.m6e(CameraConstants.TAG, "[DeleteOrUndo] cursor error ", e);
        } catch (IllegalStateException e2) {
            CamLog.m6e(CameraConstants.TAG, "[DeleteOrUndo] cursor error ", e2);
        } catch (SecurityException e3) {
            CamLog.m6e(CameraConstants.TAG, "[DeleteOrUndo] Security Exception error ", e3);
        }
    }

    public void onDismissUndoDialog() {
        this.mUri = null;
        removeState(2);
    }
}
