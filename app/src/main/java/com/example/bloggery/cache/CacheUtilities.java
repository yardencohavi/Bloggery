package com.example.bloggery.cache;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.bloggery.model.User;
import com.google.gson.Gson;

public class CacheUtilities {

    private static final Gson gson = new Gson();
    private static final String USER_FILE = "user";
    private static final String USER_KEY = "userkey";

    public static void setUser(Activity activity, User user) {
        SharedPreferences prefs = activity.getSharedPreferences(USER_FILE, Context.MODE_PRIVATE);
        prefs.edit().putString(USER_KEY, gson.toJson(user)).apply();
    }

    public static User getUser(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(USER_FILE, Context.MODE_PRIVATE);
        String result = prefs.getString(USER_KEY, "");
        return gson.fromJson(result, User.class);
    }

    public static void clearUser(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(USER_FILE, Context.MODE_PRIVATE);
        prefs.edit().clear();
    }


}
