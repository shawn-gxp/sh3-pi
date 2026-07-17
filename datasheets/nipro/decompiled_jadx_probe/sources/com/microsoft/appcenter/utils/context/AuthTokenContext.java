package com.microsoft.appcenter.utils.context;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.crypto.CryptoUtils;
import com.microsoft.appcenter.utils.storage.SharedPreferencesManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/* loaded from: classes.dex */
public class AuthTokenContext {

    @VisibleForTesting
    static final int ACCOUNT_ID_LENGTH = 36;

    @VisibleForTesting
    static final String PREFERENCE_KEY_TOKEN_HISTORY = "AppCenter.auth_token_history";

    @VisibleForTesting
    static final int TOKEN_HISTORY_LIMIT = 5;

    @SuppressLint({"StaticFieldLeak"})
    private static AuthTokenContext sInstance;
    private Context mContext;
    private List<AuthTokenHistoryEntry> mHistory;
    private final Set<Listener> mListeners = Collections.newSetFromMap(new ConcurrentHashMap());
    private boolean mResetAuthTokenRequired = true;

    public interface Listener {
        void onNewAuthToken(String str);

        void onNewUser(String str);

        void onTokenRequiresRefresh(String str);
    }

    public static synchronized void initialize(@NonNull Context context) {
        synchronized (AuthTokenContext.class) {
            AuthTokenContext authTokenContext = getInstance();
            authTokenContext.mContext = context.getApplicationContext();
            authTokenContext.getHistory();
        }
    }

    public static synchronized AuthTokenContext getInstance() {
        AuthTokenContext authTokenContext;
        synchronized (AuthTokenContext.class) {
            if (sInstance == null) {
                sInstance = new AuthTokenContext();
            }
            authTokenContext = sInstance;
        }
        return authTokenContext;
    }

    @VisibleForTesting
    public static synchronized void unsetInstance() {
        synchronized (AuthTokenContext.class) {
            sInstance = null;
        }
    }

    public void addListener(@NonNull Listener listener) {
        this.mListeners.add(listener);
    }

    public void removeListener(@NonNull Listener listener) {
        this.mListeners.remove(listener);
    }

    public synchronized void doNotResetAuthAfterStart() {
        this.mResetAuthTokenRequired = false;
    }

    public synchronized void finishInitialization() {
        if (this.mResetAuthTokenRequired) {
            this.mResetAuthTokenRequired = false;
            setAuthToken(null, null, null);
        }
    }

    public void setAuthToken(String str, String str2, Date date) {
        if (str == null) {
            str2 = null;
            date = null;
        }
        Boolean addTokenHistory = addTokenHistory(str, str2, date);
        if (addTokenHistory == null) {
            return;
        }
        for (Listener listener : this.mListeners) {
            listener.onNewAuthToken(str);
            if (addTokenHistory.booleanValue()) {
                listener.onNewUser(str2 == null ? null : str2.substring(0, Math.min(36, str2.length())));
            }
        }
    }

    private synchronized Boolean addTokenHistory(String str, String str2, Date date) {
        List<AuthTokenHistoryEntry> history = getHistory();
        if (history == null) {
            history = new ArrayList<>();
        }
        boolean z = true;
        AuthTokenHistoryEntry authTokenHistoryEntry = history.size() > 0 ? history.get(history.size() - 1) : null;
        if (authTokenHistoryEntry != null && TextUtils.equals(authTokenHistoryEntry.getAuthToken(), str)) {
            return null;
        }
        if (authTokenHistoryEntry != null && TextUtils.equals(authTokenHistoryEntry.getHomeAccountId(), str2)) {
            z = false;
        }
        Date date2 = new Date();
        if (authTokenHistoryEntry != null && authTokenHistoryEntry.getExpiresOn() != null && date2.after(authTokenHistoryEntry.getExpiresOn())) {
            if (z && str != null) {
                history.add(new AuthTokenHistoryEntry(null, null, authTokenHistoryEntry.getExpiresOn(), date2));
            }
            date2 = authTokenHistoryEntry.getExpiresOn();
        }
        history.add(new AuthTokenHistoryEntry(str, str2, date2, date));
        if (history.size() > 5) {
            history.subList(0, history.size() - 5).clear();
            AppCenterLog.debug("AppCenter", "Size of the token history is exceeded. The oldest token has been removed.");
        }
        setHistory(history);
        return Boolean.valueOf(z);
    }

    public String getAuthToken() {
        AuthTokenHistoryEntry lastHistoryEntry = getLastHistoryEntry();
        if (lastHistoryEntry != null) {
            return lastHistoryEntry.getAuthToken();
        }
        return null;
    }

    public String getHomeAccountId() {
        AuthTokenHistoryEntry lastHistoryEntry = getLastHistoryEntry();
        if (lastHistoryEntry != null) {
            return lastHistoryEntry.getHomeAccountId();
        }
        return null;
    }

    public String getAccountId() {
        String homeAccountId = getHomeAccountId();
        if (homeAccountId == null) {
            return null;
        }
        return homeAccountId.substring(0, Math.min(36, homeAccountId.length()));
    }

    @NonNull
    public synchronized List<AuthTokenInfo> getAuthTokenValidityList() {
        List<AuthTokenHistoryEntry> history = getHistory();
        if (history != null && history.size() != 0) {
            ArrayList arrayList = new ArrayList();
            int i = 0;
            if (history.get(0).getAuthToken() != null) {
                arrayList.add(new AuthTokenInfo(null, null, history.get(0).getTime()));
            }
            while (i < history.size()) {
                AuthTokenHistoryEntry authTokenHistoryEntry = history.get(i);
                String authToken = authTokenHistoryEntry.getAuthToken();
                Date time = authTokenHistoryEntry.getTime();
                if (authToken == null && i == 0) {
                    time = null;
                }
                Date expiresOn = authTokenHistoryEntry.getExpiresOn();
                i++;
                Date time2 = history.size() > i ? history.get(i).getTime() : null;
                if (time2 != null) {
                    if (expiresOn != null && time2.before(expiresOn)) {
                        expiresOn = time2;
                        arrayList.add(new AuthTokenInfo(authToken, time, expiresOn));
                    }
                }
                if (expiresOn == null) {
                    if (time2 == null) {
                    }
                    expiresOn = time2;
                }
                arrayList.add(new AuthTokenInfo(authToken, time, expiresOn));
            }
            return arrayList;
        }
        return Collections.singletonList(new AuthTokenInfo());
    }

    public synchronized void removeOldestTokenIfMatching(String str) {
        List<AuthTokenHistoryEntry> history = getHistory();
        if (history != null && history.size() != 0) {
            if (history.size() == 1) {
                AppCenterLog.debug("AppCenter", "Couldn't remove token from history: token history contains only current one.");
                return;
            } else {
                if (!TextUtils.equals(history.get(0).getAuthToken(), str)) {
                    AppCenterLog.debug("AppCenter", "Couldn't remove token from history: the token isn't oldest or is already removed.");
                    return;
                }
                history.remove(0);
                setHistory(history);
                AppCenterLog.debug("AppCenter", "The token has been removed from token history.");
                return;
            }
        }
        AppCenterLog.warn("AppCenter", "Couldn't remove token from history: token history is empty.");
    }

    public void checkIfTokenNeedsToBeRefreshed(@NonNull AuthTokenInfo authTokenInfo) {
        AuthTokenHistoryEntry lastHistoryEntry = getLastHistoryEntry();
        if (lastHistoryEntry == null || authTokenInfo.getAuthToken() == null || !authTokenInfo.getAuthToken().equals(lastHistoryEntry.getAuthToken()) || !authTokenInfo.isAboutToExpire()) {
            return;
        }
        Iterator<Listener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onTokenRequiresRefresh(lastHistoryEntry.getHomeAccountId());
        }
    }

    private synchronized AuthTokenHistoryEntry getLastHistoryEntry() {
        List<AuthTokenHistoryEntry> history = getHistory();
        if (history == null || history.size() <= 0) {
            return null;
        }
        return history.get(history.size() - 1);
    }

    @VisibleForTesting
    List<AuthTokenHistoryEntry> getHistory() {
        List<AuthTokenHistoryEntry> list = this.mHistory;
        if (list != null) {
            return list;
        }
        String string = SharedPreferencesManager.getString(PREFERENCE_KEY_TOKEN_HISTORY, null);
        String decryptedData = (string == null || string.isEmpty()) ? null : CryptoUtils.getInstance(this.mContext).decrypt(string, false).getDecryptedData();
        if (decryptedData == null || decryptedData.isEmpty()) {
            return null;
        }
        try {
            this.mHistory = deserializeHistory(decryptedData);
        } catch (JSONException e) {
            AppCenterLog.warn("AppCenter", "Failed to deserialize auth token history.", e);
        }
        return this.mHistory;
    }

    @VisibleForTesting
    void setHistory(List<AuthTokenHistoryEntry> list) {
        this.mHistory = list;
        if (list != null) {
            try {
                SharedPreferencesManager.putString(PREFERENCE_KEY_TOKEN_HISTORY, CryptoUtils.getInstance(this.mContext).encrypt(serializeHistory(list)));
                return;
            } catch (JSONException e) {
                AppCenterLog.warn("AppCenter", "Failed to serialize auth token history.", e);
                return;
            }
        }
        SharedPreferencesManager.remove(PREFERENCE_KEY_TOKEN_HISTORY);
    }

    private String serializeHistory(List<AuthTokenHistoryEntry> list) throws JSONException {
        JSONStringer jSONStringer = new JSONStringer();
        jSONStringer.array();
        for (AuthTokenHistoryEntry authTokenHistoryEntry : list) {
            jSONStringer.object();
            authTokenHistoryEntry.write(jSONStringer);
            jSONStringer.endObject();
        }
        jSONStringer.endArray();
        return jSONStringer.toString();
    }

    private List<AuthTokenHistoryEntry> deserializeHistory(String str) throws JSONException {
        JSONArray jSONArray = new JSONArray(str);
        ArrayList arrayList = new ArrayList(jSONArray.length());
        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i);
            AuthTokenHistoryEntry authTokenHistoryEntry = new AuthTokenHistoryEntry();
            authTokenHistoryEntry.read(jSONObject);
            arrayList.add(authTokenHistoryEntry);
        }
        return arrayList;
    }
}
