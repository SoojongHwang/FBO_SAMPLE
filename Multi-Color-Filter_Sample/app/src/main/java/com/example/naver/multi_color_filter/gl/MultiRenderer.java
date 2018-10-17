package com.example.naver.multi_color_filter.gl;

import android.content.Context;
import android.net.Uri;
import android.opengl.GLSurfaceView;

import com.example.naver.multi_color_filter.R;
import com.example.naver.multi_color_filter.shader.BasicShaderProgram;
import com.example.naver.multi_color_filter.shader.MultiShaderProgram;
import com.example.naver.multi_color_filter.util.TextResourceReader;
import com.example.naver.multi_color_filter.util.TextureHelper;

import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glViewport;

/**
 * Created by NAVER on 2017. 8. 14..
 */
public class MultiRenderer implements GLSurfaceView.Renderer {
    private Context mContext;
    private BasicShaderProgram bProgram;
    private MultiShaderProgram multiProgram;

    private FBO fbo = null;
    private int imageId = -1;
    private int multiFilterId = -1;
    private int mTarget = 0;

    private Queue<Runnable> before;
    private Queue<Runnable> after;

    private int mWidth, mHeight;

    public MultiRenderer(Context mContext) {
        this.mContext = mContext;

        before = new LinkedList<>();
        after = new LinkedList<>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0, 0, 0, 1);

        bProgram = new BasicShaderProgram(
                TextResourceReader.readTextFileFromResource(mContext, R.raw.basic_vertex_shader),
                TextResourceReader.readTextFileFromResource(mContext, R.raw.basic_fragment_shader)
        );

        multiProgram = new MultiShaderProgram(
                TextResourceReader.readTextFileFromResource(mContext, R.raw.basic_vertex_shader),
                TextResourceReader.readTextFileFromResource(mContext, R.raw.multi_fragment_shader)
        );

        imageId = TextureHelper.loadBitmapWithDrawable(mContext, R.drawable.image);
        multiFilterId = TextureHelper.loadBitmapFilter(mContext, R.drawable.filters);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        mWidth = width;
        mHeight = height;

        fbo = new FBO(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        runAll(before);

        glBindFramebuffer(GL_FRAMEBUFFER, fbo.getFboId());
        glUseProgram(multiProgram.getProgramId());
        multiProgram.setTextureUnit(imageId);
        multiProgram.setFilterUnit(multiFilterId);
        multiProgram.setTarget(mTarget);
        multiProgram.setFlipMatrix(1);
        multiProgram.bindData();
        multiProgram.onDraw();
        glUseProgram(0);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glUseProgram(bProgram.getProgramId());
        bProgram.setTextureUnit(fbo.getTexId());
        bProgram.setFlipMatrix(0);
        bProgram.onDraw();
        glUseProgram(0);

        runAll(after);
    }

    private void runAll(Queue<Runnable> queue) {
        if (queue.size() > 0) {
            synchronized (queue) {
                while (!queue.isEmpty()) {
                    queue.poll().run();
                }
            }
        }
    }

    public void setImage(final Uri uri) {
        addBeforeQueue(new Runnable() {
            @Override
            public void run() {
                int[] temp = {imageId};
                glDeleteTextures(1, temp, 0);
                imageId = TextureHelper.loadBitmapWithUri(mContext, uri);
            }
        });
    }

    public void setFilterTarget(int target) {
        mTarget = target;
    }


    private void addBeforeQueue(final Runnable r) {
        synchronized (before) {
            before.add(r);
        }
    }

    public void addAfterQueue(final Runnable r) {
        synchronized (before) {
            after.add(r);
        }
    }
}


