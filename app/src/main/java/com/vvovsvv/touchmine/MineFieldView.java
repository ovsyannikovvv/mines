package com.vvovsvv.touchmine;

import android.content.Context;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class MineFieldView extends GLSurfaceView {
	private MineField mineField; 
	private MineFieldRenderer mineFieldRenderer;
	private DisplayMetrics devMetrics;
	private AudioManager audioMgr;
	
	public MineFieldView(Context context, Bundle savedState, DisplayMetrics deviceMetrics) 
	{
		super(context);
		Initialize(context, deviceMetrics);
		mineField.Load(savedState);
		mineFieldRenderer.Load(savedState);
	}

	public MineFieldView(Context context, int x, int y, int mines, DisplayMetrics deviceMetrics) 
	{
		super(context);
		Initialize(context, deviceMetrics);
		mineField.ConstructNewField(x, y, mines);
	}

	public void Initialize(Context context, DisplayMetrics deviceMetrics)
	{
		setEGLContextClientVersion(2);
		
		audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		devMetrics = deviceMetrics;
		
		mineField = new MineField();
		mineFieldRenderer = new MineFieldRenderer(context, mineField, devMetrics);
		setRenderer(mineFieldRenderer);
	}
	
	public void ScreenTap(float x, float y)
	{
		boolean needSound = mineFieldRenderer.ScreenTap(x, y);
		if(needSound)
		{
			audioMgr.playSoundEffect(AudioManager.FX_KEY_CLICK);
		}
		
	} 
	
	public void onActionDown(float x, float y)
	{
		mineFieldRenderer.onActionDown(x,y);
	}
	
	public void onActionMove(float x, float y)
	{
		mineFieldRenderer.onActionMove(x, y);
	}
	
	public void onScaleDown()
	{
		mineFieldRenderer.onScaleDown();
	}
	
	public void onScaleMove(float scale)
	{
		mineFieldRenderer.onScaleMove(scale);
	}

	public void Save(Bundle state)
	{
		mineField.Save(state);
		mineFieldRenderer.Save(state);
	}
	
}

