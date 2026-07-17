package com.microsoft.appcenter.utils.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.microsoft.appcenter.utils.AppCenterLog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/* loaded from: classes.dex */
public class FileManager {

    @SuppressLint({"StaticFieldLeak"})
    private static Context sContext;

    public static synchronized void initialize(Context context) {
        synchronized (FileManager.class) {
            if (sContext == null) {
                sContext = context;
            }
        }
    }

    public static String read(@NonNull String str) {
        return read(new File(str));
    }

    public static String read(@NonNull File file) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            try {
                String property = System.getProperty("line.separator");
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine != null) {
                        sb.append(readLine);
                        sb.append(property);
                    } else {
                        bufferedReader.close();
                        return sb.toString();
                    }
                }
            } catch (Throwable th) {
                bufferedReader.close();
                throw th;
            }
        } catch (IOException e) {
            AppCenterLog.error("AppCenter", "Could not read file " + file.getAbsolutePath(), e);
            return null;
        }
    }

    public static byte[] readBytes(@NonNull File file) {
        byte[] bArr = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                new DataInputStream(fileInputStream).readFully(bArr);
                return bArr;
            } finally {
                fileInputStream.close();
            }
        } catch (IOException e) {
            AppCenterLog.error("AppCenter", "Could not read file " + file.getAbsolutePath(), e);
            return null;
        }
    }

    public static void write(@NonNull String str, @NonNull String str2) throws IOException {
        write(new File(str), str2);
    }

    public static void write(@NonNull File file, @NonNull String str) throws IOException {
        if (TextUtils.isEmpty(str) || TextUtils.getTrimmedLength(str) <= 0) {
            return;
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        try {
            bufferedWriter.write(str);
        } finally {
            bufferedWriter.close();
        }
    }

    public static <T extends Serializable> T readObject(@NonNull File file) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
        try {
            return (T) objectInputStream.readObject();
        } finally {
            objectInputStream.close();
        }
    }

    public static <T extends Serializable> void writeObject(@NonNull File file, @NonNull T t) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
        try {
            objectOutputStream.writeObject(t);
        } finally {
            objectOutputStream.close();
        }
    }

    @NonNull
    public static String[] getFilenames(@NonNull String str, @Nullable FilenameFilter filenameFilter) {
        File file = new File(str);
        return file.exists() ? file.list(filenameFilter) : new String[0];
    }

    @Nullable
    public static File lastModifiedFile(@NonNull String str, @Nullable FilenameFilter filenameFilter) {
        return lastModifiedFile(new File(str), filenameFilter);
    }

    @Nullable
    public static File lastModifiedFile(@NonNull File file, @Nullable FilenameFilter filenameFilter) {
        File file2 = null;
        if (file.exists()) {
            File[] listFiles = file.listFiles(filenameFilter);
            long j = 0;
            if (listFiles != null) {
                for (File file3 : listFiles) {
                    if (file3.lastModified() > j) {
                        j = file3.lastModified();
                        file2 = file3;
                    }
                }
            }
        }
        return file2;
    }

    public static boolean delete(@NonNull String str) {
        return delete(new File(str));
    }

    public static boolean delete(@NonNull File file) {
        return file.delete();
    }

    public static void mkdir(@NonNull String str) {
        new File(str).mkdirs();
    }
}
