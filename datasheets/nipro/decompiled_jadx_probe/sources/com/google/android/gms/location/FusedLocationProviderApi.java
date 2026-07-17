package com.google.android.gms.location;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;
import androidx.annotation.RecentlyNonNull;
import androidx.annotation.RequiresPermission;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;

/* compiled from: com.google.android.gms:play-services-location@@18.0.0 */
@Deprecated
/* loaded from: classes.dex */
public interface FusedLocationProviderApi {

    @RecentlyNonNull
    @Deprecated
    public static final String KEY_LOCATION_CHANGED = "com.google.android.location.LOCATION";

    @RecentlyNonNull
    public static final String KEY_MOCK_LOCATION = "mockLocation";

    @RecentlyNonNull
    PendingResult<Status> flushLocations(@RecentlyNonNull GoogleApiClient googleApiClient);

    @RecentlyNonNull
    @RequiresPermission(anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    Location getLastLocation(@RecentlyNonNull GoogleApiClient googleApiClient);

    @RecentlyNonNull
    @RequiresPermission(anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    LocationAvailability getLocationAvailability(@RecentlyNonNull GoogleApiClient googleApiClient);

    @RecentlyNonNull
    PendingResult<Status> removeLocationUpdates(@RecentlyNonNull GoogleApiClient googleApiClient, @RecentlyNonNull PendingIntent pendingIntent);

    @RecentlyNonNull
    PendingResult<Status> removeLocationUpdates(@RecentlyNonNull GoogleApiClient googleApiClient, @RecentlyNonNull LocationCallback locationCallback);

    @RecentlyNonNull
    PendingResult<Status> removeLocationUpdates(@RecentlyNonNull GoogleApiClient googleApiClient, @RecentlyNonNull LocationListener locationListener);

    @RecentlyNonNull
    @RequiresPermission(anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    PendingResult<Status> requestLocationUpdates(@RecentlyNonNull GoogleApiClient googleApiClient, @RecentlyNonNull LocationRequest locationRequest, @RecentlyNonNull PendingIntent pendingIntent);

    @RecentlyNonNull
    @RequiresPermission(anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    PendingResult<Status> requestLocationUpdates(@RecentlyNonNull GoogleApiClient googleApiClient, @RecentlyNonNull LocationRequest locationRequest, @RecentlyNonNull LocationCallback locationCallback, @RecentlyNonNull Looper looper);

    @RecentlyNonNull
    @RequiresPermission(anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    PendingResult<Status> requestLocationUpdates(@RecentlyNonNull GoogleApiClient googleApiClient, @RecentlyNonNull LocationRequest locationRequest, @RecentlyNonNull LocationListener locationListener);

    @RecentlyNonNull
    @RequiresPermission(anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    PendingResult<Status> requestLocationUpdates(@RecentlyNonNull GoogleApiClient googleApiClient, @RecentlyNonNull LocationRequest locationRequest, @RecentlyNonNull LocationListener locationListener, @RecentlyNonNull Looper looper);

    @RecentlyNonNull
    @RequiresPermission(anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    PendingResult<Status> setMockLocation(@RecentlyNonNull GoogleApiClient googleApiClient, @RecentlyNonNull Location location);

    @RecentlyNonNull
    @RequiresPermission(anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    PendingResult<Status> setMockMode(@RecentlyNonNull GoogleApiClient googleApiClient, boolean z);
}
