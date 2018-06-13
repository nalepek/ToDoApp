package com.example.kamm.todoapp;

import java.util.Comparator;

/**
 * Created by kamm on 13.06.2018.
 */

public class Comparators
{
    public static class ItemTitleComparator implements Comparator<Item>
    {
        public int compare(Item left, Item right) {
            return left.getTitle().compareTo(right.getTitle());
        }
    }

    public static class ItemDateComparator implements Comparator<Item>
    {
        public int compare(Item left, Item right) {
            long l = left.getDate();
            long r = right.getDate();
            return l < r ? 1 : (l == r ? 0 : -1);
        }
    }

    public static class ItemDoneComparator implements Comparator<Item>
    {
        public int compare(Item left, Item right) {
            boolean l = left.getDone();
            boolean r = right.getDone();
            return (l != r) ? (l) ? -1 : 1 : 0;
        }
    }

    public static class ItemPriorityComparator implements Comparator<Item>
    {
        public int compare(Item left, Item right) {
            int l = left.getPriority();
            int r = right.getPriority();
            return l < r ? 1 : (l == r ? 0 : -1);
        }
    }
}
