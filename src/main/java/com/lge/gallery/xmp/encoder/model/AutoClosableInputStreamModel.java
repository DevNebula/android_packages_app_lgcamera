package com.lge.gallery.xmp.encoder.model;

import android.util.Base64OutputStream;
import com.lge.gallery.xmp.encoder.util.Utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AutoClosableInputStreamModel implements Model {
    private String mData;
    private final InputStream mInputStream;

    public AutoClosableInputStreamModel(InputStream is) {
        this.mInputStream = is;
    }

    public synchronized String getData() {
        if (this.mData == null) {
            this.mData = createData(this.mInputStream);
        }
        return this.mData;
    }

    protected static String createData(InputStream is) {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            byte[] buffer = new byte[9012];
            BufferedInputStream bis2 = new BufferedInputStream(is);
            try {
                BufferedOutputStream bos2 = new BufferedOutputStream(new Base64OutputStream(baos, 0));
                while (bis2.read(buffer) > 0) {
                    try {
                        bos2.write(buffer);
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        bos = bos2;
                        bis = bis2;
                        try {
                            e.printStackTrace();
                            Utils.closeSilently(bis);
                            Utils.closeSilently(bos);
                            return new String(baos.toByteArray());
                        } catch (Throwable th2) {
                            th = th2;
                            Utils.closeSilently(bis);
                            Utils.closeSilently(bos);
                            throw th;
                        }
                    } catch (IOException e4) {
                        e2 = e4;
                        bos = bos2;
                        bis = bis2;
                        e2.printStackTrace();
                        Utils.closeSilently(bis);
                        Utils.closeSilently(bos);
                        return new String(baos.toByteArray());
                    } catch (Throwable th3) {
                        th = th3;
                        bos = bos2;
                        bis = bis2;
                        Utils.closeSilently(bis);
                        Utils.closeSilently(bos);
                        throw th;
                    }
                }
                Utils.closeSilently(bis2);
                Utils.closeSilently(bos2);
                bos = bos2;
                bis = bis2;
            } catch (FileNotFoundException e5) {
                e = e5;
                bis = bis2;
                e.printStackTrace();
                Utils.closeSilently(bis);
                Utils.closeSilently(bos);
                return new String(baos.toByteArray());
            } catch (IOException e6) {
                e2 = e6;
                bis = bis2;
                e2.printStackTrace();
                Utils.closeSilently(bis);
                Utils.closeSilently(bos);
                return new String(baos.toByteArray());
            } catch (Throwable th4) {
                th = th4;
                bis = bis2;
                Utils.closeSilently(bis);
                Utils.closeSilently(bos);
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            Utils.closeSilently(bis);
            Utils.closeSilently(bos);
            return new String(baos.toByteArray());
        } catch (IOException e8) {
            e2 = e8;
            e2.printStackTrace();
            Utils.closeSilently(bis);
            Utils.closeSilently(bos);
            return new String(baos.toByteArray());
        }
        return new String(baos.toByteArray());
    }

    public void readData(InputStream is) {
    }

    public void writeData(OutputStream os) {
    }

    public boolean isRaw() {
        return true;
    }
}
