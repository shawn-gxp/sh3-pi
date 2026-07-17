package crc64a4555f9f70c213ae;

import com.microsoft.appcenter.crashes.CrashesListener;
import com.microsoft.appcenter.crashes.model.ErrorReport;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class Crashes_AndroidCrashListener implements IGCUserPeer, CrashesListener {
    public static final String __md_methods = "n_getErrorAttachments:(Lcom/microsoft/appcenter/crashes/model/ErrorReport;)Ljava/lang/Iterable;:GetGetErrorAttachments_Lcom_microsoft_appcenter_crashes_model_ErrorReport_Handler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\nn_onBeforeSending:(Lcom/microsoft/appcenter/crashes/model/ErrorReport;)V:GetOnBeforeSending_Lcom_microsoft_appcenter_crashes_model_ErrorReport_Handler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\nn_onSendingFailed:(Lcom/microsoft/appcenter/crashes/model/ErrorReport;Ljava/lang/Exception;)V:GetOnSendingFailed_Lcom_microsoft_appcenter_crashes_model_ErrorReport_Ljava_lang_Exception_Handler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\nn_onSendingSucceeded:(Lcom/microsoft/appcenter/crashes/model/ErrorReport;)V:GetOnSendingSucceeded_Lcom_microsoft_appcenter_crashes_model_ErrorReport_Handler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\nn_shouldAwaitUserConfirmation:()Z:GetShouldAwaitUserConfirmationHandler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\nn_shouldProcess:(Lcom/microsoft/appcenter/crashes/model/ErrorReport;)Z:GetShouldProcess_Lcom_microsoft_appcenter_crashes_model_ErrorReport_Handler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\n";
    private ArrayList refList;

    private native Iterable n_getErrorAttachments(ErrorReport errorReport);

    private native void n_onBeforeSending(ErrorReport errorReport);

    private native void n_onSendingFailed(ErrorReport errorReport, Exception exc);

    private native void n_onSendingSucceeded(ErrorReport errorReport);

    private native boolean n_shouldAwaitUserConfirmation();

    private native boolean n_shouldProcess(ErrorReport errorReport);

    static {
        Runtime.register("Microsoft.AppCenter.Crashes.Crashes+AndroidCrashListener, Microsoft.AppCenter.Crashes", Crashes_AndroidCrashListener.class, "n_getErrorAttachments:(Lcom/microsoft/appcenter/crashes/model/ErrorReport;)Ljava/lang/Iterable;:GetGetErrorAttachments_Lcom_microsoft_appcenter_crashes_model_ErrorReport_Handler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\nn_onBeforeSending:(Lcom/microsoft/appcenter/crashes/model/ErrorReport;)V:GetOnBeforeSending_Lcom_microsoft_appcenter_crashes_model_ErrorReport_Handler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\nn_onSendingFailed:(Lcom/microsoft/appcenter/crashes/model/ErrorReport;Ljava/lang/Exception;)V:GetOnSendingFailed_Lcom_microsoft_appcenter_crashes_model_ErrorReport_Ljava_lang_Exception_Handler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\nn_onSendingSucceeded:(Lcom/microsoft/appcenter/crashes/model/ErrorReport;)V:GetOnSendingSucceeded_Lcom_microsoft_appcenter_crashes_model_ErrorReport_Handler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\nn_shouldAwaitUserConfirmation:()Z:GetShouldAwaitUserConfirmationHandler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\nn_shouldProcess:(Lcom/microsoft/appcenter/crashes/model/ErrorReport;)Z:GetShouldProcess_Lcom_microsoft_appcenter_crashes_model_ErrorReport_Handler:Com.Microsoft.Appcenter.Crashes.ICrashesListenerInvoker, Microsoft.AppCenter.Crashes.Android.Bindings\n");
    }

    public Crashes_AndroidCrashListener() {
        if (Crashes_AndroidCrashListener.class == Crashes_AndroidCrashListener.class) {
            TypeManager.Activate("Microsoft.AppCenter.Crashes.Crashes+AndroidCrashListener, Microsoft.AppCenter.Crashes", "", this, new Object[0]);
        }
    }

    @Override // com.microsoft.appcenter.crashes.CrashesListener
    public Iterable getErrorAttachments(ErrorReport errorReport) {
        return n_getErrorAttachments(errorReport);
    }

    @Override // com.microsoft.appcenter.crashes.CrashesListener
    public void onBeforeSending(ErrorReport errorReport) {
        n_onBeforeSending(errorReport);
    }

    @Override // com.microsoft.appcenter.crashes.CrashesListener
    public void onSendingFailed(ErrorReport errorReport, Exception exc) {
        n_onSendingFailed(errorReport, exc);
    }

    @Override // com.microsoft.appcenter.crashes.CrashesListener
    public void onSendingSucceeded(ErrorReport errorReport) {
        n_onSendingSucceeded(errorReport);
    }

    @Override // com.microsoft.appcenter.crashes.CrashesListener
    public boolean shouldAwaitUserConfirmation() {
        return n_shouldAwaitUserConfirmation();
    }

    @Override // com.microsoft.appcenter.crashes.CrashesListener
    public boolean shouldProcess(ErrorReport errorReport) {
        return n_shouldProcess(errorReport);
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
