package com.lge.gallery.xmp.encoder;

import android.content.Context;
import android.net.Uri;
import com.lge.gallery.xmp.encoder.model.AutoClosableInputStreamModel;
import com.lge.gallery.xmp.encoder.model.Model;
import com.lge.gallery.xmp.encoder.model.StringModel;
import com.lge.gallery.xmp.encoder.model.UriModel;
import com.lge.gallery.xmp.encoder.prop.LgPictureMode;
import com.lge.gallery.xmp.encoder.prop.Properties.ExtAudio;
import com.lge.gallery.xmp.encoder.prop.Properties.RawBasic;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class XmpMetadata {
    private final XmpDto mMetadata;
    private int mSubAudioCounter;
    private int mSubImageCounter;

    public XmpMetadata(LgPictureMode mode) {
        this.mMetadata = new XmpDto(mode.getNamespace(), mode.getPrefix());
    }

    public void addProperty(String key, String value) {
        this.mMetadata.addChildren(this.mMetadata.getPrefix() + ":" + key, new StringModel(value));
    }

    public synchronized void addImage(Context context, Uri uri, String mimeType) throws FileNotFoundException {
        addImage(new UriModel(context.getContentResolver(), uri), mimeType);
    }

    public synchronized void addImage(InputStream is, String mimeType) throws FileNotFoundException {
        addImage(new AutoClosableInputStreamModel(is), mimeType);
    }

    private synchronized void addImage(Model model, String mimeType) {
        String namespace = LgPictureMode.EXTIMAGE.getNamespace();
        StringBuilder append = new StringBuilder().append(LgPictureMode.EXTIMAGE.getPrefix());
        int i = this.mSubImageCounter;
        this.mSubImageCounter = i + 1;
        XmpDto xmp = new XmpDto(namespace, append.append(i == 0 ? "" : Integer.valueOf(this.mSubImageCounter)).toString());
        xmp.addChildren(xmp.getPrefix() + ":" + RawBasic.MIME, new StringModel(mimeType));
        xmp.addChildren(xmp.getPrefix() + ":" + RawBasic.DATA, model);
        this.mMetadata.addExtension(xmp);
    }

    public synchronized void addAudio(Context context, Uri uri, String mimeType) throws FileNotFoundException {
        addAudio(context, uri, mimeType, -1);
    }

    public synchronized void addAudio(Context context, Uri uri, String mimeType, int duration) throws FileNotFoundException {
        addAudio(new UriModel(context.getContentResolver(), uri), mimeType, duration);
    }

    public synchronized void addAudio(InputStream is, String mimeType) throws FileNotFoundException {
        addAudio(is, mimeType, -1);
    }

    public synchronized void addAudio(InputStream is, String mimeType, int duration) throws FileNotFoundException {
        addAudio(new AutoClosableInputStreamModel(is), mimeType, duration);
    }

    private synchronized void addAudio(Model model, String mimeType, int duration) {
        String namespace = LgPictureMode.EXTAUDIO.getNamespace();
        StringBuilder append = new StringBuilder().append(LgPictureMode.EXTAUDIO.getPrefix());
        int i = this.mSubAudioCounter;
        this.mSubAudioCounter = i + 1;
        XmpDto xmp = new XmpDto(namespace, append.append(i == 0 ? "" : Integer.valueOf(this.mSubAudioCounter)).toString());
        xmp.addChildren(xmp.getPrefix() + ":" + RawBasic.MIME, new StringModel(mimeType));
        xmp.addChildren(xmp.getPrefix() + ":" + RawBasic.DATA, model);
        if (duration > 0) {
            xmp.addChildren(xmp.getPrefix() + ":" + ExtAudio.DURATION, new StringModel(String.valueOf(duration)));
        }
        this.mMetadata.addExtension(xmp);
    }

    public boolean hasExension() {
        return this.mMetadata.hasExtension();
    }

    XmpDto getData() {
        return this.mMetadata;
    }
}
