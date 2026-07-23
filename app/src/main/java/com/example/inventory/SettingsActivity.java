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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {
    ImageView profileImg,next;

    TextView name, email;

    Button btnEditProfile, btnLogout;

    Switch switchNotification;

    LinearLayout layoutAbout;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        profileImg = findViewById(R.id.profileImage);

        name = findViewById(R.id.txtName);
        email = findViewById(R.id.tvEmail);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        layoutAbout=findViewById(R.id.layoutAbout);
        switchNotification = findViewById(R.id.switchNotification);
        next=findViewById(R.id.btnNext);
        loadUserData();

        btnEditProfile.setOnClickListener(v -> {

            Intent intent = new Intent(SettingsActivity.this,
                    EditProfileActivity.class);
            startActivity(intent);
        });

        next.setOnClickListener(v -> {

            Intent intent = new Intent(SettingsActivity.this,
                    AboutActivity.class);
            startActivity(intent);
        });

        // Notification Switch
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences preferences =
                    getSharedPreferences("Settings", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("notification", isChecked);
            editor.apply();
            if (isChecked) {
                Toast.makeText(this, "Notifications Enabled",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications Disabled",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // About App
        layoutAbout.setOnClickListener(v -> {

            Intent intent =
                    new Intent(SettingsActivity.this,
                            AboutActivity.class);
            startActivity(intent);
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent =
                    new Intent(SettingsActivity.this,
                            LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    private void loadUserData() {

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {

            email.setText(user.getEmail());

            if (user.getDisplayName() != null) {

                name.setText(user.getDisplayName());
            } else {
                name.setText("User Name");
            }
        }

        SharedPreferences preferences =
                getSharedPreferences("Settings", MODE_PRIVATE);

        boolean notification =
                preferences.getBoolean("notification", true);

        switchNotification.setChecked(notification);
    }
}
