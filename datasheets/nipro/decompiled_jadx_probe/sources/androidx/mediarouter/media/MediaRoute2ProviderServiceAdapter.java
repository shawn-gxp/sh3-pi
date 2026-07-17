package androidx.mediarouter.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRoute2ProviderService;
import android.media.RouteDiscoveryPreference;
import android.media.RoutingSessionInfo;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.collection.ArrayMap;
import androidx.mediarouter.media.MediaRoute2ProviderServiceAdapter;
import androidx.mediarouter.media.MediaRouteProvider;
import androidx.mediarouter.media.MediaRouteProviderService;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiresApi(api = 30)
/* loaded from: classes.dex */
class MediaRoute2ProviderServiceAdapter extends MediaRoute2ProviderService {

    @SuppressLint({"InlinedApi"})
    public static final String SERVICE_INTERFACE = "android.media.MediaRoute2ProviderService";
    private volatile MediaRouteProviderDescriptor mProviderDescriptor;
    final MediaRouteProviderService.MediaRouteProviderServiceImplApi30 mServiceImpl;
    private static final String TAG = "MR2ProviderService";
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private final Object mLock = new Object();

    @GuardedBy("mLock")
    final Map<String, SessionRecord> mSessionRecords = new ArrayMap();
    final SparseArray<String> mSessionIdMap = new SparseArray<>();

    static /* synthetic */ MediaRouteDescriptor lambda$setProviderDescriptor$1(MediaRouteDescriptor mediaRouteDescriptor) {
        return mediaRouteDescriptor;
    }

    static /* synthetic */ MediaRouteDescriptor lambda$setProviderDescriptor$2(MediaRouteDescriptor mediaRouteDescriptor, MediaRouteDescriptor mediaRouteDescriptor2) {
        return mediaRouteDescriptor;
    }

    MediaRoute2ProviderServiceAdapter(MediaRouteProviderService.MediaRouteProviderServiceImplApi30 mediaRouteProviderServiceImplApi30) {
        this.mServiceImpl = mediaRouteProviderServiceImplApi30;
    }

    @Override // android.app.Service, android.content.ContextWrapper
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
    }

    @Override // android.media.MediaRoute2ProviderService
    public void onSetRouteVolume(long j, @NonNull String str, int i) {
        MediaRouteProvider.RouteController findControllerByRouteId = findControllerByRouteId(str);
        if (findControllerByRouteId == null) {
            Log.w(TAG, "onSetRouteVolume: Couldn't find a controller for routeId=" + str);
            notifyRequestFailed(j, 3);
            return;
        }
        findControllerByRouteId.onSetVolume(i);
    }

    @Override // android.media.MediaRoute2ProviderService
    public void onSetSessionVolume(long j, @NonNull String str, int i) {
        if (getSessionInfo(str) == null) {
            Log.w(TAG, "onSetSessionVolume: Couldn't find a session");
            notifyRequestFailed(j, 4);
            return;
        }
        MediaRouteProvider.DynamicGroupRouteController findControllerBySessionId = findControllerBySessionId(str);
        if (findControllerBySessionId == null) {
            Log.w(TAG, "onSetSessionVolume: Couldn't find a controller");
            notifyRequestFailed(j, 3);
        } else {
            findControllerBySessionId.onSetVolume(i);
        }
    }

    @Override // android.media.MediaRoute2ProviderService
    public void onCreateSession(long j, @NonNull String str, @NonNull String str2, @Nullable Bundle bundle) {
        int i;
        MediaRouteProvider.DynamicGroupRouteController dynamicGroupRouteControllerProxy;
        MediaRouteProvider mediaRouteProvider = getMediaRouteProvider();
        MediaRouteDescriptor routeDescriptor = getRouteDescriptor(str2, "onCreateSession");
        if (routeDescriptor == null) {
            notifyRequestFailed(j, 3);
            return;
        }
        if (this.mProviderDescriptor.supportsDynamicGroupRoute()) {
            dynamicGroupRouteControllerProxy = mediaRouteProvider.onCreateDynamicGroupRouteController(str2);
            i = 7;
            if (dynamicGroupRouteControllerProxy == null) {
                Log.w(TAG, "onCreateSession: Couldn't create a dynamic controller");
                notifyRequestFailed(j, 1);
                return;
            }
        } else {
            MediaRouteProvider.RouteController onCreateRouteController = mediaRouteProvider.onCreateRouteController(str2);
            if (onCreateRouteController == null) {
                Log.w(TAG, "onCreateSession: Couldn't create a controller");
                notifyRequestFailed(j, 1);
                return;
            } else {
                i = routeDescriptor.getGroupMemberIds().isEmpty() ? 1 : 3;
                dynamicGroupRouteControllerProxy = new DynamicGroupRouteControllerProxy(str2, onCreateRouteController);
            }
        }
        dynamicGroupRouteControllerProxy.onSelect();
        SessionRecord sessionRecord = new SessionRecord(this, dynamicGroupRouteControllerProxy, j, i);
        RoutingSessionInfo.Builder volumeMax = new RoutingSessionInfo.Builder(assignSessionId(sessionRecord), str).setName(routeDescriptor.getName()).setVolumeHandling(routeDescriptor.getVolumeHandling()).setVolume(routeDescriptor.getVolume()).setVolumeMax(routeDescriptor.getVolumeMax());
        if (routeDescriptor.getGroupMemberIds().isEmpty()) {
            volumeMax.addSelectedRoute(str2);
        } else {
            Iterator<String> it = routeDescriptor.getGroupMemberIds().iterator();
            while (it.hasNext()) {
                volumeMax.addSelectedRoute(it.next());
            }
        }
        RoutingSessionInfo build = volumeMax.build();
        sessionRecord.setSessionInfo(build);
        if ((i & 6) == 2) {
            sessionRecord.updateMemberRouteControllers(str2, null, build);
        }
        this.mServiceImpl.setDynamicRoutesChangedListener(dynamicGroupRouteControllerProxy);
    }

    @Override // android.media.MediaRoute2ProviderService
    public void onReleaseSession(long j, @NonNull String str) {
        SessionRecord remove;
        if (getSessionInfo(str) == null) {
            return;
        }
        synchronized (this.mLock) {
            remove = this.mSessionRecords.remove(str);
        }
        if (remove == null) {
            Log.w(TAG, "onReleaseSession: Couldn't find a session");
            notifyRequestFailed(j, 4);
        } else {
            remove.release(true);
        }
    }

    @Override // android.media.MediaRoute2ProviderService
    public void onSelectRoute(long j, @NonNull String str, @NonNull String str2) {
        if (getSessionInfo(str) == null) {
            Log.w(TAG, "onSelectRoute: Couldn't find a session");
            notifyRequestFailed(j, 4);
        } else {
            if (getRouteDescriptor(str2, "onSelectRoute") == null) {
                notifyRequestFailed(j, 3);
                return;
            }
            MediaRouteProvider.DynamicGroupRouteController findControllerBySessionId = findControllerBySessionId(str);
            if (findControllerBySessionId == null) {
                Log.w(TAG, "onSelectRoute: Couldn't find a controller");
                notifyRequestFailed(j, 3);
            } else {
                findControllerBySessionId.onAddMemberRoute(str2);
            }
        }
    }

    @Override // android.media.MediaRoute2ProviderService
    public void onDeselectRoute(long j, @NonNull String str, @NonNull String str2) {
        if (getSessionInfo(str) == null) {
            Log.w(TAG, "onDeselectRoute: Couldn't find a session");
            notifyRequestFailed(j, 4);
        } else {
            if (getRouteDescriptor(str2, "onDeselectRoute") == null) {
                notifyRequestFailed(j, 3);
                return;
            }
            MediaRouteProvider.DynamicGroupRouteController findControllerBySessionId = findControllerBySessionId(str);
            if (findControllerBySessionId == null) {
                Log.w(TAG, "onDeselectRoute: Couldn't find a controller");
                notifyRequestFailed(j, 3);
            } else {
                findControllerBySessionId.onRemoveMemberRoute(str2);
            }
        }
    }

    @Override // android.media.MediaRoute2ProviderService
    public void onTransferToRoute(long j, @NonNull String str, @NonNull String str2) {
        if (getSessionInfo(str) == null) {
            Log.w(TAG, "onTransferToRoute: Couldn't find a session");
            notifyRequestFailed(j, 4);
        } else {
            if (getRouteDescriptor(str2, "onTransferToRoute") == null) {
                notifyRequestFailed(j, 3);
                return;
            }
            MediaRouteProvider.DynamicGroupRouteController findControllerBySessionId = findControllerBySessionId(str);
            if (findControllerBySessionId == null) {
                Log.w(TAG, "onTransferToRoute: Couldn't find a controller");
                notifyRequestFailed(j, 3);
            } else {
                findControllerBySessionId.onUpdateMemberRoutes(Collections.singletonList(str2));
            }
        }
    }

    @Override // android.media.MediaRoute2ProviderService
    public void onDiscoveryPreferenceChanged(@NonNull RouteDiscoveryPreference routeDiscoveryPreference) {
        this.mServiceImpl.setBaseDiscoveryRequest(new MediaRouteDiscoveryRequest(new MediaRouteSelector.Builder().addControlCategories((Collection) routeDiscoveryPreference.getPreferredFeatures().stream().map(new Function() { // from class: androidx.mediarouter.media.-$$Lambda$nHCoRGqeXOfAyZcF9QiL0A7Yx-o
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return MediaRouter2Utils.toControlCategory((String) obj);
            }
        }).collect(Collectors.toList())).build(), routeDiscoveryPreference.shouldPerformActiveScan()));
    }

    public void setProviderDescriptor(@Nullable MediaRouteProviderDescriptor mediaRouteProviderDescriptor) {
        this.mProviderDescriptor = mediaRouteProviderDescriptor;
        Map<String, MediaRouteDescriptor> map = (Map) (mediaRouteProviderDescriptor == null ? Collections.emptyList() : mediaRouteProviderDescriptor.getRoutes()).stream().filter($$Lambda$jMO9OfSzscMxGho8zZuPtPiQlPo.INSTANCE).collect(Collectors.toMap(new Function() { // from class: androidx.mediarouter.media.-$$Lambda$MediaRoute2ProviderServiceAdapter$0jTJWtr4QLIXwQjbqGP-HBmnt70
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                String id;
                id = ((MediaRouteDescriptor) obj).getId();
                return id;
            }
        }, new Function() { // from class: androidx.mediarouter.media.-$$Lambda$MediaRoute2ProviderServiceAdapter$RPbJczRkx7FR3NH05asdoDvuemc
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                MediaRouteDescriptor mediaRouteDescriptor = (MediaRouteDescriptor) obj;
                MediaRoute2ProviderServiceAdapter.lambda$setProviderDescriptor$1(mediaRouteDescriptor);
                return mediaRouteDescriptor;
            }
        }, new BinaryOperator() { // from class: androidx.mediarouter.media.-$$Lambda$MediaRoute2ProviderServiceAdapter$WHJZa6ndvPp0_JhA0RpUui-jARk
            @Override // java.util.function.BiFunction
            public final Object apply(Object obj, Object obj2) {
                MediaRouteDescriptor mediaRouteDescriptor = (MediaRouteDescriptor) obj;
                MediaRoute2ProviderServiceAdapter.lambda$setProviderDescriptor$2(mediaRouteDescriptor, (MediaRouteDescriptor) obj2);
                return mediaRouteDescriptor;
            }
        }));
        updateStaticSessions(map);
        notifyRoutes((Collection) map.values().stream().map(new Function() { // from class: androidx.mediarouter.media.-$$Lambda$Nj9OYR6TduhgFYKcn-h4bkjilY0
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return MediaRouter2Utils.toFwkMediaRoute2Info((MediaRouteDescriptor) obj);
            }
        }).filter($$Lambda$8fo3RPrzkq5mg2wxR3lAN3cgNY.INSTANCE).collect(Collectors.toList()));
    }

    private MediaRouteProvider.DynamicGroupRouteController findControllerBySessionId(String str) {
        MediaRouteProvider.DynamicGroupRouteController groupController;
        synchronized (this.mLock) {
            SessionRecord sessionRecord = this.mSessionRecords.get(str);
            groupController = sessionRecord == null ? null : sessionRecord.getGroupController();
        }
        return groupController;
    }

    private MediaRouteDescriptor getRouteDescriptor(String str, String str2) {
        if (getMediaRouteProvider() == null || this.mProviderDescriptor == null) {
            Log.w(TAG, str2 + ": no provider info");
            return null;
        }
        for (MediaRouteDescriptor mediaRouteDescriptor : this.mProviderDescriptor.getRoutes()) {
            if (TextUtils.equals(mediaRouteDescriptor.getId(), str)) {
                return mediaRouteDescriptor;
            }
        }
        Log.w(TAG, str2 + ": Couldn't find a route : " + str);
        return null;
    }

    private SessionRecord findSessionRecordByController(MediaRouteProvider.DynamicGroupRouteController dynamicGroupRouteController) {
        synchronized (this.mLock) {
            Iterator<Map.Entry<String, SessionRecord>> it = this.mSessionRecords.entrySet().iterator();
            while (it.hasNext()) {
                SessionRecord value = it.next().getValue();
                if (value.getGroupController() == dynamicGroupRouteController) {
                    return value;
                }
            }
            return null;
        }
    }

    public void setDynamicRouteDescriptor(MediaRouteProvider.DynamicGroupRouteController dynamicGroupRouteController, MediaRouteDescriptor mediaRouteDescriptor, Collection<MediaRouteProvider.DynamicGroupRouteController.DynamicRouteDescriptor> collection) {
        SessionRecord findSessionRecordByController = findSessionRecordByController(dynamicGroupRouteController);
        if (findSessionRecordByController == null) {
            Log.w(TAG, "setDynamicRouteDescriptor: Ignoring unknown controller");
        } else {
            findSessionRecordByController.updateSessionInfo(mediaRouteDescriptor, collection);
        }
    }

    void updateStaticSessions(Map<String, MediaRouteDescriptor> map) {
        List<SessionRecord> list;
        synchronized (this.mLock) {
            list = (List) this.mSessionRecords.values().stream().filter(new Predicate() { // from class: androidx.mediarouter.media.-$$Lambda$MediaRoute2ProviderServiceAdapter$jCQ6D1XoxqqGZNXfOvVvVmBhQWg
                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return MediaRoute2ProviderServiceAdapter.lambda$updateStaticSessions$3((MediaRoute2ProviderServiceAdapter.SessionRecord) obj);
                }
            }).collect(Collectors.toList());
        }
        for (SessionRecord sessionRecord : list) {
            DynamicGroupRouteControllerProxy dynamicGroupRouteControllerProxy = (DynamicGroupRouteControllerProxy) sessionRecord.getGroupController();
            if (map.containsKey(dynamicGroupRouteControllerProxy.getRouteId())) {
                sessionRecord.updateSessionInfo(map.get(dynamicGroupRouteControllerProxy.getRouteId()), null);
            }
        }
    }

    static /* synthetic */ boolean lambda$updateStaticSessions$3(SessionRecord sessionRecord) {
        return (sessionRecord.getFlags() & 4) == 0;
    }

    void onControlRequest(final Messenger messenger, final int i, final String str, final Intent intent) {
        if (getSessionInfo(str) == null) {
            Log.w(TAG, "onCustomCommand: Couldn't find a session");
            return;
        }
        MediaRouteProvider.DynamicGroupRouteController findControllerBySessionId = findControllerBySessionId(str);
        if (findControllerBySessionId == null) {
            Log.w(TAG, "onControlRequest: Couldn't find a controller");
            notifyRequestFailed(i, 3);
        } else {
            findControllerBySessionId.onControlRequest(intent, new MediaRouter.ControlRequestCallback() { // from class: androidx.mediarouter.media.MediaRoute2ProviderServiceAdapter.1
                @Override // androidx.mediarouter.media.MediaRouter.ControlRequestCallback
                public void onResult(Bundle bundle) {
                    if (MediaRoute2ProviderServiceAdapter.DEBUG) {
                        Log.d(MediaRoute2ProviderServiceAdapter.TAG, "Route control request succeeded, sessionId=" + str + ", intent=" + intent + ", data=" + bundle);
                    }
                    sendReply(messenger, 3, i, 0, bundle, null);
                }

                @Override // androidx.mediarouter.media.MediaRouter.ControlRequestCallback
                public void onError(String str2, Bundle bundle) {
                    if (MediaRoute2ProviderServiceAdapter.DEBUG) {
                        Log.d(MediaRoute2ProviderServiceAdapter.TAG, "Route control request failed, sessionId=" + str + ", intent=" + intent + ", error=" + str2 + ", data=" + bundle);
                    }
                    if (str2 != null) {
                        Bundle bundle2 = new Bundle();
                        bundle2.putString(MediaRouteProviderProtocol.SERVICE_DATA_ERROR, str2);
                        sendReply(messenger, 4, i, 0, bundle, bundle2);
                        return;
                    }
                    sendReply(messenger, 4, i, 0, bundle, null);
                }

                void sendReply(Messenger messenger2, int i2, int i3, int i4, Object obj, Bundle bundle) {
                    Message obtain = Message.obtain();
                    obtain.what = i2;
                    obtain.arg1 = i3;
                    obtain.arg2 = i4;
                    obtain.obj = obj;
                    obtain.setData(bundle);
                    try {
                        messenger2.send(obtain);
                    } catch (DeadObjectException unused) {
                    } catch (RemoteException e) {
                        Log.e(MediaRoute2ProviderServiceAdapter.TAG, "Could not send message to the client.", e);
                    }
                }
            });
        }
    }

    void setRouteVolume(@NonNull String str, int i) {
        MediaRouteProvider.RouteController findControllerByRouteId = findControllerByRouteId(str);
        if (findControllerByRouteId == null) {
            Log.w(TAG, "setRouteVolume: Couldn't find a controller for routeId=" + str);
            return;
        }
        findControllerByRouteId.onSetVolume(i);
    }

    void updateRouteVolume(@NonNull String str, int i) {
        MediaRouteProvider.RouteController findControllerByRouteId = findControllerByRouteId(str);
        if (findControllerByRouteId == null) {
            Log.w(TAG, "updateRouteVolume: Couldn't find a controller for routeId=" + str);
            return;
        }
        findControllerByRouteId.onUpdateVolume(i);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v2, types: [androidx.mediarouter.media.MediaRouteProvider$DynamicGroupRouteController] */
    void notifyRouteControllerAdded(MediaRouteProviderService.MediaRouteProviderServiceImplApi30.ClientRecord clientRecord, MediaRouteProvider.RouteController routeController, int i, String str, String str2) {
        int i2;
        DynamicGroupRouteControllerProxy dynamicGroupRouteControllerProxy;
        MediaRouteDescriptor routeDescriptor = getRouteDescriptor(str2, "notifyRouteControllerAdded");
        if (routeDescriptor == null) {
            return;
        }
        if (routeController instanceof MediaRouteProvider.DynamicGroupRouteController) {
            dynamicGroupRouteControllerProxy = (MediaRouteProvider.DynamicGroupRouteController) routeController;
            i2 = 6;
        } else {
            i2 = routeDescriptor.getGroupMemberIds().isEmpty() ? 0 : 2;
            dynamicGroupRouteControllerProxy = new DynamicGroupRouteControllerProxy(str2, routeController);
        }
        SessionRecord sessionRecord = new SessionRecord(dynamicGroupRouteControllerProxy, 0L, i2, clientRecord);
        sessionRecord.mRouteId = str2;
        String assignSessionId = assignSessionId(sessionRecord);
        this.mSessionIdMap.put(i, assignSessionId);
        sessionRecord.setSessionInfo(new RoutingSessionInfo.Builder(assignSessionId, str).addSelectedRoute(str2).setName(routeDescriptor.getName()).setVolumeHandling(routeDescriptor.getVolumeHandling()).setVolume(routeDescriptor.getVolume()).setVolumeMax(routeDescriptor.getVolumeMax()).build());
    }

    void notifyRouteControllerRemoved(int i) {
        SessionRecord remove;
        String str = this.mSessionIdMap.get(i);
        if (str == null) {
            return;
        }
        this.mSessionIdMap.remove(i);
        synchronized (this.mLock) {
            remove = this.mSessionRecords.remove(str);
        }
        if (remove != null) {
            remove.release(false);
        }
    }

    private MediaRouteProvider.RouteController findControllerByRouteId(String str) {
        ArrayList arrayList = new ArrayList();
        synchronized (this.mLock) {
            arrayList.addAll(this.mSessionRecords.values());
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            MediaRouteProvider.RouteController findControllerByRouteId = ((SessionRecord) it.next()).findControllerByRouteId(str);
            if (findControllerByRouteId != null) {
                return findControllerByRouteId;
            }
        }
        return null;
    }

    MediaRouteProvider getMediaRouteProvider() {
        MediaRouteProviderService service = this.mServiceImpl.getService();
        if (service == null) {
            return null;
        }
        return service.getMediaRouteProvider();
    }

    private String assignSessionId(SessionRecord sessionRecord) {
        String uuid;
        synchronized (this.mLock) {
            do {
                uuid = UUID.randomUUID().toString();
            } while (this.mSessionRecords.containsKey(uuid));
            sessionRecord.mSessionId = uuid;
            this.mSessionRecords.put(uuid, sessionRecord);
        }
        return uuid;
    }

    private static class DynamicGroupRouteControllerProxy extends MediaRouteProvider.DynamicGroupRouteController {
        final MediaRouteProvider.RouteController mRouteController;
        private final String mRouteId;

        @Override // androidx.mediarouter.media.MediaRouteProvider.DynamicGroupRouteController
        public void onAddMemberRoute(@NonNull String str) {
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.DynamicGroupRouteController
        public void onRemoveMemberRoute(String str) {
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.DynamicGroupRouteController
        public void onUpdateMemberRoutes(@Nullable List<String> list) {
        }

        DynamicGroupRouteControllerProxy(String str, MediaRouteProvider.RouteController routeController) {
            this.mRouteId = str;
            this.mRouteController = routeController;
        }

        public String getRouteId() {
            return this.mRouteId;
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public void onRelease() {
            this.mRouteController.onRelease();
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public void onSelect() {
            this.mRouteController.onSelect();
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public void onUnselect(int i) {
            this.mRouteController.onUnselect(i);
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public void onSetVolume(int i) {
            this.mRouteController.onSetVolume(i);
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public void onUpdateVolume(int i) {
            this.mRouteController.onUpdateVolume(i);
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public boolean onControlRequest(Intent intent, MediaRouter.ControlRequestCallback controlRequestCallback) {
            return this.mRouteController.onControlRequest(intent, controlRequestCallback);
        }
    }

    final class SessionRecord {
        static final int SESSION_FLAG_DYNAMIC = 4;
        static final int SESSION_FLAG_GROUP = 2;
        static final int SESSION_FLAG_MR2 = 1;
        private final WeakReference<MediaRouteProviderService.MediaRouteProviderServiceImplApi30.ClientRecord> mClientRecord;
        private final MediaRouteProvider.DynamicGroupRouteController mController;
        private final int mFlags;
        private boolean mIsCreated;
        private boolean mIsReleased;
        private final long mRequestId;
        String mRouteId;
        private final Map<String, MediaRouteProvider.RouteController> mRouteIdToControllerMap;
        String mSessionId;
        private RoutingSessionInfo mSessionInfo;

        SessionRecord(MediaRoute2ProviderServiceAdapter mediaRoute2ProviderServiceAdapter, MediaRouteProvider.DynamicGroupRouteController dynamicGroupRouteController, long j, int i) {
            this(dynamicGroupRouteController, j, i, null);
        }

        SessionRecord(MediaRouteProvider.DynamicGroupRouteController dynamicGroupRouteController, long j, int i, MediaRouteProviderService.MediaRouteProviderServiceImplApi30.ClientRecord clientRecord) {
            this.mRouteIdToControllerMap = new ArrayMap();
            this.mIsCreated = false;
            this.mController = dynamicGroupRouteController;
            this.mRequestId = j;
            this.mFlags = i;
            this.mClientRecord = new WeakReference<>(clientRecord);
        }

        public int getFlags() {
            return this.mFlags;
        }

        MediaRouteProvider.DynamicGroupRouteController getGroupController() {
            return this.mController;
        }

        MediaRouteProvider.RouteController findControllerByRouteId(String str) {
            MediaRouteProviderService.MediaRouteProviderServiceImplApi30.ClientRecord clientRecord = this.mClientRecord.get();
            if (clientRecord != null) {
                return clientRecord.findControllerByRouteId(str);
            }
            return this.mRouteIdToControllerMap.get(str);
        }

        void setSessionInfo(@NonNull RoutingSessionInfo routingSessionInfo) {
            if (this.mSessionInfo != null) {
                Log.w(MediaRoute2ProviderServiceAdapter.TAG, "setSessionInfo: This shouldn't be called after sesionInfo is set");
                return;
            }
            Messenger messenger = new Messenger(new IncomingHandler(MediaRoute2ProviderServiceAdapter.this, this.mSessionId));
            RoutingSessionInfo.Builder builder = new RoutingSessionInfo.Builder(routingSessionInfo);
            Bundle bundle = new Bundle();
            bundle.putParcelable("androidx.mediarouter.media.KEY_MESSENGER", messenger);
            bundle.putString("androidx.mediarouter.media.KEY_SESSION_NAME", routingSessionInfo.getName() != null ? routingSessionInfo.getName().toString() : null);
            this.mSessionInfo = builder.setControlHints(bundle).build();
        }

        public void updateSessionInfo(@Nullable MediaRouteDescriptor mediaRouteDescriptor, @Nullable Collection<MediaRouteProvider.DynamicGroupRouteController.DynamicRouteDescriptor> collection) {
            RoutingSessionInfo routingSessionInfo = this.mSessionInfo;
            if (routingSessionInfo == null) {
                Log.w(MediaRoute2ProviderServiceAdapter.TAG, "updateSessionInfo: mSessionInfo is null. This shouldn't happen.");
                return;
            }
            if (mediaRouteDescriptor != null && !mediaRouteDescriptor.isEnabled()) {
                MediaRoute2ProviderServiceAdapter.this.onReleaseSession(0L, this.mSessionId);
                return;
            }
            RoutingSessionInfo.Builder builder = new RoutingSessionInfo.Builder(routingSessionInfo);
            if (mediaRouteDescriptor != null) {
                this.mRouteId = mediaRouteDescriptor.getId();
                builder.setName(mediaRouteDescriptor.getName()).setVolume(mediaRouteDescriptor.getVolume()).setVolumeMax(mediaRouteDescriptor.getVolumeMax()).setVolumeHandling(mediaRouteDescriptor.getVolumeHandling());
                Bundle controlHints = routingSessionInfo.getControlHints();
                if (controlHints == null) {
                    Log.w(MediaRoute2ProviderServiceAdapter.TAG, "updateSessionInfo: controlHints is null. This shouldn't happen.");
                    controlHints = new Bundle();
                }
                controlHints.putString("androidx.mediarouter.media.KEY_SESSION_NAME", mediaRouteDescriptor.getName());
                controlHints.putBundle("androidx.mediarouter.media.KEY_GROUP_ROUTE", mediaRouteDescriptor.asBundle());
                builder.setControlHints(controlHints);
            }
            this.mSessionInfo = builder.build();
            if (collection != null && !collection.isEmpty()) {
                boolean z = false;
                builder.clearSelectedRoutes();
                builder.clearSelectableRoutes();
                builder.clearDeselectableRoutes();
                builder.clearTransferableRoutes();
                for (MediaRouteProvider.DynamicGroupRouteController.DynamicRouteDescriptor dynamicRouteDescriptor : collection) {
                    String id = dynamicRouteDescriptor.getRouteDescriptor().getId();
                    int i = dynamicRouteDescriptor.mSelectionState;
                    if (i == 2 || i == 3) {
                        builder.addSelectedRoute(id);
                        z = true;
                    }
                    if (dynamicRouteDescriptor.isGroupable()) {
                        builder.addSelectableRoute(id);
                    }
                    if (dynamicRouteDescriptor.isUnselectable()) {
                        builder.addDeselectableRoute(id);
                    }
                    if (dynamicRouteDescriptor.isTransferable()) {
                        builder.addTransferableRoute(id);
                    }
                }
                if (z) {
                    this.mSessionInfo = builder.build();
                }
            }
            if ((this.mFlags & 5) == 5 && mediaRouteDescriptor != null) {
                updateMemberRouteControllers(mediaRouteDescriptor.getId(), routingSessionInfo, this.mSessionInfo);
            }
            if (!this.mIsCreated) {
                notifySessionCreated();
            } else {
                MediaRoute2ProviderServiceAdapter.this.notifySessionUpdated(this.mSessionInfo);
            }
        }

        public void release(boolean z) {
            MediaRouteProviderService.MediaRouteProviderServiceImplApi30.ClientRecord clientRecord;
            if (this.mIsReleased) {
                return;
            }
            if ((this.mFlags & 3) == 3) {
                updateMemberRouteControllers(null, this.mSessionInfo, null);
            }
            if (z) {
                this.mController.onUnselect(2);
                this.mController.onRelease();
                if ((this.mFlags & 1) == 0 && (clientRecord = this.mClientRecord.get()) != null) {
                    MediaRouteProvider.RouteController routeController = this.mController;
                    if (routeController instanceof DynamicGroupRouteControllerProxy) {
                        routeController = ((DynamicGroupRouteControllerProxy) routeController).mRouteController;
                    }
                    clientRecord.releaseControllerByProvider(routeController, this.mRouteId);
                }
            }
            this.mIsReleased = true;
            MediaRoute2ProviderServiceAdapter.this.notifySessionReleased(this.mSessionId);
        }

        public void updateMemberRouteControllers(String str, RoutingSessionInfo routingSessionInfo, RoutingSessionInfo routingSessionInfo2) {
            List<String> selectedRoutes;
            List<String> selectedRoutes2;
            if (routingSessionInfo == null) {
                selectedRoutes = Collections.emptyList();
            } else {
                selectedRoutes = routingSessionInfo.getSelectedRoutes();
            }
            if (routingSessionInfo2 == null) {
                selectedRoutes2 = Collections.emptyList();
            } else {
                selectedRoutes2 = routingSessionInfo2.getSelectedRoutes();
            }
            for (String str2 : selectedRoutes2) {
                if (findControllerByRouteId(str2) == null) {
                    getOrCreateRouteController(str2, str).onSelect();
                }
            }
            for (String str3 : selectedRoutes) {
                if (!selectedRoutes2.contains(str3)) {
                    releaseRouteControllerByRouteId(str3);
                }
            }
        }

        private void notifySessionCreated() {
            if (this.mIsCreated) {
                Log.w(MediaRoute2ProviderServiceAdapter.TAG, "notifySessionCreated: Routing session is already created.");
            } else {
                this.mIsCreated = true;
                MediaRoute2ProviderServiceAdapter.this.notifySessionCreated(this.mRequestId, this.mSessionInfo);
            }
        }

        private MediaRouteProvider.RouteController getOrCreateRouteController(String str, String str2) {
            MediaRouteProvider.RouteController onCreateRouteController;
            MediaRouteProvider.RouteController routeController = this.mRouteIdToControllerMap.get(str);
            if (routeController != null) {
                return routeController;
            }
            if (str2 == null) {
                onCreateRouteController = MediaRoute2ProviderServiceAdapter.this.getMediaRouteProvider().onCreateRouteController(str);
            } else {
                onCreateRouteController = MediaRoute2ProviderServiceAdapter.this.getMediaRouteProvider().onCreateRouteController(str, str2);
            }
            if (onCreateRouteController != null) {
                this.mRouteIdToControllerMap.put(str, onCreateRouteController);
            }
            return onCreateRouteController;
        }

        private boolean releaseRouteControllerByRouteId(String str) {
            MediaRouteProvider.RouteController remove = this.mRouteIdToControllerMap.remove(str);
            if (remove == null) {
                return false;
            }
            remove.onUnselect(0);
            remove.onRelease();
            return true;
        }
    }

    static class IncomingHandler extends Handler {
        private final MediaRoute2ProviderServiceAdapter mServiceAdapter;
        private final String mSessionId;

        IncomingHandler(MediaRoute2ProviderServiceAdapter mediaRoute2ProviderServiceAdapter, String str) {
            super(Looper.myLooper());
            this.mServiceAdapter = mediaRoute2ProviderServiceAdapter;
            this.mSessionId = str;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            Messenger messenger = message.replyTo;
            int i = message.what;
            int i2 = message.arg1;
            Object obj = message.obj;
            Bundle data = message.getData();
            if (i == 7) {
                int i3 = data.getInt(MediaRouteProviderProtocol.CLIENT_DATA_VOLUME, -1);
                String string = data.getString(MediaRouteProviderProtocol.CLIENT_DATA_ROUTE_ID);
                if (i3 < 0 || string == null) {
                    return;
                }
                this.mServiceAdapter.setRouteVolume(string, i3);
                return;
            }
            if (i != 8) {
                if (i == 9 && (obj instanceof Intent)) {
                    this.mServiceAdapter.onControlRequest(messenger, i2, this.mSessionId, (Intent) obj);
                    return;
                }
                return;
            }
            int i4 = data.getInt(MediaRouteProviderProtocol.CLIENT_DATA_VOLUME, 0);
            String string2 = data.getString(MediaRouteProviderProtocol.CLIENT_DATA_ROUTE_ID);
            if (i4 == 0 || string2 == null) {
                return;
            }
            this.mServiceAdapter.updateRouteVolume(string2, i4);
        }
    }
}
