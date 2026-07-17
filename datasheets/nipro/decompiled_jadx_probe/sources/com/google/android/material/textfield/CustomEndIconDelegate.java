package com.google.android.material.textfield;

import androidx.annotation.NonNull;

/* loaded from: classes.dex */
class CustomEndIconDelegate extends EndIconDelegate {
    CustomEndIconDelegate(@NonNull TextInputLayout textInputLayout) {
        super(textInputLayout);
    }

    @Override // com.google.android.material.textfield.EndIconDelegate
    void initialize() {
        this.textInputLayout.setEndIconOnClickListener(null);
        this.textInputLayout.setEndIconOnLongClickListener(null);
    }
}
