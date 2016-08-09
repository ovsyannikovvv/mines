package com.vvovsvv.touchmine;

public class Field
{
	public enum FieldUIState {Opened, Flagged, Closed};
	
	public FieldUIState ui_state;
	public boolean is_mine;
	public byte mines_around;
	
	public Field()
	{
		ui_state=FieldUIState.Closed;
	}
	
}