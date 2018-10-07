package com.arcsoft.stickerlibrary.sticker;

import java.util.ArrayList;

public class FunnyType {
    public static final int ET_TYPE_ALIEN = 6;
    public static final String ET_TYPE_ALIEN_STRING = "Alien";
    public static final int ET_TYPE_BIGFACE = 2;
    public static final String ET_TYPE_BIGFACE_STRING = "Big face";
    public static final int ET_TYPE_GOBBLER = 7;
    public static final String ET_TYPE_GOBBLER_STRING = "Gobbler";
    public static final int ET_TYPE_LONGNOSE = 8;
    public static final String ET_TYPE_LONGNOSE_STRING = "Long nose";
    public static final int ET_TYPE_NOEFFECT = 0;
    public static final String ET_TYPE_NOEFFECT_STRING = "No effect";
    public static final int ET_TYPE_PINCH = 5;
    public static final String ET_TYPE_PINCH_STRING = "Pinch";
    public static final int ET_TYPE_PROFESSOR = 3;
    public static final String ET_TYPE_PROFESSOR_STRING = "Professor";
    public static final int ET_TYPE_UPNOSE = 1;
    public static final String ET_TYPE_UPNOSE_STRING = "Up nose";
    public static final int ET_TYPE_WILDSMILE = 4;
    public static final String ET_TYPE_WILDSMILE_STRING = "Wild Smile";
    public static final int mSize = 8;

    public ArrayList<String> init() {
        ArrayList<String> funnyStringList = new ArrayList();
        funnyStringList.add(0, ET_TYPE_NOEFFECT_STRING);
        funnyStringList.add(1, ET_TYPE_UPNOSE_STRING);
        funnyStringList.add(2, ET_TYPE_BIGFACE_STRING);
        funnyStringList.add(3, ET_TYPE_PROFESSOR_STRING);
        funnyStringList.add(4, ET_TYPE_WILDSMILE_STRING);
        funnyStringList.add(5, "Pinch");
        funnyStringList.add(6, ET_TYPE_ALIEN_STRING);
        funnyStringList.add(7, ET_TYPE_GOBBLER_STRING);
        funnyStringList.add(8, ET_TYPE_LONGNOSE_STRING);
        return funnyStringList;
    }
}
