package kotlinx.coroutines.flow;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;

/* JADX INFO: Add missing generic type declarations: [T] */
/* compiled from: SafeCollector.common.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0019\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002*\u0001\u0000\b\n\u0018\u00002\b\u0012\u0004\u0012\u00028\u00000\u0001J\u001f\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00028\u00000\u0005H\u0096@ø\u0001\u0000¢\u0006\u0002\u0010\u0006\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u0007¸\u0006\u0000"}, d2 = {"kotlinx/coroutines/flow/internal/SafeCollector_commonKt$unsafeFlow$1", "Lkotlinx/coroutines/flow/Flow;", "collect", "", "collector", "Lkotlinx/coroutines/flow/FlowCollector;", "(Lkotlinx/coroutines/flow/FlowCollector;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "kotlinx-coroutines-core"}, k = 1, mv = {1, 4, 0})
/* loaded from: classes.dex */
public final class FlowKt__BuildersKt$asFlow$$inlined$unsafeFlow$6<T> implements Flow<T> {
    final /* synthetic */ Object[] $this_asFlow$inlined;

    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001a\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\u0010\u0000\u001a\u0004\u0018\u00010\u0001\"\u0004\b\u0000\u0010\u00022\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u0002H\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u0096@¨\u0006\b"}, d2 = {"collect", "", "T", "collector", "Lkotlinx/coroutines/flow/FlowCollector;", "continuation", "Lkotlin/coroutines/Continuation;", "", "kotlinx/coroutines/flow/internal/SafeCollector_commonKt$unsafeFlow$1$collect$1"}, k = 3, mv = {1, 4, 0})
    @DebugMetadata(c = "kotlinx.coroutines.flow.FlowKt__BuildersKt$asFlow$$inlined$unsafeFlow$6", f = "Builders.kt", i = {0, 0, 0, 0, 0, 0, 0}, l = {115}, m = "collect", n = {"this", "collector", "continuation", "$receiver", "$this$forEach$iv", "element$iv", "value"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$6", "L$7"})
    /* renamed from: kotlinx.coroutines.flow.FlowKt__BuildersKt$asFlow$$inlined$unsafeFlow$6$1, reason: invalid class name */
    public static final class AnonymousClass1 extends ContinuationImpl {
        int I$0;
        int I$1;
        Object L$0;
        Object L$1;
        Object L$2;
        Object L$3;
        Object L$4;
        Object L$5;
        Object L$6;
        Object L$7;
        int label;
        /* synthetic */ Object result;

        public AnonymousClass1(Continuation continuation) {
            super(continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return FlowKt__BuildersKt$asFlow$$inlined$unsafeFlow$6.this.collect(null, this);
        }
    }

    public FlowKt__BuildersKt$asFlow$$inlined$unsafeFlow$6(Object[] objArr) {
        this.$this_asFlow$inlined = objArr;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:12:0x006e  */
    /* JADX WARN: Removed duplicated region for block: B:16:0x008f  */
    /* JADX WARN: Removed duplicated region for block: B:20:0x0059  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:13:0x008a -> B:10:0x008d). Please report as a decompilation issue!!! */
    @Override // kotlinx.coroutines.flow.Flow
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object collect(FlowCollector flowCollector, Continuation continuation) {
        AnonymousClass1 anonymousClass1;
        int i;
        int length;
        FlowKt__BuildersKt$asFlow$$inlined$unsafeFlow$6<T> flowKt__BuildersKt$asFlow$$inlined$unsafeFlow$6;
        Continuation continuation2;
        Object[] objArr;
        Object obj;
        AnonymousClass1 anonymousClass12;
        int i2;
        FlowCollector flowCollector2;
        Object[] objArr2;
        FlowCollector flowCollector3;
        if (continuation instanceof AnonymousClass1) {
            anonymousClass1 = (AnonymousClass1) continuation;
            if ((anonymousClass1.label & Integer.MIN_VALUE) != 0) {
                anonymousClass1.label -= Integer.MIN_VALUE;
                Object obj2 = anonymousClass1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = anonymousClass1.label;
                if (i != 0) {
                    ResultKt.throwOnFailure(obj2);
                    Object[] objArr3 = this.$this_asFlow$inlined;
                    length = objArr3.length;
                    flowKt__BuildersKt$asFlow$$inlined$unsafeFlow$6 = this;
                    continuation2 = anonymousClass1;
                    objArr = objArr3;
                    FlowCollector flowCollector4 = flowCollector;
                    obj = coroutine_suspended;
                    anonymousClass12 = anonymousClass1;
                    i2 = 0;
                    flowCollector2 = flowCollector4;
                    objArr2 = objArr;
                    flowCollector3 = flowCollector4;
                    if (i2 < length) {
                    }
                } else {
                    if (i != 1) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    Object obj3 = anonymousClass1.L$7;
                    Object obj4 = anonymousClass1.L$6;
                    i2 = anonymousClass1.I$1;
                    int i3 = anonymousClass1.I$0;
                    Object[] objArr4 = (Object[]) anonymousClass1.L$5;
                    Object[] objArr5 = (Object[]) anonymousClass1.L$4;
                    FlowCollector flowCollector5 = (FlowCollector) anonymousClass1.L$3;
                    continuation2 = (Continuation) anonymousClass1.L$2;
                    FlowCollector flowCollector6 = (FlowCollector) anonymousClass1.L$1;
                    flowKt__BuildersKt$asFlow$$inlined$unsafeFlow$6 = (FlowKt__BuildersKt$asFlow$$inlined$unsafeFlow$6) anonymousClass1.L$0;
                    ResultKt.throwOnFailure(obj2);
                    FlowCollector flowCollector7 = flowCollector5;
                    objArr = objArr5;
                    objArr2 = objArr4;
                    length = i3;
                    obj = coroutine_suspended;
                    anonymousClass12 = anonymousClass1;
                    flowCollector2 = flowCollector6;
                    i2++;
                    flowCollector3 = flowCollector7;
                    if (i2 < length) {
                        Object obj5 = objArr2[i2];
                        anonymousClass12.L$0 = flowKt__BuildersKt$asFlow$$inlined$unsafeFlow$6;
                        anonymousClass12.L$1 = flowCollector2;
                        anonymousClass12.L$2 = continuation2;
                        anonymousClass12.L$3 = flowCollector3;
                        anonymousClass12.L$4 = objArr;
                        anonymousClass12.L$5 = objArr2;
                        anonymousClass12.I$0 = length;
                        anonymousClass12.I$1 = i2;
                        anonymousClass12.L$6 = obj5;
                        anonymousClass12.L$7 = obj5;
                        anonymousClass12.label = 1;
                        Object emit = flowCollector3.emit(obj5, anonymousClass12);
                        flowCollector7 = flowCollector3;
                        if (emit == obj) {
                            return obj;
                        }
                        i2++;
                        flowCollector3 = flowCollector7;
                        if (i2 < length) {
                            return Unit.INSTANCE;
                        }
                    }
                }
            }
        }
        anonymousClass1 = new AnonymousClass1(continuation);
        Object obj22 = anonymousClass1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = anonymousClass1.label;
        if (i != 0) {
        }
    }
}
