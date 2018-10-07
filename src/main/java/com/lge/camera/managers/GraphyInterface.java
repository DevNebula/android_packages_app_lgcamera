package com.lge.camera.managers;

import com.lge.graphy.data.GraphyItem;
import java.util.ArrayList;

public interface GraphyInterface extends ModuleInterface {
    void attachBestImageItems();

    void attachMyFilterImageItems();

    void changeCameraAngle(boolean z);

    boolean checkGraphyAppInstalled();

    void foldImageItems(int i, int i2, int i3);

    boolean getAELock();

    int getGraphyIndex();

    ArrayList<GraphyItem> getGraphyItems();

    double getIlluminance();

    int getLastGraphyIndex();

    ManualData getManualData(int i);

    boolean isFoldedBestItem();

    boolean isFoldedMyFilterItem();

    boolean isFromGraphyApp();

    boolean isFromLDU();

    boolean isRotateDialogVisible(int i);

    void onHideGraphyList();

    void onShowGraphyList();

    void removeBestImageItems();

    void removeMyFilterImageItems();

    void saveLastGraphyIndex(int i);

    void selectItem();

    void selectItem(int i);

    void sendSelectedItemToControlManager(GraphyItem graphyItem, boolean z);

    boolean setAELock(Boolean bool);

    void setAllAuto();

    void setButtonLocked(boolean z, int i);

    void setEVGuideLayoutVisibility(boolean z, boolean z2);

    void setFromGraphyFlag(boolean z);

    void setGraphyButtonVisiblity(boolean z);

    void setGraphyData(String str, String str2, String str3);

    void setGraphyIndex(int i);

    void setGraphyItems(ArrayList<GraphyItem> arrayList);

    void setGraphyListVisibility(boolean z, boolean z2);

    boolean setManualDataByGraphy(int i, String str, String str2, boolean z);

    void spreadImageItems(int i, int i2, int i3);
}
