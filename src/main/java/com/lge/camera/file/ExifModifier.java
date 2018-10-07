package com.lge.camera.file;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

class ExifModifier {
    public static final boolean DEBUG = false;
    public static final String TAG = "ExifModifier";
    private final ByteBuffer mByteBuffer;
    private final ExifBridge mInterface;
    private int mOffsetBase;
    private final List<TagOffset> mTagOffsets = new ArrayList();
    private final ExifData mTagToModified;

    private static class TagOffset {
        final int mOffset;
        final ExifTag mTag;

        TagOffset(ExifTag tag, int offset) {
            this.mTag = tag;
            this.mOffset = offset;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0041 A:{SYNTHETIC, Splitter: B:10:0x0041} */
    protected ExifModifier(java.nio.ByteBuffer r6, com.lge.camera.file.ExifBridge r7) throws java.io.IOException, com.lge.camera.file.ExifInvalidFormatException {
        /*
        r5 = this;
        r5.<init>();
        r3 = new java.util.ArrayList;
        r3.<init>();
        r5.mTagOffsets = r3;
        r5.mByteBuffer = r6;
        r3 = r6.position();
        r5.mOffsetBase = r3;
        r5.mInterface = r7;
        r0 = 0;
        r1 = new com.lge.camera.file.ByteBufferInputStream;	 Catch:{ all -> 0x003e }
        r1.<init>(r6);	 Catch:{ all -> 0x003e }
        r2 = com.lge.camera.file.ExifParser.parse(r1, r7);	 Catch:{ all -> 0x0049 }
        r3 = new com.lge.camera.file.ExifData;	 Catch:{ all -> 0x0049 }
        r4 = r2.getByteOrder();	 Catch:{ all -> 0x0049 }
        r3.<init>(r4, r7);	 Catch:{ all -> 0x0049 }
        r5.mTagToModified = r3;	 Catch:{ all -> 0x0049 }
        r3 = r5.mOffsetBase;	 Catch:{ all -> 0x0049 }
        r4 = r2.getTiffStartPosition();	 Catch:{ all -> 0x0049 }
        r3 = r3 + r4;
        r5.mOffsetBase = r3;	 Catch:{ all -> 0x0049 }
        r3 = r5.mByteBuffer;	 Catch:{ all -> 0x0049 }
        r4 = 0;
        r3.position(r4);	 Catch:{ all -> 0x0049 }
        if (r1 == 0) goto L_0x003d;
    L_0x003a:
        r1.close();	 Catch:{ Throwable -> 0x0045 }
    L_0x003d:
        return;
    L_0x003e:
        r3 = move-exception;
    L_0x003f:
        if (r0 == 0) goto L_0x0044;
    L_0x0041:
        r0.close();	 Catch:{ Throwable -> 0x0047 }
    L_0x0044:
        throw r3;
    L_0x0045:
        r3 = move-exception;
        goto L_0x003d;
    L_0x0047:
        r4 = move-exception;
        goto L_0x0044;
    L_0x0049:
        r3 = move-exception;
        r0 = r1;
        goto L_0x003f;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.file.ExifModifier.<init>(java.nio.ByteBuffer, com.lge.camera.file.ExifBridge):void");
    }

    protected ByteOrder getByteOrder() {
        return this.mTagToModified.getByteOrder();
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0089 A:{SYNTHETIC, Splitter: B:31:0x0089} */
    protected boolean commit() throws java.io.IOException, com.lge.camera.file.ExifInvalidFormatException {
        /*
        r15 = this;
        r11 = 1;
        r10 = 0;
        r5 = 0;
        r6 = new com.lge.camera.file.ByteBufferInputStream;	 Catch:{ all -> 0x0101 }
        r12 = r15.mByteBuffer;	 Catch:{ all -> 0x0101 }
        r6.<init>(r12);	 Catch:{ all -> 0x0101 }
        r2 = 0;
        r12 = 5;
        r4 = new com.lge.camera.file.IfdData[r12];	 Catch:{ all -> 0x0085 }
        r12 = 0;
        r13 = r15.mTagToModified;	 Catch:{ all -> 0x0085 }
        r14 = 0;
        r13 = r13.getIfdData(r14);	 Catch:{ all -> 0x0085 }
        r4[r12] = r13;	 Catch:{ all -> 0x0085 }
        r12 = 1;
        r13 = r15.mTagToModified;	 Catch:{ all -> 0x0085 }
        r14 = 1;
        r13 = r13.getIfdData(r14);	 Catch:{ all -> 0x0085 }
        r4[r12] = r13;	 Catch:{ all -> 0x0085 }
        r12 = 2;
        r13 = r15.mTagToModified;	 Catch:{ all -> 0x0085 }
        r14 = 2;
        r13 = r13.getIfdData(r14);	 Catch:{ all -> 0x0085 }
        r4[r12] = r13;	 Catch:{ all -> 0x0085 }
        r12 = 3;
        r13 = r15.mTagToModified;	 Catch:{ all -> 0x0085 }
        r14 = 3;
        r13 = r13.getIfdData(r14);	 Catch:{ all -> 0x0085 }
        r4[r12] = r13;	 Catch:{ all -> 0x0085 }
        r12 = 4;
        r13 = r15.mTagToModified;	 Catch:{ all -> 0x0085 }
        r14 = 4;
        r13 = r13.getIfdData(r14);	 Catch:{ all -> 0x0085 }
        r4[r12] = r13;	 Catch:{ all -> 0x0085 }
        r12 = 0;
        r12 = r4[r12];	 Catch:{ all -> 0x0085 }
        if (r12 == 0) goto L_0x0047;
    L_0x0045:
        r2 = r2 | 1;
    L_0x0047:
        r12 = 1;
        r12 = r4[r12];	 Catch:{ all -> 0x0085 }
        if (r12 == 0) goto L_0x004e;
    L_0x004c:
        r2 = r2 | 2;
    L_0x004e:
        r12 = 2;
        r12 = r4[r12];	 Catch:{ all -> 0x0085 }
        if (r12 == 0) goto L_0x0055;
    L_0x0053:
        r2 = r2 | 4;
    L_0x0055:
        r12 = 4;
        r12 = r4[r12];	 Catch:{ all -> 0x0085 }
        if (r12 == 0) goto L_0x005c;
    L_0x005a:
        r2 = r2 | 8;
    L_0x005c:
        r12 = 3;
        r12 = r4[r12];	 Catch:{ all -> 0x0085 }
        if (r12 == 0) goto L_0x0063;
    L_0x0061:
        r2 = r2 | 16;
    L_0x0063:
        r12 = r15.mInterface;	 Catch:{ all -> 0x0085 }
        r9 = com.lge.camera.file.ExifParser.parse(r6, r2, r12);	 Catch:{ all -> 0x0085 }
        r1 = r9.next();	 Catch:{ all -> 0x0085 }
        r0 = 0;
    L_0x006e:
        r12 = 5;
        if (r1 == r12) goto L_0x00d8;
    L_0x0071:
        switch(r1) {
            case 0: goto L_0x0079;
            case 1: goto L_0x008d;
            default: goto L_0x0074;
        };	 Catch:{ all -> 0x0085 }
    L_0x0074:
        r1 = r9.next();	 Catch:{ all -> 0x0085 }
        goto L_0x006e;
    L_0x0079:
        r12 = r9.getCurrentIfd();	 Catch:{ all -> 0x0085 }
        r0 = r4[r12];	 Catch:{ all -> 0x0085 }
        if (r0 != 0) goto L_0x0074;
    L_0x0081:
        r9.skipRemainingTagsInCurrentIfd();	 Catch:{ all -> 0x0085 }
        goto L_0x0074;
    L_0x0085:
        r10 = move-exception;
        r5 = r6;
    L_0x0087:
        if (r5 == 0) goto L_0x008c;
    L_0x0089:
        r5.close();	 Catch:{ Throwable -> 0x00ff }
    L_0x008c:
        throw r10;
    L_0x008d:
        r8 = r9.getTag();	 Catch:{ all -> 0x0085 }
        if (r8 == 0) goto L_0x0074;
    L_0x0093:
        if (r0 == 0) goto L_0x0074;
    L_0x0095:
        r12 = r8.getTagId();	 Catch:{ all -> 0x0085 }
        r7 = r0.getTag(r12);	 Catch:{ all -> 0x0085 }
        if (r7 == 0) goto L_0x0074;
    L_0x009f:
        r12 = r7.getComponentCount();	 Catch:{ all -> 0x0085 }
        r13 = r8.getComponentCount();	 Catch:{ all -> 0x0085 }
        if (r12 != r13) goto L_0x00b3;
    L_0x00a9:
        r12 = r7.getDataType();	 Catch:{ all -> 0x0085 }
        r13 = r8.getDataType();	 Catch:{ all -> 0x0085 }
        if (r12 == r13) goto L_0x00b9;
    L_0x00b3:
        if (r6 == 0) goto L_0x00b8;
    L_0x00b5:
        r6.close();	 Catch:{ Throwable -> 0x00fb }
    L_0x00b8:
        return r10;
    L_0x00b9:
        r12 = r15.mTagOffsets;	 Catch:{ all -> 0x0085 }
        r13 = new com.lge.camera.file.ExifModifier$TagOffset;	 Catch:{ all -> 0x0085 }
        r14 = r8.getOffset();	 Catch:{ all -> 0x0085 }
        r13.<init>(r7, r14);	 Catch:{ all -> 0x0085 }
        r12.add(r13);	 Catch:{ all -> 0x0085 }
        r12 = r8.getTagId();	 Catch:{ all -> 0x0085 }
        r0.removeTag(r12);	 Catch:{ all -> 0x0085 }
        r12 = r0.getTagCount();	 Catch:{ all -> 0x0085 }
        if (r12 != 0) goto L_0x0074;
    L_0x00d4:
        r9.skipRemainingTagsInCurrentIfd();	 Catch:{ all -> 0x0085 }
        goto L_0x0074;
    L_0x00d8:
        r13 = r4.length;	 Catch:{ all -> 0x0085 }
        r12 = r10;
    L_0x00da:
        if (r12 >= r13) goto L_0x00f1;
    L_0x00dc:
        r3 = r4[r12];	 Catch:{ all -> 0x0085 }
        if (r3 == 0) goto L_0x00ee;
    L_0x00e0:
        r14 = r3.getTagCount();	 Catch:{ all -> 0x0085 }
        if (r14 <= 0) goto L_0x00ee;
    L_0x00e6:
        if (r6 == 0) goto L_0x00b8;
    L_0x00e8:
        r6.close();	 Catch:{ Throwable -> 0x00ec }
        goto L_0x00b8;
    L_0x00ec:
        r11 = move-exception;
        goto L_0x00b8;
    L_0x00ee:
        r12 = r12 + 1;
        goto L_0x00da;
    L_0x00f1:
        r15.modify();	 Catch:{ all -> 0x0085 }
        if (r6 == 0) goto L_0x00f9;
    L_0x00f6:
        r6.close();	 Catch:{ Throwable -> 0x00fd }
    L_0x00f9:
        r10 = r11;
        goto L_0x00b8;
    L_0x00fb:
        r11 = move-exception;
        goto L_0x00b8;
    L_0x00fd:
        r10 = move-exception;
        goto L_0x00f9;
    L_0x00ff:
        r11 = move-exception;
        goto L_0x008c;
    L_0x0101:
        r10 = move-exception;
        goto L_0x0087;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.file.ExifModifier.commit():boolean");
    }

    private void modify() {
        this.mByteBuffer.order(getByteOrder());
        for (TagOffset tagOffset : this.mTagOffsets) {
            writeTagValue(tagOffset.mTag, tagOffset.mOffset);
        }
    }

    private void writeTagValue(ExifTag tag, int offset) {
        this.mByteBuffer.position(this.mOffsetBase + offset);
        byte[] buf;
        int n;
        int i;
        switch (tag.getDataType()) {
            case (short) 1:
            case (short) 7:
                buf = new byte[tag.getComponentCount()];
                tag.getBytes(buf);
                this.mByteBuffer.put(buf);
                return;
            case (short) 2:
                buf = tag.getStringByte();
                if (buf.length == tag.getComponentCount()) {
                    buf[buf.length - 1] = (byte) 0;
                    this.mByteBuffer.put(buf);
                    return;
                }
                this.mByteBuffer.put(buf);
                this.mByteBuffer.put((byte) 0);
                return;
            case (short) 3:
                n = tag.getComponentCount();
                for (i = 0; i < n; i++) {
                    this.mByteBuffer.putShort((short) ((int) tag.getValueAt(i)));
                }
                return;
            case (short) 4:
            case (short) 9:
                n = tag.getComponentCount();
                for (i = 0; i < n; i++) {
                    this.mByteBuffer.putInt((int) tag.getValueAt(i));
                }
                return;
            case (short) 5:
            case (short) 10:
                n = tag.getComponentCount();
                for (i = 0; i < n; i++) {
                    Rational v = tag.getRational(i);
                    this.mByteBuffer.putInt((int) v.getNumerator());
                    this.mByteBuffer.putInt((int) v.getDenominator());
                }
                return;
            default:
                return;
        }
    }

    public void modifyTag(ExifTag tag) {
        this.mTagToModified.addTag(tag);
    }
}
