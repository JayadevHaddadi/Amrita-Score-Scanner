package edu.amrita.aerl.scorereader.core.support;

public class Marker {
	public Point[] fourCorners;
	public Point topLeft,topRight,bottomRight,bottomLeft;

	public Marker(Point topLeftPoint, Point topRightPoint,
			Point bottomRightPoint, Point bottomLeftPoint) {
		fourCorners = new Point[4];
		fourCorners[0] = this.topLeft = topLeftPoint;
		fourCorners[1] = this.topRight = topRightPoint;
		fourCorners[2] = this.bottomRight = bottomRightPoint;
		fourCorners[3] = this.bottomLeft = bottomLeftPoint;
	}

	public boolean contains(int x, int y) {
		int margin = 5;
		int topY = (topLeft.y < topRight.y ? topLeft.y
				: topRight.y) - margin;
		int bottomY = (bottomLeft.y > bottomRight.y ? bottomLeft.y
				: bottomRight.y) + margin;
		int leftX = (topLeft.x < bottomLeft.x ? topLeft.x
				:  bottomLeft.x ) - margin;
		int rightX = (topRight.x > bottomRight.x ? topRight.x
				: bottomRight.x) + margin;

		return x > leftX && x < rightX && y > topY && y < bottomY;
	}

	public int getMaxYLength() {
		int lowest = bottomRight.y>bottomLeft.y?bottomRight.y:bottomLeft.y;
		int highest = topRight.y>topLeft.y?topRight.y:topLeft.y;
		return lowest - highest;
	}

	public double getTopLength() {
		return topLeft.distance(topRight);
	}

    public double getLeftSideLength() {
        return topLeft.distance(bottomLeft);
    }

	public double getRightSideLength() {
		return topRight.distance(bottomRight);
	}
}