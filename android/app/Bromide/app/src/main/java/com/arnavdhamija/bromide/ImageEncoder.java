package com.arnavdhamija.bromide;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import id.zelory.compressor.Compressor;

/**
 * Created by arnavdhamija on 25/10/17.
 */


// Class for Base64 encoding the image and saving the result to a file in an AsyncTask

class ImageEncoder extends AsyncTask<Uri, Void, Void> {
    Activity activity;
    String resultString;
    final int BITMAP_MAX_DIMENSION = 640;

    String compressedFilename;
    String highResFilename;

    int selectedBytes;
    int resizedBytes;

    ImageEncoder(Activity act, String compressed, String highRes) {
        activity = act;
        compressedFilename = compressed;
        highResFilename = highRes;
    }

    @Override
    protected Void doInBackground(Uri... uris) {
        Uri uri = uris[0];
        try {
            InputStream imageStream = activity.getContentResolver().openInputStream(uri);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            imageStream.close();

            final float aspectRatio = (float) selectedImage.getWidth()/(float) selectedImage.getHeight();
            Bitmap resizedBitmap = null;
            // We resize the image for a compressed image by limiting the resolution to a max of 640x640
            if (aspectRatio > 1) {
                if (selectedImage.getWidth() > BITMAP_MAX_DIMENSION) {
                    int scaledHeight = Math.round(BITMAP_MAX_DIMENSION / aspectRatio);
                    resizedBitmap = Bitmap.createScaledBitmap(selectedImage, BITMAP_MAX_DIMENSION, scaledHeight, false);
                }
            } else {
                if (selectedImage.getHeight() > BITMAP_MAX_DIMENSION) {
                    int scaledWidth = Math.round(BITMAP_MAX_DIMENSION * aspectRatio);
                    resizedBitmap = Bitmap.createScaledBitmap(selectedImage, scaledWidth, BITMAP_MAX_DIMENSION, false);
                }
            }
            // Once we have encoded the images, we can write it to a file
            writeImageToFile(selectedImage, highResFilename, 90);
            writeImageToFile(resizedBitmap, compressedFilename, 60);
        } catch (IOException e) {
            Log.e("BROMIDE", "File not found");
        }
        return null;
    }

    private void writeImageToFile(Bitmap img, String filename, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.JPEG, quality, baos); //32KB
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        Log.i("BROMIDE", "SelectedImageBytes: " + img.getByteCount());

        if (filename == highResFilename) {
            selectedBytes = img.getByteCount();
        }
        if (filename == compressedFilename) {
            resizedBytes = b.length;
        }

        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = activity.openFileOutput(filename, activity.MODE_PRIVATE);
            fileOutputStream.write(encImage.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            Log.e("BROMIDE", "Image Save failed");
        }

    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        // Update the UI by un-hiding the Send buttons and displaying the compression ratio
        TextView selectedSizeText = (TextView) activity.findViewById(R.id.selectedSizeText);
        TextView compressedSizeText = (TextView) activity.findViewById(R.id.compressedSizeText);
        TextView compressionRatioText = (TextView) activity.findViewById(R.id.compressionRatioText);

        Button sendCompressedButton = (Button) activity.findViewById(R.id.compressed_button);
        Button sendHighResButton = (Button) activity.findViewById(R.id.highres_button);

        selectedSizeText.setText("Original Size: " + String.valueOf(selectedBytes));
        compressedSizeText.setText("Compressed Size: " + String.valueOf(resizedBytes));
        compressionRatioText.setText("Compression Ratio: " + String.valueOf((float) selectedBytes / resizedBytes));

        sendCompressedButton.setVisibility(View.VISIBLE);
        sendHighResButton.setVisibility(View.VISIBLE);
    }
}
