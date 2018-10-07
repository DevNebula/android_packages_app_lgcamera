package com.lge.camera.file;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class DBScanManager {
    private Context mContext = null;
    private String mFilePath = null;
    private DBScanInterface mGet = null;
    private MediaScannerConnectionClient mScanClient = null;
    private MediaScannerConnection msc = null;

    /* renamed from: com.lge.camera.file.DBScanManager$1 */
    class C07891 implements MediaScannerConnectionClient {
        C07891() {
        }

        public void onMediaScannerConnected() {
            if (DBScanManager.this.msc != null) {
                DBScanManager.this.msc.scanFile(DBScanManager.this.mFilePath, null);
            }
        }

        public void onScanCompleted(String path, Uri uri) {
            DBScanManager.this.mGet.onDBScanCompleted(path, uri);
            if (DBScanManager.this.msc != null) {
                DBScanManager.this.msc.disconnect();
            }
        }
    }

    public interface DBScanInterface {
        void onDBScanCompleted(String str, Uri uri);
    }

    public DBScanManager(Context context, DBScanInterface dBInterface) {
        this.mContext = context;
        this.mGet = dBInterface;
        this.mScanClient = getDBScanClient();
    }

    public void startScan(String filePath) {
        this.mFilePath = filePath;
        this.msc = new MediaScannerConnection(this.mContext, this.mScanClient);
        if (this.msc != null) {
            this.msc.connect();
        }
    }

    public MediaScannerConnectionClient getDBScanClient() {
        return new C07891();
    }
}
