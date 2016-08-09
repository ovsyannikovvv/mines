package com.vvovsvv.touchmine;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class MineFieldRenderer implements Renderer {
	private MineField mineField;
	
	public MineFieldTile tiles;
	private MineFieldTextures textureloader;
	public int[] spriteSheets = new int[4];
	public int fieldSpriteSheet=0;
	public int numbersSpriteSheet=1;
	public int gameEndSpriteSheet=2;
	public int recordSpriteSheet=3;
	
	private UIElementTimeCounter timeCounter;
	private UIElementMinesCounter minesCounter;
	private UIElementFieldMap fieldMap;
	private UIElementNewGame newGame;
	private UIElementSelectMode selectMode;
	private UIElementGameEnd gameEnd;
	
	private Context context;
	
	private final float[] mTMatrix = new float[16];
	private float[] mVMatrix = new float[16];
	public float[] mMVPMatrix = new float[16];
	private float[] mProjMatrix = new float[16];
	public float[] scaleMatrix = new float[16];
	public float[] infoScaleMatrix = new float[16];
	
	private float[] screenToModel = new float[16];
	private float[] curTileMatrix = new float[16];
	
	private float[] tapModelCoords = new float[4];
	
	private float[] tempCoord = new float[4];
	
	int screen_width = 1;
	int screen_height = 1;
	float width_ratio=0;
	float height_ratio=0;

    public SoftMoveInfo lookAt = new SoftMoveInfo();

    //float lookAtX = 0;
	//float lookAtY = 0;
	float scaleFactor = 0; 

	float infoScaleFactor = 0;
	
	private float actionDownX = 0;
	private float actionDownY = 0;
	private float actionDownLookAtX = 0;
	private float actionDownLookAtY = 0;
	private float actionScaleFactor = 0;
	
	public MineFieldRenderer(Context appContext, MineField mmineField, DisplayMetrics devMetrics)
	{
		context = appContext;
		setMineField(mmineField);
		
		float devDensity = devMetrics.density;
		scaleFactor = devDensity/12f;
		infoScaleFactor = devDensity/16f;
		
		Matrix.setIdentityM(scaleMatrix, 0);
		Matrix.scaleM(scaleMatrix, 0, scaleFactor, scaleFactor, scaleFactor);
		
		Matrix.setIdentityM(infoScaleMatrix, 0);
		Matrix.scaleM(infoScaleMatrix, 0, infoScaleFactor, infoScaleFactor, infoScaleFactor);
		
		timeCounter = new UIElementTimeCounter(); 
		minesCounter = new UIElementMinesCounter();
		fieldMap = new UIElementFieldMap(this);
		newGame = new UIElementNewGame();
		selectMode = new UIElementSelectMode();
		gameEnd = new UIElementGameEnd(this);
	}
	
	@Override
	public void onDrawFrame(GL10 gl) 
	{
		GLES20.glClearColor(0.1f, 0.2f, 0.3f, 1.0f);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT|GLES20.GL_COLOR_BUFFER_BIT);
		drawField();
		drawUI();
        lookAt.integrate();
    }

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{
		screen_width=width;
		screen_height=height;
		GLES20.glViewport(0, 0, width, height);
		
		float ratio = (float) width / height;
		if(ratio<1)
		{
			width_ratio=ratio;
			height_ratio=1;
		}
		else
		{
			width_ratio=1f;
			height_ratio=1f/ratio;
		}
		Matrix.orthoM(mProjMatrix, 0, -width_ratio, width_ratio, -height_ratio, height_ratio, 3, 7);
		
		__Count_initial_look();
		__Count_initial_matrices();
	}

	private float maxlookAtX=0;
	private float maxlookAtY=0;
	private float minlookAtX=0;
	private float minlookAtY=0;
	
	private void __Count_initial_look()
	{
		int xsize = getMineField().getSize_x();
		int ysize = getMineField().getSize_y();

        lookAt.lookAtX = xsize*scaleFactor/2f;
        lookAt.lookAtY = ysize*scaleFactor/2f;
        //lookAtX = xsize*scaleFactor/2f;
		//lookAtY = ysize*scaleFactor/2f;
		
		float deltaX = xsize*scaleFactor/2f;
		float deltaY = ysize*scaleFactor/2f;
		
		maxlookAtX=lookAt.lookAtX+deltaX;
		maxlookAtY=lookAt.lookAtY+deltaY;
		minlookAtX=lookAt.lookAtX-deltaX;
		minlookAtY=lookAt.lookAtY-deltaY;
	}
	
	private void __Count_initial_matrices()
	{
		//Matrix.setLookAtM(mVMatrix, 0, lookAtX, lookAtY, -3, lookAtX, lookAtY, 0f, -1.0f, 0f, 0.0f);
        Matrix.setLookAtM(mVMatrix, 0, lookAt.lookAtX, lookAt.lookAtY, 3, lookAt.lookAtX, lookAt.lookAtY, 0f, 0f, 1f, 0.0f);
        //Matrix.setLookAtM(mVMatrix, 0, lookAtX, lookAtY, 3, lookAtX, lookAtY, 0f, 0f, 1f, 0.0f);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
		
		//screenToModel
		Matrix.invertM(screenToModel, 0, mMVPMatrix, 0);
		
		timeCounter.updatePosition(this);
		minesCounter.updatePosition(this);
		fieldMap.updatePosition(this);
		newGame.updatePosition(this);
		selectMode.updatePosition(this);
		gameEnd.updatePosition(this);
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		tiles = new MineFieldTile();
		textureloader = new MineFieldTextures();
		spriteSheets = textureloader.loadTexture(R.drawable.tiles, context, fieldSpriteSheet);
		spriteSheets = textureloader.loadTexture(R.drawable.numbers, context, numbersSpriteSheet);
		spriteSheets = textureloader.loadTexture(R.drawable.game_end, context, gameEndSpriteSheet);
		spriteSheets = textureloader.loadTexture(R.drawable.to_record, context, recordSpriteSheet);
	}

	public boolean ScreenTap(float x, float y)
	{
		boolean needSound = false;
		__screenCoordsToModelTapCoords(x,y);
		
		if(newGame.contains(tapModelCoords[0], tapModelCoords[1]))
		{
			needSound = true;
			mineField.putOpenMode(true);
			__Count_initial_look();
			__Count_initial_matrices();
			mineField.ConstructNewField(mineField.getSize_x(), mineField.getSize_y(), mineField.getMines_count());
		}
		else if(selectMode.contains(tapModelCoords[0], tapModelCoords[1]))
		{
			needSound = true;
			mineField.putOpenMode(!mineField.OpenMode());
		}
		else if(fieldMap.contains(tapModelCoords[0], tapModelCoords[1]))
		{
			//ignoring
		}
		else if(minesCounter.contains(tapModelCoords[0], tapModelCoords[1]))
		{
			//ignoring
		}
		else if(timeCounter.contains(tapModelCoords[0], tapModelCoords[1]))
		{
			//ignoring
		}
		else if(!getMineField().isGameOver())
		{
			float xx = tapModelCoords[0]/scaleFactor;
			float yy = tapModelCoords[1]/scaleFactor;
			if(xx >= 0 && yy >=0)
			{
				int field_x = (int) (xx);
				int field_y = (int) (yy);
				
				if(mineField.isXYInField(field_x, field_y))
					needSound = true;
		
				if(mineField.OpenMode())
				{
					mineField.OpenField(field_x, field_y);
				}
				else
				{
					mineField.SetFlag(field_x, field_y);
					mineField.DoubleClickOnOpenField(field_x, field_y);
				}
				
				if(mineField.isGameOver() && mineField.isWin())
				{
					int x_field_size = mineField.getSize_x();
					int y_field_size = mineField.getSize_y();
					int mines = mineField.getMines_count();
					
					SharedPreferences myPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
					String keyName = String.valueOf(x_field_size)+"x"+String.valueOf(y_field_size)+"x"+String.valueOf(mines);
					int saved_record = myPrefs.getInt(keyName, 999);
					int cur_record = mineField.getElapsedTime();
					gameEnd.setSecsToRecord(cur_record - saved_record);
					
					if(cur_record < saved_record)
					{
						Editor editor = myPrefs.edit();
						editor.putInt(keyName, cur_record);
						editor.commit();
						gameEnd.setIsRecord(true);
					}
					else
					{
						gameEnd.setIsRecord(false);
					}
				}
			}
		}
		return needSound;
	}
	
	private void drawUI()
	{
		selectMode.draw(this);
		newGame.draw(this);
		minesCounter.draw(this);
		timeCounter.draw(this);
		fieldMap.draw(this);
		gameEnd.draw(this);
	}
	
	public void __draw_string(float tileLocX, float tileLocY, String drawed_string, boolean info)
	{
		int strLen = drawed_string.length();
		for(int i=0;i<strLen;i++)
		{
			Matrix.setIdentityM(mTMatrix, 0);
			Matrix.translateM(mTMatrix, 0, tileLocX, tileLocY, 0);
			if(!info)
			{
				Matrix.multiplyMM(curTileMatrix, 0, mTMatrix, 0, scaleMatrix, 0);
			}
			else
			{
				Matrix.multiplyMM(curTileMatrix, 0, mTMatrix, 0, infoScaleMatrix, 0);
			}
			Matrix.multiplyMM(curTileMatrix, 0, mMVPMatrix, 0, curTileMatrix, 0);
			
			char symbol=drawed_string.charAt(i);
			switch(symbol)
			{
			case '0':
				tiles.draw(curTileMatrix, 0f, 0f, spriteSheets, numbersSpriteSheet);
				break;
			case '1':
				tiles.draw(curTileMatrix, 0.25f, 0f, spriteSheets, numbersSpriteSheet);
				break;
			case '2':
				tiles.draw(curTileMatrix, 0.5f, 0f, spriteSheets, numbersSpriteSheet);
				break;
			case '3':
				tiles.draw(curTileMatrix, 0.75f, 0f, spriteSheets, numbersSpriteSheet);
				break;
			case '4':
				tiles.draw(curTileMatrix, 0f, 0.25f, spriteSheets, numbersSpriteSheet);
				break;
			case '5':
				tiles.draw(curTileMatrix, 0.25f, 0.25f, spriteSheets, numbersSpriteSheet);
				break;
			case '6':
				tiles.draw(curTileMatrix, 0.5f, 0.25f, spriteSheets, numbersSpriteSheet);
				break;
			case '7':
				tiles.draw(curTileMatrix, 0.75f, 0.25f, spriteSheets, numbersSpriteSheet);
				break;
			case '8':
				tiles.draw(curTileMatrix, 0f, 0.5f, spriteSheets, numbersSpriteSheet);
				break;
			case '9':
				tiles.draw(curTileMatrix, 0.25f, 0.5f, spriteSheets, numbersSpriteSheet);
				break;
			case '-':
				tiles.draw(curTileMatrix, 0.5f, 0.5f, spriteSheets, numbersSpriteSheet);
				break;
			default:
				tiles.draw(curTileMatrix, 0.75f, 0.75f, spriteSheets, numbersSpriteSheet);
				break;
			}
			if(!info)
			{
				tileLocX += scaleFactor;
			}
			else
			{
				tileLocX += infoScaleFactor;
			}
		}
	}
	
	private void drawField()
	{
		float tileLocY = 0f;
		float tileLocX = 0f;
		
		int xsize = mineField.getSize_x();
		int ysize = mineField.getSize_y();
		
		for(int x=0; x<xsize; x++)
		{
			for(int y=0; y<ysize; y++)
			{
				Matrix.setIdentityM(mTMatrix, 0);
				Matrix.translateM(mTMatrix, 0, tileLocX, tileLocY, 0);
				
				Matrix.multiplyMM(curTileMatrix, 0, mTMatrix, 0, scaleMatrix, 0);
				Matrix.multiplyMM(curTileMatrix, 0, mMVPMatrix, 0, curTileMatrix, 0);
				Field fieldState = mineField.getField(x, y);
				if(fieldState.ui_state == Field.FieldUIState.Closed)
				{
					if(mineField.isGameOver() && fieldState.is_mine)
					{
						tiles.draw(curTileMatrix, 0.5f, 0.5f, spriteSheets, fieldSpriteSheet);
					}
					else
					{
						tiles.draw(curTileMatrix, 0f, 0f, spriteSheets, fieldSpriteSheet);						
					}
				}
				else if(fieldState.ui_state == Field.FieldUIState.Flagged)
				{
					if(mineField.isGameOver() && !fieldState.is_mine )
					{
						tiles.draw(curTileMatrix, 0f, 0.75f, spriteSheets, fieldSpriteSheet);
					}
					else
					{
						tiles.draw(curTileMatrix, 0.25f, 0.5f, spriteSheets, fieldSpriteSheet);
					}
				}
				else
				{
					if(fieldState.is_mine)
					{
						tiles.draw(curTileMatrix, 0.5f, 0.5f, spriteSheets, fieldSpriteSheet);
					}
					else
					{
						switch(fieldState.mines_around)
						{
							case 0:
								tiles.draw(curTileMatrix, 0.75f, 0.75f, spriteSheets, fieldSpriteSheet);
								break;
							case 1:
								tiles.draw(curTileMatrix, 0.25f, 0f, spriteSheets, fieldSpriteSheet);
								break;
							case 2:
								tiles.draw(curTileMatrix, 0.5f, 0f, spriteSheets, fieldSpriteSheet);
								break;
							case 3:
								tiles.draw(curTileMatrix, 0.75f, 0f, spriteSheets, fieldSpriteSheet);
								break;
							case 4:
								tiles.draw(curTileMatrix, 0f, 0.25f, spriteSheets, fieldSpriteSheet);
								break;
							case 5:
								tiles.draw(curTileMatrix, 0.25f, 0.25f, spriteSheets, fieldSpriteSheet);
								break;
							case 6:
								tiles.draw(curTileMatrix, 0.5f, 0.25f, spriteSheets, fieldSpriteSheet);
								break;
							case 7:
								tiles.draw(curTileMatrix, 0.75f, 0.25f, spriteSheets, fieldSpriteSheet);
								break;
							case 8:
								tiles.draw(curTileMatrix, 0f, 0.5f, spriteSheets, fieldSpriteSheet);
								break;
						}
					}
				}
				tileLocY += scaleFactor;
			}
			tileLocY = 0f;
			tileLocX += scaleFactor;
		}
	}
	
	/*private float prevScreenX=0;
	private float prevScreenY=0;*/
	
	public void onActionDown(float x, float y)
	{
		//prevScreenX = x;
		//prevScreenY = y;
				
		__screenCoordsToModelTapCoords(x,y);
		
		actionDownX = tapModelCoords[0];
		actionDownY = tapModelCoords[1];
		
		//actionDownLookAtX = lookAtX;
		//actionDownLookAtY = lookAtY;
        actionDownLookAtX = lookAt.lookAtX;
        actionDownLookAtY = lookAt.lookAtY;
		
		//isTap = true;
	}
	
	
	public void onScaleDown()
	{
		actionScaleFactor = scaleFactor;
	}
	
	public void onScaleMove(float scale)
	{
		scaleFactor = actionScaleFactor*scale;
		__Count_initial_matrices();
	}

	public void onActionMove(float x, float y)
	{
		//if(Math.abs(prevScreenX - x) < 10f && Math.abs(prevScreenY - y) < 10f) return;
		//prevScreenX = x;
		//prevScreenY = y;
		
		__screenCoordsToModelTapCoords(x,y);
		
		float actionMoveX = tapModelCoords[0];
		float actionMoveY = tapModelCoords[1];
		
		/*lookAtX = actionDownLookAtX-(actionMoveX-actionDownX);
		lookAtY = actionDownLookAtY-(actionMoveY-actionDownY);*/

        /*lookAt.lookAtX = actionDownLookAtX-(actionMoveX-actionDownX);
        lookAt.lookAtY = actionDownLookAtY-(actionMoveY-actionDownY);*/

        lookAt.addDragDelta((actionMoveX-actionDownX),
                (actionMoveY-actionDownY));
		
		if(lookAt.lookAtX>maxlookAtX) lookAt.lookAtX = maxlookAtX;
		if(lookAt.lookAtX<minlookAtX) lookAt.lookAtX = minlookAtX;
		if(lookAt.lookAtY>maxlookAtY) lookAt.lookAtY = maxlookAtY;
		if(lookAt.lookAtY<minlookAtY) lookAt.lookAtY = minlookAtY;
		
		__Count_initial_matrices();
		
		//count isTap
		/*float absDeltaX = actionMoveX-actionDownX;
		if(absDeltaX<0) absDeltaX = -absDeltaX;
		
		float absDeltaY = actionMoveY-actionDownY;
		if(absDeltaY<0) absDeltaY = -absDeltaY;
		
		if(absDeltaX > scaleFactor || absDeltaY > scaleFactor) isTap = false;*/
	}

	public MineField getMineField() {
		return mineField;
	}

	public void setMineField(MineField mineField) {
		this.mineField = mineField;
	}
	
	private void __screenCoordsToModelTapCoords(float x, float y)
	{
		tempCoord[0]= (float) ((2f*x / screen_width) - 1f);
		tempCoord[1]= (float) (2f*(screen_height-y) / screen_height)-1f;
		tempCoord[2]=0;
		tempCoord[3]=1;
		
		Matrix.multiplyMV(tapModelCoords, 0, screenToModel, 0, tempCoord, 0);
	}
	
	public void Save(Bundle state)
	{
		state.putInt("renderer.SecsToRecord", this.gameEnd.getSecsToRecord());
		state.putBoolean("renderer.isRecord", this.gameEnd.getIsRecord());
	}
	
	public void Load(Bundle state)
	{
		this.gameEnd.setSecsToRecord(state.getInt("renderer.SecsToRecord"));
		this.gameEnd.setIsRecord(state.getBoolean("renderer.isRecord"));
	}
	
}

