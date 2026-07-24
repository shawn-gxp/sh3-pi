package jp.co.nipro.cocoron.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.List;
import java.util.Objects;
import jp.co.nipro.Cocoron.C0009R;
import jp.co.nipro.cocoron.common.BaseApplication;
import jp.co.nipro.cocoron.common.UtilsKt;
import jp.co.nipro.cocoron.data.FileRecorder;
import jp.co.nipro.cocoron.service.BluetoothLeService;
import jp.co.nipro.cocoron.ui.fragment.MainFragment;
import jp.co.nipro.cocoron.ui.viewmodel.BaseModel;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MainActivity.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\b\u0010\u000f\u001a\u00020\u0010H\u0016J\u0012\u0010\u0011\u001a\u00020\u00102\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0014J\b\u0010\u0014\u001a\u00020\u0010H\u0014J\b\u0010\u0015\u001a\u00020\u0010H\u0014J\b\u0010\u0016\u001a\u00020\u0010H\u0014J\b\u0010\u0017\u001a\u00020\u0010H\u0014J\b\u0010\u0018\u001a\u00020\u0010H\u0014R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001c\u0010\t\u001a\u0004\u0018\u00010\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000e¨\u0006\u0019"}, d2 = {"Ljp/co/nipro/cocoron/ui/activity/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "fileRecorder", "Ljp/co/nipro/cocoron/data/FileRecorder;", "getFileRecorder", "()Ljp/co/nipro/cocoron/data/FileRecorder;", "setFileRecorder", "(Ljp/co/nipro/cocoron/data/FileRecorder;)V", NotificationCompat.CATEGORY_SERVICE, "Ljp/co/nipro/cocoron/service/BluetoothLeService;", "getService", "()Ljp/co/nipro/cocoron/service/BluetoothLeService;", "setService", "(Ljp/co/nipro/cocoron/service/BluetoothLeService;)V", "onBackPressed", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onPause", "onResume", "onStart", "onStop", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class MainActivity extends AppCompatActivity {
    private FileRecorder fileRecorder = FileRecorder.INSTANCE.getInstance();
    private BluetoothLeService service;

    public final FileRecorder getFileRecorder() {
        return this.fileRecorder;
    }

    public final void setFileRecorder(FileRecorder fileRecorder) {
        Intrinsics.checkNotNullParameter(fileRecorder, "<set-?>");
        this.fileRecorder = fileRecorder;
    }

    public final BluetoothLeService getService() {
        return this.service;
    }

    public final void setService(BluetoothLeService bluetoothLeService) {
        this.service = bluetoothLeService;
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.INSTANCE.setActivity(this);
        setContentView(C0009R.layout.activity_main);
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            UtilsKt.showiOSDialog(new BaseModel.DialogInfo("この端末はBluetoothをサポートされていません。", null, "アプリを閉じる", new Function0<Unit>() { // from class: jp.co.nipro.cocoron.ui.activity.MainActivity$onCreate$1
                {
                    super(0);
                }

                @Override // kotlin.jvm.functions.Function0
                public /* bridge */ /* synthetic */ Unit invoke() {
                    invoke2();
                    return Unit.INSTANCE;
                }

                /* renamed from: invoke, reason: avoid collision after fix types in other method */
                public final void invoke2() {
                    MainActivity.this.finish();
                }
            }, null, null, 48, null), this);
            return;
        }
        MainActivity mainActivity = this;
        Intent intent = new Intent(mainActivity, (Class<?>) BluetoothLeService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        bindService(new Intent(mainActivity, (Class<?>) BluetoothLeService.class), new ServiceConnection() { // from class: jp.co.nipro.cocoron.ui.activity.MainActivity$onCreate$$inlined$also$lambda$1
            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName p0) {
            }

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName p0, IBinder binder) {
                Objects.requireNonNull(binder, "null cannot be cast to non-null type jp.co.nipro.cocoron.service.BluetoothLeService.BleBinder");
                BluetoothLeService.BleBinder bleBinder = (BluetoothLeService.BleBinder) binder;
                MainActivity.this.setService(bleBinder.getThis$0());
                FragmentManager supportFragmentManager = MainActivity.this.getSupportFragmentManager();
                Intrinsics.checkNotNullExpressionValue(supportFragmentManager, "getSupportFragmentManager()");
                List<Fragment> fragments = supportFragmentManager.getFragments();
                Intrinsics.checkNotNullExpressionValue(fragments, "getSupportFragmentManager().fragments");
                for (Fragment fragment : fragments) {
                    if (fragment instanceof MainFragment) {
                        ((MainFragment) fragment).initByService(bleBinder.getThis$0());
                    }
                }
            }
        }, 1);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(C0009R.id.container, new MainFragment()).commit();
        }
        Log.d("MainActivity", "onCreate");
        FileRecorder.INSTANCE.getInstance().writeText("APP STARTED");
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        if (BaseApplication.INSTANCE.isEdgeToEdgeEnabled() == 2) {
            UtilsKt.showiOSDialog(new BaseModel.DialogInfo("画面中央を右から左にスワイプして下さい", null, null, null, null, null, 62, null), this);
        }
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        this.fileRecorder.writeText("ON DESTORY");
        Log.d("MainActivity", "onDestroy!");
        super.onDestroy();
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "onStart!");
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume!");
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause!");
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop!");
    }
}
