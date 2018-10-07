package com.arcsoft.stickerlibrary.utils;

import com.arcsoft.stickerlibrary.utils.XMLParser.SuperParams;
import java.util.ArrayList;

public class TemplateItem {
    private ArrayList<SuperParams> mConfigList = null;
    private String mConfigPath = null;

    public void setConfigPath(String configPath) {
        this.mConfigPath = configPath;
    }

    public void setConfigList(ArrayList<SuperParams> configList) {
        this.mConfigList = configList;
    }

    public String getConfigPath() {
        return this.mConfigPath;
    }

    public ArrayList<SuperParams> getConfigList() {
        return this.mConfigList;
    }
}
