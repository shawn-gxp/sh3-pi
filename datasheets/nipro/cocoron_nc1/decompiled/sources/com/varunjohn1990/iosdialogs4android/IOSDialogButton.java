package com.varunjohn1990.iosdialogs4android;

import java.io.Serializable;

/* loaded from: classes.dex */
public class IOSDialogButton implements Serializable {
    public static final int TYPE_NEGATIVE = 2;
    public static final int TYPE_POSITIVE = 1;
    private int id;
    private boolean makeBold;
    private String text;
    private int type;

    public IOSDialogButton(int i, String str) {
        this.id = i;
        this.text = str;
    }

    public IOSDialogButton(int i, String str, boolean z, int i2) {
        this.id = i;
        this.text = str;
        this.makeBold = z;
        this.type = i2;
    }

    public int getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public boolean isMakeBold() {
        return this.makeBold;
    }

    public int getType() {
        return this.type;
    }
}
