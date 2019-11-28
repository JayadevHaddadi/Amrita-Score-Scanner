package edu.amrita.aerl.scorereader.core.anchor.yuv;

import edu.amrita.aerl.scorereader.core.support.Marker;

public class AnchorExtractorYUV {

	private final int width;
	private final int height;
	private final MarkerFinderYUV mf;
	private final int threshold;
	private double widthHeightRatio;

	public AnchorExtractorYUV(int width, int height) {
		this.width = width;
		this.height = height;
		widthHeightRatio = width / (double) height;
		threshold = 40; //30
		mf = new MarkerFinderYUV(width, threshold); // -10
	}

	public Marker extract(byte[] img) {
		int halfWidth = width/2;

		int topContrast = 0;
		int thisContrast = 0;
		int bottomContrast;

		for (int y = 50; y < height-50; y++) {
			bottomContrast = (img[halfWidth + y*width] & 0xFF)
					- (img[halfWidth + (y+2)*width] & 0xFF);

			if (thisContrast > threshold && thisContrast >= bottomContrast
					&& thisContrast > topContrast) {

				try{
					Marker marker = mf.followLine(img, halfWidth, y);
					if (correctMarker(marker))
						return marker;
				} catch(Exception e){
					System.out.println("Failed with line...");
					e.printStackTrace();
				}
			}
			topContrast = thisContrast;
			thisContrast = bottomContrast;
		}

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
