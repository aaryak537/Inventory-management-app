package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText name, email, phone, password, confirm;
    Button signup;
    TextView login;

    FirebaseAuth auth;
    DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Initialize Views
        name = findViewById(R.id.etName);
        email = findViewById(R.id.etEmail);
        phone = findViewById(R.id.etPhone);
        password = findViewById(R.id.etPassword);
        confirm = findViewById(R.id.etConfirm);
        signup = findViewById(R.id.btnSignup);
        login = findViewById(R.id.tvLogin);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        login.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        signup.setOnClickListener(v -> {

            String fullName = name.getText().toString().trim();
            String mail = email.getText().toString().trim();
            String phoneNo = phone.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String same = confirm.getText().toString().trim();

            // Validation
            if (fullName.isEmpty()) {
                name.setError("Enter Full Name");
                name.requestFocus();
                return;
            }

            if (mail.isEmpty()) {
                email.setError("Enter Email");
                email.requestFocus();
                return;
            }

            if (phoneNo.isEmpty()) {
                phone.setError("Enter Phone Number");
                phone.requestFocus();
                return;
            }

            if (phoneNo.length() != 10) {
                phone.setError("Enter Valid Phone Number");
                phone.requestFocus();
                return;
            }

            if (pass.isEmpty()) {
                password.setError("Enter Password");
                password.requestFocus();
                return;
            }

            if (pass.length() < 6) {
                password.setError("Password must be at least 6 characters");
                password.requestFocus();
                return;
            }

            if (same.isEmpty()) {
                confirm.setError("Confirm Password");
                confirm.requestFocus();
                return;
            }

            if (!pass.equals(same)) {
                confirm.setError("Passwords do not match");
                confirm.requestFocus();
                return;
            }

            // Create Firebase Account
            auth.createUserWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                String uid = auth.getCurrentUser().getUid();

                                // Save user details
                                User user = new User(
                                        fullName,
                                        mail,
                                        phoneNo
                                );

                                databaseRef.child(uid).setValue(user);

                                Toast.makeText(SignupActivity.this,
                                        "Account Created Successfully",
                                        Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(SignupActivity.this,
                                        DashActivity.class));
                                finish();

                            } else {

                                Toast.makeText(SignupActivity.this,
                                        task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        });

    }
}