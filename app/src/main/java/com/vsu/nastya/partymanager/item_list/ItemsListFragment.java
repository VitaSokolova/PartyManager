package com.vsu.nastya.partymanager.item_list;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.item_list.data.Item;

import java.util.ArrayList;

/**
 * Created by Вита on 08.12.2016.
 */
public class ItemsListFragment extends Fragment {
    public static String TAG = "itemListFragment";
    private RecyclerView mRecyclerView;
    private ImageButton addItemFAB;
    private TextView wholeSumTxt;
    private ArrayList<Item> itemList;
    private ItemAdapter adapter;
    private ActionMode actionMode;
    double sumPerOne = 0;
    int wholeSum = 0;

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
                for (int i = itemList.size(); i >= 0; i--) {
                    if (mMultiSelector.isSelected(i, 0)) {
                        //TODO:удалить еще и из базы и вообще навсегда
                        removeItemFromSum(itemList.get(i));
                        itemList.remove(i);
                        mRecyclerView.getAdapter().notifyItemRemoved(i);
                    }
                }
                actionMode.finish();
                return true;
            }

            //редактирование
            if (menuItem.getItemId() == R.id.action_edit) {
                final ArrayList<Integer> indexes = (ArrayList<Integer>) mMultiSelector.getSelectedPositions();
                if (indexes.size() == 1) {
                    final Item editableItem = itemList.get(indexes.get(0));

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
                            adapter.notifyItemChanged(indexes.get(0));
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_items_list, container, false);
        //находим
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.items_list_recycler_view);
        this.addItemFAB = (ImageButton) view.findViewById(R.id.items_list_add_item_fab);
        this.wholeSumTxt = (TextView) view.findViewById(R.id.items_list_res_sum_number_txt);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        if (mMultiSelector != null) {
            Bundle bundle = savedInstanceState;
            if (bundle != null) {
                mMultiSelector.restoreSelectionStates(bundle.getBundle(TAG));
            }

            if (mMultiSelector.isSelectable()) {
                if (mActionModeCallback != null) {
                    mActionModeCallback.setClearOnPrepare(false);
                    ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
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

    public static ItemsListFragment newInstance() {
        return new ItemsListFragment();
    }

    private void initRecycler() {

        //TODO: вытащить это список из экземпляра User, но пока пусть так
        this.itemList = new ArrayList<>();
        this.itemList.add(new Item("Очень вкусный тортик", 2, new Guest("Вита"), 400));
        this.itemList.add(new Item("Фрукты", 1, new Guest("Настя"), 500));
        this.itemList.add(new Item("Сок", 4, new Guest("Вита"), 80));
        this.itemList.add(new Item("Салфетки", 1, new Guest("Вита"), 50));


        this.adapter = new ItemAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        this.mRecyclerView.setLayoutManager(layoutManager);
        this.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        this.mRecyclerView.setAdapter(adapter);
    }

    private void initSum() {

        for (Item item : itemList) {
            wholeSum += item.getPrice() * item.getQuantity();
        }
        this.wholeSumTxt.setText(String.valueOf(wholeSum));

        //TODO: как только мы получим вечеринку на эту активити, можно будет запросить количество гостей и заполнить второй текствью с суммой на человека
    }

    private void addItemToSum(Item item) {
        wholeSum += item.getPrice() * item.getQuantity();
        this.wholeSumTxt.setText(String.valueOf(wholeSum));
    }

    private void removeItemFromSum(Item item) {
        wholeSum -= item.getPrice() * item.getQuantity();
        this.wholeSumTxt.setText(String.valueOf(wholeSum));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        Item newItem = (Item) data.getSerializableExtra("item");
        this.itemList.add(newItem);
        this.adapter.notifyItemInserted(this.itemList.size());
        //пересчитываем сумму
        addItemToSum(newItem);

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
            Item item = (Item) itemList.get(position);

            holder.itemName.setText(item.getName());
            holder.quantity.setText(String.valueOf(item.getQuantity()));
            holder.quantityInPrice.setText(String.valueOf(item.getQuantity()));
            holder.whoBrings.setText(item.getWhoBrings().getGuestName());
            holder.price.setText(String.valueOf(item.getPrice()));
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }
    }

}
