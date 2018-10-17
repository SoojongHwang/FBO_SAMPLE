package com.example.naver.multi_color_filter.shader;

import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;

/**
 * Created by NAVER on 2017. 8. 22..
 */

public class MultiShaderProgram extends BasicShaderProgram{
    private int uTextureUnitLocation2;
    private int uTargetLocation;

    public MultiShaderProgram(String vShader, String fShader) {
        super(vShader, fShader);

        uTextureUnitLocation2 = glGetUniformLocation(programId, "u_TextureUnit2");
        uTargetLocation = glGetUniformLocation(programId, "u_Location");
    }


    public void setFilterUnit(int filter) {
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, filter);
        glUniform1i(uTextureUnitLocation2, 1);
    }

    public void setTarget(int target){
        glUniform1i(uTargetLocation, target);
    }
}
