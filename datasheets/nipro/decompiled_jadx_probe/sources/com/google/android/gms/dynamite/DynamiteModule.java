package com.google.android.gms.dynamite;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RecentlyNonNull;
import androidx.annotation.RecentlyNullable;
import com.google.android.gms.common.annotation.KeepForSdk;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.util.CrashUtils;
import com.google.android.gms.common.util.DynamiteApi;
import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.dynamic.ObjectWrapper;
import com.microsoft.appcenter.Constants;
import dalvik.system.DelegateLastClassLoader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.concurrent.GuardedBy;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
@KeepForSdk
/* loaded from: classes.dex */
public final class DynamiteModule {

    @Nullable
    @GuardedBy("DynamiteModule.class")
    private static Boolean zzb = null;

    @Nullable
    @GuardedBy("DynamiteModule.class")
    private static String zzc = null;

    @GuardedBy("DynamiteModule.class")
    private static int zzd = -1;

    @Nullable
    @GuardedBy("DynamiteModule.class")
    private static zzo zzi;

    @Nullable
    @GuardedBy("DynamiteModule.class")
    private static zzp zzj;
    private final Context zzh;
    private static final ThreadLocal<zzk> zze = new ThreadLocal<>();
    private static final ThreadLocal<Long> zzf = new zzb();
    private static final zzm zzg = new zzc();

    @RecentlyNonNull
    @KeepForSdk
    public static final VersionPolicy PREFER_REMOTE = new zzd();

    @RecentlyNonNull
    @KeepForSdk
    public static final VersionPolicy PREFER_LOCAL = new zze();

    @RecentlyNonNull
    @KeepForSdk
    public static final VersionPolicy PREFER_REMOTE_VERSION_NO_FORCE_STAGING = new zzf();

    @RecentlyNonNull
    @KeepForSdk
    public static final VersionPolicy PREFER_HIGHEST_OR_LOCAL_VERSION = new zzg();

    @RecentlyNonNull
    @KeepForSdk
    public static final VersionPolicy PREFER_HIGHEST_OR_LOCAL_VERSION_NO_FORCE_STAGING = new zzh();

    @RecentlyNonNull
    @KeepForSdk
    public static final VersionPolicy PREFER_HIGHEST_OR_REMOTE_VERSION = new zzi();

    @RecentlyNonNull
    public static final VersionPolicy zza = new zzj();

    /* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
    @DynamiteApi
    public static class DynamiteLoaderClassLoader {

        @RecentlyNullable
        @GuardedBy("DynamiteLoaderClassLoader.class")
        public static ClassLoader sClassLoader;
    }

    /* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
    @KeepForSdk
    public static class LoadingException extends Exception {
        /* synthetic */ LoadingException(String str, zzb zzbVar) {
            super(str);
        }

        /* synthetic */ LoadingException(String str, Throwable th, zzb zzbVar) {
            super(str, th);
        }
    }

    /* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
    public interface VersionPolicy {
        zzn zza(Context context, String str, zzm zzmVar) throws LoadingException;
    }

    private DynamiteModule(Context context) {
        Preconditions.checkNotNull(context);
        this.zzh = context;
    }

    @KeepForSdk
    public static int getLocalVersion(@RecentlyNonNull Context context, @RecentlyNonNull String str) {
        try {
            ClassLoader classLoader = context.getApplicationContext().getClassLoader();
            StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 61);
            sb.append("com.google.android.gms.dynamite.descriptors.");
            sb.append(str);
            sb.append(".");
            sb.append("ModuleDescriptor");
            Class<?> loadClass = classLoader.loadClass(sb.toString());
            Field declaredField = loadClass.getDeclaredField("MODULE_ID");
            Field declaredField2 = loadClass.getDeclaredField("MODULE_VERSION");
            if (Objects.equal(declaredField.get(null), str)) {
                return declaredField2.getInt(null);
            }
            String valueOf = String.valueOf(declaredField.get(null));
            StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf).length() + 51 + String.valueOf(str).length());
            sb2.append("Module descriptor id '");
            sb2.append(valueOf);
            sb2.append("' didn't match expected id '");
            sb2.append(str);
            sb2.append("'");
            Log.e("DynamiteModule", sb2.toString());
            return 0;
        } catch (ClassNotFoundException unused) {
            StringBuilder sb3 = new StringBuilder(String.valueOf(str).length() + 45);
            sb3.append("Local module descriptor class for ");
            sb3.append(str);
            sb3.append(" not found.");
            Log.w("DynamiteModule", sb3.toString());
            return 0;
        } catch (Exception e) {
            String valueOf2 = String.valueOf(e.getMessage());
            Log.e("DynamiteModule", valueOf2.length() != 0 ? "Failed to load module descriptor class: ".concat(valueOf2) : new String("Failed to load module descriptor class: "));
            return 0;
        }
    }

    @KeepForSdk
    public static int getRemoteVersion(@RecentlyNonNull Context context, @RecentlyNonNull String str) {
        return zza(context, str, false);
    }

    /* JADX WARN: Code restructure failed: missing block: B:134:0x02b1, code lost:
    
        if (r1 != null) goto L21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:20:0x00b0, code lost:
    
        if (r1 != null) goto L21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x00b2, code lost:
    
        r1.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x00b5, code lost:
    
        com.google.android.gms.dynamite.DynamiteModule.zze.set(r5);
     */
    /* JADX WARN: Code restructure failed: missing block: B:23:0x00ba, code lost:
    
        return r0;
     */
    @RecentlyNonNull
    @KeepForSdk
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static DynamiteModule load(@RecentlyNonNull Context context, @RecentlyNonNull VersionPolicy versionPolicy, @RecentlyNonNull String str) throws LoadingException {
        long j;
        DynamiteModule zzd2;
        Cursor cursor;
        Boolean bool;
        IObjectWrapper zze2;
        DynamiteModule dynamiteModule;
        zzp zzpVar;
        Boolean valueOf;
        IObjectWrapper zze3;
        zzk zzkVar = zze.get();
        zzk zzkVar2 = new zzk(null);
        zze.set(zzkVar2);
        long longValue = zzf.get().longValue();
        try {
            zzf.set(Long.valueOf(SystemClock.elapsedRealtime()));
            zzn zza2 = versionPolicy.zza(context, str, zzg);
            int i = zza2.zza;
            int i2 = zza2.zzb;
            StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 68 + String.valueOf(str).length());
            sb.append("Considering local module ");
            sb.append(str);
            sb.append(Constants.COMMON_SCHEMA_PREFIX_SEPARATOR);
            sb.append(i);
            sb.append(" and remote module ");
            sb.append(str);
            sb.append(Constants.COMMON_SCHEMA_PREFIX_SEPARATOR);
            sb.append(i2);
            Log.i("DynamiteModule", sb.toString());
            int i3 = zza2.zzc;
            try {
                if (i3 == 0 || ((i3 == -1 && zza2.zza == 0) || (i3 == 1 && zza2.zzb == 0))) {
                    int i4 = zza2.zza;
                    int i5 = zza2.zzb;
                    StringBuilder sb2 = new StringBuilder(91);
                    sb2.append("No acceptable module found. Local version is ");
                    sb2.append(i4);
                    sb2.append(" and remote version is ");
                    sb2.append(i5);
                    sb2.append(".");
                    throw new LoadingException(sb2.toString(), null);
                }
                if (i3 == -1) {
                    zzd2 = zzd(context, str);
                    if (longValue == 0) {
                        zzf.remove();
                    } else {
                        zzf.set(Long.valueOf(longValue));
                    }
                    cursor = zzkVar2.zza;
                } else {
                    if (i3 != 1) {
                        StringBuilder sb3 = new StringBuilder(47);
                        sb3.append("VersionPolicy returned invalid code:");
                        sb3.append(0);
                        throw new LoadingException(sb3.toString(), null);
                    }
                    try {
                        int i6 = zza2.zzb;
                        try {
                            synchronized (DynamiteModule.class) {
                                bool = zzb;
                            }
                            if (bool == null) {
                                throw new LoadingException("Failed to determine which loading route to use.", null);
                            }
                            if (bool.booleanValue()) {
                                StringBuilder sb4 = new StringBuilder(String.valueOf(str).length() + 51);
                                sb4.append("Selected remote version of ");
                                sb4.append(str);
                                sb4.append(", version >= ");
                                sb4.append(i6);
                                Log.i("DynamiteModule", sb4.toString());
                                synchronized (DynamiteModule.class) {
                                    zzpVar = zzj;
                                }
                                if (zzpVar == null) {
                                    throw new LoadingException("DynamiteLoaderV2 was not cached.", null);
                                }
                                zzk zzkVar3 = zze.get();
                                if (zzkVar3 == null || zzkVar3.zza == null) {
                                    throw new LoadingException("No result cursor", null);
                                }
                                Context applicationContext = context.getApplicationContext();
                                Cursor cursor2 = zzkVar3.zza;
                                ObjectWrapper.wrap(null);
                                synchronized (DynamiteModule.class) {
                                    valueOf = Boolean.valueOf(zzd >= 2);
                                }
                                if (valueOf.booleanValue()) {
                                    Log.v("DynamiteModule", "Dynamite loader version >= 2, using loadModule2NoCrashUtils");
                                    zze3 = zzpVar.zzf(ObjectWrapper.wrap(applicationContext), str, i6, ObjectWrapper.wrap(cursor2));
                                } else {
                                    Log.w("DynamiteModule", "Dynamite loader version < 2, falling back to loadModule2");
                                    zze3 = zzpVar.zze(ObjectWrapper.wrap(applicationContext), str, i6, ObjectWrapper.wrap(cursor2));
                                }
                                Context context2 = (Context) ObjectWrapper.unwrap(zze3);
                                if (context2 == null) {
                                    throw new LoadingException("Failed to get module context", null);
                                }
                                dynamiteModule = new DynamiteModule(context2);
                            } else {
                                StringBuilder sb5 = new StringBuilder(String.valueOf(str).length() + 51);
                                sb5.append("Selected remote version of ");
                                sb5.append(str);
                                sb5.append(", version >= ");
                                sb5.append(i6);
                                Log.i("DynamiteModule", sb5.toString());
                                zzo zzf2 = zzf(context);
                                if (zzf2 == null) {
                                    throw new LoadingException("Failed to create IDynamiteLoader.", null);
                                }
                                int zzi2 = zzf2.zzi();
                                if (zzi2 >= 3) {
                                    zzk zzkVar4 = zze.get();
                                    if (zzkVar4 == null) {
                                        throw new LoadingException("No cached result cursor holder", null);
                                    }
                                    zze2 = zzf2.zzk(ObjectWrapper.wrap(context), str, i6, ObjectWrapper.wrap(zzkVar4.zza));
                                } else if (zzi2 == 2) {
                                    Log.w("DynamiteModule", "IDynamite loader version = 2");
                                    zze2 = zzf2.zzg(ObjectWrapper.wrap(context), str, i6);
                                } else {
                                    Log.w("DynamiteModule", "Dynamite loader version < 2, falling back to createModuleContext");
                                    zze2 = zzf2.zze(ObjectWrapper.wrap(context), str, i6);
                                }
                                if (ObjectWrapper.unwrap(zze2) == null) {
                                    throw new LoadingException("Failed to load remote module.", null);
                                }
                                dynamiteModule = new DynamiteModule((Context) ObjectWrapper.unwrap(zze2));
                            }
                            if (longValue == 0) {
                                zzf.remove();
                            } else {
                                zzf.set(Long.valueOf(longValue));
                            }
                            Cursor cursor3 = zzkVar2.zza;
                            if (cursor3 != null) {
                                cursor3.close();
                            }
                            zze.set(zzkVar);
                            return dynamiteModule;
                        } catch (RemoteException e) {
                            throw new LoadingException("Failed to load remote module.", e, null);
                        } catch (LoadingException e2) {
                            throw e2;
                        } catch (Throwable th) {
                            CrashUtils.addDynamiteErrorToDropBox(context, th);
                            throw new LoadingException("Failed to load remote module.", th, null);
                        }
                    } catch (LoadingException e3) {
                        String valueOf2 = String.valueOf(e3.getMessage());
                        Log.w("DynamiteModule", valueOf2.length() != 0 ? "Failed to load remote module: ".concat(valueOf2) : new String("Failed to load remote module: "));
                        int i7 = zza2.zza;
                        if (i7 == 0 || versionPolicy.zza(context, str, new zzl(i7, 0)).zzc != -1) {
                            throw new LoadingException("Remote load failed. No local fallback found.", e3, null);
                        }
                        zzd2 = zzd(context, str);
                        if (longValue == 0) {
                            zzf.remove();
                        } else {
                            zzf.set(Long.valueOf(longValue));
                        }
                        cursor = zzkVar2.zza;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                j = 0;
                if (longValue == j) {
                    zzf.remove();
                } else {
                    zzf.set(Long.valueOf(longValue));
                }
                Cursor cursor4 = zzkVar2.zza;
                if (cursor4 != null) {
                    cursor4.close();
                }
                zze.set(zzkVar);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            j = 0;
        }
    }

    public static int zza(@RecentlyNonNull Context context, @RecentlyNonNull String str, boolean z) {
        Field declaredField;
        ClassLoader zzaVar;
        Throwable th;
        RemoteException e;
        try {
            synchronized (DynamiteModule.class) {
                Boolean bool = zzb;
                if (bool == null) {
                    try {
                        declaredField = context.getApplicationContext().getClassLoader().loadClass(DynamiteLoaderClassLoader.class.getName()).getDeclaredField("sClassLoader");
                    } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e2) {
                        String valueOf = String.valueOf(e2);
                        StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 30);
                        sb.append("Failed to load module via V2: ");
                        sb.append(valueOf);
                        Log.w("DynamiteModule", sb.toString());
                        bool = Boolean.FALSE;
                    }
                    synchronized (declaredField.getDeclaringClass()) {
                        ClassLoader classLoader = (ClassLoader) declaredField.get(null);
                        if (classLoader != null) {
                            if (classLoader == ClassLoader.getSystemClassLoader()) {
                                bool = Boolean.FALSE;
                            } else {
                                try {
                                    zze(classLoader);
                                } catch (LoadingException unused) {
                                }
                                bool = Boolean.TRUE;
                            }
                        } else if ("com.google.android.gms".equals(context.getApplicationContext().getPackageName())) {
                            declaredField.set(null, ClassLoader.getSystemClassLoader());
                            bool = Boolean.FALSE;
                        } else {
                            try {
                                int zzb2 = zzb(context, str, z);
                                if (zzc != null && !zzc.isEmpty()) {
                                    if (Build.VERSION.SDK_INT >= 29) {
                                        String str2 = zzc;
                                        Preconditions.checkNotNull(str2);
                                        zzaVar = new DelegateLastClassLoader(str2, ClassLoader.getSystemClassLoader());
                                    } else {
                                        String str3 = zzc;
                                        Preconditions.checkNotNull(str3);
                                        zzaVar = new zza(str3, ClassLoader.getSystemClassLoader());
                                    }
                                    zze(zzaVar);
                                    declaredField.set(null, zzaVar);
                                    zzb = Boolean.TRUE;
                                    return zzb2;
                                }
                                return zzb2;
                            } catch (LoadingException unused2) {
                                declaredField.set(null, ClassLoader.getSystemClassLoader());
                                bool = Boolean.FALSE;
                            }
                        }
                        zzb = bool;
                    }
                }
                boolean booleanValue = bool.booleanValue();
                int i = 0;
                if (booleanValue) {
                    try {
                        return zzb(context, str, z);
                    } catch (LoadingException e3) {
                        String valueOf2 = String.valueOf(e3.getMessage());
                        Log.w("DynamiteModule", valueOf2.length() != 0 ? "Failed to retrieve remote module version: ".concat(valueOf2) : new String("Failed to retrieve remote module version: "));
                        return 0;
                    }
                }
                zzo zzf2 = zzf(context);
                try {
                    if (zzf2 != null) {
                        try {
                            int zzi2 = zzf2.zzi();
                            if (zzi2 >= 3) {
                                Cursor cursor = (Cursor) ObjectWrapper.unwrap(zzf2.zzj(ObjectWrapper.wrap(context), str, z, zzf.get().longValue()));
                                if (cursor != null) {
                                    try {
                                        if (cursor.moveToFirst()) {
                                            int i2 = cursor.getInt(0);
                                            r2 = (i2 <= 0 || !zzc(cursor)) ? cursor : null;
                                            if (r2 != null) {
                                                r2.close();
                                            }
                                            i = i2;
                                        }
                                    } catch (RemoteException e4) {
                                        e = e4;
                                        r2 = cursor;
                                        String valueOf3 = String.valueOf(e.getMessage());
                                        Log.w("DynamiteModule", valueOf3.length() != 0 ? "Failed to retrieve remote module version: ".concat(valueOf3) : new String("Failed to retrieve remote module version: "));
                                        if (r2 != null) {
                                            r2.close();
                                        }
                                        return i;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        r2 = cursor;
                                        if (r2 != null) {
                                            r2.close();
                                        }
                                        throw th;
                                    }
                                }
                                Log.w("DynamiteModule", "Failed to retrieve remote module version.");
                                if (cursor != null) {
                                    cursor.close();
                                }
                            } else if (zzi2 == 2) {
                                Log.w("DynamiteModule", "IDynamite loader version = 2, no high precision latency measurement.");
                                i = zzf2.zzh(ObjectWrapper.wrap(context), str, z);
                            } else {
                                Log.w("DynamiteModule", "IDynamite loader version < 2, falling back to getModuleVersion2");
                                i = zzf2.zzf(ObjectWrapper.wrap(context), str, z);
                            }
                        } catch (RemoteException e5) {
                            e = e5;
                        }
                    }
                    return i;
                } catch (Throwable th3) {
                    th = th3;
                }
            }
        } catch (Throwable th4) {
            CrashUtils.addDynamiteErrorToDropBox(context, th4);
            throw th4;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:25:0x0074, code lost:
    
        if (zzc(r9) != false) goto L26;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:44:0x00a9  */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.google.android.gms.dynamite.zzb] */
    /* JADX WARN: Type inference failed for: r0v1, types: [android.database.Cursor] */
    /* JADX WARN: Type inference failed for: r0v2 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static int zzb(Context context, String str, boolean z) throws LoadingException {
        Throwable th;
        Exception e;
        ?? r0 = 0;
        Cursor cursor = null;
        try {
            try {
                Cursor query = context.getContentResolver().query(new Uri.Builder().scheme("content").authority("com.google.android.gms.chimera").path(true != z ? "api" : "api_force_staging").appendPath(str).appendQueryParameter("requestStartTime", String.valueOf(zzf.get().longValue())).build(), null, null, null, null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            int i = query.getInt(0);
                            if (i > 0) {
                                synchronized (DynamiteModule.class) {
                                    zzc = query.getString(2);
                                    int columnIndex = query.getColumnIndex("loaderVersion");
                                    if (columnIndex >= 0) {
                                        zzd = query.getInt(columnIndex);
                                    }
                                }
                            }
                            cursor = query;
                            if (cursor != null) {
                                cursor.close();
                            }
                            return i;
                        }
                    } catch (Exception e2) {
                        e = e2;
                        if (e instanceof LoadingException) {
                            throw e;
                        }
                        throw new LoadingException("V2 version check failed", e, r0);
                    }
                }
                Log.w("DynamiteModule", "Failed to retrieve remote module version.");
                throw new LoadingException("Failed to connect to dynamite module ContentResolver.", r0);
            } catch (Throwable th2) {
                th = th2;
                r0 = context;
                if (r0 != 0) {
                    r0.close();
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
        } catch (Throwable th3) {
            th = th3;
            if (r0 != 0) {
            }
            throw th;
        }
    }

    private static boolean zzc(Cursor cursor) {
        zzk zzkVar = zze.get();
        if (zzkVar == null || zzkVar.zza != null) {
            return false;
        }
        zzkVar.zza = cursor;
        return true;
    }

    private static DynamiteModule zzd(Context context, String str) {
        String valueOf = String.valueOf(str);
        Log.i("DynamiteModule", valueOf.length() != 0 ? "Selected local version of ".concat(valueOf) : new String("Selected local version of "));
        return new DynamiteModule(context.getApplicationContext());
    }

    @GuardedBy("DynamiteModule.class")
    private static void zze(ClassLoader classLoader) throws LoadingException {
        zzp zzpVar;
        zzb zzbVar = null;
        try {
            IBinder iBinder = (IBinder) classLoader.loadClass("com.google.android.gms.dynamiteloader.DynamiteLoaderV2").getConstructor(new Class[0]).newInstance(new Object[0]);
            if (iBinder == null) {
                zzpVar = null;
            } else {
                IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.dynamite.IDynamiteLoaderV2");
                zzpVar = queryLocalInterface instanceof zzp ? (zzp) queryLocalInterface : new zzp(iBinder);
            }
            zzj = zzpVar;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new LoadingException("Failed to instantiate dynamite loader", e, zzbVar);
        }
    }

    @Nullable
    private static zzo zzf(Context context) {
        zzo zzoVar;
        synchronized (DynamiteModule.class) {
            if (zzi != null) {
                return zzi;
            }
            try {
                IBinder iBinder = (IBinder) context.createPackageContext("com.google.android.gms", 3).getClassLoader().loadClass("com.google.android.gms.chimera.container.DynamiteLoaderImpl").newInstance();
                if (iBinder == null) {
                    zzoVar = null;
                } else {
                    IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.dynamite.IDynamiteLoader");
                    zzoVar = queryLocalInterface instanceof zzo ? (zzo) queryLocalInterface : new zzo(iBinder);
                }
                if (zzoVar != null) {
                    zzi = zzoVar;
                    return zzoVar;
                }
            } catch (Exception e) {
                String valueOf = String.valueOf(e.getMessage());
                Log.e("DynamiteModule", valueOf.length() != 0 ? "Failed to load IDynamiteLoader from GmsCore: ".concat(valueOf) : new String("Failed to load IDynamiteLoader from GmsCore: "));
            }
            return null;
        }
    }

    @RecentlyNonNull
    @KeepForSdk
    public Context getModuleContext() {
        return this.zzh;
    }

    @RecentlyNonNull
    @KeepForSdk
    public IBinder instantiate(@RecentlyNonNull String str) throws LoadingException {
        try {
            return (IBinder) this.zzh.getClassLoader().loadClass(str).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            String valueOf = String.valueOf(str);
            throw new LoadingException(valueOf.length() != 0 ? "Failed to instantiate module class: ".concat(valueOf) : new String("Failed to instantiate module class: "), e, null);
        }
    }
}
