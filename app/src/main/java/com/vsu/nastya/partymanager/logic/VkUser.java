package com.vsu.nastya.partymanager.logic;

/**
 * Created by vita7 on 23.04.2017.
 */

public abstract class VkUser {
    public String firstName;
    public String lastName;
    public String vkId;
    public String vkPhotoUrl;

    public VkUser() {
    }

    public VkUser(String firstName, String lastName, String vkId, String vkPhotoUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.vkId = vkId;
        this.vkPhotoUrl = vkPhotoUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
}
