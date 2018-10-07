package com.lge.camera.database;

import com.lge.camera.C0088R;

public class OverlapProjectDefaults {
    private static Integer[] sDefaultRes = new Integer[]{Integer.valueOf(C0088R.drawable.img_cell_guide_sample_food_1_720), Integer.valueOf(C0088R.drawable.img_cell_guide_sample_grip_1_720), Integer.valueOf(C0088R.drawable.img_cell_guide_sample_phone_1_720)};
    private static Integer[][] sDefaultRes01_Sub;

    static {
        r0 = new Integer[3][];
        r0[0] = new Integer[]{Integer.valueOf(C0088R.drawable.img_cell_guide_sample_food_1_720), Integer.valueOf(C0088R.drawable.img_cell_guide_sample_food_1_720), Integer.valueOf(C0088R.drawable.img_cell_guide_sample_food_1_720)};
        r0[1] = new Integer[]{Integer.valueOf(C0088R.drawable.img_cell_guide_sample_grip_1_720), Integer.valueOf(C0088R.drawable.img_cell_guide_sample_grip_1_720), Integer.valueOf(C0088R.drawable.img_cell_guide_sample_grip_1_720)};
        r0[2] = new Integer[]{Integer.valueOf(C0088R.drawable.img_cell_guide_sample_phone_1_720), Integer.valueOf(C0088R.drawable.img_cell_guide_sample_phone_1_720), Integer.valueOf(C0088R.drawable.img_cell_guide_sample_phone_1_720)};
        sDefaultRes01_Sub = r0;
    }

    public static int getDefaultSample(int value) {
        return sDefaultRes[value].intValue();
    }

    public static int getSubOfDefaultOne(int a, int b) {
        return sDefaultRes01_Sub[a][b].intValue();
    }
}
