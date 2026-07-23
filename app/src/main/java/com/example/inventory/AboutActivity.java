package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    Button privacyPolicy,terms;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        privacyPolicy=findViewById(R.id.btnPrivacy);
        terms=findViewById(R.id.btnTerms);

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AboutActivity.this, PrivacyPolicyActivity.class);
                startActivity(intent);
            }
        });
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AboutActivity.this, TermsActivity.class);
                startActivity(intent);
            }
        });

    }
}
