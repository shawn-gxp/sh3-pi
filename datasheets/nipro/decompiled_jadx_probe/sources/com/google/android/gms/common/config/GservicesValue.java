package com.google.android.gms.common.config;

import android.os.Binder;
import android.os.StrictMode;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RecentlyNonNull;
import com.google.android.gms.common.annotation.KeepForSdk;
import com.google.android.gms.common.util.VisibleForTesting;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
@KeepForSdk
/* loaded from: classes.dex */
public abstract class GservicesValue<T> {
    private static final Object zzc = new Object();

    @RecentlyNonNull
    protected final String zza;

    @RecentlyNonNull
    protected final T zzb;

    @Nullable
    private T zzd = null;

    protected GservicesValue(@RecentlyNonNull String str, @RecentlyNonNull T t) {
        this.zza = str;
        this.zzb = t;
    }

    @KeepForSdk
    public static boolean isInitialized() {
        synchronized (zzc) {
        }
        return false;
    }

    @RecentlyNonNull
    @KeepForSdk
    public static GservicesValue<Float> value(@RecentlyNonNull String str, @RecentlyNonNull Float f) {
        return new zzd(str, f);
    }

    @RecentlyNonNull
    @KeepForSdk
    public final T get() {
        T t = this.zzd;
        if (t != null) {
            return t;
        }
        StrictMode.ThreadPolicy allowThreadDiskReads = StrictMode.allowThreadDiskReads();
        synchronized (zzc) {
        }
        synchronized (zzc) {
            try {
            } catch (Throwable th) {
                StrictMode.setThreadPolicy(allowThreadDiskReads);
                throw th;
            }
        }
        try {
            T zza = zza(this.zza);
            StrictMode.setThreadPolicy(allowThreadDiskReads);
            return zza;
        } catch (SecurityException unused) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                T zza2 = zza(this.zza);
                StrictMode.setThreadPolicy(allowThreadDiskReads);
                return zza2;
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
    }

    @RecentlyNonNull
    @KeepForSdk
    @Deprecated
    public final T getBinderSafe() {
        return get();
    }

    @VisibleForTesting
    @KeepForSdk
    public void override(@RecentlyNonNull T t) {
        Log.w("GservicesValue", "GservicesValue.override(): test should probably call initForTests() first");
        this.zzd = t;
        synchronized (zzc) {
            synchronized (zzc) {
            }
        }
    }

    @VisibleForTesting
    @KeepForSdk
    public void resetOverride() {
        this.zzd = null;
    }

    @RecentlyNonNull
    protected abstract T zza(@RecentlyNonNull String str);

    @RecentlyNonNull
    @KeepForSdk
    public static GservicesValue<Integer> value(@RecentlyNonNull String str, @RecentlyNonNull Integer num) {
        return new zzc(str, num);
    }

    @RecentlyNonNull
    @KeepForSdk
    public static GservicesValue<Long> value(@RecentlyNonNull String str, @RecentlyNonNull Long l) {
        return new zzb(str, l);
    }

    @RecentlyNonNull
    @KeepForSdk
    public static GservicesValue<String> value(@RecentlyNonNull String str, @RecentlyNonNull String str2) {
        return new zze(str, str2);
    }

    @RecentlyNonNull
    @KeepForSdk
    public static GservicesValue<Boolean> value(@RecentlyNonNull String str, boolean z) {
        return new zza(str, Boolean.valueOf(z));
    }
}
