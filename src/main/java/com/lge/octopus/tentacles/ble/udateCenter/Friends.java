package com.lge.octopus.tentacles.ble.udateCenter;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.lge.appbox.updateproxy.AppInfo;
import com.lge.appbox.updateproxy.AppList;
import com.lge.appbox.updateproxy.RequestForm.RequestFormBuilder;
import com.lge.appbox.updateproxy.manager.Delegate;
import com.lge.appbox.updateproxy.manager.ISelfUpdateServiceCallback;
import com.lge.appbox.updateproxy.manager.SelfUpdateBinder;
import com.lge.appbox.updateproxy.manager.SelfUpdateBinder.ISelfupdateConnection;
import java.util.Iterator;

public class Friends {
    private static final String TAG = Friends.class.getSimpleName();
    private AppList mAppList;
    private final ISelfUpdateServiceCallback mCallback = new C16822();
    private SelfUpdateBinder mSelfUpdateBinder;
    private Delegate mSelfUpdateDelegate;

    /* renamed from: com.lge.octopus.tentacles.ble.udateCenter.Friends$2 */
    class C16822 extends ISelfUpdateServiceCallback {
        C16822() {
        }

        public void onReceiveAppList(AppList appList) throws RemoteException {
            if (appList != null && appList.isSuccess()) {
                Friends.this.mAppList = appList;
                Iterator i$ = Friends.this.mAppList.iterator();
                while (i$.hasNext()) {
                    Friends.this.mSelfUpdateDelegate.downloadAndInstallAppAsync((AppInfo) i$.next());
                }
            }
        }

        public void onDownloadStatus(String appId, int resultCode, int reasonCode, long progress, long total) throws RemoteException {
            Log.d(Friends.TAG, "appId : " + appId + ", resultCode : " + resultCode + ", progress : " + progress + ", tatal : " + total);
        }

        public void onInstallStatus(String appId, int resultCode, int reasonCode) throws RemoteException {
            Log.d(Friends.TAG, "appId : " + appId + ", resultCode : " + resultCode);
        }
    }

    public void create(final Context context) {
        this.mSelfUpdateBinder = SelfUpdateBinder.getInstance(context);
        this.mSelfUpdateBinder.bind(new ISelfupdateConnection() {
            public void onConnected(Delegate delegate) {
                Friends.this.mSelfUpdateDelegate = delegate;
                Friends.this.mSelfUpdateDelegate.registeSelfUpdateCallback(Friends.this.mCallback);
                Friends.this.mSelfUpdateDelegate.retrieveAppListAsync(new RequestFormBuilder(context).setRequestModel("").build());
            }

            public void onDisConnected() {
                Friends.this.mSelfUpdateDelegate = null;
            }
        });
    }

    public void finish() {
        if (this.mSelfUpdateBinder != null) {
            this.mSelfUpdateBinder = null;
        }
        if (this.mSelfUpdateDelegate != null) {
            this.mSelfUpdateDelegate.unregisteSelfUpdateCallback(this.mCallback);
        }
    }
}
