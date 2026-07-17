package crc6495d4f5d63cc5c882;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class AwaitableTaskCompleteListener_1 implements IGCUserPeer, OnCompleteListener {
    public static final String __md_methods = "n_onComplete:(Lcom/google/android/gms/tasks/Task;)V:GetOnComplete_Lcom_google_android_gms_tasks_Task_Handler:Android.Gms.Tasks.IOnCompleteListenerInvoker, Xamarin.GooglePlayServices.Tasks\n";
    private ArrayList refList;

    private native void n_onComplete(Task task);

    static {
        Runtime.register("Android.Gms.Extensions.AwaitableTaskCompleteListener`1, Xamarin.GooglePlayServices.Tasks", AwaitableTaskCompleteListener_1.class, "n_onComplete:(Lcom/google/android/gms/tasks/Task;)V:GetOnComplete_Lcom_google_android_gms_tasks_Task_Handler:Android.Gms.Tasks.IOnCompleteListenerInvoker, Xamarin.GooglePlayServices.Tasks\n");
    }

    public AwaitableTaskCompleteListener_1() {
        if (AwaitableTaskCompleteListener_1.class == AwaitableTaskCompleteListener_1.class) {
            TypeManager.Activate("Android.Gms.Extensions.AwaitableTaskCompleteListener`1, Xamarin.GooglePlayServices.Tasks", "", this, new Object[0]);
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
