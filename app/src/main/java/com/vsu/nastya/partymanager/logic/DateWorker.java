package com.vsu.nastya.partymanager.logic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;

/**
 * Класс для работы с представлением даты и времени
 */
public class DateWorker {

    public static String getDateAsString(Calendar date){
        Locale myLocale = Locale.getDefault();
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("d MMM", myLocale);
        return simpleDateFormat.format(date.getTime());
    }

    public static String getDateAsString(long sec){
        Locale myLocale = Locale.getDefault();
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("d MMM", myLocale);
        return simpleDateFormat.format(sec);
    }

    public static String getTimeAsString(Calendar date){
        Locale myLocale = Locale.getDefault();
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("H:mm", myLocale);
        return simpleDateFormat.format(date.getTime());
    }

    public static String getTimeAsString(long sec){
       // Date date = new Date(sec);
        Locale myLocale = Locale.getDefault();
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("H:mm", myLocale);
        return simpleDateFormat.format(sec);
    }

    public static Calendar getCalendarFromMilliseconds(long sec) {
        Date date = new Date(sec);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}
