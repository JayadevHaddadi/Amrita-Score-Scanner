package edu.amrita.aerl.scorereader.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import amritacard.android.R;
import edu.amrita.aerl.scorereader.core.SheetReaderAndroid;
import edu.amrita.aerl.jayadev.util.Printer;
import edu.amrita.aerl.scorereader.core.anchor.yuv.AnchorExtractorYUV;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
    public static SheetReaderAndroid sheetReader;
    public static Bitmap image;
    private final Context mContext;
    private int previewWidth;
    private int displayWidth;
    private int displayHeight;
    private Camera mCamera;
    private SurfaceHolder mHolder;

    private AnchorExtractorYUV de;
    private int previewHeight;
    private Camera.PreviewCallback mPreviewCallback = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            sheetReader.setYUVArray(data);
            invalidate();
        }
    };

    public Preview(Context context, Camera camera, int width, int height) {
        super(context);
        mCamera = camera;
        mContext = context;
        displayWidth = width;
        displayHeight = height;

        Button captureButton = ((PreviewActivity) context).findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(sheetReader.goodYUV!=null) {
//                    image = sheetReader.capture();
//
//                    Intent intent = new Intent(mContext, ShowResultsActivity.class);
//                    mContext.startActivity(intent);
//                }
//                else {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        sheetReader.saveNext();
//                        image = sheetReader.capture();
//                        if (image == null) {
//                            Printer.p("null");
//                            return;
//                        }
//                        Printer.p("found");
//                        mCamera.stopPreview();
//
//                        Intent intent = new Intent(mContext, ShowResultsActivity.class);
//                        mContext.startActivity(intent);
                    }
                });
//                }
            }
        });

        mHolder = getHolder();
        mHolder.addCallback(this);
        setBackgroundColor(Color.TRANSPARENT);

//        final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
//            @Override
//            public void onPictureTaken(byte[] bytes, Camera camera) {
//                Printer.tEnd("taking picture");
//                Printer.p("Finished taking pic");
//
//                Printer.p("Called JPEG callback, size: " + bytes.length);
//                image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                image = convertToGray(image);
//                invalidate();
//            }
//        };

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Printer.tStart("autofocus");
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Printer.tEnd("autofocus");
                    }
                });
            }
        };
        setOnClickListener(listener);
    }

    public void imageReady(Bitmap image) {
        this.image = image;
        mCamera.stopPreview();

        Intent intent = new Intent(mContext, ShowResultsActivity.class);
        mContext.startActivity(intent);
    }

    public void setCamera(Camera camera) {
        this.mCamera = camera;
    }

    public void onPause() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Printer.debug("Surface created");
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void setupPreview() {
//        try {
//            releaseCameraAndPreview();
//            mCamera = Camera.open();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        Camera.Parameters parameters = mCamera.getParameters();
        Printer.p("Size: " + parameters.getPictureSize().width + "x" + parameters.getPictureSize().height);

        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

        /*
        * Getting highest preview resolution
        */
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            parameters.setPreviewSize(size.width, size.height);
            previewWidth = size.width;
            previewHeight = size.height;
            de = new AnchorExtractorYUV(previewWidth, previewHeight);
            break;
        }

        sheetReader = new SheetReaderAndroid(displayWidth, displayHeight, previewWidth, previewHeight,this);
        Printer.p("displayWidth: " + displayWidth);
        Printer.p("displayHeight: " + displayHeight);
        Printer.p("previewWidth: " + previewWidth);
        Printer.p("previewHeight: " + previewHeight);

        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            parameters.setPreviewFormat(ImageFormat.NV21);
            mCamera.setPreviewCallback(mPreviewCallback);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            mCamera.autoFocus(null);
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Printer.debug("Surface destroyed");
//        if (mCamera != null) {
//            mCamera.stopPreview();
//            mCamera.release();
//            mCamera = null;
//        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Printer.debug("Surface changed");
        setupPreview();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        sheetReader.updatePreview(canvas);
    }

//    private void PrintLongString(String sb) {
//        String TAG = "AmritaCard";
//        if (sb.length() > 4000) {
//            Log.v(TAG, "sb.length = " + sb.length());
//            int chunkCount = sb.length() / 4000;     // integer division
//            for (int i = 0; i <= chunkCount; i++) {
//                int max = 4000 * (i + 1);
//                if (max >= sb.length()) {
//                    Log.v(TAG, "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i));
//                } else {
//                    Log.v(TAG, "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i, max));
//                }
//            }
//        } else {
//            Log.v(TAG, sb.stringValue());
//        }
//    }
//
    //    private Bitmap convertToGray(Bitmap image) {
//        int height = image.getHeight();
//        int width = image.getWidth();
//        Bitmap grayImg = Bitmap.createBitmap(width, height,
//                Bitmap.Config.RGB_565);
//        Canvas c = new Canvas(grayImg);
//        Paint paint = new Paint();
//        ColorMatrix cm = new ColorMatrix();
//        cm.setSaturation(0);
//        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
//        paint.setColorFilter(f);
//        c.drawBitmap(image, 0, 0, paint);
//        return grayImg;
//    }
}
