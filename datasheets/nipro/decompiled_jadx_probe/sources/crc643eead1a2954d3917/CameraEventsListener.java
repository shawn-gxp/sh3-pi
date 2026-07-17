package crc643eead1a2954d3917;

import android.hardware.Camera;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class CameraEventsListener implements IGCUserPeer, Camera.PreviewCallback, Camera.AutoFocusCallback {
    public static final String __md_methods = "n_onPreviewFrame:([BLandroid/hardware/Camera;)V:GetOnPreviewFrame_arrayBLandroid_hardware_Camera_Handler:ApxLabs.FastAndroidCamera.INonMarshalingPreviewCallbackInvoker, FastAndroidCamera, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\nn_onAutoFocus:(ZLandroid/hardware/Camera;)V:GetOnAutoFocus_ZLandroid_hardware_Camera_Handler:Android.Hardware.Camera/IAutoFocusCallbackInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_onAutoFocus(boolean z, Camera camera);

    private native void n_onPreviewFrame(byte[] bArr, Camera camera);

    static {
        Runtime.register("ZXing.Mobile.CameraAccess.CameraEventsListener, ZXingNetMobile", CameraEventsListener.class, __md_methods);
    }

    public CameraEventsListener() {
        if (CameraEventsListener.class == CameraEventsListener.class) {
            TypeManager.Activate("ZXing.Mobile.CameraAccess.CameraEventsListener, ZXingNetMobile", "", this, new Object[0]);
        }
    }

    @Override // android.hardware.Camera.PreviewCallback
    public void onPreviewFrame(byte[] bArr, Camera camera) {
        n_onPreviewFrame(bArr, camera);
    }

    @Override // android.hardware.Camera.AutoFocusCallback
    public void onAutoFocus(boolean z, Camera camera) {
        n_onAutoFocus(z, camera);
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
