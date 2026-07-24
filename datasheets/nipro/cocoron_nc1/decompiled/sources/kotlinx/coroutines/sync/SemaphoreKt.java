package kotlinx.coroutines.sync;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.InlineMarker;
import kotlinx.coroutines.internal.Symbol;
import kotlinx.coroutines.internal.SystemPropsKt__SystemProps_commonKt;

/* compiled from: Semaphore.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u00000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0018\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00072\b\b\u0002\u0010\u0012\u001a\u00020\u0007\u001a\u001a\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0014H\u0002\u001a6\u0010\u0018\u001a\u0002H\u0019\"\u0004\b\u0000\u0010\u0019*\u00020\u00102\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u0002H\u00190\u001bH\u0086Hø\u0001\u0000\u0082\u0002\n\n\b\b\u0001\u0012\u0002\u0010\u0001 \u0001¢\u0006\u0002\u0010\u001c\"\u0016\u0010\u0000\u001a\u00020\u00018\u0002X\u0083\u0004¢\u0006\b\n\u0000\u0012\u0004\b\u0002\u0010\u0003\"\u0016\u0010\u0004\u001a\u00020\u00018\u0002X\u0083\u0004¢\u0006\b\n\u0000\u0012\u0004\b\u0005\u0010\u0003\"\u0016\u0010\u0006\u001a\u00020\u00078\u0002X\u0083\u0004¢\u0006\b\n\u0000\u0012\u0004\b\b\u0010\u0003\"\u0016\u0010\t\u001a\u00020\u00018\u0002X\u0083\u0004¢\u0006\b\n\u0000\u0012\u0004\b\n\u0010\u0003\"\u0016\u0010\u000b\u001a\u00020\u00078\u0002X\u0083\u0004¢\u0006\b\n\u0000\u0012\u0004\b\f\u0010\u0003\"\u0016\u0010\r\u001a\u00020\u00018\u0002X\u0083\u0004¢\u0006\b\n\u0000\u0012\u0004\b\u000e\u0010\u0003\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u001d"}, d2 = {"BROKEN", "Lkotlinx/coroutines/internal/Symbol;", "getBROKEN$annotations", "()V", "CANCELLED", "getCANCELLED$annotations", "MAX_SPIN_CYCLES", "", "getMAX_SPIN_CYCLES$annotations", "PERMIT", "getPERMIT$annotations", "SEGMENT_SIZE", "getSEGMENT_SIZE$annotations", "TAKEN", "getTAKEN$annotations", "Semaphore", "Lkotlinx/coroutines/sync/Semaphore;", "permits", "acquiredPermits", "createSegment", "Lkotlinx/coroutines/sync/SemaphoreSegment;", "id", "", "prev", "withPermit", "T", "action", "Lkotlin/Function0;", "(Lkotlinx/coroutines/sync/Semaphore;Lkotlin/jvm/functions/Function0;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "kotlinx-coroutines-core"}, k = 2, mv = {1, 4, 0})
/* loaded from: classes.dex */
public final class SemaphoreKt {
    private static final Symbol BROKEN;
    private static final Symbol CANCELLED;
    private static final int MAX_SPIN_CYCLES;
    private static final Symbol PERMIT;
    private static final int SEGMENT_SIZE;
    private static final Symbol TAKEN;

    private static /* synthetic */ void getBROKEN$annotations() {
    }

    private static /* synthetic */ void getCANCELLED$annotations() {
    }

    private static /* synthetic */ void getMAX_SPIN_CYCLES$annotations() {
    }

    private static /* synthetic */ void getPERMIT$annotations() {
    }

    private static /* synthetic */ void getSEGMENT_SIZE$annotations() {
    }

    private static /* synthetic */ void getTAKEN$annotations() {
    }

    public static final Semaphore Semaphore(int i, int i2) {
        return new SemaphoreImpl(i, i2);
    }

    public static /* synthetic */ Semaphore Semaphore$default(int i, int i2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = 0;
        }
        return Semaphore(i, i2);
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x003b  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0024  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static final <T> Object withPermit(Semaphore semaphore, Function0<? extends T> function0, Continuation<? super T> continuation) {
        SemaphoreKt$withPermit$1 semaphoreKt$withPermit$1;
        int i;
        try {
            if (continuation instanceof SemaphoreKt$withPermit$1) {
                semaphoreKt$withPermit$1 = (SemaphoreKt$withPermit$1) continuation;
                if ((semaphoreKt$withPermit$1.label & Integer.MIN_VALUE) != 0) {
                    semaphoreKt$withPermit$1.label -= Integer.MIN_VALUE;
                    Object obj = semaphoreKt$withPermit$1.result;
                    Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                    i = semaphoreKt$withPermit$1.label;
                    if (i != 0) {
                        ResultKt.throwOnFailure(obj);
                        semaphoreKt$withPermit$1.L$0 = semaphore;
                        semaphoreKt$withPermit$1.L$1 = function0;
                        semaphoreKt$withPermit$1.label = 1;
                        if (semaphore.acquire(semaphoreKt$withPermit$1) == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                    } else {
                        if (i != 1) {
                            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                        }
                        function0 = (Function0) semaphoreKt$withPermit$1.L$1;
                        semaphore = (Semaphore) semaphoreKt$withPermit$1.L$0;
                        ResultKt.throwOnFailure(obj);
                    }
                    return function0.invoke();
                }
            }
            return function0.invoke();
        } finally {
            InlineMarker.finallyStart(1);
            semaphore.release();
            InlineMarker.finallyEnd(1);
        }
        semaphoreKt$withPermit$1 = new SemaphoreKt$withPermit$1(continuation);
        Object obj2 = semaphoreKt$withPermit$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = semaphoreKt$withPermit$1.label;
        if (i != 0) {
        }
    }

    private static final Object withPermit$$forInline(Semaphore semaphore, Function0 function0, Continuation continuation) {
        InlineMarker.mark(0);
        semaphore.acquire(continuation);
        InlineMarker.mark(2);
        InlineMarker.mark(1);
        try {
            return function0.invoke();
        } finally {
            InlineMarker.finallyStart(1);
            semaphore.release();
            InlineMarker.finallyEnd(1);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final SemaphoreSegment createSegment(long j, SemaphoreSegment semaphoreSegment) {
        return new SemaphoreSegment(j, semaphoreSegment, 0);
    }

    static {
        int systemProp$default;
        int systemProp$default2;
        systemProp$default = SystemPropsKt__SystemProps_commonKt.systemProp$default("kotlinx.coroutines.semaphore.maxSpinCycles", 100, 0, 0, 12, (Object) null);
        MAX_SPIN_CYCLES = systemProp$default;
        PERMIT = new Symbol("PERMIT");
        TAKEN = new Symbol("TAKEN");
        BROKEN = new Symbol("BROKEN");
        CANCELLED = new Symbol("CANCELLED");
        systemProp$default2 = SystemPropsKt__SystemProps_commonKt.systemProp$default("kotlinx.coroutines.semaphore.segmentSize", 16, 0, 0, 12, (Object) null);
        SEGMENT_SIZE = systemProp$default2;
    }
}
