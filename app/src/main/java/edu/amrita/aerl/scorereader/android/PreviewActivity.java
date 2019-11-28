package edu.amrita.aerl.scorereader.android;

import android.app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import amritacard.android.R;
import edu.amrita.aerl.jayadev.util.Printer;

public class PreviewActivity extends Activity {
    public static final int QR_REQUEST_CODE = 10;
    private Preview mPreview;

    public static String SETTINGS = "setttings";
    private Camera camera;
    private CameraManager mCameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.amritacard_layout);

        getActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mCameraManager = new CameraManager(this);

        Display display = getWindowManager().getDefaultDisplay();
        mPreview = new Preview(this, mCameraManager.getCamera(), display.getWidth(), display.getHeight());
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == QR_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String[] resSplit = data.getStringExtra("result").split(":");
                Toast.makeText(this, "Success! Connecting to: " + resSplit[2] + " (" + resSplit[1] + ")", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Printer.debug("pause");
        mCameraManager.onPause();
//        if(camera != null) {
//            camera.stopPreview();
//            mPreview.setCamera(null);
//            camera.release();
//            camera = null;
//        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Printer.debug("Resume");

//        int numCams = Camera.getNumberOfCameras();
//        if(numCams > 0){
//            try{
//                camera = Camera.open(0);
//                camera.startPreview();
//                mPreview.setCamera(camera);
//            } catch (RuntimeException ex){
//                Printer.p("Lots of errors");
//            }
//        }
        mCameraManager.onResume();
        mPreview.setCamera(mCameraManager.getCamera());
    }
}
