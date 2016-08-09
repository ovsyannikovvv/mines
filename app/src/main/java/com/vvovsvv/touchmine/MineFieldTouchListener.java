package com.vvovsvv.touchmine;

import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;

public class MineFieldTouchListener extends SimpleOnGestureListener implements OnTouchListener, OnScaleGestureListener, OnDragListener {
	private MineFieldView view;
	private ScaleGestureDetector scaleDetector;
	private GestureDetector gestureDetector;
	private float initial_span=0;

	public MineFieldTouchListener(MineFieldView field_view) {
		view = field_view;
		scaleDetector = new ScaleGestureDetector(view.getContext(),this);
		gestureDetector = new GestureDetector(view.getContext(),this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		scaleDetector.onTouchEvent(event);
		if(!scaleDetector.isInProgress())
		{
			gestureDetector.onTouchEvent(event);
		}
		
		return true;
		/*if (event != null)
		{
			final float x1 = event.getX();
			final float y1 = event.getY();
			
			if(event.getActionMasked()==MotionEvent.ACTION_MOVE)
			{
				view.queueEvent(
							new Runnable()
							{
								@Override
								public void run()
								{
									view.onActionMove(x1, y1);
								}
							}
				);
			}
			else if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
			{
				view.queueEvent(
							new Runnable()
							{
								@Override
								public void run()
								{
									view.onActionDown(x1, y1);
								}
							}
				);
			}
			else if (event.getActionMasked() == MotionEvent.ACTION_UP)
			{
					view.queueEvent(
						new Runnable()
						{
							@Override
							public void run()
							{
								view.ScreenTap(x1,y1);
							}
						}
						);
			}
			return true;
		}
		return false;*/
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		final float scale = detector.getCurrentSpan()/initial_span;
		view.queueEvent(
				new Runnable()
				{
					@Override
					public void run()
					{
						view.onScaleMove(scale);
					}
				}
    			);
		
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		initial_span = detector.getCurrentSpan();
		view.queueEvent(
				new Runnable()
				{
					@Override
					public void run()
					{
						view.onScaleDown();
					}
				}
    			);
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLongPress(MotionEvent e) {
		DragShadowBuilder shadowBuilder = new DragShadowBuilder(view);  
		view.startDrag(null, shadowBuilder, null, 0);
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		final float x1 = e.getX();
		final float y1 = e.getY();
		view.queueEvent(
				new Runnable()
				{
					@Override
					public void run()
					{
						view.ScreenTap(x1,y1);
					}
				}
				);
		return true;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		final int action = event.getAction();
		final float x1 = event.getX();
		final float y1 = event.getY();
		
		switch(action) {
        case DragEvent.ACTION_DRAG_STARTED:
        	view.queueEvent(
					new Runnable()
					{
						@Override
						public void run()
						{
							view.onActionDown(x1, y1);
						}
					}
        			);
        	break;
        
        case DragEvent.ACTION_DRAG_LOCATION:
        case DragEvent.ACTION_DROP:
        	view.queueEvent(
					new Runnable()
					{
						@Override
						public void run()
						{
							view.onActionMove(x1, y1);
						}
					}
        			);
        	break;
		}
		return true;
	}
	
}
