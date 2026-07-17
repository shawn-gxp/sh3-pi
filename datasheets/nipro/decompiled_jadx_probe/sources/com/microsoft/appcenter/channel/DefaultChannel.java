package com.microsoft.appcenter.channel;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import com.microsoft.appcenter.CancellationException;
import com.microsoft.appcenter.channel.Channel;
import com.microsoft.appcenter.http.HttpUtils;
import com.microsoft.appcenter.http.ServiceCallback;
import com.microsoft.appcenter.ingestion.AppCenterIngestion;
import com.microsoft.appcenter.ingestion.Ingestion;
import com.microsoft.appcenter.ingestion.models.Device;
import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.LogContainer;
import com.microsoft.appcenter.ingestion.models.json.LogSerializer;
import com.microsoft.appcenter.ingestion.models.one.PartAUtils;
import com.microsoft.appcenter.persistence.DatabasePersistence;
import com.microsoft.appcenter.persistence.Persistence;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.DeviceInfoHelper;
import com.microsoft.appcenter.utils.HandlerUtils;
import com.microsoft.appcenter.utils.IdHelper;
import com.microsoft.appcenter.utils.context.AbstractTokenContextListener;
import com.microsoft.appcenter.utils.context.AuthTokenContext;
import com.microsoft.appcenter.utils.context.AuthTokenInfo;
import com.microsoft.appcenter.utils.storage.SharedPreferencesManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/* loaded from: classes.dex */
public class DefaultChannel implements Channel {

    @VisibleForTesting
    static final int CLEAR_BATCH_SIZE = 100;
    private static final long MINIMUM_TRANSMISSION_INTERVAL = 3000;

    @VisibleForTesting
    static final String START_TIMER_PREFIX = "startTimerPrefix.";
    private final Handler mAppCenterHandler;
    private String mAppSecret;
    private final Context mContext;
    private int mCurrentState;
    private Device mDevice;
    private boolean mDiscardLogs;
    private boolean mEnabled;
    private final Map<String, GroupState> mGroupStates;
    private final Ingestion mIngestion;
    private final Set<Ingestion> mIngestions;
    private final UUID mInstallId;
    private final Collection<Channel.Listener> mListeners;
    private final Persistence mPersistence;

    public DefaultChannel(@NonNull Context context, String str, @NonNull LogSerializer logSerializer, @NonNull Handler handler) {
        this(context, str, buildDefaultPersistence(context, logSerializer), new AppCenterIngestion(context, logSerializer), handler);
    }

    @VisibleForTesting
    DefaultChannel(@NonNull Context context, String str, @NonNull Persistence persistence, @NonNull Ingestion ingestion, @NonNull Handler handler) {
        this.mContext = context;
        this.mAppSecret = str;
        this.mInstallId = IdHelper.getInstallId();
        this.mGroupStates = new HashMap();
        this.mListeners = new LinkedHashSet();
        this.mPersistence = persistence;
        this.mIngestion = ingestion;
        HashSet hashSet = new HashSet();
        this.mIngestions = hashSet;
        hashSet.add(this.mIngestion);
        this.mAppCenterHandler = handler;
        this.mEnabled = true;
    }

    private static Persistence buildDefaultPersistence(@NonNull Context context, @NonNull LogSerializer logSerializer) {
        DatabasePersistence databasePersistence = new DatabasePersistence(context);
        databasePersistence.setLogSerializer(logSerializer);
        return databasePersistence;
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized boolean setMaxStorageSize(long j) {
        return this.mPersistence.setMaxStorageSize(j);
    }

    private synchronized boolean checkStateDidNotChange(GroupState groupState, int i) {
        boolean z;
        if (i == this.mCurrentState) {
            z = groupState == this.mGroupStates.get(groupState.mName);
        }
        return z;
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void setAppSecret(@NonNull String str) {
        this.mAppSecret = str;
        if (this.mEnabled) {
            for (GroupState groupState : this.mGroupStates.values()) {
                if (groupState.mIngestion == this.mIngestion) {
                    checkPendingLogs(groupState);
                }
            }
        }
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void addGroup(String str, int i, long j, int i2, Ingestion ingestion, Channel.GroupListener groupListener) {
        AppCenterLog.debug("AppCenter", "addGroup(" + str + ")");
        Ingestion ingestion2 = ingestion == null ? this.mIngestion : ingestion;
        this.mIngestions.add(ingestion2);
        GroupState groupState = new GroupState(str, i, j, i2, ingestion2, groupListener);
        this.mGroupStates.put(str, groupState);
        groupState.mPendingLogCount = this.mPersistence.countLogs(str);
        AuthTokenContext.getInstance().addListener(groupState);
        if (this.mAppSecret != null || this.mIngestion != ingestion2) {
            checkPendingLogs(groupState);
        }
        Iterator<Channel.Listener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onGroupAdded(str, groupListener, j);
        }
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void removeGroup(String str) {
        AppCenterLog.debug("AppCenter", "removeGroup(" + str + ")");
        GroupState remove = this.mGroupStates.remove(str);
        if (remove != null) {
            cancelTimer(remove);
            AuthTokenContext.getInstance().removeListener(remove);
        }
        Iterator<Channel.Listener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onGroupRemoved(str);
        }
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void pauseGroup(String str, String str2) {
        GroupState groupState = this.mGroupStates.get(str);
        if (groupState != null) {
            if (str2 != null) {
                String targetKey = PartAUtils.getTargetKey(str2);
                if (groupState.mPausedTargetKeys.add(targetKey)) {
                    AppCenterLog.debug("AppCenter", "pauseGroup(" + str + ", " + targetKey + ")");
                }
            } else if (!groupState.mPaused) {
                AppCenterLog.debug("AppCenter", "pauseGroup(" + str + ")");
                groupState.mPaused = true;
                cancelTimer(groupState);
            }
            Iterator<Channel.Listener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onPaused(str, str2);
            }
        }
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void resumeGroup(String str, String str2) {
        GroupState groupState = this.mGroupStates.get(str);
        if (groupState != null) {
            if (str2 != null) {
                String targetKey = PartAUtils.getTargetKey(str2);
                if (groupState.mPausedTargetKeys.remove(targetKey)) {
                    AppCenterLog.debug("AppCenter", "resumeGroup(" + str + ", " + targetKey + ")");
                    groupState.mPendingLogCount = this.mPersistence.countLogs(str);
                    checkPendingLogs(groupState);
                }
            } else if (groupState.mPaused) {
                AppCenterLog.debug("AppCenter", "resumeGroup(" + str + ")");
                groupState.mPaused = false;
                checkPendingLogs(groupState);
            }
            Iterator<Channel.Listener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onResumed(str, str2);
            }
        }
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized boolean isEnabled() {
        return this.mEnabled;
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void setEnabled(boolean z) {
        if (this.mEnabled == z) {
            return;
        }
        if (z) {
            this.mEnabled = true;
            this.mDiscardLogs = false;
            this.mCurrentState++;
            Iterator<Ingestion> it = this.mIngestions.iterator();
            while (it.hasNext()) {
                it.next().reopen();
            }
            Iterator<GroupState> it2 = this.mGroupStates.values().iterator();
            while (it2.hasNext()) {
                checkPendingLogs(it2.next());
            }
        } else {
            suspend(true, new CancellationException());
        }
        Iterator<Channel.Listener> it3 = this.mListeners.iterator();
        while (it3.hasNext()) {
            it3.next().onGloballyEnabled(z);
        }
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void setLogUrl(String str) {
        this.mIngestion.setLogUrl(str);
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void clear(String str) {
        if (this.mGroupStates.containsKey(str)) {
            AppCenterLog.debug("AppCenter", "clear(" + str + ")");
            this.mPersistence.deleteLogs(str);
            Iterator<Channel.Listener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onClear(str);
            }
        }
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void invalidateDeviceCache() {
        this.mDevice = null;
    }

    private void suspend(boolean z, Exception exc) {
        Channel.GroupListener groupListener;
        this.mEnabled = false;
        this.mDiscardLogs = z;
        this.mCurrentState++;
        for (GroupState groupState : this.mGroupStates.values()) {
            cancelTimer(groupState);
            Iterator<Map.Entry<String, List<Log>>> it = groupState.mSendingBatches.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, List<Log>> next = it.next();
                it.remove();
                if (z && (groupListener = groupState.mListener) != null) {
                    Iterator<Log> it2 = next.getValue().iterator();
                    while (it2.hasNext()) {
                        groupListener.onFailure(it2.next(), exc);
                    }
                }
            }
        }
        for (Ingestion ingestion : this.mIngestions) {
            try {
                ingestion.close();
            } catch (IOException e) {
                AppCenterLog.error("AppCenter", "Failed to close ingestion: " + ingestion, e);
            }
        }
        if (z) {
            Iterator<GroupState> it3 = this.mGroupStates.values().iterator();
            while (it3.hasNext()) {
                deleteLogsOnSuspended(it3.next());
            }
            return;
        }
        this.mPersistence.clearPendingLogState();
    }

    private void deleteLogsOnSuspended(GroupState groupState) {
        ArrayList<Log> arrayList = new ArrayList();
        this.mPersistence.getLogs(groupState.mName, Collections.emptyList(), 100, arrayList, null, null);
        if (arrayList.size() > 0 && groupState.mListener != null) {
            for (Log log : arrayList) {
                groupState.mListener.onBeforeSending(log);
                groupState.mListener.onFailure(log, new CancellationException());
            }
        }
        if (arrayList.size() >= 100 && groupState.mListener != null) {
            deleteLogsOnSuspended(groupState);
        } else {
            this.mPersistence.deleteLogs(groupState.mName);
        }
    }

    @VisibleForTesting
    void cancelTimer(GroupState groupState) {
        if (groupState.mScheduled) {
            groupState.mScheduled = false;
            this.mAppCenterHandler.removeCallbacks(groupState.mRunnable);
            SharedPreferencesManager.remove(START_TIMER_PREFIX + groupState.mName);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void triggerIngestion(@NonNull final GroupState groupState) {
        Date date;
        Date date2;
        String str;
        if (this.mEnabled) {
            int i = groupState.mPendingLogCount;
            int min = Math.min(i, groupState.mMaxLogsPerBatch);
            AppCenterLog.debug("AppCenter", "triggerIngestion(" + groupState.mName + ") pendingLogCount=" + i);
            cancelTimer(groupState);
            if (groupState.mSendingBatches.size() == groupState.mMaxParallelBatches) {
                AppCenterLog.debug("AppCenter", "Already sending " + groupState.mMaxParallelBatches + " batches of analytics data to the server.");
                return;
            }
            AuthTokenContext authTokenContext = AuthTokenContext.getInstance();
            ListIterator<AuthTokenInfo> listIterator = authTokenContext.getAuthTokenValidityList().listIterator();
            while (listIterator.hasNext()) {
                AuthTokenInfo next = listIterator.next();
                if (next != null) {
                    String authToken = next.getAuthToken();
                    Date startTime = next.getStartTime();
                    Date endTime = next.getEndTime();
                    authTokenContext.checkIfTokenNeedsToBeRefreshed(next);
                    str = authToken;
                    date = startTime;
                    date2 = endTime;
                } else {
                    date = null;
                    date2 = null;
                    str = null;
                }
                final ArrayList arrayList = new ArrayList(min);
                final int i2 = this.mCurrentState;
                final String logs = this.mPersistence.getLogs(groupState.mName, groupState.mPausedTargetKeys, min, arrayList, date, date2);
                groupState.mPendingLogCount -= arrayList.size();
                if (logs == null) {
                    if (listIterator.previousIndex() == 0 && date2 != null && this.mPersistence.countLogs(date2) == 0) {
                        authTokenContext.removeOldestTokenIfMatching(str);
                    }
                } else {
                    AppCenterLog.debug("AppCenter", "ingestLogs(" + groupState.mName + "," + logs + ") pendingLogCount=" + groupState.mPendingLogCount);
                    if (groupState.mListener != null) {
                        Iterator it = arrayList.iterator();
                        while (it.hasNext()) {
                            groupState.mListener.onBeforeSending((Log) it.next());
                        }
                    }
                    groupState.mSendingBatches.put(logs, arrayList);
                    final String str2 = str;
                    HandlerUtils.runOnUiThread(new Runnable() { // from class: com.microsoft.appcenter.channel.DefaultChannel.1
                        @Override // java.lang.Runnable
                        public void run() {
                            DefaultChannel.this.sendLogs(groupState, i2, arrayList, logs, str2);
                        }
                    });
                    return;
                }
            }
            groupState.mPendingLogCount = this.mPersistence.countLogs(groupState.mName);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    @MainThread
    public synchronized void sendLogs(final GroupState groupState, final int i, List<Log> list, final String str, String str2) {
        if (checkStateDidNotChange(groupState, i)) {
            LogContainer logContainer = new LogContainer();
            logContainer.setLogs(list);
            groupState.mIngestion.sendAsync(str2, this.mAppSecret, this.mInstallId, logContainer, new ServiceCallback() { // from class: com.microsoft.appcenter.channel.DefaultChannel.2
                @Override // com.microsoft.appcenter.http.ServiceCallback
                public void onCallSucceeded(String str3, Map<String, String> map) {
                    DefaultChannel.this.mAppCenterHandler.post(new Runnable() { // from class: com.microsoft.appcenter.channel.DefaultChannel.2.1
                        @Override // java.lang.Runnable
                        public void run() {
                            AnonymousClass2 anonymousClass2 = AnonymousClass2.this;
                            DefaultChannel.this.handleSendingSuccess(groupState, str);
                        }
                    });
                }

                @Override // com.microsoft.appcenter.http.ServiceCallback
                public void onCallFailed(final Exception exc) {
                    DefaultChannel.this.mAppCenterHandler.post(new Runnable() { // from class: com.microsoft.appcenter.channel.DefaultChannel.2.2
                        @Override // java.lang.Runnable
                        public void run() {
                            AnonymousClass2 anonymousClass2 = AnonymousClass2.this;
                            DefaultChannel.this.handleSendingFailure(groupState, str, exc);
                        }
                    });
                }
            });
            this.mAppCenterHandler.post(new Runnable() { // from class: com.microsoft.appcenter.channel.DefaultChannel.3
                @Override // java.lang.Runnable
                public void run() {
                    DefaultChannel.this.checkPendingLogsAfterPost(groupState, i);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkPendingLogsAfterPost(@NonNull GroupState groupState, int i) {
        if (checkStateDidNotChange(groupState, i)) {
            checkPendingLogs(groupState);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void handleSendingSuccess(@NonNull GroupState groupState, @NonNull String str) {
        List<Log> remove = groupState.mSendingBatches.remove(str);
        if (remove != null) {
            this.mPersistence.deleteLogs(groupState.mName, str);
            Channel.GroupListener groupListener = groupState.mListener;
            if (groupListener != null) {
                Iterator<Log> it = remove.iterator();
                while (it.hasNext()) {
                    groupListener.onSuccess(it.next());
                }
            }
            checkPendingLogs(groupState);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void handleSendingFailure(@NonNull GroupState groupState, @NonNull String str, @NonNull Exception exc) {
        String str2 = groupState.mName;
        List<Log> remove = groupState.mSendingBatches.remove(str);
        if (remove != null) {
            AppCenterLog.error("AppCenter", "Sending logs groupName=" + str2 + " id=" + str + " failed", exc);
            boolean isRecoverableError = HttpUtils.isRecoverableError(exc);
            if (isRecoverableError) {
                groupState.mPendingLogCount += remove.size();
            } else {
                Channel.GroupListener groupListener = groupState.mListener;
                if (groupListener != null) {
                    Iterator<Log> it = remove.iterator();
                    while (it.hasNext()) {
                        groupListener.onFailure(it.next(), exc);
                    }
                }
            }
            suspend(!isRecoverableError, exc);
        }
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void enqueue(@NonNull Log log, @NonNull String str, int i) {
        boolean z;
        GroupState groupState = this.mGroupStates.get(str);
        if (groupState == null) {
            AppCenterLog.error("AppCenter", "Invalid group name:" + str);
            return;
        }
        if (this.mDiscardLogs) {
            AppCenterLog.warn("AppCenter", "Channel is disabled, the log is discarded.");
            if (groupState.mListener != null) {
                groupState.mListener.onBeforeSending(log);
                groupState.mListener.onFailure(log, new CancellationException());
            }
            return;
        }
        Iterator<Channel.Listener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onPreparingLog(log, str);
        }
        if (log.getDevice() == null) {
            if (this.mDevice == null) {
                try {
                    this.mDevice = DeviceInfoHelper.getDeviceInfo(this.mContext);
                } catch (DeviceInfoHelper.DeviceInfoException e) {
                    AppCenterLog.error("AppCenter", "Device log cannot be generated", e);
                    return;
                }
            }
            log.setDevice(this.mDevice);
        }
        if (log.getTimestamp() == null) {
            log.setTimestamp(new Date());
        }
        Iterator<Channel.Listener> it2 = this.mListeners.iterator();
        while (it2.hasNext()) {
            it2.next().onPreparedLog(log, str, i);
        }
        loop2: while (true) {
            for (Channel.Listener listener : this.mListeners) {
                z = z || listener.shouldFilter(log);
            }
        }
        if (z) {
            AppCenterLog.debug("AppCenter", "Log of type '" + log.getType() + "' was filtered out by listener(s)");
        } else {
            if (this.mAppSecret == null && groupState.mIngestion == this.mIngestion) {
                AppCenterLog.debug("AppCenter", "Log of type '" + log.getType() + "' was not filtered out by listener(s) but no app secret was provided. Not persisting/sending the log.");
                return;
            }
            try {
                this.mPersistence.putLog(log, str, i);
                Iterator<String> it3 = log.getTransmissionTargetTokens().iterator();
                String targetKey = it3.hasNext() ? PartAUtils.getTargetKey(it3.next()) : null;
                if (groupState.mPausedTargetKeys.contains(targetKey)) {
                    AppCenterLog.debug("AppCenter", "Transmission target ikey=" + targetKey + " is paused.");
                    return;
                }
                groupState.mPendingLogCount++;
                AppCenterLog.debug("AppCenter", "enqueue(" + groupState.mName + ") pendingLogCount=" + groupState.mPendingLogCount);
                if (this.mEnabled) {
                    checkPendingLogs(groupState);
                } else {
                    AppCenterLog.debug("AppCenter", "Channel is temporarily disabled, log was saved to disk.");
                }
            } catch (Persistence.PersistenceException e2) {
                AppCenterLog.error("AppCenter", "Error persisting log", e2);
                if (groupState.mListener != null) {
                    groupState.mListener.onBeforeSending(log);
                    groupState.mListener.onFailure(log, e2);
                }
            }
        }
    }

    @VisibleForTesting
    synchronized void checkPendingLogs(@NonNull GroupState groupState) {
        AppCenterLog.debug("AppCenter", String.format("checkPendingLogs(%s) pendingLogCount=%s batchTimeInterval=%s", groupState.mName, Integer.valueOf(groupState.mPendingLogCount), Long.valueOf(groupState.mBatchTimeInterval)));
        Long resolveTriggerInterval = resolveTriggerInterval(groupState);
        if (resolveTriggerInterval != null && !groupState.mPaused) {
            if (resolveTriggerInterval.longValue() == 0) {
                triggerIngestion(groupState);
            } else if (!groupState.mScheduled) {
                groupState.mScheduled = true;
                this.mAppCenterHandler.postDelayed(groupState.mRunnable, resolveTriggerInterval.longValue());
            }
        }
    }

    @WorkerThread
    private Long resolveTriggerInterval(@NonNull GroupState groupState) {
        if (groupState.mBatchTimeInterval > MINIMUM_TRANSMISSION_INTERVAL) {
            return resolveCustomTriggerInterval(groupState);
        }
        return resolveDefaultTriggerInterval(groupState);
    }

    @WorkerThread
    private Long resolveCustomTriggerInterval(@NonNull GroupState groupState) {
        long currentTimeMillis = System.currentTimeMillis();
        long j = SharedPreferencesManager.getLong(START_TIMER_PREFIX + groupState.mName);
        if (groupState.mPendingLogCount <= 0) {
            if (j + groupState.mBatchTimeInterval >= currentTimeMillis) {
                return null;
            }
            SharedPreferencesManager.remove(START_TIMER_PREFIX + groupState.mName);
            AppCenterLog.debug("AppCenter", "The timer for " + groupState.mName + " channel finished.");
            return null;
        }
        if (j == 0 || j > currentTimeMillis) {
            SharedPreferencesManager.putLong(START_TIMER_PREFIX + groupState.mName, currentTimeMillis);
            AppCenterLog.debug("AppCenter", "The timer value for " + groupState.mName + " has been saved.");
            return Long.valueOf(groupState.mBatchTimeInterval);
        }
        return Long.valueOf(Math.max(groupState.mBatchTimeInterval - (currentTimeMillis - j), 0L));
    }

    private Long resolveDefaultTriggerInterval(@NonNull GroupState groupState) {
        int i = groupState.mPendingLogCount;
        if (i >= groupState.mMaxLogsPerBatch) {
            return 0L;
        }
        if (i > 0) {
            return Long.valueOf(groupState.mBatchTimeInterval);
        }
        return null;
    }

    @VisibleForTesting
    GroupState getGroupState(String str) {
        return this.mGroupStates.get(str);
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void addListener(Channel.Listener listener) {
        this.mListeners.add(listener);
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void removeListener(Channel.Listener listener) {
        this.mListeners.remove(listener);
    }

    @Override // com.microsoft.appcenter.channel.Channel
    public synchronized void shutdown() {
        suspend(false, new CancellationException());
    }

    @VisibleForTesting
    class GroupState extends AbstractTokenContextListener {
        final long mBatchTimeInterval;
        final Ingestion mIngestion;
        final Channel.GroupListener mListener;
        final int mMaxLogsPerBatch;
        final int mMaxParallelBatches;
        final String mName;
        boolean mPaused;
        int mPendingLogCount;
        boolean mScheduled;
        final Map<String, List<Log>> mSendingBatches = new HashMap();
        final Collection<String> mPausedTargetKeys = new HashSet();
        final Runnable mRunnable = new Runnable() { // from class: com.microsoft.appcenter.channel.DefaultChannel.GroupState.1
            @Override // java.lang.Runnable
            public void run() {
                GroupState groupState = GroupState.this;
                groupState.mScheduled = false;
                DefaultChannel.this.triggerIngestion(groupState);
            }
        };

        GroupState(String str, int i, long j, int i2, Ingestion ingestion, Channel.GroupListener groupListener) {
            this.mName = str;
            this.mMaxLogsPerBatch = i;
            this.mBatchTimeInterval = j;
            this.mMaxParallelBatches = i2;
            this.mIngestion = ingestion;
            this.mListener = groupListener;
        }

        @Override // com.microsoft.appcenter.utils.context.AbstractTokenContextListener, com.microsoft.appcenter.utils.context.AuthTokenContext.Listener
        public void onNewAuthToken(String str) {
            DefaultChannel.this.checkPendingLogs(this);
        }
    }
}
