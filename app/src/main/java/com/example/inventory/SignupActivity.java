package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private EditText name, email, phone, password, confirm;
    private Button signup, googleBtn;
    private TextView login;
    private CheckBox cbTerms;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        name = findViewById(R.id.etName);
        email = findViewById(R.id.etEmail);
        phone = findViewById(R.id.etPhone);
        password = findViewById(R.id.etPassword);
        confirm = findViewById(R.id.etConfirm);

        signup = findViewById(R.id.btnSignup);
        googleBtn = findViewById(R.id.btnGoogle);

        login = findViewById(R.id.tvLogin);
        cbTerms = findViewById(R.id.cbTerms);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");
        hideSystemUI();
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        login.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        signup.setOnClickListener(v -> {

            String fullName = name.getText().toString().trim();
            String mail = email.getText().toString().trim();
            String phoneNo = phone.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirmPass = confirm.getText().toString().trim();

            if (TextUtils.isEmpty(fullName)) {
                name.setError("Enter Full Name");
                name.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(mail)) {
                email.setError("Enter Email");
                email.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                email.setError("Enter Valid Email");
                email.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(phoneNo)) {
                phone.setError("Enter Phone Number");
                phone.requestFocus();
                return;
            }

            if (phoneNo.length() != 10) {
                phone.setError("Enter Valid Phone Number");
                phone.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(pass)) {
                password.setError("Enter Password");
                password.requestFocus();
                return;
            }

            if (pass.length() < 6) {
                password.setError("Password must be at least 6 characters");
                password.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(confirmPass)) {
                confirm.setError("Confirm Password");
                confirm.requestFocus();
                return;
            }

            if (!pass.equals(confirmPass)) {
                confirm.setError("Passwords do not match");
                confirm.requestFocus();
                return;
            }

            if (!cbTerms.isChecked()) {
                Toast.makeText(this,
                        "Please accept Terms & Privacy Policy",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            signup.setEnabled(false);

            auth.createUserWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            signup.setEnabled(true);

                            if (task.isSuccessful()) {
                                String uid = auth.getCurrentUser().getUid();
                                User user = new User(
                                        fullName,
                                        mail,
                                        phoneNo
                                );

                                databaseRef.child(uid)
                                        .setValue(user)
                                        .addOnCompleteListener(task1 -> {

                                            Toast.makeText(SignupActivity.this,
                                                    "Account Created Successfully",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            startActivity(new Intent(SignupActivity.this,
                                                    DashActivity.class));
                                            finish();
                                        });
                            } else {
                                Toast.makeText(SignupActivity.this,
                                        task.getException().getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                    });
        });

        googleBtn.setOnClickListener(v -> {
            googleSignInClient.signOut();

            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(SignupActivity.this,
                        "Google Sign-In Failed: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        if (auth.getCurrentUser() == null) {
                            Toast.makeText(SignupActivity.this,
                                    "Authentication Failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        String uid = auth.getCurrentUser().getUid();
                        String fullName = "";
                        String emailAddress = "";

                        if (auth.getCurrentUser().getDisplayName() != null) {
                            fullName = auth.getCurrentUser().getDisplayName();
                        }
                        if (auth.getCurrentUser().getEmail() != null) {
                            emailAddress = auth.getCurrentUser().getEmail();
                        }
                        User user = new User(
                                fullName,
                                emailAddress,
                                ""
                        );
                        databaseRef.child(uid)
                                .setValue(user)
                                .addOnCompleteListener(saveTask -> {

                                    if (saveTask.isSuccessful()) {

                                        Toast.makeText(SignupActivity.this,
                                                "Google Sign-In Successful",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        startActivity(new Intent(SignupActivity.this,
                                                        DashActivity.class
                                                )
                                        );
                                        finish();
                                    } else {
                                        Toast.makeText(SignupActivity.this,
                                                "Failed to save user data",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                });
                    } else {
                        String message = "Authentication Failed";

                        if (task.getException() != null) {
                            message = task.getException().getMessage();
                        }
                        Toast.makeText(SignupActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
    private void hideSystemUI() {

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }
}