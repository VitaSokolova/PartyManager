package com.vsu.nastya.partymanager.services;

import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.vsu.nastya.partymanager.logic.FirebaseNotificationTokenWorker;

/**
 * Сервис срабатывает, когда у пользователя обновляется notification token.
 */

public class FirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        Log.d("token refresh", "here!");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseNotificationTokenWorker.sendNewTokenToServer(this, refreshedToken);
    }

}
