package com.vsu.nastya.partymanager.party_details;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.party_list.Party;

import static com.vsu.nastya.partymanager.party_details.PartyDetailsPagerAdapter.*;

/**
 * Окно с вкладками (где смотрим и изменям любую информацию о вечеринке)
 */
public class PartyDetailsActivity extends AppCompatActivity {

    private static final String CURRENT_TAB = "current_tab";
    private static final String PARTY = "party";
    private Party currentParty;

    private static final int OFFSET_SCREEN_PAGE_LIMIT = 3;

    public static void start(Context context) {
        Intent intent = new Intent(context, PartyDetailsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_details);
        currentParty = (Party) getIntent().getSerializableExtra(PARTY);
        initView();
    }

    public Party getCurrentParty() {
        return currentParty;
    }

    private void initView() {

        ViewPager pager = (ViewPager) findViewById(R.id.details_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        PartyDetailsPagerAdapter adapter = new PartyDetailsPagerAdapter(fragmentManager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(OFFSET_SCREEN_PAGE_LIMIT);

        TabLayout tabs = (TabLayout) findViewById(R.id.details_tabs);
        tabs.setupWithViewPager(pager);

        tabs.getTabAt(ITEMS_TAB).setIcon(R.drawable.ic_shopping_cart_white_24dp);
        tabs.getTabAt(GUESTS_TAB).setIcon(R.drawable.ic_group_white_24dp);
        tabs.getTabAt(MESSAGES_TAB).setIcon(R.drawable.ic_question_answer_white_48dp);
        tabs.getTabAt(LOCATION_TAB).setIcon(R.drawable.ic_pin_drop_white_24dp);

        /*Если активити стартовала через оповещение, то открываем ту вкладку,
        которой оно соответсвует.*/
        pager.setCurrentItem(getIntent().getIntExtra(CURRENT_TAB, 0));
    }
}