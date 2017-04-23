package com.vsu.nastya.partymanager.logic;

import com.google.firebase.database.Exclude;
import com.vk.sdk.VKAccessToken;

import java.util.ArrayList;

public class User extends VkUser {

    @Exclude
    private VKAccessToken token;
    private String notificationToken;
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

    public void init(String firstName, String lastName, String vkId, ArrayList<String> partyList, String vkPhotoUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.vkId = vkId;
        this.partiesIdList = partyList;
        this.vkPhotoUrl = vkPhotoUrl;
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

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }


    public String getNotificationToken() {
        return notificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = notificationToken;
    }
}
