package com.arcsoft.stickerlibrary.utils;

import android.media.Image.Plane;
import android.util.Log;
import com.lge.camera.constants.CameraConstantsEx;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileUtil {
    public static void dumpNV21ForCamera2(String dumpDir, Plane yPlane, Plane vuPlane, int width, int height) {
        FileNotFoundException e;
        IOException e2;
        String fullPath = dumpDir + File.separator + System.currentTimeMillis() + "_" + width + "_" + height + ".nv21";
        try {
            OutputStream out = new FileOutputStream(fullPath);
            OutputStream outputStream;
            try {
                int yRealSize = yPlane.getBuffer().remaining();
                int vuRealSize = vuPlane.getBuffer().remaining();
                int ySize = yPlane.getRowStride() * CameraConstantsEx.HD_SCREEN_RESOLUTION;
                byte[] nv21Bytes = new byte[(ySize + ((vuPlane.getRowStride() * CameraConstantsEx.HD_SCREEN_RESOLUTION) / 2))];
                yPlane.getBuffer().get(nv21Bytes, 0, yRealSize);
                vuPlane.getBuffer().get(nv21Bytes, ySize, vuRealSize);
                out.write(nv21Bytes);
                out.close();
                Log.d("DumpPPATH", "path : " + fullPath + " size=" + nv21Bytes.length);
                outputStream = out;
            } catch (FileNotFoundException e3) {
                e = e3;
                outputStream = out;
                Log.d("DumpPPATH", "FileNotFoundException : " + e.getMessage());
            } catch (IOException e4) {
                e2 = e4;
                outputStream = out;
                e2.printStackTrace();
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            Log.d("DumpPPATH", "FileNotFoundException : " + e.getMessage());
        } catch (IOException e6) {
            e2 = e6;
            e2.printStackTrace();
        }
    }

    public static boolean copyFile(String fromFilePath, String toFilePath) {
        try {
            File fromFile = new File(fromFilePath);
            File file;
            try {
                File toFile = new File(toFilePath);
                File file2;
                if (!fromFile.exists()) {
                    file2 = toFile;
                    file = fromFile;
                    return false;
                } else if (!fromFile.isFile()) {
                    file2 = toFile;
                    file = fromFile;
                    return false;
                } else if (fromFile.canRead()) {
                    if (!toFile.getParentFile().exists()) {
                        Log.i("RES", "res = " + toFile.getParentFile().mkdirs() + "  , parent = " + toFile.getParentFile().getAbsolutePath());
                    }
                    if (toFile.exists()) {
                        toFile.delete();
                    }
                    try {
                        FileInputStream fis = new FileInputStream(fromFile);
                        FileOutputStream fos = new FileOutputStream(toFile);
                        byte[] buffer = new byte[1024];
                        while (true) {
                            int count = fis.read(buffer);
                            if (count <= 0) {
                                break;
                            }
                            fos.write(buffer, 0, count);
                        }
                        fis.close();
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        Log.e("ERRORX", "BEGIN:" + e.getMessage());
                        e.printStackTrace();
                        Log.e("ERRORX", "END:");
                    }
                    file2 = toFile;
                    file = fromFile;
                    return true;
                } else {
                    file2 = toFile;
                    file = fromFile;
                    return false;
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                file = fromFile;
                return false;
            }
        } catch (Exception e22) {
            e22.printStackTrace();
            return false;
        }
    }
}
