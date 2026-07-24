package jp.co.nipro.cocoron.ui.view;

import android.view.MotionEvent;
import android.view.View;
import androidx.core.app.NotificationCompat;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: FlickListener.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u0000 \u00122\u00020\u0001:\u0002\u0012\u0013B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005¢\u0006\u0002\u0010\u0006J\u001a\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000eH\u0016J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u0010\u0010\u0011\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eH\u0002R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0014"}, d2 = {"Ljp/co/nipro/cocoron/ui/view/FlickListener;", "Landroid/view/View$OnTouchListener;", "listener", "Ljp/co/nipro/cocoron/ui/view/FlickListener$Listener;", "play", "", "(Ljp/co/nipro/cocoron/ui/view/FlickListener$Listener;F)V", "lastX", "lastY", "onTouch", "", "v", "Landroid/view/View;", NotificationCompat.CATEGORY_EVENT, "Landroid/view/MotionEvent;", "touchDown", "", "touchOff", "Companion", "Listener", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class FlickListener implements View.OnTouchListener {
    private static final float DEFAULT_PLAY = 100.0f;
    private float lastX;
    private float lastY;
    private final Listener listener;
    private final float play;

    /* compiled from: FlickListener.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0003H&J\b\u0010\u0005\u001a\u00020\u0003H&J\b\u0010\u0006\u001a\u00020\u0003H&¨\u0006\u0007"}, d2 = {"Ljp/co/nipro/cocoron/ui/view/FlickListener$Listener;", "", "onFlickToDown", "", "onFlickToLeft", "onFlickToRight", "onFlickToUp", "app_release"}, k = 1, mv = {1, 4, 2})
    public interface Listener {
        void onFlickToDown();

        void onFlickToLeft();

        void onFlickToRight();

        void onFlickToUp();
    }

    public FlickListener(Listener listener) {
        this(listener, 0.0f, 2, null);
    }

    public FlickListener(Listener listener, float f) {
        Intrinsics.checkNotNullParameter(listener, "listener");
        this.listener = listener;
        this.play = f;
    }

    public /* synthetic */ FlickListener(Listener listener, float f, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(listener, (i & 2) != 0 ? DEFAULT_PLAY : f);
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View v, MotionEvent event) {
        Intrinsics.checkNotNullParameter(event, "event");
        int action = event.getAction();
        if (action == 0) {
            touchDown(event);
        } else if (action == 1) {
            touchOff(event);
        }
        return true;
    }

    private final void touchDown(MotionEvent event) {
        this.lastX = event.getX();
        this.lastY = event.getY();
    }

    private final void touchOff(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float f = this.play;
        float f2 = x + f;
        float f3 = this.lastX;
        if (f2 < f3) {
            this.listener.onFlickToLeft();
            return;
        }
        if (f3 < x - f) {
            this.listener.onFlickToRight();
            return;
        }
        float f4 = y + f;
        float f5 = this.lastY;
        if (f4 < f5) {
            this.listener.onFlickToUp();
        } else if (f5 < y - f) {
            this.listener.onFlickToDown();
        }
    }
}
