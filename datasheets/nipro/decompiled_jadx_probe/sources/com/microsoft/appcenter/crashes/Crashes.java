package com.microsoft.appcenter.crashes;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.AbstractAppCenterService;
import com.microsoft.appcenter.Constants;
import com.microsoft.appcenter.channel.Channel;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;
import com.microsoft.appcenter.crashes.ingestion.models.Exception;
import com.microsoft.appcenter.crashes.ingestion.models.HandledErrorLog;
import com.microsoft.appcenter.crashes.ingestion.models.ManagedErrorLog;
import com.microsoft.appcenter.crashes.ingestion.models.json.ErrorAttachmentLogFactory;
import com.microsoft.appcenter.crashes.ingestion.models.json.HandledErrorLogFactory;
import com.microsoft.appcenter.crashes.ingestion.models.json.ManagedErrorLogFactory;
import com.microsoft.appcenter.crashes.model.ErrorReport;
import com.microsoft.appcenter.crashes.model.NativeException;
import com.microsoft.appcenter.crashes.model.TestCrashException;
import com.microsoft.appcenter.crashes.utils.ErrorLogHelper;
import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.json.DefaultLogSerializer;
import com.microsoft.appcenter.ingestion.models.json.LogFactory;
import com.microsoft.appcenter.ingestion.models.json.LogSerializer;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.DeviceInfoHelper;
import com.microsoft.appcenter.utils.HandlerUtils;
import com.microsoft.appcenter.utils.async.AppCenterFuture;
import com.microsoft.appcenter.utils.async.DefaultAppCenterFuture;
import com.microsoft.appcenter.utils.context.SessionContext;
import com.microsoft.appcenter.utils.context.UserIdContext;
import com.microsoft.appcenter.utils.storage.FileManager;
import com.microsoft.appcenter.utils.storage.SharedPreferencesManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;

/* loaded from: classes.dex */
public class Crashes extends AbstractAppCenterService {
    public static final int ALWAYS_SEND = 2;
    public static final int DONT_SEND = 1;

    @VisibleForTesting
    static final String ERROR_GROUP = "groupErrors";
    public static final String LOG_TAG = "AppCenterCrashes";
    private static final int MAX_ATTACHMENT_PER_CRASH = 2;

    @VisibleForTesting
    public static final String PREF_KEY_ALWAYS_SEND = "com.microsoft.appcenter.crashes.always.send";
    public static final int SEND = 0;
    private static final String SERVICE_NAME = "Crashes";
    private boolean mAutomaticProcessing = true;
    private Context mContext;
    private CrashesListener mCrashesListener;
    private final Map<UUID, ErrorLogReport> mErrorReportCache;
    private final Map<String, LogFactory> mFactories;
    private long mInitializeTimestamp;
    private ErrorReport mLastSessionErrorReport;
    private LogSerializer mLogSerializer;
    private boolean mSavedUncaughtException;
    private UncaughtExceptionHandler mUncaughtExceptionHandler;
    private final Map<UUID, ErrorLogReport> mUnprocessedErrorReports;
    private static final CrashesListener DEFAULT_ERROR_REPORTING_LISTENER = new DefaultCrashesListener();

    @SuppressLint({"StaticFieldLeak"})
    private static Crashes sInstance = null;

    private interface CallbackProcessor {
        void onCallBack(ErrorReport errorReport);

        boolean shouldDeleteThrowable();
    }

    private interface ExceptionModelBuilder {
        Exception buildExceptionModel();
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService
    protected String getGroupName() {
        return ERROR_GROUP;
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService
    protected String getLoggerTag() {
        return LOG_TAG;
    }

    @Override // com.microsoft.appcenter.AppCenterService
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService
    protected int getTriggerCount() {
        return 1;
    }

    private Crashes() {
        HashMap hashMap = new HashMap();
        this.mFactories = hashMap;
        hashMap.put(ManagedErrorLog.TYPE, ManagedErrorLogFactory.getInstance());
        this.mFactories.put(HandledErrorLog.TYPE, HandledErrorLogFactory.getInstance());
        this.mFactories.put(ErrorAttachmentLog.TYPE, ErrorAttachmentLogFactory.getInstance());
        DefaultLogSerializer defaultLogSerializer = new DefaultLogSerializer();
        this.mLogSerializer = defaultLogSerializer;
        defaultLogSerializer.addLogFactory(ManagedErrorLog.TYPE, ManagedErrorLogFactory.getInstance());
        this.mLogSerializer.addLogFactory(ErrorAttachmentLog.TYPE, ErrorAttachmentLogFactory.getInstance());
        this.mCrashesListener = DEFAULT_ERROR_REPORTING_LISTENER;
        this.mUnprocessedErrorReports = new LinkedHashMap();
        this.mErrorReportCache = new LinkedHashMap();
    }

    @NonNull
    public static synchronized Crashes getInstance() {
        Crashes crashes;
        synchronized (Crashes.class) {
            if (sInstance == null) {
                sInstance = new Crashes();
            }
            crashes = sInstance;
        }
        return crashes;
    }

    @VisibleForTesting
    static synchronized void unsetInstance() {
        synchronized (Crashes.class) {
            sInstance = null;
        }
    }

    public static AppCenterFuture<Boolean> isEnabled() {
        return getInstance().isInstanceEnabledAsync();
    }

    public static AppCenterFuture<Void> setEnabled(boolean z) {
        return getInstance().setInstanceEnabledAsync(z);
    }

    static void trackException(@NonNull Throwable th) {
        trackException(th, null);
    }

    static void trackException(@NonNull Throwable th, Map<String, String> map) {
        getInstance().queueException(th, ErrorLogHelper.validateProperties(map, "HandledError"));
    }

    public static void generateTestCrash() {
        if (Constants.APPLICATION_DEBUGGABLE) {
            throw new TestCrashException();
        }
        AppCenterLog.warn(LOG_TAG, "The application is not debuggable so SDK won't generate test crash");
    }

    public static void setListener(CrashesListener crashesListener) {
        getInstance().setInstanceListener(crashesListener);
    }

    public static AppCenterFuture<String> getMinidumpDirectory() {
        return getInstance().getNewMinidumpDirectoryAsync();
    }

    public static void notifyUserConfirmation(int i) {
        getInstance().handleUserConfirmation(i);
    }

    public static AppCenterFuture<Boolean> hasCrashedInLastSession() {
        return getInstance().hasInstanceCrashedInLastSession();
    }

    public static AppCenterFuture<ErrorReport> getLastSessionCrashReport() {
        return getInstance().getInstanceLastSessionCrashReport();
    }

    private synchronized AppCenterFuture<String> getNewMinidumpDirectoryAsync() {
        final DefaultAppCenterFuture defaultAppCenterFuture;
        defaultAppCenterFuture = new DefaultAppCenterFuture();
        postAsyncGetter(new Runnable() { // from class: com.microsoft.appcenter.crashes.Crashes.1
            @Override // java.lang.Runnable
            public void run() {
                defaultAppCenterFuture.complete(ErrorLogHelper.getNewMinidumpDirectory().getAbsolutePath());
            }
        }, defaultAppCenterFuture, null);
        return defaultAppCenterFuture;
    }

    private synchronized AppCenterFuture<Boolean> hasInstanceCrashedInLastSession() {
        final DefaultAppCenterFuture defaultAppCenterFuture;
        defaultAppCenterFuture = new DefaultAppCenterFuture();
        postAsyncGetter(new Runnable() { // from class: com.microsoft.appcenter.crashes.Crashes.2
            @Override // java.lang.Runnable
            public void run() {
                defaultAppCenterFuture.complete(Boolean.valueOf(Crashes.this.mLastSessionErrorReport != null));
            }
        }, defaultAppCenterFuture, false);
        return defaultAppCenterFuture;
    }

    private synchronized AppCenterFuture<ErrorReport> getInstanceLastSessionCrashReport() {
        final DefaultAppCenterFuture defaultAppCenterFuture;
        defaultAppCenterFuture = new DefaultAppCenterFuture();
        postAsyncGetter(new Runnable() { // from class: com.microsoft.appcenter.crashes.Crashes.3
            @Override // java.lang.Runnable
            public void run() {
                defaultAppCenterFuture.complete(Crashes.this.mLastSessionErrorReport);
            }
        }, defaultAppCenterFuture, null);
        return defaultAppCenterFuture;
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService
    protected synchronized void applyEnabledState(boolean z) {
        initialize();
        if (!z) {
            for (File file : ErrorLogHelper.getErrorStorageDirectory().listFiles()) {
                AppCenterLog.debug(LOG_TAG, "Deleting file " + file);
                if (!file.delete()) {
                    AppCenterLog.warn(LOG_TAG, "Failed to delete file " + file);
                }
            }
            AppCenterLog.info(LOG_TAG, "Deleted crashes local files");
        }
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService, com.microsoft.appcenter.AppCenterService
    public synchronized void onStarted(@NonNull Context context, @NonNull Channel channel, String str, String str2, boolean z) {
        this.mContext = context;
        super.onStarted(context, channel, str, str2, z);
        if (isInstanceEnabled()) {
            processPendingErrors();
        }
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService, com.microsoft.appcenter.AppCenterService
    public Map<String, LogFactory> getLogFactories() {
        return this.mFactories;
    }

    @Override // com.microsoft.appcenter.AbstractAppCenterService
    protected Channel.GroupListener getChannelListener() {
        return new Channel.GroupListener() { // from class: com.microsoft.appcenter.crashes.Crashes.4
            private void processCallback(final Log log, final CallbackProcessor callbackProcessor) {
                Crashes.this.post(new Runnable() { // from class: com.microsoft.appcenter.crashes.Crashes.4.1
                    @Override // java.lang.Runnable
                    public void run() {
                        Log log2 = log;
                        if (log2 instanceof ManagedErrorLog) {
                            ManagedErrorLog managedErrorLog = (ManagedErrorLog) log2;
                            final ErrorReport buildErrorReport = Crashes.this.buildErrorReport(managedErrorLog);
                            UUID id = managedErrorLog.getId();
                            if (buildErrorReport != null) {
                                if (callbackProcessor.shouldDeleteThrowable()) {
                                    Crashes.this.removeStoredThrowable(id);
                                }
                                HandlerUtils.runOnUiThread(new Runnable() { // from class: com.microsoft.appcenter.crashes.Crashes.4.1.1
                                    @Override // java.lang.Runnable
                                    public void run() {
                                        callbackProcessor.onCallBack(buildErrorReport);
                                    }
                                });
                                return;
                            } else {
                                AppCenterLog.warn(Crashes.LOG_TAG, "Cannot find crash report for the error log: " + id);
                                return;
                            }
                        }
                        if ((log2 instanceof ErrorAttachmentLog) || (log2 instanceof HandledErrorLog)) {
                            return;
                        }
                        AppCenterLog.warn(Crashes.LOG_TAG, "A different type of log comes to crashes: " + log.getClass().getName());
                    }
                });
            }

            @Override // com.microsoft.appcenter.channel.Channel.GroupListener
            public void onBeforeSending(Log log) {
                processCallback(log, new CallbackProcessor() { // from class: com.microsoft.appcenter.crashes.Crashes.4.2
                    @Override // com.microsoft.appcenter.crashes.Crashes.CallbackProcessor
                    public boolean shouldDeleteThrowable() {
                        return false;
                    }

                    @Override // com.microsoft.appcenter.crashes.Crashes.CallbackProcessor
                    public void onCallBack(ErrorReport errorReport) {
                        Crashes.this.mCrashesListener.onBeforeSending(errorReport);
                    }
                });
            }

            @Override // com.microsoft.appcenter.channel.Channel.GroupListener
            public void onSuccess(Log log) {
                processCallback(log, new CallbackProcessor() { // from class: com.microsoft.appcenter.crashes.Crashes.4.3
                    @Override // com.microsoft.appcenter.crashes.Crashes.CallbackProcessor
                    public boolean shouldDeleteThrowable() {
                        return true;
                    }

                    @Override // com.microsoft.appcenter.crashes.Crashes.CallbackProcessor
                    public void onCallBack(ErrorReport errorReport) {
                        Crashes.this.mCrashesListener.onSendingSucceeded(errorReport);
                    }
                });
            }

            @Override // com.microsoft.appcenter.channel.Channel.GroupListener
            public void onFailure(Log log, final Exception exc) {
                processCallback(log, new CallbackProcessor() { // from class: com.microsoft.appcenter.crashes.Crashes.4.4
                    @Override // com.microsoft.appcenter.crashes.Crashes.CallbackProcessor
                    public boolean shouldDeleteThrowable() {
                        return true;
                    }

                    @Override // com.microsoft.appcenter.crashes.Crashes.CallbackProcessor
                    public void onCallBack(ErrorReport errorReport) {
                        Crashes.this.mCrashesListener.onSendingFailed(errorReport, exc);
                    }
                });
            }
        };
    }

    @VisibleForTesting
    synchronized long getInitializeTimestamp() {
        return this.mInitializeTimestamp;
    }

    private synchronized void queueException(@NonNull final Throwable th, Map<String, String> map) {
        queueException(new ExceptionModelBuilder() { // from class: com.microsoft.appcenter.crashes.Crashes.5
            @Override // com.microsoft.appcenter.crashes.Crashes.ExceptionModelBuilder
            public Exception buildExceptionModel() {
                return ErrorLogHelper.getModelExceptionFromThrowable(th);
            }
        }, map);
    }

    synchronized void queueException(@NonNull final Exception exception, Map<String, String> map) {
        queueException(new ExceptionModelBuilder() { // from class: com.microsoft.appcenter.crashes.Crashes.6
            @Override // com.microsoft.appcenter.crashes.Crashes.ExceptionModelBuilder
            public Exception buildExceptionModel() {
                return exception;
            }
        }, map);
    }

    private synchronized void queueException(@NonNull final ExceptionModelBuilder exceptionModelBuilder, final Map<String, String> map) {
        post(new Runnable() { // from class: com.microsoft.appcenter.crashes.Crashes.7
            @Override // java.lang.Runnable
            public void run() {
                HandledErrorLog handledErrorLog = new HandledErrorLog();
                handledErrorLog.setId(UUID.randomUUID());
                handledErrorLog.setUserId(UserIdContext.getInstance().getUserId());
                handledErrorLog.setException(exceptionModelBuilder.buildExceptionModel());
                handledErrorLog.setProperties(map);
                ((AbstractAppCenterService) Crashes.this).mChannel.enqueue(handledErrorLog, Crashes.ERROR_GROUP, 1);
            }
        });
    }

    private void initialize() {
        boolean isInstanceEnabled = isInstanceEnabled();
        this.mInitializeTimestamp = isInstanceEnabled ? System.currentTimeMillis() : -1L;
        if (!isInstanceEnabled) {
            UncaughtExceptionHandler uncaughtExceptionHandler = this.mUncaughtExceptionHandler;
            if (uncaughtExceptionHandler != null) {
                uncaughtExceptionHandler.unregister();
                this.mUncaughtExceptionHandler = null;
                return;
            }
            return;
        }
        UncaughtExceptionHandler uncaughtExceptionHandler2 = new UncaughtExceptionHandler();
        this.mUncaughtExceptionHandler = uncaughtExceptionHandler2;
        uncaughtExceptionHandler2.register();
        processMinidumpFiles();
    }

    private void processMinidumpFiles() {
        for (File file : ErrorLogHelper.getNewMinidumpFiles()) {
            AppCenterLog.debug(LOG_TAG, "Process pending minidump file: " + file);
            long lastModified = file.lastModified();
            File file2 = new File(ErrorLogHelper.getPendingMinidumpDirectory(), file.getName());
            NativeException nativeException = new NativeException();
            Exception exception = new Exception();
            exception.setType("minidump");
            exception.setWrapperSdkName(Constants.WRAPPER_SDK_NAME_NDK);
            exception.setMinidumpFilePath(file2.getPath());
            ManagedErrorLog managedErrorLog = new ManagedErrorLog();
            managedErrorLog.setException(exception);
            managedErrorLog.setTimestamp(new Date(lastModified));
            managedErrorLog.setFatal(true);
            managedErrorLog.setId(UUID.randomUUID());
            SessionContext.SessionInfo sessionAt = SessionContext.getInstance().getSessionAt(lastModified);
            if (sessionAt != null && sessionAt.getAppLaunchTimestamp() <= lastModified) {
                managedErrorLog.setAppLaunchTimestamp(new Date(sessionAt.getAppLaunchTimestamp()));
            } else {
                managedErrorLog.setAppLaunchTimestamp(managedErrorLog.getTimestamp());
            }
            managedErrorLog.setProcessId(0);
            managedErrorLog.setProcessName("");
            managedErrorLog.setUserId(UserIdContext.getInstance().getUserId());
            try {
                managedErrorLog.setDevice(DeviceInfoHelper.getDeviceInfo(this.mContext));
                managedErrorLog.getDevice().setWrapperSdkName(Constants.WRAPPER_SDK_NAME_NDK);
                saveErrorLogFiles(nativeException, managedErrorLog);
            } catch (Exception e) {
                file.delete();
                removeAllStoredErrorLogFiles(managedErrorLog.getId());
                AppCenterLog.error(LOG_TAG, "Failed to process new minidump file: " + file, e);
            }
            if (!file.renameTo(file2)) {
                throw new IOException("Failed to move file");
            }
        }
        File lastErrorLogFile = ErrorLogHelper.getLastErrorLogFile();
        while (lastErrorLogFile != null && lastErrorLogFile.length() == 0) {
            AppCenterLog.warn(LOG_TAG, "Deleting empty error file: " + lastErrorLogFile);
            lastErrorLogFile.delete();
            lastErrorLogFile = ErrorLogHelper.getLastErrorLogFile();
        }
        if (lastErrorLogFile != null) {
            AppCenterLog.debug(LOG_TAG, "Processing crash report for the last session.");
            String read = FileManager.read(lastErrorLogFile);
            if (read == null) {
                AppCenterLog.error(LOG_TAG, "Error reading last session error log.");
                return;
            }
            try {
                this.mLastSessionErrorReport = buildErrorReport((ManagedErrorLog) this.mLogSerializer.deserializeLog(read, null));
                AppCenterLog.debug(LOG_TAG, "Processed crash report for the last session.");
            } catch (JSONException e2) {
                AppCenterLog.error(LOG_TAG, "Error parsing last session error log.", e2);
            }
        }
    }

    private void processPendingErrors() {
        for (File file : ErrorLogHelper.getStoredErrorLogFiles()) {
            AppCenterLog.debug(LOG_TAG, "Process pending error file: " + file);
            String read = FileManager.read(file);
            if (read != null) {
                try {
                    ManagedErrorLog managedErrorLog = (ManagedErrorLog) this.mLogSerializer.deserializeLog(read, null);
                    UUID id = managedErrorLog.getId();
                    ErrorReport buildErrorReport = buildErrorReport(managedErrorLog);
                    if (buildErrorReport == null) {
                        removeAllStoredErrorLogFiles(id);
                    } else {
                        if (this.mAutomaticProcessing && !this.mCrashesListener.shouldProcess(buildErrorReport)) {
                            AppCenterLog.debug(LOG_TAG, "CrashesListener.shouldProcess returned false, clean up and ignore log: " + id.toString());
                            removeAllStoredErrorLogFiles(id);
                        }
                        if (!this.mAutomaticProcessing) {
                            AppCenterLog.debug(LOG_TAG, "CrashesListener.shouldProcess returned true, continue processing log: " + id.toString());
                        }
                        this.mUnprocessedErrorReports.put(id, this.mErrorReportCache.get(id));
                    }
                } catch (JSONException e) {
                    AppCenterLog.error(LOG_TAG, "Error parsing error log. Deleting invalid file: " + file, e);
                    file.delete();
                }
            }
        }
        if (this.mAutomaticProcessing) {
            sendCrashReportsOrAwaitUserConfirmation();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean sendCrashReportsOrAwaitUserConfirmation() {
        final boolean z = SharedPreferencesManager.getBoolean(PREF_KEY_ALWAYS_SEND, false);
        HandlerUtils.runOnUiThread(new Runnable() { // from class: com.microsoft.appcenter.crashes.Crashes.8
            @Override // java.lang.Runnable
            public void run() {
                if (Crashes.this.mUnprocessedErrorReports.size() > 0) {
                    if (!z) {
                        if (Crashes.this.mAutomaticProcessing) {
                            if (!Crashes.this.mCrashesListener.shouldAwaitUserConfirmation()) {
                                AppCenterLog.debug(Crashes.LOG_TAG, "CrashesListener.shouldAwaitUserConfirmation returned false, will send logs.");
                                Crashes.this.handleUserConfirmation(0);
                                return;
                            } else {
                                AppCenterLog.debug(Crashes.LOG_TAG, "CrashesListener.shouldAwaitUserConfirmation returned true, wait sending logs.");
                                return;
                            }
                        }
                        AppCenterLog.debug(Crashes.LOG_TAG, "Automatic processing disabled, will wait for explicit user confirmation.");
                        return;
                    }
                    AppCenterLog.debug(Crashes.LOG_TAG, "The flag for user confirmation is set to ALWAYS_SEND, will send logs.");
                    Crashes.this.handleUserConfirmation(0);
                }
            }
        });
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeAllStoredErrorLogFiles(UUID uuid) {
        ErrorLogHelper.removeStoredErrorLogFile(uuid);
        removeStoredThrowable(uuid);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeStoredThrowable(UUID uuid) {
        this.mErrorReportCache.remove(uuid);
        WrapperSdkExceptionManager.deleteWrapperExceptionData(uuid);
        ErrorLogHelper.removeStoredThrowableFile(uuid);
    }

    @VisibleForTesting
    UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return this.mUncaughtExceptionHandler;
    }

    @VisibleForTesting
    void setUncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.mUncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    @Nullable
    @VisibleForTesting
    ErrorReport buildErrorReport(ManagedErrorLog managedErrorLog) {
        Throwable th;
        UUID id = managedErrorLog.getId();
        if (!this.mErrorReportCache.containsKey(id)) {
            File storedThrowableFile = ErrorLogHelper.getStoredThrowableFile(id);
            if (storedThrowableFile == null) {
                return null;
            }
            if (storedThrowableFile.length() > 0) {
                try {
                    th = (Throwable) FileManager.readObject(storedThrowableFile);
                } catch (IOException | ClassNotFoundException | RuntimeException | StackOverflowError e) {
                    AppCenterLog.error(LOG_TAG, "Cannot read throwable file " + storedThrowableFile.getName(), e);
                }
                ErrorReport errorReportFromErrorLog = ErrorLogHelper.getErrorReportFromErrorLog(managedErrorLog, th);
                this.mErrorReportCache.put(id, new ErrorLogReport(managedErrorLog, errorReportFromErrorLog));
                return errorReportFromErrorLog;
            }
            th = null;
            ErrorReport errorReportFromErrorLog2 = ErrorLogHelper.getErrorReportFromErrorLog(managedErrorLog, th);
            this.mErrorReportCache.put(id, new ErrorLogReport(managedErrorLog, errorReportFromErrorLog2));
            return errorReportFromErrorLog2;
        }
        ErrorReport errorReport = this.mErrorReportCache.get(id).report;
        errorReport.setDevice(managedErrorLog.getDevice());
        return errorReport;
    }

    @VisibleForTesting
    CrashesListener getInstanceListener() {
        return this.mCrashesListener;
    }

    @VisibleForTesting
    synchronized void setInstanceListener(CrashesListener crashesListener) {
        if (crashesListener == null) {
            crashesListener = DEFAULT_ERROR_REPORTING_LISTENER;
        }
        this.mCrashesListener = crashesListener;
    }

    /* JADX INFO: Access modifiers changed from: private */
    @VisibleForTesting
    public synchronized void handleUserConfirmation(final int i) {
        post(new Runnable() { // from class: com.microsoft.appcenter.crashes.Crashes.9
            /* JADX WARN: Removed duplicated region for block: B:25:0x00a5  */
            /* JADX WARN: Removed duplicated region for block: B:28:0x00c1  */
            /* JADX WARN: Removed duplicated region for block: B:31:0x00dc A[SYNTHETIC] */
            @Override // java.lang.Runnable
            /*
                Code decompiled incorrectly, please refer to instructions dump.
            */
            public void run() {
                File file;
                int i2 = i;
                if (i2 == 1) {
                    Iterator it = Crashes.this.mUnprocessedErrorReports.keySet().iterator();
                    while (it.hasNext()) {
                        UUID uuid = (UUID) it.next();
                        it.remove();
                        Crashes.this.removeAllStoredErrorLogFiles(uuid);
                    }
                    return;
                }
                if (i2 == 2) {
                    SharedPreferencesManager.putBoolean(Crashes.PREF_KEY_ALWAYS_SEND, true);
                }
                Iterator it2 = Crashes.this.mUnprocessedErrorReports.entrySet().iterator();
                while (it2.hasNext()) {
                    Map.Entry entry = (Map.Entry) it2.next();
                    ErrorLogReport errorLogReport = (ErrorLogReport) entry.getValue();
                    ErrorAttachmentLog errorAttachmentLog = null;
                    if (errorLogReport.report.getThrowable() instanceof NativeException) {
                        Exception exception = errorLogReport.log.getException();
                        String minidumpFilePath = exception.getMinidumpFilePath();
                        exception.setMinidumpFilePath(null);
                        if (minidumpFilePath == null) {
                            minidumpFilePath = exception.getStackTrace();
                            exception.setStackTrace(null);
                        }
                        if (minidumpFilePath != null) {
                            File file2 = new File(minidumpFilePath);
                            errorAttachmentLog = ErrorAttachmentLog.attachmentWithBinary(FileManager.readBytes(file2), "minidump.dmp", "application/octet-stream");
                            file = file2;
                            ((AbstractAppCenterService) Crashes.this).mChannel.enqueue(errorLogReport.log, Crashes.ERROR_GROUP, 2);
                            if (errorAttachmentLog != null) {
                                Crashes.this.sendErrorAttachment(errorLogReport.log.getId(), Collections.singleton(errorAttachmentLog));
                                file.delete();
                            }
                            if (!Crashes.this.mAutomaticProcessing) {
                                Crashes.this.sendErrorAttachment(errorLogReport.log.getId(), Crashes.this.mCrashesListener.getErrorAttachments(errorLogReport.report));
                            }
                            it2.remove();
                            ErrorLogHelper.removeStoredErrorLogFile((UUID) entry.getKey());
                        } else {
                            AppCenterLog.warn(Crashes.LOG_TAG, "NativeException found without minidump.");
                        }
                    }
                    file = null;
                    ((AbstractAppCenterService) Crashes.this).mChannel.enqueue(errorLogReport.log, Crashes.ERROR_GROUP, 2);
                    if (errorAttachmentLog != null) {
                    }
                    if (!Crashes.this.mAutomaticProcessing) {
                    }
                    it2.remove();
                    ErrorLogHelper.removeStoredErrorLogFile((UUID) entry.getKey());
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendErrorAttachment(UUID uuid, Iterable<ErrorAttachmentLog> iterable) {
        if (iterable == null) {
            AppCenterLog.debug(LOG_TAG, "CrashesListener.getErrorAttachments returned null, no additional information will be attached to log: " + uuid.toString());
            return;
        }
        int i = 0;
        for (ErrorAttachmentLog errorAttachmentLog : iterable) {
            if (errorAttachmentLog != null) {
                errorAttachmentLog.setId(UUID.randomUUID());
                errorAttachmentLog.setErrorId(uuid);
                if (errorAttachmentLog.isValid()) {
                    i++;
                    this.mChannel.enqueue(errorAttachmentLog, ERROR_GROUP, 1);
                } else {
                    AppCenterLog.error(LOG_TAG, "Not all required fields are present in ErrorAttachmentLog.");
                }
            } else {
                AppCenterLog.warn(LOG_TAG, "Skipping null ErrorAttachmentLog in CrashesListener.getErrorAttachments.");
            }
        }
        if (i > 2) {
            AppCenterLog.warn(LOG_TAG, "A limit of 2 attachments per error report might be enforced by server.");
        }
    }

    @VisibleForTesting
    void setLogSerializer(LogSerializer logSerializer) {
        this.mLogSerializer = logSerializer;
    }

    void saveUncaughtException(Thread thread, Throwable th) {
        try {
            saveUncaughtException(thread, th, ErrorLogHelper.getModelExceptionFromThrowable(th));
        } catch (IOException e) {
            AppCenterLog.error(LOG_TAG, "Error writing error log to file", e);
        } catch (JSONException e2) {
            AppCenterLog.error(LOG_TAG, "Error serializing error log to JSON", e2);
        }
    }

    UUID saveUncaughtException(Thread thread, Throwable th, Exception exception) throws JSONException, IOException {
        if (!isEnabled().get().booleanValue() || this.mSavedUncaughtException) {
            return null;
        }
        this.mSavedUncaughtException = true;
        return saveErrorLogFiles(th, ErrorLogHelper.createErrorLog(this.mContext, thread, exception, Thread.getAllStackTraces(), this.mInitializeTimestamp, true));
    }

    @NonNull
    private UUID saveErrorLogFiles(Throwable th, ManagedErrorLog managedErrorLog) throws JSONException, IOException {
        File errorStorageDirectory = ErrorLogHelper.getErrorStorageDirectory();
        UUID id = managedErrorLog.getId();
        String uuid = id.toString();
        AppCenterLog.debug(LOG_TAG, "Saving uncaught exception.");
        File file = new File(errorStorageDirectory, uuid + ErrorLogHelper.ERROR_LOG_FILE_EXTENSION);
        FileManager.write(file, this.mLogSerializer.serializeLog(managedErrorLog));
        AppCenterLog.debug(LOG_TAG, "Saved JSON content for ingestion into " + file);
        File file2 = new File(errorStorageDirectory, uuid + ErrorLogHelper.THROWABLE_FILE_EXTENSION);
        if (th != null) {
            try {
                FileManager.writeObject(file2, th);
                AppCenterLog.debug(LOG_TAG, "Saved Throwable as is for client side inspection in " + file2 + " throwable:", th);
            } catch (StackOverflowError e) {
                AppCenterLog.error(LOG_TAG, "Failed to store throwable", e);
                th = null;
                file2.delete();
            }
        }
        if (th == null) {
            if (!file2.createNewFile()) {
                throw new IOException(file2.getName());
            }
            AppCenterLog.debug(LOG_TAG, "Saved empty Throwable file in " + file2);
        }
        return id;
    }

    void setAutomaticProcessing(boolean z) {
        this.mAutomaticProcessing = z;
    }

    AppCenterFuture<Collection<ErrorReport>> getUnprocessedErrorReports() {
        final DefaultAppCenterFuture defaultAppCenterFuture = new DefaultAppCenterFuture();
        postAsyncGetter(new Runnable() { // from class: com.microsoft.appcenter.crashes.Crashes.10
            @Override // java.lang.Runnable
            public void run() {
                ArrayList arrayList = new ArrayList(Crashes.this.mUnprocessedErrorReports.size());
                Iterator it = Crashes.this.mUnprocessedErrorReports.values().iterator();
                while (it.hasNext()) {
                    arrayList.add(((ErrorLogReport) it.next()).report);
                }
                defaultAppCenterFuture.complete(arrayList);
            }
        }, defaultAppCenterFuture, Collections.emptyList());
        return defaultAppCenterFuture;
    }

    AppCenterFuture<Boolean> sendCrashReportsOrAwaitUserConfirmation(final Collection<String> collection) {
        final DefaultAppCenterFuture defaultAppCenterFuture = new DefaultAppCenterFuture();
        postAsyncGetter(new Runnable() { // from class: com.microsoft.appcenter.crashes.Crashes.11
            @Override // java.lang.Runnable
            public void run() {
                Iterator it = Crashes.this.mUnprocessedErrorReports.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    UUID uuid = (UUID) entry.getKey();
                    String id = ((ErrorLogReport) entry.getValue()).report.getId();
                    Collection collection2 = collection;
                    if (collection2 != null && collection2.contains(id)) {
                        AppCenterLog.debug(Crashes.LOG_TAG, "CrashesListener.shouldProcess returned true, continue processing log: " + id);
                    } else {
                        AppCenterLog.debug(Crashes.LOG_TAG, "CrashesListener.shouldProcess returned false, clean up and ignore log: " + id);
                        Crashes.this.removeAllStoredErrorLogFiles(uuid);
                        it.remove();
                    }
                }
                defaultAppCenterFuture.complete(Boolean.valueOf(Crashes.this.sendCrashReportsOrAwaitUserConfirmation()));
            }
        }, defaultAppCenterFuture, false);
        return defaultAppCenterFuture;
    }

    void sendErrorAttachments(final String str, final Iterable<ErrorAttachmentLog> iterable) {
        post(new Runnable() { // from class: com.microsoft.appcenter.crashes.Crashes.12
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Crashes.this.sendErrorAttachment(UUID.fromString(str), iterable);
                } catch (RuntimeException unused) {
                    AppCenterLog.error(Crashes.LOG_TAG, "Error report identifier has an invalid format for sending attachments.");
                }
            }
        });
    }

    private static class DefaultCrashesListener extends AbstractCrashesListener {
        private DefaultCrashesListener() {
        }
    }

    private static class ErrorLogReport {
        private final ManagedErrorLog log;
        private final ErrorReport report;

        private ErrorLogReport(ManagedErrorLog managedErrorLog, ErrorReport errorReport) {
            this.log = managedErrorLog;
            this.report = errorReport;
        }
    }
}
