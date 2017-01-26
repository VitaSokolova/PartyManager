package com.vsu.nastya.partymanager.party_list;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.vsu.nastya.partymanager.AddPartyActivity;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;
import com.vsu.nastya.partymanager.R;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Окно со списоком всех вечеринок
 */
public class PartyListActivity extends AppCompatActivity {

    private List<Party> partiesList;
    private PartiesAdapter adapter;
    private static final int ADD_PARTY_REQUEST_CODE = 1;

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

    /**
     * Метод вызывается, когда AddPartyActivity завершает работу.
     * Новая вечеринка добавляется в список.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_PARTY_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                Party party = (Party) data.getSerializableExtra("party");
                partiesList.add(party);
                adapter.notifyItemInserted(adapter.getItemCount());
            }
        }
    }

    private void initView() {
        //инициализируем список вечеринок
        partiesList = new ArrayList<>();
        partiesList.add(new Party("Виточкин ДР", new GregorianCalendar()));
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.partylist_parties_recycler);
        //Вита добавила открытие активити с тремя фрагментами по нажатию на вечеринку
        // TODO: доделать передачу на эти фрагменты нужных данных
        adapter = new PartiesAdapter(partiesList, new PartiesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Party party) {
                PartyDetailsActivity.start(PartyListActivity.this);
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        //кнопка добавления новой вечеринки
        FloatingActionButton addPartyButton = (FloatingActionButton) findViewById(R.id.partylist_partyAdd_fab);
        addPartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPartyActivity.startForResult(PartyListActivity.this, ADD_PARTY_REQUEST_CODE);
            }
        });
    }
}
