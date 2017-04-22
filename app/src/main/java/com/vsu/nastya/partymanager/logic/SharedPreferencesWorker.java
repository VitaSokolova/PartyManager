package com.vsu.nastya.partymanager.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by anast on 22.04.2017.
 */

public abstract class SharedPreferencesWorker {

    private static final String PREFERENCES = "preferences";
    private static final String PREFERENCES_VK_ID = "vk_id";

    public static void saveUsersVkIdToPreferences(Context context, String id) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCES_VK_ID, id);
        editor.apply();
    }

    public static String getUsersVkIdFromPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, AppCompatActivity.MODE_PRIVATE);
        if (preferences.contains(PREFERENCES_VK_ID)){
            return preferences.getString(PREFERENCES_VK_ID, "");
        }
        return null;
    }
}
