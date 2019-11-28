package edu.amrita.aerl.scorereader.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;

import amritacard.dl4j.NNModel;
import edu.amrita.aerl.jayadev.util.Printer;

/**
 * Made at Amrita E-learning Research Lab
 *
 * Created by mreza on 29-Jan-18.
 */

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 15;
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InputStream digitIS = null;
        InputStream lettersIS = null;
        try {
            Printer.tStart("Reading in files");
            AssetManager assets = getAssets();
            digitIS = assets.open("Digits_NN_Model.zip");
            lettersIS = assets.open("Letters_NN_Model.zip");
            Printer.p("found: " + digitIS.toString());
            Printer.tEnd("Reading in files");
        } catch (IOException e) {
            e.printStackTrace();
        }


        Printer.tStart("Read Models");
        if (!NNModel.initate(digitIS,lettersIS)) {
            Toast.makeText(this, "Can't access NN models", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
            } else {
                startAfterPermissionGranted();
            }
        }
        Printer.tEnd("Read Models");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Printer.debug("got permission");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startAfterPermissionGranted();
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startAfterPermissionGranted() {
        Printer.debug("PERMISSION GRANTED");

        Intent intent = new Intent(this, PreviewActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                Printer.debug("Got? " + permission);
                if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                    return false;
                }
            }
        }
        Printer.debug("Got all");
        return true;
    }

}
