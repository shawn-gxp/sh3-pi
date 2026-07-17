package crc640d7c6d57b8a5f296;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class Adapter_Api21BleScanCallback extends ScanCallback implements IGCUserPeer {
    public static final String __md_methods = "n_onScanFailed:(I)V:GetOnScanFailed_IHandler\nn_onScanResult:(ILandroid/bluetooth/le/ScanResult;)V:GetOnScanResult_ILandroid_bluetooth_le_ScanResult_Handler\n";
    private ArrayList refList;

    private native void n_onScanFailed(int i);

    private native void n_onScanResult(int i, ScanResult scanResult);

    static {
        Runtime.register("Plugin.BLE.Android.Adapter+Api21BleScanCallback, Plugin.BLE", Adapter_Api21BleScanCallback.class, __md_methods);
    }

    public Adapter_Api21BleScanCallback() {
        if (Adapter_Api21BleScanCallback.class == Adapter_Api21BleScanCallback.class) {
            TypeManager.Activate("Plugin.BLE.Android.Adapter+Api21BleScanCallback, Plugin.BLE", "", this, new Object[0]);
        }
    }

    @Override // android.bluetooth.le.ScanCallback
    public void onScanFailed(int i) {
        n_onScanFailed(i);
    }

    @Override // android.bluetooth.le.ScanCallback
    public void onScanResult(int i, ScanResult scanResult) {
        n_onScanResult(i, scanResult);
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
