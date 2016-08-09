package com.vvovsvv.touchmine;

import android.opengl.Matrix;

public class UIElementNewGame extends UIElement {
	private final float[] mTMatrix = new float[16]; 
	private float[] tempMatrix = new float[16];
	private float[] newGameMatrix = new float[16];

	public UIElementNewGame() {
		super();
	}

	@Override
	public void updatePosition(MineFieldRenderer renderer) {
		float startx = renderer.lookAt.lookAtX;
		float starty = renderer.lookAt.lookAtY+renderer.height_ratio-renderer.scaleFactor*1.5f;
		
		modelRect.set(startx, 
				starty,
				startx + renderer.scaleFactor,
				starty + renderer.scaleFactor
				);
		
		Matrix.setIdentityM(mTMatrix, 0);
		Matrix.translateM(mTMatrix, 0, startx, starty, 0);
		
		Matrix.multiplyMM(tempMatrix, 0, mTMatrix, 0, renderer.scaleMatrix, 0);
		Matrix.multiplyMM(newGameMatrix, 0, renderer.mMVPMatrix, 0, tempMatrix, 0);
	}

	@Override
	public void draw(MineFieldRenderer renderer) {
		renderer.tiles.draw(newGameMatrix, 0.25f, 0.75f, renderer.spriteSheets, renderer.fieldSpriteSheet);
	}

}
