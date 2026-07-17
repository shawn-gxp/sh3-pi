package com.google.android.gms.location;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import androidx.annotation.RecentlyNonNull;
import androidx.annotation.RequiresPermission;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.internal.RemoteCall;
import com.google.android.gms.common.api.internal.TaskApiCall;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

/* compiled from: com.google.android.gms:play-services-location@@18.0.0 */
/* loaded from: classes.dex */
public class ActivityRecognitionClient extends GoogleApi<Api.ApiOptions.NoOptions> {
    public static final /* synthetic */ int zza = 0;

    public ActivityRecognitionClient(@RecentlyNonNull Activity activity) {
        super(activity, LocationServices.API, Api.ApiOptions.NO_OPTIONS, GoogleApi.Settings.DEFAULT_SETTINGS);
    }

    @RecentlyNonNull
    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    public Task<Void> removeActivityTransitionUpdates(@RecentlyNonNull final PendingIntent pendingIntent) {
        return doWrite(TaskApiCall.builder().run(new RemoteCall(pendingIntent) { // from class: com.google.android.gms.location.zzg
            private final PendingIntent zza;

            {
                this.zza = pendingIntent;
            }

            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) {
                ((com.google.android.gms.internal.location.zzaz) obj).zzs(this.zza, new zzj((TaskCompletionSource) obj2));
            }
        }).setMethodKey(2406).build());
    }

    @RecentlyNonNull
    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    public Task<Void> removeActivityUpdates(@RecentlyNonNull final PendingIntent pendingIntent) {
        return doWrite(TaskApiCall.builder().run(new RemoteCall(pendingIntent) { // from class: com.google.android.gms.location.zze
            private final PendingIntent zza;

            {
                this.zza = pendingIntent;
            }

            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) {
                ((com.google.android.gms.internal.location.zzaz) obj).zzt(this.zza);
                ((TaskCompletionSource) obj2).setResult(null);
            }
        }).setMethodKey(2402).build());
    }

    @RecentlyNonNull
    public Task<Void> removeSleepSegmentUpdates(@RecentlyNonNull final PendingIntent pendingIntent) {
        return doWrite(TaskApiCall.builder().run(new RemoteCall(pendingIntent) { // from class: com.google.android.gms.location.zzh
            private final PendingIntent zza;

            {
                this.zza = pendingIntent;
            }

            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) {
                ((com.google.android.gms.internal.location.zzaz) obj).zzu(this.zza, new zzj((TaskCompletionSource) obj2));
            }
        }).setMethodKey(2411).build());
    }

    @RecentlyNonNull
    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    public Task<Void> requestActivityTransitionUpdates(@RecentlyNonNull final ActivityTransitionRequest activityTransitionRequest, @RecentlyNonNull final PendingIntent pendingIntent) {
        activityTransitionRequest.zza(getContextAttributionTag());
        return doWrite(TaskApiCall.builder().run(new RemoteCall(activityTransitionRequest, pendingIntent) { // from class: com.google.android.gms.location.zzf
            private final ActivityTransitionRequest zza;
            private final PendingIntent zzb;

            {
                this.zza = activityTransitionRequest;
                this.zzb = pendingIntent;
            }

            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) {
                ((com.google.android.gms.internal.location.zzaz) obj).zzr(this.zza, this.zzb, new zzj((TaskCompletionSource) obj2));
            }
        }).setMethodKey(2405).build());
    }

    @RecentlyNonNull
    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    public Task<Void> requestActivityUpdates(final long j, @RecentlyNonNull final PendingIntent pendingIntent) {
        return doWrite(TaskApiCall.builder().run(new RemoteCall(j, pendingIntent) { // from class: com.google.android.gms.location.zzc
            private final long zza;
            private final PendingIntent zzb;

            {
                this.zza = j;
                this.zzb = pendingIntent;
            }

            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) {
                ((com.google.android.gms.internal.location.zzaz) obj).zzq(this.zza, this.zzb);
                ((TaskCompletionSource) obj2).setResult(null);
            }
        }).setMethodKey(2401).build());
    }

    @RecentlyNonNull
    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    public Task<Void> requestSleepSegmentUpdates(@RecentlyNonNull final PendingIntent pendingIntent, @RecentlyNonNull final SleepSegmentRequest sleepSegmentRequest) {
        Preconditions.checkNotNull(pendingIntent, "PendingIntent must be specified.");
        return doRead(TaskApiCall.builder().run(new RemoteCall(this, pendingIntent, sleepSegmentRequest) { // from class: com.google.android.gms.location.zzd
            private final ActivityRecognitionClient zza;
            private final PendingIntent zzb;
            private final SleepSegmentRequest zzc;

            {
                this.zza = this;
                this.zzb = pendingIntent;
                this.zzc = sleepSegmentRequest;
            }

            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) {
                ActivityRecognitionClient activityRecognitionClient = this.zza;
                ((com.google.android.gms.internal.location.zzam) ((com.google.android.gms.internal.location.zzaz) obj).getService()).zzv(this.zzb, this.zzc, new zzi(activityRecognitionClient, (TaskCompletionSource) obj2));
            }
        }).setFeatures(zzu.zzb).setMethodKey(2410).build());
    }

    public ActivityRecognitionClient(@RecentlyNonNull Context context) {
        super(context, LocationServices.API, Api.ApiOptions.NO_OPTIONS, GoogleApi.Settings.DEFAULT_SETTINGS);
    }
}
