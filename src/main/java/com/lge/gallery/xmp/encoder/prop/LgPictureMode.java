package com.lge.gallery.xmp.encoder.prop;

public enum LgPictureMode {
    MULTIPICTURE("http://ns.lge.com/gallery/1.0/multipicture/", "LGMultiPic", false),
    SOUNDPICTURE("http://ns.lge.com/gallery/1.0/soundpicture/", "LGSoundPic", false),
    EXTIMAGE("http://ns.lge.com/gallery/1.0/extimage/", "LGExtImage", true),
    EXTAUDIO("http://ns.lge.com/gallery/1.0/extaudio/", "LGExtAudio", true);
    
    private final boolean mIsRawType;
    private final String mNamespace;
    private final String mPrefix;

    private LgPictureMode(String name, String prefix, boolean isRaw) {
        this.mNamespace = name;
        this.mPrefix = prefix;
        this.mIsRawType = isRaw;
    }

    public String getNamespace() {
        return this.mNamespace;
    }

    public String getPrefix() {
        return this.mPrefix;
    }

    public boolean isRawType() {
        return this.mIsRawType;
    }
}
