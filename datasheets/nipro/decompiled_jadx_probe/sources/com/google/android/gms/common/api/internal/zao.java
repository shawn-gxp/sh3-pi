package com.google.android.gms.common.api.internal;

import android.app.Dialog;
import android.app.PendingIntent;
import androidx.annotation.MainThread;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.internal.Preconditions;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
/* loaded from: classes.dex */
final class zao implements Runnable {
    final /* synthetic */ zap zaa;
    private final zam zab;

    zao(zap zapVar, zam zamVar) {
        this.zaa = zapVar;
        this.zab = zamVar;
    }

    @Override // java.lang.Runnable
    @MainThread
    public final void run() {
        if (this.zaa.zaa) {
            ConnectionResult zab = this.zab.zab();
            if (zab.hasResolution()) {
                zap zapVar = this.zaa;
                zapVar.mLifecycleFragment.startActivityForResult(GoogleApiActivity.zaa(zapVar.getActivity(), (PendingIntent) Preconditions.checkNotNull(zab.getResolution()), this.zab.zaa(), false), 1);
                return;
            }
            zap zapVar2 = this.zaa;
            if (zapVar2.zac.getErrorResolutionIntent(zapVar2.getActivity(), zab.getErrorCode(), null) != null) {
                zap zapVar3 = this.zaa;
                zapVar3.zac.zaa(zapVar3.getActivity(), this.zaa.mLifecycleFragment, zab.getErrorCode(), 2, this.zaa);
            } else {
                if (zab.getErrorCode() != 18) {
                    this.zaa.zab(zab, this.zab.zaa());
                    return;
                }
                zap zapVar4 = this.zaa;
                Dialog zad = zapVar4.zac.zad(zapVar4.getActivity(), this.zaa);
                zap zapVar5 = this.zaa;
                zapVar5.zac.zae(zapVar5.getActivity().getApplicationContext(), new zan(this, zad));
            }
        }
    }
}
