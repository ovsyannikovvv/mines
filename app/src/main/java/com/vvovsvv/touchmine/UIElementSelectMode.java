package com.vvovsvv.touchmine;

import android.opengl.Matrix;

public class UIElementSelectMode extends UIElement {
	private final float[] mTMatrix = new float[16]; 
	private float[] tempMatrix = new float[16];
	private float[] ui_selectModeMatrix = new float[16];
	
	public UIElementSelectMode() {
		super();
	}

	@Override
	public void updatePosition(MineFieldRenderer renderer) {
		float startx=renderer.lookAt.lookAtX-renderer.width_ratio+renderer.scaleFactor*.5f;
		float starty=renderer.lookAt.lookAtY-renderer.height_ratio+renderer.scaleFactor*.5f;
		
		modelRect.set(startx, 
				starty,
				startx + renderer.scaleFactor,
				starty + renderer.scaleFactor
				);
		
		Matrix.setIdentityM(mTMatrix, 0);
		Matrix.translateM(mTMatrix, 0, startx, starty, 0);
		
		Matrix.multiplyMM(tempMatrix, 0, mTMatrix, 0, renderer.scaleMatrix, 0);
		Matrix.multiplyMM(ui_selectModeMatrix, 0, renderer.mMVPMatrix, 0, tempMatrix, 0);
	}

	@Override
	public void draw(MineFieldRenderer renderer) {
		if(renderer.getMineField().OpenMode())
		{
			renderer.tiles.draw(ui_selectModeMatrix, 0.75f, 0.5f, renderer.spriteSheets, renderer.fieldSpriteSheet);
		}
		else
		{
			renderer.tiles.draw(ui_selectModeMatrix, 0.25f, 0.5f, renderer.spriteSheets, renderer.fieldSpriteSheet);
		}
	}
}
