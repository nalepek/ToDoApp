package com.example.kamm.todoapp;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;

import java.util.Date;
import java.util.List;

/**
 * Created by kamm on 01.06.2018.
 */

public class ListAdapter extends BaseAdapter {

    Context context;
    List<Item> data;
    private static LayoutInflater inflater = null;

    public ListAdapter(Context context, List<Item> data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.row, null);

        TextView text = (TextView) vi.findViewById(R.id.row_title);
        text.setText(data.get(position).getTitle());

        TextView date = (TextView) vi.findViewById(R.id.row_date);
        long milisec = data.get(position).getDate();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(milisec)).toString();
        date.setText(dateString);

        CheckBox done = (CheckBox) vi.findViewById(R.id.row_done);
        done.setChecked(data.get(position).getDone());

        return vi;
    }
}