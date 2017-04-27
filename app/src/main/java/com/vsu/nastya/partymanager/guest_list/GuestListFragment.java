package com.vsu.nastya.partymanager.guest_list;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.logic.DatabaseConsts;
import com.vsu.nastya.partymanager.logic.Friend;
import com.vsu.nastya.partymanager.logic.Notifications;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;
import com.vsu.nastya.partymanager.party_list.Party;

import java.util.ArrayList;

import static com.vsu.nastya.partymanager.logic.DatabaseConsts.PARTIES_ID_LIST;
import static com.vsu.nastya.partymanager.logic.DatabaseConsts.USERS;
import static com.vsu.nastya.partymanager.logic.ErrorsConstants.FIREBASE_ERROR;

/**
 * Created by Вита on 08.12.2016.
 * Это фрагмент для списка гостей
 */
public class GuestListFragment extends Fragment {

    public static String TAG = "guestListFragment";
    public static String GUEST_EXTRA = "guest";

    private Party currentParty;

    private RecyclerView recyclerView;
    private FloatingActionButton addGuestFab;

    private GuestAdapter adapter;
    private MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode actionMode;
    private ProgressBar progressBar;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference guestsDatabaseReference;
    private DatabaseReference usersDatabaseReference;
    private ChildEventListener guestsEventListener;

    private boolean initialization;

    private ModalMultiSelectorCallback mActionModeCallback = new ModalMultiSelectorCallback(mMultiSelector) {
        //открывается наше меню
        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            getActivity().getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        }

        //обрабатываем нажатия на иконочки
        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_delete) {
                //проходимся по всему списку, если элемент присутствует в MultiSelector, удаляем его
                for (int i = currentParty.getGuests().size() - 1; i >= 0; i--) {
                    if (mMultiSelector.isSelected(i, 0)) {
                        removeGuest(i);
                    }
                }
                actionMode.finish();
                return true;
            }

            if (menuItem.getItemId() == R.id.action_edit) {
                final ArrayList<Integer> indexes = (ArrayList<Integer>) mMultiSelector.getSelectedPositions();
                if (indexes.size() == 1) {
                    EditGuestDialogFragment dialog = EditGuestDialogFragment.newInstance(currentParty.getGuests().get(indexes.get(0)).getGuestName());
                    dialog.setListener(text -> {
                        Guest guest = currentParty.getGuests().get(indexes.get(0));
                        guest.setGuestName(text);
                        adapter.notifyItemChanged(indexes.get(0));
                        guestsDatabaseReference.child(String.valueOf(indexes.get(0))).setValue(guest);

                    });
                    dialog.show(getFragmentManager(), "guestEditDialog");
                }
                return true;
            }
            return false;
        }


        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            //если предварительно перед закрытием не почистить MultiSelector начинают баги с окрашиванием,
            // поэтому тут такой костылик, ведь его сделать проще, чем предъявлять претензии автору библиотеки
            mMultiSelector.clearSelections();
            super.onDestroyActionMode(actionMode);
        }
    };


    public static GuestListFragment newInstance() {
        return new GuestListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //получаем информацию о вечеринке от родительской активити
        PartyDetailsActivity activity = (PartyDetailsActivity) getActivity();
        this.currentParty = activity.getCurrentParty();

        View view = inflater.inflate(R.layout.fragment_guest_list, container, false);

        initDatabaseReferences();
        initViews(view);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        initialization = true;
        attachDatabaseReadListener();
        attachStopProgressBarListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        if (mMultiSelector != null) {
            Bundle bundle = savedInstanceState;
            if (bundle != null) {
                mMultiSelector.restoreSelectionStates(bundle.getBundle(TAG));
            }

            if (mMultiSelector.isSelectable()) {
                if (mActionModeCallback != null) {
                    mActionModeCallback.setClearOnPrepare(false);
                    actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                }

            }
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle(TAG, mMultiSelector.saveSelectionStates());
        super.onSaveInstanceState(outState);
    }

    /**
     * Получаем нашего нового гостя
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        Guest newGuest = (Guest) data.getSerializableExtra(GUEST_EXTRA);
        guestsDatabaseReference.child(String.valueOf(currentParty.getGuests().size())).setValue(newGuest);
        this.currentParty.getGuests().add(newGuest);
        this.adapter.notifyItemInserted(this.currentParty.getGuests().size());

        addPartyIdToGuestPartyList(newGuest);
    }

    /**
     * Инициализирует Views
     */
    private void initViews(View view) {
        this.progressBar = (ProgressBar) view.findViewById(R.id.guest_list_progressBar);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.guest_list_recycler_view);
        this.addGuestFab = (FloatingActionButton) view.findViewById(R.id.guest_list_add_fab);

        addGuestFab.setOnClickListener(view1 -> {
            Intent intent = new Intent(view1.getContext(), AddGuestActivity.class);
            startActivityForResult(intent, 1);      // 1 - requestCode
        });

        initRecycler();
    }

    /**
     * Инициализирует RecyclerView
     */
    private void initRecycler() {

        this.adapter = new GuestAdapter(currentParty.getGuests());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Метод устанавливает ссылки на нужные куски базы, к которым надо нужен будет доступ
     */
    private void initDatabaseReferences() {

        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.guestsDatabaseReference = firebaseDatabase.getReference()
                .child(DatabaseConsts.PARTIES)
                .child(currentParty.getKey())
                .child(DatabaseConsts.GUESTS);

        this.usersDatabaseReference = firebaseDatabase.getReference()
                .child(USERS);
    }

    /**
     * Метод ищем в базе приложения пользователя с номером гостя и добавляет в его список вечеринок номер нашей вечеринки
     *
     * @param newGuest приглашенный гость
     */
    private void addPartyIdToGuestPartyList(Guest newGuest) {
        DatabaseReference rootDatabaseReference = firebaseDatabase.getReference();
        if (newGuest.getVkId() != null) {
            final DatabaseReference guestPartiesDatabaseRef = rootDatabaseReference.child(USERS).child(newGuest.getVkId()).child(PARTIES_ID_LIST);
//
            guestPartiesDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long count = dataSnapshot.getChildrenCount();
                    guestPartiesDatabaseRef.child(String.valueOf(count)).setValue(currentParty.getKey());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Прикрепляем слушателя к базе
     */
    private void attachDatabaseReadListener() {
        if (guestsEventListener == null) {
            guestsEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Guest guest = dataSnapshot.getValue(Guest.class);
                    //этот же кусочек исполняетс для загрузки гостей вечеринки впервые
                    if (!currentParty.getGuests().contains(guest)) {
                        currentParty.getGuests().add(guest);
                        adapter.notifyItemInserted(currentParty.getGuests().size());
                    }
                    if (!initialization) {
                        Notifications.newGuestAdded(getActivity(), currentParty);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Guest guest = dataSnapshot.getValue(Guest.class);
                    if (guest != null) {
                        int position = currentParty.getGuests().indexOf(guest);
                        if (position != -1) {
                            currentParty.getGuests().set(position, guest);
                            adapter.notifyItemChanged(position);
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Guest guest = dataSnapshot.getValue(Guest.class);
                    if (guest != null) {
                        int position = currentParty.getGuests().indexOf(guest);
                        if (position != -1) {
                            currentParty.getGuests().remove(position);
                            adapter.notifyItemRemoved(position);
                        }
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
            guestsDatabaseReference.addChildEventListener(guestsEventListener);
        }
    }

    /**
     * Снимаем слушателя с базы
     */
    private void detachDatabaseReadListener() {
        if (guestsEventListener != null) {
            guestsDatabaseReference.removeEventListener(guestsEventListener);
            guestsEventListener = null;
        }
    }

    private void attachStopProgressBarListener() {
        guestsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                initialization = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обычно вызывается, когда нет прав на чтение данных из базы
                Log.d(FIREBASE_ERROR, "onCancelled: " + databaseError);
            }
        });
    }

    private void removeCurrentParty() {
        firebaseDatabase.getReference()
                .child(DatabaseConsts.PARTIES)
                .child(currentParty.getKey())
                .removeValue();
        // removePartyStorage(); ??
        getActivity().finish();
    }

    private void removeGuest(int position) {
        //удаление id вечеринки из списка юзера
        Guest guest = currentParty.getGuests().get(position);
        removePartyIdFromUser(guest);

        //удаления юзера из полей вечеринки
        currentParty.getGuests().remove(position);
        recyclerView.getAdapter().notifyItemRemoved(position);

        if (currentParty.getGuests().size() == 0) {
            removeCurrentParty();
        } else {
            guestsDatabaseReference.removeValue();
            guestsDatabaseReference.setValue(currentParty.getGuests());
        }
    }

    private void removePartyIdFromUser(Guest guest) {
        usersDatabaseReference.child(guest.getVkId()).child(PARTIES_ID_LIST).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<String> partiesIdList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String id = (String) child.getValue();
                    partiesIdList.add(id);
                }
                int position = partiesIdList.indexOf(currentParty.getKey());
                partiesIdList.remove(position);
                usersDatabaseReference.child(guest.getVkId()).child(PARTIES_ID_LIST).setValue(partiesIdList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обычно вызывается, когда нет прав на чтение данных из базы
                Log.d(FIREBASE_ERROR, "onCancelled: " + databaseError);
            }
        });

    }

    //эти внутренние классы для адаптера и ViewHolder предлагает сюда поместить автор библиотеки для MultiSelection,
    //потому что это значительно упрощает работу с MultiSelector
    private class GuestViewHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView nameTxtView;
        ImageView profilePhoto;

        GuestViewHolder(View itemView) {
            super(itemView, mMultiSelector);
            this.nameTxtView = (TextView) itemView.findViewById(R.id.guest_name_txt);
            this.profilePhoto = (ImageView) itemView.findViewById(R.id.guest_profile_image);
            itemView.setLongClickable(true);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            mMultiSelector.setSelected(GuestViewHolder.this, true);
            return true;
        }

        @Override
        public void onClick(View view) {
            if (mMultiSelector.isSelectable()) {
                // Selection is active; toggle activation
                setActivated(!isActivated());
                mMultiSelector.setSelected(GuestViewHolder.this, isActivated());
                if (mMultiSelector.getSelectedPositions().size() == 0) {
                    actionMode.finish();
                }
            }
        }
    }

    private class GuestAdapter extends RecyclerView.Adapter<GuestViewHolder> {
        private ArrayList<Guest> guestList;

        GuestAdapter(ArrayList<Guest> guestList) {
            this.guestList = guestList;
        }

        @Override
        public GuestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.guest_list_item_appearence, parent, false);
            return new GuestViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(GuestViewHolder holder, int position) {
            Guest guest = this.guestList.get(position);
            holder.nameTxtView.setText(guest.getGuestName());
            //грузим картинку
            Glide.with(getContext())
                    .load(guest.getVkPhotoUrl())
                    .centerCrop()
                    .into(holder.profilePhoto);
        }

        @Override
        public int getItemCount() {
            return this.guestList.size();
        }
    }
}

