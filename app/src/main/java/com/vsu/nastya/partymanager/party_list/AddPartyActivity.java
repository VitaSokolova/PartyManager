package com.vsu.nastya.partymanager.party_list;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.logic.DateWorker;
import com.vsu.nastya.partymanager.pickers.DatePickerFragment;
import com.vsu.nastya.partymanager.pickers.TimePickerFragment;

import java.util.Calendar;

import static com.vsu.nastya.partymanager.party_list.PartyListActivity.ICON_EXTRA;
import static com.vsu.nastya.partymanager.party_list.PartyListActivity.PARTY_EXTRA;
import static com.vsu.nastya.partymanager.party_list.PartyListActivity.POSITION_EXTRA;

/**
 * Окно для добавления новой вечеринки
 */
public class AddPartyActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
        Toolbar.OnMenuItemClickListener, View.OnClickListener{

    private static final String TIME_PICKER = "timePicker";
    private static final String DATE_PICKER = "datePicker";
    private static final int PHOTO_PICKER = 1;

    private Uri iconUri;
    private int position;

    private ImageView partyBigPhotoImageView;
    private EditText partyName;
    private TextView dateText;
    private TextView timeText;
    private Calendar calendar;
    private FloatingActionButton addPartyPhotoButton;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            partyBigPhotoImageView.setImageURI(selectedImageUri);
            iconUri = selectedImageUri;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_party_photo_button: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), PHOTO_PICKER);
                break;
            }
            case R.id.add_party_time_tv: {
                DialogFragment timeFragment = TimePickerFragment.newInstance(calendar);
                timeFragment.show(getSupportFragmentManager(), TIME_PICKER);
                break;
            }
            case R.id.add_party_date_tv: {
                DialogFragment dateFragment = DatePickerFragment.newInstance(calendar);
                dateFragment.show(getSupportFragmentManager(), DATE_PICKER);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_party_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.save_menu_item) {
            if (!String.valueOf(partyName.getText()).equals("")) {
                Party party = new Party(partyName.getText().toString(), calendar.getTimeInMillis());
                Intent intent = new Intent();
                intent.putExtra(PARTY_EXTRA, party);
                intent.putExtra(POSITION_EXTRA, position);
                if (iconUri != null) {
                    intent.putExtra(ICON_EXTRA, iconUri);
                }
                setResult(RESULT_OK, intent);
                finish();
                return true;
            } else {
                Toast.makeText(this, getResources().getString(R.string.enter_party_name), Toast.LENGTH_LONG).show();
            }
        }
        return false;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_student_toolbar);
        toolbar.inflateMenu(R.menu.add_party_menu);

        partyName = (EditText) findViewById(R.id.add_party_name_tv);
        partyBigPhotoImageView = (ImageView) findViewById(R.id.add_party_big_photo);
        addPartyPhotoButton = (FloatingActionButton) findViewById(R.id.add_party_photo_button);
        dateText = (TextView) findViewById(R.id.add_party_date_tv);
        timeText = (TextView) findViewById(R.id.add_party_time_tv);

        toolbar.setOnMenuItemClickListener(this);
        addPartyPhotoButton.setOnClickListener(this);
        dateText.setOnClickListener(this);
        timeText.setOnClickListener(this);

        setValuesToViews();
    }

    private void setValuesToViews() {
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
           /* Если надо поменять информацию о существующей вечеринке,
            заполняем поля старой информацией о вечеринке */
            Party party = (Party) intent.getSerializableExtra(PARTY_EXTRA);
            position = intent.getIntExtra(POSITION_EXTRA, 0);
            calendar = DateWorker.getCalendarFromMilliseconds(party.getDate());
            partyName.setText(party.getName());
            String iconUrl = party.getIcon();
            if (iconUrl != null) {
                Glide.with(partyBigPhotoImageView.getContext())
                        .load(iconUrl)
                        .into(partyBigPhotoImageView);
            }
        } else {
            /* Если создаем новую вечеринку, то устанавливаем текущее время и дату.
             Поле с названием вечеринки оставляем пустым */
            calendar = Calendar.getInstance();
            position = -1;
        }

        dateText.setText(DateWorker.getDateAsString(calendar));
        timeText.setText(DateWorker.getTimeAsString(calendar));
    }
}