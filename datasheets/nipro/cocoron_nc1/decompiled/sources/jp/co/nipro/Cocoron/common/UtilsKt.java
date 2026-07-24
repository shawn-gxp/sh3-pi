package jp.co.nipro.cocoron.common;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import com.varunjohn1990.iosdialogs4android.IOSDialog;
import com.varunjohn1990.iosdialogs4android.IOSDialogView;
import java.lang.reflect.Field;
import java.util.List;
import jp.co.nipro.cocoron.ui.viewmodel.BaseModel;
import kotlin.KotlinNothingValueException;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Utils.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000B\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010 \n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0001\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u000e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0001\u001a\u000e\u0010\u0003\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u0001\u001aG\u0010\u0005\u001a\b\u0012\u0004\u0012\u0002H\u00070\u0006\"\b\b\u0000\u0010\u0007*\u00020\b2\u0016\u0010\t\u001a\f\u0012\b\b\u0001\u0012\u0004\u0018\u0001H\u00070\n\"\u0004\u0018\u0001H\u00072\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u0086\bø\u0001\u0000¢\u0006\u0002\u0010\u000e\u001aM\u0010\u000f\u001a\u00020\u0010\"\b\b\u0000\u0010\u0007*\u00020\b2\u0016\u0010\t\u001a\f\u0012\b\b\u0001\u0012\u0004\u0018\u0001H\u00070\n\"\u0004\u0018\u0001H\u00072\u0018\u0010\u000b\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00070\u0006\u0012\u0004\u0012\u00020\u00100\u0011H\u0086\bø\u0001\u0000¢\u0006\u0002\u0010\u0012\u001a\u0016\u0010\u0013\u001a\u00020\u00102\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017\u001a\u0016\u0010\u0018\u001a\u00020\u00102\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017\u0082\u0002\u0007\n\u0005\b\u009920\u0001¨\u0006\u0019"}, d2 = {"convertDp2Px", "", "dp", "convertPx2Dp", "px", "guardLet", "", "T", "", "elements", "", "closure", "Lkotlin/Function0;", "", "([Ljava/lang/Object;Lkotlin/jvm/functions/Function0;)Ljava/util/List;", "ifLet", "", "Lkotlin/Function1;", "([Ljava/lang/Object;Lkotlin/jvm/functions/Function1;)V", "showAboutDialog", "info", "Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel$DialogInfo;", "context", "Landroid/content/Context;", "showiOSDialog", "app_release"}, k = 2, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class UtilsKt {
    public static final int convertDp2Px(int i) {
        DisplayMetrics displayMetrics = BaseApplication.INSTANCE.getContext().getResources().getDisplayMetrics();
        Intrinsics.checkNotNullExpressionValue(displayMetrics, "BaseApplication.context.…ces().getDisplayMetrics()");
        return (int) (i * displayMetrics.density);
    }

    public static final int convertPx2Dp(int i) {
        DisplayMetrics displayMetrics = BaseApplication.INSTANCE.getContext().getResources().getDisplayMetrics();
        Intrinsics.checkNotNullExpressionValue(displayMetrics, "BaseApplication.context.…ces().getDisplayMetrics()");
        return (int) (i / displayMetrics.density);
    }

    public static final void showiOSDialog(final BaseModel.DialogInfo info, Context context) {
        Intrinsics.checkNotNullParameter(info, "info");
        Intrinsics.checkNotNullParameter(context, "context");
        IOSDialog.Builder message = new IOSDialog.Builder(context).title(info.getTitle()).cancelable(false).message(info.getMessage());
        String actionTitle = info.getActionTitle();
        if (actionTitle == null) {
            actionTitle = "OK";
        }
        final IOSDialog.Builder positiveClickListener = message.positiveButtonText(actionTitle).positiveClickListener(new IOSDialog.Listener() { // from class: jp.co.nipro.cocoron.common.UtilsKt$showiOSDialog$builder$1
            @Override // com.varunjohn1990.iosdialogs4android.IOSDialog.Listener
            public final void onClick(IOSDialog iOSDialog) {
                Function0<Unit> action = BaseModel.DialogInfo.this.getAction();
                if (action != null) {
                    action.invoke();
                }
                iOSDialog.dismiss();
            }
        });
        String cancelTitle = info.getCancelTitle();
        if (cancelTitle != null) {
            positiveClickListener.negativeButtonText(cancelTitle).negativeClickListener(new IOSDialog.Listener() { // from class: jp.co.nipro.cocoron.common.UtilsKt$showiOSDialog$$inlined$also$lambda$1
                @Override // com.varunjohn1990.iosdialogs4android.IOSDialog.Listener
                public final void onClick(IOSDialog iOSDialog) {
                    Function0<Unit> cancel = info.getCancel();
                    if (cancel != null) {
                        cancel.invoke();
                    }
                    iOSDialog.dismiss();
                }
            });
        }
        positiveClickListener.build().show();
    }

    public static final void showAboutDialog(final BaseModel.DialogInfo info, Context context) {
        Intrinsics.checkNotNullParameter(info, "info");
        Intrinsics.checkNotNullParameter(context, "context");
        IOSDialog.Builder message = new IOSDialog.Builder(context).title(info.getTitle()).cancelable(false).message(info.getMessage());
        String actionTitle = info.getActionTitle();
        if (actionTitle == null) {
            actionTitle = "OK";
        }
        final IOSDialog.Builder positiveClickListener = message.positiveButtonText(actionTitle).positiveClickListener(new IOSDialog.Listener() { // from class: jp.co.nipro.cocoron.common.UtilsKt$showAboutDialog$builder$1
            @Override // com.varunjohn1990.iosdialogs4android.IOSDialog.Listener
            public final void onClick(IOSDialog iOSDialog) {
                Function0<Unit> action = BaseModel.DialogInfo.this.getAction();
                if (action != null) {
                    action.invoke();
                }
                iOSDialog.dismiss();
            }
        });
        String cancelTitle = info.getCancelTitle();
        if (cancelTitle != null) {
            positiveClickListener.negativeButtonText(cancelTitle).negativeClickListener(new IOSDialog.Listener() { // from class: jp.co.nipro.cocoron.common.UtilsKt$showAboutDialog$$inlined$also$lambda$1
                @Override // com.varunjohn1990.iosdialogs4android.IOSDialog.Listener
                public final void onClick(IOSDialog iOSDialog) {
                    Function0<Unit> cancel = info.getCancel();
                    if (cancel != null) {
                        cancel.invoke();
                    }
                    iOSDialog.dismiss();
                }
            });
        }
        IOSDialog build = positiveClickListener.build();
        Field field = new IOSDialogView().getClass().getDeclaredField("iosDialog");
        Intrinsics.checkNotNullExpressionValue(field, "field");
        field.setAccessible(true);
        field.set(null, build);
        context.startActivity(new Intent(context, (Class<?>) TouchDialog.class).addFlags(67108864).addFlags(268435456));
    }

    public static final <T> List<T> guardLet(T[] elements, Function0 closure) {
        Intrinsics.checkNotNullParameter(elements, "elements");
        Intrinsics.checkNotNullParameter(closure, "closure");
        int length = elements.length;
        boolean z = false;
        int i = 0;
        while (true) {
            if (i >= length) {
                z = true;
                break;
            }
            if (!(elements[i] != null)) {
                break;
            }
            i++;
        }
        if (z) {
            return ArraysKt.filterNotNull(elements);
        }
        closure.invoke();
        throw new KotlinNothingValueException();
    }

    public static final <T> void ifLet(T[] elements, Function1<? super List<? extends T>, Unit> closure) {
        Intrinsics.checkNotNullParameter(elements, "elements");
        Intrinsics.checkNotNullParameter(closure, "closure");
        int length = elements.length;
        boolean z = false;
        int i = 0;
        while (true) {
            if (i >= length) {
                z = true;
                break;
            } else {
                if (!(elements[i] != null)) {
                    break;
                } else {
                    i++;
                }
            }
        }
        if (z) {
            closure.invoke(ArraysKt.filterNotNull(elements));
        }
    }
}
