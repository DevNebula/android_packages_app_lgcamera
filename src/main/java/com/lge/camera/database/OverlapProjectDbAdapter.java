package com.lge.camera.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;

public class OverlapProjectDbAdapter {
    public static final int COLUMN_EXTRA01 = 8;
    public static final int COLUMN_EXTRA02 = 9;
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_LIST_INDEX = 6;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_PRESET = 4;
    public static final int COLUMN_PROJECT_ID = 1;
    public static final int COLUMN_SAMPLE_PATH = 5;
    public static final int COLUMN_URI = 3;
    public static final int COLUMN_U_SAMPLE_CNT = 7;
    private static final String DATABASE_NAME = "project.db";
    private static final int DATABASE_VERSION = 1;
    public static final String KEY_EXTRA01 = "extra_01";
    public static final String KEY_EXTRA02 = "extra_02";
    public static final String KEY_ID = "_id";
    public static final String KEY_LIST_INDEX = "list_index";
    public static final String KEY_NAME = "name";
    public static final String KEY_PRESET = "preset";
    public static final String KEY_PROJECT_ID = "project_id";
    public static final String KEY_SAMPLE_PATH = "sample_path";
    public static final String KEY_URI = "uri";
    public static final String KEY_U_SAMPLE_CNT = "u_sample_cnt";
    private static final String PROJECT_TABLE = "project";
    public static final String SAMPLE_IMAGE = "user_sample";
    public static final int URI_DEFAULT_SAMPLE_CNT = 1;
    public static final String URI_OVERLAP = "overlap_project";
    public static final String URI_PARAM_PRESET = "preset";
    public static final String URI_PARAM_PRESET_SUB = "preset_sub";
    public static final String URI_PARAM_SAMPLE_PATH = "sample_path";
    public static final int USER_SAMPLE = -1;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mSqlDb;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE = "create table project (_id integer primary key autoincrement, project_id TEXT, name TEXT, uri TEXT, preset INTEGER, sample_path TEXT, list_index INTEGER, u_sample_cnt INTEGER, extra_01 TEXT, extra_02 TEXT); ";

        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS project");
            onCreate(db);
        }
    }

    public OverlapProjectDbAdapter(Context context) {
        this.mDbHelper = new DatabaseHelper(context, DATABASE_NAME, null, 1);
    }

    public void open() throws SQLException {
        try {
            this.mSqlDb = this.mDbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            CamLog.m5e(CameraConstants.TAG, "[Cell]DB open fail");
            this.mSqlDb = this.mDbHelper.getReadableDatabase();
        }
    }

    public void close() {
        this.mDbHelper.close();
    }

    public void onUpgrade() {
        this.mDbHelper.onUpgrade(this.mDbHelper.getWritableDatabase(), 1, 1);
    }

    public long insert(OverlapProjectDb projectDb) {
        long retVal = 0;
        ContentValues newChValues = new ContentValues();
        newChValues.put(KEY_PROJECT_ID, projectDb.getProjectId());
        newChValues.put("name", projectDb.getName());
        newChValues.put(KEY_URI, projectDb.getUri());
        newChValues.put("preset", Integer.valueOf(projectDb.getPreset()));
        newChValues.put("sample_path", projectDb.getSamplePath());
        newChValues.put(KEY_LIST_INDEX, Integer.valueOf(projectDb.getListIndex()));
        newChValues.put(KEY_U_SAMPLE_CNT, Integer.valueOf(projectDb.getUserSampleCnt()));
        try {
            return this.mSqlDb.insert(PROJECT_TABLE, null, newChValues);
        } catch (SQLiteException e) {
            CamLog.m5e(CameraConstants.TAG, "[Cell]DB insert fail");
            return retVal;
        }
    }

    public boolean updateListIndex(long id, int value) {
        ContentValues newValue = new ContentValues();
        newValue.put(KEY_LIST_INDEX, Integer.valueOf(value));
        try {
            return this.mSqlDb.update(PROJECT_TABLE, newValue, new StringBuilder().append("_id=").append(id).toString(), null) > 0;
        } catch (SQLiteException e) {
            CamLog.m5e(CameraConstants.TAG, "[Cell]DB update fail");
            return false;
        }
    }

    public boolean delete(long id) {
        try {
            return this.mSqlDb.delete(PROJECT_TABLE, new StringBuilder().append("_id=").append(id).toString(), null) > 0;
        } catch (SQLiteException e) {
            CamLog.m5e(CameraConstants.TAG, "[Cell]DB delete fail");
            return false;
        }
    }

    public boolean deleteAll() {
        try {
            return this.mSqlDb.delete(PROJECT_TABLE, null, null) > 0;
        } catch (SQLiteException e) {
            CamLog.m5e(CameraConstants.TAG, "[Cell]DB deleteAll fail");
            return false;
        }
    }

    private Cursor getAllLists() {
        Cursor c = null;
        try {
            c = this.mSqlDb.query(true, PROJECT_TABLE, null, null, null, null, null, "list_index ASC", null);
            if (c != null) {
                c.moveToFirst();
            }
        } catch (SQLiteException e) {
            CamLog.m5e(CameraConstants.TAG, "[Cell]getAllLists");
        }
        return c;
    }

    private Cursor getAllListsBySamplecnt() {
        Cursor c = null;
        try {
            c = this.mSqlDb.query(true, PROJECT_TABLE, null, null, null, null, null, "u_sample_cnt DESC", null);
            if (c != null) {
                c.moveToFirst();
            }
        } catch (SQLiteException e) {
            CamLog.m5e(CameraConstants.TAG, "[Cell]getAllListsBySamplecnt");
        }
        return c;
    }

    public int getCount() {
        Cursor cursor = getAllLists();
        if (cursor == null) {
            return 0;
        }
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void getProjectLists(ArrayList<OverlapProjectDb> projectList) {
        Cursor cursor = getAllLists();
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                do {
                    projectList.add(new OverlapProjectDb(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5), cursor.getInt(6), cursor.getInt(7)));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public String getProjectId(long id) {
        String name = "";
        Cursor c = null;
        try {
            c = this.mSqlDb.query(true, PROJECT_TABLE, null, "_id=" + id, null, null, null, null, null);
            if (c != null) {
                c.moveToFirst();
                name = c.getString(1);
            }
        } catch (SQLiteException e) {
            CamLog.m5e(CameraConstants.TAG, "[Cell]getProjectId");
        }
        if (c != null) {
            c.close();
        }
        return name;
    }

    public int getUSampleCnt() {
        int result = 0;
        Cursor cursor = getAllListsBySamplecnt();
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                result = cursor.getInt(7);
            } else {
                result = 0;
            }
            cursor.close();
        }
        return result;
    }
}
