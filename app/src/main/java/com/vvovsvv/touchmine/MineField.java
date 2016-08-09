package com.vvovsvv.touchmine;

import java.util.Calendar;
import java.util.Random;
import android.graphics.Rect;
import android.os.Bundle;
import com.vvovsvv.touchmine.Field.FieldUIState;


public class MineField {
	private int size_x;
	private int size_y;
	private int fieldSize;
	
	private long timeStart=0;
	private long timeEnd=0;
	private boolean gameOver=false;
	private boolean openMode=true;
	private boolean isWin=false;
	
	public void putOpenMode(boolean mode)
	{
		openMode = mode;
	}
	public boolean OpenMode()
	{
		return openMode;
	}
	

	public boolean isGameOver() {
		return gameOver;
	}

	private Field[][] raw_field;
	
	private int mines_count;
	public int getMines_count() {
		return mines_count;
	}

	public int getElapsedTime() {
		if(fields_opened==0)  return 0;
		
		if(!gameOver)
		{
			return (int)(Calendar.getInstance().getTimeInMillis()-timeStart)/1000;
		}
		else return (int)(timeEnd-timeStart)/1000;
	}
	
	private int fields_opened;
	private int flags_count;
	
	public int getFlags_count() {
		return flags_count;
	}

	private int raw_size_x;
	private int raw_size_y;
	private int[] lastGeneratedMine;
	
	//temp variables
	Rect fullField = new Rect();
	Rect requestField = new Rect();
	
	public MineField()
	{
		size_x = 0;
		size_y = 0;
		mines_count = 0;
		flags_count = 0;
		fields_opened = 0;
		
		raw_size_x=32;
		raw_size_y=32;
		raw_field = new Field[raw_size_x][raw_size_y];
		lastGeneratedMine = new int[2];
	}
	
	public Field getField(int x_pub, int y_pub)
	{
		return raw_field[x_pub+1][y_pub+1];
	}
	
	public int getSize_x() 
	{
		return size_x;
	}
	
	public int getSize_y() 
	{
		return size_y;
	}

	public boolean isWin()
	{
		return isWin;
	}
	
	private void __FillMines(int count)
	{
		Random randomizer = new Random();
		int x=0;
		int y=0;
		boolean added = false;
		for(int i=0; i<count; i++)
		{
			do
			{
				x = randomizer.nextInt(size_x)+1;
				y = randomizer.nextInt(size_y)+1;
				if(raw_field[x][y].is_mine) added = false;
				else
				{
					raw_field[x][y].is_mine = true;
					added = true;
				}
			} while (!added);
		}
		lastGeneratedMine[0]=x;
		lastGeneratedMine[1]=y;
	}
	
	private void __CountSurroundMines(int x_start, int y_start, int width, int height)
	{
		fullField.set(1, 1, size_x+1, size_y+1);
		requestField.set(x_start,y_start,x_start+width, y_start+height);
		requestField.setIntersect(requestField, fullField);
		
		int x,y,cur_x,cur_y=0;
		
		for(x=requestField.left; x < requestField.right; x++)
			for(y=requestField.top; y < requestField.bottom; y++)
			{
				if(raw_field[x][y].is_mine) continue;
				
				byte surround_mines = 0;
				for(cur_x = x-1; cur_x < x+2; cur_x++)
					for(cur_y = y-1; cur_y < y+2; cur_y++)
					{
						if(!(cur_x==x && cur_y==y) && raw_field[cur_x][cur_y].is_mine)
							surround_mines++;

					}
				
				raw_field[x][y].mines_around=surround_mines;
			}
	}
	
	private void __OpenInvisibleCells()
	{
		for(int x=0; x<size_x+2; x++)
		{
			raw_field[x][0].ui_state = FieldUIState.Opened;
			raw_field[x][size_y+1].ui_state = FieldUIState.Opened;
		}
		for(int y=0; y<size_y+2; y++)
		{
			raw_field[0][y].ui_state = FieldUIState.Opened;
			raw_field[size_x+1][y].ui_state = FieldUIState.Opened;
		}
	}
	
	private void __MoveFirstMine(int x, int y)
	{
		if(raw_field[x][y].is_mine)
		{
			while(true)
			{
				__FillMines(1);
				if(lastGeneratedMine[0]!=x || lastGeneratedMine[1]!=y) break; 
			}
			
			raw_field[x][y].is_mine = false;
			
			__CountSurroundMines(x-1, y-1, 3, 3);
			__CountSurroundMines(lastGeneratedMine[0]-1, lastGeneratedMine[1]-1, 3, 3);
		}
	}
	
	private void __ClearRect(int x_start, int y_start, int width, int height)
	{
		int x,y=0;
		for(x=x_start; x < x_start+width; x++)
			for(y=y_start; y < y_start+height; y++)
			{
				if(raw_field[x][y]==null) raw_field[x][y] = new Field();
				else
				{
					raw_field[x][y].is_mine=false;
					raw_field[x][y].mines_around=0;
					raw_field[x][y].ui_state=FieldUIState.Closed;
				}
			}
	}

	public void ConstructNewField(int x, int y, int mines)
	{
		if(x>(raw_size_x-2) || y>(raw_size_y-2))
		{
			raw_size_x=x+2;
			raw_size_y=y+2;
			raw_field = new Field[raw_size_x][raw_size_y];	
		}
		else
		{
			__ClearRect(0,0,x+2, y+2); //frame for simplify calculation
		}
		size_x = x;
		size_y = y;
		fieldSize = x*y;
		mines_count = mines;
		flags_count = 0;
		fields_opened = 0;
		__FillMines(mines_count);
		__CountSurroundMines(1,1,size_x,size_y);
		__OpenInvisibleCells();
		gameOver = false;
		isWin=false;
	}

	public boolean isXYInField(int x_pub, int y_pub)
	{
		if(x_pub>=size_x || y_pub>=size_y || x_pub<0 || y_pub<0) return false;
		return true;
	}
	
	public boolean OpenField(int x_pub, int y_pub)
	{
		if(x_pub>=size_x || y_pub>=size_y || x_pub<0 || y_pub<0) return true;
				
		int x = x_pub+1;
		int y = y_pub+1;
		
		if(raw_field[x][y].ui_state != FieldUIState.Closed) return true;
		
		if(fields_opened==0)
		{
			__MoveFirstMine(x,y);
			timeStart = Calendar.getInstance().getTimeInMillis();
		}
		fields_opened++;
		
		raw_field[x][y].ui_state = FieldUIState.Opened;
				
		if(raw_field[x][y].is_mine)
		{
			//lose
			gameOver=true;
			timeEnd = Calendar.getInstance().getTimeInMillis();
			return false;
		}
		else if(raw_field[x][y].mines_around==0)
		{
			//open surrounding fields
			for(int cur_x=x-1; cur_x<x+2;cur_x++)
				for(int cur_y=y-1; cur_y<y+2;cur_y++)
					OpenField(cur_x-1,cur_y-1);
		}
		else if(fieldSize-fields_opened==mines_count)
		{
			//win
			gameOver=true;
			isWin=true;
			timeEnd = Calendar.getInstance().getTimeInMillis();
			return false;
		}
		
		return true;
	}
	
	public boolean DoubleClickOnOpenField(int x_pub, int y_pub)
	{
		if(x_pub>=size_x || y_pub>=size_y || x_pub<0 || y_pub<0) return true;
		
		int x = x_pub+1;
		int y = y_pub+1;
		
		if(raw_field[x][y].ui_state != FieldUIState.Opened) return true;
		
		boolean ret_result = true;
		
		//count surrounding flags
		int flags_count=0;
		for(int cur_x=x-1; cur_x<x+2;cur_x++)
			for(int cur_y=y-1; cur_y<y+2;cur_y++)
			{
				if(raw_field[cur_x][cur_y].ui_state==FieldUIState.Flagged) flags_count++;
			}
		
		if(raw_field[x][y].mines_around==flags_count)
		{
			//open surrounding fields
			for(int cur_x=x-1; cur_x<x+2;cur_x++)
				for(int cur_y=y-1; cur_y<y+2;cur_y++)
					if(!OpenField(cur_x-1,cur_y-1))
					{
						ret_result=false;
					}
		}
		
		return ret_result;
	}
	
	public void SetFlag(int x_pub, int y_pub)
	{
		if(x_pub>=size_x || y_pub>=size_y || x_pub<0 || y_pub<0) return;
		
		int x = x_pub+1;
		int y = y_pub+1;
		
		if(raw_field[x][y].ui_state == FieldUIState.Opened) return;
		else if(raw_field[x][y].ui_state == FieldUIState.Closed)
		{
			raw_field[x][y].ui_state = FieldUIState.Flagged;
			flags_count++;
		}
		else 
		{
			raw_field[x][y].ui_state = FieldUIState.Closed;
			flags_count--;
		}
	}
	
	public void Save(Bundle state)
	{
		state.putBoolean("openMode", openMode);
		state.putInt("size_x", size_x);
		state.putInt("size_y", size_y);
		state.putInt("mines_count", mines_count);
		state.putInt("flags_count", flags_count);
		state.putInt("fields_opened", fields_opened);
		state.putInt("raw_size_x", raw_size_x);
		state.putInt("raw_size_y", raw_size_y);
		state.putIntArray("lastGeneratedMine", lastGeneratedMine);
		state.putBoolean("gameOver", gameOver);
		state.putBoolean("isWin", isWin);
		
		long gandicap = timeEnd-timeStart;
		if(gandicap<0)
		{
			gandicap = Calendar.getInstance().getTimeInMillis() - timeStart;
		}
		state.putLong("gandicap", gandicap);
		
		boolean[] raw_field_mines = new boolean[raw_size_x*raw_size_y];
		byte[] raw_field_mines_around = new byte[raw_size_x*raw_size_y];
		byte[] raw_field_mines_ui_state = new byte[raw_size_x*raw_size_y];
		
		int position=0;
		for(int x = 0; x<size_x+1; x++)
		{
			for(int y = 0; y<size_y+1; y++, position++)
			{
				raw_field_mines[position]=raw_field[x][y].is_mine;
				raw_field_mines_around[position]=raw_field[x][y].mines_around;
				raw_field_mines_ui_state[position]=(byte)(raw_field[x][y].ui_state.ordinal());
			}
		}
		
		state.putBooleanArray("raw_field_mines", raw_field_mines);
		state.putByteArray("raw_field_mines_around", raw_field_mines_around);
		state.putByteArray("raw_field_mines_ui_state", raw_field_mines_ui_state);
	}
	
	public void Load(Bundle state)
	{
		openMode = state.getBoolean("openMode");
		size_x = state.getInt("size_x");
		size_y = state.getInt("size_y");
		fieldSize = size_x*size_y;
		mines_count = state.getInt("mines_count");
		flags_count = state.getInt("flags_count");
		fields_opened = state.getInt("fields_opened");
		raw_size_x = state.getInt("raw_size_x");
		raw_size_y = state.getInt("raw_size_y");
		lastGeneratedMine = state.getIntArray("lastGeneratedMine");
		gameOver = state.getBoolean("gameOver");
		isWin = state.getBoolean("isWin");
		
		boolean[] raw_field_mines = state.getBooleanArray("raw_field_mines");
		byte[] raw_field_mines_around = state.getByteArray("raw_field_mines_around");
		byte[] raw_field_mines_ui_state = state.getByteArray("raw_field_mines_ui_state");
		
		raw_field = new Field[raw_size_x][raw_size_y];
		__ClearRect(0,0,size_x+2, size_y+2);
		
		int position=0;
		Field.FieldUIState[] all_states = Field.FieldUIState.values();
		for(int x = 0; x<size_x+1; x++)
		{
			for(int y = 0; y<size_y+1; y++, position++)
			{
				raw_field[x][y].is_mine = raw_field_mines[position];
				raw_field[x][y].mines_around = raw_field_mines_around[position];
				raw_field[x][y].ui_state = all_states[raw_field_mines_ui_state[position]];
			}
		}
		
		// time
		long gandicap = state.getLong("gandicap"); // timeEnd-timeStart);
		timeStart = Calendar.getInstance().getTimeInMillis() - gandicap;
		if(gameOver)
		{
			timeEnd = timeStart + gandicap;
		}
	}
	
}
