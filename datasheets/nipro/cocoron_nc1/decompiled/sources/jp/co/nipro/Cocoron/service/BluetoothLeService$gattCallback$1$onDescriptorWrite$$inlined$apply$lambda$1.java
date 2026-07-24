package jp.co.nipro.cocoron.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;
import java.util.Objects;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: BluetoothLeService.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004¨\u0006\u0005"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "jp/co/nipro/cocoron/service/BluetoothLeService$gattCallback$1$onDescriptorWrite$1$1"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onDescriptorWrite$1$1", f = "BluetoothLeService.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class BluetoothLeService$gattCallback$1$onDescriptorWrite$$inlined$apply$lambda$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ BluetoothGatt $gatt$inlined;
    final /* synthetic */ BluetoothGattDescriptor $this_apply;
    int label;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    BluetoothLeService$gattCallback$1$onDescriptorWrite$$inlined$apply$lambda$1(BluetoothGattDescriptor bluetoothGattDescriptor, Continuation continuation, BluetoothGatt bluetoothGatt) {
        super(2, continuation);
        this.$this_apply = bluetoothGattDescriptor;
        this.$gatt$inlined = bluetoothGatt;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new BluetoothLeService$gattCallback$1$onDescriptorWrite$$inlined$apply$lambda$1(this.$this_apply, completion, this.$gatt$inlined);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((BluetoothLeService$gattCallback$1$onDescriptorWrite$$inlined$apply$lambda$1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        if (this.label != 0) {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        ResultKt.throwOnFailure(obj);
        BluetoothGatt bluetoothGatt = this.$gatt$inlined;
        boolean z = bluetoothGatt != null && bluetoothGatt.writeDescriptor(this.$this_apply);
        StringBuilder sb = new StringBuilder();
        sb.append("writeDescriptor:");
        BluetoothGattCharacteristic characteristic = this.$this_apply.getCharacteristic();
        Intrinsics.checkNotNullExpressionValue(characteristic, "characteristic");
        String uuid = characteristic.getUuid().toString();
        Intrinsics.checkNotNullExpressionValue(uuid, "characteristic.uuid.toString()");
        Objects.requireNonNull(uuid, "null cannot be cast to non-null type java.lang.String");
        String upperCase = uuid.toUpperCase();
        Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
        sb.append(upperCase);
        sb.append(' ');
        sb.append(z);
        Log.d(BluetoothLeService.TAG, sb.toString());
        return Unit.INSTANCE;
    }
}
