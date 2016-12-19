package com.vsu.nastya.partymanager.GuestList;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.vsu.nastya.partymanager.R;

import java.util.ArrayList;

/**
 * Created by Вита on 08.12.2016.
 */
public class GuestListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ImageButton addGuestFab;
    private ArrayList<Guest> guestList;
    private GuestAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guest_list, container, false);

        this.guestList = new ArrayList<>();
        this.adapter = new GuestAdapter(guestList);

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

    public static GuestListFragment newInstance() {
        return new GuestListFragment();
    }

    private void initRecycler() {
        //TODO: вытащить это список из экземпляра User, но пока пусть так
        guestList.add(new Guest("Вита"));
        guestList.add(new Guest("Настя"));


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        Guest newGuest = (Guest) data.getSerializableExtra("guest");
        this.guestList.add(newGuest);
        this.adapter.notifyItemInserted(this.guestList.size());
    }
}

