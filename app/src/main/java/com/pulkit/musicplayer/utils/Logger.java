
package com.pulkit.musicplayer.utils;

import android.util.Log;
/**
 * @author PULKIT MITAL
 * @date 31/7/2016
 */

public class Logger {

    public static void debug(String TAG, String writeText) {
        Log.d(TAG, writeText);
    }

    public static void info(String TAG, String writeText) {
        Log.i(TAG, writeText);
    }
}
