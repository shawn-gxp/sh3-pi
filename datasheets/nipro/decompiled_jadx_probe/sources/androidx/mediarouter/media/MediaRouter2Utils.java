package androidx.mediarouter.media;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.media.MediaRoute2Info;
import android.media.RouteDiscoveryPreference;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.mediarouter.media.MediaRouteDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiresApi(api = 30)
@SuppressLint({"NewApi"})
/* loaded from: classes.dex */
class MediaRouter2Utils {
    static final String FEATURE_EMPTY = "android.media.route.feature.EMPTY";
    static final String FEATURE_REMOTE_GROUP_PLAYBACK = "android.media.route.feature.REMOTE_GROUP_PLAYBACK";
    static final String KEY_CONTROL_FILTERS = "androidx.mediarouter.media.KEY_CONTROL_FILTERS";
    static final String KEY_DEVICE_TYPE = "androidx.mediarouter.media.KEY_DEVICE_TYPE";
    static final String KEY_EXTRAS = "androidx.mediarouter.media.KEY_EXTRAS";
    static final String KEY_GROUP_ROUTE = "androidx.mediarouter.media.KEY_GROUP_ROUTE";
    static final String KEY_MESSENGER = "androidx.mediarouter.media.KEY_MESSENGER";
    static final String KEY_ORIGINAL_ROUTE_ID = "androidx.mediarouter.media.KEY_ORIGINAL_ROUTE_ID";
    static final String KEY_PLAYBACK_TYPE = "androidx.mediarouter.media.KEY_PLAYBACK_TYPE";
    static final String KEY_SESSION_NAME = "androidx.mediarouter.media.KEY_SESSION_NAME";

    private MediaRouter2Utils() {
    }

    /* JADX WARN: Code restructure failed: missing block: B:7:0x0055, code lost:
    
        if (r1 != 2) goto L12;
     */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static MediaRoute2Info toFwkMediaRoute2Info(@Nullable MediaRouteDescriptor mediaRouteDescriptor) {
        if (mediaRouteDescriptor == null) {
            return null;
        }
        MediaRoute2Info.Builder iconUri = new MediaRoute2Info.Builder(mediaRouteDescriptor.getId(), mediaRouteDescriptor.getName()).setDescription(mediaRouteDescriptor.getDescription()).setConnectionState(mediaRouteDescriptor.getConnectionState()).setVolumeHandling(mediaRouteDescriptor.getVolumeHandling()).setVolume(mediaRouteDescriptor.getVolume()).setVolumeMax(mediaRouteDescriptor.getVolumeMax()).addFeatures(toFeatures(mediaRouteDescriptor.getControlFilters())).setIconUri(mediaRouteDescriptor.getIconUri());
        int deviceType = mediaRouteDescriptor.getDeviceType();
        if (deviceType == 1) {
            iconUri.addFeature("android.media.route.feature.REMOTE_VIDEO_PLAYBACK");
        }
        iconUri.addFeature("android.media.route.feature.REMOTE_AUDIO_PLAYBACK");
        if (!mediaRouteDescriptor.getGroupMemberIds().isEmpty()) {
            iconUri.addFeature(FEATURE_REMOTE_GROUP_PLAYBACK);
        }
        Bundle bundle = new Bundle();
        bundle.putBundle(KEY_EXTRAS, mediaRouteDescriptor.getExtras());
        bundle.putParcelableArrayList(KEY_CONTROL_FILTERS, new ArrayList<>(mediaRouteDescriptor.getControlFilters()));
        bundle.putInt(KEY_DEVICE_TYPE, mediaRouteDescriptor.getDeviceType());
        bundle.putInt(KEY_PLAYBACK_TYPE, mediaRouteDescriptor.getPlaybackType());
        bundle.putString(KEY_ORIGINAL_ROUTE_ID, mediaRouteDescriptor.getId());
        iconUri.setExtras(bundle);
        if (mediaRouteDescriptor.getControlFilters().isEmpty()) {
            iconUri.addFeature(FEATURE_EMPTY);
        }
        return iconUri.build();
    }

    @Nullable
    public static MediaRouteDescriptor toMediaRouteDescriptor(@Nullable MediaRoute2Info mediaRoute2Info) {
        if (mediaRoute2Info == null) {
            return null;
        }
        MediaRouteDescriptor.Builder canDisconnect = new MediaRouteDescriptor.Builder(mediaRoute2Info.getId(), mediaRoute2Info.getName().toString()).setConnectionState(mediaRoute2Info.getConnectionState()).setVolumeHandling(mediaRoute2Info.getVolumeHandling()).setVolumeMax(mediaRoute2Info.getVolumeMax()).setVolume(mediaRoute2Info.getVolume()).setExtras(mediaRoute2Info.getExtras()).setEnabled(true).setCanDisconnect(false);
        CharSequence description = mediaRoute2Info.getDescription();
        if (description != null) {
            canDisconnect.setDescription(description.toString());
        }
        Uri iconUri = mediaRoute2Info.getIconUri();
        if (iconUri != null) {
            canDisconnect.setIconUri(iconUri);
        }
        Bundle extras = mediaRoute2Info.getExtras();
        if (extras == null || !extras.containsKey(KEY_EXTRAS) || !extras.containsKey(KEY_DEVICE_TYPE) || !extras.containsKey(KEY_CONTROL_FILTERS)) {
            return null;
        }
        canDisconnect.setExtras(extras.getBundle(KEY_EXTRAS));
        canDisconnect.setDeviceType(extras.getInt(KEY_DEVICE_TYPE, 0));
        canDisconnect.setPlaybackType(extras.getInt(KEY_PLAYBACK_TYPE, 1));
        ArrayList parcelableArrayList = extras.getParcelableArrayList(KEY_CONTROL_FILTERS);
        if (parcelableArrayList != null) {
            canDisconnect.addControlFilters(parcelableArrayList);
        }
        return canDisconnect.build();
    }

    static Collection<String> toFeatures(List<IntentFilter> list) {
        HashSet hashSet = new HashSet();
        for (IntentFilter intentFilter : list) {
            int countCategories = intentFilter.countCategories();
            for (int i = 0; i < countCategories; i++) {
                hashSet.add(toRouteFeature(intentFilter.getCategory(i)));
            }
        }
        return hashSet;
    }

    @NonNull
    static List<IntentFilter> toControlFilters(@Nullable Collection<String> collection) {
        if (collection == null) {
            return new ArrayList();
        }
        return (List) collection.stream().distinct().map(new Function() { // from class: androidx.mediarouter.media.-$$Lambda$MediaRouter2Utils$uCpi47E8rheyDXJ-HinBKf5A5uc
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return MediaRouter2Utils.lambda$toControlFilters$0((String) obj);
            }
        }).collect(Collectors.toList());
    }

    static /* synthetic */ IntentFilter lambda$toControlFilters$0(String str) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(toControlCategory(str));
        return intentFilter;
    }

    @NonNull
    static List<String> getRouteIds(@Nullable List<MediaRoute2Info> list) {
        if (list == null) {
            return new ArrayList();
        }
        return (List) list.stream().filter($$Lambda$8fo3RPrzkq5mg2wxR3lAN3cgNY.INSTANCE).map(new Function() { // from class: androidx.mediarouter.media.-$$Lambda$Jl1VWT2dPpodkj8vkFOye7iVD0Y
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return ((MediaRoute2Info) obj).getId();
            }
        }).collect(Collectors.toList());
    }

    @NonNull
    static RouteDiscoveryPreference toDiscoveryPreference(@Nullable MediaRouteDiscoveryRequest mediaRouteDiscoveryRequest) {
        if (mediaRouteDiscoveryRequest == null || !mediaRouteDiscoveryRequest.isValid()) {
            return new RouteDiscoveryPreference.Builder(new ArrayList(), false).build();
        }
        return new RouteDiscoveryPreference.Builder((List) mediaRouteDiscoveryRequest.getSelector().getControlCategories().stream().map(new Function() { // from class: androidx.mediarouter.media.-$$Lambda$zMyvfVxKhaSv8GFN-7x4sfyRIzM
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return MediaRouter2Utils.toRouteFeature((String) obj);
            }
        }).collect(Collectors.toList()), mediaRouteDiscoveryRequest.isActiveScan()).build();
    }

    static String toRouteFeature(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode == -2065577523) {
            if (str.equals(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)) {
                c = 2;
            }
            c = 65535;
        } else if (hashCode != 956939050) {
            if (hashCode == 975975375 && str.equals(MediaControlIntent.CATEGORY_LIVE_VIDEO)) {
                c = 1;
            }
            c = 65535;
        } else {
            if (str.equals(MediaControlIntent.CATEGORY_LIVE_AUDIO)) {
                c = 0;
            }
            c = 65535;
        }
        return c != 0 ? c != 1 ? c != 2 ? str : "android.media.route.feature.REMOTE_PLAYBACK" : "android.media.route.feature.LIVE_VIDEO" : "android.media.route.feature.LIVE_AUDIO";
    }

    static String toControlCategory(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode == 94496206) {
            if (str.equals("android.media.route.feature.REMOTE_PLAYBACK")) {
                c = 2;
            }
            c = 65535;
        } else if (hashCode != 1328964233) {
            if (hashCode == 1348000558 && str.equals("android.media.route.feature.LIVE_VIDEO")) {
                c = 1;
            }
            c = 65535;
        } else {
            if (str.equals("android.media.route.feature.LIVE_AUDIO")) {
                c = 0;
            }
            c = 65535;
        }
        return c != 0 ? c != 1 ? c != 2 ? str : MediaControlIntent.CATEGORY_REMOTE_PLAYBACK : MediaControlIntent.CATEGORY_LIVE_VIDEO : MediaControlIntent.CATEGORY_LIVE_AUDIO;
    }
}
