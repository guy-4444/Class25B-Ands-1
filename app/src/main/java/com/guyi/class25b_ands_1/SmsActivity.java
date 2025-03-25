package com.guyi.class25b_ands_1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.guyi.class25b_ands_1.databinding.ActivityContactsBinding;

public class SmsActivity extends AppCompatActivity {

    private static final String TAG = "ContactsActivity";
    private ActivityContactsBinding binding;
    private final String PERMISSION = Manifest.permission.READ_SMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.btnAction.setOnClickListener(v -> readLastSmsClicked());
        binding.btnUpdate.setOnClickListener(v -> update());
    }

    private void readLastSmsClicked() {
        requestPermissionLauncher.launch(PERMISSION);
    }

    private void readLastSms() {
        Toast.makeText(this, "New Sms received", Toast.LENGTH_SHORT).show();
    }

    private void update() {
        String status = "";
        boolean isGranted = ContextCompat.checkSelfPermission(this, PERMISSION) == PackageManager.PERMISSION_GRANTED;
        status += PERMISSION + ":\n";
        status += isGranted ? "Permission granted" : "Permission not granted";

        binding.lblStatus.setText(status);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                handlePermission();
            });

    private void handlePermission() {
        if (ContextCompat.checkSelfPermission(this, PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            readLastSms();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected, and what
            // features are disabled if it's declined. In this UI, include a
            // "cancel" or "no thanks" button that lets the user continue
            // using your app without granting the permission.

            openPermissionInfo();
        } else {
            openSettingsInfo();
        }
    }

    private void openPermissionInfo() {
        new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle("SMS")
                .setMessage("We need it for...")
                .setPositiveButton("Got It", (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.READ_SMS))
                .setNegativeButton("No", null)
                .show();
    }

    private void openSettingsInfo() {
        new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle("SMS")
                .setMessage("Settings -> Permissions -> SMS -> Allow all the time")
                .setPositiveButton("Got It", (dialog, which) -> openSettings())
                .setNegativeButton("No", null)
                .show();
    }

    private ActivityResultLauncher<Intent> manuallyPermissionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> readLastSmsClicked());

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        manuallyPermissionResultLauncher.launch(intent);
    }

}