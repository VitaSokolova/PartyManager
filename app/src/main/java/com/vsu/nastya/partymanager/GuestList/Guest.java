package com.vsu.nastya.partymanager.GuestList;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Комп on 01.12.2016.
 */

public class Guest implements Serializable {
    private String guestName; public Guest(String guestName) {

        this.guestName = guestName;
    }

    public String getGuestName() {
        return guestName;
    }


}
