package com.example.naver.multi_color_filter.shader;

import android.util.Log;

import com.example.naver.multi_color_filter.util.LoggerConfig;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by NAVER on 2017. 8. 22..
 */

public class BasicShaderProgram {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT;

    protected int programId;
    protected int vShaderId;
    protected int fShaderId;

    private int aPositionLocation;
    private int aTextureCoordinateLocation;
    private int uTextureUnitLocation1;
    private int uFlipMatLocation;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private final float[] VERTEX_DATA = {
            1f, -1f,
            -1f, -1f,
            -1f, 1f,
            1f, 1f
    };

    private final float[] TEXTURE_DATA = {
            1f, 0f,
            0f, 0f,
            0f, 1f,
            1f, 1f
    };

    private final float[] flipMat = new float[]{
            1, 0, 0, 0,
            0, -1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };

    private final float[] eyeMat = new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };

    public BasicShaderProgram(String vShader, String fShader) {
        mVertexBuffer = ByteBuffer
                .allocateDirect(VERTEX_DATA.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX_DATA);

        mTextureBuffer = ByteBuffer
                .allocateDirect(TEXTURE_DATA.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEXTURE_DATA);

        initProgram(vShader, fShader);
    }

    private void initProgram(String vertex, String fragment) {
        makeProgram(vertex, fragment);

        aPositionLocation = glGetAttribLocation(programId, "a_Position");
        aTextureCoordinateLocation = glGetAttribLocation(programId, "a_TextureInputCoordinates1");
        uTextureUnitLocation1 = glGetUniformLocation(programId, "u_TextureUnit1");
        uFlipMatLocation = glGetUniformLocation(programId, "u_flipMat");

        mVertexBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 8, mVertexBuffer);
        glEnableVertexAttribArray(aPositionLocation);

        mTextureBuffer.position(0);
        glVertexAttribPointer(aTextureCoordinateLocation, TEXTURE_COORDINATES_COMPONENT_COUNT, GL_FLOAT, false, 8, mTextureBuffer);
        glEnableVertexAttribArray(aTextureCoordinateLocation);
    }

    private void makeProgram(String vertex, String fragment) {
        programId = glCreateProgram();

        vShaderId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vShaderId, vertex);
        glCompileShader(vShaderId);

        fShaderId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fShaderId, fragment);
        glCompileShader(fShaderId);

        glAttachShader(programId, vShaderId);
        glAttachShader(programId, fShaderId);

        glLinkProgram(programId);

        final int[] linkStatus = new int[1];
        glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);

        if (LoggerConfig.ON) {
            Log.v("BasicShaderProgram", "Results of linking program:\n" + glGetProgramInfoLog(programId));
        }
    }

    public void bindData() {
        mVertexBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, mVertexBuffer);
        glEnableVertexAttribArray(aPositionLocation);

        mTextureBuffer.position(0);
        glVertexAttribPointer(aTextureCoordinateLocation, TEXTURE_COORDINATES_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, mTextureBuffer);
        glEnableVertexAttribArray(aTextureCoordinateLocation);
    }

    public int getProgramId() {
        return programId;
    }

    public void setTextureUnit(int texture) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glUniform1i(uTextureUnitLocation1, 0);
    }

    public void onDraw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }

    public void setFlipMatrix(int flip) {
        if(flip == 1)
            glUniformMatrix4fv(uFlipMatLocation, 1, false, flipMat, 0);
        else
            glUniformMatrix4fv(uFlipMatLocation, 1, false, eyeMat, 0);
    }
}
