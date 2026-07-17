package com.google.android.gms.common.logging;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RecentlyNonNull;
import com.google.android.gms.common.annotation.KeepForSdk;
import com.google.android.gms.common.internal.GmsLogger;
import java.util.Locale;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
@KeepForSdk
/* loaded from: classes.dex */
public class Logger {
    private final String zza;
    private final String zzb;
    private final GmsLogger zzc;
    private final int zzd;

    @KeepForSdk
    public Logger(@RecentlyNonNull String str, @RecentlyNonNull String... strArr) {
        String sb;
        if (strArr.length == 0) {
            sb = "";
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append('[');
            for (String str2 : strArr) {
                if (sb2.length() > 1) {
                    sb2.append(",");
                }
                sb2.append(str2);
            }
            sb2.append("] ");
            sb = sb2.toString();
        }
        this.zzb = sb;
        this.zza = str;
        this.zzc = new GmsLogger(str);
        int i = 2;
        while (i <= 7 && !Log.isLoggable(this.zza, i)) {
            i++;
        }
        this.zzd = i;
    }

    @KeepForSdk
    public void d(@RecentlyNonNull String str, @RecentlyNonNull Throwable th, @Nullable Object... objArr) {
        if (isLoggable(3)) {
            Log.d(this.zza, format(str, objArr), th);
        }
    }

    @KeepForSdk
    public void e(@RecentlyNonNull String str, @RecentlyNonNull Throwable th, @Nullable Object... objArr) {
        Log.e(this.zza, format(str, objArr), th);
    }

    @RecentlyNonNull
    @KeepForSdk
    protected String format(@RecentlyNonNull String str, @Nullable Object... objArr) {
        if (objArr != null && objArr.length > 0) {
            str = String.format(Locale.US, str, objArr);
        }
        return this.zzb.concat(str);
    }

    @RecentlyNonNull
    @KeepForSdk
    public String getTag() {
        return this.zza;
    }

    @KeepForSdk
    public void i(@RecentlyNonNull String str, @Nullable Object... objArr) {
        Log.i(this.zza, format(str, objArr));
    }

    @KeepForSdk
    public boolean isLoggable(int i) {
        return this.zzd <= i;
    }

    @KeepForSdk
    public void v(@RecentlyNonNull String str, @RecentlyNonNull Throwable th, @Nullable Object... objArr) {
        if (isLoggable(2)) {
            Log.v(this.zza, format(str, objArr), th);
        }
    }

    @KeepForSdk
    public void w(@RecentlyNonNull String str, @Nullable Object... objArr) {
        Log.w(this.zza, format(str, objArr));
    }

    @KeepForSdk
    public void wtf(@RecentlyNonNull String str, @RecentlyNonNull Throwable th, @Nullable Object... objArr) {
        Log.wtf(this.zza, format(str, objArr), th);
    }

    @KeepForSdk
    public void e(@RecentlyNonNull String str, @Nullable Object... objArr) {
        Log.e(this.zza, format(str, objArr));
    }

    @KeepForSdk
    public void wtf(@RecentlyNonNull Throwable th) {
        Log.wtf(this.zza, th);
    }

    @KeepForSdk
    public void d(@RecentlyNonNull String str, @Nullable Object... objArr) {
        if (isLoggable(3)) {
            Log.d(this.zza, format(str, objArr));
        }
    }

    @KeepForSdk
    public void v(@RecentlyNonNull String str, @Nullable Object... objArr) {
        if (isLoggable(2)) {
            Log.v(this.zza, format(str, objArr));
        }
    }
}
