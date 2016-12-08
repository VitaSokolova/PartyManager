package com.vsu.nastya.partymanager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vsu.nastya.partymanager.PartyList.Party;
import com.vsu.nastya.partymanager.Pickers.DatePickerFragment;
import com.vsu.nastya.partymanager.Pickers.TimePickerFragment;

import java.util.Calendar;

public class AddPartyActivity extends AppCompatActivity {

    private Party party;
    private TextView dateText;
    private TextView timeText;

    public static void start(Context context){
        Intent intent = new Intent(context, AddPartyActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_party);
        initView();
    }

    private void initView() {

        dateText = (TextView) findViewById(R.id.addparty_date_text);
        timeText = (TextView) findViewById(R.id.addparty_time_text);

        //создадим новую вечеринку и проинициализируем дату и время текущим временем
        party = new Party();
        Calendar calendar = Calendar.getInstance();
        party.setDateTime(calendar);

        //установим текущее время и дату в текст вью
        dateText.setText(party.getDateAsString());
        timeText.setText(party.getTimeAsString());

        Button showTimePickerButton = (Button) findViewById(R.id.addparty_time_button);
        showTimePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timeFragment = new TimePickerFragment();
                timeFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        Button showDatePickerButton = (Button) findViewById(R.id.addparty_date_button);
        showDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dateFragment = new DatePickerFragment();
                dateFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }
}
