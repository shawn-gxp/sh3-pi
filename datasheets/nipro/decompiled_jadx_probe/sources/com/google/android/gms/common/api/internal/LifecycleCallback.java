package com.google.android.gms.common.api.internal;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.annotation.RecentlyNonNull;
import com.google.android.gms.common.annotation.KeepForSdk;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
@KeepForSdk
/* loaded from: classes.dex */
public class LifecycleCallback {

    @RecentlyNonNull
    @KeepForSdk
    protected final LifecycleFragment mLifecycleFragment;

    @KeepForSdk
    protected LifecycleCallback(@RecentlyNonNull LifecycleFragment lifecycleFragment) {
        this.mLifecycleFragment = lifecycleFragment;
    }

    @Keep
    private static LifecycleFragment getChimeraLifecycleFragmentImpl(LifecycleActivity lifecycleActivity) {
        throw new IllegalStateException("Method not available in SDK.");
    }

    @RecentlyNonNull
    @KeepForSdk
    public static LifecycleFragment getFragment(@RecentlyNonNull Activity activity) {
        return getFragment(new LifecycleActivity(activity));
    }

    @KeepForSdk
    @MainThread
    public void dump(@RecentlyNonNull String str, @RecentlyNonNull FileDescriptor fileDescriptor, @RecentlyNonNull PrintWriter printWriter, @RecentlyNonNull String[] strArr) {
    }

    @RecentlyNonNull
    @KeepForSdk
    public Activity getActivity() {
        return this.mLifecycleFragment.getLifecycleActivity();
    }

    @KeepForSdk
    @MainThread
    public void onActivityResult(int i, int i2, @RecentlyNonNull Intent intent) {
    }

    @KeepForSdk
    @MainThread
    public void onCreate(@Nullable Bundle bundle) {
    }

    @KeepForSdk
    @MainThread
    public void onDestroy() {
    }

    @KeepForSdk
    @MainThread
    public void onResume() {
    }

    @KeepForSdk
    @MainThread
    public void onSaveInstanceState(@RecentlyNonNull Bundle bundle) {
    }

    @KeepForSdk
    @MainThread
    public void onStart() {
    }

    @KeepForSdk
    @MainThread
    public void onStop() {
    }

    @RecentlyNonNull
    @KeepForSdk
    public static LifecycleFragment getFragment(@RecentlyNonNull ContextWrapper contextWrapper) {
        throw new UnsupportedOperationException();
    }

    @RecentlyNonNull
    @KeepForSdk
    protected static LifecycleFragment getFragment(@RecentlyNonNull LifecycleActivity lifecycleActivity) {
        if (lifecycleActivity.isSupport()) {
            return zzd.zza(lifecycleActivity.asFragmentActivity());
        }
        if (lifecycleActivity.zza()) {
            return zzb.zza(lifecycleActivity.asActivity());
        }
        throw new IllegalArgumentException("Can't get fragment for unexpected activity.");
    }
}
