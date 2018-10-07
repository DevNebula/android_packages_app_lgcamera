package com.lge.graphy.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.lge.camera.managers.GraphyDataManager;
import java.util.HashMap;

public class GraphyImageItem extends GraphyItem {
    private HashMap<String, Object> mMap;

    public GraphyImageItem(Context context) {
        super(context);
        this.mMap = new HashMap();
        this.mType = 1;
    }

    public int getIntValue(String key) {
        if (this.mMap != null) {
            Object value = this.mMap.get(key);
            if (value != null && (value instanceof Integer)) {
                return ((Integer) value).intValue();
            }
        }
        return -1;
    }

    public void setIntValue(String key, int value) {
        if (this.mMap != null) {
            this.mMap.put(key, Integer.valueOf(value));
        }
    }

    public String getStringValue(String key) {
        if (this.mMap != null) {
            Object value = this.mMap.get(key);
            if (value != null && (value instanceof String)) {
                return (String) value;
            }
        }
        return null;
    }

    public void setStringValue(String key, String value) {
        if (this.mMap != null) {
            this.mMap.put(key, value);
        }
    }

    public Bitmap getBitmap() {
        ContentResolver cr = this.mContext.getContentResolver();
        Uri uri = Uri.parse("content://com.lge.graphy.mobile/GraFilter_Contents");
        String[] projection = new String[]{GraphyDataManager.COLUMN_IMAGE_FILE_PATH};
        StringBuilder selection = new StringBuilder();
        selection.append("Idx=" + getIntValue("_id"));
        Cursor cursor = cr.query(uri, projection, selection.toString(), null, null);
        Bitmap bitmap = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            byte[] rawData = cursor.getBlob(cursor.getColumnIndex(GraphyDataManager.COLUMN_IMAGE_FILE_PATH));
            bitmap = BitmapFactory.decodeByteArray(rawData, 0, rawData.length);
        }
        if (!(cursor == null || cursor.isClosed())) {
            cursor.close();
        }
        return bitmap;
    }

    public double getDoubleValue(String key) {
        if (this.mMap != null) {
            Object value = this.mMap.get(key);
            if (value != null && (value instanceof Double)) {
                return ((Double) value).doubleValue();
            }
        }
        return -100.0d;
    }

    public void setDoubleValue(String key, double value) {
        if (this.mMap != null) {
            this.mMap.put(key, Double.valueOf(value));
        }
    }
}
