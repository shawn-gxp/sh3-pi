package jp.co.nipro.cocoron.ui.fragment;

import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.solver.widgets.analyzer.BasicMeasure;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewKt;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentViewModelLazyKt;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.varunjohn1990.iosdialogs4android.IOSDialog;
import com.varunjohn1990.iosdialogs4android.IOSDialogButton;
import com.varunjohn1990.iosdialogs4android.IOSDialogMultiOptionsListeners;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import jp.co.nipro.Cocoron.C0009R;
import jp.co.nipro.cocoron.common.BaseApplication;
import jp.co.nipro.cocoron.common.Config;
import jp.co.nipro.cocoron.common.Event;
import jp.co.nipro.cocoron.common.EventObserver;
import jp.co.nipro.cocoron.common.UtilsKt;
import jp.co.nipro.cocoron.common.extension.ExtensionKt;
import jp.co.nipro.cocoron.common.extension.NonContinuousLineChartRenderer;
import jp.co.nipro.cocoron.data.FileRecorder;
import jp.co.nipro.cocoron.data.value.BatteryLevelCharacteristicValue;
import jp.co.nipro.cocoron.databinding.FragmentMainBinding;
import jp.co.nipro.cocoron.service.BluetoothLeService;
import jp.co.nipro.cocoron.ui.activity.MainActivity;
import jp.co.nipro.cocoron.ui.view.FlickListener;
import jp.co.nipro.cocoron.ui.viewmodel.BaseModel;
import jp.co.nipro.cocoron.ui.viewmodel.MainModel;
import kotlin.Lazy;
import kotlin.Metadata;
import kotlin.NoWhenBranchMatchedException;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.io.CloseableKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.jvm.internal.Reflection;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.text.Charsets;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;
import kotlinx.coroutines.Job;

/* compiled from: MainFragment.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000°\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\t\u0018\u00002\u00020\u0001:\u0002\u0086\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u0006\u0010^\u001a\u00020_J\u0006\u0010`\u001a\u00020_J\u0006\u0010a\u001a\u00020_J\u0006\u0010b\u001a\u00020_J\u0006\u0010c\u001a\u00020_J\u0006\u0010d\u001a\u00020_J\u0006\u0010e\u001a\u00020_J\u0006\u0010f\u001a\u00020_J\u000e\u0010g\u001a\u00020_2\u0006\u0010h\u001a\u00020\u0013J\u0010\u0010i\u001a\u00020_2\u0006\u0010j\u001a\u00020kH\u0003J&\u0010l\u001a\u0004\u0018\u00010m2\u0006\u0010n\u001a\u00020o2\b\u0010p\u001a\u0004\u0018\u00010q2\b\u0010r\u001a\u0004\u0018\u00010sH\u0017J\b\u0010t\u001a\u00020_H\u0016J\b\u0010u\u001a\u00020_H\u0016J\b\u0010v\u001a\u00020_H\u0016J\b\u0010w\u001a\u00020_H\u0016J\b\u0010x\u001a\u00020_H\u0016J\b\u0010y\u001a\u00020_H\u0016J\u0006\u0010z\u001a\u00020_J\u0010\u0010{\u001a\u00020_2\u0006\u0010j\u001a\u00020kH\u0014J\u0016\u0010|\u001a\u00020_2\u0006\u0010}\u001a\u00020~2\u0006\u0010\u007f\u001a\u00020JJ\u0007\u0010\u0080\u0001\u001a\u00020_J\u0007\u0010\u0081\u0001\u001a\u00020_J\u0007\u0010\u0082\u0001\u001a\u00020_J\u0007\u0010\u0083\u0001\u001a\u00020_J\u0007\u0010\u0084\u0001\u001a\u00020_J\u0007\u0010\u0085\u0001\u001a\u00020_R\u0014\u0010\u0003\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0014\u0010\u0007\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0006R\u000e\u0010\t\u001a\u00020\nX\u0082D¢\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u0006R\u001a\u0010\r\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u001a\u0010\u0012\u001a\u00020\u0013X\u0086.¢\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u0015\"\u0004\b\u0016\u0010\u0017R\u001a\u0010\u0018\u001a\u00020\u0019X\u0086.¢\u0006\u000e\n\u0000\u001a\u0004\b\u001a\u0010\u001b\"\u0004\b\u001c\u0010\u001dR\u001a\u0010\u001e\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\u000f\"\u0004\b \u0010\u0011R\u001c\u0010!\u001a\u0004\u0018\u00010\"X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b#\u0010$\"\u0004\b%\u0010&R\u001c\u0010'\u001a\u0004\u0018\u00010\"X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b(\u0010$\"\u0004\b)\u0010&R\u001c\u0010*\u001a\u0004\u0018\u00010+X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b,\u0010-\"\u0004\b.\u0010/R\u001c\u00100\u001a\u0004\u0018\u00010\"X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b1\u0010$\"\u0004\b2\u0010&R\u0011\u00103\u001a\u000204¢\u0006\b\n\u0000\u001a\u0004\b5\u00106R\u001b\u00107\u001a\u0002088BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b;\u0010<\u001a\u0004\b9\u0010:R\"\u0010=\u001a\n\u0012\u0004\u0012\u00020?\u0018\u00010>X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b@\u0010A\"\u0004\bB\u0010CR\u001f\u0010D\u001a\u0010\u0012\f\u0012\n F*\u0004\u0018\u00010\u00040\u00040E¢\u0006\b\n\u0000\u001a\u0004\bG\u0010HR\u001a\u0010I\u001a\u00020JX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bK\u0010L\"\u0004\bM\u0010NR\u001f\u0010O\u001a\u0010\u0012\f\u0012\n F*\u0004\u0018\u00010P0P0E¢\u0006\b\n\u0000\u001a\u0004\bQ\u0010HR\u001a\u0010R\u001a\u00020SX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bT\u0010U\"\u0004\bV\u0010WR\u001a\u0010X\u001a\u00020YX\u0086.¢\u0006\u000e\n\u0000\u001a\u0004\bZ\u0010[\"\u0004\b\\\u0010]¨\u0006\u0087\u0001"}, d2 = {"Ljp/co/nipro/cocoron/ui/fragment/MainFragment;", "Ljp/co/nipro/cocoron/ui/fragment/BaseFragment;", "()V", "CHANNEL_ID", "", "getCHANNEL_ID", "()Ljava/lang/String;", "CHANNEL_NAME", "getCHANNEL_NAME", "REQUEST_PERMISSION", "", "TAG", "getTAG", "bleStatus", "getBleStatus", "()I", "setBleStatus", "(I)V", "bluetoothLeService", "Ljp/co/nipro/cocoron/service/BluetoothLeService;", "getBluetoothLeService", "()Ljp/co/nipro/cocoron/service/BluetoothLeService;", "setBluetoothLeService", "(Ljp/co/nipro/cocoron/service/BluetoothLeService;)V", "bpmAnimation", "Landroid/graphics/drawable/AnimationDrawable;", "getBpmAnimation", "()Landroid/graphics/drawable/AnimationDrawable;", "setBpmAnimation", "(Landroid/graphics/drawable/AnimationDrawable;)V", "chartAreaMode", "getChartAreaMode", "setChartAreaMode", "checkTimer", "Landroid/os/CountDownTimer;", "getCheckTimer", "()Landroid/os/CountDownTimer;", "setCheckTimer", "(Landroid/os/CountDownTimer;)V", "connectionRetryingTimer", "getConnectionRetryingTimer", "setConnectionRetryingTimer", "disConnectJob", "Lkotlinx/coroutines/Job;", "getDisConnectJob", "()Lkotlinx/coroutines/Job;", "setDisConnectJob", "(Lkotlinx/coroutines/Job;)V", "drawTimer", "getDrawTimer", "setDrawTimer", "fickListener", "Ljp/co/nipro/cocoron/ui/view/FlickListener$Listener;", "getFickListener", "()Ljp/co/nipro/cocoron/ui/view/FlickListener$Listener;", "mainModel", "Ljp/co/nipro/cocoron/ui/viewmodel/MainModel;", "getMainModel", "()Ljp/co/nipro/cocoron/ui/viewmodel/MainModel;", "mainModel$delegate", "Lkotlin/Lazy;", "observer", "Ljp/co/nipro/cocoron/common/EventObserver;", "Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel$EventParam;", "getObserver", "()Ljp/co/nipro/cocoron/common/EventObserver;", "setObserver", "(Ljp/co/nipro/cocoron/common/EventObserver;)V", "requestPermission", "Landroidx/activity/result/ActivityResultLauncher;", "kotlin.jvm.PlatformType", "getRequestPermission", "()Landroidx/activity/result/ActivityResultLauncher;", "serivceInited", "", "getSerivceInited", "()Z", "setSerivceInited", "(Z)V", "startForResult", "Landroid/content/Intent;", "getStartForResult", "startWaitTime", "", "getStartWaitTime", "()J", "setStartWaitTime", "(J)V", "viewBinding", "Ljp/co/nipro/cocoron/databinding/FragmentMainBinding;", "getViewBinding", "()Ljp/co/nipro/cocoron/databinding/FragmentMainBinding;", "setViewBinding", "(Ljp/co/nipro/cocoron/databinding/FragmentMainBinding;)V", "aboutDialog", "", "changeDevice", "changeDrawTimer", "checkBleStatusTimer", "checkPermission", "disconnectDevice", "disconnectionNotification", "enableBluetooth", "initByService", "bleService", "initView", "binding", "Landroidx/databinding/ViewDataBinding;", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onDestroyView", "onPause", "onResume", "onStart", "onStop", "onSubscribeService", "onSubscribeUi", "refreshIcon", "icon", "Ljp/co/nipro/cocoron/ui/fragment/MainFragment$Icon;", "animated", "searchDevice", "sendIntervalOk", "setEcgMode", "showBleOffAlert", "topBottomOk", "waitNoFindDevice", "Icon", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class MainFragment extends BaseFragment {
    private int bleStatus;
    public BluetoothLeService bluetoothLeService;
    public AnimationDrawable bpmAnimation;
    private int chartAreaMode;
    private CountDownTimer checkTimer;
    private CountDownTimer connectionRetryingTimer;
    private Job disConnectJob;
    private CountDownTimer drawTimer;
    private final FlickListener.Listener fickListener;

    /* renamed from: mainModel$delegate, reason: from kotlin metadata */
    private final Lazy mainModel;
    private EventObserver<BaseModel.EventParam> observer;
    private final ActivityResultLauncher<String> requestPermission;
    private boolean serivceInited;
    private final ActivityResultLauncher<Intent> startForResult;
    private long startWaitTime;
    public FragmentMainBinding viewBinding;
    private final String TAG = "MainFragment";
    private final String CHANNEL_ID = "jp.co.nipro.cocoron.channel_id";
    private final String CHANNEL_NAME = "通知";
    private final int REQUEST_PERMISSION = 1000;

    /* compiled from: MainFragment.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0086\u0001\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005¨\u0006\u0006"}, d2 = {"Ljp/co/nipro/cocoron/ui/fragment/MainFragment$Icon;", "", "(Ljava/lang/String;I)V", "CONNECT", "OUT_SERVICE", "BATTERY", "app_release"}, k = 1, mv = {1, 4, 2})
    public enum Icon {
        CONNECT,
        OUT_SERVICE,
        BATTERY
    }

    @Metadata(bv = {1, 0, 3}, k = 3, mv = {1, 4, 2})
    public final /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[Icon.values().length];
            $EnumSwitchMapping$0 = iArr;
            iArr[Icon.CONNECT.ordinal()] = 1;
            iArr[Icon.OUT_SERVICE.ordinal()] = 2;
            iArr[Icon.BATTERY.ordinal()] = 3;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final MainModel getMainModel() {
        return (MainModel) this.mainModel.getValue();
    }

    public MainFragment() {
        MainFragment$mainModel$2 mainFragment$mainModel$2 = new Function0<ViewModelProvider.Factory>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$mainModel$2
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // kotlin.jvm.functions.Function0
            public final ViewModelProvider.Factory invoke() {
                return new ViewModelProvider.NewInstanceFactory();
            }
        };
        final Function0<Fragment> function0 = new Function0<Fragment>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$$special$$inlined$viewModels$1
            {
                super(0);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // kotlin.jvm.functions.Function0
            public final Fragment invoke() {
                return Fragment.this;
            }
        };
        this.mainModel = FragmentViewModelLazyKt.createViewModelLazy(this, Reflection.getOrCreateKotlinClass(MainModel.class), new Function0<ViewModelStore>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$$special$$inlined$viewModels$2
            {
                super(0);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // kotlin.jvm.functions.Function0
            public final ViewModelStore invoke() {
                ViewModelStore viewModelStore = ((ViewModelStoreOwner) Function0.this.invoke()).getViewModelStore();
                Intrinsics.checkNotNullExpressionValue(viewModelStore, "ownerProducer().viewModelStore");
                return viewModelStore;
            }
        }, mainFragment$mainModel$2);
        this.fickListener = new FlickListener.Listener() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$fickListener$1
            @Override // jp.co.nipro.cocoron.ui.view.FlickListener.Listener
            public void onFlickToLeft() {
                MainModel mainModel;
                Log.d(MainFragment.this.getTAG(), "FlickToLeft");
                ImageView imageView = MainFragment.this.getViewBinding().flickBackgroud;
                LinearLayout linearLayout = MainFragment.this.getViewBinding().flickArea;
                Intrinsics.checkNotNullExpressionValue(linearLayout, "viewBinding.flickArea");
                imageView.setImageBitmap(ViewKt.drawToBitmap$default(linearLayout, null, 1, null));
                if (MainFragment.this.getChartAreaMode() == 0) {
                    MainFragment.this.setChartAreaMode(1);
                    LinearLayout linearLayout2 = MainFragment.this.getViewBinding().rriArea;
                    Intrinsics.checkNotNullExpressionValue(linearLayout2, "viewBinding.rriArea");
                    linearLayout2.setVisibility(0);
                    LinearLayout linearLayout3 = MainFragment.this.getViewBinding().ecgArea;
                    Intrinsics.checkNotNullExpressionValue(linearLayout3, "viewBinding.ecgArea");
                    linearLayout3.setVisibility(8);
                    MainFragment.this.getViewBinding().dotImage.setImageResource(C0009R.drawable.dot2);
                } else if (MainFragment.this.getChartAreaMode() == 1) {
                    MainFragment.this.setChartAreaMode(2);
                    LinearLayout linearLayout4 = MainFragment.this.getViewBinding().rriArea;
                    Intrinsics.checkNotNullExpressionValue(linearLayout4, "viewBinding.rriArea");
                    linearLayout4.setVisibility(8);
                    LinearLayout linearLayout5 = MainFragment.this.getViewBinding().ecgArea;
                    Intrinsics.checkNotNullExpressionValue(linearLayout5, "viewBinding.ecgArea");
                    linearLayout5.setVisibility(0);
                    MainFragment.this.getViewBinding().dotImage.setImageResource(C0009R.drawable.dot3);
                } else if (MainFragment.this.getChartAreaMode() == 2) {
                    MainFragment.this.setChartAreaMode(0);
                    LinearLayout linearLayout6 = MainFragment.this.getViewBinding().rriArea;
                    Intrinsics.checkNotNullExpressionValue(linearLayout6, "viewBinding.rriArea");
                    linearLayout6.setVisibility(0);
                    LinearLayout linearLayout7 = MainFragment.this.getViewBinding().ecgArea;
                    Intrinsics.checkNotNullExpressionValue(linearLayout7, "viewBinding.ecgArea");
                    linearLayout7.setVisibility(0);
                    MainFragment.this.getViewBinding().dotImage.setImageResource(C0009R.drawable.dot1);
                }
                LinearLayout linearLayout8 = MainFragment.this.getViewBinding().flickArea;
                Intrinsics.checkNotNullExpressionValue(linearLayout8, "viewBinding.flickArea");
                Intrinsics.checkNotNullExpressionValue(MainFragment.this.getViewBinding().flickArea, "viewBinding.flickArea");
                linearLayout8.setTranslationX(r1.getWidth());
                MainFragment.this.getViewBinding().rriMarkView.requestLayout();
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(MainFragment.this.getViewBinding().flickArea, "translationX", 0.0f);
                ofFloat.setDuration(500L);
                ofFloat.start();
                mainModel = MainFragment.this.getMainModel();
                mainModel.mvHolderChanged();
            }

            @Override // jp.co.nipro.cocoron.ui.view.FlickListener.Listener
            public void onFlickToRight() {
                Log.d(MainFragment.this.getTAG(), "onFlickToRight");
                ImageView imageView = MainFragment.this.getViewBinding().batteryLabel;
                Intrinsics.checkNotNullExpressionValue(imageView, "viewBinding.batteryLabel");
                ExtensionKt.setFlashing(imageView, true);
                ImageView imageView2 = MainFragment.this.getViewBinding().batteryLabel;
                Intrinsics.checkNotNullExpressionValue(imageView2, "viewBinding.batteryLabel");
                ExtensionKt.refresh(imageView2);
            }

            @Override // jp.co.nipro.cocoron.ui.view.FlickListener.Listener
            public void onFlickToUp() {
                Log.d(MainFragment.this.getTAG(), "onFlickToUp");
            }

            @Override // jp.co.nipro.cocoron.ui.view.FlickListener.Listener
            public void onFlickToDown() {
                Log.d(MainFragment.this.getTAG(), "onFlickToDown");
            }
        };
        this.startWaitTime = new Date().getTime();
        ActivityResultLauncher<String> registerForActivityResult = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$requestPermission$1
            @Override // androidx.activity.result.ActivityResultCallback
            public final void onActivityResult(Boolean result) {
                Intrinsics.checkNotNullExpressionValue(result, "result");
                if (result.booleanValue()) {
                    MainFragment.this.enableBluetooth();
                    return;
                }
                final FragmentActivity act = MainFragment.this.getActivity();
                if (act != null) {
                    BaseModel.DialogInfo dialogInfo = new BaseModel.DialogInfo("デバイスを接続するには位置情報取得の許可が必要です。", null, "設定へ", new Function0<Unit>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$requestPermission$1$1$1
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
                            Intent intent = new Intent();
                            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                            FragmentActivity act2 = FragmentActivity.this;
                            Intrinsics.checkNotNullExpressionValue(act2, "act");
                            Context applicationContext = act2.getApplicationContext();
                            Intrinsics.checkNotNullExpressionValue(applicationContext, "act.applicationContext");
                            intent.setData(Uri.fromParts("package", applicationContext.getPackageName(), null));
                            intent.setFlags(268435456);
                            FragmentActivity act3 = FragmentActivity.this;
                            Intrinsics.checkNotNullExpressionValue(act3, "act");
                            act3.getApplicationContext().startActivity(intent);
                            FragmentActivity.this.finish();
                        }
                    }, null, null, 48, null);
                    Intrinsics.checkNotNullExpressionValue(act, "act");
                    UtilsKt.showiOSDialog(dialogInfo, act);
                }
            }
        });
        Intrinsics.checkNotNullExpressionValue(registerForActivityResult, "registerForActivityResul…}\n            }\n        }");
        this.requestPermission = registerForActivityResult;
        ActivityResultLauncher<Intent> registerForActivityResult2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$startForResult$1
            @Override // androidx.activity.result.ActivityResultCallback
            public final void onActivityResult(ActivityResult activityResult) {
                if (activityResult != null && activityResult.getResultCode() == -1) {
                    MainFragment.this.enableBluetooth();
                    return;
                }
                final FragmentActivity it = MainFragment.this.getActivity();
                if (it != null) {
                    BaseModel.DialogInfo dialogInfo = new BaseModel.DialogInfo("デバイスを接続するにはBluetoothを有効にする必要があります。", null, "OK", new Function0<Unit>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$startForResult$1$1$1
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
                            Intent intent = new Intent();
                            intent.setAction("android.settings.BLUETOOTH_SETTINGS");
                            intent.setFlags(268435456);
                            FragmentActivity it2 = FragmentActivity.this;
                            Intrinsics.checkNotNullExpressionValue(it2, "it");
                            it2.getApplicationContext().startActivity(intent);
                            FragmentActivity.this.finish();
                        }
                    }, null, null, 48, null);
                    Intrinsics.checkNotNullExpressionValue(it, "it");
                    UtilsKt.showiOSDialog(dialogInfo, it);
                }
            }
        });
        Intrinsics.checkNotNullExpressionValue(registerForActivityResult2, "registerForActivityResul…\n            }\n\n        }");
        this.startForResult = registerForActivityResult2;
    }

    public final String getTAG() {
        return this.TAG;
    }

    public final String getCHANNEL_ID() {
        return this.CHANNEL_ID;
    }

    public final String getCHANNEL_NAME() {
        return this.CHANNEL_NAME;
    }

    public final Job getDisConnectJob() {
        return this.disConnectJob;
    }

    public final void setDisConnectJob(Job job) {
        this.disConnectJob = job;
    }

    public final CountDownTimer getConnectionRetryingTimer() {
        return this.connectionRetryingTimer;
    }

    public final void setConnectionRetryingTimer(CountDownTimer countDownTimer) {
        this.connectionRetryingTimer = countDownTimer;
    }

    public final BluetoothLeService getBluetoothLeService() {
        BluetoothLeService bluetoothLeService = this.bluetoothLeService;
        if (bluetoothLeService == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        return bluetoothLeService;
    }

    public final void setBluetoothLeService(BluetoothLeService bluetoothLeService) {
        Intrinsics.checkNotNullParameter(bluetoothLeService, "<set-?>");
        this.bluetoothLeService = bluetoothLeService;
    }

    public final CountDownTimer getDrawTimer() {
        return this.drawTimer;
    }

    public final void setDrawTimer(CountDownTimer countDownTimer) {
        this.drawTimer = countDownTimer;
    }

    public final FragmentMainBinding getViewBinding() {
        FragmentMainBinding fragmentMainBinding = this.viewBinding;
        if (fragmentMainBinding == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        return fragmentMainBinding;
    }

    public final void setViewBinding(FragmentMainBinding fragmentMainBinding) {
        Intrinsics.checkNotNullParameter(fragmentMainBinding, "<set-?>");
        this.viewBinding = fragmentMainBinding;
    }

    public final int getChartAreaMode() {
        return this.chartAreaMode;
    }

    public final void setChartAreaMode(int i) {
        this.chartAreaMode = i;
    }

    public final AnimationDrawable getBpmAnimation() {
        AnimationDrawable animationDrawable = this.bpmAnimation;
        if (animationDrawable == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bpmAnimation");
        }
        return animationDrawable;
    }

    public final void setBpmAnimation(AnimationDrawable animationDrawable) {
        Intrinsics.checkNotNullParameter(animationDrawable, "<set-?>");
        this.bpmAnimation = animationDrawable;
    }

    public final int getBleStatus() {
        return this.bleStatus;
    }

    public final void setBleStatus(int i) {
        this.bleStatus = i;
    }

    public final CountDownTimer getCheckTimer() {
        return this.checkTimer;
    }

    public final void setCheckTimer(CountDownTimer countDownTimer) {
        this.checkTimer = countDownTimer;
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Intrinsics.checkNotNullParameter(inflater, "inflater");
        setViewModel(getMainModel());
        ViewDataBinding inflate = DataBindingUtil.inflate(inflater, C0009R.layout.fragment_main, container, false);
        Intrinsics.checkNotNullExpressionValue(inflate, "DataBindingUtil.inflate(…ontainer, false\n        )");
        FragmentMainBinding fragmentMainBinding = (FragmentMainBinding) inflate;
        FragmentActivity activity = getActivity();
        Objects.requireNonNull(activity, "null cannot be cast to non-null type jp.co.nipro.cocoron.ui.activity.MainActivity");
        BluetoothLeService service = ((MainActivity) activity).getService();
        if (service != null) {
            initByService(service);
        }
        initView(fragmentMainBinding);
        changeDrawTimer();
        return fragmentMainBinding.getRoot();
    }

    public final boolean getSerivceInited() {
        return this.serivceInited;
    }

    public final void setSerivceInited(boolean z) {
        this.serivceInited = z;
    }

    public final void initByService(BluetoothLeService bleService) {
        Intrinsics.checkNotNullParameter(bleService, "bleService");
        Log.d(this.TAG, "initByService:" + this.serivceInited + " bleService:" + bleService);
        this.bluetoothLeService = bleService;
        if (this.serivceInited) {
            return;
        }
        onSubscribeService();
        if (!BaseApplication.INSTANCE.getStartedScan()) {
            checkPermission();
        } else {
            Icon icon = Icon.CONNECT;
            BluetoothLeService bluetoothLeService = this.bluetoothLeService;
            if (bluetoothLeService == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            refreshIcon(icon, bluetoothLeService.getDisconnect_rri());
            Icon icon2 = Icon.OUT_SERVICE;
            BluetoothLeService bluetoothLeService2 = this.bluetoothLeService;
            if (bluetoothLeService2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            refreshIcon(icon2, bluetoothLeService2.getOut_service());
            Icon icon3 = Icon.BATTERY;
            BluetoothLeService bluetoothLeService3 = this.bluetoothLeService;
            if (bluetoothLeService3 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            refreshIcon(icon3, bluetoothLeService3.getLow_battery());
            BluetoothLeService bluetoothLeService4 = this.bluetoothLeService;
            if (bluetoothLeService4 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            if (bluetoothLeService4.getRri_over_limit()) {
                AnimationDrawable animationDrawable = this.bpmAnimation;
                if (animationDrawable == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("bpmAnimation");
                }
                if (!animationDrawable.isRunning()) {
                    AnimationDrawable animationDrawable2 = this.bpmAnimation;
                    if (animationDrawable2 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("bpmAnimation");
                    }
                    animationDrawable2.start();
                }
                getMainModel().setRealEcgMode(1);
            } else {
                AnimationDrawable animationDrawable3 = this.bpmAnimation;
                if (animationDrawable3 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("bpmAnimation");
                }
                animationDrawable3.selectDrawable(0);
                AnimationDrawable animationDrawable4 = this.bpmAnimation;
                if (animationDrawable4 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("bpmAnimation");
                }
                animationDrawable4.stop();
            }
        }
        BluetoothLeService bluetoothLeService5 = this.bluetoothLeService;
        if (bluetoothLeService5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        bluetoothLeService5.getConfig().setMode(getMainModel().getRealEcgMode());
        sendIntervalOk();
        topBottomOk();
        this.serivceInited = true;
    }

    private final void initView(ViewDataBinding binding) {
        Objects.requireNonNull(binding, "null cannot be cast to non-null type jp.co.nipro.cocoron.databinding.FragmentMainBinding");
        FragmentMainBinding fragmentMainBinding = (FragmentMainBinding) binding;
        this.viewBinding = fragmentMainBinding;
        if (fragmentMainBinding == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding.setModel(getMainModel());
        FragmentMainBinding fragmentMainBinding2 = this.viewBinding;
        if (fragmentMainBinding2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding2.setFragment(this);
        FragmentMainBinding fragmentMainBinding3 = this.viewBinding;
        if (fragmentMainBinding3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        Drawable drawable = fragmentMainBinding3.rriBpm.getDrawable();
        Objects.requireNonNull(drawable, "null cannot be cast to non-null type android.graphics.drawable.AnimationDrawable");
        this.bpmAnimation = (AnimationDrawable) drawable;
        FragmentMainBinding fragmentMainBinding4 = this.viewBinding;
        if (fragmentMainBinding4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        CombinedChart combinedChart = fragmentMainBinding4.rriChartView;
        YAxis axisLeft = combinedChart.getAxisLeft();
        Intrinsics.checkNotNullExpressionValue(axisLeft, "axisLeft");
        axisLeft.setAxisMaximum(210.0f);
        YAxis axisLeft2 = combinedChart.getAxisLeft();
        Intrinsics.checkNotNullExpressionValue(axisLeft2, "axisLeft");
        axisLeft2.setAxisMinimum(0.0f);
        combinedChart.getAxisLeft().setLabelCount(8, true);
        combinedChart.getAxisLeft().setDrawGridLines(true);
        YAxis axisRight = combinedChart.getAxisRight();
        Intrinsics.checkNotNullExpressionValue(axisRight, "axisRight");
        axisRight.setEnabled(false);
        XAxis xAxis = combinedChart.getXAxis();
        Intrinsics.checkNotNullExpressionValue(xAxis, "xAxis");
        xAxis.setEnabled(false);
        combinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.BAR});
        YAxis axisLeft3 = combinedChart.getAxisLeft();
        Intrinsics.checkNotNullExpressionValue(axisLeft3, "axisLeft");
        axisLeft3.setTextColor(Config.INSTANCE.getLEFTAXIS_TEXT_COLOR());
        BarDataSet barDataSet = new BarDataSet(CollectionsKt.arrayListOf(new BarEntry(0.0f, new float[]{2.0f, 2.0f, 2.0f, 2.0f})), null);
        barDataSet.setColors(CollectionsKt.listOf((Object[]) new Integer[]{Integer.valueOf(Config.INSTANCE.getBAR_COLOR_1()), Integer.valueOf(Config.INSTANCE.getBAR_COLOR_2()), Integer.valueOf(Config.INSTANCE.getBAR_COLOR_3()), Integer.valueOf(Config.INSTANCE.getBAR_COLOR_4())}));
        barDataSet.setDrawValues(false);
        barDataSet.setDrawIcons(false);
        BarData barData = new BarData(barDataSet);
        barData.setHighlightEnabled(false);
        barData.setDrawValues(false);
        LineDataSet lineDataSet = new LineDataSet(CollectionsKt.arrayListOf(new Entry(0.0f, 2.0f)), null);
        lineDataSet.setColors(CollectionsKt.listOf(Integer.valueOf(Config.INSTANCE.getTOP_LINE_COLOR())));
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawIcons(false);
        lineDataSet.setDrawFilled(false);
        lineDataSet.setDrawCircles(false);
        LineDataSet lineDataSet2 = new LineDataSet(CollectionsKt.arrayListOf(new Entry(0.0f, 2.0f)), null);
        lineDataSet2.setColors(CollectionsKt.listOf(Integer.valueOf(Config.INSTANCE.getBOTTOM_LINE_COLOR())));
        lineDataSet2.setDrawValues(false);
        lineDataSet2.setDrawIcons(false);
        lineDataSet2.setDrawFilled(true);
        lineDataSet2.setDrawCircles(false);
        LineData lineData = new LineData((List<ILineDataSet>) CollectionsKt.listOf((Object[]) new LineDataSet[]{lineDataSet, lineDataSet2}));
        lineData.setHighlightEnabled(false);
        lineData.setDrawValues(false);
        getMainModel().getRriChartViewData().setData(lineData);
        getMainModel().getRriChartViewData().setData(barData);
        Legend legend = combinedChart.getLegend();
        Intrinsics.checkNotNullExpressionValue(legend, "legend");
        legend.setEnabled(false);
        combinedChart.setScaleEnabled(false);
        Intrinsics.checkNotNullExpressionValue(combinedChart, "this");
        combinedChart.setData(getMainModel().getRriChartViewData());
        Description description = (Description) null;
        combinedChart.setDescription(description);
        combinedChart.notifyDataSetChanged();
        combinedChart.invalidate();
        FragmentMainBinding fragmentMainBinding5 = this.viewBinding;
        if (fragmentMainBinding5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        LineChart lineChart = fragmentMainBinding5.ecgChartView;
        lineChart.setRenderer(new NonContinuousLineChartRenderer(lineChart, lineChart.getAnimator(), lineChart.getViewPortHandler()));
        XAxis xAxis2 = lineChart.getXAxis();
        Intrinsics.checkNotNullExpressionValue(xAxis2, "xAxis");
        xAxis2.setEnabled(true);
        XAxis xAxis3 = lineChart.getXAxis();
        Intrinsics.checkNotNullExpressionValue(xAxis3, "xAxis");
        xAxis3.setAxisMinimum(0.0f);
        XAxis xAxis4 = lineChart.getXAxis();
        Intrinsics.checkNotNullExpressionValue(xAxis4, "xAxis");
        xAxis4.setAxisMaximum(437.5f);
        lineChart.getXAxis().setDrawLimitLinesBehindData(true);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setDrawLabels(false);
        lineChart.getAxisLeft().setLabelCount(5, true);
        YAxis axisLeft4 = lineChart.getAxisLeft();
        Intrinsics.checkNotNullExpressionValue(axisLeft4, "axisLeft");
        axisLeft4.setAxisMaximum(10.0f);
        YAxis axisLeft5 = lineChart.getAxisLeft();
        Intrinsics.checkNotNullExpressionValue(axisLeft5, "axisLeft");
        axisLeft5.setAxisMinimum(-10.0f);
        lineChart.getAxisLeft().setDrawGridLines(true);
        YAxis axisLeft6 = lineChart.getAxisLeft();
        Intrinsics.checkNotNullExpressionValue(axisLeft6, "axisLeft");
        axisLeft6.setTextColor(Config.INSTANCE.getLEFTAXIS_TEXT_COLOR());
        YAxis axisLeft7 = lineChart.getAxisLeft();
        Intrinsics.checkNotNullExpressionValue(axisLeft7, "axisLeft");
        axisLeft7.setTextSize(11.0f);
        YAxis axisLeft8 = lineChart.getAxisLeft();
        Intrinsics.checkNotNullExpressionValue(axisLeft8, "axisLeft");
        axisLeft8.setValueFormatter(new ValueFormatter() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$initView$$inlined$apply$lambda$1
            @Override // com.github.mikephil.charting.formatter.ValueFormatter
            public String getAxisLabel(float value, AxisBase axis) {
                MainModel mainModel;
                MainModel mainModel2;
                mainModel = MainFragment.this.getMainModel();
                Integer value2 = mainModel.getMvHolderSelected().getValue();
                if (value2 != null && value2.intValue() == 1 && (value == 3.75f || value == -3.75f)) {
                    return "";
                }
                mainModel2 = MainFragment.this.getMainModel();
                Integer value3 = mainModel2.getMvHolderSelected().getValue();
                if (value3 != null && value3.intValue() == 3 && (value == 1.25f || value == -1.25f)) {
                    return "";
                }
                if (value == 2.5f || value == -2.5f || value == -7.5f || value == -7.5f) {
                    String format = String.format("%3.1f", Arrays.copyOf(new Object[]{Float.valueOf(value)}, 1));
                    Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
                    return format;
                }
                String axisLabel = super.getAxisLabel(value, axis);
                Intrinsics.checkNotNullExpressionValue(axisLabel, "super.getAxisLabel(value, axis)");
                return axisLabel;
            }
        });
        YAxis axisRight2 = lineChart.getAxisRight();
        Intrinsics.checkNotNullExpressionValue(axisRight2, "axisRight");
        axisRight2.setEnabled(false);
        lineChart.setDrawGridBackground(false);
        Legend legend2 = lineChart.getLegend();
        Intrinsics.checkNotNullExpressionValue(legend2, "legend");
        legend2.setEnabled(false);
        LineDataSet lineDataSet3 = new LineDataSet(getMainModel().getEcgChartViewDataSet(), "");
        lineDataSet3.setDrawIcons(false);
        lineDataSet3.setColor(Config.INSTANCE.getECG_DATA_COLOR());
        lineDataSet3.setDrawValues(false);
        lineDataSet3.setDrawFilled(false);
        lineDataSet3.setDrawIcons(false);
        lineDataSet3.setDrawCircles(false);
        lineDataSet3.setFillColor(C0009R.color.black);
        lineDataSet3.setFillAlpha(77);
        lineDataSet3.setHighlightEnabled(false);
        lineChart.setData(new LineData(lineDataSet3));
        lineChart.setDescription(description);
        FragmentMainBinding fragmentMainBinding6 = this.viewBinding;
        if (fragmentMainBinding6 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding6.flickArea.setOnTouchListener(new FlickListener(this.fickListener, 0.0f, 2, null));
        FragmentMainBinding fragmentMainBinding7 = this.viewBinding;
        if (fragmentMainBinding7 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding7.ecgChartView.setOnTouchListener(new FlickListener(this.fickListener, 0.0f, 2, null));
        FragmentMainBinding fragmentMainBinding8 = this.viewBinding;
        if (fragmentMainBinding8 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding8.rriChartView.setOnTouchListener(new FlickListener(this.fickListener, 0.0f, 2, null));
        FragmentMainBinding fragmentMainBinding9 = this.viewBinding;
        if (fragmentMainBinding9 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding9.rriHolder.setOnTouchListener(new FlickListener(this.fickListener, 0.0f, 2, null));
        FragmentMainBinding fragmentMainBinding10 = this.viewBinding;
        if (fragmentMainBinding10 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding10.mvHolder.setOnTouchListener(new FlickListener(this.fickListener, 0.0f, 2, null));
        FragmentMainBinding fragmentMainBinding11 = this.viewBinding;
        if (fragmentMainBinding11 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding11.secHolder.setOnTouchListener(new FlickListener(this.fickListener, 0.0f, 2, null));
        FragmentMainBinding fragmentMainBinding12 = this.viewBinding;
        if (fragmentMainBinding12 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding12.sendIntervalPickerArea.setOnTouchListener(new FlickListener(this.fickListener, 0.0f, 2, null));
        FragmentMainBinding fragmentMainBinding13 = this.viewBinding;
        if (fragmentMainBinding13 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding13.topBottomPickerArea.setOnTouchListener(new FlickListener(this.fickListener, 0.0f, 2, null));
        FragmentMainBinding fragmentMainBinding14 = this.viewBinding;
        if (fragmentMainBinding14 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding14.sendIntervalPicker.setCyclic(false);
        FragmentMainBinding fragmentMainBinding15 = this.viewBinding;
        if (fragmentMainBinding15 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding15.sendIntervalPicker.setTypeface(Typeface.DEFAULT);
        FragmentMainBinding fragmentMainBinding16 = this.viewBinding;
        if (fragmentMainBinding16 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding16.topPicker.setCyclic(false);
        FragmentMainBinding fragmentMainBinding17 = this.viewBinding;
        if (fragmentMainBinding17 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding17.topPicker.setTypeface(Typeface.DEFAULT);
        FragmentMainBinding fragmentMainBinding18 = this.viewBinding;
        if (fragmentMainBinding18 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding18.topPicker.setItemsVisibleCount(5);
        FragmentMainBinding fragmentMainBinding19 = this.viewBinding;
        if (fragmentMainBinding19 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding19.bottomPicker.setCyclic(false);
        FragmentMainBinding fragmentMainBinding20 = this.viewBinding;
        if (fragmentMainBinding20 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding20.bottomPicker.setTypeface(Typeface.DEFAULT);
        FragmentMainBinding fragmentMainBinding21 = this.viewBinding;
        if (fragmentMainBinding21 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        fragmentMainBinding21.bottomPicker.setItemsVisibleCount(5);
    }

    public final FlickListener.Listener getFickListener() {
        return this.fickListener;
    }

    @Override // jp.co.nipro.cocoron.ui.fragment.BaseFragment
    protected void onSubscribeUi(ViewDataBinding binding) {
        Intrinsics.checkNotNullParameter(binding, "binding");
        super.onSubscribeUi(binding);
        final FragmentMainBinding fragmentMainBinding = (FragmentMainBinding) binding;
        getMainModel().getEcgChartViewXMax().observe(getViewLifecycleOwner(), new Observer<Float>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$onSubscribeUi$1
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Float it) {
                LineChart lineChart = FragmentMainBinding.this.ecgChartView;
                Intrinsics.checkNotNullExpressionValue(lineChart, "viewBinding.ecgChartView");
                XAxis xAxis = lineChart.getXAxis();
                Intrinsics.checkNotNullExpressionValue(xAxis, "viewBinding.ecgChartView.xAxis");
                Intrinsics.checkNotNullExpressionValue(it, "it");
                xAxis.setAxisMaximum(it.floatValue());
            }
        });
        getMainModel().getEcgChartViewXMin().observe(getViewLifecycleOwner(), new Observer<Float>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$onSubscribeUi$2
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Float it) {
                LineChart lineChart = FragmentMainBinding.this.ecgChartView;
                Intrinsics.checkNotNullExpressionValue(lineChart, "viewBinding.ecgChartView");
                XAxis xAxis = lineChart.getXAxis();
                Intrinsics.checkNotNullExpressionValue(xAxis, "viewBinding.ecgChartView.xAxis");
                Intrinsics.checkNotNullExpressionValue(it, "it");
                xAxis.setAxisMinimum(it.floatValue());
            }
        });
        getMainModel().getEvent().observe(getViewLifecycleOwner(), new EventObserver(new Function1<BaseModel.EventParam, Unit>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$onSubscribeUi$3
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(BaseModel.EventParam eventParam) {
                invoke2(eventParam);
                return Unit.INSTANCE;
            }

            /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2(BaseModel.EventParam it) {
                Intrinsics.checkNotNullParameter(it, "it");
                String name = it.getName();
                switch (name.hashCode()) {
                    case -973489578:
                        if (name.equals("removeEcgLimit")) {
                            Object value = it.getValue();
                            Objects.requireNonNull(value, "null cannot be cast to non-null type kotlin.Float");
                            float floatValue = ((Float) value).floatValue();
                            LineChart lineChart = fragmentMainBinding.ecgChartView;
                            Intrinsics.checkNotNullExpressionValue(lineChart, "viewBinding.ecgChartView");
                            XAxis xAxis = lineChart.getXAxis();
                            Intrinsics.checkNotNullExpressionValue(xAxis, "viewBinding.ecgChartView.xAxis");
                            List<LimitLine> limitLines = xAxis.getLimitLines();
                            Intrinsics.checkNotNullExpressionValue(limitLines, "viewBinding.ecgChartView.xAxis.limitLines");
                            for (LimitLine it2 : limitLines) {
                                Intrinsics.checkNotNullExpressionValue(it2, "it");
                                if (it2.getLimit() == floatValue) {
                                    LineChart lineChart2 = fragmentMainBinding.ecgChartView;
                                    Intrinsics.checkNotNullExpressionValue(lineChart2, "viewBinding.ecgChartView");
                                    lineChart2.getXAxis().removeLimitLine(it2);
                                    break;
                                }
                            }
                            break;
                        }
                        break;
                    case -905800561:
                        if (name.equals("setSec")) {
                            Object value2 = it.getValue();
                            Objects.requireNonNull(value2, "null cannot be cast to non-null type kotlin.Float");
                            float floatValue2 = ((Float) value2).floatValue();
                            LineChart lineChart3 = fragmentMainBinding.ecgChartView;
                            Intrinsics.checkNotNullExpressionValue(lineChart3, "viewBinding.ecgChartView");
                            XAxis xAxis2 = lineChart3.getXAxis();
                            Intrinsics.checkNotNullExpressionValue(xAxis2, "viewBinding.ecgChartView.xAxis");
                            xAxis2.setAxisMaximum(125 * floatValue2);
                            MainFragment.this.changeDrawTimer();
                            LineChart lineChart4 = fragmentMainBinding.ecgChartView;
                            Intrinsics.checkNotNullExpressionValue(lineChart4, "viewBinding.ecgChartView");
                            LineData lineData = (LineData) lineChart4.getData();
                            if (lineData != null) {
                                lineData.notifyDataChanged();
                            }
                            fragmentMainBinding.ecgChartView.notifyDataSetChanged();
                            fragmentMainBinding.ecgChartView.invalidate();
                            break;
                        }
                        break;
                    case -353317517:
                        if (name.equals("addEcgLimit")) {
                            Object value3 = it.getValue();
                            Objects.requireNonNull(value3, "null cannot be cast to non-null type kotlin.Float");
                            LimitLine limitLine = new LimitLine(((Float) value3).floatValue());
                            limitLine.setLineWidth(1.0f);
                            limitLine.setLineColor(Config.INSTANCE.getTRIGGER_LINE_COLOR());
                            LineChart lineChart5 = fragmentMainBinding.ecgChartView;
                            Intrinsics.checkNotNullExpressionValue(lineChart5, "viewBinding.ecgChartView");
                            lineChart5.getXAxis().addLimitLine(limitLine);
                            break;
                        }
                        break;
                    case -321847986:
                        if (name.equals("refreshRri")) {
                            CombinedChart combinedChart = fragmentMainBinding.rriChartView;
                            Intrinsics.checkNotNullExpressionValue(combinedChart, "viewBinding.rriChartView");
                            CombinedData combinedData = (CombinedData) combinedChart.getData();
                            if (combinedData != null) {
                                combinedData.notifyDataChanged();
                            }
                            fragmentMainBinding.rriChartView.notifyDataSetChanged();
                            fragmentMainBinding.rriChartView.invalidate();
                            break;
                        }
                        break;
                    case -18075244:
                        if (name.equals("setRriLimit")) {
                            CombinedChart combinedChart2 = fragmentMainBinding.rriChartView;
                            Intrinsics.checkNotNullExpressionValue(combinedChart2, "viewBinding.rriChartView");
                            combinedChart2.getAxisLeft().removeAllLimitLines();
                            if (Config.INSTANCE.getBottom() >= Config.INSTANCE.getBOTTOM_MIN()) {
                                LimitLine limitLine2 = new LimitLine(Config.INSTANCE.getBottom());
                                limitLine2.setLineWidth(1.0f);
                                limitLine2.setLineColor(Config.INSTANCE.getTOP_LINE_COLOR());
                                limitLine2.enableDashedLine(3.0f, 3.0f, 0.0f);
                                CombinedChart combinedChart3 = fragmentMainBinding.rriChartView;
                                Intrinsics.checkNotNullExpressionValue(combinedChart3, "viewBinding.rriChartView");
                                combinedChart3.getAxisLeft().addLimitLine(limitLine2);
                            }
                            if (Config.INSTANCE.getTop() >= Config.INSTANCE.getTOP_MIN()) {
                                LimitLine limitLine3 = new LimitLine(Config.INSTANCE.getTop());
                                limitLine3.setLineWidth(1.0f);
                                limitLine3.setLineColor(Config.INSTANCE.getBOTTOM_LINE_COLOR());
                                limitLine3.enableDashedLine(3.0f, 3.0f, 0.0f);
                                CombinedChart combinedChart4 = fragmentMainBinding.rriChartView;
                                Intrinsics.checkNotNullExpressionValue(combinedChart4, "viewBinding.rriChartView");
                                combinedChart4.getAxisLeft().addLimitLine(limitLine3);
                                break;
                            }
                        }
                        break;
                    case 109327787:
                        if (name.equals("setMv")) {
                            Object value4 = it.getValue();
                            Objects.requireNonNull(value4, "null cannot be cast to non-null type kotlin.Float");
                            float floatValue3 = ((Float) value4).floatValue();
                            LineChart lineChart6 = fragmentMainBinding.ecgChartView;
                            Intrinsics.checkNotNullExpressionValue(lineChart6, "viewBinding.ecgChartView");
                            YAxis axisLeft = lineChart6.getAxisLeft();
                            Intrinsics.checkNotNullExpressionValue(axisLeft, "viewBinding.ecgChartView.axisLeft");
                            axisLeft.setAxisMaximum(floatValue3);
                            LineChart lineChart7 = fragmentMainBinding.ecgChartView;
                            Intrinsics.checkNotNullExpressionValue(lineChart7, "viewBinding.ecgChartView");
                            YAxis axisLeft2 = lineChart7.getAxisLeft();
                            Intrinsics.checkNotNullExpressionValue(axisLeft2, "viewBinding.ecgChartView.axisLeft");
                            axisLeft2.setAxisMinimum(-floatValue3);
                            MainFragment.this.changeDrawTimer();
                            LineChart lineChart8 = fragmentMainBinding.ecgChartView;
                            Intrinsics.checkNotNullExpressionValue(lineChart8, "viewBinding.ecgChartView");
                            LineData lineData2 = (LineData) lineChart8.getData();
                            if (lineData2 != null) {
                                lineData2.notifyDataChanged();
                            }
                            fragmentMainBinding.ecgChartView.notifyDataSetChanged();
                            fragmentMainBinding.ecgChartView.invalidate();
                            break;
                        }
                        break;
                    case 138294483:
                        if (name.equals("setRriXMax")) {
                            Object value5 = it.getValue();
                            Objects.requireNonNull(value5, "null cannot be cast to non-null type kotlin.Long");
                            long longValue = ((Long) value5).longValue();
                            CombinedChart combinedChart5 = fragmentMainBinding.rriChartView;
                            Intrinsics.checkNotNullExpressionValue(combinedChart5, "viewBinding.rriChartView");
                            XAxis xAxis3 = combinedChart5.getXAxis();
                            Intrinsics.checkNotNullExpressionValue(xAxis3, "viewBinding.rriChartView.xAxis");
                            xAxis3.setAxisMaximum(longValue - 0.5f);
                            break;
                        }
                        break;
                    case 138294721:
                        if (name.equals("setRriXMin")) {
                            Object value6 = it.getValue();
                            Objects.requireNonNull(value6, "null cannot be cast to non-null type kotlin.Long");
                            long longValue2 = ((Long) value6).longValue();
                            CombinedChart combinedChart6 = fragmentMainBinding.rriChartView;
                            Intrinsics.checkNotNullExpressionValue(combinedChart6, "viewBinding.rriChartView");
                            XAxis xAxis4 = combinedChart6.getXAxis();
                            Intrinsics.checkNotNullExpressionValue(xAxis4, "viewBinding.rriChartView.xAxis");
                            xAxis4.setAxisMinimum(longValue2 - 1.5f);
                            break;
                        }
                        break;
                    case 558640509:
                        if (name.equals("setRriMax")) {
                            Object value7 = it.getValue();
                            Objects.requireNonNull(value7, "null cannot be cast to non-null type kotlin.Int");
                            int intValue = ((Integer) value7).intValue();
                            if (intValue <= 90) {
                                CombinedChart combinedChart7 = fragmentMainBinding.rriChartView;
                                Intrinsics.checkNotNullExpressionValue(combinedChart7, "viewBinding.rriChartView");
                                YAxis axisLeft3 = combinedChart7.getAxisLeft();
                                Intrinsics.checkNotNullExpressionValue(axisLeft3, "viewBinding.rriChartView.axisLeft");
                                axisLeft3.setAxisMaximum(105.0f);
                                break;
                            } else if (intValue <= 120) {
                                CombinedChart combinedChart8 = fragmentMainBinding.rriChartView;
                                Intrinsics.checkNotNullExpressionValue(combinedChart8, "viewBinding.rriChartView");
                                YAxis axisLeft4 = combinedChart8.getAxisLeft();
                                Intrinsics.checkNotNullExpressionValue(axisLeft4, "viewBinding.rriChartView.axisLeft");
                                axisLeft4.setAxisMaximum(140.0f);
                                break;
                            } else if (intValue <= 150) {
                                CombinedChart combinedChart9 = fragmentMainBinding.rriChartView;
                                Intrinsics.checkNotNullExpressionValue(combinedChart9, "viewBinding.rriChartView");
                                YAxis axisLeft5 = combinedChart9.getAxisLeft();
                                Intrinsics.checkNotNullExpressionValue(axisLeft5, "viewBinding.rriChartView.axisLeft");
                                axisLeft5.setAxisMaximum(175.0f);
                                break;
                            } else if (intValue <= 180) {
                                CombinedChart combinedChart10 = fragmentMainBinding.rriChartView;
                                Intrinsics.checkNotNullExpressionValue(combinedChart10, "viewBinding.rriChartView");
                                YAxis axisLeft6 = combinedChart10.getAxisLeft();
                                Intrinsics.checkNotNullExpressionValue(axisLeft6, "viewBinding.rriChartView.axisLeft");
                                axisLeft6.setAxisMaximum(210.0f);
                                break;
                            } else if (intValue <= 240) {
                                CombinedChart combinedChart11 = fragmentMainBinding.rriChartView;
                                Intrinsics.checkNotNullExpressionValue(combinedChart11, "viewBinding.rriChartView");
                                YAxis axisLeft7 = combinedChart11.getAxisLeft();
                                Intrinsics.checkNotNullExpressionValue(axisLeft7, "viewBinding.rriChartView.axisLeft");
                                axisLeft7.setAxisMaximum(280.0f);
                                break;
                            } else {
                                CombinedChart combinedChart12 = fragmentMainBinding.rriChartView;
                                Intrinsics.checkNotNullExpressionValue(combinedChart12, "viewBinding.rriChartView");
                                YAxis axisLeft8 = combinedChart12.getAxisLeft();
                                Intrinsics.checkNotNullExpressionValue(axisLeft8, "viewBinding.rriChartView.axisLeft");
                                axisLeft8.setAxisMaximum(350.0f);
                                break;
                            }
                        }
                        break;
                    case 790272508:
                        if (name.equals("clearEcg")) {
                            LineChart lineChart9 = fragmentMainBinding.ecgChartView;
                            Intrinsics.checkNotNullExpressionValue(lineChart9, "viewBinding.ecgChartView");
                            lineChart9.getXAxis().removeAllLimitLines();
                            fragmentMainBinding.ecgChartView.notifyDataSetChanged();
                            fragmentMainBinding.ecgChartView.invalidate();
                            break;
                        }
                        break;
                }
            }
        }));
    }

    public final void refreshIcon(Icon icon, boolean animated) {
        ImageView imageView;
        Intrinsics.checkNotNullParameter(icon, "icon");
        int i = WhenMappings.$EnumSwitchMapping$0[icon.ordinal()];
        if (i == 1) {
            FragmentMainBinding fragmentMainBinding = this.viewBinding;
            if (fragmentMainBinding == null) {
                Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
            }
            imageView = fragmentMainBinding.connectIcon;
        } else if (i == 2) {
            FragmentMainBinding fragmentMainBinding2 = this.viewBinding;
            if (fragmentMainBinding2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
            }
            imageView = fragmentMainBinding2.outServiceIcon;
        } else {
            if (i != 3) {
                throw new NoWhenBranchMatchedException();
            }
            FragmentMainBinding fragmentMainBinding3 = this.viewBinding;
            if (fragmentMainBinding3 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
            }
            imageView = fragmentMainBinding3.batteryLabel;
        }
        Intrinsics.checkNotNullExpressionValue(imageView, "when(icon) {\n           …ng.batteryLabel\n        }");
        if (animated) {
            ImageView imageView2 = imageView;
            if (ExtensionKt.getFlashing(imageView2)) {
                return;
            }
            ExtensionKt.setFlashing(imageView2, true);
            ExtensionKt.refresh(imageView2);
            return;
        }
        ExtensionKt.setFlashing(imageView, false);
    }

    public final EventObserver<BaseModel.EventParam> getObserver() {
        return this.observer;
    }

    public final void setObserver(EventObserver<BaseModel.EventParam> eventObserver) {
        this.observer = eventObserver;
    }

    public final void onSubscribeService() {
        Log.d(this.TAG, "onSubscribeService");
        EventObserver<BaseModel.EventParam> eventObserver = new EventObserver<>(new MainFragment$onSubscribeService$1(this));
        this.observer = eventObserver;
        if (eventObserver != null) {
            BluetoothLeService bluetoothLeService = this.bluetoothLeService;
            if (bluetoothLeService == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            bluetoothLeService.getEvent().observeForever(eventObserver);
        }
    }

    public final void changeDrawTimer() {
        CountDownTimer countDownTimer = this.drawTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        this.drawTimer = (CountDownTimer) null;
        final Ref.LongRef longRef = new Ref.LongRef();
        Integer value = getMainModel().getSecHolderSelected().getValue();
        long j = 100;
        if ((value == null || value.intValue() != 0) && (value == null || value.intValue() != 1)) {
            if (value != null && value.intValue() == 2) {
                j = 200;
            } else if (value != null && value.intValue() == 3) {
                j = 500;
            }
        }
        longRef.element = j;
        final long j2 = 10000 * longRef.element;
        final long j3 = longRef.element;
        CountDownTimer countDownTimer2 = new CountDownTimer(j2, j3) { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$changeDrawTimer$1
            @Override // android.os.CountDownTimer
            public void onTick(long millisUntilFinished) {
                MainModel mainModel;
                MainModel mainModel2;
                MainModel mainModel3;
                mainModel = MainFragment.this.getMainModel();
                mainModel.updateEcgDaytime();
                mainModel2 = MainFragment.this.getMainModel();
                if (mainModel2.getDrawEcg()) {
                    MainFragment.this.getViewBinding().ecgChartView.notifyDataSetChanged();
                    MainFragment.this.getViewBinding().ecgChartView.invalidate();
                    mainModel3 = MainFragment.this.getMainModel();
                    mainModel3.setDrawEcg(false);
                }
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                MainFragment.this.changeDrawTimer();
            }
        };
        this.drawTimer = countDownTimer2;
        if (countDownTimer2 != null) {
            countDownTimer2.start();
        }
    }

    public final void checkBleStatusTimer() {
        CountDownTimer countDownTimer = this.checkTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        this.checkTimer = (CountDownTimer) null;
        final Ref.LongRef longRef = new Ref.LongRef();
        longRef.element = Config.INSTANCE.getBLE_CHECK_INTERVAL();
        final long j = 10000 * longRef.element;
        final long j2 = longRef.element;
        CountDownTimer countDownTimer2 = new CountDownTimer(j, j2) { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$checkBleStatusTimer$1
            @Override // android.os.CountDownTimer
            public void onTick(long millisUntilFinished) {
                boolean isEnabled = MainFragment.this.getBluetoothLeService().getBluetoothAdapter().isEnabled();
                if (isEnabled && MainFragment.this.getBleStatus() == 2) {
                    FileRecorder.INSTANCE.getInstance().writeText("CHECKED BLE ON");
                    BluetoothDevice connectedDevice = MainFragment.this.getBluetoothLeService().getConnectedDevice();
                    if (connectedDevice != null) {
                        MainFragment.this.getBluetoothLeService().connectDevice(connectedDevice);
                    }
                } else if (!isEnabled && MainFragment.this.getBleStatus() == 1) {
                    FileRecorder.INSTANCE.getInstance().writeText("CHECKED BLE OFF");
                    MainFragment.this.disconnectionNotification();
                    Job disConnectJob = MainFragment.this.getDisConnectJob();
                    if (disConnectJob != null) {
                        Job.DefaultImpls.cancel$default(disConnectJob, (CancellationException) null, 1, (Object) null);
                    }
                    Job job = (Job) null;
                    MainFragment.this.setDisConnectJob(job);
                    Job workItemDisconnect = MainFragment.this.getBluetoothLeService().getWorkItemDisconnect();
                    if (workItemDisconnect != null) {
                        Job.DefaultImpls.cancel$default(workItemDisconnect, (CancellationException) null, 1, (Object) null);
                    }
                    MainFragment.this.getBluetoothLeService().setWorkItemDisconnect(job);
                }
                MainFragment.this.setBleStatus(isEnabled ? 1 : 2);
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                MainFragment.this.checkBleStatusTimer();
            }
        };
        this.checkTimer = countDownTimer2;
        if (countDownTimer2 != null) {
            countDownTimer2.start();
        }
        FileRecorder.INSTANCE.getInstance().writeText("START CHECKED TIMER");
    }

    public final void disconnectionNotification() {
        FragmentMainBinding fragmentMainBinding = this.viewBinding;
        if (fragmentMainBinding == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        ImageView imageView = fragmentMainBinding.connectIcon;
        Intrinsics.checkNotNullExpressionValue(imageView, "viewBinding.connectIcon");
        if (ExtensionKt.getFlashing(imageView)) {
            return;
        }
        FragmentMainBinding fragmentMainBinding2 = this.viewBinding;
        if (fragmentMainBinding2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        ImageView imageView2 = fragmentMainBinding2.connectIcon;
        Intrinsics.checkNotNullExpressionValue(imageView2, "viewBinding.connectIcon");
        ExtensionKt.setFlashing(imageView2, true);
        FragmentMainBinding fragmentMainBinding3 = this.viewBinding;
        if (fragmentMainBinding3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        ImageView imageView3 = fragmentMainBinding3.connectIcon;
        Intrinsics.checkNotNullExpressionValue(imageView3, "viewBinding.connectIcon");
        ExtensionKt.refresh(imageView3);
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(getContext(), (Class<?>) MainActivity.class);
            intent.addFlags(67108864);
            PendingIntent activity = PendingIntent.getActivity(getContext(), 0, intent, BasicMeasure.EXACTLY);
            Object systemService = context.getSystemService("notification");
            Objects.requireNonNull(systemService, "null cannot be cast to non-null type android.app.NotificationManager");
            NotificationManager notificationManager = (NotificationManager) systemService;
            if (Build.VERSION.SDK_INT >= 26 && notificationManager.getNotificationChannel(this.CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(new NotificationChannel(this.CHANNEL_ID, this.CHANNEL_NAME, 4));
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, this.CHANNEL_ID);
            builder.setSmallIcon(C0009R.mipmap.ic_launcher).setContentTitle("心電計との接続が切れました").setDefaults(3).setAutoCancel(true).setContentIntent(activity);
            notificationManager.notify(0, builder.build());
        }
    }

    public final void disconnectDevice() {
        AnimationDrawable animationDrawable = this.bpmAnimation;
        if (animationDrawable == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bpmAnimation");
        }
        animationDrawable.selectDrawable(0);
        AnimationDrawable animationDrawable2 = this.bpmAnimation;
        if (animationDrawable2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bpmAnimation");
        }
        animationDrawable2.stop();
        FragmentMainBinding fragmentMainBinding = this.viewBinding;
        if (fragmentMainBinding == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        ImageView imageView = fragmentMainBinding.connectIcon;
        Intrinsics.checkNotNullExpressionValue(imageView, "viewBinding.connectIcon");
        ExtensionKt.setFlashing(imageView, false);
        Job job = this.disConnectJob;
        if (job != null) {
            Job.DefaultImpls.cancel$default(job, (CancellationException) null, 1, (Object) null);
        }
        Job job2 = (Job) null;
        this.disConnectJob = job2;
        BluetoothLeService bluetoothLeService = this.bluetoothLeService;
        if (bluetoothLeService == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        Job workItemDisconnect = bluetoothLeService.getWorkItemDisconnect();
        if (workItemDisconnect != null) {
            Job.DefaultImpls.cancel$default(workItemDisconnect, (CancellationException) null, 1, (Object) null);
        }
        BluetoothLeService bluetoothLeService2 = this.bluetoothLeService;
        if (bluetoothLeService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        bluetoothLeService2.setWorkItemDisconnect(job2);
        CountDownTimer countDownTimer = this.checkTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        this.checkTimer = (CountDownTimer) null;
        this.bleStatus = 0;
        FragmentMainBinding fragmentMainBinding2 = this.viewBinding;
        if (fragmentMainBinding2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        ImageView imageView2 = fragmentMainBinding2.batteryLabel;
        Intrinsics.checkNotNullExpressionValue(imageView2, "viewBinding.batteryLabel");
        ExtensionKt.setFlashing(imageView2, false);
        getMainModel().getDeviceName().setValue(" ");
        getMainModel().getRriStr().setValue("-");
        BluetoothLeService bluetoothLeService3 = this.bluetoothLeService;
        if (bluetoothLeService3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        bluetoothLeService3.disConnectCurrDevice();
        BluetoothLeService bluetoothLeService4 = this.bluetoothLeService;
        if (bluetoothLeService4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        bluetoothLeService4.setSended_config(false);
        BluetoothLeService bluetoothLeService5 = this.bluetoothLeService;
        if (bluetoothLeService5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        bluetoothLeService5.setSended_time(false);
        BluetoothLeService bluetoothLeService6 = this.bluetoothLeService;
        if (bluetoothLeService6 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        bluetoothLeService6.setOut_service(false);
        BluetoothLeService bluetoothLeService7 = this.bluetoothLeService;
        if (bluetoothLeService7 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        bluetoothLeService7.getConfig().setMode(0);
        Config.INSTANCE.setConnectName("");
        if (getMainModel().getRealTimeMode()) {
            getMainModel().getEcgData().clear();
            getMainModel().clearEcgView();
        } else {
            getMainModel().clockClicked();
        }
        getMainModel().setRealEcgMode(0);
    }

    @Override // jp.co.nipro.cocoron.ui.fragment.BaseFragment, androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        Log.d(this.TAG, "onResume");
        BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new MainFragment$onResume$1(this, null), 2, null);
        getMainModel().getRriStr().setValue(BaseApplication.INSTANCE.getReceived_rri());
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        Log.d(this.TAG, "onDestroyView");
        super.onDestroyView();
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        Log.d(this.TAG, "onDestroy!");
        super.onDestroy();
    }

    @Override // androidx.fragment.app.Fragment
    public void onStart() {
        super.onStart();
        getMainModel().getEcgChartViewXMax().removeObservers(getViewLifecycleOwner());
        getMainModel().getEcgChartViewXMin().removeObservers(getViewLifecycleOwner());
        getMainModel().getEvent().removeObservers(getViewLifecycleOwner());
        EventObserver<BaseModel.EventParam> eventObserver = this.observer;
        if (eventObserver != null) {
            BluetoothLeService bluetoothLeService = this.bluetoothLeService;
            if (bluetoothLeService == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            bluetoothLeService.getEvent().removeObserver(eventObserver);
        }
        FragmentMainBinding fragmentMainBinding = this.viewBinding;
        if (fragmentMainBinding == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        onSubscribeUi(fragmentMainBinding);
        if (this.bluetoothLeService != null) {
            onSubscribeService();
        }
        Log.d(this.TAG, "onStart!");
    }

    @Override // jp.co.nipro.cocoron.ui.fragment.BaseFragment, androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        Log.d(this.TAG, "onPause!");
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
        getMainModel().getEcgChartViewXMax().removeObservers(getViewLifecycleOwner());
        getMainModel().getEcgChartViewXMin().removeObservers(getViewLifecycleOwner());
        getMainModel().getEvent().removeObservers(getViewLifecycleOwner());
        EventObserver<BaseModel.EventParam> eventObserver = this.observer;
        if (eventObserver != null) {
            BluetoothLeService bluetoothLeService = this.bluetoothLeService;
            if (bluetoothLeService == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            bluetoothLeService.getEvent().removeObserver(eventObserver);
        }
        Log.d(this.TAG, "onStop!");
    }

    public final void setEcgMode() {
        getMainModel().setRealEcgMode(getMainModel().getRealEcgMode() == 0 ? 1 : 0);
        BluetoothLeService bluetoothLeService = this.bluetoothLeService;
        if (bluetoothLeService == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        bluetoothLeService.getConfig().setMode(getMainModel().getRealEcgMode());
        if (getMainModel().getRealEcgMode() == 0) {
            BluetoothLeService bluetoothLeService2 = this.bluetoothLeService;
            if (bluetoothLeService2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            bluetoothLeService2.setEcgMode(0);
            return;
        }
        BluetoothLeService bluetoothLeService3 = this.bluetoothLeService;
        if (bluetoothLeService3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        bluetoothLeService3.setEcgMode(1);
        if (getMainModel().getEcgData().getEcg().size() > 1250) {
            getMainModel().getEcgData().setEcg(getMainModel().getEcgData().getEcg().subList(0, 1250));
        }
    }

    public final void sendIntervalOk() {
        getMainModel().getPickerShow().setValue(0);
        getMainModel().getPickerTimer().removeCallbacks(getMainModel().getPickerCancel());
        Integer value = getMainModel().getSendInterval().getValue();
        if (value != null) {
            Intrinsics.checkNotNullExpressionValue(value, "mainModel.sendInterval.value ?: run { return }");
            int intValue = value.intValue();
            if (intValue > 0) {
                getMainModel().getSendIntervalPrev().setValue(Config.INSTANCE.getSEND_INTERVAL_VALUE()[intValue - 1]);
            } else {
                getMainModel().getSendIntervalPrev().setValue("");
            }
            if (intValue < Config.INSTANCE.getSEND_INTERVAL_VALUE().length - 1) {
                getMainModel().getSendIntervalNext().setValue(Config.INSTANCE.getSEND_INTERVAL_VALUE()[intValue + 1]);
            } else {
                getMainModel().getSendIntervalNext().setValue("");
            }
            getMainModel().getSendIntervalCurr().setValue(Config.INSTANCE.getSEND_INTERVAL_VALUE()[intValue]);
            Config.INSTANCE.setSendInterval(intValue);
            BluetoothLeService bluetoothLeService = this.bluetoothLeService;
            if (bluetoothLeService == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            bluetoothLeService.getConfig().setInterval((Config.INSTANCE.getSEND_INTERVAL_VALUE().length - intValue) - 1);
            BluetoothLeService bluetoothLeService2 = this.bluetoothLeService;
            if (bluetoothLeService2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            bluetoothLeService2.setSendInterval((Config.INSTANCE.getSEND_INTERVAL_VALUE().length - intValue) - 1);
        }
    }

    public final void topBottomOk() {
        getMainModel().getPickerShow().setValue(0);
        getMainModel().getPickerTimer().removeCallbacks(getMainModel().getPickerCancel());
        Integer value = getMainModel().getTop().getValue();
        if (value != null) {
            Intrinsics.checkNotNullExpressionValue(value, "mainModel.top.value ?: run { return }");
            int intValue = value.intValue();
            Integer value2 = getMainModel().getBottom().getValue();
            if (value2 != null) {
                Intrinsics.checkNotNullExpressionValue(value2, "mainModel.bottom.value ?: run { return }");
                int intValue2 = value2.intValue();
                if (intValue > intValue2 && intValue != (Config.INSTANCE.getTOP_MAX() - Config.INSTANCE.getTOP_MIN()) + 1 && intValue2 != (Config.INSTANCE.getBOTTOM_MAX() - Config.INSTANCE.getBOTTOM_MIN()) + 1) {
                    getMainModel().getDialogEvent().setValue(new Event<>(new BaseModel.DialogInfo("下限値が上限値を超えています", null, "閉じる", null, null, null, 56, null)));
                    return;
                }
                if (intValue > 0) {
                    getMainModel().getRriTopPrev().setValue(getMainModel().getTopValues().get(intValue - 1));
                } else {
                    getMainModel().getRriTopPrev().setValue("");
                }
                if (intValue < getMainModel().getTopValues().size() - 1) {
                    getMainModel().getRriTopNext().setValue(getMainModel().getTopValues().get(intValue + 1));
                } else {
                    getMainModel().getRriTopNext().setValue("");
                }
                Config.INSTANCE.setTop(Config.INSTANCE.getTOP_MAX() - intValue);
                getMainModel().getRriTopCurr().setValue(getMainModel().getTopValues().get(intValue));
                if (intValue2 > 0) {
                    getMainModel().getRriBottomPrev().setValue(getMainModel().getBottomValues().get(intValue2 - 1));
                } else {
                    getMainModel().getRriBottomPrev().setValue("");
                }
                if (intValue2 < getMainModel().getBottomValues().size() - 1) {
                    getMainModel().getRriBottomNext().setValue(getMainModel().getBottomValues().get(intValue2 + 1));
                } else {
                    getMainModel().getRriBottomNext().setValue("");
                }
                Config.INSTANCE.setBottom(Config.INSTANCE.getBOTTOM_MAX() - intValue2);
                getMainModel().getRriBottomCurr().setValue(getMainModel().getBottomValues().get(intValue2));
                getMainModel().setRriData(true);
            }
        }
    }

    public final void aboutDialog() {
        try {
            InputStream open = BaseApplication.INSTANCE.getContext().getAssets().open("license.txt");
            Intrinsics.checkNotNullExpressionValue(open, "assetManager.open(\"license.txt\")");
            Reader inputStreamReader = new InputStreamReader(open, Charsets.UTF_8);
            BufferedReader bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
            Throwable th = (Throwable) null;
            try {
                String readText = TextStreamsKt.readText(bufferedReader);
                CloseableKt.closeFinally(bufferedReader, th);
                Intrinsics.checkNotNullExpressionValue(BaseApplication.INSTANCE.getContext().getPackageManager().getPackageInfo(BaseApplication.INSTANCE.getContext().getPackageName(), 0), "BaseApplication.context.…text.getPackageName(), 0)");
                BluetoothLeService bluetoothLeService = this.bluetoothLeService;
                if (bluetoothLeService == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
                }
                BatteryLevelCharacteristicValue batteryLevel = bluetoothLeService.getBatteryLevel();
                StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
                Object[] objArr = new Object[5];
                objArr[0] = "1.0.2";
                objArr[1] = Integer.valueOf(batteryLevel != null ? batteryLevel.getMajorVer() : 0);
                objArr[2] = Integer.valueOf(batteryLevel != null ? batteryLevel.getMinorVer() : 0);
                objArr[3] = Integer.valueOf(batteryLevel != null ? batteryLevel.getBugfixVer() : 0);
                objArr[4] = Config.INSTANCE.getConnectName();
                String format = String.format(readText, Arrays.copyOf(objArr, 5));
                Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(format, *args)");
                getMainModel().getDialogEvent().setValue(new Event<>(new BaseModel.DialogInfo("Cocoron", format, "閉じる", null, null, null, 56, null)));
            } finally {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v1, types: [T, java.lang.String] */
    public final void changeDevice() {
        final Ref.ObjectRef objectRef = new Ref.ObjectRef();
        objectRef.element = Config.INSTANCE.getConnectName();
        if (((String) objectRef.element).length() == 0) {
            disconnectDevice();
            searchDevice();
            FileRecorder.INSTANCE.getInstance().writeText("DO CHANGE DEVICE");
            return;
        }
        final Context it = getContext();
        if (it != null) {
            BaseModel.DialogInfo dialogInfo = new BaseModel.DialogInfo("心電計" + ((String) objectRef.element) + "と接続されています。\n心電計を変更しますか？", null, "変更する", new Function0<Unit>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$changeDevice$$inlined$let$lambda$1
                /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
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
                    BaseModel.DialogInfo dialogInfo2 = new BaseModel.DialogInfo("心電計本体情報を初期化し、心電計を再検索します。\n本当によろしいですか？", null, "実行", new Function0<Unit>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$changeDevice$$inlined$let$lambda$1.1
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
                            CountDownTimer connectionRetryingTimer = this.getConnectionRetryingTimer();
                            if (connectionRetryingTimer != null) {
                                connectionRetryingTimer.cancel();
                            }
                            this.disconnectDevice();
                            this.searchDevice();
                            FileRecorder.INSTANCE.getInstance().writeText("DO CHANGE DEVICE");
                        }
                    }, "中止", null, 32, null);
                    Context it2 = it;
                    Intrinsics.checkNotNullExpressionValue(it2, "it");
                    UtilsKt.showiOSDialog(dialogInfo2, it2);
                }
            }, "変更しない", null, 32, null);
            Intrinsics.checkNotNullExpressionValue(it, "it");
            UtilsKt.showiOSDialog(dialogInfo, it);
        }
    }

    public final void showBleOffAlert() {
        FragmentActivity it1 = getActivity();
        if (it1 != null) {
            BaseModel.DialogInfo dialogInfo = new BaseModel.DialogInfo("設定からBluetoothを有効にしてください", null, "閉じる", new Function0<Unit>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$showBleOffAlert$$inlined$let$lambda$1
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
                    if (MainFragment.this.getBluetoothLeService().getBluetoothAdapter().isEnabled()) {
                        MainFragment.this.searchDevice();
                    } else {
                        MainFragment.this.showBleOffAlert();
                    }
                }
            }, null, null, 48, null);
            Intrinsics.checkNotNullExpressionValue(it1, "it1");
            UtilsKt.showiOSDialog(dialogInfo, it1);
        }
    }

    public final void searchDevice() {
        Job launch$default;
        FileRecorder.INSTANCE.getInstance().writeText("DO SEARCH DEVICE");
        BluetoothLeService bluetoothLeService = this.bluetoothLeService;
        if (bluetoothLeService == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        if (!bluetoothLeService.getBluetoothAdapter().isEnabled()) {
            showBleOffAlert();
            return;
        }
        BluetoothLeService bluetoothLeService2 = this.bluetoothLeService;
        if (bluetoothLeService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        bluetoothLeService2.startScan();
        FragmentMainBinding fragmentMainBinding = this.viewBinding;
        if (fragmentMainBinding == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        TextView textView = fragmentMainBinding.rriDay;
        Intrinsics.checkNotNullExpressionValue(textView, "viewBinding.rriDay");
        textView.setVisibility(4);
        FragmentMainBinding fragmentMainBinding2 = this.viewBinding;
        if (fragmentMainBinding2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
        }
        TextView textView2 = fragmentMainBinding2.rriTime;
        Intrinsics.checkNotNullExpressionValue(textView2, "viewBinding.rriTime");
        textView2.setVisibility(4);
        if (Config.INSTANCE.getConnectName().length() == 0) {
            showProgress("心電計を検索中...");
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$searchDevice$1
                @Override // java.lang.Runnable
                public final void run() {
                    MainModel mainModel;
                    if (!MainFragment.this.getBluetoothLeService().getBluetoothAdapter().isEnabled()) {
                        MainFragment.this.showBleOffAlert();
                        return;
                    }
                    int size = MainFragment.this.getBluetoothLeService().getDevices().size();
                    if (size == 0) {
                        MainFragment.this.setStartWaitTime(new Date().getTime());
                        MainFragment.this.waitNoFindDevice();
                        return;
                    }
                    int i = 0;
                    if (size == 1) {
                        MainFragment.this.dismissProgress();
                        MainFragment.this.getBluetoothLeService().stopScan();
                        BluetoothDevice bluetoothDevice = MainFragment.this.getBluetoothLeService().getDevices().get(0);
                        Intrinsics.checkNotNullExpressionValue(bluetoothDevice, "bluetoothLeService.devices[0]");
                        BluetoothDevice bluetoothDevice2 = bluetoothDevice;
                        mainModel = MainFragment.this.getMainModel();
                        mainModel.getDeviceName().setValue("SN:" + bluetoothDevice2.getName());
                        TextView textView3 = MainFragment.this.getViewBinding().rriDay;
                        Intrinsics.checkNotNullExpressionValue(textView3, "viewBinding.rriDay");
                        textView3.setVisibility(0);
                        TextView textView4 = MainFragment.this.getViewBinding().rriTime;
                        Intrinsics.checkNotNullExpressionValue(textView4, "viewBinding.rriTime");
                        textView4.setVisibility(0);
                        MainFragment.this.getBluetoothLeService().connectDevice(bluetoothDevice2);
                        BaseApplication.INSTANCE.setStartedScan(true);
                        MainFragment.this.checkBleStatusTimer();
                        return;
                    }
                    MainFragment.this.dismissProgress();
                    MainFragment.this.getBluetoothLeService().stopScan();
                    IOSDialog.Builder builder = new IOSDialog.Builder(MainFragment.this.requireContext());
                    builder.title("接続する心電計を選択してください");
                    builder.cancelable(false);
                    builder.multiOptions(true);
                    ArrayList<BluetoothDevice> devices = MainFragment.this.getBluetoothLeService().getDevices();
                    ArrayList arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(devices, 10));
                    for (Object obj : devices) {
                        int i2 = i + 1;
                        if (i < 0) {
                            CollectionsKt.throwIndexOverflow();
                        }
                        arrayList.add(new IOSDialogButton(i, ((BluetoothDevice) obj).getName()));
                        i = i2;
                    }
                    builder.iosDialogButtonList(arrayList);
                    builder.multiOptionsListeners(new IOSDialogMultiOptionsListeners() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$searchDevice$1.2
                        @Override // com.varunjohn1990.iosdialogs4android.IOSDialogMultiOptionsListeners
                        public final void onClick(IOSDialog iOSDialog, IOSDialogButton iosDialogButton) {
                            MainModel mainModel2;
                            iOSDialog.dismiss();
                            ArrayList<BluetoothDevice> devices2 = MainFragment.this.getBluetoothLeService().getDevices();
                            Intrinsics.checkNotNullExpressionValue(iosDialogButton, "iosDialogButton");
                            BluetoothDevice bluetoothDevice3 = devices2.get(iosDialogButton.getId());
                            Intrinsics.checkNotNullExpressionValue(bluetoothDevice3, "bluetoothLeService.devices[iosDialogButton.id]");
                            BluetoothDevice bluetoothDevice4 = bluetoothDevice3;
                            MainFragment.this.getBluetoothLeService().connectDevice(bluetoothDevice4);
                            BaseApplication.INSTANCE.setStartedScan(true);
                            MainFragment.this.checkBleStatusTimer();
                            mainModel2 = MainFragment.this.getMainModel();
                            mainModel2.getDeviceName().setValue("SN:" + bluetoothDevice4.getName());
                            TextView textView5 = MainFragment.this.getViewBinding().rriDay;
                            Intrinsics.checkNotNullExpressionValue(textView5, "viewBinding.rriDay");
                            textView5.setVisibility(0);
                            TextView textView6 = MainFragment.this.getViewBinding().rriTime;
                            Intrinsics.checkNotNullExpressionValue(textView6, "viewBinding.rriTime");
                            textView6.setVisibility(0);
                        }
                    });
                    builder.build().show();
                }
            }, Config.INSTANCE.getSCAN_DELAY_TIME());
            return;
        }
        Job job = this.disConnectJob;
        if (job != null) {
            Job.DefaultImpls.cancel$default(job, (CancellationException) null, 1, (Object) null);
        }
        launch$default = BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new MainFragment$searchDevice$2(this, null), 2, null);
        this.disConnectJob = launch$default;
        CountDownTimer countDownTimer = this.connectionRetryingTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        this.connectionRetryingTimer = (CountDownTimer) null;
        final long j = 20000000;
        final long j2 = 2000;
        CountDownTimer countDownTimer2 = new CountDownTimer(j, j2) { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$searchDevice$3
            @Override // android.os.CountDownTimer
            public void onTick(long millisUntilFinished) {
                Object obj;
                MainModel mainModel;
                if (!MainFragment.this.getBluetoothLeService().getBluetoothAdapter().isEnabled()) {
                    MainFragment.this.showBleOffAlert();
                    CountDownTimer connectionRetryingTimer = MainFragment.this.getConnectionRetryingTimer();
                    if (connectionRetryingTimer != null) {
                        connectionRetryingTimer.cancel();
                    }
                    Job disConnectJob = MainFragment.this.getDisConnectJob();
                    if (disConnectJob != null) {
                        Job.DefaultImpls.cancel$default(disConnectJob, (CancellationException) null, 1, (Object) null);
                    }
                    MainFragment.this.setDisConnectJob((Job) null);
                    return;
                }
                Iterator<T> it = MainFragment.this.getBluetoothLeService().getDevices().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        obj = null;
                        break;
                    } else {
                        obj = it.next();
                        if (Intrinsics.areEqual(((BluetoothDevice) obj).getName(), Config.INSTANCE.getConnectName())) {
                            break;
                        }
                    }
                }
                BluetoothDevice bluetoothDevice = (BluetoothDevice) obj;
                if (bluetoothDevice != null) {
                    MainFragment.this.getBluetoothLeService().stopScan();
                    CountDownTimer connectionRetryingTimer2 = MainFragment.this.getConnectionRetryingTimer();
                    if (connectionRetryingTimer2 != null) {
                        connectionRetryingTimer2.cancel();
                    }
                    Job disConnectJob2 = MainFragment.this.getDisConnectJob();
                    if (disConnectJob2 != null) {
                        Job.DefaultImpls.cancel$default(disConnectJob2, (CancellationException) null, 1, (Object) null);
                    }
                    MainFragment.this.setDisConnectJob((Job) null);
                    mainModel = MainFragment.this.getMainModel();
                    mainModel.getDeviceName().setValue("SN:" + bluetoothDevice.getName());
                    TextView textView3 = MainFragment.this.getViewBinding().rriDay;
                    Intrinsics.checkNotNullExpressionValue(textView3, "viewBinding.rriDay");
                    textView3.setVisibility(0);
                    TextView textView4 = MainFragment.this.getViewBinding().rriTime;
                    Intrinsics.checkNotNullExpressionValue(textView4, "viewBinding.rriTime");
                    textView4.setVisibility(0);
                    MainFragment.this.getBluetoothLeService().connectDevice(bluetoothDevice);
                    BaseApplication.INSTANCE.setStartedScan(true);
                    MainFragment.this.checkBleStatusTimer();
                }
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                CountDownTimer connectionRetryingTimer = MainFragment.this.getConnectionRetryingTimer();
                if (connectionRetryingTimer != null) {
                    connectionRetryingTimer.start();
                }
            }
        };
        this.connectionRetryingTimer = countDownTimer2;
        if (countDownTimer2 != null) {
            countDownTimer2.start();
        }
    }

    public final long getStartWaitTime() {
        return this.startWaitTime;
    }

    public final void setStartWaitTime(long j) {
        this.startWaitTime = j;
    }

    public final void waitNoFindDevice() {
        if (new Date().getTime() - this.startWaitTime > Config.INSTANCE.getSCAN_TIMEOUT() - Config.INSTANCE.getSCAN_DELAY_TIME()) {
            dismissProgress();
            BluetoothLeService bluetoothLeService = this.bluetoothLeService;
            if (bluetoothLeService == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            bluetoothLeService.stopScan();
            FragmentActivity it = getActivity();
            if (it != null) {
                BaseModel.DialogInfo dialogInfo = new BaseModel.DialogInfo("心電計が見つかりません", null, "再検索", new Function0<Unit>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$waitNoFindDevice$$inlined$let$lambda$1
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
                        MainFragment.this.searchDevice();
                    }
                }, null, null, 48, null);
                Intrinsics.checkNotNullExpressionValue(it, "it");
                UtilsKt.showiOSDialog(dialogInfo, it);
                return;
            }
            return;
        }
        BluetoothLeService bluetoothLeService2 = this.bluetoothLeService;
        if (bluetoothLeService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
        }
        if (bluetoothLeService2.getDevices().size() > 0) {
            dismissProgress();
            BluetoothLeService bluetoothLeService3 = this.bluetoothLeService;
            if (bluetoothLeService3 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            bluetoothLeService3.stopScan();
            BluetoothLeService bluetoothLeService4 = this.bluetoothLeService;
            if (bluetoothLeService4 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            BluetoothDevice bluetoothDevice = (BluetoothDevice) CollectionsKt.first((List) bluetoothLeService4.getDevices());
            getMainModel().getDeviceName().setValue("SN:" + bluetoothDevice.getName());
            FragmentMainBinding fragmentMainBinding = this.viewBinding;
            if (fragmentMainBinding == null) {
                Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
            }
            TextView textView = fragmentMainBinding.rriDay;
            Intrinsics.checkNotNullExpressionValue(textView, "viewBinding.rriDay");
            textView.setVisibility(0);
            FragmentMainBinding fragmentMainBinding2 = this.viewBinding;
            if (fragmentMainBinding2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("viewBinding");
            }
            TextView textView2 = fragmentMainBinding2.rriTime;
            Intrinsics.checkNotNullExpressionValue(textView2, "viewBinding.rriTime");
            textView2.setVisibility(0);
            BluetoothLeService bluetoothLeService5 = this.bluetoothLeService;
            if (bluetoothLeService5 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("bluetoothLeService");
            }
            bluetoothLeService5.connectDevice(bluetoothDevice);
            checkBleStatusTimer();
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$waitNoFindDevice$2
            @Override // java.lang.Runnable
            public final void run() {
                MainFragment.this.waitNoFindDevice();
            }
        }, 1000L);
    }

    public final ActivityResultLauncher<String> getRequestPermission() {
        return this.requestPermission;
    }

    public final void checkPermission() {
        if (ContextCompat.checkSelfPermission(BaseApplication.INSTANCE.getContext(), "android.permission.ACCESS_FINE_LOCATION") != 0) {
            this.requestPermission.launch("android.permission.ACCESS_FINE_LOCATION");
        } else {
            enableBluetooth();
        }
    }

    public final ActivityResultLauncher<Intent> getStartForResult() {
        return this.startForResult;
    }

    public final void enableBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intrinsics.checkNotNullExpressionValue(bluetoothAdapter, "bluetoothAdapter");
        if (!bluetoothAdapter.isEnabled()) {
            this.startForResult.launch(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"));
        } else {
            searchDevice();
        }
    }
}
