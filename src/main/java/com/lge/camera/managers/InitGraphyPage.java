package com.lge.camera.managers;

import android.view.View;
import com.lge.camera.C0088R;

public class InitGraphyPage extends InitHelpPage {
    public InitGraphyPage(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void initUI(View v, int degree) {
        super.initUI(v, degree);
    }

    public void init() {
        super.init();
        this.mImageId = C0088R.drawable.camera_initail_image_graphy;
    }
}
