package crc64e95e69e34d869711;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationResult;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class LocationCallback extends com.google.android.gms.location.LocationCallback implements IGCUserPeer {
    public static final String __md_methods = "n_onLocationResult:(Lcom/google/android/gms/location/LocationResult;)V:GetOnLocationResult_Lcom_google_android_gms_location_LocationResult_Handler\nn_onLocationAvailability:(Lcom/google/android/gms/location/LocationAvailability;)V:GetOnLocationAvailability_Lcom_google_android_gms_location_LocationAvailability_Handler\n";
    private ArrayList refList;

    private native void n_onLocationAvailability(LocationAvailability locationAvailability);

    private native void n_onLocationResult(LocationResult locationResult);

    static {
        Runtime.register("Android.Gms.Location.LocationCallback, Xamarin.GooglePlayServices.Location", LocationCallback.class, __md_methods);
    }

    public LocationCallback() {
        if (LocationCallback.class == LocationCallback.class) {
            TypeManager.Activate("Android.Gms.Location.LocationCallback, Xamarin.GooglePlayServices.Location", "", this, new Object[0]);
        }
    }

    @Override // com.google.android.gms.location.LocationCallback
    public void onLocationResult(LocationResult locationResult) {
        n_onLocationResult(locationResult);
    }

    @Override // com.google.android.gms.location.LocationCallback
    public void onLocationAvailability(LocationAvailability locationAvailability) {
        n_onLocationAvailability(locationAvailability);
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
