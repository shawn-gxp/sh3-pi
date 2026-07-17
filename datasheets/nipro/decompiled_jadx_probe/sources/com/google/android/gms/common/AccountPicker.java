package com.google.android.gms.common;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RecentlyNonNull;
import com.google.android.gms.common.internal.Preconditions;
import java.util.ArrayList;
import java.util.List;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
/* loaded from: classes.dex */
public final class AccountPicker {

    /* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
    public static class AccountChooserOptions {

        @Nullable
        private Account zza;
        private boolean zzb;

        @Nullable
        private ArrayList<Account> zzc;

        @Nullable
        private ArrayList<String> zzd;
        private boolean zze;

        @Nullable
        private String zzf;

        @Nullable
        private Bundle zzg;
        private boolean zzh;
        private int zzi;

        @Nullable
        private String zzj;
        private boolean zzk;

        @Nullable
        private zza zzl;

        @Nullable
        private String zzm;
        private boolean zzn;
        private boolean zzo;

        /* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
        public static class Builder {

            @Nullable
            private Account zza;

            @Nullable
            private ArrayList<Account> zzb;

            @Nullable
            private ArrayList<String> zzc;
            private boolean zzd = false;

            @Nullable
            private String zze;

            @Nullable
            private Bundle zzf;

            @RecentlyNonNull
            public AccountChooserOptions build() {
                Preconditions.checkArgument(true, "We only support hostedDomain filter for account chip styled account picker");
                Preconditions.checkArgument(true, "Consent is only valid for account chip styled account picker");
                AccountChooserOptions accountChooserOptions = new AccountChooserOptions();
                accountChooserOptions.zzd = this.zzc;
                accountChooserOptions.zzc = this.zzb;
                accountChooserOptions.zze = this.zzd;
                AccountChooserOptions.zzs(accountChooserOptions, null);
                AccountChooserOptions.zzt(accountChooserOptions, null);
                accountChooserOptions.zzg = this.zzf;
                accountChooserOptions.zza = this.zza;
                AccountChooserOptions.zzw(accountChooserOptions, false);
                AccountChooserOptions.zzx(accountChooserOptions, false);
                AccountChooserOptions.zzy(accountChooserOptions, null);
                AccountChooserOptions.zzz(accountChooserOptions, 0);
                accountChooserOptions.zzf = this.zze;
                AccountChooserOptions.zzB(accountChooserOptions, false);
                AccountChooserOptions.zzC(accountChooserOptions, false);
                AccountChooserOptions.zzD(accountChooserOptions, false);
                return accountChooserOptions;
            }

            @RecentlyNonNull
            public Builder setAllowableAccounts(@Nullable List<Account> list) {
                this.zzb = list == null ? null : new ArrayList<>(list);
                return this;
            }

            @RecentlyNonNull
            public Builder setAllowableAccountsTypes(@Nullable List<String> list) {
                this.zzc = list == null ? null : new ArrayList<>(list);
                return this;
            }

            @RecentlyNonNull
            public Builder setAlwaysShowAccountPicker(boolean z) {
                this.zzd = z;
                return this;
            }

            @RecentlyNonNull
            public Builder setOptionsForAddingAccount(@Nullable Bundle bundle) {
                this.zzf = bundle;
                return this;
            }

            @RecentlyNonNull
            public Builder setSelectedAccount(@Nullable Account account) {
                this.zza = account;
                return this;
            }

            @RecentlyNonNull
            public Builder setTitleOverrideText(@Nullable String str) {
                this.zze = str;
                return this;
            }
        }

        static /* synthetic */ boolean zzB(AccountChooserOptions accountChooserOptions, boolean z) {
            accountChooserOptions.zzk = false;
            return false;
        }

        static /* synthetic */ boolean zzC(AccountChooserOptions accountChooserOptions, boolean z) {
            accountChooserOptions.zzn = false;
            return false;
        }

        static /* synthetic */ boolean zzD(AccountChooserOptions accountChooserOptions, boolean z) {
            accountChooserOptions.zzo = false;
            return false;
        }

        static /* synthetic */ boolean zza(AccountChooserOptions accountChooserOptions) {
            boolean z = accountChooserOptions.zzk;
            return false;
        }

        static /* synthetic */ String zzb(AccountChooserOptions accountChooserOptions) {
            String str = accountChooserOptions.zzj;
            return null;
        }

        static /* synthetic */ zza zzc(AccountChooserOptions accountChooserOptions) {
            zza zzaVar = accountChooserOptions.zzl;
            return null;
        }

        static /* synthetic */ boolean zzd(AccountChooserOptions accountChooserOptions) {
            boolean z = accountChooserOptions.zzb;
            return false;
        }

        static /* synthetic */ int zze(AccountChooserOptions accountChooserOptions) {
            int i = accountChooserOptions.zzi;
            return 0;
        }

        static /* synthetic */ boolean zzl(AccountChooserOptions accountChooserOptions) {
            boolean z = accountChooserOptions.zzh;
            return false;
        }

        static /* synthetic */ String zzm(AccountChooserOptions accountChooserOptions) {
            String str = accountChooserOptions.zzm;
            return null;
        }

        static /* synthetic */ boolean zzn(AccountChooserOptions accountChooserOptions) {
            boolean z = accountChooserOptions.zzn;
            return false;
        }

        static /* synthetic */ boolean zzo(AccountChooserOptions accountChooserOptions) {
            boolean z = accountChooserOptions.zzo;
            return false;
        }

        static /* synthetic */ zza zzs(AccountChooserOptions accountChooserOptions, zza zzaVar) {
            accountChooserOptions.zzl = null;
            return null;
        }

        static /* synthetic */ String zzt(AccountChooserOptions accountChooserOptions, String str) {
            accountChooserOptions.zzj = null;
            return null;
        }

        static /* synthetic */ boolean zzw(AccountChooserOptions accountChooserOptions, boolean z) {
            accountChooserOptions.zzb = false;
            return false;
        }

        static /* synthetic */ boolean zzx(AccountChooserOptions accountChooserOptions, boolean z) {
            accountChooserOptions.zzh = false;
            return false;
        }

        static /* synthetic */ String zzy(AccountChooserOptions accountChooserOptions, String str) {
            accountChooserOptions.zzm = null;
            return null;
        }

        static /* synthetic */ int zzz(AccountChooserOptions accountChooserOptions, int i) {
            accountChooserOptions.zzi = 0;
            return 0;
        }
    }

    private AccountPicker() {
    }

    @RecentlyNonNull
    @Deprecated
    public static Intent newChooseAccountIntent(@Nullable Account account, @Nullable ArrayList<Account> arrayList, @Nullable String[] strArr, boolean z, @Nullable String str, @Nullable String str2, @Nullable String[] strArr2, @Nullable Bundle bundle) {
        Intent intent = new Intent();
        Preconditions.checkArgument(true, "We only support hostedDomain filter for account chip styled account picker");
        intent.setAction("com.google.android.gms.common.account.CHOOSE_ACCOUNT");
        intent.setPackage("com.google.android.gms");
        intent.putExtra("allowableAccounts", arrayList);
        intent.putExtra("allowableAccountTypes", strArr);
        intent.putExtra("addAccountOptions", bundle);
        intent.putExtra("selectedAccount", account);
        intent.putExtra("alwaysPromptForAccount", z);
        intent.putExtra("descriptionTextOverride", str);
        intent.putExtra("authTokenType", str2);
        intent.putExtra("addAccountRequiredFeatures", strArr2);
        intent.putExtra("setGmsCoreAccount", false);
        intent.putExtra("overrideTheme", 0);
        intent.putExtra("overrideCustomTheme", 0);
        intent.putExtra("hostedDomainFilter", (String) null);
        return intent;
    }

    @RecentlyNonNull
    public static Intent newChooseAccountIntent(@RecentlyNonNull AccountChooserOptions accountChooserOptions) {
        Intent intent = new Intent();
        AccountChooserOptions.zza(accountChooserOptions);
        AccountChooserOptions.zzb(accountChooserOptions);
        Preconditions.checkArgument(true, "We only support hostedDomain filter for account chip styled account picker");
        AccountChooserOptions.zzc(accountChooserOptions);
        Preconditions.checkArgument(true, "Consent is only valid for account chip styled account picker");
        AccountChooserOptions.zzd(accountChooserOptions);
        Preconditions.checkArgument(true, "Making the selected account non-clickable is only supported for the theme THEME_DAY_NIGHT_GOOGLE_MATERIAL2");
        AccountChooserOptions.zza(accountChooserOptions);
        intent.setAction("com.google.android.gms.common.account.CHOOSE_ACCOUNT");
        intent.setPackage("com.google.android.gms");
        intent.putExtra("allowableAccounts", accountChooserOptions.zzc);
        if (accountChooserOptions.zzd != null) {
            intent.putExtra("allowableAccountTypes", (String[]) accountChooserOptions.zzd.toArray(new String[0]));
        }
        intent.putExtra("addAccountOptions", accountChooserOptions.zzg);
        intent.putExtra("selectedAccount", accountChooserOptions.zza);
        AccountChooserOptions.zzd(accountChooserOptions);
        intent.putExtra("selectedAccountIsNotClickable", false);
        intent.putExtra("alwaysPromptForAccount", accountChooserOptions.zze);
        intent.putExtra("descriptionTextOverride", accountChooserOptions.zzf);
        AccountChooserOptions.zzl(accountChooserOptions);
        intent.putExtra("setGmsCoreAccount", false);
        AccountChooserOptions.zzm(accountChooserOptions);
        intent.putExtra("realClientPackage", (String) null);
        AccountChooserOptions.zze(accountChooserOptions);
        intent.putExtra("overrideTheme", 0);
        AccountChooserOptions.zza(accountChooserOptions);
        intent.putExtra("overrideCustomTheme", 0);
        AccountChooserOptions.zzb(accountChooserOptions);
        intent.putExtra("hostedDomainFilter", (String) null);
        Bundle bundle = new Bundle();
        AccountChooserOptions.zza(accountChooserOptions);
        AccountChooserOptions.zzc(accountChooserOptions);
        AccountChooserOptions.zzn(accountChooserOptions);
        AccountChooserOptions.zzo(accountChooserOptions);
        if (!bundle.isEmpty()) {
            intent.putExtra("first_party_options_bundle", bundle);
        }
        return intent;
    }
}
