package com.lge.camera.constants;

import com.lge.camera.C0088R;

public final class PreferenceProperties {
    private static final int[] FRONT_PREFERENCES = new int[]{C0088R.xml.front_preferences};
    private static final int[] REAR_PREFERENCES = new int[]{C0088R.xml.rear_preferences};

    public static int getRearPreference() {
        return REAR_PREFERENCES[0];
    }

    public static int getFrontPreference() {
        return FRONT_PREFERENCES[0];
    }
}
