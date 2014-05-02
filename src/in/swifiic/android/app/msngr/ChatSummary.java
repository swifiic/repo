package in.swifiic.android.app.msngr;

import in.swifiic.android.app.lib.Helper;
import in.swifiic.android.app.lib.ui.SwifiicActivity;
import in.swifiic.android.app.lib.ui.UserChooserActivity;
import in.swifiic.android.app.lib.xml.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ChatSummary extends SwifiicActivity {
	
	private static final int SELECT_USER = 1;
	private String TAG = "ChatSummary";
	
	private DatabaseHelper db;
	
	/**
     * Mandatory implementation to receive swifiic notifications
     */
    public ChatSummary() {
    	super();
    	
    	// This is a must for all applications - hook to get notification from GenericService
    	mDataReceiver = new BroadcastReceiver() {
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
                    	db = new DatabaseHelper(getApplicationContext());
                    	db.addMessage(msg);
                    	db.closeDB();
                    	Log.d(TAG, "Showing notification now...");
                    	showNotification(msg);
                    }
                } else {
                    Log.d(TAG, "Broadcast Receiver ignoring message - no notification found");
                }
            }
        };      
    }
    
    public void showNotification(Msg msg){

        // define sound URI, the sound to be played when there's a notification
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // intent triggered, you can add other intent for other actions
        Intent intent = new Intent(ChatSummary.this, NotificationCompat.class);
        PendingIntent pIntent = PendingIntent.getActivity(ChatSummary.this, 0, intent, 0);

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_summary);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		DatabaseHelper db = new DatabaseHelper(this);
		Cursor c = db.getFirstMessageForAllUsers();
		ChatSummaryCursorAdapter adapter = new ChatSummaryCursorAdapter(this, c);
		DatabaseUtils.dumpCursor(c);
		Log.d("ChatSummary", "Setting cursor!");
		c.moveToFirst();
		adapter.changeCursor(c);
		ListView chatList = (ListView) findViewById(R.id.list);
		chatList.setAdapter(adapter);
		chatList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView userNameView = (TextView) view.findViewById(R.id.firstLine);
				String userName = userNameView.getText().toString();
				Intent i = new Intent(parent.getContext(), MainActivity.class);
				i.putExtra("userName", userName);
				startActivity(i);
			}
		});
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_summary, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.itemSelectUser) {
			Intent selectNeighbor = new Intent(this, UserChooserActivity.class);
			startActivityForResult(selectNeighbor, SELECT_USER);
			return true;
		} else if (itemId == R.id.settings) {
			Intent selectedSettings = new Intent(this, SettingsActivity.class);
			startActivity(selectedSettings);
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
    }
    
    /**
     * Called when activity exits from "startActivityForResult"
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SELECT_USER == requestCode) {
            if ((data != null)){
            	String userName = "";
            	if (data.hasExtra("userName")) {
            		userName = data.getStringExtra("userName");
            	}
            	Intent i = new Intent(this, MainActivity.class);
            	i.putExtra("userName", userName);
            	startActivity(i);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
