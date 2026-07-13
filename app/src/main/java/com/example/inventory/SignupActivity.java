package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    EditText name,email,password,confirm;
    Button signup;
    TextView login;
    FirebaseAuth auth;
    DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        name=findViewById(R.id.etName);
        email=findViewById(R.id.etEmail);
        password=findViewById(R.id.etPassword);
        signup=findViewById(R.id.btnSignup);
        confirm=findViewById(R.id.etConfirm);
        login=findViewById(R.id.txtLogin);

        auth=FirebaseAuth.getInstance();
        databaseRef= FirebaseDatabase.getInstance().getReference("Users");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SignupActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = email.getText().toString().trim();
                String pass = password.getText().toString().trim();
                String same=confirm.getText().toString().trim();

                if (mail.isEmpty()) {
                    email.setError("Enter Email");
                    return;
                }
                else if(pass.isEmpty()){
                    password.setError("Enter Password");
                    return;
                }
                else if(same.isEmpty()){
                    confirm.setError("Confirm Password");
                    return;
                }
                else if(!pass.equals(same)){
                    password.setError("Password do not match");
                }
                else{
                    auth.createUserWithEmailAndPassword(mail,pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String uid=auth.getCurrentUser().getUid();
                                User user=new User(mail);

                                databaseRef.child(uid).setValue(user);

                                Toast.makeText(SignupActivity.this,"Account Created Successfully",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent=new Intent(SignupActivity.this,DashActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(SignupActivity.this,
                                        task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}