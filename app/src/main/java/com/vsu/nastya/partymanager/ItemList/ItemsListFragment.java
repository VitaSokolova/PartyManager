package com.vsu.nastya.partymanager.ItemList;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vsu.nastya.partymanager.GuestList.AddGuestActivity;
import com.vsu.nastya.partymanager.GuestList.Guest;
import com.vsu.nastya.partymanager.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Вита on 08.12.2016.
 */
public class ItemsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageButton addItemFAB;
    private TextView wholeSumTxt;

    private ArrayList<Item> itemList;
    private ItemAdapter adapter;
    double sumPerOne = 0;
    int wholeSum = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_items_list, container, false);
        //находим
        this.recyclerView = (RecyclerView) view.findViewById(R.id.items_list_recycler_view);
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


        this.adapter = new ItemAdapter(itemList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        this.recyclerView.setLayoutManager(layoutManager);
        this.recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.recyclerView.setAdapter(adapter);
    }

    private void initSum() {

        for (Item item : itemList) {
            wholeSum += item.getPrice()*item.getQuantity();
        }
        this.wholeSumTxt.setText(String.valueOf(wholeSum));

        //TODO: как только мы получим вечеринку на эту активити, можно будет запросить количество гостей и заполнить второй текствью с суммой на человека
    }

    private void addItemToSum(Item item){
        wholeSum+=item.getPrice()*item.getQuantity();
        this.wholeSumTxt.setText(String.valueOf(wholeSum));
    }

    private void removeItemFromSum(Item item){
        wholeSum-=item.getPrice()*item.getQuantity();
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
}
