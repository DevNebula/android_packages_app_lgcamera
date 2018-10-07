package com.lge.camera.managers;

import android.content.Context;
import android.content.res.Resources;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtilBase;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class FilmEmulatorHelper {
    protected static final String LUT_EXTENSION = "dat";

    /* renamed from: com.lge.camera.managers.FilmEmulatorHelper$1 */
    class C08991 implements FilenameFilter {
        C08991() {
        }

        public boolean accept(File path, String fileName) {
            return fileName.endsWith(FilmEmulatorHelper.LUT_EXTENSION);
        }
    }

    /* renamed from: com.lge.camera.managers.FilmEmulatorHelper$2 */
    class C09002 implements Comparator<String> {
        C09002() {
        }

        public int compare(String lhs, String rhs) {
            String[] parseValues = lhs.split("_");
            if (parseValues == null) {
                return 0;
            }
            int leftInt = Integer.parseInt(parseValues[0]);
            int righInt = Integer.parseInt(rhs.split("_")[0]);
            if (leftInt < righInt) {
                return -1;
            }
            if (leftInt > righInt) {
                return 1;
            }
            return 0;
        }
    }

    public File[] searchbyFileFilter(File fileList) {
        return fileList.listFiles(new C08991());
    }

    protected void collectionSortForLUTFile(List<String> Lutlist) {
        Collections.sort(Lutlist, new C09002());
    }

    public boolean checkHideMenuDistance(int degree, int dx, int dy, long touchDownTime) {
        if (System.currentTimeMillis() - touchDownTime > 400) {
            return false;
        }
        if (degree == 0 && dx >= 300) {
            return true;
        }
        if (degree == 180 && dx <= -300) {
            return true;
        }
        if (degree == 270 && dy >= 300) {
            return true;
        }
        if (degree != 90 || dy > -300) {
            return false;
        }
        return true;
    }

    public void updateDownloadFilterPreference(Context c, ArrayList<String> fileNameArrayList, ListPreference listPref, String downloadPath) {
        if (listPref != null) {
            String lastFilmValue = SharedPreferenceUtilBase.getLastSelectFilter(c);
            boolean isDownloadFilterSelected = false;
            String[] entries = (String[]) listPref.getEntries();
            String[] entryValues = (String[]) listPref.getEntryValues();
            int newEntryCnt = entries.length + fileNameArrayList.size();
            String[] newEntries = new String[newEntryCnt];
            String[] newEntryValues = new String[newEntryCnt];
            int index = 0;
            while (index < entries.length - 1) {
                newEntries[index] = entries[index];
                newEntryValues[index] = entryValues[index];
                index++;
            }
            int i = 0;
            while (i < fileNameArrayList.size()) {
                if ("ko_KR".equals(Locale.getDefault().toString())) {
                    newEntries[index + i] = ((String) fileNameArrayList.get(i)).split("_")[1];
                } else {
                    newEntries[index + i] = ((String) fileNameArrayList.get(i)).split("_")[1];
                }
                newEntryValues[index + i] = downloadPath + ((String) fileNameArrayList.get(i));
                if (lastFilmValue != null && newEntryValues[index + i].equals(lastFilmValue)) {
                    isDownloadFilterSelected = true;
                }
                i++;
            }
            newEntries[newEntryCnt - 1] = entries[entries.length - 1];
            newEntryValues[newEntryCnt - 1] = entryValues[entryValues.length - 1];
            listPref.setEntries(newEntries);
            listPref.setEntryValues(newEntryValues);
            if (isDownloadFilterSelected) {
                listPref.setValue(lastFilmValue);
                CamLog.m3d(CameraConstants.TAG, "[Film] setValue as download filter : " + lastFilmValue);
            }
        }
    }

    public void restoreFilterPreference(Context context, ListPreference listPref) {
        if (listPref != null) {
            CamLog.m3d(CameraConstants.TAG, "[Film] restoreFilterPreference");
            Resources resources = context.getResources();
            String[] entries = resources.getStringArray(ModelProperties.isJapanModel() ? C0088R.array.advanced_selfie_filter_entries_japan : C0088R.array.advanced_selfie_filter_entries);
            String[] entriyValues = resources.getStringArray(C0088R.array.advanced_selfie_filter_entriyValues);
            listPref.setEntries(entries);
            listPref.setEntryValues(entriyValues);
        }
    }

    public int getIndexByLutNumber(ArrayList<FilterMenuItem> list, int lutnumber) {
        for (int i = 0; i < list.size(); i++) {
            if (((FilterMenuItem) list.get(i)).mLUTNumber == lutnumber) {
                return i;
            }
        }
        return -1;
    }

    public int getCenterScrollPosition(int selectedMenuIndex, int halfSideItemCnt, ArrayList<FilterMenuItem> list, int[] lcdSize, int menuEndMargin, boolean isRTLDirection) {
        if (selectedMenuIndex <= halfSideItemCnt) {
            return 0;
        }
        FilterMenuItem item;
        if (selectedMenuIndex <= halfSideItemCnt || selectedMenuIndex >= (list.size() - halfSideItemCnt) - 1) {
            item = (FilterMenuItem) list.get(list.size() - 1);
            if (isRTLDirection) {
                return item.f29mX - menuEndMargin;
            }
            return (((-lcdSize[1]) + item.f29mX) + item.mWidth) + menuEndMargin;
        }
        item = (FilterMenuItem) list.get(selectedMenuIndex);
        return ((item.mWidth / 2) + item.f29mX) - (lcdSize[1] / 2);
    }
}
