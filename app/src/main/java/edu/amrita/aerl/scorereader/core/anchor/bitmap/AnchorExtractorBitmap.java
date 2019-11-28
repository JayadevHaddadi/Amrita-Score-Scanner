package edu.amrita.aerl.scorereader.core.anchor.bitmap;

import android.graphics.Bitmap;

import edu.amrita.aerl.jayadev.util.Printer;
import edu.amrita.aerl.scorereader.core.support.Marker;

public class AnchorExtractorBitmap {
    private final MarkerFinderBitmap mf;
    private final int threshold;
    private double widthHeightRatio;

    public AnchorExtractorBitmap(double widthHeightRatio) {
        this.widthHeightRatio = widthHeightRatio;
        threshold = 40; //30
        mf = new MarkerFinderBitmap(threshold); // -10
    }

    public Marker extract(Bitmap image) {
        int height = image.getHeight();
        int width = image.getWidth();
        int halfWidth = width / 2;

        int topContrast = 0;
        int thisContrast = 0;
        int bottomContrast = 0;

        for (int y = 5; y < height-5; y++) {
            bottomContrast = image.getPixel(halfWidth, y) & 0xFF - image.getPixel(halfWidth, y + 2) & 0xFF;

            if (thisContrast > threshold && thisContrast >= bottomContrast
                    && thisContrast > topContrast) {
                try {
                    Marker marker = mf.followLine(image, halfWidth, y);
                    if (correctMarker(marker))
                        return marker;
                } catch (Exception e) {
                    System.out.println("Failed with line...");
                    e.printStackTrace();
                }
            }
            topContrast = thisContrast;
            thisContrast = bottomContrast;
        }

        Printer.p("marker was null");
        return null;
    }

    private boolean correctMarker(Marker marker) {
        double topLength = marker.getTopLength();
        if (topLength < 200)
            return false;
        double desiredSideLengths = topLength / widthHeightRatio;
        double errorMargin = desiredSideLengths * 0.1f;
        double leftSideLength = marker.getLeftSideLength();
        double rightSideLength = marker.getRightSideLength();
        if (desiredSideLengths > leftSideLength - errorMargin && desiredSideLengths < leftSideLength + errorMargin &&
                desiredSideLengths > rightSideLength - errorMargin && desiredSideLengths < rightSideLength + errorMargin) {
            return true;
        }
        return false;
    }
}
