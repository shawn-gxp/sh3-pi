package kotlinx.coroutines.flow;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.Ref;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.channels.ProduceKt;
import kotlinx.coroutines.channels.ReceiveChannel;
import kotlinx.coroutines.flow.internal.NullSurrogateKt;
import kotlinx.coroutines.selects.SelectInstance;

/* JADX INFO: Add missing generic type declarations: [T] */
/* compiled from: Delay.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0016\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0005H\u008a@¢\u0006\u0004\b\u0006\u0010\u0007"}, d2 = {"<anonymous>", "", "T", "Lkotlinx/coroutines/CoroutineScope;", "downstream", "Lkotlinx/coroutines/flow/FlowCollector;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 0})
@DebugMetadata(c = "kotlinx.coroutines.flow.FlowKt__DelayKt$sample$2", f = "Delay.kt", i = {0, 0, 0, 0, 0}, l = {355}, m = "invokeSuspend", n = {"$this$scopedFlow", "downstream", "values", "lastValue", "ticker"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4"})
/* loaded from: classes.dex */
final class FlowKt__DelayKt$sample$2<T> extends SuspendLambda implements Function3<CoroutineScope, FlowCollector<? super T>, Continuation<? super Unit>, Object> {
    final /* synthetic */ long $periodMillis;
    final /* synthetic */ Flow $this_sample;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    int label;
    private CoroutineScope p$;
    private FlowCollector p$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    FlowKt__DelayKt$sample$2(Flow flow, long j, Continuation continuation) {
        super(3, continuation);
        this.$this_sample = flow;
        this.$periodMillis = j;
    }

    public final Continuation<Unit> create(CoroutineScope coroutineScope, FlowCollector<? super T> flowCollector, Continuation<? super Unit> continuation) {
        FlowKt__DelayKt$sample$2 flowKt__DelayKt$sample$2 = new FlowKt__DelayKt$sample$2(this.$this_sample, this.$periodMillis, continuation);
        flowKt__DelayKt$sample$2.p$ = coroutineScope;
        flowKt__DelayKt$sample$2.p$0 = flowCollector;
        return flowKt__DelayKt$sample$2;
    }

    @Override // kotlin.jvm.functions.Function3
    public final Object invoke(CoroutineScope coroutineScope, Object obj, Continuation<? super Unit> continuation) {
        return ((FlowKt__DelayKt$sample$2) create(coroutineScope, (FlowCollector) obj, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Can't wrap try/catch for region: R(13:8|(1:9)|10|11|12|13|14|15|(1:17)|(1:19)|5|6|(2:29|30)(0)) */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x00e2, code lost:
    
        if (r0 != r2) goto L26;
     */
    /* JADX WARN: Code restructure failed: missing block: B:23:0x00c7, code lost:
    
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x00c8, code lost:
    
        r3 = r8;
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x00cf, code lost:
    
        r3.handleBuilderException(r0);
     */
    /* JADX WARN: Removed duplicated region for block: B:17:0x00dc  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x00e1 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:29:0x00e4  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0074  */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        FlowCollector flowCollector;
        Ref.ObjectRef objectRef;
        Ref.ObjectRef objectRef2;
        ReceiveChannel fixedPeriodTicker$default;
        FlowKt__DelayKt$sample$2<T> flowKt__DelayKt$sample$2;
        CoroutineScope coroutineScope;
        ReceiveChannel receiveChannel;
        Object obj2;
        FlowKt__DelayKt$sample$2<T> flowKt__DelayKt$sample$22;
        SelectInstance selectInstance;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        int i2 = 1;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            CoroutineScope coroutineScope2 = this.p$;
            flowCollector = this.p$0;
            objectRef = new Ref.ObjectRef();
            objectRef.element = (T) ProduceKt.produce$default(coroutineScope2, null, -1, new FlowKt__DelayKt$sample$2$values$1(this, null), 1, null);
            objectRef2 = new Ref.ObjectRef();
            objectRef2.element = null;
            fixedPeriodTicker$default = FlowKt__DelayKt.fixedPeriodTicker$default(coroutineScope2, this.$periodMillis, 0L, 2, null);
            flowKt__DelayKt$sample$2 = this;
            coroutineScope = coroutineScope2;
            receiveChannel = fixedPeriodTicker$default;
            obj2 = coroutine_suspended;
            if (objectRef2.element == NullSurrogateKt.DONE) {
            }
        } else if (i == 1) {
            ReceiveChannel receiveChannel2 = (ReceiveChannel) this.L$4;
            Ref.ObjectRef objectRef3 = (Ref.ObjectRef) this.L$3;
            Ref.ObjectRef objectRef4 = (Ref.ObjectRef) this.L$2;
            FlowCollector flowCollector2 = (FlowCollector) this.L$1;
            CoroutineScope coroutineScope3 = (CoroutineScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            flowKt__DelayKt$sample$2 = this;
            receiveChannel = receiveChannel2;
            objectRef2 = objectRef3;
            objectRef = objectRef4;
            flowCollector = flowCollector2;
            coroutineScope = coroutineScope3;
            obj2 = coroutine_suspended;
            i2 = 1;
            if (objectRef2.element == NullSurrogateKt.DONE) {
                flowKt__DelayKt$sample$2.L$0 = coroutineScope;
                flowKt__DelayKt$sample$2.L$1 = flowCollector;
                flowKt__DelayKt$sample$2.L$2 = objectRef;
                flowKt__DelayKt$sample$2.L$3 = objectRef2;
                flowKt__DelayKt$sample$2.L$4 = receiveChannel;
                flowKt__DelayKt$sample$2.L$5 = flowKt__DelayKt$sample$2;
                flowKt__DelayKt$sample$2.label = i2;
                FlowKt__DelayKt$sample$2<T> flowKt__DelayKt$sample$23 = flowKt__DelayKt$sample$2;
                SelectInstance selectInstance2 = new SelectInstance(flowKt__DelayKt$sample$23);
                try {
                } catch (Throwable th) {
                    th = th;
                    flowKt__DelayKt$sample$22 = flowKt__DelayKt$sample$23;
                    selectInstance = selectInstance2;
                }
                SelectInstance selectInstance3 = selectInstance2;
                flowKt__DelayKt$sample$22 = flowKt__DelayKt$sample$23;
                selectInstance3.invoke(((ReceiveChannel) objectRef.element).getOnReceiveOrNull(), new FlowKt__DelayKt$sample$2$invokeSuspend$$inlined$select$lambda$1(null, objectRef, receiveChannel, objectRef2, flowCollector));
                selectInstance3.invoke(receiveChannel.getOnReceive(), new FlowKt__DelayKt$sample$2$invokeSuspend$$inlined$select$lambda$2(null, objectRef, receiveChannel, objectRef2, flowCollector));
                selectInstance = selectInstance2;
                Object result = selectInstance.getResult();
                if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                    DebugProbesKt.probeCoroutineSuspended(flowKt__DelayKt$sample$22);
                }
                if (result == obj2) {
                    return obj2;
                }
                i2 = 1;
                if (objectRef2.element == NullSurrogateKt.DONE) {
                    return Unit.INSTANCE;
                }
            }
        } else {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
