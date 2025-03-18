package com.guyi.class25b_ands_1;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.guyi.class25b_ands_1.databinding.ActivityContactsBinding;

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "ContactsActivity";
    private ActivityContactsBinding binding;
    private final String PERMISSION = android.Manifest.permission.READ_CONTACTS;
    private final int CONTACTS_REQUEST_CODE = 3344;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.btnRequest.setOnClickListener(v -> requestPermission());
        binding.btnUpdate.setOnClickListener(v -> update());



    }

    private void update() {
        String status = "";
        boolean isGranted = ContextCompat.checkSelfPermission(this, PERMISSION) == PackageManager.PERMISSION_GRANTED;
        status += PERMISSION + ":\n";
        status += isGranted ? "Permission granted" : "Permission not granted";

        binding.lblStatus.setText(status);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{PERMISSION}, CONTACTS_REQUEST_CODE);

        ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case CONTACTS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    update();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } }
}