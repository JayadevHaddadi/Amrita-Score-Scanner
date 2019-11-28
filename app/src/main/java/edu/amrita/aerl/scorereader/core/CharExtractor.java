package edu.amrita.aerl.scorereader.core;

import android.graphics.Bitmap;

import java.util.ArrayList;

import edu.amrita.aerl.jayadev.util.Printer;

/**
 * Made at Amrita E-learning Research Lab
 *
 * Created by mreza on 09-Feb-18.
 */

class CharExtractor {

    private static int calcOtsuThresh(final int[] pixels) {
        final int[] histData = new int[256];
        int threshold;

        // Calculate histogram
        int ptr = 0;
        while (ptr < pixels.length) {
            final int h = 0xFF & pixels[ptr];
            histData[h]++;
            ptr++;
        }

        // Total number of pixels
        final int total = pixels.length;

        float totalIntensitySum = 0;
        for (int t = 0; t < 256; t++) {
            // Printer.p(histData[t]);
            totalIntensitySum += t * histData[t];
        }

        float intensitySumB = 0;
        int wB = 0;
        int wF;

        float varMax = 0;
        threshold = 0;

        int bestF = 0, bestB = 0;

        for (int t = 0; t < 256; t++) {
            wB += histData[t]; // Weight Background
            if (wB == 0) {
                continue;
            }

            wF = total - wB; // Weight Foreground
            if (wF == 0) {
                break;
            }

            intensitySumB += t * histData[t];

            final float mB = intensitySumB / wB; // Mean Background
            final float mF = (totalIntensitySum - intensitySumB) / wF; // Mean
            // Foreground

            // Calculate Between Class Variance
            final float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            // Printer.p(varBetween);
            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
                bestF = (int) mF;
                bestB = (int) mB;
            }
        }

        Printer.p("bestF: " + bestF);
        Printer.p("bestB: " + bestB);
        Printer.p("var max: " + varMax);
        Printer.p("sigma: " + Math.sqrt(varMax));
        Printer.p("threshold best: " + threshold);
        if (varMax < 1E9) {
            Printer.p("Too low variance");
            return 0;
        }

        return threshold;
    }

    static double[] findCharInBoxCentered(int centerX, int centerY, int width, int height, Bitmap imageB) {
        int startX = centerX - width / 2;
        int startY = centerY - height / 2;
        int endX = centerX + width / 2;
        int endY = centerY + height / 2;

        int wLength = endX - startX;
        int hLength = endY - startY;
        int size = wLength * hLength;
        Printer.p("Calc threshold on: " + wLength + "x" + hLength);

        int[] image = new int[size];
        imageB.getPixels(image, 0, wLength, startX, startY, wLength, hLength);

        final int threshold = calcOtsuThresh(image);

        final int w = wLength;
        final int h = hLength;
        final int xCenter = w / 2;
        final int yCenter = h / 2;
        final int maxSearchLength = w / 3;
        int foundX = -1;
        int foundY = -1;

        for (int i = 1; i < maxSearchLength; i++) {
            if ((image[xCenter + i + ((yCenter + i) * w)] & 0xFF) <= threshold) {
                foundX = xCenter + i;
                foundY = yCenter + i;
                break;
            }
            if ((image[(xCenter - i) + ((yCenter - i) * w)] & 0xFF) <= threshold) {
                foundX = xCenter - i;
                foundY = yCenter - i;
                break;
            }
            if ((image[(xCenter - i) + ((yCenter + i) * w)] & 0xFF) <= threshold) {
                foundX = xCenter - i;
                foundY = yCenter + i;
                break;
            }
            if ((image[xCenter + i + ((yCenter - i) * w)] & 0xFF) <= threshold) {
                foundX = xCenter + i;
                foundY = yCenter - i;
                break;
            }
        }
        if (foundX == -1) {
            Printer.p("Cant find character");
            return null;
        }
        Printer.p("foundX: " + foundX);
        Printer.p("foundY: " + foundY);

        ArrayList<Integer> oldPoints, newPoints = new ArrayList<Integer>();
        newPoints.add(foundX + (foundY * w));
        boolean modified = true;

        final int[] updateMatrix = new int[image.length];
        Printer.p("length: " + image.length);

        int minX = image.length, maxX = 0;
        int minY = image.length, maxY = 0;
        while (modified) {
            modified = false;

            oldPoints = newPoints;
            newPoints = new ArrayList<>();

            for (int i = 0; i < oldPoints.size(); i++) {
                for (int xn = -1; xn <= 1; xn++) {
                    for (int yn = -w; yn <= w; yn += w) {

                        int index = oldPoints.get(i) + xn + yn;

                        if (index<0 || index >= image.length || updateMatrix[index] != 0) {
                            /*
                            * TODO
                            * Maybe convert int to points so we can check if it is within limits....
                            * */
                            continue;
                        }

                        final int pixel = image[index] & 0xFF;
                        if (pixel <= threshold) {
                            if ((index % w) < minX) {
                                minX = index % w;
                            } else if ((index % w) > maxX) {
                                maxX = index % w;
                            }

                            if ((index / w) < minY) {
                                minY = index / w;
                            } else if ((index / w) > maxY) {
                                maxY = index / w;
                            }

                            updateMatrix[index] = 1;

                            newPoints.add(index);
                            modified = true;
                        } else {
                            updateMatrix[index] = 1;
                        }
                    }
                }
            }
        }

        Printer.arrayImageWithThreshold(image,w,h,threshold);

        Printer.p("minX: " + (minX));
        Printer.p("maxX: " + (maxX));
        Printer.p("minY: " + (minY));
        Printer.p("maxY: " + (maxY));

        int lengthX = maxX - minX;
        int lengthY = maxY - minY;

        double ratio = (double) lengthX / (double) lengthY;
        double reSizeX = ratio * 24;

        double step = lengthY / 24f;
        Printer.p("lengthX: " + lengthX);
        Printer.p("lengthY: " + lengthY);
        Printer.p("Step: " + step);
        double[] finalMatrix = new double[28 * 28];
        int xMargin = (int) Math.round((28 - reSizeX) / 2);
        Printer.p("xMargin: " + xMargin);
        for (int x = 0; x < reSizeX; x++) {
            for (int y = 0; y < 24; y++) {
                BOXES: for (double xBox = -step / 2; xBox < (step / 2); xBox++) {
                    for (double yBox = -step / 2; yBox < (step / 2); yBox++) {
                        int index = ((int) Math.round(minX + (x * step) + yBox)) +
                                ((int) Math.round(minY + (y * step) + xBox) * w);
                        if(index<0 || index>= image.length)
                            continue;
                        if (updateMatrix[index] == 1) {
                            finalMatrix[x + xMargin + ((y + 2) * 28)] = 1;
                            break BOXES;
                        }
                    }
                }
            }
        }
        Printer.p("Done resize");

        Printer.arrayImage(finalMatrix,28,28);

        return finalMatrix;
    }
}
