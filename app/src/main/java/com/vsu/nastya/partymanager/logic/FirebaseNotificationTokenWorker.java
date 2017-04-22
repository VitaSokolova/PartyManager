package com.vsu.nastya.partymanager.logic;

import android.content.Context;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import static com.vsu.nastya.partymanager.logic.DatabaseConsts.*;

/**
 * Класс предназначен для работы с Firebase notification token, предназначенный для
 * отправки оповещений на конкретные устройства.
 */

public abstract class FirebaseNotificationTokenWorker {

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
        String id = SharedPreferencesWorker.getUsersVkIdFromPreferences(context);
        if (id != null) {
            DatabaseReference tokenReference = databaseReference.child(USERS).child(id).child(NOTIFICATION_TOKEN);
            tokenReference.setValue(token);
        }
    }
}
