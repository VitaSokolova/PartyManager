package com.vsu.nastya.partymanager;

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

import com.vsu.nastya.partymanager.Logic.DateWorker;
import com.vsu.nastya.partymanager.PartyList.Party;
import com.vsu.nastya.partymanager.Pickers.DatePickerFragment;
import com.vsu.nastya.partymanager.Pickers.TimePickerFragment;

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

        //установим текущее время и дату в текст вью
        calendar = Calendar.getInstance();
        dateText.setText(DateWorker.getDateAsString(calendar));
        timeText.setText(DateWorker.getTimeAsString(calendar));

        ImageButton showTimePickerButton = (ImageButton) findViewById(R.id.addparty_time_button);
        showTimePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timeFragment = new TimePickerFragment();
                timeFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        ImageButton showDatePickerButton = (ImageButton) findViewById(R.id.addparty_date_button);
        showDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dateFragment = new DatePickerFragment();
                dateFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        final EditText partyName = (EditText) findViewById(R.id.addparty_name_edit);
        Button okButton = (Button) findViewById(R.id.addparty_ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!partyName.getText().toString().equals("")) {
                    //Создаем новую вечеринку и закрываем активити
                    Party party = new Party(partyName.getText().toString(), calendar);
                    Intent intent = new Intent();
                    intent.putExtra("party", party);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else {
                    Toast toast = Toast.makeText(AddPartyActivity.this, "Введите название вечеринки", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }
}
