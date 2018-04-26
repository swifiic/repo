package com.arnavdhamija.bromide;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import in.swifiic.plat.helper.andi.ui.SwifiicActivity;

public class MainActivity extends SwifiicActivity {

    final static int PICK_IMAGE = 1; // required for getting the result from the image picker intent
    boolean payloadReady = false;

    final String compressedFilename = "compressedImage";
    final String highResFilename = "highResImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Button bromideButton = (Button) findViewById(R.id.bromide_button);
        Button sendCompressedButton = (Button) findViewById(R.id.compressed_button);
        Button sendHighResButton = (Button) findViewById(R.id.highres_button);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String hubAddress = sharedPreferences.getString("hub_address", "");

        toolbar.setTitle("Bromide");

        // Setup callbacks for UI elements
        bromideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Image chooser Intent gets fired here
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        sendCompressedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String base64img = readFile(compressedFilename);
                    ImageSender imageSender = new ImageSender(v.getContext(), false);
                    imageSender.execute(base64img);
                } catch (Exception e) {
                    Log.e("BROMIDE", "Could not send image!");
                }
            }
        });

        sendHighResButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("BROMIDE", "HDIMG");
                    String base64img = readFile(highResFilename);
                    ImageSender imageSender = new ImageSender(v.getContext(), true);
                    imageSender.execute(base64img);
                } catch (Exception e) {
                    Log.e("BROMIDE", "Could not send image!");
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, hubAddress, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


    }

    // Gets called after the Image is chosen from the Intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri result = data.getData();
                Log.d("BROMIDE", result.toString());
                ImageView displayImage = (ImageView) findViewById(R.id.displayImage);
                // We use the Glide library for loading the image into the ImageView
                Glide.with(this).load(result).into(displayImage);
                // Base 64 encode both images and write to their respective files
                ImageEncoder imageEncoder = new ImageEncoder(this, compressedFilename, highResFilename);
                imageEncoder.execute(result);
            }
        }
    }

    private String readFile(String fileName) {
        try {
            File file = getFileStreamPath(fileName);
            byte[] bytes = new byte[(int)file.length()];

            InputStream inputStream = new FileInputStream(file);
            try {
                inputStream.read(bytes);
            } finally {
                inputStream.close();
            }
            String enc = new String(bytes);
            return enc;
        } catch (Exception e) {
            Log.e("BROMIDE", "Could not send image!" + e.getMessage());
            return null;
        }
    }
}