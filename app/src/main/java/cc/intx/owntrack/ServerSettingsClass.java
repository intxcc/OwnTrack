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

import java.net.URL;
import java.security.cert.Certificate;

/*  This class does implement the server settings view, so the user is able to check and set settings
    of the remote server */

/*  TODO check that if the server settings are in default state (exmpl.tld and so on), it should just
    ignore it and show a not working server in the server settings view */

public class ServerSettingsClass {
    public String TAG;

    /* Global objects to communicate with the application and the service */
    private Activity activity;
    private MainActivity.ServiceControl serviceControl;

    /* The content */
    private GridLayout settingsInner;
    /* The title */
    private TextView settingsHead1;
    private TextView settingsHead2;

    /* Shows the configured server host */
    private TextView showCurrentServerTextView;
    /* Shows the state of the server (e.g. working installation, no connection, ...) */
    private TextView showCurrentServerStatus;

    /* The editable settings */
    private TextView serverUrlEdit;
    private TextView serverCommonSecretEdit;
    private TextView serverCertEdit;

    /* Speed at which to expand the server setting view */
    private int extendingSpeed;

    /* Constructor - initializes all views needed and sets the action to perform on click */
    public ServerSettingsClass(Activity activity, String TAG, MainActivity.ServiceControl serviceControl, int extendingSpeed) {
        this.TAG = TAG;
        this.activity = activity;
        this.extendingSpeed = extendingSpeed;
        this.serviceControl = serviceControl;

        settingsInner = (GridLayout) activity.findViewById(R.id.serverSettingsInner);
        settingsHead1 = (TextView) activity.findViewById(R.id.serverSettingsHead1);
        settingsHead2 = (TextView) activity.findViewById(R.id.serverSettingsHead2);

        showCurrentServerTextView = (TextView) activity.findViewById(R.id.showCurrentServerTextView);
        showCurrentServerStatus = (TextView) activity.findViewById(R.id.showCurrentServerStatus);

        serverUrlEdit = (TextView) activity.findViewById(R.id.serverUrlEdit);
        serverCommonSecretEdit = (TextView) activity.findViewById(R.id.serverCommonSecretEdit);
        serverCertEdit = (TextView) activity.findViewById(R.id.serverCertEdit);

        setOnClick();
    }

    /* Update the shown settings to be in sync with the saved ones */
    public void loadSavedSettings() {
        /* Show configured remote url */
        serverUrlEdit.setText(serviceControl.getUrl());
        /* Show configured common secret */
        serverCommonSecretEdit.setText(serviceControl.getCommonSecret());

        /* Extract the hostname from the url */
        URL url;
        try {
            url = new URL(serviceControl.getUrl());
        } catch (Exception e) {
            showError("Error", e.getMessage());
            return;
        }
        /* Shows the hostname of the configured url */
        showCurrentServerTextView.setText(url.getHost());

        /* Get the configured certificate, if there is one. If there is none and none is pinned display warning */
        String pinnedCert = serviceControl.getPinnedCert();
        if (pinnedCert.equals("none")) {
            if (serviceControl.getAllowSelfSigning()) {
                serverCertEdit.setText(activity.getString(R.string.useselfsignednopinned));
            } else {
                serverCertEdit.setText(activity.getString(R.string.usepubliccerts));
            }
        } else {
            /* Show only a preview of the cert to safe space */
            String showText = pinnedCert.subSequence(0, 16) + "...";
            serverCertEdit.setText(showText);
        }

        /* Update height in case some TextView does break a line */
        settingsInner.measure(View.MeasureSpec.makeMeasureSpec(settingsInner.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));
        targetLastLocationDetailsHeight = settingsInner.getMeasuredHeight();

        /*  Checks server settings and display errors/working - the error codes, if the server responds,
            are that strange to prevent a random server to send error codes and confuse the user */
        String serverStatusString;
        int result = serviceControl.checkServerSettings();
        if (result == 612) {
            throwErrorDialog(7);

            serverStatusString = "Error " + result;
        } else if (result != 130) {
            throwErrorDialog(result);

            serverStatusString = "Error " + result;
        } else {

            serverStatusString = "Working";
        }

        /* Show the server status */
        showCurrentServerStatus.setText(serverStatusString);
    }

    private void setOnClick() {
        /* These both toggle the server settings view extended state */
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

        /*  On click one can set a new url. This new url will be tested and only accepted if the server
            responds. No other settings are checked here */
        serverUrlEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView f = (TextView) v;

                /* Build an input dialog to choose the new url */
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

                        /*  We don't save the url our self but let the service take control of this, as
                            it can communicate better with the server */
                        int result = serviceControl.saveUrl(text);

                        /* f we get an error, don't update settings, but show an error dialog */
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

                /* Show the dialog */
                builder.show();
            }
        });

        /*  If the user changes the commonSecret the server is completely checked (goes through the
            authentication phase - ping flag set) */
        serverCommonSecretEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView f = (TextView) v;

                /* Create dialog to change the commonSecret */
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
                            /* TODO dont save the secret, but show errordialog indicating that an empty secret is not allowed */
                        }
                        int result = serviceControl.saveCommonSecret(text);

                        /* Load new settings or throw error */
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

                /* Show the dialog */
                builder.show();
            }
        });

        /*  These are two dialogs. The first shows the current certificate and asks if we want to change
            or delete it. If we change it a new dialog appears, downloads the certificate for the server set,
            shows it and allows to reject/accept it */
        serverCertEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Change pinned certificate?");

                URL url;
                try {
                    url = new URL(serviceControl.getUrl());
                } catch (Exception e) {
                    showError("Error", e.getMessage());
                    return;
                }

                String message = "Would you like to change the pinned certificate for " + url.getHost() + "?";
                String pinnedCert = serviceControl.getPinnedCert();
                if (!pinnedCert.equals("none")) {
                    TextView textView = new TextView(activity);
                    textView.setText(pinnedCert);

                    /* A fix, to change the width relatively to the settings, which looks better than without it */
                    int padding = Math.round((settingsInner.getMeasuredWidth() / 100f) * 15f);

                    textView.setPadding(padding, 10, padding, 10);
                    builder.setView(textView);

                    /* If a certificate is already pinned indicate that this is the current one */
                    message = message + " Current certificate [SHA-256]: ";
                }

                builder.setMessage(message);
                builder.setIcon(android.R.drawable.ic_partial_secure);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* Load the dialog to change the pinned Cert and update the settings */
                        pinCertificateDialog();
                        loadSavedSettings();
                    }
                });

                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* Delete the current certificate and local settings */
                        serviceControl.pinCertificate("");
                        loadSavedSettings();
                    }
                });

                /* Show the dialog */
                builder.show();
            }
        });
    }

    /* The dialog to change the pinned certificate */
    private void pinCertificateDialog() {
        Certificate[] certificates = serviceControl.getCerts();
        if (certificates == null) {
            throwErrorDialog(serviceControl.getLastError());
            return;
        }

        /* Load the certificate for the current set url */
        final String newCert = Misc.getCertFingerprint(certificates[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Pin this certificate?");

        URL url;
        try {
            url = new URL(serviceControl.getUrl());
        } catch (Exception e) {
            showError("Error", e.getMessage());
            return;
        }

        TextView textView = new TextView(activity);
        textView.setText(newCert);
        int padding = Math.round((settingsInner.getMeasuredWidth() / 100f) * 15f);
        textView.setPadding(padding, 10, padding, 10);
        builder.setView(textView);

        builder.setMessage("New certificate for " + url.getHost() + " [SHA-256]:");
        builder.setIcon(android.R.drawable.ic_partial_secure);

        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /* Pin the new cert and load settings */
                serviceControl.pinCertificate(newCert);
                loadSavedSettings();
            }
        });

        builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        /* Show the dialog */
        builder.show();
    }

    /* Creates an error dialog with fitting error messages */
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
            case 6:
                showError(activity.getString(R.string.selfsignederr_title), activity.getString(R.string.selfsignederr));
                break;
            case 7:
                showError(activity.getString(R.string.wrongsettings_title), activity.getString(R.string.wrongcommonsecret));
                break;
            case 8:
                showError(activity.getString(R.string.wrongsettings_title), activity.getString(R.string.noowntrackerr));
                break;
            case 9:
                showError(activity.getString(R.string.wrongsettings_title), activity.getString(R.string.badcert));
                break;
            default:
                showError(activity.getString(R.string.unexpecederror_title), activity.getString(R.string.unexpecederror));
                break;
        }
    }

    /* Show error dialog, with error icon and custom text - called by throwErrorDialog */
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
            /*  measure the expected height of the server settings view content
                TODO This is where you need to adjust, to react to changing view height */
            settingsInner.measure(View.MeasureSpec.makeMeasureSpec(settingsInner.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));
            targetLastLocationDetailsHeight = settingsInner.getMeasuredHeight();
        }

        /* Toggle and animate between extended state */
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
