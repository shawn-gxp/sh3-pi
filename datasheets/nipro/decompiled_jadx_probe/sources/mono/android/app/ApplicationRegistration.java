package mono.android.app;

import crc64357c0806a054c747.Application;
import crc64357c0806a054c747.MainApplication;
import crc648c15711fce523d6b.CaliburnApplication;
import mono.android.Runtime;

/* loaded from: classes.dex */
public class ApplicationRegistration {
    public static void registerApplications() {
        Runtime.register("NHL.Droid.Application, NHL.Android, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null", Application.class, Application.__md_methods);
        Runtime.register("NHL.Droid.MainApplication, NHL.Android, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null", MainApplication.class, MainApplication.__md_methods);
        Runtime.register("Caliburn.Micro.CaliburnApplication, Caliburn.Micro.Platform, Version=3.2.0.0, Culture=neutral, PublicKeyToken=null", CaliburnApplication.class, CaliburnApplication.__md_methods);
    }
}
