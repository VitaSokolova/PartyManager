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

    public interface OnLongClickListener {
        void onLongClick(View view, Guest guest, int position);
    }

    private ArrayList guestList;
    private OnLongClickListener longListener;

    public GuestAdapter(ArrayList guestList, OnLongClickListener longListener) {
        this.guestList = guestList;
        this.longListener = longListener;
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
    public void onBindViewHolder(final GuestViewHolder holder, int position) {
        final Guest guest = (Guest) guestList.get(position);

        if (longListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    longListener.onLongClick(view, guest, holder.getAdapterPosition());
                    holder.itemView.setSelected(true);
                    return true;
                }
            });
        }

        holder.nameTxtView.setText(guest.getGuestName());
    }

    @Override
    public int getItemCount() {
        return guestList.size();
    }
}
