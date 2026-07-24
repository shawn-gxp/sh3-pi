package jp.co.nipro.cocoron.ui.fragment;

import androidx.fragment.app.FragmentActivity;
import java.util.Objects;
import jp.co.nipro.cocoron.common.BaseApplication;
import jp.co.nipro.cocoron.common.Config;
import jp.co.nipro.cocoron.common.UtilsKt;
import jp.co.nipro.cocoron.data.value.ECGMeasurementCharacteristicValue;
import jp.co.nipro.cocoron.data.value.RRTimeCharacteristicValue;
import jp.co.nipro.cocoron.ui.fragment.MainFragment;
import jp.co.nipro.cocoron.ui.viewmodel.BaseModel;
import jp.co.nipro.cocoron.ui.viewmodel.MainModel;
import kotlin.Metadata;
import kotlin.UShort;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

/* compiled from: MainFragment.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\n¢\u0006\u0002\b\u0004"}, d2 = {"<anonymous>", "", "it", "Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel$EventParam;", "invoke"}, k = 3, mv = {1, 4, 2})
/* loaded from: classes.dex */
final class MainFragment$onSubscribeService$1 extends Lambda implements Function1<BaseModel.EventParam, Unit> {
    final /* synthetic */ MainFragment this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    MainFragment$onSubscribeService$1(MainFragment mainFragment) {
        super(1);
        this.this$0 = mainFragment;
    }

    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(BaseModel.EventParam eventParam) {
        invoke2(eventParam);
        return Unit.INSTANCE;
    }

    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    /* renamed from: invoke, reason: avoid collision after fix types in other method */
    public final void invoke2(BaseModel.EventParam it) {
        MainModel mainModel;
        MainModel mainModel2;
        MainModel mainModel3;
        MainModel mainModel4;
        MainModel mainModel5;
        MainModel mainModel6;
        Intrinsics.checkNotNullParameter(it, "it");
        String name = it.getName();
        switch (name.hashCode()) {
            case -2014452502:
                if (name.equals("DID_CONNECT")) {
                    mainModel = this.this$0.getMainModel();
                    mainModel.getDeviceName().setValue("SN:" + Config.INSTANCE.getConnectName());
                    break;
                }
                break;
            case -1965297540:
                name.equals("DID_DISCONNECT");
                break;
            case -485941628:
                if (name.equals("OUT_SERVICE")) {
                    Object value = it.getValue();
                    Objects.requireNonNull(value, "null cannot be cast to non-null type kotlin.Boolean");
                    this.this$0.refreshIcon(MainFragment.Icon.OUT_SERVICE, ((Boolean) value).booleanValue());
                    break;
                }
                break;
            case -213169777:
                if (name.equals("RECEIVED_BATTERY")) {
                    Object value2 = it.getValue();
                    Objects.requireNonNull(value2, "null cannot be cast to non-null type kotlin.Boolean");
                    this.this$0.refreshIcon(MainFragment.Icon.BATTERY, ((Boolean) value2).booleanValue());
                    break;
                }
                break;
            case 1336950651:
                if (name.equals("RRT_OVER_LIMIT")) {
                    Object value3 = it.getValue();
                    Objects.requireNonNull(value3, "null cannot be cast to non-null type kotlin.Boolean");
                    if (((Boolean) value3).booleanValue()) {
                        if (!this.this$0.getBpmAnimation().isRunning()) {
                            this.this$0.getBpmAnimation().start();
                        }
                        if (this.this$0.getBluetoothLeService().getConfig().getMode() == 1) {
                            mainModel2 = this.this$0.getMainModel();
                            mainModel2.setRealEcgMode(1);
                            break;
                        }
                    } else {
                        this.this$0.getBpmAnimation().selectDrawable(0);
                        this.this$0.getBpmAnimation().stop();
                        break;
                    }
                }
                break;
            case 1550597131:
                if (name.equals("RECEIVED_ECG")) {
                    Object value4 = it.getValue();
                    Objects.requireNonNull(value4, "null cannot be cast to non-null type kotlin.ByteArray");
                    ECGMeasurementCharacteristicValue eCGMeasurementCharacteristicValue = new ECGMeasurementCharacteristicValue();
                    eCGMeasurementCharacteristicValue.readECGMeasurementPacket((byte[]) value4);
                    if (!this.this$0.getBluetoothLeService().getOut_service()) {
                        mainModel3 = this.this$0.getMainModel();
                        mainModel3.receivedEcg(eCGMeasurementCharacteristicValue);
                        break;
                    }
                }
                break;
            case 1550610102:
                if (name.equals("RECEIVED_RRT")) {
                    Object value5 = it.getValue();
                    Objects.requireNonNull(value5, "null cannot be cast to non-null type jp.co.nipro.cocoron.data.value.RRTimeCharacteristicValue");
                    RRTimeCharacteristicValue rRTimeCharacteristicValue = (RRTimeCharacteristicValue) value5;
                    if (rRTimeCharacteristicValue.getIsIllegalIphone()) {
                        final String connectName = Config.INSTANCE.getConnectName();
                        this.this$0.disconnectDevice();
                        FragmentActivity it1 = this.this$0.getActivity();
                        if (it1 != null) {
                            BaseModel.DialogInfo dialogInfo = new BaseModel.DialogInfo("心電計" + connectName + "は他の端末と接続されています。\n" + connectName + "の電源を切入して「再検索」ボタンを押してください。", null, "再検索", new Function0<Unit>() { // from class: jp.co.nipro.cocoron.ui.fragment.MainFragment$onSubscribeService$1$$special$$inlined$let$lambda$1
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
                                    MainFragment$onSubscribeService$1.this.this$0.searchDevice();
                                }
                            }, null, null, 48, null);
                            Intrinsics.checkNotNullExpressionValue(it1, "it1");
                            UtilsKt.showiOSDialog(dialogInfo, it1);
                            break;
                        }
                    } else {
                        int rRTime = rRTimeCharacteristicValue.getRRTime() & UShort.MAX_VALUE;
                        String valueOf = (rRTimeCharacteristicValue.getOutservice() != 0 || rRTime == 65535 || rRTime < Config.INSTANCE.getRRI_SHOW_LIMIT()) ? "-" : String.valueOf(rRTime);
                        mainModel4 = this.this$0.getMainModel();
                        mainModel4.getRriStr().setValue(valueOf);
                        BaseApplication.INSTANCE.setReceived_rri(valueOf);
                        mainModel5 = this.this$0.getMainModel();
                        if (mainModel5.getRealTimeMode()) {
                            mainModel6 = this.this$0.getMainModel();
                            MainModel.setRriData$default(mainModel6, false, 1, null);
                            break;
                        }
                    }
                }
                break;
            case 1745355750:
                if (name.equals("DISCONNECT_RRI")) {
                    Object value6 = it.getValue();
                    Objects.requireNonNull(value6, "null cannot be cast to non-null type kotlin.Boolean");
                    this.this$0.refreshIcon(MainFragment.Icon.CONNECT, ((Boolean) value6).booleanValue());
                    break;
                }
                break;
        }
    }
}
