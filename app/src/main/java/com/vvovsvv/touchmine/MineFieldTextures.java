package com.vvovsvv.touchmine;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class MineFieldTextures {
	private int[] textures = new int[4];
	
	public MineFieldTextures()
	{}
	
	public int[] loadTexture(int texture, Context context, int textureNumber)
	{
		InputStream imagestream = context.getResources().openRawResource(texture);
		Bitmap bitmap = null;
		
		try 
		{
			bitmap = BitmapFactory.decodeStream(imagestream);
		}
		catch(Exception e)
		{
			//Handle your exceptions here
		}
		finally 
		{
			try
			{
				imagestream.close();
				imagestream = null;
			} 
			catch (IOException e) 
			{
				//Handle your exceptions here
			}
		}
		
		GLES20.glGenTextures(1, textures, textureNumber);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[textureNumber]);
		
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
		
		return textures;
	}
	
}
