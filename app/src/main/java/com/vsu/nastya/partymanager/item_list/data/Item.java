package com.vsu.nastya.partymanager.item_list.data;

import com.google.firebase.database.Exclude;
import com.vsu.nastya.partymanager.guest_list.data.Guest;

import java.io.Serializable;


/**
 * Created by Вита on 01.12.2016.
 */
public class Item implements Serializable {
    @Exclude
    private String key;
    private String name;
    private int quantity;
    private Guest whoBrings;
    private int price;

    public Item() {
    }

    public Item(String name, int quantity, Guest whoBrings, int price) {
        this.name = name;
        this.quantity = quantity;
        this.whoBrings = whoBrings;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Guest getWhoBrings() {
        return whoBrings;
    }

    public void setWhoBrings(Guest whoBrings) {
        this.whoBrings = whoBrings;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
