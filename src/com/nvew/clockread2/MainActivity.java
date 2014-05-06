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
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity  implements CvCameraViewListener2 {
	
	final private String TAG = "Debug";
	private CameraBridgeViewBase mOpenCvCameraView;
	int frame_count = 0;
	private Mat cur_img;
	
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
	   
	   double lowThreshold = 100;
	   int ratio = 3;
	   Size sz = new Size(3,3);
	   
	   //dst.create( src.size(), src.type());
	   Imgproc.blur(src, detected_edges, sz);  
	   Imgproc.Canny(src, detected_edges, lowThreshold, lowThreshold*ratio);
	   //src.copyTo(dst, detected_edges);
	   
	   return detected_edges;
   }  
   
   public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	   if (frame_count == 0) {
		   cur_img = new Mat(inputFrame.gray().size(), inputFrame.gray().type());
	   }
	   
	   //implement edge canny edge detection on src
	   
	   //if (frame_count != 0 && frame_count % 25 == 0) {
		//   Log.i(TAG, "wouldve edge detected");
		   
		   cur_img = edge_detect(inputFrame.gray());
	   //}
	   	   
	   //return inputFrame.rgba();
	   
	   frame_count++;
	   
	   return cur_img;
   }
}