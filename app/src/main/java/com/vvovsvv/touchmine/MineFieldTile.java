package com.vvovsvv.touchmine;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteBuffer;

import android.opengl.GLES20;
import android.util.Log;

public class MineFieldTile {
	private static String TAG = "MineFieldTile";
	
	private final String vertexShaderCode = 
			"uniform mat4 uMVPMatrix;"+
			"attribute vec4 vPosition;"+
			"attribute vec2 TexCoordIn;"+
			"varying vec2 TexCoordOut;" +
			"void main() {"+
			"gl_Position = uMVPMatrix * vPosition;" +
			" TexCoordOut = TexCoordIn;"+
			"}";
	
	private final String fragmentShaderCode = 
			"precision mediump float;" +
			"varying vec2 TexCoordOut;"+
			"uniform sampler2D coordIn;"+
			"uniform float posX;"+
			"uniform float posY;"+
			"void main() {"+
			"gl_FragColor = texture2D(coordIn, vec2(TexCoordOut.x+posX, TexCoordOut.y+posY));"+
			"}";
	
	private final String simpleColorVertexShaderCode = 
			"uniform mat4 uMVPMatrix;"+
			"attribute vec4 vPosition;"+
			"void main() {"+
			"gl_Position = uMVPMatrix * vPosition;" +
			"}";
	
	private final String simpleColorFragmentShaderCode = 
			"precision mediump float;" +
			"uniform vec4 u_Color;"+
			"void main() {"+
			"gl_FragColor = u_Color;"+
			"}";
	
	private float texture[] = {
			0f, 0f,
			0f, .25f,
			.25f, .25f,
			.25f, 0f
			};
	
	private float rect_texture[] = {
			0f, 0f,
			0f, .5f,
			1f, .5f,
			1f, 0f
			};
	
	private final FloatBuffer vertexBuffer;
	private final FloatBuffer rectVertexBuffer;
	private final ShortBuffer drawListBuffer;
	private final FloatBuffer textureBuffer;
	private final FloatBuffer rectTextureBuffer;
	private final ShortBuffer squareDrawListBuffer;
	private int mProgram;
	private int mPositionHandle;
	private int mMVPMatrixHandle;
	
	private int simpleColorProgram;
	private int simpleColorPositionHandle;
	private int simpleColorMVPMatrixHandle;
	
	static final int COORDS_PER_VERTEX = 3;
	static final int COORDS_PER_TEXTURE = 2;
	static float squareCoords[] = { 0f, 1f, 0.0f,
		0f, 0f, 0.0f,
		1f, 0f, 0.0f,
		1f, 1f, 0.0f };
	static float rectCoords[] = { 0f, 0.5f, 0.0f,
		0f, 0f, 0.0f,
		1f, 0f, 0.0f,
		1f, 0.5f, 0.0f };
	
	private final short drawOrder[] = {0,1,2,0,2,3};
	private final short squareDrawOrder[] = {0,1,1,2,2,3,3,0};
	
	private final int vertexStride = COORDS_PER_VERTEX*4;
	public static int textureStride = COORDS_PER_TEXTURE*4;
	
	public MineFieldTile()
	{
		ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length*4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(squareCoords);
		vertexBuffer.position(0);
		
		ByteBuffer rectb = ByteBuffer.allocateDirect(rectCoords.length*4);
		rectb.order(ByteOrder.nativeOrder());
		rectVertexBuffer = rectb.asFloatBuffer();
		rectVertexBuffer.put(rectCoords);
		rectVertexBuffer.position(0);
		
		ByteBuffer bb2 = ByteBuffer.allocateDirect(texture.length*4);
		bb2.order(ByteOrder.nativeOrder());
		textureBuffer = bb2.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
		
		ByteBuffer bb3 = ByteBuffer.allocateDirect(rect_texture.length*4);
		bb3.order(ByteOrder.nativeOrder());
		rectTextureBuffer = bb3.asFloatBuffer();
		rectTextureBuffer.put(rect_texture);
		rectTextureBuffer.position(0);
		
		ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length*2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);
		
		ByteBuffer sqdlb = ByteBuffer.allocateDirect(squareDrawOrder.length*2);
		sqdlb.order(ByteOrder.nativeOrder());
		squareDrawListBuffer = sqdlb.asShortBuffer();
		squareDrawListBuffer.put(squareDrawOrder);
		squareDrawListBuffer.position(0);
		
		mProgram = createProgram(vertexShaderCode, fragmentShaderCode);
        if (mProgram == 0) {
            return;
        }
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        
        simpleColorProgram = createProgram(simpleColorVertexShaderCode, simpleColorFragmentShaderCode);
        if (simpleColorProgram == 0) {
            return;
        }
        simpleColorPositionHandle = GLES20.glGetAttribLocation(simpleColorProgram, "vPosition");
        simpleColorMVPMatrixHandle = GLES20.glGetUniformLocation(simpleColorProgram, "uMVPMatrix");
	}
	
    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }
    
    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
	
	private int loadShader(int shaderType, String source)
	{
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }
	
	public void draw(float[] mvpMatrix, float posX, float posY, int[] spriteSheet, int currentSheet)
	{
		GLES20.glUseProgram(mProgram);
		checkGlError("glUseProgram");
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, spriteSheet[currentSheet]);
		
		//vertex shader
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false,
				vertexStride, vertexBuffer);
		checkGlError("glVertexAttribPointer mPositionHandle");
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		checkGlError("glEnableVertexAttribArray mPositionHandle");
				
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		
		int vsTextureCoord = GLES20.glGetAttribLocation(mProgram, "TexCoordIn");
		checkGlError("glGetAttribLocation TexCoordIn");
		GLES20.glVertexAttribPointer(vsTextureCoord, COORDS_PER_TEXTURE,
				GLES20.GL_FLOAT, false,
				textureStride, textureBuffer);
		checkGlError("glVertexAttribPointer vsTextureCoord");
		GLES20.glEnableVertexAttribArray(vsTextureCoord);
		checkGlError("glEnableVertexAttribArray vsTextureCoord");
		
		//fragment shader
		int fsTexture = GLES20.glGetUniformLocation(mProgram, "coordIn");
		int fsPosX = GLES20.glGetUniformLocation(mProgram, "posX");
		int fsPosY = GLES20.glGetUniformLocation(mProgram, "posY");
		GLES20.glUniform1i(fsTexture, 0);
		GLES20.glUniform1f(fsPosX, posX);
		GLES20.glUniform1f(fsPosY, posY);
		
		//draw
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
				GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
		checkGlError("glDrawElements");
	}
	
	public void drawRect(float[] mvpMatrix, float posX, float posY, int[] spriteSheet, int currentSheet)
	{
		GLES20.glUseProgram(mProgram);
		checkGlError("glUseProgram");
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, spriteSheet[currentSheet]);
		
		//vertex shader
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false,
				vertexStride, rectVertexBuffer);
		checkGlError("glVertexAttribPointer mPositionHandle");
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		checkGlError("glEnableVertexAttribArray mPositionHandle");
				
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		
		int vsTextureCoord = GLES20.glGetAttribLocation(mProgram, "TexCoordIn");
		checkGlError("glGetAttribLocation TexCoordIn");
		GLES20.glVertexAttribPointer(vsTextureCoord, COORDS_PER_TEXTURE,
				GLES20.GL_FLOAT, false,
				textureStride, rectTextureBuffer);
		checkGlError("glVertexAttribPointer vsTextureCoord");
		GLES20.glEnableVertexAttribArray(vsTextureCoord);
		checkGlError("glEnableVertexAttribArray vsTextureCoord");
		
		//fragment shader
		int fsTexture = GLES20.glGetUniformLocation(mProgram, "coordIn");
		int fsPosX = GLES20.glGetUniformLocation(mProgram, "posX");
		int fsPosY = GLES20.glGetUniformLocation(mProgram, "posY");
		GLES20.glUniform1i(fsTexture, 0);
		GLES20.glUniform1f(fsPosX, posX);
		GLES20.glUniform1f(fsPosY, posY);
		
		//draw
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
				GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
		checkGlError("glDrawElements");
	}

	
	public void drawSimpleColor(float[] mvpMatrix, float[] color, boolean frame)
	{
		GLES20.glUseProgram(simpleColorProgram);
		checkGlError("glUseProgram");
		
		//vertex shader
		GLES20.glVertexAttribPointer(simpleColorPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false,
				vertexStride, vertexBuffer);
		checkGlError("glVertexAttribPointer simpleColorPositionHandle");
		GLES20.glEnableVertexAttribArray(simpleColorPositionHandle);
		checkGlError("glEnableVertexAttribArray simpleColorPositionHandle");
				
		GLES20.glUniformMatrix4fv(simpleColorMVPMatrixHandle, 1, false, mvpMatrix, 0);
		
		//fragment shader
		int colorLoc = GLES20.glGetUniformLocation(simpleColorProgram, "u_Color");
		GLES20.glUniform4f(colorLoc, color[0], color[1], color[2], 1f);
		
		//draw
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
				GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
		checkGlError("glDrawElements");
		if(frame)
		{
			GLES20.glUniform4f(colorLoc, 0f, 0f, 0f, 1f);
			GLES20.glDrawElements(GLES20.GL_LINES, squareDrawOrder.length,
					GLES20.GL_UNSIGNED_SHORT, squareDrawListBuffer);
			checkGlError("glDrawElements");
		}
	}

}
