package com.google.android.gms.common.api.internal;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RecentlyNonNull;
import androidx.annotation.WorkerThread;
import androidx.collection.ArraySet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.annotation.KeepForSdk;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.HasApiKey;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.internal.BaseImplementation;
import com.google.android.gms.common.api.internal.ListenerHolder;
import com.google.android.gms.common.internal.MethodInvocation;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.internal.RootTelemetryConfigManager;
import com.google.android.gms.common.internal.RootTelemetryConfiguration;
import com.google.android.gms.common.internal.ShowFirstParty;
import com.google.android.gms.common.internal.TelemetryData;
import com.google.android.gms.common.internal.TelemetryLogging;
import com.google.android.gms.common.internal.TelemetryLoggingClient;
import com.google.android.gms.common.util.DeviceProperties;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.GuardedBy;
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
@ShowFirstParty
@KeepForSdk
/* loaded from: classes.dex */
public class GoogleApiManager implements Handler.Callback {

    @RecentlyNonNull
    public static final Status zaa = new Status(4, "Sign-out occurred while this API call was in progress.");
    private static final Status zab = new Status(4, "The user must be signed in to make this API call.");
    private static final Object zag = new Object();

    @Nullable
    @GuardedBy("lock")
    private static GoogleApiManager zaj;

    @Nullable
    private TelemetryData zah;

    @Nullable
    private TelemetryLoggingClient zai;
    private final Context zak;
    private final GoogleApiAvailability zal;
    private final com.google.android.gms.common.internal.zal zam;

    @NotOnlyInitialized
    private final Handler zat;
    private volatile boolean zau;
    private long zac = 5000;
    private long zad = 120000;
    private long zae = 10000;
    private boolean zaf = false;
    private final AtomicInteger zan = new AtomicInteger(1);
    private final AtomicInteger zao = new AtomicInteger(0);
    private final Map<ApiKey<?>, zabl<?>> zap = new ConcurrentHashMap(5, 0.75f, 1);

    @Nullable
    @GuardedBy("lock")
    private zaab zaq = null;

    @GuardedBy("lock")
    private final Set<ApiKey<?>> zar = new ArraySet();
    private final Set<ApiKey<?>> zas = new ArraySet();

    @KeepForSdk
    private GoogleApiManager(Context context, Looper looper, GoogleApiAvailability googleApiAvailability) {
        this.zau = true;
        this.zak = context;
        this.zat = new com.google.android.gms.internal.base.zap(looper, this);
        this.zal = googleApiAvailability;
        this.zam = new com.google.android.gms.common.internal.zal(googleApiAvailability);
        if (DeviceProperties.isAuto(context)) {
            this.zau = false;
        }
        Handler handler = this.zat;
        handler.sendMessage(handler.obtainMessage(6));
    }

    @KeepForSdk
    public static void reportSignOut() {
        synchronized (zag) {
            if (zaj != null) {
                GoogleApiManager googleApiManager = zaj;
                googleApiManager.zao.incrementAndGet();
                Handler handler = googleApiManager.zat;
                handler.sendMessageAtFrontOfQueue(handler.obtainMessage(10));
            }
        }
    }

    static /* synthetic */ boolean zaA(GoogleApiManager googleApiManager, boolean z) {
        googleApiManager.zaf = true;
        return true;
    }

    @WorkerThread
    private final zabl<?> zaH(GoogleApi<?> googleApi) {
        ApiKey<?> apiKey = googleApi.getApiKey();
        zabl<?> zablVar = this.zap.get(apiKey);
        if (zablVar == null) {
            zablVar = new zabl<>(this, googleApi);
            this.zap.put(apiKey, zablVar);
        }
        if (zablVar.zap()) {
            this.zas.add(apiKey);
        }
        zablVar.zam();
        return zablVar;
    }

    private final <T> void zaI(TaskCompletionSource<T> taskCompletionSource, int i, GoogleApi googleApi) {
        zabx zaa2;
        if (i == 0 || (zaa2 = zabx.zaa(this, i, googleApi.getApiKey())) == null) {
            return;
        }
        Task<T> task = taskCompletionSource.getTask();
        Handler handler = this.zat;
        handler.getClass();
        task.addOnCompleteListener(zabf.zaa(handler), zaa2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Status zaJ(ApiKey<?> apiKey, ConnectionResult connectionResult) {
        String zab2 = apiKey.zab();
        String valueOf = String.valueOf(connectionResult);
        StringBuilder sb = new StringBuilder(String.valueOf(zab2).length() + 63 + String.valueOf(valueOf).length());
        sb.append("API: ");
        sb.append(zab2);
        sb.append(" is not available on this device. Connection failed with: ");
        sb.append(valueOf);
        return new Status(connectionResult, sb.toString());
    }

    @WorkerThread
    private final void zaK() {
        TelemetryData telemetryData = this.zah;
        if (telemetryData != null) {
            if (telemetryData.zaa() > 0 || zam()) {
                zaL().log(telemetryData);
            }
            this.zah = null;
        }
    }

    @WorkerThread
    private final TelemetryLoggingClient zaL() {
        if (this.zai == null) {
            this.zai = TelemetryLogging.getClient(this.zak);
        }
        return this.zai;
    }

    @RecentlyNonNull
    public static GoogleApiManager zaa(@RecentlyNonNull Context context) {
        GoogleApiManager googleApiManager;
        synchronized (zag) {
            if (zaj == null) {
                HandlerThread handlerThread = new HandlerThread("GoogleApiHandler", 9);
                handlerThread.start();
                zaj = new GoogleApiManager(context.getApplicationContext(), handlerThread.getLooper(), GoogleApiAvailability.getInstance());
            }
            googleApiManager = zaj;
        }
        return googleApiManager;
    }

    @RecentlyNonNull
    public static GoogleApiManager zab() {
        GoogleApiManager googleApiManager;
        synchronized (zag) {
            Preconditions.checkNotNull(zaj, "Must guarantee manager is non-null before using getInstance");
            googleApiManager = zaj;
        }
        return googleApiManager;
    }

    @Override // android.os.Handler.Callback
    @WorkerThread
    public final boolean handleMessage(@RecentlyNonNull Message message) {
        ApiKey apiKey;
        boolean zaL;
        ApiKey apiKey2;
        ApiKey apiKey3;
        ApiKey apiKey4;
        ApiKey apiKey5;
        int i = message.what;
        zabl<?> zablVar = null;
        switch (i) {
            case 1:
                this.zae = true == ((Boolean) message.obj).booleanValue() ? 10000L : 300000L;
                this.zat.removeMessages(12);
                for (ApiKey<?> apiKey6 : this.zap.keySet()) {
                    Handler handler = this.zat;
                    handler.sendMessageDelayed(handler.obtainMessage(12, apiKey6), this.zae);
                }
                return true;
            case 2:
                zal zalVar = (zal) message.obj;
                Iterator<ApiKey<?>> it = zalVar.zaa().iterator();
                while (true) {
                    if (it.hasNext()) {
                        ApiKey<?> next = it.next();
                        zabl<?> zablVar2 = this.zap.get(next);
                        if (zablVar2 == null) {
                            zalVar.zac(next, new ConnectionResult(13), null);
                        } else if (zablVar2.zao()) {
                            zalVar.zac(next, ConnectionResult.RESULT_SUCCESS, zablVar2.zaf().getEndpointPackageName());
                        } else {
                            ConnectionResult zai = zablVar2.zai();
                            if (zai != null) {
                                zalVar.zac(next, zai, null);
                            } else {
                                zablVar2.zan(zalVar);
                                zablVar2.zam();
                            }
                        }
                    }
                }
                return true;
            case 3:
                for (zabl<?> zablVar3 : this.zap.values()) {
                    zablVar3.zah();
                    zablVar3.zam();
                }
                return true;
            case 4:
            case 8:
            case 13:
                zacb zacbVar = (zacb) message.obj;
                zabl<?> zablVar4 = this.zap.get(zacbVar.zac.getApiKey());
                if (zablVar4 == null) {
                    zablVar4 = zaH(zacbVar.zac);
                }
                if (!zablVar4.zap() || this.zao.get() == zacbVar.zab) {
                    zablVar4.zad(zacbVar.zaa);
                } else {
                    zacbVar.zaa.zac(zaa);
                    zablVar4.zae();
                }
                return true;
            case 5:
                int i2 = message.arg1;
                ConnectionResult connectionResult = (ConnectionResult) message.obj;
                Iterator<zabl<?>> it2 = this.zap.values().iterator();
                while (true) {
                    if (it2.hasNext()) {
                        zabl<?> next2 = it2.next();
                        if (next2.zaq() == i2) {
                            zablVar = next2;
                        }
                    }
                }
                if (zablVar == null) {
                    StringBuilder sb = new StringBuilder(76);
                    sb.append("Could not find API instance ");
                    sb.append(i2);
                    sb.append(" while trying to fail enqueued calls.");
                    Log.wtf("GoogleApiManager", sb.toString(), new Exception());
                } else if (connectionResult.getErrorCode() == 13) {
                    String errorString = this.zal.getErrorString(connectionResult.getErrorCode());
                    String errorMessage = connectionResult.getErrorMessage();
                    StringBuilder sb2 = new StringBuilder(String.valueOf(errorString).length() + 69 + String.valueOf(errorMessage).length());
                    sb2.append("Error resolution was canceled by the user, original error message: ");
                    sb2.append(errorString);
                    sb2.append(": ");
                    sb2.append(errorMessage);
                    zablVar.zaI(new Status(17, sb2.toString()));
                } else {
                    apiKey = ((zabl) zablVar).zad;
                    zablVar.zaI(zaJ(apiKey, connectionResult));
                }
                return true;
            case 6:
                if (this.zak.getApplicationContext() instanceof Application) {
                    BackgroundDetector.initialize((Application) this.zak.getApplicationContext());
                    BackgroundDetector.getInstance().addListener(new zabg(this));
                    if (!BackgroundDetector.getInstance().readCurrentStateIfPossible(true)) {
                        this.zae = 300000L;
                    }
                }
                return true;
            case 7:
                zaH((GoogleApi) message.obj);
                return true;
            case 9:
                if (this.zap.containsKey(message.obj)) {
                    this.zap.get(message.obj).zaj();
                }
                return true;
            case 10:
                Iterator<ApiKey<?>> it3 = this.zas.iterator();
                while (it3.hasNext()) {
                    zabl<?> remove = this.zap.remove(it3.next());
                    if (remove != null) {
                        remove.zae();
                    }
                }
                this.zas.clear();
                return true;
            case 11:
                if (this.zap.containsKey(message.obj)) {
                    this.zap.get(message.obj).zak();
                }
                return true;
            case 12:
                if (this.zap.containsKey(message.obj)) {
                    this.zap.get(message.obj).zal();
                }
                return true;
            case 14:
                zaac zaacVar = (zaac) message.obj;
                ApiKey<?> zaa2 = zaacVar.zaa();
                if (this.zap.containsKey(zaa2)) {
                    zaL = this.zap.get(zaa2).zaL(false);
                    zaacVar.zab().setResult(Boolean.valueOf(zaL));
                } else {
                    zaacVar.zab().setResult(false);
                }
                return true;
            case 15:
                zabm zabmVar = (zabm) message.obj;
                Map<ApiKey<?>, zabl<?>> map = this.zap;
                apiKey2 = zabmVar.zaa;
                if (map.containsKey(apiKey2)) {
                    Map<ApiKey<?>, zabl<?>> map2 = this.zap;
                    apiKey3 = zabmVar.zaa;
                    zabl.zau(map2.get(apiKey3), zabmVar);
                }
                return true;
            case 16:
                zabm zabmVar2 = (zabm) message.obj;
                Map<ApiKey<?>, zabl<?>> map3 = this.zap;
                apiKey4 = zabmVar2.zaa;
                if (map3.containsKey(apiKey4)) {
                    Map<ApiKey<?>, zabl<?>> map4 = this.zap;
                    apiKey5 = zabmVar2.zaa;
                    zabl.zav(map4.get(apiKey5), zabmVar2);
                }
                return true;
            case 17:
                zaK();
                return true;
            case 18:
                zaby zabyVar = (zaby) message.obj;
                if (zabyVar.zac == 0) {
                    zaL().log(new TelemetryData(zabyVar.zab, Arrays.asList(zabyVar.zaa)));
                } else {
                    TelemetryData telemetryData = this.zah;
                    if (telemetryData != null) {
                        List<MethodInvocation> zab2 = telemetryData.zab();
                        if (this.zah.zaa() != zabyVar.zab || (zab2 != null && zab2.size() >= zabyVar.zad)) {
                            this.zat.removeMessages(17);
                            zaK();
                        } else {
                            this.zah.zac(zabyVar.zaa);
                        }
                    }
                    if (this.zah == null) {
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(zabyVar.zaa);
                        this.zah = new TelemetryData(zabyVar.zab, arrayList);
                        Handler handler2 = this.zat;
                        handler2.sendMessageDelayed(handler2.obtainMessage(17), zabyVar.zac);
                    }
                }
                return true;
            case 19:
                this.zaf = false;
                return true;
            default:
                StringBuilder sb3 = new StringBuilder(31);
                sb3.append("Unknown message id: ");
                sb3.append(i);
                Log.w("GoogleApiManager", sb3.toString());
                return false;
        }
    }

    public final int zac() {
        return this.zan.getAndIncrement();
    }

    public final void zad(@RecentlyNonNull GoogleApi<?> googleApi) {
        Handler handler = this.zat;
        handler.sendMessage(handler.obtainMessage(7, googleApi));
    }

    public final void zae(@NonNull zaab zaabVar) {
        synchronized (zag) {
            if (this.zaq != zaabVar) {
                this.zaq = zaabVar;
                this.zar.clear();
            }
            this.zar.addAll(zaabVar.zab());
        }
    }

    final void zaf(@NonNull zaab zaabVar) {
        synchronized (zag) {
            if (this.zaq == zaabVar) {
                this.zaq = null;
                this.zar.clear();
            }
        }
    }

    @Nullable
    final zabl zag(ApiKey<?> apiKey) {
        return this.zap.get(apiKey);
    }

    @RecentlyNonNull
    public final Task<Map<ApiKey<?>, String>> zah(@RecentlyNonNull Iterable<? extends HasApiKey<?>> iterable) {
        zal zalVar = new zal(iterable);
        Handler handler = this.zat;
        handler.sendMessage(handler.obtainMessage(2, zalVar));
        return zalVar.zab();
    }

    public final void zai() {
        Handler handler = this.zat;
        handler.sendMessage(handler.obtainMessage(3));
    }

    @RecentlyNonNull
    public final Task<Boolean> zaj(@RecentlyNonNull GoogleApi<?> googleApi) {
        zaac zaacVar = new zaac(googleApi.getApiKey());
        Handler handler = this.zat;
        handler.sendMessage(handler.obtainMessage(14, zaacVar));
        return zaacVar.zab().getTask();
    }

    public final <O extends Api.ApiOptions> void zak(@RecentlyNonNull GoogleApi<O> googleApi, int i, @RecentlyNonNull BaseImplementation.ApiMethodImpl<? extends Result, Api.AnyClient> apiMethodImpl) {
        zae zaeVar = new zae(i, apiMethodImpl);
        Handler handler = this.zat;
        handler.sendMessage(handler.obtainMessage(4, new zacb(zaeVar, this.zao.get(), googleApi)));
    }

    public final <O extends Api.ApiOptions, ResultT> void zal(@RecentlyNonNull GoogleApi<O> googleApi, int i, @RecentlyNonNull TaskApiCall<Api.AnyClient, ResultT> taskApiCall, @RecentlyNonNull TaskCompletionSource<ResultT> taskCompletionSource, @RecentlyNonNull StatusExceptionMapper statusExceptionMapper) {
        zaI(taskCompletionSource, taskApiCall.zab(), googleApi);
        zag zagVar = new zag(i, taskApiCall, taskCompletionSource, statusExceptionMapper);
        Handler handler = this.zat;
        handler.sendMessage(handler.obtainMessage(4, new zacb(zagVar, this.zao.get(), googleApi)));
    }

    @WorkerThread
    final boolean zam() {
        if (this.zaf) {
            return false;
        }
        RootTelemetryConfiguration config = RootTelemetryConfigManager.getInstance().getConfig();
        if (config != null && !config.getMethodInvocationTelemetryEnabled()) {
            return false;
        }
        int zab2 = this.zam.zab(this.zak, 203390000);
        return zab2 == -1 || zab2 == 0;
    }

    @RecentlyNonNull
    public final <O extends Api.ApiOptions> Task<Void> zan(@RecentlyNonNull GoogleApi<O> googleApi, @RecentlyNonNull RegisterListenerMethod<Api.AnyClient, ?> registerListenerMethod, @RecentlyNonNull UnregisterListenerMethod<Api.AnyClient, ?> unregisterListenerMethod, @RecentlyNonNull Runnable runnable) {
        TaskCompletionSource taskCompletionSource = new TaskCompletionSource();
        zaI(taskCompletionSource, registerListenerMethod.zab(), googleApi);
        zaf zafVar = new zaf(new zacc(registerListenerMethod, unregisterListenerMethod, runnable), taskCompletionSource);
        Handler handler = this.zat;
        handler.sendMessage(handler.obtainMessage(8, new zacb(zafVar, this.zao.get(), googleApi)));
        return taskCompletionSource.getTask();
    }

    @RecentlyNonNull
    public final <O extends Api.ApiOptions> Task<Boolean> zao(@RecentlyNonNull GoogleApi<O> googleApi, @RecentlyNonNull ListenerHolder.ListenerKey listenerKey, int i) {
        TaskCompletionSource taskCompletionSource = new TaskCompletionSource();
        zaI(taskCompletionSource, i, googleApi);
        zah zahVar = new zah(listenerKey, taskCompletionSource);
        Handler handler = this.zat;
        handler.sendMessage(handler.obtainMessage(13, new zacb(zahVar, this.zao.get(), googleApi)));
        return taskCompletionSource.getTask();
    }

    final boolean zap(ConnectionResult connectionResult, int i) {
        return this.zal.zac(this.zak, connectionResult, i);
    }

    public final void zaq(@RecentlyNonNull ConnectionResult connectionResult, int i) {
        if (zap(connectionResult, i)) {
            return;
        }
        Handler handler = this.zat;
        handler.sendMessage(handler.obtainMessage(5, i, 0, connectionResult));
    }

    final void zar(MethodInvocation methodInvocation, int i, long j, int i2) {
        Handler handler = this.zat;
        handler.sendMessage(handler.obtainMessage(18, new zaby(methodInvocation, i, j, i2)));
    }
}
