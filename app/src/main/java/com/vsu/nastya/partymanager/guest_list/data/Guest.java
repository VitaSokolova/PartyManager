package com.vsu.nastya.partymanager.guest_list.data;

import com.vsu.nastya.partymanager.item_list.data.Item;

import java.io.Serializable;

/**
 * Created by Вита on 01.12.2016.
 */

public class Guest implements Serializable {

    private String vkId;
    private String guestName;

    public Guest() {
    }

    public Guest(String vkId, String guestName) {
        this.vkId = vkId;
        this.guestName = guestName;
    }

    @Override
    public boolean equals(Object obj) {
        boolean retVal = false;

        if (obj instanceof Guest) {
            Guest ptr = (Guest) obj;
            retVal =((ptr.guestName.equals(this.guestName)) && ((ptr.vkId==null)&&(this.vkId==null)||(ptr.vkId.equals(this.vkId))));
        }

        return retVal;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getVkId() {
        return vkId;
    }

    public void setVkId(String vkId) {
        this.vkId = vkId;
    }

}
