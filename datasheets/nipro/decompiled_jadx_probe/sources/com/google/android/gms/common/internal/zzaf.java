package com.google.android.gms.common.internal;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.android.gms.common.wrappers.Wrappers;
import javax.annotation.concurrent.GuardedBy;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
/* loaded from: classes.dex */
public final class zzaf {
    private static final Object zza = new Object();

    @GuardedBy("sLock")
    private static boolean zzb;

    @Nullable
    private static String zzc;
    private static int zzd;

    @Nullable
    public static String zza(Context context) {
        zzc(context);
        return zzc;
    }

    public static int zzb(Context context) {
        zzc(context);
        return zzd;
    }

    private static void zzc(Context context) {
        Bundle bundle;
        synchronized (zza) {
            if (zzb) {
                return;
            }
            zzb = true;
            try {
                bundle = Wrappers.packageManager(context).getApplicationInfo(context.getPackageName(), 128).metaData;
            } catch (PackageManager.NameNotFoundException e) {
                Log.wtf("MetadataValueReader", "This should never happen.", e);
            }
            if (bundle == null) {
                return;
            }
            zzc = bundle.getString("com.google.app.id");
            zzd = bundle.getInt("com.google.android.gms.version");
        }
    }
}
