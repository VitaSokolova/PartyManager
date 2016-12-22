package com.vsu.nastya.partymanager.Logic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Класс для работы с представлением даты и времени
 */
public class DateWorker {

    public static String getDateAsString(Calendar date){
        Locale myLocale = Locale.getDefault();
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("d MMM", myLocale);
        return simpleDateFormat.format(date.getTime());
    }

    public static String getTimeAsString(Calendar date){
        Locale myLocale = Locale.getDefault();
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("H:mm", myLocale);
        return simpleDateFormat.format(date.getTime());
    }
}
