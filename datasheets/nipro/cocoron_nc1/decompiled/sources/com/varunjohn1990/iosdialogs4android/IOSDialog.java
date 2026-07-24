package com.varunjohn1990.iosdialogs4android;

import android.content.Context;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class IOSDialog {
    static IOSDialogView iosDialogView;
    private Listener cancelListener;
    private boolean cancelable;
    private Context context;
    private boolean enableAnimation;
    private List<IOSDialogButton> iosDialogButtons;
    private IOSDialogMultiOptionsListeners iosDialogMultiOptionsListeners;
    private String message;
    private boolean multiOptions;
    private String negativeButtonText;
    private Listener negativeClickListener;
    private String positiveButtonText;
    private Listener positiveClickListener;
    private String title;
    private View view;

    public interface Listener {
        void onClick(IOSDialog iOSDialog);
    }

    private IOSDialog(Context context) {
        this.enableAnimation = true;
        this.cancelable = true;
        this.multiOptions = false;
        this.iosDialogButtons = new ArrayList(10);
        this.context = context;
    }

    public void show() {
        IOSDialogView.open(this.context, this);
    }

    public void dismiss() {
        IOSDialogView iOSDialogView = iosDialogView;
        if (iOSDialogView != null) {
            iOSDialogView.dismiss();
        }
    }

    public View getView() {
        return this.view;
    }

    public String getTitle() {
        return this.title;
    }

    public String getMessage() {
        return this.message;
    }

    public IOSDialogMultiOptionsListeners getIosDialogMultiOptionsListeners() {
        return this.iosDialogMultiOptionsListeners;
    }

    public String getPositiveButtonText() {
        return this.positiveButtonText;
    }

    public String getNegativeButtonText() {
        return this.negativeButtonText;
    }

    public Listener getPositiveClickListener() {
        return this.positiveClickListener;
    }

    public Listener getNegativeClickListener() {
        return this.negativeClickListener;
    }

    public Listener getCancelListener() {
        return this.cancelListener;
    }

    public boolean isEnableAnimation() {
        return this.enableAnimation;
    }

    public boolean isCancelable() {
        return this.cancelable;
    }

    public boolean isMultiOptions() {
        return this.multiOptions;
    }

    public List<IOSDialogButton> getIosDialogButtons() {
        return this.iosDialogButtons;
    }

    public static class Builder {
        private Context context;
        private IOSDialog iosDialog;

        public Builder(Context context) {
            this.context = context;
            this.iosDialog = new IOSDialog(context);
        }

        public IOSDialog build() {
            return this.iosDialog;
        }

        public Builder title(String str) {
            this.iosDialog.title = str;
            return this;
        }

        public Builder message(String str) {
            this.iosDialog.message = str;
            return this;
        }

        public Builder title(int i) {
            this.iosDialog.title = this.context.getString(i);
            return this;
        }

        public Builder message(int i) {
            this.iosDialog.message = this.context.getString(i);
            return this;
        }

        public Builder positiveButtonText(String str) {
            this.iosDialog.positiveButtonText = str;
            return this;
        }

        public Builder negativeButtonText(String str) {
            this.iosDialog.negativeButtonText = str;
            return this;
        }

        public Builder positiveButtonText(int i) {
            this.iosDialog.positiveButtonText = this.context.getString(i);
            return this;
        }

        public Builder negativeButtonText(int i) {
            this.iosDialog.negativeButtonText = this.context.getString(i);
            return this;
        }

        public Builder positiveClickListener(Listener listener) {
            this.iosDialog.positiveClickListener = listener;
            return this;
        }

        public Builder negativeClickListener(Listener listener) {
            this.iosDialog.negativeClickListener = listener;
            return this;
        }

        public Builder cancelListener(Listener listener) {
            this.iosDialog.cancelListener = listener;
            return this;
        }

        public Builder enableAnimation(boolean z) {
            this.iosDialog.enableAnimation = z;
            return this;
        }

        public Builder cancelable(boolean z) {
            this.iosDialog.cancelable = z;
            return this;
        }

        public Builder multiOptions(boolean z) {
            this.iosDialog.multiOptions = z;
            return this;
        }

        public Builder iosDialogButtonList(List<IOSDialogButton> list) {
            this.iosDialog.iosDialogButtons.addAll(list);
            return this;
        }

        public Builder multiOptionsListeners(IOSDialogMultiOptionsListeners iOSDialogMultiOptionsListeners) {
            this.iosDialog.iosDialogMultiOptionsListeners = iOSDialogMultiOptionsListeners;
            return this;
        }
    }
}
