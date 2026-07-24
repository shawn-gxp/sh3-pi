package com.varunjohn1990.iosdialogs4android;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/* loaded from: classes.dex */
public class IOSDialogView extends AppCompatActivity implements View.OnClickListener {
    private static IOSDialog iosDialog;
    private Context context;
    float dp;
    private boolean isAnimationExitDone;
    private LinearLayout layout2Options;
    private View layoutContent;
    private View layoutDialog;
    private LinearLayout layoutMultipleOptions;
    private View layoutNegative;
    private TextView textViewMessage;
    private TextView textViewNegative;
    private TextView textViewPositive;
    private TextView textViewTitle;

    protected static void open(Context context, IOSDialog iOSDialog) {
        iosDialog = iOSDialog;
        context.startActivity(new Intent(context, (Class<?>) IOSDialogView.class).addFlags(67108864).addFlags(268435456));
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        overridePendingTransition(0, 0);
        super.onCreate(bundle);
        setContentView(R.layout.activity_iosdialog);
        this.context = this;
        this.dp = TypedValue.applyDimension(1, 1.0f, getResources().getDisplayMetrics());
        this.layoutDialog = findViewById(R.id.layoutDialog);
        this.layoutContent = findViewById(R.id.layoutContent);
        this.layoutNegative = findViewById(R.id.layoutNegative);
        this.textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        this.textViewMessage = (TextView) findViewById(R.id.textViewMessage);
        this.textViewNegative = (TextView) findViewById(R.id.textViewNegative);
        this.textViewPositive = (TextView) findViewById(R.id.textViewPositive);
        this.layout2Options = (LinearLayout) findViewById(R.id.layout2Options);
        this.layoutMultipleOptions = (LinearLayout) findViewById(R.id.layoutMultipleOptions);
        IOSDialog.iosDialogView = this;
        if (iosDialog.isEnableAnimation()) {
            this.layoutDialog.setScaleX(1.4f);
            this.layoutDialog.setScaleY(1.4f);
            this.layoutContent.setAlpha(0.0f);
            this.layoutContent.animate().alpha(1.0f).setDuration(400L).setInterpolator(new DecelerateInterpolator()).start();
            this.layoutDialog.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300L).setInterpolator(new DecelerateInterpolator()).start();
        }
        if (iosDialog.getTitle() != null) {
            this.textViewTitle.setText(iosDialog.getTitle());
        } else {
            this.textViewTitle.setVisibility(8);
        }
        if (iosDialog.getMessage() != null) {
            this.textViewMessage.setText(iosDialog.getMessage());
        } else {
            this.textViewMessage.setText("");
        }
        if (!iosDialog.isMultiOptions()) {
            this.layout2Options.setVisibility(0);
            this.layoutMultipleOptions.setVisibility(8);
            if (iosDialog.getPositiveButtonText() != null) {
                this.textViewPositive.setText(iosDialog.getPositiveButtonText());
            } else {
                this.textViewPositive.setText("Ok");
            }
            if (iosDialog.getNegativeButtonText() != null) {
                this.textViewNegative.setText(iosDialog.getNegativeButtonText());
            } else {
                this.textViewNegative.setText("");
                this.layoutNegative.setVisibility(8);
            }
            this.textViewPositive.setOnClickListener(this);
            this.textViewNegative.setOnClickListener(this);
            return;
        }
        this.layout2Options.setVisibility(8);
        this.layoutMultipleOptions.setVisibility(0);
        if (iosDialog.getIosDialogButtons() == null || iosDialog.getIosDialogButtons().isEmpty()) {
            return;
        }
        this.layoutMultipleOptions.removeAllViews();
        for (final IOSDialogButton iOSDialogButton : iosDialog.getIosDialogButtons()) {
            LinearLayout linearLayout = new LinearLayout(this.context);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, Math.round(this.dp * 1.0f)));
            linearLayout.setBackgroundColor(ContextCompat.getColor(this.context, R.color.separator_ios_dialog));
            this.layoutMultipleOptions.addView(linearLayout);
            TextView textView = new TextView(this.context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(-1, (int) (this.dp * 48.0f)));
            textView.setBackgroundResource(R.drawable.click_highlight);
            textView.setClickable(true);
            textView.setText(iOSDialogButton.getText());
            textView.setMaxLines(1);
            textView.setGravity(17);
            textView.setTextSize(2, 15.0f);
            textView.setShadowLayer(1.0f, 1.0f, 1.0f, ContextCompat.getColor(this.context, R.color.text_shadow));
            if (iOSDialogButton.getType() == 2) {
                textView.setTextColor(ContextCompat.getColor(this.context, R.color.action_button_negative_ios_dialog));
            } else {
                textView.setTextColor(ContextCompat.getColor(this.context, R.color.action_button_ios_dialog));
            }
            if (iOSDialogButton.isMakeBold()) {
                textView.setTypeface(Typeface.DEFAULT_BOLD);
            }
            textView.setOnClickListener(new View.OnClickListener() { // from class: com.varunjohn1990.iosdialogs4android.IOSDialogView.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    if (IOSDialogView.iosDialog.getIosDialogMultiOptionsListeners() != null) {
                        IOSDialogView.iosDialog.getIosDialogMultiOptionsListeners().onClick(IOSDialogView.iosDialog, iOSDialogButton);
                    } else {
                        IOSDialogView.this.dismiss();
                    }
                }
            });
            this.layoutMultipleOptions.addView(textView);
        }
    }

    public void onOutsideClick(View view) {
        if (iosDialog.isCancelable()) {
            if (iosDialog.getCancelListener() != null) {
                iosDialog.getCancelListener().onClick(iosDialog);
            }
            onBackPressed();
        }
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        if (this.isAnimationExitDone || !iosDialog.isEnableAnimation()) {
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
        }
        this.layoutContent.animate().alpha(0.0f).setDuration(200L).setListener(new Animator.AnimatorListener() { // from class: com.varunjohn1990.iosdialogs4android.IOSDialogView.2
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                IOSDialogView.this.isAnimationExitDone = true;
                IOSDialogView.this.onBackPressed();
            }
        }).setInterpolator(new AccelerateInterpolator()).start();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.textViewPositive) {
            if (iosDialog.getPositiveClickListener() != null) {
                iosDialog.getPositiveClickListener().onClick(iosDialog);
                return;
            } else {
                dismiss();
                return;
            }
        }
        if (id == R.id.textViewNegative) {
            if (iosDialog.getNegativeClickListener() != null) {
                iosDialog.getNegativeClickListener().onClick(iosDialog);
            } else {
                dismiss();
            }
        }
    }

    public void dismiss() {
        onBackPressed();
    }
}
