package com.lge.gallery.xmp.encoder;

import com.lge.gallery.xmp.encoder.model.Model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

final class XmpDto {
    private Map<String, Model> mChildren = new HashMap();
    private List<XmpDto> mExtension = new ArrayList();
    private final String mNamespace;
    private final String mPrefix;

    public XmpDto(String ns, String prefix) {
        this.mNamespace = ns;
        this.mPrefix = prefix;
    }

    public String getNamespace() {
        return this.mNamespace;
    }

    public String getPrefix() {
        return this.mPrefix;
    }

    public ArrayList<String> getNamespaces() {
        ArrayList<String> result = new ArrayList();
        result.add(this.mNamespace);
        for (XmpDto m : this.mExtension) {
            result.add(m.getNamespace());
        }
        return result;
    }

    public void addChildren(String key, Model value) {
        this.mChildren.put(key, value);
    }

    public Model getChildren(String key) {
        return (Model) this.mChildren.get(key);
    }

    public Iterator<Entry<String, Model>> childrenIterator() {
        return this.mChildren.entrySet().iterator();
    }

    public void addExtension(XmpDto meta) {
        this.mExtension.add(meta);
    }

    public Iterator<XmpDto> extensionIterator() {
        return this.mExtension.iterator();
    }

    public boolean hasExtension() {
        return this.mExtension.size() > 0;
    }
}
