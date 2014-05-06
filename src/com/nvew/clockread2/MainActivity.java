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
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;
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

public class MainActivity extends Activity  implements OnTouchListener, CvCameraViewListener2 {
	
	private static final boolean VERBOSE = true;
	
	final private String TAG = "Debug";
	private CameraBridgeViewBase mOpenCvCameraView;
	int frame_count = 0;
	private Mat cur_img;
    private Mat mRgba_global;
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {
	     Log.i(TAG, "called onCreate");
	     super.onCreate(savedInstanceState);
	     getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	     setContentView(R.layout.activity_main);
	     
	     
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
                   mOpenCvCameraView.setOnTouchListener(MainActivity.this);
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
   
   public boolean onTouch(View v, MotionEvent event) {
       int cols = mRgba_global.cols();
       int rows = mRgba_global.rows();

       int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
       int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

       int x = (int)event.getX() - xOffset;
       int y = (int)event.getY() - yOffset;

       Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

       if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

       return false; // don't need subsequent touch events
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
	   double minLineLength = 150;
	   double maxLineGap = 25;
	   
	   Imgproc.HoughLinesP(src, lines, rho, theta, threshold, minLineLength, maxLineGap);
	   
	   for (int x = 0; x < lines.cols(); x++) 
	    {
	          double[] vec = lines.get(0, x);
	          double x1 = vec[0], 
	                 y1 = vec[1],
	                 x2 = vec[2],
	                 y2 = vec[3];
	          Point start = new Point(x1, y1);
	          Point end = new Point(x2, y2);

	          Core.line(mRgba, start, end, new Scalar(255,0,0), 3);

	    }
	   
	   return mRgba;
   }
   
   
   public Mat overlay_target(Mat mRgba){
	   
	   if (VERBOSE) Log.v(TAG, "+++ Pixels = " + Long.toString(mRgba.total()) + " +++");
	   if (VERBOSE) Log.v(TAG, "+++ Channels = " + Integer.toString(mRgba.channels()) + " +++");
	   
	   int rows = mRgba.rows();
	   int cols = mRgba.cols();
	   int channels = mRgba.channels();
	   
	   int row_start = rows / 3;
	   int row_end = rows * 2 / 3;
	   
	   int col_start = (cols - rows ) / 2;
	   int col_end = col_start + rows;
	   
	   //Core.rectangle(mRgba, new Point(col_start, 0), new Point(col_end, rows - 1), new Scalar(255,0,0,100), 20);
	   Core.circle(mRgba, new Point(cols/2, rows/2), rows/2, new Scalar(255,0,0,100), 20);
	   Core.line(mRgba, new Point(cols/2,0), new Point(cols/2, rows/10), new Scalar(255,0,0,100), 15);
	   return mRgba;
	   
   }
   
   public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	   if (frame_count == 0) {
		   mRgba_global = inputFrame.rgba();
		   cur_img = new Mat(inputFrame.gray().size(), inputFrame.gray().type());
	   }
	   
	   //if (frame_count != 0 && frame_count % 25 == 0) {
	   //cur_img = edge_detect(inputFrame.gray());
	   //}
	   //frame_count++;
	   
	   //cur_img = lines_detect(edge_detect(inputFrame.gray()), inputFrame.rgba());
	   
	   cur_img = overlay_target(inputFrame.rgba());
	   
	   return cur_img;
   }
}