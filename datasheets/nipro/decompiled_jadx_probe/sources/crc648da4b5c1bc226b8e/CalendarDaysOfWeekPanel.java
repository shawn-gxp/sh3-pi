package crc648da4b5c1bc226b8e;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class CalendarDaysOfWeekPanel extends CalendarSlotsPanel_1 implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("C1.Android.Calendar.CalendarDaysOfWeekPanel, C1.Android.Calendar", CalendarDaysOfWeekPanel.class, "");
    }

    public CalendarDaysOfWeekPanel(Context context) {
        super(context);
        if (CalendarDaysOfWeekPanel.class == CalendarDaysOfWeekPanel.class) {
            TypeManager.Activate("C1.Android.Calendar.CalendarDaysOfWeekPanel, C1.Android.Calendar", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public CalendarDaysOfWeekPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (CalendarDaysOfWeekPanel.class == CalendarDaysOfWeekPanel.class) {
            TypeManager.Activate("C1.Android.Calendar.CalendarDaysOfWeekPanel, C1.Android.Calendar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public CalendarDaysOfWeekPanel(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (CalendarDaysOfWeekPanel.class == CalendarDaysOfWeekPanel.class) {
            TypeManager.Activate("C1.Android.Calendar.CalendarDaysOfWeekPanel, C1.Android.Calendar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // crc648da4b5c1bc226b8e.CalendarSlotsPanel_1, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc648da4b5c1bc226b8e.CalendarSlotsPanel_1, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
