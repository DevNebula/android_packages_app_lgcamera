package com.lge.gallery.xmp.encoder.prop;

public class Properties {

    public static class BaseProperty {
        public static final String VERSION = "Version";
    }

    public static class RawBasic {
        public static final String DATA = "Data";
        public static final String MIME = "Mime";
    }

    public static class ExtAudio extends RawBasic {
        public static final String DURATION = "Duration";
    }

    public static class ExtImage extends RawBasic {
    }

    public static class MultiPicture extends BaseProperty {
        public static final String EFFECT = "Effect";
        public static final String NUMBER_OF_IMAGE = "NumberOfImage";
    }

    public static class SoundPicture extends BaseProperty {
    }
}
