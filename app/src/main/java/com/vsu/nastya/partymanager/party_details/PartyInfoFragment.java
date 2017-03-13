package com.vsu.nastya.partymanager.party_details;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.party_list.Party;


/**
 * Окно для просмотра и изменения основной информации о вечеринке (название, место, дата, время)
 */
public class PartyInfoFragment extends Fragment {
    private Party currentParty;

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
        //получаем информацию о вечеринке от родительской активити
        PartyDetailsActivity activity = (PartyDetailsActivity) getActivity();
        this.currentParty = activity.getCurrentParty();
        return inflater.inflate(R.layout.fragment_party_info, container, false);
    }
}
