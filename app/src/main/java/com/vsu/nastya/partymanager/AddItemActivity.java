package com.vsu.nastya.partymanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vsu.nastya.partymanager.ItemList.Item;
import com.vsu.nastya.partymanager.GuestList.Guest;
/**
 * Created by Вита on 01.12.2016.
 */

public class AddItemActivity extends AppCompatActivity {

    private EditText nameEditTxt;
    private EditText whoBuyEditTxt;
    private SeekBar quantitySeekBar;
    private SeekBar averagePriceSeekBar;
    private TextView quantityTxt;
    private TextView averagePriceTxt;
    private Button addItemButton;

    public int quantityNumber;
    private int priceNumber;

    public static void start(Context context) {
        Intent intent = new Intent(context, AddItemActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        InitView();
    }

    private void InitView() {

        nameEditTxt = (EditText) findViewById(R.id.add_item_name_edtxt);
        whoBuyEditTxt = (EditText) findViewById(R.id.add_item_who_edtxt);

        addItemButton = (Button) findViewById(R.id.add_item_btn);

        quantitySeekBar = (SeekBar) findViewById(R.id.add_item_quantity_seekbar);
        quantityTxt = (TextView) findViewById(R.id.add_item_quantity_progress_txt);

        averagePriceSeekBar = (SeekBar) findViewById(R.id.add_item_price_seekbar);
        averagePriceTxt = (TextView) findViewById(R.id.add_item_price_progress_txt);

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Item newItem = GetItemFromFields();
                Intent intent = new Intent();
                intent.putExtra("newItem", newItem);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        // инициализируем значения текущей позицией прогресса SeekBar
        quantityNumber = 0;
        priceNumber = 0;



        //получаем подписи из строковых ресурсов
        final String quantityText = getString(R.string.quantity);
        final String priceText = getString(R.string.averageCost);


        quantityTxt.setText(quantityText + String.valueOf(quantityNumber));
        averagePriceTxt.setText(priceText + String.valueOf(priceNumber));

        // устанавливаем слушателей для прокрутку SeekBar
        quantitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // метод отвечает за изменения прогресса
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                quantityTxt.setText(quantityText + String.valueOf(progressValue));
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
                averagePriceTxt.setText(priceText + String.valueOf(progressValue));
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

    private Item GetItemFromFields() {

        String itemName = String.valueOf(this.nameEditTxt.getText());
        String who = String.valueOf(this.whoBuyEditTxt.getText());
        Guest guest = new Guest(who);
        int quantity = (int) Integer.valueOf(this.quantityTxt.getText().toString());
        int price = (int) Integer.valueOf(this.averagePriceTxt.getText().toString());

        Item item = new Item(itemName, quantity, guest, price);

        return item;
    }
}
