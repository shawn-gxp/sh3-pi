package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.internal.BaseGmsClient;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
/* loaded from: classes.dex */
final class zabk implements BaseGmsClient.SignOutCallbacks {
    final /* synthetic */ zabl zaa;

    zabk(zabl zablVar) {
        this.zaa = zablVar;
    }

    @Override // com.google.android.gms.common.internal.BaseGmsClient.SignOutCallbacks
    public final void onSignOutComplete() {
        this.zaa.zaa.zat.post(new zabj(this));
    }
}
