package com.example.inventory;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {
    private TextInputEditText etCompany;
    private TextInputEditText etUsername;
    private TextInputEditText etEmployeeId;

    // Business Type
    private AutoCompleteTextView actBusinessType;

    // Buttons
    private MaterialButton btnSaveChanges;
    private FloatingActionButton fabEditPhoto;

    // Selected Image
    private Uri imageUri;
    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            imageUri = uri;
                            Toast.makeText(this,
                                    "Profile image selected",
                                    Toast.LENGTH_SHORT).show();

                            // If you have ImageView:
                            // imgProfile.setImageURI(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);


        etCompany = findViewById(R.id.etCompany);

        etUsername = findViewById(R.id.etUsername);

        etEmployeeId = findViewById(R.id.etEmployeeId);

        actBusinessType = findViewById(R.id.actBusinessType);

        fabEditPhoto = findViewById(R.id.fabEditPhoto);

        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        fabEditPhoto.setOnClickListener(v ->
                imagePicker.launch("image/*"));

        btnSaveChanges.setOnClickListener(v ->
                saveProfile());

        loadProfileData();
    }

    private void loadProfileData() {

        // Example Data
        etCompany.setText("Smart Shelf");

        etUsername.setText("John Name");

        etEmployeeId.setText("EMP001");

        actBusinessType.setText("Retail", false);
    }

    private void saveProfile() {

        String company =
                etCompany.getText().toString().trim();

        String username =
                etUsername.getText().toString().trim();

        String employeeId =
                etEmployeeId.getText().toString().trim();

        String business =
                actBusinessType.getText().toString().trim();

        if (TextUtils.isEmpty(company)) {
            etCompany.setError("Enter Company Name");
            etCompany.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Enter Username");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(employeeId)) {
            etEmployeeId.setError("Enter Employee ID");
            etEmployeeId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(business)) {
            actBusinessType.setError("Select Business Type");
            actBusinessType.requestFocus();
            return;
        }

        // TODO:
        // Save data to Firebase / SQLite

        Toast.makeText(this,
                "Profile Updated Successfully",
                Toast.LENGTH_LONG).show();

        finish();
    }
}