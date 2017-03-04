package com.vsu.nastya.partymanager.guest_list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.logic.User;

import java.util.ArrayList;

/**
 * Created by Вита on 08.12.2016.
 */

public class AddGuestActivity extends AppCompatActivity {

    private Button addBtn;
    private AutoCompleteTextView autoCompleteTextView;

    private Guest newGuest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_guest);

        this.addBtn = (Button) findViewById(R.id.add_guest_btn);
        this.autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.add_guest_autocomplete_txt);
        // TODO: Надо получить массив строк из списка друзей вконтакте для автозаполнения

        // Создаем адаптер для автозаполнения элемента AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, getVkFriendsArray());
        autoCompleteTextView.setAdapter(adapter);

        this.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTextFieldEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.alertGuestNameIsEmpty, Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    // этого гостя мы потом правильно создадим по указанным полям
                    newGuest = new Guest(getTextFromField());
                    Intent intent = new Intent();
                    intent.putExtra("guest", newGuest);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private String getTextFromField() {
        return this.autoCompleteTextView.getText().toString();
    }

    private boolean isTextFieldEmpty() {
        return this.autoCompleteTextView.getText().toString().isEmpty();
    }
    private String[] getVkFriendsArray(){
        User user = User.getInstance();
        ArrayList arrayListFriends = user.getFriendsList();
        String[] arrayFriends = new String[arrayListFriends.size()];

        for (int i = 0; i < arrayListFriends.size(); i++) {
            arrayFriends[i] = arrayListFriends.get(i).toString();
        }
        return  arrayFriends;
    }
}

