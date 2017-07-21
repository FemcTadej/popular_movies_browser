package com.hugiell.popularmoviesapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import java.io.File;

public class Utility {

    static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    static void deleteFileeTree(File file) {
        if (file.isDirectory())
            for (File child : file.listFiles())
                deleteFileeTree(child);
        file.delete();
    }

    static boolean isSortListByFavorites(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPrefs.getString(context.getString(R.string.settings_sortBy_key), "3").equalsIgnoreCase("3")) {
            return true;
        } else {
            return false;
        }
    }
}
