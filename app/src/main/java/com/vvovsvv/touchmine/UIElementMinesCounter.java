package com.vvovsvv.touchmine;

import android.opengl.Matrix;

public class UIElementMinesCounter extends UIElement {
	private final float[] mTMatrix = new float[16]; 
	private float[] curTileMatrix = new float[16];
	
	public UIElementMinesCounter() {
		super();
	}

	@Override
	public void updatePosition(MineFieldRenderer renderer) {
		String mines_flags = String.valueOf(renderer.getMineField().getMines_count()-renderer.getMineField().getFlags_count());
		
		float startx = renderer.lookAt.lookAtX-renderer.width_ratio+renderer.scaleFactor*.5f;
		float starty = renderer.lookAt.lookAtY+renderer.height_ratio-renderer.scaleFactor*1.5f-renderer.infoScaleFactor;

		modelRect.set(startx, 
				starty, 
				startx + renderer.infoScaleFactor*(mines_flags.length()+1),
				starty + renderer.infoScaleFactor
				);

	}

	@Override
	public void draw(MineFieldRenderer renderer) {
		String mines_flags = String.valueOf(renderer.getMineField().getMines_count()-renderer.getMineField().getFlags_count());
		
		float tileLocX = modelRect.left;
		float tileLocY = modelRect.bottom;
		
		Matrix.setIdentityM(mTMatrix, 0);
		Matrix.translateM(mTMatrix, 0, tileLocX, tileLocY, 0);
		
		Matrix.multiplyMM(curTileMatrix, 0, mTMatrix, 0, renderer.infoScaleMatrix, 0);
		Matrix.multiplyMM(curTileMatrix, 0, renderer.mMVPMatrix, 0, curTileMatrix, 0);
		renderer.tiles.draw(curTileMatrix, 0.25f, 0.5f, renderer.spriteSheets, renderer.fieldSpriteSheet);
		tileLocX += renderer.infoScaleFactor;
		renderer.__draw_string(tileLocX, tileLocY, mines_flags, true);
	}

}
