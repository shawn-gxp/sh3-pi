package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.ConnectionResult;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
/* loaded from: classes.dex */
final class zacl implements Runnable {
    final /* synthetic */ zaco zaa;

    zacl(zaco zacoVar) {
        this.zaa = zacoVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        zacn zacnVar;
        zacnVar = this.zaa.zah;
        zacnVar.zaa(new ConnectionResult(4));
    }
}
