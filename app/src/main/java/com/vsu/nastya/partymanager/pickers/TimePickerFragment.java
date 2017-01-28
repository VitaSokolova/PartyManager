package com.vsu.nastya.partymanager.pickers;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.vsu.nastya.partymanager.party_list.AddPartyActivity;

import java.util.Calendar;

/**
 * Created by nastya on 07.12.16.
 */

public class TimePickerFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), (AddPartyActivity) getActivity(), hour, minute, true);
    }
}
