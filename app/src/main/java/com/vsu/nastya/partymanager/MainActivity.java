package com.vsu.nastya.partymanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vsu.nastya.partymanager.logic.Network;

/**
 * Экран со списком всех вечеринок
 */
public class MainActivity extends AppCompatActivity {

    private static final String VK_ERROR = "vk_error";

    private static final String[] myScope = new String[]{
            VKScope.FRIENDS,
    };

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (VKSdk.isLoggedIn()) {
            SingInProcessActivity.start(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!Network.networkIsAvailable(this)) {
            Toast.makeText(this, getResources().getString(R.string.network_is_not_available), Toast.LENGTH_LONG).show();
        }

        Button loginButton = (Button) findViewById(R.id.loginActv_login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VKSdk.login(MainActivity.this, myScope);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken token) {
                // Пользователь успешно авторизовался
                SingInProcessActivity.start(MainActivity.this);
            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                Toast toast = Toast.makeText(MainActivity.this, "Ошибка: " + error, Toast.LENGTH_SHORT);
                toast.show();
                Log.d(VK_ERROR, "onError: " + error);
            }
        });
    }
}
