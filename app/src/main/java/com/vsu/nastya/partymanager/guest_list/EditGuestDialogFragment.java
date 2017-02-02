package com.vsu.nastya.partymanager.guest_list;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.vsu.nastya.partymanager.R;

/**
 * Created by Вита on 25.01.2017.
 * этот класс отвечает за диалоговое окно с редактированием имени гостя
 */

public class EditGuestDialogFragment extends DialogFragment {

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String text);
    }

    public static EditGuestDialogFragment newInstance(String name) {
        EditGuestDialogFragment frag = new EditGuestDialogFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //забираем имя с активити
        String name = getArguments().getString("name");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View main_view = inflater.inflate(R.layout.dialog_guest_edit, null);
        final AutoCompleteTextView txtView = (AutoCompleteTextView) main_view.findViewById(R.id.guest_list_edit_etxt);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(main_view)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onItemClick(txtView.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditGuestDialogFragment.this.getDialog().cancel();
                    }
                })
                .setTitle(R.string.edit);

        //исправляем баги AutoCompleteTextView советами со Stackoverflow
        AlertDialog alert = builder.create();
        txtView.postDelayed(new Runnable() {
            @Override
            public void run() {
                txtView.showDropDown();
            }
        }, 500);
        txtView.setText(name);
        txtView.dismissDropDown();
        txtView.setSelection(txtView.getText().length());

        return alert;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
