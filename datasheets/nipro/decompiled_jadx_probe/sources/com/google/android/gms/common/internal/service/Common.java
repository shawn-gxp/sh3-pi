package com.google.android.gms.common.internal.service;

import androidx.annotation.RecentlyNonNull;
import com.google.android.gms.common.annotation.KeepForSdk;
import com.google.android.gms.common.api.Api;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
/* loaded from: classes.dex */
public final class Common {

    @RecentlyNonNull
    @KeepForSdk
    public static final Api<Api.ApiOptions.NoOptions> API;

    @RecentlyNonNull
    @KeepForSdk
    public static final Api.ClientKey<zah> CLIENT_KEY = new Api.ClientKey<>();
    public static final zae zaa;
    private static final Api.AbstractClientBuilder<zah, Api.ApiOptions.NoOptions> zab;

    static {
        zab zabVar = new zab();
        zab = zabVar;
        API = new Api<>("Common.API", zabVar, CLIENT_KEY);
        zaa = new zae();
    }
}
