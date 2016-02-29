package cc.intx.owntrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.ArrayList;

public class Preferences {
    //Debug tag
    public String TAG;
    private Context context;

    private final String preferenceFile = "preferences";
    private SharedPreferences preferenceData;

    private ArrayList<Item> preferenceItems;

    public class Item {
        private String key;
        private String currentValue;
        private String dataType;

        private String descriptionTop;
        private String valueSuffix;
        private String descriptionBottom;

        private int backgroundColor;
        private int textColor;
        private int activeBackgroundColor;
        private int activeTextColor;

        private int defaultValue;
        private ArrayList<String> possibleValues;
        private boolean isCustomizable = false;

        public Item(String key, String dataType, ArrayList<String> possibleValues, int defaultValue, boolean isCustomizable, String descriptionTop,
                    String descriptionBottom, String valueSuffix, int backgroundColor, int textColor, int activeBackgroundColor, int activeTextColor) {
            this.key = key;
            this.dataType = dataType;
            this.possibleValues = possibleValues;
            this.defaultValue = defaultValue;
            this.isCustomizable = isCustomizable;
            this.currentValue = preferenceData.getString(this.key, this.possibleValues.get(this.defaultValue));
            this.descriptionTop = descriptionTop;
            this.descriptionBottom = descriptionBottom;
            this.valueSuffix = valueSuffix;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
            this.activeBackgroundColor = activeBackgroundColor;
            this.activeTextColor = activeTextColor;
        }

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
    }

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

        preferenceData = context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE);
        preferenceItems = new ArrayList<>();

        ArrayList<String> possibleValues;
        Item newItem;

        possibleValues = new ArrayList<>();
        possibleValues.add(0, "Not active");
        possibleValues.add(1, "Active");//default
        newItem = new Item("autostart", Boolean.TYPE.toString(), possibleValues, 1, false, "Enable autostart", "", "",
                           getColor(R.color.settingsflipper_bg), getColor(R.color.settingsflipper), getColor(R.color.active_green), getColor(R.color.settingsflipper_bg));
        preferenceItems.add(newItem);

        possibleValues = new ArrayList<>();
        possibleValues.add("2");
        possibleValues.add("5");
        possibleValues.add("10");
        possibleValues.add("15");
        possibleValues.add("30");
        possibleValues.add("45");
        possibleValues.add("60");
        newItem = new Item("interval", Integer.TYPE.toString(), possibleValues, possibleValues.indexOf("5"), true, "Interval", "Time between localisation attempts",
                           " minutes",getColor(R.color.settingsflipper_bg), getColor(R.color.settingsflipper), getColor(R.color.active_green), getColor(R.color.settingsflipper_bg));
        preferenceItems.add(newItem);
    }

    public ArrayList<Item> getItems() {
        return preferenceItems;
    }
}
