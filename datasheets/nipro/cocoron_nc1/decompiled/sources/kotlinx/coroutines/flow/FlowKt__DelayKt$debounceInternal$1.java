package kotlinx.coroutines.flow;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.Ref;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.DebugKt;
import kotlinx.coroutines.channels.ProduceKt;
import kotlinx.coroutines.channels.ReceiveChannel;
import kotlinx.coroutines.flow.internal.NullSurrogateKt;
import kotlinx.coroutines.internal.Symbol;
import kotlinx.coroutines.selects.SelectInstance;

/* JADX INFO: Add missing generic type declarations: [T] */
/* compiled from: Delay.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0016\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0005H\u008a@¢\u0006\u0004\b\u0006\u0010\u0007"}, d2 = {"<anonymous>", "", "T", "Lkotlinx/coroutines/CoroutineScope;", "downstream", "Lkotlinx/coroutines/flow/FlowCollector;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 0})
@DebugMetadata(c = "kotlinx.coroutines.flow.FlowKt__DelayKt$debounceInternal$1", f = "Delay.kt", i = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, l = {354, 358}, m = "invokeSuspend", n = {"$this$scopedFlow", "downstream", "values", "lastValue", "timeoutMillis", "$this$scopedFlow", "downstream", "values", "lastValue", "timeoutMillis"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4"})
/* loaded from: classes.dex */
final class FlowKt__DelayKt$debounceInternal$1<T> extends SuspendLambda implements Function3<CoroutineScope, FlowCollector<? super T>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Flow $this_debounceInternal;
    final /* synthetic */ Function1 $timeoutMillisSelector;
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
    FlowKt__DelayKt$debounceInternal$1(Flow flow, Function1 function1, Continuation continuation) {
        super(3, continuation);
        this.$this_debounceInternal = flow;
        this.$timeoutMillisSelector = function1;
    }

    public final Continuation<Unit> create(CoroutineScope coroutineScope, FlowCollector<? super T> flowCollector, Continuation<? super Unit> continuation) {
        FlowKt__DelayKt$debounceInternal$1 flowKt__DelayKt$debounceInternal$1 = new FlowKt__DelayKt$debounceInternal$1(this.$this_debounceInternal, this.$timeoutMillisSelector, continuation);
        flowKt__DelayKt$debounceInternal$1.p$ = coroutineScope;
        flowKt__DelayKt$debounceInternal$1.p$0 = flowCollector;
        return flowKt__DelayKt$debounceInternal$1;
    }

    @Override // kotlin.jvm.functions.Function3
    public final Object invoke(CoroutineScope coroutineScope, Object obj, Continuation<? super Unit> continuation) {
        return ((FlowKt__DelayKt$debounceInternal$1) create(coroutineScope, (FlowCollector) obj, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Can't wrap try/catch for region: R(14:9|(5:11|(1:13)|14|(1:16)(1:30)|(2:28|29)(2:18|(5:20|(1:22)|23|(1:25)|27)))|31|32|(4:34|(1:43)(1:38)|39|(2:41|42))|44|45|46|(5:48|49|50|51|52)(1:67)|53|54|55|(1:57)|(1:59)(4:60|6|7|(2:71|72)(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(5:48|49|50|51|52) */
    /* JADX WARN: Code restructure failed: missing block: B:62:0x017b, code lost:
    
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:63:0x0186, code lost:
    
        r8.handleBuilderException(r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:65:0x0154, code lost:
    
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:66:0x0155, code lost:
    
        r4 = r14;
     */
    /* JADX WARN: Code restructure failed: missing block: B:69:0x017d, code lost:
    
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:70:0x017e, code lost:
    
        r8 = r10;
        r19 = r11;
        r20 = r12;
        r21 = r13;
        r4 = r14;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00f6  */
    /* JADX WARN: Removed duplicated region for block: B:48:0x0134 A[Catch: all -> 0x017d, TRY_LEAVE, TryCatch #2 {all -> 0x017d, blocks: (B:46:0x012d, B:48:0x0134), top: B:45:0x012d }] */
    /* JADX WARN: Removed duplicated region for block: B:57:0x0193  */
    /* JADX WARN: Removed duplicated region for block: B:59:0x0198 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:60:0x0199  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x0158  */
    /* JADX WARN: Removed duplicated region for block: B:71:0x01a8  */
    /* JADX WARN: Removed duplicated region for block: B:9:0x008e  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:60:0x0199 -> B:6:0x01a1). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        Ref.ObjectRef objectRef;
        CoroutineScope coroutineScope;
        FlowKt__DelayKt$debounceInternal$1<T> flowKt__DelayKt$debounceInternal$1;
        FlowCollector flowCollector;
        Ref.ObjectRef objectRef2;
        Ref.LongRef longRef;
        Ref.ObjectRef objectRef3;
        Object obj2;
        SelectInstance selectInstance;
        FlowKt__DelayKt$debounceInternal$1<T> flowKt__DelayKt$debounceInternal$12;
        CoroutineScope coroutineScope2;
        FlowCollector flowCollector2;
        Ref.ObjectRef objectRef4;
        Object result;
        SelectInstance selectInstance2;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        int i2 = 2;
        long j = 0;
        int i3 = 1;
        T t = null;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            CoroutineScope coroutineScope3 = this.p$;
            FlowCollector flowCollector3 = this.p$0;
            objectRef = new Ref.ObjectRef();
            objectRef.element = (T) ProduceKt.produce$default(coroutineScope3, null, 0, new FlowKt__DelayKt$debounceInternal$1$values$1(this, null), 3, null);
            Ref.ObjectRef objectRef5 = new Ref.ObjectRef();
            objectRef5.element = null;
            coroutineScope = coroutineScope3;
            flowKt__DelayKt$debounceInternal$1 = this;
            flowCollector = flowCollector3;
            objectRef2 = objectRef5;
            if (objectRef2.element != NullSurrogateKt.DONE) {
            }
        } else if (i == 1) {
            Ref.LongRef longRef2 = (Ref.LongRef) this.L$4;
            objectRef2 = (Ref.ObjectRef) this.L$3;
            objectRef = (Ref.ObjectRef) this.L$2;
            flowCollector = (FlowCollector) this.L$1;
            coroutineScope = (CoroutineScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            longRef = longRef2;
            flowKt__DelayKt$debounceInternal$1 = this;
            objectRef2.element = t;
            FlowKt__DelayKt$debounceInternal$1<T> flowKt__DelayKt$debounceInternal$13 = flowKt__DelayKt$debounceInternal$1;
            objectRef3 = objectRef2;
            Ref.ObjectRef objectRef6 = objectRef;
            obj2 = coroutine_suspended;
            Ref.LongRef longRef3 = longRef;
            FlowCollector flowCollector4 = flowCollector;
            if (DebugKt.getASSERTIONS_ENABLED()) {
            }
            flowKt__DelayKt$debounceInternal$13.L$0 = coroutineScope;
            flowKt__DelayKt$debounceInternal$13.L$1 = flowCollector4;
            flowKt__DelayKt$debounceInternal$13.L$2 = objectRef6;
            flowKt__DelayKt$debounceInternal$13.L$3 = objectRef3;
            flowKt__DelayKt$debounceInternal$13.L$4 = longRef3;
            flowKt__DelayKt$debounceInternal$13.L$5 = flowKt__DelayKt$debounceInternal$13;
            flowKt__DelayKt$debounceInternal$13.label = i2;
            FlowKt__DelayKt$debounceInternal$1<T> flowKt__DelayKt$debounceInternal$14 = flowKt__DelayKt$debounceInternal$13;
            SelectInstance selectInstance3 = new SelectInstance(flowKt__DelayKt$debounceInternal$14);
            SelectInstance selectInstance4 = selectInstance3;
            if (objectRef3.element == null) {
            }
            selectInstance2.invoke(((ReceiveChannel) objectRef4.element).getOnReceiveOrNull(), new FlowKt__DelayKt$debounceInternal$1$invokeSuspend$$inlined$select$lambda$2(null, objectRef3, longRef3, flowCollector2, objectRef4));
            result = selectInstance.getResult();
            if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
            }
            if (result != obj2) {
            }
        } else if (i == 2) {
            Ref.ObjectRef objectRef7 = (Ref.ObjectRef) this.L$3;
            Ref.ObjectRef objectRef8 = (Ref.ObjectRef) this.L$2;
            FlowCollector flowCollector5 = (FlowCollector) this.L$1;
            CoroutineScope coroutineScope4 = (CoroutineScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            coroutineScope = coroutineScope4;
            flowCollector = flowCollector5;
            objectRef = objectRef8;
            objectRef2 = objectRef7;
            flowKt__DelayKt$debounceInternal$1 = this;
            i2 = 2;
            j = 0;
            i3 = 1;
            t = null;
            if (objectRef2.element != NullSurrogateKt.DONE) {
                longRef = new Ref.LongRef();
                longRef.element = j;
                if (objectRef2.element != null) {
                    Function1 function1 = flowKt__DelayKt$debounceInternal$1.$timeoutMillisSelector;
                    Symbol symbol = NullSurrogateKt.NULL;
                    T t2 = objectRef2.element;
                    if (t2 == symbol) {
                        t2 = t;
                    }
                    longRef.element = ((Number) function1.invoke(t2)).longValue();
                    if ((longRef.element >= j ? i3 : 0) == 0) {
                        throw new IllegalArgumentException("Debounce timeout should not be negative".toString());
                    }
                    if (longRef.element == j) {
                        Symbol symbol2 = NullSurrogateKt.NULL;
                        T t3 = objectRef2.element;
                        if (t3 == symbol2) {
                            t3 = t;
                        }
                        flowKt__DelayKt$debounceInternal$1.L$0 = coroutineScope;
                        flowKt__DelayKt$debounceInternal$1.L$1 = flowCollector;
                        flowKt__DelayKt$debounceInternal$1.L$2 = objectRef;
                        flowKt__DelayKt$debounceInternal$1.L$3 = objectRef2;
                        flowKt__DelayKt$debounceInternal$1.L$4 = longRef;
                        flowKt__DelayKt$debounceInternal$1.label = i3;
                        if (flowCollector.emit(t3, flowKt__DelayKt$debounceInternal$1) == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        objectRef2.element = t;
                    }
                }
                FlowKt__DelayKt$debounceInternal$1<T> flowKt__DelayKt$debounceInternal$132 = flowKt__DelayKt$debounceInternal$1;
                objectRef3 = objectRef2;
                Ref.ObjectRef objectRef62 = objectRef;
                obj2 = coroutine_suspended;
                Ref.LongRef longRef32 = longRef;
                FlowCollector flowCollector42 = flowCollector;
                if (DebugKt.getASSERTIONS_ENABLED()) {
                    if (!Boxing.boxBoolean((objectRef3.element == null || longRef32.element > j) ? i3 : 0).booleanValue()) {
                        throw new AssertionError();
                    }
                }
                flowKt__DelayKt$debounceInternal$132.L$0 = coroutineScope;
                flowKt__DelayKt$debounceInternal$132.L$1 = flowCollector42;
                flowKt__DelayKt$debounceInternal$132.L$2 = objectRef62;
                flowKt__DelayKt$debounceInternal$132.L$3 = objectRef3;
                flowKt__DelayKt$debounceInternal$132.L$4 = longRef32;
                flowKt__DelayKt$debounceInternal$132.L$5 = flowKt__DelayKt$debounceInternal$132;
                flowKt__DelayKt$debounceInternal$132.label = i2;
                FlowKt__DelayKt$debounceInternal$1<T> flowKt__DelayKt$debounceInternal$142 = flowKt__DelayKt$debounceInternal$132;
                SelectInstance selectInstance32 = new SelectInstance(flowKt__DelayKt$debounceInternal$142);
                SelectInstance selectInstance42 = selectInstance32;
                if (objectRef3.element == null) {
                    selectInstance2 = selectInstance42;
                    selectInstance = selectInstance32;
                    flowKt__DelayKt$debounceInternal$12 = flowKt__DelayKt$debounceInternal$142;
                    coroutineScope2 = coroutineScope;
                    flowCollector2 = flowCollector42;
                    selectInstance2.onTimeout(longRef32.element, new FlowKt__DelayKt$debounceInternal$1$invokeSuspend$$inlined$select$lambda$1(null, objectRef3, longRef32, flowCollector42, objectRef62));
                    objectRef4 = objectRef62;
                } else {
                    selectInstance2 = selectInstance42;
                    selectInstance = selectInstance32;
                    flowKt__DelayKt$debounceInternal$12 = flowKt__DelayKt$debounceInternal$142;
                    coroutineScope2 = coroutineScope;
                    flowCollector2 = flowCollector42;
                    objectRef4 = objectRef62;
                }
                selectInstance2.invoke(((ReceiveChannel) objectRef4.element).getOnReceiveOrNull(), new FlowKt__DelayKt$debounceInternal$1$invokeSuspend$$inlined$select$lambda$2(null, objectRef3, longRef32, flowCollector2, objectRef4));
                result = selectInstance.getResult();
                if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                    DebugProbesKt.probeCoroutineSuspended(flowKt__DelayKt$debounceInternal$12);
                }
                if (result != obj2) {
                    return obj2;
                }
                coroutine_suspended = obj2;
                objectRef2 = objectRef3;
                objectRef = objectRef4;
                flowKt__DelayKt$debounceInternal$1 = flowKt__DelayKt$debounceInternal$132;
                coroutineScope = coroutineScope2;
                flowCollector = flowCollector2;
                i2 = 2;
                j = 0;
                i3 = 1;
                t = null;
                if (objectRef2.element != NullSurrogateKt.DONE) {
                    return Unit.INSTANCE;
                }
            }
        } else {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
