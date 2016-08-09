package com.vvovsvv.touchmine;

import android.opengl.Matrix;

public class UIElementGameEnd extends UIElement {
	private float[] gameEndMatrix = new float[16];
	private final float[] mTMatrix = new float[16]; 
	private float[] tempMatrix = new float[16];
	public float[] scaleMatrix = new float[16];
	
	private float[] recordMatrix = new float[16];
	
	float scaleFactor = 0;
	private int secsToRecord=0;
	private boolean isRecord=false;
	
	public UIElementGameEnd(MineFieldRenderer renderer) {
		super();
		
		scaleFactor = renderer.scaleFactor*5f;
		Matrix.setIdentityM(scaleMatrix, 0);
		Matrix.scaleM(scaleMatrix, 0, scaleFactor, scaleFactor, scaleFactor);
	}

	@Override
	public void updatePosition(MineFieldRenderer renderer) {
		float startx=renderer.lookAt.lookAtX;
		float starty=renderer.lookAt.lookAtY;
		
		modelRect.set(startx - scaleFactor/2f, 
				starty - scaleFactor/4f,
				startx + scaleFactor/2f,
				starty + scaleFactor/4f
				);
		
		Matrix.setIdentityM(mTMatrix, 0);
		Matrix.translateM(mTMatrix, 0, modelRect.left, modelRect.top, 0f);
		
		Matrix.multiplyMM(tempMatrix, 0, mTMatrix, 0, scaleMatrix, 0);
		Matrix.multiplyMM(gameEndMatrix, 0, renderer.mMVPMatrix, 0, tempMatrix, 0);
		
		Matrix.translateM(recordMatrix, 0, gameEndMatrix, 0, 0f, -scaleFactor, 0f);
		//Matrix.rotateM(recordMatrix, 0, 15f, 0f, 0f, 1f);
	}

	@Override
	public void draw(MineFieldRenderer renderer) {
		
		MineField mineField = renderer.getMineField();
		if(mineField.isGameOver())
		{
			if(mineField.isWin())
			{
				renderer.tiles.drawRect(gameEndMatrix, 0f, 0f, renderer.spriteSheets, renderer.gameEndSpriteSheet);
				if(isRecord)
				{
					renderer.tiles.drawRect(recordMatrix, 0f, 0f, renderer.spriteSheets, renderer.recordSpriteSheet);
					String elapsed_time = String.valueOf(-secsToRecord);
					renderer.__draw_string(modelRect.left+scaleFactor/2f-elapsed_time.length()*renderer.infoScaleFactor/2f,
							modelRect.bottom-scaleFactor+renderer.infoScaleFactor/2f, elapsed_time, true);
				}
				else
				{
					renderer.tiles.drawRect(recordMatrix, 0f, 0.5f, renderer.spriteSheets, renderer.recordSpriteSheet);
					String elapsed_time = String.valueOf(secsToRecord);
					renderer.__draw_string(modelRect.left+scaleFactor/2f-elapsed_time.length()*renderer.infoScaleFactor/2f,
							modelRect.bottom-scaleFactor+renderer.infoScaleFactor/2f, elapsed_time, true);
				}
			}
			else
			{
				renderer.tiles.drawRect(gameEndMatrix, 0f, 0.5f, renderer.spriteSheets, renderer.gameEndSpriteSheet);
			}
		}
	}

	public int getSecsToRecord() {
		return secsToRecord;
	}

	public void setSecsToRecord(int secsToRecord) {
		this.secsToRecord = secsToRecord;
	}

	public boolean getIsRecord() {
		return isRecord;
	}

	public void setIsRecord(boolean isRecord) {
		this.isRecord = isRecord;
	}

}
