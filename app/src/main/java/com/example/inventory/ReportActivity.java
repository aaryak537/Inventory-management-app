package com.example.inventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {





    private TextView txtTotalProducts;
    private TextView txtLowStock;
    private TextView txtValue;

    private TextView txtOutStock;
    private TextView txtGoodStock;

    private TextView txtSalesRevenue;
    private TextView txtProductsSold;
    private TextView txtTransactions;

    private TextView txtReportDate;
    private TextView txtMovementEmpty;

    private ProgressBar progressOut;
    private ProgressBar progressGood;
    private ProgressBar progressLow;

    private LinearLayout movementContainer;

    private Button btnGenerate;
    private Button btnExport;






    private DatabaseReference productsRef;
    private DatabaseReference purchasesRef;
    private DatabaseReference salesRef;






    private final List<Product> productList = new ArrayList<>();
    private final List<Purchase> purchaseList = new ArrayList<>();
    private final List<Sale> saleList = new ArrayList<>();

    private final Map<String, Product> productMap = new HashMap<>();






    private int totalProducts = 0;
    private int totalUnits = 0;

    private int lowStockCount = 0;
    private int outOfStockCount = 0;
    private int goodStockCount = 0;

    private double totalInventoryValue = 0;






    private int totalTransactions = 0;
    private int totalProductsSold = 0;

    private double totalSalesRevenue = 0;






    private final ActivityResultLauncher<String> excelFileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    ),
                    this::writeExcelToUri
            );






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report);

        initializeViews();

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String uid = user.getUid();















        productsRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Products")
                        .child(uid);

        purchasesRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Purchases")
                        .child(uid);

        salesRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Sales")
                        .child(uid);


        btnGenerate.setOnClickListener(v ->
                loadCompleteReport()
        );

        btnExport.setOnClickListener(v ->
                exportExcel()
        );

        loadCompleteReport();
    }






    private void initializeViews() {

        txtTotalProducts =
                findViewById(R.id.txtTotalProducts);

        txtLowStock =
                findViewById(R.id.txtLowStock);

        txtValue =
                findViewById(R.id.txtValue);

        txtOutStock =
                findViewById(R.id.txtOutStock);

        txtGoodStock =
                findViewById(R.id.txtGoodStock);

        txtSalesRevenue =
                findViewById(R.id.txtSalesRevenue);

        txtProductsSold =
                findViewById(R.id.txtProductsSold);

        txtTransactions =
                findViewById(R.id.txtTransactions);

        txtReportDate =
                findViewById(R.id.txtReportDate);

        progressOut =
                findViewById(R.id.progressOut);

        progressGood =
                findViewById(R.id.progressGood);

        progressLow =
                findViewById(R.id.progressLow);

        movementContainer =
                findViewById(R.id.movementContainer);

        txtMovementEmpty =
                findViewById(R.id.txtMovementEmpty);

        btnGenerate =
                findViewById(R.id.btnGenerate);

        btnExport =
                findViewById(R.id.btnExport);
    }






    private void loadCompleteReport() {

        if (productsRef == null ||
                purchasesRef == null ||
                salesRef == null) {

            return;
        }

        setLoadingState();





        productsRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        productList.clear();
                        productMap.clear();

                        for (DataSnapshot child :
                                snapshot.getChildren()) {

                            Product product =
                                    child.getValue(Product.class);

                            if (product == null) {
                                continue;
                            }

                            product.setProductId(
                                    child.getKey()
                            );

                            productList.add(product);

                            if (child.getKey() != null) {

                                productMap.put(
                                        child.getKey(),
                                        product
                                );
                            }
                        }

                        calculateInventorySummary();





                        loadPurchases();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        showDatabaseError(
                                "Products",
                                error
                        );
                    }
                }
        );
    }






    private void loadPurchases() {

        purchasesRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        purchaseList.clear();

                        for (DataSnapshot child :
                                snapshot.getChildren()) {

                            Purchase purchase =
                                    child.getValue(
                                            Purchase.class
                                    );

                            if (purchase == null) {
                                continue;
                            }

                            if (purchase.getPurchaseId() == null ||
                                    purchase.getPurchaseId().isEmpty()) {

                                purchase.setPurchaseId(
                                        child.getKey()
                                );
                            }

                            purchaseList.add(purchase);
                        }





                        loadSales();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        showDatabaseError(
                                "Purchases",
                                error
                        );
                    }
                }
        );
    }






    private void loadSales() {

        salesRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        saleList.clear();

                        for (DataSnapshot child :
                                snapshot.getChildren()) {

                            Sale sale =
                                    child.getValue(Sale.class);

                            if (sale == null) {
                                continue;
                            }

                            if (sale.getSaleId() == null ||
                                    sale.getSaleId().isEmpty()) {

                                sale.setSaleId(
                                        child.getKey()
                                );
                            }

                            saleList.add(sale);
                        }

                        calculateSalesSummary();

                        updateReportUI();

                        loadInventoryMovement();

                        Toast.makeText(
                                ReportActivity.this,
                                "Report generated successfully",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        showDatabaseError(
                                "Sales",
                                error
                        );
                    }
                }
        );
    }






    private void calculateInventorySummary() {

        totalProducts = 0;
        totalUnits = 0;

        lowStockCount = 0;
        outOfStockCount = 0;
        goodStockCount = 0;

        totalInventoryValue = 0;


        for (Product product : productList) {

            if (product == null) {
                continue;
            }

            totalProducts++;

            int quantity =
                    Math.max(
                            product.getQuantity(),
                            0
                    );

            totalUnits += quantity;






            if (quantity <= 0) {

                outOfStockCount++;

            } else if (quantity <= 10) {

                lowStockCount++;

            } else {

                goodStockCount++;
            }








            totalInventoryValue +=
                    quantity *
                            Math.max(
                                    product.getCostPrice(),
                                    0
                            );
        }
    }






    private void calculateSalesSummary() {

        totalTransactions =
                saleList.size();

        totalProductsSold = 0;

        totalSalesRevenue = 0;


        for (Sale sale : saleList) {

            if (sale == null) {
                continue;
            }

            int quantity =
                    Math.max(
                            sale.getQuantity(),
                            0
                    );

            double amount =
                    Math.max(
                            sale.getTotalAmount(),
                            0
                    );

            totalProductsSold += quantity;

            totalSalesRevenue += amount;
        }
    }






    private void updateReportUI() {





        txtTotalProducts.setText(
                String.valueOf(totalProducts)
        );

        txtLowStock.setText(
                String.valueOf(lowStockCount)
        );

        txtOutStock.setText(
                String.valueOf(outOfStockCount)
        );

        txtGoodStock.setText(
                String.valueOf(goodStockCount)
        );

        txtValue.setText(
                "₹" +
                        formatAmount(
                                totalInventoryValue
                        )
        );






        txtSalesRevenue.setText(
                "₹" +
                        formatAmount(
                                totalSalesRevenue
                        )
        );

        txtProductsSold.setText(
                String.valueOf(totalProductsSold)
        );

        txtTransactions.setText(
                String.valueOf(totalTransactions)
        );






        txtReportDate.setText(
                "Generated: " +
                        getCurrentDateTime()
        );






        int max =
                Math.max(
                        totalProducts,
                        1
                );

        progressOut.setMax(max);
        progressLow.setMax(max);
        progressGood.setMax(max);

        progressOut.setProgress(
                Math.min(
                        outOfStockCount,
                        max
                )
        );

        progressLow.setProgress(
                Math.min(
                        lowStockCount,
                        max
                )
        );

        progressGood.setProgress(
                Math.min(
                        goodStockCount,
                        max
                )
        );
    }






    private void loadInventoryMovement() {

        movementContainer.removeAllViews();

        if (purchaseList.isEmpty() &&
                saleList.isEmpty()) {

            txtMovementEmpty.setVisibility(
                    TextView.VISIBLE
            );

            return;
        }

        txtMovementEmpty.setVisibility(
                TextView.GONE
        );






        for (Purchase purchase :
                purchaseList) {

            addMovementRow(
                    "PURCHASE",
                    purchase.getProductName(),
                    "+" +
                            purchase.getQuantity(),
                    purchase.getPurchaseDate(),
                    "Supplier: " +
                            safe(
                                    purchase.getSupplierName()
                            )
            );
        }






        for (Sale sale :
                saleList) {

            addMovementRow(
                    "SALE",
                    sale.getProductName(),
                    "-" +
                            sale.getQuantity(),
                    sale.getSaleDate(),
                    "Customer: " +
                            safe(
                                    sale.getCustomerName()
                            )
            );
        }
    }






    private void addMovementRow(
            String type,
            String productName,
            String quantity,
            String date,
            String extra
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.VERTICAL
        );

        row.setPadding(
                16,
                14,
                16,
                14
        );


        TextView title =
                new TextView(this);

        title.setText(
                type +
                        "  •  " +
                        safe(productName)
        );

        title.setTextSize(15);

        title.setTextColor(
                0xFF1565C0
        );

        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );


        TextView details =
                new TextView(this);

        details.setText(
                "Quantity: " +
                        quantity +
                        "\nDate: " +
                        safe(date) +
                        "\n" +
                        extra
        );

        details.setTextSize(13);

        details.setTextColor(
                0xFF666666
        );


        row.addView(title);

        row.addView(details);


        ViewDivider divider =
                new ViewDivider(this);

        movementContainer.addView(row);

        movementContainer.addView(divider);
    }






    private void setLoadingState() {

        txtTotalProducts.setText("...");
        txtLowStock.setText("...");
        txtOutStock.setText("...");
        txtGoodStock.setText("...");

        txtValue.setText("₹...");

        txtSalesRevenue.setText("₹...");
        txtProductsSold.setText("...");
        txtTransactions.setText("...");

        txtReportDate.setText(
                "Generating report..."
        );
    }






    private void exportExcel() {

        if (productList.isEmpty() &&
                purchaseList.isEmpty() &&
                saleList.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please generate the report first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        excelFileLauncher.launch("Smart_Shelf_Inventory_Report.xlsx");
    }


    private void writeExcelToUri(Uri uri) {

        if (uri == null) return;

        Toast.makeText(
                this,
                "Creating Excel report...",
                Toast.LENGTH_SHORT
        ).show();

        new Thread(() -> {

            try (XSSFWorkbook workbook = createExcelWorkbook();
                 OutputStream outputStream =
                         getContentResolver().openOutputStream(uri)) {

                if (outputStream == null) {
                    throw new Exception("Unable to open selected file");
                }

                workbook.write(outputStream);
                outputStream.flush();

                runOnUiThread(() ->
                        Toast.makeText(
                                ReportActivity.this,
                                "Excel report saved successfully",
                                Toast.LENGTH_LONG
                        ).show()
                );

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                ReportActivity.this,
                                "Could not save Excel report: " +
                                        e.getClass().getSimpleName() +
                                        ": " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }


    private XSSFWorkbook createExcelWorkbook() {

        XSSFWorkbook workbook =
                new XSSFWorkbook();






        Sheet summary =
                workbook.createSheet(
                        "Summary"
                );

        Row title =
                summary.createRow(0);

        title.createCell(0).setCellValue(
                "SMART SHELF - INVENTORY REPORT"
        );

        Row date =
                summary.createRow(1);

        date.createCell(0).setCellValue(
                "Generated"
        );

        date.createCell(1).setCellValue(
                getCurrentDateTime()
        );


        addSummaryRow(
                summary,
                3,
                "Total Products",
                String.valueOf(
                        totalProducts
                )
        );

        addSummaryRow(
                summary,
                4,
                "Total Units",
                String.valueOf(
                        totalUnits
                )
        );

        addSummaryRow(
                summary,
                5,
                "Good Stock",
                String.valueOf(
                        goodStockCount
                )
        );

        addSummaryRow(
                summary,
                6,
                "Low Stock",
                String.valueOf(
                        lowStockCount
                )
        );

        addSummaryRow(
                summary,
                7,
                "Out of Stock",
                String.valueOf(
                        outOfStockCount
                )
        );

        addSummaryRow(
                summary,
                8,
                "Inventory Value",
                "₹" +
                        formatAmount(
                                totalInventoryValue
                        )
        );

        addSummaryRow(
                summary,
                9,
                "Products Sold",
                String.valueOf(
                        totalProductsSold
                )
        );

        addSummaryRow(
                summary,
                10,
                "Sales Transactions",
                String.valueOf(
                        totalTransactions
                )
        );

        addSummaryRow(
                summary,
                11,
                "Sales Revenue",
                "₹" +
                        formatAmount(
                                totalSalesRevenue
                        )
        );






        Sheet inventory =
                workbook.createSheet(
                        "Inventory"
                );

        createInventoryHeader(
                inventory
        );

        int inventoryRow = 1;

        for (Product product :
                productList) {

            Row row =
                    inventory.createRow(
                            inventoryRow++
                    );

            int quantity =
                    Math.max(
                            product.getQuantity(),
                            0
                    );

            double cost =
                    Math.max(
                            product.getCostPrice(),
                            0
                    );

            double stockValue =
                    quantity * cost;

            row.createCell(0).setCellValue(
                    safe(product.getProductName())
            );

            row.createCell(1).setCellValue(
                    safe(product.getCategory())
            );

            row.createCell(2).setCellValue(
                    quantity
            );

            row.createCell(3).setCellValue(
                    cost
            );

            row.createCell(4).setCellValue(
                    stockValue
            );

            row.createCell(5).setCellValue(
                    getStockStatus(quantity)
            );
        }






        Sheet purchases =
                workbook.createSheet(
                        "Purchases"
                );

        createPurchaseHeader(
                purchases
        );

        int purchaseRow = 1;

        for (Purchase purchase :
                purchaseList) {

            Row row =
                    purchases.createRow(
                            purchaseRow++
                    );

            row.createCell(0).setCellValue(
                    safe(
                            purchase.getPurchaseDate()
                    )
            );

            row.createCell(1).setCellValue(
                    safe(
                            purchase.getProductName()
                    )
            );

            row.createCell(2).setCellValue(
                    purchase.getQuantity()
            );

            row.createCell(3).setCellValue(
                    purchase.getPurchasePrice()
            );

            row.createCell(4).setCellValue(
                    purchase.getTotalAmount()
            );

            row.createCell(5).setCellValue(
                    safe(
                            purchase.getSupplierName()
                    )
            );
        }






        Sheet sales =
                workbook.createSheet(
                        "Sales"
                );

        createSalesHeader(
                sales
        );

        int saleRow = 1;

        for (Sale sale :
                saleList) {

            Row row =
                    sales.createRow(
                            saleRow++
                    );

            row.createCell(0).setCellValue(
                    safe(
                            sale.getSaleDate()
                    )
            );

            row.createCell(1).setCellValue(
                    safe(
                            sale.getProductName()
                    )
            );

            row.createCell(2).setCellValue(
                    sale.getQuantity()
            );

            row.createCell(3).setCellValue(
                    sale.getSellingPrice()
            );

            row.createCell(4).setCellValue(
                    sale.getTotalAmount()
            );

            row.createCell(5).setCellValue(
                    safe(
                            sale.getCustomerName()
                    )
            );
        }






        Sheet movement =
                workbook.createSheet(
                        "Inventory Movement"
                );

        Row movementHeader =
                movement.createRow(0);

        movementHeader.createCell(0)
                .setCellValue("Type");

        movementHeader.createCell(1)
                .setCellValue("Product");

        movementHeader.createCell(2)
                .setCellValue("Quantity Change");

        movementHeader.createCell(3)
                .setCellValue("Date");

        int movementRow = 1;

        for (Purchase purchase :
                purchaseList) {

            Row row =
                    movement.createRow(
                            movementRow++
                    );

            row.createCell(0)
                    .setCellValue("PURCHASE");

            row.createCell(1)
                    .setCellValue(
                            safe(
                                    purchase.getProductName()
                            )
                    );

            row.createCell(2)
                    .setCellValue(
                            purchase.getQuantity()
                    );

            row.createCell(3)
                    .setCellValue(
                            safe(
                                    purchase.getPurchaseDate()
                            )
                    );
        }

        for (Sale sale :
                saleList) {

            Row row =
                    movement.createRow(
                            movementRow++
                    );

            row.createCell(0)
                    .setCellValue("SALE");

            row.createCell(1)
                    .setCellValue(
                            safe(
                                    sale.getProductName()
                            )
                    );

            row.createCell(2)
                    .setCellValue(
                            -sale.getQuantity()
                    );

            row.createCell(3)
                    .setCellValue(
                            safe(
                                    sale.getSaleDate()
                            )
                    );
        }






        // IMPORTANT: Do not use autoSizeColumn() on Android.
        // Apache POI's auto-size implementation depends on java.awt.font.FontRenderContext,
        // which is not available on Android and causes NoClassDefFoundError.
        // Use fixed column widths instead. Width values are in 1/256th of a character.
        for (Sheet sheet : workbook) {
            for (int i = 0; i < 8; i++) {
                sheet.setColumnWidth(i, 5000);
            }
        }

        return workbook;
    }






    private void addSummaryRow(
            Sheet sheet,
            int rowNumber,
            String label,
            String value
    ) {

        Row row =
                sheet.createRow(
                        rowNumber
                );

        row.createCell(0)
                .setCellValue(label);

        row.createCell(1)
                .setCellValue(value);
    }


    private void createInventoryHeader(
            Sheet sheet
    ) {

        Row row =
                sheet.createRow(0);

        row.createCell(0)
                .setCellValue("Product");

        row.createCell(1)
                .setCellValue("Category");

        row.createCell(2)
                .setCellValue("Quantity");

        row.createCell(3)
                .setCellValue("Cost Price");

        row.createCell(4)
                .setCellValue("Stock Value");

        row.createCell(5)
                .setCellValue("Status");
    }


    private void createPurchaseHeader(
            Sheet sheet
    ) {

        Row row =
                sheet.createRow(0);

        row.createCell(0)
                .setCellValue("Date");

        row.createCell(1)
                .setCellValue("Product");

        row.createCell(2)
                .setCellValue("Quantity");

        row.createCell(3)
                .setCellValue("Purchase Price");

        row.createCell(4)
                .setCellValue("Total Amount");

        row.createCell(5)
                .setCellValue("Supplier");
    }


    private void createSalesHeader(
            Sheet sheet
    ) {

        Row row =
                sheet.createRow(0);

        row.createCell(0)
                .setCellValue("Date");

        row.createCell(1)
                .setCellValue("Product");

        row.createCell(2)
                .setCellValue("Quantity");

        row.createCell(3)
                .setCellValue("Selling Price");

        row.createCell(4)
                .setCellValue("Total Amount");

        row.createCell(5)
                .setCellValue("Customer");
    }













    private String getStockStatus(
            int quantity
    ) {

        if (quantity <= 0) {

            return "Out of Stock";

        } else if (quantity <= 10) {

            return "Low Stock";

        } else {

            return "Healthy Stock";
        }
    }






    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }






    private String formatAmount(
            double amount
    ) {

        return String.format(
                Locale.getDefault(),
                "%,.2f",
                amount
        );
    }






    private String getCurrentDateTime() {

        return new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
        ).format(
                new Date()
        );
    }






    private void showDatabaseError(
            String section,
            DatabaseError error
    ) {

        Toast.makeText(
                this,
                "Failed to load " +
                        section +
                        ": " +
                        error.getMessage(),
                Toast.LENGTH_LONG
        ).show();

        txtReportDate.setText(
                "Unable to generate report"
        );
    }






    private static class ViewDivider
            extends android.view.View {

        public ViewDivider(
                android.content.Context context
        ) {

            super(context);

            setBackgroundColor(
                    0xFFE0E0E0
            );

            setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    )
            );
        }
    }
}