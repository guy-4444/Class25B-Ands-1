package com.guyi.class25b_ands_1;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

public class Activity_Location extends AppCompatActivity {

    /*
    1. Location Services
    2. Location Permissions (s!)
    3. Background Location
    4. location Settings

     */

    private enum STATE {
        NA,
        NO_REGULAR_PERMISSION,
        NO_BACKGROUND_PERMISSION,
        LOCATION_DISABLE,
        LOCATION_SETTINGS_PROCCESS,
        LOCATION_SETTINGS_OK,
        LOCATION_ENABLE
    }

    private MaterialTextView location_LBL_title;
    private MaterialTextView location_LBL_content;
    private MaterialTextView location_LBL_progress;
    private MaterialButton location_BTN_next;
    private MaterialButton location_BTN_back;

    private STATE state = STATE.NA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        findViews();
        initViews();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d("pttt", "hasFocus= " + hasFocus);
        if (hasFocus) {
            start();
        }
    }

    private void start() {
        String missingPermission = checkForMissingPermission(this);
        if (!isLocationEnabled(this)) {
            state = STATE.LOCATION_DISABLE;
        } else if (missingPermission != null) {
            if (missingPermission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                state = STATE.NO_BACKGROUND_PERMISSION;
            } else {
                state = STATE.NO_REGULAR_PERMISSION;
            }
        } else {
            state = STATE.LOCATION_SETTINGS_PROCCESS;
            // All permissions granted
            validateLocationSensorsEnabled();
        }
        updateUI();
    }

    private void updateUI() {
        if (state == STATE.NA) {
            location_LBL_title.setText("0");
            location_LBL_content.setText("NA");
            location_LBL_progress.setText("-/-");
            location_BTN_back.setVisibility(View.INVISIBLE);
            location_BTN_next.setVisibility(View.INVISIBLE);
        } else if (state == STATE.LOCATION_DISABLE) {
            location_LBL_title.setText("Enable location services");
            location_LBL_content.setText("The app samples your location.\nPlease enable location services (GPS).");
            location_LBL_progress.setText("1/4");
            location_BTN_next.setOnClickListener(v -> {
                enableLocationServicesProgrammatically();
            });
            location_BTN_next.setText("Turn On");
            location_BTN_back.setVisibility(View.VISIBLE);
            location_BTN_next.setVisibility(View.VISIBLE);
        } else if (state == STATE.NO_REGULAR_PERMISSION) {
            location_LBL_title.setText("Location permission");
            location_LBL_content.setText("Location permission is needed for core functionality.\nPlease Enable the app permission to access your location data");
            location_LBL_progress.setText("2/4");
            location_BTN_next.setOnClickListener(v -> {
                askForPermissions(checkForMissingPermission(this));
            });
            location_BTN_next.setText("Allow");
            location_BTN_back.setVisibility(View.VISIBLE);
            location_BTN_next.setVisibility(View.VISIBLE);
        } else if (state == STATE.NO_BACKGROUND_PERMISSION) {
            location_LBL_title.setText("Background location permission");
            location_LBL_content.setText("This app collects location data even when the app is closed or not in use.\nTo protect your privacy, the app stores only calculated indicators, like distance from home and never exact location.\nA notification is always displayed in the notifications bar when service is running.");
            location_LBL_progress.setText("3/4");
            location_BTN_next.setOnClickListener(v -> {
                askForPermissions(checkForMissingPermission(this));
            });
            location_BTN_next.setText("Allow");
            location_BTN_back.setVisibility(View.VISIBLE);
            location_BTN_next.setVisibility(View.VISIBLE);
        } else if (state == STATE.LOCATION_SETTINGS_PROCCESS) {
            location_LBL_title.setText("4");
            location_LBL_content.setText("LOCATION_SETTINGS_PROCCESS");
            location_LBL_progress.setText("");
            location_BTN_back.setVisibility(View.INVISIBLE);
            location_BTN_next.setVisibility(View.INVISIBLE);
        } else if (state == STATE.LOCATION_SETTINGS_OK) {
            location_LBL_title.setText("");
            location_LBL_content.setText("Location services are running and all permissions have been granted.\n" +
                    "You can now start recording.");
            location_LBL_progress.setText("4/4");
            location_BTN_next.setOnClickListener(v -> {
                finish();
            });
            location_BTN_next.setText("Close");
            location_BTN_back.setVisibility(View.INVISIBLE);
            location_BTN_next.setVisibility(View.VISIBLE);
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("pttt", "isGranted");
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    start();
                } else {
                    Log.d("pttt", "NOT Granted");
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.

                    if (shouldShowRequestPermissionRationale(checkForMissingPermission(Activity_Location.this))) {
                        Snackbar.make(findViewById(android.R.id.content),
                                        R.string.permission_rationale,
                                        Snackbar.LENGTH_INDEFINITE)
                                .setDuration(Snackbar.LENGTH_LONG)
                                .setAction(R.string.settings, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        requestPermissionLauncher.launch(checkForMissingPermission(Activity_Location.this));
                                    }
                                })
                                .show();
                    } else {
                        buildAlertMessageManuallyBackgroundPermission(checkForMissingPermission(Activity_Location.this));
                    }
                }
            });

    private void buildAlertMessageManuallyBackgroundPermission(String permission) {
        if (permission == null) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String sofix = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? "Allow all the time" : "Allow";

        builder.setMessage("You need to enable background location permission manually." +
                        "\nOn the page that opens - click on PERMISSIONS, then on LOCATION and then check '" + sofix + "'")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        openAppSettings();                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    ActivityResultLauncher<IntentSenderRequest> locationClientSettingsResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("pttt", "onActivityResult2" + result.toString());
                    if (result.getResultCode() == RESULT_CANCELED) {
                        finish();
                    } else {
                        start();
                    }
                }
            });

    ActivityResultLauncher<Intent> appSettingsResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("pttt", "onActivityResult" + result.toString());
                    start();
                }
            });

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appSettingsResultLauncher.launch(intent);
    }

    private void askForPermissions(String permission) {
        Log.d("pttt", "permission = " + permission);
        if (shouldShowRequestPermissionRationale(permission)) {
            Log.d("pttt", "shouldShowRequestPermissionRationale");
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.

            if (permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)  &&  Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // This is a new method provided in API 30
                // Manually background location permission
                buildAlertMessageManuallyBackgroundPermission(permission);
            } else {
                requestPermissionLauncher.launch(permission);
            }

        } else {
            // 1. First Time
            // 2. Don't Ask Me Again state


            Log.d("pttt", "NOT shouldShowRequestPermissionRationale");
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(permission);
        }
    }

    private static String checkForMissingPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Manifest.permission.ACCESS_FINE_LOCATION;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Manifest.permission.ACCESS_COARSE_LOCATION;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q  &&  ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Manifest.permission.ACCESS_BACKGROUND_LOCATION;
        }

        return null;
    }

    private void enableLocationServicesProgrammatically() {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    @SuppressWarnings("deprecation")
    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This was deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    private void validateLocationSensorsEnabled() {
        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient

        LocationRequest locationRequest = new LocationRequest.Builder(1000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(1.0f)
                .setMinUpdateIntervalMillis(1000)
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);

        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(locationSettingsResponse -> {
                    int x = 0;
                    int y = x + 0;
                    state = STATE.LOCATION_SETTINGS_OK;
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {

                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(resolvable.getResolution()).build();
                                locationClientSettingsResultLauncher.launch(intentSenderRequest);


                                // // Cast to a resolvable exception.
                                // ResolvableApiException resolvable = (ResolvableApiException) e;
                                // // Show the dialog by calling startResolutionForResult(),
                                // // and check the result in onActivityResult().
                                // resolvable.startResolutionForResult(Activity_LocationValidationPro.this, 123);
                            } catch (Exception sie) {
                                Log.e("GPS", "Unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // 	Location settings can't be changed to meet the requirements, no dialog pops up

                            /*
                                Instead the default Yes, Not now and Never buttons if you call setAlwaysShow(true);
                                you will have only Yes and No, so the user won't choose Never and you will never
                                receive SETTINGS_CHANGE_UNAVAILABLE

                                ! Note !
                                if you have airplane mode on, and location off while requesting this,
                                you will actually receive a SETTINGS_CHANGE_UNAVAILABLE, even if you have setAlwaysShow
                             */

                            // Ask to disable Airplane Mode
                            // or
                            // Manually enable GPS
//                             buildAlertMessageNoGps();
                            Log.e("GPS", "Location settings are inadequate, and cannot be fixed here. Fix in Settings.");
                            break;
                    }
                })
                .addOnCanceledListener(() -> Log.e("GPS", "checkLocationSettings -> onCanceled"));
    }

    private void initViews() {
        location_BTN_back.setText("Close");
        location_BTN_back.setOnClickListener(v -> onBackPressed());
    }

    private void findViews() {
        location_LBL_title = findViewById(R.id.location_LBL_title);
        location_LBL_content = findViewById(R.id.location_LBL_content);
        location_LBL_progress = findViewById(R.id.location_LBL_progress);
        location_BTN_next = findViewById(R.id.location_BTN_next);
        location_BTN_back = findViewById(R.id.location_BTN_back);
    }
}