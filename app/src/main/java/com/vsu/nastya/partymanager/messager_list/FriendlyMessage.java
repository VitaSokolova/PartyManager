package com.vsu.nastya.partymanager.messager_list;

import java.io.Serializable;

/**
 * Created by Vita Sokolova on 01.04.2017.
 */

public class FriendlyMessage implements Serializable {

    private String text;
    private String name;
    private String photoUrl;

    public FriendlyMessage() {
    }

    public FriendlyMessage(String text, String name, String photoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    @Override
    public boolean equals(Object obj) {
        boolean retVal = false;
        boolean isTextEqual = false;
        boolean isNameEqual = false;
        boolean isUrlEqual = false;

        if (obj instanceof FriendlyMessage) {
            FriendlyMessage ptr = (FriendlyMessage) obj;

            isTextEqual = isStringsEqual(ptr.text, this.text);

            isNameEqual = isStringsEqual(ptr.name, this.name);

            isUrlEqual = isStringsEqual(ptr.photoUrl, this.photoUrl);

            retVal = isTextEqual && isNameEqual && isUrlEqual;
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

    private boolean isStringsEqual(String s1, String s2) {

        if ((s1 == null) && (s2 == null)) {
            return true;
        } else if ((s1 != null)) {
            return s1.equals(this.photoUrl);
        } else {
            return false;
        }
    }
}
