package com.nvew.clockread2;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity  implements CvCameraViewListener2 {
	
	final private String TAG = "Debug";
	private CameraBridgeViewBase mOpenCvCameraView;
	int frame_count = 0;
	private Mat cur_img;
	
	TextView timeTextView;
	
	List<double[]> hands = new ArrayList<double[]>();
	
	//double[][] hands = new double[5][4]; 
		//represents start and end coordinates for 3 clock hands
		//hands[0] = hour hand, hands[1] = minute hand, hands[2] = second hand
		//hands[0][0-3] are x1, y1, x2, y2 coordinates of hour hand
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {
	     Log.i(TAG, "called onCreate");
	     super.onCreate(savedInstanceState);
	     getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	     setContentView(R.layout.activity_main);
	     
	     timeTextView = (TextView) findViewById(R.id.timeTextView);
	     
	     timeTextView.setText("test");
	     
	     
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
	   int threshold = 50;
	   double minLineLength = 100;
	   double maxLineGap = 35;
	   
	   Imgproc.HoughLinesP(src, lines, rho, theta, threshold, minLineLength, maxLineGap);

	   
	   for (int i = 0; i < lines.cols(); i++){
		   double[] vec1 = lines.get(0, i);
		   if (i == 0)
			   hands.add(vec1);
		   
		   if (p_distance(vec1[0], vec1[1], src.width()/2, src.height()/2) < 100 || 
				   p_distance(vec1[2], vec1[3], src.width()/2, src.height()/2) < 100) {
			   
			   
			   /*for (int j = 0; j < hands.size(); j++) {
				   if (compare_lines(hands.get(j), vec1) == 1) {
					   double[] avg_vec = new double[4];
					   avg_vec[0] = (vec1[0] + hands.get(j)[0])/2;
					   avg_vec[1] = (vec1[1] + hands.get(j)[1])/2;
					   avg_vec[2] = (vec1[2] + hands.get(j)[2])/2;
					   avg_vec[3] = (vec1[3] + hands.get(j)[3])/2;
					   hands.set(j, avg_vec);
					   
					   
					   
				   } else {
					   
				   }
			   }*/
			   
			   //hands.add(vec1);
			   
		   }
		   
		   for (double[] temp : hands) {
			   Point start = new Point(temp[0], temp[1]);
			   Point end = new Point (temp[2], temp[3]);
			   Core.line(mRgba, start, end, new Scalar(0, 255, 0), 3);
		   }
	   }
	   hands.clear();

	   return mRgba;
   }
   
   public double p_distance (double x1, double y1, double x2, double y2) {
	   return( Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
   }
   
   public int compare_lines(double[] line1, double[] line2) {
	   if (((p_distance(line1[0], line1[1], line2[0], line2[1]) < 50 &&
		     p_distance(line1[2], line1[3], line2[2], line2[3]) < 50)||
		     p_distance(line1[2], line1[3], line2[0], line2[1]) < 50 &&
		     p_distance(line1[0], line1[1], line2[2], line2[3]) < 50) )
		   return 1;
	   else
		   return 0;
	   
	   //0 -> lines are not similar
	   //1 -> lines are similar
   }
   
   public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	   if (frame_count == 0) {
		   cur_img = new Mat(inputFrame.gray().size(), inputFrame.gray().type());
	   }
	   
	   //if (frame_count != 0 && frame_count % 25 == 0) {
	   //cur_img = edge_detect(inputFrame.gray());
	   //}
	   //frame_count++;
	   
	   cur_img = lines_detect(edge_detect(inputFrame.gray()), inputFrame.rgba());
	   
	   return cur_img;
   }
}