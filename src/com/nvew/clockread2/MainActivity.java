package com.nvew.clockread2;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity  implements CvCameraViewListener2 {
	
	final private String TAG = "Debug";
	public final static String X_Y_COORS = "com.nvew.clockread2.x_y_coors";
	public final static String RGB_FRAME = "com.nvew.clockread2.rgb_frame";
	private CameraBridgeViewBase mOpenCvCameraView;
	int frame_count = 0;
	private Mat cur_img;
    public static Mat mRgba;
    public static int overlay_left, overlay_top, overlay_radius, overlay_diameter, overlay_vert_center, overlay_horiz_center;
    public static int frame_rows, frame_cols;
    public static TextView timeTextView;
    public static int minutes, hours;
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {
	     Log.i(TAG, "called onCreate");
	     super.onCreate(savedInstanceState);
	     getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	     setContentView(R.layout.activity_main);
	     
	     timeTextView = (TextView) findViewById(R.id.timeTextView);
	     
	     timeTextView.setText("00:00");
	     
	     
	     mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
	     mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	     mOpenCvCameraView.setCvCameraViewListener(this);
	     
	 }
   
   private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
       @Override
       public void onManagerConnected(int status) {
           switch (status) {
               case LoaderCallbackInterface.SUCCESS:
               {
                   Log.i(TAG, "OpenCV loaded successfully");
                   mOpenCvCameraView.enableView();
               } break;
               default:
               {
                   super.onManagerConnected(status);
               } break;
           }
       }
   };
   
   @Override
   public void onResume()
   {
       super.onResume();
       OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }
   @Override
   public void onPause()
   {
       super.onPause();
       if (mOpenCvCameraView != null)
           mOpenCvCameraView.disableView();
   }

   public void onDestroy() {
       super.onDestroy();
       if (mOpenCvCameraView != null)
           mOpenCvCameraView.disableView();
   }

   public void onCameraViewStarted(int width, int height) {
   }

   public void onCameraViewStopped() {
   }
   
   public Mat edge_detect(Mat src) {
	   Mat detected_edges = new Mat(src.size(), src.type());
	   
	   double lowThreshold = 50;
	   int ratio = 5;
	   Size sz = new Size(3,3);
	   
	   Imgproc.blur(src, detected_edges, sz);  
	   Imgproc.Canny(src, detected_edges, lowThreshold, lowThreshold*ratio);
	   
	   return detected_edges;
   }
   
   public Mat lines_detect(Mat src, Mat mRgba) {
	   Mat lines = new Mat();
	   double rho = 1;
	   double theta = Math.PI/180;
	   int threshold = 85;
	   double minLineLength = 100;
	   double maxLineGap = 10;
	   
	   Imgproc.HoughLinesP(src, lines, rho, theta, threshold, minLineLength, maxLineGap);
	   
	   Point[] midpt_arr = new Point[lines.cols()]; //store valid midpoints
	   Point center = new Point(overlay_horiz_center, overlay_vert_center);
	   int dist_threshold = 100;
	   
	   Core.circle(mRgba, center, 5, new Scalar(255,255,0), 10);
	   
	   int cnt = 0; // count valid midpoints
	   
	   for (int x = 0; x < lines.cols(); x++) 
	    {
	          double[] vec = lines.get(0, x);
	          double x1 = vec[0], 
	                 y1 = vec[1],
	                 x2 = vec[2],
	                 y2 = vec[3];
	          Point start = new Point(x1, y1);
	          Point end = new Point(x2, y2);
	          
	          //check if line is close to center
	          
	          if(points_are_close(start,center, dist_threshold) || points_are_close(end,center,dist_threshold))
	          {
	        	  Point midpt = new Point((x1 + x2)/2,(y1 + y2)/2);
	              Core.circle(mRgba, midpt, 10, new Scalar(255,0,0), 3);
	              midpt_arr[cnt] = midpt;
	              cnt++;
	          }
	          //Core.line(mRgba, start, end, new Scalar(255,0,0), 3); // Draw detected lines

	    }
	   
	   if( cnt >= 2)
	   {
	   
		   //kmeans
		   Mat midpts = new Mat(cnt, 2, CvType.CV_32F);
		   
		   for(int i = 0; i < cnt; i++)
		   {
			   midpts.put(i, 0, midpt_arr[i].x);
			   midpts.put(i, 1, midpt_arr[i].y);
		   }
		   
		   
		   int K = 2; //# of clusters
		   
		   //parameters for kmeans
		   
		   Mat bestLabels = new Mat(cnt, 2, CvType.CV_32F);
		   Mat centers = new Mat(K, 2, CvType.CV_32F);
		   TermCriteria criteria = new TermCriteria( TermCriteria.EPS+TermCriteria.MAX_ITER, 10, 1.0);
		   int attempts = 3;
		   int flags = Core.KMEANS_PP_CENTERS;
		   
		   //find 2 clusters from midpoints
		   
		   Core.kmeans(midpts, K, bestLabels, criteria, attempts, flags, centers);
		   
		   //create points from cluster centers set in kmeans function
		   
		   Point cluster_center_1 = new Point(centers.get(0,0)[0], centers.get(0,1)[0]);
		   Point cluster_center_2 = new Point(centers.get(1,0)[0], centers.get(1,1)[0]);
		   
		   //draw green circles around cluster centers
		   
		   Core.circle(mRgba, cluster_center_1, 10, new Scalar(0,255,0), 3);
		   Core.circle(mRgba, cluster_center_2, 10, new Scalar(0,255,0), 3);
		   
		   Point clock_center = center;
		   Point min_midpt, hour_midpt;
		   
		   
		   //midpoint further from center indicated longer line indicates minute hand
		   
		   if(pixel_dist(cluster_center_1, clock_center) > pixel_dist(cluster_center_2, clock_center))
		   {
			   min_midpt = cluster_center_1;
			   hour_midpt = cluster_center_2;
		   }
		   else
		   {
			   min_midpt = cluster_center_2;
			   hour_midpt = cluster_center_1;
		   }
		   
		   minutes = calculate_minutes(min_midpt.x - clock_center.x, clock_center.y- min_midpt.y);
		   hours = calculate_hours(hour_midpt.x - clock_center.x,clock_center.y - hour_midpt.y);
		   
		   runOnUiThread(new Runnable() {

			    public void run() {
			    	timeTextView.setText(Integer.toString(hours) + ":" + Integer.toString(minutes));
			    }
			}); 
	   }
	   
	   return mRgba;
   }
   
   public boolean points_are_close(Point a, Point b, int max_dist)
   {
	   int dist = (int)Math.sqrt(Math.pow((a.x - b.x),2) + Math.pow((a.y - b.y),2));
	   
	   return dist <= max_dist;
   }
   
   
   public Mat overlay_target(Mat mRgba){
	   
	   int rows = mRgba.rows();
	   int cols = mRgba.cols();
	   
	   Core.circle(mRgba, new Point(cols/2, rows/2), rows/2, new Scalar(255,0,0,100), 20);
	   Core.line(mRgba, new Point(cols/2,0), new Point(cols/2, rows/10), new Scalar(255,0,0,100), 15);
	   
	   return mRgba;
	   
   }
   
   public double pixel_dist(Point a, Point b)
   {
	   return Math.sqrt(Math.pow((a.x - b.x),2) + Math.pow((a.y - b.y),2));
   }
   
   public int calculate_minutes(double x, double y)
	{
		//Multiply clockwise percent rotation by 60 and floor the final result
		
		return (int)Math.floor(calculate_hand_rotation(x,y) * 60); 
	}
	
	public int calculate_hours(double x, double y)
	{
		//Multiply clockwise percent rotation by 12 and floor the final result
		
		int hour = (int)Math.floor(calculate_hand_rotation(x,y) * 12); 
		
		if( hour == 0)
		{
			hour = 12;
		}
		
		return hour;
	}
	
	public double calculate_hand_rotation(double x, double y)
	{
		double angle = Math.atan(y/x);
		
		if(x < 0)
		{
			angle += Math.PI; // convert angles in 2nd and 3rd quadrants to 0 -> 360 range
		}
		else if(y < 0)
		{
			angle += 2 * Math.PI; // convert angles in 4th quadrant to 0 -> 360 range
		}
		
		Log.v(TAG, "X: " + Double.toString(x) + ", Y: " + Double.toString(y));
		Log.v(TAG, "Arctan Angle: " + Double.toString(angle*180.0/( 2.0 * Math.PI)));
		Log.v(TAG, "Arctan2 Angle: " + Double.toString(Math.atan2(y,x)*180/( 2 * Math.PI)));
		
		// convert angles to percentages, then shift to a clockwise range from 0->1,
		
		return (1.25  -  (angle / (2 * Math.PI))  ) % 1; 
	}

   
   
   
   public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	   mRgba = inputFrame.rgba();
	   frame_rows = mRgba.rows();
	   frame_cols = mRgba.cols();
	   cur_img = new Mat(inputFrame.gray().size(), inputFrame.gray().type());
	   
	   int rows = mRgba.rows();
	   int cols = mRgba.cols();
	   
	   overlay_left = (cols - rows ) / 2;
	   overlay_top = 0;
	   overlay_radius = rows/2;
	   overlay_diameter = overlay_radius * 2;
	   overlay_vert_center = rows / 2;
	   overlay_horiz_center = cols / 2;
	   
	   cur_img = lines_detect(edge_detect(inputFrame.gray()), inputFrame.rgba());
	   
	   return cur_img;
   }
}