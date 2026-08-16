package com.example.inventory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageView profileImg;
    private ImageView next;

    private TextView name;
    private TextView email;

    private Button btnEditProfile;
    private Button btnLogout;

    private Switch switchNotification;

    private LinearLayout layoutAbout;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        // =====================================================
        // FIREBASE AUTH
        // =====================================================

        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser =
                auth.getCurrentUser();

        // =====================================================
        // CHECK LOGIN
        // =====================================================

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            goToLogin();

            return;
        }

        // =====================================================
        // FIREBASE USER REFERENCE
        // =====================================================

        String uid =
                currentUser.getUid();

        userRef =
                FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(uid);

        // =====================================================
        // FIND VIEWS
        // =====================================================

        profileImg =
                findViewById(R.id.profileImage);

        name =
                findViewById(R.id.txtName);

        email =
                findViewById(R.id.tvEmail);

        btnEditProfile =
                findViewById(R.id.btnEditProfile);

        btnLogout =
                findViewById(R.id.btnLogout);

        layoutAbout =
                findViewById(R.id.layoutAbout);

        switchNotification =
                findViewById(R.id.switchNotification);

        next =
                findViewById(R.id.btnNext);

        // =====================================================
        // LOAD USER DATA
        // =====================================================

        loadUserData();

        // =====================================================
        // LOAD PROFILE IMAGE
        // =====================================================

        loadProfileImage();

        // =====================================================
        // EDIT PROFILE
        // =====================================================

        btnEditProfile.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SettingsActivity.this,
                            EditProfileActivity.class
                    );

            startActivity(intent);
        });

        // =====================================================
        // ABOUT - NEXT BUTTON
        // =====================================================

        next.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SettingsActivity.this,
                            AboutActivity.class
                    );

            startActivity(intent);
        });

        // =====================================================
        // ABOUT APP
        // =====================================================

        layoutAbout.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SettingsActivity.this,
                            AboutActivity.class
                    );

            startActivity(intent);
        });

        // =====================================================
        // NOTIFICATION SETTING
        // =====================================================

        SharedPreferences preferences =
                getSharedPreferences(
                        "Settings",
                        MODE_PRIVATE
                );

        boolean notification =
                preferences.getBoolean(
                        "notification",
                        true
                );

        // Set initial value BEFORE listener
        switchNotification.setChecked(notification);

        switchNotification.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    SharedPreferences.Editor editor =
                            getSharedPreferences(
                                    "Settings",
                                    MODE_PRIVATE
                            ).edit();

                    editor.putBoolean(
                            "notification",
                            isChecked
                    );

                    editor.apply();

                    if (isChecked) {

                        Toast.makeText(
                                SettingsActivity.this,
                                "Notifications Enabled",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                SettingsActivity.this,
                                "Notifications Disabled",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        // =====================================================
        // LOGOUT
        // =====================================================

        btnLogout.setOnClickListener(v -> {

            auth.signOut();

            goToLogin();
        });
    }

    // =========================================================
    // LOAD USER DATA
    // =========================================================

    private void loadUserData() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {
            return;
        }

        // =====================================================
        // EMAIL FROM FIREBASE AUTH
        // =====================================================

        String authEmail =
                user.getEmail();

        if (authEmail != null &&
                !authEmail.isEmpty()) {

            email.setText(authEmail);

        } else {

            email.setText("No email");
        }

        // =====================================================
        // LOAD NAME FROM USERS/{UID}
        // =====================================================

        userRef.child("name")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                String userName =
                                        snapshot.getValue(
                                                String.class
                                        );

                                if (userName != null &&
                                        !userName.trim().isEmpty()) {

                                    name.setText(
                                            userName
                                    );

                                } else {

                                    /*
                                     * If Firebase database doesn't
                                     * have a name yet, check Auth.
                                     */

                                    if (user.getDisplayName() != null &&
                                            !user.getDisplayName()
                                                    .trim()
                                                    .isEmpty()) {

                                        name.setText(
                                                user.getDisplayName()
                                        );

                                    } else {

                                        name.setText(
                                                "User Name"
                                        );
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                                name.setText(
                                        "User Name"
                                );
                            }
                        }
                );
    }

    // =========================================================
    // LOAD PROFILE IMAGE
    // =========================================================

    private void loadProfileImage() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {
            return;
        }

        userRef.child("profileImageUrl")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                String imageUrl =
                                        snapshot.getValue(
                                                String.class
                                        );

                                // =====================================
                                // IMAGE EXISTS
                                // =====================================

                                if (imageUrl != null &&
                                        !imageUrl.trim().isEmpty()) {

                                    Glide.with(
                                                    SettingsActivity.this
                                            )
                                            .load(imageUrl)
                                            .placeholder(
                                                    R.drawable.ic_person
                                            )
                                            .error(
                                                    R.drawable.ic_person
                                            )
                                            .into(profileImg);

                                }

                                // =====================================
                                // NO IMAGE
                                // =====================================

                                else {

                                    profileImg.setImageResource(
                                            R.drawable.ic_person
                                    );
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                                profileImg.setImageResource(
                                        R.drawable.ic_person
                                );
                            }
                        }
                );
    }

    // =========================================================
    // GO TO LOGIN
    // =========================================================

    private void goToLogin() {

        Intent intent =
                new Intent(
                        SettingsActivity.this,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    // =========================================================
    // REFRESH DATA WHEN RETURNING FROM EDIT PROFILE
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        /*
         * This is important.
         *
         * When you return from EditProfileActivity,
         * SettingsActivity loads the updated name and
         * profile picture again.
         */

        if (auth != null &&
                auth.getCurrentUser() != null) {

            loadUserData();

            loadProfileImage();
        }
    }
}