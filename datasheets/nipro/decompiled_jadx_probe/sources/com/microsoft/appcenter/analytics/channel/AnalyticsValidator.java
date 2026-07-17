package com.microsoft.appcenter.analytics.channel;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.analytics.ingestion.models.EventLog;
import com.microsoft.appcenter.analytics.ingestion.models.LogWithNameAndProperties;
import com.microsoft.appcenter.analytics.ingestion.models.PageLog;
import com.microsoft.appcenter.channel.AbstractChannelListener;
import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.properties.BooleanTypedProperty;
import com.microsoft.appcenter.ingestion.models.properties.DateTimeTypedProperty;
import com.microsoft.appcenter.ingestion.models.properties.DoubleTypedProperty;
import com.microsoft.appcenter.ingestion.models.properties.LongTypedProperty;
import com.microsoft.appcenter.ingestion.models.properties.StringTypedProperty;
import com.microsoft.appcenter.ingestion.models.properties.TypedProperty;
import com.microsoft.appcenter.utils.AppCenterLog;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/* loaded from: classes.dex */
public class AnalyticsValidator extends AbstractChannelListener {

    @VisibleForTesting
    static final int MAX_NAME_LENGTH = 256;

    @VisibleForTesting
    static final int MAX_PROPERTY_COUNT = 20;

    @VisibleForTesting
    static final int MAX_PROPERTY_ITEM_LENGTH = 125;

    private boolean validateLog(@NonNull LogWithNameAndProperties logWithNameAndProperties) {
        String validateName = validateName(logWithNameAndProperties.getName(), logWithNameAndProperties.getType());
        if (validateName == null) {
            return false;
        }
        Map<String, String> validateProperties = validateProperties(logWithNameAndProperties.getProperties(), validateName, logWithNameAndProperties.getType());
        logWithNameAndProperties.setName(validateName);
        logWithNameAndProperties.setProperties(validateProperties);
        return true;
    }

    private boolean validateLog(@NonNull EventLog eventLog) {
        String validateName = validateName(eventLog.getName(), eventLog.getType());
        if (validateName == null) {
            return false;
        }
        validateProperties(eventLog.getTypedProperties());
        eventLog.setName(validateName);
        return true;
    }

    private static String validateName(String str, String str2) {
        if (str == null || str.isEmpty()) {
            AppCenterLog.error(Analytics.LOG_TAG, str2 + " name cannot be null or empty.");
            return null;
        }
        if (str.length() <= 256) {
            return str;
        }
        AppCenterLog.warn(Analytics.LOG_TAG, String.format("%s '%s' : name length cannot be longer than %s characters. Name will be truncated.", str2, str, 256));
        return str.substring(0, 256);
    }

    private static Map<String, String> validateProperties(Map<String, String> map, String str, String str2) {
        if (map == null) {
            return null;
        }
        HashMap hashMap = new HashMap();
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<String, String> next = it.next();
            String key = next.getKey();
            String value = next.getValue();
            if (hashMap.size() >= 20) {
                AppCenterLog.warn(Analytics.LOG_TAG, String.format("%s '%s' : properties cannot contain more than %s items. Skipping other properties.", str2, str, 20));
                break;
            }
            if (key == null || key.isEmpty()) {
                AppCenterLog.warn(Analytics.LOG_TAG, String.format("%s '%s' : a property key cannot be null or empty. Property will be skipped.", str2, str));
            } else if (value == null) {
                AppCenterLog.warn(Analytics.LOG_TAG, String.format("%s '%s' : property '%s' : property value cannot be null. Property '%s' will be skipped.", str2, str, key, key));
            } else {
                if (key.length() > 125) {
                    AppCenterLog.warn(Analytics.LOG_TAG, String.format("%s '%s' : property '%s' : property key length cannot be longer than %s characters. Property key will be truncated.", str2, str, key, 125));
                    key = key.substring(0, 125);
                }
                if (value.length() > 125) {
                    AppCenterLog.warn(Analytics.LOG_TAG, String.format("%s '%s' : property '%s' : property value cannot be longer than %s characters. Property value will be truncated.", str2, str, key, 125));
                    value = value.substring(0, 125);
                }
                hashMap.put(key, value);
            }
        }
        return hashMap;
    }

    private static void validateProperties(List<TypedProperty> list) {
        boolean z;
        if (list == null) {
            return;
        }
        ListIterator<TypedProperty> listIterator = list.listIterator();
        int i = 0;
        boolean z2 = false;
        while (listIterator.hasNext()) {
            TypedProperty next = listIterator.next();
            String name = next.getName();
            if (i >= 20) {
                if (!z2) {
                    AppCenterLog.warn(Analytics.LOG_TAG, String.format("Typed properties cannot contain more than %s items. Skipping other properties.", 20));
                    z2 = true;
                }
                listIterator.remove();
            } else if (name == null || name.isEmpty()) {
                AppCenterLog.warn(Analytics.LOG_TAG, "A typed property key cannot be null or empty. Property will be skipped.");
                listIterator.remove();
            } else {
                if (name.length() > 125) {
                    AppCenterLog.warn(Analytics.LOG_TAG, String.format("Typed property '%s' : property key length cannot be longer than %s characters. Property key will be truncated.", name, 125));
                    name = name.substring(0, 125);
                    next = copyProperty(next, name);
                    listIterator.set(next);
                    z = false;
                } else {
                    z = true;
                }
                if (next instanceof StringTypedProperty) {
                    StringTypedProperty stringTypedProperty = (StringTypedProperty) next;
                    String value = stringTypedProperty.getValue();
                    if (value == null) {
                        AppCenterLog.warn(Analytics.LOG_TAG, String.format("Typed property '%s' : property value cannot be null. Property '%s' will be skipped.", name, name));
                        listIterator.remove();
                    } else if (value.length() > 125) {
                        AppCenterLog.warn(Analytics.LOG_TAG, String.format("A String property '%s' : property value cannot be longer than %s characters. Property value will be truncated.", name, 125));
                        String substring = value.substring(0, 125);
                        if (z) {
                            StringTypedProperty stringTypedProperty2 = new StringTypedProperty();
                            stringTypedProperty2.setName(name);
                            stringTypedProperty2.setValue(substring);
                            listIterator.set(stringTypedProperty2);
                        } else {
                            stringTypedProperty.setValue(substring);
                        }
                    }
                }
                i++;
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static TypedProperty copyProperty(TypedProperty typedProperty, String str) {
        StringTypedProperty stringTypedProperty;
        String type = typedProperty.getType();
        if (BooleanTypedProperty.TYPE.equals(type)) {
            BooleanTypedProperty booleanTypedProperty = new BooleanTypedProperty();
            booleanTypedProperty.setValue(((BooleanTypedProperty) typedProperty).getValue());
            stringTypedProperty = booleanTypedProperty;
        } else if (DateTimeTypedProperty.TYPE.equals(type)) {
            DateTimeTypedProperty dateTimeTypedProperty = new DateTimeTypedProperty();
            dateTimeTypedProperty.setValue(((DateTimeTypedProperty) typedProperty).getValue());
            stringTypedProperty = dateTimeTypedProperty;
        } else if (DoubleTypedProperty.TYPE.equals(type)) {
            DoubleTypedProperty doubleTypedProperty = new DoubleTypedProperty();
            doubleTypedProperty.setValue(((DoubleTypedProperty) typedProperty).getValue());
            stringTypedProperty = doubleTypedProperty;
        } else if (LongTypedProperty.TYPE.equals(type)) {
            LongTypedProperty longTypedProperty = new LongTypedProperty();
            longTypedProperty.setValue(((LongTypedProperty) typedProperty).getValue());
            stringTypedProperty = longTypedProperty;
        } else {
            StringTypedProperty stringTypedProperty2 = new StringTypedProperty();
            stringTypedProperty2.setValue(((StringTypedProperty) typedProperty).getValue());
            stringTypedProperty = stringTypedProperty2;
        }
        stringTypedProperty.setName(str);
        return stringTypedProperty;
    }

    @Override // com.microsoft.appcenter.channel.AbstractChannelListener, com.microsoft.appcenter.channel.Channel.Listener
    public boolean shouldFilter(@NonNull Log log) {
        if (log instanceof PageLog) {
            return !validateLog((LogWithNameAndProperties) log);
        }
        if (log instanceof EventLog) {
            return !validateLog((EventLog) log);
        }
        return false;
    }
}
