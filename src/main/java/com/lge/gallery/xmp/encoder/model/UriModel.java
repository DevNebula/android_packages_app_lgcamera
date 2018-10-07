package com.lge.gallery.xmp.encoder.model;

import android.content.ContentResolver;
import android.net.Uri;
import java.io.FileNotFoundException;

public class UriModel extends AutoClosableInputStreamModel {
    public UriModel(ContentResolver resolver, Uri uri) throws FileNotFoundException {
        super(resolver.openInputStream(uri));
    }
}
