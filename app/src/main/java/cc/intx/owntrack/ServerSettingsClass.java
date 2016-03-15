package cc.intx.owntrack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

public class ServerSettingsClass {
    public String TAG;

    private Activity activity;
    private MainActivity.ServiceControl serviceControl;

    private GridLayout settingsInner;
    private TextView settingsHead1;
    private TextView settingsHead2;

    private TextView serverUrlEdit;
    private TextView serverCommonSecretEdit;

    private int extendingSpeed;

    public ServerSettingsClass(Activity activity, String TAG, MainActivity.ServiceControl serviceControl, int extendingSpeed) {
        this.TAG = TAG;
        this.activity = activity;
        this.extendingSpeed = extendingSpeed;
        this.serviceControl = serviceControl;

        settingsInner = (GridLayout) activity.findViewById(R.id.serverSettingsInner);
        settingsHead1 = (TextView) activity.findViewById(R.id.serverSettingsHead1);
        settingsHead2 = (TextView) activity.findViewById(R.id.serverSettingsHead2);

        serverUrlEdit = (TextView) activity.findViewById(R.id.serverUrlEdit);
        serverCommonSecretEdit = (TextView) activity.findViewById(R.id.serverCommonSecretEdit);

        setOnClick();
    }

    private void loadSavedSettings() {
        serverUrlEdit.setText(serviceControl.getUrl());
        serverCommonSecretEdit.setText(serviceControl.getCommonSecret());

        //Update height in case some TextView does break a line
        settingsInner.measure(View.MeasureSpec.makeMeasureSpec(settingsInner.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));
        targetLastLocationDetailsHeight = settingsInner.getMeasuredHeight();

        int result = serviceControl.checkConnection();
        if (result != 0) {
            throwErrorDialog(result);
        }
    }

    private void setOnClick() {
        settingsHead1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHeadClick();
            }
        });
        settingsHead2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHeadClick();
            }
        });

        //Server URL Edit
        serverUrlEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView f = (TextView) v;

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Edit server URL");

                final EditText input = new EditText(activity);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                input.setText(f.getText());
                input.selectAll();
                builder.setView(input);

                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = input.getText().toString();

                        int result = serviceControl.saveUrl(text);

                        if (result == 0) {
                            loadSavedSettings();
                        } else {
                            throwErrorDialog(result);
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        //Server Common Secret Edit
        serverCommonSecretEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView f = (TextView) v;

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Edit common secret");

                final EditText input = new EditText(activity);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                input.setText(f.getText());
                input.selectAll();
                builder.setView(input);

                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = input.getText().toString();

                        if (text.equals("")) {
                            text = "nosecret";
                        }
                        int result = serviceControl.saveCommonSecret(text);

                        if (result == 0) {
                            loadSavedSettings();
                        } else {
                            throwErrorDialog(result);
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    private void throwErrorDialog(int errorCode) {
        switch (errorCode) {
            case 1:
                showError(activity.getString(R.string.norespond_title), activity.getString(R.string.norespond));
                break;
            case 2:
                showError(activity.getString(R.string.invalidurl_title), activity.getString(R.string.invalidurl));
                break;
            case 3:
                showError(activity.getString(R.string.nohttps_title), activity.getString(R.string.nohttps));
                break;
            case 4:
                showError(activity.getString(R.string.noconnection_title), activity.getString(R.string.noconnection));
                break;
            case 5:
                showError(activity.getString(R.string.cantsave_title), activity.getString(R.string.cantsave));
                break;
            default:
                showError(activity.getString(R.string.unexpecederror_title), activity.getString(R.string.unexpecederror));
                break;
        }
    }

    private void showError(String title, String error) {
        new AlertDialog.Builder(activity)
        .setTitle(title)
        .setMessage(error)
        .setPositiveButton(android.R.string.ok, null)
        .setIcon(android.R.drawable.ic_delete)
        .show();
    }

    private int targetLastLocationDetailsHeight = 0;
    private boolean extended = false;
    public void onHeadClick() {
        if (targetLastLocationDetailsHeight == 0) {
            settingsInner.measure(View.MeasureSpec.makeMeasureSpec(settingsInner.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));
            targetLastLocationDetailsHeight = settingsInner.getMeasuredHeight();
        }

        if (!extended) {
            loadSavedSettings();

            Animation animation = new ShowAnimation(settingsInner, targetLastLocationDetailsHeight, false);
            animation.setDuration(Math.round(extendingSpeed * 0.75));

            settingsInner.clearAnimation();
            settingsInner.startAnimation(animation);

            extended = true;
        } else {
            Animation animation = new ShowAnimation(settingsInner, targetLastLocationDetailsHeight, true);
            animation.setDuration(Math.round(extendingSpeed * 0.75));

            settingsInner.clearAnimation();
            settingsInner.startAnimation(animation);

            extended = false;
        }
    }
}
