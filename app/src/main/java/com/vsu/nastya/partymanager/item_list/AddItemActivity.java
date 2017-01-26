package com.vsu.nastya.partymanager.item_list;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.R;

/**
 * Created by Вита on 01.12.2016.
 */

public class AddItemActivity extends AppCompatActivity {

    private EditText nameEditTxt;
    private AutoCompleteTextView whoBuyEditTxt;
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

        nameEditTxt = (EditText) findViewById(R.id.add_item_name_edtxt);
        whoBuyEditTxt = (AutoCompleteTextView) findViewById(R.id.add_item_who_autocomplete);
        //TODO: надо закинуть в список для автодополнения гостей текущей вечеринки.

        addItemButton = (Button) findViewById(R.id.add_item_btn);

        quantitySeekBar = (SeekBar) findViewById(R.id.add_item_quantity_seekbar);
        quantityTxt = (TextView) findViewById(R.id.add_item_quantity_progress_txt);

        averagePriceSeekBar = (SeekBar) findViewById(R.id.add_item_price_seekbar);
        averagePriceTxt = (TextView) findViewById(R.id.add_item_price_progress_txt);

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Item newItem = getItemFromFields();
                Intent intent = new Intent();
                intent.putExtra("item", newItem);
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

    private Item getItemFromFields() {

        String itemName = String.valueOf(this.nameEditTxt.getText());
        String who = String.valueOf(this.whoBuyEditTxt.getText());
        Guest guest = new Guest(who);

        Item item = new Item(itemName, quantityNumber, guest, priceNumber);

        return item;
    }
}
