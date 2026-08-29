package com.example.inventory;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {



    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etCompany;
    private TextInputEditText etUsername;
    private TextInputEditText etEmployeeId;

    private AutoCompleteTextView actBusinessType;



    private TextView tvProfileName;
    private TextView tvProductCount;



    private ImageView btnBack;
    private ImageView btnSave;

    private MaterialButton btnSaveChanges;
    private FloatingActionButton fabEditPhoto;



    private ImageView profileImage;

    private Uri imageUri;



    private FirebaseAuth firebaseAuth;


    private DatabaseReference userRef;
    private DatabaseReference productsRef;



    private FirebaseStorage firebaseStorage;
    private StorageReference profileImageRef;



    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {

                            imageUri = uri;


                            if (profileImage != null) {

                                profileImage.setImageURI(uri);
                            }

                            uploadProfileImage(uri);
                        }
                    }
            );



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editprofile);


        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }



        String uid = currentUser.getUid();




        userRef = FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child(uid);


        productsRef = FirebaseDatabase
                .getInstance()
                .getReference("Products")
                .child(uid);



        firebaseStorage =
                FirebaseStorage.getInstance();



        profileImageRef =
                firebaseStorage
                        .getReference()
                        .child("profile_images")
                        .child(uid + ".jpg");



        etName =
                findViewById(R.id.etName);

        etEmail =
                findViewById(R.id.etEmail);

        etPhone =
                findViewById(R.id.etPhone);

        etCompany =
                findViewById(R.id.etCompany);

        etUsername =
                findViewById(R.id.etUsername);

        etEmployeeId =
                findViewById(R.id.etEmployeeId);

        actBusinessType =
                findViewById(R.id.actBusinessType);

        tvProfileName =
                findViewById(R.id.tvProfileName);

        tvProductCount =
                findViewById(R.id.tvProductCount);

        profileImage =
                findViewById(R.id.profileImage);

        btnBack =
                findViewById(R.id.btnBack);

        btnSave =
                findViewById(R.id.btnSave);

        btnSaveChanges =
                findViewById(R.id.btnSaveChanges);

        fabEditPhoto =
                findViewById(R.id.fabEditPhoto);



        btnBack.setOnClickListener(v -> {

            finish();
        });



        btnSave.setOnClickListener(v -> {

            saveProfile();
        });



        btnSaveChanges.setOnClickListener(v -> {

            saveProfile();
        });



        fabEditPhoto.setOnClickListener(v -> {

            imagePicker.launch("image/*");
        });



        setupBusinessTypeDropdown();



        loadProfileData();



        loadProductCount();
    }

    private void setupBusinessTypeDropdown() {

        String[] businessTypes = {

                "Retail",
                "Wholesale",
                "Manufacturing",
                "E-Commerce",
                "Grocery",
                "Electronics",
                "Clothing",
                "Pharmacy",
                "Other"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        businessTypes
                );

        actBusinessType.setAdapter(adapter);

        actBusinessType.setOnClickListener(v -> {

            actBusinessType.showDropDown();
        });
    }



    private void loadProfileData() {

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            return;
        }



        String email =
                currentUser.getEmail();

        if (email != null && !email.isEmpty()) {

            etEmail.setText(email);



            etEmail.setEnabled(false);
        }



        userRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {

                            tvProfileName.setText(
                                    "Your Name"
                            );

                            return;
                        }



                        String name =
                                getStringValue(
                                        snapshot,
                                        "name"
                                );

                        if (!name.isEmpty()) {

                            etName.setText(name);

                            tvProfileName.setText(name);

                        } else {

                            tvProfileName.setText(
                                    "Your Name"
                            );
                        }



                        String phone =
                                getStringValue(
                                        snapshot,
                                        "phone"
                                );

                        if (!phone.isEmpty()) {

                            etPhone.setText(phone);
                        }



                        String company =
                                getStringValue(
                                        snapshot,
                                        "company"
                                );

                        if (!company.isEmpty()) {

                            etCompany.setText(company);
                        }



                        String username =
                                getStringValue(
                                        snapshot,
                                        "username"
                                );

                        if (!username.isEmpty()) {

                            etUsername.setText(username);
                        }


                        String employeeId =
                                getStringValue(
                                        snapshot,
                                        "employeeId"
                                );

                        if (!employeeId.isEmpty()) {

                            etEmployeeId.setText(employeeId);
                        }


                        String businessType =
                                getStringValue(
                                        snapshot,
                                        "businessType"
                                );

                        if (!businessType.isEmpty()) {

                            actBusinessType.setText(
                                    businessType,
                                    false
                            );
                        }



                        String imageUrl =
                                getStringValue(
                                        snapshot,
                                        "profileImageUrl"
                                );

                        if (!imageUrl.isEmpty()) {

                            Glide.with(
                                            EditProfileActivity.this
                                    )
                                    .load(imageUrl)
                                    .placeholder(
                                            R.drawable.ic_person
                                    )
                                    .error(
                                            R.drawable.ic_person
                                    )
                                    .into(profileImage);
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                EditProfileActivity.this,
                                "Unable to load profile: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void loadProductCount() {

        productsRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        long count =
                                snapshot.getChildrenCount();

                        tvProductCount.setText(
                                String.valueOf(count)
                        );
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        tvProductCount.setText("0");
                    }
                }
        );
    }


    private void uploadProfileImage(Uri uri) {

        if (uri == null) {
            return;
        }

        Toast.makeText(
                this,
                "Uploading profile picture...",
                Toast.LENGTH_SHORT
        ).show();



        profileImageRef
                .putFile(uri)

                .addOnSuccessListener(
                        taskSnapshot -> {



                            profileImageRef
                                    .getDownloadUrl()

                                    .addOnSuccessListener(
                                            downloadUri -> {

                                                String imageUrl =
                                                        downloadUri
                                                                .toString();

                                                userRef
                                                        .child(
                                                                "profileImageUrl"
                                                        )
                                                        .setValue(imageUrl)

                                                        .addOnSuccessListener(
                                                                unused -> {

                                                                    Toast.makeText(
                                                                            EditProfileActivity.this,
                                                                            "Profile picture saved",
                                                                            Toast.LENGTH_SHORT
                                                                    ).show();
                                                                }
                                                        )

                                                        .addOnFailureListener(
                                                                e -> {

                                                                    Toast.makeText(
                                                                            EditProfileActivity.this,
                                                                            "Failed to save image URL: "
                                                                                    + e.getMessage(),
                                                                            Toast.LENGTH_LONG
                                                                    ).show();
                                                                }
                                                        );
                                            }
                                    )

                                    .addOnFailureListener(
                                            e -> {

                                                Toast.makeText(
                                                        EditProfileActivity.this,
                                                        "Unable to get image URL",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )

                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    EditProfileActivity.this,
                                    "Image upload failed: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }



    private void saveProfile() {

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "User is not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String name =
                getText(etName);

        String phone =
                getText(etPhone);

        String company =
                getText(etCompany);

        String username =
                getText(etUsername);

        String employeeId =
                getText(etEmployeeId);

        String businessType =
                actBusinessType
                        .getText()
                        .toString()
                        .trim();



        if (TextUtils.isEmpty(name)) {

            etName.setError(
                    "Enter full name"
            );

            etName.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(company)) {

            etCompany.setError(
                    "Enter company name"
            );

            etCompany.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(username)) {

            etUsername.setError(
                    "Enter username"
            );

            etUsername.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(employeeId)) {

            etEmployeeId.setError(
                    "Enter Employee ID"
            );

            etEmployeeId.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(businessType)) {

            actBusinessType.setError(
                    "Select business type"
            );

            actBusinessType.requestFocus();

            return;
        }


        Map<String, Object> profileData =
                new HashMap<>();

        profileData.put(
                "name",
                name
        );

        profileData.put(
                "phone",
                phone
        );

        profileData.put(
                "company",
                company
        );

        profileData.put(
                "username",
                username
        );

        profileData.put(
                "employeeId",
                employeeId
        );

        profileData.put(
                "businessType",
                businessType
        );


        userRef
                .updateChildren(profileData)

                .addOnSuccessListener(
                        unused -> {

                            tvProfileName.setText(name);

                            Toast.makeText(
                                    EditProfileActivity.this,
                                    "Profile Updated Successfully",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                        }
                )

                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    EditProfileActivity.this,
                                    "Failed to update profile: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }



    private String getStringValue(
            DataSnapshot snapshot,
            String key) {

        String value =
                snapshot
                        .child(key)
                        .getValue(String.class);

        return value != null
                ? value
                : "";
    }



    private String getText(
            TextInputEditText editText) {

        if (editText == null) {

            return "";
        }

        if (editText.getText() == null) {

            return "";
        }

        return editText
                .getText()
                .toString()
                .trim();
    }
}