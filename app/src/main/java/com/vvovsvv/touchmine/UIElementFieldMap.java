package com.vvovsvv.touchmine;

import android.opengl.Matrix;

public class UIElementFieldMap extends UIElement {
	
	private final float[] mTMatrix = new float[16];
	private float[] curTileMatrix = new float[16];

	private float[] fillColor = new float[3];
	private float[] emptyColor = new float[3];
	private float[] lookFillColor = new float[3];
	private float[] lookEmptyColor = new float[3];
	
	private float[] baseLookColor = new float[3];
	
	float fieldMapScaleFactor = 0;
	float fieldLookScaleFactor = 0;
	public float[] fieldMapScaleMatrix = new float[16];
	
	public UIElementFieldMap(MineFieldRenderer renderer) {
		super();
		
		baseLookColor[0] = 0.1f;
		baseLookColor[1] = 0.1f;
		baseLookColor[2] = 0.1f;
		
		fillColor[0] = 0.65f;
		fillColor[1] = 0.65f;
		fillColor[2] = 0.65f;
		
		lookFillColor[0] = fillColor[0]+baseLookColor[0];
		lookFillColor[1] = fillColor[1]+baseLookColor[1];
		lookFillColor[2] = fillColor[2]+baseLookColor[2];

		emptyColor[0] = 0.9f;
		emptyColor[1] = 0.9f;
		emptyColor[2] = 0.9f;
		
		lookEmptyColor[0] = emptyColor[0]+baseLookColor[0];
		lookEmptyColor[1] = emptyColor[1]+baseLookColor[1];
		lookEmptyColor[2] = emptyColor[2]+baseLookColor[2];
		
		fieldLookScaleFactor = 0.1f;
		fieldMapScaleFactor = renderer.scaleFactor*fieldLookScaleFactor;
		
		Matrix.setIdentityM(fieldMapScaleMatrix, 0);
		Matrix.scaleM(fieldMapScaleMatrix, 0, fieldMapScaleFactor, fieldMapScaleFactor, fieldMapScaleFactor);
	}

	@Override
	public void updatePosition(MineFieldRenderer renderer) {
		float startx=renderer.lookAt.lookAtX+renderer.width_ratio-renderer.scaleFactor*.5f;
		float starty=renderer.lookAt.lookAtY-(renderer.height_ratio-renderer.scaleFactor*0.5f);
		
		modelRect.set(startx - fieldMapScaleFactor*renderer.getMineField().getSize_x(), 
				starty, 
				startx,
				starty + fieldMapScaleFactor*renderer.getMineField().getSize_y()
				);
		
	}

	@Override
	public void draw(MineFieldRenderer renderer) {
		float tileLocY = 0f;
		float tileLocX = 0f;
		
		float startx = modelRect.left;
		float starty = modelRect.top;
		
		MineField mineField = renderer.getMineField();
		int xsize = mineField.getSize_x();
		int ysize = mineField.getSize_y();
		
		boolean visible = false;
		float rend_xpos = 0;
		float rend_ypos = 0;
		for(int x=0; x<xsize; x++)
		{
			for(int y=0; y<ysize; y++)
			{
				Matrix.setIdentityM(mTMatrix, 0);
				Matrix.translateM(mTMatrix, 0, tileLocX+startx, tileLocY+starty, 0f);
				
				Matrix.multiplyMM(curTileMatrix, 0, mTMatrix, 0, fieldMapScaleMatrix, 0);
				Matrix.multiplyMM(curTileMatrix, 0, renderer.mMVPMatrix, 0, curTileMatrix, 0);
				
				rend_xpos = x*renderer.scaleFactor;
				rend_ypos = y*renderer.scaleFactor;
				
				visible = (rend_xpos < (renderer.lookAt.lookAtX+renderer.width_ratio) &&
					rend_xpos> (renderer.lookAt.lookAtX-renderer.width_ratio) &&
					rend_ypos< (renderer.lookAt.lookAtY+renderer.height_ratio) &&
					rend_ypos> (renderer.lookAt.lookAtY-renderer.height_ratio)
					) ? true : false;
				
				Field fieldState = mineField.getField(x, y);
				if(fieldState.ui_state == Field.FieldUIState.Closed)
				{
					if(visible)
					{
						renderer.tiles.drawSimpleColor(curTileMatrix, lookFillColor, true);
					}
					else
					{
						renderer.tiles.drawSimpleColor(curTileMatrix, fillColor, true);
					}
					
				}
				else
				{
					if(visible)
					{
						renderer.tiles.drawSimpleColor(curTileMatrix, lookEmptyColor, false);
					}
					else
					{
						renderer.tiles.drawSimpleColor(curTileMatrix, emptyColor, false);
					}
				}
				tileLocY += fieldMapScaleFactor;
			}
			tileLocY = 0f;
			tileLocX += fieldMapScaleFactor;
		}
	}

}
