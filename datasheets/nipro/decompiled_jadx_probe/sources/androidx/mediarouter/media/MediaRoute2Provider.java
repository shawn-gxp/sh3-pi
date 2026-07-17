package androidx.mediarouter.media;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRoute2Info;
import android.media.MediaRouter2;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.mediarouter.R;
import androidx.mediarouter.media.MediaRoute2Provider;
import androidx.mediarouter.media.MediaRouteDescriptor;
import androidx.mediarouter.media.MediaRouteProvider;
import androidx.mediarouter.media.MediaRouteProviderDescriptor;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiresApi(30)
/* loaded from: classes.dex */
class MediaRoute2Provider extends MediaRouteProvider {
    final Callback mCallback;
    private final MediaRouter2.ControllerCallback mControllerCallback;
    final Map<MediaRouter2.RoutingController, GroupRouteController> mControllerMap;
    private final Handler mHandler;
    private final Executor mHandlerExecutor;
    final MediaRouter2 mMediaRouter2;
    private final MediaRouter2.RouteCallback mRouteCallback;
    private Map<String, String> mRouteIdToOriginalRouteIdMap;
    private List<MediaRoute2Info> mRoutes;
    private final MediaRouter2.TransferCallback mTransferCallback;
    static final String TAG = "MR2Provider";
    static final boolean DEBUG = Log.isLoggable(TAG, 3);

    MediaRoute2Provider(@NonNull Context context, @NonNull Callback callback) {
        super(context);
        this.mControllerMap = new ArrayMap();
        this.mRouteCallback = new RouteCallback();
        this.mTransferCallback = new TransferCallback();
        this.mControllerCallback = new ControllerCallback();
        this.mRoutes = new ArrayList();
        this.mRouteIdToOriginalRouteIdMap = new ArrayMap();
        this.mMediaRouter2 = MediaRouter2.getInstance(context);
        this.mCallback = callback;
        final Handler handler = new Handler(Looper.getMainLooper());
        this.mHandler = handler;
        this.mHandlerExecutor = new Executor() { // from class: androidx.mediarouter.media.-$$Lambda$LfzJt661qZfn2w-6SYHFbD3aMy0
            @Override // java.util.concurrent.Executor
            public final void execute(Runnable runnable) {
                handler.post(runnable);
            }
        };
    }

    @Override // androidx.mediarouter.media.MediaRouteProvider
    public void onDiscoveryRequestChanged(@Nullable MediaRouteDiscoveryRequest mediaRouteDiscoveryRequest) {
        if (MediaRouter.getGlobalCallbackCount() > 0) {
            this.mMediaRouter2.registerRouteCallback(this.mHandlerExecutor, this.mRouteCallback, MediaRouter2Utils.toDiscoveryPreference(updateDiscoveryRequest(mediaRouteDiscoveryRequest, MediaRouter.isTransferToLocalEnabled())));
            this.mMediaRouter2.registerTransferCallback(this.mHandlerExecutor, this.mTransferCallback);
            this.mMediaRouter2.registerControllerCallback(this.mHandlerExecutor, this.mControllerCallback);
            return;
        }
        this.mMediaRouter2.unregisterRouteCallback(this.mRouteCallback);
        this.mMediaRouter2.unregisterTransferCallback(this.mTransferCallback);
        this.mMediaRouter2.unregisterControllerCallback(this.mControllerCallback);
    }

    @Override // androidx.mediarouter.media.MediaRouteProvider
    @Nullable
    public MediaRouteProvider.RouteController onCreateRouteController(@NonNull String str) {
        return new MemberRouteController(this.mRouteIdToOriginalRouteIdMap.get(str), null);
    }

    @Override // androidx.mediarouter.media.MediaRouteProvider
    @Nullable
    public MediaRouteProvider.RouteController onCreateRouteController(@NonNull String str, @NonNull String str2) {
        String str3 = this.mRouteIdToOriginalRouteIdMap.get(str);
        for (GroupRouteController groupRouteController : this.mControllerMap.values()) {
            if (TextUtils.equals(str2, groupRouteController.mRoutingController.getId())) {
                return new MemberRouteController(str3, groupRouteController);
            }
        }
        Log.w(TAG, "Could not find the matching GroupRouteController. routeId=" + str + ", routeGroupId=" + str2);
        return new MemberRouteController(str3, null);
    }

    @Override // androidx.mediarouter.media.MediaRouteProvider
    @Nullable
    public MediaRouteProvider.DynamicGroupRouteController onCreateDynamicGroupRouteController(@NonNull String str) {
        Iterator<Map.Entry<MediaRouter2.RoutingController, GroupRouteController>> it = this.mControllerMap.entrySet().iterator();
        while (it.hasNext()) {
            GroupRouteController value = it.next().getValue();
            if (TextUtils.equals(str, value.mInitialMemberRouteId)) {
                return value;
            }
        }
        return null;
    }

    public void transferTo(@NonNull String str) {
        MediaRoute2Info routeById = getRouteById(str);
        if (routeById == null) {
            Log.w(TAG, "transferTo: Specified route not found. routeId=" + str);
            return;
        }
        this.mMediaRouter2.transferTo(routeById);
    }

    protected void refreshRoutes() {
        List<MediaRoute2Info> list = (List) this.mMediaRouter2.getRoutes().stream().distinct().filter(new Predicate() { // from class: androidx.mediarouter.media.-$$Lambda$MediaRoute2Provider$VT-gS4VAy5vSwKN7_lZ6W4L_NPw
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return MediaRoute2Provider.lambda$refreshRoutes$0((MediaRoute2Info) obj);
            }
        }).collect(Collectors.toList());
        if (list.equals(this.mRoutes)) {
            return;
        }
        this.mRoutes = list;
        this.mRouteIdToOriginalRouteIdMap.clear();
        for (MediaRoute2Info mediaRoute2Info : this.mRoutes) {
            Bundle extras = mediaRoute2Info.getExtras();
            if (extras == null || extras.getString("androidx.mediarouter.media.KEY_ORIGINAL_ROUTE_ID") == null) {
                Log.w(TAG, "Cannot find the original route Id. route=" + mediaRoute2Info);
            } else {
                this.mRouteIdToOriginalRouteIdMap.put(mediaRoute2Info.getId(), extras.getString("androidx.mediarouter.media.KEY_ORIGINAL_ROUTE_ID"));
            }
        }
        setDescriptor(new MediaRouteProviderDescriptor.Builder().setSupportsDynamicGroupRoute(true).addRoutes((List) this.mRoutes.stream().map(new Function() { // from class: androidx.mediarouter.media.-$$Lambda$853YVfaGw0-G4oNUYI6Z1ujaq6k
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return MediaRouter2Utils.toMediaRouteDescriptor((MediaRoute2Info) obj);
            }
        }).filter($$Lambda$jMO9OfSzscMxGho8zZuPtPiQlPo.INSTANCE).collect(Collectors.toList())).build());
    }

    static /* synthetic */ boolean lambda$refreshRoutes$0(MediaRoute2Info mediaRoute2Info) {
        return !mediaRoute2Info.isSystemRoute();
    }

    @Nullable
    MediaRoute2Info getRouteById(@Nullable String str) {
        if (str == null) {
            return null;
        }
        for (MediaRoute2Info mediaRoute2Info : this.mRoutes) {
            if (TextUtils.equals(mediaRoute2Info.getId(), str)) {
                return mediaRoute2Info;
            }
        }
        return null;
    }

    @Nullable
    static Messenger getMessengerFromRoutingController(@Nullable MediaRouter2.RoutingController routingController) {
        Bundle controlHints;
        if (routingController == null || (controlHints = routingController.getControlHints()) == null) {
            return null;
        }
        return (Messenger) controlHints.getParcelable("androidx.mediarouter.media.KEY_MESSENGER");
    }

    @Nullable
    static String getSessionIdForRouteController(@Nullable MediaRouteProvider.RouteController routeController) {
        MediaRouter2.RoutingController routingController;
        if ((routeController instanceof GroupRouteController) && (routingController = ((GroupRouteController) routeController).mRoutingController) != null) {
            return routingController.getId();
        }
        return null;
    }

    void setDynamicRouteDescriptors(MediaRouter2.RoutingController routingController) {
        GroupRouteController groupRouteController = this.mControllerMap.get(routingController);
        if (groupRouteController == null) {
            Log.w(TAG, "setDynamicRouteDescriptors: No matching routeController found. routingController=" + routingController);
            return;
        }
        List<String> routeIds = MediaRouter2Utils.getRouteIds(routingController.getSelectedRoutes());
        MediaRouteDescriptor mediaRouteDescriptor = MediaRouter2Utils.toMediaRouteDescriptor(routingController.getSelectedRoutes().get(0));
        MediaRouteDescriptor mediaRouteDescriptor2 = null;
        Bundle controlHints = routingController.getControlHints();
        String string = getContext().getString(R.string.mr_dialog_default_group_name);
        if (controlHints != null) {
            try {
                String string2 = controlHints.getString("androidx.mediarouter.media.KEY_SESSION_NAME");
                if (!TextUtils.isEmpty(string2)) {
                    string = string2;
                }
                Bundle bundle = controlHints.getBundle("androidx.mediarouter.media.KEY_GROUP_ROUTE");
                if (bundle != null) {
                    mediaRouteDescriptor2 = MediaRouteDescriptor.fromBundle(bundle);
                }
            } catch (Exception e) {
                Log.w(TAG, "Exception while unparceling control hints.", e);
            }
        }
        if (mediaRouteDescriptor2 == null) {
            mediaRouteDescriptor2 = new MediaRouteDescriptor.Builder(routingController.getId(), string).setConnectionState(2).setPlaybackType(1).setVolume(routingController.getVolume()).setVolumeMax(routingController.getVolumeMax()).setVolumeHandling(routingController.getVolumeHandling()).addControlFilters(mediaRouteDescriptor.getControlFilters()).addGroupMemberIds(routeIds).build();
        }
        List<String> routeIds2 = MediaRouter2Utils.getRouteIds(routingController.getSelectableRoutes());
        List<String> routeIds3 = MediaRouter2Utils.getRouteIds(routingController.getDeselectableRoutes());
        MediaRouteProviderDescriptor descriptor = getDescriptor();
        if (descriptor == null) {
            Log.w(TAG, "setDynamicRouteDescriptors: providerDescriptor is not set.");
            return;
        }
        ArrayList arrayList = new ArrayList();
        List<MediaRouteDescriptor> routes = descriptor.getRoutes();
        if (!routes.isEmpty()) {
            for (MediaRouteDescriptor mediaRouteDescriptor3 : routes) {
                String id = mediaRouteDescriptor3.getId();
                arrayList.add(new MediaRouteProvider.DynamicGroupRouteController.DynamicRouteDescriptor.Builder(mediaRouteDescriptor3).setSelectionState(routeIds.contains(id) ? 3 : 1).setIsGroupable(routeIds2.contains(id)).setIsUnselectable(routeIds3.contains(id)).setIsTransferable(true).build());
            }
        }
        groupRouteController.notifyDynamicRoutesChanged(mediaRouteDescriptor2, arrayList);
    }

    private MediaRouteDiscoveryRequest updateDiscoveryRequest(@Nullable MediaRouteDiscoveryRequest mediaRouteDiscoveryRequest, boolean z) {
        if (mediaRouteDiscoveryRequest == null) {
            mediaRouteDiscoveryRequest = new MediaRouteDiscoveryRequest(MediaRouteSelector.EMPTY, false);
        }
        List<String> controlCategories = mediaRouteDiscoveryRequest.getSelector().getControlCategories();
        if (z) {
            if (!controlCategories.contains(MediaControlIntent.CATEGORY_LIVE_AUDIO)) {
                controlCategories.add(MediaControlIntent.CATEGORY_LIVE_AUDIO);
            }
        } else {
            controlCategories.remove(MediaControlIntent.CATEGORY_LIVE_AUDIO);
        }
        return new MediaRouteDiscoveryRequest(new MediaRouteSelector.Builder().addControlCategories(controlCategories).build(), mediaRouteDiscoveryRequest.isActiveScan());
    }

    static abstract class Callback {
        public abstract void onReleaseController(@NonNull MediaRouteProvider.RouteController routeController);

        public abstract void onSelectFallbackRoute(int i);

        public abstract void onSelectRoute(@NonNull String str, int i);

        Callback() {
        }
    }

    private class RouteCallback extends MediaRouter2.RouteCallback {
        RouteCallback() {
        }

        @Override // android.media.MediaRouter2.RouteCallback
        public void onRoutesAdded(@NonNull List<MediaRoute2Info> list) {
            MediaRoute2Provider.this.refreshRoutes();
        }

        @Override // android.media.MediaRouter2.RouteCallback
        public void onRoutesRemoved(@NonNull List<MediaRoute2Info> list) {
            MediaRoute2Provider.this.refreshRoutes();
        }

        @Override // android.media.MediaRouter2.RouteCallback
        public void onRoutesChanged(@NonNull List<MediaRoute2Info> list) {
            MediaRoute2Provider.this.refreshRoutes();
        }
    }

    private class TransferCallback extends MediaRouter2.TransferCallback {
        TransferCallback() {
        }

        @Override // android.media.MediaRouter2.TransferCallback
        public void onTransfer(@NonNull MediaRouter2.RoutingController routingController, @NonNull MediaRouter2.RoutingController routingController2) {
            MediaRoute2Provider.this.mControllerMap.remove(routingController);
            if (routingController2 == MediaRoute2Provider.this.mMediaRouter2.getSystemController()) {
                MediaRoute2Provider.this.mCallback.onSelectFallbackRoute(3);
                return;
            }
            List<MediaRoute2Info> selectedRoutes = routingController2.getSelectedRoutes();
            if (selectedRoutes.isEmpty()) {
                Log.w(MediaRoute2Provider.TAG, "Selected routes are empty. This shouldn't happen.");
                return;
            }
            String id = selectedRoutes.get(0).getId();
            MediaRoute2Provider.this.mControllerMap.put(routingController2, MediaRoute2Provider.this.new GroupRouteController(routingController2, id));
            MediaRoute2Provider.this.mCallback.onSelectRoute(id, 3);
            MediaRoute2Provider.this.setDynamicRouteDescriptors(routingController2);
        }

        @Override // android.media.MediaRouter2.TransferCallback
        public void onTransferFailure(@NonNull MediaRoute2Info mediaRoute2Info) {
            Log.w(MediaRoute2Provider.TAG, "Transfer failed. requestedRoute=" + mediaRoute2Info);
        }

        @Override // android.media.MediaRouter2.TransferCallback
        public void onStop(@NonNull MediaRouter2.RoutingController routingController) {
            GroupRouteController remove = MediaRoute2Provider.this.mControllerMap.remove(routingController);
            if (remove != null) {
                MediaRoute2Provider.this.mCallback.onReleaseController(remove);
                return;
            }
            Log.w(MediaRoute2Provider.TAG, "onStop: No matching routeController found. routingController=" + routingController);
        }
    }

    private class ControllerCallback extends MediaRouter2.ControllerCallback {
        ControllerCallback() {
        }

        @Override // android.media.MediaRouter2.ControllerCallback
        public void onControllerUpdated(@NonNull MediaRouter2.RoutingController routingController) {
            MediaRoute2Provider.this.setDynamicRouteDescriptors(routingController);
        }
    }

    private class MemberRouteController extends MediaRouteProvider.RouteController {
        final GroupRouteController mGroupRouteController;
        final String mOriginalRouteId;

        MemberRouteController(@Nullable String str, @Nullable GroupRouteController groupRouteController) {
            this.mOriginalRouteId = str;
            this.mGroupRouteController = groupRouteController;
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public void onSetVolume(int i) {
            GroupRouteController groupRouteController;
            String str = this.mOriginalRouteId;
            if (str == null || (groupRouteController = this.mGroupRouteController) == null) {
                return;
            }
            groupRouteController.setMemberRouteVolume(str, i);
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public void onUpdateVolume(int i) {
            GroupRouteController groupRouteController;
            String str = this.mOriginalRouteId;
            if (str == null || (groupRouteController = this.mGroupRouteController) == null) {
                return;
            }
            groupRouteController.updateMemberRouteVolume(str, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class GroupRouteController extends MediaRouteProvider.DynamicGroupRouteController {
        private static final long OPTIMISTIC_VOLUME_TIMEOUT_MS = 1000;
        final Handler mControllerHandler;
        final String mInitialMemberRouteId;

        @Nullable
        final Messenger mReceiveMessenger;
        final MediaRouter2.RoutingController mRoutingController;

        @Nullable
        final Messenger mServiceMessenger;
        final SparseArray<MediaRouter.ControlRequestCallback> mPendingCallbacks = new SparseArray<>();
        AtomicInteger mNextRequestId = new AtomicInteger(1);
        private final Runnable mClearOptimisticVolumeRunnable = new Runnable() { // from class: androidx.mediarouter.media.-$$Lambda$MediaRoute2Provider$GroupRouteController$VuEErC2WJPvNnsDkymBxR697UT0
            @Override // java.lang.Runnable
            public final void run() {
                MediaRoute2Provider.GroupRouteController.this.lambda$new$0$MediaRoute2Provider$GroupRouteController();
            }
        };
        int mOptimisticVolume = -1;

        public /* synthetic */ void lambda$new$0$MediaRoute2Provider$GroupRouteController() {
            this.mOptimisticVolume = -1;
        }

        GroupRouteController(@NonNull MediaRouter2.RoutingController routingController, @NonNull String str) {
            this.mRoutingController = routingController;
            this.mInitialMemberRouteId = str;
            Messenger messengerFromRoutingController = MediaRoute2Provider.getMessengerFromRoutingController(routingController);
            this.mServiceMessenger = messengerFromRoutingController;
            this.mReceiveMessenger = messengerFromRoutingController == null ? null : new Messenger(new ReceiveHandler());
            this.mControllerHandler = new Handler(Looper.getMainLooper());
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public void onSetVolume(int i) {
            MediaRouter2.RoutingController routingController = this.mRoutingController;
            if (routingController == null) {
                return;
            }
            routingController.setVolume(i);
            this.mOptimisticVolume = i;
            scheduleClearOptimisticVolume();
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public void onUpdateVolume(int i) {
            MediaRouter2.RoutingController routingController = this.mRoutingController;
            if (routingController == null) {
                return;
            }
            int i2 = this.mOptimisticVolume;
            if (i2 < 0) {
                i2 = routingController.getVolume();
            }
            int max = Math.max(0, Math.min(i2 + i, this.mRoutingController.getVolumeMax()));
            this.mOptimisticVolume = max;
            this.mRoutingController.setVolume(max);
            scheduleClearOptimisticVolume();
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public boolean onControlRequest(Intent intent, @Nullable MediaRouter.ControlRequestCallback controlRequestCallback) {
            MediaRouter2.RoutingController routingController = this.mRoutingController;
            if (routingController != null && !routingController.isReleased() && this.mServiceMessenger != null) {
                int andIncrement = this.mNextRequestId.getAndIncrement();
                Message obtain = Message.obtain();
                obtain.what = 9;
                obtain.arg1 = andIncrement;
                obtain.obj = intent;
                obtain.replyTo = this.mReceiveMessenger;
                try {
                    this.mServiceMessenger.send(obtain);
                    if (controlRequestCallback == null) {
                        return true;
                    }
                    this.mPendingCallbacks.put(andIncrement, controlRequestCallback);
                    return true;
                } catch (DeadObjectException unused) {
                } catch (RemoteException e) {
                    Log.e(MediaRoute2Provider.TAG, "Could not send control request to service.", e);
                }
            }
            return false;
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.RouteController
        public void onRelease() {
            this.mRoutingController.release();
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.DynamicGroupRouteController
        public void onUpdateMemberRoutes(@Nullable List<String> list) {
            if (list == null || list.isEmpty()) {
                Log.w(MediaRoute2Provider.TAG, "onUpdateMemberRoutes: Ignoring null or empty routeIds.");
                return;
            }
            String str = list.get(0);
            MediaRoute2Info routeById = MediaRoute2Provider.this.getRouteById(str);
            if (routeById == null) {
                Log.w(MediaRoute2Provider.TAG, "onUpdateMemberRoutes: Specified route not found. routeId=" + str);
                return;
            }
            MediaRoute2Provider.this.mMediaRouter2.transferTo(routeById);
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.DynamicGroupRouteController
        public void onAddMemberRoute(@NonNull String str) {
            if (str == null || str.isEmpty()) {
                Log.w(MediaRoute2Provider.TAG, "onAddMemberRoute: Ignoring null or empty routeId.");
                return;
            }
            MediaRoute2Info routeById = MediaRoute2Provider.this.getRouteById(str);
            if (routeById == null) {
                Log.w(MediaRoute2Provider.TAG, "onAddMemberRoute: Specified route not found. routeId=" + str);
                return;
            }
            this.mRoutingController.selectRoute(routeById);
        }

        @Override // androidx.mediarouter.media.MediaRouteProvider.DynamicGroupRouteController
        public void onRemoveMemberRoute(String str) {
            if (str == null || str.isEmpty()) {
                Log.w(MediaRoute2Provider.TAG, "onRemoveMemberRoute: Ignoring null or empty routeId.");
                return;
            }
            MediaRoute2Info routeById = MediaRoute2Provider.this.getRouteById(str);
            if (routeById == null) {
                Log.w(MediaRoute2Provider.TAG, "onRemoveMemberRoute: Specified route not found. routeId=" + str);
                return;
            }
            this.mRoutingController.deselectRoute(routeById);
        }

        private void scheduleClearOptimisticVolume() {
            this.mControllerHandler.removeCallbacks(this.mClearOptimisticVolumeRunnable);
            this.mControllerHandler.postDelayed(this.mClearOptimisticVolumeRunnable, OPTIMISTIC_VOLUME_TIMEOUT_MS);
        }

        void setMemberRouteVolume(@NonNull String str, int i) {
            int andIncrement = this.mNextRequestId.getAndIncrement();
            Message obtain = Message.obtain();
            obtain.what = 7;
            obtain.arg1 = andIncrement;
            Bundle bundle = new Bundle();
            bundle.putInt(MediaRouteProviderProtocol.CLIENT_DATA_VOLUME, i);
            bundle.putString(MediaRouteProviderProtocol.CLIENT_DATA_ROUTE_ID, str);
            obtain.setData(bundle);
            obtain.replyTo = this.mReceiveMessenger;
            try {
                this.mServiceMessenger.send(obtain);
            } catch (DeadObjectException unused) {
            } catch (RemoteException e) {
                Log.e(MediaRoute2Provider.TAG, "Could not send control request to service.", e);
            }
        }

        void updateMemberRouteVolume(@NonNull String str, int i) {
            int andIncrement = this.mNextRequestId.getAndIncrement();
            Message obtain = Message.obtain();
            obtain.what = 8;
            obtain.arg1 = andIncrement;
            Bundle bundle = new Bundle();
            bundle.putInt(MediaRouteProviderProtocol.CLIENT_DATA_VOLUME, i);
            bundle.putString(MediaRouteProviderProtocol.CLIENT_DATA_ROUTE_ID, str);
            obtain.setData(bundle);
            obtain.replyTo = this.mReceiveMessenger;
            try {
                this.mServiceMessenger.send(obtain);
            } catch (DeadObjectException unused) {
            } catch (RemoteException e) {
                Log.e(MediaRoute2Provider.TAG, "Could not send control request to service.", e);
            }
        }

        class ReceiveHandler extends Handler {
            ReceiveHandler() {
                super(Looper.getMainLooper());
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                int i2 = message.arg1;
                int i3 = message.arg2;
                Object obj = message.obj;
                Bundle peekData = message.peekData();
                MediaRouter.ControlRequestCallback controlRequestCallback = GroupRouteController.this.mPendingCallbacks.get(i2);
                if (controlRequestCallback == null) {
                    Log.w(MediaRoute2Provider.TAG, "Pending callback not found for control request.");
                    return;
                }
                GroupRouteController.this.mPendingCallbacks.remove(i2);
                if (i == 3) {
                    controlRequestCallback.onResult((Bundle) obj);
                } else {
                    if (i != 4) {
                        return;
                    }
                    controlRequestCallback.onError(peekData == null ? null : peekData.getString(MediaRouteProviderProtocol.SERVICE_DATA_ERROR), (Bundle) obj);
                }
            }
        }
    }
}
