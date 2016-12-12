package com.vsu.nastya.partymanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Окно для просмотра и изменения основной информации о вечеринке (название, место, дата, время)
 */
public class PartyInfoFragment extends Fragment {

    public PartyInfoFragment() {
    }

    public static PartyInfoFragment newInstance() {
       return new PartyInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_party_info, container, false);
    }
}
