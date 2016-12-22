package com.vsu.nastya.partymanager.PartyList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vsu.nastya.partymanager.Logic.DateWorker;
import com.vsu.nastya.partymanager.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by nastya on 06.12.16.
 */

public class PartiesAdapter extends RecyclerView.Adapter<PartiesAdapter.PartyViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(View v, Party party);
    }

    private List<Party> partiesList;
    private OnItemClickListener listener;

    public class PartyViewHolder extends RecyclerView.ViewHolder {
        public TextView partyName, date;

        public PartyViewHolder(View itemView) {
            super(itemView);

            partyName = (TextView) itemView.findViewById(R.id.parties_item_name);
            date = (TextView) itemView.findViewById(R.id.parties_item_date);
        }
    }

    public PartiesAdapter(List<Party> partiesList,  OnItemClickListener listener) {
        this.partiesList = partiesList;
        this.listener = listener;
    }

    @Override
    public PartyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.party_list_item_appereance, parent, false);
        return new PartyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PartyViewHolder holder, int position) {
        final Party party = partiesList.get(position);
        holder.partyName.setText(party.getName());
        String date = DateWorker.getDateAsString(party.getDate()) + ", " +
                DateWorker.getTimeAsString(party.getDate());
        holder.date.setText(date);

        if (listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, party);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return partiesList.size();
    }
}
