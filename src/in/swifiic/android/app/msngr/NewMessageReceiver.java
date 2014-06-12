package in.swifiic.android.app.msngr;

import in.swifiic.android.app.lib.Helper;
import in.swifiic.android.app.lib.xml.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NewMessageReceiver extends BroadcastReceiver {

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
            	showNotification(msg, context);
            	LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        } else {
            Log.d(TAG, "Broadcast Receiver ignoring message - no notification found");
        }
	}
	
	public void showNotification(Msg msg, Context context){
		Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("New Message - " + msg.getUser())
		        .setContentText(msg.getMsg())
		        .setSound(sound);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.putExtra("userName", msg.getUser());

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int mId = 1;
		// mId allows you to update the notification later on.
		android.app.Notification notification = mBuilder.build();
		notification.flags = android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(mId , notification);
		Intent chatActivityIntent = new Intent("newMessageReceived");
		chatActivityIntent.putExtra("notification", mId);
		LocalBroadcastManager.getInstance(context).sendBroadcast(chatActivityIntent);
    }
}
