package com.example.naver.multi_color_filter;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.naver.multi_color_filter.gl.MultiRenderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    public static final int REQ_CODE_IMAGE = 100;
    private static final String[] MENU = {"사진 선택", "필터 선택", "저장"};
    private static final String[] FILTERS = {
            "Original", "Clear", "Brilliant", "Cream", "Delight", "Blossom", "Warm", "Lollipop", "Candy",
            "Mint", "Cool", "Cobalt", "Classic", "Vintage", "Urban", "Chic", "Blank", "Tint-P", "Tint-Y"
    };

    private GLSurfaceView mGLSurfaceView;
    private MultiRenderer mRenderer;

    private Dialog mChoiceSelectDialog, mFilterSelectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        makeDialog();
        initGL();

        setContentView(mGLSurfaceView);
    }

    private void initGL() {
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mRenderer = new MultiRenderer(this);
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mGLSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mChoiceSelectDialog.show();
                }
                return false;
            }
        });
    }

    private void makeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("메뉴")
                .setNegativeButton("취소", null)
                .setItems(MENU, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent imageIntent = new Intent(Intent.ACTION_PICK);
                                imageIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                                imageIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(imageIntent, REQ_CODE_IMAGE);
                                break;
                            case 1:
                                mFilterSelectDialog.show();
                                break;
                            case 2:
                                String fileName = System.currentTimeMillis() + ".jpg";
                                saveImage("MultiColorFilter", fileName, new OnPictureSavedListener() {
                                    @Override
                                    public void onPictureSaved(final String path) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), path + "saved.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                                break;
                        }
                    }
                });
        mChoiceSelectDialog = builder.create();

        builder = new AlertDialog.Builder(this);
        builder
                .setTitle("필터선택")
                .setNegativeButton("취소", null)
                .setItems(FILTERS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRenderer.setFilterTarget(which);
                        mGLSurfaceView.requestRender();
                    }
                });
        mFilterSelectDialog = builder.create();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                mRenderer.setImage(uri);
                mGLSurfaceView.requestRender();
            }
        }
    }

    public Bitmap capture() throws InterruptedException {
        final Semaphore waiter = new Semaphore(0);

        final int width = mGLSurfaceView.getMeasuredWidth();
        final int height = mGLSurfaceView.getMeasuredHeight();

        // Take picture on OpenGL thread
        final int[] pixelMirroredArray = new int[width * height];
        mRenderer.addAfterQueue(new Runnable() {
            @Override
            public void run() {
                final IntBuffer pixelBuffer = IntBuffer.allocate(width * height);
                GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
                int[] pixelArray = pixelBuffer.array();

                // Convert upside down mirror-reversed image to right-side up normal image.
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        pixelMirroredArray[(height - i - 1) * width + j] = pixelArray[i * width + j];
                    }
                }
                waiter.release();
            }
        });
        mGLSurfaceView.requestRender();
        waiter.acquire();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixelMirroredArray));
        return bitmap;
    }

    public void saveImage(final String folderName, final String fileName, final OnPictureSavedListener listener) {
        new SaveTask(folderName, fileName, listener).execute();
    }

    private class SaveTask extends AsyncTask<Void, Void, Void> {
        private final String mFolderName;
        private final String mFileName;
        private final OnPictureSavedListener mListener;

        public SaveTask(final String folderName, final String fileName, final OnPictureSavedListener listener) {
            mFolderName = folderName;
            mFileName = fileName;
            mListener = listener;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                Bitmap result = capture();
                saveImage(mFolderName, mFileName, result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void saveImage(final String folderName, final String fileName, final Bitmap image) {
            File path = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(path, folderName + "/" + fileName);
            try {
                file.getParentFile().mkdirs();
                image.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(file));
                MediaScannerConnection.scanFile(getApplicationContext(),
                        new String[]{
                                file.toString()
                        }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(final String path, final Uri uri) {
                                if (mListener != null) {
                                    mListener.onPictureSaved(path);
                                }
                            }
                        });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnPictureSavedListener {
        void onPictureSaved(String path);
    }
}
