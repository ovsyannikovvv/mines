package com.vvovsvv.touchmine;

import android.graphics.RectF;

abstract public class UIElement {
	protected RectF modelRect;
	
	public UIElement()
	{
		modelRect = new RectF();
	}
	
	abstract public void updatePosition(MineFieldRenderer renderer);
	abstract public void draw(MineFieldRenderer renderer);
	
	public boolean contains(float x, float y)
	{
		return modelRect.contains(x, y);
	}
}
