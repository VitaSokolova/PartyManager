package com.vsu.nastya.partymanager.logic;

import com.vk.sdk.VKAccessToken;
import com.vsu.nastya.partymanager.party_list.Party;

import java.util.ArrayList;

/**
 * Created by Вита on 08.12.2016.
 */

//создала я этот класс и крепко задумалась о том как получать из него эти данные из всех точек
//в голову пришел Singleton, но это надо обсудить

public class User {

    private static User user;

    private String firstName;
    private String lastName;
    private String vkId;
    private VKAccessToken token;
    private ArrayList<Party> partyList;

   /* private User() {}

    public static User getInstance() {
        if (user == null) {
                user = new User();
        }
        return user;
    }*/

    public User() {}

    public User(String firstName, String lastName, String vkId, VKAccessToken token, ArrayList<Party> partyList) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.vkId = vkId;
        this.token = token;
        this.partyList = partyList;
    }

    public static void setUser() {}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String surname) {
        this.lastName = surname;
    }

    public String getVkId() {
        return vkId;
    }

    public void setVkId(String vkId) {
        this.vkId = vkId;
    }

    public ArrayList<Party> getPartyList() {
        return partyList;
    }

    public void setPartyList(ArrayList<Party> partyList) {
        this.partyList = partyList;
    }

    public VKAccessToken getToken() {
        return token;
    }

    public void setToken(VKAccessToken token) {
        this.token = token;
    }

}
