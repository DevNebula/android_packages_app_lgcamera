package com.lge.camera.managers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore.Images.Media;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.ExifInterfaceBase;
import com.lge.camera.file.Rational;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.MathUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.graphy.data.GraphyFunctionItem;
import com.lge.graphy.data.GraphyImageItem;
import com.lge.graphy.data.GraphyItem;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import org.json.JSONException;
import org.json.JSONObject;

public class GraphyDataManager extends ManagerInterfaceImpl {
    private static final int BEST_COUNT = 16;
    public static final String COLUMN_BEST = "Best";
    public static final String COLUMN_CATEGORY = "Category";
    public static final String COLUMN_CONTENTS = "Contents";
    public static final String COLUMN_EXIF = "EXIF";
    public static final String COLUMN_FILTER = "Filter";
    public static final String COLUMN_IDX = "Idx";
    public static final String COLUMN_IMAGE_FILE_NAME = "ImageFileName";
    public static final String COLUMN_IMAGE_FILE_PATH = "ImageFilePath";
    public static final String COLUMN_IMAGE_FILE_PATH_URL = "ImageFilePathURL";
    public static final String COLUMN_ISO = "ISO";
    public static final String COLUMN_MAKER = "Maker";
    public static final String COLUMN_MAKERNOTE = "MakerNote";
    public static final String COLUMN_REGDATE = "regdate";
    public static final String COLUMN_SS = "SS";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_UPDATE_DATE = "UpdateDate";
    public static final String COLUMN_WB = "WB";
    public static final String GRAPHY_CLASS_NAME = "com.lge.graphy.mobile.scene.lens.LensActivity";
    public static final String GRAPHY_DB_PATH = "GraFilter_Contents";
    public static final String GRAPHY_PACKAGE_NAME = "com.lge.graphy.mobile";
    public static final String KEY_APERTURE_FOR_JASON = "Aperture Value";
    public static final String KEY_MODEL_FOR_JASON = "Model";
    public static final String KEY_USERCOMMENT_FOR_JASON = "User Comment";
    public static final String KEY_WB_FOR_JASON = "White Balance Mode";
    private static final int MAX_IMAGE_COUNT = 30;
    private static final int WB_OFFSET = 56;
    private int mBestCount = 0;
    private GraphyInterface mGraphyGet = null;
    private ArrayList<GraphyItem> mGraphyItems = null;
    private boolean mIsFromGraphy = false;
    private boolean mMaxImageStatus = false;
    private int mMyFilterCount = 0;
    private int mMyFilterIdx = -1;
    private ArrayList<GraphyItem> mRemovedBestItems = null;
    private ArrayList<GraphyItem> mRemovedMyFilterItems = null;

    /* renamed from: com.lge.camera.managers.GraphyDataManager$1 */
    class C09751 extends Thread {
        C09751() {
        }

        public void run() {
            GraphyDataManager.this.cancelToAddMyFilterItem();
            GraphyDataManager.this.mIsFromGraphy = false;
            GraphyDataManager.this.mGraphyGet.setFromGraphyFlag(false);
            GraphyDataManager.this.mGraphyGet.setGraphyIndex(-1);
            GraphyDataManager.this.mMyFilterIdx = -1;
        }
    }

    class GraphyDataGetter extends AsyncTask<Void, Void, ArrayList<GraphyItem>> {
        private boolean mRequery = false;

        public GraphyDataGetter(boolean requery) {
            this.mRequery = requery;
        }

        protected ArrayList<GraphyItem> doInBackground(Void... arg0) {
            if (this.mRequery) {
                GraphyDataManager.this.mIsFromGraphy = false;
                GraphyDataManager.this.cancelToAddMyFilterItem();
            }
            ArrayList<GraphyItem> items = new ArrayList();
            GraphyDataManager.this.addGraphyImageItems(items);
            GraphyDataManager.this.addNoneFunctionItem(items);
            GraphyDataManager.this.addGraphyFunctionItem(items);
            return items;
        }

        protected void onPostExecute(ArrayList<GraphyItem> result) {
            GraphyDataManager.this.mGraphyItems = result;
            if (!GraphyDataManager.this.mIsFromGraphy && !GraphyDataManager.this.mGraphyGet.isFromLDU()) {
                GraphyDataManager.this.mGraphyGet.setGraphyItems(GraphyDataManager.this.mGraphyItems);
                if (this.mRequery) {
                    GraphyDataManager.this.mGraphyGet.setGraphyButtonVisiblity(true);
                    GraphyDataManager.this.mGraphyGet.setGraphyListVisibility(true, false);
                }
            } else if ("off".equals(GraphyDataManager.this.mGraphyGet.getSettingValue(Setting.KEY_GRAPHY))) {
                GraphyDataManager.this.mGraphyGet.setSetting(Setting.KEY_GRAPHY, "on", true);
                if (GraphyDataManager.this.mGraphyGet.isFromLDU()) {
                    GraphyDataManager.this.mGet.postOnUiThread(new HandlerRunnable(GraphyDataManager.this) {
                        public void handleRun() {
                            GraphyDataManager.this.selectMyFilterItem();
                        }
                    }, 300);
                } else {
                    GraphyDataManager.this.mGet.showDialog(15, GraphyDataManager.this.mMaxImageStatus);
                }
            } else {
                SharedPreferences pref = GraphyDataManager.this.mGet.getActivity().getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
                if (!GraphyDataManager.this.mMaxImageStatus || pref == null || pref.getBoolean(CameraConstantsEx.GRAPHY_ITEM_LIMIT_DO_NOT_SHOW_SAVE_NOTE, false)) {
                    GraphyDataManager.this.mGet.postOnUiThread(new HandlerRunnable(GraphyDataManager.this) {
                        public void handleRun() {
                            GraphyDataManager.this.selectMyFilterItem();
                        }
                    }, 300);
                } else {
                    GraphyDataManager.this.mGet.showDialog(16, false);
                }
            }
        }
    }

    public GraphyDataManager(GraphyInterface moduleInterface) {
        super(moduleInterface);
        this.mGraphyGet = moduleInterface;
        CamLog.m3d(CameraConstants.TAG, "[Graphy] GraphyDataManager created");
    }

    public void onResumeAfter() {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] onResumeAfter");
        super.onResumeAfter();
        Intent intent = this.mGet.getActivity().getIntent();
        if (this.mGraphyGet.isFromGraphyApp()) {
            this.mIsFromGraphy = true;
            this.mMyFilterIdx = this.mGraphyGet.getGraphyIndex();
            if (this.mMyFilterIdx == -1) {
                this.mMyFilterIdx = intent.getIntExtra("graphy_idx", -1);
            }
            CamLog.m3d(CameraConstants.TAG, "[Graphy] mMyFilterIdx : " + this.mMyFilterIdx);
        }
        if (FunctionProperties.isSupportedGraphy()) {
            new GraphyDataGetter(false).execute(new Void[0]);
        }
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        if (this.mIsFromGraphy && this.mGraphyGet.isRotateDialogVisible(16)) {
            new C09751().start();
            return;
        }
        this.mIsFromGraphy = false;
        this.mGraphyGet.setFromGraphyFlag(false);
        this.mGraphyGet.setGraphyIndex(-1);
        this.mMyFilterIdx = -1;
    }

    private ArrayList<GraphyItem> addGraphyImageItems(ArrayList<GraphyItem> items) {
        StringBuilder selection = new StringBuilder();
        selection.append("Filter=1");
        Cursor cursor = getGraphyCursor(selection.toString(), 30);
        if (cursor != null && cursor.getCount() > 0) {
            createGraphyImageItemListByCursor(items, cursor, 2);
        }
        if (cursor != null) {
            cursor.close();
        }
        addBestGraphyItems(items);
        if (items.size() > 30) {
            this.mMaxImageStatus = true;
            while (items.size() > 30) {
                items.remove(items.size() - 1);
            }
        } else {
            this.mMaxImageStatus = false;
        }
        return items;
    }

    private ArrayList<GraphyItem> addDefaultBestGraphyItems(ArrayList<GraphyItem> items) {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] addDefaultBestGraphyItems");
        int[] defaultImageResId = new int[]{C0088R.drawable.graphy_005001_sample0, C0088R.drawable.graphy_005001_sample1, C0088R.drawable.graphy_001001_sample2, C0088R.drawable.graphy_010002_sample3, C0088R.drawable.graphy_014002_sample4, C0088R.drawable.graphy_003003_sample5, C0088R.drawable.graphy_014003_sample6, C0088R.drawable.graphy_010005_sample7, C0088R.drawable.graphy_010005_sample8, C0088R.drawable.graphy_008004_sample9, C0088R.drawable.graphy_100004_sample10, C0088R.drawable.graphy_100004_sample11, C0088R.drawable.graphy_003007_sample12, C0088R.drawable.graphy_004007_sample13, C0088R.drawable.graphy_011006_sample14, C0088R.drawable.graphy_004006_sample15};
        int length = defaultImageResId.length;
        int i = 0;
        int defaultDataIndex = 0;
        while (i < length) {
            int resId = defaultImageResId[i];
            int defaultDataIndex2 = defaultDataIndex + 1;
            CamLog.m11w(CameraConstants.TAG, "[graphy] data number : " + defaultDataIndex);
            GraphyImageItem item = createGraphyImageItemByResId(resId);
            if (item != null) {
                items.add(item);
            }
            i++;
            defaultDataIndex = defaultDataIndex2;
        }
        return items;
    }

    private ArrayList<GraphyItem> addNoneFunctionItem(ArrayList<GraphyItem> items) {
        GraphyFunctionItem noneItem = new GraphyFunctionItem(getAppContext(), 3);
        noneItem.setIntValue(GraphyItem.KEY_CATEGORY_ID_INT, 0);
        items.add(0, noneItem);
        return items;
    }

    private ArrayList<GraphyItem> addBestGraphyItems(ArrayList<GraphyItem> items) {
        StringBuilder selection = new StringBuilder();
        selection.append("Best=1");
        int oldCount = items.size();
        Cursor cursor = getGraphyCursor(selection.toString(), 16);
        if (cursor == null || cursor.getCount() <= 0) {
            addDefaultBestGraphyItems(items);
        } else {
            createGraphyImageItemListByCursor(items, cursor, 1);
        }
        if (cursor != null) {
            cursor.close();
        }
        this.mBestCount = items.size() - oldCount;
        return items;
    }

    private ArrayList<GraphyItem> addMyFilterGraphyItems(ArrayList<GraphyItem> items) {
        StringBuilder selection = new StringBuilder();
        selection.append("Filter=1");
        Cursor cursor = getGraphyCursor(selection.toString(), 31);
        if (cursor != null && cursor.getCount() > 0) {
            GraphyFunctionItem myFilterCategory = new GraphyFunctionItem(getAppContext(), 0);
            myFilterCategory.setIntValue(GraphyItem.KEY_CATEGORY_ID_INT, 2);
            items.add(myFilterCategory);
            int oldCount = items.size();
            createGraphyImageItemListByCursor(items, cursor, 2);
            this.mMyFilterCount = items.size() - oldCount;
        }
        if (cursor != null) {
            cursor.close();
        }
        if (this.mMyFilterCount > 30) {
            this.mMaxImageStatus = true;
        } else {
            this.mMaxImageStatus = false;
        }
        return items;
    }

    private ArrayList<GraphyItem> addRecentGraphyItems(ArrayList<GraphyItem> items) {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] addRecentGraphyItems");
        Uri baseImageUri = Media.getContentUri(CameraConstants.STORAGE_NAME_EXTERNAL);
        String[] imageProjection = new String[]{"_id", "_data"};
        Cursor cursor = getAppContext().getContentResolver().query(baseImageUri, imageProjection, null, null, "date_added DESC LIMIT 50");
        CamLog.m3d(CameraConstants.TAG, "[Graphy] cursor size : " + cursor.getCount());
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                CamLog.m3d(CameraConstants.TAG, "[Graphy] path : " + cursor.getString(1));
                items.add(createGraphyImageItemByPath(cursor.getString(1)));
                if (!cursor.moveToNext()) {
                    break;
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return items;
    }

    private ArrayList<GraphyItem> addGraphyFunctionItem(ArrayList<GraphyItem> items) {
        GraphyFunctionItem noneItem = new GraphyFunctionItem(getAppContext(), 2);
        noneItem.setIntValue(GraphyItem.KEY_CATEGORY_ID_INT, 3);
        items.add(noneItem);
        return items;
    }

    public void attachBestImageItems() {
        if (this.mGraphyItems != null && this.mRemovedBestItems != null && this.mRemovedBestItems.size() != 0) {
            int offset = getCategoryPosition(1) + 1;
            int end = offset + this.mBestCount;
            if (this.mBestCount > 0) {
                for (int i = offset; i < end; i++) {
                    this.mGraphyItems.add(i, this.mRemovedBestItems.remove(0));
                }
                this.mGraphyGet.spreadImageItems(offset, this.mBestCount, 1);
            }
        }
    }

    public void removeBestImageItems() {
        if (this.mGraphyItems != null) {
            int offset = getCategoryPosition(1) + 1;
            int end = offset + this.mBestCount;
            if (this.mBestCount > 0) {
                this.mRemovedBestItems = new ArrayList();
                for (int i = offset; i < end; i++) {
                    this.mRemovedBestItems.add((GraphyItem) this.mGraphyItems.remove(offset));
                }
                this.mGraphyGet.foldImageItems(offset, this.mBestCount, 1);
            }
        }
    }

    public void attachMyFilterImageItems() {
        if (this.mGraphyItems != null && this.mRemovedMyFilterItems != null && this.mRemovedMyFilterItems.size() != 0) {
            int offset = getCategoryPosition(2) + 1;
            int end = offset + this.mMyFilterCount;
            if (this.mMyFilterCount > 0) {
                for (int i = offset; i < end; i++) {
                    this.mGraphyItems.add(i, this.mRemovedMyFilterItems.remove(0));
                }
                this.mGraphyGet.spreadImageItems(offset, this.mMyFilterCount, 2);
            }
        }
    }

    public void removeMyFilterImageItems() {
        if (this.mGraphyItems != null) {
            int offset = getCategoryPosition(2) + 1;
            int end = offset + this.mMyFilterCount;
            if (this.mMyFilterCount > 0) {
                this.mRemovedMyFilterItems = new ArrayList();
                for (int i = offset; i < end; i++) {
                    this.mRemovedMyFilterItems.add((GraphyItem) this.mGraphyItems.remove(offset));
                }
                this.mGraphyGet.foldImageItems(offset, this.mMyFilterCount, 2);
            }
        }
    }

    private int getCategoryPosition(int category) {
        int position = 0;
        Iterator it = this.mGraphyItems.iterator();
        while (it.hasNext()) {
            GraphyItem item = (GraphyItem) it.next();
            int type = item.getType();
            int categoryId = item.getIntValue(GraphyItem.KEY_CATEGORY_ID_INT);
            if (type == 0 && categoryId == category) {
                break;
            }
            position++;
        }
        return position;
    }

    private Cursor getGraphyCursor(String selection, int limit) {
        return getAppContext().getContentResolver().query(Uri.parse("content://com.lge.graphy.mobile/GraFilter_Contents"), null, selection.toString(), null, "UpdateDate desc limit " + limit);
    }

    public ArrayList<GraphyItem> getGraphyItems() {
        return this.mGraphyItems;
    }

    private GraphyImageItem createGraphyImageItemByPath(String path) {
        GraphyImageItem item = createGraphyImageItem(Exif.readExif(path));
        if (item != null) {
            item.setStringValue(GraphyItem.KEY_IMAGE_PATH_STR, path);
        }
        return item;
    }

    private byte[] getByteArrayFromResId(int resId) {
        AssetFileDescriptor fd = getAppContext().getResources().openRawResourceFd(resId);
        byte[] jpegData = new byte[((int) fd.getLength())];
        try {
            if (fd.createInputStream().read(jpegData) < 0) {
                CamLog.m5e(CameraConstants.TAG, "[Graphy] fail to load image resource");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jpegData;
    }

    private ArrayList<GraphyItem> createGraphyImageItemListByCursor(ArrayList<GraphyItem> items, Cursor cursor, int type) {
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            String wbType = getWBType(cursor.getString(cursor.getColumnIndex(COLUMN_EXIF)));
            float wb = 0.0f;
            try {
                wb = ByteBuffer.wrap(cursor.getBlob(cursor.getColumnIndex(COLUMN_WB))).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            } catch (Exception e) {
                e.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, e.getMessage());
            }
            String shutterSpeed = parsingShutterSpeed(cursor.getString(cursor.getColumnIndex(COLUMN_SS)));
            String categoryPrefix = parsingCategoryPrefix(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_FILE_NAME)), true);
            String luxPrefix = parsingCategoryPrefix(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_FILE_NAME)), false);
            String userComment = getJsonValue(cursor.getString(cursor.getColumnIndex(COLUMN_EXIF)), KEY_USERCOMMENT_FOR_JASON);
            String angle = "";
            if (userComment != null) {
                angle = Character.toString(userComment.trim().charAt(0));
            }
            String aperture = parsingAperture(getJsonValue(cursor.getString(cursor.getColumnIndex(COLUMN_EXIF)), KEY_APERTURE_FOR_JASON));
            String iso = cursor.getString(cursor.getColumnIndex("ISO"));
            CamLog.m3d(CameraConstants.TAG, "[Graphy] wbType : " + wbType);
            CamLog.m3d(CameraConstants.TAG, "[Graphy] idx : " + cursor.getString(cursor.getColumnIndex(COLUMN_IDX)));
            CamLog.m3d(CameraConstants.TAG, "[Graphy] iso : " + iso);
            CamLog.m3d(CameraConstants.TAG, "[Graphy] ss : " + shutterSpeed);
            CamLog.m3d(CameraConstants.TAG, "[Graphy] wb : " + wb);
            CamLog.m3d(CameraConstants.TAG, "[Graphy] aperture : " + aperture);
            GraphyImageItem item = new GraphyImageItem(getAppContext());
            CamLog.m3d(CameraConstants.TAG, "[Graphy] SS float : " + MathUtil.parseStringToFloat(shutterSpeed) + ", ISO float : " + Float.parseFloat(iso) + ", f-number Float : " + Float.parseFloat(aperture));
            CamLog.m3d(CameraConstants.TAG, "[Graphy] categoryPrefix : " + categoryPrefix + ", category name : " + item.getCategory(categoryPrefix) + ", luxPrefix : " + luxPrefix);
            item.setIntValue("_id", cursor.getInt(cursor.getColumnIndex(COLUMN_IDX)));
            item.setStringValue(GraphyItem.KEY_ISO_STR, cursor.getString(cursor.getColumnIndex("ISO")));
            item.setStringValue(GraphyItem.KEY_SHUTTER_SPEED_STR, shutterSpeed);
            item.setStringValue(GraphyItem.KEY_WB_TYPE_STR, wbType);
            item.setStringValue(GraphyItem.KEY_WB_STR, Integer.valueOf((int) wb).toString());
            item.setStringValue(GraphyItem.KEY_ANGLE, angle);
            if (!(ModelProperties.isJoanRenewal() || ModelProperties.isFakeMode())) {
                item.setStringValue(GraphyItem.KEY_CATEGORY_PREFIX, categoryPrefix);
                item.setStringValue(GraphyItem.KEY_LUX, luxPrefix);
                item.setDoubleValue(GraphyItem.KEY_ILLUMINANCE, ManualUtil.calculateIlluminance(Float.parseFloat(iso), MathUtil.parseStringToFloat(shutterSpeed), Float.parseFloat(aperture)));
                item.setSelected(false);
            }
            item.setStringValue(GraphyItem.KEY_APERTURE, aperture);
            item.setIntValue(GraphyItem.KEY_CATEGORY_ID_INT, type);
            items.add(item);
            if (!cursor.moveToNext()) {
                break;
            }
        }
        return items;
    }

    private GraphyImageItem createGraphyImageItemByResId(int resId) {
        byte[] jpegData = getByteArrayFromResId(resId);
        if (jpegData == null) {
            return null;
        }
        GraphyImageItem item = createGraphyImageItem(Exif.readExif(jpegData), resId);
        if (item == null) {
            return item;
        }
        item.setIntValue(GraphyItem.KEY_RESOURCE_ID_INT, resId);
        return item;
    }

    private GraphyImageItem createGraphyImageItem(ExifInterface exif) {
        CamLog.m11w(CameraConstants.TAG, "[graphy] call createGraphyImageItem. resId = -1 ");
        return createGraphyImageItem(exif, -1);
    }

    private GraphyImageItem createGraphyImageItem(ExifInterface exif, int resId) {
        if (exif == null) {
            return null;
        }
        String iso = getISO(exif);
        String shutterSpeed = getShutterSpeed(exif);
        String wbType = getWBType(exif);
        String wb = getWhiteBalance(exif);
        String aperture = getAperture(exif);
        String userComment = exif.getUserComment(ExifInterfaceBase.getTrueTagKey(ExifInterface.TAG_USER_COMMENT));
        char angleMode = 0;
        if (userComment != null) {
            angleMode = userComment.trim().charAt(0);
        }
        String fileName = getAppContext().getResources().getResourceEntryName(resId);
        String categoryPrefix = fileName.substring(7, 10);
        String luxPrefix = fileName.substring(10, 13);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] iso : " + iso);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] shutterSpeed : " + shutterSpeed);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] wbType : " + wbType);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] wb : " + wb);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] userComment : " + userComment);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] aperture : " + aperture);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] angleMode : " + angleMode);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] SS float : " + MathUtil.parseStringToFloat(shutterSpeed) + ", ISO float : " + Float.parseFloat(iso) + ", f-number Float : " + Float.parseFloat(aperture));
        GraphyImageItem item = new GraphyImageItem(getAppContext());
        item.setStringValue(GraphyItem.KEY_ISO_STR, iso);
        item.setStringValue(GraphyItem.KEY_SHUTTER_SPEED_STR, shutterSpeed);
        item.setStringValue(GraphyItem.KEY_WB_TYPE_STR, wbType);
        item.setStringValue(GraphyItem.KEY_WB_STR, wb);
        item.setStringValue(GraphyItem.KEY_ANGLE, Character.toString(angleMode));
        if (!(ModelProperties.isJoanRenewal() || ModelProperties.isFakeMode())) {
            item.setStringValue(GraphyItem.KEY_CATEGORY_PREFIX, categoryPrefix);
            item.setSelected(false);
            item.setStringValue(GraphyItem.KEY_LUX, luxPrefix);
            item.setDoubleValue(GraphyItem.KEY_ILLUMINANCE, ManualUtil.calculateIlluminance(Float.parseFloat(iso), MathUtil.parseStringToFloat(shutterSpeed), Float.parseFloat(aperture)));
            CamLog.m3d(CameraConstants.TAG, "[Graphy] fileName : " + fileName + ", prefix : " + categoryPrefix + ", category name : " + item.getCategory(categoryPrefix) + ", lux : " + luxPrefix);
        }
        item.setStringValue(GraphyItem.KEY_APERTURE, aperture);
        return item;
    }

    private String getISO(ExifInterface exif) {
        Integer iso = exif.getTagIntValue(ExifInterface.TAG_ISO_SPEED_RATINGS);
        if (iso == null) {
            return null;
        }
        return iso.toString();
    }

    private String getWhiteBalance(ExifInterface exif) {
        byte[] makerNoteByte = exif.getTagByteValues(ExifInterface.TAG_MAKER_NOTE);
        if (makerNoteByte == null) {
            return null;
        }
        return Integer.valueOf((int) ByteBuffer.wrap(Arrays.copyOfRange(makerNoteByte, 56, 60)).order(ByteOrder.LITTLE_ENDIAN).getFloat()).toString();
    }

    private String getShutterSpeed(ExifInterface exif) {
        Rational shutterSpeedValueObj = exif.getTagRationalValue(ExifInterface.TAG_EXPOSURE_TIME);
        if (shutterSpeedValueObj == null) {
            return null;
        }
        String shutterSpeedValue = "1";
        if (shutterSpeedValueObj.getDenominator() != 1) {
            shutterSpeedValue = shutterSpeedValueObj.getNumerator() + "/" + shutterSpeedValueObj.getDenominator();
        } else {
            shutterSpeedValue = Long.toString(shutterSpeedValueObj.getNumerator());
        }
        return makeIrreducibleFraction(shutterSpeedValue);
    }

    private String getModelName(ExifInterface exif) {
        if (exif == null) {
            return null;
        }
        return exif.getTagStringValue(ExifInterface.TAG_MODEL);
    }

    private String getAperture(ExifInterface exif) {
        if (exif == null) {
            return null;
        }
        Rational aperture = exif.getTagRationalValue(ExifInterface.TAG_F_NUMBER);
        if (aperture != null) {
            return Float.toString(((float) Math.round((((float) aperture.getNumerator()) / ((float) aperture.getDenominator())) * 10.0f)) / 10.0f);
        }
        return null;
    }

    private String parsingAperture(String aperture) {
        try {
            aperture = aperture.toLowerCase().replace("f/", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        CamLog.m3d(CameraConstants.TAG, "[Graphy] parsed aperture : " + aperture);
        return aperture;
    }

    private String parsingShutterSpeed(String shutterSpeed) {
        try {
            shutterSpeed = shutterSpeed.toLowerCase().replace("sec", "");
            String[] rational = shutterSpeed.split("/");
            if (rational.length == 1) {
                float value = Float.valueOf(rational[0]).floatValue();
                if (value < 1.0f) {
                    shutterSpeed = "1/" + ((int) (1.0f / value));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        CamLog.m3d(CameraConstants.TAG, "[Graphy] parsed shutter spped : " + shutterSpeed);
        return shutterSpeed;
    }

    private String parsingCategoryPrefix(String imageFileName, boolean isCategory) {
        if (imageFileName == null) {
            return null;
        }
        String prefix = null;
        if (isCategory) {
            try {
                prefix = imageFileName.substring(0, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            prefix = imageFileName.substring(3, 6);
        }
        CamLog.m3d(CameraConstants.TAG, "[Graphy] isCategory ? : " + isCategory + ", parsed preFix code : " + prefix);
        return prefix;
    }

    private String makeIrreducibleFraction(String shutterSpeed) {
        try {
            shutterSpeed = shutterSpeed.toLowerCase().replace("sec", "");
            String[] rational = shutterSpeed.split("/");
            if (rational.length > 1 && Integer.valueOf(rational[0]).intValue() != 1 && Integer.valueOf(rational[0]).intValue() < Integer.valueOf(rational[1]).intValue()) {
                shutterSpeed = "1/" + Math.round(Float.valueOf(rational[1]).floatValue() / Float.valueOf(rational[0]).floatValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        CamLog.m3d(CameraConstants.TAG, "[Graphy] parsed shutter spped : " + shutterSpeed);
        return shutterSpeed;
    }

    private String getWBType(ExifInterface exif) {
        Integer wbTypeValue = exif.getTagIntValue(ExifInterface.TAG_WHITE_BALANCE);
        if (wbTypeValue == null) {
            return "manual";
        }
        CamLog.m3d(CameraConstants.TAG, "[Graphy] wbTypeValue : " + wbTypeValue);
        if (wbTypeValue.intValue() == 0) {
            return "auto";
        }
        return "manual";
    }

    private String getWBType(String exif) {
        if (exif == null || exif.trim().equals("")) {
            return null;
        }
        String wbType = null;
        try {
            wbType = new JSONObject(exif).getString(KEY_WB_FOR_JASON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (wbType == null) {
            return "auto";
        }
        if (wbType.toLowerCase().contains("auto")) {
            return "auto";
        }
        return "manual";
    }

    private String getJsonValue(String exif, String key) {
        if (exif == null || exif.trim().equals("")) {
            return null;
        }
        String value = null;
        try {
            value = new JSONObject(exif).getString(key);
            CamLog.m3d(CameraConstants.TAG, "[Graphy] json value : " + value);
            return value;
        } catch (JSONException e) {
            e.printStackTrace();
            return value;
        }
    }

    private int getSelectedMyFilterPositionByIdx(int idx) {
        ListIterator<GraphyItem> iterator = this.mGraphyItems.listIterator();
        int position = -1;
        while (iterator.hasNext()) {
            int next_position = iterator.nextIndex();
            GraphyItem item = (GraphyItem) iterator.next();
            int categoryId = item.getIntValue(GraphyItem.KEY_CATEGORY_ID_INT);
            int itemIdx = item.getIntValue("_id");
            if (categoryId == 2 && idx == itemIdx) {
                position = next_position;
            }
        }
        return position;
    }

    public void selectMyFilterItem() {
        this.mGraphyGet.setGraphyItems(this.mGraphyItems);
        this.mGraphyGet.setGraphyButtonVisiblity(true);
        this.mGraphyGet.setGraphyListVisibility(true, false);
        int myFilterPosition = getSelectedMyFilterPositionByIdx(this.mMyFilterIdx);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] myFilterPosition : " + myFilterPosition);
        if (myFilterPosition > 0 && myFilterPosition < this.mGraphyItems.size()) {
            this.mGraphyGet.selectItem(myFilterPosition);
        }
    }

    public void requery() {
        if (FunctionProperties.isSupportedGraphy()) {
            new GraphyDataGetter(true).execute(new Void[0]);
        }
    }

    private void cancelToAddMyFilterItem() {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] cancel item idx : " + this.mMyFilterIdx);
        ContentResolver cr = getAppContext().getContentResolver();
        Uri uri = Uri.parse("content://com.lge.graphy.mobile/GraFilter_Contents");
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILTER, Integer.valueOf(0));
        cr.update(uri, values, "Idx = " + this.mMyFilterIdx, null);
    }
}
