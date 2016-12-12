package com.vsu.nastya.partymanager.PartyDetails;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.vsu.nastya.partymanager.R;

public class PartyDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_details);

        ViewPager pager = (ViewPager) findViewById(R.id.details_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        PartyDetailsPagerAdapter adapter = new PartyDetailsPagerAdapter(fragmentManager, this);
        pager.setAdapter(adapter);

        TabLayout tabs = (TabLayout) findViewById(R.id.details_tabs);
        tabs.setupWithViewPager(pager);
    }
}
