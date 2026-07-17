package mono.com.microsoft.appcenter.analytics.channel;

import com.microsoft.appcenter.analytics.channel.AnalyticsListener;
import com.microsoft.appcenter.ingestion.models.Log;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class AnalyticsListenerImplementor implements IGCUserPeer, AnalyticsListener {
    public static final String __md_methods = "n_onBeforeSending:(Lcom/microsoft/appcenter/ingestion/models/Log;)V:GetOnBeforeSending_Lcom_microsoft_appcenter_ingestion_models_Log_Handler:Com.Microsoft.Appcenter.Analytics.Channel.IAnalyticsListenerInvoker, Microsoft.AppCenter.Analytics.Android.Bindings\nn_onSendingFailed:(Lcom/microsoft/appcenter/ingestion/models/Log;Ljava/lang/Exception;)V:GetOnSendingFailed_Lcom_microsoft_appcenter_ingestion_models_Log_Ljava_lang_Exception_Handler:Com.Microsoft.Appcenter.Analytics.Channel.IAnalyticsListenerInvoker, Microsoft.AppCenter.Analytics.Android.Bindings\nn_onSendingSucceeded:(Lcom/microsoft/appcenter/ingestion/models/Log;)V:GetOnSendingSucceeded_Lcom_microsoft_appcenter_ingestion_models_Log_Handler:Com.Microsoft.Appcenter.Analytics.Channel.IAnalyticsListenerInvoker, Microsoft.AppCenter.Analytics.Android.Bindings\n";
    private ArrayList refList;

    private native void n_onBeforeSending(Log log);

    private native void n_onSendingFailed(Log log, Exception exc);

    private native void n_onSendingSucceeded(Log log);

    static {
        Runtime.register("Com.Microsoft.Appcenter.Analytics.Channel.IAnalyticsListenerImplementor, Microsoft.AppCenter.Analytics.Android.Bindings", AnalyticsListenerImplementor.class, __md_methods);
    }

    public AnalyticsListenerImplementor() {
        if (AnalyticsListenerImplementor.class == AnalyticsListenerImplementor.class) {
            TypeManager.Activate("Com.Microsoft.Appcenter.Analytics.Channel.IAnalyticsListenerImplementor, Microsoft.AppCenter.Analytics.Android.Bindings", "", this, new Object[0]);
        }
    }

    @Override // com.microsoft.appcenter.analytics.channel.AnalyticsListener
    public void onBeforeSending(Log log) {
        n_onBeforeSending(log);
    }

    @Override // com.microsoft.appcenter.analytics.channel.AnalyticsListener
    public void onSendingFailed(Log log, Exception exc) {
        n_onSendingFailed(log, exc);
    }

    @Override // com.microsoft.appcenter.analytics.channel.AnalyticsListener
    public void onSendingSucceeded(Log log) {
        n_onSendingSucceeded(log);
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
