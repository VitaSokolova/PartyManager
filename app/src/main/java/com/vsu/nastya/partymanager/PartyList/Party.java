package com.vsu.nastya.partymanager.PartyList;

import java.util.Calendar;

/**
 * Created by nastya on 06.12.16.
 */

public class Party {
    private String name;
    private Calendar date;

    public Party(String partyName, Calendar date) {
        this.name = partyName;
        this.date = date;
    }

    public Calendar getDateTime() {
        return date;
    }

    public void setDateTime(Calendar dateTime) {
        this.date = dateTime;
    }

    public String getPartyName() {
        return name;
    }

    public void setPartyName(String partyName) {
        this.name = partyName;
    }
}

