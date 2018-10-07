package android.support.p000v4.media;

import android.annotation.TargetApi;
import android.media.browse.MediaBrowser.MediaItem;
import android.support.annotation.RequiresApi;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@TargetApi(21)
@RequiresApi(21)
/* renamed from: android.support.v4.media.ParceledListSliceAdapterApi21 */
class ParceledListSliceAdapterApi21 {
    private static Constructor sConstructor;

    ParceledListSliceAdapterApi21() {
    }

    static {
        ReflectiveOperationException e;
        try {
            sConstructor = Class.forName("android.content.pm.ParceledListSlice").getConstructor(new Class[]{List.class});
            return;
        } catch (ClassNotFoundException e2) {
            e = e2;
        } catch (NoSuchMethodException e3) {
            e = e3;
        }
        e.printStackTrace();
    }

    static Object newInstance(List<MediaItem> itemList) {
        ReflectiveOperationException e;
        Object result = null;
        try {
            return sConstructor.newInstance(new Object[]{itemList});
        } catch (InstantiationException e2) {
            e = e2;
        } catch (IllegalAccessException e3) {
            e = e3;
        } catch (InvocationTargetException e4) {
            e = e4;
        }
        e.printStackTrace();
        return result;
    }
}
