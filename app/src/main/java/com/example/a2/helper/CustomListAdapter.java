package com.example.a2.helper;

import com.example.a2.R;
import com.example.a2.model.User;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private List<User> users;
    private LayoutInflater layoutInflater;
    public CustomListAdapter(Context aContext, List<User> users) {
        this.users = users;
        layoutInflater = LayoutInflater.from(aContext);
    }
    @Override
    public int getCount() {
        return users.size();
    }
    @Override
    public Object getItem(int position) {
        return users.get(position);
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
        holder.username.setText(users.get(position).getName());
        holder.mail.setText(users.get(position).getEmail());
        return v;
    }
    static class ViewHolder {
        TextView username;
        TextView mail;
    }
}
