package com.lge.camera.managers.ext.sticker.utils;

import java.io.File;
import java.io.FilenameFilter;

public class OriginFilenameFilter implements FilenameFilter {
    public boolean accept(File file, String name) {
        if (file == null || name == null || !name.endsWith(".origin")) {
            return false;
        }
        return true;
    }
}
