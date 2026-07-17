package crc64ea80555991f994ce;

import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class OnCompleteListener implements IGCUserPeer, com.google.android.gms.tasks.OnCompleteListener {
    public static final String __md_methods = "n_onComplete:(Lcom/google/android/gms/tasks/Task;)V:GetOnComplete_Lcom_google_android_gms_tasks_Task_Handler:Android.Gms.Tasks.IOnCompleteListenerInvoker, Xamarin.GooglePlayServices.Tasks\n";
    private ArrayList refList;

    private native void n_onComplete(Task task);

    static {
        Runtime.register("NHL.Droid.DependencyService.OnCompleteListener, NHL.Android", OnCompleteListener.class, "n_onComplete:(Lcom/google/android/gms/tasks/Task;)V:GetOnComplete_Lcom_google_android_gms_tasks_Task_Handler:Android.Gms.Tasks.IOnCompleteListenerInvoker, Xamarin.GooglePlayServices.Tasks\n");
    }

    public OnCompleteListener() {
        if (OnCompleteListener.class == OnCompleteListener.class) {
            TypeManager.Activate("NHL.Droid.DependencyService.OnCompleteListener, NHL.Android", "", this, new Object[0]);
        }
    }

    @Override // com.google.android.gms.tasks.OnCompleteListener
    public void onComplete(Task task) {
        n_onComplete(task);
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
