package com.vsu.nastya.partymanager.PartyList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vsu.nastya.partymanager.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by nastya on 06.12.16.
 */

public class PartiesAdapter extends RecyclerView.Adapter<PartiesAdapter.PartyViewHolder>{

    private List<Party> partiesList;

    public class PartyViewHolder extends RecyclerView.ViewHolder {
        public TextView partyName, date;

        public PartyViewHolder(View itemView) {
            super(itemView);

            partyName = (TextView) itemView.findViewById(R.id.parties_item_name);
            date = (TextView) itemView.findViewById(R.id.parties_item_date);
        }
    }

    public PartiesAdapter(List<Party> partiesList){
        this.partiesList = partiesList;
    }

    @Override
    public PartyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.party_list_item_appereance, parent, false);
        return new PartyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PartyViewHolder holder, int position) {
        Party party = partiesList.get(position);
        holder.partyName.setText(party.getPartyName());
        String date = getDateAsString(party.getDateTime());
        holder.date.setText(date);
    }

    @Override
    public int getItemCount() {
        return partiesList.size();
    }

    private String getDateAsString(Calendar date){
        Locale myLocale = new Locale("ru","RU");
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("d MMM, H:mm", myLocale);
        return simpleDateFormat.format(date.getTime());
    }
}
