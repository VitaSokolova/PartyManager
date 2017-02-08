package com.vsu.nastya.partymanager.guest_list.data;

import java.io.Serializable;

/**
 * Created by Вита on 01.12.2016.
 */

public class Guest implements Serializable {

    private String guestName;

    public Guest(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }
}
