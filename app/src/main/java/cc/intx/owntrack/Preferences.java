package cc.intx.owntrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class Preferences {
    //Debug tag
    public String TAG;

    private final String preferenceFile = "preferences";
    private SharedPreferences preferences;

    private static final Map<String, String> defPreferences = new HashMap<>();
    static {
        defPreferences.put("Autostart", "true");
    }

    public Preferences(Context context, String TAG) {
        this.TAG = TAG;

        preferences = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
    }

    public String get(String key) {
        return preferences.getString(key, defPreferences.get(key));
    }

    public boolean set(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }
}
