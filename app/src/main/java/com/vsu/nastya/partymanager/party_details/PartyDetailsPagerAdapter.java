package com.vsu.nastya.partymanager.party_details;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.vsu.nastya.partymanager.guest_list.GuestListFragment;
import com.vsu.nastya.partymanager.item_list.ItemsListFragment;

/**
 * Created by nastya on 08.12.16.
 */

public class PartyDetailsPagerAdapter extends FragmentPagerAdapter {

    private static final int COUNT = 3;

    public PartyDetailsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                return PartyInfoFragment.newInstance();
            case 1:
                return ItemsListFragment.newInstance();
            case 2:
                return GuestListFragment.newInstance();
            default:
                return ItemsListFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return COUNT;
    }
}
