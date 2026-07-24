package kotlinx.coroutines.flow;

import java.util.NoSuchElementException;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.InlineMarker;
import kotlin.jvm.internal.Ref;
import kotlinx.coroutines.flow.internal.AbortFlowException;
import kotlinx.coroutines.flow.internal.FlowExceptions_commonKt;
import kotlinx.coroutines.flow.internal.NullSurrogateKt;

/* compiled from: Reduce.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000,\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\u0010\u0000\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\u001a!\u0010\u0000\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\b\u0012\u0004\u0012\u0002H\u00010\u0002H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0003\u001aE\u0010\u0000\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\b\u0012\u0004\u0012\u0002H\u00010\u00022\"\u0010\u0004\u001a\u001e\b\u0001\u0012\u0004\u0012\u0002H\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u0006\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0005H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\t\u001a#\u0010\n\u001a\u0004\u0018\u0001H\u0001\"\u0004\b\u0000\u0010\u0001*\b\u0012\u0004\u0012\u0002H\u00010\u0002H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0003\u001aG\u0010\n\u001a\u0004\u0018\u0001H\u0001\"\u0004\b\u0000\u0010\u0001*\b\u0012\u0004\u0012\u0002H\u00010\u00022\"\u0010\u0004\u001a\u001e\b\u0001\u0012\u0004\u0012\u0002H\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u0006\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0005H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\t\u001ay\u0010\u000b\u001a\u0002H\f\"\u0004\b\u0000\u0010\u0001\"\u0004\b\u0001\u0010\f*\b\u0012\u0004\u0012\u0002H\u00010\u00022\u0006\u0010\r\u001a\u0002H\f2H\b\u0004\u0010\u000e\u001aB\b\u0001\u0012\u0013\u0012\u0011H\f¢\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0012\u0012\u0013\u0012\u0011H\u0001¢\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0013\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\f0\u0006\u0012\u0006\u0012\u0004\u0018\u00010\b0\u000fH\u0086Hø\u0001\u0000¢\u0006\u0002\u0010\u0014\u001as\u0010\u0015\u001a\u0002H\u0016\"\u0004\b\u0000\u0010\u0016\"\b\b\u0001\u0010\u0001*\u0002H\u0016*\b\u0012\u0004\u0012\u0002H\u00010\u00022F\u0010\u000e\u001aB\b\u0001\u0012\u0013\u0012\u0011H\u0016¢\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0017\u0012\u0013\u0012\u0011H\u0001¢\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0013\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00160\u0006\u0012\u0006\u0012\u0004\u0018\u00010\b0\u000fH\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0018\u001a!\u0010\u0019\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\b\u0012\u0004\u0012\u0002H\u00010\u0002H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0003\u001a#\u0010\u001a\u001a\u0004\u0018\u0001H\u0001\"\u0004\b\u0000\u0010\u0001*\b\u0012\u0004\u0012\u0002H\u00010\u0002H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0003\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u001b"}, d2 = {"first", "T", "Lkotlinx/coroutines/flow/Flow;", "(Lkotlinx/coroutines/flow/Flow;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "predicate", "Lkotlin/Function2;", "Lkotlin/coroutines/Continuation;", "", "", "(Lkotlinx/coroutines/flow/Flow;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "firstOrNull", "fold", "R", "initial", "operation", "Lkotlin/Function3;", "Lkotlin/ParameterName;", "name", "acc", "value", "(Lkotlinx/coroutines/flow/Flow;Ljava/lang/Object;Lkotlin/jvm/functions/Function3;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "reduce", "S", "accumulator", "(Lkotlinx/coroutines/flow/Flow;Lkotlin/jvm/functions/Function3;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "single", "singleOrNull", "kotlinx-coroutines-core"}, k = 5, mv = {1, 4, 0}, xs = "kotlinx/coroutines/flow/FlowKt")
/* loaded from: classes.dex */
final /* synthetic */ class FlowKt__ReduceKt {
    /* JADX WARN: Removed duplicated region for block: B:12:0x006d  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0070  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0042  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /* JADX WARN: Type inference failed for: r2v1, types: [T, kotlinx.coroutines.internal.Symbol] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final <S, T extends S> Object reduce(Flow<? extends T> flow, Function3<? super S, ? super T, ? super Continuation<? super S>, ? extends Object> function3, Continuation<? super S> continuation) {
        FlowKt__ReduceKt$reduce$1 flowKt__ReduceKt$reduce$1;
        int i;
        Ref.ObjectRef objectRef;
        if (continuation instanceof FlowKt__ReduceKt$reduce$1) {
            flowKt__ReduceKt$reduce$1 = (FlowKt__ReduceKt$reduce$1) continuation;
            if ((flowKt__ReduceKt$reduce$1.label & Integer.MIN_VALUE) != 0) {
                flowKt__ReduceKt$reduce$1.label -= Integer.MIN_VALUE;
                Object obj = flowKt__ReduceKt$reduce$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = flowKt__ReduceKt$reduce$1.label;
                if (i != 0) {
                    ResultKt.throwOnFailure(obj);
                    Ref.ObjectRef objectRef2 = new Ref.ObjectRef();
                    objectRef2.element = NullSurrogateKt.NULL;
                    FlowCollector<? super Object> flowKt__ReduceKt$reduce$$inlined$collect$1 = new FlowKt__ReduceKt$reduce$$inlined$collect$1<>(objectRef2, function3);
                    flowKt__ReduceKt$reduce$1.L$0 = flow;
                    flowKt__ReduceKt$reduce$1.L$1 = function3;
                    flowKt__ReduceKt$reduce$1.L$2 = objectRef2;
                    flowKt__ReduceKt$reduce$1.L$3 = flow;
                    flowKt__ReduceKt$reduce$1.label = 1;
                    if (flow.collect(flowKt__ReduceKt$reduce$$inlined$collect$1, flowKt__ReduceKt$reduce$1) == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                    objectRef = objectRef2;
                } else {
                    if (i != 1) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    objectRef = (Ref.ObjectRef) flowKt__ReduceKt$reduce$1.L$2;
                    ResultKt.throwOnFailure(obj);
                }
                if (objectRef.element != NullSurrogateKt.NULL) {
                    throw new NoSuchElementException("Empty flow can't be reduced");
                }
                return objectRef.element;
            }
        }
        flowKt__ReduceKt$reduce$1 = new FlowKt__ReduceKt$reduce$1(continuation);
        Object obj2 = flowKt__ReduceKt$reduce$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = flowKt__ReduceKt$reduce$1.label;
        if (i != 0) {
        }
        if (objectRef.element != NullSurrogateKt.NULL) {
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0044  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final <T, R> Object fold(Flow<? extends T> flow, R r, Function3<? super R, ? super T, ? super Continuation<? super R>, ? extends Object> function3, Continuation<? super R> continuation) {
        FlowKt__ReduceKt$fold$1 flowKt__ReduceKt$fold$1;
        int i;
        Ref.ObjectRef objectRef;
        if (continuation instanceof FlowKt__ReduceKt$fold$1) {
            flowKt__ReduceKt$fold$1 = (FlowKt__ReduceKt$fold$1) continuation;
            if ((flowKt__ReduceKt$fold$1.label & Integer.MIN_VALUE) != 0) {
                flowKt__ReduceKt$fold$1.label -= Integer.MIN_VALUE;
                Object obj = flowKt__ReduceKt$fold$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = flowKt__ReduceKt$fold$1.label;
                if (i != 0) {
                    ResultKt.throwOnFailure(obj);
                    Ref.ObjectRef objectRef2 = new Ref.ObjectRef();
                    objectRef2.element = r;
                    FlowCollector<? super Object> flowKt__ReduceKt$fold$$inlined$collect$1 = new FlowKt__ReduceKt$fold$$inlined$collect$1<>(objectRef2, function3);
                    flowKt__ReduceKt$fold$1.L$0 = flow;
                    flowKt__ReduceKt$fold$1.L$1 = r;
                    flowKt__ReduceKt$fold$1.L$2 = function3;
                    flowKt__ReduceKt$fold$1.L$3 = objectRef2;
                    flowKt__ReduceKt$fold$1.L$4 = flow;
                    flowKt__ReduceKt$fold$1.label = 1;
                    if (flow.collect(flowKt__ReduceKt$fold$$inlined$collect$1, flowKt__ReduceKt$fold$1) == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                    objectRef = objectRef2;
                } else {
                    if (i != 1) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    objectRef = (Ref.ObjectRef) flowKt__ReduceKt$fold$1.L$3;
                    Object obj2 = flowKt__ReduceKt$fold$1.L$1;
                    ResultKt.throwOnFailure(obj);
                }
                return objectRef.element;
            }
        }
        flowKt__ReduceKt$fold$1 = new FlowKt__ReduceKt$fold$1(continuation);
        Object obj3 = flowKt__ReduceKt$fold$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = flowKt__ReduceKt$fold$1.label;
        if (i != 0) {
        }
        return objectRef.element;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static final Object fold$$forInline(Flow flow, Object obj, Function3 function3, Continuation continuation) {
        Ref.ObjectRef objectRef = new Ref.ObjectRef();
        objectRef.element = obj;
        FlowKt__ReduceKt$fold$$inlined$collect$1 flowKt__ReduceKt$fold$$inlined$collect$1 = new FlowKt__ReduceKt$fold$$inlined$collect$1(objectRef, function3);
        InlineMarker.mark(0);
        flow.collect(flowKt__ReduceKt$fold$$inlined$collect$1, continuation);
        InlineMarker.mark(2);
        InlineMarker.mark(1);
        return objectRef.element;
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x0067  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x006a  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x003e  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final <T> Object single(Flow<? extends T> flow, Continuation<? super T> continuation) {
        FlowKt__ReduceKt$single$1 flowKt__ReduceKt$single$1;
        int i;
        Ref.ObjectRef objectRef;
        if (continuation instanceof FlowKt__ReduceKt$single$1) {
            flowKt__ReduceKt$single$1 = (FlowKt__ReduceKt$single$1) continuation;
            if ((flowKt__ReduceKt$single$1.label & Integer.MIN_VALUE) != 0) {
                flowKt__ReduceKt$single$1.label -= Integer.MIN_VALUE;
                Object obj = flowKt__ReduceKt$single$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = flowKt__ReduceKt$single$1.label;
                if (i != 0) {
                    ResultKt.throwOnFailure(obj);
                    final Ref.ObjectRef objectRef2 = new Ref.ObjectRef();
                    objectRef2.element = (T) NullSurrogateKt.NULL;
                    FlowCollector<T> flowCollector = new FlowCollector<T>() { // from class: kotlinx.coroutines.flow.FlowKt__ReduceKt$single$$inlined$collect$1
                        /* JADX WARN: Multi-variable type inference failed */
                        @Override // kotlinx.coroutines.flow.FlowCollector
                        public Object emit(Object obj2, Continuation continuation2) {
                            if (!(Ref.ObjectRef.this.element == NullSurrogateKt.NULL)) {
                                throw new IllegalArgumentException("Flow has more than one element".toString());
                            }
                            Ref.ObjectRef.this.element = obj2;
                            return Unit.INSTANCE;
                        }
                    };
                    flowKt__ReduceKt$single$1.L$0 = flow;
                    flowKt__ReduceKt$single$1.L$1 = objectRef2;
                    flowKt__ReduceKt$single$1.L$2 = flow;
                    flowKt__ReduceKt$single$1.label = 1;
                    if (flow.collect(flowCollector, flowKt__ReduceKt$single$1) == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                    objectRef = objectRef2;
                } else {
                    if (i != 1) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    objectRef = (Ref.ObjectRef) flowKt__ReduceKt$single$1.L$1;
                    ResultKt.throwOnFailure(obj);
                }
                if (objectRef.element != NullSurrogateKt.NULL) {
                    throw new NoSuchElementException("Flow is empty");
                }
                return objectRef.element;
            }
        }
        flowKt__ReduceKt$single$1 = new FlowKt__ReduceKt$single$1(continuation);
        Object obj2 = flowKt__ReduceKt$single$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = flowKt__ReduceKt$single$1.label;
        if (i != 0) {
        }
        if (objectRef.element != NullSurrogateKt.NULL) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x007a A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:16:0x007c  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0044  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final <T> Object singleOrNull(Flow<? extends T> flow, Continuation<? super T> continuation) {
        FlowKt__ReduceKt$singleOrNull$1 flowKt__ReduceKt$singleOrNull$1;
        int i;
        Ref.ObjectRef objectRef;
        AbortFlowException e;
        FlowCollector<T> flowCollector;
        if (continuation instanceof FlowKt__ReduceKt$singleOrNull$1) {
            flowKt__ReduceKt$singleOrNull$1 = (FlowKt__ReduceKt$singleOrNull$1) continuation;
            if ((flowKt__ReduceKt$singleOrNull$1.label & Integer.MIN_VALUE) != 0) {
                flowKt__ReduceKt$singleOrNull$1.label -= Integer.MIN_VALUE;
                Object obj = flowKt__ReduceKt$singleOrNull$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = flowKt__ReduceKt$singleOrNull$1.label;
                if (i != 0) {
                    ResultKt.throwOnFailure(obj);
                    final Ref.ObjectRef objectRef2 = new Ref.ObjectRef();
                    objectRef2.element = (T) NullSurrogateKt.NULL;
                    FlowCollector<T> flowCollector2 = new FlowCollector<T>() { // from class: kotlinx.coroutines.flow.FlowKt__ReduceKt$singleOrNull$$inlined$collectWhile$1
                        /* JADX WARN: Multi-variable type inference failed */
                        @Override // kotlinx.coroutines.flow.FlowCollector
                        public Object emit(Object obj2, Continuation continuation2) {
                            boolean z;
                            if (Ref.ObjectRef.this.element == NullSurrogateKt.NULL) {
                                Ref.ObjectRef.this.element = obj2;
                                z = true;
                            } else {
                                Ref.ObjectRef.this.element = (T) NullSurrogateKt.NULL;
                                z = false;
                            }
                            if (!Boxing.boxBoolean(z).booleanValue()) {
                                throw new AbortFlowException(this);
                            }
                            return Unit.INSTANCE;
                        }
                    };
                    try {
                        flowKt__ReduceKt$singleOrNull$1.L$0 = flow;
                        flowKt__ReduceKt$singleOrNull$1.L$1 = objectRef2;
                        flowKt__ReduceKt$singleOrNull$1.L$2 = flow;
                        flowKt__ReduceKt$singleOrNull$1.L$3 = flowCollector2;
                        flowKt__ReduceKt$singleOrNull$1.label = 1;
                        if (flow.collect(flowCollector2, flowKt__ReduceKt$singleOrNull$1) == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        objectRef = objectRef2;
                    } catch (AbortFlowException e2) {
                        objectRef = objectRef2;
                        e = e2;
                        flowCollector = flowCollector2;
                        FlowExceptions_commonKt.checkOwnership(e, flowCollector);
                        if (objectRef.element != NullSurrogateKt.NULL) {
                        }
                    }
                } else {
                    if (i != 1) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    flowCollector = (FlowKt__ReduceKt$singleOrNull$$inlined$collectWhile$1) flowKt__ReduceKt$singleOrNull$1.L$3;
                    objectRef = (Ref.ObjectRef) flowKt__ReduceKt$singleOrNull$1.L$1;
                    try {
                        ResultKt.throwOnFailure(obj);
                    } catch (AbortFlowException e3) {
                        e = e3;
                        FlowExceptions_commonKt.checkOwnership(e, flowCollector);
                        if (objectRef.element != NullSurrogateKt.NULL) {
                        }
                    }
                }
                if (objectRef.element != NullSurrogateKt.NULL) {
                    return null;
                }
                return objectRef.element;
            }
        }
        flowKt__ReduceKt$singleOrNull$1 = new FlowKt__ReduceKt$singleOrNull$1(continuation);
        Object obj2 = flowKt__ReduceKt$singleOrNull$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = flowKt__ReduceKt$singleOrNull$1.label;
        if (i != 0) {
        }
        if (objectRef.element != NullSurrogateKt.NULL) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x007a  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x007d  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0044  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final <T> Object first(Flow<? extends T> flow, Continuation<? super T> continuation) {
        FlowKt__ReduceKt$first$1 flowKt__ReduceKt$first$1;
        int i;
        Ref.ObjectRef objectRef;
        AbortFlowException e;
        FlowCollector<T> flowCollector;
        if (continuation instanceof FlowKt__ReduceKt$first$1) {
            flowKt__ReduceKt$first$1 = (FlowKt__ReduceKt$first$1) continuation;
            if ((flowKt__ReduceKt$first$1.label & Integer.MIN_VALUE) != 0) {
                flowKt__ReduceKt$first$1.label -= Integer.MIN_VALUE;
                Object obj = flowKt__ReduceKt$first$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = flowKt__ReduceKt$first$1.label;
                if (i != 0) {
                    ResultKt.throwOnFailure(obj);
                    final Ref.ObjectRef objectRef2 = new Ref.ObjectRef();
                    objectRef2.element = (T) NullSurrogateKt.NULL;
                    FlowCollector<T> flowCollector2 = new FlowCollector<T>() { // from class: kotlinx.coroutines.flow.FlowKt__ReduceKt$first$$inlined$collectWhile$1
                        /* JADX WARN: Multi-variable type inference failed */
                        @Override // kotlinx.coroutines.flow.FlowCollector
                        public Object emit(Object obj2, Continuation continuation2) {
                            Ref.ObjectRef.this.element = obj2;
                            if (!Boxing.boxBoolean(false).booleanValue()) {
                                throw new AbortFlowException(this);
                            }
                            return Unit.INSTANCE;
                        }
                    };
                    try {
                        flowKt__ReduceKt$first$1.L$0 = flow;
                        flowKt__ReduceKt$first$1.L$1 = objectRef2;
                        flowKt__ReduceKt$first$1.L$2 = flow;
                        flowKt__ReduceKt$first$1.L$3 = flowCollector2;
                        flowKt__ReduceKt$first$1.label = 1;
                        if (flow.collect(flowCollector2, flowKt__ReduceKt$first$1) == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        objectRef = objectRef2;
                    } catch (AbortFlowException e2) {
                        objectRef = objectRef2;
                        e = e2;
                        flowCollector = flowCollector2;
                        FlowExceptions_commonKt.checkOwnership(e, flowCollector);
                        if (objectRef.element != NullSurrogateKt.NULL) {
                        }
                    }
                } else {
                    if (i != 1) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    flowCollector = (FlowKt__ReduceKt$first$$inlined$collectWhile$1) flowKt__ReduceKt$first$1.L$3;
                    objectRef = (Ref.ObjectRef) flowKt__ReduceKt$first$1.L$1;
                    try {
                        ResultKt.throwOnFailure(obj);
                    } catch (AbortFlowException e3) {
                        e = e3;
                        FlowExceptions_commonKt.checkOwnership(e, flowCollector);
                        if (objectRef.element != NullSurrogateKt.NULL) {
                        }
                    }
                }
                if (objectRef.element != NullSurrogateKt.NULL) {
                    throw new NoSuchElementException("Expected at least one element");
                }
                return objectRef.element;
            }
        }
        flowKt__ReduceKt$first$1 = new FlowKt__ReduceKt$first$1(continuation);
        Object obj2 = flowKt__ReduceKt$first$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = flowKt__ReduceKt$first$1.label;
        if (i != 0) {
        }
        if (objectRef.element != NullSurrogateKt.NULL) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x0082  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x0085  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0048  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final <T> Object first(Flow<? extends T> flow, Function2<? super T, ? super Continuation<? super Boolean>, ? extends Object> function2, Continuation<? super T> continuation) {
        FlowKt__ReduceKt$first$3 flowKt__ReduceKt$first$3;
        int i;
        Function2<? super T, ? super Continuation<? super Boolean>, ? extends Object> function22;
        Ref.ObjectRef objectRef;
        AbortFlowException e;
        FlowCollector<? super Object> flowCollector;
        if (continuation instanceof FlowKt__ReduceKt$first$3) {
            flowKt__ReduceKt$first$3 = (FlowKt__ReduceKt$first$3) continuation;
            if ((flowKt__ReduceKt$first$3.label & Integer.MIN_VALUE) != 0) {
                flowKt__ReduceKt$first$3.label -= Integer.MIN_VALUE;
                Object obj = flowKt__ReduceKt$first$3.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = flowKt__ReduceKt$first$3.label;
                if (i != 0) {
                    ResultKt.throwOnFailure(obj);
                    Ref.ObjectRef objectRef2 = new Ref.ObjectRef();
                    objectRef2.element = (T) NullSurrogateKt.NULL;
                    FlowCollector<? super Object> flowKt__ReduceKt$first$$inlined$collectWhile$2 = new FlowKt__ReduceKt$first$$inlined$collectWhile$2<>(function2, objectRef2);
                    try {
                        flowKt__ReduceKt$first$3.L$0 = flow;
                        flowKt__ReduceKt$first$3.L$1 = function2;
                        flowKt__ReduceKt$first$3.L$2 = objectRef2;
                        flowKt__ReduceKt$first$3.L$3 = flow;
                        flowKt__ReduceKt$first$3.L$4 = flowKt__ReduceKt$first$$inlined$collectWhile$2;
                        flowKt__ReduceKt$first$3.label = 1;
                        if (flow.collect(flowKt__ReduceKt$first$$inlined$collectWhile$2, flowKt__ReduceKt$first$3) == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        function22 = function2;
                        objectRef = objectRef2;
                    } catch (AbortFlowException e2) {
                        function22 = function2;
                        objectRef = objectRef2;
                        e = e2;
                        flowCollector = flowKt__ReduceKt$first$$inlined$collectWhile$2;
                        FlowExceptions_commonKt.checkOwnership(e, flowCollector);
                        if (objectRef.element != NullSurrogateKt.NULL) {
                        }
                    }
                } else {
                    if (i != 1) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    flowCollector = (FlowKt__ReduceKt$first$$inlined$collectWhile$2) flowKt__ReduceKt$first$3.L$4;
                    objectRef = (Ref.ObjectRef) flowKt__ReduceKt$first$3.L$2;
                    function22 = (Function2) flowKt__ReduceKt$first$3.L$1;
                    try {
                        ResultKt.throwOnFailure(obj);
                    } catch (AbortFlowException e3) {
                        e = e3;
                        FlowExceptions_commonKt.checkOwnership(e, flowCollector);
                        if (objectRef.element != NullSurrogateKt.NULL) {
                        }
                    }
                }
                if (objectRef.element != NullSurrogateKt.NULL) {
                    throw new NoSuchElementException("Expected at least one element matching the predicate " + function22);
                }
                return objectRef.element;
            }
        }
        flowKt__ReduceKt$first$3 = new FlowKt__ReduceKt$first$3(continuation);
        Object obj2 = flowKt__ReduceKt$first$3.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = flowKt__ReduceKt$first$3.label;
        if (i != 0) {
        }
        if (objectRef.element != NullSurrogateKt.NULL) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x0044  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final <T> Object firstOrNull(Flow<? extends T> flow, Continuation<? super T> continuation) {
        FlowKt__ReduceKt$firstOrNull$1 flowKt__ReduceKt$firstOrNull$1;
        int i;
        Ref.ObjectRef objectRef;
        AbortFlowException e;
        FlowCollector<T> flowCollector;
        if (continuation instanceof FlowKt__ReduceKt$firstOrNull$1) {
            flowKt__ReduceKt$firstOrNull$1 = (FlowKt__ReduceKt$firstOrNull$1) continuation;
            if ((flowKt__ReduceKt$firstOrNull$1.label & Integer.MIN_VALUE) != 0) {
                flowKt__ReduceKt$firstOrNull$1.label -= Integer.MIN_VALUE;
                Object obj = flowKt__ReduceKt$firstOrNull$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = flowKt__ReduceKt$firstOrNull$1.label;
                if (i != 0) {
                    ResultKt.throwOnFailure(obj);
                    final Ref.ObjectRef objectRef2 = new Ref.ObjectRef();
                    objectRef2.element = null;
                    FlowCollector<T> flowCollector2 = new FlowCollector<T>() { // from class: kotlinx.coroutines.flow.FlowKt__ReduceKt$firstOrNull$$inlined$collectWhile$1
                        /* JADX WARN: Multi-variable type inference failed */
                        @Override // kotlinx.coroutines.flow.FlowCollector
                        public Object emit(Object obj2, Continuation continuation2) {
                            Ref.ObjectRef.this.element = obj2;
                            if (!Boxing.boxBoolean(false).booleanValue()) {
                                throw new AbortFlowException(this);
                            }
                            return Unit.INSTANCE;
                        }
                    };
                    try {
                        flowKt__ReduceKt$firstOrNull$1.L$0 = flow;
                        flowKt__ReduceKt$firstOrNull$1.L$1 = objectRef2;
                        flowKt__ReduceKt$firstOrNull$1.L$2 = flow;
                        flowKt__ReduceKt$firstOrNull$1.L$3 = flowCollector2;
                        flowKt__ReduceKt$firstOrNull$1.label = 1;
                        if (flow.collect(flowCollector2, flowKt__ReduceKt$firstOrNull$1) == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        objectRef = objectRef2;
                    } catch (AbortFlowException e2) {
                        objectRef = objectRef2;
                        e = e2;
                        flowCollector = flowCollector2;
                        FlowExceptions_commonKt.checkOwnership(e, flowCollector);
                        return objectRef.element;
                    }
                } else {
                    if (i != 1) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    flowCollector = (FlowKt__ReduceKt$firstOrNull$$inlined$collectWhile$1) flowKt__ReduceKt$firstOrNull$1.L$3;
                    objectRef = (Ref.ObjectRef) flowKt__ReduceKt$firstOrNull$1.L$1;
                    try {
                        ResultKt.throwOnFailure(obj);
                    } catch (AbortFlowException e3) {
                        e = e3;
                        FlowExceptions_commonKt.checkOwnership(e, flowCollector);
                        return objectRef.element;
                    }
                }
                return objectRef.element;
            }
        }
        flowKt__ReduceKt$firstOrNull$1 = new FlowKt__ReduceKt$firstOrNull$1(continuation);
        Object obj2 = flowKt__ReduceKt$firstOrNull$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = flowKt__ReduceKt$firstOrNull$1.label;
        if (i != 0) {
        }
        return objectRef.element;
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x0048  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final <T> Object firstOrNull(Flow<? extends T> flow, Function2<? super T, ? super Continuation<? super Boolean>, ? extends Object> function2, Continuation<? super T> continuation) {
        FlowKt__ReduceKt$firstOrNull$3 flowKt__ReduceKt$firstOrNull$3;
        int i;
        Ref.ObjectRef objectRef;
        AbortFlowException e;
        FlowCollector<? super Object> flowCollector;
        if (continuation instanceof FlowKt__ReduceKt$firstOrNull$3) {
            flowKt__ReduceKt$firstOrNull$3 = (FlowKt__ReduceKt$firstOrNull$3) continuation;
            if ((flowKt__ReduceKt$firstOrNull$3.label & Integer.MIN_VALUE) != 0) {
                flowKt__ReduceKt$firstOrNull$3.label -= Integer.MIN_VALUE;
                Object obj = flowKt__ReduceKt$firstOrNull$3.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = flowKt__ReduceKt$firstOrNull$3.label;
                if (i != 0) {
                    ResultKt.throwOnFailure(obj);
                    Ref.ObjectRef objectRef2 = new Ref.ObjectRef();
                    objectRef2.element = null;
                    FlowCollector<? super Object> flowKt__ReduceKt$firstOrNull$$inlined$collectWhile$2 = new FlowKt__ReduceKt$firstOrNull$$inlined$collectWhile$2<>(function2, objectRef2);
                    try {
                        flowKt__ReduceKt$firstOrNull$3.L$0 = flow;
                        flowKt__ReduceKt$firstOrNull$3.L$1 = function2;
                        flowKt__ReduceKt$firstOrNull$3.L$2 = objectRef2;
                        flowKt__ReduceKt$firstOrNull$3.L$3 = flow;
                        flowKt__ReduceKt$firstOrNull$3.L$4 = flowKt__ReduceKt$firstOrNull$$inlined$collectWhile$2;
                        flowKt__ReduceKt$firstOrNull$3.label = 1;
                        if (flow.collect(flowKt__ReduceKt$firstOrNull$$inlined$collectWhile$2, flowKt__ReduceKt$firstOrNull$3) == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        objectRef = objectRef2;
                    } catch (AbortFlowException e2) {
                        objectRef = objectRef2;
                        e = e2;
                        flowCollector = flowKt__ReduceKt$firstOrNull$$inlined$collectWhile$2;
                        FlowExceptions_commonKt.checkOwnership(e, flowCollector);
                        return objectRef.element;
                    }
                } else {
                    if (i != 1) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    flowCollector = (FlowKt__ReduceKt$firstOrNull$$inlined$collectWhile$2) flowKt__ReduceKt$firstOrNull$3.L$4;
                    objectRef = (Ref.ObjectRef) flowKt__ReduceKt$firstOrNull$3.L$2;
                    try {
                        ResultKt.throwOnFailure(obj);
                    } catch (AbortFlowException e3) {
                        e = e3;
                        FlowExceptions_commonKt.checkOwnership(e, flowCollector);
                        return objectRef.element;
                    }
                }
                return objectRef.element;
            }
        }
        flowKt__ReduceKt$firstOrNull$3 = new FlowKt__ReduceKt$firstOrNull$3(continuation);
        Object obj2 = flowKt__ReduceKt$firstOrNull$3.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = flowKt__ReduceKt$firstOrNull$3.label;
        if (i != 0) {
        }
        return objectRef.element;
    }
}
