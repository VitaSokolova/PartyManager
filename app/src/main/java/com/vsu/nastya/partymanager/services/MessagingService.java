package com.vsu.nastya.partymanager.services;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vsu.nastya.partymanager.logic.Notifications;
import com.vsu.nastya.partymanager.party_list.Party;

import java.util.Map;

import static com.vsu.nastya.partymanager.logic.DatabaseConsts.PARTIES;
import static com.vsu.nastya.partymanager.logic.ErrorsConstants.FIREBASE_ERROR;

/**
 * Сервис для обработки, полученных от Firebase сообщений (FCM).
 * Данные приходят сюда и обрабатываются, даже если приложение находится в бэкграунде.
 */

public class MessagingService extends FirebaseMessagingService {

    private static final String NEW_GUEST = "new guest";
    private static final String NEW_ITEM = "new item";
    private static final String NEW_PLACE = "new place";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Map<String, String> data =  remoteMessage.getData();
        sendNotification(data);
    }

    private void sendNotification(Map<String, String> data) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference partyReference = databaseReference.child(PARTIES).child(data.get("party_id"));
        partyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Party party = dataSnapshot.getValue(Party.class);
                String type = data.get("type");
                switch (type) {
                    case NEW_GUEST: {
                        Notifications.newGuestAdded(MessagingService.this, party);
                        break;
                    }
                    case NEW_ITEM: {
                        Notifications.newItemAdded(MessagingService.this, party);
                        break;
                    }
                    case NEW_PLACE: {
                        Notifications.newLocationSet(MessagingService.this, party);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обычно вызывается, когда нет прав на чтение данных из базы
                Log.d(FIREBASE_ERROR, "onCancelled: " + databaseError);
            }
        });
    }
}
