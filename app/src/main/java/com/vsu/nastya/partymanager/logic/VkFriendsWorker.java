package com.vsu.nastya.partymanager.logic;

import com.vsu.nastya.partymanager.guest_list.data.Guest;

import java.util.ArrayList;

/**
 * Created by Vita Sokolova on 31.03.2017.
 */

public abstract class VkFriendsWorker {

    public static String[] getVkFriendsArray(ArrayList<Friend> arrayListFriends) {
        String[] arrayFriends = new String[arrayListFriends.size()];
        for (int i = 0; i < arrayListFriends.size(); i++) {
            arrayFriends[i] = arrayListFriends.get(i).toString();
        }
        return arrayFriends;
    }

    /**
     * @param name текст из поля ввода
     * @return id этого друга
     */
    public static Guest getVkFriendIdByName(String name, ArrayList<Friend> arrayListFriends) {
        String vkId = null;
        String vkPhotoUrl = null;
        for (Friend friend : arrayListFriends) {
            if (friend.toString().equals(name)) {
                vkId = friend.getVkId();
                vkPhotoUrl = friend.getVkPhotoUrl();
                break;
            }
        }
        return new Guest(vkId, vkPhotoUrl, name);

    }
}

