package kotlinx.coroutines.flow;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Ref;
import kotlinx.coroutines.flow.internal.NullSurrogateKt;
import kotlinx.coroutines.internal.Symbol;

/* compiled from: Delay.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u00022\b\u0010\u0003\u001a\u0004\u0018\u00010\u0004H\u008a@¢\u0006\u0004\b\u0005\u0010\u0006¨\u0006\u0007"}, d2 = {"<anonymous>", "", "T", "value", "", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "kotlinx/coroutines/flow/FlowKt__DelayKt$debounceInternal$1$3$2"}, k = 3, mv = {1, 4, 0})
/* loaded from: classes.dex */
final class FlowKt__DelayKt$debounceInternal$1$invokeSuspend$$inlined$select$lambda$2 extends SuspendLambda implements Function2<Object, Continuation<? super Unit>, Object> {
    final /* synthetic */ FlowCollector $downstream$inlined;
    final /* synthetic */ Ref.ObjectRef $lastValue$inlined;
    final /* synthetic */ Ref.LongRef $timeoutMillis$inlined;
    final /* synthetic */ Ref.ObjectRef $values$inlined;
    Object L$0;
    int label;
    private Object p$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    FlowKt__DelayKt$debounceInternal$1$invokeSuspend$$inlined$select$lambda$2(Continuation continuation, Ref.ObjectRef objectRef, Ref.LongRef longRef, FlowCollector flowCollector, Ref.ObjectRef objectRef2) {
        super(2, continuation);
        this.$lastValue$inlined = objectRef;
        this.$timeoutMillis$inlined = longRef;
        this.$downstream$inlined = flowCollector;
        this.$values$inlined = objectRef2;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        FlowKt__DelayKt$debounceInternal$1$invokeSuspend$$inlined$select$lambda$2 flowKt__DelayKt$debounceInternal$1$invokeSuspend$$inlined$select$lambda$2 = new FlowKt__DelayKt$debounceInternal$1$invokeSuspend$$inlined$select$lambda$2(continuation, this.$lastValue$inlined, this.$timeoutMillis$inlined, this.$downstream$inlined, this.$values$inlined);
        flowKt__DelayKt$debounceInternal$1$invokeSuspend$$inlined$select$lambda$2.p$0 = obj;
        return flowKt__DelayKt$debounceInternal$1$invokeSuspend$$inlined$select$lambda$2;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(Object obj, Continuation<? super Unit> continuation) {
        return ((FlowKt__DelayKt$debounceInternal$1$invokeSuspend$$inlined$select$lambda$2) create(obj, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [T, kotlinx.coroutines.internal.Symbol] */
    /* JADX WARN: Type inference failed for: r6v1, types: [T, java.lang.Object] */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            ?? r6 = this.p$0;
            if (r6 == 0) {
                if (this.$lastValue$inlined.element != 0) {
                    FlowCollector flowCollector = this.$downstream$inlined;
                    Symbol symbol = NullSurrogateKt.NULL;
                    Object obj2 = this.$lastValue$inlined.element;
                    if (obj2 == symbol) {
                        obj2 = null;
                    }
                    this.L$0 = r6;
                    this.label = 1;
                    if (flowCollector.emit(obj2, this) == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                }
            } else {
                this.$lastValue$inlined.element = r6;
                return Unit.INSTANCE;
            }
        } else {
            if (i != 1) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
        }
        this.$lastValue$inlined.element = NullSurrogateKt.DONE;
        return Unit.INSTANCE;
    }
}
