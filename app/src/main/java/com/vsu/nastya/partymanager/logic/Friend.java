package com.vsu.nastya.partymanager.logic;

/**
 * Created by Комп on 04.03.2017.
 */

public class Friend {
    private String firstName;
    private String lastName;
    private String vkId;

    public Friend() {
    }

    public Friend(String firstName, String lastName, String vkId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.vkId = vkId;
    }

    @Override
    public String toString(){
        return this.getFirstName() + " " + this.getLastName();
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

}
