package cc.intx.owntrack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

        setOnClick();
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

                        settingsInner.measure(View.MeasureSpec.makeMeasureSpec(settingsInner.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));
                        targetLastLocationDetailsHeight = settingsInner.getMeasuredHeight();

                        int result = serviceControl.saveUrl(text);

                        switch (result) {
                            case 0:
                                serverUrlEdit.setText(text);
                                break;
                            case 1:
                                showError("Service doesn't respond", "The service is currently not bound. Please restart the app and/or service and try again.");
                                break;
                            case 2:
                                showError("Invalid URL", "The URL you entered is not in a valid format. Please use a format like \"https://example.tld/optpath/optfile.php\".");
                                break;
                            case 3:
                                showError("Use HTTPS", "Only https is supported. Yes, self signed as well, look in the settings.");
                                break;
                            case 4:
                                showError("Can't connect", "No connection possible.");
                                break;
                            default:
                                showError("Unexpected error", "These goddamn developers.");
                                break;
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
