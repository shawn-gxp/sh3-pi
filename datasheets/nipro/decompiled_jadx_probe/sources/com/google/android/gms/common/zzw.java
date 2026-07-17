package com.google.android.gms.common;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.util.AndroidUtilsLight;
import com.google.android.gms.common.util.Hex;
import java.security.MessageDigest;
import java.util.concurrent.Callable;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
@CheckReturnValue
/* loaded from: classes.dex */
class zzw {
    private static final zzw zzd = new zzw(true, null, null);
    final boolean zza;

    @Nullable
    final String zzb;

    @Nullable
    final Throwable zzc;

    zzw(boolean z, @Nullable String str, @Nullable Throwable th) {
        this.zza = z;
        this.zzb = str;
        this.zzc = th;
    }

    static zzw zzb() {
        return zzd;
    }

    static zzw zzc(Callable<String> callable) {
        return new zzv(callable, null);
    }

    static zzw zzd(@NonNull String str) {
        return new zzw(false, str, null);
    }

    static zzw zze(@NonNull String str, @NonNull Throwable th) {
        return new zzw(false, str, th);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String zzg(String str, zzi zziVar, boolean z, boolean z2) {
        String str2 = true != z2 ? "not allowed" : "debug cert rejected";
        MessageDigest zza = AndroidUtilsLight.zza("SHA-1");
        Preconditions.checkNotNull(zza);
        return String.format("%s: pkg=%s, sha1=%s, atk=%s, ver=%s", str2, str, Hex.bytesToStringLowercase(zza.digest(zziVar.zzc())), Boolean.valueOf(z), "12451000.false");
    }

    @Nullable
    String zza() {
        return this.zzb;
    }

    final void zzf() {
        if (this.zza || !Log.isLoggable("GoogleCertificatesRslt", 3)) {
            return;
        }
        if (this.zzc != null) {
            Log.d("GoogleCertificatesRslt", zza(), this.zzc);
        } else {
            Log.d("GoogleCertificatesRslt", zza());
        }
    }
}
