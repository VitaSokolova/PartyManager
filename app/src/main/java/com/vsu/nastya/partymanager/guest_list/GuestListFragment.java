package com.vsu.nastya.partymanager.guest_list;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.guest_list.data.Guest;

import java.util.ArrayList;

/**
 * Created by Вита on 08.12.2016.
 * Это фрагмент для списка гостей
 */
public class GuestListFragment extends Fragment {
    public static String TAG = "guestListFragment";
    private RecyclerView mRecyclerView;
    private ImageButton addGuestFab;
    private ArrayList<Guest> guestList;
    private GuestAdapter adapter;
    private MultiSelector mMultiSelector = new MultiSelector();
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
                    }
                }
                actionMode.finish();
                return true;
            }

            //TODO:сделать редактирование
            if (menuItem.getItemId() == R.id.action_edit) {
                for (int i = guestList.size(); i >= 0; i--) {
                    if (mMultiSelector.isSelected(i, 0)) {
                        EditDialogFragment dialog = new EditDialogFragment();
                        final int finalI = i;
                        dialog.setListener(new EditDialogFragment.OnItemClickListener() {
                            @Override
                            public void onItemClick(String text) {
                                Guest guest = guestList.get(finalI);
                                guest.setGuestName(text);
                                adapter.notifyItemChanged(finalI);
                            }
                        });
                        dialog.show(getFragmentManager(), "dialog");
                    }
                    return true;
                }
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guest_list, container, false);

        this.guestList = new ArrayList<>();
        this.adapter = new GuestAdapter();

        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.guest_list_recycler_view);
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
    //получаем нашего нового гостя
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

    //инициализирует RecyclerView
    private void initRecycler() {
        //TODO: вытащить это список из экземпляра User, но пока пусть так
        guestList.add(new Guest("Вита"));
        guestList.add(new Guest("Настя"));
        guestList.add(new Guest("Вова"));
        guestList.add(new Guest("Аня"));


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);
    }

    //эти внутренние классы для адаптера и ViewHolder предлагает сюда поместить автор библиотеки для множественного выбора
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
            ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            mMultiSelector.setSelected(GuestViewHolder.this, true);
            return true;
        }

        @Override
        public void onClick(View view) {
            if (mMultiSelector.isSelectable()) {
                // Selection is active; toggle activation
                setActivated(!isActivated());
                mMultiSelector.setSelected(GuestViewHolder.this, isActivated());
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

