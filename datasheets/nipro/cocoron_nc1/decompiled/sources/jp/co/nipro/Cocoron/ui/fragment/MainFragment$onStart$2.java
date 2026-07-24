package jp.co.nipro.cocoron.ui.fragment;

import jp.co.nipro.cocoron.service.BluetoothLeService;
import kotlin.Metadata;
import kotlin.jvm.internal.MutablePropertyReference0Impl;

/* compiled from: MainFragment.kt */
@Metadata(bv = {1, 0, 3}, k = 3, mv = {1, 4, 2})
/* loaded from: classes.dex */
final /* synthetic */ class MainFragment$onStart$2 extends MutablePropertyReference0Impl {
    MainFragment$onStart$2(MainFragment mainFragment) {
        super(mainFragment, MainFragment.class, "bluetoothLeService", "getBluetoothLeService()Ljp/co/nipro/cocoron/service/BluetoothLeService;", 0);
    }

    @Override // kotlin.jvm.internal.MutablePropertyReference0Impl, kotlin.reflect.KProperty0
    public Object get() {
        return ((MainFragment) this.receiver).getBluetoothLeService();
    }

    @Override // kotlin.jvm.internal.MutablePropertyReference0Impl, kotlin.reflect.KMutableProperty0
    public void set(Object obj) {
        ((MainFragment) this.receiver).setBluetoothLeService((BluetoothLeService) obj);
    }
}
