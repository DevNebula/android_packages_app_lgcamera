package com.lge.camera.managers;

public class QuickclipManagerIF {

    public interface onQuickClipListListener {
        void onListClosed();

        void onListOpend();
    }

    public enum DrawerShowOption {
        OPEN,
        CLOSE,
        KEEP_CURRENT_STATE
    }
}
