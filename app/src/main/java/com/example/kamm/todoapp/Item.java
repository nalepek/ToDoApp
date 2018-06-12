package com.example.kamm.todoapp;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by knalepa on 2018-05-29.
 */

public class Item implements Serializable {

    private String title;
    private boolean done;
    private long date;
    public String key;
    public int priority;

    public Item() {}

    public Item(String title, long date, boolean done, int priority)
    {
        this.title = title;
        this.date = date;
        this.done = done;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }
    public long getDate() { return date; }
    public boolean getDone() { return done; }
    public String getKey() { return key; }
    public int getPriority() { return priority; }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setDate(long date) { this.date = date; }
    public void setDone(boolean done) { this.done = done; }
    public void setPriority(int priority) { this.priority = priority; }
}