package androidx.mediarouter.media;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* loaded from: classes.dex */
public class MediaRouterParams {
    public static final int DIALOG_TYPE_DEFAULT = 1;
    public static final int DIALOG_TYPE_DYNAMIC_GROUP = 2;

    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static final String EXTRAS_KEY_FIXED_CAST_ICON = "androidx.mediarouter.media.MediaRouterParams.FIXED_CAST_ICON";

    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static final String EXTRAS_KEY_TEST_PRIVATE_UI = "androidx.mediarouter.media.MediaRouterParams.TEST_PRIVATE_UI";
    final int mDialogType;
    final Bundle mExtras;
    final boolean mOutputSwitcherEnabled;
    final boolean mTransferToLocalEnabled;

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public @interface DialogType {
    }

    MediaRouterParams(@NonNull Builder builder) {
        this.mDialogType = builder.mDialogType;
        this.mOutputSwitcherEnabled = builder.mOutputSwitcherEnabled;
        this.mTransferToLocalEnabled = builder.mTransferToLocalEnabled;
        Bundle bundle = builder.mExtras;
        this.mExtras = bundle == null ? Bundle.EMPTY : new Bundle(bundle);
    }

    public int getDialogType() {
        return this.mDialogType;
    }

    public boolean isOutputSwitcherEnabled() {
        return this.mOutputSwitcherEnabled;
    }

    public boolean isTransferToLocalEnabled() {
        return this.mTransferToLocalEnabled;
    }

    @NonNull
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public Bundle getExtras() {
        return this.mExtras;
    }

    public static final class Builder {
        int mDialogType;
        Bundle mExtras;
        boolean mOutputSwitcherEnabled;
        boolean mTransferToLocalEnabled;

        public Builder() {
            this.mDialogType = 1;
        }

        public Builder(@NonNull MediaRouterParams mediaRouterParams) {
            this.mDialogType = 1;
            if (mediaRouterParams == null) {
                throw new NullPointerException("params should not be null!");
            }
            this.mDialogType = mediaRouterParams.mDialogType;
            this.mOutputSwitcherEnabled = mediaRouterParams.mOutputSwitcherEnabled;
            this.mTransferToLocalEnabled = mediaRouterParams.mTransferToLocalEnabled;
            this.mExtras = mediaRouterParams.mExtras == null ? null : new Bundle(mediaRouterParams.mExtras);
        }

        @NonNull
        public Builder setDialogType(int i) {
            this.mDialogType = i;
            return this;
        }

        @NonNull
        public Builder setOutputSwitcherEnabled(boolean z) {
            if (Build.VERSION.SDK_INT >= 30) {
                this.mOutputSwitcherEnabled = z;
            }
            return this;
        }

        @NonNull
        public Builder setTransferToLocalEnabled(boolean z) {
            if (Build.VERSION.SDK_INT >= 30) {
                this.mTransferToLocalEnabled = z;
            }
            return this;
        }

        @NonNull
        @RestrictTo({RestrictTo.Scope.LIBRARY})
        public Builder setExtras(@Nullable Bundle bundle) {
            this.mExtras = bundle == null ? null : new Bundle(bundle);
            return this;
        }

        @NonNull
        public MediaRouterParams build() {
            return new MediaRouterParams(this);
        }
    }
}
