package mono.com.microsoft.appcenter.channel;

import com.microsoft.appcenter.channel.Channel;
import com.microsoft.appcenter.ingestion.models.Log;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class Channel_GroupListenerImplementor implements IGCUserPeer, Channel.GroupListener {
    public static final String __md_methods = "n_onBeforeSending:(Lcom/microsoft/appcenter/ingestion/models/Log;)V:GetOnBeforeSending_Lcom_microsoft_appcenter_ingestion_models_Log_Handler:Com.Microsoft.Appcenter.Channel.IChannelGroupListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_onFailure:(Lcom/microsoft/appcenter/ingestion/models/Log;Ljava/lang/Exception;)V:GetOnFailure_Lcom_microsoft_appcenter_ingestion_models_Log_Ljava_lang_Exception_Handler:Com.Microsoft.Appcenter.Channel.IChannelGroupListenerInvoker, Microsoft.AppCenter.Android.Bindings\nn_onSuccess:(Lcom/microsoft/appcenter/ingestion/models/Log;)V:GetOnSuccess_Lcom_microsoft_appcenter_ingestion_models_Log_Handler:Com.Microsoft.Appcenter.Channel.IChannelGroupListenerInvoker, Microsoft.AppCenter.Android.Bindings\n";
    private ArrayList refList;

    private native void n_onBeforeSending(Log log);

    private native void n_onFailure(Log log, Exception exc);

    private native void n_onSuccess(Log log);

    static {
        Runtime.register("Com.Microsoft.Appcenter.Channel.IChannelGroupListenerImplementor, Microsoft.AppCenter.Android.Bindings", Channel_GroupListenerImplementor.class, __md_methods);
    }

    public Channel_GroupListenerImplementor() {
        if (Channel_GroupListenerImplementor.class == Channel_GroupListenerImplementor.class) {
            TypeManager.Activate("Com.Microsoft.Appcenter.Channel.IChannelGroupListenerImplementor, Microsoft.AppCenter.Android.Bindings", "", this, new Object[0]);
        }
    }

    @Override // com.microsoft.appcenter.channel.Channel.GroupListener
    public void onBeforeSending(Log log) {
        n_onBeforeSending(log);
    }

    @Override // com.microsoft.appcenter.channel.Channel.GroupListener
    public void onFailure(Log log, Exception exc) {
        n_onFailure(log, exc);
    }

    @Override // com.microsoft.appcenter.channel.Channel.GroupListener
    public void onSuccess(Log log) {
        n_onSuccess(log);
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
