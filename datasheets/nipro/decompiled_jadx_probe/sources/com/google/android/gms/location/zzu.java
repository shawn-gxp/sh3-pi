package com.google.android.gms.location;

import com.google.android.gms.common.Feature;

/* compiled from: com.google.android.gms:play-services-location@@18.0.0 */
/* loaded from: classes.dex */
public final class zzu {
    public static final Feature zza = new Feature("name_ulr_private", 1);
    public static final Feature zzb = new Feature("name_sleep_segment_request", 1);
    public static final Feature zzc = new Feature("support_context_feature_id", 1);
    public static final Feature zzd = new Feature("get_current_location", 1);
    public static final Feature zze;
    public static final Feature[] zzf;

    static {
        Feature feature = new Feature("get_last_activity_feature_id", 1L);
        zze = feature;
        zzf = new Feature[]{zza, zzb, zzc, zzd, feature};
    }
}
