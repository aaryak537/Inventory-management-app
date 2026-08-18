package com.example.inventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    // ============================================================
    // UI
    // ============================================================

    private TextView totalPro;
    private TextView lowStock;
    private TextView value;

    private TextView txtSalesRevenue;
    private TextView txtProductsSold;
    private TextView txtTransactions;
    private TextView txtReportDate;

    private Button btnGenerate;
    private Button btnExport;

    private ProgressBar out;
    private ProgressBar good;
    private ProgressBar low;


    // ============================================================
    // FIREBASE
    // ============================================================

    private DatabaseReference productRef;
    private DatabaseReference salesRef;


    // ============================================================
    // DATA
    // ============================================================

    private final List<Product> productList =
            new ArrayList<>();

    private final List<Sale> saleList =
            new ArrayList<>();

    /*
     * Product ID -> Product
     *
     * Used to find cost price for every sale.
     */
    private final Map<String, Product> productMap =
            new HashMap<>();


    // ============================================================
    // INVENTORY SUMMARY
    // ============================================================

    private int totalProducts = 0;
    private int totalUnits = 0;

    private int lowStocks = 0;
    private int outStock = 0;
    private int goodStock = 0;

    private double totalValue = 0;


    // ============================================================
    // SALES SUMMARY
    // ============================================================

    private int totalTransactions = 0;
    private int totalProductsSold = 0;

    private double totalSalesRevenue = 0;
    private double totalSalesCost = 0;
    private double totalProfit = 0;


    // ============================================================
    // EXCEL
    // ============================================================

    private static final int CREATE_EXCEL_REQUEST = 2001;


    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report);

        initializeViews();

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

        String uid =
                currentUser.getUid();


        // ========================================================
        // FIREBASE REFERENCES
        // ========================================================

        productRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Products")
                        .child(uid);

        salesRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("Sales")
                        .child(uid);


        // ========================================================
        // BUTTONS
        // ========================================================

        btnGenerate.setOnClickListener(v -> {

            loadCompleteReport();

        });


        btnExport.setOnClickListener(v -> {

            exportExcel();

        });


        // ========================================================
        // INITIAL LOAD
        // ========================================================

        loadCompleteReport();
    }


    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        totalPro =
                findViewById(R.id.txtTotalProducts);

        lowStock =
                findViewById(R.id.txtLowStock);

        value =
                findViewById(R.id.txtValue);


        txtSalesRevenue =
                findViewById(R.id.txtSalesRevenue);

        txtProductsSold =
                findViewById(R.id.txtProductsSold);

        txtTransactions =
                findViewById(R.id.txtTransactions);

        txtReportDate =
                findViewById(R.id.txtReportDate);


        btnGenerate =
                findViewById(R.id.btnGenerate);

        btnExport =
                findViewById(R.id.btnExport);


        out =
                findViewById(R.id.progressOut);

        good =
                findViewById(R.id.progressGood);

        low =
                findViewById(R.id.progressLow);
    }


    // ============================================================
    // LOAD COMPLETE REPORT
    // ============================================================

    private void loadCompleteReport() {

        if (productRef == null ||
                salesRef == null) {

            return;
        }


        /*
         * First load products.
         *
         * We need products before sales because
         * the product gives us the cost price.
         */

        productRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        productList.clear();
                        productMap.clear();

                        for (DataSnapshot data :
                                snapshot.getChildren()) {

                            Product product =
                                    data.getValue(Product.class);

                            if (product == null) {
                                continue;
                            }


                            // Firebase key is Product ID
                            product.setProductId(
                                    data.getKey()
                            );


                            productList.add(product);


                            if (product.getProductId() != null) {

                                productMap.put(
                                        product.getProductId(),
                                        product
                                );
                            }
                        }


                        calculateInventorySummary();


                        // Now load sales
                        loadSalesForReport();
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                ReportActivity.this,
                                "Failed to load products: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ============================================================
    // LOAD SALES
    // ============================================================

    private void loadSalesForReport() {

        salesRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        saleList.clear();

                        for (DataSnapshot data :
                                snapshot.getChildren()) {

                            Sale sale =
                                    data.getValue(Sale.class);

                            if (sale == null) {
                                continue;
                            }


                            /*
                             * Firebase key is Sale ID.
                             */

                            if (sale.getSaleId() == null ||
                                    sale.getSaleId().isEmpty()) {

                                sale.setSaleId(
                                        data.getKey()
                                );
                            }


                            saleList.add(sale);
                        }


                        calculateSalesSummary();

                        updateReportUI();


                        Toast.makeText(
                                ReportActivity.this,
                                "Report generated successfully",
                                Toast.LENGTH_SHORT
                        ).show();
                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                ReportActivity.this,
                                "Failed to load sales: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ============================================================
    // CALCULATE INVENTORY SUMMARY
    // ============================================================

    private void calculateInventorySummary() {

        totalProducts = 0;
        totalUnits = 0;

        lowStocks = 0;
        outStock = 0;
        goodStock = 0;

        totalValue = 0;


        for (Product product :
                productList) {

            if (product == null) {
                continue;
            }


            totalProducts++;


            int quantity =
                    product.getQuantity();

            totalUnits += quantity;


            // ====================================================
            // STOCK STATUS
            // ====================================================

            if (quantity <= 0) {

                outStock++;

            } else if (quantity <= 10) {

                lowStocks++;

            } else {

                goodStock++;
            }


            // ====================================================
            // INVENTORY VALUE
            // ====================================================

            totalValue +=
                    quantity *
                            product.getCostPrice();
        }
    }


    // ============================================================
    // CALCULATE SALES SUMMARY
    // ============================================================

    private void calculateSalesSummary() {

        totalTransactions =
                saleList.size();

        totalProductsSold = 0;

        totalSalesRevenue = 0;
        totalSalesCost = 0;
        totalProfit = 0;


        for (Sale sale :
                saleList) {

            if (sale == null) {
                continue;
            }


            int quantity =
                    sale.getQuantity();


            double revenue =
                    sale.getTotalAmount();


            /*
             * Find original product.
             *
             * This allows us to get cost price.
             */

            Product product =
                    productMap.get(
                            sale.getProductId()
                    );


            double costPrice = 0;


            if (product != null) {

                costPrice =
                        product.getCostPrice();
            }


            double saleCost =
                    quantity * costPrice;


            double profit =
                    revenue - saleCost;


            totalProductsSold += quantity;

            totalSalesRevenue += revenue;

            totalSalesCost += saleCost;

            totalProfit += profit;
        }
    }


    // ============================================================
    // UPDATE REPORT UI
    // ============================================================

    private void updateReportUI() {

        // ========================================================
        // INVENTORY
        // ========================================================

        totalPro.setText(
                String.valueOf(totalProducts)
        );


        lowStock.setText(
                String.valueOf(lowStocks)
        );


        value.setText(
                "₹" +
                        formatAmount(totalValue)
        );


        // ========================================================
        // SALES
        // ========================================================

        txtSalesRevenue.setText(
                "₹" +
                        formatAmount(totalSalesRevenue)
        );


        txtProductsSold.setText(
                String.valueOf(totalProductsSold)
        );


        txtTransactions.setText(
                String.valueOf(totalTransactions)
        );


        // ========================================================
        // DATE
        // ========================================================

        String currentDate =
                new SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.getDefault()
                ).format(
                        new Date()
                );


        txtReportDate.setText(
                "Generated: " + currentDate
        );


        // ========================================================
        // STOCK PROGRESS
        // ========================================================

        int max =
                Math.max(
                        totalProducts,
                        1
                );


        out.setMax(max);
        low.setMax(max);
        good.setMax(max);


        out.setProgress(outStock);
        low.setProgress(lowStocks);
        good.setProgress(goodStock);
    }


    // ============================================================
    // EXPORT EXCEL
    // ============================================================

    private void exportExcel() {

        /*
         * Make sure we have current data.
         */

        if (productList.isEmpty() &&
                saleList.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please generate the report first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        try {

            XSSFWorkbook workbook =
                    createExcelWorkbook();


            /*
             * Store workbook temporarily in memory/cache.
             *
             * We use ACTION_CREATE_DOCUMENT so the
             * user can select the save location.
             */

            java.io.File tempFile =
                    new java.io.File(
                            getCacheDir(),
                            "Smart_Shelf_Inventory_Report.xlsx"
                    );


            OutputStream fileOutputStream =
                    new java.io.FileOutputStream(
                            tempFile
                    );


            workbook.write(
                    fileOutputStream
            );


            fileOutputStream.close();

            workbook.close();


            Intent intent =
                    new Intent(
                            Intent.ACTION_CREATE_DOCUMENT
                    );


            intent.addCategory(
                    Intent.CATEGORY_OPENABLE
            );


            intent.setType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );


            intent.putExtra(
                    Intent.EXTRA_TITLE,
                    "Smart_Shelf_Inventory_Report.xlsx"
            );


            exportedExcelPath =
                    tempFile.getAbsolutePath();


            startActivityForResult(
                    intent,
                    CREATE_EXCEL_REQUEST
            );


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Excel export failed: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // ============================================================
    // TEMP EXCEL PATH
    // ============================================================

    private String exportedExcelPath;


    // ============================================================
    // CREATE WORKBOOK
    // ============================================================

    private XSSFWorkbook createExcelWorkbook() {

        XSSFWorkbook workbook =
                new XSSFWorkbook();


        // ========================================================
        // STYLES
        // ========================================================

        CellStyle titleStyle =
                createTitleStyle(workbook);

        CellStyle sectionStyle =
                createSectionStyle(workbook);

        CellStyle headerStyle =
                createHeaderStyle(workbook);

        CellStyle normalStyle =
                createNormalStyle(workbook);

        CellStyle currencyStyle =
                createCurrencyStyle(workbook);

        CellStyle integerStyle =
                createIntegerStyle(workbook);


        // ========================================================
        // SHEET 1 - SUMMARY
        // ========================================================

        createSummarySheet(
                workbook,
                titleStyle,
                sectionStyle,
                headerStyle,
                normalStyle,
                currencyStyle,
                integerStyle
        );


        // ========================================================
        // SHEET 2 - INVENTORY
        // ========================================================

        createInventorySheet(
                workbook,
                titleStyle,
                headerStyle,
                normalStyle,
                currencyStyle,
                integerStyle
        );


        // ========================================================
        // SHEET 3 - OUTGOING SALES
        // ========================================================

        createSalesSheet(
                workbook,
                titleStyle,
                headerStyle,
                normalStyle,
                currencyStyle,
                integerStyle
        );


        // ========================================================
        // SHEET 4 - CATEGORY SUMMARY
        // ========================================================

        createCategorySheet(
                workbook,
                titleStyle,
                headerStyle,
                normalStyle,
                currencyStyle,
                integerStyle
        );


        return workbook;
    }


    // ============================================================
    // SUMMARY SHEET
    // ============================================================

    private void createSummarySheet(
            XSSFWorkbook workbook,
            CellStyle titleStyle,
            CellStyle sectionStyle,
            CellStyle headerStyle,
            CellStyle normalStyle,
            CellStyle currencyStyle,
            CellStyle integerStyle
    ) {

        Sheet sheet =
                workbook.createSheet(
                        "Report Summary"
                );


        int rowNumber = 0;


        // ========================================================
        // TITLE
        // ========================================================

        Row titleRow =
                sheet.createRow(rowNumber++);


        Cell titleCell =
                titleRow.createCell(0);


        titleCell.setCellValue(
                "SMART SHELF - INVENTORY & SALES REPORT"
        );


        titleCell.setCellStyle(
                titleStyle
        );


        sheet.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0,
                        0,
                        0,
                        3
                )
        );


        rowNumber++;


        // ========================================================
        // REPORT INFORMATION
        // ========================================================

        Row dateRow =
                sheet.createRow(rowNumber++);

        dateRow.createCell(0)
                .setCellValue(
                        "Generated On"
                );

        dateRow.createCell(1)
                .setCellValue(
                        getCurrentDateTime()
                );


        rowNumber++;


        // ========================================================
        // INVENTORY SUMMARY
        // ========================================================

        Row inventoryTitle =
                sheet.createRow(rowNumber++);

        inventoryTitle
                .createCell(0)
                .setCellValue(
                        "INVENTORY SUMMARY"
                );

        inventoryTitle
                .getCell(0)
                .setCellStyle(
                        sectionStyle
                );


        String[] inventoryLabels = {

                "Total Products",
                "Total Units in Stock",
                "Good Stock Products",
                "Low Stock Products",
                "Out of Stock Products",
                "Current Inventory Value"
        };


        for (String label :
                inventoryLabels) {

            Row row =
                    sheet.createRow(
                            rowNumber++
                    );

            row.createCell(0)
                    .setCellValue(label);


            switch (label) {

                case "Total Products":

                    row.createCell(1)
                            .setCellValue(
                                    totalProducts
                            );

                    row.getCell(1)
                            .setCellStyle(
                                    integerStyle
                            );

                    break;


                case "Total Units in Stock":

                    row.createCell(1)
                            .setCellValue(
                                    totalUnits
                            );

                    row.getCell(1)
                            .setCellStyle(
                                    integerStyle
                            );

                    break;


                case "Good Stock Products":

                    row.createCell(1)
                            .setCellValue(
                                    goodStock
                            );

                    row.getCell(1)
                            .setCellStyle(
                                    integerStyle
                            );

                    break;


                case "Low Stock Products":

                    row.createCell(1)
                            .setCellValue(
                                    lowStocks
                            );

                    row.getCell(1)
                            .setCellStyle(
                                    integerStyle
                            );

                    break;


                case "Out of Stock Products":

                    row.createCell(1)
                            .setCellValue(
                                    outStock
                            );

                    row.getCell(1)
                            .setCellStyle(
                                    integerStyle
                            );

                    break;


                case "Current Inventory Value":

                    row.createCell(1)
                            .setCellValue(
                                    totalValue
                            );

                    row.getCell(1)
                            .setCellStyle(
                                    currencyStyle
                            );

                    break;
            }
        }


        rowNumber++;


        // ========================================================
        // SALES SUMMARY
        // ========================================================

        Row salesTitle =
                sheet.createRow(rowNumber++);

        salesTitle
                .createCell(0)
                .setCellValue(
                        "OUTGOING SALES SUMMARY"
                );

        salesTitle
                .getCell(0)
                .setCellStyle(
                        sectionStyle
                );


        String[] salesLabels = {

                "Total Transactions",
                "Total Products Sold",
                "Total Sales Revenue",
                "Total Sales Cost",
                "Total Profit"
        };


        for (String label :
                salesLabels) {

            Row row =
                    sheet.createRow(
                            rowNumber++
                    );


            row.createCell(0)
                    .setCellValue(label);


            switch (label) {

                case "Total Transactions":

                    row.createCell(1)
                            .setCellValue(
                                    totalTransactions
                            );

                    row.getCell(1)
                            .setCellStyle(
                                    integerStyle
                            );

                    break;


                case "Total Products Sold":

                    row.createCell(1)
                            .setCellValue(
                                    totalProductsSold
                            );

                    row.getCell(1)
                            .setCellStyle(
                                    integerStyle
                            );

                    break;


                case "Total Sales Revenue":

                    row.createCell(1)
                            .setCellValue(
                                    totalSalesRevenue
                            );

                    row.getCell(1)
                            .setCellStyle(
                                    currencyStyle
                            );

                    break;


                case "Total Sales Cost":

                    row.createCell(1)
                            .setCellValue(
                                    totalSalesCost
                            );

                    row.getCell(1)
                            .setCellStyle(
                                    currencyStyle
                            );

                    break;


                case "Total Profit":

                    row.createCell(1)
                            .setCellValue(
                                    totalProfit
                            );

                    row.getCell(1)
                            .setCellStyle(
                                    currencyStyle
                            );

                    break;
            }
        }


        // ========================================================
        // FOOTER
        // ========================================================

        rowNumber += 2;

        Row footer =
                sheet.createRow(rowNumber);

        footer.createCell(0)
                .setCellValue(
                        "Generated by Smart Shelf Inventory Management"
                );


        footer.getCell(0)
                .setCellStyle(
                        normalStyle
                );


        // ========================================================
        // WIDTH
        // ========================================================

        sheet.setColumnWidth(
                0,
                32 * 256
        );

        sheet.setColumnWidth(
                1,
                24 * 256
        );

        sheet.setColumnWidth(
                2,
                20 * 256
        );

        sheet.setColumnWidth(
                3,
                20 * 256
        );
    }


    // ============================================================
    // INVENTORY SHEET
    // ============================================================

    private void createInventorySheet(
            XSSFWorkbook workbook,
            CellStyle titleStyle,
            CellStyle headerStyle,
            CellStyle normalStyle,
            CellStyle currencyStyle,
            CellStyle integerStyle
    ) {

        Sheet sheet =
                workbook.createSheet(
                        "Inventory"
                );


        Row title =
                sheet.createRow(0);


        title.createCell(0)
                .setCellValue(
                        "INVENTORY DETAILS"
                );


        title.getCell(0)
                .setCellStyle(
                        titleStyle
                );


        sheet.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0,
                        0,
                        0,
                        11
                )
        );


        // ========================================================
        // HEADERS
        // ========================================================

        String[] headers = {

                "Product ID",
                "Product Name",
                "Category",
                "Brand",
                "Quantity",
                "Cost Price",
                "Selling Price",
                "Inventory Value",
                "Potential Sales Value",
                "Potential Profit",
                "Stock Status",
                "Description"
        };


        Row header =
                sheet.createRow(2);


        for (int i = 0;
             i < headers.length;
             i++) {

            Cell cell =
                    header.createCell(i);

            cell.setCellValue(
                    headers[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }


        // ========================================================
        // PRODUCT ROWS
        // ========================================================

        int rowNumber = 3;


        for (Product product :
                productList) {

            if (product == null) {
                continue;
            }


            Row row =
                    sheet.createRow(
                            rowNumber++
                    );


            int quantity =
                    product.getQuantity();


            double costPrice =
                    product.getCostPrice();


            double sellingPrice =
                    product.getSellingPrice();


            double inventoryValue =
                    quantity * costPrice;


            double potentialSales =
                    quantity * sellingPrice;


            double potentialProfit =
                    potentialSales -
                            inventoryValue;


            String status =
                    getStockStatus(
                            quantity
                    );


            row.createCell(0)
                    .setCellValue(
                            safe(
                                    product.getProductId()
                            )
                    );


            row.createCell(1)
                    .setCellValue(
                            safe(
                                    product.getProductName()
                            )
                    );


            row.createCell(2)
                    .setCellValue(
                            safe(
                                    product.getCategory()
                            )
                    );


            row.createCell(3)
                    .setCellValue(
                            safe(
                                    product.getBrandName()
                            )
                    );


            row.createCell(4)
                    .setCellValue(
                            quantity
                    );


            row.getCell(4)
                    .setCellStyle(
                            integerStyle
                    );


            row.createCell(5)
                    .setCellValue(
                            costPrice
                    );


            row.getCell(5)
                    .setCellStyle(
                            currencyStyle
                    );


            row.createCell(6)
                    .setCellValue(
                            sellingPrice
                    );


            row.getCell(6)
                    .setCellStyle(
                            currencyStyle
                    );


            row.createCell(7)
                    .setCellValue(
                            inventoryValue
                    );


            row.getCell(7)
                    .setCellStyle(
                            currencyStyle
                    );


            row.createCell(8)
                    .setCellValue(
                            potentialSales
                    );


            row.getCell(8)
                    .setCellStyle(
                            currencyStyle
                    );


            row.createCell(9)
                    .setCellValue(
                            potentialProfit
                    );


            row.getCell(9)
                    .setCellStyle(
                            currencyStyle
                    );


            row.createCell(10)
                    .setCellValue(
                            status
                    );


            row.createCell(11)
                    .setCellValue(
                            safe(
                                    product.getDescription()
                            )
                    );
        }


        // ========================================================
        // WIDTHS
        // ========================================================

        int[] widths = {

                22,
                25,
                18,
                18,
                12,
                15,
                15,
                18,
                22,
                20,
                18,
                35
        };


        for (int i = 0;
             i < widths.length;
             i++) {

            sheet.setColumnWidth(
                    i,
                    widths[i] * 256
            );
        }


        sheet.createFreezePane(
                0,
                3
        );
    }


    // ============================================================
    // SALES SHEET
    // ============================================================

    private void createSalesSheet(
            XSSFWorkbook workbook,
            CellStyle titleStyle,
            CellStyle headerStyle,
            CellStyle normalStyle,
            CellStyle currencyStyle,
            CellStyle integerStyle
    ) {

        Sheet sheet =
                workbook.createSheet(
                        "Outgoing Sales"
                );


        Row title =
                sheet.createRow(0);


        title.createCell(0)
                .setCellValue(
                        "OUTGOING SALES DETAILS"
                );


        title.getCell(0)
                .setCellStyle(
                        titleStyle
                );


        sheet.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0,
                        0,
                        0,
                        12
                )
        );


        String[] headers = {

                "Sale ID",
                "Date",
                "Time",
                "Product ID",
                "Product Name",
                "Customer Name",
                "Quantity",
                "Selling Price",
                "Total Amount",
                "Cost Price",
                "Total Cost",
                "Profit",
                "Payment Method"
        };


        Row header =
                sheet.createRow(2);


        for (int i = 0;
             i < headers.length;
             i++) {

            Cell cell =
                    header.createCell(i);

            cell.setCellValue(
                    headers[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }


        int rowNumber = 3;


        for (Sale sale :
                saleList) {

            if (sale == null) {
                continue;
            }


            Row row =
                    sheet.createRow(
                            rowNumber++
                    );


            Product product =
                    productMap.get(
                            sale.getProductId()
                    );


            double costPrice = 0;


            if (product != null) {

                costPrice =
                        product.getCostPrice();
            }


            int quantity =
                    sale.getQuantity();


            double totalAmount =
                    sale.getTotalAmount();


            double totalCost =
                    quantity *
                            costPrice;


            double profit =
                    totalAmount -
                            totalCost;


            // Sale ID

            row.createCell(0)
                    .setCellValue(
                            safe(
                                    sale.getSaleId()
                            )
                    );


            // Date

            row.createCell(1)
                    .setCellValue(
                            safe(
                                    sale.getSaleDate()
                            )
                    );


            // Time

            row.createCell(2)
                    .setCellValue(
                            safe(
                                    sale.getSaleTime()
                            )
                    );


            // Product ID

            row.createCell(3)
                    .setCellValue(
                            safe(
                                    sale.getProductId()
                            )
                    );


            // Product Name

            row.createCell(4)
                    .setCellValue(
                            safe(
                                    sale.getProductName()
                            )
                    );


            // Customer

            row.createCell(5)
                    .setCellValue(
                            safe(
                                    sale.getCustomerName()
                            )
                    );


            // Quantity

            row.createCell(6)
                    .setCellValue(
                            quantity
                    );


            row.getCell(6)
                    .setCellStyle(
                            integerStyle
                    );


            // Selling Price

            row.createCell(7)
                    .setCellValue(
                            sale.getSellingPrice()
                    );


            row.getCell(7)
                    .setCellStyle(
                            currencyStyle
                    );


            // Total Amount

            row.createCell(8)
                    .setCellValue(
                            totalAmount
                    );


            row.getCell(8)
                    .setCellStyle(
                            currencyStyle
                    );


            // Cost Price

            row.createCell(9)
                    .setCellValue(
                            costPrice
                    );


            row.getCell(9)
                    .setCellStyle(
                            currencyStyle
                    );


            // Total Cost

            row.createCell(10)
                    .setCellValue(
                            totalCost
                    );


            row.getCell(10)
                    .setCellStyle(
                            currencyStyle
                    );


            // Profit

            row.createCell(11)
                    .setCellValue(
                            profit
                    );


            row.getCell(11)
                    .setCellStyle(
                            currencyStyle
                    );


            // Payment

            row.createCell(12)
                    .setCellValue(
                            safe(
                                    sale.getPaymentMethod()
                            )
                    );
        }


        int[] widths = {

                22,
                17,
                15,
                22,
                25,
                25,
                12,
                17,
                18,
                15,
                18,
                17,
                18
        };


        for (int i = 0;
             i < widths.length;
             i++) {

            sheet.setColumnWidth(
                    i,
                    widths[i] * 256
            );
        }


        sheet.createFreezePane(
                0,
                3
        );
    }


    // ============================================================
    // CATEGORY SUMMARY
    // ============================================================

    private void createCategorySheet(
            XSSFWorkbook workbook,
            CellStyle titleStyle,
            CellStyle headerStyle,
            CellStyle normalStyle,
            CellStyle currencyStyle,
            CellStyle integerStyle
    ) {

        Sheet sheet =
                workbook.createSheet(
                        "Category Summary"
                );


        Row title =
                sheet.createRow(0);


        title.createCell(0)
                .setCellValue(
                        "CATEGORY SUMMARY"
                );


        title.getCell(0)
                .setCellStyle(
                        titleStyle
                );


        sheet.addMergedRegion(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0,
                        0,
                        0,
                        6
                )
        );


        String[] headers = {

                "Category",
                "Products",
                "Current Quantity",
                "Inventory Value",
                "Quantity Sold",
                "Sales Revenue",
                "Profit"
        };


        Row header =
                sheet.createRow(2);


        for (int i = 0;
             i < headers.length;
             i++) {

            Cell cell =
                    header.createCell(i);

            cell.setCellValue(
                    headers[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }


        // ========================================================
        // CATEGORY DATA
        // ========================================================

        Map<String, CategoryReportData> categoryMap =
                new HashMap<>();


        // ========================================================
        // INVENTORY
        // ========================================================

        for (Product product :
                productList) {

            if (product == null) {
                continue;
            }


            String category =
                    safe(
                            product.getCategory()
                    );


            if (category.trim().isEmpty()) {

                category =
                        "Uncategorized";
            }


            CategoryReportData data =
                    categoryMap.get(category);


            if (data == null) {

                data =
                        new CategoryReportData();

                categoryMap.put(
                        category,
                        data
                );
            }


            data.productCount++;

            data.currentQuantity +=
                    product.getQuantity();


            data.inventoryValue +=
                    product.getQuantity() *
                            product.getCostPrice();
        }


        // ========================================================
        // SALES
        // ========================================================

        for (Sale sale :
                saleList) {

            if (sale == null) {
                continue;
            }


            Product product =
                    productMap.get(
                            sale.getProductId()
                    );


            String category =
                    "Uncategorized";


            double costPrice = 0;


            if (product != null) {

                category =
                        safe(
                                product.getCategory()
                        );


                if (category.trim().isEmpty()) {

                    category =
                            "Uncategorized";
                }


                costPrice =
                        product.getCostPrice();
            }


            CategoryReportData data =
                    categoryMap.get(category);


            if (data == null) {

                data =
                        new CategoryReportData();

                categoryMap.put(
                        category,
                        data
                );
            }


            double revenue =
                    sale.getTotalAmount();


            double cost =
                    sale.getQuantity() *
                            costPrice;


            double profit =
                    revenue -
                            cost;


            data.quantitySold +=
                    sale.getQuantity();


            data.salesRevenue +=
                    revenue;


            data.profit +=
                    profit;
        }


        // ========================================================
        // WRITE CATEGORY ROWS
        // ========================================================

        int rowNumber = 3;


        for (Map.Entry<String,
                CategoryReportData> entry :
                categoryMap.entrySet()) {

            String category =
                    entry.getKey();


            CategoryReportData data =
                    entry.getValue();


            Row row =
                    sheet.createRow(
                            rowNumber++
                    );


            row.createCell(0)
                    .setCellValue(
                            category
                    );


            row.createCell(1)
                    .setCellValue(
                            data.productCount
                    );


            row.getCell(1)
                    .setCellStyle(
                            integerStyle
                    );


            row.createCell(2)
                    .setCellValue(
                            data.currentQuantity
                    );


            row.getCell(2)
                    .setCellStyle(
                            integerStyle
                    );


            row.createCell(3)
                    .setCellValue(
                            data.inventoryValue
                    );


            row.getCell(3)
                    .setCellStyle(
                            currencyStyle
                    );


            row.createCell(4)
                    .setCellValue(
                            data.quantitySold
                    );


            row.getCell(4)
                    .setCellStyle(
                            integerStyle
                    );


            row.createCell(5)
                    .setCellValue(
                            data.salesRevenue
                    );


            row.getCell(5)
                    .setCellStyle(
                            currencyStyle
                    );


            row.createCell(6)
                    .setCellValue(
                            data.profit
                    );


            row.getCell(6)
                    .setCellStyle(
                            currencyStyle
                    );
        }


        // ========================================================
        // WIDTHS
        // ========================================================

        sheet.setColumnWidth(
                0,
                25 * 256
        );

        sheet.setColumnWidth(
                1,
                15 * 256
        );

        sheet.setColumnWidth(
                2,
                20 * 256
        );

        sheet.setColumnWidth(
                3,
                20 * 256
        );

        sheet.setColumnWidth(
                4,
                18 * 256
        );

        sheet.setColumnWidth(
                5,
                20 * 256
        );

        sheet.setColumnWidth(
                6,
                18 * 256
        );


        sheet.createFreezePane(
                0,
                3
        );
    }


    // ============================================================
    // CATEGORY DATA CLASS
    // ============================================================

    private static class CategoryReportData {

        int productCount = 0;

        int currentQuantity = 0;

        int quantitySold = 0;

        double inventoryValue = 0;

        double salesRevenue = 0;

        double profit = 0;
    }


    // ============================================================
    // TITLE STYLE
    // ============================================================

    private CellStyle createTitleStyle(
            XSSFWorkbook workbook
    ) {

        CellStyle style =
                workbook.createCellStyle();


        Font font =
                workbook.createFont();

        font.setBold(true);
        font.setFontHeightInPoints(
                (short) 16
        );


        style.setFont(font);

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );


        return style;
    }


    // ============================================================
    // SECTION STYLE
    // ============================================================

    private CellStyle createSectionStyle(
            XSSFWorkbook workbook
    ) {

        CellStyle style =
                workbook.createCellStyle();


        Font font =
                workbook.createFont();

        font.setBold(true);
        font.setFontHeightInPoints(
                (short) 13
        );


        style.setFont(font);


        return style;
    }


    // ============================================================
    // HEADER STYLE
    // ============================================================

    private CellStyle createHeaderStyle(
            XSSFWorkbook workbook
    ) {

        CellStyle style =
                workbook.createCellStyle();


        Font font =
                workbook.createFont();

        font.setBold(true);

        font.setColor(
                IndexedColors.WHITE.getIndex()
        );


        style.setFont(font);


        style.setFillForegroundColor(
                IndexedColors.BLUE.getIndex()
        );


        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );


        style.setAlignment(
                HorizontalAlignment.CENTER
        );


        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );


        style.setBorderBottom(
                BorderStyle.THIN
        );


        style.setBorderTop(
                BorderStyle.THIN
        );


        style.setBorderLeft(
                BorderStyle.THIN
        );


        style.setBorderRight(
                BorderStyle.THIN
        );


        return style;
    }


    // ============================================================
    // NORMAL STYLE
    // ============================================================

    private CellStyle createNormalStyle(
            XSSFWorkbook workbook
    ) {

        CellStyle style =
                workbook.createCellStyle();


        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );


        return style;
    }


    // ============================================================
    // CURRENCY STYLE
    // ============================================================

    private CellStyle createCurrencyStyle(
            XSSFWorkbook workbook
    ) {

        CellStyle style =
                workbook.createCellStyle();


        style.setDataFormat(
                workbook.createDataFormat()
                        .getFormat(
                                "₹#,##0.00"
                        )
        );


        return style;
    }


    // ============================================================
    // INTEGER STYLE
    // ============================================================

    private CellStyle createIntegerStyle(
            XSSFWorkbook workbook
    ) {

        CellStyle style =
                workbook.createCellStyle();


        style.setDataFormat(
                workbook.createDataFormat()
                        .getFormat(
                                "#,##0"
                        )
        );


        return style;
    }


    // ============================================================
    // SAVE EXCEL USING DOCUMENT PICKER
    // ============================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );


        if (requestCode !=
                CREATE_EXCEL_REQUEST) {

            return;
        }


        if (resultCode !=
                RESULT_OK ||
                data == null) {

            return;
        }


        Uri uri =
                data.getData();


        if (uri == null) {
            return;
        }


        if (exportedExcelPath == null) {
            return;
        }


        try {

            java.io.File sourceFile =
                    new java.io.File(
                            exportedExcelPath
                    );


            java.io.InputStream inputStream =
                    new java.io.FileInputStream(
                            sourceFile
                    );


            java.io.OutputStream outputStream =
                    getContentResolver()
                            .openOutputStream(
                                    uri
                            );


            if (outputStream == null) {

                inputStream.close();

                Toast.makeText(
                        this,
                        "Unable to create Excel file",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }


            byte[] buffer =
                    new byte[8192];


            int length;


            while (
                    (length =
                            inputStream.read(buffer))
                            > 0
            ) {

                outputStream.write(
                        buffer,
                        0,
                        length
                );
            }


            outputStream.flush();

            inputStream.close();

            outputStream.close();


            sourceFile.delete();


            Toast.makeText(
                    this,
                    "Excel report exported successfully!",
                    Toast.LENGTH_LONG
            ).show();


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Could not save Excel report: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // ============================================================
    // STOCK STATUS
    // ============================================================

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


    // ============================================================
    // SAFE STRING
    // ============================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }


    // ============================================================
    // FORMAT AMOUNT
    // ============================================================

    private String formatAmount(
            double amount
    ) {

        if (amount ==
                (long) amount) {

            return String.format(
                    Locale.getDefault(),
                    "%d",
                    (long) amount
            );
        }


        return String.format(
                Locale.getDefault(),
                "%,.2f",
                amount
        );
    }


    // ============================================================
    // DATE
    // ============================================================

    private String getCurrentDateTime() {

        return new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
        ).format(
                new Date()
        );
    }
}