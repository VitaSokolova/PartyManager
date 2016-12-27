package com.vsu.nastya.partymanager.GuestList;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private ActionMode mActionMode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guest_list, container, false);

        this.guestList = new ArrayList<>();
        this.adapter = new GuestAdapter(guestList, new GuestAdapter.OnLongClickListener() {
            @Override
            public void onLongClick(View view, Guest item, int position) {
                GuestListFragment.this.mActionMode =
                        mActionMode = GuestListFragment.this.getActivity().startActionMode(new ActionBarCallBack(position));
               //TODO: пометить view как selected
            }
        });

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        Guest newGuest = (Guest) data.getSerializableExtra("guest");
        this.guestList.add(newGuest);
        this.adapter.notifyItemInserted(this.guestList.size());
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

    class ActionBarCallBack implements ActionMode.Callback {
        int position;

        public ActionBarCallBack(int position) {
            this.position = position;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            //инфлейт ресурса меню
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    guestList.remove(position);
                    adapter.notifyItemRemoved(position);
                    mode.finish(); //действие выбрано, закрыть контекстное меню
                    return true;
                case R.id.action_edit:
                    //TODO:вызвать окно редактирования
                    mode.finish(); //действие выбрано, закрыть контекстное меню
                    return true;
                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null; //обнуляется экземпляр ActionMode
        }
    }
}

