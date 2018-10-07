package com.lge.camera.managers.ext.sticker.recentdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lge.camera.managers.ext.sticker.StickerInformationDataClass;
import com.lge.camera.util.CamLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecentDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "sticker_recent_db";
    private static final String TAG = "RecentDBHelper";
    private static RecentDBHelper sInstance = null;
    private static final int version = 1;
    private SQLiteDatabase mDB = null;

    public static synchronized RecentDBHelper getInstance(Context ctx) {
        RecentDBHelper recentDBHelper;
        synchronized (RecentDBHelper.class) {
            if (sInstance == null) {
                sInstance = new RecentDBHelper(ctx);
            }
            recentDBHelper = sInstance;
        }
        return recentDBHelper;
    }

    private RecentDBHelper(Context ctx) {
        super(ctx.getApplicationContext(), DB_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        CamLog.m3d(TAG, "db create start");
        sqLiteDatabase.execSQL(RecentTable.QUERY_CREATE);
        CamLog.m3d(TAG, "db create end");
    }

    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    public void openDB() {
        closeDB();
        if (this.mDB == null) {
            this.mDB = getWritableDatabase();
        }
        trim();
    }

    public boolean isOpened() {
        if (this.mDB == null || !this.mDB.isOpen()) {
            return false;
        }
        return true;
    }

    public void closeDB() {
        if (this.mDB != null && this.mDB.isOpen()) {
            this.mDB.close();
            this.mDB = null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x007c A:{Catch:{ Exception -> 0x0096, all -> 0x00be }} */
    private void trim() {
        /*
        r14 = this;
        r0 = r14.mDB;
        if (r0 == 0) goto L_0x00a5;
    L_0x0004:
        r0 = r14.mDB;
        r0 = r0.isOpen();
        if (r0 == 0) goto L_0x00a5;
    L_0x000c:
        r9 = 0;
        r0 = r14.mDB;	 Catch:{ Exception -> 0x0096 }
        r1 = "Recent_Sticker_Table";
        r2 = com.lge.camera.managers.ext.sticker.recentdb.RecentTable.COLUMNS;	 Catch:{ Exception -> 0x0096 }
        r3 = 0;
        r4 = 0;
        r5 = 0;
        r6 = 0;
        r7 = "_last_used_time DESC";
        r8 = 0;
        r9 = r0.query(r1, r2, r3, r4, r5, r6, r7, r8);	 Catch:{ Exception -> 0x0096 }
        r0 = r14.mDB;	 Catch:{ Exception -> 0x0096 }
        if (r0 == 0) goto L_0x00a6;
    L_0x0022:
        r0 = r14.mDB;	 Catch:{ Exception -> 0x0096 }
        r0 = r0.isOpen();	 Catch:{ Exception -> 0x0096 }
        if (r0 == 0) goto L_0x00a6;
    L_0x002a:
        r13 = 0;
        if (r9 == 0) goto L_0x006a;
    L_0x002d:
        r0 = r9.getCount();	 Catch:{ Exception -> 0x0096 }
        if (r0 <= 0) goto L_0x006a;
    L_0x0033:
        r0 = r9.moveToFirst();	 Catch:{ Exception -> 0x0096 }
        if (r0 == 0) goto L_0x006a;
    L_0x0039:
        r13 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0096 }
        r13.<init>();	 Catch:{ Exception -> 0x0096 }
    L_0x003e:
        r11 = new java.io.File;	 Catch:{ Exception -> 0x0096 }
        r0 = "_config_file_path";
        r0 = r9.getColumnIndex(r0);	 Catch:{ Exception -> 0x0096 }
        r0 = r9.getString(r0);	 Catch:{ Exception -> 0x0096 }
        r11.<init>(r0);	 Catch:{ Exception -> 0x0096 }
        r0 = r11.exists();	 Catch:{ Exception -> 0x0096 }
        if (r0 != 0) goto L_0x0064;
    L_0x0053:
        r0 = "_id";
        r0 = r9.getColumnIndex(r0);	 Catch:{ Exception -> 0x0096 }
        r0 = r9.getInt(r0);	 Catch:{ Exception -> 0x0096 }
        r0 = java.lang.Integer.valueOf(r0);	 Catch:{ Exception -> 0x0096 }
        r13.add(r0);	 Catch:{ Exception -> 0x0096 }
    L_0x0064:
        r0 = r9.moveToNext();	 Catch:{ Exception -> 0x0096 }
        if (r0 != 0) goto L_0x003e;
    L_0x006a:
        if (r13 == 0) goto L_0x00a6;
    L_0x006c:
        r0 = r13.size();	 Catch:{ Exception -> 0x0096 }
        if (r0 <= 0) goto L_0x00a6;
    L_0x0072:
        r0 = r13.iterator();	 Catch:{ Exception -> 0x0096 }
    L_0x0076:
        r1 = r0.hasNext();	 Catch:{ Exception -> 0x0096 }
        if (r1 == 0) goto L_0x00a6;
    L_0x007c:
        r12 = r0.next();	 Catch:{ Exception -> 0x0096 }
        r12 = (java.lang.Integer) r12;	 Catch:{ Exception -> 0x0096 }
        r1 = r14.mDB;	 Catch:{ Exception -> 0x0096 }
        r2 = "Recent_Sticker_Table";
        r3 = "_id =? ";
        r4 = 1;
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0096 }
        r5 = 0;
        r6 = r12.toString();	 Catch:{ Exception -> 0x0096 }
        r4[r5] = r6;	 Catch:{ Exception -> 0x0096 }
        r1.delete(r2, r3, r4);	 Catch:{ Exception -> 0x0096 }
        goto L_0x0076;
    L_0x0096:
        r10 = move-exception;
        r10.printStackTrace();	 Catch:{ all -> 0x00be }
        if (r9 == 0) goto L_0x00a5;
    L_0x009c:
        r0 = r9.isClosed();
        if (r0 != 0) goto L_0x00a5;
    L_0x00a2:
        r9.close();
    L_0x00a5:
        return;
    L_0x00a6:
        if (r9 == 0) goto L_0x00b2;
    L_0x00a8:
        r0 = r9.isClosed();	 Catch:{ Exception -> 0x0096 }
        if (r0 != 0) goto L_0x00b2;
    L_0x00ae:
        r9.close();	 Catch:{ Exception -> 0x0096 }
        r9 = 0;
    L_0x00b2:
        if (r9 == 0) goto L_0x00a5;
    L_0x00b4:
        r0 = r9.isClosed();
        if (r0 != 0) goto L_0x00a5;
    L_0x00ba:
        r9.close();
        goto L_0x00a5;
    L_0x00be:
        r0 = move-exception;
        if (r9 == 0) goto L_0x00ca;
    L_0x00c1:
        r1 = r9.isClosed();
        if (r1 != 0) goto L_0x00ca;
    L_0x00c7:
        r9.close();
    L_0x00ca:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.sticker.recentdb.RecentDBHelper.trim():void");
    }

    public boolean hasContents() {
        int count = getCount();
        CamLog.m3d(TAG, "content count = " + count);
        return count > 0;
    }

    public ArrayList<StickerInformationDataClass> getRecentList() {
        ArrayList<StickerInformationDataClass> mList = new ArrayList();
        Cursor cur = null;
        try {
            if (this.mDB != null && this.mDB.isOpen()) {
                cur = this.mDB.query(RecentTable.TABLE_NAME, RecentTable.COLUMNS, null, null, null, null, "_last_used_time DESC", "10");
                if (cur != null && cur.moveToFirst()) {
                    do {
                        StickerInformationDataClass sid = new StickerInformationDataClass();
                        sid.icon_path = cur.getString(cur.getColumnIndex(RecentTable.ICON_PATH));
                        sid.configFile = cur.getString(cur.getColumnIndex(RecentTable.CONFIG_FILE_PATH));
                        sid.sticker_name = cur.getString(cur.getColumnIndex(RecentTable.STICKER_NAME));
                        sid.solution_type = cur.getInt(cur.getColumnIndex(RecentTable.SOLUTION_TYPE));
                        sid.sticker_data_position = cur.getInt(cur.getColumnIndex(RecentTable.STICKER_DATA_POSITION));
                        sid.sticker_id = cur.getString(cur.getColumnIndex(RecentTable.STICKER_ID));
                        mList.add(sid);
                    } while (cur.moveToNext());
                    cur.close();
                    cur = null;
                } else if (!(cur == null || cur.isClosed())) {
                    cur.close();
                    cur = null;
                }
            }
            if (!(cur == null || cur.isClosed())) {
                cur.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (!(cur == null || cur.isClosed())) {
                cur.close();
            }
        } catch (Throwable th) {
            if (!(cur == null || cur.isClosed())) {
                cur.close();
            }
        }
        return mList;
    }

    private int getCount() {
        int retval = 0;
        Cursor cur = null;
        try {
            if (this.mDB.isOpen()) {
                cur = this.mDB.query(RecentTable.TABLE_NAME, RecentTable.COLUMNS, null, null, null, null, "_last_used_time DESC", "10");
                if (cur != null && cur.getCount() > 0) {
                    retval = cur.getCount();
                }
            }
            if (!(cur == null || cur.isClosed())) {
                cur.close();
                cur = null;
            }
            CamLog.m3d(TAG, "db record count = " + retval);
            if (!(cur == null || cur.isClosed())) {
                cur.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            retval = 0;
            if (!(cur == null || cur.isClosed())) {
                cur.close();
            }
        } catch (Throwable th) {
            if (!(cur == null || cur.isClosed())) {
                cur.close();
            }
        }
        return retval;
    }

    private boolean hasRecord(StickerInformationDataClass dc) {
        boolean retval = false;
        Cursor cur = null;
        if (dc != null) {
            try {
                if (this.mDB != null && this.mDB.isOpen()) {
                    cur = this.mDB.query(RecentTable.TABLE_NAME, RecentTable.COLUMNS, "_sticker_id =? ", new String[]{dc.sticker_id}, null, null, null);
                    if (cur != null && cur.getCount() > 0) {
                        retval = true;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                retval = false;
                if (!(cur == null || cur.isClosed())) {
                    cur.close();
                }
            } catch (Throwable th) {
                if (!(cur == null || cur.isClosed())) {
                    cur.close();
                }
            }
        }
        if (!(cur == null || cur.isClosed())) {
            cur.close();
            cur = null;
        }
        if (!(cur == null || cur.isClosed())) {
            cur.close();
        }
        return retval;
    }

    public void insertOrUpdate(StickerInformationDataClass dc) {
        if (dc != null && this.mDB != null && this.mDB.isOpen()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = new Date();
            ContentValues cv = new ContentValues();
            cv.put(RecentTable.STICKER_ID, dc.sticker_id);
            cv.put(RecentTable.LAST_USED_TIME, dateFormat.format(date));
            cv.put(RecentTable.ICON_PATH, dc.icon_path);
            cv.put(RecentTable.STICKER_NAME, dc.sticker_name);
            cv.put(RecentTable.SOLUTION_TYPE, Integer.valueOf(dc.solution_type));
            cv.put(RecentTable.STICKER_DATA_POSITION, Integer.valueOf(dc.sticker_data_position));
            cv.put(RecentTable.CONFIG_FILE_PATH, dc.configFile);
            try {
                this.mDB.beginTransaction();
                if (hasRecord(dc)) {
                    this.mDB.update(RecentTable.TABLE_NAME, cv, "_sticker_id =? ", new String[]{dc.sticker_id});
                } else {
                    this.mDB.insert(RecentTable.TABLE_NAME, null, cv);
                }
                this.mDB.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.mDB.endTransaction();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0095 A:{Catch:{ Exception -> 0x00af, all -> 0x00d7 }} */
    public void deleteRecord(java.lang.String r14) {
        /*
        r13 = this;
        r0 = android.text.TextUtils.isEmpty(r14);
        if (r0 != 0) goto L_0x00be;
    L_0x0006:
        r0 = r13.mDB;
        if (r0 == 0) goto L_0x00be;
    L_0x000a:
        r0 = r13.mDB;
        r0 = r0.isOpen();
        if (r0 == 0) goto L_0x00be;
    L_0x0012:
        r0 = "RecentDBHelper";
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "path = ";
        r1 = r1.append(r2);
        r1 = r1.append(r14);
        r1 = r1.toString();
        com.lge.camera.util.CamLog.m5e(r0, r1);
        r9 = 0;
        r0 = r13.mDB;	 Catch:{ Exception -> 0x00af }
        r1 = "Recent_Sticker_Table";
        r2 = com.lge.camera.managers.ext.sticker.recentdb.RecentTable.COLUMNS;	 Catch:{ Exception -> 0x00af }
        r3 = 0;
        r4 = 0;
        r5 = 0;
        r6 = 0;
        r7 = "_last_used_time DESC";
        r8 = 0;
        r9 = r0.query(r1, r2, r3, r4, r5, r6, r7, r8);	 Catch:{ Exception -> 0x00af }
        r0 = r13.mDB;	 Catch:{ Exception -> 0x00af }
        if (r0 == 0) goto L_0x00bf;
    L_0x0040:
        r0 = r13.mDB;	 Catch:{ Exception -> 0x00af }
        r0 = r0.isOpen();	 Catch:{ Exception -> 0x00af }
        if (r0 == 0) goto L_0x00bf;
    L_0x0048:
        r12 = 0;
        if (r9 == 0) goto L_0x0083;
    L_0x004b:
        r0 = r9.getCount();	 Catch:{ Exception -> 0x00af }
        if (r0 <= 0) goto L_0x0083;
    L_0x0051:
        r0 = r9.moveToFirst();	 Catch:{ Exception -> 0x00af }
        if (r0 == 0) goto L_0x0083;
    L_0x0057:
        r12 = new java.util.ArrayList;	 Catch:{ Exception -> 0x00af }
        r12.<init>();	 Catch:{ Exception -> 0x00af }
    L_0x005c:
        r0 = "_config_file_path";
        r0 = r9.getColumnIndex(r0);	 Catch:{ Exception -> 0x00af }
        r0 = r9.getString(r0);	 Catch:{ Exception -> 0x00af }
        r0 = r0.startsWith(r14);	 Catch:{ Exception -> 0x00af }
        if (r0 == 0) goto L_0x007d;
    L_0x006c:
        r0 = "_id";
        r0 = r9.getColumnIndex(r0);	 Catch:{ Exception -> 0x00af }
        r0 = r9.getInt(r0);	 Catch:{ Exception -> 0x00af }
        r0 = java.lang.Integer.valueOf(r0);	 Catch:{ Exception -> 0x00af }
        r12.add(r0);	 Catch:{ Exception -> 0x00af }
    L_0x007d:
        r0 = r9.moveToNext();	 Catch:{ Exception -> 0x00af }
        if (r0 != 0) goto L_0x005c;
    L_0x0083:
        if (r12 == 0) goto L_0x00bf;
    L_0x0085:
        r0 = r12.size();	 Catch:{ Exception -> 0x00af }
        if (r0 <= 0) goto L_0x00bf;
    L_0x008b:
        r0 = r12.iterator();	 Catch:{ Exception -> 0x00af }
    L_0x008f:
        r1 = r0.hasNext();	 Catch:{ Exception -> 0x00af }
        if (r1 == 0) goto L_0x00bf;
    L_0x0095:
        r11 = r0.next();	 Catch:{ Exception -> 0x00af }
        r11 = (java.lang.Integer) r11;	 Catch:{ Exception -> 0x00af }
        r1 = r13.mDB;	 Catch:{ Exception -> 0x00af }
        r2 = "Recent_Sticker_Table";
        r3 = "_id =? ";
        r4 = 1;
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x00af }
        r5 = 0;
        r6 = r11.toString();	 Catch:{ Exception -> 0x00af }
        r4[r5] = r6;	 Catch:{ Exception -> 0x00af }
        r1.delete(r2, r3, r4);	 Catch:{ Exception -> 0x00af }
        goto L_0x008f;
    L_0x00af:
        r10 = move-exception;
        r10.printStackTrace();	 Catch:{ all -> 0x00d7 }
        if (r9 == 0) goto L_0x00be;
    L_0x00b5:
        r0 = r9.isClosed();
        if (r0 != 0) goto L_0x00be;
    L_0x00bb:
        r9.close();
    L_0x00be:
        return;
    L_0x00bf:
        if (r9 == 0) goto L_0x00cb;
    L_0x00c1:
        r0 = r9.isClosed();	 Catch:{ Exception -> 0x00af }
        if (r0 != 0) goto L_0x00cb;
    L_0x00c7:
        r9.close();	 Catch:{ Exception -> 0x00af }
        r9 = 0;
    L_0x00cb:
        if (r9 == 0) goto L_0x00be;
    L_0x00cd:
        r0 = r9.isClosed();
        if (r0 != 0) goto L_0x00be;
    L_0x00d3:
        r9.close();
        goto L_0x00be;
    L_0x00d7:
        r0 = move-exception;
        if (r9 == 0) goto L_0x00e3;
    L_0x00da:
        r1 = r9.isClosed();
        if (r1 != 0) goto L_0x00e3;
    L_0x00e0:
        r9.close();
    L_0x00e3:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.sticker.recentdb.RecentDBHelper.deleteRecord(java.lang.String):void");
    }

    public static synchronized void removeInstance() {
        synchronized (RecentDBHelper.class) {
            if (sInstance != null) {
                sInstance.closeDB();
            }
            sInstance = null;
        }
    }
}
