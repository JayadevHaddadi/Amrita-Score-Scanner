package edu.amrita.aerl.scorereader.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;

import java.text.DecimalFormat;

import edu.amrita.aerl.jayadev.util.Printer;
import edu.amrita.aerl.scorereader.android.Preview;
import amritacard.dl4j.NNModel;
import edu.amrita.aerl.scorereader.core.anchor.bitmap.AnchorExtractorBitmap;
import edu.amrita.aerl.scorereader.core.anchor.yuv.AnchorExtractorYUV;
import edu.amrita.aerl.scorereader.core.support.CharWithConf;
import edu.amrita.aerl.scorereader.core.support.Marker;

public class SheetReaderAndroid {
    public final SheetConfigNew2 config;
    private final AnchorExtractorYUV YUVAnchor;
    private final AnchorExtractorBitmap bitmapAnchor;
    private final int pictureWidth;
    private final double xRatio;
    private final double yRatio;
    private final Preview prev;
    private final Paint saveImagePaint;
    private double widthHeightRatio;
    private Marker latestMarker;
    private Marker latestMarkerFinal;
    public Bitmap image;
    public static final char[] LETTERS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z'};
    public static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private byte[] YUVArray;
    private Paint boxPaint;
    private DecimalFormat formatTwoDecimals = new DecimalFormat("#.##");
    private SheetReaderListener updateListener;
    private boolean saveNext = false;

    public SheetReaderAndroid(int displayWidth, int displayHeight, int pictureWidth, int pictureHeight, Preview prev) {
//        setupPaints();
        boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(3);
        boxPaint.setColor(Color.GREEN);

//        digitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        digitPaint.setStyle(Paint.Style.FILL);
//        digitPaint.setStrokeWidth(3);
//        digitPaint.setColor(Color.GREEN);
//        digitPaint.setTextSize(30);

        saveImagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        saveImagePaint.setStyle(Paint.Style.STROKE);
        saveImagePaint.setStrokeWidth(1);
        saveImagePaint.setColor(Color.RED);

        this.prev = prev;
        config = new SheetConfigNew2();
        widthHeightRatio = config.totalWidth / (double) config.totalHeight;
        YUVAnchor = new AnchorExtractorYUV(pictureWidth, pictureHeight);
        bitmapAnchor = new AnchorExtractorBitmap(widthHeightRatio);

        this.pictureWidth = pictureWidth;
        xRatio = (double) pictureWidth / (double) displayWidth;
        yRatio = (double) pictureHeight / (double) displayHeight;
        Printer.p("xRatio: " + xRatio);
        Printer.p("yRatio: " + yRatio);

    }

//    private void setupPaints() {
//        saveImagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        saveImagePaint.setStyle(Paint.Style.FILL_AND_STROKE);
//        saveImagePaint.setStrokeWidth(3);
//        saveImagePaint.setColor(Color.RED);
//
//        checkedBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        checkedBoxPaint.setStyle(Paint.Style.STROKE);
//        checkedBoxPaint.setStrokeWidth(3);
//        checkedBoxPaint.setColor(Color.GREEN);
//
//        float scale = getResources().getDisplayMetrics().density;
//        saveImagePaint.setTextSize(20 * scale);
//        checkedBoxPaint.setTextSize(13 * scale);
//    }

    public void setYUVArray(byte[] YUVArray) {
        this.YUVArray = YUVArray;
    }

    public Bitmap capture() {
        image = convertLastMarkerYuvToBitmap();
        image = projectImageToMarkerPlane(image);
        image = cropToOnlyMarker(image);
        return image;
    }

    public void updatePreview(Canvas canvas) {
        if (YUVArray == null)
            return;
        latestMarker = YUVAnchor.extract(YUVArray);
        if (latestMarker == null)
            return;

        if (saveNext) {
            image = capture();
            if (image != null) {
                saveNext = false;
                prev.imageReady(image);
                return;
            }
        }

        canvas.drawLine(x(latestMarker.topLeft.x), y(latestMarker.topLeft.y),
                x(latestMarker.topRight.x), y(latestMarker.topRight.y), boxPaint);
        canvas.drawLine(x(latestMarker.topRight.x), y(latestMarker.topRight.y),
                x(latestMarker.bottomRight.x), y(latestMarker.bottomRight.y), boxPaint);
        canvas.drawLine(x(latestMarker.topLeft.x), y(latestMarker.topLeft.y),
                x(latestMarker.bottomLeft.x), y(latestMarker.bottomLeft.y), boxPaint);
        canvas.drawLine(x(latestMarker.bottomLeft.x), y(latestMarker.bottomLeft.y),
                x(latestMarker.bottomRight.x), y(latestMarker.bottomRight.y), boxPaint);

        int margin = 8;
        double scale = latestMarker.getTopLength() / config.totalWidth;
        double startDigitX = latestMarker.topLeft.x + config.startDigitX * scale;
        double startDigitY = latestMarker.topLeft.y + config.startDigitY * scale;
        double startRollY = latestMarker.topLeft.y + config.startRollY * scale;
        int widthDigitBox = (int) ((config.boxWidth - margin) * scale);
        int heightDigitBox = (int) ((config.boxHeight - margin) * scale);
        double xTraverse = config.boxToBoxX * scale;
        double yTraverse = config.boxToBoxY * scale;
        Printer.p("scale: " + scale);
        double startY;

        for (int row = 0; row < config.layout.length; row++) {
            for (int col = 0; col < config.layout[0].length; col++) {
                if (row == 0)
                    startY = startRollY;
                else
                    startY = startDigitY + (row - 1) * yTraverse;

                if (config.layout[row][col] == config.LETTER) {
                    drawBox((int) (startDigitX + col * xTraverse), (int) (startY),
                            widthDigitBox, heightDigitBox, canvas, Color.GREEN,boxPaint);
                } else if (config.layout[row][col] == config.DIGIT) {
                    drawBox((int) (startDigitX + col * xTraverse), (int) (startY),
                            widthDigitBox, heightDigitBox, canvas, Color.BLUE,boxPaint);
                } else if (config.layout[row][col] == config.TOTAL || config.layout[row][col] == config.GRAND_TOTAL) {
                    drawBox((int) (startDigitX + col * xTraverse), (int) (startY),
                            widthDigitBox, heightDigitBox, canvas, Color.RED,boxPaint);
                }
            }
        }
    }

    private void drawBox(int centerX, int centerY, int width, int height, Canvas canvas, int color, Paint paint) {
        int startX = centerX - width / 2;
        int startY = centerY - height / 2;
        int endX = centerX + width / 2;
        int endY = centerY + height / 2;
        paint.setColor(color);
        canvas.drawRect(x(startX), y(startY), x(endX), y(endY), paint);
    }

    private int x(int x) {
        return (int) (x / xRatio);
    }

    private int y(int y) {
        return (int) (y / yRatio);
    }

    private Bitmap cropToOnlyMarker(Bitmap image) {
        if (image == null)
            return null;
        AnchorExtractorBitmap ae = new AnchorExtractorBitmap(widthHeightRatio);
        Marker marker = ae.extract(image);
        if (marker == null)
            return null;
        latestMarkerFinal = marker;
        return Bitmap.createBitmap(image, marker.topLeft.x, marker.topLeft.y,
                (int) (marker.getTopLength()), (int) (marker.getLeftSideLength()));
    }

    private Bitmap convertLastMarkerYuvToBitmap() {
        Printer.tStart("convertingt o yuv");
        int startX = latestMarker.bottomLeft.x < latestMarker.topLeft.x ? latestMarker.bottomLeft.x : latestMarker.topLeft.x;
        startX -= 10; //for margin
        int startY = latestMarker.topRight.y < latestMarker.topLeft.y ? latestMarker.topRight.y : latestMarker.topLeft.y;
        startY -= 10; //for margin
        int endX = latestMarker.bottomRight.x > latestMarker.topRight.x ? latestMarker.bottomRight.x : latestMarker.topRight.x;
        endX += 10; //for margin
        int endY = latestMarker.bottomRight.y > latestMarker.bottomLeft.y ? latestMarker.bottomRight.y : latestMarker.bottomLeft.y;
        endY += 10; //for margin

        int width = endX - startX;
        int height = endY - startY;

        int[] img = new int[width * height];

        for (int x = 0; x < width; x++) {
            int index = (x + startX) + (startY) * pictureWidth;
            for (int y = 0; y < height; y++, index += pictureWidth) {
                int val = (YUVArray[index] & 0xFF);
                img[x + y * width] = val + (val << 8)
                        + (val << 16) + 0xFF000000;
            }
        }

        Bitmap image = Bitmap.createBitmap(img, width, height, Bitmap.Config.ARGB_8888);
        Printer.tEnd("convertingt o yuv");
        return image;
    }

    private Bitmap projectImageToMarkerPlane(Bitmap image) {
        Marker marker = bitmapAnchor.extract(image);
        if (marker == null)
            return null;
        double topLineLength = marker.getTopLength();
        double bottomLineLength = marker.bottomLeft.distance(marker.bottomRight);

        /*
        * We want to project marker to become as
        * small as it's shortest width length
        * */
        float width, height;
        if (topLineLength < bottomLineLength) {
            width = (float) topLineLength;
            height = (float) (topLineLength / widthHeightRatio);
        } else {
            width = (float) bottomLineLength;
            height = (float) (bottomLineLength / widthHeightRatio);
        }

        float[] srcPoints = {marker.topLeft.x, marker.topLeft.y, marker.topRight.x, marker.topRight.y,
                marker.bottomRight.x, marker.bottomRight.y, marker.bottomLeft.x, marker.bottomLeft.y};
        float[] dstPoints = {0, 0, width, 0, width, height, 0, height};

        Matrix matrix = new Matrix();
        Printer.p("Can project?: " + matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4));

        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }

    public void startUpdateTask(Bitmap image, SheetReaderListener updateListener) {
        this.updateListener = updateListener;
        new UpdateImageTask().execute(image);
    }

    public void saveNext() {
        saveNext = true;
    }


    public class UpdateImageTask extends AsyncTask<Bitmap, CharWithConf, CharWithConf[][]> {
        @Override
        protected CharWithConf[][] doInBackground(Bitmap... bitmaps) {
            Printer.p("UpdateImageTask");
            Bitmap image = bitmaps[0];

            Canvas canvas = new Canvas(image);
            double markerWidth = image.getWidth();
            Printer.p("mainLine.getRealLength(): " + latestMarkerFinal.getTopLength());
            Printer.p("markerWidth: " + markerWidth);

            double scale = markerWidth / config.totalWidth;
            double startX = config.startDigitX * scale;
            double startDigitY = config.startDigitY * scale;
            double startRollY = config.startRollY * scale;
            double marginReduce = 0.9;
            int widthDigitBox = (int) ((config.boxWidth * marginReduce) * scale);
            int heightDigitBox = (int) ((config.boxHeight * marginReduce) * scale);
            double xTraverse = config.boxToBoxX * scale;
            double yTraverse = config.boxToBoxY * scale; // * 0.97;
            Printer.p("scale: " + scale);

            CharWithConf[][] allChars = new CharWithConf[config.layout.length][config.layout[0].length];

            for (int row = 0; row < config.layout.length; row++) {
                for (int col = 0; col < config.layout[0].length; col++) {
                    double[] foundInBox;
                    double startY;
                    if (config.layout[row][col] == SheetConfig.SKIP) {
                        Printer.p("Skip");
//                        publishProgress(null);
                        publishProgress();
                        continue;
                    } else {
                        if (row == 0)
                            startY = startRollY;
                        else
                            startY = startDigitY + (row - 1) * yTraverse;

                        /*
                        * If character is not fully connected there will be partial reading only
                        * */
                        foundInBox = CharExtractor.findCharInBoxCentered((int) Math.round(startX
                                + col * xTraverse), (int) startY, widthDigitBox, heightDigitBox, image);
                        drawBox((int) Math.round(startX + col * xTraverse), (int) startY,
                                widthDigitBox, heightDigitBox, canvas, Color.RED, saveImagePaint);

                        if (foundInBox == null) {
                            Printer.p("found null");

//                            publishProgress(null);
                            publishProgress();
                            continue;
                        }
                    }

                    CharWithConf prediction;
                    if (config.layout[row][col] == SheetConfig.LETTER) {
                        Printer.p("" + foundInBox);
                        double[] confArray = NNModel.forwardNNLetter(foundInBox);
                        prediction = new CharWithConf(confArray, LETTERS);
                    } else {
                        double[] confArray = NNModel.forwardNNDigit(foundInBox);
                        prediction = new CharWithConf(confArray, DIGITS);
                    }
                    Printer.p(prediction.toString());
                    allChars[row][col] = prediction;

                    publishProgress(prediction);
                    canvas.drawText("" + prediction.getChar(),
                            (float) (startX + col * xTraverse), (float) (startY), boxPaint);
                }
            }
            return allChars;
        }

        @Override
        public void onPostExecute(CharWithConf[][] result) {
            updateListener.onFinishUpdate(result);
        }

        @Override
        public void onProgressUpdate(CharWithConf... charWith) {
            if (charWith == null || charWith.length == 0)
                updateListener.onUpdateResult(null);
            else
                updateListener.onUpdateResult(charWith[0]);
        }
    }
}
