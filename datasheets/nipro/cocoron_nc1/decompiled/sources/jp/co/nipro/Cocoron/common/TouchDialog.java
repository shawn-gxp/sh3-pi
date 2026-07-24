package jp.co.nipro.cocoron.common;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.varunjohn1990.iosdialogs4android.IOSDialogView;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Objects;
import jp.co.nipro.cocoron.data.FileRecorder;
import jp.co.nipro.cocoron.ui.viewmodel.BaseModel;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Utils.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u0012\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u0014R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000e¨\u0006\u0013"}, d2 = {"Ljp/co/nipro/cocoron/common/TouchDialog;", "Lcom/varunjohn1990/iosdialogs4android/IOSDialogView;", "()V", "clickCount", "", "getClickCount", "()I", "setClickCount", "(I)V", "startClinkDate", "Ljava/util/Date;", "getStartClinkDate", "()Ljava/util/Date;", "setStartClinkDate", "(Ljava/util/Date;)V", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class TouchDialog extends IOSDialogView {
    private int clickCount;
    private Date startClinkDate = new Date();

    public final Date getStartClinkDate() {
        return this.startClinkDate;
    }

    public final void setStartClinkDate(Date date) {
        Intrinsics.checkNotNullParameter(date, "<set-?>");
        this.startClinkDate = date;
    }

    public final int getClickCount() {
        return this.clickCount;
    }

    public final void setClickCount(int i) {
        this.clickCount = i;
    }

    @Override // com.varunjohn1990.iosdialogs4android.IOSDialogView, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Field field = getClass().getSuperclass().getDeclaredField("textViewTitle");
        Intrinsics.checkNotNullExpressionValue(field, "field");
        field.setAccessible(true);
        Object obj = field.get(this);
        Objects.requireNonNull(obj, "null cannot be cast to non-null type android.widget.TextView");
        ((TextView) obj).setOnClickListener(new View.OnClickListener() { // from class: jp.co.nipro.cocoron.common.TouchDialog$onCreate$1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Log.d("TouchDialog", "ClickTitle");
                if (new Date().getTime() - TouchDialog.this.getStartClinkDate().getTime() > 3000) {
                    TouchDialog.this.setClickCount(1);
                    TouchDialog.this.setStartClinkDate(new Date());
                    return;
                }
                TouchDialog touchDialog = TouchDialog.this;
                touchDialog.setClickCount(touchDialog.getClickCount() + 1);
                if (TouchDialog.this.getClickCount() >= 5) {
                    TouchDialog.this.dismiss();
                    TouchDialog.this.setClickCount(0);
                    FileRecorder.Companion.moveFileToSd();
                    UtilsKt.showiOSDialog(new BaseModel.DialogInfo("ログファイルとDBファイルを保存しました。", null, null, null, null, null, 62, null), BaseApplication.INSTANCE.getActivity());
                }
            }
        });
    }
}
