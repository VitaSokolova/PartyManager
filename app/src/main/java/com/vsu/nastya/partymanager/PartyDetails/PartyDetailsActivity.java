package com.vsu.nastya.partymanager.PartyDetails;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.vsu.nastya.partymanager.R;

/**
 * Окно с вкладками (где смотрим и изменям любую информацию о вечеринке)
 */
public class PartyDetailsActivity extends AppCompatActivity {

    public static void start(Context context){
        Intent intent = new Intent(context, PartyDetailsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_details);
        initView();
    }

    private  void initView(){
        ViewPager pager = (ViewPager) findViewById(R.id.details_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        PartyDetailsPagerAdapter adapter = new PartyDetailsPagerAdapter(fragmentManager);
        pager.setAdapter(adapter);

        TabLayout tabs = (TabLayout) findViewById(R.id.details_tabs);
        tabs.setupWithViewPager(pager);

        tabs.getTabAt(0).setIcon(R.drawable.ic_pin_drop_white_24dp);
        tabs.getTabAt(1).setIcon(R.drawable.ic_shopping_cart_white_24dp);
        tabs.getTabAt(2).setIcon(R.drawable.ic_group_white_24dp);
    }
}
