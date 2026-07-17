package androidx.browser.browseractions;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated
/* loaded from: classes.dex */
public class BrowserActionsIntent {
    public static final String ACTION_BROWSER_ACTIONS_OPEN = "androidx.browser.browseractions.browser_action_open";
    public static final String EXTRA_APP_ID = "androidx.browser.browseractions.APP_ID";
    public static final String EXTRA_MENU_ITEMS = "androidx.browser.browseractions.extra.MENU_ITEMS";
    public static final String EXTRA_SELECTED_ACTION_PENDING_INTENT = "androidx.browser.browseractions.extra.SELECTED_ACTION_PENDING_INTENT";
    public static final String EXTRA_TYPE = "androidx.browser.browseractions.extra.TYPE";
    public static final int ITEM_COPY = 3;
    public static final int ITEM_DOWNLOAD = 2;
    public static final int ITEM_INVALID_ITEM = -1;
    public static final int ITEM_OPEN_IN_INCOGNITO = 1;
    public static final int ITEM_OPEN_IN_NEW_TAB = 0;
    public static final int ITEM_SHARE = 4;
    public static final String KEY_ACTION = "androidx.browser.browseractions.ACTION";
    public static final String KEY_ICON_ID = "androidx.browser.browseractions.ICON_ID";
    private static final String KEY_ICON_URI = "androidx.browser.browseractions.ICON_URI";
    public static final String KEY_TITLE = "androidx.browser.browseractions.TITLE";

    @SuppressLint({"MinMaxConstant"})
    public static final int MAX_CUSTOM_ITEMS = 5;
    private static final String TAG = "BrowserActions";
    private static final String TEST_URL = "https://www.example.com";
    public static final int URL_TYPE_AUDIO = 3;
    public static final int URL_TYPE_FILE = 4;
    public static final int URL_TYPE_IMAGE = 1;
    public static final int URL_TYPE_NONE = 0;
    public static final int URL_TYPE_PLUGIN = 5;
    public static final int URL_TYPE_VIDEO = 2;

    @Nullable
    private static BrowserActionsFallDialogListener sDialogListenter;

    @NonNull
    private final Intent mIntent;

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    @VisibleForTesting
    interface BrowserActionsFallDialogListener {
        void onDialogShown();
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public @interface BrowserActionsItemId {
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public @interface BrowserActionsUrlType {
    }

    @NonNull
    public Intent getIntent() {
        return this.mIntent;
    }

    BrowserActionsIntent(@NonNull Intent intent) {
        this.mIntent = intent;
    }

    public static final class Builder {
        private Context mContext;
        private Uri mUri;
        private final Intent mIntent = new Intent(BrowserActionsIntent.ACTION_BROWSER_ACTIONS_OPEN);
        private int mType = 0;
        private ArrayList<Bundle> mMenuItems = new ArrayList<>();

        @Nullable
        private PendingIntent mOnItemSelectedPendingIntent = null;
        private List<Uri> mImageUris = new ArrayList();

        public Builder(@NonNull Context context, @NonNull Uri uri) {
            this.mContext = context;
            this.mUri = uri;
        }

        @NonNull
        public Builder setUrlType(int type) {
            this.mType = type;
            return this;
        }

        @NonNull
        public Builder setCustomItems(@NonNull ArrayList<BrowserActionItem> items) {
            if (items.size() > 5) {
                throw new IllegalStateException("Exceeded maximum toolbar item count of 5");
            }
            for (int i = 0; i < items.size(); i++) {
                if (TextUtils.isEmpty(items.get(i).getTitle()) || items.get(i).getAction() == null) {
                    throw new IllegalArgumentException("Custom item should contain a non-empty title and non-null intent.");
                }
                this.mMenuItems.add(getBundleFromItem(items.get(i)));
                if (items.get(i).getIconUri() != null) {
                    this.mImageUris.add(items.get(i).getIconUri());
                }
            }
            return this;
        }

        @NonNull
        public Builder setCustomItems(@NonNull BrowserActionItem... items) {
            return setCustomItems(new ArrayList<>(Arrays.asList(items)));
        }

        @NonNull
        public Builder setOnItemSelectedAction(@NonNull PendingIntent onItemSelectedPendingIntent) {
            this.mOnItemSelectedPendingIntent = onItemSelectedPendingIntent;
            return this;
        }

        @NonNull
        private Bundle getBundleFromItem(@NonNull BrowserActionItem item) {
            Bundle bundle = new Bundle();
            bundle.putString(BrowserActionsIntent.KEY_TITLE, item.getTitle());
            bundle.putParcelable(BrowserActionsIntent.KEY_ACTION, item.getAction());
            if (item.getIconId() != 0) {
                bundle.putInt(BrowserActionsIntent.KEY_ICON_ID, item.getIconId());
            }
            if (item.getIconUri() != null) {
                bundle.putParcelable(BrowserActionsIntent.KEY_ICON_URI, item.getIconUri());
            }
            return bundle;
        }

        @NonNull
        public BrowserActionsIntent build() {
            this.mIntent.setData(this.mUri);
            this.mIntent.putExtra(BrowserActionsIntent.EXTRA_TYPE, this.mType);
            this.mIntent.putParcelableArrayListExtra(BrowserActionsIntent.EXTRA_MENU_ITEMS, this.mMenuItems);
            this.mIntent.putExtra(BrowserActionsIntent.EXTRA_APP_ID, PendingIntent.getActivity(this.mContext, 0, new Intent(), 0));
            PendingIntent pendingIntent = this.mOnItemSelectedPendingIntent;
            if (pendingIntent != null) {
                this.mIntent.putExtra(BrowserActionsIntent.EXTRA_SELECTED_ACTION_PENDING_INTENT, pendingIntent);
            }
            BrowserServiceFileProvider.grantReadPermission(this.mIntent, this.mImageUris, this.mContext);
            return new BrowserActionsIntent(this.mIntent);
        }
    }

    public static void openBrowserAction(@NonNull Context context, @NonNull Uri uri) {
        launchIntent(context, new Builder(context, uri).build().getIntent());
    }

    public static void openBrowserAction(@NonNull Context context, @NonNull Uri uri, int type, @NonNull ArrayList<BrowserActionItem> items, @NonNull PendingIntent pendingIntent) {
        launchIntent(context, new Builder(context, uri).setUrlType(type).setCustomItems(items).setOnItemSelectedAction(pendingIntent).build().getIntent());
    }

    public static void launchIntent(@NonNull Context context, @NonNull Intent intent) {
        launchIntent(context, intent, getBrowserActionsIntentHandlers(context));
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    @VisibleForTesting
    static void launchIntent(Context context, Intent intent, List<ResolveInfo> handlers) {
        if (handlers == null || handlers.size() == 0) {
            openFallbackBrowserActionsMenu(context, intent);
            return;
        }
        int i = 0;
        if (handlers.size() == 1) {
            intent.setPackage(handlers.get(0).activityInfo.packageName);
        } else {
            ResolveInfo resolveActivity = context.getPackageManager().resolveActivity(new Intent("android.intent.action.VIEW", Uri.parse(TEST_URL)), 65536);
            if (resolveActivity != null) {
                String str = resolveActivity.activityInfo.packageName;
                while (true) {
                    if (i >= handlers.size()) {
                        break;
                    }
                    if (str.equals(handlers.get(i).activityInfo.packageName)) {
                        intent.setPackage(str);
                        break;
                    }
                    i++;
                }
            }
        }
        ContextCompat.startActivity(context, intent, null);
    }

    @NonNull
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static List<ResolveInfo> getBrowserActionsIntentHandlers(@NonNull Context context) {
        return context.getPackageManager().queryIntentActivities(new Intent(ACTION_BROWSER_ACTIONS_OPEN, Uri.parse(TEST_URL)), 131072);
    }

    private static void openFallbackBrowserActionsMenu(Context context, Intent intent) {
        Uri data = intent.getData();
        ArrayList parcelableArrayListExtra = intent.getParcelableArrayListExtra(EXTRA_MENU_ITEMS);
        openFallbackBrowserActionsMenu(context, data, parcelableArrayListExtra != null ? parseBrowserActionItems(parcelableArrayListExtra) : null);
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    @VisibleForTesting
    static void setDialogShownListenter(BrowserActionsFallDialogListener dialogListener) {
        sDialogListenter = dialogListener;
    }

    private static void openFallbackBrowserActionsMenu(Context context, Uri uri, List<BrowserActionItem> menuItems) {
        new BrowserActionsFallbackMenuUi(context, uri, menuItems).displayMenu();
        BrowserActionsFallDialogListener browserActionsFallDialogListener = sDialogListenter;
        if (browserActionsFallDialogListener != null) {
            browserActionsFallDialogListener.onDialogShown();
        }
    }

    @NonNull
    public static List<BrowserActionItem> parseBrowserActionItems(@NonNull ArrayList<Bundle> bundles) {
        BrowserActionItem browserActionItem;
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < bundles.size(); i++) {
            Bundle bundle = bundles.get(i);
            String string = bundle.getString(KEY_TITLE);
            PendingIntent pendingIntent = (PendingIntent) bundle.getParcelable(KEY_ACTION);
            int i2 = bundle.getInt(KEY_ICON_ID);
            Uri uri = (Uri) bundle.getParcelable(KEY_ICON_URI);
            if (!TextUtils.isEmpty(string) && pendingIntent != null) {
                if (i2 != 0) {
                    browserActionItem = new BrowserActionItem(string, pendingIntent, i2);
                } else {
                    browserActionItem = new BrowserActionItem(string, pendingIntent, uri);
                }
                arrayList.add(browserActionItem);
            } else {
                throw new IllegalArgumentException("Custom item should contain a non-empty title and non-null intent.");
            }
        }
        return arrayList;
    }

    @Nullable
    public static String getUntrustedCreatorPackageName(@NonNull Intent intent) {
        PendingIntent pendingIntent = (PendingIntent) intent.getParcelableExtra(EXTRA_APP_ID);
        if (pendingIntent == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= 17) {
            return pendingIntent.getCreatorPackage();
        }
        return pendingIntent.getTargetPackage();
    }

    @Nullable
    @Deprecated
    public static String getCreatorPackageName(@NonNull Intent intent) {
        return getUntrustedCreatorPackageName(intent);
    }
}
