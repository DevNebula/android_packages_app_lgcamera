package com.nostra13.universalimageloader.cache.disc.impl.ext;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

class StrictLineReader implements Closeable {
    /* renamed from: CR */
    private static final byte f48CR = (byte) 13;
    /* renamed from: LF */
    private static final byte f49LF = (byte) 10;
    private byte[] buf;
    private final Charset charset;
    private int end;
    /* renamed from: in */
    private final InputStream f50in;
    private int pos;

    public StrictLineReader(InputStream in, Charset charset) {
        this(in, 8192, charset);
    }

    public StrictLineReader(InputStream in, int capacity, Charset charset) {
        if (in == null || charset == null) {
            throw new NullPointerException();
        } else if (capacity < 0) {
            throw new IllegalArgumentException("capacity <= 0");
        } else if (charset.equals(Util.US_ASCII)) {
            this.f50in = in;
            this.charset = charset;
            this.buf = new byte[capacity];
        } else {
            throw new IllegalArgumentException("Unsupported encoding");
        }
    }

    public void close() throws IOException {
        synchronized (this.f50in) {
            if (this.buf != null) {
                this.buf = null;
                this.f50in.close();
            }
        }
    }

    public String readLine() throws IOException {
        String res;
        synchronized (this.f50in) {
            if (this.buf == null) {
                throw new IOException("LineReader is closed");
            }
            if (this.pos >= this.end) {
                fillBuf();
            }
            int i = this.pos;
            while (i != this.end) {
                if (this.buf[i] == f49LF) {
                    int lineEnd;
                    if (i == this.pos || this.buf[i - 1] != f48CR) {
                        lineEnd = i;
                    } else {
                        lineEnd = i - 1;
                    }
                    res = new String(this.buf, this.pos, lineEnd - this.pos, this.charset.name());
                    this.pos = i + 1;
                } else {
                    i++;
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream((this.end - this.pos) + 80) {
                public String toString() {
                    int length = (this.count <= 0 || this.buf[this.count - 1] != StrictLineReader.f48CR) ? this.count : this.count - 1;
                    try {
                        return new String(this.buf, 0, length, StrictLineReader.this.charset.name());
                    } catch (UnsupportedEncodingException e) {
                        throw new AssertionError(e);
                    }
                }
            };
            loop1:
            while (true) {
                out.write(this.buf, this.pos, this.end - this.pos);
                this.end = -1;
                fillBuf();
                i = this.pos;
                while (i != this.end) {
                    if (this.buf[i] == f49LF) {
                        break loop1;
                    }
                    i++;
                }
            }
            if (i != this.pos) {
                out.write(this.buf, this.pos, i - this.pos);
            }
            this.pos = i + 1;
            res = out.toString();
        }
        return res;
    }

    private void fillBuf() throws IOException {
        int result = this.f50in.read(this.buf, 0, this.buf.length);
        if (result == -1) {
            throw new EOFException();
        }
        this.pos = 0;
        this.end = result;
    }
}
