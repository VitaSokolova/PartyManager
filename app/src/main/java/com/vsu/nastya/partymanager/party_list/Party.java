package com.vsu.nastya.partymanager.party_list;

import com.vsu.nastya.partymanager.item_list.data.Item;
import com.vsu.nastya.partymanager.guest_list.data.Guest;
import com.vsu.nastya.partymanager.messager_list.FriendlyMessage;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by nastya on 06.12.16.
 */
public class Party implements Serializable {

    private String key;
    private String name;
    private long date;
    private String place;
    private String icon;
    private String comment;
    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<Guest> guests = new ArrayList<>();
    private ArrayList<FriendlyMessage> messagesList = new ArrayList<>();

    public Party() {
    }

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

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public ArrayList<Guest> getGuests() {
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

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public ArrayList<FriendlyMessage> getMessagesList() {
        return messagesList;
    }

    public void setMessagesList(ArrayList<FriendlyMessage> messagesList) {
        this.messagesList = messagesList;
    }

    public String[] giveMePleaseGuestsNames() {
        String[] arrayGuests = new String[guests.size()];
        for (int i = 0; i < guests.size(); i++) {
            arrayGuests[i] = guests.get(i).getGuestName();
        }
        return arrayGuests;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

