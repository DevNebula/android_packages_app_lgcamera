package android.support.p000v4.p003os;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.UserManager;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;

@TargetApi(24)
@RequiresApi(24)
@RestrictTo({Scope.LIBRARY_GROUP})
/* renamed from: android.support.v4.os.UserManagerCompatApi24 */
public class UserManagerCompatApi24 {
    public static boolean isUserUnlocked(Context context) {
        return ((UserManager) context.getSystemService(UserManager.class)).isUserUnlocked();
    }
}
