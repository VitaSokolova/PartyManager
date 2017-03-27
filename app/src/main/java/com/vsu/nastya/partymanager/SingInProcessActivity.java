package com.vsu.nastya.partymanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vsu.nastya.partymanager.logic.Friend;
import com.vsu.nastya.partymanager.logic.User;
import com.vsu.nastya.partymanager.party_list.PartyListActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SingInProcessActivity extends AppCompatActivity {

    private static final String VK_ERROR = "vk_error";
    private static final String FIREBASE_ERROR = "firebase_error";
    private DatabaseReference usersReference;

    public static void start(Context context) {
        Intent intent = new Intent(context, SingInProcessActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_process);

        usersReference = FirebaseDatabase.getInstance().getReference().child("users");
        onSignIn(VKAccessToken.currentToken());
    }

    /**
     * В данном методе происходит занесение нового пользователя в базу данных firebase или
     * получение данных о пользователе из базы, если он уже там существует.
     *
     * @param token - приходит от VK при успешной авторизации пользователя.
     */
    private void onSignIn(final VKAccessToken token) {
        DatabaseReference userRef = usersReference.child(token.userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    getUserFromDatabase(token.userId);
                } else {
                    createUser(token);
                }
                getFriendsList(token);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обычно вызывается, когда нет прав на чтение данных из базы
                Log.d(FIREBASE_ERROR, "onCancelled: " + databaseError);
            }
        });
    }

    /**
     * Получение информации из базы о юзере по его vk id.
     *
     * @param id - vk id.
     */
    private void getUserFromDatabase(String id) {
        DatabaseReference userRef = usersReference.child(id);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                User.getInstance().init (user.getFirstName(),
                        user.getLastName(),
                        user.getVkId(),
                        user.getPartiesIdList());
                PartyListActivity.start(SingInProcessActivity.this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обычно вызывается, когда нет прав на чтение данных из базы
                Log.d(FIREBASE_ERROR, "onCancelled: " + databaseError);
            }
        });
    }

    /**
     * Создание нового пользователя.
     * Данные о пользователе запрашиваются у vk (id, first_name, last_name).
     * Создается объект User. Затем информация заносится в базу.
     *
     * @param token - приходит от VK при успешной авторизации пользователя.
     */
    private void createUser(final VKAccessToken token) {
        final User user = User.getInstance();
        VKParameters vkParameters = new VKParameters();
        vkParameters.put(VKApiConst.FIELDS, "id, first_name, last_name");
        vkParameters.put(VKApiConst.USER_IDS, token.userId);
        vkParameters.put(VKApiConst.NAME_CASE, "nom");

        VKRequest request = new VKRequest("users.get", vkParameters);

        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                try {
                    JSONObject json = (JSONObject) ((JSONArray) response.json.get("response")).get(0);
                    user.setFirstName((String) json.get("first_name"));
                    user.setLastName((String) json.get("last_name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                user.setVkId(token.userId);
                user.setToken(token);
                usersReference.child(user.getVkId()).setValue(user);
                PartyListActivity.start(SingInProcessActivity.this);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d(VK_ERROR, "onError: " + error);
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d(VK_ERROR, "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }
        });
    }

    private void getFriendsList(final VKAccessToken token) {

        VKParameters vkParameters = new VKParameters();
        vkParameters.put(VKApiConst.FIELDS, "id, first_name, last_name");
        vkParameters.put(VKApiConst.USER_ID, token.userId);
        vkParameters.put(VKApiConst.NAME_CASE, "nom");
        VKRequest request = new VKRequest("friends.get", vkParameters);

        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                try {
                    User user = User.getInstance();
                    JSONObject root = (JSONObject)  response.json.get("response");
                    JSONArray friends = root.getJSONArray("items");

                    for (int i = 0; i < friends.length(); i++) {
                        Friend friend = new Friend();
                        JSONObject properties = friends.getJSONObject(i);
                        friend.setVkId(String.valueOf(properties.get("id")));
                        friend.setFirstName((String) properties.get("first_name"));
                        friend.setLastName((String) properties.get("last_name"));
                        user.addFriend(friend);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // usersReference.child(user.getVkId()).setValue(???);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d(VK_ERROR, "onError: " + error);
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d(VK_ERROR, "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }
        });
    }
}
