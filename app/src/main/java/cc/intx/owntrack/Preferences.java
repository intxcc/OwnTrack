package cc.intx.owntrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

public class Preferences {
    //Debug tag
    public String TAG;

    private final String preferenceFile = "preferences";
    private SharedPreferences preferences;

    private int orderC = 0;
    public class PrefItem<T> {
        private String key;
        private String defValue;
        private String value;
        private String type;
        private int order;

        private ArrayList<Pair<String,T>> states = new ArrayList<>();
        private boolean addCustom = false;

        public PrefItem(String key, T defValue) {
            this.order = orderC++;
            this.key = key;
            this.type = defValue.getClass().getSimpleName();
            this.defValue = defValue.toString();
            this.value = preferences.getString(this.key, this.defValue);
        }

        public void addState(String desc, T state) {
            states.add(new Pair<String, T>(desc, state));
        }

        public void setAddCustom(boolean custom) {
            addCustom = custom;
        }

        public boolean getIsCustom() {
            return addCustom;
        }

        public ArrayList<Pair<String,T>> getStates() {
            return states;
        }

        public String getKey() {
            return key;
        }

        public String getDefValue() {
            return defValue;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    private static final ArrayList<PrefItem> prefItems = new ArrayList<>();

    public Preferences(Context context, String TAG) {
        this.TAG = TAG;

        prefItems.clear();

        preferences = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);

        PrefItem nItem = new PrefItem<>("Run service", true);
        nItem.addState("Active", true);
        nItem.addState("Not Active", false);
        prefItems.add(nItem);

        nItem = new PrefItem<>("Enable autostart", true);
        nItem.addState("Active", true);
        nItem.addState("Not active", false);
        prefItems.add(nItem);

        nItem = new PrefItem<>("Interval", 5);
        nItem.addState("min", 1);
        nItem.addState("max", 60);
        nItem.addState("1 minute", 1);
        nItem.addState("2 minutes", 2);
        nItem.addState("5 minutes", 5);
        nItem.addState("10 minutes", 10);
        nItem.addState("15 minutes", 15);
        nItem.addState("30 minutes", 30);
        nItem.addState("45 minutes", 45);
        nItem.addState("60 minutes", 60);
        nItem.setAddCustom(true);
        prefItems.add(nItem);
    }

    public ArrayList<PrefItem> getAll() {
        return prefItems;
    }
}
