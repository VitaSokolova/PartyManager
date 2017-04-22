package com.vsu.nastya.partymanager.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import static com.vsu.nastya.partymanager.logic.DatabaseConsts.*;

/**
 * Класс предназначен для работы с Firebase notification token, предназначенный для
 * отправки оповещений на конкретные устройства.
 */

public abstract class FirebaseNotificationTokenWorker {

    private static final String PREFERENCES = "preferences";
    private static final String PREFERENCES_VK_ID = "vk_id";

    /**
     * Метод отправляет новый notification token в базу данных Firebase.
     *
     * @param token notification token
     * @param id vk id пользователя
     */
    public static void sendNewTokenToServer(String token, String id) {
        DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference();
        if (id != null) {
            DatabaseReference tokenReference = databaseReference.child(USERS).child(id).child(NOTIFICATION_TOKEN);
            tokenReference.setValue(token);
        }
    }

    /**
     * Метод отправляет новый notification token в базу данных Firebase.
     * Vk id берется из shared preferences. Данный метод нужен для отправки нового токена
     * из сервиса FirebaseIdService, так как там нет возможности быстро получить vk id пользователя.
     *
     * @param token notification token
     */
    public static void sendNewTokenToServer(Context context, String token) {
        DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference();
        String id = getUsersVkIdFromPreferences(context);
        if (id != null) {
            DatabaseReference tokenReference = databaseReference.child(USERS).child(id).child(NOTIFICATION_TOKEN);
            tokenReference.setValue(token);
        }
    }

    public static void saveUsersVkIdToPreferences(Context context, String id) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCES_VK_ID, id);
        editor.apply();
    }

    private static String getUsersVkIdFromPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, AppCompatActivity.MODE_PRIVATE);
        if (preferences.contains(PREFERENCES_VK_ID)){
            return preferences.getString(PREFERENCES_VK_ID, "");
        }
        return null;
    }
}
