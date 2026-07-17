package com.google.android.gms.internal.common;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
/* loaded from: classes.dex */
final class zzs<E> extends zzn<E> {
    private final zzu<E> zza;

    zzs(zzu<E> zzuVar, int i) {
        super(zzuVar.size(), i);
        this.zza = zzuVar;
    }

    @Override // com.google.android.gms.internal.common.zzn
    protected final E zza(int i) {
        return this.zza.get(i);
    }
}
