package kotlinx.coroutines.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CancellableContinuationImpl;
import kotlinx.coroutines.CancellableContinuationKt;
import kotlinx.coroutines.DebugKt;
import kotlinx.coroutines.DisposableHandle;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.JobKt;
import kotlinx.coroutines.channels.BufferOverflow;
import kotlinx.coroutines.flow.internal.AbstractSharedFlow;
import kotlinx.coroutines.flow.internal.AbstractSharedFlowKt;
import kotlinx.coroutines.flow.internal.AbstractSharedFlowSlot;
import kotlinx.coroutines.flow.internal.FusibleFlow;

/* compiled from: SharedFlow.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000~\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\n\n\u0002\u0010 \n\u0002\b\t\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0012\b\u0002\u0018\u0000*\u0004\b\u0000\u0010\u00012\b\u0012\u0004\u0012\u00020\u00030\u00022\b\u0012\u0004\u0012\u0002H\u00010\u00042\b\u0012\u0004\u0012\u0002H\u00010\u00052\b\u0012\u0004\u0012\u0002H\u00010\u0006:\u0001bB\u001d\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\b\u0012\u0006\u0010\n\u001a\u00020\u000b¢\u0006\u0002\u0010\fJ\u0019\u0010&\u001a\u00020'2\u0006\u0010(\u001a\u00020\u0003H\u0082@ø\u0001\u0000¢\u0006\u0002\u0010)J\u0010\u0010*\u001a\u00020'2\u0006\u0010+\u001a\u00020,H\u0002J\b\u0010-\u001a\u00020'H\u0002J\u001f\u0010.\u001a\u00020'2\f\u0010/\u001a\b\u0012\u0004\u0012\u00028\u000000H\u0096@ø\u0001\u0000¢\u0006\u0002\u00101J\u0010\u00102\u001a\u00020'2\u0006\u00103\u001a\u00020\u0012H\u0002J\b\u00104\u001a\u00020\u0003H\u0014J\u001d\u00105\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00030\u000e2\u0006\u00106\u001a\u00020\bH\u0014¢\u0006\u0002\u00107J\b\u00108\u001a\u00020'H\u0002J\u0019\u00109\u001a\u00020'2\u0006\u0010:\u001a\u00028\u0000H\u0096@ø\u0001\u0000¢\u0006\u0002\u0010;J\u0019\u0010<\u001a\u00020'2\u0006\u0010:\u001a\u00028\u0000H\u0082@ø\u0001\u0000¢\u0006\u0002\u0010;J\u0012\u0010=\u001a\u00020'2\b\u0010>\u001a\u0004\u0018\u00010\u000fH\u0002J1\u0010?\u001a\u0010\u0012\f\u0012\n\u0012\u0004\u0012\u00020'\u0018\u00010@0\u000e2\u0014\u0010A\u001a\u0010\u0012\f\u0012\n\u0012\u0004\u0012\u00020'\u0018\u00010@0\u000eH\u0002¢\u0006\u0002\u0010BJ&\u0010C\u001a\b\u0012\u0004\u0012\u00028\u00000D2\u0006\u0010E\u001a\u00020F2\u0006\u0010G\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u000bH\u0016J\u0012\u0010H\u001a\u0004\u0018\u00010\u000f2\u0006\u0010I\u001a\u00020\u0012H\u0002J7\u0010J\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u000e2\u0010\u0010K\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u000f\u0018\u00010\u000e2\u0006\u0010L\u001a\u00020\b2\u0006\u0010M\u001a\u00020\bH\u0002¢\u0006\u0002\u0010NJ\b\u0010O\u001a\u00020'H\u0016J\u0015\u0010P\u001a\u00020Q2\u0006\u0010:\u001a\u00028\u0000H\u0016¢\u0006\u0002\u0010RJ\u0015\u0010S\u001a\u00020Q2\u0006\u0010:\u001a\u00028\u0000H\u0002¢\u0006\u0002\u0010RJ\u0015\u0010T\u001a\u00020Q2\u0006\u0010:\u001a\u00028\u0000H\u0002¢\u0006\u0002\u0010RJ\u0010\u0010U\u001a\u00020\u00122\u0006\u0010(\u001a\u00020\u0003H\u0002J\u0012\u0010V\u001a\u0004\u0018\u00010\u000f2\u0006\u0010(\u001a\u00020\u0003H\u0002J(\u0010W\u001a\u00020'2\u0006\u0010X\u001a\u00020\u00122\u0006\u0010Y\u001a\u00020\u00122\u0006\u0010Z\u001a\u00020\u00122\u0006\u0010[\u001a\u00020\u0012H\u0002J%\u0010\\\u001a\u0010\u0012\f\u0012\n\u0012\u0004\u0012\u00020'\u0018\u00010@0\u000e2\u0006\u0010]\u001a\u00020\u0012H\u0000¢\u0006\u0004\b^\u0010_J\r\u0010`\u001a\u00020\u0012H\u0000¢\u0006\u0002\baR\u001a\u0010\r\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u000f\u0018\u00010\u000eX\u0082\u000e¢\u0006\u0004\n\u0002\u0010\u0010R\u000e\u0010\t\u001a\u00020\bX\u0082\u0004¢\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\u00020\u00128BX\u0082\u0004¢\u0006\u0006\u001a\u0004\b\u0013\u0010\u0014R\u000e\u0010\u0015\u001a\u00020\bX\u0082\u000e¢\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\u00020\u00128BX\u0082\u0004¢\u0006\u0006\u001a\u0004\b\u0017\u0010\u0014R\u000e\u0010\u0018\u001a\u00020\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000R\u0014\u0010\u0019\u001a\u00020\u00128BX\u0082\u0004¢\u0006\u0006\u001a\u0004\b\u001a\u0010\u0014R\u000e\u0010\u001b\u001a\u00020\bX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010\u001c\u001a\b\u0012\u0004\u0012\u00028\u00000\u001d8VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u001e\u0010\u001fR\u000e\u0010 \u001a\u00020\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u0014\u0010!\u001a\u00020\b8BX\u0082\u0004¢\u0006\u0006\u001a\u0004\b\"\u0010#R\u0014\u0010$\u001a\u00020\b8BX\u0082\u0004¢\u0006\u0006\u001a\u0004\b%\u0010#\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006c"}, d2 = {"Lkotlinx/coroutines/flow/SharedFlowImpl;", "T", "Lkotlinx/coroutines/flow/internal/AbstractSharedFlow;", "Lkotlinx/coroutines/flow/SharedFlowSlot;", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lkotlinx/coroutines/flow/CancellableFlow;", "Lkotlinx/coroutines/flow/internal/FusibleFlow;", "replay", "", "bufferCapacity", "onBufferOverflow", "Lkotlinx/coroutines/channels/BufferOverflow;", "(IILkotlinx/coroutines/channels/BufferOverflow;)V", "buffer", "", "", "[Ljava/lang/Object;", "bufferEndIndex", "", "getBufferEndIndex", "()J", "bufferSize", "head", "getHead", "minCollectorIndex", "queueEndIndex", "getQueueEndIndex", "queueSize", "replayCache", "", "getReplayCache", "()Ljava/util/List;", "replayIndex", "replaySize", "getReplaySize", "()I", "totalSize", "getTotalSize", "awaitValue", "", "slot", "(Lkotlinx/coroutines/flow/SharedFlowSlot;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "cancelEmitter", "emitter", "Lkotlinx/coroutines/flow/SharedFlowImpl$Emitter;", "cleanupTailLocked", "collect", "collector", "Lkotlinx/coroutines/flow/FlowCollector;", "(Lkotlinx/coroutines/flow/FlowCollector;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "correctCollectorIndexesOnDropOldest", "newHead", "createSlot", "createSlotArray", "size", "(I)[Lkotlinx/coroutines/flow/SharedFlowSlot;", "dropOldestLocked", "emit", "value", "(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "emitSuspend", "enqueueLocked", "item", "findSlotsToResumeLocked", "Lkotlin/coroutines/Continuation;", "resumesIn", "([Lkotlin/coroutines/Continuation;)[Lkotlin/coroutines/Continuation;", "fuse", "Lkotlinx/coroutines/flow/Flow;", "context", "Lkotlin/coroutines/CoroutineContext;", "capacity", "getPeekedValueLockedAt", "index", "growBuffer", "curBuffer", "curSize", "newSize", "([Ljava/lang/Object;II)[Ljava/lang/Object;", "resetReplayCache", "tryEmit", "", "(Ljava/lang/Object;)Z", "tryEmitLocked", "tryEmitNoCollectorsLocked", "tryPeekLocked", "tryTakeValue", "updateBufferLocked", "newReplayIndex", "newMinCollectorIndex", "newBufferEndIndex", "newQueueEndIndex", "updateCollectorIndexLocked", "oldIndex", "updateCollectorIndexLocked$kotlinx_coroutines_core", "(J)[Lkotlin/coroutines/Continuation;", "updateNewCollectorIndexLocked", "updateNewCollectorIndexLocked$kotlinx_coroutines_core", "Emitter", "kotlinx-coroutines-core"}, k = 1, mv = {1, 4, 0})
/* loaded from: classes.dex */
final class SharedFlowImpl<T> extends AbstractSharedFlow<SharedFlowSlot> implements MutableSharedFlow<T>, CancellableFlow<T>, FusibleFlow<T> {
    private Object[] buffer;
    private final int bufferCapacity;
    private int bufferSize;
    private long minCollectorIndex;
    private final BufferOverflow onBufferOverflow;
    private int queueSize;
    private final int replay;
    private long replayIndex;

    @Metadata(bv = {1, 0, 3}, k = 3, mv = {1, 4, 0})
    public final /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[BufferOverflow.values().length];
            $EnumSwitchMapping$0 = iArr;
            iArr[BufferOverflow.SUSPEND.ordinal()] = 1;
            iArr[BufferOverflow.DROP_LATEST.ordinal()] = 2;
            iArr[BufferOverflow.DROP_OLDEST.ordinal()] = 3;
        }
    }

    public SharedFlowImpl(int i, int i2, BufferOverflow bufferOverflow) {
        this.replay = i;
        this.bufferCapacity = i2;
        this.onBufferOverflow = bufferOverflow;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final long getHead() {
        return Math.min(this.minCollectorIndex, this.replayIndex);
    }

    private final int getReplaySize() {
        return (int) ((getHead() + this.bufferSize) - this.replayIndex);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final int getTotalSize() {
        return this.bufferSize + this.queueSize;
    }

    private final long getBufferEndIndex() {
        return getHead() + this.bufferSize;
    }

    private final long getQueueEndIndex() {
        return getHead() + this.bufferSize + this.queueSize;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:19:0x00c7 A[Catch: all -> 0x0074, TRY_LEAVE, TryCatch #0 {all -> 0x0074, blocks: (B:13:0x003e, B:17:0x00a7, B:27:0x00b1, B:28:0x00b4, B:19:0x00c7, B:35:0x005e, B:37:0x0070, B:38:0x0097), top: B:7:0x0024 }] */
    /* JADX WARN: Removed duplicated region for block: B:25:0x00af A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:39:0x0076  */
    /* JADX WARN: Removed duplicated region for block: B:9:0x0026  */
    /* JADX WARN: Type inference failed for: r2v0, types: [int] */
    /* JADX WARN: Type inference failed for: r2v1 */
    /* JADX WARN: Type inference failed for: r2v2 */
    /* JADX WARN: Type inference failed for: r2v20 */
    /* JADX WARN: Type inference failed for: r2v21 */
    /* JADX WARN: Type inference failed for: r2v5, types: [java.lang.Object, kotlinx.coroutines.flow.SharedFlowSlot] */
    /* JADX WARN: Type inference failed for: r2v7, types: [java.lang.Object, kotlinx.coroutines.flow.SharedFlowSlot] */
    /* JADX WARN: Type inference failed for: r5v13 */
    /* JADX WARN: Type inference failed for: r5v3, types: [java.lang.Object, kotlinx.coroutines.flow.FlowCollector] */
    /* JADX WARN: Type inference failed for: r5v4 */
    /* JADX WARN: Type inference failed for: r6v1, types: [kotlinx.coroutines.flow.SharedFlowImpl] */
    /* JADX WARN: Type inference failed for: r6v18 */
    /* JADX WARN: Type inference failed for: r6v19 */
    /* JADX WARN: Type inference failed for: r6v2 */
    /* JADX WARN: Type inference failed for: r6v5, types: [java.lang.Object, kotlinx.coroutines.flow.SharedFlowImpl] */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:21:0x00c4 -> B:14:0x0041). Please report as a decompilation issue!!! */
    @Override // kotlinx.coroutines.flow.Flow
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object collect(FlowCollector<? super T> flowCollector, Continuation<? super Unit> continuation) {
        SharedFlowImpl$collect$1 sharedFlowImpl$collect$1;
        Object coroutine_suspended;
        SharedFlowSlot sharedFlowSlot;
        ?? r6;
        SharedFlowImpl<T> sharedFlowImpl;
        SharedFlowSlot sharedFlowSlot2;
        Job job;
        Job job2;
        ?? r5;
        Object tryTakeValue;
        SharedFlowImpl<T> sharedFlowImpl2;
        SharedFlowSlot sharedFlowSlot3;
        try {
            if (continuation instanceof SharedFlowImpl$collect$1) {
                sharedFlowImpl$collect$1 = (SharedFlowImpl$collect$1) continuation;
                if ((sharedFlowImpl$collect$1.label & Integer.MIN_VALUE) != 0) {
                    sharedFlowImpl$collect$1.label -= Integer.MIN_VALUE;
                    Object obj = sharedFlowImpl$collect$1.result;
                    coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                    sharedFlowSlot = sharedFlowImpl$collect$1.label;
                    if (sharedFlowSlot != 0) {
                        ResultKt.throwOnFailure(obj);
                        sharedFlowSlot = allocateSlot();
                        try {
                            if (flowCollector instanceof SubscribedFlowCollector) {
                                sharedFlowImpl$collect$1.L$0 = this;
                                sharedFlowImpl$collect$1.L$1 = flowCollector;
                                sharedFlowImpl$collect$1.L$2 = sharedFlowSlot;
                                sharedFlowImpl$collect$1.label = 1;
                                if (((SubscribedFlowCollector) flowCollector).onSubscription(sharedFlowImpl$collect$1) == coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                            }
                            sharedFlowImpl = this;
                            sharedFlowSlot2 = sharedFlowSlot;
                        } catch (Throwable th) {
                            th = th;
                            r6 = this;
                            r6.freeSlot((AbstractSharedFlowSlot) sharedFlowSlot);
                            throw th;
                        }
                    } else {
                        if (sharedFlowSlot != 1) {
                            if (sharedFlowSlot == 2) {
                                Object obj2 = sharedFlowImpl$collect$1.L$4;
                                job2 = (Job) sharedFlowImpl$collect$1.L$3;
                                SharedFlowSlot sharedFlowSlot4 = (SharedFlowSlot) sharedFlowImpl$collect$1.L$2;
                                FlowCollector flowCollector2 = (FlowCollector) sharedFlowImpl$collect$1.L$1;
                                SharedFlowImpl sharedFlowImpl3 = (SharedFlowImpl) sharedFlowImpl$collect$1.L$0;
                                ResultKt.throwOnFailure(obj);
                                sharedFlowSlot = sharedFlowSlot4;
                                r5 = flowCollector2;
                                r6 = sharedFlowImpl3;
                                do {
                                    tryTakeValue = r6.tryTakeValue(sharedFlowSlot);
                                    if (tryTakeValue == SharedFlowKt.NO_VALUE) {
                                    }
                                } while (r6.awaitValue(sharedFlowSlot, sharedFlowImpl$collect$1) != coroutine_suspended);
                                return coroutine_suspended;
                            }
                            if (sharedFlowSlot != 3) {
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }
                            Object obj3 = sharedFlowImpl$collect$1.L$4;
                            job2 = (Job) sharedFlowImpl$collect$1.L$3;
                            SharedFlowSlot sharedFlowSlot5 = (SharedFlowSlot) sharedFlowImpl$collect$1.L$2;
                            FlowCollector<? super T> flowCollector3 = (FlowCollector) sharedFlowImpl$collect$1.L$1;
                            SharedFlowImpl<T> sharedFlowImpl4 = (SharedFlowImpl) sharedFlowImpl$collect$1.L$0;
                            ResultKt.throwOnFailure(obj);
                            SharedFlowSlot sharedFlowSlot6 = sharedFlowSlot5;
                            FlowCollector<? super T> flowCollector4 = flowCollector3;
                            SharedFlowImpl<T> sharedFlowImpl5 = sharedFlowImpl4;
                            job = job2;
                            flowCollector = flowCollector4;
                            sharedFlowSlot3 = sharedFlowSlot6;
                            sharedFlowImpl2 = sharedFlowImpl5;
                            r5 = flowCollector;
                            job2 = job;
                            sharedFlowSlot = sharedFlowSlot3;
                            r6 = sharedFlowImpl2;
                            do {
                                tryTakeValue = r6.tryTakeValue(sharedFlowSlot);
                                if (tryTakeValue == SharedFlowKt.NO_VALUE) {
                                    sharedFlowImpl$collect$1.L$0 = r6;
                                    sharedFlowImpl$collect$1.L$1 = r5;
                                    sharedFlowImpl$collect$1.L$2 = sharedFlowSlot;
                                    sharedFlowImpl$collect$1.L$3 = job2;
                                    sharedFlowImpl$collect$1.L$4 = tryTakeValue;
                                    sharedFlowImpl$collect$1.label = 2;
                                } else {
                                    if (job2 != null) {
                                        JobKt.ensureActive(job2);
                                    }
                                    sharedFlowImpl$collect$1.L$0 = r6;
                                    sharedFlowImpl$collect$1.L$1 = r5;
                                    sharedFlowImpl$collect$1.L$2 = sharedFlowSlot;
                                    sharedFlowImpl$collect$1.L$3 = job2;
                                    sharedFlowImpl$collect$1.L$4 = tryTakeValue;
                                    sharedFlowImpl$collect$1.label = 3;
                                    Object emit = r5.emit(tryTakeValue, sharedFlowImpl$collect$1);
                                    sharedFlowSlot6 = sharedFlowSlot;
                                    flowCollector4 = r5;
                                    sharedFlowImpl5 = r6;
                                    if (emit == coroutine_suspended) {
                                        return coroutine_suspended;
                                    }
                                    job = job2;
                                    flowCollector = flowCollector4;
                                    sharedFlowSlot3 = sharedFlowSlot6;
                                    sharedFlowImpl2 = sharedFlowImpl5;
                                    r5 = flowCollector;
                                    job2 = job;
                                    sharedFlowSlot = sharedFlowSlot3;
                                    r6 = sharedFlowImpl2;
                                    tryTakeValue = r6.tryTakeValue(sharedFlowSlot);
                                    if (tryTakeValue == SharedFlowKt.NO_VALUE) {
                                    }
                                }
                            } while (r6.awaitValue(sharedFlowSlot, sharedFlowImpl$collect$1) != coroutine_suspended);
                            return coroutine_suspended;
                        }
                        SharedFlowSlot sharedFlowSlot7 = (SharedFlowSlot) sharedFlowImpl$collect$1.L$2;
                        flowCollector = (FlowCollector) sharedFlowImpl$collect$1.L$1;
                        SharedFlowImpl<T> sharedFlowImpl6 = (SharedFlowImpl) sharedFlowImpl$collect$1.L$0;
                        ResultKt.throwOnFailure(obj);
                        sharedFlowSlot2 = sharedFlowSlot7;
                        sharedFlowImpl = sharedFlowImpl6;
                    }
                    job = (Job) sharedFlowImpl$collect$1.getContext().get(Job.INSTANCE);
                    sharedFlowSlot3 = sharedFlowSlot2;
                    sharedFlowImpl2 = sharedFlowImpl;
                    r5 = flowCollector;
                    job2 = job;
                    sharedFlowSlot = sharedFlowSlot3;
                    r6 = sharedFlowImpl2;
                    do {
                        tryTakeValue = r6.tryTakeValue(sharedFlowSlot);
                        if (tryTakeValue == SharedFlowKt.NO_VALUE) {
                        }
                    } while (r6.awaitValue(sharedFlowSlot, sharedFlowImpl$collect$1) != coroutine_suspended);
                    return coroutine_suspended;
                }
            }
            if (sharedFlowSlot != 0) {
            }
            job = (Job) sharedFlowImpl$collect$1.getContext().get(Job.INSTANCE);
            sharedFlowSlot3 = sharedFlowSlot2;
            sharedFlowImpl2 = sharedFlowImpl;
            r5 = flowCollector;
            job2 = job;
            sharedFlowSlot = sharedFlowSlot3;
            r6 = sharedFlowImpl2;
            do {
                tryTakeValue = r6.tryTakeValue(sharedFlowSlot);
                if (tryTakeValue == SharedFlowKt.NO_VALUE) {
                }
            } while (r6.awaitValue(sharedFlowSlot, sharedFlowImpl$collect$1) != coroutine_suspended);
            return coroutine_suspended;
        } catch (Throwable th2) {
            th = th2;
        }
        sharedFlowImpl$collect$1 = new SharedFlowImpl$collect$1(this, continuation);
        Object obj4 = sharedFlowImpl$collect$1.result;
        coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        sharedFlowSlot = sharedFlowImpl$collect$1.label;
    }

    @Override // kotlinx.coroutines.flow.MutableSharedFlow
    public boolean tryEmit(T value) {
        int i;
        boolean z;
        Continuation<Unit>[] continuationArr = AbstractSharedFlowKt.EMPTY_RESUMES;
        synchronized (this) {
            if (tryEmitLocked(value)) {
                continuationArr = findSlotsToResumeLocked(continuationArr);
                z = true;
            } else {
                z = false;
            }
        }
        for (Continuation<Unit> continuation : continuationArr) {
            if (continuation != null) {
                Unit unit = Unit.INSTANCE;
                Result.Companion companion = Result.INSTANCE;
                continuation.resumeWith(Result.m15constructorimpl(unit));
            }
        }
        return z;
    }

    @Override // kotlinx.coroutines.flow.FlowCollector
    public Object emit(T t, Continuation<? super Unit> continuation) {
        if (tryEmit(t)) {
            return Unit.INSTANCE;
        }
        Object emitSuspend = emitSuspend(t, continuation);
        return emitSuspend == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? emitSuspend : Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean tryEmitLocked(T value) {
        if (getNCollectors() == 0) {
            return tryEmitNoCollectorsLocked(value);
        }
        if (this.bufferSize >= this.bufferCapacity && this.minCollectorIndex <= this.replayIndex) {
            int i = WhenMappings.$EnumSwitchMapping$0[this.onBufferOverflow.ordinal()];
            if (i == 1) {
                return false;
            }
            if (i == 2) {
                return true;
            }
        }
        enqueueLocked(value);
        int i2 = this.bufferSize + 1;
        this.bufferSize = i2;
        if (i2 > this.bufferCapacity) {
            dropOldestLocked();
        }
        if (getReplaySize() > this.replay) {
            updateBufferLocked(this.replayIndex + 1, this.minCollectorIndex, getBufferEndIndex(), getQueueEndIndex());
        }
        return true;
    }

    private final boolean tryEmitNoCollectorsLocked(T value) {
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(getNCollectors() == 0)) {
                throw new AssertionError();
            }
        }
        if (this.replay == 0) {
            return true;
        }
        enqueueLocked(value);
        int i = this.bufferSize + 1;
        this.bufferSize = i;
        if (i > this.replay) {
            dropOldestLocked();
        }
        this.minCollectorIndex = getHead() + this.bufferSize;
        return true;
    }

    private final void dropOldestLocked() {
        Object[] objArr = this.buffer;
        Intrinsics.checkNotNull(objArr);
        SharedFlowKt.setBufferAt(objArr, getHead(), null);
        this.bufferSize--;
        long head = getHead() + 1;
        if (this.replayIndex < head) {
            this.replayIndex = head;
        }
        if (this.minCollectorIndex < head) {
            correctCollectorIndexesOnDropOldest(head);
        }
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(getHead() == head)) {
                throw new AssertionError();
            }
        }
    }

    private final void correctCollectorIndexesOnDropOldest(long newHead) {
        AbstractSharedFlowSlot[] abstractSharedFlowSlotArr;
        SharedFlowImpl<T> sharedFlowImpl = this;
        if (((AbstractSharedFlow) sharedFlowImpl).nCollectors != 0 && (abstractSharedFlowSlotArr = ((AbstractSharedFlow) sharedFlowImpl).slots) != null) {
            for (AbstractSharedFlowSlot abstractSharedFlowSlot : abstractSharedFlowSlotArr) {
                if (abstractSharedFlowSlot != null) {
                    SharedFlowSlot sharedFlowSlot = (SharedFlowSlot) abstractSharedFlowSlot;
                    if (sharedFlowSlot.index >= 0 && sharedFlowSlot.index < newHead) {
                        sharedFlowSlot.index = newHead;
                    }
                }
            }
        }
        this.minCollectorIndex = newHead;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void enqueueLocked(Object item) {
        int totalSize = getTotalSize();
        Object[] objArr = this.buffer;
        if (objArr == null) {
            objArr = growBuffer(null, 0, 2);
        } else if (totalSize >= objArr.length) {
            objArr = growBuffer(objArr, totalSize, objArr.length * 2);
        }
        SharedFlowKt.setBufferAt(objArr, getHead() + totalSize, item);
    }

    private final Object[] growBuffer(Object[] curBuffer, int curSize, int newSize) {
        Object bufferAt;
        if (!(newSize > 0)) {
            throw new IllegalStateException("Buffer size overflow".toString());
        }
        Object[] objArr = new Object[newSize];
        this.buffer = objArr;
        if (curBuffer == null) {
            return objArr;
        }
        long head = getHead();
        for (int i = 0; i < curSize; i++) {
            long j = i + head;
            bufferAt = SharedFlowKt.getBufferAt(curBuffer, j);
            SharedFlowKt.setBufferAt(objArr, j, bufferAt);
        }
        return objArr;
    }

    public final long updateNewCollectorIndexLocked$kotlinx_coroutines_core() {
        long j = this.replayIndex;
        if (j < this.minCollectorIndex) {
            this.minCollectorIndex = j;
        }
        return j;
    }

    public final Continuation<Unit>[] updateCollectorIndexLocked$kotlinx_coroutines_core(long oldIndex) {
        int i;
        long j;
        Object bufferAt;
        Object bufferAt2;
        long j2;
        AbstractSharedFlowSlot[] abstractSharedFlowSlotArr;
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(oldIndex >= this.minCollectorIndex)) {
                throw new AssertionError();
            }
        }
        if (oldIndex > this.minCollectorIndex) {
            return AbstractSharedFlowKt.EMPTY_RESUMES;
        }
        long head = getHead();
        long j3 = this.bufferSize + head;
        if (this.bufferCapacity == 0 && this.queueSize > 0) {
            j3++;
        }
        SharedFlowImpl<T> sharedFlowImpl = this;
        if (((AbstractSharedFlow) sharedFlowImpl).nCollectors != 0 && (abstractSharedFlowSlotArr = ((AbstractSharedFlow) sharedFlowImpl).slots) != null) {
            for (AbstractSharedFlowSlot abstractSharedFlowSlot : abstractSharedFlowSlotArr) {
                if (abstractSharedFlowSlot != null) {
                    SharedFlowSlot sharedFlowSlot = (SharedFlowSlot) abstractSharedFlowSlot;
                    if (sharedFlowSlot.index >= 0 && sharedFlowSlot.index < j3) {
                        j3 = sharedFlowSlot.index;
                    }
                }
            }
        }
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(j3 >= this.minCollectorIndex)) {
                throw new AssertionError();
            }
        }
        if (j3 <= this.minCollectorIndex) {
            return AbstractSharedFlowKt.EMPTY_RESUMES;
        }
        long bufferEndIndex = getBufferEndIndex();
        if (getNCollectors() > 0) {
            i = Math.min(this.queueSize, this.bufferCapacity - ((int) (bufferEndIndex - j3)));
        } else {
            i = this.queueSize;
        }
        Continuation<Unit>[] continuationArr = AbstractSharedFlowKt.EMPTY_RESUMES;
        long j4 = this.queueSize + bufferEndIndex;
        if (i > 0) {
            continuationArr = new Continuation[i];
            Object[] objArr = this.buffer;
            Intrinsics.checkNotNull(objArr);
            long j5 = bufferEndIndex;
            int i2 = 0;
            while (true) {
                if (bufferEndIndex >= j4) {
                    j = j3;
                    break;
                }
                bufferAt2 = SharedFlowKt.getBufferAt(objArr, bufferEndIndex);
                if (bufferAt2 != SharedFlowKt.NO_VALUE) {
                    Objects.requireNonNull(bufferAt2, "null cannot be cast to non-null type kotlinx.coroutines.flow.SharedFlowImpl.Emitter");
                    Emitter emitter = (Emitter) bufferAt2;
                    int i3 = i2 + 1;
                    j = j3;
                    continuationArr[i2] = emitter.cont;
                    SharedFlowKt.setBufferAt(objArr, bufferEndIndex, SharedFlowKt.NO_VALUE);
                    SharedFlowKt.setBufferAt(objArr, j5, emitter.value);
                    j2 = 1;
                    j5++;
                    if (i3 >= i) {
                        break;
                    }
                    i2 = i3;
                } else {
                    j = j3;
                    j2 = 1;
                }
                bufferEndIndex += j2;
                j3 = j;
            }
            bufferEndIndex = j5;
        } else {
            j = j3;
        }
        int i4 = (int) (bufferEndIndex - head);
        long j6 = getNCollectors() == 0 ? bufferEndIndex : j;
        long max = Math.max(this.replayIndex, bufferEndIndex - Math.min(this.replay, i4));
        if (this.bufferCapacity == 0 && max < j4) {
            Object[] objArr2 = this.buffer;
            Intrinsics.checkNotNull(objArr2);
            bufferAt = SharedFlowKt.getBufferAt(objArr2, max);
            if (Intrinsics.areEqual(bufferAt, SharedFlowKt.NO_VALUE)) {
                bufferEndIndex++;
                max++;
            }
        }
        updateBufferLocked(max, j6, bufferEndIndex, j4);
        cleanupTailLocked();
        return true ^ (continuationArr.length == 0) ? findSlotsToResumeLocked(continuationArr) : continuationArr;
    }

    private final void updateBufferLocked(long newReplayIndex, long newMinCollectorIndex, long newBufferEndIndex, long newQueueEndIndex) {
        long min = Math.min(newMinCollectorIndex, newReplayIndex);
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(min >= getHead())) {
                throw new AssertionError();
            }
        }
        for (long head = getHead(); head < min; head++) {
            Object[] objArr = this.buffer;
            Intrinsics.checkNotNull(objArr);
            SharedFlowKt.setBufferAt(objArr, head, null);
        }
        this.replayIndex = newReplayIndex;
        this.minCollectorIndex = newMinCollectorIndex;
        this.bufferSize = (int) (newBufferEndIndex - min);
        this.queueSize = (int) (newQueueEndIndex - newBufferEndIndex);
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(this.bufferSize >= 0)) {
                throw new AssertionError();
            }
        }
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(this.queueSize >= 0)) {
                throw new AssertionError();
            }
        }
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(this.replayIndex <= getHead() + ((long) this.bufferSize))) {
                throw new AssertionError();
            }
        }
    }

    private final void cleanupTailLocked() {
        Object bufferAt;
        if (this.bufferCapacity != 0 || this.queueSize > 1) {
            Object[] objArr = this.buffer;
            Intrinsics.checkNotNull(objArr);
            while (this.queueSize > 0) {
                bufferAt = SharedFlowKt.getBufferAt(objArr, (getHead() + getTotalSize()) - 1);
                if (bufferAt != SharedFlowKt.NO_VALUE) {
                    return;
                }
                this.queueSize--;
                SharedFlowKt.setBufferAt(objArr, getHead() + getTotalSize(), null);
            }
        }
    }

    private final Object tryTakeValue(SharedFlowSlot slot) {
        Object obj;
        Continuation<Unit>[] continuationArr = AbstractSharedFlowKt.EMPTY_RESUMES;
        synchronized (this) {
            long tryPeekLocked = tryPeekLocked(slot);
            if (tryPeekLocked < 0) {
                obj = SharedFlowKt.NO_VALUE;
            } else {
                long j = slot.index;
                Object peekedValueLockedAt = getPeekedValueLockedAt(tryPeekLocked);
                slot.index = tryPeekLocked + 1;
                continuationArr = updateCollectorIndexLocked$kotlinx_coroutines_core(j);
                obj = peekedValueLockedAt;
            }
        }
        for (Continuation<Unit> continuation : continuationArr) {
            if (continuation != null) {
                Unit unit = Unit.INSTANCE;
                Result.Companion companion = Result.INSTANCE;
                continuation.resumeWith(Result.m15constructorimpl(unit));
            }
        }
        return obj;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final long tryPeekLocked(SharedFlowSlot slot) {
        long j = slot.index;
        if (j < getBufferEndIndex()) {
            return j;
        }
        if (this.bufferCapacity <= 0 && j <= getHead() && this.queueSize != 0) {
            return j;
        }
        return -1L;
    }

    private final Object getPeekedValueLockedAt(long index) {
        Object bufferAt;
        Object[] objArr = this.buffer;
        Intrinsics.checkNotNull(objArr);
        bufferAt = SharedFlowKt.getBufferAt(objArr, index);
        return bufferAt instanceof Emitter ? ((Emitter) bufferAt).value : bufferAt;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r11v3 */
    public final Continuation<Unit>[] findSlotsToResumeLocked(Continuation<Unit>[] resumesIn) {
        AbstractSharedFlowSlot[] abstractSharedFlowSlotArr;
        SharedFlowSlot sharedFlowSlot;
        Continuation<? super Unit> continuation;
        int length = resumesIn.length;
        SharedFlowImpl<T> sharedFlowImpl = this;
        if (((AbstractSharedFlow) sharedFlowImpl).nCollectors != 0 && (abstractSharedFlowSlotArr = ((AbstractSharedFlow) sharedFlowImpl).slots) != null) {
            int length2 = abstractSharedFlowSlotArr.length;
            int i = 0;
            resumesIn = resumesIn;
            while (i < length2) {
                AbstractSharedFlowSlot abstractSharedFlowSlot = abstractSharedFlowSlotArr[i];
                if (abstractSharedFlowSlot != null && (continuation = (sharedFlowSlot = (SharedFlowSlot) abstractSharedFlowSlot).cont) != null && tryPeekLocked(sharedFlowSlot) >= 0) {
                    int length3 = resumesIn.length;
                    resumesIn = resumesIn;
                    if (length >= length3) {
                        Object[] copyOf = Arrays.copyOf(resumesIn, Math.max(2, resumesIn.length * 2));
                        Intrinsics.checkNotNullExpressionValue(copyOf, "java.util.Arrays.copyOf(this, newSize)");
                        resumesIn = (Continuation[]) copyOf;
                    }
                    resumesIn[length] = continuation;
                    sharedFlowSlot.cont = (Continuation) null;
                    length++;
                }
                i++;
                resumesIn = resumesIn;
            }
        }
        return resumesIn;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // kotlinx.coroutines.flow.internal.AbstractSharedFlow
    public SharedFlowSlot createSlot() {
        return new SharedFlowSlot();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // kotlinx.coroutines.flow.internal.AbstractSharedFlow
    public SharedFlowSlot[] createSlotArray(int size) {
        return new SharedFlowSlot[size];
    }

    @Override // kotlinx.coroutines.flow.internal.FusibleFlow
    public Flow<T> fuse(CoroutineContext context, int capacity, BufferOverflow onBufferOverflow) {
        return SharedFlowKt.fuseSharedFlow(this, context, capacity, onBufferOverflow);
    }

    /* compiled from: SharedFlow.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0002\u0018\u00002\u00020\u0001B1\u0012\n\u0010\u0002\u001a\u0006\u0012\u0002\b\u00030\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t¢\u0006\u0002\u0010\u000bJ\b\u0010\f\u001a\u00020\nH\u0016R\u0016\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t8\u0006X\u0087\u0004¢\u0006\u0002\n\u0000R\u0014\u0010\u0002\u001a\u0006\u0012\u0002\b\u00030\u00038\u0006X\u0087\u0004¢\u0006\u0002\n\u0000R\u0012\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087\u000e¢\u0006\u0002\n\u0000R\u0012\u0010\u0006\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004¢\u0006\u0002\n\u0000¨\u0006\r"}, d2 = {"Lkotlinx/coroutines/flow/SharedFlowImpl$Emitter;", "Lkotlinx/coroutines/DisposableHandle;", "flow", "Lkotlinx/coroutines/flow/SharedFlowImpl;", "index", "", "value", "", "cont", "Lkotlin/coroutines/Continuation;", "", "(Lkotlinx/coroutines/flow/SharedFlowImpl;JLjava/lang/Object;Lkotlin/coroutines/Continuation;)V", "dispose", "kotlinx-coroutines-core"}, k = 1, mv = {1, 4, 0})
    private static final class Emitter implements DisposableHandle {
        public final Continuation<Unit> cont;
        public final SharedFlowImpl<?> flow;
        public long index;
        public final Object value;

        /* JADX WARN: Multi-variable type inference failed */
        public Emitter(SharedFlowImpl<?> sharedFlowImpl, long j, Object obj, Continuation<? super Unit> continuation) {
            this.flow = sharedFlowImpl;
            this.index = j;
            this.value = obj;
            this.cont = continuation;
        }

        @Override // kotlinx.coroutines.DisposableHandle
        public void dispose() {
            this.flow.cancelEmitter(this);
        }
    }

    @Override // kotlinx.coroutines.flow.SharedFlow
    public List<T> getReplayCache() {
        Object bufferAt;
        synchronized (this) {
            int replaySize = getReplaySize();
            if (replaySize == 0) {
                return CollectionsKt.emptyList();
            }
            ArrayList arrayList = new ArrayList(replaySize);
            Object[] objArr = this.buffer;
            Intrinsics.checkNotNull(objArr);
            for (int i = 0; i < replaySize; i++) {
                bufferAt = SharedFlowKt.getBufferAt(objArr, this.replayIndex + i);
                arrayList.add(bufferAt);
            }
            return arrayList;
        }
    }

    final /* synthetic */ Object emitSuspend(T t, Continuation<? super Unit> continuation) {
        Continuation<Unit>[] continuationArr;
        Emitter emitter;
        CancellableContinuationImpl cancellableContinuationImpl = new CancellableContinuationImpl(IntrinsicsKt.intercepted(continuation), 1);
        cancellableContinuationImpl.initCancellability();
        CancellableContinuationImpl cancellableContinuationImpl2 = cancellableContinuationImpl;
        Continuation<Unit>[] continuationArr2 = AbstractSharedFlowKt.EMPTY_RESUMES;
        synchronized (this) {
            if (tryEmitLocked(t)) {
                Unit unit = Unit.INSTANCE;
                Result.Companion companion = Result.INSTANCE;
                cancellableContinuationImpl2.resumeWith(Result.m15constructorimpl(unit));
                continuationArr = findSlotsToResumeLocked(continuationArr2);
                emitter = null;
            } else {
                Emitter emitter2 = new Emitter(this, getTotalSize() + getHead(), t, cancellableContinuationImpl2);
                enqueueLocked(emitter2);
                this.queueSize++;
                if (this.bufferCapacity == 0) {
                    continuationArr2 = findSlotsToResumeLocked(continuationArr2);
                }
                continuationArr = continuationArr2;
                emitter = emitter2;
            }
        }
        if (emitter != null) {
            CancellableContinuationKt.disposeOnCancellation(cancellableContinuationImpl2, emitter);
        }
        for (Continuation<Unit> continuation2 : continuationArr) {
            if (continuation2 != null) {
                Unit unit2 = Unit.INSTANCE;
                Result.Companion companion2 = Result.INSTANCE;
                continuation2.resumeWith(Result.m15constructorimpl(unit2));
            }
        }
        Object result = cancellableContinuationImpl.getResult();
        if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
            DebugProbesKt.probeCoroutineSuspended(continuation);
        }
        return result;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void cancelEmitter(Emitter emitter) {
        Object bufferAt;
        synchronized (this) {
            if (emitter.index < getHead()) {
                return;
            }
            Object[] objArr = this.buffer;
            Intrinsics.checkNotNull(objArr);
            bufferAt = SharedFlowKt.getBufferAt(objArr, emitter.index);
            if (bufferAt != emitter) {
                return;
            }
            SharedFlowKt.setBufferAt(objArr, emitter.index, SharedFlowKt.NO_VALUE);
            cleanupTailLocked();
            Unit unit = Unit.INSTANCE;
        }
    }

    final /* synthetic */ Object awaitValue(SharedFlowSlot sharedFlowSlot, Continuation<? super Unit> continuation) {
        CancellableContinuationImpl cancellableContinuationImpl = new CancellableContinuationImpl(IntrinsicsKt.intercepted(continuation), 1);
        cancellableContinuationImpl.initCancellability();
        CancellableContinuationImpl cancellableContinuationImpl2 = cancellableContinuationImpl;
        synchronized (this) {
            if (tryPeekLocked(sharedFlowSlot) < 0) {
                sharedFlowSlot.cont = cancellableContinuationImpl2;
                sharedFlowSlot.cont = cancellableContinuationImpl2;
            } else {
                Unit unit = Unit.INSTANCE;
                Result.Companion companion = Result.INSTANCE;
                cancellableContinuationImpl2.resumeWith(Result.m15constructorimpl(unit));
            }
            Unit unit2 = Unit.INSTANCE;
        }
        Object result = cancellableContinuationImpl.getResult();
        if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
            DebugProbesKt.probeCoroutineSuspended(continuation);
        }
        return result;
    }

    @Override // kotlinx.coroutines.flow.MutableSharedFlow
    public void resetReplayCache() {
        synchronized (this) {
            updateBufferLocked(getBufferEndIndex(), this.minCollectorIndex, getBufferEndIndex(), getQueueEndIndex());
            Unit unit = Unit.INSTANCE;
        }
    }
}
