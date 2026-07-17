package com.google.android.gms.common.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.os.WorkSource;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RecentlyNonNull;
import androidx.annotation.RecentlyNullable;
import com.google.android.gms.common.annotation.KeepForSdk;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.wrappers.Wrappers;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
@KeepForSdk
/* loaded from: classes.dex */
public class WorkSourceUtil {
    private static final int zza = Process.myUid();

    @Nullable
    private static final Method zzb;

    @Nullable
    private static final Method zzc;

    @Nullable
    private static final Method zzd;

    @Nullable
    private static final Method zze;

    @Nullable
    private static final Method zzf;

    @Nullable
    private static final Method zzg;

    @Nullable
    private static final Method zzh;

    /* JADX WARN: Can't wrap try/catch for region: R(23:0|1|2|3|4|(18:42|43|7|8|9|10|11|12|13|(9:34|35|16|(6:29|30|19|(2:24|25)|21|22)|18|19|(0)|21|22)|15|16|(0)|18|19|(0)|21|22)|6|7|8|9|10|11|12|13|(0)|15|16|(0)|18|19|(0)|21|22) */
    /* JADX WARN: Code restructure failed: missing block: B:39:0x0053, code lost:
    
        r1 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:41:0x0041, code lost:
    
        r1 = null;
     */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0090 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:29:0x0076 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:34:0x005c A[EXC_TOP_SPLITTER, SYNTHETIC] */
    static {
        Method method;
        Method method2;
        Method method3;
        Method method4;
        Method method5 = null;
        try {
            method = WorkSource.class.getMethod("add", Integer.TYPE);
        } catch (Exception unused) {
            method = null;
        }
        zzb = method;
        if (PlatformVersion.isAtLeastJellyBeanMR2()) {
            try {
                method2 = WorkSource.class.getMethod("add", Integer.TYPE, String.class);
            } catch (Exception unused2) {
            }
            zzc = method2;
            Method method6 = WorkSource.class.getMethod("size", new Class[0]);
            zzd = method6;
            Method method7 = WorkSource.class.getMethod("get", Integer.TYPE);
            zze = method7;
            if (PlatformVersion.isAtLeastJellyBeanMR2()) {
                try {
                    method3 = WorkSource.class.getMethod("getName", Integer.TYPE);
                } catch (Exception unused3) {
                }
                zzf = method3;
                if (PlatformVersion.isAtLeastP()) {
                    try {
                        method4 = WorkSource.class.getMethod("createWorkChain", new Class[0]);
                    } catch (Exception e) {
                        Log.w("WorkSourceUtil", "Missing WorkChain API createWorkChain", e);
                    }
                    zzg = method4;
                    if (PlatformVersion.isAtLeastP()) {
                        try {
                            method5 = Class.forName("android.os.WorkSource$WorkChain").getMethod("addNode", Integer.TYPE, String.class);
                        } catch (Exception e2) {
                            Log.w("WorkSourceUtil", "Missing WorkChain class", e2);
                        }
                    }
                    zzh = method5;
                }
                method4 = null;
                zzg = method4;
                if (PlatformVersion.isAtLeastP()) {
                }
                zzh = method5;
            }
            method3 = null;
            zzf = method3;
            if (PlatformVersion.isAtLeastP()) {
            }
            method4 = null;
            zzg = method4;
            if (PlatformVersion.isAtLeastP()) {
            }
            zzh = method5;
        }
        method2 = null;
        zzc = method2;
        Method method62 = WorkSource.class.getMethod("size", new Class[0]);
        zzd = method62;
        Method method72 = WorkSource.class.getMethod("get", Integer.TYPE);
        zze = method72;
        if (PlatformVersion.isAtLeastJellyBeanMR2()) {
        }
        method3 = null;
        zzf = method3;
        if (PlatformVersion.isAtLeastP()) {
        }
        method4 = null;
        zzg = method4;
        if (PlatformVersion.isAtLeastP()) {
        }
        zzh = method5;
    }

    private WorkSourceUtil() {
    }

    @RecentlyNullable
    @KeepForSdk
    public static WorkSource fromPackage(@RecentlyNonNull Context context, @Nullable String str) {
        if (context != null && context.getPackageManager() != null && str != null) {
            try {
                ApplicationInfo applicationInfo = Wrappers.packageManager(context).getApplicationInfo(str, 0);
                if (applicationInfo == null) {
                    Log.e("WorkSourceUtil", str.length() != 0 ? "Could not get applicationInfo from package: ".concat(str) : new String("Could not get applicationInfo from package: "));
                    return null;
                }
                int i = applicationInfo.uid;
                WorkSource workSource = new WorkSource();
                zza(workSource, i, str);
                return workSource;
            } catch (PackageManager.NameNotFoundException unused) {
                Log.e("WorkSourceUtil", str.length() != 0 ? "Could not find package: ".concat(str) : new String("Could not find package: "));
            }
        }
        return null;
    }

    @RecentlyNullable
    @KeepForSdk
    public static WorkSource fromPackageAndModuleExperimentalPi(@RecentlyNonNull Context context, @RecentlyNonNull String str, @RecentlyNonNull String str2) {
        if (context == null || context.getPackageManager() == null || str2 == null || str == null) {
            Log.w("WorkSourceUtil", "Unexpected null arguments");
            return null;
        }
        int i = -1;
        try {
            ApplicationInfo applicationInfo = Wrappers.packageManager(context).getApplicationInfo(str, 0);
            if (applicationInfo == null) {
                Log.e("WorkSourceUtil", str.length() != 0 ? "Could not get applicationInfo from package: ".concat(str) : new String("Could not get applicationInfo from package: "));
            } else {
                i = applicationInfo.uid;
            }
        } catch (PackageManager.NameNotFoundException unused) {
            Log.e("WorkSourceUtil", str.length() != 0 ? "Could not find package: ".concat(str) : new String("Could not find package: "));
        }
        if (i < 0) {
            return null;
        }
        WorkSource workSource = new WorkSource();
        Method method = zzg;
        if (method == null || zzh == null) {
            zza(workSource, i, str);
        } else {
            try {
                Object invoke = method.invoke(workSource, new Object[0]);
                if (i != zza) {
                    zzh.invoke(invoke, Integer.valueOf(i), str);
                }
                zzh.invoke(invoke, Integer.valueOf(zza), str2);
            } catch (Exception e) {
                Log.w("WorkSourceUtil", "Unable to assign chained blame through WorkSource", e);
            }
        }
        return workSource;
    }

    /* JADX WARN: Removed duplicated region for block: B:5:0x0029  */
    @RecentlyNonNull
    @KeepForSdk
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static List<String> getNames(@Nullable WorkSource workSource) {
        Method method;
        int intValue;
        ArrayList arrayList = new ArrayList();
        if (workSource != null && (method = zzd) != null) {
            try {
                Object invoke = method.invoke(workSource, new Object[0]);
                Preconditions.checkNotNull(invoke);
                intValue = ((Integer) invoke).intValue();
            } catch (Exception e) {
                Log.wtf("WorkSourceUtil", "Unable to assign blame through WorkSource", e);
            }
            if (intValue != 0) {
                for (int i = 0; i < intValue; i++) {
                    Method method2 = zzf;
                    String str = null;
                    if (method2 != null) {
                        try {
                            str = (String) method2.invoke(workSource, Integer.valueOf(i));
                        } catch (Exception e2) {
                            Log.wtf("WorkSourceUtil", "Unable to assign blame through WorkSource", e2);
                        }
                    }
                    if (!Strings.isEmptyOrWhitespace(str)) {
                        Preconditions.checkNotNull(str);
                        arrayList.add(str);
                    }
                }
            }
            return arrayList;
        }
        intValue = 0;
        if (intValue != 0) {
        }
        return arrayList;
    }

    @KeepForSdk
    public static boolean hasWorkSourcePermission(@RecentlyNonNull Context context) {
        return (context == null || context.getPackageManager() == null || Wrappers.packageManager(context).checkPermission("android.permission.UPDATE_DEVICE_STATS", context.getPackageName()) != 0) ? false : true;
    }

    public static void zza(@RecentlyNonNull WorkSource workSource, int i, @Nullable String str) {
        Method method = zzc;
        if (method != null) {
            try {
                method.invoke(workSource, Integer.valueOf(i), str);
                return;
            } catch (Exception e) {
                Log.wtf("WorkSourceUtil", "Unable to assign blame through WorkSource", e);
                return;
            }
        }
        Method method2 = zzb;
        if (method2 != null) {
            try {
                method2.invoke(workSource, Integer.valueOf(i));
            } catch (Exception e2) {
                Log.wtf("WorkSourceUtil", "Unable to assign blame through WorkSource", e2);
            }
        }
    }
}
