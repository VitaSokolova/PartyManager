package com.vsu.nastya.partymanager.messager_list;

import java.io.Serializable;

/**
 * Created by Vita Sokolova on 01.04.2017.
 */

public class FriendlyMessage implements Serializable {

    private String text;
    private String name;
    private String photoUrl;
    private long time;


    public FriendlyMessage() {
    }

    public FriendlyMessage(String text, String name, String photoUrl, long time) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.time = time;
    }

    @Override
    public boolean equals(Object obj) {
        boolean retVal = false;
        boolean isTextEqual = false;
        boolean isNameEqual = false;
        boolean isUrlEqual = false;
        boolean isTimeEqual = false;

        if (obj instanceof FriendlyMessage) {
            FriendlyMessage ptr = (FriendlyMessage) obj;

            isTextEqual = isStringsEqual(ptr.text, this.text);
            isNameEqual = isStringsEqual(ptr.name, this.name);
            isUrlEqual = isStringsEqual(ptr.photoUrl, this.photoUrl);
            isTimeEqual = ptr.time == this.time;

            retVal = isTextEqual && isNameEqual && isUrlEqual && isTimeEqual;
        }

        return retVal;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    private boolean isStringsEqual(String s1, String s2) {

        if ((s1 == null) && (s2 == null)) {
            return true;
        } else if ((s1 != null)) {
            return s1.equals(s2);
        } else {
            return false;
        }
    }
}
