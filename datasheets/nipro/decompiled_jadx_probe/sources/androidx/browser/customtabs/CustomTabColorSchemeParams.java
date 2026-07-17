package androidx.browser.customtabs;

import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

/* loaded from: classes.dex */
public final class CustomTabColorSchemeParams {

    @Nullable
    @ColorInt
    public final Integer navigationBarColor;

    @Nullable
    @ColorInt
    public final Integer navigationBarDividerColor;

    @Nullable
    @ColorInt
    public final Integer secondaryToolbarColor;

    @Nullable
    @ColorInt
    public final Integer toolbarColor;

    CustomTabColorSchemeParams(@Nullable @ColorInt Integer toolbarColor, @Nullable @ColorInt Integer secondaryToolbarColor, @Nullable @ColorInt Integer navigationBarColor, @Nullable @ColorInt Integer navigationBarDividerColor) {
        this.toolbarColor = toolbarColor;
        this.secondaryToolbarColor = secondaryToolbarColor;
        this.navigationBarColor = navigationBarColor;
        this.navigationBarDividerColor = navigationBarDividerColor;
    }

    @NonNull
    Bundle toBundle() {
        Bundle bundle = new Bundle();
        Integer num = this.toolbarColor;
        if (num != null) {
            bundle.putInt(CustomTabsIntent.EXTRA_TOOLBAR_COLOR, num.intValue());
        }
        Integer num2 = this.secondaryToolbarColor;
        if (num2 != null) {
            bundle.putInt(CustomTabsIntent.EXTRA_SECONDARY_TOOLBAR_COLOR, num2.intValue());
        }
        Integer num3 = this.navigationBarColor;
        if (num3 != null) {
            bundle.putInt(CustomTabsIntent.EXTRA_NAVIGATION_BAR_COLOR, num3.intValue());
        }
        Integer num4 = this.navigationBarDividerColor;
        if (num4 != null) {
            bundle.putInt(CustomTabsIntent.EXTRA_NAVIGATION_BAR_DIVIDER_COLOR, num4.intValue());
        }
        return bundle;
    }

    @NonNull
    static CustomTabColorSchemeParams fromBundle(@Nullable Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle(0);
        }
        return new CustomTabColorSchemeParams((Integer) bundle.get(CustomTabsIntent.EXTRA_TOOLBAR_COLOR), (Integer) bundle.get(CustomTabsIntent.EXTRA_SECONDARY_TOOLBAR_COLOR), (Integer) bundle.get(CustomTabsIntent.EXTRA_NAVIGATION_BAR_COLOR), (Integer) bundle.get(CustomTabsIntent.EXTRA_NAVIGATION_BAR_DIVIDER_COLOR));
    }

    @NonNull
    CustomTabColorSchemeParams withDefaults(@NonNull CustomTabColorSchemeParams defaults) {
        Integer num = this.toolbarColor;
        if (num == null) {
            num = defaults.toolbarColor;
        }
        Integer num2 = this.secondaryToolbarColor;
        if (num2 == null) {
            num2 = defaults.secondaryToolbarColor;
        }
        Integer num3 = this.navigationBarColor;
        if (num3 == null) {
            num3 = defaults.navigationBarColor;
        }
        Integer num4 = this.navigationBarDividerColor;
        if (num4 == null) {
            num4 = defaults.navigationBarDividerColor;
        }
        return new CustomTabColorSchemeParams(num, num2, num3, num4);
    }

    public static final class Builder {

        @Nullable
        @ColorInt
        private Integer mNavigationBarColor;

        @Nullable
        @ColorInt
        private Integer mNavigationBarDividerColor;

        @Nullable
        @ColorInt
        private Integer mSecondaryToolbarColor;

        @Nullable
        @ColorInt
        private Integer mToolbarColor;

        @NonNull
        public Builder setToolbarColor(@ColorInt int color) {
            this.mToolbarColor = Integer.valueOf(color | ViewCompat.MEASURED_STATE_MASK);
            return this;
        }

        @NonNull
        public Builder setSecondaryToolbarColor(@ColorInt int color) {
            this.mSecondaryToolbarColor = Integer.valueOf(color);
            return this;
        }

        @NonNull
        public Builder setNavigationBarColor(@ColorInt int color) {
            this.mNavigationBarColor = Integer.valueOf(color | ViewCompat.MEASURED_STATE_MASK);
            return this;
        }

        @NonNull
        public Builder setNavigationBarDividerColor(@ColorInt int color) {
            this.mNavigationBarDividerColor = Integer.valueOf(color);
            return this;
        }

        @NonNull
        public CustomTabColorSchemeParams build() {
            return new CustomTabColorSchemeParams(this.mToolbarColor, this.mSecondaryToolbarColor, this.mNavigationBarColor, this.mNavigationBarDividerColor);
        }
    }
}
