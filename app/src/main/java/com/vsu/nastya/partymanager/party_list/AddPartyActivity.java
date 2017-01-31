package com.vsu.nastya.partymanager.party_list;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.logic.DateWorker;
import com.vsu.nastya.partymanager.pickers.DatePickerFragment;
import com.vsu.nastya.partymanager.pickers.TimePickerFragment;

import java.util.Calendar;

/**
 * Окно для добавления новой вечеринки
 */
public class AddPartyActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    private TextView dateText;
    private TextView timeText;
    private Calendar calendar;

    public static void startForResult(Context context, int requestCode){
        Intent intent = new Intent(context, AddPartyActivity.class);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_party);
        initView();
    }

    /**
     * Метод вызывается, когда пользователь выбрал дату в календаре.
     * В объект calendar записывается выбранная дата.
     */
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        dateText.setText(DateWorker.getDateAsString(calendar));
    }

    /**
     * Метод вызывается, когда пользователь выбрал время.
     * В объект calendar записывается выбранное время.
     */
    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        timeText.setText(DateWorker.getTimeAsString(calendar));
    }

    private void initView() {

        dateText = (TextView) findViewById(R.id.addparty_date_text);
        timeText = (TextView) findViewById(R.id.addparty_time_text);
        final EditText partyName = (EditText) findViewById(R.id.addparty_name_edit);

        Intent intent = getIntent();
        final int pos; // Если меняем старую информацию о вечеринке, то это ее позиция в списке

        if (intent.getExtras() != null) {
           /* Если надо поменять информацию о существующей вечеринке,
            заполняем поля старой информацией о вечеринке */
            Party party = (Party) intent.getSerializableExtra("party");
            calendar = party.getDate();
            partyName.setText(party.getName());
            pos = intent.getIntExtra("position", 0);
        } else {
            /* Если создаем новую вечеринку, то устанавливаем текущее время и дату.
             Поле с названием вечеринки оставляем пустым */
            calendar = Calendar.getInstance();
            pos = -1;
        }

        dateText.setText(DateWorker.getDateAsString(calendar));
        timeText.setText(DateWorker.getTimeAsString(calendar));

        ImageButton showTimePickerButton = (ImageButton) findViewById(R.id.addparty_time_button);
        showTimePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timeFragment = TimePickerFragment.newInstance(calendar);
                timeFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        ImageButton showDatePickerButton = (ImageButton) findViewById(R.id.addparty_date_button);
        showDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dateFragment = DatePickerFragment.newInstance(calendar);
                dateFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        Button okButton = (Button) findViewById(R.id.addparty_ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!partyName.getText().toString().equals("")) {
                    //Заполняем информацию о вечеринке
                    Party party = new Party(partyName.getText().toString(), calendar);
                    Intent intent = new Intent();
                    intent.putExtra("party", party);
                    intent.putExtra("position", pos);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else {
                    Toast toast = Toast.makeText(AddPartyActivity.this, getResources().getString(R.string.enter_party_name), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }
}
