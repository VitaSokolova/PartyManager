package com.vsu.nastya.partymanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.vsu.nastya.partymanager.GuestList.Guest;

/**
 * Created by Вита on 08.12.2016.
 */

public class AddGuestActivity extends AppCompatActivity {
    private Button addBtn;
    private Guest newGuest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_guest);

        this.addBtn = (Button) findViewById(R.id.add_guest_btn);
        this.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // этого гостя мы потом правильно создадим по указанным полям
                newGuest = new Guest("Вита");
                Intent intent = new Intent();
                intent.putExtra("guest",newGuest);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
