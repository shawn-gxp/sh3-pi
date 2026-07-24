package kotlin.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.sequences.SequenceScope;

/* JADX INFO: Add missing generic type declarations: [T] */
/* compiled from: SlidingWindow.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0014\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00040\u0003H\u008a@¢\u0006\u0004\b\u0005\u0010\u0006"}, d2 = {"<anonymous>", "", "T", "Lkotlin/sequences/SequenceScope;", "", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 1})
@DebugMetadata(c = "kotlin.collections.SlidingWindowKt$windowedIterator$1", f = "SlidingWindow.kt", i = {0, 0, 0, 2, 2, 3, 3}, l = {34, 40, 49, 55, 58}, m = "invokeSuspend", n = {"$this$iterator", "buffer", "gap", "$this$iterator", "buffer", "$this$iterator", "buffer"}, s = {"L$0", "L$1", "I$0", "L$0", "L$1", "L$0", "L$1"})
/* loaded from: classes.dex */
final class SlidingWindowKt$windowedIterator$1<T> extends RestrictedSuspendLambda implements Function2<SequenceScope<? super List<? extends T>>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Iterator $iterator;
    final /* synthetic */ boolean $partialWindows;
    final /* synthetic */ boolean $reuseBuffer;
    final /* synthetic */ int $size;
    final /* synthetic */ int $step;
    int I$0;
    private /* synthetic */ Object L$0;
    Object L$1;
    Object L$2;
    int label;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    SlidingWindowKt$windowedIterator$1(int i, int i2, Iterator it, boolean z, boolean z2, Continuation continuation) {
        super(2, continuation);
        this.$size = i;
        this.$step = i2;
        this.$iterator = it;
        this.$reuseBuffer = z;
        this.$partialWindows = z2;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        SlidingWindowKt$windowedIterator$1 slidingWindowKt$windowedIterator$1 = new SlidingWindowKt$windowedIterator$1(this.$size, this.$step, this.$iterator, this.$reuseBuffer, this.$partialWindows, completion);
        slidingWindowKt$windowedIterator$1.L$0 = obj;
        return slidingWindowKt$windowedIterator$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(Object obj, Continuation<? super Unit> continuation) {
        return ((SlidingWindowKt$windowedIterator$1) create(obj, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:15:0x013c  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0163  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00ed  */
    /* JADX WARN: Removed duplicated region for block: B:54:0x0132  */
    /* JADX WARN: Removed duplicated region for block: B:59:0x00ac  */
    /* JADX WARN: Removed duplicated region for block: B:64:0x0082  */
    /* JADX WARN: Removed duplicated region for block: B:88:0x00dd A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:89:0x00b0  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:19:0x015a -> B:12:0x015d). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:42:0x0125 -> B:30:0x0128). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:64:0x00a7 -> B:50:0x00a8). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        int i;
        Iterator it;
        SequenceScope sequenceScope;
        SlidingWindowKt$windowedIterator$1<T> slidingWindowKt$windowedIterator$1;
        int i2;
        SequenceScope sequenceScope2;
        SlidingWindowKt$windowedIterator$1<T> slidingWindowKt$windowedIterator$12;
        ArrayList arrayList;
        Iterator it2;
        RingBuffer ringBuffer;
        RingBuffer ringBuffer2;
        SequenceScope sequenceScope3;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i3 = this.label;
        if (i3 == 0) {
            ResultKt.throwOnFailure(obj);
            SequenceScope sequenceScope4 = (SequenceScope) this.L$0;
            int coerceAtMost = RangesKt.coerceAtMost(this.$size, 1024);
            i = this.$step - this.$size;
            if (i >= 0) {
                ArrayList arrayList2 = new ArrayList(coerceAtMost);
                i2 = 0;
                sequenceScope2 = sequenceScope4;
                slidingWindowKt$windowedIterator$12 = this;
                arrayList = arrayList2;
                it2 = this.$iterator;
                while (it2.hasNext()) {
                }
                if (!arrayList.isEmpty()) {
                    slidingWindowKt$windowedIterator$12.L$0 = null;
                    slidingWindowKt$windowedIterator$12.L$1 = null;
                    slidingWindowKt$windowedIterator$12.L$2 = null;
                    slidingWindowKt$windowedIterator$12.label = 2;
                    if (sequenceScope2.yield(arrayList, slidingWindowKt$windowedIterator$12) == coroutine_suspended) {
                    }
                }
                return Unit.INSTANCE;
            }
            RingBuffer ringBuffer3 = new RingBuffer(coerceAtMost);
            it = this.$iterator;
            sequenceScope = sequenceScope4;
            slidingWindowKt$windowedIterator$1 = this;
            ringBuffer = ringBuffer3;
            while (it.hasNext()) {
            }
            if (slidingWindowKt$windowedIterator$1.$partialWindows) {
            }
            return Unit.INSTANCE;
        }
        if (i3 == 1) {
            i2 = this.I$0;
            it2 = (Iterator) this.L$2;
            arrayList = (ArrayList) this.L$1;
            sequenceScope2 = (SequenceScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            slidingWindowKt$windowedIterator$12 = this;
            if (slidingWindowKt$windowedIterator$12.$reuseBuffer) {
                arrayList = new ArrayList(slidingWindowKt$windowedIterator$12.$size);
            } else {
                arrayList.clear();
            }
            i = i2;
            while (it2.hasNext()) {
                Object next = it2.next();
                if (i2 > 0) {
                    i2--;
                } else {
                    arrayList.add(next);
                    if (arrayList.size() == slidingWindowKt$windowedIterator$12.$size) {
                        slidingWindowKt$windowedIterator$12.L$0 = sequenceScope2;
                        slidingWindowKt$windowedIterator$12.L$1 = arrayList;
                        slidingWindowKt$windowedIterator$12.L$2 = it2;
                        slidingWindowKt$windowedIterator$12.I$0 = i;
                        slidingWindowKt$windowedIterator$12.label = 1;
                        if (sequenceScope2.yield(arrayList, slidingWindowKt$windowedIterator$12) == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        i2 = i;
                        if (slidingWindowKt$windowedIterator$12.$reuseBuffer) {
                        }
                        i = i2;
                        while (it2.hasNext()) {
                        }
                    }
                }
            }
            if ((!arrayList.isEmpty()) && (slidingWindowKt$windowedIterator$12.$partialWindows || arrayList.size() == slidingWindowKt$windowedIterator$12.$size)) {
                slidingWindowKt$windowedIterator$12.L$0 = null;
                slidingWindowKt$windowedIterator$12.L$1 = null;
                slidingWindowKt$windowedIterator$12.L$2 = null;
                slidingWindowKt$windowedIterator$12.label = 2;
                if (sequenceScope2.yield(arrayList, slidingWindowKt$windowedIterator$12) == coroutine_suspended) {
                    return coroutine_suspended;
                }
            }
            return Unit.INSTANCE;
        }
        if (i3 != 2) {
            if (i3 == 3) {
                it = (Iterator) this.L$2;
                RingBuffer ringBuffer4 = (RingBuffer) this.L$1;
                sequenceScope = (SequenceScope) this.L$0;
                ResultKt.throwOnFailure(obj);
                slidingWindowKt$windowedIterator$1 = this;
                RingBuffer ringBuffer5 = ringBuffer4;
                ringBuffer5.removeFirst(slidingWindowKt$windowedIterator$1.$step);
                ringBuffer = ringBuffer5;
                while (it.hasNext()) {
                    ringBuffer.add((RingBuffer) it.next());
                    if (ringBuffer.isFull()) {
                        int size = ringBuffer.size();
                        int i4 = slidingWindowKt$windowedIterator$1.$size;
                        if (size >= i4) {
                            List arrayList3 = slidingWindowKt$windowedIterator$1.$reuseBuffer ? ringBuffer : new ArrayList(ringBuffer);
                            slidingWindowKt$windowedIterator$1.L$0 = sequenceScope;
                            slidingWindowKt$windowedIterator$1.L$1 = ringBuffer;
                            slidingWindowKt$windowedIterator$1.L$2 = it;
                            slidingWindowKt$windowedIterator$1.label = 3;
                            ringBuffer5 = ringBuffer;
                            if (sequenceScope.yield(arrayList3, slidingWindowKt$windowedIterator$1) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            ringBuffer5.removeFirst(slidingWindowKt$windowedIterator$1.$step);
                            ringBuffer = ringBuffer5;
                            while (it.hasNext()) {
                            }
                        } else {
                            ringBuffer = ringBuffer.expanded(i4);
                        }
                    }
                }
                if (slidingWindowKt$windowedIterator$1.$partialWindows) {
                    ringBuffer2 = ringBuffer;
                    sequenceScope3 = sequenceScope;
                    if (ringBuffer2.size() <= slidingWindowKt$windowedIterator$1.$step) {
                    }
                }
                return Unit.INSTANCE;
            }
            if (i3 == 4) {
                ringBuffer2 = (RingBuffer) this.L$1;
                sequenceScope3 = (SequenceScope) this.L$0;
                ResultKt.throwOnFailure(obj);
                slidingWindowKt$windowedIterator$1 = this;
                ringBuffer2.removeFirst(slidingWindowKt$windowedIterator$1.$step);
                if (ringBuffer2.size() <= slidingWindowKt$windowedIterator$1.$step) {
                    List arrayList4 = slidingWindowKt$windowedIterator$1.$reuseBuffer ? ringBuffer2 : new ArrayList(ringBuffer2);
                    slidingWindowKt$windowedIterator$1.L$0 = sequenceScope3;
                    slidingWindowKt$windowedIterator$1.L$1 = ringBuffer2;
                    slidingWindowKt$windowedIterator$1.L$2 = null;
                    slidingWindowKt$windowedIterator$1.label = 4;
                    if (sequenceScope3.yield(arrayList4, slidingWindowKt$windowedIterator$1) == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                    ringBuffer2.removeFirst(slidingWindowKt$windowedIterator$1.$step);
                    if (ringBuffer2.size() <= slidingWindowKt$windowedIterator$1.$step) {
                        if (!ringBuffer2.isEmpty()) {
                            slidingWindowKt$windowedIterator$1.L$0 = null;
                            slidingWindowKt$windowedIterator$1.L$1 = null;
                            slidingWindowKt$windowedIterator$1.L$2 = null;
                            slidingWindowKt$windowedIterator$1.label = 5;
                            if (sequenceScope3.yield(ringBuffer2, slidingWindowKt$windowedIterator$1) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        }
                        return Unit.INSTANCE;
                    }
                }
            } else if (i3 != 5) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
        ResultKt.throwOnFailure(obj);
        return Unit.INSTANCE;
    }
}
