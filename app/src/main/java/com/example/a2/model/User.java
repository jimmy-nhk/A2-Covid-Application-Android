package com.example.a2.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

public class User implements Parcelable {

    private String name;
    private String email;
    private boolean isSuperUser;

    public static final String USER_NAME ="name";
    public static final String USER_EMAIL ="email";
    public static final String USER_SUPERUSER ="isSuperUser";

    public boolean getIsSuperUser() {
        return isSuperUser;
    }

    public void setSuperUser(boolean superUser) {
        isSuperUser = superUser;
    }

    public User(){};
    public User(String name, String email, boolean isSuperUser) {
        this.name = name;
        this.email = email;
        this.isSuperUser = isSuperUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected User(Parcel in) {
        name = in.readString();
        email = in.readString();
        isSuperUser = in.readBoolean();
    }


    public static final Creator<User> CREATOR = new Creator<User>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(name);
        dest.writeString(email);
        dest.writeBoolean(isSuperUser);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", isSuperUser=" + isSuperUser +
                '}';
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(USER_NAME, name);
        result.put(USER_EMAIL, email);
        result.put(USER_SUPERUSER, isSuperUser);
        return result;
    }
}
