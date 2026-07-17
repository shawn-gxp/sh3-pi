package androidx.mediarouter.media;

import android.media.MediaRouter;
import androidx.annotation.RequiresApi;

@RequiresApi(24)
/* loaded from: classes.dex */
final class MediaRouterApi24 {

    public static final class RouteInfo {
        public static int getDeviceType(Object obj) {
            return ((MediaRouter.RouteInfo) obj).getDeviceType();
        }

        private RouteInfo() {
        }
    }

    private MediaRouterApi24() {
    }
}
