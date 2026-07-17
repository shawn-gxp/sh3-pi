package com.google.android.gms.common.api.internal;

import android.content.Context;
import com.google.android.gms.common.GoogleApiAvailabilityLight;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
/* loaded from: classes.dex */
final class zaah implements Runnable {
    final /* synthetic */ zaar zaa;

    zaah(zaar zaarVar) {
        this.zaa = zaarVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        GoogleApiAvailabilityLight googleApiAvailabilityLight;
        Context context;
        googleApiAvailabilityLight = this.zaa.zad;
        context = this.zaa.zac;
        googleApiAvailabilityLight.cancelAvailabilityErrorNotifications(context);
    }
}
