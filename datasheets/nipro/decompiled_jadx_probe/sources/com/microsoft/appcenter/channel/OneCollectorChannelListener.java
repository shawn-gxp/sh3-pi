package com.microsoft.appcenter.channel;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.channel.Channel;
import com.microsoft.appcenter.ingestion.Ingestion;
import com.microsoft.appcenter.ingestion.OneCollectorIngestion;
import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.json.LogSerializer;
import com.microsoft.appcenter.ingestion.models.one.CommonSchemaLog;
import com.microsoft.appcenter.ingestion.models.one.SdkExtension;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.UUIDUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/* loaded from: classes.dex */
public class OneCollectorChannelListener extends AbstractChannelListener {

    @VisibleForTesting
    static final String ONE_COLLECTOR_GROUP_NAME_SUFFIX = "/one";

    @VisibleForTesting
    static final int ONE_COLLECTOR_TRIGGER_COUNT = 50;

    @VisibleForTesting
    static final int ONE_COLLECTOR_TRIGGER_MAX_PARALLEL_REQUESTS = 2;
    private final Channel mChannel;
    private final Map<String, EpochAndSeq> mEpochsAndSeqsByIKey;
    private final Ingestion mIngestion;
    private final UUID mInstallId;
    private final LogSerializer mLogSerializer;

    public OneCollectorChannelListener(@NonNull Context context, @NonNull Channel channel, @NonNull LogSerializer logSerializer, @NonNull UUID uuid) {
        this(new OneCollectorIngestion(context, logSerializer), channel, logSerializer, uuid);
    }

    @VisibleForTesting
    OneCollectorChannelListener(@NonNull OneCollectorIngestion oneCollectorIngestion, @NonNull Channel channel, @NonNull LogSerializer logSerializer, @NonNull UUID uuid) {
        this.mEpochsAndSeqsByIKey = new HashMap();
        this.mChannel = channel;
        this.mLogSerializer = logSerializer;
        this.mInstallId = uuid;
        this.mIngestion = oneCollectorIngestion;
    }

    public void setLogUrl(@NonNull String str) {
        this.mIngestion.setLogUrl(str);
    }

    @Override // com.microsoft.appcenter.channel.AbstractChannelListener, com.microsoft.appcenter.channel.Channel.Listener
    public void onGroupAdded(@NonNull String str, Channel.GroupListener groupListener, long j) {
        if (isOneCollectorGroup(str)) {
            return;
        }
        this.mChannel.addGroup(getOneCollectorGroupName(str), 50, j, 2, this.mIngestion, groupListener);
    }

    @Override // com.microsoft.appcenter.channel.AbstractChannelListener, com.microsoft.appcenter.channel.Channel.Listener
    public void onGroupRemoved(@NonNull String str) {
        if (isOneCollectorGroup(str)) {
            return;
        }
        this.mChannel.removeGroup(getOneCollectorGroupName(str));
    }

    @Override // com.microsoft.appcenter.channel.AbstractChannelListener, com.microsoft.appcenter.channel.Channel.Listener
    public void onPreparedLog(@NonNull Log log, @NonNull String str, int i) {
        if (isOneCollectorCompatible(log)) {
            try {
                Collection<CommonSchemaLog> commonSchemaLog = this.mLogSerializer.toCommonSchemaLog(log);
                for (CommonSchemaLog commonSchemaLog2 : commonSchemaLog) {
                    commonSchemaLog2.setFlags(Long.valueOf(i));
                    EpochAndSeq epochAndSeq = this.mEpochsAndSeqsByIKey.get(commonSchemaLog2.getIKey());
                    if (epochAndSeq == null) {
                        epochAndSeq = new EpochAndSeq(UUIDUtils.randomUUID().toString());
                        this.mEpochsAndSeqsByIKey.put(commonSchemaLog2.getIKey(), epochAndSeq);
                    }
                    SdkExtension sdk = commonSchemaLog2.getExt().getSdk();
                    sdk.setEpoch(epochAndSeq.epoch);
                    long j = epochAndSeq.seq + 1;
                    epochAndSeq.seq = j;
                    sdk.setSeq(Long.valueOf(j));
                    sdk.setInstallId(this.mInstallId);
                }
                String oneCollectorGroupName = getOneCollectorGroupName(str);
                Iterator<CommonSchemaLog> it = commonSchemaLog.iterator();
                while (it.hasNext()) {
                    this.mChannel.enqueue(it.next(), oneCollectorGroupName, i);
                }
            } catch (IllegalArgumentException e) {
                AppCenterLog.error("AppCenter", "Cannot send a log to one collector: " + e.getMessage());
            }
        }
    }

    @Override // com.microsoft.appcenter.channel.AbstractChannelListener, com.microsoft.appcenter.channel.Channel.Listener
    public boolean shouldFilter(@NonNull Log log) {
        return isOneCollectorCompatible(log);
    }

    private static String getOneCollectorGroupName(@NonNull String str) {
        return str + ONE_COLLECTOR_GROUP_NAME_SUFFIX;
    }

    @Override // com.microsoft.appcenter.channel.AbstractChannelListener, com.microsoft.appcenter.channel.Channel.Listener
    public void onClear(@NonNull String str) {
        if (isOneCollectorGroup(str)) {
            return;
        }
        this.mChannel.clear(getOneCollectorGroupName(str));
    }

    @Override // com.microsoft.appcenter.channel.AbstractChannelListener, com.microsoft.appcenter.channel.Channel.Listener
    public void onPaused(@NonNull String str, String str2) {
        if (isOneCollectorGroup(str)) {
            return;
        }
        this.mChannel.pauseGroup(getOneCollectorGroupName(str), str2);
    }

    @Override // com.microsoft.appcenter.channel.AbstractChannelListener, com.microsoft.appcenter.channel.Channel.Listener
    public void onResumed(@NonNull String str, String str2) {
        if (isOneCollectorGroup(str)) {
            return;
        }
        this.mChannel.resumeGroup(getOneCollectorGroupName(str), str2);
    }

    private static boolean isOneCollectorGroup(@NonNull String str) {
        return str.endsWith(ONE_COLLECTOR_GROUP_NAME_SUFFIX);
    }

    private static boolean isOneCollectorCompatible(@NonNull Log log) {
        return ((log instanceof CommonSchemaLog) || log.getTransmissionTargetTokens().isEmpty()) ? false : true;
    }

    @Override // com.microsoft.appcenter.channel.AbstractChannelListener, com.microsoft.appcenter.channel.Channel.Listener
    public void onGloballyEnabled(boolean z) {
        if (z) {
            return;
        }
        this.mEpochsAndSeqsByIKey.clear();
    }

    private static class EpochAndSeq {
        final String epoch;
        long seq;

        EpochAndSeq(String str) {
            this.epoch = str;
        }
    }
}
