package com.nvew.clockread2;

import java.util.Comparator;

import org.opencv.core.Point;

import android.util.Log;

public class EndPoint implements Comparable<EndPoint>{
	
	public Point self, other;
	boolean isLeft;
	
	public EndPoint(Point a, Point b, boolean left)
	{
		self = a;
		other = b;
		isLeft = left;
	}
	
	public static EndPoint[] get_endpoints(Point a, Point b)
	{
		EndPoint[] pts = new EndPoint[2];
		
		boolean a_isLeft = a.x < b.x;
		
		pts[0] = new EndPoint(a,b,a_isLeft);
		pts[1] = new EndPoint(b,a,!a_isLeft);
		
		return pts;
	}
	
	public int compareTo(EndPoint compareEndPoint)
	{
		int compareQuantity = (int)((EndPoint) compareEndPoint).self.y;
		
		return (int)this.self.y - compareQuantity;
	}
	
	static class YComparator implements Comparator<EndPoint>
    {
    	public int compare(EndPoint p1, EndPoint p2)
    	{
    		double y1 = p1.self.y;
    		double y2 = p2.self.y;

    		if (y1 == y2)
    			return 0;
    		else if (y1 > y2)
    			return 1;
    		else
    			return -1;
    	}
    }
	
	public static void test_EndPoint()
	{
		
		Point a = new Point(1,1);
		Point b = new Point(5,5);
		
		EndPoint[] pts = get_endpoints(a,b);
		
		Log.v("Debug", "Point a: " + Double.toString(pts[0].self.x) + " Point b: "
		+ Double.toString(pts[1].self.x) + " Point b is left: " + Boolean.toString(pts[1].isLeft));
	}

}
