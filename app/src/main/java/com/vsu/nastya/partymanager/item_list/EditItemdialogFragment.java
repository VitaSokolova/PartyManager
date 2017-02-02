package com.vsu.nastya.partymanager.item_list;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.guest_list.data.Guest;

/**
 * Created by Вита on 30.01.2017.
 * этот класс отвечает за диалоговое окно с редактированием покупки
 */

public class EditItemDialogFragment extends DialogFragment {

    private OnItemClickListener listener;

    private EditText nameTxtView;
    private AutoCompleteTextView whoBuyTxtView;
    private TextView quantityNumberTxt;
    private TextView priceNumberTxt;
    private SeekBar quantitySeekBar;
    private SeekBar priceSeekBar;

    String whatToBuy;
    Guest whoBuy;
    int quantity;
    int price;

    public interface OnItemClickListener {
        void onItemClick(String whatToBuy, Guest whoBuy, int quantity, int price);
    }

    public static EditItemDialogFragment newInstance(String whatToBuy, Guest whoBuy, int quantity, int price) {
        EditItemDialogFragment frag = new EditItemDialogFragment();
        Bundle args = new Bundle();
        args.putString("whatToBuy", whatToBuy);
        args.putSerializable("whoBuy", whoBuy);
        args.putInt("quantity", quantity);
        args.putInt("price", price);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View main_view = inflater.inflate(R.layout.dialog_item_edit, null);

        initViews(main_view);
        //забираем данные с активити
        initData();
        initQuantitySeekBar();
        initPriceSeekBar();

        showData();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(main_view)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                         listener.onItemClick(nameTxtView.getText().toString(), new Guest(whoBuyTxtView.getText().toString()), quantity, price);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditItemDialogFragment.this.getDialog().cancel();
                    }
                })
                .setTitle(R.string.edit);


        AlertDialog alert = builder.create();


        return alert;
    }

    private void initViews(View main_view) {

        this.nameTxtView = (EditText) main_view.findViewById(R.id.item_name_edtxt);
        this.whoBuyTxtView = (AutoCompleteTextView) main_view.findViewById(R.id.item_who_autocomplete);
        this.quantitySeekBar = (SeekBar) main_view.findViewById(R.id.item_quantity_seekbar);
        this.priceSeekBar = (SeekBar) main_view.findViewById(R.id.item_price_seekbar);
        this.quantityNumberTxt = (TextView) main_view.findViewById(R.id.item_quantity_number_txt);
        this.priceNumberTxt = (TextView) main_view.findViewById(R.id.item_price_number_txt);
    }

    private void initData(){

        this.whatToBuy = getArguments().getString("whatToBuy");
        this.whoBuy = (Guest) getArguments().getSerializable("whoBuy");
        this.quantity = getArguments().getInt("quantity");
        this.price = getArguments().getInt("price");
    }

    private void showData(){
        this.nameTxtView.setText(this.whatToBuy);
        this.quantityNumberTxt.setText(String.valueOf(this.quantity));
        this.priceNumberTxt.setText(String.valueOf(this.price));

        //исправляем баги AutoCompleteTextView советами со Stackoverflow
        whoBuyTxtView.postDelayed(new Runnable() {
            @Override
            public void run() {
                whoBuyTxtView.showDropDown();
            }
        }, 500);
        whoBuyTxtView.setText(this.whoBuy.getGuestName());
        whoBuyTxtView.dismissDropDown();
        whoBuyTxtView.setSelection(whoBuyTxtView.getText().length());
    }

    private void initQuantitySeekBar(){
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

    private void initPriceSeekBar(){
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
