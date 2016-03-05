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

    public class Item {
        private String key;//The key for identifying single preferences
        private String currentValue;//Contains always the current value
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
        private boolean isCustomizable = false;//TODO True is the user should be able to save a custom value

        //The constructor sets all variables of this preference
        public Item(String key, String dataType, ArrayList<String> possibleValues, int defaultValue, boolean isCustomizable, String descriptionTop,
                    String descriptionBottom, String valueSuffix, int backgroundColor, int textColor, int activeBackgroundColor, int activeTextColor) {
            this.key = key;
            this.dataType = dataType;
            this.possibleValues = possibleValues;
            this.defaultValue = defaultValue;
            this.isCustomizable = isCustomizable;//TODO Implement custom settings
            this.currentValue = preferenceData.getString(this.key, this.possibleValues.get(this.defaultValue));
            this.descriptionTop = descriptionTop;
            this.descriptionBottom = descriptionBottom;
            this.valueSuffix = valueSuffix;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
            this.activeBackgroundColor = activeBackgroundColor;
            this.activeTextColor = activeTextColor;
        }

        //Saves the current value whenever the current tile changes
        public boolean save(String value) {
            //Load preference editor
            SharedPreferences.Editor editor = preferenceData.edit();
            editor.putString(this.key, value);

            //Commit the change
            Boolean commited = editor.commit();

            //Check if the commit was successful
            if (!commited) {
                Log.d(TAG, "Couldn't save settings.");
            } else {
                //Change the current value only if the commit was successful
                currentValue = value;
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

        public boolean getIsCustomizable() {
            return isCustomizable;
        }

        public int getDefaultValue() {
            return defaultValue;
        }

        public String getCurrentValue() {
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

        //Local variables, which we use to load the preferences
        ArrayList<String> possibleValues;
        Item newItem;

        /*
        LOAD ALL PREFERENCE SETTINGS
         */

        //Yes these are kind of hardcoded here, because we use a different approach then the standard android preferences

        //Autostart setting
        possibleValues = new ArrayList<>();
        possibleValues.add(0, "Not active");
        possibleValues.add(1, "Active");//default
        newItem = new Item("autostart", Boolean.TYPE.toString(), possibleValues, 1, false, "Enable autostart", "Service will restart after reboot", "",
                           getColor(R.color.settingsflipper_bg), getColor(R.color.settingsflipper), getColor(R.color.active_green), getColor(R.color.settingsflipper_bg));
        preferenceItems.add(newItem);

        //Location retrieve interval setting
        possibleValues = new ArrayList<>();
        possibleValues.add("2");
        possibleValues.add("5");
        possibleValues.add("10");
        possibleValues.add("15");
        possibleValues.add("30");
        possibleValues.add("45");
        possibleValues.add("60");
        newItem = new Item("interval", Integer.TYPE.toString(), possibleValues, possibleValues.indexOf("5"), true, "Set interval", "Time between localisation attempts",
                           " minutes",getColor(R.color.settingsflipper_bg), getColor(R.color.settingsflipper), getColor(R.color.active_green), getColor(R.color.settingsflipper_bg));
        preferenceItems.add(newItem);
    }

    //Get all preferences
    public ArrayList<Item> getItems() {
        return preferenceItems;
    }
}
