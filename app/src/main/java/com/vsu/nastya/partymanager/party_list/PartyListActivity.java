package com.vsu.nastya.partymanager.party_list;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
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
import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vk.sdk.VKSdk;
import com.vsu.nastya.partymanager.MainActivity;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.logic.DatabaseConsts;
import com.vsu.nastya.partymanager.logic.DateWorker;
import com.vsu.nastya.partymanager.logic.User;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.vsu.nastya.partymanager.logic.DatabaseConsts.DATE;
import static com.vsu.nastya.partymanager.logic.DatabaseConsts.NAME;
import static com.vsu.nastya.partymanager.logic.DatabaseConsts.PARTIES;
import static com.vsu.nastya.partymanager.logic.DatabaseConsts.PARTIES_ID_LIST;
import static com.vsu.nastya.partymanager.logic.DatabaseConsts.USERS;

/**
 * Окно со списоком всех вечеринок
 */
public class PartyListActivity extends AppCompatActivity {

    public static final String ICON_EXTRA = "icon";
    public static final String PARTY_EXTRA = "party";
    public static final String POSITION_EXTRA = "position";

    private static final int ADD_PARTY_REQUEST_CODE = 1;
    private static final int EDIT_PARTY_REQUEST_CODE = 2;
    private static final String MULTI_SELECTOR = "PartyListActivity";
    private static final String FIREBASE_ERROR = "firebase_error";

    private User user = User.getInstance();
    private ArrayList<Party> partiesList;
    private PartiesAdapter adapter;
    private MultiSelector multiSelector = new MultiSelector();
    private DatabaseReference partiesReference;
    private DatabaseReference usersReference;
    private ChildEventListener partyAddListener = null;
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
                        intent.putExtra(PARTY_EXTRA, partiesList.get(positions.get(0)));
                        intent.putExtra(POSITION_EXTRA, positions.get(0));
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
        context.startActivity(new Intent(context, PartyListActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_list);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                    DatabaseReference partyReference = partiesReference.push();

                    Uri iconUri = data.getParcelableExtra(ICON_EXTRA);
                    if (iconUri != null) {
                        setNewPartyIcon(null, iconUri, partyReference);
                    }

                    Party party = (Party) data.getSerializableExtra(PARTY_EXTRA);
                    User user = User.getInstance();
                    party.getGuests().add(new Guest(user.getVkId(), user.getVkPhotoUrl(), user.getFullName()));

                    party.setKey(partyReference.getKey());
                    party.setMessagesList(new ArrayList<>());
                    partyReference.setValue(party);

                    user.getPartiesIdList().add(party.getKey());

                    // Заносим в базу новую вечеринку в список у текущего юзера
                    String partyIndex = String.valueOf(partiesList.size());
                    usersReference.child(user.getVkId()).
                            child(DatabaseConsts.PARTIES_ID_LIST).
                            child(partyIndex).setValue(party.getKey());
                }
                break;
            case EDIT_PARTY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    int position = data.getIntExtra(POSITION_EXTRA, 0);
                    Party oldParty = partiesList.get(position);
                    DatabaseReference partyReference = partiesReference.child(oldParty.getKey());

                    Uri iconUri = data.getParcelableExtra(ICON_EXTRA);
                    if (iconUri != null) {
                        setNewPartyIcon(oldParty.getIcon(), iconUri, partyReference);
                    }

                    Party newParty = (Party) data.getSerializableExtra(PARTY_EXTRA);
                    //int position = data.getIntExtra("position", 0);
                    HashMap<String, Object> task = new HashMap<>();
                    task.put(NAME, newParty.getName());
                    task.put(DATE, newParty.getDate());
                    partyReference.updateChildren(task);
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
        partiesReference = databaseReference.child(PARTIES);
        usersReference = databaseReference.child(USERS);

        // Инициализируем список вечеринок
        partiesList = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.partylist_parties_recycler);

        adapter = new PartiesAdapter();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                1);//1-вертикальная ориентация LinearLayout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //этот метод получения ресурса дико ругается и аналог ему не найден, поэтому обладатели устройств
            // до 21й версии будут жить с простой стандартной полосочкой
            Drawable divider = getResources().getDrawable(R.drawable.divider, getTheme());
            dividerItemDecoration.setDrawable(divider);

        }
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Кнопка добавления новой вечеринки
        FloatingActionButton addPartyButton = (FloatingActionButton) findViewById(R.id.partylist_partyAdd_fab);
        addPartyButton.setOnClickListener(view -> startActivityForResult(new Intent(PartyListActivity.this, AddPartyActivity.class), ADD_PARTY_REQUEST_CODE));

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
            if (p.getKey().equals(key)) {
                return partiesList.indexOf(p);
            }
        }
        return -1;
    }

    private void removePartyIcon(String iconUrl) {
        StorageReference partyIconStorageReference = FirebaseStorage.getInstance()
                .getReferenceFromUrl(iconUrl);
        partyIconStorageReference.delete();
    }

    private void setNewPartyIcon(String oldIconUrl, Uri newIconUri, DatabaseReference partyReference) {
        if (oldIconUrl != null) {
            removePartyIcon(oldIconUrl);
        }
        StorageReference partyIconStorageReference = FirebaseStorage.getInstance()
                .getReference()
                .child(partyReference.getKey())
                .child(DatabaseConsts.PARTY_ICON)
                .child(newIconUri.getLastPathSegment());

        partyIconStorageReference.putFile(newIconUri)
                .addOnSuccessListener(this, taskSnapshot -> {
            String icon = String.valueOf(taskSnapshot.getDownloadUrl());
            partyReference.child(DatabaseConsts.ICON).setValue(icon);
        });
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
                    if ((party != null) && (user.getPartiesIdList().contains(party.getKey()))) {
                        partiesList.add(party);
                        adapter.notifyItemInserted(partiesList.size() - 1);
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
                            // Удаляем вечеринку из списка вечеринок
                            String key = partiesList.get(position).getKey();
                            partiesList.remove(position);

                            // Удаляем id этой вечеринки
                            User currentUser = User.getInstance();
                            ArrayList<String> partiesIds = currentUser.getPartiesIdList();
                            partiesIds.remove(key);

                            // Для удаления id вечеринки из базы сначала удаляем весь список id,
                            // а затем записываем все заново
                            DatabaseReference partiesIdReference = usersReference.child(User.getInstance().getVkId()).child(PARTIES_ID_LIST);
                            partiesIdReference.removeValue();
                            for (String id : partiesIds) {
                                partiesIdReference.child(String.valueOf(partiesIds.indexOf(id))).setValue(id);
                            }
                        }
                        adapter.notifyItemRemoved(position);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

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

        TextView partyName, date;
        CircleImageView icon;

        PartyViewHolder(View itemView) {
            super(itemView, multiSelector);

            partyName = (TextView) itemView.findViewById(R.id.parties_item_name);
            date = (TextView) itemView.findViewById(R.id.parties_item_date);
            icon = (CircleImageView) itemView.findViewById(R.id.parties_item_icon);

            itemView.setLongClickable(true);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void bindParty(Party party) {
            partyName.setText(party.getName());
            String date = DateWorker.getDateAsString(party.getDate()) + ", " +
                    DateWorker.getTimeAsString(party.getDate());
            this.date.setText(date);

            if (party.getIcon() != null) {
                Glide.with(icon.getContext())
                        .load(party.getIcon())
                        .into(icon);
            } else {
                icon.setImageResource(R.drawable.ic_star_primary_24dp);
            }
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
                int index = getAdapterPosition();

                Intent intent = new Intent(getApplicationContext(), PartyDetailsActivity.class);
                intent.putExtra(PARTY_EXTRA, partiesList.get(index));
                view.getContext().startActivity(intent);
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
