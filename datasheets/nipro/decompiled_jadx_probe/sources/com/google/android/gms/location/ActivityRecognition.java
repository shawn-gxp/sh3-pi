package com.google.android.gms.location;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.RecentlyNonNull;
import com.google.android.gms.common.api.Api;

/* compiled from: com.google.android.gms:play-services-location@@18.0.0 */
/* loaded from: classes.dex */
public class ActivityRecognition {

    @RecentlyNonNull
    public static final Api<Api.ApiOptions.NoOptions> API;

    @RecentlyNonNull
    @Deprecated
    public static final ActivityRecognitionApi ActivityRecognitionApi;

    @RecentlyNonNull
    public static final String CLIENT_NAME = "activity_recognition";
    private static final Api.ClientKey<com.google.android.gms.internal.location.zzaz> zza = new Api.ClientKey<>();
    private static final Api.AbstractClientBuilder<com.google.android.gms.internal.location.zzaz, Api.ApiOptions.NoOptions> zzb;

    static {
        zza zzaVar = new zza();
        zzb = zzaVar;
        API = new Api<>("ActivityRecognition.API", zzaVar, zza);
        ActivityRecognitionApi = new com.google.android.gms.internal.location.zzg();
    }

    private ActivityRecognition() {
    }

    @RecentlyNonNull
    public static ActivityRecognitionClient getClient(@RecentlyNonNull Activity activity) {
        return new ActivityRecognitionClient(activity);
    }

    @RecentlyNonNull
    public static ActivityRecognitionClient getClient(@RecentlyNonNull Context context) {
        return new ActivityRecognitionClient(context);
    }
}
