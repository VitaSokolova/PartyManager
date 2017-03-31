package com.vsu.nastya.partymanager.logic;

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
    public static String getVkFriendIdByName(String name, ArrayList<Friend> arrayListFriends) {
        String vkId = null;
        for (Friend friend : arrayListFriends) {
            if (friend.toString().equals(name)) {
                vkId = friend.getVkId();
                break;
            }
        }
        return vkId;
    }
}

