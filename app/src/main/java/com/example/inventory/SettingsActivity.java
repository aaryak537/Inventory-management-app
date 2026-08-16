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



    private ImageView profileImg;
    private ImageView next;

    private TextView name;
    private TextView email;

    private Button btnEditProfile;
    private Button btnLogout;

    private Switch switchNotification;

    private LinearLayout layoutAbout;



    private FirebaseAuth auth;
    private DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);


        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser =
                auth.getCurrentUser();



        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            goToLogin();

            return;
        }


        String uid =
                currentUser.getUid();

        userRef =
                FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(uid);



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



        loadUserData();



        loadProfileImage();



        btnEditProfile.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SettingsActivity.this,
                            EditProfileActivity.class
                    );

            startActivity(intent);
        });

        next.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SettingsActivity.this,
                            AboutActivity.class
                    );

            startActivity(intent);
        });

        layoutAbout.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SettingsActivity.this,
                            AboutActivity.class
                    );

            startActivity(intent);
        });


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



        btnLogout.setOnClickListener(v -> {

            auth.signOut();

            goToLogin();
        });
    }


    private void loadUserData() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {
            return;
        }



        String authEmail =
                user.getEmail();

        if (authEmail != null &&
                !authEmail.isEmpty()) {

            email.setText(authEmail);

        } else {

            email.setText("No email");
        }


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


    @Override
    protected void onResume() {

        super.onResume();



        if (auth != null &&
                auth.getCurrentUser() != null) {

            loadUserData();

            loadProfileImage();
        }
    }
}