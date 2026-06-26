package com.example.fooddelivery.data.local.prefs;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {

    private static final String PREF_NAME   = "FoodDeliverySession";
    private static final String KEY_TOKEN   = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME    = "user_name";
    private static final String KEY_EMAIL   = "user_email";
    private static final String KEY_ROLE    = "user_role";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        SharedPreferences tempPrefs = null;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            tempPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Fallback to normal shared preferences in case of failure, or throw exception
            tempPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
        prefs = tempPrefs;
        editor = prefs.edit();
    }


    public void saveSession(String token, int userId, String name, String email, String role) {
        editor.putString(KEY_TOKEN,   token);
        editor.putInt(KEY_USER_ID,    userId);
        editor.putString(KEY_NAME,    name);
        editor.putString(KEY_EMAIL,   email);
        editor.putString(KEY_ROLE,    role);
        editor.apply();
    }


    public String getBearerToken() {
        String token = prefs.getString(KEY_TOKEN, null);
        return token != null ? "Bearer " + token : null;
    }

    public String getToken()   { return prefs.getString(KEY_TOKEN, null); }
    public int    getUserId()  { return prefs.getInt(KEY_USER_ID, -1); }
    public String getUserName(){ return prefs.getString(KEY_NAME, ""); }
    public String getEmail()   { return prefs.getString(KEY_EMAIL, ""); }
    public String getRole()    { return prefs.getString(KEY_ROLE, "customer"); }

    public boolean isLoggedIn() { return getToken() != null; }


    public void clearSession() { editor.clear().apply(); }
}
