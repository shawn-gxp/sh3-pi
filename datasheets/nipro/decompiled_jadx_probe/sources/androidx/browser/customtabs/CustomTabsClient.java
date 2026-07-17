package androidx.browser.customtabs;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.customtabs.ICustomTabsCallback;
import android.support.customtabs.ICustomTabsService;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.browser.customtabs.CustomTabsSession;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class CustomTabsClient {
    private static final String TAG = "CustomTabsClient";
    private final Context mApplicationContext;
    private final ICustomTabsService mService;
    private final ComponentName mServiceComponentName;

    CustomTabsClient(ICustomTabsService service, ComponentName componentName, Context applicationContext) {
        this.mService = service;
        this.mServiceComponentName = componentName;
        this.mApplicationContext = applicationContext;
    }

    public static boolean bindCustomTabsService(@NonNull Context context, @Nullable String packageName, @NonNull CustomTabsServiceConnection connection) {
        connection.setApplicationContext(context.getApplicationContext());
        Intent intent = new Intent(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
        if (!TextUtils.isEmpty(packageName)) {
            intent.setPackage(packageName);
        }
        return context.bindService(intent, connection, 33);
    }

    public static boolean bindCustomTabsServicePreservePriority(@NonNull Context context, @Nullable String packageName, @NonNull CustomTabsServiceConnection connection) {
        connection.setApplicationContext(context.getApplicationContext());
        Intent intent = new Intent(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
        if (!TextUtils.isEmpty(packageName)) {
            intent.setPackage(packageName);
        }
        return context.bindService(intent, connection, 1);
    }

    @Nullable
    public static String getPackageName(@NonNull Context context, @Nullable List<String> packages) {
        return getPackageName(context, packages, false);
    }

    @Nullable
    public static String getPackageName(@NonNull Context context, @Nullable List<String> packages, boolean ignoreDefault) {
        ResolveInfo resolveActivity;
        PackageManager packageManager = context.getPackageManager();
        List<String> arrayList = packages == null ? new ArrayList<>() : packages;
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://"));
        if (!ignoreDefault && (resolveActivity = packageManager.resolveActivity(intent, 0)) != null) {
            String str = resolveActivity.activityInfo.packageName;
            ArrayList arrayList2 = new ArrayList(arrayList.size() + 1);
            arrayList2.add(str);
            if (packages != null) {
                arrayList2.addAll(packages);
            }
            arrayList = arrayList2;
        }
        Intent intent2 = new Intent(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
        for (String str2 : arrayList) {
            intent2.setPackage(str2);
            if (packageManager.resolveService(intent2, 0) != null) {
                return str2;
            }
        }
        if (Build.VERSION.SDK_INT < 30) {
            return null;
        }
        Log.w(TAG, "Unable to find any Custom Tabs packages, you may need to add a <queries> element to your manifest. See the docs for CustomTabsClient#getPackageName.");
        return null;
    }

    public static boolean connectAndInitialize(@NonNull Context context, @NonNull String packageName) {
        if (packageName == null) {
            return false;
        }
        final Context applicationContext = context.getApplicationContext();
        try {
            return bindCustomTabsService(applicationContext, packageName, new CustomTabsServiceConnection() { // from class: androidx.browser.customtabs.CustomTabsClient.1
                @Override // android.content.ServiceConnection
                public void onServiceDisconnected(ComponentName componentName) {
                }

                @Override // androidx.browser.customtabs.CustomTabsServiceConnection
                public final void onCustomTabsServiceConnected(@NonNull ComponentName name, @NonNull CustomTabsClient client) {
                    client.warmup(0L);
                    applicationContext.unbindService(this);
                }
            });
        } catch (SecurityException unused) {
            return false;
        }
    }

    public boolean warmup(long flags) {
        try {
            return this.mService.warmup(flags);
        } catch (RemoteException unused) {
            return false;
        }
    }

    private static PendingIntent createSessionId(Context context, int sessionId) {
        return PendingIntent.getActivity(context, sessionId, new Intent(), 0);
    }

    @Nullable
    public CustomTabsSession newSession(@Nullable final CustomTabsCallback callback) {
        return newSessionInternal(callback, null);
    }

    @Nullable
    public CustomTabsSession newSession(@Nullable final CustomTabsCallback callback, int id) {
        return newSessionInternal(callback, createSessionId(this.mApplicationContext, id));
    }

    @NonNull
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static CustomTabsSession.PendingSession newPendingSession(@NonNull Context context, @Nullable final CustomTabsCallback callback, int id) {
        return new CustomTabsSession.PendingSession(callback, createSessionId(context, id));
    }

    @Nullable
    private CustomTabsSession newSessionInternal(@Nullable final CustomTabsCallback callback, @Nullable PendingIntent sessionId) {
        boolean newSession;
        ICustomTabsCallback.Stub createCallbackWrapper = createCallbackWrapper(callback);
        try {
            if (sessionId != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(CustomTabsIntent.EXTRA_SESSION_ID, sessionId);
                newSession = this.mService.newSessionWithExtras(createCallbackWrapper, bundle);
            } else {
                newSession = this.mService.newSession(createCallbackWrapper);
            }
            if (newSession) {
                return new CustomTabsSession(this.mService, createCallbackWrapper, this.mServiceComponentName, sessionId);
            }
            return null;
        } catch (RemoteException unused) {
            return null;
        }
    }

    @Nullable
    public Bundle extraCommand(@NonNull String commandName, @Nullable Bundle args) {
        try {
            return this.mService.extraCommand(commandName, args);
        } catch (RemoteException unused) {
            return null;
        }
    }

    private ICustomTabsCallback.Stub createCallbackWrapper(@Nullable final CustomTabsCallback callback) {
        return new ICustomTabsCallback.Stub() { // from class: androidx.browser.customtabs.CustomTabsClient.2
            private Handler mHandler = new Handler(Looper.getMainLooper());

            @Override // android.support.customtabs.ICustomTabsCallback
            public void onNavigationEvent(final int navigationEvent, final Bundle extras) {
                if (callback == null) {
                    return;
                }
                this.mHandler.post(new Runnable() { // from class: androidx.browser.customtabs.CustomTabsClient.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        callback.onNavigationEvent(navigationEvent, extras);
                    }
                });
            }

            @Override // android.support.customtabs.ICustomTabsCallback
            public void extraCallback(final String callbackName, final Bundle args) throws RemoteException {
                if (callback == null) {
                    return;
                }
                this.mHandler.post(new Runnable() { // from class: androidx.browser.customtabs.CustomTabsClient.2.2
                    @Override // java.lang.Runnable
                    public void run() {
                        callback.extraCallback(callbackName, args);
                    }
                });
            }

            @Override // android.support.customtabs.ICustomTabsCallback
            public Bundle extraCallbackWithResult(@NonNull String callbackName, @Nullable Bundle args) throws RemoteException {
                CustomTabsCallback customTabsCallback = callback;
                if (customTabsCallback == null) {
                    return null;
                }
                return customTabsCallback.extraCallbackWithResult(callbackName, args);
            }

            @Override // android.support.customtabs.ICustomTabsCallback
            public void onMessageChannelReady(final Bundle extras) throws RemoteException {
                if (callback == null) {
                    return;
                }
                this.mHandler.post(new Runnable() { // from class: androidx.browser.customtabs.CustomTabsClient.2.3
                    @Override // java.lang.Runnable
                    public void run() {
                        callback.onMessageChannelReady(extras);
                    }
                });
            }

            @Override // android.support.customtabs.ICustomTabsCallback
            public void onPostMessage(final String message, final Bundle extras) throws RemoteException {
                if (callback == null) {
                    return;
                }
                this.mHandler.post(new Runnable() { // from class: androidx.browser.customtabs.CustomTabsClient.2.4
                    @Override // java.lang.Runnable
                    public void run() {
                        callback.onPostMessage(message, extras);
                    }
                });
            }

            @Override // android.support.customtabs.ICustomTabsCallback
            public void onRelationshipValidationResult(final int relation, final Uri requestedOrigin, final boolean result, @Nullable final Bundle extras) throws RemoteException {
                if (callback == null) {
                    return;
                }
                this.mHandler.post(new Runnable() { // from class: androidx.browser.customtabs.CustomTabsClient.2.5
                    @Override // java.lang.Runnable
                    public void run() {
                        callback.onRelationshipValidationResult(relation, requestedOrigin, result, extras);
                    }
                });
            }
        };
    }

    @Nullable
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public CustomTabsSession attachSession(@NonNull CustomTabsSession.PendingSession session) {
        return newSessionInternal(session.getCallback(), session.getId());
    }
}
