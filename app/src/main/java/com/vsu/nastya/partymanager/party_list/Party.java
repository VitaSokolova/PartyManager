package com.vsu.nastya.partymanager.party_list;

import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.item_list.data.Item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nastya on 06.12.16.
 */
public class Party implements Serializable {

    private String key;
    private String name;
    private long date;
    private ArrayList<Item> items;
    private ArrayList<Guest> guests;
    //Еще потом добавим place и messages

    public Party() {}

    public Party(String partyName, long date) {
        this.name = partyName;
        this.date = date;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String party) {
        this.name = party;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public List<Guest> getGuests() {
        return guests;
    }

    public void setGuests(ArrayList<Guest> guests) {
        this.guests = guests;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

