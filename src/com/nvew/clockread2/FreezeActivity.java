package com.nvew.clockread2;

import org.opencv.core.Size;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class FreezeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		int[] coors = intent.getIntArrayExtra(MainActivity.X_Y_COORS);
		
		Size mat_size = MainActivity.mRgba_global.size();
		
		int cols = (int)mat_size.width;
		int rows = (int)mat_size.height;
		
		TextView textView = new TextView(this);
	    textView.setTextSize(40);
	    textView.setText("rows: " + Integer.toString(rows) + ", cols: " + Integer.toString(cols));

		
	    setContentView(textView);
		//setContentView(R.layout.activity_freeze);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
/*	
 * 
 * Returns integer representing minutes displayed by the clock
 *	x, y coordinates of a point on the minute hand w/ respect to the center of the clock
 *
 */
	
	public int calculate_minutes(int x, int y)
	{
		//Multiply clockwise percent rotation by 60 and floor the final result
		
		return (int)Math.floor(calculate_hand_rotation(x,y) * 60); 
	}
	
	public int calculate_hours(int x, int y)
	{
		//Multiply clockwise percent rotation by 12 and floor the final result
		
		int hour = (int)Math.floor(calculate_hand_rotation(x,y) * 60); 
		
		if( hour == 0)
		{
			hour = 12;
		}
		
		return hour;
	}
	
	public double calculate_hand_rotation(int x, int y)
	{
		double angle = (int) Math.atan(y/x);
		
		if(x < 0)
		{
			angle += Math.PI; // convert angles in 2nd and 3rd quadrants to 0 -> 360 range
		}
		else if(y < 0)
		{
			angle += 2 * Math.PI; // convert angles in 4th quadrant to 0 -> 360 range
		}
		
		// convert angles to percentages, then shift to a clockwise range from 0->1,
		
		return (1.25  -  (angle / (2 * Math.PI))  ) % 1; 
	}

}
