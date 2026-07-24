package jp.co.nipro.cocoron.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jp.co.nipro.cocoron.common.Config;
import jp.co.nipro.cocoron.data.FileRecorder;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import kotlinx.coroutines.BuildersKt__BuildersKt;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;

/* compiled from: BluetoothLeService.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000/\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002*\u0001\u0000\b\n\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J \u0010\b\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\nH\u0016J$\u0010\u000b\u001a\u00020\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0006\u0010\t\u001a\u00020\nH\u0016J \u0010\f\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\r\u001a\u00020\nH\u0016J$\u0010\u000e\u001a\u00020\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\u0010\u000f\u001a\u0004\u0018\u00010\u00102\u0006\u0010\t\u001a\u00020\nH\u0016J\u0018\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\t\u001a\u00020\nH\u0016¨\u0006\u0012"}, d2 = {"jp/co/nipro/cocoron/service/BluetoothLeService$gattCallback$1", "Landroid/bluetooth/BluetoothGattCallback;", "onCharacteristicChanged", "", "gatt", "Landroid/bluetooth/BluetoothGatt;", "characteristic", "Landroid/bluetooth/BluetoothGattCharacteristic;", "onCharacteristicRead", NotificationCompat.CATEGORY_STATUS, "", "onCharacteristicWrite", "onConnectionStateChange", "newState", "onDescriptorWrite", "descriptor", "Landroid/bluetooth/BluetoothGattDescriptor;", "onServicesDiscovered", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class BluetoothLeService$gattCallback$1 extends BluetoothGattCallback {
    final /* synthetic */ BluetoothLeService this$0;

    BluetoothLeService$gattCallback$1(BluetoothLeService bluetoothLeService) {
        this.this$0 = bluetoothLeService;
    }

    @Override // android.bluetooth.BluetoothGattCallback
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Intrinsics.checkNotNullParameter(gatt, "gatt");
        Log.d(BluetoothLeService.TAG, "onConnectionStateChange newState:" + newState + " status:" + status);
        if (newState == 2) {
            if (!this.this$0.getAutoReconnect()) {
                this.this$0.getFileRecorder().writeDisconnectGatt(gatt);
                gatt.close();
                gatt.disconnect();
                this.this$0.setBluetoothGatt((BluetoothGatt) null);
                return;
            }
            this.this$0.getFileRecorder().writeText("ON CONNECT:" + gatt);
            this.this$0.setBluetoothGatt(gatt);
            gatt.discoverServices();
            String connectName = Config.INSTANCE.getConnectName();
            Intrinsics.checkNotNullExpressionValue(gatt.getDevice(), "gatt.device");
            if (!Intrinsics.areEqual(connectName, r10.getName())) {
                FileRecorder fileRecorder = this.this$0.getFileRecorder();
                BluetoothDevice device = gatt.getDevice();
                Intrinsics.checkNotNullExpressionValue(device, "gatt.device");
                String name = device.getName();
                Intrinsics.checkNotNullExpressionValue(name, "gatt.device.name");
                fileRecorder.writePeripheralName(name);
            }
            BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new BluetoothLeService$gattCallback$1$onConnectionStateChange$1(this, null), 2, null);
            return;
        }
        if ((status != 133 && !Config.INSTANCE.getUSE_ECG_SIM()) || newState != 0) {
            if (newState == 0) {
                this.this$0.getFileRecorder().writeText("ON DISCONNECT:" + gatt + " NO RECONNECT");
                return;
            }
            return;
        }
        this.this$0.getFileRecorder().writeText("ON DISCONNECT:" + gatt + " NEED RECONNECT");
        if (this.this$0.getAutoReconnect()) {
            BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new BluetoothLeService$gattCallback$1$onConnectionStateChange$2(this, this, null), 2, null);
        }
        this.this$0.setConfigCharacteristic((BluetoothGattCharacteristic) null);
    }

    @Override // android.bluetooth.BluetoothGattCallback
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Intrinsics.checkNotNullParameter(gatt, "gatt");
        Log.d(BluetoothLeService.TAG, "onServicesDiscovered state:" + status);
        if (status == 0 && !(!Intrinsics.areEqual(gatt, this.this$0.getBluetoothGatt()))) {
            this.this$0.getDescriptors().clear();
            for (BluetoothGattService gattService : gatt.getServices()) {
                Intrinsics.checkNotNullExpressionValue(gattService, "gattService");
                for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                    Intrinsics.checkNotNullExpressionValue(gattCharacteristic, "gattCharacteristic");
                    String uuid = gattCharacteristic.getUuid().toString();
                    Intrinsics.checkNotNullExpressionValue(uuid, "gattCharacteristic.uuid.toString()");
                    Objects.requireNonNull(uuid, "null cannot be cast to non-null type java.lang.String");
                    String upperCase = uuid.toUpperCase();
                    Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
                    if (Intrinsics.areEqual(upperCase, BluetoothLeService.ECG) || Intrinsics.areEqual(upperCase, BluetoothLeService.RRT) || Intrinsics.areEqual(upperCase, BluetoothLeService.BATTERY)) {
                        gatt.setCharacteristicNotification(gattCharacteristic, true);
                        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(UUID.fromString(BluetoothLeService.UUID_NOTIFY));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            this.this$0.getDescriptors().add(descriptor);
                        }
                    }
                    if (Intrinsics.areEqual(upperCase, BluetoothLeService.CONFIG)) {
                        this.this$0.setConfigCharacteristic(gattCharacteristic);
                    } else if (Intrinsics.areEqual(upperCase, BluetoothLeService.DATETIME)) {
                        this.this$0.setDateTimeCharacteristic(gattCharacteristic);
                    }
                }
            }
            BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new BluetoothLeService$gattCallback$1$onServicesDiscovered$2(this, gatt, null), 2, null);
        }
    }

    @Override // android.bluetooth.BluetoothGattCallback
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Intrinsics.checkNotNullParameter(gatt, "gatt");
        Intrinsics.checkNotNullParameter(characteristic, "characteristic");
        StringBuilder sb = new StringBuilder();
        sb.append("onCharacteristicRead : ");
        byte[] value = characteristic.getValue();
        Intrinsics.checkNotNullExpressionValue(value, "characteristic.value");
        String joinToString$default = ArraysKt.joinToString$default(value, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicRead$1
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ CharSequence invoke(Byte b) {
                return invoke(b.byteValue());
            }

            public final CharSequence invoke(byte b) {
                String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
                Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
                return format;
            }
        }, 30, (Object) null);
        Objects.requireNonNull(joinToString$default, "null cannot be cast to non-null type java.lang.String");
        String upperCase = joinToString$default.toUpperCase();
        Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
        sb.append(upperCase);
        Log.d(BluetoothLeService.TAG, sb.toString());
    }

    @Override // android.bluetooth.BluetoothGattCallback
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Intrinsics.checkNotNullParameter(gatt, "gatt");
        Intrinsics.checkNotNullParameter(characteristic, "characteristic");
        BuildersKt__BuildersKt.runBlocking$default(null, new BluetoothLeService$gattCallback$1$onCharacteristicChanged$1(this, characteristic, null), 1, null);
    }

    @Override // android.bluetooth.BluetoothGattCallback
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        StringBuilder sb = new StringBuilder();
        sb.append("onCharacteristicWrite : ");
        sb.append(String.valueOf(characteristic != null ? characteristic.getUuid() : null));
        sb.append(" status:");
        sb.append(status);
        Log.d(BluetoothLeService.TAG, sb.toString());
        String valueOf = String.valueOf(characteristic != null ? characteristic.getUuid() : null);
        Objects.requireNonNull(valueOf, "null cannot be cast to non-null type java.lang.String");
        String upperCase = valueOf.toUpperCase();
        Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
        if (Intrinsics.areEqual(upperCase, BluetoothLeService.CONFIG) && status == 0) {
            this.this$0.setSended_config(true);
            if (!this.this$0.getSended_time()) {
                BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new BluetoothLeService$gattCallback$1$onCharacteristicWrite$1(this, null), 2, null);
            }
        }
        String valueOf2 = String.valueOf(characteristic != null ? characteristic.getUuid() : null);
        Objects.requireNonNull(valueOf2, "null cannot be cast to non-null type java.lang.String");
        String upperCase2 = valueOf2.toUpperCase();
        Intrinsics.checkNotNullExpressionValue(upperCase2, "(this as java.lang.String).toUpperCase()");
        if (Intrinsics.areEqual(upperCase2, BluetoothLeService.DATETIME) && status == 0) {
            this.this$0.setSended_time(true);
        }
    }

    @Override // android.bluetooth.BluetoothGattCallback
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        BluetoothGattCharacteristic characteristic;
        super.onDescriptorWrite(gatt, descriptor, status);
        StringBuilder sb = new StringBuilder();
        sb.append("onDescriptorWrite : ");
        String valueOf = String.valueOf((descriptor == null || (characteristic = descriptor.getCharacteristic()) == null) ? null : characteristic.getUuid());
        Objects.requireNonNull(valueOf, "null cannot be cast to non-null type java.lang.String");
        String upperCase = valueOf.toUpperCase();
        Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
        sb.append(upperCase);
        sb.append(' ');
        sb.append(status == 0 ? "Sucess" : "false");
        Log.d(BluetoothLeService.TAG, sb.toString());
        if (status == 0) {
            ArrayList<BluetoothGattDescriptor> descriptors = this.this$0.getDescriptors();
            Objects.requireNonNull(descriptors, "null cannot be cast to non-null type kotlin.collections.MutableCollection<T>");
            TypeIntrinsics.asMutableCollection(descriptors).remove(descriptor);
        }
        BluetoothGattDescriptor bluetoothGattDescriptor = (BluetoothGattDescriptor) CollectionsKt.firstOrNull((List) this.this$0.getDescriptors());
        if (bluetoothGattDescriptor != null) {
            BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new BluetoothLeService$gattCallback$1$onDescriptorWrite$$inlined$apply$lambda$1(bluetoothGattDescriptor, null, gatt), 2, null);
            if (bluetoothGattDescriptor != null) {
                return;
            }
        }
        if (!this.this$0.getSended_time()) {
            BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new BluetoothLeService$gattCallback$1$onDescriptorWrite$$inlined$run$lambda$1(null, this), 2, null);
        }
        Unit unit = Unit.INSTANCE;
    }
}
