package jp.co.nipro.cocoron.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import jp.co.nipro.cocoron.data.FileRecorder;
import jp.co.nipro.cocoron.data.noise.Ntf;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.BuildersKt__BuildersKt;

/* compiled from: BaseApplication.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\b\u0016\u0018\u0000 \u00162\u00020\u00012\u00020\u0002:\u0001\u0016B\u0005¢\u0006\u0002\u0010\u0003J\u001a\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0016J\u0010\u0010\u000e\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0016J\u0010\u0010\u000f\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0016J\u0010\u0010\u0010\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0016J\u0018\u0010\u0011\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0012\u001a\u00020\rH\u0016J\u0010\u0010\u0013\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0016J\u0010\u0010\u0014\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0016J\b\u0010\u0015\u001a\u00020\tH\u0016R\u0014\u0010\u0004\u001a\u00020\u0005X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007¨\u0006\u0017"}, d2 = {"Ljp/co/nipro/cocoron/common/BaseApplication;", "Landroid/app/Application;", "Landroid/app/Application$ActivityLifecycleCallbacks;", "()V", "TAG", "", "getTAG", "()Ljava/lang/String;", "onActivityCreated", "", "activity", "Landroid/app/Activity;", "savedInstanceState", "Landroid/os/Bundle;", "onActivityDestroyed", "onActivityPaused", "onActivityResumed", "onActivitySaveInstanceState", "outState", "onActivityStarted", "onActivityStopped", "onCreate", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public class BaseApplication extends Application implements Application.ActivityLifecycleCallbacks {
    public static Activity activity;
    public static Context context;
    private static boolean startedScan;
    private final String TAG = "BaseApplication";

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static String received_rri = "-";

    public final String getTAG() {
        return this.TAG;
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        context = this;
        registerActivityLifecycleCallbacks(this);
        Ntf.INSTANCE.ntf();
        FileRecorder.INSTANCE.removeOldFile();
        BuildersKt__BuildersKt.runBlocking$default(null, new BaseApplication$onCreate$1(null), 1, null);
    }

    /* compiled from: BaseApplication.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0006\u0010\u001b\u001a\u00020\u001cR\u001a\u0010\u0003\u001a\u00020\u0004X\u0086.¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\nX\u0086.¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001a\u0010\u000f\u001a\u00020\u0010X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u001a\u0010\u0015\u001a\u00020\u0016X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001a¨\u0006\u001d"}, d2 = {"Ljp/co/nipro/cocoron/common/BaseApplication$Companion;", "", "()V", "activity", "Landroid/app/Activity;", "getActivity", "()Landroid/app/Activity;", "setActivity", "(Landroid/app/Activity;)V", "context", "Landroid/content/Context;", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "received_rri", "", "getReceived_rri", "()Ljava/lang/String;", "setReceived_rri", "(Ljava/lang/String;)V", "startedScan", "", "getStartedScan", "()Z", "setStartedScan", "(Z)V", "isEdgeToEdgeEnabled", "", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final Context getContext() {
            Context context = BaseApplication.context;
            if (context == null) {
                Intrinsics.throwUninitializedPropertyAccessException("context");
            }
            return context;
        }

        public final void setContext(Context context) {
            Intrinsics.checkNotNullParameter(context, "<set-?>");
            BaseApplication.context = context;
        }

        public final Activity getActivity() {
            Activity activity = BaseApplication.activity;
            if (activity == null) {
                Intrinsics.throwUninitializedPropertyAccessException("activity");
            }
            return activity;
        }

        public final void setActivity(Activity activity) {
            Intrinsics.checkNotNullParameter(activity, "<set-?>");
            BaseApplication.activity = activity;
        }

        public final boolean getStartedScan() {
            return BaseApplication.startedScan;
        }

        public final void setStartedScan(boolean z) {
            BaseApplication.startedScan = z;
        }

        public final String getReceived_rri() {
            return BaseApplication.received_rri;
        }

        public final void setReceived_rri(String str) {
            Intrinsics.checkNotNullParameter(str, "<set-?>");
            BaseApplication.received_rri = str;
        }

        public final int isEdgeToEdgeEnabled() {
            Resources resources = getContext().getResources();
            Intrinsics.checkNotNullExpressionValue(resources, "context.resources");
            int identifier = resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
            if (identifier > 0) {
                return resources.getInteger(identifier);
            }
            return 0;
        }
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityCreated(Activity activity2, Bundle savedInstanceState) {
        Intrinsics.checkNotNullParameter(activity2, "activity");
        Log.d(this.TAG, "onActivityCreated");
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStarted(Activity activity2) {
        Intrinsics.checkNotNullParameter(activity2, "activity");
        Log.d(this.TAG, "onActivityStarted");
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityResumed(Activity activity2) {
        Intrinsics.checkNotNullParameter(activity2, "activity");
        Log.d(this.TAG, "onActivityResumed");
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityPaused(Activity activity2) {
        Intrinsics.checkNotNullParameter(activity2, "activity");
        Log.d(this.TAG, "onActivityPaused");
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStopped(Activity activity2) {
        Intrinsics.checkNotNullParameter(activity2, "activity");
        Log.d(this.TAG, "onActivityStopped");
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivitySaveInstanceState(Activity activity2, Bundle outState) {
        Intrinsics.checkNotNullParameter(activity2, "activity");
        Intrinsics.checkNotNullParameter(outState, "outState");
        Log.d(this.TAG, "onActivitySaveInstanceState");
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityDestroyed(Activity activity2) {
        Intrinsics.checkNotNullParameter(activity2, "activity");
        Log.d(this.TAG, "onActivityDestroyed");
    }
}
