package com.microsoft.appcenter.utils;

import androidx.annotation.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class TicketCache {
    private static final Map<String, String> sTickets = new HashMap();

    public static String getTicket(String str) {
        return sTickets.get(str);
    }

    public static void putTicket(String str, String str2) {
        sTickets.put(str, str2);
    }

    @VisibleForTesting
    public static void clear() {
        sTickets.clear();
    }
}
