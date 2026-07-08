package com.example.inventory;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignupActivity extends AppCompatActivity {

    EditText email,password,confirm;
    Button signup;
    TextView login;
    CheckBox remember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);


        email=findViewById(R.id.etEmail);
        password=findViewById(R.id.etPassword);
        signup=findViewById(R.id.btnSignup);
        confirm=findViewById(R.id.etConfirm);
        login=findViewById(R.id.tvSignup);
        remember=findViewById(R.id.checkBox);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SignupActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = email.getText().toString();
                String pass = password.getText().toString();
                String same=confirm.getText().toString();

                if (mail.equals("") && pass.equals("")) {
                    Toast.makeText(SignupActivity.this, "Signup Successful",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignupActivity.this,
                            DashActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignupActivity.this, "Invalid email or password.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
