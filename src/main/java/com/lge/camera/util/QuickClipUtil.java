package com.lge.camera.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.ShareActionProviderEx;
import com.lge.camera.C0088R;
import com.lge.camera.app.QuickclipFakeActivity;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.QuickClipConfig;
import com.lge.camera.database.OverlapProjectDbAdapter;
import com.lge.content.pm.PackageManagerEx;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class QuickClipUtil {
    public static final int ALL_LIST = 10000;
    public static final String GIF = "gif";
    public static final String IMAGE = "jpg";
    public static final String VIDEO = "mp4";
    public static final String VIDEO_3GPP = "3gpp";
    public static boolean sIsEnabled = true;

    public static class SharedContentData {
        public String mMimeType;
        public int mStorageType;
    }

    public static synchronized ArrayList<QuickClipSharedItem> getPreferSharedList(Context context, Intent intent, int maxListCount) {
        ArrayList<QuickClipSharedItem> sharedList;
        synchronized (QuickClipUtil.class) {
            LinkedHashMap<ResolveInfo, Float> sharableActivities = loadSharableActivities(context, intent);
            String mimeType = intent.getType();
            if (mimeType == null) {
                CamLog.m11w(CameraConstants.TAG, "type = " + mimeType);
                sharedList = null;
            } else {
                sharedList = makeSharedItemList(context, sharableActivities, maxListCount, mimeType);
                if (sharedList != null) {
                    for (int i = maxListCount; i < sharedList.size(); i++) {
                        sharedList.remove(i);
                    }
                }
            }
        }
        return sharedList;
    }

    public static ArrayList<QuickClipSharedItem> getPreferSharedList(Uri uri, Context context, int mListCount) {
        return getPreferSharedList(context, makeSharedIntent(uri, context), mListCount);
    }

    public static ArrayList<QuickClipSharedItem> getPreferSharedList(String contentType, Context context, int mListCount) {
        return getPreferSharedList(context, makeSharedIntent(contentType, context), mListCount);
    }

    public static void reportActivitySelected(Context context, String name) {
        new ShareActionProviderEx(context).activitySelected(name);
    }

    public static boolean launchSharedActivity(Context context, Uri uri, QuickClipSharedItem item) {
        Intent shareIntent = makeSharedIntent(uri, context);
        AppControlUtilBase.setLaunchingShareActivity(true);
        return launchSharedActivity(context, shareIntent, item);
    }

    public static boolean launchSharedActivity(Context context, Uri uri, QuickClipSharedItem item, boolean isUpload) {
        Intent shareIntent = makeSharedIntent(uri, context);
        AppControlUtilBase.setLaunchingShareActivity(true);
        return launchSharedActivity(context, shareIntent, item, isUpload);
    }

    public static boolean launchSharedActivity(Context context, Intent shareIntent, QuickClipSharedItem item) {
        if (shareIntent == null || item == null) {
            return false;
        }
        shareIntent.addFlags(185073664);
        shareIntent.setComponent(new ComponentName(item.mPackageName, item.mActivityName));
        AppControlUtilBase.setLaunchingShareActivity(true);
        context.startActivity(shareIntent);
        reportActivitySelected(context, item.mActivityName);
        return true;
    }

    public static boolean launchSharedActivity(Context context, Intent shareIntent, QuickClipSharedItem item, boolean isUpload) {
        if (shareIntent == null || item == null) {
            return false;
        }
        if (isUpload) {
            shareIntent.setComponent(new ComponentName(item.mPackageName, item.mActivityName));
            AppControlUtilBase.setLaunchingShareActivity(true);
            context.startActivity(shareIntent);
        }
        reportActivitySelected(context, item.mActivityName);
        return true;
    }

    public static Intent makeSharedIntent(Uri uri, Context context) {
        Intent shareIntent = new Intent("android.intent.action.SEND");
        shareIntent.setType(getMimeType(uri, context));
        shareIntent.putExtra("android.intent.extra.STREAM", uri);
        return shareIntent;
    }

    public static Intent makeSharedIntent(String contentType, Context context) {
        Intent shareIntent = new Intent("android.intent.action.SEND");
        shareIntent.setType(contentType);
        return shareIntent;
    }

    public static SharedContentData querySharedDate(Uri uri, Context context) {
        Cursor cursor = null;
        SharedContentData retData = new SharedContentData();
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"mime_type", "storage_type"}, null, null, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                retData.mStorageType = cursor.getInt(cursor.getColumnIndex("storage_type"));
                retData.mMimeType = cursor.getString(cursor.getColumnIndex("mime_type"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            CamLog.m6e(CameraConstants.TAG, "cursor error ", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return retData;
    }

    public static String getMimeType(Uri uri, Context context) {
        if (uri == null) {
            return "image/*";
        }
        return convertToSimpleMime(context.getApplicationContext().getContentResolver().getType(uri));
    }

    public static String convertToSimpleMime(String mimeType) {
        String type = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (IMAGE.equals(type) || GIF.equals(type)) {
            return "image/*";
        }
        if (VIDEO.equals(type) || VIDEO_3GPP.equals(type)) {
            return "video/*";
        }
        return null;
    }

    private static ArrayList<QuickClipSharedItem> makeSharedItemList(Context context, LinkedHashMap<ResolveInfo, Float> list, int maxCount, String type) {
        if (list == null || list.size() == 0 || type == null) {
            return null;
        }
        QuickClipConfig.loadDefinedSNSList(context);
        ArrayList<QuickClipSharedItem> sharedList = sort(context, list, maxCount, type);
        CamLog.m7i(CameraConstants.TAG, "requested Count " + maxCount + " shareList size " + sharedList.size());
        return sharedList;
    }

    private static LinkedHashMap<ResolveInfo, Float> loadSharableActivities(Context context, Intent intent) {
        try {
            ShareActionProviderEx shareActionProviderEx = new ShareActionProviderEx(context);
            shareActionProviderEx.setShareIntent(intent);
            return shareActionProviderEx.getSortedListAndWeight();
        } catch (Exception e) {
            return null;
        }
    }

    private static ArrayList<QuickClipSharedItem> sort(Context context, LinkedHashMap<ResolveInfo, Float> list, int maxCount, String type) {
        Object[] activitis;
        PackageManager pm = context.getPackageManager();
        PackageManagerEx pmEx = PackageManagerEx.getDefault();
        ArrayList<QuickClipSharedItem> sharedList = new ArrayList();
        int startIndex = 0;
        if ((SecureImageUtil.isSecureCamera() && SecureImageUtil.useSecureLockImage()) || ModelProperties.isNFCBlocked()) {
            activitis = list.keySet().toArray();
            int nfcIndex = getNFCIndex(activitis);
            if (nfcIndex != -1) {
                list.remove(activitis[nfcIndex]);
            }
        }
        activitis = list.keySet().toArray();
        if (list.size() < maxCount) {
            maxCount = list.size();
        }
        String defaultMessageApp = QuickClipConfig.getDefaultMessageApp(context);
        do {
            int compareIndex = findLastSameWeight(startIndex, activitis, list);
            if (startIndex == compareIndex) {
                int startIndex2 = startIndex + 1;
                sharedList.add(convertSharedItem(context, pm, pmEx, (ResolveInfo) activitis[startIndex]));
                startIndex = startIndex2;
            } else {
                sharedList = sortSameWeight(context, pm, pmEx, startIndex, compareIndex, activitis, sharedList, maxCount, type, defaultMessageApp);
                startIndex = sharedList.size();
            }
            if (startIndex >= maxCount) {
                break;
            }
        } while (startIndex < activitis.length - 1);
        return sharedList;
    }

    private static ArrayList<QuickClipSharedItem> sortSameWeight(Context context, PackageManager pm, PackageManagerEx pmEx, int startIndex, int compareIndex, Object[] activitis, ArrayList<QuickClipSharedItem> sharedList, int maxCount, String type, String defaultMessageApp) {
        ArrayList<String> snsList = QuickClipConfig.getSNSPackageList(type);
        if (snsList != null) {
            int i;
            Iterator it = snsList.iterator();
            loop0:
            while (it.hasNext()) {
                String preDefined = (String) it.next();
                if (QuickClipConfig.isMessageApp(preDefined)) {
                    preDefined = defaultMessageApp;
                }
                for (i = startIndex; i <= compareIndex; i++) {
                    ResolveInfo resolveInfo = activitis[i];
                    if (resolveInfo != null && preDefined.equals(resolveInfo.activityInfo.packageName)) {
                        sharedList.add(convertSharedItem(context, pm, pmEx, resolveInfo));
                        activitis[i] = null;
                        if (sharedList.size() == maxCount) {
                            break loop0;
                        }
                    }
                }
            }
            for (i = startIndex; i <= compareIndex; i++) {
                if (activitis[i] != null) {
                    sharedList.add(convertSharedItem(context, pm, pmEx, (ResolveInfo) activitis[i]));
                    if (sharedList.size() == maxCount) {
                        break;
                    }
                }
            }
        }
        return sharedList;
    }

    private static int findLastSameWeight(int startIndex, Object[] activitis, LinkedHashMap<ResolveInfo, Float> list) {
        for (int i = startIndex; i < activitis.length - 1; i++) {
            if (((Float) list.get(activitis[i])).floatValue() != ((Float) list.get(activitis[i + 1])).floatValue()) {
                return i;
            }
        }
        return activitis.length - 1;
    }

    private static QuickClipSharedItem convertSharedItem(Context context, PackageManager pm, PackageManagerEx pmEx, ResolveInfo resolveInfo) {
        String label = (String) resolveInfo.loadLabel(pm);
        String packageName = resolveInfo.activityInfo.packageName;
        String activityName = resolveInfo.activityInfo.name;
        Drawable appIcon = pm.getDrawable(packageName, resolveInfo.activityInfo.getIconResource(), resolveInfo.activityInfo.applicationInfo);
        int appIconResource = resolveInfo.getIconResource();
        if (pmEx != null) {
            appIcon = pmEx.getIconDrawableAsIconFrameTheme(context, appIcon, packageName, appIconResource);
        }
        return new QuickClipSharedItem(appIcon, label, packageName, activityName, appIconResource);
    }

    public static boolean isFakeMode() {
        if (ModelProperties.isFakeMode() || !ModelProperties.isFakeExif() || ModelProperties.isFakeExifAtnt()) {
            return false;
        }
        return true;
    }

    public static void setQuickClipFakeMode() {
        sIsEnabled ^= 1;
    }

    public static void resetQuickClipFakeMode() {
        sIsEnabled = true;
    }

    private static int getNFCIndex(Object[] activitis) {
        String nfc = "com.android.nfc";
        for (int i = 0; i < activitis.length; i++) {
            if (nfc.equals(activitis[i].activityInfo.packageName)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean onShowShareDialog(Activity activity, Uri uri) {
        if (activity == null || uri == null) {
            return false;
        }
        String mimeType = getMimeType(uri, activity.getApplicationContext());
        Intent emptyIntent = new Intent();
        ArrayList<QuickClipSharedItem> itemList = getPreferSharedList(activity.getApplicationContext(), new Intent("android.intent.action.SEND").setType(mimeType), 10000);
        if (itemList == null) {
            return false;
        }
        Intent chooserIntent;
        List<LabeledIntent> intentList = new ArrayList();
        if (isFakeMode()) {
            chooserIntent = Intent.createChooser(emptyIntent, activity.getApplicationContext().getResources().getString(C0088R.string.sp_share_via_NORMAL), null);
        } else {
            Intent broadCastIntent = new Intent();
            broadCastIntent.setAction(CameraConstants.QUICK_CLIP_FILTER);
            chooserIntent = Intent.createChooser(emptyIntent, activity.getApplicationContext().getResources().getString(C0088R.string.sp_share_via_NORMAL), PendingIntent.getBroadcast(activity, 0, broadCastIntent, 134217728).getIntentSender());
        }
        Iterator it = itemList.iterator();
        while (it.hasNext()) {
            Intent intent;
            QuickClipSharedItem sharedItem = (QuickClipSharedItem) it.next();
            if (isFakeMode() && sIsEnabled) {
                intent = getFakeIntent(activity, mimeType, uri, sharedItem);
            } else {
                intent = getQuickclipIntent(mimeType, uri, sharedItem);
            }
            intentList.add(new LabeledIntent(intent, sharedItem.getPackageName(), sharedItem.getLabel(), sharedItem.getAppIconResource()));
        }
        chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", (LabeledIntent[]) intentList.toArray(new LabeledIntent[intentList.size()]));
        AppControlUtilBase.setLaunchingShareActivity(true);
        activity.startActivity(chooserIntent);
        return true;
    }

    private static Intent getQuickclipIntent(String mimeType, Uri uri, QuickClipSharedItem sharedItem) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.setType(mimeType);
        intent.putExtra("android.intent.extra.STREAM", uri);
        intent.setComponent(new ComponentName(sharedItem.getPackageName(), sharedItem.getActivityName()));
        return intent;
    }

    private static Intent getFakeIntent(Activity activity, String mimeType, Uri uri, QuickClipSharedItem sharedItem) {
        Intent intent = new Intent(activity, QuickclipFakeActivity.class);
        intent.putExtra("mimeType", mimeType);
        intent.putExtra(OverlapProjectDbAdapter.KEY_URI, uri.toString());
        intent.putExtra("packageName", sharedItem.getPackageName());
        intent.putExtra("activityName", sharedItem.getActivityName());
        intent.addFlags(1073741824);
        return intent;
    }
}
