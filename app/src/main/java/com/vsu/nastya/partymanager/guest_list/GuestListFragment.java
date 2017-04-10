package com.vsu.nastya.partymanager.guest_list;

import android.content.Intent;
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
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.logic.Notifications;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;
import com.vsu.nastya.partymanager.party_list.Party;

import java.util.ArrayList;

/**
 * Created by Вита on 08.12.2016.
 * Это фрагмент для списка гостей
 */
public class GuestListFragment extends Fragment {
    public static String TAG = "guestListFragment";
    private static final String FIREBASE_ERROR = "firebase_error";

    private boolean initialization;
    private Party currentParty;
    private RecyclerView mRecyclerView;
    private FloatingActionButton addGuestFab;
    private ArrayList<Guest> guestList;
    private GuestAdapter adapter;
    private MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode actionMode;
    private ProgressBar progressBar;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference guestsDatabaseReference;
    private ChildEventListener guestsEventListener;

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
                for (int i = guestList.size(); i >= 0; i--) {
                    if (mMultiSelector.isSelected(i, 0)) {
                        //TODO:удалить еще и из базы и вообще навсегда
                        guestList.remove(i);
                        mRecyclerView.getAdapter().notifyItemRemoved(i);
                        guestsDatabaseReference.setValue(currentParty.getGuests());
                    }
                }
                actionMode.finish();
                return true;
            }

            //TODO:сделать редактирование
            if (menuItem.getItemId() == R.id.action_edit) {
                final ArrayList<Integer> indexes = (ArrayList<Integer>) mMultiSelector.getSelectedPositions();
                if (indexes.size() == 1) {
                    EditGuestDialogFragment dialog = EditGuestDialogFragment.newInstance(guestList.get(indexes.get(0)).getGuestName());
                    dialog.setListener(new EditGuestDialogFragment.OnItemClickListener() {
                        @Override
                        public void onItemClick(String text) {
                            Guest guest = guestList.get(indexes.get(0));
                            guest.setGuestName(text);
                            adapter.notifyItemChanged(indexes.get(0));
                            guestsDatabaseReference.child(String.valueOf(indexes.get(0))).setValue(guest);

                        }
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

        firebaseDatabase = FirebaseDatabase.getInstance();
        this.guestsDatabaseReference = firebaseDatabase.getReference().child("parties").child(currentParty.getKey()).child("guests");

        this.guestList = new ArrayList<>();
        this.adapter = new GuestAdapter();

        progressBar = (ProgressBar) view.findViewById(R.id.guest_list_progressBar);
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.guest_list_recycler_view);
        this.addGuestFab = (FloatingActionButton) view.findViewById(R.id.guest_list_add_fab);

        initRecycler();

        addGuestFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddGuestActivity.class);
                // 1 - requestCode
                startActivityForResult(intent, 1);
            }
        });
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
     * получаем нашего нового гостя
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        Guest newGuest = (Guest) data.getSerializableExtra("guest");
        guestsDatabaseReference.child(String.valueOf(currentParty.getGuests().size())).setValue(newGuest);
        this.guestList.add(newGuest);
        this.adapter.notifyItemInserted(this.guestList.size());

        addPartyIdToGuestPartyList(newGuest);
    }

    /**
     * инициализирует RecyclerView
     */
    private void initRecycler() {
        //TODO: вытащить это список из экземпляра User, но пока пусть так
        guestList = this.currentParty.getGuests();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * метод ищем в базе приложения пользователя с номером гостя и добавляет в его список вечеринок номер нашей вечеринки
     *
     * @param newGuest приглашенный гость
     */
    private void addPartyIdToGuestPartyList(Guest newGuest) {
        DatabaseReference rootDatabaseReference = firebaseDatabase.getReference();
        if (newGuest.getVkId() != null) {
            final DatabaseReference guestPartiesDatabaseRef = rootDatabaseReference.child("users").child(newGuest.getVkId()).child("partiesIdList");
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
     * снимаем слушателя с базы
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

    //эти внутренние классы для адаптера и ViewHolder предлагает сюда поместить автор библиотеки для MultiSelection,
    //потому что это значительно упрощает работу с MultiSelector
    private class GuestViewHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView nameTxtView;

        public GuestViewHolder(View itemView) {
            super(itemView, mMultiSelector);
            this.nameTxtView = (TextView) itemView.findViewById(R.id.guest_name_txt);
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
            } else {
                // Selection not active
            }
        }
    }

    private class GuestAdapter extends RecyclerView.Adapter<GuestViewHolder> {

        @Override
        public GuestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.guest_list_item_appearence, parent, false);
            return new GuestViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(GuestViewHolder holder, int position) {
            Guest guest = guestList.get(position);
            holder.nameTxtView.setText(guest.getGuestName());
        }

        @Override
        public int getItemCount() {
            return guestList.size();
        }
    }
}

