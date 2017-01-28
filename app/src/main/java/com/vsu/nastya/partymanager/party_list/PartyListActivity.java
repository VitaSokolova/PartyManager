package com.vsu.nastya.partymanager.party_list;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.logic.DateWorker;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Окно со списоком всех вечеринок
 */
public class PartyListActivity extends AppCompatActivity{

    private static final int ADD_PARTY_REQUEST_CODE = 1;
    private static final String TAG = "PartyListActivity";
    private List<Party> partiesList;
    private PartiesAdapter adapter;
    private RecyclerView recyclerView;
    private MultiSelector multiSelector = new MultiSelector();

    private ModalMultiSelectorCallback callback = new ModalMultiSelectorCallback(multiSelector) {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    for (int i = partiesList.size(); i >= 0; i--) {
                        if (multiSelector.isSelected(i, 0)) {
                            partiesList.remove(i);
                            recyclerView.getAdapter().notifyItemRemoved(i);
                        }
                    }
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelector.clearSelections();
            multiSelector.setSelectable(false);
            super.onDestroyActionMode(mode);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_list);

        initView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle(TAG, multiSelector.saveSelectionStates());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (multiSelector != null) {
            Bundle bundle = savedInstanceState;
            if (bundle != null) {
                multiSelector.restoreSelectionStates(bundle.getBundle(TAG));
            }

            if (multiSelector.isSelectable()) {
                if (callback != null) {
                    callback.setClearOnPrepare(false);
                    startSupportActionMode(callback);
                }
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
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

    public static void start(Context context) {
        Intent intent = new Intent(context, PartyListActivity.class);
        context.startActivity(intent);
    }

    private void initView() {
        //инициализируем список вечеринок
        partiesList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.partylist_parties_recycler);
        //Вита добавила открытие активити с тремя фрагментами по нажатию на вечеринку
        // TODO: доделать передачу на эти фрагменты нужных данных

        adapter = new PartiesAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Кнопка добавления новой вечеринки
        FloatingActionButton addPartyButton = (FloatingActionButton) findViewById(R.id.partylist_partyAdd_fab);
        addPartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPartyActivity.startForResult(PartyListActivity.this, ADD_PARTY_REQUEST_CODE);
            }
        });
    }

    // ViewHolder для списка вечеринок
    private class PartyViewHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView partyName, date;

        public PartyViewHolder(View itemView) {
            super(itemView, multiSelector);

            partyName = (TextView) itemView.findViewById(R.id.parties_item_name);
            date = (TextView) itemView.findViewById(R.id.parties_item_date);

            itemView.setLongClickable(true);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void bindParty(Party party) {
            partyName.setText(party.getName());
            String date = DateWorker.getDateAsString(party.getDate()) + ", " +
                    DateWorker.getTimeAsString(party.getDate());
            this.date.setText(date);
        }

        @Override
        public void onClick(View view) {
            if (multiSelector.isSelectable()) {
                setActivated(!isActivated());
                multiSelector.setSelected(PartyViewHolder.this, isActivated());
            } else {
                PartyDetailsActivity.start(PartyListActivity.this);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            startSupportActionMode(callback);
            multiSelector.setSelected(PartyViewHolder.this, true);
            multiSelector.setSelectable(true);
            return true;
        }
    }

    // Адаптер для списка
    private class PartiesAdapter extends RecyclerView.Adapter<PartyViewHolder> {

        @Override
        public PartyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.party_list_item_appereance, parent, false);
            return new PartyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(PartyViewHolder holder, int position) {
            final Party party = partiesList.get(position);
            holder.bindParty(party);
        }

        @Override
        public int getItemCount() {
            return partiesList.size();
        }
    }


}
