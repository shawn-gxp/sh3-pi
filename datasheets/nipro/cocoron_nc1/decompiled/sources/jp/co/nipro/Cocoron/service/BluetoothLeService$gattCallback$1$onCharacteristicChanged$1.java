package jp.co.nipro.cocoron.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;
import androidx.core.internal.view.SupportMenu;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import jp.co.nipro.cocoron.common.Config;
import jp.co.nipro.cocoron.common.Event;
import jp.co.nipro.cocoron.data.FileRecorder;
import jp.co.nipro.cocoron.data.db.DatabaseHelper;
import jp.co.nipro.cocoron.data.value.BatteryLevelCharacteristicValue;
import jp.co.nipro.cocoron.data.value.ConfigCharacteristicValue;
import jp.co.nipro.cocoron.data.value.RRTimeCharacteristicValue;
import jp.co.nipro.cocoron.ui.viewmodel.BaseModel;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.UShort;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.BuildersKt__BuildersKt;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.DelayKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;
import kotlinx.coroutines.Job;

/* compiled from: BluetoothLeService.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1", f = "BluetoothLeService.kt", i = {0}, l = {489, 501}, m = "invokeSuspend", n = {"date"}, s = {"L$0"})
/* loaded from: classes.dex */
final class BluetoothLeService$gattCallback$1$onCharacteristicChanged$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ BluetoothGattCharacteristic $characteristic;
    Object L$0;
    int label;
    final /* synthetic */ BluetoothLeService$gattCallback$1 this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    BluetoothLeService$gattCallback$1$onCharacteristicChanged$1(BluetoothLeService$gattCallback$1 bluetoothLeService$gattCallback$1, BluetoothGattCharacteristic bluetoothGattCharacteristic, Continuation continuation) {
        super(2, continuation);
        this.this$0 = bluetoothLeService$gattCallback$1;
        this.$characteristic = bluetoothGattCharacteristic;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new BluetoothLeService$gattCallback$1$onCharacteristicChanged$1(this.this$0, this.$characteristic, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((BluetoothLeService$gattCallback$1$onCharacteristicChanged$1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        Integer num;
        Date date;
        Date date2;
        Job launch$default;
        Job launch$default2;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            String uuid = this.$characteristic.getUuid().toString();
            Intrinsics.checkNotNullExpressionValue(uuid, "characteristic.uuid.toString()");
            Objects.requireNonNull(uuid, "null cannot be cast to non-null type java.lang.String");
            String upperCase = uuid.toUpperCase();
            Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
            switch (upperCase.hashCode()) {
                case -1893224563:
                    if (upperCase.equals(BluetoothLeService.BATTERY)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("receive BATTERY:");
                        sb.append(this.$characteristic.getUuid());
                        sb.append(" data:");
                        byte[] value = this.$characteristic.getValue();
                        Intrinsics.checkNotNullExpressionValue(value, "characteristic.value");
                        String joinToString$default = ArraysKt.joinToString$default(value, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.8
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
                        String upperCase2 = joinToString$default.toUpperCase();
                        Intrinsics.checkNotNullExpressionValue(upperCase2, "(this as java.lang.String).toUpperCase()");
                        sb.append(upperCase2);
                        Log.d(BluetoothLeService.TAG, sb.toString());
                        this.this$0.this$0.setBatteryLevel(new BatteryLevelCharacteristicValue());
                        BatteryLevelCharacteristicValue batteryLevel = this.this$0.this$0.getBatteryLevel();
                        if (batteryLevel != null) {
                            byte[] value2 = this.$characteristic.getValue();
                            Intrinsics.checkNotNullExpressionValue(value2, "characteristic.value");
                            num = Boxing.boxInt(batteryLevel.readBatteryLevelPacket(value2));
                        } else {
                            num = null;
                        }
                        if (num != null) {
                            this.this$0.this$0.getFileRecorder().writeBatteryLevel(num.intValue());
                        }
                        Log.d(BluetoothLeService.TAG, "BATTERY level:" + num);
                        BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new AnonymousClass10(num, null), 2, null);
                    }
                    return Unit.INSTANCE;
                case -119027949:
                    if (upperCase.equals(BluetoothLeService.ECG)) {
                        if (this.$characteristic.getValue().length == 0) {
                            return Unit.INSTANCE;
                        }
                        date = new Date();
                        if (!this.this$0.this$0.getOut_service()) {
                            DatabaseHelper companion = DatabaseHelper.INSTANCE.getInstance();
                            byte[] value3 = this.$characteristic.getValue();
                            Intrinsics.checkNotNullExpressionValue(value3, "characteristic.value");
                            this.L$0 = date;
                            this.label = 1;
                            if (DatabaseHelper.insertECG$default(companion, value3, null, this, 2, null) != coroutine_suspended) {
                                date2 = date;
                                date = date2;
                                break;
                            } else {
                                return coroutine_suspended;
                            }
                        }
                    }
                    return Unit.INSTANCE;
                case 574073748:
                    if (upperCase.equals(BluetoothLeService.CONFIG)) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("receive CONFIG:");
                        sb2.append(this.$characteristic.getUuid());
                        sb2.append(" data:");
                        byte[] value4 = this.$characteristic.getValue();
                        Intrinsics.checkNotNullExpressionValue(value4, "characteristic.value");
                        String joinToString$default2 = ArraysKt.joinToString$default(value4, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.11
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
                        Objects.requireNonNull(joinToString$default2, "null cannot be cast to non-null type java.lang.String");
                        String upperCase3 = joinToString$default2.toUpperCase();
                        Intrinsics.checkNotNullExpressionValue(upperCase3, "(this as java.lang.String).toUpperCase()");
                        sb2.append(upperCase3);
                        Log.d(BluetoothLeService.TAG, sb2.toString());
                        ConfigCharacteristicValue configCharacteristicValue = new ConfigCharacteristicValue();
                        byte[] value5 = this.$characteristic.getValue();
                        Intrinsics.checkNotNullExpressionValue(value5, "characteristic.value");
                        configCharacteristicValue.readConfigPacket(value5);
                        FileRecorder fileRecorder = this.this$0.this$0.getFileRecorder();
                        byte[] value6 = this.$characteristic.getValue();
                        Intrinsics.checkNotNullExpressionValue(value6, "characteristic.value");
                        fileRecorder.writeConfig(value6);
                        BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new AnonymousClass12(null), 2, null);
                        if (configCharacteristicValue.getInterval() != this.this$0.this$0.getConfig().getInterval()) {
                            this.this$0.this$0.setSendInterval(this.this$0.this$0.getConfig().getInterval());
                        }
                    }
                    return Unit.INSTANCE;
                case 1267175445:
                    if (upperCase.equals(BluetoothLeService.RRT)) {
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append("receive RRT:");
                        sb3.append(this.$characteristic.getUuid());
                        sb3.append(" data:");
                        byte[] value7 = this.$characteristic.getValue();
                        Intrinsics.checkNotNullExpressionValue(value7, "characteristic.value");
                        String joinToString$default3 = ArraysKt.joinToString$default(value7, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.3
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
                        Objects.requireNonNull(joinToString$default3, "null cannot be cast to non-null type java.lang.String");
                        String upperCase4 = joinToString$default3.toUpperCase();
                        Intrinsics.checkNotNullExpressionValue(upperCase4, "(this as java.lang.String).toUpperCase()");
                        sb3.append(upperCase4);
                        Log.d(BluetoothLeService.TAG, sb3.toString());
                        RRTimeCharacteristicValue rRTimeCharacteristicValue = new RRTimeCharacteristicValue();
                        byte[] value8 = this.$characteristic.getValue();
                        Intrinsics.checkNotNullExpressionValue(value8, "characteristic.value");
                        short m8readRRTimePacketBwKQO78 = rRTimeCharacteristicValue.m8readRRTimePacketBwKQO78(value8);
                        if (rRTimeCharacteristicValue.getOutservice() == 0 && rRTimeCharacteristicValue.getRRTime() != UShort.m269constructorimpl((short) SupportMenu.USER_MASK) && !rRTimeCharacteristicValue.getIsIllegalIphone()) {
                            BuildersKt__BuildersKt.runBlocking$default(null, new AnonymousClass4(m8readRRTimePacketBwKQO78, rRTimeCharacteristicValue, null), 1, null);
                        }
                        if (rRTimeCharacteristicValue.getIsInDateTime()) {
                            Log.d(BluetoothLeService.TAG, "RRI time:" + rRTimeCharacteristicValue.getYear() + '/' + rRTimeCharacteristicValue.getMonth() + '/' + rRTimeCharacteristicValue.getDay() + ' ' + rRTimeCharacteristicValue.getHour() + ':' + rRTimeCharacteristicValue.getMin() + ':' + rRTimeCharacteristicValue.getSec());
                        }
                        int i2 = 65535 & m8readRRTimePacketBwKQO78;
                        this.this$0.this$0.getFileRecorder().writeRRTime(i2);
                        this.this$0.this$0.getFileRecorder().writeOutService(rRTimeCharacteristicValue.getOutservice());
                        Log.d(BluetoothLeService.TAG, "RRI mode:" + rRTimeCharacteristicValue.getMode() + " interval:" + rRTimeCharacteristicValue.getInterval() + " rri:" + i2 + " out_service:" + rRTimeCharacteristicValue.getOutservice() + " isIllegalIphone:" + rRTimeCharacteristicValue.getIsIllegalIphone());
                        if (!rRTimeCharacteristicValue.getIsIllegalIphone() && this.this$0.this$0.getSended_config()) {
                            BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new AnonymousClass5(null), 2, null);
                        }
                        this.this$0.this$0.setOut_service(rRTimeCharacteristicValue.getOutservice() != 0);
                        if (!this.this$0.this$0.getSended_config()) {
                            return Unit.INSTANCE;
                        }
                        Job workItemDisconnect = this.this$0.this$0.getWorkItemDisconnect();
                        if (workItemDisconnect != null) {
                            Job.DefaultImpls.cancel$default(workItemDisconnect, (CancellationException) null, 1, (Object) null);
                        }
                        BluetoothLeService bluetoothLeService = this.this$0.this$0;
                        launch$default = BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new AnonymousClass6(null), 2, null);
                        bluetoothLeService.setWorkItemDisconnect(launch$default);
                        BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new AnonymousClass7(rRTimeCharacteristicValue, m8readRRTimePacketBwKQO78, null), 2, null);
                    }
                    return Unit.INSTANCE;
                default:
                    return Unit.INSTANCE;
            }
        }
        if (i != 1) {
            if (i == 2) {
                ResultKt.throwOnFailure(obj);
                return Unit.INSTANCE;
            }
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        date2 = (Date) this.L$0;
        ResultKt.throwOnFailure(obj);
        date = date2;
        StringBuilder sb4 = new StringBuilder();
        sb4.append("receive ECG: ");
        byte[] value9 = this.$characteristic.getValue();
        Intrinsics.checkNotNullExpressionValue(value9, "characteristic.value");
        String joinToString$default4 = ArraysKt.joinToString$default(value9, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.1
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
        Objects.requireNonNull(joinToString$default4, "null cannot be cast to non-null type java.lang.String");
        String upperCase5 = joinToString$default4.toUpperCase();
        Intrinsics.checkNotNullExpressionValue(upperCase5, "(this as java.lang.String).toUpperCase()");
        sb4.append(upperCase5);
        Log.d(BluetoothLeService.TAG, sb4.toString());
        FileRecorder fileRecorder2 = this.this$0.this$0.getFileRecorder();
        byte[] value10 = this.$characteristic.getValue();
        Intrinsics.checkNotNullExpressionValue(value10, "characteristic.value");
        fileRecorder2.writeECGMeasurement(value10);
        launch$default2 = BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new AnonymousClass2(date, null), 2, null);
        this.L$0 = null;
        this.label = 2;
        if (launch$default2.join(this) == coroutine_suspended) {
            return coroutine_suspended;
        }
        return Unit.INSTANCE;
    }

    /* compiled from: BluetoothLeService.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
    @DebugMetadata(c = "jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$2", f = "BluetoothLeService.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ Date $date;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass2(Date date, Continuation continuation) {
            super(2, continuation);
            this.$date = date;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
            Intrinsics.checkNotNullParameter(completion, "completion");
            return BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.new AnonymousClass2(this.$date, completion);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label != 0) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
            BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getEvent().setValue(new Event<>(new BaseModel.EventParam("RECEIVED_ECG", BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.$characteristic.getValue())));
            Log.d(BluetoothLeService.TAG, this.$date.getTime() + " use:" + (new Date().getTime() - this.$date.getTime()));
            return Unit.INSTANCE;
        }
    }

    /* compiled from: BluetoothLeService.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
    @DebugMetadata(c = "jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$4", f = "BluetoothLeService.kt", i = {}, l = {519}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$4, reason: invalid class name */
    static final class AnonymousClass4 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Object>, Object> {
        final /* synthetic */ short $RRTObj;
        final /* synthetic */ RRTimeCharacteristicValue $rrtM;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass4(short s, RRTimeCharacteristicValue rRTimeCharacteristicValue, Continuation continuation) {
            super(2, continuation);
            this.$RRTObj = s;
            this.$rrtM = rRTimeCharacteristicValue;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
            Intrinsics.checkNotNullParameter(completion, "completion");
            return new AnonymousClass4(this.$RRTObj, this.$rrtM, completion);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Object> continuation) {
            return ((AnonymousClass4) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            int i = this.label;
            if (i == 0) {
                ResultKt.throwOnFailure(obj);
                DatabaseHelper companion = DatabaseHelper.INSTANCE.getInstance();
                int i2 = this.$RRTObj & UShort.MAX_VALUE;
                int outservice = this.$rrtM.getOutservice();
                this.label = 1;
                obj = DatabaseHelper.insertRRI$default(companion, i2, outservice, null, this, 4, null);
                if (obj == coroutine_suspended) {
                    return coroutine_suspended;
                }
            } else {
                if (i != 1) {
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                ResultKt.throwOnFailure(obj);
            }
            return obj;
        }
    }

    /* compiled from: BluetoothLeService.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
    @DebugMetadata(c = "jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$5", f = "BluetoothLeService.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$5, reason: invalid class name */
    static final class AnonymousClass5 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        int label;

        AnonymousClass5(Continuation continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
            Intrinsics.checkNotNullParameter(completion, "completion");
            return BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.new AnonymousClass5(completion);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass5) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label != 0) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
            BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.setConfig(BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getConfig().getInterval(), BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getConfig().getMode());
            return Unit.INSTANCE;
        }
    }

    /* compiled from: BluetoothLeService.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
    @DebugMetadata(c = "jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$6", f = "BluetoothLeService.kt", i = {}, l = {543}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$6, reason: invalid class name */
    static final class AnonymousClass6 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        int label;

        AnonymousClass6(Continuation continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
            Intrinsics.checkNotNullParameter(completion, "completion");
            return BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.new AnonymousClass6(completion);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass6) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            int i = this.label;
            if (i == 0) {
                ResultKt.throwOnFailure(obj);
                long rriTimeout = Config.INSTANCE.rriTimeout() * 1000;
                this.label = 1;
                if (DelayKt.delay(rriTimeout, this) == coroutine_suspended) {
                    return coroutine_suspended;
                }
            } else {
                if (i != 1) {
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                ResultKt.throwOnFailure(obj);
            }
            BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.setDisconnect_rri(true);
            BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getEvent().setValue(new Event<>(new BaseModel.EventParam("DISCONNECT_RRI", Boxing.boxBoolean(true))));
            BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.sendNotification("心電計との接続が切れました", "NOTIFY DISCONNECT");
            return Unit.INSTANCE;
        }
    }

    /* compiled from: BluetoothLeService.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
    @DebugMetadata(c = "jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$7", f = "BluetoothLeService.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$7, reason: invalid class name */
    static final class AnonymousClass7 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ short $RRTObj;
        final /* synthetic */ RRTimeCharacteristicValue $rrtM;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass7(RRTimeCharacteristicValue rRTimeCharacteristicValue, short s, Continuation continuation) {
            super(2, continuation);
            this.$rrtM = rRTimeCharacteristicValue;
            this.$RRTObj = s;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
            Intrinsics.checkNotNullParameter(completion, "completion");
            return BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.new AnonymousClass7(this.$rrtM, this.$RRTObj, completion);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass7) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label != 0) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
            BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getEvent().setValue(new Event<>(new BaseModel.EventParam("RECEIVED_RRT", this.$rrtM)));
            BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getEvent().setValue(new Event<>(new BaseModel.EventParam("DISCONNECT_RRI", Boxing.boxBoolean(false))));
            if (this.$rrtM.getIsIllegalIphone()) {
                return Unit.INSTANCE;
            }
            if (this.$rrtM.getOutservice() != 0) {
                if (!BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getDidNotifyOutService()) {
                    BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.sendNotification("心電計が身体から外れています", "NOTIFY OUT SERVICE");
                }
                BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.setDidNotifyOutService(true);
                BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getEvent().setValue(new Event<>(new BaseModel.EventParam("OUT_SERVICE", Boxing.boxBoolean(BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getDidNotifyOutService()))));
                return Unit.INSTANCE;
            }
            BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.setDidNotifyOutService(false);
            BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getEvent().setValue(new Event<>(new BaseModel.EventParam("OUT_SERVICE", Boxing.boxBoolean(BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getDidNotifyOutService()))));
            int i = this.$RRTObj & UShort.MAX_VALUE;
            if ((i < Config.INSTANCE.getBottom() && Config.INSTANCE.getBottom() >= Config.INSTANCE.getBOTTOM_MIN()) || (i > Config.INSTANCE.getTop() && Config.INSTANCE.getTop() >= Config.INSTANCE.getTOP_MIN())) {
                if (this.$rrtM.getMode() != 1) {
                    BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.setEcgMode(1);
                }
                BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.setRri_over_limit(true);
                BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getEvent().setValue(new Event<>(new BaseModel.EventParam("RRT_OVER_LIMIT", Boxing.boxBoolean(true))));
            } else {
                BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.setRri_over_limit(false);
                BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getEvent().setValue(new Event<>(new BaseModel.EventParam("RRT_OVER_LIMIT", Boxing.boxBoolean(false))));
            }
            return Unit.INSTANCE;
        }
    }

    /* compiled from: BluetoothLeService.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
    @DebugMetadata(c = "jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$10", f = "BluetoothLeService.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$10, reason: invalid class name */
    static final class AnonymousClass10 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ Integer $level;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass10(Integer num, Continuation continuation) {
            super(2, continuation);
            this.$level = num;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
            Intrinsics.checkNotNullParameter(completion, "completion");
            return BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.new AnonymousClass10(this.$level, completion);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass10) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label != 0) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
            BluetoothLeService bluetoothLeService = BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0;
            Integer num = this.$level;
            bluetoothLeService.setLow_battery((num != null ? num.intValue() : 0) < Config.INSTANCE.getBATTERY_WARNING_LEVEL());
            BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getEvent().setValue(new Event<>(new BaseModel.EventParam("RECEIVED_BATTERY", Boxing.boxBoolean(BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getLow_battery()))));
            if (BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getLow_battery()) {
                BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.sendNotification("心電計の電池がもうすぐ切れます", "NOTIFY BATTERY");
            }
            return Unit.INSTANCE;
        }
    }

    /* compiled from: BluetoothLeService.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
    @DebugMetadata(c = "jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$12", f = "BluetoothLeService.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: jp.co.nipro.cocoron.service.BluetoothLeService$gattCallback$1$onCharacteristicChanged$1$12, reason: invalid class name */
    static final class AnonymousClass12 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        int label;

        AnonymousClass12(Continuation continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
            Intrinsics.checkNotNullParameter(completion, "completion");
            return BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.new AnonymousClass12(completion);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass12) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label != 0) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
            BluetoothLeService$gattCallback$1$onCharacteristicChanged$1.this.this$0.this$0.getEvent().setValue(new Event<>(new BaseModel.EventParam("RECEIVED_CONFIG", null, 2, null)));
            return Unit.INSTANCE;
        }
    }
}
