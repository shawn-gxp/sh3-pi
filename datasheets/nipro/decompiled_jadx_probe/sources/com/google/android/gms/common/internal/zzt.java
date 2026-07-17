package com.google.android.gms.common.internal;

import androidx.annotation.Nullable;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
/* loaded from: classes.dex */
public final class zzt {

    @Nullable
    private final String zza;
    private final String zzb;
    private final int zzc;
    private final boolean zzd;

    public zzt(String str, @Nullable String str2, boolean z, int i, boolean z2) {
        this.zzb = str;
        this.zza = str2;
        this.zzc = i;
        this.zzd = z2;
    }

    @Nullable
    final String zza() {
        return this.zza;
    }

    final String zzb() {
        return this.zzb;
    }

    final int zzc() {
        return this.zzc;
    }

    final boolean zzd() {
        return this.zzd;
    }
}
