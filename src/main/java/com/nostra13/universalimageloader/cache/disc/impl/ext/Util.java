package com.nostra13.universalimageloader.cache.disc.impl.ext;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

final class Util {
    static final Charset US_ASCII = Charset.forName("US-ASCII");
    static final Charset UTF_8 = Charset.forName("UTF-8");

    private Util() {
    }

    /* JADX WARNING: Missing block: B:4:0x0010, code:
            r3 = r2.toString();
     */
    static java.lang.String readFully(java.io.Reader r4) throws java.io.IOException {
        /*
        r2 = new java.io.StringWriter;	 Catch:{ all -> 0x001d }
        r2.<init>();	 Catch:{ all -> 0x001d }
        r3 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r0 = new char[r3];	 Catch:{ all -> 0x001d }
    L_0x0009:
        r1 = r4.read(r0);	 Catch:{ all -> 0x001d }
        r3 = -1;
        if (r1 != r3) goto L_0x0018;
    L_0x0010:
        r3 = r2.toString();	 Catch:{ all -> 0x001d }
        r4.close();
        return r3;
    L_0x0018:
        r3 = 0;
        r2.write(r0, r3, r1);	 Catch:{ all -> 0x001d }
        goto L_0x0009;
    L_0x001d:
        r3 = move-exception;
        r4.close();
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nostra13.universalimageloader.cache.disc.impl.ext.Util.readFully(java.io.Reader):java.lang.String");
    }

    static void deleteContents(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("not a readable directory: " + dir);
        }
        int length = files.length;
        int i = 0;
        while (i < length) {
            File file = files[i];
            if (file.isDirectory()) {
                deleteContents(file);
            }
            if (file.delete()) {
                i++;
            } else {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }

    static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e) {
            }
        }
    }
}
