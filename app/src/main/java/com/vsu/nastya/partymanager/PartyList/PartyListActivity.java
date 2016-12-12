package com.vsu.nastya.partymanager.PartyList;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.vsu.nastya.partymanager.AddPartyActivity;
import com.vsu.nastya.partymanager.R;

import java.util.ArrayList;
import java.util.List;

public class PartyListActivity extends AppCompatActivity {

    private List<Party> partiesList;

    public static void start(Context context) {
        Intent intent = new Intent(context, PartyListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_list);
        initView();
    }

    private void initView() {
        //инициализируем список вечеринок
        partiesList = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.partylist_parties_recycler);
        PartiesAdapter adapter = new PartiesAdapter(partiesList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        //кнопка добавления новой вечеринки
        FloatingActionButton addPartyButton = (FloatingActionButton) findViewById(R.id.partylist_partyAdd_fab);
        addPartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //появляется окно с возможностью добавить новую вечеринку
                AddPartyActivity.start(PartyListActivity.this);
            }
        });
    }
}
