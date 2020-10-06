package cl.figonzal.lastquakechile.services;

import android.content.Context;
import android.content.SharedPreferences;

import cl.figonzal.lastquakechile.R;

public class SharedPrefService {

    private SharedPreferences sharedPreferences;

    private Context context;

    public SharedPrefService(Context context) {
        this.context = context;
        initSharedPref();
    }

    private void initSharedPref() {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.SHARED_PREF_MASTER_KEY), Context.MODE_PRIVATE);
    }

    public void saveData(String key, Object value) {

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        }

        editor.apply();
    }

    public Object getData(String key, Object defaultvalue) {

        Object result = null;

        if (defaultvalue instanceof Integer) {
            result = sharedPreferences.getInt(key, (Integer) defaultvalue);
        } else if (defaultvalue instanceof String) {
            result = sharedPreferences.getString(key, (String) defaultvalue);
        } else if (defaultvalue instanceof Boolean) {
            result = sharedPreferences.getBoolean(key, (Boolean) defaultvalue);
        } else if (defaultvalue instanceof Float) {
            result = sharedPreferences.getFloat(key, (Float) defaultvalue);
        }

        return result;
    }
}
