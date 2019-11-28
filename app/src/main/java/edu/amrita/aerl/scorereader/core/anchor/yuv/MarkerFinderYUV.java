package edu.amrita.aerl.scorereader.core.anchor.yuv;

import edu.amrita.aerl.scorereader.core.support.Marker;
import edu.amrita.aerl.scorereader.core.support.Point;

public class MarkerFinderYUV {

	public int width;
	public int height;
	public int threshold;
	protected int doubleWidth;
	private Point topRightPoint;
	private Point topLeftPoint;
	private Point bottomRightPoint;
	private Point bottomLeftPoint;
	private byte[] img;

    private int[][] topRightCornerFilterSmall = {{ 1, 1, 1},
        {-1,-1, 1},
        {-1,-1, 1}};
    private int[][] topLeftCornerFilterSmall = {{ 1, 1, 1},
        { 1,-1,-1},
        { 1,-1,-1}};


	public MarkerFinderYUV(int width, int threshold) {
		this.width = width;
		this.doubleWidth = width*2;
		this.threshold = threshold;
	}

	public Marker followLine(byte[] img, int x, int y) {
		this.img = img;
		int originalX = x, originalY = y;
		// going right
		while(true) {
			int top = (img[x+1 + (y-2)*width] & 0xFF) - (img[x+1 + (y+0)*width] & 0xFF);
			int mid = (img[x+1 + (y-1)*width] & 0xFF) - (img[x+1 + (y+1)*width] & 0xFF);
			int bot = (img[x+1 + (y-0)*width] & 0xFF) - (img[x+1 + (y+2)*width] & 0xFF);
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
		int index = x + y * width;
		while(true) {
			int left = (img[index] & 0xFF) - (img[index - 2] & 0xFF);
			int mid = (img[index + 1] & 0xFF) - (img[index - 1] & 0xFF);
			int right = (img[index + 2] & 0xFF) - (img[index] & 0xFF);
			if (mid > threshold && mid >= right && mid > left) {
				index += width;
			} else if (left > threshold && left >= right) {
				index += - 1 + width;
			} else if (right > threshold) {
				index += 1 + width;
			} else {
				bottomRightPoint = new Point(index % width, index / width);
				break;
			}
		}

		//going left
		x = originalX;
		y = originalY;
		while(true) {
			int top = (img[x-1 + (y-2)*width] & 0xFF) - (img[x-1 + (y+0)*width] & 0xFF);
			int mid = (img[x-1 + (y-1)*width] & 0xFF) - (img[x-1 + (y+1)*width] & 0xFF);
			int bot = (img[x-1 + (y-0)*width] & 0xFF) - (img[x-1 + (y+2)*width] & 0xFF);
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
		index = x + y * width;
		while(true) {
			int left = (img[index] & 0xFF) - (img[index - 2] & 0xFF);
			int mid = (img[index + 1] & 0xFF) - (img[index - 1] & 0xFF);
			int right = (img[index + 2] & 0xFF) - (img[index] & 0xFF);
			if (mid < -threshold && mid <= right && mid < left) {
				index += width;
			} else if (left < -threshold && left <= right) {
				index += - 1 + width;
			} else if (right < -threshold) {
				index += 1 + width;
			} else {
				bottomLeftPoint = new Point(index % width, index / width);
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
                sum += (img[x+i + (y+j)*width] & 0xFF) * filter[j + filter[0].length / 2][i + filter.length / 2];
//				Printer.po(i+","+j+"*" + filter[i + filter.length / 2][j + filter[0].length / 2]+"  ");
            }
//			Printer.p("\n");
        }
        return sum;
    }
}
