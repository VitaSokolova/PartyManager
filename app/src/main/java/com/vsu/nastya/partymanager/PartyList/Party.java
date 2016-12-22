package com.vsu.nastya.partymanager.PartyList;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by nastya on 06.12.16.
 */
public class Party implements Serializable{
    private String name;
    private Calendar date;

    public Party(String partyName, Calendar date) {
        this.name = partyName;
        this.date = date;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String party) {
        this.name = party;
    }
}

