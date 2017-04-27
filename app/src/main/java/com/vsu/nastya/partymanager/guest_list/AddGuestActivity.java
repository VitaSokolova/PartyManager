package com.vsu.nastya.partymanager.guest_list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
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

import static com.vsu.nastya.partymanager.guest_list.GuestListFragment.GUEST_EXTRA;

/**
 * Created by Вита on 08.12.2016.
 * Экран для добавления нового гостя
 */

public class AddGuestActivity extends AppCompatActivity implements TextView.OnEditorActionListener {

    private AutoCompleteTextView autoCompleteTextView;
    private ArrayList<Friend> arrayListFriends;

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
            Guest guest = VkFriendsWorker.getVkFriendIdByName(name, arrayListFriends);
            if (guest.getVkId() == null) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.alertHisIsNotYourFriend, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Intent intent = new Intent();
                intent.putExtra(GUEST_EXTRA, guest);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    private String getTextFromField() {
        return this.autoCompleteTextView.getText().toString();
    }

    private boolean isTextFieldEmpty() {
        return this.autoCompleteTextView.getText().toString().isEmpty();
    }

    private void initViews() {
        Button addBtn = (Button) findViewById(R.id.add_guest_btn);
        this.autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.add_guest_autocomplete_txt);
        this.autoCompleteTextView.setOnEditorActionListener(this);

        // Создаем адаптер для автозаполнения элемента AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, VkFriendsWorker.getVkFriendsArray(this.arrayListFriends));
        autoCompleteTextView.setAdapter(adapter);

        addBtn.setOnClickListener(view -> addingEvent());

    }
}

