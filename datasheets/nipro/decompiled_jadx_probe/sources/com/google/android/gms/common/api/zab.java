package com.google.android.gms.common.api;

import com.google.android.gms.common.api.PendingResult;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
/* loaded from: classes.dex */
final class zab implements PendingResult.StatusListener {
    final /* synthetic */ Batch zaa;

    zab(Batch batch) {
        this.zaa = batch;
    }

    @Override // com.google.android.gms.common.api.PendingResult.StatusListener
    public final void onComplete(Status status) {
        Object obj;
        int i;
        boolean z;
        boolean z2;
        PendingResult[] pendingResultArr;
        obj = this.zaa.zai;
        synchronized (obj) {
            if (this.zaa.isCanceled()) {
                return;
            }
            if (status.isCanceled()) {
                Batch.zab(this.zaa, true);
            } else if (!status.isSuccess()) {
                Batch.zac(this.zaa, true);
            }
            Batch.zad(this.zaa);
            i = this.zaa.zae;
            if (i == 0) {
                z = this.zaa.zag;
                if (z) {
                    super/*com.google.android.gms.common.api.internal.BasePendingResult*/.cancel();
                } else {
                    z2 = this.zaa.zaf;
                    Status status2 = z2 ? new Status(13) : Status.RESULT_SUCCESS;
                    Batch batch = this.zaa;
                    pendingResultArr = batch.zah;
                    batch.setResult(new BatchResult(status2, pendingResultArr));
                }
            }
        }
    }
}
