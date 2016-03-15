package cc.intx.owntrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;

public class Preferences {
    //Debug tag
    public String TAG;

    //Service context
    private Context context;

    //Filename to save the preferences in
    private final String preferenceFile = "preferences";
    //Preference object, so we only have to load them once
    private SharedPreferences preferenceData;

    //Arraylist which contains all preferences
    private ArrayList<Item> preferenceItems;
    private ArrayList<String> preferenceItemsKeys;

    public SharedPreferences getPreferenceObject() {
        return preferenceData;
    }

    public class Item {
        private String key;//The key for identifying single preferences
        private int currentValue;//Contains always the current value
        private String dataType;//Contains a string representing the datatype. This string is created with e.g. Boolean.TYPE.toString()

        private String descriptionTop;//The description to show above the value on the settings tile
        private String valueSuffix;//The string to append to the value for better clarification (like " minute" for "1 minute", instead of just "1")
        private String descriptionBottom;//The desctiption to show under the value in the settings tile. This is hidden by default and shows up on long click

        private int backgroundColor;//Background color of the tile
        private int textColor;//Text color of the tile
        private int activeBackgroundColor;//Background color if active
        private int activeTextColor;//Text color if active

        private int defaultValue;//The value to use if none is set and to show "recommended" under
        private ArrayList<String> possibleValues;//A list of all possible values

        //The constructor sets all variables of this preference
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

            get();
        }

        public void get() {
            this.currentValue = preferenceData.getInt(this.key, this.defaultValue);
        }

        //Saves the current value whenever the current tile changes
        public boolean save(int index) {
            //Load preference editor
            SharedPreferences.Editor editor = preferenceData.edit();
            editor.putInt(this.key, index);

            //Commit the change
            Boolean commited = editor.commit();

            //Check if the commit was successful
            if (!commited) {
                Log.d(TAG, "Couldn't save settings.");
            } else {
                //Change the current value only if the commit was successful
                currentValue = index;
            }

            //Return if successful or not
            return commited;
        }

        /*
        GETTER FUNCTIONS
         */
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
            get();

            return currentValue;
        }

        public String getDescriptionTop() {
            return descriptionTop;
        }

        public String getDescriptionBottom() {
            return descriptionBottom;
        }

        public String getKey() {
            return key;
        }
    }

    //Retrieve color from the xml. We need this function, because getColor(color) was deprecated, but we need to support the APIs
    private int getColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(color, null);
        } else {
            return context.getResources().getColor(color);
        }
    }

    public Preferences(Context context, String TAG) {
        this.TAG = TAG;
        this.context = context;

        //Load the settings interface
        preferenceData = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        //Create the preference list
        preferenceItems = new ArrayList<>();
        preferenceItemsKeys = new ArrayList<>();

        //Local variables, which we use to load the preferences
        ArrayList<String> possibleValues;
        Item newItem;
        String currentKey;

        /*
        LOAD ALL PREFERENCE SETTINGS
         */

        //Yes these are kind of hardcoded here, because we use a different approach then the standard android preferences

        //Autostart setting
        possibleValues = new ArrayList<>();
        currentKey = "autostart";

        possibleValues.add("Not active");
        possibleValues.add("Active");//default
        newItem = new Item(currentKey, Boolean.TYPE.toString(), possibleValues, 1, "Enable autostart", "Service will restart after reboot", "",
                           getColor(R.color.settingsflipper_bg), getColor(R.color.settingsflipper), getColor(R.color.active_green), getColor(R.color.settingsflipper_bg));
        preferenceItems.add(newItem);
        preferenceItemsKeys.add(currentKey);

        //Selfsigned setting
        possibleValues = new ArrayList<>();
        currentKey = "allowselfsigned";

        possibleValues.add("Don't allow");//default
        possibleValues.add("Allow");
        newItem = new Item(currentKey, Boolean.TYPE.toString(), possibleValues, 0, "Allow self signing", "Certificate is allowed to be self signed", "",
                getColor(R.color.settingsflipper_bg), getColor(R.color.settingsflipper), getColor(R.color.active_green), getColor(R.color.settingsflipper_bg));
        preferenceItems.add(newItem);
        preferenceItemsKeys.add(currentKey);

        //Location retrieve interval setting
        possibleValues = new ArrayList<>();
        currentKey = "interval";

        possibleValues.add("2");
        possibleValues.add("5");
        possibleValues.add("10");
        possibleValues.add("15");
        possibleValues.add("30");
        possibleValues.add("45");
        possibleValues.add("60");
        newItem = new Item(currentKey, Integer.TYPE.toString(), possibleValues, 1, "Set interval", "Time between localisation attempts",
                           " minutes",getColor(R.color.settingsflipper_bg), getColor(R.color.settingsflipper), getColor(R.color.active_green), getColor(R.color.settingsflipper_bg));
        preferenceItems.add(newItem);
        preferenceItemsKeys.add(currentKey);
    }

    //Get all preference keys
    public ArrayList<String> getKeys() {
        return preferenceItemsKeys;
    }

    //Get all preferences
    public ArrayList<Item> getItems() {
        return preferenceItems;
    }
}
