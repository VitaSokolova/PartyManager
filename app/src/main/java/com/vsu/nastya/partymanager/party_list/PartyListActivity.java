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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vk.sdk.VKSdk;
import com.vsu.nastya.partymanager.MainActivity;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.logic.DateWorker;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Окно со списоком всех вечеринок
 */
public class PartyListActivity extends AppCompatActivity{

    private static final int ADD_PARTY_REQUEST_CODE = 1;
    private static final int EDIT_PARTY_REQUEST_CODE = 2;
    private static final String MULTI_SELECTOR = "PartyListActivity";
    private static final String FIREBASE_ERROR = "firebase_error";
    private ArrayList<Party> partiesList;
    private PartiesAdapter adapter;
    private MultiSelector multiSelector = new MultiSelector();
    private DatabaseReference partiesReference;
    private ChildEventListener partyAddListener;
    private ProgressBar progressBar;

    private ActionMode actionMode;
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
                            partiesReference.child(partiesList.get(i).getKey()).removeValue();
                        }
                    }
                    mode.finish();
                    return true;
                case R.id.action_edit:
                    ArrayList<Integer> positions = (ArrayList<Integer>) multiSelector.getSelectedPositions();
                    if (positions.size() == 1) {
                        Intent intent = new Intent(PartyListActivity.this, AddPartyActivity.class);
                        intent.putExtra("party", partiesList.get(positions.get(0)));
                        intent.putExtra("position", positions.get(0));
                        startActivityForResult(intent, EDIT_PARTY_REQUEST_CODE);
                        mode.finish();
                    }
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

    @Override
    protected void onResume() {
        super.onResume();
        /* При возобновлении активити очищаем каждый раз список вечеринок,
           потому что partyAddListener снова добавит все в список */
        if (partiesList.size() != 0) {
            partiesList.clear();
            adapter.notifyDataSetChanged();
        }
        progressBar.setVisibility(ProgressBar.VISIBLE);
        attachDatabaseReadListener();
        attachStopProgressBarListener();
    }



    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle(MULTI_SELECTOR, multiSelector.saveSelectionStates());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (multiSelector != null) {
            if (savedInstanceState != null) {
                multiSelector.restoreSelectionStates(savedInstanceState.getBundle(MULTI_SELECTOR));
            }

            if (multiSelector.isSelectable()) {
                if (callback != null) {
                    callback.setClearOnPrepare(false);
                    actionMode = startSupportActionMode(callback);
                }
            }
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Метод вызывается, когда AddPartyActivity завершает работу.
     * Новая вечеринка добавляется в список или изменяется информация об одной из.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_PARTY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Party party = (Party) data.getSerializableExtra("party");
                    DatabaseReference reference = partiesReference.push();
                    party.setKey(reference.getKey());
                    reference.setValue(party);
                }
                break;
            case EDIT_PARTY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Party newParty = (Party) data.getSerializableExtra("party");
                    int position = data.getIntExtra("position", 0);
                    HashMap<String, Object> task = new HashMap<>();
                    task.put("name", newParty.getName());
                    task.put("date", newParty.getDate());
                    partiesReference.child(partiesList.get(position).getKey()).updateChildren(task);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.parametrs_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu_item:
                logOut();
                return true;
            default:
                return false;
        }
    }

    private void initView() {
        //Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        partiesReference = databaseReference.child("parties");

        // Инициализируем список вечеринок
        partiesList = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.partylist_parties_recycler);

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
                startActivityForResult(new Intent(PartyListActivity.this, AddPartyActivity.class), ADD_PARTY_REQUEST_CODE);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.partyList_progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    /**
     * Выход из VK
     */
    private void logOut() {
        VKSdk.logout();
        MainActivity.start(this);
    }

    private int getPositionByKey(String key) {
        for (Party p : partiesList) {
            if (p.getKey().equals(key)){
                return partiesList.indexOf(p);
            }
        }
        return -1;
    }

    /**
     * В метод устанавливается listener, который следит за изменениями в базе данных
     * (добавление вечеринки, удаление, обновление).
     */
    private void attachDatabaseReadListener() {
        if (partyAddListener == null) {
            partyAddListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Party party = dataSnapshot.getValue(Party.class);
                    if (party != null) {
                        partiesList.add(party);
                        adapter.notifyItemInserted(partiesList.size()-1);
                    }
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Party party = dataSnapshot.getValue(Party.class);
                    if (party != null) {
                        int position = getPositionByKey(party.getKey());
                        if (position != -1) {
                            partiesList.set(position, party);
                            adapter.notifyItemChanged(position);
                        }
                    }
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Party party = dataSnapshot.getValue(Party.class);
                    if (party != null) {
                        int position = getPositionByKey(party.getKey());
                        if (position != -1) {
                            partiesList.remove(position);
                            adapter.notifyItemRemoved(position);
                        }
                    }
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Обычно вызывается, когда нет прав на чтение данных из базы
                    Log.d(FIREBASE_ERROR, "onCancelled: " + databaseError);
                }
            };

            partiesReference.addChildEventListener(partyAddListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (partyAddListener != null) {
            partiesReference.removeEventListener(partyAddListener);
            partyAddListener = null;
        }
    }

    private void attachStopProgressBarListener() {
        partiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обычно вызывается, когда нет прав на чтение данных из базы
                Log.d(FIREBASE_ERROR, "onCancelled: " + databaseError);
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
                if (multiSelector.getSelectedPositions().size() == 0) {
                    actionMode.finish();
                }
            } else {
                PartyDetailsActivity.start(PartyListActivity.this);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            actionMode = startSupportActionMode(callback);
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
