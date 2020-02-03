package com.example.qrscanner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MainActivity extends AppCompatActivity {
    private Button generate, scan;
    private EditText mytext;
    private ImageView qr_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generate = findViewById(R.id.generate);
        scan = findViewById(R.id.scan);
        mytext = findViewById(R.id.text);
        qr_code = findViewById(R.id.qrcode);

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mytext.getText().toString();
                if (text!= null && !text.isEmpty()) {
                    try {
                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                        BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 500, 500);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                        qr_code.setImageBitmap(bitmap);

                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(  MainActivity.this);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                intentIntegrator.setCameraId(0);
                intentIntegrator.setOrientationLocked(false);
                intentIntegrator.setPrompt("scanning");
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setBarcodeImageEnabled(true);
                intentIntegrator.initiateScan();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode,data);

        if(result!=null && result.getContents()!=null) {

            //After scanning we will create another qr code from given data
            int n = 50;
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = null;
            try {
                //encode content from qr code to bitmatrix
                matrix = writer.encode(result.getContents(), BarcodeFormat.QR_CODE, n, n);
            } catch (WriterException ex) {
                //
            }

            //matrix contains the boolean data about qr code
            //cut 4 first rows and columns, because there is padding
            //we cannot send 2d array to another app, but we can send 1d array
            //if qr code is too small (there is no data in some points) we will recieve an exception
            //we will just skip it and write false in our gameOfLifeData (golData)

            boolean[] golData = new boolean[n*n]; //array for GoL data
            //run through all golData array
            for (int r = 4; r < n+4; r++) //converting matrix content to golData array for rows
                for (int c = 4; c < n+4; c++) //columns
                    try {
                        golData[n*(r-4) + c-4] = matrix.get(c, r); //trying to get data from matrix, if this exists we put matrix data to array golData
                    } catch (Exception e) {
                        golData[n*(r-4) + c-4] = false; //if not return false
                    }

            //creating an intent for running Game of life app by package name
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.gameoflife");
                    //put extra golData in our intent
            launchIntent.putExtra("CELL_DATA", golData);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    Toast.makeText(MainActivity.this, "There is no package available in android", Toast.LENGTH_LONG).show();
                }


//            new AlertDialog.Builder(MainActivity.this)
//                    .setTitle("Scan Result")
//                    .setMessage(result.getContents())
//                    .setPositiveButton("Copy", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//                            ClipData data = ClipData.newPlainText("result", result.getContents());
//                            manager.setPrimaryClip(data);
//
//                        }
//                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            }).create().show();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
