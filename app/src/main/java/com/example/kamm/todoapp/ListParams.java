package com.example.kamm.todoapp;

/**
 * Created by kamm on 13.06.2018.
 */

public class ListParams {

    private Enums.Order order;
    public boolean asc;

    public ListParams(Enums.Order order, boolean asc){
        this.order = order;
        this.asc = asc;
    }

    public boolean getAsc () { return asc; }
    public Enums.Order getOrder () { return order; }

    public void setOrder (Enums.Order order) { this.order = order; }
    public void setAsc (boolean asc) { this.asc = asc; }
}
