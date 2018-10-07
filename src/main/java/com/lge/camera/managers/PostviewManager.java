package com.lge.camera.managers;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.postview.PostviewAttach;
import com.lge.camera.postview.PostviewBase;
import com.lge.camera.postview.PostviewBridge;
import com.lge.camera.postview.PostviewNormal;
import com.lge.camera.postview.PostviewParameters;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;

public class PostviewManager extends PostviewManagerBase {
    private OnPostviewListener mListener = null;
    @SuppressLint({"UseSparseArrays"})
    private HashMap<Integer, PostviewBase> mPostviewMap = new HashMap();

    public interface OnPostviewListener {
        void onPostviewDisplayed();

        void onPostviewReleased();

        void onPostviewReleasedAfter(int i);
    }

    public PostviewManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void releasePostview() {
        if (this.mListener != null) {
            this.mListener.onPostviewReleased();
        }
        this.mCurPostview = null;
    }

    public void releasePostviewAfter(int type) {
        if (this.mListener != null) {
            this.mListener.onPostviewReleasedAfter(type);
            this.mListener = null;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mPostviewMap != null) {
            this.mPostviewMap.clear();
        }
        if (this.mListener != null) {
            this.mListener = null;
        }
    }

    public void executePostview(OnPostviewListener listener, int postView, ArrayList<Uri> uriList, int type) {
        if (this.mPostviewMap.containsKey(Integer.valueOf(postView))) {
            this.mCurPostview = (PostviewBase) this.mPostviewMap.get(Integer.valueOf(postView));
            CamLog.m3d(CameraConstants.TAG, "postView : " + postView);
            if (this.mCurPostview != null) {
                IntentBroadcastUtil.sendBroadcastIntentCameraEnded(getActivity());
                this.mListener = listener;
                PostviewParameters params = this.mCurPostview.getPostviewParams();
                params.setUriList(cloneUri(uriList));
                params.setContentType(type);
                int[] size = Utils.sizeStringToArray(this.mGet.getCurrentSelectedPreviewSize());
                this.mCurPostview.getPreviewSize(size[1], size[0]);
                this.mCurPostview.executePostview(params);
                if (this.mListener != null) {
                    this.mListener.onPostviewDisplayed();
                }
            }
        }
    }

    private ArrayList<Uri> cloneUri(ArrayList<Uri> uriList) {
        ArrayList<Uri> result = new ArrayList();
        if (uriList != null) {
            for (int i = 0; i < uriList.size(); i++) {
                result.add(uriList.get(i));
            }
        }
        return result;
    }

    public void createPostview() {
        createPostview(1, PostviewAttach.class.getName());
        createPostview(0, PostviewNormal.class.getName());
    }

    public void createPostview(int type, String postviewClassName) {
        try {
            PostviewBase postview = (PostviewBase) Class.forName(postviewClassName).getConstructor(new Class[]{PostviewBridge.class}).newInstance(new Object[]{this});
            if (this.mPostviewMap != null) {
                this.mPostviewMap.put(Integer.valueOf(type), postview);
            }
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, "registerReceiver error : " + e);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mCurPostview != null) {
            return this.mCurPostview.onTouchEvent(event);
        }
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mCurPostview != null) {
            return this.mCurPostview.onKeyDown(keyCode, event);
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mCurPostview != null) {
            return this.mCurPostview.onKeyUp(keyCode, event);
        }
        return false;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.mCurPostview != null) {
            return this.mCurPostview.onPrepareOptionsMenu(menu);
        }
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.mCurPostview != null) {
            return this.mCurPostview.onOptionsItemSelected(item);
        }
        return false;
    }

    public void requestPostViewButtonFocus() {
        if (this.mCurPostview != null) {
            this.mCurPostview.requestPostViewButtonFocus();
        }
    }

    public View inflateStub(int id) {
        return this.mGet.inflateStub(id);
    }
}
