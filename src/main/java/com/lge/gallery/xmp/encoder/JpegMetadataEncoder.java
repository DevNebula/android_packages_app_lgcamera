package com.lge.gallery.xmp.encoder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import com.lge.gallery.xmp.encoder.util.Utils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class JpegMetadataEncoder {
    private static final byte[] BASIC_NAMESPACE = "http://ns.adobe.com/xap/1.0/\u0000".getBytes();
    private static final int CHUNK_LENGTH = 65000;
    private static final String TAG = "JpegMetadataEncoder";
    private final Context mContext;
    private JpegMetadataEncoderTask mEncoderTask;
    private long mFileTotlaSize = 0;
    private long mFirstImageSize = 0;
    private boolean mIsCancelTask = false;
    private final XmpMetadata mMetadata;
    private final XmlGenerator mXmlGenerator;

    public interface EncodingListener {
        void onCancel();

        void onFinish();

        void onProgress(Integer num);

        void onStart();
    }

    class JpegMetadataEncoderTask extends AsyncTask<Uri, Void, Void> {
        private EncodingListener mEncodingListener;
        private OutputStream mOs;

        public JpegMetadataEncoderTask(EncodingListener cb, OutputStream os) {
            this.mEncodingListener = cb;
            this.mOs = os;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mEncodingListener != null) {
                this.mEncodingListener.onStart();
                JpegMetadataEncoder.this.mIsCancelTask = false;
            }
        }

        protected Void doInBackground(Uri... uris) {
            JpegMetadataEncoder.this.encodeUri(uris[0], this.mOs, this.mEncodingListener);
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            if (this.mEncodingListener != null) {
                this.mEncodingListener.onFinish();
            }
        }

        private void cancel() {
            if (JpegMetadataEncoder.this.mEncoderTask != null && !JpegMetadataEncoder.this.mIsCancelTask && !JpegMetadataEncoder.this.mEncoderTask.isCancelled()) {
                JpegMetadataEncoder.this.mEncoderTask.cancel(true);
                JpegMetadataEncoder.this.mIsCancelTask = true;
                if (this.mEncodingListener != null) {
                    this.mEncodingListener.onCancel();
                }
            }
        }
    }

    public JpegMetadataEncoder(Context context, XmpMetadata metadata) {
        this.mContext = context;
        this.mMetadata = metadata;
        this.mXmlGenerator = new XmlGenerator(metadata);
    }

    public void encodeStream(InputStream is, OutputStream os) {
        encodeStream(is, os, null);
    }

    /* JADX WARNING: Missing block: B:34:?, code:
            r6.flush();
     */
    /* JADX WARNING: Missing block: B:35:0x00b5, code:
            r5 = r6;
            r3 = r4;
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            return;
     */
    void encodeStream(java.io.InputStream r19, java.io.OutputStream r20, com.lge.gallery.xmp.encoder.JpegMetadataEncoder.EncodingListener r21) {
        /*
        r18 = this;
        r2 = new java.io.BufferedInputStream;
        r0 = r19;
        r2.<init>(r0);
        r13 = r2.markSupported();
        if (r13 != 0) goto L_0x001a;
    L_0x000d:
        r13 = "JpegMetadataEncoder";
        r14 = "Should use inputstream mark & reset are supported.";
        android.util.Log.e(r13, r14);
        r13 = new java.lang.RuntimeException;
        r13.<init>();
        throw r13;
    L_0x001a:
        r13 = 2147483647; // 0x7fffffff float:NaN double:1.060997895E-314;
        r2.mark(r13);
        r0 = r18;
        r13 = r0.mXmlGenerator;
        r12 = r13.makeXmpDescriptionElements(r2);
        if (r21 == 0) goto L_0x0048;
    L_0x002a:
        r0 = r18;
        r14 = r0.mFirstImageSize;
        r16 = 0;
        r13 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1));
        if (r13 == 0) goto L_0x0048;
    L_0x0034:
        if (r12 == 0) goto L_0x0048;
    L_0x0036:
        r0 = r18;
        r14 = r0.mFirstImageSize;
        r13 = r12.getBytes();
        r13 = r13.length;
        r0 = (long) r13;
        r16 = r0;
        r14 = r14 - r16;
        r0 = r18;
        r0.mFirstImageSize = r14;
    L_0x0048:
        r3 = 0;
        r5 = 0;
        r0 = r18;
        r13 = r0.mIsCancelTask;	 Catch:{ IOException -> 0x00b8 }
        if (r13 == 0) goto L_0x0051;
    L_0x0050:
        return;
    L_0x0051:
        r2.reset();	 Catch:{ IOException -> 0x00b8 }
        r4 = new java.io.DataInputStream;	 Catch:{ IOException -> 0x00b8 }
        r4.<init>(r2);	 Catch:{ IOException -> 0x00b8 }
        r6 = new java.io.DataOutputStream;	 Catch:{ IOException -> 0x00ba }
        r13 = new java.io.BufferedOutputStream;	 Catch:{ IOException -> 0x00ba }
        r0 = r20;
        r13.<init>(r0);	 Catch:{ IOException -> 0x00ba }
        r6.<init>(r13);	 Catch:{ IOException -> 0x00ba }
        r11 = r4.readChar();	 Catch:{ IOException -> 0x00ab }
        r6.writeChar(r11);	 Catch:{ IOException -> 0x00ab }
        r0 = r18;
        r13 = r0.mXmlGenerator;	 Catch:{ IOException -> 0x00ab }
        r8 = r13.generate();	 Catch:{ IOException -> 0x00ab }
        r0 = r18;
        r1 = r21;
        r0.writeMetadata(r8, r6, r1);	 Catch:{ IOException -> 0x00ab }
        if (r12 == 0) goto L_0x008a;
    L_0x007d:
        r0 = r18;
        r13 = r0.mIsCancelTask;	 Catch:{ IOException -> 0x00ab }
        if (r13 != 0) goto L_0x008a;
    L_0x0083:
        r0 = r18;
        r1 = r21;
        r0.writeJpegDataBeforeXmpSignature(r4, r6, r1);	 Catch:{ IOException -> 0x00ab }
    L_0x008a:
        r13 = 9012; // 0x2334 float:1.2629E-41 double:4.4525E-320;
        r10 = new byte[r13];	 Catch:{ IOException -> 0x00ab }
        r9 = 0;
    L_0x008f:
        r9 = r4.read(r10);	 Catch:{ IOException -> 0x00ab }
        if (r9 <= 0) goto L_0x00b2;
    L_0x0095:
        r0 = r18;
        r13 = r0.mIsCancelTask;	 Catch:{ IOException -> 0x00ab }
        if (r13 != 0) goto L_0x00b2;
    L_0x009b:
        r13 = 0;
        r6.write(r10, r13, r9);	 Catch:{ IOException -> 0x00ab }
        r13 = r6.size();	 Catch:{ IOException -> 0x00ab }
        r0 = r18;
        r1 = r21;
        r0.notifyProgress(r1, r13);	 Catch:{ IOException -> 0x00ab }
        goto L_0x008f;
    L_0x00ab:
        r7 = move-exception;
        r5 = r6;
        r3 = r4;
    L_0x00ae:
        r7.printStackTrace();
        goto L_0x0050;
    L_0x00b2:
        r6.flush();	 Catch:{ IOException -> 0x00ab }
        r5 = r6;
        r3 = r4;
        goto L_0x0050;
    L_0x00b8:
        r7 = move-exception;
        goto L_0x00ae;
    L_0x00ba:
        r7 = move-exception;
        r3 = r4;
        goto L_0x00ae;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.gallery.xmp.encoder.JpegMetadataEncoder.encodeStream(java.io.InputStream, java.io.OutputStream, com.lge.gallery.xmp.encoder.JpegMetadataEncoder$EncodingListener):void");
    }

    private long computeTotalFileSize(List<XmpDataWrapper> list, int extLen, long currentSize) {
        long totalSize = currentSize;
        for (XmpDataWrapper wrapper : list) {
            if (wrapper.isExtension) {
                totalSize += (long) (wrapper.data.length + (((wrapper.data.length / CHUNK_LENGTH) + 1) * (((extLen + 4) + 4) + 4)));
            } else {
                totalSize += (long) ((BASIC_NAMESPACE.length + wrapper.data.length) + 4);
            }
        }
        return totalSize + this.mFirstImageSize;
    }

    private void writeMetadata(List<XmpDataWrapper> list, DataOutputStream dos, EncodingListener listener) throws IOException {
        byte[] nsExtension;
        if (this.mMetadata.hasExension()) {
            nsExtension = ("http://ns.adobe.com/xmp/extension/\u0000" + this.mXmlGenerator.getUuid()).getBytes();
        } else {
            nsExtension = new byte[0];
        }
        if (!(listener == null || this.mFirstImageSize == 0)) {
            this.mFileTotlaSize = computeTotalFileSize(list, nsExtension.length, 2);
        }
        for (XmpDataWrapper wrapper : list) {
            if (wrapper.isExtension) {
                int remained = wrapper.data.length;
                int i = 0;
                long totalLength = (long) wrapper.data.length;
                while (remained > 0 && !this.mIsCancelTask) {
                    int i2;
                    dos.writeChar(65505);
                    if (remained > CHUNK_LENGTH) {
                        i2 = CHUNK_LENGTH;
                    } else {
                        i2 = remained;
                    }
                    long currentPos = (long) (CHUNK_LENGTH * i);
                    dos.writeChar((i2 + nsExtension.length) + 10);
                    dos.write(nsExtension);
                    dos.writeInt((int) totalLength);
                    dos.writeInt((int) currentPos);
                    dos.write(wrapper.data, (int) currentPos, Math.min(CHUNK_LENGTH, remained));
                    remained -= CHUNK_LENGTH;
                    i++;
                    notifyProgress(listener, dos.size());
                }
            } else {
                dos.writeChar(65505);
                dos.writeChar((BASIC_NAMESPACE.length + wrapper.data.length) + 2);
                dos.write(BASIC_NAMESPACE);
                dos.write(wrapper.data);
                notifyProgress(listener, dos.size());
            }
        }
    }

    private void writeJpegDataBeforeXmpSignature(DataInputStream dis, DataOutputStream dos, EncodingListener listener) {
        while (true) {
            try {
                char marker = dis.readChar();
                char length = dis.readChar();
                if (length >= 2) {
                    byte[] blockBuffer = new byte[(length - 2)];
                    int read = dis.read(blockBuffer, 0, length - 2);
                    if (!XmpUtils.isXMP(blockBuffer) && read > 0) {
                        dos.writeChar(marker);
                        dos.writeChar(length);
                        dos.write(blockBuffer);
                        notifyProgress(listener, dos.size());
                    } else {
                        return;
                    }
                }
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void notifyProgress(EncodingListener l, int curSize) {
        if (l != null && this.mFileTotlaSize != 0) {
            l.onProgress(Integer.valueOf((int) ((((float) curSize) / ((float) this.mFileTotlaSize)) * 100.0f)));
        }
    }

    public void encodeUri(Uri from, OutputStream os) {
        encodeUri(from, os, null);
    }

    void encodeUri(Uri from, OutputStream os, EncodingListener listener) {
        InputStream is = null;
        try {
            is = this.mContext.getContentResolver().openInputStream(from);
            if (listener != null) {
                this.mFirstImageSize = getFileSize(from);
            }
            encodeStream(is, os, listener);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            Utils.closeSilently(is);
        }
    }

    private long getFileSize(Uri from) {
        Cursor returnCursor = null;
        try {
            returnCursor = this.mContext.getContentResolver().query(from, null, null, null, null);
            if (returnCursor == null || returnCursor.moveToFirst()) {
            }
            if (returnCursor != null) {
                returnCursor.close();
            }
            return 0;
        } catch (Throwable th) {
            if (returnCursor != null) {
                returnCursor.close();
            }
            throw th;
        }
    }

    public void startEncoding(Uri uri, OutputStream os, EncodingListener cb) {
        if (this.mEncoderTask == null) {
            this.mEncoderTask = new JpegMetadataEncoderTask(cb, os);
        } else {
            this.mEncoderTask.cancel();
        }
        if (this.mEncoderTask.isCancelled()) {
            this.mEncoderTask = new JpegMetadataEncoderTask(cb, os);
        }
        this.mEncoderTask.execute(new Uri[]{uri});
    }
}
