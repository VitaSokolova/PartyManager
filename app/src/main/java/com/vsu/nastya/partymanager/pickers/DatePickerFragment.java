package com.vsu.nastya.partymanager.pickers;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.vsu.nastya.partymanager.party_list.AddPartyActivity;

import java.util.Calendar;

/**
 * Created by nastya on 07.12.16.
 */

public class DatePickerFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), (AddPartyActivity) getActivity(), year, month, day);
        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        return dialog;
    }
}
