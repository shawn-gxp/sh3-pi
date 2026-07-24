package kotlin.time;

import com.github.mikephil.charting.utils.Utils;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.functions.Function5;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;

/* compiled from: Duration.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0010\u000f\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b&\n\u0002\u0010\u000b\n\u0002\u0010\u0000\n\u0002\b\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0012\b\u0087@\u0018\u0000 v2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001vB\u0014\b\u0000\u0012\u0006\u0010\u0002\u001a\u00020\u0003ø\u0001\u0000¢\u0006\u0004\b\u0004\u0010\u0005J\u001b\u0010%\u001a\u00020\t2\u0006\u0010&\u001a\u00020\u0000H\u0096\u0002ø\u0001\u0000¢\u0006\u0004\b'\u0010(J\u001e\u0010)\u001a\u00020\u00002\u0006\u0010*\u001a\u00020\u0003H\u0086\u0002ø\u0001\u0000ø\u0001\u0001¢\u0006\u0004\b+\u0010,J\u001e\u0010)\u001a\u00020\u00002\u0006\u0010*\u001a\u00020\tH\u0086\u0002ø\u0001\u0000ø\u0001\u0001¢\u0006\u0004\b+\u0010-J\u001b\u0010)\u001a\u00020\u00032\u0006\u0010&\u001a\u00020\u0000H\u0086\u0002ø\u0001\u0000¢\u0006\u0004\b.\u0010,J\u001a\u0010/\u001a\u0002002\b\u0010&\u001a\u0004\u0018\u000101HÖ\u0003¢\u0006\u0004\b2\u00103J\u0010\u00104\u001a\u00020\tHÖ\u0001¢\u0006\u0004\b5\u0010\rJ\r\u00106\u001a\u000200¢\u0006\u0004\b7\u00108J\r\u00109\u001a\u000200¢\u0006\u0004\b:\u00108J\r\u0010;\u001a\u000200¢\u0006\u0004\b<\u00108J\r\u0010=\u001a\u000200¢\u0006\u0004\b>\u00108J\u001b\u0010?\u001a\u00020\u00002\u0006\u0010&\u001a\u00020\u0000H\u0086\u0002ø\u0001\u0000¢\u0006\u0004\b@\u0010,J\u001b\u0010A\u001a\u00020\u00002\u0006\u0010&\u001a\u00020\u0000H\u0086\u0002ø\u0001\u0000¢\u0006\u0004\bB\u0010,J\u0017\u0010C\u001a\u00020\t2\u0006\u0010\u0002\u001a\u00020\u0003H\u0002¢\u0006\u0004\bD\u0010(J\u001e\u0010E\u001a\u00020\u00002\u0006\u0010*\u001a\u00020\u0003H\u0086\u0002ø\u0001\u0000ø\u0001\u0001¢\u0006\u0004\bF\u0010,J\u001e\u0010E\u001a\u00020\u00002\u0006\u0010*\u001a\u00020\tH\u0086\u0002ø\u0001\u0000ø\u0001\u0001¢\u0006\u0004\bF\u0010-J\u009d\u0001\u0010G\u001a\u0002HH\"\u0004\b\u0000\u0010H2u\u0010I\u001aq\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(M\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(N\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(O\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(P\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(Q\u0012\u0004\u0012\u0002HH0JH\u0086\bø\u0001\u0002\u0082\u0002\n\n\b\b\u0001\u0012\u0002\u0010\u0001 \u0001¢\u0006\u0004\bR\u0010SJ\u0088\u0001\u0010G\u001a\u0002HH\"\u0004\b\u0000\u0010H2`\u0010I\u001a\\\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(N\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(O\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(P\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(Q\u0012\u0004\u0012\u0002HH0TH\u0086\bø\u0001\u0002\u0082\u0002\n\n\b\b\u0001\u0012\u0002\u0010\u0001 \u0001¢\u0006\u0004\bR\u0010UJs\u0010G\u001a\u0002HH\"\u0004\b\u0000\u0010H2K\u0010I\u001aG\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(O\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(P\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(Q\u0012\u0004\u0012\u0002HH0VH\u0086\bø\u0001\u0002\u0082\u0002\n\n\b\b\u0001\u0012\u0002\u0010\u0001 \u0001¢\u0006\u0004\bR\u0010WJ^\u0010G\u001a\u0002HH\"\u0004\b\u0000\u0010H26\u0010I\u001a2\u0012\u0013\u0012\u00110Y¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(P\u0012\u0013\u0012\u00110\t¢\u0006\f\bK\u0012\b\bL\u0012\u0004\b\b(Q\u0012\u0004\u0012\u0002HH0XH\u0086\bø\u0001\u0002\u0082\u0002\n\n\b\b\u0001\u0012\u0002\u0010\u0001 \u0001¢\u0006\u0004\bR\u0010ZJ\u0019\u0010[\u001a\u00020\u00032\n\u0010\\\u001a\u00060]j\u0002`^¢\u0006\u0004\b_\u0010`J\u0019\u0010a\u001a\u00020\t2\n\u0010\\\u001a\u00060]j\u0002`^¢\u0006\u0004\bb\u0010cJ\r\u0010d\u001a\u00020e¢\u0006\u0004\bf\u0010gJ\u0019\u0010h\u001a\u00020Y2\n\u0010\\\u001a\u00060]j\u0002`^¢\u0006\u0004\bi\u0010jJ\r\u0010k\u001a\u00020Y¢\u0006\u0004\bl\u0010mJ\r\u0010n\u001a\u00020Y¢\u0006\u0004\bo\u0010mJ\u000f\u0010p\u001a\u00020eH\u0016¢\u0006\u0004\bq\u0010gJ#\u0010p\u001a\u00020e2\n\u0010\\\u001a\u00060]j\u0002`^2\b\b\u0002\u0010r\u001a\u00020\t¢\u0006\u0004\bq\u0010sJ\u0016\u0010t\u001a\u00020\u0000H\u0086\u0002ø\u0001\u0000ø\u0001\u0001¢\u0006\u0004\bu\u0010\u0005R\u0017\u0010\u0006\u001a\u00020\u00008Fø\u0001\u0000ø\u0001\u0001¢\u0006\u0006\u001a\u0004\b\u0007\u0010\u0005R\u001a\u0010\b\u001a\u00020\t8@X\u0081\u0004¢\u0006\f\u0012\u0004\b\n\u0010\u000b\u001a\u0004\b\f\u0010\rR\u0011\u0010\u000e\u001a\u00020\u00038F¢\u0006\u0006\u001a\u0004\b\u000f\u0010\u0005R\u0011\u0010\u0010\u001a\u00020\u00038F¢\u0006\u0006\u001a\u0004\b\u0011\u0010\u0005R\u0011\u0010\u0012\u001a\u00020\u00038F¢\u0006\u0006\u001a\u0004\b\u0013\u0010\u0005R\u0011\u0010\u0014\u001a\u00020\u00038F¢\u0006\u0006\u001a\u0004\b\u0015\u0010\u0005R\u0011\u0010\u0016\u001a\u00020\u00038F¢\u0006\u0006\u001a\u0004\b\u0017\u0010\u0005R\u0011\u0010\u0018\u001a\u00020\u00038F¢\u0006\u0006\u001a\u0004\b\u0019\u0010\u0005R\u0011\u0010\u001a\u001a\u00020\u00038F¢\u0006\u0006\u001a\u0004\b\u001b\u0010\u0005R\u001a\u0010\u001c\u001a\u00020\t8@X\u0081\u0004¢\u0006\f\u0012\u0004\b\u001d\u0010\u000b\u001a\u0004\b\u001e\u0010\rR\u001a\u0010\u001f\u001a\u00020\t8@X\u0081\u0004¢\u0006\f\u0012\u0004\b \u0010\u000b\u001a\u0004\b!\u0010\rR\u001a\u0010\"\u001a\u00020\t8@X\u0081\u0004¢\u0006\f\u0012\u0004\b#\u0010\u000b\u001a\u0004\b$\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0080\u0004¢\u0006\u0002\n\u0000ø\u0001\u0000\u0082\u0002\u000f\n\u0002\b\u0019\n\u0002\b!\n\u0005\b\u009920\u0001¨\u0006w"}, d2 = {"Lkotlin/time/Duration;", "", "value", "", "constructor-impl", "(D)D", "absoluteValue", "getAbsoluteValue-UwyO8pc", "hoursComponent", "", "getHoursComponent$annotations", "()V", "getHoursComponent-impl", "(D)I", "inDays", "getInDays-impl", "inHours", "getInHours-impl", "inMicroseconds", "getInMicroseconds-impl", "inMilliseconds", "getInMilliseconds-impl", "inMinutes", "getInMinutes-impl", "inNanoseconds", "getInNanoseconds-impl", "inSeconds", "getInSeconds-impl", "minutesComponent", "getMinutesComponent$annotations", "getMinutesComponent-impl", "nanosecondsComponent", "getNanosecondsComponent$annotations", "getNanosecondsComponent-impl", "secondsComponent", "getSecondsComponent$annotations", "getSecondsComponent-impl", "compareTo", "other", "compareTo-LRDsOJo", "(DD)I", "div", "scale", "div-UwyO8pc", "(DD)D", "(DI)D", "div-LRDsOJo", "equals", "", "", "equals-impl", "(DLjava/lang/Object;)Z", "hashCode", "hashCode-impl", "isFinite", "isFinite-impl", "(D)Z", "isInfinite", "isInfinite-impl", "isNegative", "isNegative-impl", "isPositive", "isPositive-impl", "minus", "minus-LRDsOJo", "plus", "plus-LRDsOJo", "precision", "precision-impl", "times", "times-UwyO8pc", "toComponents", "T", "action", "Lkotlin/Function5;", "Lkotlin/ParameterName;", "name", "days", "hours", "minutes", "seconds", "nanoseconds", "toComponents-impl", "(DLkotlin/jvm/functions/Function5;)Ljava/lang/Object;", "Lkotlin/Function4;", "(DLkotlin/jvm/functions/Function4;)Ljava/lang/Object;", "Lkotlin/Function3;", "(DLkotlin/jvm/functions/Function3;)Ljava/lang/Object;", "Lkotlin/Function2;", "", "(DLkotlin/jvm/functions/Function2;)Ljava/lang/Object;", "toDouble", "unit", "Ljava/util/concurrent/TimeUnit;", "Lkotlin/time/DurationUnit;", "toDouble-impl", "(DLjava/util/concurrent/TimeUnit;)D", "toInt", "toInt-impl", "(DLjava/util/concurrent/TimeUnit;)I", "toIsoString", "", "toIsoString-impl", "(D)Ljava/lang/String;", "toLong", "toLong-impl", "(DLjava/util/concurrent/TimeUnit;)J", "toLongMilliseconds", "toLongMilliseconds-impl", "(D)J", "toLongNanoseconds", "toLongNanoseconds-impl", "toString", "toString-impl", "decimals", "(DLjava/util/concurrent/TimeUnit;I)Ljava/lang/String;", "unaryMinus", "unaryMinus-UwyO8pc", "Companion", "kotlin-stdlib"}, k = 1, mv = {1, 4, 1})
/* loaded from: classes.dex */
public final class Duration implements Comparable<Duration> {
    private final double value;

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static final double ZERO = m1273constructorimpl(Utils.DOUBLE_EPSILON);
    private static final double INFINITE = m1273constructorimpl(Double.POSITIVE_INFINITY);

    /* renamed from: box-impl, reason: not valid java name */
    public static final /* synthetic */ Duration m1271boximpl(double d) {
        return new Duration(d);
    }

    /* renamed from: constructor-impl, reason: not valid java name */
    public static double m1273constructorimpl(double d) {
        return d;
    }

    /* renamed from: div-LRDsOJo, reason: not valid java name */
    public static final double m1274divLRDsOJo(double d, double d2) {
        return d / d2;
    }

    /* renamed from: equals-impl, reason: not valid java name */
    public static boolean m1277equalsimpl(double d, Object obj) {
        return (obj instanceof Duration) && Double.compare(d, ((Duration) obj).getValue()) == 0;
    }

    /* renamed from: equals-impl0, reason: not valid java name */
    public static final boolean m1278equalsimpl0(double d, double d2) {
        return Double.compare(d, d2) == 0;
    }

    public static /* synthetic */ void getHoursComponent$annotations() {
    }

    public static /* synthetic */ void getMinutesComponent$annotations() {
    }

    public static /* synthetic */ void getNanosecondsComponent$annotations() {
    }

    public static /* synthetic */ void getSecondsComponent$annotations() {
    }

    /* renamed from: hashCode-impl, reason: not valid java name */
    public static int m1291hashCodeimpl(double d) {
        long doubleToLongBits = Double.doubleToLongBits(d);
        return (int) (doubleToLongBits ^ (doubleToLongBits >>> 32));
    }

    /* renamed from: isNegative-impl, reason: not valid java name */
    public static final boolean m1294isNegativeimpl(double d) {
        return d < ((double) 0);
    }

    /* renamed from: isPositive-impl, reason: not valid java name */
    public static final boolean m1295isPositiveimpl(double d) {
        return d > ((double) 0);
    }

    /* renamed from: precision-impl, reason: not valid java name */
    private static final int m1298precisionimpl(double d, double d2) {
        if (d2 < 1) {
            return 3;
        }
        if (d2 < 10) {
            return 2;
        }
        return d2 < ((double) 100) ? 1 : 0;
    }

    /* renamed from: compareTo-LRDsOJo, reason: not valid java name */
    public int m1315compareToLRDsOJo(double d) {
        return m1272compareToLRDsOJo(this.value, d);
    }

    public boolean equals(Object obj) {
        return m1277equalsimpl(this.value, obj);
    }

    public int hashCode() {
        return m1291hashCodeimpl(this.value);
    }

    public String toString() {
        return m1311toStringimpl(this.value);
    }

    /* renamed from: unbox-impl, reason: not valid java name and from getter */
    public final /* synthetic */ double getValue() {
        return this.value;
    }

    private /* synthetic */ Duration(double d) {
        this.value = d;
    }

    @Override // java.lang.Comparable
    public /* bridge */ /* synthetic */ int compareTo(Duration duration) {
        return m1315compareToLRDsOJo(duration.getValue());
    }

    /* compiled from: Duration.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J&\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000b2\n\u0010\r\u001a\u00060\u000ej\u0002`\u000f2\n\u0010\u0010\u001a\u00060\u000ej\u0002`\u000fR\u0019\u0010\u0003\u001a\u00020\u0004ø\u0001\u0000ø\u0001\u0001¢\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u0005\u0010\u0006R\u0019\u0010\b\u001a\u00020\u0004ø\u0001\u0000ø\u0001\u0001¢\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\t\u0010\u0006\u0082\u0002\b\n\u0002\b\u0019\n\u0002\b!¨\u0006\u0011"}, d2 = {"Lkotlin/time/Duration$Companion;", "", "()V", "INFINITE", "Lkotlin/time/Duration;", "getINFINITE-UwyO8pc", "()D", "D", "ZERO", "getZERO-UwyO8pc", "convert", "", "value", "sourceUnit", "Ljava/util/concurrent/TimeUnit;", "Lkotlin/time/DurationUnit;", "targetUnit", "kotlin-stdlib"}, k = 1, mv = {1, 4, 1})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        /* renamed from: getZERO-UwyO8pc, reason: not valid java name */
        public final double m1318getZEROUwyO8pc() {
            return Duration.ZERO;
        }

        /* renamed from: getINFINITE-UwyO8pc, reason: not valid java name */
        public final double m1317getINFINITEUwyO8pc() {
            return Duration.INFINITE;
        }

        public final double convert(double value, TimeUnit sourceUnit, TimeUnit targetUnit) {
            Intrinsics.checkNotNullParameter(sourceUnit, "sourceUnit");
            Intrinsics.checkNotNullParameter(targetUnit, "targetUnit");
            return DurationUnitKt.convertDurationUnit(value, sourceUnit, targetUnit);
        }
    }

    /* renamed from: unaryMinus-UwyO8pc, reason: not valid java name */
    public static final double m1314unaryMinusUwyO8pc(double d) {
        return m1273constructorimpl(-d);
    }

    /* renamed from: plus-LRDsOJo, reason: not valid java name */
    public static final double m1297plusLRDsOJo(double d, double d2) {
        return m1273constructorimpl(d + d2);
    }

    /* renamed from: minus-LRDsOJo, reason: not valid java name */
    public static final double m1296minusLRDsOJo(double d, double d2) {
        return m1273constructorimpl(d - d2);
    }

    /* renamed from: times-UwyO8pc, reason: not valid java name */
    public static final double m1300timesUwyO8pc(double d, int i) {
        return m1273constructorimpl(d * i);
    }

    /* renamed from: times-UwyO8pc, reason: not valid java name */
    public static final double m1299timesUwyO8pc(double d, double d2) {
        return m1273constructorimpl(d * d2);
    }

    /* renamed from: div-UwyO8pc, reason: not valid java name */
    public static final double m1276divUwyO8pc(double d, int i) {
        return m1273constructorimpl(d / i);
    }

    /* renamed from: div-UwyO8pc, reason: not valid java name */
    public static final double m1275divUwyO8pc(double d, double d2) {
        return m1273constructorimpl(d / d2);
    }

    /* renamed from: isInfinite-impl, reason: not valid java name */
    public static final boolean m1293isInfiniteimpl(double d) {
        return Double.isInfinite(d);
    }

    /* renamed from: isFinite-impl, reason: not valid java name */
    public static final boolean m1292isFiniteimpl(double d) {
        return (Double.isInfinite(d) || Double.isNaN(d)) ? false : true;
    }

    /* renamed from: getAbsoluteValue-UwyO8pc, reason: not valid java name */
    public static final double m1279getAbsoluteValueUwyO8pc(double d) {
        return m1294isNegativeimpl(d) ? m1314unaryMinusUwyO8pc(d) : d;
    }

    /* renamed from: compareTo-LRDsOJo, reason: not valid java name */
    public static int m1272compareToLRDsOJo(double d, double d2) {
        return Double.compare(d, d2);
    }

    /* renamed from: toComponents-impl, reason: not valid java name */
    public static final <T> T m1304toComponentsimpl(double d, Function5<? super Integer, ? super Integer, ? super Integer, ? super Integer, ? super Integer, ? extends T> action) {
        Intrinsics.checkNotNullParameter(action, "action");
        return action.invoke(Integer.valueOf((int) m1281getInDaysimpl(d)), Integer.valueOf(m1280getHoursComponentimpl(d)), Integer.valueOf(m1288getMinutesComponentimpl(d)), Integer.valueOf(m1290getSecondsComponentimpl(d)), Integer.valueOf(m1289getNanosecondsComponentimpl(d)));
    }

    /* renamed from: toComponents-impl, reason: not valid java name */
    public static final <T> T m1303toComponentsimpl(double d, Function4<? super Integer, ? super Integer, ? super Integer, ? super Integer, ? extends T> action) {
        Intrinsics.checkNotNullParameter(action, "action");
        return action.invoke(Integer.valueOf((int) m1282getInHoursimpl(d)), Integer.valueOf(m1288getMinutesComponentimpl(d)), Integer.valueOf(m1290getSecondsComponentimpl(d)), Integer.valueOf(m1289getNanosecondsComponentimpl(d)));
    }

    /* renamed from: toComponents-impl, reason: not valid java name */
    public static final <T> T m1302toComponentsimpl(double d, Function3<? super Integer, ? super Integer, ? super Integer, ? extends T> action) {
        Intrinsics.checkNotNullParameter(action, "action");
        return action.invoke(Integer.valueOf((int) m1285getInMinutesimpl(d)), Integer.valueOf(m1290getSecondsComponentimpl(d)), Integer.valueOf(m1289getNanosecondsComponentimpl(d)));
    }

    /* renamed from: toComponents-impl, reason: not valid java name */
    public static final <T> T m1301toComponentsimpl(double d, Function2<? super Long, ? super Integer, ? extends T> action) {
        Intrinsics.checkNotNullParameter(action, "action");
        return action.invoke(Long.valueOf((long) m1287getInSecondsimpl(d)), Integer.valueOf(m1289getNanosecondsComponentimpl(d)));
    }

    /* renamed from: getHoursComponent-impl, reason: not valid java name */
    public static final int m1280getHoursComponentimpl(double d) {
        return (int) (m1282getInHoursimpl(d) % 24);
    }

    /* renamed from: getMinutesComponent-impl, reason: not valid java name */
    public static final int m1288getMinutesComponentimpl(double d) {
        return (int) (m1285getInMinutesimpl(d) % 60);
    }

    /* renamed from: getSecondsComponent-impl, reason: not valid java name */
    public static final int m1290getSecondsComponentimpl(double d) {
        return (int) (m1287getInSecondsimpl(d) % 60);
    }

    /* renamed from: getNanosecondsComponent-impl, reason: not valid java name */
    public static final int m1289getNanosecondsComponentimpl(double d) {
        return (int) (m1286getInNanosecondsimpl(d) % 1.0E9d);
    }

    /* renamed from: toDouble-impl, reason: not valid java name */
    public static final double m1305toDoubleimpl(double d, TimeUnit unit) {
        TimeUnit storageUnit;
        Intrinsics.checkNotNullParameter(unit, "unit");
        storageUnit = DurationKt.getStorageUnit();
        return DurationUnitKt.convertDurationUnit(d, storageUnit, unit);
    }

    /* renamed from: toLong-impl, reason: not valid java name */
    public static final long m1308toLongimpl(double d, TimeUnit unit) {
        Intrinsics.checkNotNullParameter(unit, "unit");
        return (long) m1305toDoubleimpl(d, unit);
    }

    /* renamed from: toInt-impl, reason: not valid java name */
    public static final int m1306toIntimpl(double d, TimeUnit unit) {
        Intrinsics.checkNotNullParameter(unit, "unit");
        return (int) m1305toDoubleimpl(d, unit);
    }

    /* renamed from: getInDays-impl, reason: not valid java name */
    public static final double m1281getInDaysimpl(double d) {
        return m1305toDoubleimpl(d, TimeUnit.DAYS);
    }

    /* renamed from: getInHours-impl, reason: not valid java name */
    public static final double m1282getInHoursimpl(double d) {
        return m1305toDoubleimpl(d, TimeUnit.HOURS);
    }

    /* renamed from: getInMinutes-impl, reason: not valid java name */
    public static final double m1285getInMinutesimpl(double d) {
        return m1305toDoubleimpl(d, TimeUnit.MINUTES);
    }

    /* renamed from: getInSeconds-impl, reason: not valid java name */
    public static final double m1287getInSecondsimpl(double d) {
        return m1305toDoubleimpl(d, TimeUnit.SECONDS);
    }

    /* renamed from: getInMilliseconds-impl, reason: not valid java name */
    public static final double m1284getInMillisecondsimpl(double d) {
        return m1305toDoubleimpl(d, TimeUnit.MILLISECONDS);
    }

    /* renamed from: getInMicroseconds-impl, reason: not valid java name */
    public static final double m1283getInMicrosecondsimpl(double d) {
        return m1305toDoubleimpl(d, TimeUnit.MICROSECONDS);
    }

    /* renamed from: getInNanoseconds-impl, reason: not valid java name */
    public static final double m1286getInNanosecondsimpl(double d) {
        return m1305toDoubleimpl(d, TimeUnit.NANOSECONDS);
    }

    /* renamed from: toLongNanoseconds-impl, reason: not valid java name */
    public static final long m1310toLongNanosecondsimpl(double d) {
        return m1308toLongimpl(d, TimeUnit.NANOSECONDS);
    }

    /* renamed from: toLongMilliseconds-impl, reason: not valid java name */
    public static final long m1309toLongMillisecondsimpl(double d) {
        return m1308toLongimpl(d, TimeUnit.MILLISECONDS);
    }

    /* JADX WARN: Removed duplicated region for block: B:15:0x009b  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x00a0  */
    /* renamed from: toString-impl, reason: not valid java name */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static String m1311toStringimpl(double d) {
        TimeUnit timeUnit;
        int i;
        String formatToExactDecimals;
        if (m1293isInfiniteimpl(d)) {
            return String.valueOf(d);
        }
        if (d == Utils.DOUBLE_EPSILON) {
            return "0s";
        }
        double m1286getInNanosecondsimpl = m1286getInNanosecondsimpl(m1279getAbsoluteValueUwyO8pc(d));
        boolean z = false;
        if (m1286getInNanosecondsimpl < 1.0E-6d) {
            timeUnit = TimeUnit.SECONDS;
        } else {
            if (m1286getInNanosecondsimpl < 1) {
                timeUnit = TimeUnit.NANOSECONDS;
                i = 7;
            } else {
                if (m1286getInNanosecondsimpl < 1000.0d) {
                    timeUnit = TimeUnit.NANOSECONDS;
                } else if (m1286getInNanosecondsimpl < 1000000.0d) {
                    timeUnit = TimeUnit.MICROSECONDS;
                } else if (m1286getInNanosecondsimpl < 1.0E9d) {
                    timeUnit = TimeUnit.MILLISECONDS;
                } else if (m1286getInNanosecondsimpl < 1.0E12d) {
                    timeUnit = TimeUnit.SECONDS;
                } else if (m1286getInNanosecondsimpl < 6.0E13d) {
                    timeUnit = TimeUnit.MINUTES;
                } else if (m1286getInNanosecondsimpl < 3.6E15d) {
                    timeUnit = TimeUnit.HOURS;
                } else if (m1286getInNanosecondsimpl < 8.64E20d) {
                    timeUnit = TimeUnit.DAYS;
                } else {
                    timeUnit = TimeUnit.DAYS;
                }
                i = 0;
            }
            double m1305toDoubleimpl = m1305toDoubleimpl(d, timeUnit);
            StringBuilder sb = new StringBuilder();
            if (!z) {
                formatToExactDecimals = FormatToDecimalsKt.formatScientific(m1305toDoubleimpl);
            } else if (i > 0) {
                formatToExactDecimals = FormatToDecimalsKt.formatUpToDecimals(m1305toDoubleimpl, i);
            } else {
                formatToExactDecimals = FormatToDecimalsKt.formatToExactDecimals(m1305toDoubleimpl, m1298precisionimpl(d, Math.abs(m1305toDoubleimpl)));
            }
            sb.append(formatToExactDecimals);
            sb.append(DurationUnitKt.shortName(timeUnit));
            return sb.toString();
        }
        i = 0;
        z = true;
        double m1305toDoubleimpl2 = m1305toDoubleimpl(d, timeUnit);
        StringBuilder sb2 = new StringBuilder();
        if (!z) {
        }
        sb2.append(formatToExactDecimals);
        sb2.append(DurationUnitKt.shortName(timeUnit));
        return sb2.toString();
    }

    /* renamed from: toString-impl$default, reason: not valid java name */
    public static /* synthetic */ String m1313toStringimpl$default(double d, TimeUnit timeUnit, int i, int i2, Object obj) {
        if ((i2 & 2) != 0) {
            i = 0;
        }
        return m1312toStringimpl(d, timeUnit, i);
    }

    /* renamed from: toString-impl, reason: not valid java name */
    public static final String m1312toStringimpl(double d, TimeUnit unit, int i) {
        Intrinsics.checkNotNullParameter(unit, "unit");
        if (!(i >= 0)) {
            throw new IllegalArgumentException(("decimals must be not negative, but was " + i).toString());
        }
        if (m1293isInfiniteimpl(d)) {
            return String.valueOf(d);
        }
        double m1305toDoubleimpl = m1305toDoubleimpl(d, unit);
        StringBuilder sb = new StringBuilder();
        sb.append(Math.abs(m1305toDoubleimpl) < 1.0E14d ? FormatToDecimalsKt.formatToExactDecimals(m1305toDoubleimpl, RangesKt.coerceAtMost(i, 12)) : FormatToDecimalsKt.formatScientific(m1305toDoubleimpl));
        sb.append(DurationUnitKt.shortName(unit));
        return sb.toString();
    }

    /* renamed from: toIsoString-impl, reason: not valid java name */
    public static final String m1307toIsoStringimpl(double d) {
        StringBuilder sb = new StringBuilder();
        if (m1294isNegativeimpl(d)) {
            sb.append('-');
        }
        sb.append("PT");
        double m1279getAbsoluteValueUwyO8pc = m1279getAbsoluteValueUwyO8pc(d);
        int m1282getInHoursimpl = (int) m1282getInHoursimpl(m1279getAbsoluteValueUwyO8pc);
        int m1288getMinutesComponentimpl = m1288getMinutesComponentimpl(m1279getAbsoluteValueUwyO8pc);
        int m1290getSecondsComponentimpl = m1290getSecondsComponentimpl(m1279getAbsoluteValueUwyO8pc);
        int m1289getNanosecondsComponentimpl = m1289getNanosecondsComponentimpl(m1279getAbsoluteValueUwyO8pc);
        boolean z = true;
        boolean z2 = m1282getInHoursimpl != 0;
        boolean z3 = (m1290getSecondsComponentimpl == 0 && m1289getNanosecondsComponentimpl == 0) ? false : true;
        if (m1288getMinutesComponentimpl == 0 && (!z3 || !z2)) {
            z = false;
        }
        if (z2) {
            sb.append(m1282getInHoursimpl);
            sb.append('H');
        }
        if (z) {
            sb.append(m1288getMinutesComponentimpl);
            sb.append('M');
        }
        if (z3 || (!z2 && !z)) {
            sb.append(m1290getSecondsComponentimpl);
            if (m1289getNanosecondsComponentimpl != 0) {
                sb.append('.');
                String padStart = StringsKt.padStart(String.valueOf(m1289getNanosecondsComponentimpl), 9, '0');
                if (m1289getNanosecondsComponentimpl % 1000000 == 0) {
                    sb.append((CharSequence) padStart, 0, 3);
                    Intrinsics.checkNotNullExpressionValue(sb, "this.append(value, startIndex, endIndex)");
                } else if (m1289getNanosecondsComponentimpl % 1000 == 0) {
                    sb.append((CharSequence) padStart, 0, 6);
                    Intrinsics.checkNotNullExpressionValue(sb, "this.append(value, startIndex, endIndex)");
                } else {
                    sb.append(padStart);
                }
            }
            sb.append('S');
        }
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "StringBuilder().apply(builderAction).toString()");
        return sb2;
    }
}
