package kotlinx.coroutines.flow.internal;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.IndexedValue;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.channels.Channel;
import kotlinx.coroutines.channels.ChannelKt;
import kotlinx.coroutines.channels.ChannelsKt;
import kotlinx.coroutines.channels.SendChannel;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;

/* compiled from: Combine.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003*\u00020\u0004H\u008a@¢\u0006\u0004\b\u0005\u0010\u0006"}, d2 = {"<anonymous>", "", "R", "T", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 0})
@DebugMetadata(c = "kotlinx.coroutines.flow.internal.CombineKt$combineInternal$2", f = "Combine.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2}, l = {57, 79, 82}, m = "invokeSuspend", n = {"$this$flowScope", "size", "latestValues", "resultChannel", "nonClosed", "remainingAbsentValues", "lastReceivedEpoch", "currentEpoch", "$this$flowScope", "size", "latestValues", "resultChannel", "nonClosed", "remainingAbsentValues", "lastReceivedEpoch", "currentEpoch", "element", "results", "$this$flowScope", "size", "latestValues", "resultChannel", "nonClosed", "remainingAbsentValues", "lastReceivedEpoch", "currentEpoch", "element", "results"}, s = {"L$0", "I$0", "L$1", "L$2", "L$3", "I$1", "L$4", "B$0", "L$0", "I$0", "L$1", "L$2", "L$3", "I$1", "L$4", "I$2", "L$5", "L$6", "L$0", "I$0", "L$1", "L$2", "L$3", "I$1", "L$4", "I$2", "L$5", "L$6"})
/* loaded from: classes.dex */
final class CombineKt$combineInternal$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ Function0 $arrayFactory;
    final /* synthetic */ Flow[] $flows;
    final /* synthetic */ FlowCollector $this_combineInternal;
    final /* synthetic */ Function3 $transform;
    byte B$0;
    int I$0;
    int I$1;
    int I$2;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    int label;
    private CoroutineScope p$;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    CombineKt$combineInternal$2(FlowCollector flowCollector, Flow[] flowArr, Function0 function0, Function3 function3, Continuation continuation) {
        super(2, continuation);
        this.$this_combineInternal = flowCollector;
        this.$flows = flowArr;
        this.$arrayFactory = function0;
        this.$transform = function3;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        CombineKt$combineInternal$2 combineKt$combineInternal$2 = new CombineKt$combineInternal$2(this.$this_combineInternal, this.$flows, this.$arrayFactory, this.$transform, continuation);
        combineKt$combineInternal$2.p$ = (CoroutineScope) obj;
        return combineKt$combineInternal$2;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((CombineKt$combineInternal$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0120 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:13:0x0121  */
    /* JADX WARN: Removed duplicated region for block: B:16:0x0132 A[LOOP:0: B:16:0x0132->B:23:0x0153, LOOP_START, PHI: r2 r5
  0x0132: PHI (r2v5 int) = (r2v4 int), (r2v6 int) binds: [B:15:0x0130, B:23:0x0153] A[DONT_GENERATE, DONT_INLINE]
  0x0132: PHI (r5v6 kotlin.collections.IndexedValue) = (r5v5 kotlin.collections.IndexedValue), (r5v12 kotlin.collections.IndexedValue) binds: [B:15:0x0130, B:23:0x0153] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Removed duplicated region for block: B:38:0x01db  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:33:0x01c9 -> B:7:0x01cc). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:35:0x01d4 -> B:7:0x01cc). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        CoroutineScope coroutineScope;
        Object[] objArr;
        AtomicInteger atomicInteger;
        byte[] bArr;
        int i;
        int i2;
        CombineKt$combineInternal$2 combineKt$combineInternal$2;
        Channel channel;
        CoroutineScope coroutineScope2;
        int i3;
        Object obj2;
        Object[] objArr2;
        Object obj3;
        Channel channel2;
        byte[] bArr2;
        byte b;
        IndexedValue indexedValue;
        char c;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i4 = this.label;
        int i5 = 0;
        int i6 = 1;
        if (i4 == 0) {
            ResultKt.throwOnFailure(obj);
            coroutineScope = this.p$;
            int length = this.$flows.length;
            if (length == 0) {
                return Unit.INSTANCE;
            }
            objArr = new Object[length];
            ArraysKt.fill$default(objArr, NullSurrogateKt.UNINITIALIZED, 0, 0, 6, (Object) null);
            Channel Channel$default = ChannelKt.Channel$default(length, null, null, 6, null);
            atomicInteger = new AtomicInteger(length);
            int i7 = 0;
            while (i7 < length) {
                BuildersKt__Builders_commonKt.launch$default(coroutineScope, null, null, new AnonymousClass1(i7, Channel$default, atomicInteger, null), 3, null);
                i7++;
                atomicInteger = atomicInteger;
                objArr = objArr;
                length = length;
            }
            int i8 = length;
            bArr = new byte[i8];
            i = i8;
            i2 = i;
            combineKt$combineInternal$2 = this;
            channel = Channel$default;
            byte b2 = (byte) (i5 + i6);
            combineKt$combineInternal$2.L$0 = coroutineScope;
            combineKt$combineInternal$2.I$0 = i2;
            combineKt$combineInternal$2.L$1 = objArr;
            combineKt$combineInternal$2.L$2 = channel;
            combineKt$combineInternal$2.L$3 = atomicInteger;
            combineKt$combineInternal$2.I$1 = i;
            combineKt$combineInternal$2.L$4 = bArr;
            combineKt$combineInternal$2.B$0 = b2;
            combineKt$combineInternal$2.label = i6;
            obj3 = ChannelsKt.receiveOrNull(channel, combineKt$combineInternal$2);
            if (obj3 == coroutine_suspended) {
            }
        } else if (i4 == 1) {
            byte b3 = this.B$0;
            bArr2 = (byte[]) this.L$4;
            i = this.I$1;
            AtomicInteger atomicInteger2 = (AtomicInteger) this.L$3;
            channel2 = (Channel) this.L$2;
            Object[] objArr3 = (Object[]) this.L$1;
            int i9 = this.I$0;
            CoroutineScope coroutineScope3 = (CoroutineScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            atomicInteger = atomicInteger2;
            combineKt$combineInternal$2 = this;
            coroutineScope2 = coroutineScope3;
            i3 = i9;
            obj2 = coroutine_suspended;
            objArr2 = objArr3;
            obj3 = obj;
            b = b3;
            indexedValue = (IndexedValue) obj3;
            if (indexedValue == null) {
            }
        } else if (i4 == 2) {
            int i10 = this.I$2;
            byte[] bArr3 = (byte[]) this.L$4;
            i = this.I$1;
            AtomicInteger atomicInteger3 = (AtomicInteger) this.L$3;
            Channel channel3 = (Channel) this.L$2;
            Object[] objArr4 = (Object[]) this.L$1;
            int i11 = this.I$0;
            coroutineScope = (CoroutineScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            atomicInteger = atomicInteger3;
            combineKt$combineInternal$2 = this;
            objArr = objArr4;
            i2 = i11;
            c = 3;
            i5 = i10;
            bArr = bArr3;
            channel = channel3;
            i6 = 1;
            byte b22 = (byte) (i5 + i6);
            combineKt$combineInternal$2.L$0 = coroutineScope;
            combineKt$combineInternal$2.I$0 = i2;
            combineKt$combineInternal$2.L$1 = objArr;
            combineKt$combineInternal$2.L$2 = channel;
            combineKt$combineInternal$2.L$3 = atomicInteger;
            combineKt$combineInternal$2.I$1 = i;
            combineKt$combineInternal$2.L$4 = bArr;
            combineKt$combineInternal$2.B$0 = b22;
            combineKt$combineInternal$2.label = i6;
            obj3 = ChannelsKt.receiveOrNull(channel, combineKt$combineInternal$2);
            if (obj3 == coroutine_suspended) {
            }
        } else if (i4 == 3) {
            int i12 = this.I$2;
            bArr2 = (byte[]) this.L$4;
            i = this.I$1;
            AtomicInteger atomicInteger4 = (AtomicInteger) this.L$3;
            channel2 = (Channel) this.L$2;
            Object[] objArr5 = (Object[]) this.L$1;
            int i13 = this.I$0;
            CoroutineScope coroutineScope4 = (CoroutineScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            atomicInteger = atomicInteger4;
            combineKt$combineInternal$2 = this;
            CoroutineScope coroutineScope5 = coroutineScope4;
            i3 = i13;
            obj2 = coroutine_suspended;
            objArr2 = objArr5;
            c = 3;
            int i14 = i12;
            objArr = objArr2;
            coroutine_suspended = obj2;
            i2 = i3;
            i5 = i14;
            bArr = bArr2;
            channel = channel2;
            coroutineScope = coroutineScope5;
            i6 = 1;
            byte b222 = (byte) (i5 + i6);
            combineKt$combineInternal$2.L$0 = coroutineScope;
            combineKt$combineInternal$2.I$0 = i2;
            combineKt$combineInternal$2.L$1 = objArr;
            combineKt$combineInternal$2.L$2 = channel;
            combineKt$combineInternal$2.L$3 = atomicInteger;
            combineKt$combineInternal$2.I$1 = i;
            combineKt$combineInternal$2.L$4 = bArr;
            combineKt$combineInternal$2.B$0 = b222;
            combineKt$combineInternal$2.label = i6;
            obj3 = ChannelsKt.receiveOrNull(channel, combineKt$combineInternal$2);
            if (obj3 == coroutine_suspended) {
                return coroutine_suspended;
            }
            obj2 = coroutine_suspended;
            objArr2 = objArr;
            Channel channel4 = channel;
            bArr2 = bArr;
            b = b222;
            channel2 = channel4;
            int i15 = i2;
            coroutineScope2 = coroutineScope;
            i3 = i15;
            indexedValue = (IndexedValue) obj3;
            if (indexedValue == null) {
                while (true) {
                    int index = indexedValue.getIndex();
                    Object obj4 = objArr2[index];
                    objArr2[index] = indexedValue.getValue();
                    if (obj4 == NullSurrogateKt.UNINITIALIZED) {
                        i--;
                    }
                    if (bArr2[index] != b) {
                        bArr2[index] = b;
                        IndexedValue indexedValue2 = (IndexedValue) channel2.poll();
                        if (indexedValue2 == null) {
                            break;
                        }
                        indexedValue = indexedValue2;
                    } else {
                        break;
                    }
                }
                if (i != 0) {
                    coroutineScope5 = coroutineScope2;
                    c = 3;
                    i14 = b;
                } else {
                    Object[] objArr6 = (Object[]) combineKt$combineInternal$2.$arrayFactory.invoke();
                    if (objArr6 == null) {
                        Function3 function3 = combineKt$combineInternal$2.$transform;
                        FlowCollector flowCollector = combineKt$combineInternal$2.$this_combineInternal;
                        Objects.requireNonNull(objArr2, "null cannot be cast to non-null type kotlin.Array<T>");
                        combineKt$combineInternal$2.L$0 = coroutineScope2;
                        combineKt$combineInternal$2.I$0 = i3;
                        combineKt$combineInternal$2.L$1 = objArr2;
                        combineKt$combineInternal$2.L$2 = channel2;
                        combineKt$combineInternal$2.L$3 = atomicInteger;
                        combineKt$combineInternal$2.I$1 = i;
                        combineKt$combineInternal$2.L$4 = bArr2;
                        combineKt$combineInternal$2.I$2 = b;
                        combineKt$combineInternal$2.L$5 = indexedValue;
                        combineKt$combineInternal$2.L$6 = objArr6;
                        combineKt$combineInternal$2.label = 2;
                        if (function3.invoke(flowCollector, objArr2, combineKt$combineInternal$2) == obj2) {
                            return obj2;
                        }
                        Object[] objArr7 = objArr2;
                        coroutine_suspended = obj2;
                        int i16 = i3;
                        coroutineScope = coroutineScope2;
                        objArr = objArr7;
                        i2 = i16;
                        c = 3;
                        i5 = b;
                        bArr = bArr2;
                        channel = channel2;
                        i6 = 1;
                        byte b2222 = (byte) (i5 + i6);
                        combineKt$combineInternal$2.L$0 = coroutineScope;
                        combineKt$combineInternal$2.I$0 = i2;
                        combineKt$combineInternal$2.L$1 = objArr;
                        combineKt$combineInternal$2.L$2 = channel;
                        combineKt$combineInternal$2.L$3 = atomicInteger;
                        combineKt$combineInternal$2.I$1 = i;
                        combineKt$combineInternal$2.L$4 = bArr;
                        combineKt$combineInternal$2.B$0 = b2222;
                        combineKt$combineInternal$2.label = i6;
                        obj3 = ChannelsKt.receiveOrNull(channel, combineKt$combineInternal$2);
                        if (obj3 == coroutine_suspended) {
                        }
                    } else {
                        Objects.requireNonNull(objArr2, "null cannot be cast to non-null type kotlin.Array<T?>");
                        coroutineScope5 = coroutineScope2;
                        ArraysKt.copyInto$default(objArr2, objArr6, 0, 0, 0, 14, (Object) null);
                        Function3 function32 = combineKt$combineInternal$2.$transform;
                        FlowCollector flowCollector2 = combineKt$combineInternal$2.$this_combineInternal;
                        combineKt$combineInternal$2.L$0 = coroutineScope5;
                        combineKt$combineInternal$2.I$0 = i3;
                        combineKt$combineInternal$2.L$1 = objArr2;
                        combineKt$combineInternal$2.L$2 = channel2;
                        combineKt$combineInternal$2.L$3 = atomicInteger;
                        combineKt$combineInternal$2.I$1 = i;
                        combineKt$combineInternal$2.L$4 = bArr2;
                        combineKt$combineInternal$2.I$2 = b;
                        combineKt$combineInternal$2.L$5 = indexedValue;
                        combineKt$combineInternal$2.L$6 = objArr6;
                        c = 3;
                        combineKt$combineInternal$2.label = 3;
                        i14 = b;
                        if (function32.invoke(flowCollector2, objArr6, combineKt$combineInternal$2) == obj2) {
                            return obj2;
                        }
                    }
                }
                objArr = objArr2;
                coroutine_suspended = obj2;
                i2 = i3;
                i5 = i14;
                bArr = bArr2;
                channel = channel2;
                coroutineScope = coroutineScope5;
                i6 = 1;
                byte b22222 = (byte) (i5 + i6);
                combineKt$combineInternal$2.L$0 = coroutineScope;
                combineKt$combineInternal$2.I$0 = i2;
                combineKt$combineInternal$2.L$1 = objArr;
                combineKt$combineInternal$2.L$2 = channel;
                combineKt$combineInternal$2.L$3 = atomicInteger;
                combineKt$combineInternal$2.I$1 = i;
                combineKt$combineInternal$2.L$4 = bArr;
                combineKt$combineInternal$2.B$0 = b22222;
                combineKt$combineInternal$2.label = i6;
                obj3 = ChannelsKt.receiveOrNull(channel, combineKt$combineInternal$2);
                if (obj3 == coroutine_suspended) {
                }
            } else {
                return Unit.INSTANCE;
            }
        } else {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }

    /* compiled from: Combine.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003*\u00020\u0004H\u008a@¢\u0006\u0004\b\u0005\u0010\u0006"}, d2 = {"<anonymous>", "", "R", "T", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 0})
    @DebugMetadata(c = "kotlinx.coroutines.flow.internal.CombineKt$combineInternal$2$1", f = "Combine.kt", i = {0, 0}, l = {145}, m = "invokeSuspend", n = {"$this$launch", "$this$collect$iv"}, s = {"L$0", "L$1"})
    /* renamed from: kotlinx.coroutines.flow.internal.CombineKt$combineInternal$2$1, reason: invalid class name */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ int $i;
        final /* synthetic */ AtomicInteger $nonClosed;
        final /* synthetic */ Channel $resultChannel;
        Object L$0;
        Object L$1;
        int label;
        private CoroutineScope p$;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(int i, Channel channel, AtomicInteger atomicInteger, Continuation continuation) {
            super(2, continuation);
            this.$i = i;
            this.$resultChannel = channel;
            this.$nonClosed = atomicInteger;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            AnonymousClass1 anonymousClass1 = CombineKt$combineInternal$2.this.new AnonymousClass1(this.$i, this.$resultChannel, this.$nonClosed, continuation);
            anonymousClass1.p$ = (CoroutineScope) obj;
            return anonymousClass1;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            AtomicInteger atomicInteger;
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            int i = this.label;
            try {
                if (i == 0) {
                    ResultKt.throwOnFailure(obj);
                    CoroutineScope coroutineScope = this.p$;
                    Flow flow = CombineKt$combineInternal$2.this.$flows[this.$i];
                    CombineKt$combineInternal$2$1$invokeSuspend$$inlined$collect$1 combineKt$combineInternal$2$1$invokeSuspend$$inlined$collect$1 = new CombineKt$combineInternal$2$1$invokeSuspend$$inlined$collect$1(this);
                    this.L$0 = coroutineScope;
                    this.L$1 = flow;
                    this.label = 1;
                    if (flow.collect(combineKt$combineInternal$2$1$invokeSuspend$$inlined$collect$1, this) == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                } else {
                    if (i != 1) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    ResultKt.throwOnFailure(obj);
                }
                if (atomicInteger.decrementAndGet() == 0) {
                    SendChannel.DefaultImpls.close$default(this.$resultChannel, null, 1, null);
                }
                return Unit.INSTANCE;
            } finally {
                if (this.$nonClosed.decrementAndGet() == 0) {
                    SendChannel.DefaultImpls.close$default(this.$resultChannel, null, 1, null);
                }
            }
        }
    }
}
