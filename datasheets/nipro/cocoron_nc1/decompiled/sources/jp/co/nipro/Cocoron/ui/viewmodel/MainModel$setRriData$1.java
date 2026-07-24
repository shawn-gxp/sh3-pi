package jp.co.nipro.cocoron.ui.viewmodel;

import java.util.List;
import jp.co.nipro.cocoron.data.db.DatabaseHelper;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri12hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri1mDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri24hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri7dDto;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: MainModel.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.ui.viewmodel.MainModel$setRriData$1", f = "MainModel.kt", i = {}, l = {324, 330, 336, 342}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class MainModel$setRriData$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ Ref.LongRef $duration;
    final /* synthetic */ Ref.ObjectRef $entities;
    final /* synthetic */ Ref.LongRef $range;
    Object L$0;
    int label;
    final /* synthetic */ MainModel this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    MainModel$setRriData$1(MainModel mainModel, Ref.ObjectRef objectRef, Ref.LongRef longRef, Ref.LongRef longRef2, Continuation continuation) {
        super(2, continuation);
        this.this$0 = mainModel;
        this.$entities = objectRef;
        this.$duration = longRef;
        this.$range = longRef2;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new MainModel$setRriData$1(this.this$0, this.$entities, this.$duration, this.$range, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((MainModel$setRriData$1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Type inference failed for: r10v15, types: [T, java.util.List] */
    /* JADX WARN: Type inference failed for: r10v19, types: [T, java.util.List] */
    /* JADX WARN: Type inference failed for: r10v23, types: [T, java.util.List] */
    /* JADX WARN: Type inference failed for: r10v28, types: [T, java.util.List] */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        Ref.ObjectRef objectRef;
        Ref.ObjectRef objectRef2;
        Ref.ObjectRef objectRef3;
        Ref.ObjectRef objectRef4;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            Integer value = this.this$0.getDayHolderSelected().getValue();
            if (value != null && value.intValue() == 0) {
                Ref.ObjectRef objectRef5 = this.$entities;
                DatabaseHelper companion = DatabaseHelper.INSTANCE.getInstance();
                this.L$0 = objectRef5;
                this.label = 1;
                Object maxMinRri12h = companion.getMaxMinRri12h(null, this);
                if (maxMinRri12h == coroutine_suspended) {
                    return coroutine_suspended;
                }
                objectRef4 = objectRef5;
                obj = maxMinRri12h;
                objectRef4.element = (List) obj;
                this.$duration.element = MaxMinRri12hDto.duration;
                this.$range.element = 43200000L;
            } else if (value != null && value.intValue() == 1) {
                Ref.ObjectRef objectRef6 = this.$entities;
                DatabaseHelper companion2 = DatabaseHelper.INSTANCE.getInstance();
                this.L$0 = objectRef6;
                this.label = 2;
                Object maxMinRri24h = companion2.getMaxMinRri24h(null, this);
                if (maxMinRri24h == coroutine_suspended) {
                    return coroutine_suspended;
                }
                objectRef3 = objectRef6;
                obj = maxMinRri24h;
                objectRef3.element = (List) obj;
                this.$duration.element = MaxMinRri24hDto.duration;
                this.$range.element = MaxMinRri24hDto.range;
            } else if (value != null && value.intValue() == 2) {
                Ref.ObjectRef objectRef7 = this.$entities;
                DatabaseHelper companion3 = DatabaseHelper.INSTANCE.getInstance();
                this.L$0 = objectRef7;
                this.label = 3;
                Object maxMinRri7d = companion3.getMaxMinRri7d(null, this);
                if (maxMinRri7d == coroutine_suspended) {
                    return coroutine_suspended;
                }
                objectRef2 = objectRef7;
                obj = maxMinRri7d;
                objectRef2.element = (List) obj;
                this.$duration.element = MaxMinRri7dDto.duration;
                this.$range.element = MaxMinRri7dDto.range;
            } else if (value != null && value.intValue() == 3) {
                Ref.ObjectRef objectRef8 = this.$entities;
                DatabaseHelper companion4 = DatabaseHelper.INSTANCE.getInstance();
                this.L$0 = objectRef8;
                this.label = 4;
                Object maxMinRri1m = companion4.getMaxMinRri1m(null, this);
                if (maxMinRri1m == coroutine_suspended) {
                    return coroutine_suspended;
                }
                objectRef = objectRef8;
                obj = maxMinRri1m;
                objectRef.element = (List) obj;
                this.$duration.element = 43200000L;
                this.$range.element = MaxMinRri1mDto.range;
            }
        } else if (i == 1) {
            objectRef4 = (Ref.ObjectRef) this.L$0;
            ResultKt.throwOnFailure(obj);
            objectRef4.element = (List) obj;
            this.$duration.element = MaxMinRri12hDto.duration;
            this.$range.element = 43200000L;
        } else if (i == 2) {
            objectRef3 = (Ref.ObjectRef) this.L$0;
            ResultKt.throwOnFailure(obj);
            objectRef3.element = (List) obj;
            this.$duration.element = MaxMinRri24hDto.duration;
            this.$range.element = MaxMinRri24hDto.range;
        } else if (i == 3) {
            objectRef2 = (Ref.ObjectRef) this.L$0;
            ResultKt.throwOnFailure(obj);
            objectRef2.element = (List) obj;
            this.$duration.element = MaxMinRri7dDto.duration;
            this.$range.element = MaxMinRri7dDto.range;
        } else {
            if (i != 4) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            objectRef = (Ref.ObjectRef) this.L$0;
            ResultKt.throwOnFailure(obj);
            objectRef.element = (List) obj;
            this.$duration.element = 43200000L;
            this.$range.element = MaxMinRri1mDto.range;
        }
        return Unit.INSTANCE;
    }
}
