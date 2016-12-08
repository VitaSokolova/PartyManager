package com.vsu.nastya.partymanager.GuestList;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.vsu.nastya.partymanager.AddGuestActivity;
import com.vsu.nastya.partymanager.ItemList.Item;
import com.vsu.nastya.partymanager.ItemList.ItemAdapter;
import com.vsu.nastya.partymanager.R;

import java.util.ArrayList;
/**
 * Created by Вита on 08.12.2016.
 */
public class GuestListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ImageButton addGuestFab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guest_list, container, false);

        this.recyclerView = (RecyclerView) view.findViewById(R.id.guest_list_recycler_view);
        this.addGuestFab = (ImageButton) view.findViewById(R.id.guest_list_add_fab);

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

    private void initRecycler() {
        //TODO: вытащить это список из экземпляра User, но пока пусть так
        final ArrayList<Guest> guestList = new ArrayList<>();
        guestList.add(new Guest("Вита"));
        guestList.add(new Guest("Настя"));

        final GuestAdapter adapter = new GuestAdapter(guestList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }
}

