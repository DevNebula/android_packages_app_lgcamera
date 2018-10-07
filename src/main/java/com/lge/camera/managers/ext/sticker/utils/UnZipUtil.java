package com.lge.camera.managers.ext.sticker.utils;

import com.arcsoft.stickerlibrary.utils.ZipUtil;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnZipUtil {
    private static final String TAG = "UnZipper";

    private static void Decompress(String src, String dest) throws IOException {
        File srcFile = new File(src);
        if (srcFile.exists()) {
            Decompress(srcFile, new File(dest));
            return;
        }
        throw new IOException("cannot find source file + " + srcFile.getAbsolutePath().toString());
    }

    public static void Decompress(File src, File destFolder) throws IOException {
        if (destFolder.exists() && destFolder.isDirectory()) {
            File realDest = new File(destFolder, src.getName().replace(ZipUtil.EXT, ""));
            CamLog.m3d(TAG, "realDest = " + realDest.getAbsolutePath());
            if (realDest.mkdir()) {
                File decompressing = new File(realDest, "decompressing.working");
                decompressing.createNewFile();
                CamLog.m3d(TAG, "mkdir success");
                FileInputStream fis = new FileInputStream(src);
                ZipInputStream zis = new ZipInputStream(fis);
                try {
                    byte[] buffer = new byte[8192];
                    while (true) {
                        ZipEntry ze = zis.getNextEntry();
                        if (ze != null) {
                            String name = ze.getName();
                            if (ze.isDirectory()) {
                                File dir = new File(realDest, name);
                                CamLog.m3d(TAG, "dir = " + dir.getAbsolutePath());
                                if (!dir.mkdir()) {
                                    throw new IOException("cannot create directory : " + realDest.getAbsolutePath().toString() + "/" + name);
                                }
                            } else {
                                FileOutputStream fos = new FileOutputStream(new File(realDest, name));
                                while (true) {
                                    try {
                                        int count = zis.read(buffer);
                                        if (count == -1) {
                                            break;
                                        }
                                        fos.write(buffer, 0, count);
                                    } catch (IOException e) {
                                        CamLog.m5e(TAG, "Decompress Thread : IOException1");
                                        throw new IOException("cannot copy entry");
                                    } catch (Throwable th) {
                                        if (fos != null) {
                                            try {
                                                fos.close();
                                            } catch (IOException e2) {
                                                e2.printStackTrace();
                                            }
                                        }
                                    }
                                }
                                fos.close();
                                zis.closeEntry();
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e22) {
                                        e22.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            zis.close();
                            fis.close();
                            if (zis != null) {
                                try {
                                    zis.close();
                                } catch (Exception e3) {
                                    e3.printStackTrace();
                                }
                            }
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (Exception e32) {
                                    e32.printStackTrace();
                                }
                            }
                            if (decompressing.exists()) {
                                decompressing.delete();
                                return;
                            }
                            return;
                        }
                    }
                } catch (IOException e4) {
                    CamLog.m5e(TAG, "Decompress Thread : IOException2");
                    throw new IOException("cannot create directory : ");
                } catch (Throwable th2) {
                    if (zis != null) {
                        try {
                            zis.close();
                        } catch (Exception e322) {
                            e322.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (Exception e3222) {
                            e3222.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
