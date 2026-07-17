package crc640d7c6d57b8a5f296;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class Adapter_Api18BleScanCallback implements IGCUserPeer, BluetoothAdapter.LeScanCallback {
    public static final String __md_methods = "n_onLeScan:(Landroid/bluetooth/BluetoothDevice;I[B)V:GetOnLeScan_Landroid_bluetooth_BluetoothDevice_IarrayBHandler:Android.Bluetooth.BluetoothAdapter/ILeScanCallbackInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr);

    static {
        Runtime.register("Plugin.BLE.Android.Adapter+Api18BleScanCallback, Plugin.BLE", Adapter_Api18BleScanCallback.class, __md_methods);
    }

    public Adapter_Api18BleScanCallback() {
        if (Adapter_Api18BleScanCallback.class == Adapter_Api18BleScanCallback.class) {
            TypeManager.Activate("Plugin.BLE.Android.Adapter+Api18BleScanCallback, Plugin.BLE", "", this, new Object[0]);
        }
    }

    @Override // android.bluetooth.BluetoothAdapter.LeScanCallback
    public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
        n_onLeScan(bluetoothDevice, i, bArr);
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
