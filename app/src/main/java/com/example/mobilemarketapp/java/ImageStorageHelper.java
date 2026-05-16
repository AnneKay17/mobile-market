package com.example.mobilemarketapp.java;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageStorageHelper {

    // Copies image from gallery into internal storage
    public static String saveImageToInternalStorage(Context context, Uri uri) {

        try {

            InputStream inputStream =
                    context.getContentResolver().openInputStream(uri);

            File file = new File(
                    context.getFilesDir(),
                    "img_" + System.currentTimeMillis() + ".jpg"
            );

            FileOutputStream outputStream =
                    new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}