package com.vsu.nastya.partymanager.party_details;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.vsu.nastya.partymanager.guest_list.GuestListFragment;
import com.vsu.nastya.partymanager.item_list.ItemsListFragment;
import com.vsu.nastya.partymanager.messager_list.MessageListFragment;
import com.vsu.nastya.partymanager.party_info.PartyInfoFragment;

/**
 * Created by nastya on 08.12.16.
 */

public class PartyDetailsPagerAdapter extends FragmentPagerAdapter {

    public static final int ITEMS_TAB = 0;
    public static final int GUESTS_TAB = 1;
    public static final int MESSAGES_TAB = 2;
    public static final int LOCATION_TAB = 3;
    private static final int COUNT = 4;

    public PartyDetailsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position){
        switch (position){
            case ITEMS_TAB:
                return ItemsListFragment.newInstance();
            case GUESTS_TAB:
                return GuestListFragment.newInstance();
            case MESSAGES_TAB:
                return MessageListFragment.newInstance();
            case LOCATION_TAB:
                return PartyInfoFragment.newInstance();
            default:
                return ItemsListFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return COUNT;
    }
}
