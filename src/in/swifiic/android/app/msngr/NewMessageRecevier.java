package in.swifiic.android.app.msngr;

import in.swifiic.android.app.lib.Helper;
import in.swifiic.android.app.lib.xml.Notification;
import android.app.ActionBar;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NewMessageRecevier extends BroadcastReceiver {

	private static final String TAG = "NewMessageReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.hasExtra("notification")) {
        	String payload= intent.getStringExtra("notification");
        	
            Log.d(TAG, "Handling incoming message: " + payload);
            Notification notif = Helper.parseNotification(payload);
        	// Checking for opName of Notification
            if(notif.getNotificationName().equals("DeliverMessage")) {
            	Log.d(TAG, "Adding received message to the database.");
            	Msg msg = new Msg(notif);
            	DatabaseHelper db = new DatabaseHelper(context);
            	db.addMessage(msg);
            	db.closeDB();
            	Log.d(TAG, "Showing notification now...");
            	showNotification(msg);
            	ActionBar actionBar = getActionBar();
            	customAdapter.changeCursor(db.getMessagesForUser(actionBar.getTitle().toString()));
                conversation.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        conversation.setSelection(conversation.getCount());
                        conversation.smoothScrollToPosition(conversation.getCount());
                    }
                }, 100);
            }
        } else {
            Log.d(TAG, "Broadcast Receiver ignoring message - no notification found");
        }
	}
	
	public void showNotification(Msg msg){

        // define sound URI, the sound to be played when there's a notification
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // intent triggered, you can add other intent for other actions
        Intent intent = new Intent(MainActivity.this, NotificationCompat.class);
        PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        android.app.Notification mNotification = new NotificationCompat.Builder(this)
            .setContentTitle("New Message!")
            .setContentText(msg.getUser() + ": " + msg.getMsg())
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pIntent)
            .setSound(soundUri)
            .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // If you want to hide the notification after it was selected, do the code below
        mNotification.flags |= android.app.Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, mNotification);
    }

}
