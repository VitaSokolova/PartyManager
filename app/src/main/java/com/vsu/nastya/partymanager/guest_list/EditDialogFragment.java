package com.vsu.nastya.partymanager.guest_list;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.LayoutInflater;
import android.widget.AutoCompleteTextView;

import com.vsu.nastya.partymanager.R;

/**
 * Created by Вита on 25.01.2017.
 * этот класс отвечает за диалоговое окно с редактированием имени гостя
 */

public class EditDialogFragment extends DialogFragment {

    public interface OnItemClickListener {
        void onItemClick(String text);
    }

    private OnItemClickListener listener;

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_guest_edit, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AutoCompleteTextView txtView = (AutoCompleteTextView) getView().findViewById(R.id.guest_list_edit_etxt);
                        listener.onItemClick(txtView.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}
