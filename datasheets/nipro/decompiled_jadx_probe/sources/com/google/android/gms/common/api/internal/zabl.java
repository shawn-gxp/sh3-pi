package com.google.android.gms.common.api.internal;

import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.collection.ArrayMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Feature;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.UnsupportedApiCallException;
import com.google.android.gms.common.api.internal.ListenerHolder;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
/* loaded from: classes.dex */
public final class zabl<O extends Api.ApiOptions> implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, zat {
    final /* synthetic */ GoogleApiManager zaa;

    @NotOnlyInitialized
    private final Api.Client zac;
    private final ApiKey<O> zad;
    private final int zah;

    @Nullable
    private final zaco zai;
    private boolean zaj;
    private final Queue<zai> zab = new LinkedList();
    private final Set<zal> zaf = new HashSet();
    private final Map<ListenerHolder.ListenerKey<?>, zacc> zag = new HashMap();
    private final List<zabm> zak = new ArrayList();

    @Nullable
    private ConnectionResult zal = null;
    private int zam = 0;
    private final zaaa zae = new zaaa();

    @WorkerThread
    public zabl(GoogleApiManager googleApiManager, GoogleApi<O> googleApi) {
        this.zaa = googleApiManager;
        this.zac = googleApi.zaa(googleApiManager.zat.getLooper(), this);
        this.zad = googleApi.getApiKey();
        this.zah = googleApi.zab();
        if (this.zac.requiresSignIn()) {
            this.zai = googleApi.zac(googleApiManager.zak, googleApiManager.zat);
        } else {
            this.zai = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    @WorkerThread
    public final void zaB() {
        zah();
        zaM(ConnectionResult.RESULT_SUCCESS);
        zaJ();
        Iterator<zacc> it = this.zag.values().iterator();
        while (it.hasNext()) {
            zacc next = it.next();
            if (zaN(next.zaa.getRequiredFeatures()) != null) {
                it.remove();
            } else {
                try {
                    next.zaa.registerListener(this.zac, new TaskCompletionSource<>());
                } catch (DeadObjectException unused) {
                    onConnectionSuspended(3);
                    this.zac.disconnect("DeadObjectException thrown while calling register listener method.");
                } catch (RemoteException unused2) {
                    it.remove();
                }
            }
        }
        zaE();
        zaK();
    }

    /* JADX INFO: Access modifiers changed from: private */
    @WorkerThread
    public final void zaC(int i) {
        zah();
        this.zaj = true;
        this.zae.zae(i, this.zac.getLastDisconnectMessage());
        this.zaa.zat.sendMessageDelayed(Message.obtain(this.zaa.zat, 9, this.zad), this.zaa.zac);
        this.zaa.zat.sendMessageDelayed(Message.obtain(this.zaa.zat, 11, this.zad), this.zaa.zad);
        this.zaa.zam.zac();
        Iterator<zacc> it = this.zag.values().iterator();
        while (it.hasNext()) {
            it.next().zac.run();
        }
    }

    @WorkerThread
    private final boolean zaD(@NonNull ConnectionResult connectionResult) {
        synchronized (GoogleApiManager.zag) {
            if (this.zaa.zaq == null || !this.zaa.zar.contains(this.zad)) {
                return false;
            }
            this.zaa.zaq.zaf(connectionResult, this.zah);
            return true;
        }
    }

    @WorkerThread
    private final void zaE() {
        ArrayList arrayList = new ArrayList(this.zab);
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            zai zaiVar = (zai) arrayList.get(i);
            if (!this.zac.isConnected()) {
                return;
            }
            if (zaF(zaiVar)) {
                this.zab.remove(zaiVar);
            }
        }
    }

    @WorkerThread
    private final boolean zaF(zai zaiVar) {
        if (!(zaiVar instanceof zac)) {
            zaG(zaiVar);
            return true;
        }
        zac zacVar = (zac) zaiVar;
        Feature zaN = zaN(zacVar.zaa(this));
        if (zaN == null) {
            zaG(zaiVar);
            return true;
        }
        String name = this.zac.getClass().getName();
        String name2 = zaN.getName();
        long version = zaN.getVersion();
        StringBuilder sb = new StringBuilder(String.valueOf(name).length() + 77 + String.valueOf(name2).length());
        sb.append(name);
        sb.append(" could not execute call because it requires feature (");
        sb.append(name2);
        sb.append(", ");
        sb.append(version);
        sb.append(").");
        Log.w("GoogleApiManager", sb.toString());
        if (!this.zaa.zau || !zacVar.zab(this)) {
            zacVar.zad(new UnsupportedApiCallException(zaN));
            return true;
        }
        zabm zabmVar = new zabm(this.zad, zaN, null);
        int indexOf = this.zak.indexOf(zabmVar);
        if (indexOf >= 0) {
            zabm zabmVar2 = this.zak.get(indexOf);
            this.zaa.zat.removeMessages(15, zabmVar2);
            this.zaa.zat.sendMessageDelayed(Message.obtain(this.zaa.zat, 15, zabmVar2), this.zaa.zac);
            return false;
        }
        this.zak.add(zabmVar);
        this.zaa.zat.sendMessageDelayed(Message.obtain(this.zaa.zat, 15, zabmVar), this.zaa.zac);
        this.zaa.zat.sendMessageDelayed(Message.obtain(this.zaa.zat, 16, zabmVar), this.zaa.zad);
        ConnectionResult connectionResult = new ConnectionResult(2, null);
        if (zaD(connectionResult)) {
            return false;
        }
        this.zaa.zap(connectionResult, this.zah);
        return false;
    }

    @WorkerThread
    private final void zaG(zai zaiVar) {
        zaiVar.zae(this.zae, zap());
        try {
            zaiVar.zaf(this);
        } catch (DeadObjectException unused) {
            onConnectionSuspended(1);
            this.zac.disconnect("DeadObjectException thrown while running ApiCallRunner.");
        } catch (Throwable th) {
            throw new IllegalStateException(String.format("Error in GoogleApi implementation for client %s.", this.zac.getClass().getName()), th);
        }
    }

    @WorkerThread
    private final void zaH(@Nullable Status status, @Nullable Exception exc, boolean z) {
        Preconditions.checkHandlerThread(this.zaa.zat);
        if ((status == null) == (exc == null)) {
            throw new IllegalArgumentException("Status XOR exception should be null");
        }
        Iterator<zai> it = this.zab.iterator();
        while (it.hasNext()) {
            zai next = it.next();
            if (!z || next.zac == 2) {
                if (status != null) {
                    next.zac(status);
                } else {
                    next.zad(exc);
                }
                it.remove();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    @WorkerThread
    public final void zaI(Status status) {
        Preconditions.checkHandlerThread(this.zaa.zat);
        zaH(status, null, false);
    }

    @WorkerThread
    private final void zaJ() {
        if (this.zaj) {
            this.zaa.zat.removeMessages(11, this.zad);
            this.zaa.zat.removeMessages(9, this.zad);
            this.zaj = false;
        }
    }

    private final void zaK() {
        this.zaa.zat.removeMessages(12, this.zad);
        this.zaa.zat.sendMessageDelayed(this.zaa.zat.obtainMessage(12, this.zad), this.zaa.zae);
    }

    /* JADX INFO: Access modifiers changed from: private */
    @WorkerThread
    public final boolean zaL(boolean z) {
        Preconditions.checkHandlerThread(this.zaa.zat);
        if (!this.zac.isConnected() || this.zag.size() != 0) {
            return false;
        }
        if (!this.zae.zac()) {
            this.zac.disconnect("Timing out service connection.");
            return true;
        }
        if (z) {
            zaK();
        }
        return false;
    }

    @WorkerThread
    private final void zaM(ConnectionResult connectionResult) {
        Iterator<zal> it = this.zaf.iterator();
        while (it.hasNext()) {
            it.next().zac(this.zad, connectionResult, Objects.equal(connectionResult, ConnectionResult.RESULT_SUCCESS) ? this.zac.getEndpointPackageName() : null);
        }
        this.zaf.clear();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Nullable
    @WorkerThread
    private final Feature zaN(@Nullable Feature[] featureArr) {
        if (featureArr != null && featureArr.length != 0) {
            Feature[] availableFeatures = this.zac.getAvailableFeatures();
            if (availableFeatures == null) {
                availableFeatures = new Feature[0];
            }
            ArrayMap arrayMap = new ArrayMap(availableFeatures.length);
            for (Feature feature : availableFeatures) {
                arrayMap.put(feature.getName(), Long.valueOf(feature.getVersion()));
            }
            for (Feature feature2 : featureArr) {
                Long l = (Long) arrayMap.get(feature2.getName());
                if (l == null || l.longValue() < feature2.getVersion()) {
                    return feature2;
                }
            }
        }
        return null;
    }

    static /* synthetic */ void zau(zabl zablVar, zabm zabmVar) {
        if (zablVar.zak.contains(zabmVar) && !zablVar.zaj) {
            if (zablVar.zac.isConnected()) {
                zablVar.zaE();
            } else {
                zablVar.zam();
            }
        }
    }

    static /* synthetic */ void zav(zabl zablVar, zabm zabmVar) {
        Feature feature;
        Feature[] zaa;
        if (zablVar.zak.remove(zabmVar)) {
            zablVar.zaa.zat.removeMessages(15, zabmVar);
            zablVar.zaa.zat.removeMessages(16, zabmVar);
            feature = zabmVar.zab;
            ArrayList arrayList = new ArrayList(zablVar.zab.size());
            for (zai zaiVar : zablVar.zab) {
                if ((zaiVar instanceof zac) && (zaa = ((zac) zaiVar).zaa(zablVar)) != null && ArrayUtils.contains(zaa, feature)) {
                    arrayList.add(zaiVar);
                }
            }
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                zai zaiVar2 = (zai) arrayList.get(i);
                zablVar.zab.remove(zaiVar2);
                zaiVar2.zad(new UnsupportedApiCallException(feature));
            }
        }
    }

    @Override // com.google.android.gms.common.api.internal.ConnectionCallbacks
    public final void onConnected(@Nullable Bundle bundle) {
        if (Looper.myLooper() == this.zaa.zat.getLooper()) {
            zaB();
        } else {
            this.zaa.zat.post(new zabh(this));
        }
    }

    @Override // com.google.android.gms.common.api.internal.OnConnectionFailedListener
    @WorkerThread
    public final void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        zac(connectionResult, null);
    }

    @Override // com.google.android.gms.common.api.internal.ConnectionCallbacks
    public final void onConnectionSuspended(int i) {
        if (Looper.myLooper() == this.zaa.zat.getLooper()) {
            zaC(i);
        } else {
            this.zaa.zat.post(new zabi(this, i));
        }
    }

    @Override // com.google.android.gms.common.api.internal.zat
    public final void zaa(ConnectionResult connectionResult, Api<?> api, boolean z) {
        throw null;
    }

    @WorkerThread
    public final void zab(@NonNull ConnectionResult connectionResult) {
        Preconditions.checkHandlerThread(this.zaa.zat);
        Api.Client client = this.zac;
        String name = client.getClass().getName();
        String valueOf = String.valueOf(connectionResult);
        StringBuilder sb = new StringBuilder(String.valueOf(name).length() + 25 + String.valueOf(valueOf).length());
        sb.append("onSignInFailed for ");
        sb.append(name);
        sb.append(" with ");
        sb.append(valueOf);
        client.disconnect(sb.toString());
        zac(connectionResult, null);
    }

    @WorkerThread
    public final void zac(@NonNull ConnectionResult connectionResult, @Nullable Exception exc) {
        Preconditions.checkHandlerThread(this.zaa.zat);
        zaco zacoVar = this.zai;
        if (zacoVar != null) {
            zacoVar.zad();
        }
        zah();
        this.zaa.zam.zac();
        zaM(connectionResult);
        if ((this.zac instanceof com.google.android.gms.common.internal.service.zap) && connectionResult.getErrorCode() != 24) {
            GoogleApiManager.zaA(this.zaa, true);
            this.zaa.zat.sendMessageDelayed(this.zaa.zat.obtainMessage(19), 300000L);
        }
        if (connectionResult.getErrorCode() == 4) {
            zaI(GoogleApiManager.zab);
            return;
        }
        if (this.zab.isEmpty()) {
            this.zal = connectionResult;
            return;
        }
        if (exc != null) {
            Preconditions.checkHandlerThread(this.zaa.zat);
            zaH(null, exc, false);
            return;
        }
        if (!this.zaa.zau) {
            zaI(GoogleApiManager.zaJ(this.zad, connectionResult));
            return;
        }
        zaH(GoogleApiManager.zaJ(this.zad, connectionResult), null, true);
        if (this.zab.isEmpty() || zaD(connectionResult) || this.zaa.zap(connectionResult, this.zah)) {
            return;
        }
        if (connectionResult.getErrorCode() == 18) {
            this.zaj = true;
        }
        if (this.zaj) {
            this.zaa.zat.sendMessageDelayed(Message.obtain(this.zaa.zat, 9, this.zad), this.zaa.zac);
        } else {
            zaI(GoogleApiManager.zaJ(this.zad, connectionResult));
        }
    }

    @WorkerThread
    public final void zad(zai zaiVar) {
        Preconditions.checkHandlerThread(this.zaa.zat);
        if (this.zac.isConnected()) {
            if (zaF(zaiVar)) {
                zaK();
                return;
            } else {
                this.zab.add(zaiVar);
                return;
            }
        }
        this.zab.add(zaiVar);
        ConnectionResult connectionResult = this.zal;
        if (connectionResult == null || !connectionResult.hasResolution()) {
            zam();
        } else {
            zac(this.zal, null);
        }
    }

    @WorkerThread
    public final void zae() {
        Preconditions.checkHandlerThread(this.zaa.zat);
        zaI(GoogleApiManager.zaa);
        this.zae.zad();
        for (ListenerHolder.ListenerKey listenerKey : (ListenerHolder.ListenerKey[]) this.zag.keySet().toArray(new ListenerHolder.ListenerKey[0])) {
            zad(new zah(listenerKey, new TaskCompletionSource()));
        }
        zaM(new ConnectionResult(4));
        if (this.zac.isConnected()) {
            this.zac.onUserSignOut(new zabk(this));
        }
    }

    public final Api.Client zaf() {
        return this.zac;
    }

    public final Map<ListenerHolder.ListenerKey<?>, zacc> zag() {
        return this.zag;
    }

    @WorkerThread
    public final void zah() {
        Preconditions.checkHandlerThread(this.zaa.zat);
        this.zal = null;
    }

    @Nullable
    @WorkerThread
    public final ConnectionResult zai() {
        Preconditions.checkHandlerThread(this.zaa.zat);
        return this.zal;
    }

    @WorkerThread
    public final void zaj() {
        Preconditions.checkHandlerThread(this.zaa.zat);
        if (this.zaj) {
            zam();
        }
    }

    @WorkerThread
    public final void zak() {
        Preconditions.checkHandlerThread(this.zaa.zat);
        if (this.zaj) {
            zaJ();
            zaI(this.zaa.zal.isGooglePlayServicesAvailable(this.zaa.zak) == 18 ? new Status(21, "Connection timed out waiting for Google Play services update to complete.") : new Status(22, "API failed to connect while resuming due to an unknown error."));
            this.zac.disconnect("Timing out connection while resuming.");
        }
    }

    @WorkerThread
    public final boolean zal() {
        return zaL(true);
    }

    @WorkerThread
    public final void zam() {
        Preconditions.checkHandlerThread(this.zaa.zat);
        if (this.zac.isConnected() || this.zac.isConnecting()) {
            return;
        }
        try {
            int zaa = this.zaa.zam.zaa(this.zaa.zak, this.zac);
            if (zaa == 0) {
                zabo zaboVar = new zabo(this.zaa, this.zac, this.zad);
                if (this.zac.requiresSignIn()) {
                    ((zaco) Preconditions.checkNotNull(this.zai)).zac(zaboVar);
                }
                try {
                    this.zac.connect(zaboVar);
                    return;
                } catch (SecurityException e) {
                    zac(new ConnectionResult(10), e);
                    return;
                }
            }
            ConnectionResult connectionResult = new ConnectionResult(zaa, null);
            String name = this.zac.getClass().getName();
            String valueOf = String.valueOf(connectionResult);
            StringBuilder sb = new StringBuilder(String.valueOf(name).length() + 35 + String.valueOf(valueOf).length());
            sb.append("The service for ");
            sb.append(name);
            sb.append(" is not available: ");
            sb.append(valueOf);
            Log.w("GoogleApiManager", sb.toString());
            zac(connectionResult, null);
        } catch (IllegalStateException e2) {
            zac(new ConnectionResult(10), e2);
        }
    }

    @WorkerThread
    public final void zan(zal zalVar) {
        Preconditions.checkHandlerThread(this.zaa.zat);
        this.zaf.add(zalVar);
    }

    final boolean zao() {
        return this.zac.isConnected();
    }

    public final boolean zap() {
        return this.zac.requiresSignIn();
    }

    public final int zaq() {
        return this.zah;
    }

    @WorkerThread
    final int zar() {
        return this.zam;
    }

    @WorkerThread
    final void zas() {
        this.zam++;
    }
}
