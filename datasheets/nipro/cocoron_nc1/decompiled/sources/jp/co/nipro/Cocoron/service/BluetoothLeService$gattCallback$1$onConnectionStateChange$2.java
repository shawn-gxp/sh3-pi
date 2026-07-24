package jp.co.nipro.cocoron.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import jp.co.nipro.cocoron.common.Config;
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
import kotlinx.coroutines.DelayKt;

/* compiled from: BluetoothLeService.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onConnectionStateChange$2", f = "BluetoothLeService.kt", i = {}, l = {358}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class BluetoothLeService$gattCallback$1$onConnectionStateChange$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ BluetoothLeService$gattCallback$1 $bThis;
    int label;
    final /* synthetic */ BluetoothLeService$gattCallback$1 this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    BluetoothLeService$gattCallback$1$onConnectionStateChange$2(BluetoothLeService$gattCallback$1 bluetoothLeService$gattCallback$1, BluetoothLeService$gattCallback$1 bluetoothLeService$gattCallback$12, Continuation continuation) {
        super(2, continuation);
        this.this$0 = bluetoothLeService$gattCallback$1;
        this.$bThis = bluetoothLeService$gattCallback$12;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new BluetoothLeService$gattCallback$1$onConnectionStateChange$2(this.this$0, this.$bThis, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((BluetoothLeService$gattCallback$1$onConnectionStateChange$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            this.label = 1;
            if (DelayKt.delay(1000L, this) == coroutine_suspended) {
                return coroutine_suspended;
            }
        } else {
            if (i != 1) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
        }
        this.this$0.this$0.getFileRecorder().writeDisconnectGatt(this.this$0.this$0.getBluetoothGatt());
        BluetoothGatt bluetoothGatt = this.this$0.this$0.getBluetoothGatt();
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        BluetoothGatt bluetoothGatt2 = this.this$0.this$0.getBluetoothGatt();
        if (bluetoothGatt2 != null) {
            bluetoothGatt2.disconnect();
        }
        this.this$0.this$0.setBluetoothGatt((BluetoothGatt) null);
        BluetoothDevice connectedDevice = this.this$0.this$0.getConnectedDevice();
        this.this$0.this$0.getFileRecorder().writeConnectGatt(connectedDevice != null ? connectedDevice.connectGatt(this.this$0.this$0, !Config.INSTANCE.getUSE_ECG_SIM(), this.$bThis, 2) : null);
        return Unit.INSTANCE;
    }
}
