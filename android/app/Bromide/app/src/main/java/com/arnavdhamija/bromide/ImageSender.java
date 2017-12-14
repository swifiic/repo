package com.arnavdhamija.bromide;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import in.swifiic.plat.helper.andi.AppEndpointContext;
import in.swifiic.plat.helper.andi.Constants;
import in.swifiic.plat.helper.andi.Helper;
import in.swifiic.plat.helper.andi.ui.SwifiicActivity;
import in.swifiic.plat.helper.andi.xml.Action;

import java.util.Date;

/**
 * Created by nic on 25/10/17.
 */

public class ImageSender extends AsyncTask<String, Void, Void> {
    Context context;
    boolean bigMessage;

    ImageSender(Context c, boolean bigMsg) {
        context = c;
        bigMessage = bigMsg;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String encodedImage = strings[0];
        if (null != encodedImage) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String hubAddress = sharedPreferences.getString("hub_address", "");
            String fromUser = sharedPreferences.getString("my_identity", "");

            Date date = new Date();
            String epochDelta = String.valueOf(date.getTime()); // fancy way of saying millisec since Unix epoch

            Action action = new Action("SendBromideImage", new AppEndpointContext("Bromide", "0.1", "55")); //2do - how to decide appid?
            action.addArgument("encodedImage", encodedImage);
            action.addArgument("fromUser", fromUser);
            action.addArgument("toUser", hubAddress);
            action.addArgument("sentAt", epochDelta);
            if (bigMessage) {
                Helper.sendBigMessage(action, hubAddress + "/Bromide", context);
            } else {
                Helper.sendAction(action, hubAddress + "/Bromide", context);
            }
        }
        return  null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context, "Image sent!", Toast.LENGTH_LONG).show();
    }
}
