package jp.co.nipro.cocoron.common;

import android.content.SharedPreferences;
import java.util.Set;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: AppPrefsUtils.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\"\n\u0000\n\u0002\u0010\u0002\n\u0002\b\b\bÆ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0018\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00042\b\b\u0002\u0010\f\u001a\u00020\nJ\u0018\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\u00042\b\b\u0002\u0010\f\u001a\u00020\u000eJ\u0018\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u000b\u001a\u00020\u00042\b\b\u0002\u0010\f\u001a\u00020\u0010J\u001c\u0010\u0011\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u000b\u001a\u00020\u00042\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0004J\u0016\u0010\u0012\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u00132\u0006\u0010\u000b\u001a\u00020\u0004J\u0016\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\nJ\u0016\u0010\u0017\u001a\u00020\u00152\u0006\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u000eJ\u0016\u0010\u0018\u001a\u00020\u00152\u0006\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u0010J\u0016\u0010\u0019\u001a\u00020\u00152\u0006\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u0004J\u001c\u0010\u001a\u001a\u00020\u00152\u0006\u0010\u000b\u001a\u00020\u00042\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00040\u0013J\u000e\u0010\u001c\u001a\u00020\u00152\u0006\u0010\u000b\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u001d"}, d2 = {"Ljp/co/nipro/cocoron/common/AppPrefsUtils;", "", "()V", "TABLE_PREFS", "", "ed", "Landroid/content/SharedPreferences$Editor;", "sp", "Landroid/content/SharedPreferences;", "getBoolean", "", "key", "defValue", "getInt", "", "getLong", "", "getString", "getStringSet", "", "putBoolean", "", "value", "putInt", "putLong", "putString", "putStringSet", "set", "remove", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class AppPrefsUtils {
    public static final AppPrefsUtils INSTANCE = new AppPrefsUtils();
    public static final String TABLE_PREFS = "cocoron";
    private static SharedPreferences.Editor ed;
    private static SharedPreferences sp;

    static {
        SharedPreferences sharedPreferences = BaseApplication.INSTANCE.getContext().getSharedPreferences(TABLE_PREFS, 0);
        Intrinsics.checkNotNullExpressionValue(sharedPreferences, "BaseApplication.context.…FS, Context.MODE_PRIVATE)");
        sp = sharedPreferences;
        SharedPreferences.Editor edit = sharedPreferences.edit();
        Intrinsics.checkNotNullExpressionValue(edit, "sp.edit()");
        ed = edit;
    }

    private AppPrefsUtils() {
    }

    public final void putBoolean(String key, boolean value) {
        Intrinsics.checkNotNullParameter(key, "key");
        ed.putBoolean(key, value);
        ed.commit();
    }

    public static /* synthetic */ boolean getBoolean$default(AppPrefsUtils appPrefsUtils, String str, boolean z, int i, Object obj) {
        if ((i & 2) != 0) {
            z = true;
        }
        return appPrefsUtils.getBoolean(str, z);
    }

    public final boolean getBoolean(String key, boolean defValue) {
        Intrinsics.checkNotNullParameter(key, "key");
        return sp.getBoolean(key, defValue);
    }

    public final void putString(String key, String value) {
        Intrinsics.checkNotNullParameter(key, "key");
        Intrinsics.checkNotNullParameter(value, "value");
        ed.putString(key, value);
        ed.commit();
    }

    public static /* synthetic */ String getString$default(AppPrefsUtils appPrefsUtils, String str, String str2, int i, Object obj) {
        if ((i & 2) != 0) {
            str2 = (String) null;
        }
        return appPrefsUtils.getString(str, str2);
    }

    public final String getString(String key, String defValue) {
        Intrinsics.checkNotNullParameter(key, "key");
        return sp.getString(key, defValue);
    }

    public final void putInt(String key, int value) {
        Intrinsics.checkNotNullParameter(key, "key");
        ed.putInt(key, value);
        ed.commit();
    }

    public static /* synthetic */ int getInt$default(AppPrefsUtils appPrefsUtils, String str, int i, int i2, Object obj) {
        if ((i2 & 2) != 0) {
            i = 0;
        }
        return appPrefsUtils.getInt(str, i);
    }

    public final int getInt(String key, int defValue) {
        Intrinsics.checkNotNullParameter(key, "key");
        return sp.getInt(key, defValue);
    }

    public final void putLong(String key, long value) {
        Intrinsics.checkNotNullParameter(key, "key");
        ed.putLong(key, value);
        ed.commit();
    }

    public static /* synthetic */ long getLong$default(AppPrefsUtils appPrefsUtils, String str, long j, int i, Object obj) {
        if ((i & 2) != 0) {
            j = 0;
        }
        return appPrefsUtils.getLong(str, j);
    }

    public final long getLong(String key, long defValue) {
        Intrinsics.checkNotNullParameter(key, "key");
        return sp.getLong(key, defValue);
    }

    public final void putStringSet(String key, Set<String> set) {
        Intrinsics.checkNotNullParameter(key, "key");
        Intrinsics.checkNotNullParameter(set, "set");
        Set<String> stringSet = getStringSet(key);
        Set<String> mutableSet = stringSet != null ? CollectionsKt.toMutableSet(stringSet) : null;
        if (mutableSet != null) {
            mutableSet.addAll(set);
        }
        ed.putStringSet(key, mutableSet);
        ed.commit();
    }

    public final Set<String> getStringSet(String key) {
        Intrinsics.checkNotNullParameter(key, "key");
        return sp.getStringSet(key, SetsKt.emptySet());
    }

    public final void remove(String key) {
        Intrinsics.checkNotNullParameter(key, "key");
        ed.remove(key);
        ed.commit();
    }
}
