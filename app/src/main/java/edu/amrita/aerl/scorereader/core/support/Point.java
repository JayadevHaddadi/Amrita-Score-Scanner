package edu.amrita.aerl.scorereader.core.support;

public class Point {
	public int x,y;
	public Point(int x, int y){
		this.x=x;
		this.y=y;
	}
	public double distance(Point ref) {
		return Math.hypot(x-ref.x, y-ref.y);
	}
}
