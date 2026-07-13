package com.example.inventory;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashActivity extends AppCompatActivity {
    ImageButton menu,notify,home,product,plus,report,profile;
    TextView TotalPro,low,TotalStock,value;
    DatabaseReference databaseReference;
    int pro=0;
    int lowStock=0;
    int stock=0;
    int totalValue=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dash);

        menu=findViewById(R.id.btnMenu);
        notify=findViewById(R.id.btnNotify);
        home=findViewById(R.id.btnHome);
        product=findViewById(R.id.btnProduct);
        plus=findViewById(R.id.btnPlus);
        report=findViewById(R.id.btnReport);
        profile=findViewById(R.id.btnProfile);

        TotalPro=findViewById(R.id.tvTotalPro);
        low=findViewById(R.id.tvLow);
        TotalStock=findViewById(R.id.tvTotal);
        value=findViewById(R.id.tvValue);

        databaseReference= FirebaseDatabase.getInstance().getReference("Products");
        loadDashboard();

    }
    private void loadDashboard(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pro=0;
                lowStock=0;
                stock=0;
                totalValue=0;

                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Product product=dataSnapshot.getValue(Product.class);
                    if(product!=null){
                        pro++;
                        stock+=product.getQuantity();
                        totalValue+=product.getQuantity()*product.getPrice();
                        if(product.getQuantity()<10){
                            lowStock++;
                        }
                    }
                }
                TotalPro.setText(String.valueOf(pro));
                low.setText(String.valueOf(lowStock));
                TotalStock.setText(String.valueOf(stock));
                value.setText(String.valueOf(totalValue));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
