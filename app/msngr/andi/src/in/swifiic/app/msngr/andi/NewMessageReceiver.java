package in.swifiic.app.msngr.andi;

import in.swifiic.app.msngr.andi.R;
import in.swifiic.plat.helper.andi.Helper;
import in.swifiic.plat.helper.andi.xml.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
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
            	Log.d(TAG, "Returned from notification");
            }
        } else {
            Log.d(TAG, "Broadcast Receiver ignoring message - no notification found");
        }
	}
	
	public void showNotification(Msg msg, Context context) {
		Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Intent notificationIntent = new Intent(context, MainActivity.class);
		notificationIntent.putExtra("userName", msg.getUser());
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setContentTitle("New Message - " + msg.getUser())
		        .setContentText(msg.getMsg())
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentIntent(contentIntent)
		        .setSound(sound);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int mId = 1;
		// mId allows you to update the notification later on.
		android.app.Notification notification = mBuilder.build();
		notification.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(mId , notification);
		Log.d(TAG, "Done showing notification, sending broadcast to update conversation view.");
		Intent chatActivityIntent = new Intent("newMessageReceived");
		chatActivityIntent.putExtra("notificationId", mId);
		LocalBroadcastManager.getInstance(context).sendBroadcast(chatActivityIntent);
		return;
    }
}
