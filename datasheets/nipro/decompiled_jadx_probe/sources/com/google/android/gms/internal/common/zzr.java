package com.google.android.gms.internal.common;

import java.util.Iterator;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
/* loaded from: classes.dex */
public final class zzr<E> extends zzo<E> {
    public zzr() {
        super(4);
    }

    public final zzr<E> zzb(E e) {
        super.zza(e);
        return this;
    }

    public final zzr<E> zzc(Iterator<? extends E> it) {
        while (it.hasNext()) {
            super.zza(it.next());
        }
        return this;
    }

    zzr(int i) {
        super(4);
    }
}
