package com.vvovsvv.touchmine;

public class UIElementTimeCounter extends UIElement {

	private int prevLength;
	public UIElementTimeCounter()
	{
		super();
		prevLength=0;
	}
	
	@Override
	public void updatePosition(MineFieldRenderer renderer) {
		String elapsed_time = String.valueOf(renderer.getMineField().getElapsedTime());
		prevLength = elapsed_time.length();
		float startx = renderer.lookAt.lookAtX+renderer.width_ratio-renderer.scaleFactor*.5f-prevLength*renderer.infoScaleFactor;
		float starty = renderer.lookAt.lookAtY+renderer.height_ratio-renderer.scaleFactor*1.5f-renderer.infoScaleFactor;
		
		modelRect.set(startx, 
				starty, 
				startx + renderer.infoScaleFactor*(elapsed_time.length()-1),
				starty + renderer.infoScaleFactor
				);
	}

	@Override
	public void draw(MineFieldRenderer renderer) {
		//time
		String elapsed_time = String.valueOf(renderer.getMineField().getElapsedTime());
		if(elapsed_time.length()>prevLength);
		{
			updatePosition(renderer);
		}
		renderer.__draw_string(modelRect.left, modelRect.bottom, elapsed_time, true);
	}

}
