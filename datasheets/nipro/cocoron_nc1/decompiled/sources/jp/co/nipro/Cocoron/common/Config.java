package jp.co.nipro.cocoron.common;

import androidx.lifecycle.CoroutineLiveDataKt;
import java.util.Objects;
import java.util.UUID;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Config.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0010\t\n\u0002\b\u001b\n\u0002\u0010\u0006\n\u0002\b\r\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b)\bÆ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0006\u0010r\u001a\u00020\u0010R\u0014\u0010\u0003\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0014\u0010\u0007\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0006R\u0014\u0010\t\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u0006R\u0014\u0010\u000b\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u0006R\u0014\u0010\r\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u0006R\u0014\u0010\u000f\u001a\u00020\u0010X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0014\u0010\u0013\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0006R\u0014\u0010\u0015\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0006R\u0014\u0010\u0017\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0006R\u0014\u0010\u0019\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0006R\u0014\u0010\u001b\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0006R\u0014\u0010\u001d\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0006R\u0014\u0010\u001f\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b \u0010\u0006R\u0014\u0010!\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0006R\u0014\u0010#\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b$\u0010\u0006R\u0014\u0010%\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b&\u0010\u0006R\u0014\u0010'\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b(\u0010\u0006R\u0014\u0010)\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b*\u0010\u0006R\u0014\u0010+\u001a\u00020,X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b-\u0010.R\u0014\u0010/\u001a\u00020\u0010X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b0\u0010\u0012R\u0014\u00101\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b2\u0010\u0006R\u0014\u00103\u001a\u00020\u0010X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b4\u0010\u0012R\u0014\u00105\u001a\u00020\u0010X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b6\u0010\u0012R\u0014\u00107\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b8\u0010\u0006R\u0019\u00109\u001a\b\u0012\u0004\u0012\u00020;0:¢\u0006\n\n\u0002\u0010>\u001a\u0004\b<\u0010=R\u0014\u0010?\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b@\u0010\u0006R\u0014\u0010A\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\bB\u0010\u0006R\u0014\u0010C\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\bD\u0010\u0006R\u0014\u0010E\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\bF\u0010\u0006R\u0014\u0010G\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\bH\u0010\u0006R\u0014\u0010I\u001a\u00020JX\u0086D¢\u0006\b\n\u0000\u001a\u0004\bK\u0010LR$\u0010N\u001a\u00020;2\u0006\u0010M\u001a\u00020;8F@FX\u0086\u000e¢\u0006\f\u001a\u0004\bO\u0010P\"\u0004\bQ\u0010RR$\u0010S\u001a\u00020\u00042\u0006\u0010M\u001a\u00020\u00048F@FX\u0086\u000e¢\u0006\f\u001a\u0004\bT\u0010\u0006\"\u0004\bU\u0010VR$\u0010W\u001a\u00020;2\u0006\u0010M\u001a\u00020;8F@FX\u0086\u000e¢\u0006\f\u001a\u0004\bX\u0010P\"\u0004\bY\u0010RR$\u0010Z\u001a\u00020\u00042\u0006\u0010M\u001a\u00020\u00048F@FX\u0086\u000e¢\u0006\f\u001a\u0004\b[\u0010\u0006\"\u0004\b\\\u0010VR$\u0010]\u001a\u00020\u00042\u0006\u0010M\u001a\u00020\u00048F@FX\u0086\u000e¢\u0006\f\u001a\u0004\b^\u0010\u0006\"\u0004\b_\u0010VR$\u0010`\u001a\u00020J2\u0006\u0010M\u001a\u00020J8F@FX\u0086\u000e¢\u0006\f\u001a\u0004\ba\u0010L\"\u0004\bb\u0010cR$\u0010d\u001a\u00020\u00042\u0006\u0010M\u001a\u00020\u00048F@FX\u0086\u000e¢\u0006\f\u001a\u0004\be\u0010\u0006\"\u0004\bf\u0010VR$\u0010g\u001a\u00020\u00042\u0006\u0010M\u001a\u00020\u00048F@FX\u0086\u000e¢\u0006\f\u001a\u0004\bh\u0010\u0006\"\u0004\bi\u0010VR$\u0010j\u001a\u00020\u00042\u0006\u0010M\u001a\u00020\u00048F@FX\u0086\u000e¢\u0006\f\u001a\u0004\bk\u0010\u0006\"\u0004\bl\u0010VR\u0011\u0010m\u001a\u00020\u00108F¢\u0006\u0006\u001a\u0004\bn\u0010\u0012R$\u0010o\u001a\u00020\u00042\u0006\u0010M\u001a\u00020\u00048F@FX\u0086\u000e¢\u0006\f\u001a\u0004\bp\u0010\u0006\"\u0004\bq\u0010V¨\u0006s"}, d2 = {"Ljp/co/nipro/cocoron/common/Config;", "", "()V", "BAR_COLOR_1", "", "getBAR_COLOR_1", "()I", "BAR_COLOR_2", "getBAR_COLOR_2", "BAR_COLOR_3", "getBAR_COLOR_3", "BAR_COLOR_4", "getBAR_COLOR_4", "BATTERY_WARNING_LEVEL", "getBATTERY_WARNING_LEVEL", "BLE_CHECK_INTERVAL", "", "getBLE_CHECK_INTERVAL", "()J", "BOTTOM_DEFAULT", "getBOTTOM_DEFAULT", "BOTTOM_LINE_COLOR", "getBOTTOM_LINE_COLOR", "BOTTOM_MAX", "getBOTTOM_MAX", "BOTTOM_MIN", "getBOTTOM_MIN", "ECG_DATA_COLOR", "getECG_DATA_COLOR", "ECG_LINEBREAK", "getECG_LINEBREAK", "HOLDER_SELECT_TEXT_COLOR", "getHOLDER_SELECT_TEXT_COLOR", "HOLDER_TEXT_COLOR", "getHOLDER_TEXT_COLOR", "LEFTAXIS_TEXT_COLOR", "getLEFTAXIS_TEXT_COLOR", "MARK_LINE_COLOR", "getMARK_LINE_COLOR", "MARK_TEXT_COLOR", "getMARK_TEXT_COLOR", "PICKER_TEXT_COLOR", "getPICKER_TEXT_COLOR", "RECONNECT_TIME", "", "getRECONNECT_TIME", "()D", "REMOVE_TIMECOUNT", "getREMOVE_TIMECOUNT", "RRI_SHOW_LIMIT", "getRRI_SHOW_LIMIT", "SCAN_DELAY_TIME", "getSCAN_DELAY_TIME", "SCAN_TIMEOUT", "getSCAN_TIMEOUT", "SEND_INTERVAL_DEFAULT", "getSEND_INTERVAL_DEFAULT", "SEND_INTERVAL_VALUE", "", "", "getSEND_INTERVAL_VALUE", "()[Ljava/lang/String;", "[Ljava/lang/String;", "TOP_DEFAULT", "getTOP_DEFAULT", "TOP_LINE_COLOR", "getTOP_LINE_COLOR", "TOP_MAX", "getTOP_MAX", "TOP_MIN", "getTOP_MIN", "TRIGGER_LINE_COLOR", "getTRIGGER_LINE_COLOR", "USE_ECG_SIM", "", "getUSE_ECG_SIM", "()Z", "newValue", "UUID", "getUUID", "()Ljava/lang/String;", "setUUID", "(Ljava/lang/String;)V", "bottom", "getBottom", "setBottom", "(I)V", "connectName", "getConnectName", "setConnectName", "daySelected", "getDaySelected", "setDaySelected", "ecgMode", "getEcgMode", "setEcgMode", "humNoiseFilter", "getHumNoiseFilter", "setHumNoiseFilter", "(Z)V", "mvSelected", "getMvSelected", "setMvSelected", "secSelected", "getSecSelected", "setSecSelected", "sendInterval", "getSendInterval", "setSendInterval", "sendIntervalRaw", "getSendIntervalRaw", "top", "getTop", "setTop", "rriTimeout", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class Config {
    private static final int BAR_COLOR_1 = 0;
    private static final int BAR_COLOR_2;
    private static final int BAR_COLOR_3;
    private static final int BAR_COLOR_4;
    private static final int HOLDER_SELECT_TEXT_COLOR;
    private static final int HOLDER_TEXT_COLOR;
    private static final int LEFTAXIS_TEXT_COLOR;
    private static final int MARK_LINE_COLOR;
    private static final int MARK_TEXT_COLOR;
    private static final int PICKER_TEXT_COLOR;
    private static final int SEND_INTERVAL_DEFAULT = 0;
    private static final int TOP_LINE_COLOR;
    private static final boolean USE_ECG_SIM = false;
    public static final Config INSTANCE = new Config();
    private static final long SCAN_DELAY_TIME = CoroutineLiveDataKt.DEFAULT_TIMEOUT;
    private static final long SCAN_TIMEOUT = 30000;
    private static final String[] SEND_INTERVAL_VALUE = {"60s", "30s", "10s", "5s"};
    private static final long BLE_CHECK_INTERVAL = 1000;
    private static final int TOP_MAX = 300;
    private static final int TOP_MIN = 30;
    private static final int TOP_DEFAULT = 30 - 1;
    private static final int BOTTOM_MAX = 300;
    private static final int BOTTOM_MIN = 30;
    private static final int BOTTOM_DEFAULT = 30 - 1;
    private static final int BATTERY_WARNING_LEVEL = 2300;
    private static final long REMOVE_TIMECOUNT = 94608000000L;
    private static final double RECONNECT_TIME = 1.0d;
    private static final int ECG_LINEBREAK = 10;
    private static final int RRI_SHOW_LIMIT = 15;
    private static final int TRIGGER_LINE_COLOR = (int) 3980139263L;
    private static final int BOTTOM_LINE_COLOR = (int) 4289789997L;
    private static final int ECG_DATA_COLOR = (int) 4294967040L;

    static {
        int i = (int) 4294967295L;
        LEFTAXIS_TEXT_COLOR = i;
        int i2 = (int) 4290052136L;
        BAR_COLOR_2 = i2;
        BAR_COLOR_3 = i;
        BAR_COLOR_4 = i2;
        int i3 = (int) 4279340515L;
        TOP_LINE_COLOR = i3;
        PICKER_TEXT_COLOR = i;
        HOLDER_TEXT_COLOR = i;
        HOLDER_SELECT_TEXT_COLOR = i3;
        MARK_LINE_COLOR = i2;
        MARK_TEXT_COLOR = i;
    }

    private Config() {
    }

    public final long getSCAN_DELAY_TIME() {
        return SCAN_DELAY_TIME;
    }

    public final long getSCAN_TIMEOUT() {
        return SCAN_TIMEOUT;
    }

    public final String[] getSEND_INTERVAL_VALUE() {
        return SEND_INTERVAL_VALUE;
    }

    public final int getSEND_INTERVAL_DEFAULT() {
        return SEND_INTERVAL_DEFAULT;
    }

    public final long getBLE_CHECK_INTERVAL() {
        return BLE_CHECK_INTERVAL;
    }

    public final int getTOP_MAX() {
        return TOP_MAX;
    }

    public final int getTOP_MIN() {
        return TOP_MIN;
    }

    public final int getTOP_DEFAULT() {
        return TOP_DEFAULT;
    }

    public final int getBOTTOM_MAX() {
        return BOTTOM_MAX;
    }

    public final int getBOTTOM_MIN() {
        return BOTTOM_MIN;
    }

    public final int getBOTTOM_DEFAULT() {
        return BOTTOM_DEFAULT;
    }

    public final int getBATTERY_WARNING_LEVEL() {
        return BATTERY_WARNING_LEVEL;
    }

    public final long getREMOVE_TIMECOUNT() {
        return REMOVE_TIMECOUNT;
    }

    public final double getRECONNECT_TIME() {
        return RECONNECT_TIME;
    }

    public final int getECG_LINEBREAK() {
        return ECG_LINEBREAK;
    }

    public final boolean getUSE_ECG_SIM() {
        return USE_ECG_SIM;
    }

    public final int getRRI_SHOW_LIMIT() {
        return RRI_SHOW_LIMIT;
    }

    public final int getTRIGGER_LINE_COLOR() {
        return TRIGGER_LINE_COLOR;
    }

    public final int getLEFTAXIS_TEXT_COLOR() {
        return LEFTAXIS_TEXT_COLOR;
    }

    public final int getBAR_COLOR_1() {
        return BAR_COLOR_1;
    }

    public final int getBAR_COLOR_2() {
        return BAR_COLOR_2;
    }

    public final int getBAR_COLOR_3() {
        return BAR_COLOR_3;
    }

    public final int getBAR_COLOR_4() {
        return BAR_COLOR_4;
    }

    public final int getTOP_LINE_COLOR() {
        return TOP_LINE_COLOR;
    }

    public final int getBOTTOM_LINE_COLOR() {
        return BOTTOM_LINE_COLOR;
    }

    public final int getECG_DATA_COLOR() {
        return ECG_DATA_COLOR;
    }

    public final int getPICKER_TEXT_COLOR() {
        return PICKER_TEXT_COLOR;
    }

    public final int getHOLDER_TEXT_COLOR() {
        return HOLDER_TEXT_COLOR;
    }

    public final int getHOLDER_SELECT_TEXT_COLOR() {
        return HOLDER_SELECT_TEXT_COLOR;
    }

    public final int getMARK_LINE_COLOR() {
        return MARK_LINE_COLOR;
    }

    public final int getMARK_TEXT_COLOR() {
        return MARK_TEXT_COLOR;
    }

    public final int getDaySelected() {
        return AppPrefsUtils.getInt$default(AppPrefsUtils.INSTANCE, "daySelected", 0, 2, null);
    }

    public final void setDaySelected(int i) {
        AppPrefsUtils.INSTANCE.putInt("daySelected", i);
    }

    public final int getSecSelected() {
        return AppPrefsUtils.INSTANCE.getInt("secSelected", 1);
    }

    public final void setSecSelected(int i) {
        AppPrefsUtils.INSTANCE.putInt("secSelected", i);
    }

    public final int getMvSelected() {
        return AppPrefsUtils.INSTANCE.getInt("mvSelected", 1);
    }

    public final void setMvSelected(int i) {
        AppPrefsUtils.INSTANCE.putInt("mvSelected", i);
    }

    public final int getTop() {
        return AppPrefsUtils.INSTANCE.getInt("top", TOP_DEFAULT);
    }

    public final void setTop(int i) {
        AppPrefsUtils.INSTANCE.putInt("top", i);
    }

    public final int getBottom() {
        return AppPrefsUtils.INSTANCE.getInt("bottom", BOTTOM_DEFAULT);
    }

    public final void setBottom(int i) {
        AppPrefsUtils.INSTANCE.putInt("bottom", i);
    }

    public final int getSendInterval() {
        return AppPrefsUtils.INSTANCE.getInt("sendInterval", SEND_INTERVAL_DEFAULT);
    }

    public final void setSendInterval(int i) {
        AppPrefsUtils.INSTANCE.putInt("sendInterval", i);
    }

    public final String getUUID() {
        String string$default = AppPrefsUtils.getString$default(AppPrefsUtils.INSTANCE, "UUID", null, 2, null);
        if (string$default == null) {
            String uuid = UUID.randomUUID().toString();
            Intrinsics.checkNotNullExpressionValue(uuid, "java.util.UUID.randomUUID().toString()");
            Objects.requireNonNull(uuid, "null cannot be cast to non-null type java.lang.String");
            String lowerCase = uuid.toLowerCase();
            Intrinsics.checkNotNullExpressionValue(lowerCase, "(this as java.lang.String).toLowerCase()");
            Objects.requireNonNull(lowerCase, "null cannot be cast to non-null type java.lang.String");
            string$default = lowerCase.substring(0, 8);
            Intrinsics.checkNotNullExpressionValue(string$default, "(this as java.lang.Strin…ing(startIndex, endIndex)");
        }
        AppPrefsUtils.INSTANCE.putString("UUID", string$default);
        return string$default;
    }

    public final void setUUID(String newValue) {
        Intrinsics.checkNotNullParameter(newValue, "newValue");
        AppPrefsUtils.INSTANCE.putString("UUID", newValue);
    }

    public final String getConnectName() {
        String string$default = AppPrefsUtils.getString$default(AppPrefsUtils.INSTANCE, "connectName", null, 2, null);
        return string$default != null ? string$default : "";
    }

    public final void setConnectName(String newValue) {
        Intrinsics.checkNotNullParameter(newValue, "newValue");
        AppPrefsUtils.INSTANCE.putString("connectName", newValue);
    }

    public final long getSendIntervalRaw() {
        if (getSendInterval() == 0) {
            return 60L;
        }
        if (getSendInterval() == 1) {
            return 30L;
        }
        if (getSendInterval() == 2) {
            return 10L;
        }
        return getSendInterval() == 3 ? 5L : 60L;
    }

    public final long rriTimeout() {
        if (getSendInterval() == 0) {
            return 210L;
        }
        if (getSendInterval() == 1) {
            return 105L;
        }
        return (getSendInterval() == 2 || getSendInterval() == 3) ? 60L : 210L;
    }

    public final int getEcgMode() {
        return AppPrefsUtils.getInt$default(AppPrefsUtils.INSTANCE, "ecgMode", 0, 2, null);
    }

    public final void setEcgMode(int i) {
        AppPrefsUtils.INSTANCE.putInt("ecgMode", i);
    }

    public final boolean getHumNoiseFilter() {
        return AppPrefsUtils.getBoolean$default(AppPrefsUtils.INSTANCE, "hum_noise_filter", false, 2, null);
    }

    public final void setHumNoiseFilter(boolean z) {
        AppPrefsUtils.INSTANCE.putBoolean("hum_noise_filter", z);
    }
}
