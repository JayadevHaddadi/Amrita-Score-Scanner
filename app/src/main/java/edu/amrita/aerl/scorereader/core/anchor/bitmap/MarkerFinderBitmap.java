package edu.amrita.aerl.scorereader.core.anchor.bitmap;

import android.graphics.Bitmap;

import edu.amrita.aerl.scorereader.core.support.Marker;
import edu.amrita.aerl.scorereader.core.support.Point;


public class MarkerFinderBitmap {

	private Bitmap img;
	public int width;
	public int height;
	public int threshold;
	private Point topRightPoint;
	private Point topLeftPoint;
	private Point bottomRightPoint;
	private Point bottomLeftPoint;

    private int[][] topRightCornerFilterSmall = {{ 1, 1, 1},
        {-1,-1, 1},
        {-1,-1, 1}};
    private int[][] topLeftCornerFilterSmall = {{ 1, 1, 1},
        { 1,-1,-1},
        { 1,-1,-1}};

	public MarkerFinderBitmap(int threshold) {
		this.threshold = threshold;
	}

	public Marker followLine(Bitmap img, int x, int y) {
		this.img = img;
		int originalX = x, originalY = y;
		// going right
		while(true) {
			int top = img.getPixel(x+1, y-2) & 0xFF - img.getPixel(x+1, y) & 0xFF;
			int mid = img.getPixel(x+1, y-1) & 0xFF - img.getPixel(x+1, y+1) & 0xFF;
			int bot = img.getPixel(x+1, y) & 0xFF - img.getPixel(x+1, y+2) & 0xFF;
			if (mid > threshold && mid >= bot && mid > top) {
				x++;
			} else if (top > threshold && top >= bot) {
				x++;
				y--;
			} else if (bot > threshold) {
				x++;
				y++;
			} else {
				topRightPoint = findBestCorner5x5(x,y,topRightCornerFilterSmall);
//                topRightPoint = new Point(x,y);
				break;
			}
		}


		/*
		* Follow down on the right side
		*/
		x = topRightPoint.x;
		y = topRightPoint.y;
		while(true) {
			int left = (img.getPixel(x, y) & 0xFF) - (img.getPixel(x-2, y) & 0xFF);
			int mid = (img.getPixel(x+1, y) & 0xFF) - (img.getPixel(x-1, y) & 0xFF);
			int right = (img.getPixel(x+2, y) & 0xFF) - (img.getPixel(x, y) & 0xFF);
			if (mid > threshold && mid >= right && mid > left) {
				y++;
			} else if (left > threshold && left >= right) {
				x--;
				y++;
			} else if (right > threshold) {
				x++;
				y++;
			} else {
                bottomRightPoint = new Point(x,y);
				break;
			}
		}

		//going left
		x = originalX;
		y = originalY;
		while(true) {
			int top = img.getPixel(x-1, y-2) & 0xFF - img.getPixel(x-1, y) & 0xFF;
			int mid = img.getPixel(x-1, y-1) & 0xFF - img.getPixel(x-1, y+1) & 0xFF;
			int bot = img.getPixel(x-1, y) & 0xFF - img.getPixel(x-1, y+2) & 0xFF;
			if (mid > threshold && mid >= bot && mid > top) {
				x--;
			} else if (top > threshold && top >= bot) {
				x--;
				y--;
			} else if (bot > threshold) {
				x--;
				y++;
			} else {
				topLeftPoint = findBestCorner5x5(x,y,topLeftCornerFilterSmall);
//                topLeftPoint = new Point(x,y);
				break;
			}
		}

		/*
		* Follow down left
		* */
		x = topLeftPoint.x;
		y = topLeftPoint.y;
		while(true) {
			int left = (img.getPixel(x,y) & 0xFF) - (img.getPixel(x-2,y) & 0xFF);
			int mid = (img.getPixel(x+1,y) & 0xFF) - (img.getPixel(x-1,y) & 0xFF);
			int right = (img.getPixel(x+2,y) & 0xFF) - (img.getPixel(x,y) & 0xFF);
			if (mid < -threshold && mid <= right && mid < left) {
				y++;
			} else if (left < -threshold && left <= right) {
				x--;
				y++;
			} else if (right < -threshold) {
				x++;
				y++;
			} else {
				bottomLeftPoint = new Point(x,y);
				break;
			}
		}
		return new Marker(topLeftPoint,topRightPoint,bottomRightPoint,bottomLeftPoint);
	}

    private Point findBestCorner5x5(int x, int y, int[][] filter) {
        int bestX = x;
        int bestY = y;
        int bestCornerValue = Integer.MIN_VALUE;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int newValue = cornerFitnessValue(x+i,y+j, filter);
                if(newValue>bestCornerValue){
                    bestX = x+i;
                    bestY = y+j;
                    bestCornerValue = newValue;
                }
            }
        }
        return new Point(bestX,bestY);
    }

    private int cornerFitnessValue(int x, int y, int[][] filter) {
        int sum = 0;
        for (int i = - filter.length / 2; i <= filter.length / 2; i++) {
            for (int j = - filter[0].length / 2; j <= filter[0].length / 2; j++) {
                sum += (img.getPixel(x+i, y+j) & 0xFF) * filter[j + filter[0].length / 2][i + filter.length / 2];
//				Printer.po(i+","+j+"*" + filter[i + filter.length / 2][j + filter[0].length / 2]+"  ");
            }
//			Printer.p("\n");
        }
        return sum;
    }
}
