package com.microsoft.appcenter.analytics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import com.microsoft.appcenter.AbstractAppCenterService;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.Flags;
import com.microsoft.appcenter.analytics.channel.AnalyticsListener;
import com.microsoft.appcenter.analytics.channel.AnalyticsValidator;
import com.microsoft.appcenter.analytics.channel.SessionTracker;
import com.microsoft.appcenter.analytics.ingestion.models.EventLog;
import com.microsoft.appcenter.analytics.ingestion.models.PageLog;
import com.microsoft.appcenter.analytics.ingestion.models.StartSessionLog;
import com.microsoft.appcenter.analytics.ingestion.models.json.EventLogFactory;
import com.microsoft.appcenter.analytics.ingestion.models.json.PageLogFactory;
import com.microsoft.appcenter.analytics.ingestion.models.json.StartSessionLogFactory;
import com.microsoft.appcenter.analytics.ingestion.models.one.CommonSchemaEventLog;
import com.microsoft.appcenter.analytics.ingestion.models.one.json.CommonSchemaEventLogFactory;
import com.microsoft.appcenter.channel.Channel;
import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.json.LogFactory;
import com.microsoft.appcenter.ingestion.models.properties.StringTypedProperty;
import com.microsoft.appcenter.ingestion.models.properties.TypedProperty;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.UUIDUtils;
import com.microsoft.appcenter.utils.async.AppCenterFuture;
import com.microsoft.appcenter.utils.async.DefaultAppCenterFuture;
import com.microsoft.appcenter.utils.context.UserIdContext;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class Analytics extends AbstractAppCenterService {
    private static final String ACTIVITY_SUFFIX = "Activity";
    static final String ANALYTICS_CRITICAL_GROUP = "group_analytics_critical";
    static final String ANALYTICS_GROUP = "group_analytics";
    public static final String LOG_TAG = "AppCenterAnalytics";

    @VisibleForTesting
    static final int MAXIMUM_TRANSMISSION_INTERVAL_IN_SECONDS = 86400;

    @VisibleForTesting
    static final int MINIMUM_TRANSMISSION_INTERVAL_IN_SECONDS = 3;
    private static final String SERVICE_NAME = "Analytics";

    @SuppressLint({"StaticFieldLeak"})
    private static Analytics sInstance;
    private AnalyticsListener mAnalyticsListener;
    private Channel.Listener mAnalyticsTransmissionTargetListener;
    private AnalyticsValidator mAnalyticsValidator;
    private boolean mAutoPageTrackingEnabled = false;
    private Context mContext;
    private WeakReference<Activity> mCurrentActivity;

    @VisibleForTesting
    AnalyticsTransmissionTarget mDefaultTransmissionTarget;
    private final Map<String, LogFactory> mFactories;
    private SessionTracker mSessionTracker;
    private boolean mStartedFromApp;
    private long mTransmissionInterval;
    private final Map<String, AnalyticsTransmissionTarget> mTransmissionTargets;

    @Override // com.microsoft.appcenter.AbstractAppCenterService
    protected String getGroupName() {
        return ANALYTICS_GROUP;
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService
    protected String getLoggerTag() {
        return LOG_TAG;
    }

    @Override // com.microsoft.appcenter.AppCenterService
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService, com.microsoft.appcenter.AppCenterService
    public boolean isAppSecretRequired() {
        return false;
    }

    private Analytics() {
        HashMap hashMap = new HashMap();
        this.mFactories = hashMap;
        hashMap.put(StartSessionLog.TYPE, new StartSessionLogFactory());
        this.mFactories.put(PageLog.TYPE, new PageLogFactory());
        this.mFactories.put("event", new EventLogFactory());
        this.mFactories.put(CommonSchemaEventLog.TYPE, new CommonSchemaEventLogFactory());
        this.mTransmissionTargets = new HashMap();
        this.mTransmissionInterval = TimeUnit.SECONDS.toMillis(3L);
    }

    public static synchronized Analytics getInstance() {
        Analytics analytics;
        synchronized (Analytics.class) {
            if (sInstance == null) {
                sInstance = new Analytics();
            }
            analytics = sInstance;
        }
        return analytics;
    }

    @VisibleForTesting
    static synchronized void unsetInstance() {
        synchronized (Analytics.class) {
            sInstance = null;
        }
    }

    public static AppCenterFuture<Boolean> isEnabled() {
        return getInstance().isInstanceEnabledAsync();
    }

    public static AppCenterFuture<Void> setEnabled(boolean z) {
        return getInstance().setInstanceEnabledAsync(z);
    }

    public static boolean setTransmissionInterval(int i) {
        return getInstance().setInstanceTransmissionInterval(i);
    }

    public static void pause() {
        getInstance().pauseInstanceAsync();
    }

    public static void resume() {
        getInstance().resumeInstanceAsync();
    }

    @VisibleForTesting
    protected static void setListener(AnalyticsListener analyticsListener) {
        getInstance().setInstanceListener(analyticsListener);
    }

    protected static boolean isAutoPageTrackingEnabled() {
        return getInstance().isInstanceAutoPageTrackingEnabled();
    }

    protected static void setAutoPageTrackingEnabled(boolean z) {
        getInstance().setInstanceAutoPageTrackingEnabled(z);
    }

    protected static void trackPage(String str) {
        trackPage(str, null);
    }

    protected static void trackPage(String str, Map<String, String> map) {
        getInstance().trackPageAsync(str, map);
    }

    public static void trackEvent(String str) {
        trackEvent(str, null, null, 1);
    }

    public static void trackEvent(String str, Map<String, String> map) {
        getInstance().trackEventAsync(str, convertProperties(map), null, 1);
    }

    public static void trackEvent(String str, Map<String, String> map, int i) {
        getInstance().trackEventAsync(str, convertProperties(map), null, i);
    }

    public static void trackEvent(String str, EventProperties eventProperties) {
        trackEvent(str, eventProperties, 1);
    }

    public static void trackEvent(String str, EventProperties eventProperties, int i) {
        trackEvent(str, eventProperties, null, i);
    }

    static void trackEvent(String str, EventProperties eventProperties, AnalyticsTransmissionTarget analyticsTransmissionTarget, int i) {
        getInstance().trackEventAsync(str, convertProperties(eventProperties), analyticsTransmissionTarget, i);
    }

    private static List<TypedProperty> convertProperties(EventProperties eventProperties) {
        if (eventProperties == null) {
            return null;
        }
        return new ArrayList(eventProperties.getProperties().values());
    }

    private static List<TypedProperty> convertProperties(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            StringTypedProperty stringTypedProperty = new StringTypedProperty();
            stringTypedProperty.setName(entry.getKey());
            stringTypedProperty.setValue(entry.getValue());
            arrayList.add(stringTypedProperty);
        }
        return arrayList;
    }

    public static AnalyticsTransmissionTarget getTransmissionTarget(String str) {
        return getInstance().getInstanceTransmissionTarget(str);
    }

    private static String generatePageName(Class<?> cls) {
        String simpleName = cls.getSimpleName();
        return (!simpleName.endsWith(ACTIVITY_SUFFIX) || simpleName.length() <= 8) ? simpleName : simpleName.substring(0, simpleName.length() - 8);
    }

    private synchronized AnalyticsTransmissionTarget getInstanceTransmissionTarget(String str) {
        if (str != null) {
            if (!str.isEmpty()) {
                if (!AppCenter.isConfigured()) {
                    AppCenterLog.error(LOG_TAG, "Cannot create transmission target, AppCenter is not configured or started.");
                    return null;
                }
                AnalyticsTransmissionTarget analyticsTransmissionTarget = this.mTransmissionTargets.get(str);
                if (analyticsTransmissionTarget != null) {
                    AppCenterLog.debug(LOG_TAG, "Returning transmission target found with token " + str);
                    return analyticsTransmissionTarget;
                }
                AnalyticsTransmissionTarget createAnalyticsTransmissionTarget = createAnalyticsTransmissionTarget(str);
                this.mTransmissionTargets.put(str, createAnalyticsTransmissionTarget);
                return createAnalyticsTransmissionTarget;
            }
        }
        AppCenterLog.error(LOG_TAG, "Transmission target token may not be null or empty.");
        return null;
    }

    private AnalyticsTransmissionTarget createAnalyticsTransmissionTarget(String str) {
        final AnalyticsTransmissionTarget analyticsTransmissionTarget = new AnalyticsTransmissionTarget(str, null);
        AppCenterLog.debug(LOG_TAG, "Created transmission target with token " + str);
        postCommandEvenIfDisabled(new Runnable() { // from class: com.microsoft.appcenter.analytics.Analytics.1
            @Override // java.lang.Runnable
            public void run() {
                analyticsTransmissionTarget.initInBackground(Analytics.this.mContext, ((AbstractAppCenterService) Analytics.this).mChannel);
            }
        });
        return analyticsTransmissionTarget;
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService, com.microsoft.appcenter.AppCenterService
    public Map<String, LogFactory> getLogFactories() {
        return this.mFactories;
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService, android.app.Application.ActivityLifecycleCallbacks
    public synchronized void onActivityResumed(final Activity activity) {
        final Runnable runnable = new Runnable() { // from class: com.microsoft.appcenter.analytics.Analytics.2
            @Override // java.lang.Runnable
            public void run() {
                Analytics.this.mCurrentActivity = new WeakReference(activity);
            }
        };
        post(new Runnable() { // from class: com.microsoft.appcenter.analytics.Analytics.3
            @Override // java.lang.Runnable
            public void run() {
                runnable.run();
                Analytics.this.processOnResume(activity);
            }
        }, runnable, runnable);
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService
    protected long getTriggerInterval() {
        return this.mTransmissionInterval;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void processOnResume(Activity activity) {
        SessionTracker sessionTracker = this.mSessionTracker;
        if (sessionTracker != null) {
            sessionTracker.onActivityResumed();
            if (this.mAutoPageTrackingEnabled) {
                queuePage(generatePageName(activity.getClass()), null);
            }
        }
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService, android.app.Application.ActivityLifecycleCallbacks
    public synchronized void onActivityPaused(Activity activity) {
        final Runnable runnable = new Runnable() { // from class: com.microsoft.appcenter.analytics.Analytics.4
            @Override // java.lang.Runnable
            public void run() {
                Analytics.this.mCurrentActivity = null;
            }
        };
        post(new Runnable() { // from class: com.microsoft.appcenter.analytics.Analytics.5
            @Override // java.lang.Runnable
            public void run() {
                runnable.run();
                if (Analytics.this.mSessionTracker != null) {
                    Analytics.this.mSessionTracker.onActivityPaused();
                }
            }
        }, runnable, runnable);
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService
    protected Channel.GroupListener getChannelListener() {
        return new Channel.GroupListener() { // from class: com.microsoft.appcenter.analytics.Analytics.6
            @Override // com.microsoft.appcenter.channel.Channel.GroupListener
            public void onBeforeSending(Log log) {
                if (Analytics.this.mAnalyticsListener != null) {
                    Analytics.this.mAnalyticsListener.onBeforeSending(log);
                }
            }

            @Override // com.microsoft.appcenter.channel.Channel.GroupListener
            public void onSuccess(Log log) {
                if (Analytics.this.mAnalyticsListener != null) {
                    Analytics.this.mAnalyticsListener.onSendingSucceeded(log);
                }
            }

            @Override // com.microsoft.appcenter.channel.Channel.GroupListener
            public void onFailure(Log log, Exception exc) {
                if (Analytics.this.mAnalyticsListener != null) {
                    Analytics.this.mAnalyticsListener.onSendingFailed(log, exc);
                }
            }
        };
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService
    protected synchronized void applyEnabledState(boolean z) {
        if (z) {
            this.mChannel.addGroup(ANALYTICS_CRITICAL_GROUP, getTriggerCount(), 3000L, getTriggerMaxParallelRequests(), null, getChannelListener());
            startAppLevelFeatures();
        } else {
            this.mChannel.removeGroup(ANALYTICS_CRITICAL_GROUP);
            if (this.mAnalyticsValidator != null) {
                this.mChannel.removeListener(this.mAnalyticsValidator);
                this.mAnalyticsValidator = null;
            }
            if (this.mSessionTracker != null) {
                this.mChannel.removeListener(this.mSessionTracker);
                this.mSessionTracker.clearSessions();
                this.mSessionTracker = null;
            }
            if (this.mAnalyticsTransmissionTargetListener != null) {
                this.mChannel.removeListener(this.mAnalyticsTransmissionTargetListener);
                this.mAnalyticsTransmissionTargetListener = null;
            }
        }
    }

    @WorkerThread
    private void startAppLevelFeatures() {
        Activity activity;
        if (this.mStartedFromApp) {
            AnalyticsValidator analyticsValidator = new AnalyticsValidator();
            this.mAnalyticsValidator = analyticsValidator;
            this.mChannel.addListener(analyticsValidator);
            SessionTracker sessionTracker = new SessionTracker(this.mChannel, ANALYTICS_GROUP);
            this.mSessionTracker = sessionTracker;
            this.mChannel.addListener(sessionTracker);
            WeakReference<Activity> weakReference = this.mCurrentActivity;
            if (weakReference != null && (activity = weakReference.get()) != null) {
                processOnResume(activity);
            }
            Channel.Listener channelListener = AnalyticsTransmissionTarget.getChannelListener();
            this.mAnalyticsTransmissionTargetListener = channelListener;
            this.mChannel.addListener(channelListener);
        }
    }

    private synchronized void trackPageAsync(final String str, Map<String, String> map) {
        final HashMap hashMap;
        if (map != null) {
            try {
                hashMap = new HashMap(map);
            } catch (Throwable th) {
                throw th;
            }
        } else {
            hashMap = null;
        }
        post(new Runnable() { // from class: com.microsoft.appcenter.analytics.Analytics.7
            @Override // java.lang.Runnable
            public void run() {
                if (Analytics.this.mStartedFromApp) {
                    Analytics.this.queuePage(str, hashMap);
                } else {
                    AppCenterLog.error(Analytics.LOG_TAG, "Cannot track page if not started from app.");
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    @WorkerThread
    public void queuePage(String str, Map<String, String> map) {
        PageLog pageLog = new PageLog();
        pageLog.setName(str);
        pageLog.setProperties(map);
        this.mChannel.enqueue(pageLog, ANALYTICS_GROUP, 1);
    }

    private synchronized void trackEventAsync(final String str, final List<TypedProperty> list, final AnalyticsTransmissionTarget analyticsTransmissionTarget, final int i) {
        final String userId = UserIdContext.getInstance().getUserId();
        post(new Runnable() { // from class: com.microsoft.appcenter.analytics.Analytics.8
            @Override // java.lang.Runnable
            public void run() {
                AnalyticsTransmissionTarget analyticsTransmissionTarget2 = analyticsTransmissionTarget;
                if (analyticsTransmissionTarget2 == null) {
                    analyticsTransmissionTarget2 = Analytics.this.mDefaultTransmissionTarget;
                }
                EventLog eventLog = new EventLog();
                if (analyticsTransmissionTarget2 == null) {
                    if (!Analytics.this.mStartedFromApp) {
                        AppCenterLog.error(Analytics.LOG_TAG, "Cannot track event using Analytics.trackEvent if not started from app, please start from the application or use Analytics.getTransmissionTarget.");
                        return;
                    }
                } else if (analyticsTransmissionTarget2.isEnabled()) {
                    eventLog.addTransmissionTarget(analyticsTransmissionTarget2.getTransmissionTargetToken());
                    eventLog.setTag(analyticsTransmissionTarget2);
                    if (analyticsTransmissionTarget2 == Analytics.this.mDefaultTransmissionTarget) {
                        eventLog.setUserId(userId);
                    }
                } else {
                    AppCenterLog.error(Analytics.LOG_TAG, "This transmission target is disabled.");
                    return;
                }
                eventLog.setId(UUIDUtils.randomUUID());
                eventLog.setName(str);
                eventLog.setTypedProperties(list);
                int persistenceFlag = Flags.getPersistenceFlag(i, true);
                ((AbstractAppCenterService) Analytics.this).mChannel.enqueue(eventLog, persistenceFlag == 2 ? Analytics.ANALYTICS_CRITICAL_GROUP : Analytics.ANALYTICS_GROUP, persistenceFlag);
            }
        });
    }

    private boolean isInstanceAutoPageTrackingEnabled() {
        return this.mAutoPageTrackingEnabled;
    }

    private synchronized void setInstanceAutoPageTrackingEnabled(boolean z) {
        this.mAutoPageTrackingEnabled = z;
    }

    private synchronized void setInstanceListener(AnalyticsListener analyticsListener) {
        this.mAnalyticsListener = analyticsListener;
    }

    private synchronized void pauseInstanceAsync() {
        post(new Runnable() { // from class: com.microsoft.appcenter.analytics.Analytics.9
            @Override // java.lang.Runnable
            public void run() {
                ((AbstractAppCenterService) Analytics.this).mChannel.pauseGroup(Analytics.ANALYTICS_GROUP, null);
                ((AbstractAppCenterService) Analytics.this).mChannel.pauseGroup(Analytics.ANALYTICS_CRITICAL_GROUP, null);
            }
        });
    }

    private synchronized void resumeInstanceAsync() {
        post(new Runnable() { // from class: com.microsoft.appcenter.analytics.Analytics.10
            @Override // java.lang.Runnable
            public void run() {
                ((AbstractAppCenterService) Analytics.this).mChannel.resumeGroup(Analytics.ANALYTICS_GROUP, null);
                ((AbstractAppCenterService) Analytics.this).mChannel.resumeGroup(Analytics.ANALYTICS_CRITICAL_GROUP, null);
            }
        });
    }

    @VisibleForTesting
    WeakReference<Activity> getCurrentActivity() {
        return this.mCurrentActivity;
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService, com.microsoft.appcenter.AppCenterService
    public synchronized void onStarted(@NonNull Context context, @NonNull Channel channel, String str, String str2, boolean z) {
        this.mContext = context;
        this.mStartedFromApp = z;
        super.onStarted(context, channel, str, str2, z);
        setDefaultTransmissionTarget(str2);
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService, com.microsoft.appcenter.AppCenterService
    public void onConfigurationUpdated(String str, String str2) {
        this.mStartedFromApp = true;
        startAppLevelFeatures();
        setDefaultTransmissionTarget(str2);
    }

    @WorkerThread
    private void setDefaultTransmissionTarget(String str) {
        if (str != null) {
            this.mDefaultTransmissionTarget = createAnalyticsTransmissionTarget(str);
        }
    }

    private boolean setInstanceTransmissionInterval(int i) {
        if (this.mChannel != null) {
            AppCenterLog.error(LOG_TAG, "Transmission interval should be set before the service is started.");
            return false;
        }
        if (i < 3 || i > MAXIMUM_TRANSMISSION_INTERVAL_IN_SECONDS) {
            AppCenterLog.error(LOG_TAG, String.format(Locale.ENGLISH, "The transmission interval is invalid. The value should be between %d seconds and %d seconds (%d day).", 3, Integer.valueOf(MAXIMUM_TRANSMISSION_INTERVAL_IN_SECONDS), Long.valueOf(TimeUnit.SECONDS.toDays(86400L))));
            return false;
        }
        this.mTransmissionInterval = TimeUnit.SECONDS.toMillis(i);
        return true;
    }

    <T> void postCommand(Runnable runnable, DefaultAppCenterFuture<T> defaultAppCenterFuture, T t) {
        postAsyncGetter(runnable, defaultAppCenterFuture, t);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.microsoft.appcenter.AbstractAppCenterService
    public synchronized void post(Runnable runnable) {
        super.post(runnable);
    }

    void postCommandEvenIfDisabled(Runnable runnable) {
        post(runnable, runnable, runnable);
    }

    String getEnabledPreferenceKeyPrefix() {
        return getEnabledPreferenceKey() + "/";
    }
}
