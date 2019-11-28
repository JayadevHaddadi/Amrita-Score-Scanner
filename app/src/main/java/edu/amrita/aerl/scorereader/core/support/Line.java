package edu.amrita.aerl.scorereader.core.support;

public class Line {
	/**
	 *
	 */
	public int startX, startY, endX, endY;
	private double realLength = -1;
	public static int marginFromLine = 3;
	private int epsilon = 1; // added to denominator to avoid division by zero
//	private Point leftPoint;
//	private Point rightPoint;

	public Line(int sX, int sY, int eX, int eY) {
		startX = sX;
		startY = sY;
		endX = eX;
		endY = eY;
	}

	public Line(Point left, Point right) {
		startX = left.x;
		startY = left.y;
		endX = right.x;
		endY = right.y;
//		this.rightPoint = right;
	}

	public double getRealLength() {
		if (realLength == -1) {
			realLength = Math.hypot(endX - startX, endY - startY);
			return realLength;
		} else
			return realLength;
	}

	public boolean contains(int x, int y) {
		return lineCalculation(x,y);
	}

	public boolean crossProduct(int x, int y) {
		int dxc = x - startX;
		int dyc = y - startY;
		int dxl = endX - startX;
		int dyl = endY - startY;
		int cross = dxc * dyl - dyc * dxl;
		if (cross > -marginFromLine && cross < marginFromLine) {
			if (Math.abs(dxl) >= Math.abs(dyl))
				return dxl > 0 ? startX <= x && x <= endX : endX <= x
						&& x <= startX;
			else
				return dyl > 0 ? startY <= y && y <= endY : endY <= y
						&& y <= startY;
		}
		return false;
	}

	public boolean hypothemusLength(int x, int y) {
		if (x < endX + marginFromLine && x > startX - marginFromLine) {
			int startToDotLength = (int) Math.sqrt(Math.pow(x - startX, y
					- startY));
			int endToDotLength = (int) Math.sqrt(Math.pow(x - endX, y - endY));
			if (startToDotLength + endToDotLength - marginFromLine > getRealLength()
					&& startToDotLength + endToDotLength + marginFromLine < getRealLength()) {
				return true;
			}
		}
		return false;
	}

	public boolean lineCalculation(int x, int y) {
		if (x < endX + marginFromLine && x > startX - marginFromLine) {
			int lineY = (startY + (endY - startY) * (x - startX) / (endX - startX) );
			if (y < lineY + marginFromLine && y > lineY - marginFromLine) {
				return true;
			}
		}
		return false;
	}
}
