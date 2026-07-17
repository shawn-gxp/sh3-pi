package crc64357c0806a054c747;

import android.content.Intent;
import android.os.Bundle;
import crc643f46942d9dd1fff9.FormsAppCompatActivity;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class MainActivity extends FormsAppCompatActivity implements IGCUserPeer {
    public static final String __md_methods = "n_onRequestPermissionsResult:(I[Ljava/lang/String;[I)V:GetOnRequestPermissionsResult_IarrayLjava_lang_String_arrayIHandler\nn_onCreate:(Landroid/os/Bundle;)V:GetOnCreate_Landroid_os_Bundle_Handler\nn_onResume:()V:GetOnResumeHandler\nn_onActivityResult:(IILandroid/content/Intent;)V:GetOnActivityResult_IILandroid_content_Intent_Handler\nn_onPause:()V:GetOnPauseHandler\nn_onDestroy:()V:GetOnDestroyHandler\nn_onRestart:()V:GetOnRestartHandler\n";
    private ArrayList refList;

    private native void n_onActivityResult(int i, int i2, Intent intent);

    private native void n_onCreate(Bundle bundle);

    private native void n_onDestroy();

    private native void n_onPause();

    private native void n_onRequestPermissionsResult(int i, String[] strArr, int[] iArr);

    private native void n_onRestart();

    private native void n_onResume();

    static {
        Runtime.register("NHL.Droid.MainActivity, NHL.Android", MainActivity.class, __md_methods);
    }

    public MainActivity() {
        if (MainActivity.class == MainActivity.class) {
            TypeManager.Activate("NHL.Droid.MainActivity, NHL.Android", "", this, new Object[0]);
        }
    }

    public MainActivity(int i) {
        super(i);
        if (MainActivity.class == MainActivity.class) {
            TypeManager.Activate("NHL.Droid.MainActivity, NHL.Android", "System.Int32, mscorlib", this, new Object[]{Integer.valueOf(i)});
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        n_onRequestPermissionsResult(i, strArr, iArr);
    }

    @Override // crc643f46942d9dd1fff9.FormsAppCompatActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        n_onCreate(bundle);
    }

    @Override // crc643f46942d9dd1fff9.FormsAppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        n_onResume();
    }

    @Override // crc643f46942d9dd1fff9.FormsAppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        n_onActivityResult(i, i2, intent);
    }

    @Override // crc643f46942d9dd1fff9.FormsAppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onPause() {
        n_onPause();
    }

    @Override // crc643f46942d9dd1fff9.FormsAppCompatActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        n_onDestroy();
    }

    @Override // crc643f46942d9dd1fff9.FormsAppCompatActivity, android.app.Activity
    public void onRestart() {
        n_onRestart();
    }

    @Override // crc643f46942d9dd1fff9.FormsAppCompatActivity, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc643f46942d9dd1fff9.FormsAppCompatActivity, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
