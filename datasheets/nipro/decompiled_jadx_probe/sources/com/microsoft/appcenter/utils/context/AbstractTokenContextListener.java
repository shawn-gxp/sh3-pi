package com.microsoft.appcenter.utils.context;

import com.microsoft.appcenter.utils.context.AuthTokenContext;

/* loaded from: classes.dex */
public abstract class AbstractTokenContextListener implements AuthTokenContext.Listener {
    @Override // com.microsoft.appcenter.utils.context.AuthTokenContext.Listener
    public void onNewAuthToken(String str) {
    }

    @Override // com.microsoft.appcenter.utils.context.AuthTokenContext.Listener
    public void onNewUser(String str) {
    }

    @Override // com.microsoft.appcenter.utils.context.AuthTokenContext.Listener
    public void onTokenRequiresRefresh(String str) {
    }
}
