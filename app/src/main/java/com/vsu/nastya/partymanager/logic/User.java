package com.vsu.nastya.partymanager.logic;

import com.google.firebase.database.Exclude;
import com.vk.sdk.VKAccessToken;
import com.vsu.nastya.partymanager.party_list.Party;

import java.util.ArrayList;

/**
 * Created by Вита on 08.12.2016.
 */

//создала я этот класс и крепко задумалась о том как получать из него эти данные из всех точек
//в голову пришел Singleton, но это надо обсудить

public class User {

    private String firstName;
    private String lastName;
    private String vkId;
    @Exclude
    private VKAccessToken token;
    private ArrayList<String> partiesIdList = new ArrayList<>();
    @Exclude
    private ArrayList<Friend> friendsList = new ArrayList<>();

    private User() {
    }

    private static class UserHolder {
        private static final User INSTANCE = new User();
    }

    public static User getInstance() {
        return UserHolder.INSTANCE;
    }

    public void init(String firstName, String lastName, String vkId, ArrayList<String> partyList) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.vkId = vkId;
        this.partiesIdList = partyList;
    }

    public static void setUser() {
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

    public void setLastName(String surname) {
        this.lastName = surname;
    }

    public String getVkId() {
        return vkId;
    }

    public void setVkId(String vkId) {
        this.vkId = vkId;
    }

    public ArrayList<String> getPartiesIdList() {
        return partiesIdList;
    }

    public void setPartiesIdList(ArrayList<String> partiesIdList) {
        this.partiesIdList = partiesIdList;
    }

    public VKAccessToken getToken() {
        return token;
    }

    public void setToken(VKAccessToken token) {
        this.token = token;
    }

    public ArrayList<Friend> getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(ArrayList<Friend> friendsList) {
        this.friendsList = friendsList;
    }

    public void addFriend(Friend friend) {
        friendsList.add(friend);
    }

//    public String[] getFriendsNames() {
//
//    }
}
