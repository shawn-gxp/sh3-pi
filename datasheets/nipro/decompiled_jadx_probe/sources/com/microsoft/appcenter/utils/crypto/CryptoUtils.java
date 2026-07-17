package com.microsoft.appcenter.utils.crypto;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.Constants;
import com.microsoft.appcenter.utils.AppCenterLog;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

/* loaded from: classes.dex */
public class CryptoUtils {

    @VisibleForTesting
    static final ICryptoFactory DEFAULT_CRYPTO_FACTORY = new ICryptoFactory() { // from class: com.microsoft.appcenter.utils.crypto.CryptoUtils.1
        @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.ICryptoFactory
        public IKeyGenerator getKeyGenerator(String str, String str2) throws Exception {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(str, str2);
            return new IKeyGenerator() { // from class: com.microsoft.appcenter.utils.crypto.CryptoUtils.1.1
                @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.IKeyGenerator
                public void init(AlgorithmParameterSpec algorithmParameterSpec) throws Exception {
                    keyGenerator.init(algorithmParameterSpec);
                }

                @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.IKeyGenerator
                public void generateKey() {
                    keyGenerator.generateKey();
                }
            };
        }

        @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.ICryptoFactory
        public ICipher getCipher(String str, String str2) throws Exception {
            final Cipher cipher = Cipher.getInstance(str, str2);
            return new ICipher() { // from class: com.microsoft.appcenter.utils.crypto.CryptoUtils.1.2
                @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.ICipher
                public void init(int i, Key key) throws Exception {
                    cipher.init(i, key);
                }

                @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.ICipher
                public void init(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec) throws Exception {
                    cipher.init(i, key, algorithmParameterSpec);
                }

                @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.ICipher
                public byte[] doFinal(byte[] bArr) throws Exception {
                    return cipher.doFinal(bArr);
                }

                @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.ICipher
                public byte[] doFinal(byte[] bArr, int i, int i2) throws Exception {
                    return cipher.doFinal(bArr, i, i2);
                }

                @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.ICipher
                public byte[] getIV() {
                    return cipher.getIV();
                }

                @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.ICipher
                public int getBlockSize() {
                    return cipher.getBlockSize();
                }

                @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.ICipher
                public String getAlgorithm() {
                    return cipher.getAlgorithm();
                }

                @Override // com.microsoft.appcenter.utils.crypto.CryptoUtils.ICipher
                public String getProvider() {
                    return cipher.getProvider().getName();
                }
            };
        }
    };

    @SuppressLint({"StaticFieldLeak"})
    private static CryptoUtils sInstance;
    private final int mApiLevel;
    private final Context mContext;
    private final ICryptoFactory mCryptoFactory;

    @VisibleForTesting
    final Map<String, CryptoHandlerEntry> mCryptoHandlers;
    private final KeyStore mKeyStore;

    interface ICipher {
        byte[] doFinal(byte[] bArr) throws Exception;

        byte[] doFinal(byte[] bArr, int i, int i2) throws Exception;

        @VisibleForTesting
        String getAlgorithm();

        int getBlockSize();

        byte[] getIV();

        @VisibleForTesting
        String getProvider();

        void init(int i, Key key) throws Exception;

        void init(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec) throws Exception;
    }

    interface ICryptoFactory {
        ICipher getCipher(String str, String str2) throws Exception;

        IKeyGenerator getKeyGenerator(String str, String str2) throws Exception;
    }

    interface IKeyGenerator {
        void generateKey();

        void init(AlgorithmParameterSpec algorithmParameterSpec) throws Exception;
    }

    private CryptoUtils(@NonNull Context context) {
        this(context, DEFAULT_CRYPTO_FACTORY, Build.VERSION.SDK_INT);
    }

    /* JADX WARN: Removed duplicated region for block: B:15:0x0044 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    @TargetApi(23)
    @VisibleForTesting
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    CryptoUtils(@NonNull Context context, @NonNull ICryptoFactory iCryptoFactory, int i) {
        KeyStore keyStore;
        this.mCryptoHandlers = new LinkedHashMap();
        this.mContext = context.getApplicationContext();
        this.mCryptoFactory = iCryptoFactory;
        this.mApiLevel = i;
        KeyStore keyStore2 = null;
        if (i >= 19) {
            try {
                keyStore = KeyStore.getInstance("AndroidKeyStore");
            } catch (Exception unused) {
            }
            try {
                keyStore.load(null);
                keyStore2 = keyStore;
            } catch (Exception unused2) {
                keyStore2 = keyStore;
                AppCenterLog.error("AppCenter", "Cannot use secure keystore on this device.");
                this.mKeyStore = keyStore2;
                if (keyStore2 != null) {
                    try {
                        registerHandler(new CryptoAesHandler());
                    } catch (Exception unused3) {
                        AppCenterLog.error("AppCenter", "Cannot use modern encryption on this device.");
                    }
                }
                if (keyStore2 != null) {
                }
                CryptoNoOpHandler cryptoNoOpHandler = new CryptoNoOpHandler();
                this.mCryptoHandlers.put(cryptoNoOpHandler.getAlgorithm(), new CryptoHandlerEntry(0, 0, cryptoNoOpHandler));
            }
        }
        this.mKeyStore = keyStore2;
        if (keyStore2 != null && i >= 23) {
            registerHandler(new CryptoAesHandler());
        }
        if (keyStore2 != null) {
            try {
                registerHandler(new CryptoRsaHandler());
            } catch (Exception unused4) {
                AppCenterLog.error("AppCenter", "Cannot use old encryption on this device.");
            }
        }
        CryptoNoOpHandler cryptoNoOpHandler2 = new CryptoNoOpHandler();
        this.mCryptoHandlers.put(cryptoNoOpHandler2.getAlgorithm(), new CryptoHandlerEntry(0, 0, cryptoNoOpHandler2));
    }

    public static CryptoUtils getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new CryptoUtils(context);
        }
        return sInstance;
    }

    @VisibleForTesting
    ICryptoFactory getCryptoFactory() {
        return this.mCryptoFactory;
    }

    private void registerHandler(@NonNull CryptoHandler cryptoHandler) throws Exception {
        int i;
        int i2 = 0;
        String alias = getAlias(cryptoHandler, 0, false);
        String alias2 = getAlias(cryptoHandler, 1, false);
        String alias3 = getAlias(cryptoHandler, 0, true);
        String alias4 = getAlias(cryptoHandler, 1, true);
        Date creationDate = this.mKeyStore.getCreationDate(alias);
        Date creationDate2 = this.mKeyStore.getCreationDate(alias2);
        Date creationDate3 = this.mKeyStore.getCreationDate(alias3);
        Date creationDate4 = this.mKeyStore.getCreationDate(alias4);
        if (creationDate2 == null || !creationDate2.after(creationDate)) {
            i = 0;
        } else {
            alias = alias2;
            i = 1;
        }
        if (creationDate4 != null && creationDate4.after(creationDate3)) {
            i2 = 1;
        }
        if (this.mCryptoHandlers.isEmpty() && !this.mKeyStore.containsAlias(alias)) {
            AppCenterLog.debug("AppCenter", "Creating alias: " + alias);
            cryptoHandler.generateKey(this.mCryptoFactory, alias, this.mContext);
        }
        AppCenterLog.debug("AppCenter", "Using " + alias);
        this.mCryptoHandlers.put(cryptoHandler.getAlgorithm(), new CryptoHandlerEntry(i, i2, cryptoHandler));
    }

    @NonNull
    private String getAlias(@NonNull CryptoHandler cryptoHandler, int i, boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append(z ? "mobile.center" : "appcenter");
        sb.append(".");
        sb.append(i);
        sb.append(".");
        sb.append(cryptoHandler.getAlgorithm());
        return sb.toString();
    }

    @Nullable
    private KeyStore.Entry getKeyStoreEntry(@NonNull CryptoHandlerEntry cryptoHandlerEntry) throws Exception {
        return getKeyStoreEntry(cryptoHandlerEntry.mCryptoHandler, cryptoHandlerEntry.mAliasIndex, false);
    }

    @Nullable
    private KeyStore.Entry getKeyStoreEntry(CryptoHandler cryptoHandler, int i, boolean z) throws Exception {
        if (this.mKeyStore == null) {
            return null;
        }
        return this.mKeyStore.getEntry(getAlias(cryptoHandler, i, z), null);
    }

    @Nullable
    public String encrypt(@Nullable String str) {
        if (str == null) {
            return null;
        }
        try {
            CryptoHandlerEntry next = this.mCryptoHandlers.values().iterator().next();
            CryptoHandler cryptoHandler = next.mCryptoHandler;
            try {
                return cryptoHandler.getAlgorithm() + Constants.COMMON_SCHEMA_PREFIX_SEPARATOR + Base64.encodeToString(cryptoHandler.encrypt(this.mCryptoFactory, this.mApiLevel, getKeyStoreEntry(next), str.getBytes("UTF-8")), 0);
            } catch (InvalidKeyException unused) {
                AppCenterLog.debug("AppCenter", "Alias expired: " + next.mAliasIndex);
                int i = next.mAliasIndex ^ 1;
                next.mAliasIndex = i;
                String alias = getAlias(cryptoHandler, i, false);
                if (this.mKeyStore.containsAlias(alias)) {
                    AppCenterLog.debug("AppCenter", "Deleting alias: " + alias);
                    this.mKeyStore.deleteEntry(alias);
                }
                AppCenterLog.debug("AppCenter", "Creating alias: " + alias);
                cryptoHandler.generateKey(this.mCryptoFactory, alias, this.mContext);
                return encrypt(str);
            }
        } catch (Exception unused2) {
            AppCenterLog.error("AppCenter", "Failed to encrypt data.");
            return str;
        }
    }

    @NonNull
    public DecryptedData decrypt(@Nullable String str, boolean z) {
        if (str == null) {
            return new DecryptedData(null, null);
        }
        String[] split = str.split(Constants.COMMON_SCHEMA_PREFIX_SEPARATOR);
        CryptoHandlerEntry cryptoHandlerEntry = split.length == 2 ? this.mCryptoHandlers.get(split[0]) : null;
        CryptoHandler cryptoHandler = cryptoHandlerEntry == null ? null : cryptoHandlerEntry.mCryptoHandler;
        if (cryptoHandler == null) {
            AppCenterLog.error("AppCenter", "Failed to decrypt data.");
            return new DecryptedData(str, null);
        }
        try {
            try {
                return getDecryptedData(cryptoHandler, cryptoHandlerEntry.mAliasIndex, split[1], z);
            } catch (Exception unused) {
                return getDecryptedData(cryptoHandler, cryptoHandlerEntry.mAliasIndex ^ 1, split[1], z);
            }
        } catch (Exception unused2) {
            AppCenterLog.error("AppCenter", "Failed to decrypt data.");
            return new DecryptedData(str, null);
        }
    }

    @NonNull
    private DecryptedData getDecryptedData(CryptoHandler cryptoHandler, int i, String str, boolean z) throws Exception {
        String str2 = new String(cryptoHandler.decrypt(this.mCryptoFactory, this.mApiLevel, getKeyStoreEntry(cryptoHandler, i, z), Base64.decode(str, 0)), "UTF-8");
        return new DecryptedData(str2, cryptoHandler != this.mCryptoHandlers.values().iterator().next().mCryptoHandler ? encrypt(str2) : null);
    }

    @VisibleForTesting
    static class CryptoHandlerEntry {
        int mAliasIndex;
        final int mAliasIndexMC;
        final CryptoHandler mCryptoHandler;

        CryptoHandlerEntry(int i, int i2, CryptoHandler cryptoHandler) {
            this.mAliasIndex = i;
            this.mAliasIndexMC = i2;
            this.mCryptoHandler = cryptoHandler;
        }
    }

    public static class DecryptedData {
        final String mDecryptedData;
        final String mNewEncryptedData;

        @VisibleForTesting
        public DecryptedData(String str, String str2) {
            this.mDecryptedData = str;
            this.mNewEncryptedData = str2;
        }

        public String getDecryptedData() {
            return this.mDecryptedData;
        }

        public String getNewEncryptedData() {
            return this.mNewEncryptedData;
        }
    }
}
