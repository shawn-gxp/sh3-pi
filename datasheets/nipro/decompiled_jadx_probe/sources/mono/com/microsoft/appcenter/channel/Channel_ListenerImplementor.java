package mono.com.microsoft.appcenter.channel;

import com.microsoft.appcenter.channel.Channel;
import com.microsoft.appcenter.ingestion.models.Log;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class Channel_ListenerImplementor implements IGCUserPeer, Channel.Listener {
    public static final String __md_methods = "n_onClear:(Ljava/lang/String;)V:GetOnClear_Ljava_lang_String_Handler:Com.Microsoft.Appcenter.Channel.IChannelListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_onGloballyEnabled:(Z)V:GetOnGloballyEnabled_ZHandler:Com.Microsoft.Appcenter.Channel.IChannelListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_onGroupAdded:(Ljava/lang/String;Lcom/microsoft/appcenter/channel/Channel$GroupListener;J)V:GetOnGroupAdded_Ljava_lang_String_Lcom_microsoft_appcenter_channel_Channel_GroupListener_JHandler:Com.Microsoft.Appcenter.Channel.IChannelListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_onGroupRemoved:(Ljava/lang/String;)V:GetOnGroupRemoved_Ljava_lang_String_Handler:Com.Microsoft.Appcenter.Channel.IChannelListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_onPaused:(Ljava/lang/String;Ljava/lang/String;)V:GetOnPaused_Ljava_lang_String_Ljava_lang_String_Handler:Com.Microsoft.Appcenter.Channel.IChannelListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_onPreparedLog:(Lcom/microsoft/appcenter/ingestion/models/Log;Ljava/lang/String;I)V:GetOnPreparedLog_Lcom_microsoft_appcenter_ingestion_models_Log_Ljava_lang_String_IHandler:Com.Microsoft.Appcenter.Channel.IChannelListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_onPreparingLog:(Lcom/microsoft/appcenter/ingestion/models/Log;Ljava/lang/String;)V:GetOnPreparingLog_Lcom_microsoft_appcenter_ingestion_models_Log_Ljava_lang_String_Handler:Com.Microsoft.Appcenter.Channel.IChannelListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_onResumed:(Ljava/lang/String;Ljava/lang/String;)V:GetOnResumed_Ljava_lang_String_Ljava_lang_String_Handler:Com.Microsoft.Appcenter.Channel.IChannelListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_shouldFilter:(Lcom/microsoft/appcenter/ingestion/models/Log;)Z:GetShouldFilter_Lcom_microsoft_appcenter_ingestion_models_Log_Handler:Com.Microsoft.Appcenter.Channel.IChannelListenerInvoker, Microsoft.AppCenter.Android.Bindings\n";
    private ArrayList refList;

    private native void n_onClear(String str);

    private native void n_onGloballyEnabled(boolean z);

    private native void n_onGroupAdded(String str, Channel.GroupListener groupListener, long j);

    private native void n_onGroupRemoved(String str);

    private native void n_onPaused(String str, String str2);

    private native void n_onPreparedLog(Log log, String str, int i);

    private native void n_onPreparingLog(Log log, String str);

    private native void n_onResumed(String str, String str2);

    private native boolean n_shouldFilter(Log log);

    static {
        Runtime.register("Com.Microsoft.Appcenter.Channel.IChannelListenerImplementor, Microsoft.AppCenter.Android.Bindings", Channel_ListenerImplementor.class, __md_methods);
    }

    public Channel_ListenerImplementor() {
        if (Channel_ListenerImplementor.class == Channel_ListenerImplementor.class) {
            TypeManager.Activate("Com.Microsoft.Appcenter.Channel.IChannelListenerImplementor, Microsoft.AppCenter.Android.Bindings", "", this, new Object[0]);
        }
    }

    @Override // com.microsoft.appcenter.channel.Channel.Listener
    public void onClear(String str) {
        n_onClear(str);
    }

    @Override // com.microsoft.appcenter.channel.Channel.Listener
    public void onGloballyEnabled(boolean z) {
        n_onGloballyEnabled(z);
    }

    @Override // com.microsoft.appcenter.channel.Channel.Listener
    public void onGroupAdded(String str, Channel.GroupListener groupListener, long j) {
        n_onGroupAdded(str, groupListener, j);
    }

    @Override // com.microsoft.appcenter.channel.Channel.Listener
    public void onGroupRemoved(String str) {
        n_onGroupRemoved(str);
    }

    @Override // com.microsoft.appcenter.channel.Channel.Listener
    public void onPaused(String str, String str2) {
        n_onPaused(str, str2);
    }

    @Override // com.microsoft.appcenter.channel.Channel.Listener
    public void onPreparedLog(Log log, String str, int i) {
        n_onPreparedLog(log, str, i);
    }

    @Override // com.microsoft.appcenter.channel.Channel.Listener
    public void onPreparingLog(Log log, String str) {
        n_onPreparingLog(log, str);
    }

    @Override // com.microsoft.appcenter.channel.Channel.Listener
    public void onResumed(String str, String str2) {
        n_onResumed(str, str2);
    }

    @Override // com.microsoft.appcenter.channel.Channel.Listener
    public boolean shouldFilter(Log log) {
        return n_shouldFilter(log);
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
