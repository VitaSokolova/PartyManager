package com.vsu.nastya.partymanager.item_list;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.logic.User;

/**
 * Created by Вита on 30.01.2017.
 * этот класс отвечает за диалоговое окно с редактированием покупки
 */

public class EditItemDialogFragment extends DialogFragment {

    private OnItemClickListener listener;

    private EditText nameTxtView;
    private AutoCompleteTextView whoBuyAutoCompleteView;
    private TextView quantityNumberTxt;
    private TextView priceNumberTxt;
    private SeekBar quantitySeekBar;
    private SeekBar priceSeekBar;

    private String[] currentPartyGuests;
    private String whatToBuy;
    private Guest whoBuy;
    private int quantity;
    private int price;

    public interface OnItemClickListener {
        void onItemClick(String whatToBuy, String whoBuy, int quantity, int price);
    }

    public static EditItemDialogFragment newInstance(String whatToBuy, Guest whoBuy, int quantity, int price, String[] currentPartyGuests) {
        EditItemDialogFragment frag = new EditItemDialogFragment();
        Bundle args = new Bundle();
        args.putString("whatToBuy", whatToBuy);
        args.putSerializable("whoBuy", whoBuy);
        args.putInt("quantity", quantity);
        args.putInt("price", price);
        args.putStringArray("currentPartyGuests", currentPartyGuests);
        frag.setArguments(args);
        frag.setCancelable(false);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View main_view = inflater.inflate(R.layout.dialog_item_edit, null);

        initViews(main_view);
        //забираем данные с активити
        initData();
        initQuantitySeekBar();
        initPriceSeekBar();
        showData();


        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(main_view)
                .setTitle(R.string.edit)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button buttonOk = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                buttonOk.setOnClickListener(view -> {

                    //я вполне допускаю, что price может быть == 0. Например вы вносите в список клубничку, которую сорвете у себя на даче,
                    // вам надо не забыть её сорвать, а следовательно внести список, но стоимость её будет == 0
                    if (nameTxtView.getText().toString().isEmpty() || (whoBuyAutoCompleteView.getText().toString().isEmpty()) || (quantity == 0)) {
                        Toast toast = Toast.makeText(getContext(),
                                R.string.alertSomeFieldsAreEmpty, Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        String name = whoBuyAutoCompleteView.getText().toString();
                        listener.onItemClick(nameTxtView.getText().toString(), name, quantity, price);
                        //если всё хорошо, то можно закрывать диалог
                        dialog.dismiss();
                    }
                });

                Button buttonCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonCancel.setOnClickListener(view -> EditItemDialogFragment.this.getDialog().cancel());
            }
        });

        return dialog;
    }

    //отыскиваем все вьюшки, задействованные в коде
    private void initViews(View main_view) {
        this.nameTxtView = (EditText) main_view.findViewById(R.id.item_name_edtxt);
        this.whoBuyAutoCompleteView = (AutoCompleteTextView) main_view.findViewById(R.id.item_who_autocomplete);
        this.quantitySeekBar = (SeekBar) main_view.findViewById(R.id.item_quantity_seekbar);
        this.priceSeekBar = (SeekBar) main_view.findViewById(R.id.item_price_seekbar);
        this.quantityNumberTxt = (TextView) main_view.findViewById(R.id.item_quantity_number_txt);
        this.priceNumberTxt = (TextView) main_view.findViewById(R.id.item_price_number_txt);
    }

    //заполняем поля теми данными, что переданы при вызове диалога
    private void initData() {
        this.currentPartyGuests = getArguments().getStringArray("currentPartyGuests");
        this.whatToBuy = getArguments().getString("whatToBuy");
        this.whoBuy = (Guest) getArguments().getSerializable("whoBuy");
        this.quantity = getArguments().getInt("quantity");
        this.price = getArguments().getInt("price");
    }

    //заполняем все вью данными из полей
    private void showData() {
        this.nameTxtView.setText(this.whatToBuy);
        this.quantityNumberTxt.setText(String.valueOf(this.quantity));
        this.priceNumberTxt.setText(String.valueOf(this.price));
        // Создаем адаптер для автозаполнения элемента AutoCompleteTextView
        User user = User.getInstance();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, this.currentPartyGuests);
        whoBuyAutoCompleteView.setAdapter(adapter);

        //исправляем баги AutoCompleteTextView советами со Stackoverflow
        whoBuyAutoCompleteView.postDelayed(() -> whoBuyAutoCompleteView.showDropDown(), 500);
        whoBuyAutoCompleteView.setText(this.whoBuy.getGuestName());
        whoBuyAutoCompleteView.dismissDropDown();
        whoBuyAutoCompleteView.setSelection(whoBuyAutoCompleteView.getText().length());
    }

    //этот метод инициализирует SeekBar, отвечающий за количество
    private void initQuantitySeekBar() {
        quantitySeekBar.setProgress(quantity);
        quantitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                quantityNumberTxt.setText(String.valueOf(progressValue));
                quantity = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    //этот метод инициализирует SeekBar, отвечающий за цену
    private void initPriceSeekBar() {
        priceSeekBar.setProgress(price);
        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                //чтобы не утомлять пользоватьля, введём величину шага ползунка 50
                progressValue = progressValue / 50;
                progressValue = progressValue * 50;
                priceNumberTxt.setText(String.valueOf(progressValue));
                price = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void setListener(EditItemDialogFragment.OnItemClickListener listener) {
        this.listener = listener;
    }
}
