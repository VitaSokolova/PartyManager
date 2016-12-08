package com.vsu.nastya.partymanager.GuestList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vsu.nastya.partymanager.R;

import java.util.ArrayList;

/**
 * Created by Вита on 01.12.2016.
 */

public class GuestAdapter extends RecyclerView.Adapter<GuestAdapter.GuestViewHolder> {
    private ArrayList guestList;

    public GuestAdapter(ArrayList guestList) {
        this.guestList = guestList;
    }

    public class GuestViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTxtView;
        public GuestViewHolder(View itemView) {
            super(itemView);
            this.nameTxtView = (TextView) itemView.findViewById(R.id.guest_name_txt);
        }

    }

    @Override
    public GuestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.guest_list_item_appearence, parent, false);
        return new GuestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder (GuestViewHolder holder, int position) {
        Guest guest = (Guest) guestList.get(position);
        holder.nameTxtView.setText(guest.getGuestName());
    }

    @Override
    public int getItemCount() {
        return guestList.size();
    }
}
