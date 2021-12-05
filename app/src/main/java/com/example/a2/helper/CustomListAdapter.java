package com.example.a2.helper;

import com.example.a2.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends BaseAdapter {
    private ArrayList<String> userList;
    private LayoutInflater layoutInflater;
    public CustomListAdapter(Context aContext, ArrayList<String> userList) {
        this.userList = userList;
        layoutInflater = LayoutInflater.from(aContext);
    }
    @Override
    public int getCount() {
        return userList.size();
    }
    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View v, ViewGroup vg) {
        ViewHolder holder;

        if (v == null) {
            v = layoutInflater.inflate(R.layout.list_row, null);
            holder = new ViewHolder();
            holder.username = (TextView) v.findViewById(R.id.usernameTxt);
            holder.mail = (TextView) v.findViewById(R.id.gmailText);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.username.setText(userList.get(position));
        holder.mail.setText(userList.get(position));
        return v;
    }
    static class ViewHolder {
        TextView username;
        TextView mail;
    }
}
