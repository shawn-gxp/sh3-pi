package jp.co.nipro.cocoron.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: BluetoothLeService.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onServicesDiscovered$2", f = "BluetoothLeService.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class BluetoothLeService$gattCallback$1$onServicesDiscovered$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ BluetoothGatt $gatt;
    int label;
    final /* synthetic */ BluetoothLeService$gattCallback$1 this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    BluetoothLeService$gattCallback$1$onServicesDiscovered$2(BluetoothLeService$gattCallback$1 bluetoothLeService$gattCallback$1, BluetoothGatt bluetoothGatt, Continuation continuation) {
        super(2, continuation);
        this.this$0 = bluetoothLeService$gattCallback$1;
        this.$gatt = bluetoothGatt;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new BluetoothLeService$gattCallback$1$onServicesDiscovered$2(this.this$0, this.$gatt, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((BluetoothLeService$gattCallback$1$onServicesDiscovered$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        Object obj2;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        if (this.label != 0) {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        ResultKt.throwOnFailure(obj);
        ArrayList<BluetoothGattDescriptor> descriptors = this.this$0.this$0.getDescriptors();
        if (descriptors.size() > 1) {
            CollectionsKt.sortWith(descriptors, new Comparator<T>() { // from class: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onServicesDiscovered$2$invokeSuspend$$inlined$sortBy$1
                /* JADX WARN: Multi-variable type inference failed */
                @Override // java.util.Comparator
                public final int compare(T t, T t2) {
                    BluetoothGattCharacteristic characteristic = ((BluetoothGattDescriptor) t).getCharacteristic();
                    Intrinsics.checkNotNullExpressionValue(characteristic, "it.characteristic");
                    String uuid = characteristic.getUuid().toString();
                    Intrinsics.checkNotNullExpressionValue(uuid, "it.characteristic.uuid.toString()");
                    Objects.requireNonNull(uuid, "null cannot be cast to non-null type java.lang.String");
                    String upperCase = uuid.toUpperCase();
                    Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
                    BluetoothGattCharacteristic characteristic2 = ((BluetoothGattDescriptor) t2).getCharacteristic();
                    Intrinsics.checkNotNullExpressionValue(characteristic2, "it.characteristic");
                    String uuid2 = characteristic2.getUuid().toString();
                    Intrinsics.checkNotNullExpressionValue(uuid2, "it.characteristic.uuid.toString()");
                    Objects.requireNonNull(uuid2, "null cannot be cast to non-null type java.lang.String");
                    String upperCase2 = uuid2.toUpperCase();
                    Intrinsics.checkNotNullExpressionValue(upperCase2, "(this as java.lang.String).toUpperCase()");
                    return ComparisonsKt.compareValues(upperCase, upperCase2);
                }
            });
        }
        Iterator<T> it = this.this$0.this$0.getDescriptors().iterator();
        while (true) {
            if (!it.hasNext()) {
                obj2 = null;
                break;
            }
            obj2 = it.next();
            BluetoothGattCharacteristic characteristic = ((BluetoothGattDescriptor) obj2).getCharacteristic();
            Intrinsics.checkNotNullExpressionValue(characteristic, "it.characteristic");
            String uuid = characteristic.getUuid().toString();
            Intrinsics.checkNotNullExpressionValue(uuid, "it.characteristic.uuid.toString()");
            Objects.requireNonNull(uuid, "null cannot be cast to non-null type java.lang.String");
            String upperCase = uuid.toUpperCase();
            Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
            if (Boxing.boxBoolean(Intrinsics.areEqual(upperCase, BluetoothLeService.RRT)).booleanValue()) {
                break;
            }
        }
        BluetoothGattDescriptor bluetoothGattDescriptor = (BluetoothGattDescriptor) obj2;
        if (bluetoothGattDescriptor != null) {
            boolean writeDescriptor = this.$gatt.writeDescriptor(bluetoothGattDescriptor);
            StringBuilder sb = new StringBuilder();
            sb.append("writeDescriptor:");
            BluetoothGattCharacteristic characteristic2 = bluetoothGattDescriptor.getCharacteristic();
            Intrinsics.checkNotNullExpressionValue(characteristic2, "it.characteristic");
            String uuid2 = characteristic2.getUuid().toString();
            Intrinsics.checkNotNullExpressionValue(uuid2, "it.characteristic.uuid.toString()");
            Objects.requireNonNull(uuid2, "null cannot be cast to non-null type java.lang.String");
            String upperCase2 = uuid2.toUpperCase();
            Intrinsics.checkNotNullExpressionValue(upperCase2, "(this as java.lang.String).toUpperCase()");
            sb.append(upperCase2);
            sb.append(' ');
            sb.append(writeDescriptor);
            Log.d(BluetoothLeService.TAG, sb.toString());
        }
        return Unit.INSTANCE;
    }
}
