package com.example.inventory;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.widget.Button;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.graphics.Paint;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReportActivity extends AppCompatActivity {
    TextView totalPro,lowStock;
    Button btnGenerate,btnExport;
    DatabaseReference productRef;
    int totalProducts = 0;
    int LowStocks = 0;
    int outStock = 0;
    double totalValue = 0;
    ProgressBar out,good,low;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        totalPro = findViewById(R.id.txtTotalProducts);
        lowStock = findViewById(R.id.txtLowStock);

        btnGenerate = findViewById(R.id.btnGenerate);
        btnExport = findViewById(R.id.btnExport);

        out=findViewById(R.id.progressOut);
        good=findViewById(R.id.progressGood);
        low=findViewById(R.id.progressLow);

        btnGenerate.setOnClickListener(v -> {
            loadReport();
            Toast.makeText(this,
                    "Report Generated",
                    Toast.LENGTH_SHORT).show();
        });

        btnExport.setOnClickListener(v -> {

            Toast.makeText(this,
                    "Export PDF Coming Soon",
                    Toast.LENGTH_SHORT).show();
            exportPDF();
        });



        productRef = FirebaseDatabase.getInstance()
                .getReference("Products");

        loadReport();
    }

    private void exportPDF() {
        PdfDocument pdf = new PdfDocument();
        Paint title = new Paint();
        Paint text = new Paint();
        title.setTextSize(24);
        title.setFakeBoldText(true);
        text.setTextSize(16);

        PdfDocument.PageInfo info =
                new PdfDocument.PageInfo.Builder(595,842,1).create();
        PdfDocument.Page page = pdf.startPage(info);
        int y = 60;
        page.getCanvas().drawText(
                "Smart Shelf Inventory Report",
                110, y, title);
        y += 50;

        page.getCanvas().drawText(
                "Generated On : "
                        + new java.text.SimpleDateFormat(
                        "dd MMM yyyy HH:mm",
                        java.util.Locale.getDefault())
                        .format(new java.util.Date()),
                50, y, text);

        y += 40;
        page.getCanvas().drawText(
                "Total Products : " + totalProducts,
                50, y, text);

        y += 30;
        page.getCanvas().drawText(
                "Good Stock : " + (totalProducts - LowStocks - outStock),
                50, y, text);

        y += 30;
        page.getCanvas().drawText(
                "Low Stock : " + LowStocks,
                50, y, text);

        y += 30;
        page.getCanvas().drawText(
                "Out Of Stock : " + outStock,
                50, y, text);

        y += 30;
        page.getCanvas().drawText(
                "Inventory Value : ₹"
                        + String.format("%,.2f", totalValue),
                50, y, text);

        y += 60;
        page.getCanvas().drawText(
                "Thank you for using Smart Shelf",
                50, y, text);

        pdf.finishPage(page);
        File dir =
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        if (dir != null && !dir.exists())
            dir.mkdirs();
        File file = new File(dir,
                "Inventory_Report.pdf");

        try {
            FileOutputStream fos = new FileOutputStream(file);
            pdf.writeTo(fos);
            fos.close();
            pdf.close();
            Toast.makeText(
                    this,
                    "PDF Saved Successfully\n"
                            + file.getAbsolutePath(),
                    Toast.LENGTH_LONG
            ).show();
        } catch (IOException e) {
            Toast.makeText(
                    this, "Export Failed",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
    private void loadReport() {
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalProducts = 0;
                LowStocks = 0;
                outStock = 0;
                totalValue = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product == null)
                        continue;
                    totalProducts++;
                    int qty = product.getQuantity();
                    if (qty == 0) {
                        outStock++;
                    } else if (qty <= 10) {
                        LowStocks++;
                    }
                    //totalValue += qty * product.getPrice();
                }
                int goodStock = totalProducts - LowStocks - outStock;

                totalPro.setText(String.valueOf(totalProducts));
                lowStock.setText(String.valueOf(LowStocks));
                out.setMax(totalProducts);
                low.setMax(totalProducts);
                good.setMax(totalProducts);

                out.setProgress(outStock);
                low.setProgress(LowStocks);
                good.setProgress(goodStock);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(
                        ReportActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}