package androidx.mediarouter.app;

import androidx.annotation.NonNull;

/* loaded from: classes.dex */
public class MediaRouteDialogFactory {
    private static final MediaRouteDialogFactory sDefault = new MediaRouteDialogFactory();

    @NonNull
    public static MediaRouteDialogFactory getDefault() {
        return sDefault;
    }

    @NonNull
    public MediaRouteChooserDialogFragment onCreateChooserDialogFragment() {
        return new MediaRouteChooserDialogFragment();
    }

    @NonNull
    public MediaRouteControllerDialogFragment onCreateControllerDialogFragment() {
        return new MediaRouteControllerDialogFragment();
    }
}
