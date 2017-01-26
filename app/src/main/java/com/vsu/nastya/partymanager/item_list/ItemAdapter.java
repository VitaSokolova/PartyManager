package com.vsu.nastya.partymanager.item_list;

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

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    ArrayList<Item> itemArrayList;

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        public TextView itemName;
        public TextView quantity;
        public TextView quantityInPrice;
        public TextView price;
        public TextView whoBrings;

        public ItemViewHolder(View itemView) {
            super(itemView);
            this.itemName = (TextView) itemView.findViewById(R.id.item_name_txt);
            this.quantity = (TextView) itemView.findViewById(R.id.item_quantity_txt);
            this.quantityInPrice = (TextView) itemView.findViewById(R.id.item_quantity_in_price_txt);
            this.price = (TextView) itemView.findViewById(R.id.item_price_txt);
            this.whoBrings = (TextView) itemView.findViewById(R.id.item_who_brings_name_txt);
        }
    }

    public ItemAdapter(ArrayList<Item> itemArrayList) {
        this.itemArrayList = itemArrayList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appearence, parent, false);
        return new ItemAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Item item = (Item) itemArrayList.get(position);

        holder.itemName.setText(item.getName());
        holder.quantity.setText(String.valueOf(item.getQuantity()));
        holder.quantityInPrice.setText(String.valueOf(item.getQuantity()));
        holder.whoBrings.setText(item.getWhoBrings().getGuestName());
        holder.price.setText(String.valueOf(item.getPrice()));
    }

    @Override
    public int getItemCount() {
        return itemArrayList.size();
    }
}
