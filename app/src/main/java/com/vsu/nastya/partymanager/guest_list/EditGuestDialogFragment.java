package com.vsu.nastya.partymanager.guest_list;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

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
        frag.setCancelable(false);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //забираем имя с активити
        String name = getArguments().getString("name");

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View main_view = inflater.inflate(R.layout.dialog_guest_edit, null);
        final AutoCompleteTextView txtView = (AutoCompleteTextView) main_view.findViewById(R.id.guest_list_edit_etxt);
        
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(main_view)
                .setTitle(R.string.edit)
                .setPositiveButton(R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button buttonOk = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                buttonOk.setOnClickListener(view -> {
                    //я вполне допускаю, что price может быть == 0. Например вы вносите в список клубничку, которую сорвете у себя на даче,
                    // вам надо не забыть её сорвать, а следовательно внести список, но стоимость её будет == 0
                    if (txtView.getText().toString().isEmpty()) {
                        Toast toast = Toast.makeText(getContext(),
                                R.string.alertGuestNameIsEmpty, Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        listener.onItemClick(txtView.getText().toString());
                        //если всё хорошо, то можно закрывать диалог
                        dialog.dismiss();
                    }
                });

                Button buttonCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonCancel.setOnClickListener(view -> EditGuestDialogFragment.this.getDialog().cancel());
            }
        });
        //исправляем баги AutoCompleteTextView советами со Stackoverflow
        txtView.postDelayed(() -> txtView.showDropDown(), 500);
        txtView.setText(name);
        txtView.dismissDropDown();
        txtView.setSelection(txtView.getText().length());
        return dialog;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
