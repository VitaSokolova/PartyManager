package com.vsu.nastya.partymanager.ItemList;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.vsu.nastya.partymanager.AddItemActivity;
import com.vsu.nastya.partymanager.GuestList.Guest;
import com.vsu.nastya.partymanager.R;

import java.util.ArrayList;

/**
 * Created by Вита on 08.12.2016.
 */
public class ItemsListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ImageButton addItemFAB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_items_list, container, false);
        //находим
        this.recyclerView = (RecyclerView) view.findViewById(R.id.items_list_recycler_view);
        this.addItemFAB = (ImageButton) view.findViewById(R.id.items_list_add_item_fab);
        //инициализируем
        initRecycler();
        addItemFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddItemActivity.start(view.getContext());
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static ItemsListFragment newInstance(){
        return  new ItemsListFragment();
    }

    private void initRecycler() {

        //TODO: вытащить это список из экземпляра User, но пока пусть так
        final ArrayList<Item> itemList = new ArrayList<>();
        itemList.add(new Item("Очень вкусный тортик", 2, new Guest("Вита"), 400));
        itemList.add(new Item("Фрукты", 1, new Guest("Настя"), 500));
        itemList.add(new Item("Сок", 4, new Guest("Вита"), 80));
        itemList.add(new Item("Салфетки", 1, new Guest("Вита"), 50));


        final ItemAdapter adapter = new ItemAdapter(itemList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

}
