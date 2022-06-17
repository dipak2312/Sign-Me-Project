package com.app.signme.commonUtils.utility.helper;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

@SuppressLint("LogNotTimber")
public class LOGApp {

    public static boolean ENABLE_LOG = true;//true;
    public static String LOG_FILE_NAME = "WhiteLable App";

    public static void v(@NonNull String strTag, @NonNull String strMessage) {
        if (ENABLE_LOG)
            Log.v(LOG_FILE_NAME, (strMessage!=null)?strMessage:"");
    }

    public static void d(@NonNull String strTag, @NonNull String strMessage) {
        if (ENABLE_LOG)
            Log.d(strTag, (strMessage!=null)?strMessage:"");
    }

    public static void e(@NonNull String strTag, @NonNull String strMessage) {
        if (ENABLE_LOG)
            Log.e(strTag, (strMessage!=null)?strMessage:"");
    }

    public static void e(@NonNull String strMessage) {
        if (ENABLE_LOG)
            Log.e(LOG_FILE_NAME, (strMessage!=null)?strMessage:"");
    }

    public static void w(@NonNull String strTag, @NonNull String strMessage) {
        if (ENABLE_LOG)
            Log.w(LOG_FILE_NAME, strMessage);
    }

    public static void i(@NonNull String strTag, @NonNull String strMessage) {
        if (ENABLE_LOG)
            Log.i(strTag, (strMessage!=null)?strMessage:"");
    }

    public static void println(@NonNull String strMessage) {
        if (ENABLE_LOG)
            System.out.println((strMessage!=null)?strMessage:"");
    }

    public static void e(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
        if (ENABLE_LOG)
            Log.e(LOG_FILE_NAME, (msg!=null)?msg:"", tr);
    }

    public static void d(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
        if (ENABLE_LOG)
            Log.e(LOG_FILE_NAME, msg, tr);
    }

    public static void wtf(@NonNull String tag, @NonNull String msg) {
//        if (ENABLE_LOG && !CommonUtils.isEmpty(tag) && !CommonUtils.isEmpty(msg))
//            Log.wtf(LOG_FILE_NAME, msg);
    }

    public static void e(String msg, Throwable e) {
        if (!ENABLE_LOG) return;
        Log.e(LOG_FILE_NAME, msg, e);
    }
}
