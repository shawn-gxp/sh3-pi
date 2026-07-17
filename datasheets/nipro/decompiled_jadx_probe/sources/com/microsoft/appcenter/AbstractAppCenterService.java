package com.microsoft.appcenter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.microsoft.appcenter.channel.Channel;
import com.microsoft.appcenter.ingestion.models.json.LogFactory;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.HandlerUtils;
import com.microsoft.appcenter.utils.PrefStorageConstants;
import com.microsoft.appcenter.utils.async.AppCenterFuture;
import com.microsoft.appcenter.utils.async.DefaultAppCenterFuture;
import com.microsoft.appcenter.utils.storage.SharedPreferencesManager;
import java.util.Map;

/* loaded from: classes.dex */
public abstract class AbstractAppCenterService implements AppCenterService {
    private static final String PREFERENCE_KEY_SEPARATOR = "_";
    protected Channel mChannel;
    private AppCenterHandler mHandler;

    protected Channel.GroupListener getChannelListener() {
        return null;
    }

    protected abstract String getGroupName();

    @Override // com.microsoft.appcenter.AppCenterService
    public Map<String, LogFactory> getLogFactories() {
        return null;
    }

    protected abstract String getLoggerTag();

    protected int getTriggerCount() {
        return 50;
    }

    protected long getTriggerInterval() {
        return 3000L;
    }

    protected int getTriggerMaxParallelRequests() {
        return 3;
    }

    @Override // com.microsoft.appcenter.AppCenterService
    public boolean isAppSecretRequired() {
        return true;
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityDestroyed(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityPaused(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityResumed(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStarted(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStopped(Activity activity) {
    }

    @Override // com.microsoft.appcenter.AppCenterService
    public void onConfigurationUpdated(String str, String str2) {
    }

    protected synchronized AppCenterFuture<Boolean> isInstanceEnabledAsync() {
        final DefaultAppCenterFuture defaultAppCenterFuture;
        defaultAppCenterFuture = new DefaultAppCenterFuture();
        postAsyncGetter(new Runnable() { // from class: com.microsoft.appcenter.AbstractAppCenterService.1
            @Override // java.lang.Runnable
            public void run() {
                defaultAppCenterFuture.complete(true);
            }
        }, defaultAppCenterFuture, false);
        return defaultAppCenterFuture;
    }

    protected final synchronized AppCenterFuture<Void> setInstanceEnabledAsync(final boolean z) {
        final DefaultAppCenterFuture defaultAppCenterFuture;
        defaultAppCenterFuture = new DefaultAppCenterFuture();
        Runnable runnable = new Runnable() { // from class: com.microsoft.appcenter.AbstractAppCenterService.2
            @Override // java.lang.Runnable
            public void run() {
                AppCenterLog.error("AppCenter", "App Center SDK is disabled.");
                defaultAppCenterFuture.complete(null);
            }
        };
        Runnable runnable2 = new Runnable() { // from class: com.microsoft.appcenter.AbstractAppCenterService.3
            @Override // java.lang.Runnable
            public void run() {
                AbstractAppCenterService.this.setInstanceEnabled(z);
                defaultAppCenterFuture.complete(null);
            }
        };
        if (!post(runnable2, runnable, runnable2)) {
            defaultAppCenterFuture.complete(null);
        }
        return defaultAppCenterFuture;
    }

    @Override // com.microsoft.appcenter.AppCenterService
    public synchronized boolean isInstanceEnabled() {
        return SharedPreferencesManager.getBoolean(getEnabledPreferenceKey(), true);
    }

    @Override // com.microsoft.appcenter.AppCenterService
    public synchronized void setInstanceEnabled(boolean z) {
        if (z == isInstanceEnabled()) {
            String loggerTag = getLoggerTag();
            Object[] objArr = new Object[2];
            objArr[0] = getServiceName();
            objArr[1] = z ? PrefStorageConstants.KEY_ENABLED : "disabled";
            AppCenterLog.info(loggerTag, String.format("%s service has already been %s.", objArr));
            return;
        }
        String groupName = getGroupName();
        if (this.mChannel != null && groupName != null) {
            if (z) {
                this.mChannel.addGroup(groupName, getTriggerCount(), getTriggerInterval(), getTriggerMaxParallelRequests(), null, getChannelListener());
            } else {
                this.mChannel.clear(groupName);
                this.mChannel.removeGroup(groupName);
            }
        }
        SharedPreferencesManager.putBoolean(getEnabledPreferenceKey(), z);
        String loggerTag2 = getLoggerTag();
        Object[] objArr2 = new Object[2];
        objArr2[0] = getServiceName();
        objArr2[1] = z ? PrefStorageConstants.KEY_ENABLED : "disabled";
        AppCenterLog.info(loggerTag2, String.format("%s service has been %s.", objArr2));
        if (this.mChannel != null) {
            applyEnabledState(z);
        }
    }

    protected synchronized void applyEnabledState(boolean z) {
    }

    @Override // com.microsoft.appcenter.AppCenterService
    public final synchronized void onStarting(@NonNull AppCenterHandler appCenterHandler) {
        this.mHandler = appCenterHandler;
    }

    @Override // com.microsoft.appcenter.AppCenterService
    public synchronized void onStarted(@NonNull Context context, @NonNull Channel channel, String str, String str2, boolean z) {
        String groupName = getGroupName();
        boolean isInstanceEnabled = isInstanceEnabled();
        if (groupName != null) {
            channel.removeGroup(groupName);
            if (isInstanceEnabled) {
                channel.addGroup(groupName, getTriggerCount(), getTriggerInterval(), getTriggerMaxParallelRequests(), null, getChannelListener());
            } else {
                channel.clear(groupName);
            }
        }
        this.mChannel = channel;
        applyEnabledState(isInstanceEnabled);
    }

    @NonNull
    protected String getEnabledPreferenceKey() {
        return "enabled_" + getServiceName();
    }

    protected synchronized void post(Runnable runnable) {
        post(runnable, null, null);
    }

    protected synchronized boolean post(final Runnable runnable, Runnable runnable2, final Runnable runnable3) {
        if (this.mHandler == null) {
            AppCenterLog.error("AppCenter", getServiceName() + " needs to be started before it can be used.");
            return false;
        }
        this.mHandler.post(new Runnable() { // from class: com.microsoft.appcenter.AbstractAppCenterService.4
            @Override // java.lang.Runnable
            public void run() {
                if (AbstractAppCenterService.this.isInstanceEnabled()) {
                    runnable.run();
                    return;
                }
                Runnable runnable4 = runnable3;
                if (runnable4 != null) {
                    runnable4.run();
                    return;
                }
                AppCenterLog.info("AppCenter", AbstractAppCenterService.this.getServiceName() + " service disabled, discarding calls.");
            }
        }, runnable2);
        return true;
    }

    protected synchronized <T> void postAsyncGetter(final Runnable runnable, final DefaultAppCenterFuture<T> defaultAppCenterFuture, final T t) {
        Runnable runnable2 = new Runnable() { // from class: com.microsoft.appcenter.AbstractAppCenterService.5
            @Override // java.lang.Runnable
            public void run() {
                defaultAppCenterFuture.complete(t);
            }
        };
        if (!post(new Runnable() { // from class: com.microsoft.appcenter.AbstractAppCenterService.6
            @Override // java.lang.Runnable
            public void run() {
                runnable.run();
            }
        }, runnable2, runnable2)) {
            runnable2.run();
        }
    }

    protected synchronized void postOnUiThread(final Runnable runnable) {
        post(new Runnable() { // from class: com.microsoft.appcenter.AbstractAppCenterService.7
            @Override // java.lang.Runnable
            public void run() {
                HandlerUtils.runOnUiThread(new Runnable() { // from class: com.microsoft.appcenter.AbstractAppCenterService.7.1
                    @Override // java.lang.Runnable
                    public void run() {
                        AnonymousClass7 anonymousClass7 = AnonymousClass7.this;
                        AbstractAppCenterService.this.runIfEnabled(runnable);
                    }
                });
            }
        }, new Runnable() { // from class: com.microsoft.appcenter.AbstractAppCenterService.8
            @Override // java.lang.Runnable
            public void run() {
            }
        }, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void runIfEnabled(Runnable runnable) {
        if (isInstanceEnabled()) {
            runnable.run();
        }
    }
}
