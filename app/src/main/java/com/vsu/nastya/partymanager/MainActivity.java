package com.vsu.nastya.partymanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vsu.nastya.partymanager.logic.User;
import com.vsu.nastya.partymanager.party_list.PartyListActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Экран со списком всех вечеринок
 */
public class MainActivity extends AppCompatActivity {

    private static final String[] myScope = new String[]{
            VKScope.FRIENDS,
    };

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private DatabaseReference databaseReference;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        usersReference = databaseReference.child("users");
    }

    @Override
    public void onStart() {
        super.onStart();
        Button loginButton = (Button) findViewById(R.id.loginActv_login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VKSdk.login(MainActivity.this, myScope);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken token) {
                // Пользователь успешно авторизовался

                if (ifUserExists(token.userId)) {
                    //Заполнить user данными из базы
                    getUserFromDatabase(token.userId); //пока не работает как надо
                } else {
                    createUser(token);
                }
                Toast toast = Toast.makeText(MainActivity.this, "Успешная авторизация", Toast.LENGTH_SHORT);
                toast.show();

                PartyListActivity.start(MainActivity.this);
            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                Toast toast = Toast.makeText(MainActivity.this, "Ошибка: " + error, Toast.LENGTH_SHORT);
                toast.show();
            }
        }));
    }

    private boolean ifUserExists(String id) {
        DatabaseReference userRef = usersReference.child(id);
        final boolean[] exists = new boolean[1];
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                exists[0] = snapshot.exists();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //как то обработать
            }
        });
        return exists[0];
    }

    private void getUserFromDatabase(String id) {
        DatabaseReference userRef = usersReference.child(id);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //как то обработать
            }
        });
    }

    private void createUser(final VKAccessToken token) {
        user = new User();
        VKParameters vkParameters = new VKParameters();
        vkParameters.put(VKApiConst.FIELDS, "id, first_name, last_name");
        vkParameters.put(VKApiConst.USER_IDS, token.userId);
        vkParameters.put(VKApiConst.NAME_CASE, "nom");
        VKRequest request = new VKRequest("users.get", vkParameters);

        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {

                try {
                    JSONObject json = (JSONObject)((JSONArray) response.json.get("response")).get(0);
                    user.setFirstName((String) json.get("first_name"));
                    user.setLastName((String) json.get("last_name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                user.setVkId(token.userId);
                user.setToken(token);
                usersReference.child(user.getVkId()).setValue(user);
            }
            @Override
            public void onError(VKError error) {
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                //как то обработать
            }
        });
    }
}
