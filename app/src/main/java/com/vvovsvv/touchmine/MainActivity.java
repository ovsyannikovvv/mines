package com.vvovsvv.touchmine;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.vvovsvv.touchmine.MineFieldView;

public class MainActivity extends Activity {
	private MineFieldView field_view;
	MineFieldTouchListener touchListener;
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		field_view.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		field_view.Save(outState);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		field_view.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DisplayMetrics outMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		
		if(savedInstanceState == null)
		{
			Bundle data = this.getIntent().getExtras();
			int x = data.getInt("x", 2);
			int y = data.getInt("y", 2);
			int mines = data.getInt("mines", 1);
			field_view = new MineFieldView(this, x,y,mines,outMetrics);
		}
		else
		{
			field_view = new MineFieldView(this, savedInstanceState, outMetrics);
		}
		touchListener = new MineFieldTouchListener(field_view);
		field_view.setOnTouchListener(touchListener);
		field_view.setOnDragListener(touchListener);
		setContentView(field_view);
	}
}
