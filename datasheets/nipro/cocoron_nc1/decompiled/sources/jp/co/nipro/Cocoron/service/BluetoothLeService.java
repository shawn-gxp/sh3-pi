package jp.co.nipro.cocoron.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import androidx.constraintlayout.solver.widgets.analyzer.BasicMeasure;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import jp.co.nipro.Cocoron.C0009R;
import jp.co.nipro.cocoron.common.Config;
import jp.co.nipro.cocoron.common.Event;
import jp.co.nipro.cocoron.data.FileRecorder;
import jp.co.nipro.cocoron.data.value.BatteryLevelCharacteristicValue;
import jp.co.nipro.cocoron.data.value.ConfigCharacteristicValue;
import jp.co.nipro.cocoron.data.value.DateTimeCharacteristicValue;
import jp.co.nipro.cocoron.ui.activity.MainActivity;
import jp.co.nipro.cocoron.ui.viewmodel.BaseModel;
import kotlin.Metadata;
import kotlin.collections.ArraysKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.Job;

/* compiled from: BluetoothLeService.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000°\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0011\u0018\u0000 \u0089\u00012\u00020\u0001:\u0004\u0088\u0001\u0089\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u000e\u0010n\u001a\u00020o2\u0006\u0010p\u001a\u000200J\u0006\u0010q\u001a\u00020oJ\u0012\u0010r\u001a\u0004\u0018\u00010s2\u0006\u0010t\u001a\u00020uH\u0016J\b\u0010v\u001a\u00020oH\u0016J\b\u0010w\u001a\u00020oH\u0016J\"\u0010x\u001a\u00020y2\b\u0010t\u001a\u0004\u0018\u00010u2\u0006\u0010z\u001a\u00020y2\u0006\u0010{\u001a\u00020yH\u0016J\u0016\u0010|\u001a\u00020o2\u0006\u0010}\u001a\u00020\u00042\u0006\u0010~\u001a\u00020\u0004J\u0017\u0010'\u001a\u00020o2\u0006\u0010\u007f\u001a\u00020y2\u0007\u0010\u0080\u0001\u001a\u00020yJ\u0007\u0010\u0081\u0001\u001a\u00020oJ\u0010\u0010\u0082\u0001\u001a\u00020o2\u0007\u0010\u0080\u0001\u001a\u00020yJ\u000f\u0010\u0083\u0001\u001a\u00020o2\u0006\u0010\u007f\u001a\u00020yJ\u0007\u0010\u0084\u0001\u001a\u00020\nJ\u0007\u0010\u0085\u0001\u001a\u00020oJ\u0014\u0010\u0086\u0001\u001a\u00020\n2\t\u0010\u0087\u0001\u001a\u0004\u0018\u00010uH\u0016R\u0014\u0010\u0003\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0014\u0010\u0007\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0006R\u001a\u0010\t\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001c\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u0012\u0010\u0015\u001a\u00060\u0016R\u00020\u0000X\u0082\u000e¢\u0006\u0002\n\u0000R\u001a\u0010\u0017\u001a\u00020\u0018X\u0086.¢\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR\u001c\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010 \"\u0004\b!\u0010\"R\u001a\u0010#\u001a\u00020$X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b%\u0010&\"\u0004\b'\u0010(R\u001c\u0010)\u001a\u0004\u0018\u00010*X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b+\u0010,\"\u0004\b-\u0010.R\u001c\u0010/\u001a\u0004\u0018\u000100X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b1\u00102\"\u0004\b3\u00104R\u001c\u00105\u001a\u0004\u0018\u00010*X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b6\u0010,\"\u0004\b7\u0010.R*\u00108\u001a\u0012\u0012\u0004\u0012\u00020:09j\b\u0012\u0004\u0012\u00020:`;X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b<\u0010=\"\u0004\b>\u0010?R*\u0010@\u001a\u0012\u0012\u0004\u0012\u00020009j\b\u0012\u0004\u0012\u000200`;X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bA\u0010=\"\u0004\bB\u0010?R\u001a\u0010C\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bD\u0010\f\"\u0004\bE\u0010\u000eR\u001a\u0010F\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bG\u0010\f\"\u0004\bH\u0010\u000eR\u001d\u0010I\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020L0K0J¢\u0006\b\n\u0000\u001a\u0004\bM\u0010NR\u001a\u0010O\u001a\u00020PX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bQ\u0010R\"\u0004\bS\u0010TR\u000e\u0010U\u001a\u00020VX\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010W\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bX\u0010\f\"\u0004\bY\u0010\u000eR\u001a\u0010Z\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b[\u0010\f\"\u0004\b\\\u0010\u000eR\u001a\u0010]\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b^\u0010\f\"\u0004\b_\u0010\u000eR\u000e\u0010`\u001a\u00020aX\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010b\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bc\u0010\f\"\u0004\bd\u0010\u000eR\u001a\u0010e\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bf\u0010\f\"\u0004\bg\u0010\u000eR\u001c\u0010h\u001a\u0004\u0018\u00010iX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bj\u0010k\"\u0004\bl\u0010m¨\u0006\u008a\u0001"}, d2 = {"Ljp/co/nipro/cocoron/service/BluetoothLeService;", "Landroid/app/Service;", "()V", "CHANNEL_ID", "", "getCHANNEL_ID", "()Ljava/lang/String;", "CHANNEL_NAME", "getCHANNEL_NAME", "autoReconnect", "", "getAutoReconnect", "()Z", "setAutoReconnect", "(Z)V", "batteryLevel", "Ljp/co/nipro/cocoron/data/value/BatteryLevelCharacteristicValue;", "getBatteryLevel", "()Ljp/co/nipro/cocoron/data/value/BatteryLevelCharacteristicValue;", "setBatteryLevel", "(Ljp/co/nipro/cocoron/data/value/BatteryLevelCharacteristicValue;)V", "binder", "Ljp/co/nipro/cocoron/service/BluetoothLeService$BleBinder;", "bluetoothAdapter", "Landroid/bluetooth/BluetoothAdapter;", "getBluetoothAdapter", "()Landroid/bluetooth/BluetoothAdapter;", "setBluetoothAdapter", "(Landroid/bluetooth/BluetoothAdapter;)V", "bluetoothGatt", "Landroid/bluetooth/BluetoothGatt;", "getBluetoothGatt", "()Landroid/bluetooth/BluetoothGatt;", "setBluetoothGatt", "(Landroid/bluetooth/BluetoothGatt;)V", "config", "Ljp/co/nipro/cocoron/data/value/ConfigCharacteristicValue;", "getConfig", "()Ljp/co/nipro/cocoron/data/value/ConfigCharacteristicValue;", "setConfig", "(Ljp/co/nipro/cocoron/data/value/ConfigCharacteristicValue;)V", "configCharacteristic", "Landroid/bluetooth/BluetoothGattCharacteristic;", "getConfigCharacteristic", "()Landroid/bluetooth/BluetoothGattCharacteristic;", "setConfigCharacteristic", "(Landroid/bluetooth/BluetoothGattCharacteristic;)V", "connectedDevice", "Landroid/bluetooth/BluetoothDevice;", "getConnectedDevice", "()Landroid/bluetooth/BluetoothDevice;", "setConnectedDevice", "(Landroid/bluetooth/BluetoothDevice;)V", "dateTimeCharacteristic", "getDateTimeCharacteristic", "setDateTimeCharacteristic", "descriptors", "Ljava/util/ArrayList;", "Landroid/bluetooth/BluetoothGattDescriptor;", "Lkotlin/collections/ArrayList;", "getDescriptors", "()Ljava/util/ArrayList;", "setDescriptors", "(Ljava/util/ArrayList;)V", "devices", "getDevices", "setDevices", "didNotifyOutService", "getDidNotifyOutService", "setDidNotifyOutService", "disconnect_rri", "getDisconnect_rri", "setDisconnect_rri", NotificationCompat.CATEGORY_EVENT, "Landroidx/lifecycle/MutableLiveData;", "Ljp/co/nipro/cocoron/common/Event;", "Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel$EventParam;", "getEvent", "()Landroidx/lifecycle/MutableLiveData;", "fileRecorder", "Ljp/co/nipro/cocoron/data/FileRecorder;", "getFileRecorder", "()Ljp/co/nipro/cocoron/data/FileRecorder;", "setFileRecorder", "(Ljp/co/nipro/cocoron/data/FileRecorder;)V", "gattCallback", "Landroid/bluetooth/BluetoothGattCallback;", "low_battery", "getLow_battery", "setLow_battery", "out_service", "getOut_service", "setOut_service", "rri_over_limit", "getRri_over_limit", "setRri_over_limit", "scanCallback", "Landroid/bluetooth/le/ScanCallback;", "sended_config", "getSended_config", "setSended_config", "sended_time", "getSended_time", "setSended_time", "workItemDisconnect", "Lkotlinx/coroutines/Job;", "getWorkItemDisconnect", "()Lkotlinx/coroutines/Job;", "setWorkItemDisconnect", "(Lkotlinx/coroutines/Job;)V", "connectDevice", "", "bleDevice", "disConnectCurrDevice", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onStartCommand", "", "flags", "startId", "sendNotification", "title", "log", "interval", "mode", "setDateTime", "setEcgMode", "setSendInterval", "startScan", "stopScan", "stopService", "name", "BleBinder", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class BluetoothLeService extends Service {
    public static final String BATTERY = "00002A19-0000-1000-8000-00805F9B34FB";
    public static final String CONFIG = "C74D2001-457D-4194-8B37-E7188723AEA9";
    public static final String DATETIME = "00002A08-0000-1000-8000-00805F9B34FB";
    public static final String ECG = "C74D2000-457D-4194-8B37-E7188723AEA9";
    public static final String RRT = "C74D2002-457D-4194-8B37-E7188723AEA9";
    public static final String SERVICE = "C74D1000-457D-4194-8B37-E7188723AEA9";
    public static final String TAG = "BluetoothLeService";
    public static final String UUID_NOTIFY = "00002902-0000-1000-8000-00805f9b34fb";
    private BatteryLevelCharacteristicValue batteryLevel;
    public BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic configCharacteristic;
    private BluetoothDevice connectedDevice;
    private BluetoothGattCharacteristic dateTimeCharacteristic;
    private boolean didNotifyOutService;
    private boolean disconnect_rri;
    private boolean low_battery;
    private boolean out_service;
    private boolean rri_over_limit;
    private boolean sended_config;
    private boolean sended_time;
    private Job workItemDisconnect;
    private final String CHANNEL_ID = "jp.co.nipro.cocoron.channel_id";
    private final String CHANNEL_NAME = "通知";
    private BleBinder binder = new BleBinder();
    private final MutableLiveData<Event<BaseModel.EventParam>> event = new MutableLiveData<>();
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private ArrayList<BluetoothGattDescriptor> descriptors = new ArrayList<>();
    private boolean autoReconnect = true;
    private ConfigCharacteristicValue config = new ConfigCharacteristicValue();
    private FileRecorder fileRecorder = FileRecorder.INSTANCE.getInstance();
    private final ScanCallback scanCallback = new ScanCallback() { // from class: jp.co.nipro.cocoron.service.BluetoothLeService$scanCallback$1
        @Override // android.bluetooth.le.ScanCallback
        public void onScanResult(int callbackType, ScanResult result) {
            Intrinsics.checkNotNullParameter(result, "result");
            super.onScanResult(callbackType, result);
            Log.d(BluetoothLeService.TAG, "onScanResult:" + result);
            BluetoothDevice bluetoothDevice = result.getDevice();
            Intrinsics.checkNotNullExpressionValue(bluetoothDevice, "bluetoothDevice");
            String name = bluetoothDevice.getName();
            if (name == null) {
                name = ":" + bluetoothDevice.getAddress();
            }
            Log.d(BluetoothLeService.TAG, String.valueOf(name));
            ArrayList<BluetoothDevice> devices = BluetoothLeService.this.getDevices();
            boolean z = false;
            if (!(devices instanceof Collection) || !devices.isEmpty()) {
                Iterator<T> it = devices.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (Intrinsics.areEqual(((BluetoothDevice) it.next()).getAddress(), bluetoothDevice.getAddress())) {
                        z = true;
                        break;
                    }
                }
            }
            if (z) {
                return;
            }
            BluetoothLeService.this.getDevices().add(bluetoothDevice);
            BluetoothLeService.this.getFileRecorder().writeText("FIND DEVICE:" + bluetoothDevice.getName());
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onBatchScanResults(List<ScanResult> results) {
            Intrinsics.checkNotNullParameter(results, "results");
            super.onBatchScanResults(results);
            Log.d(BluetoothLeService.TAG, "onBatchScanResults:" + results);
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(BluetoothLeService.TAG, "onScanFailed:" + errorCode);
        }
    };
    private final BluetoothGattCallback gattCallback = new BluetoothLeService$gattCallback$1(this);

    public final String getCHANNEL_ID() {
        return this.CHANNEL_ID;
    }

    public final String getCHANNEL_NAME() {
        return this.CHANNEL_NAME;
    }

    /* compiled from: BluetoothLeService.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004¨\u0006\u0005"}, d2 = {"Ljp/co/nipro/cocoron/service/BluetoothLeService$BleBinder;", "Landroid/os/Binder;", "(Ljp/co/nipro/cocoron/service/BluetoothLeService;)V", "getService", "Ljp/co/nipro/cocoron/service/BluetoothLeService;", "app_release"}, k = 1, mv = {1, 4, 2})
    public final class BleBinder extends Binder {
        public BleBinder() {
        }

        /* renamed from: getService, reason: from getter */
        public final BluetoothLeService getThis$0() {
            return BluetoothLeService.this;
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        this.fileRecorder.writeText("BLE SERVICE CREATE");
        Object systemService = getSystemService("bluetooth");
        Objects.requireNonNull(systemService, "null cannot be cast to non-null type android.bluetooth.BluetoothManager");
        BluetoothAdapter adapter = ((BluetoothManager) systemService).getAdapter();
        Intrinsics.checkNotNullExpressionValue(adapter, "manager.adapter");
        this.bluetoothAdapter = adapter;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Intrinsics.checkNotNullParameter(intent, "intent");
        Log.d(TAG, "onBind");
        this.fileRecorder.writeText("BLE SERVICE ONBIND");
        return this.binder;
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        this.fileRecorder.writeText("BLE SERVICE ON START COMMAND");
        BluetoothLeService bluetoothLeService = this;
        Intent intent2 = new Intent(bluetoothLeService, (Class<?>) MainActivity.class);
        intent2.addFlags(67108864);
        PendingIntent activity = PendingIntent.getActivity(bluetoothLeService, 0, intent2, BasicMeasure.EXACTLY);
        Object systemService = getSystemService("notification");
        Objects.requireNonNull(systemService, "null cannot be cast to non-null type android.app.NotificationManager");
        NotificationManager notificationManager = (NotificationManager) systemService;
        if (Build.VERSION.SDK_INT >= 26 && notificationManager.getNotificationChannel(this.CHANNEL_ID) == null) {
            notificationManager.createNotificationChannel(new NotificationChannel(this.CHANNEL_ID, this.CHANNEL_NAME, 4));
        }
        startForeground(9999, new NotificationCompat.Builder(bluetoothLeService, this.CHANNEL_ID).setSmallIcon(C0009R.mipmap.ic_launcher).setContentTitle("Cocoron").setContentText("Bluetoothが送受信されています...").setContentIntent(activity).setDefaults(3).setPriority(0).build());
        return 1;
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public boolean stopService(Intent name) {
        Log.d(TAG, "stopService");
        this.fileRecorder.writeText("BLE SERVICE STOP");
        return super.stopService(name);
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        this.fileRecorder.writeText("BLE SERVICE DESTORY");
        super.onDestroy();
        stopSelf();
    }

    public final MutableLiveData<Event<BaseModel.EventParam>> getEvent() {
        return this.event;
    }

    public final ArrayList<BluetoothDevice> getDevices() {
        return this.devices;
    }

    public final void setDevices(ArrayList<BluetoothDevice> arrayList) {
        Intrinsics.checkNotNullParameter(arrayList, "<set-?>");
        this.devices = arrayList;
    }

    public final ArrayList<BluetoothGattDescriptor> getDescriptors() {
        return this.descriptors;
    }

    public final void setDescriptors(ArrayList<BluetoothGattDescriptor> arrayList) {
        Intrinsics.checkNotNullParameter(arrayList, "<set-?>");
        this.descriptors = arrayList;
    }

    public final BluetoothAdapter getBluetoothAdapter() {
        BluetoothAdapter bluetoothAdapter = this.bluetoothAdapter;
        if (bluetoothAdapter == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothAdapter");
        }
        return bluetoothAdapter;
    }

    public final void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        Intrinsics.checkNotNullParameter(bluetoothAdapter, "<set-?>");
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public final BluetoothDevice getConnectedDevice() {
        return this.connectedDevice;
    }

    public final void setConnectedDevice(BluetoothDevice bluetoothDevice) {
        this.connectedDevice = bluetoothDevice;
    }

    public final BluetoothGatt getBluetoothGatt() {
        return this.bluetoothGatt;
    }

    public final void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    public final BluetoothGattCharacteristic getConfigCharacteristic() {
        return this.configCharacteristic;
    }

    public final void setConfigCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.configCharacteristic = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getDateTimeCharacteristic() {
        return this.dateTimeCharacteristic;
    }

    public final void setDateTimeCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.dateTimeCharacteristic = bluetoothGattCharacteristic;
    }

    public final boolean getAutoReconnect() {
        return this.autoReconnect;
    }

    public final void setAutoReconnect(boolean z) {
        this.autoReconnect = z;
    }

    public final BatteryLevelCharacteristicValue getBatteryLevel() {
        return this.batteryLevel;
    }

    public final void setBatteryLevel(BatteryLevelCharacteristicValue batteryLevelCharacteristicValue) {
        this.batteryLevel = batteryLevelCharacteristicValue;
    }

    public final ConfigCharacteristicValue getConfig() {
        return this.config;
    }

    public final void setConfig(ConfigCharacteristicValue configCharacteristicValue) {
        Intrinsics.checkNotNullParameter(configCharacteristicValue, "<set-?>");
        this.config = configCharacteristicValue;
    }

    public final boolean getSended_config() {
        return this.sended_config;
    }

    public final void setSended_config(boolean z) {
        this.sended_config = z;
    }

    public final boolean getSended_time() {
        return this.sended_time;
    }

    public final void setSended_time(boolean z) {
        this.sended_time = z;
    }

    public final FileRecorder getFileRecorder() {
        return this.fileRecorder;
    }

    public final void setFileRecorder(FileRecorder fileRecorder) {
        Intrinsics.checkNotNullParameter(fileRecorder, "<set-?>");
        this.fileRecorder = fileRecorder;
    }

    public final Job getWorkItemDisconnect() {
        return this.workItemDisconnect;
    }

    public final void setWorkItemDisconnect(Job job) {
        this.workItemDisconnect = job;
    }

    public final boolean getOut_service() {
        return this.out_service;
    }

    public final void setOut_service(boolean z) {
        this.out_service = z;
    }

    public final boolean getDisconnect_rri() {
        return this.disconnect_rri;
    }

    public final void setDisconnect_rri(boolean z) {
        this.disconnect_rri = z;
    }

    public final boolean getLow_battery() {
        return this.low_battery;
    }

    public final void setLow_battery(boolean z) {
        this.low_battery = z;
    }

    public final boolean getRri_over_limit() {
        return this.rri_over_limit;
    }

    public final void setRri_over_limit(boolean z) {
        this.rri_over_limit = z;
    }

    public final boolean startScan() {
        BluetoothAdapter bluetoothAdapter = this.bluetoothAdapter;
        if (bluetoothAdapter == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothAdapter");
        }
        if (!bluetoothAdapter.isEnabled()) {
            return false;
        }
        this.fileRecorder.writeText("START SCAN");
        BluetoothAdapter bluetoothAdapter2 = this.bluetoothAdapter;
        if (bluetoothAdapter2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothAdapter");
        }
        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter2.getBluetoothLeScanner();
        this.devices.clear();
        ScanFilter build = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(SERVICE)).build();
        ArrayList arrayList = new ArrayList();
        arrayList.add(build);
        bluetoothLeScanner.startScan(arrayList, new ScanSettings.Builder().setScanMode(1).build(), this.scanCallback);
        this.configCharacteristic = (BluetoothGattCharacteristic) null;
        return true;
    }

    public final void stopScan() {
        this.fileRecorder.writeText("STOP SCAN");
        BluetoothAdapter bluetoothAdapter = this.bluetoothAdapter;
        if (bluetoothAdapter == null) {
            Intrinsics.throwUninitializedPropertyAccessException("bluetoothAdapter");
        }
        bluetoothAdapter.getBluetoothLeScanner().stopScan(this.scanCallback);
    }

    public final void connectDevice(BluetoothDevice bleDevice) {
        Intrinsics.checkNotNullParameter(bleDevice, "bleDevice");
        BluetoothGatt bluetoothGatt = this.bluetoothGatt;
        if (bluetoothGatt != null) {
            this.fileRecorder.writeDisconnectGatt(bluetoothGatt);
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        this.bluetoothGatt = (BluetoothGatt) null;
        this.connectedDevice = bleDevice;
        this.autoReconnect = true;
        this.out_service = false;
        this.disconnect_rri = false;
        this.low_battery = false;
        this.rri_over_limit = false;
        Config config = Config.INSTANCE;
        String name = bleDevice.getName();
        Intrinsics.checkNotNullExpressionValue(name, "bleDevice.name");
        config.setConnectName(name);
        BluetoothGatt connectGatt = bleDevice.connectGatt(this, true ^ Config.INSTANCE.getUSE_ECG_SIM(), this.gattCallback, 2);
        Log.d(TAG, "connectDevice:" + bleDevice.getName());
        this.fileRecorder.writeConnectGatt(connectGatt);
    }

    public final void disConnectCurrDevice() {
        this.out_service = false;
        this.disconnect_rri = false;
        this.low_battery = false;
        this.autoReconnect = false;
        this.rri_over_limit = false;
        BluetoothGatt bluetoothGatt = this.bluetoothGatt;
        if (bluetoothGatt != null) {
            this.fileRecorder.writeDisconnectGatt(bluetoothGatt);
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            this.connectedDevice = (BluetoothDevice) null;
        }
        this.bluetoothGatt = (BluetoothGatt) null;
        this.fileRecorder.writeText("DO DISCONNECT");
    }

    public final void setEcgMode(int mode) {
        this.config.setMode(mode);
        byte[] writeToDate = ConfigCharacteristicValue.INSTANCE.writeToDate(mode, this.config.getInterval());
        BluetoothGattCharacteristic bluetoothGattCharacteristic = this.configCharacteristic;
        if (bluetoothGattCharacteristic != null) {
            bluetoothGattCharacteristic.setValue(writeToDate);
            StringBuilder sb = new StringBuilder();
            sb.append("TRACE: config send mode:");
            sb.append(mode);
            sb.append(" BIN:");
            String joinToString$default = ArraysKt.joinToString$default(writeToDate, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.service.BluetoothLeService$setEcgMode$1$1
                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ CharSequence invoke(Byte b) {
                    return invoke(b.byteValue());
                }

                public final CharSequence invoke(byte b) {
                    String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
                    Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
                    return format;
                }
            }, 30, (Object) null);
            Objects.requireNonNull(joinToString$default, "null cannot be cast to non-null type java.lang.String");
            String upperCase = joinToString$default.toUpperCase();
            Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
            sb.append(upperCase);
            Log.d(TAG, sb.toString());
            BluetoothGatt bluetoothGatt = this.bluetoothGatt;
            if (bluetoothGatt != null) {
                bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
            }
            this.fileRecorder.writeSendBle("CONFIG", writeToDate);
        }
    }

    public final void setSendInterval(int interval) {
        this.config.setInterval(interval);
        byte[] writeToDate = ConfigCharacteristicValue.INSTANCE.writeToDate(this.config.getMode(), interval);
        BluetoothGattCharacteristic bluetoothGattCharacteristic = this.configCharacteristic;
        if (bluetoothGattCharacteristic != null) {
            bluetoothGattCharacteristic.setValue(writeToDate);
            StringBuilder sb = new StringBuilder();
            sb.append("TRACE: config send interval:");
            sb.append(interval);
            sb.append(" BIN:");
            String joinToString$default = ArraysKt.joinToString$default(writeToDate, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.service.BluetoothLeService$setSendInterval$1$1
                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ CharSequence invoke(Byte b) {
                    return invoke(b.byteValue());
                }

                public final CharSequence invoke(byte b) {
                    String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
                    Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
                    return format;
                }
            }, 30, (Object) null);
            Objects.requireNonNull(joinToString$default, "null cannot be cast to non-null type java.lang.String");
            String upperCase = joinToString$default.toUpperCase();
            Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
            sb.append(upperCase);
            Log.d(TAG, sb.toString());
            BluetoothGatt bluetoothGatt = this.bluetoothGatt;
            if (bluetoothGatt != null) {
                bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
            }
            this.fileRecorder.writeSendBle("CONFIG", writeToDate);
        }
    }

    public final void setConfig(int interval, int mode) {
        this.config.setInterval(interval);
        this.config.setMode(mode);
        byte[] writeToDate = ConfigCharacteristicValue.INSTANCE.writeToDate(mode, interval);
        BluetoothGattCharacteristic bluetoothGattCharacteristic = this.configCharacteristic;
        if (bluetoothGattCharacteristic != null) {
            bluetoothGattCharacteristic.setValue(writeToDate);
            StringBuilder sb = new StringBuilder();
            sb.append("TRACE: config send interval:");
            sb.append(interval);
            sb.append(", mode:");
            sb.append(mode);
            sb.append(" BIN:");
            String joinToString$default = ArraysKt.joinToString$default(writeToDate, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.service.BluetoothLeService$setConfig$1$1
                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ CharSequence invoke(Byte b) {
                    return invoke(b.byteValue());
                }

                public final CharSequence invoke(byte b) {
                    String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
                    Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
                    return format;
                }
            }, 30, (Object) null);
            Objects.requireNonNull(joinToString$default, "null cannot be cast to non-null type java.lang.String");
            String upperCase = joinToString$default.toUpperCase();
            Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
            sb.append(upperCase);
            Log.d(TAG, sb.toString());
            BluetoothGatt bluetoothGatt = this.bluetoothGatt;
            if (bluetoothGatt != null) {
                bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
            }
            this.fileRecorder.writeSendBle("CONFIG", writeToDate);
        }
    }

    public final void setDateTime() {
        byte[] dateTimePacket = new DateTimeCharacteristicValue().getDateTimePacket();
        BluetoothGattCharacteristic bluetoothGattCharacteristic = this.dateTimeCharacteristic;
        if (bluetoothGattCharacteristic != null) {
            bluetoothGattCharacteristic.setValue(dateTimePacket);
            BluetoothGatt bluetoothGatt = this.bluetoothGatt;
            Boolean valueOf = bluetoothGatt != null ? Boolean.valueOf(bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic)) : null;
            StringBuilder sb = new StringBuilder();
            sb.append("TRACE: time send :");
            String joinToString$default = ArraysKt.joinToString$default(dateTimePacket, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.service.BluetoothLeService$setDateTime$1$1
                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ CharSequence invoke(Byte b) {
                    return invoke(b.byteValue());
                }

                public final CharSequence invoke(byte b) {
                    String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
                    Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
                    return format;
                }
            }, 30, (Object) null);
            Objects.requireNonNull(joinToString$default, "null cannot be cast to non-null type java.lang.String");
            String upperCase = joinToString$default.toUpperCase();
            Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
            sb.append(upperCase);
            sb.append(" res:");
            sb.append(valueOf);
            Log.d(TAG, sb.toString());
            this.fileRecorder.writeSendBle("DATETIME", dateTimePacket);
        }
    }

    public final void sendNotification(String title, String log) {
        Intrinsics.checkNotNullParameter(title, "title");
        Intrinsics.checkNotNullParameter(log, "log");
        this.fileRecorder.writeText(log);
        BluetoothLeService bluetoothLeService = this;
        Intent intent = new Intent(bluetoothLeService, (Class<?>) MainActivity.class);
        intent.addFlags(67108864);
        PendingIntent activity = PendingIntent.getActivity(bluetoothLeService, 0, intent, BasicMeasure.EXACTLY);
        Object systemService = getSystemService("notification");
        Objects.requireNonNull(systemService, "null cannot be cast to non-null type android.app.NotificationManager");
        NotificationManager notificationManager = (NotificationManager) systemService;
        if (Build.VERSION.SDK_INT >= 26 && notificationManager.getNotificationChannel(this.CHANNEL_ID) == null) {
            notificationManager.createNotificationChannel(new NotificationChannel(this.CHANNEL_ID, this.CHANNEL_NAME, 4));
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(bluetoothLeService, this.CHANNEL_ID);
        builder.setSmallIcon(C0009R.mipmap.ic_launcher).setContentTitle(title).setDefaults(3).setAutoCancel(true).setContentIntent(activity);
        notificationManager.notify(0, builder.build());
    }

    public final boolean getDidNotifyOutService() {
        return this.didNotifyOutService;
    }

    public final void setDidNotifyOutService(boolean z) {
        this.didNotifyOutService = z;
    }
}
