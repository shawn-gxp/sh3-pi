package com.google.android.gms.common.api;

import androidx.annotation.RecentlyNonNull;
import com.google.android.gms.common.api.internal.BasePendingResult;
import java.util.ArrayList;
import java.util.List;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
/* loaded from: classes.dex */
public final class Batch extends BasePendingResult<BatchResult> {
    private int zae;
    private boolean zaf;
    private boolean zag;
    private final PendingResult<?>[] zah;
    private final Object zai;

    /* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
    public static final class Builder {
        private List<PendingResult<?>> zaa = new ArrayList();
        private GoogleApiClient zab;

        public Builder(@RecentlyNonNull GoogleApiClient googleApiClient) {
            this.zab = googleApiClient;
        }

        @RecentlyNonNull
        public <R extends Result> BatchResultToken<R> add(@RecentlyNonNull PendingResult<R> pendingResult) {
            BatchResultToken<R> batchResultToken = new BatchResultToken<>(this.zaa.size());
            this.zaa.add(pendingResult);
            return batchResultToken;
        }

        @RecentlyNonNull
        public Batch build() {
            return new Batch(this.zaa, this.zab, null);
        }
    }

    /* synthetic */ Batch(List list, GoogleApiClient googleApiClient, zab zabVar) {
        super(googleApiClient);
        this.zai = new Object();
        int size = list.size();
        this.zae = size;
        this.zah = new PendingResult[size];
        if (list.isEmpty()) {
            setResult(new BatchResult(Status.RESULT_SUCCESS, this.zah));
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            PendingResult<?> pendingResult = (PendingResult) list.get(i);
            this.zah[i] = pendingResult;
            pendingResult.addStatusListener(new zab(this));
        }
    }

    static /* synthetic */ boolean zab(Batch batch, boolean z) {
        batch.zag = true;
        return true;
    }

    static /* synthetic */ boolean zac(Batch batch, boolean z) {
        batch.zaf = true;
        return true;
    }

    static /* synthetic */ int zad(Batch batch) {
        int i = batch.zae;
        batch.zae = i - 1;
        return i;
    }

    @Override // com.google.android.gms.common.api.internal.BasePendingResult, com.google.android.gms.common.api.PendingResult
    public void cancel() {
        super.cancel();
        for (PendingResult<?> pendingResult : this.zah) {
            pendingResult.cancel();
        }
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.google.android.gms.common.api.internal.BasePendingResult
    @RecentlyNonNull
    public BatchResult createFailedResult(@RecentlyNonNull Status status) {
        return new BatchResult(status, this.zah);
    }
}
