package com.google.android.gms.common.api.internal;

import androidx.annotation.WorkerThread;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.internal.IAccountAccessor;
import java.util.ArrayList;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
/* loaded from: classes.dex */
final class zaam extends zaaq {
    final /* synthetic */ zaar zaa;
    private final ArrayList<Api.Client> zac;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public zaam(zaar zaarVar, ArrayList<Api.Client> arrayList) {
        super(zaarVar, null);
        this.zaa = zaarVar;
        this.zac = arrayList;
    }

    @Override // com.google.android.gms.common.api.internal.zaaq
    @WorkerThread
    public final void zaa() {
        zabd zabdVar;
        IAccountAccessor iAccountAccessor;
        zabd zabdVar2;
        zabdVar = this.zaa.zaa;
        zabdVar.zag.zad = zaar.zap(this.zaa);
        ArrayList<Api.Client> arrayList = this.zac;
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            Api.Client client = arrayList.get(i);
            iAccountAccessor = this.zaa.zao;
            zabdVar2 = this.zaa.zaa;
            client.getRemoteService(iAccountAccessor, zabdVar2.zag.zad);
        }
    }
}
