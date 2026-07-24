package jp.co.nipro.cocoron.ui.viewmodel;

import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.CoroutineLiveDataKt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import jp.co.nipro.cocoron.common.Config;
import jp.co.nipro.cocoron.common.Event;
import jp.co.nipro.cocoron.common.extension.ExtensionKt;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri12hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri24hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri7dDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRriDto;
import jp.co.nipro.cocoron.data.entity.ECGData;
import jp.co.nipro.cocoron.data.value.ECGMeasurementCharacteristicValue;
import jp.co.nipro.cocoron.ui.viewmodel.BaseModel;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.ranges.IntRange;
import kotlinx.coroutines.BuildersKt__BuildersKt;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;
import kotlinx.coroutines.Job;

/* compiled from: MainModel.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000¤\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b.\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0018\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b3\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0014\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\b\u0010Ý\u0001\u001a\u00030Þ\u0001J\b\u0010ß\u0001\u001a\u00030Þ\u0001J\b\u0010à\u0001\u001a\u00030Þ\u0001J\b\u0010á\u0001\u001a\u00030Þ\u0001J\u0012\u0010â\u0001\u001a\u00030Þ\u00012\b\u0010ã\u0001\u001a\u00030¤\u0001J\u0012\u0010ä\u0001\u001a\u00030Þ\u00012\b\u0010ã\u0001\u001a\u00030¤\u0001J\b\u0010å\u0001\u001a\u00030Þ\u0001J\u0007\u0010x\u001a\u00030Þ\u0001J\b\u0010æ\u0001\u001a\u00030Þ\u0001J\u0012\u0010ç\u0001\u001a\u00030Þ\u00012\b\u0010è\u0001\u001a\u00030é\u0001J\b\u0010ê\u0001\u001a\u00030Þ\u0001J\b\u0010ë\u0001\u001a\u00030Þ\u0001J\u0007\u0010$\u001a\u00030Þ\u0001J\u0007\u0010M\u001a\u00030Þ\u0001J\u0007\u0010Z\u001a\u00030Þ\u0001J\u0013\u0010ì\u0001\u001a\u00030Þ\u00012\t\b\u0002\u0010í\u0001\u001a\u000206J\n\u0010î\u0001\u001a\u00030Þ\u0001H\u0017J\n\u0010ï\u0001\u001a\u00030Þ\u0001H\u0016J+\u0010ð\u0001\u001a\u00030ñ\u00012\u0007\u0010ò\u0001\u001a\u00020\u000f2\u0007\u0010ó\u0001\u001a\u00020\u000f2\u0007\u0010Î\u0001\u001a\u00020\u000f2\u0006\u0010\r\u001a\u00020\u000fJ\b\u0010ô\u0001\u001a\u00030Þ\u0001J\b\u0010õ\u0001\u001a\u00030Þ\u0001J\u0012\u0010ö\u0001\u001a\u00030Þ\u00012\b\u0010ã\u0001\u001a\u00030¤\u0001R\u0014\u0010\u0003\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u001a\u0010\u0007\u001a\u00020\bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR \u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R \u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00040\u0015X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R \u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00040\u001bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u001d\"\u0004\b\u001e\u0010\u001fR\u001c\u0010 \u001a\u0004\u0018\u00010!X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\"\u0010#\"\u0004\b$\u0010%R \u0010&\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b'\u0010\u0011\"\u0004\b(\u0010\u0013R \u0010)\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b*\u0010\u0011\"\u0004\b+\u0010\u0013R \u0010,\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b-\u0010\u0011\"\u0004\b.\u0010\u0013R \u0010/\u001a\b\u0012\u0004\u0012\u00020\u00040\u001bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b0\u0010\u001d\"\u0004\b1\u0010\u001fR \u00102\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b3\u0010\u0011\"\u0004\b4\u0010\u0013R\u001a\u00105\u001a\u000206X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b7\u00108\"\u0004\b9\u0010:R \u0010;\u001a\b\u0012\u0004\u0012\u00020=0<X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b>\u0010?\"\u0004\b@\u0010AR \u0010B\u001a\b\u0012\u0004\u0012\u00020C0\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bD\u0010\u0011\"\u0004\bE\u0010\u0013R \u0010F\u001a\b\u0012\u0004\u0012\u00020C0\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bG\u0010\u0011\"\u0004\bH\u0010\u0013R\u001a\u0010I\u001a\u00020JX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bK\u0010L\"\u0004\bM\u0010NR \u0010O\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bP\u0010\u0011\"\u0004\bQ\u0010\u0013R \u0010R\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bS\u0010\u0011\"\u0004\bT\u0010\u0013R \u0010U\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bV\u0010\u0011\"\u0004\bW\u0010\u0013R\u001c\u0010X\u001a\u0004\u0018\u00010!X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bY\u0010#\"\u0004\bZ\u0010%R\u001a\u0010[\u001a\u00020\bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\\\u0010\n\"\u0004\b]\u0010\fR\u001a\u0010^\u001a\u00020\u000fX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b_\u0010`\"\u0004\ba\u0010bR\u001c\u0010c\u001a\u0004\u0018\u00010JX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bd\u0010L\"\u0004\be\u0010NR \u0010f\u001a\b\u0012\u0004\u0012\u0002060\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bg\u0010\u0011\"\u0004\bh\u0010\u0013R\u001a\u0010i\u001a\u000206X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bj\u00108\"\u0004\bk\u0010:R \u0010l\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bm\u0010\u0011\"\u0004\bn\u0010\u0013R \u0010o\u001a\b\u0012\u0004\u0012\u00020\u00040\u001bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bp\u0010\u001d\"\u0004\bq\u0010\u001fR \u0010r\u001a\b\u0012\u0004\u0012\u00020C0\u001bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bs\u0010\u001d\"\u0004\bt\u0010\u001fR\u001a\u0010u\u001a\u000206X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bv\u00108\"\u0004\bw\u0010:R\u001a\u0010x\u001a\u00020yX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bz\u0010{\"\u0004\b|\u0010}R!\u0010~\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0086\u000e¢\u0006\u000f\n\u0000\u001a\u0004\b\u007f\u0010\u0011\"\u0005\b\u0080\u0001\u0010\u0013R \u0010\u0081\u0001\u001a\u00030\u0082\u0001X\u0086\u000e¢\u0006\u0012\n\u0000\u001a\u0006\b\u0083\u0001\u0010\u0084\u0001\"\u0006\b\u0085\u0001\u0010\u0086\u0001R(\u0010\u0088\u0001\u001a\u00020\u000f2\u0007\u0010\u0087\u0001\u001a\u00020\u000f8F@FX\u0086\u000e¢\u0006\u000e\u001a\u0005\b\u0089\u0001\u0010`\"\u0005\b\u008a\u0001\u0010bR\u001d\u0010\u008b\u0001\u001a\u000206X\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b\u008c\u0001\u00108\"\u0005\b\u008d\u0001\u0010:R\u001d\u0010\u008e\u0001\u001a\u000206X\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b\u008f\u0001\u00108\"\u0005\b\u0090\u0001\u0010:R#\u0010\u0091\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b\u0092\u0001\u0010\u0011\"\u0005\b\u0093\u0001\u0010\u0013R#\u0010\u0094\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b\u0095\u0001\u0010\u0011\"\u0005\b\u0096\u0001\u0010\u0013R#\u0010\u0097\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b\u0098\u0001\u0010\u0011\"\u0005\b\u0099\u0001\u0010\u0013R \u0010\u009a\u0001\u001a\u00030\u009b\u0001X\u0086\u000e¢\u0006\u0012\n\u0000\u001a\u0006\b\u009c\u0001\u0010\u009d\u0001\"\u0006\b\u009e\u0001\u0010\u009f\u0001R#\u0010 \u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b¡\u0001\u0010\u0011\"\u0005\b¢\u0001\u0010\u0013R&\u0010£\u0001\u001a\u000b\u0012\u0007\u0012\u0005\u0018\u00010¤\u00010\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b¥\u0001\u0010\u0011\"\u0005\b¦\u0001\u0010\u0013R#\u0010§\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b¨\u0001\u0010\u0011\"\u0005\b©\u0001\u0010\u0013R#\u0010ª\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b«\u0001\u0010\u0011\"\u0005\b¬\u0001\u0010\u0013R#\u0010\u00ad\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b®\u0001\u0010\u0011\"\u0005\b¯\u0001\u0010\u0013R#\u0010°\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b±\u0001\u0010\u0011\"\u0005\b²\u0001\u0010\u0013R#\u0010³\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b´\u0001\u0010\u0011\"\u0005\bµ\u0001\u0010\u0013R#\u0010¶\u0001\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b·\u0001\u0010\u0011\"\u0005\b¸\u0001\u0010\u0013R#\u0010¹\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u001bX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\bº\u0001\u0010\u001d\"\u0005\b»\u0001\u0010\u001fR#\u0010¼\u0001\u001a\b\u0012\u0004\u0012\u00020C0\u001bX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\b½\u0001\u0010\u001d\"\u0005\b¾\u0001\u0010\u001fR#\u0010¿\u0001\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\bÀ\u0001\u0010\u0011\"\u0005\bÁ\u0001\u0010\u0013R#\u0010Â\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u0015X\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\bÃ\u0001\u0010\u0017\"\u0005\bÄ\u0001\u0010\u0019R#\u0010Å\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\bÆ\u0001\u0010\u0011\"\u0005\bÇ\u0001\u0010\u0013R#\u0010È\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\bÉ\u0001\u0010\u0011\"\u0005\bÊ\u0001\u0010\u0013R#\u0010Ë\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\bÌ\u0001\u0010\u0011\"\u0005\bÍ\u0001\u0010\u0013R#\u0010Î\u0001\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\bÏ\u0001\u0010\u0011\"\u0005\bÐ\u0001\u0010\u0013R#\u0010Ñ\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u0015X\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\bÒ\u0001\u0010\u0017\"\u0005\bÓ\u0001\u0010\u0019R#\u0010Ô\u0001\u001a\b\u0012\u0004\u0012\u00020\u00040\u001bX\u0086\u000e¢\u0006\u0010\n\u0000\u001a\u0005\bÕ\u0001\u0010\u001d\"\u0005\bÖ\u0001\u0010\u001fR\"\u0010×\u0001\u001a\u0005\u0018\u00010Ø\u0001X\u0086\u000e¢\u0006\u0012\n\u0000\u001a\u0006\bÙ\u0001\u0010Ú\u0001\"\u0006\bÛ\u0001\u0010Ü\u0001¨\u0006÷\u0001"}, d2 = {"Ljp/co/nipro/cocoron/ui/viewmodel/MainModel;", "Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel;", "()V", "TAG", "", "getTAG", "()Ljava/lang/String;", "baseRriTime", "", "getBaseRriTime", "()J", "setBaseRriTime", "(J)V", "bottom", "Landroidx/lifecycle/MutableLiveData;", "", "getBottom", "()Landroidx/lifecycle/MutableLiveData;", "setBottom", "(Landroidx/lifecycle/MutableLiveData;)V", "bottomAdapter", "Lcom/bigkoo/pickerview/adapter/ArrayWheelAdapter;", "getBottomAdapter", "()Lcom/bigkoo/pickerview/adapter/ArrayWheelAdapter;", "setBottomAdapter", "(Lcom/bigkoo/pickerview/adapter/ArrayWheelAdapter;)V", "bottomValues", "", "getBottomValues", "()Ljava/util/List;", "setBottomValues", "(Ljava/util/List;)V", "calendarDayTimer", "Landroid/os/CountDownTimer;", "getCalendarDayTimer", "()Landroid/os/CountDownTimer;", "setCalendarDayTimer", "(Landroid/os/CountDownTimer;)V", "currDay", "getCurrDay", "setCurrDay", "currTime", "getCurrTime", "setCurrTime", "dayHolderSelected", "getDayHolderSelected", "setDayHolderSelected", "dayHolderTags", "getDayHolderTags", "setDayHolderTags", "deviceName", "getDeviceName", "setDeviceName", "drawEcg", "", "getDrawEcg", "()Z", "setDrawEcg", "(Z)V", "ecgChartViewDataSet", "Ljava/util/ArrayList;", "Lcom/github/mikephil/charting/data/Entry;", "getEcgChartViewDataSet", "()Ljava/util/ArrayList;", "setEcgChartViewDataSet", "(Ljava/util/ArrayList;)V", "ecgChartViewXMax", "", "getEcgChartViewXMax", "setEcgChartViewXMax", "ecgChartViewXMin", "getEcgChartViewXMin", "setEcgChartViewXMin", "ecgData", "Ljp/co/nipro/cocoron/data/entity/ECGData;", "getEcgData", "()Ljp/co/nipro/cocoron/data/entity/ECGData;", "setEcgData", "(Ljp/co/nipro/cocoron/data/entity/ECGData;)V", "ecgDay", "getEcgDay", "setEcgDay", "ecgModeLive", "getEcgModeLive", "setEcgModeLive", "ecgTime", "getEcgTime", "setEcgTime", "ecgTimer", "getEcgTimer", "setEcgTimer", "ecgTriggerX", "getEcgTriggerX", "setEcgTriggerX", "ecgX", "getEcgX", "()I", "setEcgX", "(I)V", "hisEcgData", "getHisEcgData", "setHisEcgData", "historyShow", "getHistoryShow", "setHistoryShow", "modelInit", "getModelInit", "setModelInit", "mvHolderSelected", "getMvHolderSelected", "setMvHolderSelected", "mvHolderTags", "getMvHolderTags", "setMvHolderTags", "mvValue", "getMvValue", "setMvValue", "needClearEcgView", "getNeedClearEcgView", "setNeedClearEcgView", "pickerCancel", "Ljava/lang/Runnable;", "getPickerCancel", "()Ljava/lang/Runnable;", "setPickerCancel", "(Ljava/lang/Runnable;)V", "pickerShow", "getPickerShow", "setPickerShow", "pickerTimer", "Landroid/os/Handler;", "getPickerTimer", "()Landroid/os/Handler;", "setPickerTimer", "(Landroid/os/Handler;)V", "newValue", "realEcgMode", "getRealEcgMode", "setRealEcgMode", "realTimeMode", "getRealTimeMode", "setRealTimeMode", "receivingEcg", "getReceivingEcg", "setReceivingEcg", "rriBottomCurr", "getRriBottomCurr", "setRriBottomCurr", "rriBottomNext", "getRriBottomNext", "setRriBottomNext", "rriBottomPrev", "getRriBottomPrev", "setRriBottomPrev", "rriChartViewData", "Lcom/github/mikephil/charting/data/CombinedData;", "getRriChartViewData", "()Lcom/github/mikephil/charting/data/CombinedData;", "setRriChartViewData", "(Lcom/github/mikephil/charting/data/CombinedData;)V", "rriDay", "getRriDay", "setRriDay", "rriEntry", "Lcom/github/mikephil/charting/data/BarEntry;", "getRriEntry", "setRriEntry", "rriStr", "getRriStr", "setRriStr", "rriTime", "getRriTime", "setRriTime", "rriTopCurr", "getRriTopCurr", "setRriTopCurr", "rriTopNext", "getRriTopNext", "setRriTopNext", "rriTopPrev", "getRriTopPrev", "setRriTopPrev", "secHolderSelected", "getSecHolderSelected", "setSecHolderSelected", "secHolderTags", "getSecHolderTags", "setSecHolderTags", "secValue", "getSecValue", "setSecValue", "sendInterval", "getSendInterval", "setSendInterval", "sendIntervalAdapter", "getSendIntervalAdapter", "setSendIntervalAdapter", "sendIntervalCurr", "getSendIntervalCurr", "setSendIntervalCurr", "sendIntervalNext", "getSendIntervalNext", "setSendIntervalNext", "sendIntervalPrev", "getSendIntervalPrev", "setSendIntervalPrev", "top", "getTop", "setTop", "topAdapter", "getTopAdapter", "setTopAdapter", "topValues", "getTopValues", "setTopValues", "workItem", "Lkotlinx/coroutines/Job;", "getWorkItem", "()Lkotlinx/coroutines/Job;", "setWorkItem", "(Lkotlinx/coroutines/Job;)V", "clearEcgView", "", "clockClicked", "dayHolderChanged", "drawEcgView", "markSelected", "entry", "markSelecting", "mvHolderChanged", "pickerSelecting", "receivedEcg", "ecgM", "Ljp/co/nipro/cocoron/data/value/ECGMeasurementCharacteristicValue;", "secHolderChanged", "sendIntervalClick", "setRriData", "noChange", "setUp", "tearDown", "toYVal", "", "max", "min", "topBottomClick", "updateEcgDaytime", "updateRriDaytime", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class MainModel extends BaseModel {
    private long baseRriTime;
    private MutableLiveData<Integer> bottom;
    private ArrayWheelAdapter<String> bottomAdapter;
    private List<String> bottomValues;
    private CountDownTimer calendarDayTimer;
    private MutableLiveData<Integer> dayHolderSelected;
    private boolean drawEcg;
    private CountDownTimer ecgTimer;
    private long ecgTriggerX;
    private int ecgX;
    private ECGData hisEcgData;
    private boolean modelInit;
    private MutableLiveData<Integer> mvHolderSelected;
    private List<String> mvHolderTags;
    private List<Float> mvValue;
    private boolean needClearEcgView;
    private Runnable pickerCancel;
    private MutableLiveData<Integer> pickerShow;
    private Handler pickerTimer;
    private boolean receivingEcg;
    private MutableLiveData<Integer> secHolderSelected;
    private List<Float> secValue;
    private MutableLiveData<Integer> sendInterval;
    private ArrayWheelAdapter<String> sendIntervalAdapter;
    private MutableLiveData<Integer> top;
    private ArrayWheelAdapter<String> topAdapter;
    private List<String> topValues;
    private Job workItem;
    private final String TAG = "MainModel";
    private ECGData ecgData = new ECGData();
    private boolean realTimeMode = true;
    private ArrayList<Entry> ecgChartViewDataSet = new ArrayList<>();
    private MutableLiveData<Float> ecgChartViewXMax = new MutableLiveData<>();
    private MutableLiveData<Float> ecgChartViewXMin = new MutableLiveData<>();
    private CombinedData rriChartViewData = new CombinedData();
    private MutableLiveData<String> rriStr = new MutableLiveData<>("-");
    private MutableLiveData<String> currDay = new MutableLiveData<>("");
    private MutableLiveData<String> currTime = new MutableLiveData<>("");
    private MutableLiveData<String> deviceName = new MutableLiveData<>("SN:");
    private MutableLiveData<String> rriTopCurr = new MutableLiveData<>("");
    private MutableLiveData<String> rriTopPrev = new MutableLiveData<>("");
    private MutableLiveData<String> rriTopNext = new MutableLiveData<>("");
    private MutableLiveData<String> rriBottomCurr = new MutableLiveData<>("");
    private MutableLiveData<String> rriBottomPrev = new MutableLiveData<>("");
    private MutableLiveData<String> rriBottomNext = new MutableLiveData<>("");
    private MutableLiveData<String> sendIntervalCurr = new MutableLiveData<>("");
    private MutableLiveData<String> sendIntervalPrev = new MutableLiveData<>("");
    private MutableLiveData<String> sendIntervalNext = new MutableLiveData<>("");
    private MutableLiveData<Integer> ecgModeLive = new MutableLiveData<>(0);
    private MutableLiveData<String> rriDay = new MutableLiveData<>("");
    private MutableLiveData<String> rriTime = new MutableLiveData<>("");
    private MutableLiveData<String> ecgDay = new MutableLiveData<>("");
    private MutableLiveData<String> ecgTime = new MutableLiveData<>("");
    private MutableLiveData<Boolean> historyShow = new MutableLiveData<>(false);
    private MutableLiveData<BarEntry> rriEntry = new MutableLiveData<>(null);
    private List<String> dayHolderTags = CollectionsKt.listOf((Object[]) new String[]{"12h", "24h", "7d", "30d"});
    private List<String> secHolderTags = CollectionsKt.listOf((Object[]) new String[]{"3.5<small>sec</small>", "10<small>sec</small>", "15<small>sec</small>", "30<small>sec</small>"});

    public MainModel() {
        Float valueOf = Float.valueOf(10.0f);
        this.secValue = CollectionsKt.listOf((Object[]) new Float[]{Float.valueOf(3.5f), valueOf, Float.valueOf(15.0f), Float.valueOf(30.0f)});
        this.mvHolderTags = CollectionsKt.listOf((Object[]) new String[]{"±10<small>mV</small>", "±7.5<small>mV</small>", "±5<small>mV</</small>", "±2.5<small>mV</small>"});
        this.mvValue = CollectionsKt.listOf((Object[]) new Float[]{valueOf, Float.valueOf(7.5f), Float.valueOf(5.0f), Float.valueOf(2.5f)});
        this.dayHolderSelected = new MutableLiveData<>(0);
        this.secHolderSelected = new MutableLiveData<>(0);
        this.mvHolderSelected = new MutableLiveData<>(0);
        this.sendIntervalAdapter = new ArrayWheelAdapter<>(ArraysKt.toList(Config.INSTANCE.getSEND_INTERVAL_VALUE()));
        List reversed = CollectionsKt.reversed(CollectionsKt.toList(new IntRange(Config.INSTANCE.getTOP_MIN(), Config.INSTANCE.getTOP_MAX())));
        ArrayList arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(reversed, 10));
        Iterator it = reversed.iterator();
        while (it.hasNext()) {
            arrayList.add(String.valueOf(((Number) it.next()).intValue()));
        }
        List<String> plus = CollectionsKt.plus((Collection) arrayList, (Iterable) CollectionsKt.listOf("設定なし"));
        this.topValues = plus;
        this.topAdapter = new ArrayWheelAdapter<>(plus);
        List reversed2 = CollectionsKt.reversed(CollectionsKt.toList(new IntRange(Config.INSTANCE.getBOTTOM_MIN(), Config.INSTANCE.getBOTTOM_MAX())));
        ArrayList arrayList2 = new ArrayList(CollectionsKt.collectionSizeOrDefault(reversed2, 10));
        Iterator it2 = reversed2.iterator();
        while (it2.hasNext()) {
            arrayList2.add(String.valueOf(((Number) it2.next()).intValue()));
        }
        List<String> plus2 = CollectionsKt.plus((Collection) arrayList2, (Iterable) CollectionsKt.listOf("設定なし"));
        this.bottomValues = plus2;
        this.bottomAdapter = new ArrayWheelAdapter<>(plus2);
        this.sendInterval = new MutableLiveData<>(Integer.valueOf(Config.INSTANCE.getSendInterval()));
        this.top = new MutableLiveData<>(Integer.valueOf(Config.INSTANCE.getTOP_MAX() - Config.INSTANCE.getTop()));
        this.bottom = new MutableLiveData<>(Integer.valueOf(Config.INSTANCE.getBOTTOM_MAX() - Config.INSTANCE.getBottom()));
        this.pickerShow = new MutableLiveData<>(0);
        this.pickerTimer = new Handler(Looper.getMainLooper());
        this.pickerCancel = new Runnable() { // from class: jp.co.nipro.cocoron.ui.viewmodel.MainModel$pickerCancel$1
            @Override // java.lang.Runnable
            public final void run() {
                MainModel.this.getPickerShow().setValue(0);
            }
        };
    }

    public final String getTAG() {
        return this.TAG;
    }

    public final ECGData getEcgData() {
        return this.ecgData;
    }

    public final void setEcgData(ECGData eCGData) {
        Intrinsics.checkNotNullParameter(eCGData, "<set-?>");
        this.ecgData = eCGData;
    }

    public final ECGData getHisEcgData() {
        return this.hisEcgData;
    }

    public final void setHisEcgData(ECGData eCGData) {
        this.hisEcgData = eCGData;
    }

    public final CountDownTimer getEcgTimer() {
        return this.ecgTimer;
    }

    public final void setEcgTimer(CountDownTimer countDownTimer) {
        this.ecgTimer = countDownTimer;
    }

    public final CountDownTimer getCalendarDayTimer() {
        return this.calendarDayTimer;
    }

    public final void setCalendarDayTimer(CountDownTimer countDownTimer) {
        this.calendarDayTimer = countDownTimer;
    }

    public final int getEcgX() {
        return this.ecgX;
    }

    public final void setEcgX(int i) {
        this.ecgX = i;
    }

    public final long getEcgTriggerX() {
        return this.ecgTriggerX;
    }

    public final void setEcgTriggerX(long j) {
        this.ecgTriggerX = j;
    }

    public final int getRealEcgMode() {
        return Config.INSTANCE.getEcgMode();
    }

    public final void setRealEcgMode(int i) {
        Config.INSTANCE.setEcgMode(i);
        this.ecgModeLive.setValue(Integer.valueOf(i));
    }

    public final boolean getRealTimeMode() {
        return this.realTimeMode;
    }

    public final void setRealTimeMode(boolean z) {
        this.realTimeMode = z;
    }

    public final boolean getReceivingEcg() {
        return this.receivingEcg;
    }

    public final void setReceivingEcg(boolean z) {
        this.receivingEcg = z;
    }

    public final ArrayList<Entry> getEcgChartViewDataSet() {
        return this.ecgChartViewDataSet;
    }

    public final void setEcgChartViewDataSet(ArrayList<Entry> arrayList) {
        Intrinsics.checkNotNullParameter(arrayList, "<set-?>");
        this.ecgChartViewDataSet = arrayList;
    }

    public final MutableLiveData<Float> getEcgChartViewXMax() {
        return this.ecgChartViewXMax;
    }

    public final void setEcgChartViewXMax(MutableLiveData<Float> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.ecgChartViewXMax = mutableLiveData;
    }

    public final MutableLiveData<Float> getEcgChartViewXMin() {
        return this.ecgChartViewXMin;
    }

    public final void setEcgChartViewXMin(MutableLiveData<Float> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.ecgChartViewXMin = mutableLiveData;
    }

    public final boolean getDrawEcg() {
        return this.drawEcg;
    }

    public final void setDrawEcg(boolean z) {
        this.drawEcg = z;
    }

    public final CombinedData getRriChartViewData() {
        return this.rriChartViewData;
    }

    public final void setRriChartViewData(CombinedData combinedData) {
        Intrinsics.checkNotNullParameter(combinedData, "<set-?>");
        this.rriChartViewData = combinedData;
    }

    public final MutableLiveData<String> getRriStr() {
        return this.rriStr;
    }

    public final void setRriStr(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.rriStr = mutableLiveData;
    }

    public final MutableLiveData<String> getCurrDay() {
        return this.currDay;
    }

    public final void setCurrDay(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.currDay = mutableLiveData;
    }

    public final MutableLiveData<String> getCurrTime() {
        return this.currTime;
    }

    public final void setCurrTime(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.currTime = mutableLiveData;
    }

    public final MutableLiveData<String> getDeviceName() {
        return this.deviceName;
    }

    public final void setDeviceName(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.deviceName = mutableLiveData;
    }

    public final MutableLiveData<String> getRriTopCurr() {
        return this.rriTopCurr;
    }

    public final void setRriTopCurr(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.rriTopCurr = mutableLiveData;
    }

    public final MutableLiveData<String> getRriTopPrev() {
        return this.rriTopPrev;
    }

    public final void setRriTopPrev(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.rriTopPrev = mutableLiveData;
    }

    public final MutableLiveData<String> getRriTopNext() {
        return this.rriTopNext;
    }

    public final void setRriTopNext(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.rriTopNext = mutableLiveData;
    }

    public final MutableLiveData<String> getRriBottomCurr() {
        return this.rriBottomCurr;
    }

    public final void setRriBottomCurr(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.rriBottomCurr = mutableLiveData;
    }

    public final MutableLiveData<String> getRriBottomPrev() {
        return this.rriBottomPrev;
    }

    public final void setRriBottomPrev(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.rriBottomPrev = mutableLiveData;
    }

    public final MutableLiveData<String> getRriBottomNext() {
        return this.rriBottomNext;
    }

    public final void setRriBottomNext(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.rriBottomNext = mutableLiveData;
    }

    public final MutableLiveData<String> getSendIntervalCurr() {
        return this.sendIntervalCurr;
    }

    public final void setSendIntervalCurr(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.sendIntervalCurr = mutableLiveData;
    }

    public final MutableLiveData<String> getSendIntervalPrev() {
        return this.sendIntervalPrev;
    }

    public final void setSendIntervalPrev(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.sendIntervalPrev = mutableLiveData;
    }

    public final MutableLiveData<String> getSendIntervalNext() {
        return this.sendIntervalNext;
    }

    public final void setSendIntervalNext(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.sendIntervalNext = mutableLiveData;
    }

    public final MutableLiveData<Integer> getEcgModeLive() {
        return this.ecgModeLive;
    }

    public final void setEcgModeLive(MutableLiveData<Integer> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.ecgModeLive = mutableLiveData;
    }

    public final long getBaseRriTime() {
        return this.baseRriTime;
    }

    public final void setBaseRriTime(long j) {
        this.baseRriTime = j;
    }

    public final MutableLiveData<String> getRriDay() {
        return this.rriDay;
    }

    public final void setRriDay(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.rriDay = mutableLiveData;
    }

    public final MutableLiveData<String> getRriTime() {
        return this.rriTime;
    }

    public final void setRriTime(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.rriTime = mutableLiveData;
    }

    public final MutableLiveData<String> getEcgDay() {
        return this.ecgDay;
    }

    public final void setEcgDay(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.ecgDay = mutableLiveData;
    }

    public final MutableLiveData<String> getEcgTime() {
        return this.ecgTime;
    }

    public final void setEcgTime(MutableLiveData<String> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.ecgTime = mutableLiveData;
    }

    public final MutableLiveData<Boolean> getHistoryShow() {
        return this.historyShow;
    }

    public final void setHistoryShow(MutableLiveData<Boolean> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.historyShow = mutableLiveData;
    }

    public final MutableLiveData<BarEntry> getRriEntry() {
        return this.rriEntry;
    }

    public final void setRriEntry(MutableLiveData<BarEntry> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.rriEntry = mutableLiveData;
    }

    public final List<String> getDayHolderTags() {
        return this.dayHolderTags;
    }

    public final void setDayHolderTags(List<String> list) {
        Intrinsics.checkNotNullParameter(list, "<set-?>");
        this.dayHolderTags = list;
    }

    public final List<String> getSecHolderTags() {
        return this.secHolderTags;
    }

    public final void setSecHolderTags(List<String> list) {
        Intrinsics.checkNotNullParameter(list, "<set-?>");
        this.secHolderTags = list;
    }

    public final List<Float> getSecValue() {
        return this.secValue;
    }

    public final void setSecValue(List<Float> list) {
        Intrinsics.checkNotNullParameter(list, "<set-?>");
        this.secValue = list;
    }

    public final List<String> getMvHolderTags() {
        return this.mvHolderTags;
    }

    public final void setMvHolderTags(List<String> list) {
        Intrinsics.checkNotNullParameter(list, "<set-?>");
        this.mvHolderTags = list;
    }

    public final List<Float> getMvValue() {
        return this.mvValue;
    }

    public final void setMvValue(List<Float> list) {
        Intrinsics.checkNotNullParameter(list, "<set-?>");
        this.mvValue = list;
    }

    public final MutableLiveData<Integer> getDayHolderSelected() {
        return this.dayHolderSelected;
    }

    public final void setDayHolderSelected(MutableLiveData<Integer> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.dayHolderSelected = mutableLiveData;
    }

    public final MutableLiveData<Integer> getSecHolderSelected() {
        return this.secHolderSelected;
    }

    public final void setSecHolderSelected(MutableLiveData<Integer> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.secHolderSelected = mutableLiveData;
    }

    public final MutableLiveData<Integer> getMvHolderSelected() {
        return this.mvHolderSelected;
    }

    public final void setMvHolderSelected(MutableLiveData<Integer> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.mvHolderSelected = mutableLiveData;
    }

    public final ArrayWheelAdapter<String> getSendIntervalAdapter() {
        return this.sendIntervalAdapter;
    }

    public final void setSendIntervalAdapter(ArrayWheelAdapter<String> arrayWheelAdapter) {
        Intrinsics.checkNotNullParameter(arrayWheelAdapter, "<set-?>");
        this.sendIntervalAdapter = arrayWheelAdapter;
    }

    public final List<String> getTopValues() {
        return this.topValues;
    }

    public final void setTopValues(List<String> list) {
        Intrinsics.checkNotNullParameter(list, "<set-?>");
        this.topValues = list;
    }

    public final ArrayWheelAdapter<String> getTopAdapter() {
        return this.topAdapter;
    }

    public final void setTopAdapter(ArrayWheelAdapter<String> arrayWheelAdapter) {
        Intrinsics.checkNotNullParameter(arrayWheelAdapter, "<set-?>");
        this.topAdapter = arrayWheelAdapter;
    }

    public final List<String> getBottomValues() {
        return this.bottomValues;
    }

    public final void setBottomValues(List<String> list) {
        Intrinsics.checkNotNullParameter(list, "<set-?>");
        this.bottomValues = list;
    }

    public final ArrayWheelAdapter<String> getBottomAdapter() {
        return this.bottomAdapter;
    }

    public final void setBottomAdapter(ArrayWheelAdapter<String> arrayWheelAdapter) {
        Intrinsics.checkNotNullParameter(arrayWheelAdapter, "<set-?>");
        this.bottomAdapter = arrayWheelAdapter;
    }

    public final MutableLiveData<Integer> getSendInterval() {
        return this.sendInterval;
    }

    public final void setSendInterval(MutableLiveData<Integer> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.sendInterval = mutableLiveData;
    }

    public final MutableLiveData<Integer> getTop() {
        return this.top;
    }

    public final void setTop(MutableLiveData<Integer> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.top = mutableLiveData;
    }

    public final MutableLiveData<Integer> getBottom() {
        return this.bottom;
    }

    public final void setBottom(MutableLiveData<Integer> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.bottom = mutableLiveData;
    }

    public final MutableLiveData<Integer> getPickerShow() {
        return this.pickerShow;
    }

    public final void setPickerShow(MutableLiveData<Integer> mutableLiveData) {
        Intrinsics.checkNotNullParameter(mutableLiveData, "<set-?>");
        this.pickerShow = mutableLiveData;
    }

    public final Job getWorkItem() {
        return this.workItem;
    }

    public final void setWorkItem(Job job) {
        this.workItem = job;
    }

    public final boolean getNeedClearEcgView() {
        return this.needClearEcgView;
    }

    public final void setNeedClearEcgView(boolean z) {
        this.needClearEcgView = z;
    }

    public final boolean getModelInit() {
        return this.modelInit;
    }

    public final void setModelInit(boolean z) {
        this.modelInit = z;
    }

    @Override // jp.co.nipro.cocoron.ui.viewmodel.BaseModel
    public void setUp() {
        String str;
        if (this.modelInit) {
            return;
        }
        this.modelInit = true;
        setRealEcgMode(Config.INSTANCE.getEcgMode());
        this.sendInterval.setValue(Integer.valueOf(Config.INSTANCE.getSendInterval()));
        this.top.setValue(Integer.valueOf(Config.INSTANCE.getTOP_MAX() - Config.INSTANCE.getTop()));
        this.bottom.setValue(Integer.valueOf(Config.INSTANCE.getBOTTOM_MAX() - Config.INSTANCE.getBottom()));
        this.dayHolderSelected.setValue(Integer.valueOf(Config.INSTANCE.getDaySelected()));
        this.secHolderSelected.setValue(Integer.valueOf(Config.INSTANCE.getSecSelected()));
        this.mvHolderSelected.setValue(Integer.valueOf(Config.INSTANCE.getMvSelected()));
        setEcgData();
        setEcgTimer();
        setRriData$default(this, false, 1, null);
        setCalendarDayTimer();
        dayHolderChanged();
        secHolderChanged();
        mvHolderChanged();
        MutableLiveData<String> mutableLiveData = this.deviceName;
        if (Config.INSTANCE.getConnectName().length() == 0) {
            str = " ";
        } else {
            str = "SN:" + Config.INSTANCE.getConnectName();
        }
        mutableLiveData.setValue(str);
    }

    @Override // jp.co.nipro.cocoron.ui.viewmodel.BaseModel
    public void tearDown() {
        super.tearDown();
    }

    public final void receivedEcg(ECGMeasurementCharacteristicValue ecgM) {
        Job launch$default;
        Intrinsics.checkNotNullParameter(ecgM, "ecgM");
        Job job = this.workItem;
        if (job != null) {
            Job.DefaultImpls.cancel$default(job, (CancellationException) null, 1, (Object) null);
        }
        launch$default = BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain(), null, new MainModel$receivedEcg$1(this, null), 2, null);
        this.workItem = launch$default;
        if (this.needClearEcgView) {
            this.ecgData.clear();
            if (this.realTimeMode) {
                clearEcgView();
            }
            this.needClearEcgView = false;
        }
        this.receivingEcg = true;
        if (ecgM.getTrigger1() != 255) {
            this.ecgData.getTrigger().add(Long.valueOf(this.ecgData.getEcg().size() + ecgM.getTrigger1()));
        }
        if (ecgM.getTrigger2() != 255) {
            this.ecgData.getTrigger().add(Long.valueOf(this.ecgData.getEcg().size() + ecgM.getTrigger2()));
        }
        double[] ecg = ecgM.getECG();
        if (ecg != null) {
            this.ecgData.getEcg().addAll(ArraysKt.toList(ecg));
        }
        if (this.ecgData.getStartDate() == 0) {
            this.ecgData.setStartDate(new Date().getTime());
        }
    }

    public final void setCalendarDayTimer() {
        CountDownTimer countDownTimer = this.calendarDayTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        final Ref.LongRef longRef = new Ref.LongRef();
        longRef.element = 1000L;
        final long j = 100 * longRef.element;
        final long j2 = longRef.element;
        CountDownTimer countDownTimer2 = new CountDownTimer(j, j2) { // from class: jp.co.nipro.cocoron.ui.viewmodel.MainModel$setCalendarDayTimer$1
            @Override // android.os.CountDownTimer
            public void onTick(long millisUntilFinished) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                MainModel.this.getCurrDay().setValue(simpleDateFormat.format(new Date()));
                simpleDateFormat.applyPattern("HH:mm:ss");
                MainModel.this.getCurrTime().setValue(simpleDateFormat.format(new Date()));
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                MainModel.this.setCalendarDayTimer();
            }
        };
        this.calendarDayTimer = countDownTimer2;
        if (countDownTimer2 != null) {
            countDownTimer2.start();
        }
    }

    public final void setEcgTimer() {
        CountDownTimer countDownTimer = this.ecgTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        final Ref.LongRef longRef = new Ref.LongRef();
        longRef.element = 200L;
        final long j = 10000 * longRef.element;
        final long j2 = longRef.element;
        CountDownTimer countDownTimer2 = new CountDownTimer(j, j2) { // from class: jp.co.nipro.cocoron.ui.viewmodel.MainModel$setEcgTimer$1
            @Override // android.os.CountDownTimer
            public void onTick(long millisUntilFinished) {
                MainModel.this.drawEcgView();
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                MainModel.this.setEcgTimer();
            }
        };
        this.ecgTimer = countDownTimer2;
        if (countDownTimer2 != null) {
            countDownTimer2.start();
        }
    }

    public final void setEcgData() {
        long j;
        Ref.LongRef longRef = new Ref.LongRef();
        longRef.element = MaxMinRri12hDto.duration;
        Integer value = this.dayHolderSelected.getValue();
        if (value != null && value.intValue() == 0) {
            longRef.element = MaxMinRri12hDto.duration;
        } else if (value != null && value.intValue() == 1) {
            longRef.element = MaxMinRri24hDto.duration;
        } else if (value != null && value.intValue() == 2) {
            longRef.element = MaxMinRri7dDto.duration;
        } else if (value != null && value.intValue() == 3) {
            longRef.element = 43200000L;
        }
        Ref.LongRef longRef2 = new Ref.LongRef();
        BarEntry it = this.rriEntry.getValue();
        if (it != null) {
            Intrinsics.checkNotNullExpressionValue(it, "it");
            j = (((long) it.getX()) + this.baseRriTime) * longRef.element;
        } else {
            j = 0;
        }
        longRef2.element = j;
        BuildersKt__BuildersKt.runBlocking$default(null, new MainModel$setEcgData$1(this, longRef2, longRef, null), 1, null);
    }

    public final void clearEcgView() {
        this.ecgX = 0;
        this.ecgTriggerX = 0L;
        this.ecgChartViewDataSet.clear();
        getEvent().setValue(new Event<>(new BaseModel.EventParam("clearEcg", null, 2, null)));
    }

    /* JADX WARN: Code restructure failed: missing block: B:35:0x00eb, code lost:
    
        if (r5 != null) goto L47;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final void drawEcgView() {
        ECGData eCGData = this.hisEcgData;
        if (this.realTimeMode) {
            eCGData = this.ecgData;
        }
        if (eCGData == null || eCGData.getEcg().size() <= 0 || eCGData.getDrawIndex() >= eCGData.getEcg().size()) {
            return;
        }
        int i = 25;
        if (this.realTimeMode && getRealEcgMode() == 1 && this.receivingEcg) {
            i = 100000;
        }
        if (i >= 0) {
            int i2 = 0;
            while (true) {
                if (((!this.realTimeMode || (getRealEcgMode() == 0 && !this.receivingEcg)) && eCGData.getDrawIndex() >= 1250) || eCGData.getDrawIndex() >= eCGData.getEcg().size()) {
                    break;
                }
                double doubleValue = eCGData.getEcg().get((int) eCGData.getDrawIndex()).doubleValue();
                eCGData.setDrawIndex(eCGData.getDrawIndex() + 1);
                if (this.ecgX >= this.ecgChartViewDataSet.size()) {
                    this.ecgChartViewDataSet.add(new Entry(this.ecgX, (float) doubleValue));
                } else {
                    ArrayList<Entry> arrayList = this.ecgChartViewDataSet;
                    int i3 = this.ecgX;
                    Intrinsics.checkNotNullExpressionValue(arrayList.set(i3, new Entry(i3, (float) doubleValue)), "ecgChartViewDataSet.set(….toFloat(), y.toFloat()))");
                }
                this.ecgX++;
                this.ecgTriggerX++;
                Integer it = this.secHolderSelected.getValue();
                if (it != null) {
                    float f = this.ecgX;
                    List<Float> list = this.secValue;
                    Intrinsics.checkNotNullExpressionValue(it, "it");
                    if (f >= 125 * list.get(it.intValue()).floatValue()) {
                        this.ecgX = 0;
                    }
                }
                MainModel mainModel = this;
                if (mainModel.ecgX >= 437.5f) {
                    mainModel.ecgX = 0;
                }
                Unit unit = Unit.INSTANCE;
                if (i2 == i) {
                    break;
                } else {
                    i2++;
                }
            }
        }
        int i4 = 0;
        for (Object obj : this.ecgChartViewDataSet) {
            int i5 = i4 + 1;
            if (i4 < 0) {
                CollectionsKt.throwIndexOverflow();
            }
            Entry entry = (Entry) obj;
            int i6 = this.ecgX;
            ExtensionKt.setVisible(entry, i4 <= i6 + (-1) || i4 > (i6 + Config.INSTANCE.getECG_LINEBREAK()) - 1);
            i4 = i5;
        }
        this.drawEcg = true;
    }

    public static /* synthetic */ void setRriData$default(MainModel mainModel, boolean z, int i, Object obj) {
        if ((i & 1) != 0) {
            z = false;
        }
        mainModel.setRriData(z);
    }

    /* JADX WARN: Removed duplicated region for block: B:13:0x0106  */
    /* JADX WARN: Removed duplicated region for block: B:16:0x0109 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:29:0x01dc A[LOOP:0: B:4:0x00bb->B:29:0x01dc, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:30:0x01eb A[EDGE_INSN: B:30:0x01eb->B:31:0x01eb BREAK  A[LOOP:0: B:4:0x00bb->B:29:0x01dc], SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r0v17, types: [T, java.util.List] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final void setRriData(boolean noChange) {
        long j;
        String str;
        int i;
        String str2;
        Object obj;
        long j2;
        long j3;
        boolean z;
        getEvent().setValue(new Event<>(new BaseModel.EventParam("setRriLimit", null, 2, null)));
        BarData barData = this.rriChartViewData.getBarData();
        String str3 = "rriChartViewData.barData";
        Intrinsics.checkNotNullExpressionValue(barData, "rriChartViewData.barData");
        ((IBarDataSet) barData.getDataSets().get(0)).clear();
        LineData lineData = this.rriChartViewData.getLineData();
        Intrinsics.checkNotNullExpressionValue(lineData, "rriChartViewData.lineData");
        ((ILineDataSet) lineData.getDataSets().get(0)).clear();
        LineData lineData2 = this.rriChartViewData.getLineData();
        Intrinsics.checkNotNullExpressionValue(lineData2, "rriChartViewData.lineData");
        ((ILineDataSet) lineData2.getDataSets().get(1)).clear();
        Ref.LongRef longRef = new Ref.LongRef();
        longRef.element = 0L;
        Ref.LongRef longRef2 = new Ref.LongRef();
        longRef2.element = 0L;
        Ref.ObjectRef objectRef = new Ref.ObjectRef();
        objectRef.element = CollectionsKt.emptyList();
        BuildersKt__BuildersKt.runBlocking$default(null, new MainModel$setRriData$1(this, objectRef, longRef, longRef2, null), 1, null);
        long time = (new Date().getTime() / longRef.element) + 1;
        String str4 = "rriChartViewData.lineData";
        long j4 = 1000;
        long time2 = (((new Date().getTime() - longRef2.element) / j4) * j4) / longRef.element;
        this.baseRriTime = time2;
        long j5 = time - 1;
        if (time2 <= j5) {
            long j6 = time2;
            int i2 = 0;
            while (true) {
                List list = (List) objectRef.element;
                ArrayList arrayList = new ArrayList();
                for (Object obj2 : list) {
                    MaxMinRriDto maxMinRriDto = (MaxMinRriDto) obj2;
                    String str5 = str3;
                    if (maxMinRriDto.getStart() <= longRef.element * j6) {
                        j2 = time;
                        j3 = j5;
                        if (maxMinRriDto.getStart() > (longRef.element * j6) - longRef.element) {
                            z = true;
                            if (!z) {
                                arrayList.add(obj2);
                            }
                            time = j2;
                            str3 = str5;
                            j5 = j3;
                        }
                    } else {
                        j2 = time;
                        j3 = j5;
                    }
                    z = false;
                    if (!z) {
                    }
                    time = j2;
                    str3 = str5;
                    j5 = j3;
                }
                j = time;
                String str6 = str3;
                long j7 = j5;
                MaxMinRriDto maxMinRriDto2 = (MaxMinRriDto) CollectionsKt.firstOrNull((List) arrayList);
                if (maxMinRriDto2 != null) {
                    float f = j6 - time2;
                    BarEntry barEntry = new BarEntry(f, toYVal(maxMinRriDto2.getMax(), maxMinRriDto2.getMin(), Config.INSTANCE.getTop(), Config.INSTANCE.getBottom()));
                    BarData barData2 = this.rriChartViewData.getBarData();
                    str = str6;
                    Intrinsics.checkNotNullExpressionValue(barData2, str);
                    ((IBarDataSet) barData2.getDataSets().get(0)).addEntryOrdered(barEntry);
                    LineData lineData3 = this.rriChartViewData.getLineData();
                    str2 = str4;
                    Intrinsics.checkNotNullExpressionValue(lineData3, str2);
                    ((ILineDataSet) lineData3.getDataSets().get(0)).addEntryOrdered(new Entry(f, maxMinRriDto2.getMin()));
                    LineData lineData4 = this.rriChartViewData.getLineData();
                    Intrinsics.checkNotNullExpressionValue(lineData4, str2);
                    ((ILineDataSet) lineData4.getDataSets().get(1)).addEntryOrdered(new Entry(f, maxMinRriDto2.getMax()));
                    if (i2 < maxMinRriDto2.getMax()) {
                        i2 = maxMinRriDto2.getMax();
                    }
                    if (maxMinRriDto2 != null) {
                        obj = null;
                        if (j6 != j7) {
                            break;
                        }
                        j6++;
                        str4 = str2;
                        str3 = str;
                        j5 = j7;
                        time = j;
                    }
                } else {
                    str2 = str4;
                    str = str6;
                }
                obj = null;
                BarEntry barEntry2 = new BarEntry(j6 - time2, toYVal(0, 0, 0, 0), (Drawable) null);
                BarData barData3 = this.rriChartViewData.getBarData();
                Intrinsics.checkNotNullExpressionValue(barData3, str);
                ((IBarDataSet) barData3.getDataSets().get(0)).addEntryOrdered(barEntry2);
                Unit unit = Unit.INSTANCE;
                if (j6 != j7) {
                }
            }
            i = i2;
        } else {
            j = time;
            str = "rriChartViewData.barData";
            i = 0;
        }
        getEvent().setValue(new Event<>(new BaseModel.EventParam("setRriMax", Integer.valueOf(i))));
        getEvent().setValue(new Event<>(new BaseModel.EventParam("setRriXMin", 1L)));
        getEvent().setValue(new Event<>(new BaseModel.EventParam("setRriXMax", Long.valueOf(j - time2))));
        getEvent().setValue(new Event<>(new BaseModel.EventParam("refreshRri", Long.valueOf(j))));
        if (noChange) {
            return;
        }
        BarData barData4 = this.rriChartViewData.getBarData();
        Intrinsics.checkNotNullExpressionValue(barData4, str);
        Object obj3 = barData4.getDataSets().get(0);
        Objects.requireNonNull(obj3, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarDataSet");
        List<T> values = ((BarDataSet) obj3).getValues();
        Intrinsics.checkNotNullExpressionValue(values, "(rriChartViewData.barDat…[0] as BarDataSet).values");
        BarEntry barEntry3 = (BarEntry) CollectionsKt.lastOrNull((List) values);
        if (barEntry3 != null) {
            this.rriEntry.setValue(barEntry3);
            markSelected(barEntry3);
        }
    }

    public final float[] toYVal(int max, int min, int top, int bottom) {
        float[] fArr = new float[4];
        fArr[0] = min;
        if (top < Config.INSTANCE.getTOP_MIN()) {
            top = Integer.MAX_VALUE;
        }
        int i = bottom < Config.INSTANCE.getTOP_MIN() ? -1 : bottom;
        if (min >= i) {
            fArr[1] = 0.0f;
        } else if (max > i) {
            fArr[1] = bottom - min;
        } else {
            fArr[1] = max - min;
        }
        if (min <= i) {
            min = i;
        }
        if (max < top) {
            top = max;
        }
        if (top > min) {
            fArr[2] = top - min;
        } else {
            fArr[2] = 0.0f;
        }
        if (max > ((int) (fArr[0] + fArr[1] + fArr[2]))) {
            fArr[3] = max - r7;
        } else {
            fArr[3] = 0.0f;
        }
        return fArr;
    }

    public final void updateEcgDaytime() {
        ECGData eCGData = this.hisEcgData;
        if (this.realTimeMode) {
            eCGData = this.ecgData;
        }
        if (eCGData == null || eCGData.getStartDate() == 0) {
            this.ecgDay.setValue("-");
            this.ecgTime.setValue("-");
            return;
        }
        Date date = new Date(eCGData.getStartDate() + (eCGData.getDrawIndex() * 8));
        if (this.realTimeMode && getRealEcgMode() == 1 && this.receivingEcg) {
            date = new Date();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        this.ecgDay.setValue(simpleDateFormat.format(date));
        simpleDateFormat.applyPattern("HH:mm:ss");
        this.ecgTime.setValue(simpleDateFormat.format(date));
    }

    public final void dayHolderChanged() {
        setRriData$default(this, false, 1, null);
        Integer it = this.dayHolderSelected.getValue();
        if (it != null) {
            Config config = Config.INSTANCE;
            Intrinsics.checkNotNullExpressionValue(it, "it");
            config.setDaySelected(it.intValue());
        }
    }

    public final void secHolderChanged() {
        Integer it = this.secHolderSelected.getValue();
        if (it != null) {
            MutableLiveData<Event<BaseModel.EventParam>> event = getEvent();
            List<Float> list = this.secValue;
            Intrinsics.checkNotNullExpressionValue(it, "it");
            event.setValue(new Event<>(new BaseModel.EventParam("setSec", list.get(it.intValue()))));
            Config.INSTANCE.setSecSelected(it.intValue());
        }
        if (!this.realTimeMode) {
            setEcgData();
            return;
        }
        if (getRealEcgMode() != 1) {
            this.ecgData.setDrawIndex(0L);
            if (!this.receivingEcg) {
                this.needClearEcgView = true;
            }
        }
        clearEcgView();
    }

    public final void mvHolderChanged() {
        Integer it = this.mvHolderSelected.getValue();
        if (it != null) {
            MutableLiveData<Event<BaseModel.EventParam>> event = getEvent();
            List<Float> list = this.mvValue;
            Intrinsics.checkNotNullExpressionValue(it, "it");
            event.setValue(new Event<>(new BaseModel.EventParam("setMv", list.get(it.intValue()))));
            Config.INSTANCE.setMvSelected(it.intValue());
        }
        if (!this.realTimeMode) {
            setEcgData();
            return;
        }
        if (getRealEcgMode() != 1) {
            this.ecgData.setDrawIndex(0L);
            if (!this.receivingEcg) {
                this.needClearEcgView = true;
            }
        }
        clearEcgView();
    }

    public final void markSelecting(BarEntry entry) {
        Intrinsics.checkNotNullParameter(entry, "entry");
        MutableLiveData<Boolean> mutableLiveData = this.historyShow;
        BarData barData = this.rriChartViewData.getBarData();
        Intrinsics.checkNotNullExpressionValue(barData, "rriChartViewData.barData");
        Object obj = barData.getDataSets().get(0);
        Objects.requireNonNull(obj, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarDataSet");
        Intrinsics.checkNotNullExpressionValue(((BarDataSet) obj).getValues(), "(rriChartViewData.barDat…[0] as BarDataSet).values");
        mutableLiveData.setValue(Boolean.valueOf(!Intrinsics.areEqual((BarEntry) CollectionsKt.last((List) r1), entry)));
    }

    public final void markSelected(BarEntry entry) {
        Intrinsics.checkNotNullParameter(entry, "entry");
        BarData barData = this.rriChartViewData.getBarData();
        Intrinsics.checkNotNullExpressionValue(barData, "rriChartViewData.barData");
        Object obj = barData.getDataSets().get(0);
        Objects.requireNonNull(obj, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarDataSet");
        List<T> values = ((BarDataSet) obj).getValues();
        Intrinsics.checkNotNullExpressionValue(values, "(rriChartViewData.barDat…[0] as BarDataSet).values");
        if (Intrinsics.areEqual((BarEntry) CollectionsKt.last((List) values), entry)) {
            if (!this.realTimeMode) {
                clearEcgView();
                if (getRealEcgMode() == 1) {
                    this.ecgData.clear();
                } else {
                    this.ecgData.setDrawIndex(0L);
                    if (!this.receivingEcg) {
                        this.needClearEcgView = true;
                    }
                }
                this.realTimeMode = true;
            }
        } else {
            this.realTimeMode = false;
            setEcgData();
        }
        updateRriDaytime(entry);
    }

    public final void updateRriDaytime(BarEntry entry) {
        long j;
        long j2;
        Intrinsics.checkNotNullParameter(entry, "entry");
        long x = this.baseRriTime + ((long) entry.getX());
        Integer value = this.dayHolderSelected.getValue();
        if (value != null && value.intValue() == 0) {
            j2 = MaxMinRri12hDto.duration;
        } else if (value != null && value.intValue() == 1) {
            j2 = MaxMinRri24hDto.duration;
        } else if (value != null && value.intValue() == 2) {
            j2 = MaxMinRri7dDto.duration;
        } else {
            if (value == null || value.intValue() != 3) {
                j = 0;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                Date date = new Date(j);
                this.rriDay.setValue(simpleDateFormat.format(date));
                simpleDateFormat.applyPattern("HH:mm:ss");
                this.rriTime.setValue(simpleDateFormat.format(date));
                MutableLiveData<Boolean> mutableLiveData = this.historyShow;
                BarData barData = this.rriChartViewData.getBarData();
                Intrinsics.checkNotNullExpressionValue(barData, "rriChartViewData.barData");
                Object obj = barData.getDataSets().get(0);
                Objects.requireNonNull(obj, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarDataSet");
                Intrinsics.checkNotNullExpressionValue(((BarDataSet) obj).getValues(), "(rriChartViewData.barDat…[0] as BarDataSet).values");
                mutableLiveData.setValue(Boolean.valueOf(!Intrinsics.areEqual((BarEntry) CollectionsKt.last((List) r1), entry)));
            }
            j2 = 43200000;
        }
        j = x * j2;
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy/MM/dd");
        Date date2 = new Date(j);
        this.rriDay.setValue(simpleDateFormat2.format(date2));
        simpleDateFormat2.applyPattern("HH:mm:ss");
        this.rriTime.setValue(simpleDateFormat2.format(date2));
        MutableLiveData<Boolean> mutableLiveData2 = this.historyShow;
        BarData barData2 = this.rriChartViewData.getBarData();
        Intrinsics.checkNotNullExpressionValue(barData2, "rriChartViewData.barData");
        Object obj2 = barData2.getDataSets().get(0);
        Objects.requireNonNull(obj2, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarDataSet");
        Intrinsics.checkNotNullExpressionValue(((BarDataSet) obj2).getValues(), "(rriChartViewData.barDat…[0] as BarDataSet).values");
        mutableLiveData2.setValue(Boolean.valueOf(!Intrinsics.areEqual((BarEntry) CollectionsKt.last((List) r1), entry)));
    }

    public final void clockClicked() {
        this.historyShow.setValue(false);
        LiveData liveData = this.rriEntry;
        BarData barData = this.rriChartViewData.getBarData();
        Intrinsics.checkNotNullExpressionValue(barData, "rriChartViewData.barData");
        Object obj = barData.getDataSets().get(0);
        Objects.requireNonNull(obj, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarDataSet");
        List<T> values = ((BarDataSet) obj).getValues();
        Intrinsics.checkNotNullExpressionValue(values, "(rriChartViewData.barDat…[0] as BarDataSet).values");
        liveData.setValue(CollectionsKt.last((List) values));
        BarEntry value = this.rriEntry.getValue();
        Objects.requireNonNull(value, "null cannot be cast to non-null type com.github.mikephil.charting.data.BarEntry");
        markSelected(value);
    }

    public final Handler getPickerTimer() {
        return this.pickerTimer;
    }

    public final void setPickerTimer(Handler handler) {
        Intrinsics.checkNotNullParameter(handler, "<set-?>");
        this.pickerTimer = handler;
    }

    public final Runnable getPickerCancel() {
        return this.pickerCancel;
    }

    public final void setPickerCancel(Runnable runnable) {
        Intrinsics.checkNotNullParameter(runnable, "<set-?>");
        this.pickerCancel = runnable;
    }

    public final void pickerSelecting() {
        this.pickerTimer.removeCallbacks(this.pickerCancel);
        this.pickerTimer.postDelayed(this.pickerCancel, CoroutineLiveDataKt.DEFAULT_TIMEOUT);
    }

    public final void sendIntervalClick() {
        this.pickerShow.setValue(1);
        this.pickerTimer.postDelayed(this.pickerCancel, CoroutineLiveDataKt.DEFAULT_TIMEOUT);
        this.sendInterval.setValue(Integer.valueOf(Config.INSTANCE.getSendInterval()));
    }

    public final void topBottomClick() {
        this.pickerShow.setValue(2);
        this.pickerTimer.postDelayed(this.pickerCancel, CoroutineLiveDataKt.DEFAULT_TIMEOUT);
        this.top.setValue(Integer.valueOf(Config.INSTANCE.getTOP_MAX() - Config.INSTANCE.getTop()));
        this.bottom.setValue(Integer.valueOf(Config.INSTANCE.getBOTTOM_MAX() - Config.INSTANCE.getBottom()));
    }

    public final void pickerCancel() {
        this.pickerShow.setValue(0);
        this.pickerTimer.removeCallbacks(this.pickerCancel);
    }
}
