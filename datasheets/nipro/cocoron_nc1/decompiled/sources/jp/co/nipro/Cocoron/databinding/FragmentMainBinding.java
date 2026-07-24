package jp.co.nipro.cocoron.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import jp.co.nipro.Cocoron.C0009R;
import jp.co.nipro.cocoron.ui.fragment.MainFragment;
import jp.co.nipro.cocoron.ui.view.HolderView;
import jp.co.nipro.cocoron.ui.view.MarkView;
import jp.co.nipro.cocoron.ui.view.PickerView;
import jp.co.nipro.cocoron.ui.viewmodel.MainModel;

/* loaded from: classes.dex */
public abstract class FragmentMainBinding extends ViewDataBinding {
    public final ImageView batteryLabel;
    public final ImageView bottomImg;
    public final PickerView bottomPicker;
    public final ConstraintLayout calendar;
    public final ImageView connectIcon;
    public final TextView deviceName;
    public final ImageView dotImage;
    public final LinearLayout ecgArea;
    public final LineChart ecgChartView;
    public final ConstraintLayout ecgHeader;
    public final ImageView ecgTimeBk;
    public final LinearLayout flickArea;
    public final ImageView flickBackgroud;

    @Bindable
    protected MainFragment mFragment;

    @Bindable
    protected MainModel mModel;
    public final ConstraintLayout modeArea;
    public final HolderView mvHolder;
    public final ImageView outServiceIcon;
    public final LinearLayout rriArea;
    public final TextView rriBottomCurr;
    public final ImageView rriBpm;
    public final CombinedChart rriChartView;
    public final TextView rriDay;
    public final ConstraintLayout rriHeader;
    public final HolderView rriHolder;
    public final ConstraintLayout rriLimitArea;
    public final MarkView rriMarkView;
    public final TextView rriText;
    public final TextView rriTime;
    public final ImageView rriTimeBk;
    public final TextView rriTopCurr;
    public final HolderView secHolder;
    public final TextView sendInterval;
    public final ConstraintLayout sendIntervalArea;
    public final Button sendIntervalCancel;
    public final ImageView sendIntervalImg;
    public final Button sendIntervalOk;
    public final PickerView sendIntervalPicker;
    public final ConstraintLayout sendIntervalPickerArea;
    public final View space;
    public final Space spaceb;
    public final Space spacet;
    public final Button topBottomCancel;
    public final Button topBottomOk;
    public final ConstraintLayout topBottomPickerArea;
    public final ImageView topImg;
    public final PickerView topPicker;

    public abstract void setFragment(MainFragment fragment);

    public abstract void setModel(MainModel model);

    protected FragmentMainBinding(Object _bindingComponent, View _root, int _localFieldCount, ImageView batteryLabel, ImageView bottomImg, PickerView bottomPicker, ConstraintLayout calendar, ImageView connectIcon, TextView deviceName, ImageView dotImage, LinearLayout ecgArea, LineChart ecgChartView, ConstraintLayout ecgHeader, ImageView ecgTimeBk, LinearLayout flickArea, ImageView flickBackgroud, ConstraintLayout modeArea, HolderView mvHolder, ImageView outServiceIcon, LinearLayout rriArea, TextView rriBottomCurr, ImageView rriBpm, CombinedChart rriChartView, TextView rriDay, ConstraintLayout rriHeader, HolderView rriHolder, ConstraintLayout rriLimitArea, MarkView rriMarkView, TextView rriText, TextView rriTime, ImageView rriTimeBk, TextView rriTopCurr, HolderView secHolder, TextView sendInterval, ConstraintLayout sendIntervalArea, Button sendIntervalCancel, ImageView sendIntervalImg, Button sendIntervalOk, PickerView sendIntervalPicker, ConstraintLayout sendIntervalPickerArea, View space, Space spaceb, Space spacet, Button topBottomCancel, Button topBottomOk, ConstraintLayout topBottomPickerArea, ImageView topImg, PickerView topPicker) {
        super(_bindingComponent, _root, _localFieldCount);
        this.batteryLabel = batteryLabel;
        this.bottomImg = bottomImg;
        this.bottomPicker = bottomPicker;
        this.calendar = calendar;
        this.connectIcon = connectIcon;
        this.deviceName = deviceName;
        this.dotImage = dotImage;
        this.ecgArea = ecgArea;
        this.ecgChartView = ecgChartView;
        this.ecgHeader = ecgHeader;
        this.ecgTimeBk = ecgTimeBk;
        this.flickArea = flickArea;
        this.flickBackgroud = flickBackgroud;
        this.modeArea = modeArea;
        this.mvHolder = mvHolder;
        this.outServiceIcon = outServiceIcon;
        this.rriArea = rriArea;
        this.rriBottomCurr = rriBottomCurr;
        this.rriBpm = rriBpm;
        this.rriChartView = rriChartView;
        this.rriDay = rriDay;
        this.rriHeader = rriHeader;
        this.rriHolder = rriHolder;
        this.rriLimitArea = rriLimitArea;
        this.rriMarkView = rriMarkView;
        this.rriText = rriText;
        this.rriTime = rriTime;
        this.rriTimeBk = rriTimeBk;
        this.rriTopCurr = rriTopCurr;
        this.secHolder = secHolder;
        this.sendInterval = sendInterval;
        this.sendIntervalArea = sendIntervalArea;
        this.sendIntervalCancel = sendIntervalCancel;
        this.sendIntervalImg = sendIntervalImg;
        this.sendIntervalOk = sendIntervalOk;
        this.sendIntervalPicker = sendIntervalPicker;
        this.sendIntervalPickerArea = sendIntervalPickerArea;
        this.space = space;
        this.spaceb = spaceb;
        this.spacet = spacet;
        this.topBottomCancel = topBottomCancel;
        this.topBottomOk = topBottomOk;
        this.topBottomPickerArea = topBottomPickerArea;
        this.topImg = topImg;
        this.topPicker = topPicker;
    }

    public MainModel getModel() {
        return this.mModel;
    }

    public MainFragment getFragment() {
        return this.mFragment;
    }

    public static FragmentMainBinding inflate(LayoutInflater inflater, ViewGroup root, boolean attachToRoot) {
        return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static FragmentMainBinding inflate(LayoutInflater inflater, ViewGroup root, boolean attachToRoot, Object component) {
        return (FragmentMainBinding) ViewDataBinding.inflateInternal(inflater, C0009R.layout.fragment_main, root, attachToRoot, component);
    }

    public static FragmentMainBinding inflate(LayoutInflater inflater) {
        return inflate(inflater, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static FragmentMainBinding inflate(LayoutInflater inflater, Object component) {
        return (FragmentMainBinding) ViewDataBinding.inflateInternal(inflater, C0009R.layout.fragment_main, null, false, component);
    }

    public static FragmentMainBinding bind(View view) {
        return bind(view, DataBindingUtil.getDefaultComponent());
    }

    @Deprecated
    public static FragmentMainBinding bind(View view, Object component) {
        return (FragmentMainBinding) bind(component, view, C0009R.layout.fragment_main);
    }
}
