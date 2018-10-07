package com.lge.camera.managers.ext.sticker.utils;

import java.io.File;
import java.io.FilenameFilter;

public class DecompressingFilenameFilter implements FilenameFilter {
    public boolean accept(File file, String s) {
        if (file == null || !"decompressing.working".equalsIgnoreCase(s)) {
            return false;
        }
        return true;
    }
}
