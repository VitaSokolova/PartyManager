package com.vsu.nastya.partymanager.guest_list.data;

import java.io.Serializable;

/**
 * Created by vita7 on 23.04.2017.
 */

public class Guest implements Serializable {
    private String vkId;
    private String vkPhotoUrl;
    private String guestName;

    public Guest() {
    }

    public Guest(String vkId, String vkPhotoUrl, String guestName) {
        this.vkId = vkId;
        this.vkPhotoUrl = vkPhotoUrl;
        this.guestName = guestName;
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

    public String getVkPhotoUrl() {
        return vkPhotoUrl;
    }

    public void setVkPhotoUrl(String vkPhotoUrl) {
        this.vkPhotoUrl = vkPhotoUrl;
    }

    @Override
    public boolean equals(Object obj) {
        boolean retVal = false;

        if (obj instanceof Guest) {
            Guest ptr = (Guest) obj;
            retVal = ((ptr.guestName.equals(this.guestName)) && ((ptr.vkId == null) && (this.vkId == null) || (ptr.vkId.equals(this.vkId))));
        }

        return retVal;
    }
}
