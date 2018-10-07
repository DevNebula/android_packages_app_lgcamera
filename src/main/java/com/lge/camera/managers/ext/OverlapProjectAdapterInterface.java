package com.lge.camera.managers.ext;

import com.lge.camera.database.OverlapProjectDb;
import java.util.ArrayList;

public interface OverlapProjectAdapterInterface {
    int getCurrProjectIndex();

    ArrayList<OverlapProjectDb> onLoadedSamples();

    void onProjectSelected(int i, boolean z, boolean z2);
}
