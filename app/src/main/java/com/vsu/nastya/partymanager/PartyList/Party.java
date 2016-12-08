package com.vsu.nastya.partymanager.PartyList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by nastya on 06.12.16.
 */

public class Party {
    private String name;
    private Calendar date;

    public Party(){
        name = "";
        date = Calendar.getInstance();
    }

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

    public String getDateAndTimeAsString(){
        Locale myLocale = new Locale("ru","RU");
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("d MMM, H:mm", myLocale);
        return simpleDateFormat.format(date.getTime());
    }

    public String getDateAsString(){
        Locale myLocale = new Locale("ru","RU");
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("d MMM", myLocale);
        return simpleDateFormat.format(date.getTime());
    }

    public String getTimeAsString(){
        Locale myLocale = new Locale("ru","RU");
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("H:mm", myLocale);
        return simpleDateFormat.format(date.getTime());
    }
}

