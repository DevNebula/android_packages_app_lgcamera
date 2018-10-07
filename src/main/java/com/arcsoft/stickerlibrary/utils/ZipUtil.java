package com.arcsoft.stickerlibrary.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private static final String BASE_DIR = "";
    private static final int BUFFER = 1024;
    public static final String EXT = ".zip";
    private static final String PATH = File.separator;

    public static void decompress(String srcPath) throws Exception {
        decompress(new File(srcPath));
    }

    public static void decompress(File srcFile) throws Exception {
        decompress(srcFile, srcFile.getParent());
    }

    public static void decompress(File srcFile, File destFile) throws Exception {
        ZipInputStream zis = new ZipInputStream(new CheckedInputStream(new FileInputStream(srcFile), new CRC32()));
        decompress(destFile, zis);
        zis.close();
    }

    public static void decompress(InputStream srcFile, File destFile) throws Exception {
        ZipInputStream zis = new ZipInputStream(new CheckedInputStream(srcFile, new CRC32()));
        decompress(destFile, zis);
        zis.close();
    }

    public static void decompress(File srcFile, String destPath) throws Exception {
        decompress(srcFile, new File(destPath));
    }

    public static void decompress(InputStream srcFile, String destPath) throws Exception {
        decompress(srcFile, new File(destPath));
    }

    private static void decompress(File destFile, ZipInputStream zis) throws Exception {
        while (true) {
            ZipEntry entry = zis.getNextEntry();
            if (entry != null) {
                File dirFile = new File(destFile.getPath() + File.separator + entry.getName());
                fileProber(dirFile);
                if (entry.isDirectory()) {
                    dirFile.mkdirs();
                } else {
                    decompressFile(dirFile, zis);
                }
                zis.closeEntry();
            } else {
                return;
            }
        }
    }

    private static void fileProber(File dirFile) {
        File parentFile = dirFile.getParentFile();
        if (!parentFile.exists()) {
            fileProber(parentFile);
            parentFile.mkdir();
        }
    }

    private static void decompressFile(File destFile, ZipInputStream zis) throws Exception {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
        byte[] data = new byte[1024];
        while (true) {
            int count = zis.read(data, 0, 1024);
            if (count != -1) {
                bos.write(data, 0, count);
            } else {
                bos.close();
                return;
            }
        }
    }

    public static void compress(File srcFile) throws Exception {
        compress(srcFile, srcFile.getParent() + srcFile.getName() + EXT);
    }

    public static void compress(File srcFile, File destFile) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new CheckedOutputStream(new FileOutputStream(destFile), new CRC32()));
        compress(srcFile, zos, "");
        zos.flush();
        zos.close();
    }

    public static void compress(File srcFile, String destPath) throws Exception {
        compress(srcFile, new File(destPath));
    }

    private static void compress(File srcFile, ZipOutputStream zos, String basePath) throws Exception {
        if (srcFile.isDirectory()) {
            compressDir(srcFile, zos, basePath);
        } else {
            compressFile(srcFile, zos, basePath);
        }
    }

    public static void compress(String srcPath) throws Exception {
        compress(new File(srcPath));
    }

    public static void compress(String srcPath, String destPath) throws Exception {
        compress(new File(srcPath), destPath);
    }

    private static void compressDir(File dir, ZipOutputStream zos, String basePath) throws Exception {
        File[] files = dir.listFiles();
        if (files.length < 1) {
            zos.putNextEntry(new ZipEntry(basePath + dir.getName() + PATH));
            zos.closeEntry();
        }
        for (File file : files) {
            compress(file, zos, basePath + dir.getName() + PATH);
        }
    }

    private static void compressFile(File file, ZipOutputStream zos, String dir) throws Exception {
        zos.putNextEntry(new ZipEntry(dir + file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] data = new byte[1024];
        while (true) {
            int count = bis.read(data, 0, 1024);
            if (count != -1) {
                zos.write(data, 0, count);
            } else {
                bis.close();
                zos.closeEntry();
                return;
            }
        }
    }
}
