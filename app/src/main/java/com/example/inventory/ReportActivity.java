package com.example.inventory;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    TextView totalPro, lowStock, value;
    private static final int CREATE_PDF_REQUEST = 1001;

    private String exportedPdfPath;
    Button btnGenerate, btnExport;

    ProgressBar out, good, low;

    DatabaseReference productRef;

    int totalProducts = 0;
    int lowStocks = 0;
    int outStock = 0;
    double totalValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);


        totalPro = findViewById(R.id.txtTotalProducts);
        lowStock = findViewById(R.id.txtLowStock);
        value = findViewById(R.id.txtValue);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnExport = findViewById(R.id.btnExport);

        out = findViewById(R.id.progressOut);
        good = findViewById(R.id.progressGood);
        low = findViewById(R.id.progressLow);


        FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String uid = currentUser.getUid();


        productRef = FirebaseDatabase.getInstance()
                .getReference("Products")
                .child(uid);

        btnGenerate.setOnClickListener(v -> {

            loadReport();

            Toast.makeText(
                    ReportActivity.this,
                    "Report Generated",
                    Toast.LENGTH_SHORT
            ).show();
        });


        btnExport.setOnClickListener(v -> {
            exportPDF();
        });


        loadReport();
    }


    private void loadReport() {

        productRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        totalProducts = 0;
                        lowStocks = 0;
                        outStock = 0;
                        totalValue = 0;

                        for (DataSnapshot data :
                                snapshot.getChildren()) {

                            Product product =
                                    data.getValue(Product.class);

                            if (product == null) {
                                continue;
                            }

                            totalProducts++;

                            int qty = product.getQuantity();

                            // Stock status
                            if (qty <= 0) {

                                outStock++;

                            } else if (qty <= 10) {

                                lowStocks++;
                            }
                            totalValue += qty * product.getCostPrice();
                        }

                        int goodStock =
                                totalProducts
                                        - lowStocks
                                        - outStock;

                        if (goodStock < 0) {
                            goodStock = 0;
                        }



                        totalPro.setText(
                                String.valueOf(totalProducts)
                        );


                        lowStock.setText(
                                String.valueOf(lowStocks)
                        );



                        value.setText(
                                "₹" + String.format(
                                        Locale.getDefault(),
                                        "%,.2f",
                                        totalValue
                                )
                        );



                        int max =
                                Math.max(totalProducts, 1);

                        out.setMax(max);
                        low.setMax(max);
                        good.setMax(max);

                        out.setProgress(outStock);
                        low.setProgress(lowStocks);
                        good.setProgress(goodStock);
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                ReportActivity.this,
                                "Firebase Error: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void exportPDF() {

        PdfDocument pdf = new PdfDocument();

        Paint title = new Paint();
        Paint text = new Paint();

        title.setTextSize(24);
        title.setFakeBoldText(true);

        text.setTextSize(16);


        PdfDocument.PageInfo info =
                new PdfDocument.PageInfo.Builder(
                        595,
                        842,
                        1
                ).create();

        PdfDocument.Page page = pdf.startPage(info);

        int y = 60;


        page.getCanvas().drawText(
                "Smart Shelf Inventory Report",
                110,
                y,
                title
        );

        y += 50;


        String date =
                new java.text.SimpleDateFormat(
                        "dd MMM yyyy HH:mm",
                        java.util.Locale.getDefault()
                ).format(new java.util.Date());

        page.getCanvas().drawText(
                "Generated On : " + date,
                50,
                y,
                text
        );

        y += 40;


        page.getCanvas().drawText(
                "Total Products : " + totalProducts,
                50,
                y,
                text
        );

        y += 30;


        int goodStock =
                totalProducts - lowStocks - outStock;

        if (goodStock < 0) {
            goodStock = 0;
        }

        page.getCanvas().drawText(
                "Good Stock : " + goodStock,
                50,
                y,
                text
        );

        y += 30;

        page.getCanvas().drawText(
                "Low Stock : " + lowStocks,
                50,
                y,
                text
        );

        y += 30;


        page.getCanvas().drawText(
                "Out Of Stock : " + outStock,
                50,
                y,
                text
        );

        y += 30;


        page.getCanvas().drawText(
                "Inventory Value : ₹"
                        + String.format(
                        java.util.Locale.getDefault(),
                        "%,.2f",
                        totalValue
                ),
                50,
                y,
                text
        );

        y += 60;


        page.getCanvas().drawText(
                "Thank you for using Smart Shelf",
                50,
                y,
                text
        );

        pdf.finishPage(page);



        File tempFile = new File(
                getCacheDir(),
                "Inventory_Report.pdf"
        );

        try {

            FileOutputStream fos =
                    new FileOutputStream(tempFile);

            pdf.writeTo(fos);

            fos.close();
            pdf.close();



            Intent intent = new Intent(
                    Intent.ACTION_CREATE_DOCUMENT
            );

            intent.addCategory(
                    Intent.CATEGORY_OPENABLE
            );

            intent.setType("application/pdf");

            intent.putExtra(
                    Intent.EXTRA_TITLE,
                    "Smart_Shelf_Inventory_Report.pdf"
            );


            exportedPdfPath =
                    tempFile.getAbsolutePath();

            startActivityForResult(
                    intent,
                    CREATE_PDF_REQUEST
            );

        } catch (IOException e) {

            pdf.close();

            Toast.makeText(
                    this,
                    "PDF Export Failed: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == CREATE_PDF_REQUEST
                && resultCode == RESULT_OK
                && data != null) {

            android.net.Uri uri =
                    data.getData();

            if (uri == null) {
                return;
            }

            try {

                File sourceFile =
                        new File(exportedPdfPath);

                java.io.InputStream inputStream =
                        new java.io.FileInputStream(
                                sourceFile
                        );

                java.io.OutputStream outputStream =
                        getContentResolver()
                                .openOutputStream(uri);

                byte[] buffer = new byte[4096];

                int length;

                while ((length =
                        inputStream.read(buffer)) > 0) {

                    outputStream.write(
                            buffer,
                            0,
                            length
                    );
                }

                inputStream.close();
                outputStream.close();

                Toast.makeText(
                        this,
                        "PDF downloaded successfully!",
                        Toast.LENGTH_LONG
                ).show();

                sourceFile.delete();

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "Could not save PDF: "
                                + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}