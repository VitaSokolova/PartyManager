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

        if (obj instanceof FriendlyMessage) {
            FriendlyMessage ptr = (FriendlyMessage) obj;
            //еще бы whoBrings сравнить
            retVal = (ptr.text.equals(this.text)) && (ptr.name.equals(this.name)) && (((ptr.photoUrl == null) && (this.photoUrl == null)) ||
                    (ptr.photoUrl.equals(this.photoUrl)));
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


}
