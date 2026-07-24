package jp.co.nipro.cocoron.databinding;

import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.adapters.TextViewBindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarEntry;
import java.util.List;
import jp.co.nipro.Cocoron.C0009R;
import jp.co.nipro.cocoron.generated.callback.InverseBindingListener;
import jp.co.nipro.cocoron.generated.callback.MarkViewListener;
import jp.co.nipro.cocoron.generated.callback.OnClickListener;
import jp.co.nipro.cocoron.ui.fragment.MainFragment;
import jp.co.nipro.cocoron.ui.view.FlickListener;
import jp.co.nipro.cocoron.ui.view.HolderView;
import jp.co.nipro.cocoron.ui.view.MarkView;
import jp.co.nipro.cocoron.ui.view.PickerView;
import jp.co.nipro.cocoron.ui.viewmodel.MainModel;

/* loaded from: classes.dex */
public class FragmentMainBindingImpl extends FragmentMainBinding implements InverseBindingListener.Listener, OnClickListener.Listener, MarkViewListener.Listener {
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    private static final SparseIntArray sViewsWithIds;
    private androidx.databinding.InverseBindingListener bottomPickercurrentItemAttrChanged;
    private final View.OnClickListener mCallback1;
    private final androidx.databinding.InverseBindingListener mCallback10;
    private final androidx.databinding.InverseBindingListener mCallback11;
    private final androidx.databinding.InverseBindingListener mCallback12;
    private final View.OnClickListener mCallback13;
    private final View.OnClickListener mCallback14;
    private final androidx.databinding.InverseBindingListener mCallback15;
    private final androidx.databinding.InverseBindingListener mCallback16;
    private final View.OnClickListener mCallback17;
    private final View.OnClickListener mCallback18;
    private final View.OnClickListener mCallback2;
    private final View.OnClickListener mCallback3;
    private final View.OnClickListener mCallback4;
    private final View.OnClickListener mCallback5;
    private final View.OnClickListener mCallback6;
    private final MarkView.MarkViewListener mCallback7;
    private final MarkView.MarkViewListener mCallback8;
    private final androidx.databinding.InverseBindingListener mCallback9;
    private long mDirtyFlags;
    private final FrameLayout mboundView0;
    private final TextView mboundView10;
    private final TextView mboundView12;
    private final TextView mboundView13;
    private final ImageView mboundView14;
    private final TextView mboundView16;
    private final TextView mboundView17;
    private final TextView mboundView2;
    private final ImageView mboundView20;
    private final TextView mboundView23;
    private final TextView mboundView24;
    private final TextView mboundView3;
    private final TextView mboundView6;
    private final TextView mboundView9;
    private androidx.databinding.InverseBindingListener mvHolderselectedAttrChanged;
    private androidx.databinding.InverseBindingListener rriHolderselectedAttrChanged;
    private androidx.databinding.InverseBindingListener rriMarkViewbarEntryAttrChanged;
    private androidx.databinding.InverseBindingListener secHolderselectedAttrChanged;
    private androidx.databinding.InverseBindingListener sendIntervalPickercurrentItemAttrChanged;
    private androidx.databinding.InverseBindingListener topPickercurrentItemAttrChanged;

    static {
        SparseIntArray sparseIntArray = new SparseIntArray();
        sViewsWithIds = sparseIntArray;
        sparseIntArray.put(C0009R.id.spacet, 36);
        sparseIntArray.put(C0009R.id.spaceb, 37);
        sparseIntArray.put(C0009R.id.rri_bpm, 38);
        sparseIntArray.put(C0009R.id.mode_area, 39);
        sparseIntArray.put(C0009R.id.space, 40);
        sparseIntArray.put(C0009R.id.send_interval_area, 41);
        sparseIntArray.put(C0009R.id.flick_backgroud, 42);
        sparseIntArray.put(C0009R.id.flick_area, 43);
        sparseIntArray.put(C0009R.id.rri_area, 44);
        sparseIntArray.put(C0009R.id.rri_header, 45);
        sparseIntArray.put(C0009R.id.rri_time_bk, 46);
        sparseIntArray.put(C0009R.id.rri_chart_view, 47);
        sparseIntArray.put(C0009R.id.ecg_area, 48);
        sparseIntArray.put(C0009R.id.ecg_header, 49);
        sparseIntArray.put(C0009R.id.ecg_time_bk, 50);
        sparseIntArray.put(C0009R.id.ecg_chart_view, 51);
        sparseIntArray.put(C0009R.id.connect_icon, 52);
        sparseIntArray.put(C0009R.id.out_service_icon, 53);
        sparseIntArray.put(C0009R.id.battery_label, 54);
        sparseIntArray.put(C0009R.id.dot_image, 55);
        sparseIntArray.put(C0009R.id.send_interval_img, 56);
        sparseIntArray.put(C0009R.id.top_img, 57);
        sparseIntArray.put(C0009R.id.bottom_img, 58);
    }

    public FragmentMainBindingImpl(DataBindingComponent bindingComponent, View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 59, sIncludes, sViewsWithIds));
    }

    private FragmentMainBindingImpl(DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 28, (ImageView) bindings[54], (ImageView) bindings[58], (PickerView) bindings[33], (ConstraintLayout) bindings[1], (ImageView) bindings[52], (TextView) bindings[4], (ImageView) bindings[55], (LinearLayout) bindings[48], (LineChart) bindings[51], (ConstraintLayout) bindings[49], (ImageView) bindings[50], (LinearLayout) bindings[43], (ImageView) bindings[42], (ConstraintLayout) bindings[39], (HolderView) bindings[25], (ImageView) bindings[53], (LinearLayout) bindings[44], (TextView) bindings[11], (ImageView) bindings[38], (CombinedChart) bindings[47], (TextView) bindings[18], (ConstraintLayout) bindings[45], (HolderView) bindings[22], (ConstraintLayout) bindings[7], (MarkView) bindings[21], (TextView) bindings[5], (TextView) bindings[19], (ImageView) bindings[46], (TextView) bindings[8], (HolderView) bindings[26], (TextView) bindings[15], (ConstraintLayout) bindings[41], (Button) bindings[29], (ImageView) bindings[56], (Button) bindings[30], (PickerView) bindings[28], (ConstraintLayout) bindings[27], (View) bindings[40], (Space) bindings[37], (Space) bindings[36], (Button) bindings[34], (Button) bindings[35], (ConstraintLayout) bindings[31], (ImageView) bindings[57], (PickerView) bindings[32]);
        this.bottomPickercurrentItemAttrChanged = new androidx.databinding.InverseBindingListener() { // from class: jp.co.nipro.cocoron.databinding.FragmentMainBindingImpl.1
            @Override // androidx.databinding.InverseBindingListener
            public void onChange() {
                int currentItem = FragmentMainBindingImpl.this.bottomPicker.getCurrentItem();
                MainModel mainModel = FragmentMainBindingImpl.this.mModel;
                if (mainModel != null) {
                    MutableLiveData<Integer> bottom = mainModel.getBottom();
                    if (bottom != null) {
                        bottom.setValue(Integer.valueOf(currentItem));
                    }
                }
            }
        };
        this.mvHolderselectedAttrChanged = new androidx.databinding.InverseBindingListener() { // from class: jp.co.nipro.cocoron.databinding.FragmentMainBindingImpl.2
            @Override // androidx.databinding.InverseBindingListener
            public void onChange() {
                int selected = FragmentMainBindingImpl.this.mvHolder.getSelected();
                MainModel mainModel = FragmentMainBindingImpl.this.mModel;
                if (mainModel != null) {
                    MutableLiveData<Integer> mvHolderSelected = mainModel.getMvHolderSelected();
                    if (mvHolderSelected != null) {
                        mvHolderSelected.setValue(Integer.valueOf(selected));
                    }
                }
            }
        };
        this.rriHolderselectedAttrChanged = new androidx.databinding.InverseBindingListener() { // from class: jp.co.nipro.cocoron.databinding.FragmentMainBindingImpl.3
            @Override // androidx.databinding.InverseBindingListener
            public void onChange() {
                int selected = FragmentMainBindingImpl.this.rriHolder.getSelected();
                MainModel mainModel = FragmentMainBindingImpl.this.mModel;
                if (mainModel != null) {
                    MutableLiveData<Integer> dayHolderSelected = mainModel.getDayHolderSelected();
                    if (dayHolderSelected != null) {
                        dayHolderSelected.setValue(Integer.valueOf(selected));
                    }
                }
            }
        };
        this.rriMarkViewbarEntryAttrChanged = new androidx.databinding.InverseBindingListener() { // from class: jp.co.nipro.cocoron.databinding.FragmentMainBindingImpl.4
            @Override // androidx.databinding.InverseBindingListener
            public void onChange() {
                BarEntry entry = FragmentMainBindingImpl.this.rriMarkView.getEntry();
                MainModel mainModel = FragmentMainBindingImpl.this.mModel;
                if (mainModel != null) {
                    MutableLiveData<BarEntry> rriEntry = mainModel.getRriEntry();
                    if (rriEntry != null) {
                        rriEntry.setValue(entry);
                    }
                }
            }
        };
        this.secHolderselectedAttrChanged = new androidx.databinding.InverseBindingListener() { // from class: jp.co.nipro.cocoron.databinding.FragmentMainBindingImpl.5
            @Override // androidx.databinding.InverseBindingListener
            public void onChange() {
                int selected = FragmentMainBindingImpl.this.secHolder.getSelected();
                MainModel mainModel = FragmentMainBindingImpl.this.mModel;
                if (mainModel != null) {
                    MutableLiveData<Integer> secHolderSelected = mainModel.getSecHolderSelected();
                    if (secHolderSelected != null) {
                        secHolderSelected.setValue(Integer.valueOf(selected));
                    }
                }
            }
        };
        this.sendIntervalPickercurrentItemAttrChanged = new androidx.databinding.InverseBindingListener() { // from class: jp.co.nipro.cocoron.databinding.FragmentMainBindingImpl.6
            @Override // androidx.databinding.InverseBindingListener
            public void onChange() {
                int currentItem = FragmentMainBindingImpl.this.sendIntervalPicker.getCurrentItem();
                MainModel mainModel = FragmentMainBindingImpl.this.mModel;
                if (mainModel != null) {
                    MutableLiveData<Integer> sendInterval = mainModel.getSendInterval();
                    if (sendInterval != null) {
                        sendInterval.setValue(Integer.valueOf(currentItem));
                    }
                }
            }
        };
        this.topPickercurrentItemAttrChanged = new androidx.databinding.InverseBindingListener() { // from class: jp.co.nipro.cocoron.databinding.FragmentMainBindingImpl.7
            @Override // androidx.databinding.InverseBindingListener
            public void onChange() {
                int currentItem = FragmentMainBindingImpl.this.topPicker.getCurrentItem();
                MainModel mainModel = FragmentMainBindingImpl.this.mModel;
                if (mainModel != null) {
                    MutableLiveData<Integer> top = mainModel.getTop();
                    if (top != null) {
                        top.setValue(Integer.valueOf(currentItem));
                    }
                }
            }
        };
        this.mDirtyFlags = -1L;
        this.bottomPicker.setTag(null);
        this.calendar.setTag(null);
        this.deviceName.setTag(null);
        FrameLayout frameLayout = (FrameLayout) bindings[0];
        this.mboundView0 = frameLayout;
        frameLayout.setTag(null);
        TextView textView = (TextView) bindings[10];
        this.mboundView10 = textView;
        textView.setTag(null);
        TextView textView2 = (TextView) bindings[12];
        this.mboundView12 = textView2;
        textView2.setTag(null);
        TextView textView3 = (TextView) bindings[13];
        this.mboundView13 = textView3;
        textView3.setTag(null);
        ImageView imageView = (ImageView) bindings[14];
        this.mboundView14 = imageView;
        imageView.setTag(null);
        TextView textView4 = (TextView) bindings[16];
        this.mboundView16 = textView4;
        textView4.setTag(null);
        TextView textView5 = (TextView) bindings[17];
        this.mboundView17 = textView5;
        textView5.setTag(null);
        TextView textView6 = (TextView) bindings[2];
        this.mboundView2 = textView6;
        textView6.setTag(null);
        ImageView imageView2 = (ImageView) bindings[20];
        this.mboundView20 = imageView2;
        imageView2.setTag(null);
        TextView textView7 = (TextView) bindings[23];
        this.mboundView23 = textView7;
        textView7.setTag(null);
        TextView textView8 = (TextView) bindings[24];
        this.mboundView24 = textView8;
        textView8.setTag(null);
        TextView textView9 = (TextView) bindings[3];
        this.mboundView3 = textView9;
        textView9.setTag(null);
        TextView textView10 = (TextView) bindings[6];
        this.mboundView6 = textView10;
        textView10.setTag(null);
        TextView textView11 = (TextView) bindings[9];
        this.mboundView9 = textView11;
        textView11.setTag(null);
        this.mvHolder.setTag(null);
        this.rriBottomCurr.setTag(null);
        this.rriDay.setTag(null);
        this.rriHolder.setTag(null);
        this.rriLimitArea.setTag(null);
        this.rriMarkView.setTag(null);
        this.rriText.setTag(null);
        this.rriTime.setTag(null);
        this.rriTopCurr.setTag(null);
        this.secHolder.setTag(null);
        this.sendInterval.setTag(null);
        this.sendIntervalCancel.setTag(null);
        this.sendIntervalOk.setTag(null);
        this.sendIntervalPicker.setTag(null);
        this.sendIntervalPickerArea.setTag(null);
        this.topBottomCancel.setTag(null);
        this.topBottomOk.setTag(null);
        this.topBottomPickerArea.setTag(null);
        this.topPicker.setTag(null);
        setRootTag(root);
        this.mCallback15 = new InverseBindingListener(this, 15);
        this.mCallback2 = new OnClickListener(this, 2);
        this.mCallback16 = new InverseBindingListener(this, 16);
        this.mCallback9 = new InverseBindingListener(this, 9);
        this.mCallback1 = new OnClickListener(this, 1);
        this.mCallback13 = new OnClickListener(this, 13);
        this.mCallback8 = new MarkViewListener(this, 8);
        this.mCallback14 = new OnClickListener(this, 14);
        this.mCallback7 = new MarkViewListener(this, 7);
        this.mCallback11 = new InverseBindingListener(this, 11);
        this.mCallback6 = new OnClickListener(this, 6);
        this.mCallback12 = new InverseBindingListener(this, 12);
        this.mCallback5 = new OnClickListener(this, 5);
        this.mCallback17 = new OnClickListener(this, 17);
        this.mCallback4 = new OnClickListener(this, 4);
        this.mCallback10 = new InverseBindingListener(this, 10);
        this.mCallback18 = new OnClickListener(this, 18);
        this.mCallback3 = new OnClickListener(this, 3);
        invalidateAll();
    }

    @Override // androidx.databinding.ViewDataBinding
    public void invalidateAll() {
        synchronized (this) {
            this.mDirtyFlags = 1073741824L;
        }
        requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    public boolean hasPendingBindings() {
        synchronized (this) {
            return this.mDirtyFlags != 0;
        }
    }

    @Override // androidx.databinding.ViewDataBinding
    public boolean setVariable(int variableId, Object variable) {
        if (2 == variableId) {
            setModel((MainModel) variable);
            return true;
        }
        if (1 != variableId) {
            return false;
        }
        setFragment((MainFragment) variable);
        return true;
    }

    @Override // jp.co.nipro.cocoron.databinding.FragmentMainBinding
    public void setModel(MainModel Model) {
        this.mModel = Model;
        synchronized (this) {
            this.mDirtyFlags |= 268435456;
        }
        notifyPropertyChanged(2);
        super.requestRebind();
    }

    @Override // jp.co.nipro.cocoron.databinding.FragmentMainBinding
    public void setFragment(MainFragment Fragment) {
        this.mFragment = Fragment;
        synchronized (this) {
            this.mDirtyFlags |= 536870912;
        }
        notifyPropertyChanged(1);
        super.requestRebind();
    }

    @Override // androidx.databinding.ViewDataBinding
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
            case 0:
                return onChangeModelBottom((MutableLiveData) object, fieldId);
            case 1:
                return onChangeModelMvHolderSelected((MutableLiveData) object, fieldId);
            case 2:
                return onChangeModelRriBottomCurr((MutableLiveData) object, fieldId);
            case 3:
                return onChangeModelEcgModeLive((MutableLiveData) object, fieldId);
            case 4:
                return onChangeModelEcgTime((MutableLiveData) object, fieldId);
            case 5:
                return onChangeModelSendIntervalNext((MutableLiveData) object, fieldId);
            case 6:
                return onChangeModelDeviceName((MutableLiveData) object, fieldId);
            case 7:
                return onChangeModelCurrDay((MutableLiveData) object, fieldId);
            case 8:
                return onChangeModelSecHolderSelected((MutableLiveData) object, fieldId);
            case 9:
                return onChangeModelPickerShow((MutableLiveData) object, fieldId);
            case 10:
                return onChangeModelRriTime((MutableLiveData) object, fieldId);
            case 11:
                return onChangeModelTop((MutableLiveData) object, fieldId);
            case 12:
                return onChangeModelRriBottomPrev((MutableLiveData) object, fieldId);
            case 13:
                return onChangeModelRriDay((MutableLiveData) object, fieldId);
            case 14:
                return onChangeModelDayHolderSelected((MutableLiveData) object, fieldId);
            case 15:
                return onChangeModelRriTopCurr((MutableLiveData) object, fieldId);
            case 16:
                return onChangeModelRriTopNext((MutableLiveData) object, fieldId);
            case 17:
                return onChangeModelSendIntervalCurr((MutableLiveData) object, fieldId);
            case 18:
                return onChangeModelRriStr((MutableLiveData) object, fieldId);
            case 19:
                return onChangeModelSendInterval((MutableLiveData) object, fieldId);
            case 20:
                return onChangeModelHistoryShow((MutableLiveData) object, fieldId);
            case 21:
                return onChangeFragmentViewBinding((FragmentMainBinding) object, fieldId);
            case 22:
                return onChangeModelRriTopPrev((MutableLiveData) object, fieldId);
            case 23:
                return onChangeModelRriEntry((MutableLiveData) object, fieldId);
            case 24:
                return onChangeModelSendIntervalPrev((MutableLiveData) object, fieldId);
            case 25:
                return onChangeModelEcgDay((MutableLiveData) object, fieldId);
            case 26:
                return onChangeModelCurrTime((MutableLiveData) object, fieldId);
            case 27:
                return onChangeModelRriBottomNext((MutableLiveData) object, fieldId);
            default:
                return false;
        }
    }

    private boolean onChangeModelBottom(MutableLiveData<Integer> ModelBottom, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        return true;
    }

    private boolean onChangeModelMvHolderSelected(MutableLiveData<Integer> ModelMvHolderSelected, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 2;
        }
        return true;
    }

    private boolean onChangeModelRriBottomCurr(MutableLiveData<String> ModelRriBottomCurr, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 4;
        }
        return true;
    }

    private boolean onChangeModelEcgModeLive(MutableLiveData<Integer> ModelEcgModeLive, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 8;
        }
        return true;
    }

    private boolean onChangeModelEcgTime(MutableLiveData<String> ModelEcgTime, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 16;
        }
        return true;
    }

    private boolean onChangeModelSendIntervalNext(MutableLiveData<String> ModelSendIntervalNext, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 32;
        }
        return true;
    }

    private boolean onChangeModelDeviceName(MutableLiveData<String> ModelDeviceName, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 64;
        }
        return true;
    }

    private boolean onChangeModelCurrDay(MutableLiveData<String> ModelCurrDay, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 128;
        }
        return true;
    }

    private boolean onChangeModelSecHolderSelected(MutableLiveData<Integer> ModelSecHolderSelected, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 256;
        }
        return true;
    }

    private boolean onChangeModelPickerShow(MutableLiveData<Integer> ModelPickerShow, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 512;
        }
        return true;
    }

    private boolean onChangeModelRriTime(MutableLiveData<String> ModelRriTime, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 1024;
        }
        return true;
    }

    private boolean onChangeModelTop(MutableLiveData<Integer> ModelTop, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 2048;
        }
        return true;
    }

    private boolean onChangeModelRriBottomPrev(MutableLiveData<String> ModelRriBottomPrev, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 4096;
        }
        return true;
    }

    private boolean onChangeModelRriDay(MutableLiveData<String> ModelRriDay, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 8192;
        }
        return true;
    }

    private boolean onChangeModelDayHolderSelected(MutableLiveData<Integer> ModelDayHolderSelected, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 16384;
        }
        return true;
    }

    private boolean onChangeModelRriTopCurr(MutableLiveData<String> ModelRriTopCurr, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 32768;
        }
        return true;
    }

    private boolean onChangeModelRriTopNext(MutableLiveData<String> ModelRriTopNext, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 65536;
        }
        return true;
    }

    private boolean onChangeModelSendIntervalCurr(MutableLiveData<String> ModelSendIntervalCurr, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 131072;
        }
        return true;
    }

    private boolean onChangeModelRriStr(MutableLiveData<String> ModelRriStr, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 262144;
        }
        return true;
    }

    private boolean onChangeModelSendInterval(MutableLiveData<Integer> ModelSendInterval, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 524288;
        }
        return true;
    }

    private boolean onChangeModelHistoryShow(MutableLiveData<Boolean> ModelHistoryShow, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 1048576;
        }
        return true;
    }

    private boolean onChangeFragmentViewBinding(FragmentMainBinding FragmentViewBinding, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 2097152;
        }
        return true;
    }

    private boolean onChangeModelRriTopPrev(MutableLiveData<String> ModelRriTopPrev, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 4194304;
        }
        return true;
    }

    private boolean onChangeModelRriEntry(MutableLiveData<BarEntry> ModelRriEntry, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 8388608;
        }
        return true;
    }

    private boolean onChangeModelSendIntervalPrev(MutableLiveData<String> ModelSendIntervalPrev, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 16777216;
        }
        return true;
    }

    private boolean onChangeModelEcgDay(MutableLiveData<String> ModelEcgDay, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 33554432;
        }
        return true;
    }

    private boolean onChangeModelCurrTime(MutableLiveData<String> ModelCurrTime, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 67108864;
        }
        return true;
    }

    private boolean onChangeModelRriBottomNext(MutableLiveData<String> ModelRriBottomNext, int fieldId) {
        if (fieldId != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 134217728;
        }
        return true;
    }

    /* JADX WARN: Removed duplicated region for block: B:115:0x0229  */
    /* JADX WARN: Removed duplicated region for block: B:122:0x025d  */
    /* JADX WARN: Removed duplicated region for block: B:130:0x028d  */
    /* JADX WARN: Removed duplicated region for block: B:137:0x02b7  */
    /* JADX WARN: Removed duplicated region for block: B:144:0x02de A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:148:0x0310  */
    /* JADX WARN: Removed duplicated region for block: B:156:0x034a  */
    /* JADX WARN: Removed duplicated region for block: B:163:0x0374  */
    /* JADX WARN: Removed duplicated region for block: B:170:0x039e  */
    /* JADX WARN: Removed duplicated region for block: B:177:0x03c8  */
    /* JADX WARN: Removed duplicated region for block: B:184:0x03f2  */
    /* JADX WARN: Removed duplicated region for block: B:192:0x0422  */
    /* JADX WARN: Removed duplicated region for block: B:207:0x0470  */
    /* JADX WARN: Removed duplicated region for block: B:214:0x0494  */
    /* JADX WARN: Removed duplicated region for block: B:221:0x04b8  */
    /* JADX WARN: Removed duplicated region for block: B:228:0x04e2  */
    /* JADX WARN: Removed duplicated region for block: B:235:0x050c  */
    /* JADX WARN: Removed duplicated region for block: B:242:0x0536  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x00a7  */
    /* JADX WARN: Removed duplicated region for block: B:364:0x0527  */
    /* JADX WARN: Removed duplicated region for block: B:367:0x04fd  */
    /* JADX WARN: Removed duplicated region for block: B:370:0x04d3  */
    /* JADX WARN: Removed duplicated region for block: B:378:0x0462  */
    /* JADX WARN: Removed duplicated region for block: B:381:0x0414  */
    /* JADX WARN: Removed duplicated region for block: B:384:0x03e3  */
    /* JADX WARN: Removed duplicated region for block: B:387:0x03b9  */
    /* JADX WARN: Removed duplicated region for block: B:390:0x038f  */
    /* JADX WARN: Removed duplicated region for block: B:393:0x0365  */
    /* JADX WARN: Removed duplicated region for block: B:396:0x033a  */
    /* JADX WARN: Removed duplicated region for block: B:400:0x02d2  */
    /* JADX WARN: Removed duplicated region for block: B:403:0x02a8  */
    /* JADX WARN: Removed duplicated region for block: B:406:0x027f  */
    /* JADX WARN: Removed duplicated region for block: B:409:0x024c  */
    /* JADX WARN: Removed duplicated region for block: B:415:0x021a  */
    /* JADX WARN: Removed duplicated region for block: B:418:0x01ac  */
    /* JADX WARN: Removed duplicated region for block: B:432:0x0103  */
    /* JADX WARN: Removed duplicated region for block: B:52:0x010d  */
    /* JADX WARN: Removed duplicated region for block: B:59:0x012a  */
    /* JADX WARN: Removed duplicated region for block: B:66:0x014a  */
    /* JADX WARN: Removed duplicated region for block: B:73:0x016b  */
    /* JADX WARN: Removed duplicated region for block: B:80:0x0190  */
    /* JADX WARN: Removed duplicated region for block: B:88:0x01b5  */
    @Override // androidx.databinding.ViewDataBinding
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    protected void executeBindings() {
        long j;
        String str;
        String str2;
        String str3;
        String str4;
        List<String> list;
        ArrayWheelAdapter<String> arrayWheelAdapter;
        String str5;
        ArrayWheelAdapter<String> arrayWheelAdapter2;
        ArrayWheelAdapter<String> arrayWheelAdapter3;
        List<String> list2;
        List<String> list3;
        String str6;
        String str7;
        String str8;
        BarEntry barEntry;
        String str9;
        String str10;
        String str11;
        String str12;
        String str13;
        String str14;
        String str15;
        String str16;
        String str17;
        String str18;
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        String str19;
        CombinedChart combinedChart;
        FlickListener.Listener listener;
        FlickListener.Listener listener2;
        FragmentMainBinding fragmentMainBinding;
        int i11;
        int i12;
        String str20;
        long j2;
        String str21;
        int i13;
        String str22;
        String str23;
        String str24;
        String str25;
        int i14;
        long j3;
        String str26;
        int i15;
        int i16;
        int i17;
        int i18;
        String str27;
        String str28;
        int i19;
        int i20;
        String str29;
        String str30;
        String str31;
        List<String> list4;
        ArrayWheelAdapter<String> arrayWheelAdapter4;
        ArrayWheelAdapter<String> arrayWheelAdapter5;
        ArrayWheelAdapter<String> arrayWheelAdapter6;
        List<String> list5;
        List<String> list6;
        String str32;
        List<String> list7;
        int i21;
        int i22;
        String str33;
        String str34;
        String str35;
        String str36;
        String str37;
        String str38;
        String str39;
        String str40;
        int i23;
        long j4;
        int i24;
        int i25;
        int i26;
        String str41;
        BarEntry barEntry2;
        String str42;
        String str43;
        String str44;
        String str45;
        String str46;
        String str47;
        LiveData<?> liveData;
        LiveData<?> liveData2;
        LiveData<?> liveData3;
        LiveData<?> liveData4;
        LiveData<?> liveData5;
        LiveData<?> liveData6;
        LiveData<?> liveData7;
        LiveData<?> liveData8;
        LiveData<?> liveData9;
        LiveData<?> liveData10;
        LiveData<?> liveData11;
        LiveData<?> liveData12;
        LiveData<?> liveData13;
        LiveData<?> liveData14;
        LiveData<?> liveData15;
        int i27;
        boolean z;
        long j5;
        long j6;
        synchronized (this) {
            j = this.mDirtyFlags;
            this.mDirtyFlags = 0L;
        }
        MainModel mainModel = this.mModel;
        MainFragment mainFragment = this.mFragment;
        if ((1608515583 & j) != 0) {
            if ((j & 1342177281) != 0) {
                LiveData<?> bottom = mainModel != null ? mainModel.getBottom() : null;
                updateLiveDataRegistration(0, bottom);
                i11 = ViewDataBinding.safeUnbox(bottom != null ? bottom.getValue() : null);
            } else {
                i11 = 0;
            }
            if ((j & 1342177282) != 0) {
                LiveData<?> mvHolderSelected = mainModel != null ? mainModel.getMvHolderSelected() : null;
                updateLiveDataRegistration(1, mvHolderSelected);
                i12 = ViewDataBinding.safeUnbox(mvHolderSelected != null ? mvHolderSelected.getValue() : null);
            } else {
                i12 = 0;
            }
            if ((j & 1342177284) != 0) {
                LiveData<?> rriBottomCurr = mainModel != null ? mainModel.getRriBottomCurr() : null;
                updateLiveDataRegistration(2, rriBottomCurr);
                if (rriBottomCurr != null) {
                    str20 = rriBottomCurr.getValue();
                    j2 = j & 1342177288;
                    if (j2 == 0) {
                        LiveData<?> ecgModeLive = mainModel != null ? mainModel.getEcgModeLive() : null;
                        updateLiveDataRegistration(3, ecgModeLive);
                        boolean z2 = ViewDataBinding.safeUnbox(ecgModeLive != null ? ecgModeLive.getValue() : null) == 0;
                        if (j2 != 0) {
                            if (z2) {
                                j5 = j | 4294967296L;
                                j6 = 68719476736L;
                            } else {
                                j5 = j | 2147483648L;
                                j6 = 34359738368L;
                            }
                            j = j5 | j6;
                        }
                        str21 = z2 ? "心電\n送信" : "心電\n送信中";
                        i13 = z2 ? getColorFromResource(this.mboundView6, C0009R.color.gray) : getColorFromResource(this.mboundView6, C0009R.color.white);
                    } else {
                        str21 = null;
                        i13 = 0;
                    }
                    if ((j & 1342177296) != 0) {
                        LiveData<?> ecgTime = mainModel != null ? mainModel.getEcgTime() : null;
                        updateLiveDataRegistration(4, ecgTime);
                        if (ecgTime != null) {
                            str22 = ecgTime.getValue();
                            if ((j & 1342177312) != 0) {
                                LiveData<?> sendIntervalNext = mainModel != null ? mainModel.getSendIntervalNext() : null;
                                updateLiveDataRegistration(5, sendIntervalNext);
                                if (sendIntervalNext != null) {
                                    str23 = sendIntervalNext.getValue();
                                    if ((j & 1342177344) != 0) {
                                        LiveData<?> deviceName = mainModel != null ? mainModel.getDeviceName() : null;
                                        updateLiveDataRegistration(6, deviceName);
                                        if (deviceName != null) {
                                            str24 = deviceName.getValue();
                                            if ((j & 1342177408) != 0) {
                                                LiveData<?> currDay = mainModel != null ? mainModel.getCurrDay() : null;
                                                updateLiveDataRegistration(7, currDay);
                                                if (currDay != null) {
                                                    str25 = currDay.getValue();
                                                    if ((j & 1342177536) == 0) {
                                                        LiveData<?> secHolderSelected = mainModel != null ? mainModel.getSecHolderSelected() : null;
                                                        updateLiveDataRegistration(8, secHolderSelected);
                                                        i14 = ViewDataBinding.safeUnbox(secHolderSelected != null ? secHolderSelected.getValue() : null);
                                                    } else {
                                                        i14 = 0;
                                                    }
                                                    j3 = j & 1342177792;
                                                    if (j3 == 0) {
                                                        if (mainModel != null) {
                                                            str26 = str25;
                                                            liveData15 = mainModel.getPickerShow();
                                                        } else {
                                                            str26 = str25;
                                                            liveData15 = null;
                                                        }
                                                        updateLiveDataRegistration(9, liveData15);
                                                        int safeUnbox = ViewDataBinding.safeUnbox(liveData15 != null ? liveData15.getValue() : null);
                                                        if (safeUnbox == 2) {
                                                            i27 = 1;
                                                            z = true;
                                                        } else {
                                                            i27 = 1;
                                                            z = false;
                                                        }
                                                        if (safeUnbox != i27) {
                                                            i27 = 0;
                                                        }
                                                        if (j3 != 0) {
                                                            j |= z ? 17179869184L : 8589934592L;
                                                        }
                                                        if ((j & 1342177792) != 0) {
                                                            j |= i27 != 0 ? 1099511627776L : 549755813888L;
                                                        }
                                                        i15 = z ? 0 : 8;
                                                        if (i27 == 0) {
                                                            i16 = 8;
                                                            if ((j & 1342178304) != 0) {
                                                                if (mainModel != null) {
                                                                    i18 = i14;
                                                                    i17 = i15;
                                                                    liveData14 = mainModel.getRriTime();
                                                                } else {
                                                                    i17 = i15;
                                                                    i18 = i14;
                                                                    liveData14 = null;
                                                                }
                                                                updateLiveDataRegistration(10, liveData14);
                                                                if (liveData14 != null) {
                                                                    str27 = liveData14.getValue();
                                                                    if ((j & 1342179328) == 0) {
                                                                        if (mainModel != null) {
                                                                            liveData13 = mainModel.getTop();
                                                                            str28 = str27;
                                                                        } else {
                                                                            str28 = str27;
                                                                            liveData13 = null;
                                                                        }
                                                                        updateLiveDataRegistration(11, liveData13);
                                                                        i19 = ViewDataBinding.safeUnbox(liveData13 != null ? liveData13.getValue() : null);
                                                                    } else {
                                                                        str28 = str27;
                                                                        i19 = 0;
                                                                    }
                                                                    if ((j & 1342181376) == 0) {
                                                                        if (mainModel != null) {
                                                                            liveData12 = mainModel.getRriBottomPrev();
                                                                            i20 = i19;
                                                                        } else {
                                                                            i20 = i19;
                                                                            liveData12 = null;
                                                                        }
                                                                        updateLiveDataRegistration(12, liveData12);
                                                                        if (liveData12 != null) {
                                                                            str29 = liveData12.getValue();
                                                                            if ((j & 1342185472) != 0) {
                                                                                if (mainModel != null) {
                                                                                    liveData11 = mainModel.getRriDay();
                                                                                    str30 = str29;
                                                                                } else {
                                                                                    str30 = str29;
                                                                                    liveData11 = null;
                                                                                }
                                                                                updateLiveDataRegistration(13, liveData11);
                                                                                if (liveData11 != null) {
                                                                                    str31 = liveData11.getValue();
                                                                                    if ((j & 1342177280) != 0 || mainModel == null) {
                                                                                        list4 = null;
                                                                                        arrayWheelAdapter4 = null;
                                                                                        arrayWheelAdapter5 = null;
                                                                                        arrayWheelAdapter6 = null;
                                                                                        list5 = null;
                                                                                        list6 = null;
                                                                                    } else {
                                                                                        list4 = mainModel.getSecHolderTags();
                                                                                        arrayWheelAdapter4 = mainModel.getBottomAdapter();
                                                                                        arrayWheelAdapter5 = mainModel.getTopAdapter();
                                                                                        arrayWheelAdapter6 = mainModel.getSendIntervalAdapter();
                                                                                        list5 = mainModel.getDayHolderTags();
                                                                                        list6 = mainModel.getMvHolderTags();
                                                                                    }
                                                                                    if ((j & 1342193664) == 0) {
                                                                                        if (mainModel != null) {
                                                                                            list7 = list4;
                                                                                            str32 = str31;
                                                                                            liveData10 = mainModel.getDayHolderSelected();
                                                                                        } else {
                                                                                            str32 = str31;
                                                                                            list7 = list4;
                                                                                            liveData10 = null;
                                                                                        }
                                                                                        updateLiveDataRegistration(14, liveData10);
                                                                                        i21 = ViewDataBinding.safeUnbox(liveData10 != null ? liveData10.getValue() : null);
                                                                                    } else {
                                                                                        str32 = str31;
                                                                                        list7 = list4;
                                                                                        i21 = 0;
                                                                                    }
                                                                                    if ((j & 1342210048) == 0) {
                                                                                        if (mainModel != null) {
                                                                                            liveData9 = mainModel.getRriTopCurr();
                                                                                            i22 = i21;
                                                                                        } else {
                                                                                            i22 = i21;
                                                                                            liveData9 = null;
                                                                                        }
                                                                                        updateLiveDataRegistration(15, liveData9);
                                                                                        if (liveData9 != null) {
                                                                                            str33 = liveData9.getValue();
                                                                                            if ((j & 1342242816) != 0) {
                                                                                                if (mainModel != null) {
                                                                                                    liveData8 = mainModel.getRriTopNext();
                                                                                                    str34 = str33;
                                                                                                } else {
                                                                                                    str34 = str33;
                                                                                                    liveData8 = null;
                                                                                                }
                                                                                                updateLiveDataRegistration(16, liveData8);
                                                                                                if (liveData8 != null) {
                                                                                                    str35 = liveData8.getValue();
                                                                                                    if ((j & 1342308352) == 0) {
                                                                                                        if (mainModel != null) {
                                                                                                            liveData7 = mainModel.getSendIntervalCurr();
                                                                                                            str36 = str35;
                                                                                                        } else {
                                                                                                            str36 = str35;
                                                                                                            liveData7 = null;
                                                                                                        }
                                                                                                        updateLiveDataRegistration(17, liveData7);
                                                                                                        if (liveData7 != null) {
                                                                                                            str37 = liveData7.getValue();
                                                                                                            if ((j & 1342439424) != 0) {
                                                                                                                if (mainModel != null) {
                                                                                                                    liveData6 = mainModel.getRriStr();
                                                                                                                    str38 = str37;
                                                                                                                } else {
                                                                                                                    str38 = str37;
                                                                                                                    liveData6 = null;
                                                                                                                }
                                                                                                                updateLiveDataRegistration(18, liveData6);
                                                                                                                if (liveData6 != null) {
                                                                                                                    str39 = liveData6.getValue();
                                                                                                                    if ((j & 1342701568) == 0) {
                                                                                                                        if (mainModel != null) {
                                                                                                                            liveData5 = mainModel.getSendInterval();
                                                                                                                            str40 = str39;
                                                                                                                        } else {
                                                                                                                            str40 = str39;
                                                                                                                            liveData5 = null;
                                                                                                                        }
                                                                                                                        updateLiveDataRegistration(19, liveData5);
                                                                                                                        i23 = ViewDataBinding.safeUnbox(liveData5 != null ? liveData5.getValue() : null);
                                                                                                                    } else {
                                                                                                                        str40 = str39;
                                                                                                                        i23 = 0;
                                                                                                                    }
                                                                                                                    j4 = j & 1343225856;
                                                                                                                    if (j4 == 0) {
                                                                                                                        if (mainModel != null) {
                                                                                                                            i25 = i11;
                                                                                                                            i24 = i23;
                                                                                                                            liveData4 = mainModel.getHistoryShow();
                                                                                                                        } else {
                                                                                                                            i24 = i23;
                                                                                                                            i25 = i11;
                                                                                                                            liveData4 = null;
                                                                                                                        }
                                                                                                                        updateLiveDataRegistration(20, liveData4);
                                                                                                                        boolean safeUnbox2 = ViewDataBinding.safeUnbox(liveData4 != null ? liveData4.getValue() : null);
                                                                                                                        if (j4 != 0) {
                                                                                                                            j |= safeUnbox2 ? 274877906944L : 137438953472L;
                                                                                                                        }
                                                                                                                        if (!safeUnbox2) {
                                                                                                                            i26 = 4;
                                                                                                                            if ((j & 1346371584) != 0) {
                                                                                                                                LiveData<?> rriTopPrev = mainModel != null ? mainModel.getRriTopPrev() : null;
                                                                                                                                updateLiveDataRegistration(22, rriTopPrev);
                                                                                                                                if (rriTopPrev != null) {
                                                                                                                                    str41 = rriTopPrev.getValue();
                                                                                                                                    if ((j & 1350565888) != 0) {
                                                                                                                                        LiveData<?> rriEntry = mainModel != null ? mainModel.getRriEntry() : null;
                                                                                                                                        updateLiveDataRegistration(23, rriEntry);
                                                                                                                                        if (rriEntry != null) {
                                                                                                                                            barEntry2 = rriEntry.getValue();
                                                                                                                                            if ((j & 1358954496) != 0) {
                                                                                                                                                if (mainModel != null) {
                                                                                                                                                    liveData3 = mainModel.getSendIntervalPrev();
                                                                                                                                                    str42 = str41;
                                                                                                                                                } else {
                                                                                                                                                    str42 = str41;
                                                                                                                                                    liveData3 = null;
                                                                                                                                                }
                                                                                                                                                updateLiveDataRegistration(24, liveData3);
                                                                                                                                                if (liveData3 != null) {
                                                                                                                                                    str43 = liveData3.getValue();
                                                                                                                                                    if ((j & 1375731712) == 0) {
                                                                                                                                                        if (mainModel != null) {
                                                                                                                                                            liveData2 = mainModel.getEcgDay();
                                                                                                                                                            str44 = str43;
                                                                                                                                                        } else {
                                                                                                                                                            str44 = str43;
                                                                                                                                                            liveData2 = null;
                                                                                                                                                        }
                                                                                                                                                        updateLiveDataRegistration(25, liveData2);
                                                                                                                                                        if (liveData2 != null) {
                                                                                                                                                            str45 = liveData2.getValue();
                                                                                                                                                            if ((j & 1409286144) != 0) {
                                                                                                                                                                if (mainModel != null) {
                                                                                                                                                                    liveData = mainModel.getCurrTime();
                                                                                                                                                                    str46 = str45;
                                                                                                                                                                } else {
                                                                                                                                                                    str46 = str45;
                                                                                                                                                                    liveData = null;
                                                                                                                                                                }
                                                                                                                                                                updateLiveDataRegistration(26, liveData);
                                                                                                                                                                if (liveData != null) {
                                                                                                                                                                    str47 = liveData.getValue();
                                                                                                                                                                    if ((j & 1476395008) != 0) {
                                                                                                                                                                        LiveData<?> rriBottomNext = mainModel != null ? mainModel.getRriBottomNext() : null;
                                                                                                                                                                        updateLiveDataRegistration(27, rriBottomNext);
                                                                                                                                                                        if (rriBottomNext != null) {
                                                                                                                                                                            str = rriBottomNext.getValue();
                                                                                                                                                                            i3 = i13;
                                                                                                                                                                            i4 = i12;
                                                                                                                                                                            str18 = str20;
                                                                                                                                                                            i8 = i16;
                                                                                                                                                                            i9 = i17;
                                                                                                                                                                            i6 = i18;
                                                                                                                                                                            i10 = i20;
                                                                                                                                                                            arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                                                                                                            list = list5;
                                                                                                                                                                            list3 = list7;
                                                                                                                                                                            i5 = i22;
                                                                                                                                                                            str6 = str34;
                                                                                                                                                                            str10 = str36;
                                                                                                                                                                            str17 = str38;
                                                                                                                                                                            str8 = str40;
                                                                                                                                                                            i7 = i24;
                                                                                                                                                                            i = i25;
                                                                                                                                                                            str4 = str44;
                                                                                                                                                                            str13 = str22;
                                                                                                                                                                            str15 = str26;
                                                                                                                                                                            arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                                                                                                            str11 = str21;
                                                                                                                                                                            i2 = i26;
                                                                                                                                                                            str7 = str28;
                                                                                                                                                                            arrayWheelAdapter = arrayWheelAdapter5;
                                                                                                                                                                            str9 = str32;
                                                                                                                                                                            str14 = str46;
                                                                                                                                                                            String str48 = str30;
                                                                                                                                                                            str12 = str47;
                                                                                                                                                                            str2 = str42;
                                                                                                                                                                            barEntry = barEntry2;
                                                                                                                                                                            str3 = str48;
                                                                                                                                                                            List<String> list8 = list6;
                                                                                                                                                                            str16 = str23;
                                                                                                                                                                            str5 = str24;
                                                                                                                                                                            list2 = list8;
                                                                                                                                                                        }
                                                                                                                                                                    }
                                                                                                                                                                    i3 = i13;
                                                                                                                                                                    i4 = i12;
                                                                                                                                                                    str18 = str20;
                                                                                                                                                                    i8 = i16;
                                                                                                                                                                    str = null;
                                                                                                                                                                    i9 = i17;
                                                                                                                                                                    i6 = i18;
                                                                                                                                                                    i10 = i20;
                                                                                                                                                                    arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                                                                                                    list = list5;
                                                                                                                                                                    list3 = list7;
                                                                                                                                                                    i5 = i22;
                                                                                                                                                                    str6 = str34;
                                                                                                                                                                    str10 = str36;
                                                                                                                                                                    str17 = str38;
                                                                                                                                                                    str8 = str40;
                                                                                                                                                                    i7 = i24;
                                                                                                                                                                    i = i25;
                                                                                                                                                                    str4 = str44;
                                                                                                                                                                    str13 = str22;
                                                                                                                                                                    str15 = str26;
                                                                                                                                                                    arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                                                                                                    str11 = str21;
                                                                                                                                                                    i2 = i26;
                                                                                                                                                                    str7 = str28;
                                                                                                                                                                    arrayWheelAdapter = arrayWheelAdapter5;
                                                                                                                                                                    str9 = str32;
                                                                                                                                                                    str14 = str46;
                                                                                                                                                                    String str482 = str30;
                                                                                                                                                                    str12 = str47;
                                                                                                                                                                    str2 = str42;
                                                                                                                                                                    barEntry = barEntry2;
                                                                                                                                                                    str3 = str482;
                                                                                                                                                                    List<String> list82 = list6;
                                                                                                                                                                    str16 = str23;
                                                                                                                                                                    str5 = str24;
                                                                                                                                                                    list2 = list82;
                                                                                                                                                                }
                                                                                                                                                            } else {
                                                                                                                                                                str46 = str45;
                                                                                                                                                            }
                                                                                                                                                            str47 = null;
                                                                                                                                                            if ((j & 1476395008) != 0) {
                                                                                                                                                            }
                                                                                                                                                            i3 = i13;
                                                                                                                                                            i4 = i12;
                                                                                                                                                            str18 = str20;
                                                                                                                                                            i8 = i16;
                                                                                                                                                            str = null;
                                                                                                                                                            i9 = i17;
                                                                                                                                                            i6 = i18;
                                                                                                                                                            i10 = i20;
                                                                                                                                                            arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                                                                                            list = list5;
                                                                                                                                                            list3 = list7;
                                                                                                                                                            i5 = i22;
                                                                                                                                                            str6 = str34;
                                                                                                                                                            str10 = str36;
                                                                                                                                                            str17 = str38;
                                                                                                                                                            str8 = str40;
                                                                                                                                                            i7 = i24;
                                                                                                                                                            i = i25;
                                                                                                                                                            str4 = str44;
                                                                                                                                                            str13 = str22;
                                                                                                                                                            str15 = str26;
                                                                                                                                                            arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                                                                                            str11 = str21;
                                                                                                                                                            i2 = i26;
                                                                                                                                                            str7 = str28;
                                                                                                                                                            arrayWheelAdapter = arrayWheelAdapter5;
                                                                                                                                                            str9 = str32;
                                                                                                                                                            str14 = str46;
                                                                                                                                                            String str4822 = str30;
                                                                                                                                                            str12 = str47;
                                                                                                                                                            str2 = str42;
                                                                                                                                                            barEntry = barEntry2;
                                                                                                                                                            str3 = str4822;
                                                                                                                                                            List<String> list822 = list6;
                                                                                                                                                            str16 = str23;
                                                                                                                                                            str5 = str24;
                                                                                                                                                            list2 = list822;
                                                                                                                                                        }
                                                                                                                                                    } else {
                                                                                                                                                        str44 = str43;
                                                                                                                                                    }
                                                                                                                                                    str45 = null;
                                                                                                                                                    if ((j & 1409286144) != 0) {
                                                                                                                                                    }
                                                                                                                                                    str47 = null;
                                                                                                                                                    if ((j & 1476395008) != 0) {
                                                                                                                                                    }
                                                                                                                                                    i3 = i13;
                                                                                                                                                    i4 = i12;
                                                                                                                                                    str18 = str20;
                                                                                                                                                    i8 = i16;
                                                                                                                                                    str = null;
                                                                                                                                                    i9 = i17;
                                                                                                                                                    i6 = i18;
                                                                                                                                                    i10 = i20;
                                                                                                                                                    arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                                                                                    list = list5;
                                                                                                                                                    list3 = list7;
                                                                                                                                                    i5 = i22;
                                                                                                                                                    str6 = str34;
                                                                                                                                                    str10 = str36;
                                                                                                                                                    str17 = str38;
                                                                                                                                                    str8 = str40;
                                                                                                                                                    i7 = i24;
                                                                                                                                                    i = i25;
                                                                                                                                                    str4 = str44;
                                                                                                                                                    str13 = str22;
                                                                                                                                                    str15 = str26;
                                                                                                                                                    arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                                                                                    str11 = str21;
                                                                                                                                                    i2 = i26;
                                                                                                                                                    str7 = str28;
                                                                                                                                                    arrayWheelAdapter = arrayWheelAdapter5;
                                                                                                                                                    str9 = str32;
                                                                                                                                                    str14 = str46;
                                                                                                                                                    String str48222 = str30;
                                                                                                                                                    str12 = str47;
                                                                                                                                                    str2 = str42;
                                                                                                                                                    barEntry = barEntry2;
                                                                                                                                                    str3 = str48222;
                                                                                                                                                    List<String> list8222 = list6;
                                                                                                                                                    str16 = str23;
                                                                                                                                                    str5 = str24;
                                                                                                                                                    list2 = list8222;
                                                                                                                                                }
                                                                                                                                            } else {
                                                                                                                                                str42 = str41;
                                                                                                                                            }
                                                                                                                                            str43 = null;
                                                                                                                                            if ((j & 1375731712) == 0) {
                                                                                                                                            }
                                                                                                                                            str45 = null;
                                                                                                                                            if ((j & 1409286144) != 0) {
                                                                                                                                            }
                                                                                                                                            str47 = null;
                                                                                                                                            if ((j & 1476395008) != 0) {
                                                                                                                                            }
                                                                                                                                            i3 = i13;
                                                                                                                                            i4 = i12;
                                                                                                                                            str18 = str20;
                                                                                                                                            i8 = i16;
                                                                                                                                            str = null;
                                                                                                                                            i9 = i17;
                                                                                                                                            i6 = i18;
                                                                                                                                            i10 = i20;
                                                                                                                                            arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                                                                            list = list5;
                                                                                                                                            list3 = list7;
                                                                                                                                            i5 = i22;
                                                                                                                                            str6 = str34;
                                                                                                                                            str10 = str36;
                                                                                                                                            str17 = str38;
                                                                                                                                            str8 = str40;
                                                                                                                                            i7 = i24;
                                                                                                                                            i = i25;
                                                                                                                                            str4 = str44;
                                                                                                                                            str13 = str22;
                                                                                                                                            str15 = str26;
                                                                                                                                            arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                                                                            str11 = str21;
                                                                                                                                            i2 = i26;
                                                                                                                                            str7 = str28;
                                                                                                                                            arrayWheelAdapter = arrayWheelAdapter5;
                                                                                                                                            str9 = str32;
                                                                                                                                            str14 = str46;
                                                                                                                                            String str482222 = str30;
                                                                                                                                            str12 = str47;
                                                                                                                                            str2 = str42;
                                                                                                                                            barEntry = barEntry2;
                                                                                                                                            str3 = str482222;
                                                                                                                                            List<String> list82222 = list6;
                                                                                                                                            str16 = str23;
                                                                                                                                            str5 = str24;
                                                                                                                                            list2 = list82222;
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                    barEntry2 = null;
                                                                                                                                    if ((j & 1358954496) != 0) {
                                                                                                                                    }
                                                                                                                                    str43 = null;
                                                                                                                                    if ((j & 1375731712) == 0) {
                                                                                                                                    }
                                                                                                                                    str45 = null;
                                                                                                                                    if ((j & 1409286144) != 0) {
                                                                                                                                    }
                                                                                                                                    str47 = null;
                                                                                                                                    if ((j & 1476395008) != 0) {
                                                                                                                                    }
                                                                                                                                    i3 = i13;
                                                                                                                                    i4 = i12;
                                                                                                                                    str18 = str20;
                                                                                                                                    i8 = i16;
                                                                                                                                    str = null;
                                                                                                                                    i9 = i17;
                                                                                                                                    i6 = i18;
                                                                                                                                    i10 = i20;
                                                                                                                                    arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                                                                    list = list5;
                                                                                                                                    list3 = list7;
                                                                                                                                    i5 = i22;
                                                                                                                                    str6 = str34;
                                                                                                                                    str10 = str36;
                                                                                                                                    str17 = str38;
                                                                                                                                    str8 = str40;
                                                                                                                                    i7 = i24;
                                                                                                                                    i = i25;
                                                                                                                                    str4 = str44;
                                                                                                                                    str13 = str22;
                                                                                                                                    str15 = str26;
                                                                                                                                    arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                                                                    str11 = str21;
                                                                                                                                    i2 = i26;
                                                                                                                                    str7 = str28;
                                                                                                                                    arrayWheelAdapter = arrayWheelAdapter5;
                                                                                                                                    str9 = str32;
                                                                                                                                    str14 = str46;
                                                                                                                                    String str4822222 = str30;
                                                                                                                                    str12 = str47;
                                                                                                                                    str2 = str42;
                                                                                                                                    barEntry = barEntry2;
                                                                                                                                    str3 = str4822222;
                                                                                                                                    List<String> list822222 = list6;
                                                                                                                                    str16 = str23;
                                                                                                                                    str5 = str24;
                                                                                                                                    list2 = list822222;
                                                                                                                                }
                                                                                                                            }
                                                                                                                            str41 = null;
                                                                                                                            if ((j & 1350565888) != 0) {
                                                                                                                            }
                                                                                                                            barEntry2 = null;
                                                                                                                            if ((j & 1358954496) != 0) {
                                                                                                                            }
                                                                                                                            str43 = null;
                                                                                                                            if ((j & 1375731712) == 0) {
                                                                                                                            }
                                                                                                                            str45 = null;
                                                                                                                            if ((j & 1409286144) != 0) {
                                                                                                                            }
                                                                                                                            str47 = null;
                                                                                                                            if ((j & 1476395008) != 0) {
                                                                                                                            }
                                                                                                                            i3 = i13;
                                                                                                                            i4 = i12;
                                                                                                                            str18 = str20;
                                                                                                                            i8 = i16;
                                                                                                                            str = null;
                                                                                                                            i9 = i17;
                                                                                                                            i6 = i18;
                                                                                                                            i10 = i20;
                                                                                                                            arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                                                            list = list5;
                                                                                                                            list3 = list7;
                                                                                                                            i5 = i22;
                                                                                                                            str6 = str34;
                                                                                                                            str10 = str36;
                                                                                                                            str17 = str38;
                                                                                                                            str8 = str40;
                                                                                                                            i7 = i24;
                                                                                                                            i = i25;
                                                                                                                            str4 = str44;
                                                                                                                            str13 = str22;
                                                                                                                            str15 = str26;
                                                                                                                            arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                                                            str11 = str21;
                                                                                                                            i2 = i26;
                                                                                                                            str7 = str28;
                                                                                                                            arrayWheelAdapter = arrayWheelAdapter5;
                                                                                                                            str9 = str32;
                                                                                                                            str14 = str46;
                                                                                                                            String str48222222 = str30;
                                                                                                                            str12 = str47;
                                                                                                                            str2 = str42;
                                                                                                                            barEntry = barEntry2;
                                                                                                                            str3 = str48222222;
                                                                                                                            List<String> list8222222 = list6;
                                                                                                                            str16 = str23;
                                                                                                                            str5 = str24;
                                                                                                                            list2 = list8222222;
                                                                                                                        }
                                                                                                                    } else {
                                                                                                                        i24 = i23;
                                                                                                                        i25 = i11;
                                                                                                                    }
                                                                                                                    i26 = 0;
                                                                                                                    if ((j & 1346371584) != 0) {
                                                                                                                    }
                                                                                                                    str41 = null;
                                                                                                                    if ((j & 1350565888) != 0) {
                                                                                                                    }
                                                                                                                    barEntry2 = null;
                                                                                                                    if ((j & 1358954496) != 0) {
                                                                                                                    }
                                                                                                                    str43 = null;
                                                                                                                    if ((j & 1375731712) == 0) {
                                                                                                                    }
                                                                                                                    str45 = null;
                                                                                                                    if ((j & 1409286144) != 0) {
                                                                                                                    }
                                                                                                                    str47 = null;
                                                                                                                    if ((j & 1476395008) != 0) {
                                                                                                                    }
                                                                                                                    i3 = i13;
                                                                                                                    i4 = i12;
                                                                                                                    str18 = str20;
                                                                                                                    i8 = i16;
                                                                                                                    str = null;
                                                                                                                    i9 = i17;
                                                                                                                    i6 = i18;
                                                                                                                    i10 = i20;
                                                                                                                    arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                                                    list = list5;
                                                                                                                    list3 = list7;
                                                                                                                    i5 = i22;
                                                                                                                    str6 = str34;
                                                                                                                    str10 = str36;
                                                                                                                    str17 = str38;
                                                                                                                    str8 = str40;
                                                                                                                    i7 = i24;
                                                                                                                    i = i25;
                                                                                                                    str4 = str44;
                                                                                                                    str13 = str22;
                                                                                                                    str15 = str26;
                                                                                                                    arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                                                    str11 = str21;
                                                                                                                    i2 = i26;
                                                                                                                    str7 = str28;
                                                                                                                    arrayWheelAdapter = arrayWheelAdapter5;
                                                                                                                    str9 = str32;
                                                                                                                    str14 = str46;
                                                                                                                    String str482222222 = str30;
                                                                                                                    str12 = str47;
                                                                                                                    str2 = str42;
                                                                                                                    barEntry = barEntry2;
                                                                                                                    str3 = str482222222;
                                                                                                                    List<String> list82222222 = list6;
                                                                                                                    str16 = str23;
                                                                                                                    str5 = str24;
                                                                                                                    list2 = list82222222;
                                                                                                                }
                                                                                                            } else {
                                                                                                                str38 = str37;
                                                                                                            }
                                                                                                            str39 = null;
                                                                                                            if ((j & 1342701568) == 0) {
                                                                                                            }
                                                                                                            j4 = j & 1343225856;
                                                                                                            if (j4 == 0) {
                                                                                                            }
                                                                                                            i26 = 0;
                                                                                                            if ((j & 1346371584) != 0) {
                                                                                                            }
                                                                                                            str41 = null;
                                                                                                            if ((j & 1350565888) != 0) {
                                                                                                            }
                                                                                                            barEntry2 = null;
                                                                                                            if ((j & 1358954496) != 0) {
                                                                                                            }
                                                                                                            str43 = null;
                                                                                                            if ((j & 1375731712) == 0) {
                                                                                                            }
                                                                                                            str45 = null;
                                                                                                            if ((j & 1409286144) != 0) {
                                                                                                            }
                                                                                                            str47 = null;
                                                                                                            if ((j & 1476395008) != 0) {
                                                                                                            }
                                                                                                            i3 = i13;
                                                                                                            i4 = i12;
                                                                                                            str18 = str20;
                                                                                                            i8 = i16;
                                                                                                            str = null;
                                                                                                            i9 = i17;
                                                                                                            i6 = i18;
                                                                                                            i10 = i20;
                                                                                                            arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                                            list = list5;
                                                                                                            list3 = list7;
                                                                                                            i5 = i22;
                                                                                                            str6 = str34;
                                                                                                            str10 = str36;
                                                                                                            str17 = str38;
                                                                                                            str8 = str40;
                                                                                                            i7 = i24;
                                                                                                            i = i25;
                                                                                                            str4 = str44;
                                                                                                            str13 = str22;
                                                                                                            str15 = str26;
                                                                                                            arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                                            str11 = str21;
                                                                                                            i2 = i26;
                                                                                                            str7 = str28;
                                                                                                            arrayWheelAdapter = arrayWheelAdapter5;
                                                                                                            str9 = str32;
                                                                                                            str14 = str46;
                                                                                                            String str4822222222 = str30;
                                                                                                            str12 = str47;
                                                                                                            str2 = str42;
                                                                                                            barEntry = barEntry2;
                                                                                                            str3 = str4822222222;
                                                                                                            List<String> list822222222 = list6;
                                                                                                            str16 = str23;
                                                                                                            str5 = str24;
                                                                                                            list2 = list822222222;
                                                                                                        }
                                                                                                    } else {
                                                                                                        str36 = str35;
                                                                                                    }
                                                                                                    str37 = null;
                                                                                                    if ((j & 1342439424) != 0) {
                                                                                                    }
                                                                                                    str39 = null;
                                                                                                    if ((j & 1342701568) == 0) {
                                                                                                    }
                                                                                                    j4 = j & 1343225856;
                                                                                                    if (j4 == 0) {
                                                                                                    }
                                                                                                    i26 = 0;
                                                                                                    if ((j & 1346371584) != 0) {
                                                                                                    }
                                                                                                    str41 = null;
                                                                                                    if ((j & 1350565888) != 0) {
                                                                                                    }
                                                                                                    barEntry2 = null;
                                                                                                    if ((j & 1358954496) != 0) {
                                                                                                    }
                                                                                                    str43 = null;
                                                                                                    if ((j & 1375731712) == 0) {
                                                                                                    }
                                                                                                    str45 = null;
                                                                                                    if ((j & 1409286144) != 0) {
                                                                                                    }
                                                                                                    str47 = null;
                                                                                                    if ((j & 1476395008) != 0) {
                                                                                                    }
                                                                                                    i3 = i13;
                                                                                                    i4 = i12;
                                                                                                    str18 = str20;
                                                                                                    i8 = i16;
                                                                                                    str = null;
                                                                                                    i9 = i17;
                                                                                                    i6 = i18;
                                                                                                    i10 = i20;
                                                                                                    arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                                    list = list5;
                                                                                                    list3 = list7;
                                                                                                    i5 = i22;
                                                                                                    str6 = str34;
                                                                                                    str10 = str36;
                                                                                                    str17 = str38;
                                                                                                    str8 = str40;
                                                                                                    i7 = i24;
                                                                                                    i = i25;
                                                                                                    str4 = str44;
                                                                                                    str13 = str22;
                                                                                                    str15 = str26;
                                                                                                    arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                                    str11 = str21;
                                                                                                    i2 = i26;
                                                                                                    str7 = str28;
                                                                                                    arrayWheelAdapter = arrayWheelAdapter5;
                                                                                                    str9 = str32;
                                                                                                    str14 = str46;
                                                                                                    String str48222222222 = str30;
                                                                                                    str12 = str47;
                                                                                                    str2 = str42;
                                                                                                    barEntry = barEntry2;
                                                                                                    str3 = str48222222222;
                                                                                                    List<String> list8222222222 = list6;
                                                                                                    str16 = str23;
                                                                                                    str5 = str24;
                                                                                                    list2 = list8222222222;
                                                                                                }
                                                                                            } else {
                                                                                                str34 = str33;
                                                                                            }
                                                                                            str35 = null;
                                                                                            if ((j & 1342308352) == 0) {
                                                                                            }
                                                                                            str37 = null;
                                                                                            if ((j & 1342439424) != 0) {
                                                                                            }
                                                                                            str39 = null;
                                                                                            if ((j & 1342701568) == 0) {
                                                                                            }
                                                                                            j4 = j & 1343225856;
                                                                                            if (j4 == 0) {
                                                                                            }
                                                                                            i26 = 0;
                                                                                            if ((j & 1346371584) != 0) {
                                                                                            }
                                                                                            str41 = null;
                                                                                            if ((j & 1350565888) != 0) {
                                                                                            }
                                                                                            barEntry2 = null;
                                                                                            if ((j & 1358954496) != 0) {
                                                                                            }
                                                                                            str43 = null;
                                                                                            if ((j & 1375731712) == 0) {
                                                                                            }
                                                                                            str45 = null;
                                                                                            if ((j & 1409286144) != 0) {
                                                                                            }
                                                                                            str47 = null;
                                                                                            if ((j & 1476395008) != 0) {
                                                                                            }
                                                                                            i3 = i13;
                                                                                            i4 = i12;
                                                                                            str18 = str20;
                                                                                            i8 = i16;
                                                                                            str = null;
                                                                                            i9 = i17;
                                                                                            i6 = i18;
                                                                                            i10 = i20;
                                                                                            arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                            list = list5;
                                                                                            list3 = list7;
                                                                                            i5 = i22;
                                                                                            str6 = str34;
                                                                                            str10 = str36;
                                                                                            str17 = str38;
                                                                                            str8 = str40;
                                                                                            i7 = i24;
                                                                                            i = i25;
                                                                                            str4 = str44;
                                                                                            str13 = str22;
                                                                                            str15 = str26;
                                                                                            arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                            str11 = str21;
                                                                                            i2 = i26;
                                                                                            str7 = str28;
                                                                                            arrayWheelAdapter = arrayWheelAdapter5;
                                                                                            str9 = str32;
                                                                                            str14 = str46;
                                                                                            String str482222222222 = str30;
                                                                                            str12 = str47;
                                                                                            str2 = str42;
                                                                                            barEntry = barEntry2;
                                                                                            str3 = str482222222222;
                                                                                            List<String> list82222222222 = list6;
                                                                                            str16 = str23;
                                                                                            str5 = str24;
                                                                                            list2 = list82222222222;
                                                                                        }
                                                                                    } else {
                                                                                        i22 = i21;
                                                                                    }
                                                                                    str33 = null;
                                                                                    if ((j & 1342242816) != 0) {
                                                                                    }
                                                                                    str35 = null;
                                                                                    if ((j & 1342308352) == 0) {
                                                                                    }
                                                                                    str37 = null;
                                                                                    if ((j & 1342439424) != 0) {
                                                                                    }
                                                                                    str39 = null;
                                                                                    if ((j & 1342701568) == 0) {
                                                                                    }
                                                                                    j4 = j & 1343225856;
                                                                                    if (j4 == 0) {
                                                                                    }
                                                                                    i26 = 0;
                                                                                    if ((j & 1346371584) != 0) {
                                                                                    }
                                                                                    str41 = null;
                                                                                    if ((j & 1350565888) != 0) {
                                                                                    }
                                                                                    barEntry2 = null;
                                                                                    if ((j & 1358954496) != 0) {
                                                                                    }
                                                                                    str43 = null;
                                                                                    if ((j & 1375731712) == 0) {
                                                                                    }
                                                                                    str45 = null;
                                                                                    if ((j & 1409286144) != 0) {
                                                                                    }
                                                                                    str47 = null;
                                                                                    if ((j & 1476395008) != 0) {
                                                                                    }
                                                                                    i3 = i13;
                                                                                    i4 = i12;
                                                                                    str18 = str20;
                                                                                    i8 = i16;
                                                                                    str = null;
                                                                                    i9 = i17;
                                                                                    i6 = i18;
                                                                                    i10 = i20;
                                                                                    arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                                    list = list5;
                                                                                    list3 = list7;
                                                                                    i5 = i22;
                                                                                    str6 = str34;
                                                                                    str10 = str36;
                                                                                    str17 = str38;
                                                                                    str8 = str40;
                                                                                    i7 = i24;
                                                                                    i = i25;
                                                                                    str4 = str44;
                                                                                    str13 = str22;
                                                                                    str15 = str26;
                                                                                    arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                                    str11 = str21;
                                                                                    i2 = i26;
                                                                                    str7 = str28;
                                                                                    arrayWheelAdapter = arrayWheelAdapter5;
                                                                                    str9 = str32;
                                                                                    str14 = str46;
                                                                                    String str4822222222222 = str30;
                                                                                    str12 = str47;
                                                                                    str2 = str42;
                                                                                    barEntry = barEntry2;
                                                                                    str3 = str4822222222222;
                                                                                    List<String> list822222222222 = list6;
                                                                                    str16 = str23;
                                                                                    str5 = str24;
                                                                                    list2 = list822222222222;
                                                                                }
                                                                            } else {
                                                                                str30 = str29;
                                                                            }
                                                                            str31 = null;
                                                                            if ((j & 1342177280) != 0) {
                                                                            }
                                                                            list4 = null;
                                                                            arrayWheelAdapter4 = null;
                                                                            arrayWheelAdapter5 = null;
                                                                            arrayWheelAdapter6 = null;
                                                                            list5 = null;
                                                                            list6 = null;
                                                                            if ((j & 1342193664) == 0) {
                                                                            }
                                                                            if ((j & 1342210048) == 0) {
                                                                            }
                                                                            str33 = null;
                                                                            if ((j & 1342242816) != 0) {
                                                                            }
                                                                            str35 = null;
                                                                            if ((j & 1342308352) == 0) {
                                                                            }
                                                                            str37 = null;
                                                                            if ((j & 1342439424) != 0) {
                                                                            }
                                                                            str39 = null;
                                                                            if ((j & 1342701568) == 0) {
                                                                            }
                                                                            j4 = j & 1343225856;
                                                                            if (j4 == 0) {
                                                                            }
                                                                            i26 = 0;
                                                                            if ((j & 1346371584) != 0) {
                                                                            }
                                                                            str41 = null;
                                                                            if ((j & 1350565888) != 0) {
                                                                            }
                                                                            barEntry2 = null;
                                                                            if ((j & 1358954496) != 0) {
                                                                            }
                                                                            str43 = null;
                                                                            if ((j & 1375731712) == 0) {
                                                                            }
                                                                            str45 = null;
                                                                            if ((j & 1409286144) != 0) {
                                                                            }
                                                                            str47 = null;
                                                                            if ((j & 1476395008) != 0) {
                                                                            }
                                                                            i3 = i13;
                                                                            i4 = i12;
                                                                            str18 = str20;
                                                                            i8 = i16;
                                                                            str = null;
                                                                            i9 = i17;
                                                                            i6 = i18;
                                                                            i10 = i20;
                                                                            arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                            list = list5;
                                                                            list3 = list7;
                                                                            i5 = i22;
                                                                            str6 = str34;
                                                                            str10 = str36;
                                                                            str17 = str38;
                                                                            str8 = str40;
                                                                            i7 = i24;
                                                                            i = i25;
                                                                            str4 = str44;
                                                                            str13 = str22;
                                                                            str15 = str26;
                                                                            arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                            str11 = str21;
                                                                            i2 = i26;
                                                                            str7 = str28;
                                                                            arrayWheelAdapter = arrayWheelAdapter5;
                                                                            str9 = str32;
                                                                            str14 = str46;
                                                                            String str48222222222222 = str30;
                                                                            str12 = str47;
                                                                            str2 = str42;
                                                                            barEntry = barEntry2;
                                                                            str3 = str48222222222222;
                                                                            List<String> list8222222222222 = list6;
                                                                            str16 = str23;
                                                                            str5 = str24;
                                                                            list2 = list8222222222222;
                                                                        }
                                                                    } else {
                                                                        i20 = i19;
                                                                    }
                                                                    str29 = null;
                                                                    if ((j & 1342185472) != 0) {
                                                                    }
                                                                    str31 = null;
                                                                    if ((j & 1342177280) != 0) {
                                                                    }
                                                                    list4 = null;
                                                                    arrayWheelAdapter4 = null;
                                                                    arrayWheelAdapter5 = null;
                                                                    arrayWheelAdapter6 = null;
                                                                    list5 = null;
                                                                    list6 = null;
                                                                    if ((j & 1342193664) == 0) {
                                                                    }
                                                                    if ((j & 1342210048) == 0) {
                                                                    }
                                                                    str33 = null;
                                                                    if ((j & 1342242816) != 0) {
                                                                    }
                                                                    str35 = null;
                                                                    if ((j & 1342308352) == 0) {
                                                                    }
                                                                    str37 = null;
                                                                    if ((j & 1342439424) != 0) {
                                                                    }
                                                                    str39 = null;
                                                                    if ((j & 1342701568) == 0) {
                                                                    }
                                                                    j4 = j & 1343225856;
                                                                    if (j4 == 0) {
                                                                    }
                                                                    i26 = 0;
                                                                    if ((j & 1346371584) != 0) {
                                                                    }
                                                                    str41 = null;
                                                                    if ((j & 1350565888) != 0) {
                                                                    }
                                                                    barEntry2 = null;
                                                                    if ((j & 1358954496) != 0) {
                                                                    }
                                                                    str43 = null;
                                                                    if ((j & 1375731712) == 0) {
                                                                    }
                                                                    str45 = null;
                                                                    if ((j & 1409286144) != 0) {
                                                                    }
                                                                    str47 = null;
                                                                    if ((j & 1476395008) != 0) {
                                                                    }
                                                                    i3 = i13;
                                                                    i4 = i12;
                                                                    str18 = str20;
                                                                    i8 = i16;
                                                                    str = null;
                                                                    i9 = i17;
                                                                    i6 = i18;
                                                                    i10 = i20;
                                                                    arrayWheelAdapter3 = arrayWheelAdapter4;
                                                                    list = list5;
                                                                    list3 = list7;
                                                                    i5 = i22;
                                                                    str6 = str34;
                                                                    str10 = str36;
                                                                    str17 = str38;
                                                                    str8 = str40;
                                                                    i7 = i24;
                                                                    i = i25;
                                                                    str4 = str44;
                                                                    str13 = str22;
                                                                    str15 = str26;
                                                                    arrayWheelAdapter2 = arrayWheelAdapter6;
                                                                    str11 = str21;
                                                                    i2 = i26;
                                                                    str7 = str28;
                                                                    arrayWheelAdapter = arrayWheelAdapter5;
                                                                    str9 = str32;
                                                                    str14 = str46;
                                                                    String str482222222222222 = str30;
                                                                    str12 = str47;
                                                                    str2 = str42;
                                                                    barEntry = barEntry2;
                                                                    str3 = str482222222222222;
                                                                    List<String> list82222222222222 = list6;
                                                                    str16 = str23;
                                                                    str5 = str24;
                                                                    list2 = list82222222222222;
                                                                }
                                                            } else {
                                                                i17 = i15;
                                                                i18 = i14;
                                                            }
                                                            str27 = null;
                                                            if ((j & 1342179328) == 0) {
                                                            }
                                                            if ((j & 1342181376) == 0) {
                                                            }
                                                            str29 = null;
                                                            if ((j & 1342185472) != 0) {
                                                            }
                                                            str31 = null;
                                                            if ((j & 1342177280) != 0) {
                                                            }
                                                            list4 = null;
                                                            arrayWheelAdapter4 = null;
                                                            arrayWheelAdapter5 = null;
                                                            arrayWheelAdapter6 = null;
                                                            list5 = null;
                                                            list6 = null;
                                                            if ((j & 1342193664) == 0) {
                                                            }
                                                            if ((j & 1342210048) == 0) {
                                                            }
                                                            str33 = null;
                                                            if ((j & 1342242816) != 0) {
                                                            }
                                                            str35 = null;
                                                            if ((j & 1342308352) == 0) {
                                                            }
                                                            str37 = null;
                                                            if ((j & 1342439424) != 0) {
                                                            }
                                                            str39 = null;
                                                            if ((j & 1342701568) == 0) {
                                                            }
                                                            j4 = j & 1343225856;
                                                            if (j4 == 0) {
                                                            }
                                                            i26 = 0;
                                                            if ((j & 1346371584) != 0) {
                                                            }
                                                            str41 = null;
                                                            if ((j & 1350565888) != 0) {
                                                            }
                                                            barEntry2 = null;
                                                            if ((j & 1358954496) != 0) {
                                                            }
                                                            str43 = null;
                                                            if ((j & 1375731712) == 0) {
                                                            }
                                                            str45 = null;
                                                            if ((j & 1409286144) != 0) {
                                                            }
                                                            str47 = null;
                                                            if ((j & 1476395008) != 0) {
                                                            }
                                                            i3 = i13;
                                                            i4 = i12;
                                                            str18 = str20;
                                                            i8 = i16;
                                                            str = null;
                                                            i9 = i17;
                                                            i6 = i18;
                                                            i10 = i20;
                                                            arrayWheelAdapter3 = arrayWheelAdapter4;
                                                            list = list5;
                                                            list3 = list7;
                                                            i5 = i22;
                                                            str6 = str34;
                                                            str10 = str36;
                                                            str17 = str38;
                                                            str8 = str40;
                                                            i7 = i24;
                                                            i = i25;
                                                            str4 = str44;
                                                            str13 = str22;
                                                            str15 = str26;
                                                            arrayWheelAdapter2 = arrayWheelAdapter6;
                                                            str11 = str21;
                                                            i2 = i26;
                                                            str7 = str28;
                                                            arrayWheelAdapter = arrayWheelAdapter5;
                                                            str9 = str32;
                                                            str14 = str46;
                                                            String str4822222222222222 = str30;
                                                            str12 = str47;
                                                            str2 = str42;
                                                            barEntry = barEntry2;
                                                            str3 = str4822222222222222;
                                                            List<String> list822222222222222 = list6;
                                                            str16 = str23;
                                                            str5 = str24;
                                                            list2 = list822222222222222;
                                                        }
                                                    } else {
                                                        str26 = str25;
                                                        i15 = 0;
                                                    }
                                                    i16 = 0;
                                                    if ((j & 1342178304) != 0) {
                                                    }
                                                    str27 = null;
                                                    if ((j & 1342179328) == 0) {
                                                    }
                                                    if ((j & 1342181376) == 0) {
                                                    }
                                                    str29 = null;
                                                    if ((j & 1342185472) != 0) {
                                                    }
                                                    str31 = null;
                                                    if ((j & 1342177280) != 0) {
                                                    }
                                                    list4 = null;
                                                    arrayWheelAdapter4 = null;
                                                    arrayWheelAdapter5 = null;
                                                    arrayWheelAdapter6 = null;
                                                    list5 = null;
                                                    list6 = null;
                                                    if ((j & 1342193664) == 0) {
                                                    }
                                                    if ((j & 1342210048) == 0) {
                                                    }
                                                    str33 = null;
                                                    if ((j & 1342242816) != 0) {
                                                    }
                                                    str35 = null;
                                                    if ((j & 1342308352) == 0) {
                                                    }
                                                    str37 = null;
                                                    if ((j & 1342439424) != 0) {
                                                    }
                                                    str39 = null;
                                                    if ((j & 1342701568) == 0) {
                                                    }
                                                    j4 = j & 1343225856;
                                                    if (j4 == 0) {
                                                    }
                                                    i26 = 0;
                                                    if ((j & 1346371584) != 0) {
                                                    }
                                                    str41 = null;
                                                    if ((j & 1350565888) != 0) {
                                                    }
                                                    barEntry2 = null;
                                                    if ((j & 1358954496) != 0) {
                                                    }
                                                    str43 = null;
                                                    if ((j & 1375731712) == 0) {
                                                    }
                                                    str45 = null;
                                                    if ((j & 1409286144) != 0) {
                                                    }
                                                    str47 = null;
                                                    if ((j & 1476395008) != 0) {
                                                    }
                                                    i3 = i13;
                                                    i4 = i12;
                                                    str18 = str20;
                                                    i8 = i16;
                                                    str = null;
                                                    i9 = i17;
                                                    i6 = i18;
                                                    i10 = i20;
                                                    arrayWheelAdapter3 = arrayWheelAdapter4;
                                                    list = list5;
                                                    list3 = list7;
                                                    i5 = i22;
                                                    str6 = str34;
                                                    str10 = str36;
                                                    str17 = str38;
                                                    str8 = str40;
                                                    i7 = i24;
                                                    i = i25;
                                                    str4 = str44;
                                                    str13 = str22;
                                                    str15 = str26;
                                                    arrayWheelAdapter2 = arrayWheelAdapter6;
                                                    str11 = str21;
                                                    i2 = i26;
                                                    str7 = str28;
                                                    arrayWheelAdapter = arrayWheelAdapter5;
                                                    str9 = str32;
                                                    str14 = str46;
                                                    String str48222222222222222 = str30;
                                                    str12 = str47;
                                                    str2 = str42;
                                                    barEntry = barEntry2;
                                                    str3 = str48222222222222222;
                                                    List<String> list8222222222222222 = list6;
                                                    str16 = str23;
                                                    str5 = str24;
                                                    list2 = list8222222222222222;
                                                }
                                            }
                                            str25 = null;
                                            if ((j & 1342177536) == 0) {
                                            }
                                            j3 = j & 1342177792;
                                            if (j3 == 0) {
                                            }
                                            i16 = 0;
                                            if ((j & 1342178304) != 0) {
                                            }
                                            str27 = null;
                                            if ((j & 1342179328) == 0) {
                                            }
                                            if ((j & 1342181376) == 0) {
                                            }
                                            str29 = null;
                                            if ((j & 1342185472) != 0) {
                                            }
                                            str31 = null;
                                            if ((j & 1342177280) != 0) {
                                            }
                                            list4 = null;
                                            arrayWheelAdapter4 = null;
                                            arrayWheelAdapter5 = null;
                                            arrayWheelAdapter6 = null;
                                            list5 = null;
                                            list6 = null;
                                            if ((j & 1342193664) == 0) {
                                            }
                                            if ((j & 1342210048) == 0) {
                                            }
                                            str33 = null;
                                            if ((j & 1342242816) != 0) {
                                            }
                                            str35 = null;
                                            if ((j & 1342308352) == 0) {
                                            }
                                            str37 = null;
                                            if ((j & 1342439424) != 0) {
                                            }
                                            str39 = null;
                                            if ((j & 1342701568) == 0) {
                                            }
                                            j4 = j & 1343225856;
                                            if (j4 == 0) {
                                            }
                                            i26 = 0;
                                            if ((j & 1346371584) != 0) {
                                            }
                                            str41 = null;
                                            if ((j & 1350565888) != 0) {
                                            }
                                            barEntry2 = null;
                                            if ((j & 1358954496) != 0) {
                                            }
                                            str43 = null;
                                            if ((j & 1375731712) == 0) {
                                            }
                                            str45 = null;
                                            if ((j & 1409286144) != 0) {
                                            }
                                            str47 = null;
                                            if ((j & 1476395008) != 0) {
                                            }
                                            i3 = i13;
                                            i4 = i12;
                                            str18 = str20;
                                            i8 = i16;
                                            str = null;
                                            i9 = i17;
                                            i6 = i18;
                                            i10 = i20;
                                            arrayWheelAdapter3 = arrayWheelAdapter4;
                                            list = list5;
                                            list3 = list7;
                                            i5 = i22;
                                            str6 = str34;
                                            str10 = str36;
                                            str17 = str38;
                                            str8 = str40;
                                            i7 = i24;
                                            i = i25;
                                            str4 = str44;
                                            str13 = str22;
                                            str15 = str26;
                                            arrayWheelAdapter2 = arrayWheelAdapter6;
                                            str11 = str21;
                                            i2 = i26;
                                            str7 = str28;
                                            arrayWheelAdapter = arrayWheelAdapter5;
                                            str9 = str32;
                                            str14 = str46;
                                            String str482222222222222222 = str30;
                                            str12 = str47;
                                            str2 = str42;
                                            barEntry = barEntry2;
                                            str3 = str482222222222222222;
                                            List<String> list82222222222222222 = list6;
                                            str16 = str23;
                                            str5 = str24;
                                            list2 = list82222222222222222;
                                        }
                                    }
                                    str24 = null;
                                    if ((j & 1342177408) != 0) {
                                    }
                                    str25 = null;
                                    if ((j & 1342177536) == 0) {
                                    }
                                    j3 = j & 1342177792;
                                    if (j3 == 0) {
                                    }
                                    i16 = 0;
                                    if ((j & 1342178304) != 0) {
                                    }
                                    str27 = null;
                                    if ((j & 1342179328) == 0) {
                                    }
                                    if ((j & 1342181376) == 0) {
                                    }
                                    str29 = null;
                                    if ((j & 1342185472) != 0) {
                                    }
                                    str31 = null;
                                    if ((j & 1342177280) != 0) {
                                    }
                                    list4 = null;
                                    arrayWheelAdapter4 = null;
                                    arrayWheelAdapter5 = null;
                                    arrayWheelAdapter6 = null;
                                    list5 = null;
                                    list6 = null;
                                    if ((j & 1342193664) == 0) {
                                    }
                                    if ((j & 1342210048) == 0) {
                                    }
                                    str33 = null;
                                    if ((j & 1342242816) != 0) {
                                    }
                                    str35 = null;
                                    if ((j & 1342308352) == 0) {
                                    }
                                    str37 = null;
                                    if ((j & 1342439424) != 0) {
                                    }
                                    str39 = null;
                                    if ((j & 1342701568) == 0) {
                                    }
                                    j4 = j & 1343225856;
                                    if (j4 == 0) {
                                    }
                                    i26 = 0;
                                    if ((j & 1346371584) != 0) {
                                    }
                                    str41 = null;
                                    if ((j & 1350565888) != 0) {
                                    }
                                    barEntry2 = null;
                                    if ((j & 1358954496) != 0) {
                                    }
                                    str43 = null;
                                    if ((j & 1375731712) == 0) {
                                    }
                                    str45 = null;
                                    if ((j & 1409286144) != 0) {
                                    }
                                    str47 = null;
                                    if ((j & 1476395008) != 0) {
                                    }
                                    i3 = i13;
                                    i4 = i12;
                                    str18 = str20;
                                    i8 = i16;
                                    str = null;
                                    i9 = i17;
                                    i6 = i18;
                                    i10 = i20;
                                    arrayWheelAdapter3 = arrayWheelAdapter4;
                                    list = list5;
                                    list3 = list7;
                                    i5 = i22;
                                    str6 = str34;
                                    str10 = str36;
                                    str17 = str38;
                                    str8 = str40;
                                    i7 = i24;
                                    i = i25;
                                    str4 = str44;
                                    str13 = str22;
                                    str15 = str26;
                                    arrayWheelAdapter2 = arrayWheelAdapter6;
                                    str11 = str21;
                                    i2 = i26;
                                    str7 = str28;
                                    arrayWheelAdapter = arrayWheelAdapter5;
                                    str9 = str32;
                                    str14 = str46;
                                    String str4822222222222222222 = str30;
                                    str12 = str47;
                                    str2 = str42;
                                    barEntry = barEntry2;
                                    str3 = str4822222222222222222;
                                    List<String> list822222222222222222 = list6;
                                    str16 = str23;
                                    str5 = str24;
                                    list2 = list822222222222222222;
                                }
                            }
                            str23 = null;
                            if ((j & 1342177344) != 0) {
                            }
                            str24 = null;
                            if ((j & 1342177408) != 0) {
                            }
                            str25 = null;
                            if ((j & 1342177536) == 0) {
                            }
                            j3 = j & 1342177792;
                            if (j3 == 0) {
                            }
                            i16 = 0;
                            if ((j & 1342178304) != 0) {
                            }
                            str27 = null;
                            if ((j & 1342179328) == 0) {
                            }
                            if ((j & 1342181376) == 0) {
                            }
                            str29 = null;
                            if ((j & 1342185472) != 0) {
                            }
                            str31 = null;
                            if ((j & 1342177280) != 0) {
                            }
                            list4 = null;
                            arrayWheelAdapter4 = null;
                            arrayWheelAdapter5 = null;
                            arrayWheelAdapter6 = null;
                            list5 = null;
                            list6 = null;
                            if ((j & 1342193664) == 0) {
                            }
                            if ((j & 1342210048) == 0) {
                            }
                            str33 = null;
                            if ((j & 1342242816) != 0) {
                            }
                            str35 = null;
                            if ((j & 1342308352) == 0) {
                            }
                            str37 = null;
                            if ((j & 1342439424) != 0) {
                            }
                            str39 = null;
                            if ((j & 1342701568) == 0) {
                            }
                            j4 = j & 1343225856;
                            if (j4 == 0) {
                            }
                            i26 = 0;
                            if ((j & 1346371584) != 0) {
                            }
                            str41 = null;
                            if ((j & 1350565888) != 0) {
                            }
                            barEntry2 = null;
                            if ((j & 1358954496) != 0) {
                            }
                            str43 = null;
                            if ((j & 1375731712) == 0) {
                            }
                            str45 = null;
                            if ((j & 1409286144) != 0) {
                            }
                            str47 = null;
                            if ((j & 1476395008) != 0) {
                            }
                            i3 = i13;
                            i4 = i12;
                            str18 = str20;
                            i8 = i16;
                            str = null;
                            i9 = i17;
                            i6 = i18;
                            i10 = i20;
                            arrayWheelAdapter3 = arrayWheelAdapter4;
                            list = list5;
                            list3 = list7;
                            i5 = i22;
                            str6 = str34;
                            str10 = str36;
                            str17 = str38;
                            str8 = str40;
                            i7 = i24;
                            i = i25;
                            str4 = str44;
                            str13 = str22;
                            str15 = str26;
                            arrayWheelAdapter2 = arrayWheelAdapter6;
                            str11 = str21;
                            i2 = i26;
                            str7 = str28;
                            arrayWheelAdapter = arrayWheelAdapter5;
                            str9 = str32;
                            str14 = str46;
                            String str48222222222222222222 = str30;
                            str12 = str47;
                            str2 = str42;
                            barEntry = barEntry2;
                            str3 = str48222222222222222222;
                            List<String> list8222222222222222222 = list6;
                            str16 = str23;
                            str5 = str24;
                            list2 = list8222222222222222222;
                        }
                    }
                    str22 = null;
                    if ((j & 1342177312) != 0) {
                    }
                    str23 = null;
                    if ((j & 1342177344) != 0) {
                    }
                    str24 = null;
                    if ((j & 1342177408) != 0) {
                    }
                    str25 = null;
                    if ((j & 1342177536) == 0) {
                    }
                    j3 = j & 1342177792;
                    if (j3 == 0) {
                    }
                    i16 = 0;
                    if ((j & 1342178304) != 0) {
                    }
                    str27 = null;
                    if ((j & 1342179328) == 0) {
                    }
                    if ((j & 1342181376) == 0) {
                    }
                    str29 = null;
                    if ((j & 1342185472) != 0) {
                    }
                    str31 = null;
                    if ((j & 1342177280) != 0) {
                    }
                    list4 = null;
                    arrayWheelAdapter4 = null;
                    arrayWheelAdapter5 = null;
                    arrayWheelAdapter6 = null;
                    list5 = null;
                    list6 = null;
                    if ((j & 1342193664) == 0) {
                    }
                    if ((j & 1342210048) == 0) {
                    }
                    str33 = null;
                    if ((j & 1342242816) != 0) {
                    }
                    str35 = null;
                    if ((j & 1342308352) == 0) {
                    }
                    str37 = null;
                    if ((j & 1342439424) != 0) {
                    }
                    str39 = null;
                    if ((j & 1342701568) == 0) {
                    }
                    j4 = j & 1343225856;
                    if (j4 == 0) {
                    }
                    i26 = 0;
                    if ((j & 1346371584) != 0) {
                    }
                    str41 = null;
                    if ((j & 1350565888) != 0) {
                    }
                    barEntry2 = null;
                    if ((j & 1358954496) != 0) {
                    }
                    str43 = null;
                    if ((j & 1375731712) == 0) {
                    }
                    str45 = null;
                    if ((j & 1409286144) != 0) {
                    }
                    str47 = null;
                    if ((j & 1476395008) != 0) {
                    }
                    i3 = i13;
                    i4 = i12;
                    str18 = str20;
                    i8 = i16;
                    str = null;
                    i9 = i17;
                    i6 = i18;
                    i10 = i20;
                    arrayWheelAdapter3 = arrayWheelAdapter4;
                    list = list5;
                    list3 = list7;
                    i5 = i22;
                    str6 = str34;
                    str10 = str36;
                    str17 = str38;
                    str8 = str40;
                    i7 = i24;
                    i = i25;
                    str4 = str44;
                    str13 = str22;
                    str15 = str26;
                    arrayWheelAdapter2 = arrayWheelAdapter6;
                    str11 = str21;
                    i2 = i26;
                    str7 = str28;
                    arrayWheelAdapter = arrayWheelAdapter5;
                    str9 = str32;
                    str14 = str46;
                    String str482222222222222222222 = str30;
                    str12 = str47;
                    str2 = str42;
                    barEntry = barEntry2;
                    str3 = str482222222222222222222;
                    List<String> list82222222222222222222 = list6;
                    str16 = str23;
                    str5 = str24;
                    list2 = list82222222222222222222;
                }
            }
            str20 = null;
            j2 = j & 1342177288;
            if (j2 == 0) {
            }
            if ((j & 1342177296) != 0) {
            }
            str22 = null;
            if ((j & 1342177312) != 0) {
            }
            str23 = null;
            if ((j & 1342177344) != 0) {
            }
            str24 = null;
            if ((j & 1342177408) != 0) {
            }
            str25 = null;
            if ((j & 1342177536) == 0) {
            }
            j3 = j & 1342177792;
            if (j3 == 0) {
            }
            i16 = 0;
            if ((j & 1342178304) != 0) {
            }
            str27 = null;
            if ((j & 1342179328) == 0) {
            }
            if ((j & 1342181376) == 0) {
            }
            str29 = null;
            if ((j & 1342185472) != 0) {
            }
            str31 = null;
            if ((j & 1342177280) != 0) {
            }
            list4 = null;
            arrayWheelAdapter4 = null;
            arrayWheelAdapter5 = null;
            arrayWheelAdapter6 = null;
            list5 = null;
            list6 = null;
            if ((j & 1342193664) == 0) {
            }
            if ((j & 1342210048) == 0) {
            }
            str33 = null;
            if ((j & 1342242816) != 0) {
            }
            str35 = null;
            if ((j & 1342308352) == 0) {
            }
            str37 = null;
            if ((j & 1342439424) != 0) {
            }
            str39 = null;
            if ((j & 1342701568) == 0) {
            }
            j4 = j & 1343225856;
            if (j4 == 0) {
            }
            i26 = 0;
            if ((j & 1346371584) != 0) {
            }
            str41 = null;
            if ((j & 1350565888) != 0) {
            }
            barEntry2 = null;
            if ((j & 1358954496) != 0) {
            }
            str43 = null;
            if ((j & 1375731712) == 0) {
            }
            str45 = null;
            if ((j & 1409286144) != 0) {
            }
            str47 = null;
            if ((j & 1476395008) != 0) {
            }
            i3 = i13;
            i4 = i12;
            str18 = str20;
            i8 = i16;
            str = null;
            i9 = i17;
            i6 = i18;
            i10 = i20;
            arrayWheelAdapter3 = arrayWheelAdapter4;
            list = list5;
            list3 = list7;
            i5 = i22;
            str6 = str34;
            str10 = str36;
            str17 = str38;
            str8 = str40;
            i7 = i24;
            i = i25;
            str4 = str44;
            str13 = str22;
            str15 = str26;
            arrayWheelAdapter2 = arrayWheelAdapter6;
            str11 = str21;
            i2 = i26;
            str7 = str28;
            arrayWheelAdapter = arrayWheelAdapter5;
            str9 = str32;
            str14 = str46;
            String str4822222222222222222222 = str30;
            str12 = str47;
            str2 = str42;
            barEntry = barEntry2;
            str3 = str4822222222222222222222;
            List<String> list822222222222222222222 = list6;
            str16 = str23;
            str5 = str24;
            list2 = list822222222222222222222;
        } else {
            str = null;
            str2 = null;
            str3 = null;
            str4 = null;
            list = null;
            arrayWheelAdapter = null;
            str5 = null;
            arrayWheelAdapter2 = null;
            arrayWheelAdapter3 = null;
            list2 = null;
            list3 = null;
            str6 = null;
            str7 = null;
            str8 = null;
            barEntry = null;
            str9 = null;
            str10 = null;
            str11 = null;
            str12 = null;
            str13 = null;
            str14 = null;
            str15 = null;
            str16 = null;
            str17 = null;
            str18 = null;
            i = 0;
            i2 = 0;
            i3 = 0;
            i4 = 0;
            i5 = 0;
            i6 = 0;
            i7 = 0;
            i8 = 0;
            i9 = 0;
            i10 = 0;
        }
        long j7 = j & 1612709888;
        if (j7 != 0) {
            FlickListener.Listener fickListener = ((j & 1610612736) == 0 || mainFragment == null) ? null : mainFragment.getFickListener();
            if (mainFragment != null) {
                fragmentMainBinding = mainFragment.getViewBinding();
                str19 = str4;
            } else {
                str19 = str4;
                fragmentMainBinding = null;
            }
            updateRegistration(21, fragmentMainBinding);
            combinedChart = fragmentMainBinding != null ? fragmentMainBinding.rriChartView : null;
            listener = fickListener;
        } else {
            str19 = str4;
            combinedChart = null;
            listener = null;
        }
        if ((j & 1342177280) != 0) {
            listener2 = listener;
            this.bottomPicker.setAdapter(arrayWheelAdapter3);
            this.mvHolder.setTags(list2);
            this.rriHolder.setTags(list);
            this.secHolder.setTags(list3);
            this.sendIntervalPicker.setAdapter(arrayWheelAdapter2);
            this.topPicker.setAdapter(arrayWheelAdapter);
        } else {
            listener2 = listener;
        }
        if ((1073741824 & j) != 0) {
            this.bottomPicker.setSelecting(this.mCallback16);
            this.bottomPicker.setTextColorCenter(getColorFromResource(this.bottomPicker, C0009R.color.white));
            PickerView.setListeners(this.bottomPicker, this.bottomPickercurrentItemAttrChanged);
            this.calendar.setOnClickListener(this.mCallback1);
            this.deviceName.setOnClickListener(this.mCallback2);
            this.mboundView14.setOnClickListener(this.mCallback5);
            this.mboundView20.setOnClickListener(this.mCallback6);
            this.mboundView6.setOnClickListener(this.mCallback3);
            HolderView.setListeners(this.mvHolder, this.mvHolderselectedAttrChanged);
            this.mvHolder.setSelectedChanged(this.mCallback10);
            HolderView.setListeners(this.rriHolder, this.rriHolderselectedAttrChanged);
            this.rriHolder.setSelectedChanged(this.mCallback9);
            this.rriLimitArea.setOnClickListener(this.mCallback4);
            this.rriMarkView.setOnSelected(this.mCallback7);
            this.rriMarkView.setOnSelecting(this.mCallback8);
            MarkView.setListeners(this.rriMarkView, this.rriMarkViewbarEntryAttrChanged);
            HolderView.setListeners(this.secHolder, this.secHolderselectedAttrChanged);
            this.secHolder.setSelectedChanged(this.mCallback11);
            this.sendIntervalCancel.setOnClickListener(this.mCallback13);
            this.sendIntervalOk.setOnClickListener(this.mCallback14);
            this.sendIntervalPicker.setSelecting(this.mCallback12);
            this.sendIntervalPicker.setTextColorCenter(getColorFromResource(this.sendIntervalPicker, C0009R.color.white));
            PickerView.setListeners(this.sendIntervalPicker, this.sendIntervalPickercurrentItemAttrChanged);
            this.topBottomCancel.setOnClickListener(this.mCallback17);
            this.topBottomOk.setOnClickListener(this.mCallback18);
            this.topPicker.setSelecting(this.mCallback15);
            this.topPicker.setTextColorCenter(getColorFromResource(this.topPicker, C0009R.color.white));
            PickerView.setListeners(this.topPicker, this.topPickercurrentItemAttrChanged);
        }
        if ((j & 1342177281) != 0) {
            this.bottomPicker.setCurrentItem(i);
        }
        if ((j & 1342177344) != 0) {
            TextViewBindingAdapter.setText(this.deviceName, str5);
        }
        if ((j & 1346371584) != 0) {
            TextViewBindingAdapter.setText(this.mboundView10, str2);
        }
        if ((1342181376 & j) != 0) {
            TextViewBindingAdapter.setText(this.mboundView12, str3);
        }
        if ((1476395008 & j) != 0) {
            TextViewBindingAdapter.setText(this.mboundView13, str);
        }
        if ((1358954496 & j) != 0) {
            TextViewBindingAdapter.setText(this.mboundView16, str19);
        }
        if ((1342177312 & j) != 0) {
            TextViewBindingAdapter.setText(this.mboundView17, str16);
        }
        if ((1342177408 & j) != 0) {
            TextViewBindingAdapter.setText(this.mboundView2, str15);
        }
        if ((1343225856 & j) != 0) {
            this.mboundView20.setVisibility(i2);
        }
        if ((1375731712 & j) != 0) {
            TextViewBindingAdapter.setText(this.mboundView23, str14);
        }
        if ((j & 1342177296) != 0) {
            TextViewBindingAdapter.setText(this.mboundView24, str13);
        }
        if ((1409286144 & j) != 0) {
            TextViewBindingAdapter.setText(this.mboundView3, str12);
        }
        if ((j & 1342177288) != 0) {
            TextViewBindingAdapter.setText(this.mboundView6, str11);
            this.mboundView6.setTextColor(i3);
        }
        if ((1342242816 & j) != 0) {
            TextViewBindingAdapter.setText(this.mboundView9, str10);
        }
        if ((j & 1342177282) != 0) {
            this.mvHolder.setSelected(i4);
        }
        if ((j & 1342177284) != 0) {
            TextViewBindingAdapter.setText(this.rriBottomCurr, str18);
        }
        if ((1342185472 & j) != 0) {
            TextViewBindingAdapter.setText(this.rriDay, str9);
        }
        if ((1342193664 & j) != 0) {
            this.rriHolder.setSelected(i5);
        }
        if (j7 != 0) {
            this.rriMarkView.setChartView(combinedChart);
        }
        if ((j & 1610612736) != 0) {
            this.rriMarkView.setOnFlick(listener2);
        }
        if ((1350565888 & j) != 0) {
            this.rriMarkView.setBarEntry(barEntry);
        }
        if ((1342439424 & j) != 0) {
            TextViewBindingAdapter.setText(this.rriText, str8);
        }
        if ((1342178304 & j) != 0) {
            TextViewBindingAdapter.setText(this.rriTime, str7);
        }
        if ((1342210048 & j) != 0) {
            TextViewBindingAdapter.setText(this.rriTopCurr, str6);
        }
        if ((1342177536 & j) != 0) {
            this.secHolder.setSelected(i6);
        }
        if ((1342308352 & j) != 0) {
            TextViewBindingAdapter.setText(this.sendInterval, str17);
        }
        if ((1342701568 & j) != 0) {
            this.sendIntervalPicker.setCurrentItem(i7);
        }
        if ((j & 1342177792) != 0) {
            this.sendIntervalPickerArea.setVisibility(i8);
            this.topBottomPickerArea.setVisibility(i9);
        }
        if ((j & 1342179328) != 0) {
            this.topPicker.setCurrentItem(i10);
        }
    }

    @Override // jp.co.nipro.cocoron.generated.callback.InverseBindingListener.Listener
    public final void _internalCallbackOnChange(int sourceId) {
        switch (sourceId) {
            case 9:
                MainModel mainModel = this.mModel;
                if (mainModel != null) {
                    mainModel.dayHolderChanged();
                    break;
                }
                break;
            case 10:
                MainModel mainModel2 = this.mModel;
                if (mainModel2 != null) {
                    mainModel2.mvHolderChanged();
                    break;
                }
                break;
            case 11:
                MainModel mainModel3 = this.mModel;
                if (mainModel3 != null) {
                    mainModel3.secHolderChanged();
                    break;
                }
                break;
            case 12:
                MainModel mainModel4 = this.mModel;
                if (mainModel4 != null) {
                    mainModel4.pickerSelecting();
                    break;
                }
                break;
            case 15:
                MainModel mainModel5 = this.mModel;
                if (mainModel5 != null) {
                    mainModel5.pickerSelecting();
                    break;
                }
                break;
            case 16:
                MainModel mainModel6 = this.mModel;
                if (mainModel6 != null) {
                    mainModel6.pickerSelecting();
                    break;
                }
                break;
        }
    }

    @Override // jp.co.nipro.cocoron.generated.callback.OnClickListener.Listener
    public final void _internalCallbackOnClick(int sourceId, View callbackArg_0) {
        if (sourceId == 13) {
            MainModel mainModel = this.mModel;
            if (mainModel != null) {
                mainModel.pickerCancel();
                return;
            }
            return;
        }
        if (sourceId == 14) {
            MainFragment mainFragment = this.mFragment;
            if (mainFragment != null) {
                mainFragment.sendIntervalOk();
                return;
            }
            return;
        }
        if (sourceId == 17) {
            MainModel mainModel2 = this.mModel;
            if (mainModel2 != null) {
                mainModel2.pickerCancel();
                return;
            }
            return;
        }
        if (sourceId != 18) {
            switch (sourceId) {
                case 1:
                    MainFragment mainFragment2 = this.mFragment;
                    if (mainFragment2 != null) {
                        mainFragment2.aboutDialog();
                        break;
                    }
                    break;
                case 2:
                    MainFragment mainFragment3 = this.mFragment;
                    if (mainFragment3 != null) {
                        mainFragment3.changeDevice();
                        break;
                    }
                    break;
                case 3:
                    MainFragment mainFragment4 = this.mFragment;
                    if (mainFragment4 != null) {
                        mainFragment4.setEcgMode();
                        break;
                    }
                    break;
                case 4:
                    MainModel mainModel3 = this.mModel;
                    if (mainModel3 != null) {
                        mainModel3.topBottomClick();
                        break;
                    }
                    break;
                case 5:
                    MainModel mainModel4 = this.mModel;
                    if (mainModel4 != null) {
                        mainModel4.sendIntervalClick();
                        break;
                    }
                    break;
                case 6:
                    MainModel mainModel5 = this.mModel;
                    if (mainModel5 != null) {
                        mainModel5.clockClicked();
                        break;
                    }
                    break;
            }
            return;
        }
        MainFragment mainFragment5 = this.mFragment;
        if (mainFragment5 != null) {
            mainFragment5.topBottomOk();
        }
    }

    @Override // jp.co.nipro.cocoron.generated.callback.MarkViewListener.Listener
    public final void _internalCallbackOnChange1(int sourceId, MarkView callbackArg_0, BarEntry callbackArg_1) {
        if (sourceId == 7) {
            MainModel mainModel = this.mModel;
            if (mainModel != null) {
                mainModel.markSelected(callbackArg_1);
                return;
            }
            return;
        }
        if (sourceId != 8) {
            return;
        }
        MainModel mainModel2 = this.mModel;
        if (mainModel2 != null) {
            mainModel2.markSelecting(callbackArg_1);
        }
    }
}
