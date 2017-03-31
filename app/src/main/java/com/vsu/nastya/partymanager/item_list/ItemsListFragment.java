package com.vsu.nastya.partymanager.item_list;

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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.item_list.data.Item;
import com.vsu.nastya.partymanager.logic.User;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;
import com.vsu.nastya.partymanager.party_list.Party;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Вита on 08.12.2016.
 */
public class ItemsListFragment extends Fragment {
    public static String TAG = "itemListFragment";
    private static final String FIREBASE_ERROR = "firebase_error";

    private Party currentParty;
    private RecyclerView mRecyclerView;
    private FloatingActionButton addItemFAB;
    private TextView wholeSumTxt;
    private TextView sumPerOneTxt;
    private ItemAdapter adapter;
    private ActionMode actionMode;
    private ProgressBar progressBar;
    private int sumPerOne = 0;
    private int wholeSum = 0;

    private DatabaseReference partyItemsReference;
    private ChildEventListener itemEventListener = null;

    private MultiSelector mMultiSelector = new MultiSelector();
    private ModalMultiSelectorCallback mActionModeCallback = new ModalMultiSelectorCallback(mMultiSelector) {

        //открывается наше меню
        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            getActivity().getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_delete) {
                //проходимся по всему списку, если элемент присутствует в MultiSelector, удаляем его
                for (int i = currentParty.getItems().size(); i >= 0; i--) {
                    if (mMultiSelector.isSelected(i, 0)) {
                        //TODO:удалить еще и из базы и вообще навсегда
                        Item item = currentParty.getItems().get(i);
                        removeItemFromSum(item);
                        currentParty.getItems().remove(i);
                        mRecyclerView.getAdapter().notifyItemRemoved(i);
                        partyItemsReference.removeValue();
                        partyItemsReference.setValue(currentParty.getItems());
                    }
                }
                actionMode.finish();
                return true;
            }

            //редактирование
            if (menuItem.getItemId() == R.id.action_edit) {
                ArrayList<Integer> indexes = (ArrayList<Integer>) mMultiSelector.getSelectedPositions();
                if (indexes.size() == 1) {
                    final Item editableItem = currentParty.getItems().get(indexes.get(0));
                    final int index = indexes.get(0);
                    EditItemDialogFragment dialog = EditItemDialogFragment.newInstance(editableItem.getName(), editableItem.getWhoBrings(), editableItem.getQuantity(), editableItem.getPrice());

                    dialog.setListener(new EditItemDialogFragment.OnItemClickListener() {
                        @Override
                        public void onItemClick(String whatToBuy, Guest whoBuy, int quantity, int price) {
                            //я вполне допускаю, что price может быть == 0. Например вы вносите в список клубничку, которую сорвете у себя на даче,
                            // вам надо не забыть её сорвать, а следовательно внести список, но стоимость её будет == 0

                            removeItemFromSum(editableItem);
                            editableItem.setName(whatToBuy);
                            editableItem.setWhoBrings(whoBuy);
                            editableItem.setQuantity(quantity);
                            editableItem.setPrice(price);
                            addItemToSum(editableItem);
                            adapter.notifyItemChanged(index);

                            partyItemsReference.child(String.valueOf(index)).setValue(currentParty.getItems().get(index));
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

    public static ItemsListFragment newInstance() {
        return new ItemsListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //получаем информацию о вечеринке от родительской активити
        PartyDetailsActivity activity = (PartyDetailsActivity) getActivity();
        this.currentParty = activity.getCurrentParty();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        partyItemsReference = databaseReference.child("parties").child(this.currentParty.getKey()).child("items");

        View view = inflater.inflate(R.layout.fragment_items_list, container, false);

        //находим вьюшки
        // this.progressBar = (ProgressBar) view.findViewById(R.id.items_list_progressBar);
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.items_list_recycler_view);
        this.addItemFAB = (FloatingActionButton) view.findViewById(R.id.items_list_add_item_fab);
        this.wholeSumTxt = (TextView) view.findViewById(R.id.items_list_res_sum_number_txt);
        this.sumPerOneTxt = (TextView) view.findViewById(R.id.items_list_personal_sum_number_txt);

        //инициализируем
        initRecycler();
        initSum();
        addItemFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddItemActivity.class);
                // 2 - requestCode
                startActivityForResult(intent, 2);
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
        // progressBar.setVisibility(ProgressBar.INVISIBLE);
        attachDatabaseReadListener();
        // attachStopProgressBarListener();
        refreshSumPerOne();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        Item newItem = (Item) data.getSerializableExtra("item");

        String index = String.valueOf(currentParty.getItems().size());
        DatabaseReference reference = partyItemsReference.child(index);
        reference.setValue(newItem);

        this.currentParty.getItems().add(newItem);
        this.adapter.notifyItemInserted(this.currentParty.getItems().size());
        //пересчитываем сумму
        addItemToSum(newItem);

    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    private void getItemsFromDatabase() {

    }

    /**
     * настраивает работу адаптера и RecyclerView
     */
    private void initRecycler() {

        this.adapter = new ItemAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        this.mRecyclerView.setLayoutManager(layoutManager);
        this.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        this.mRecyclerView.setAdapter(adapter);
    }

    /**
     * рассчитыват сумму, складывающуюся из стоимости каждой покупки в списке
     */
    private void initSum() {

        for (Item item : currentParty.getItems()) {
            wholeSum += item.getPrice() * item.getQuantity();
        }
        sumPerOne = wholeSum / currentParty.getGuests().size();
        this.wholeSumTxt.setText(String.valueOf(wholeSum));
        this.sumPerOneTxt.setText(String.valueOf(sumPerOne));
    }

    private void addItemToSum(Item item) {
        wholeSum += item.getPrice() * item.getQuantity();
        this.wholeSumTxt.setText(String.valueOf(wholeSum));
        sumPerOne = wholeSum / currentParty.getGuests().size();
        this.sumPerOneTxt.setText(String.valueOf(sumPerOne));
    }

    private void removeItemFromSum(Item item) {
        wholeSum -= item.getPrice() * item.getQuantity();
        this.wholeSumTxt.setText(String.valueOf(wholeSum));
        sumPerOne = wholeSum / currentParty.getGuests().size();
        this.sumPerOneTxt.setText(String.valueOf(sumPerOne));
    }

    private void refreshSumPerOne() {
        sumPerOne = wholeSum / currentParty.getGuests().size();
        this.sumPerOneTxt.setText(String.valueOf(sumPerOne));
    }

    /**
     * метод для запуска слушателя, который слушает изменение данных в бд
     */
    private void attachDatabaseReadListener() {
        if (itemEventListener == null) {
            itemEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Item item = dataSnapshot.getValue(Item.class);
                    //этот же кусочек исполняетс для загрузки покупок из вечеринки впервые
                    if ((!currentParty.getItems().contains(item))) {
                        currentParty.getItems().add(item);
                        adapter.notifyItemInserted(currentParty.getItems().size());
                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        int position = Integer.valueOf(dataSnapshot.getKey());
                        if (position != -1) {
                            currentParty.getItems().set(position, item);
                            adapter.notifyItemChanged(position);
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        int position = currentParty.getItems().indexOf(item);
                        if (position != -1) {
//
//                            partyItemsReference.removeValue();
//                            for (int i = 0; i < currentParty.getItems().size(); i++) {
//                                partyItemsReference.child(String.valueOf(i)).setValue(currentParty.getItems().get(i));
//                            }
//                        }

                            currentParty.getItems().remove(position);
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
            partyItemsReference.addChildEventListener(itemEventListener);
        }
    }


    /**
     * метод для остановки слушателя, который слушает изменение данных в бд
     */
    private void detachDatabaseReadListener() {
        if (itemEventListener != null) {
            partyItemsReference.removeEventListener(itemEventListener);
            itemEventListener = null;
        }
    }

    private void attachStopProgressBarListener() {
        partyItemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
//                progressBar.setVisibility(ProgressBar.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обычно вызывается, когда нет прав на чтение данных из базы
                Log.d(FIREBASE_ERROR, "onCancelled: " + databaseError);
            }
        });
    }

    private class ItemViewHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView itemName;
        public TextView quantity;
        public TextView quantityInPrice;
        public TextView price;
        public TextView whoBrings;

        public ItemViewHolder(View itemView) {
            super(itemView, mMultiSelector);
            this.itemName = (TextView) itemView.findViewById(R.id.item_name_txt);
            this.quantity = (TextView) itemView.findViewById(R.id.item_quantity_txt);
            this.quantityInPrice = (TextView) itemView.findViewById(R.id.item_quantity_in_price_txt);
            this.price = (TextView) itemView.findViewById(R.id.item_price_txt);
            this.whoBrings = (TextView) itemView.findViewById(R.id.item_who_brings_name_txt);

            itemView.setLongClickable(true);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mMultiSelector.isSelectable()) {
                // Selection is active; toggle activation
                setActivated(!isActivated());
                mMultiSelector.setSelected(ItemViewHolder.this, isActivated());
                if (mMultiSelector.getSelectedPositions().size() == 0) {
                    actionMode.finish();
                }
            } else {
                // Selection not active
            }
        }

        @Override
        public boolean onLongClick(View view) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            mMultiSelector.setSelected(ItemViewHolder.this, true);
            return true;

        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {


        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appearence, parent, false);
            return new ItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            Item item = (Item) currentParty.getItems().get(position);

            holder.itemName.setText(item.getName());
            holder.quantity.setText(String.valueOf(item.getQuantity()));
            holder.quantityInPrice.setText(String.valueOf(item.getQuantity()));
            holder.whoBrings.setText(item.getWhoBrings().getGuestName());
            holder.price.setText(String.valueOf(item.getPrice()));
        }

        @Override
        public int getItemCount() {
            return currentParty.getItems().size();
        }
    }

}
