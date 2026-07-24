package jp.co.nipro.cocoron.ui.fragment;

import android.view.View;
import com.varunjohn1990.iosdialogs4android.IOSDialog;
import com.varunjohn1990.iosdialogs4android.IOSDialogView;
import java.lang.reflect.Field;
import java.util.Objects;
import jp.co.nipro.Cocoron.C0009R;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.DelayKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;

/* compiled from: BaseFragment.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.ui.fragment.BaseFragment$showProgress$1", f = "BaseFragment.kt", i = {0, 1}, l = {47, 49}, m = "invokeSuspend", n = {"field", "field"}, s = {"L$0", "L$0"})
/* loaded from: classes.dex */
final class BaseFragment$showProgress$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    Object L$0;
    int label;
    final /* synthetic */ BaseFragment this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    BaseFragment$showProgress$1(BaseFragment baseFragment, Continuation continuation) {
        super(2, continuation);
        this.this$0 = baseFragment;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new BaseFragment$showProgress$1(this.this$0, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((BaseFragment$showProgress$1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        BaseFragment$showProgress$1 baseFragment$showProgress$1;
        Field field;
        Object obj2;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            IOSDialog progressDialog = this.this$0.getProgressDialog();
            Intrinsics.checkNotNull(progressDialog);
            Field field2 = progressDialog.getClass().getDeclaredField("iosDialogView");
            Intrinsics.checkNotNullExpressionValue(field2, "field");
            field2.setAccessible(true);
            baseFragment$showProgress$1 = this;
            field = field2;
            obj2 = coroutine_suspended;
        } else {
            if (i != 1) {
                if (i == 2) {
                    field = (Field) this.L$0;
                    ResultKt.throwOnFailure(obj);
                    baseFragment$showProgress$1 = this;
                    BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, baseFragment$showProgress$1.new AnonymousClass1(field, null), 2, null);
                    return Unit.INSTANCE;
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            Field field3 = (Field) this.L$0;
            ResultKt.throwOnFailure(obj);
            obj2 = coroutine_suspended;
            field = field3;
            baseFragment$showProgress$1 = this;
        }
        while (field.get(baseFragment$showProgress$1.this$0.getProgressDialog()) == null) {
            baseFragment$showProgress$1.L$0 = field;
            baseFragment$showProgress$1.label = 1;
            if (DelayKt.delay(100L, baseFragment$showProgress$1) == obj2) {
                return obj2;
            }
        }
        baseFragment$showProgress$1.L$0 = field;
        baseFragment$showProgress$1.label = 2;
        if (DelayKt.delay(100L, baseFragment$showProgress$1) == obj2) {
            return obj2;
        }
        BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, baseFragment$showProgress$1.new AnonymousClass1(field, null), 2, null);
        return Unit.INSTANCE;
    }

    /* compiled from: BaseFragment.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
    @DebugMetadata(c = "jp.co.nipro.cocoron.ui.fragment.BaseFragment$showProgress$1$1", f = "BaseFragment.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: jp.co.nipro.cocoron.ui.fragment.BaseFragment$showProgress$1$1, reason: invalid class name */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ Field $field;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(Field field, Continuation continuation) {
            super(2, continuation);
            this.$field = field;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
            Intrinsics.checkNotNullParameter(completion, "completion");
            return BaseFragment$showProgress$1.this.new AnonymousClass1(this.$field, completion);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label != 0) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
            Object obj2 = this.$field.get(BaseFragment$showProgress$1.this.this$0.getProgressDialog());
            Objects.requireNonNull(obj2, "null cannot be cast to non-null type com.varunjohn1990.iosdialogs4android.IOSDialogView");
            IOSDialogView iOSDialogView = (IOSDialogView) obj2;
            View findViewById = iOSDialogView.findViewById(C0009R.id.layout2Options);
            Intrinsics.checkNotNullExpressionValue(findViewById, "dialog.findViewById<View>(R.id.layout2Options)");
            findViewById.setVisibility(8);
            View findViewById2 = iOSDialogView.findViewById(C0009R.id.layoutMultipleOptions);
            Intrinsics.checkNotNullExpressionValue(findViewById2, "dialog.findViewById<View…id.layoutMultipleOptions)");
            findViewById2.setVisibility(8);
            View message = iOSDialogView.findViewById(C0009R.id.textViewMessage);
            Intrinsics.checkNotNullExpressionValue(message, "message");
            message.setPadding(message.getPaddingLeft(), message.getPaddingTop(), message.getPaddingRight(), 0);
            return Unit.INSTANCE;
        }
    }
}
