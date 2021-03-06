package cc.intx.owntrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;

public class Preferences {
    /* Debug tag */
    public String TAG;

    /* Service context */
    private Context context;

    /* Filename to save the preferences in */
    private final String preferenceFile = "preferences";
    /* Preference object, so we only have to load them once */
    private SharedPreferences preferenceData;

    /* Arraylist which contains all preferences */
    private ArrayList<Item> preferenceItems;
    private ArrayList<String> preferenceItemsKeys;

    public SharedPreferences getPreferenceObject() {
        return preferenceData;
    }

    public class Item {
        /* The key for identifying single preferences */
        private String key;
        /* Contains always the current value */
        private int currentValue;
        /* Contains a string representing the datatype. This string is created with e.g. Boolean.TYPE.toString() */
        private String dataType;

        /* The description to show above the value on the settings tile */
        private String descriptionTop;
        /* The string to append to the value for better clarification (like " minute" for "1 minute", instead of just "1") */
        private String valueSuffix;
        /* The description to show under the value in the settings tile. This is hidden by default and shows up on long click */
        private String descriptionBottom;

        /* Background color of the tile */
        private int backgroundColor;
        /* Text color of the tile */
        private int textColor;
        /* Background color if active */
        private int activeBackgroundColor;
        /* Text color if active */
        private int activeTextColor;

        /* The value to use if none is set and to show "recommended" under */
        private int defaultValue;
        /* A list of all possible values */
        private ArrayList<String> possibleValues;

        /* The constructor sets all variables of this preference */
        public Item(String key, String dataType, ArrayList<String> possibleValues, int defaultValue, String descriptionTop,
                    String descriptionBottom, String valueSuffix, int backgroundColor, int textColor, int activeBackgroundColor, int activeTextColor) {
            this.key = key;
            this.dataType = dataType;
            this.possibleValues = possibleValues;
            this.defaultValue = defaultValue;
            this.descriptionTop = descriptionTop;
            this.descriptionBottom = descriptionBottom;
            this.valueSuffix = valueSuffix;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
            this.activeBackgroundColor = activeBackgroundColor;
            this.activeTextColor = activeTextColor;

            /* Load the current value index */
            get();
        }

        /* Load the current value index */
        public void get() {
            this.currentValue = preferenceData.getInt(this.key, this.defaultValue);
        }

        /* Saves the current value whenever the current tile changes */
        public boolean save(int index) {
            /* Load preference editor */
            SharedPreferences.Editor editor = preferenceData.edit();
            editor.putInt(this.key, index);

            /* Commit the change */
            Boolean committed = editor.commit();

            /* Check if the commit was successful */
            if (!committed) {
                Log.d(TAG, "Couldn't save settings.");
            } else {
                /* Change the current value only if the commit was successful */
                currentValue = index;
            }

            /* Return if successful or not */
            return committed;
        }

        /* ---------------- */
        /* GETTER FUNCTIONS */

        public ArrayList<String> getPossibleValues() {
            return possibleValues;
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public int getTextColor() {
            return textColor;
        }

        public int getActiveBackgroundColor() {
            return activeBackgroundColor;
        }

        public int getActiveTextColor() {
            return activeTextColor;
        }

        public String getDataType() {
            return dataType;
        }

        public String getValueSuffix() {
            return valueSuffix;
        }

        public int getDefaultValue() {
            return defaultValue;
        }

        public int getCurrentValue() {
            /* Get the fresh loaded value */
            get();

            return currentValue;
        }

        public String getDescriptionTop() {
            return descriptionTop;
        }

        public String getDescriptionBottom() {
            return descriptionBottom;
        }
    }

    public Preferences(Context context, String TAG) {
        this.TAG = TAG;
        this.context = context;

        /* Load the settings interface */
        preferenceData = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        /* Create the preference list */
        preferenceItems = new ArrayList<>();
        preferenceItemsKeys = new ArrayList<>();

        /* Local variables, which we use to load the preferences */
        ArrayList<String> possibleValues;
        Item newItem;
        String currentKey;

        /* ---------------------------- */
        /* LOAD ALL PREFERENCE SETTINGS */

        /* Autostart setting */
        possibleValues = new ArrayList<>();
        currentKey = "autostart";

        possibleValues.add("Not active");
        possibleValues.add("Active");//default
        newItem = new Item(currentKey, Boolean.TYPE.toString(), possibleValues, 1, "Enable autostart", "Service will restart after reboot", "",
                           Misc.getColor(context, R.color.settingsflipper_bg), Misc.getColor(context, R.color.settingsflipper), Misc.getColor(context, R.color.active_green), Misc.getColor(context, R.color.settingsflipper_bg));
        preferenceItems.add(newItem);
        preferenceItemsKeys.add(currentKey);

        /* Allow self signed setting */
        possibleValues = new ArrayList<>();
        currentKey = "allowselfsigned";

        possibleValues.add("Don't allow");//default
        possibleValues.add("Allow");
        newItem = new Item(currentKey, Boolean.TYPE.toString(), possibleValues, 0, "Allow self signing", "Certificate is allowed to be self signed", "",
                Misc.getColor(context, R.color.settingsflipper_bg), Misc.getColor(context, R.color.settingsflipper), Misc.getColor(context, R.color.active_green), Misc.getColor(context, R.color.settingsflipper_bg));
        preferenceItems.add(newItem);
        preferenceItemsKeys.add(currentKey);

        /* Location retrieve interval setting */
        possibleValues = new ArrayList<>();
        currentKey = "interval";

        possibleValues.add("2");
        possibleValues.add("5");
        possibleValues.add("10");
        possibleValues.add("15");
        possibleValues.add("30");
        possibleValues.add("45");
        possibleValues.add("60");
        newItem = new Item(currentKey, Integer.TYPE.toString(), possibleValues, 1, "Location interval", "Time between localisation attempts",
                           " minutes", Misc.getColor(context, R.color.settingsflipper_bg), Misc.getColor(context, R.color.settingsflipper), Misc.getColor(context, R.color.active_green), Misc.getColor(context, R.color.settingsflipper_bg));
        preferenceItems.add(newItem);
        preferenceItemsKeys.add(currentKey);

        /* Upload interval setting */
        possibleValues = new ArrayList<>();
        currentKey = "uploadinterval";

        possibleValues.add("1");
        possibleValues.add("2");
        possibleValues.add("5");
        possibleValues.add("10");
        possibleValues.add("25");
        possibleValues.add("50");
        possibleValues.add("100");
        newItem = new Item(currentKey, Integer.TYPE.toString(), possibleValues, 1, "Upload interval", "Time between upload attempts",
                " locations", Misc.getColor(context, R.color.settingsflipper_bg), Misc.getColor(context, R.color.settingsflipper), Misc.getColor(context, R.color.active_green), Misc.getColor(context, R.color.settingsflipper_bg));
        preferenceItems.add(newItem);
        preferenceItemsKeys.add(currentKey);

        /* Abort time setting */
        possibleValues = new ArrayList<>();
        currentKey = "abortlocrecvafter";

        possibleValues.add("2");
        possibleValues.add("4");
        possibleValues.add("6");
        possibleValues.add("10");
        possibleValues.add("15");
        possibleValues.add("20");
        possibleValues.add("30");
        newItem = new Item(currentKey, Integer.TYPE.toString(), possibleValues, 2, "Abort after", "How long to wait for new location",
                " seconds", Misc.getColor(context, R.color.settingsflipper_bg), Misc.getColor(context, R.color.settingsflipper), Misc.getColor(context, R.color.active_green), Misc.getColor(context, R.color.settingsflipper_bg));
        preferenceItems.add(newItem);
        preferenceItemsKeys.add(currentKey);
    }

    /* Get all preference keys */
    public ArrayList<String> getKeys() {
        return preferenceItemsKeys;
    }

    /* Get all preferences */
    public ArrayList<Item> getItems() {
        return preferenceItems;
    }
}
