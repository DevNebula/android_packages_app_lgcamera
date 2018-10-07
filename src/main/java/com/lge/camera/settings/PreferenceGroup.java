package com.lge.camera.settings;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.Iterator;

public class PreferenceGroup extends CameraPreference {
    private ArrayList<CameraPreference> list = new ArrayList();

    public PreferenceGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addChild(CameraPreference child) {
        if (child != null) {
            child.setSharedPreferenceName(getSharedPreferenceName());
            this.list.add(child);
        }
    }

    public void addChildAt(CameraPreference child, int index) {
        if (child != null) {
            child.setSharedPreferenceName(getSharedPreferenceName());
            this.list.add(index, child);
        }
    }

    public void removePreference(int index) {
        this.list.remove(index);
    }

    public CameraPreference get(int index) {
        return (CameraPreference) this.list.get(index);
    }

    public int size() {
        return this.list.size();
    }

    public void reloadValue() {
        Iterator it = this.list.iterator();
        while (it.hasNext()) {
            ((CameraPreference) it.next()).reloadValue();
        }
    }

    public ListPreference findPreference(String key) {
        for (int i = 0; i < this.list.size(); i++) {
            CameraPreference pref = (CameraPreference) this.list.get(i);
            ListPreference listPref;
            if (pref instanceof ListPreference) {
                listPref = (ListPreference) pref;
                if (listPref.getKey().equals(key)) {
                    return listPref;
                }
            } else if (pref instanceof PreferenceGroup) {
                listPref = ((PreferenceGroup) pref).findPreference(key);
                if (listPref != null) {
                    return listPref;
                }
            } else {
                continue;
            }
        }
        return null;
    }

    public ListPreference getListPreference(int index) {
        try {
            CameraPreference pref = (CameraPreference) this.list.get(index);
            if (pref instanceof ListPreference) {
                return (ListPreference) pref;
            }
            if (pref instanceof PreferenceGroup) {
                return ((PreferenceGroup) pref).getListPreference(index);
            }
            return null;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public int findPreferenceIndex(String key) {
        int i = 0;
        Iterator it = this.list.iterator();
        while (it.hasNext()) {
            CameraPreference pref = (CameraPreference) it.next();
            if (!(pref instanceof ListPreference)) {
                if ((pref instanceof PreferenceGroup) && ((PreferenceGroup) pref).findPreference(key) != null) {
                    break;
                }
            } else if (((ListPreference) pref).getKey().equals(key)) {
                break;
            }
            i++;
        }
        return i;
    }
}
