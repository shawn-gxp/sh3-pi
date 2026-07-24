package jp.co.nipro.cocoron.ui.fragment;

import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.varunjohn1990.iosdialogs4android.IOSDialog;
import jp.co.nipro.cocoron.common.EventObserver;
import jp.co.nipro.cocoron.common.UtilsKt;
import jp.co.nipro.cocoron.ui.viewmodel.BaseModel;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.GlobalScope;

/* compiled from: BaseFragment.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0016\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u0006\u0010\u000f\u001a\u00020\u0010J\b\u0010\u0011\u001a\u00020\u0010H\u0016J\b\u0010\u0012\u001a\u00020\u0010H\u0016J\u0010\u0010\u0013\u001a\u00020\u00102\u0006\u0010\u0014\u001a\u00020\u0015H\u0014J\u0010\u0010\u0016\u001a\u00020\u00102\b\b\u0002\u0010\u0017\u001a\u00020\u0018R\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\nX\u0084.¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000e¨\u0006\u0019"}, d2 = {"Ljp/co/nipro/cocoron/ui/fragment/BaseFragment;", "Landroidx/fragment/app/Fragment;", "()V", "progressDialog", "Lcom/varunjohn1990/iosdialogs4android/IOSDialog;", "getProgressDialog", "()Lcom/varunjohn1990/iosdialogs4android/IOSDialog;", "setProgressDialog", "(Lcom/varunjohn1990/iosdialogs4android/IOSDialog;)V", "viewModel", "Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel;", "getViewModel", "()Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel;", "setViewModel", "(Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel;)V", "dismissProgress", "", "onPause", "onResume", "onSubscribeUi", "binding", "Landroidx/databinding/ViewDataBinding;", "showProgress", "title", "", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public class BaseFragment extends Fragment {
    private IOSDialog progressDialog;
    protected BaseModel viewModel;

    protected final BaseModel getViewModel() {
        BaseModel baseModel = this.viewModel;
        if (baseModel == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewModel");
        }
        return baseModel;
    }

    protected final void setViewModel(BaseModel baseModel) {
        Intrinsics.checkNotNullParameter(baseModel, "<set-?>");
        this.viewModel = baseModel;
    }

    public final IOSDialog getProgressDialog() {
        return this.progressDialog;
    }

    public final void setProgressDialog(IOSDialog iOSDialog) {
        this.progressDialog = iOSDialog;
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        BaseModel baseModel = this.viewModel;
        if (baseModel == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewModel");
        }
        baseModel.setUp();
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        BaseModel baseModel = this.viewModel;
        if (baseModel == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewModel");
        }
        baseModel.tearDown();
    }

    public static /* synthetic */ void showProgress$default(BaseFragment baseFragment, String str, int i, Object obj) {
        if (obj != null) {
            throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: showProgress");
        }
        if ((i & 1) != 0) {
            str = "Loading...";
        }
        baseFragment.showProgress(str);
    }

    public final void showProgress(String title) {
        Intrinsics.checkNotNullParameter(title, "title");
        if (this.progressDialog != null) {
            return;
        }
        IOSDialog.Builder builder = new IOSDialog.Builder(getActivity());
        builder.title(title);
        builder.cancelable(false);
        IOSDialog build = builder.build();
        this.progressDialog = build;
        if (build != null) {
            build.show();
        }
        BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, null, null, new BaseFragment$showProgress$1(this, null), 3, null);
    }

    public final void dismissProgress() {
        IOSDialog iOSDialog = this.progressDialog;
        if (iOSDialog != null) {
            iOSDialog.dismiss();
        }
        this.progressDialog = (IOSDialog) null;
    }

    protected void onSubscribeUi(ViewDataBinding binding) {
        Intrinsics.checkNotNullParameter(binding, "binding");
        BaseFragment baseFragment = this;
        binding.setLifecycleOwner(baseFragment);
        BaseModel baseModel = this.viewModel;
        if (baseModel == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewModel");
        }
        baseModel.getProgressEvent().observe(baseFragment, new EventObserver(new Function1<BaseModel.ProgressEvent, Unit>() { // from class: jp.co.nipro.cocoron.ui.fragment.BaseFragment$onSubscribeUi$1
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(BaseModel.ProgressEvent progressEvent) {
                invoke2(progressEvent);
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2(BaseModel.ProgressEvent it) {
                Intrinsics.checkNotNullParameter(it, "it");
                if (it == BaseModel.ProgressEvent.START) {
                    BaseFragment.showProgress$default(BaseFragment.this, null, 1, null);
                } else if (it == BaseModel.ProgressEvent.END) {
                    BaseFragment.this.dismissProgress();
                }
            }
        }));
        BaseModel baseModel2 = this.viewModel;
        if (baseModel2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewModel");
        }
        baseModel2.getDialogEvent().observe(baseFragment, new EventObserver(new Function1<BaseModel.DialogInfo, Unit>() { // from class: jp.co.nipro.cocoron.ui.fragment.BaseFragment$onSubscribeUi$2
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(BaseModel.DialogInfo dialogInfo) {
                invoke2(dialogInfo);
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2(BaseModel.DialogInfo it) {
                Intrinsics.checkNotNullParameter(it, "it");
                FragmentActivity it1 = BaseFragment.this.getActivity();
                if (it1 != null) {
                    if (Intrinsics.areEqual(it.getTitle(), "Cocoron")) {
                        Intrinsics.checkNotNullExpressionValue(it1, "it1");
                        UtilsKt.showAboutDialog(it, it1);
                    } else {
                        Intrinsics.checkNotNullExpressionValue(it1, "it1");
                        UtilsKt.showiOSDialog(it, it1);
                    }
                }
            }
        }));
    }
}
