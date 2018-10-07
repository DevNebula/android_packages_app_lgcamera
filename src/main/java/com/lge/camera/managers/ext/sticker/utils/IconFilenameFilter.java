package com.lge.camera.managers.ext.sticker.utils;

import java.io.File;
import java.io.FilenameFilter;

public class IconFilenameFilter implements FilenameFilter {
    public boolean accept(File file, String s) {
        if (file == null || (!"icon.png".equalsIgnoreCase(s) && !"icon_sel.png".equalsIgnoreCase(s))) {
            return false;
        }
        return true;
    }
}
