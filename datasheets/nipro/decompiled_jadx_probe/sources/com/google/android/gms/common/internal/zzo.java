package com.google.android.gms.common.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import androidx.annotation.Nullable;
import com.google.android.gms.common.stats.ConnectionTracker;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
/* loaded from: classes.dex */
final class zzo implements ServiceConnection, zzr {
    final /* synthetic */ zzq zza;
    private final Map<ServiceConnection, ServiceConnection> zzb = new HashMap();
    private int zzc = 2;
    private boolean zzd;

    @Nullable
    private IBinder zze;
    private final zzm zzf;
    private ComponentName zzg;

    public zzo(zzq zzqVar, zzm zzmVar) {
        this.zza = zzqVar;
        this.zzf = zzmVar;
    }

    @Override // android.content.ServiceConnection
    public final void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        HashMap hashMap;
        Handler handler;
        hashMap = this.zza.zza;
        synchronized (hashMap) {
            handler = this.zza.zzc;
            handler.removeMessages(1, this.zzf);
            this.zze = iBinder;
            this.zzg = componentName;
            Iterator<ServiceConnection> it = this.zzb.values().iterator();
            while (it.hasNext()) {
                it.next().onServiceConnected(componentName, iBinder);
            }
            this.zzc = 1;
        }
    }

    @Override // android.content.ServiceConnection
    public final void onServiceDisconnected(ComponentName componentName) {
        HashMap hashMap;
        Handler handler;
        hashMap = this.zza.zza;
        synchronized (hashMap) {
            handler = this.zza.zzc;
            handler.removeMessages(1, this.zzf);
            this.zze = null;
            this.zzg = componentName;
            Iterator<ServiceConnection> it = this.zzb.values().iterator();
            while (it.hasNext()) {
                it.next().onServiceDisconnected(componentName);
            }
            this.zzc = 2;
        }
    }

    public final void zza(String str) {
        ConnectionTracker connectionTracker;
        Context context;
        Context context2;
        ConnectionTracker connectionTracker2;
        Context context3;
        Handler handler;
        Handler handler2;
        long j;
        this.zzc = 3;
        connectionTracker = this.zza.zzd;
        context = this.zza.zzb;
        zzm zzmVar = this.zzf;
        context2 = this.zza.zzb;
        boolean zza = connectionTracker.zza(context, str, zzmVar.zzd(context2), this, this.zzf.zzc());
        this.zzd = zza;
        if (zza) {
            handler = this.zza.zzc;
            Message obtainMessage = handler.obtainMessage(1, this.zzf);
            handler2 = this.zza.zzc;
            j = this.zza.zzf;
            handler2.sendMessageDelayed(obtainMessage, j);
            return;
        }
        this.zzc = 2;
        try {
            connectionTracker2 = this.zza.zzd;
            context3 = this.zza.zzb;
            connectionTracker2.unbindService(context3, this);
        } catch (IllegalArgumentException unused) {
        }
    }

    public final void zzb(String str) {
        Handler handler;
        ConnectionTracker connectionTracker;
        Context context;
        handler = this.zza.zzc;
        handler.removeMessages(1, this.zzf);
        connectionTracker = this.zza.zzd;
        context = this.zza.zzb;
        connectionTracker.unbindService(context, this);
        this.zzd = false;
        this.zzc = 2;
    }

    public final void zzc(ServiceConnection serviceConnection, ServiceConnection serviceConnection2, String str) {
        this.zzb.put(serviceConnection, serviceConnection2);
    }

    public final void zzd(ServiceConnection serviceConnection, String str) {
        this.zzb.remove(serviceConnection);
    }

    public final boolean zze() {
        return this.zzd;
    }

    public final int zzf() {
        return this.zzc;
    }

    public final boolean zzg(ServiceConnection serviceConnection) {
        return this.zzb.containsKey(serviceConnection);
    }

    public final boolean zzh() {
        return this.zzb.isEmpty();
    }

    @Nullable
    public final IBinder zzi() {
        return this.zze;
    }

    public final ComponentName zzj() {
        return this.zzg;
    }
}
