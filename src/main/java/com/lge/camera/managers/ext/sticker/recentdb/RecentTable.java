package com.lge.camera.managers.ext.sticker.recentdb;

public interface RecentTable {
    public static final String[] COLUMNS = new String[]{"_id", STICKER_ID, LAST_USED_TIME, ICON_PATH, STICKER_NAME, SOLUTION_TYPE, STICKER_DATA_POSITION, CONFIG_FILE_PATH, EXTRA_1, EXTRA_2, EXTRA_3};
    public static final String CONFIG_FILE_PATH = "_config_file_path";
    public static final int DATA_POSITION_ASSETS = -1;
    public static final int DATA_POSITION_DOWNLOAD = 1;
    public static final int DATA_POSITION_INTERNAL = 0;
    public static final String EXTRA_1 = "_extra_1";
    public static final String EXTRA_2 = "_extra_2";
    public static final String EXTRA_3 = "_extra_3";
    public static final String ICON_PATH = "_icon_path";
    /* renamed from: ID */
    public static final String f39ID = "_id";
    public static final String LAST_USED_TIME = "_last_used_time";
    public static final String LIMIT_COUNT = "10";
    public static final String QUERY_CREATE = "create table Recent_Sticker_Table(_id integer primary key autoincrement, _sticker_id text, _last_used_time datetime, _icon_path text, _sticker_name text, _solution_type integer, _sticker_data_position integer, _config_file_path text, _extra_1 text, _extra_2 text, _extra_3 text);";
    public static final String QUERY_DROP = "drop table Recent_Sticker_Table if exists";
    public static final String SOLUTION_TYPE = "_solution_type";
    public static final String STICKER_DATA_POSITION = "_sticker_data_position";
    public static final String STICKER_ID = "_sticker_id";
    public static final String STICKER_NAME = "_sticker_name";
    public static final String TABLE_NAME = "Recent_Sticker_Table";
}
