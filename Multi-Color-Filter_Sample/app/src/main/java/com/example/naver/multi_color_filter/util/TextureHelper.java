package com.example.naver.multi_color_filter.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;

/**
 * Created by NAVER on 2017. 8. 22..
 */

public class TextureHelper {
    private static final BitmapFactory.Options options;
    private static final BitmapFactory.Options optionsForHigh;

    static {
        options = new BitmapFactory.Options();
        options.inScaled = false;

        optionsForHigh = new BitmapFactory.Options();
        optionsForHigh.inScaled = false;
    }

    public static int loadBitmapWithDrawable(Context c, int resId) {
        int textureObjectId = getTextureObjectId();

        BitmapFactory.Options optionsForScale = getBitmapOptionWithDrawable(c, resId);

        int width = optionsForScale.outWidth;
        int height = optionsForScale.outHeight;
        int sampleSize = 1;
        if (width > GL_MAX_TEXTURE_SIZE || height > GL_MAX_TEXTURE_SIZE) {
            sampleSize = (int) Math.pow(2, (int) Math.round(Math.log(GL_MAX_TEXTURE_SIZE / (double) Math.max(height, width)) / Math.log(0.5)));
            optionsForHigh.inSampleSize = sampleSize;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(c.getResources(), resId, optionsForHigh);
        Bitmap resizedBitmap = getResizedBitmap(bitmap);

        return textureOperation(resizedBitmap, textureObjectId);
    }

    public static int loadBitmapWithUri(Context c, Uri uri) {
        int textureObjectId = getTextureObjectId();
        InputStream is = null;
        try {
            is = c.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ExifInterface exif = getExifWithUri(c, uri);

        int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
        int height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
        int sampleSize = 1;
        if (width > GL_MAX_TEXTURE_SIZE || height > GL_MAX_TEXTURE_SIZE) {
            sampleSize = (int) Math.pow(2, (int) Math.round(Math.log(GL_MAX_TEXTURE_SIZE / (double) Math.max(height, width)) / Math.log(0.5)));
            optionsForHigh.inSampleSize = sampleSize;
        }

        Bitmap bitmap = BitmapFactory.decodeStream(is, null, optionsForHigh);
        Bitmap resizedBitmap = getResizedBitmap(bitmap);
        return textureOperation(resizedBitmap, textureObjectId);
    }


    public static int loadBitmapFilter(Context c, int resId) {
        int textureObjectId = getTextureObjectId();

        Bitmap bitmap = BitmapFactory.decodeResource(c.getResources(), resId, options);

        return textureOperation(bitmap, textureObjectId);
    }

    private static int textureOperation(Bitmap bitmap, int textureObjectId) {
        glBindTexture(GL_TEXTURE_2D, textureObjectId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();

        glBindTexture(GL_TEXTURE_2D, 0);
        return textureObjectId;
    }

    private static int getTextureObjectId() {
        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0);  // Q. memory issue
        return textureObjectIds[0];
    }

    private static Bitmap getResizedBitmap(Bitmap bitmap) {
        Bitmap resized;

        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();
        if (bWidth <= bHeight)
            resized = Bitmap.createScaledBitmap(bitmap, bWidth * GL_MAX_TEXTURE_SIZE / bHeight, GL_MAX_TEXTURE_SIZE, true);
        else
            resized = Bitmap.createScaledBitmap(bitmap, GL_MAX_TEXTURE_SIZE, bHeight * GL_MAX_TEXTURE_SIZE / bWidth, true);

        bitmap.recycle();

        return resized;
    }

    private static BitmapFactory.Options getBitmapOptionWithDrawable(Context c, int resId) {
        BitmapFactory.Options bOptions = new BitmapFactory.Options();
        bOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(c.getResources(), resId, bOptions);
        return bOptions;
    }

    private static ExifInterface getExifWithUri(Context c, Uri uri) {
        ExifInterface exif = null;
        String path = getPathFromUri(c, uri);
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exif;
    }

    private static String getPathFromUri(Context c, Uri uri) {
        Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        cursor.close();

        return path;
    }

}
