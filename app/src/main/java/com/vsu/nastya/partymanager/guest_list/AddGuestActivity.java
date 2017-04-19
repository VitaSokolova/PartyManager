package com.vsu.nastya.partymanager.guest_list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.logic.Friend;
import com.vsu.nastya.partymanager.logic.User;
import com.vsu.nastya.partymanager.logic.VkFriendsWorker;

import java.util.ArrayList;

/**
 * Created by Вита on 08.12.2016.
 */

public class AddGuestActivity extends AppCompatActivity implements TextView.OnEditorActionListener {

    private Button addBtn;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayList<Friend> arrayListFriends;
    private Guest newGuest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_guest);
        //список друзей из контакта для автодополнения
        this.arrayListFriends = User.getInstance().getFriendsList();
        initViews();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            // обрабатываем нажатие кнопки
            addingEvent();
            return true;
        }
        return false;
    }

    public void addingEvent() {
        if (isTextFieldEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.alertGuestNameIsEmpty, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            String name = getTextFromField();
            newGuest = new Guest(VkFriendsWorker.getVkFriendIdByName(name, arrayListFriends), name);
            Intent intent = new Intent();
            intent.putExtra("guest", newGuest);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private String getTextFromField() {
        return this.autoCompleteTextView.getText().toString();
    }

    private boolean isTextFieldEmpty() {
        return this.autoCompleteTextView.getText().toString().isEmpty();
    }

    private void initViews() {
        this.addBtn = (Button) findViewById(R.id.add_guest_btn);
        this.autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.add_guest_autocomplete_txt);
        this.autoCompleteTextView.setOnEditorActionListener(this);

        // Создаем адаптер для автозаполнения элемента AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, VkFriendsWorker.getVkFriendsArray(this.arrayListFriends));
        autoCompleteTextView.setAdapter(adapter);

        this.addBtn.setOnClickListener(view -> addingEvent());

    }
}

