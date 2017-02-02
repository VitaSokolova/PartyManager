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
 * Диалог для выбора времени
 */

public class TimePickerFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Calendar calendar = (Calendar) getArguments().getSerializable("calendar");

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), (AddPartyActivity) getActivity(), hour, minute, true);
    }

    public static TimePickerFragment newInstance(Calendar calendar) {
        Bundle args = new Bundle();
        args.putSerializable("calendar", calendar);
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
