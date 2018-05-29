package com.example.kamm.todoapp;

/**
 * Created by knalepa on 2018-05-29.
 */

public class Item {

    private String title;

    public Item() {}

    public Item(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}