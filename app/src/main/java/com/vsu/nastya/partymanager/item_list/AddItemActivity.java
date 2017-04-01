package com.vsu.nastya.partymanager.item_list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.item_list.data.Item;
import com.vsu.nastya.partymanager.logic.Friend;
import com.vsu.nastya.partymanager.logic.User;
import com.vsu.nastya.partymanager.logic.VkFriendsWorker;

import java.util.ArrayList;

/**
 * Created by Вита on 01.12.2016.
 */

public class AddItemActivity extends AppCompatActivity {

    private ArrayList<Friend> arrayListFriends;

    private EditText nameEditTxt;
    private AutoCompleteTextView whoBuyAutoCompleteTxt;
    private SeekBar quantitySeekBar;
    private SeekBar averagePriceSeekBar;
    private TextView quantityTxt;
    private TextView averagePriceTxt;
    private Button addItemButton;

    private int quantityNumber;
    private int priceNumber;

    public static void start(Context context) {
        Intent intent = new Intent(context, AddItemActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        initView();
    }

    private void initView() {
        arrayListFriends = User.getInstance().getFriendsList();
        nameEditTxt = (EditText) findViewById(R.id.item_name_edtxt);
        whoBuyAutoCompleteTxt = (AutoCompleteTextView) findViewById(R.id.item_who_autocomplete);
        // Создаем адаптер для автозаполнения элемента AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, VkFriendsWorker.getVkFriendsArray(arrayListFriends));
        whoBuyAutoCompleteTxt.setAdapter(adapter);

        addItemButton = (Button) findViewById(R.id.add_item_btn);

        quantitySeekBar = (SeekBar) findViewById(R.id.item_quantity_seekbar);
        quantityTxt = (TextView) findViewById(R.id.item_quantity_number_txt);

        averagePriceSeekBar = (SeekBar) findViewById(R.id.item_price_seekbar);
        averagePriceTxt = (TextView) findViewById(R.id.item_price_number_txt);

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Item newItem = getItemFromFields();
                //я вполне допускаю, что price может быть == 0. Например вы вносите в список клубничку, которую сорвете у себя на даче,
                // вам надо не забыть её сорвать, а следовательно внести список, но стоимость её будет == 0
                if (newItem.getName().isEmpty() || (newItem.getWhoBrings().getGuestName().isEmpty()) || (newItem.getQuantity() == 0)) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.alertSomeFieldsAreEmpty, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("item", newItem);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        // инициализируем значения текущей позицией прогресса SeekBar
        quantityNumber = 0;
        priceNumber = 0;


        quantityTxt.setText(String.valueOf(quantityNumber));
        averagePriceTxt.setText(String.valueOf(priceNumber));

        // устанавливаем слушателей для прокрутку SeekBar
        quantitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // метод отвечает за изменения прогресса
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                quantityTxt.setText(String.valueOf(progressValue));
                quantityNumber = progressValue;
            }

            // никак не реагируем на начало движения индикатора прогресса
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // никак не реагируем, когда пользователь отпустил индикатор изменения прогресса
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        averagePriceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                //чтобы не утомлять пользоватьля, введём величину шага ползунка 50
                progressValue = progressValue / 50;
                progressValue = progressValue * 50;
                averagePriceTxt.setText(String.valueOf(progressValue));
                priceNumber = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private Item getItemFromFields() {

        String itemName = String.valueOf(this.nameEditTxt.getText());
        String who = String.valueOf(this.whoBuyAutoCompleteTxt.getText());
        Guest guest = new Guest(VkFriendsWorker.getVkFriendIdByName(who, arrayListFriends), who);

        Item item = new Item(itemName, quantityNumber, guest, priceNumber);

        return item;
    }
}
