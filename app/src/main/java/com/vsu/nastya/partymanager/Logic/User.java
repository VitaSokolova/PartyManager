package com.vsu.nastya.partymanager.logic;

import com.vsu.nastya.partymanager.party_list.Party;

import java.util.ArrayList;

/**
 * Created by Вита on 08.12.2016.
 */

//создала я этот класс и крепко задумалась о том как получать из него эти данные из всех точек
//в голову пришел Singleton, но это надо обсудить

public class User {
    private String name;
    private String vkId;
    private ArrayList<Party> partyList;

    public User(String name, String vkId) {
        this.name = name;
        this.vkId = vkId;
        this.partyList = new ArrayList<Party>();
    }

    public User(String name, String vkId, ArrayList<Party> partyList) {
        this.name = name;
        this.vkId = vkId;
        this.partyList = partyList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
