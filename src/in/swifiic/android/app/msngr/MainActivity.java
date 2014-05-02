package in.swifiic.android.app.msngr;

import in.swifiic.android.app.lib.AppEndpointContext;
import in.swifiic.android.app.lib.Helper;
import in.swifiic.android.app.lib.ui.SwifiicActivity;
import in.swifiic.android.app.lib.xml.Action;
import in.swifiic.android.app.lib.xml.Notification;

import java.util.Date;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class MainActivity extends SwifiicActivity {
    
    private static final String TAG = "MainActivity";
	
    private EditText messageToSend = null;
    private ListView conversation = null;
    private ImageButton b = null;
    
    private AppEndpointContext aeCtx = new AppEndpointContext("Msngr", "0.1", "1");
    
    DatabaseHelper db;
    ConversationListCursorAdapter customAdapter;
    
    /**
     * Mandatory implementation to receive swifiic notifications
     */
    public MainActivity() {
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
        };      
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.settings) {
			Intent selectedSettings = new Intent(this, SettingsActivity.class);
			startActivity(selectedSettings);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }
 
    /** 
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_msngr);
        
        messageToSend = (EditText)findViewById(R.id.msgTextToSend);
        conversation = (ListView)findViewById(R.id.conversation);
        b = (ImageButton)findViewById(R.id.buttonSendMsg);
        
        Intent i = getIntent();
        String userName = i.getStringExtra("userName");
        final ActionBar actionBar = getActionBar();
        actionBar.setTitle(userName);
        
        
        // Assigning an action to the send button        
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	if (messageToSend.getText().toString().equals("")){
            		// Do nothing if the message is empty
            	} 
            	else {
            		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(v.getContext());
            		String message = messageToSend.getText().toString();
            		String toUser = actionBar.getTitle().toString();
            		String fromUser = sharedPref.getString("my_identity", "UnknownUser");
            		Date date = new Date();
            		String sentAt = "" + date.getTime();
                    
            		Action act = new Action("SendMessage", aeCtx);
                    act.addArgument("message", message);
                    act.addArgument("toUser", toUser);
                    act.addArgument("fromUser", fromUser);
                    act.addArgument("sentAt", "" + sentAt);   
                    
                    // Loading hub address from preferences
                    String hubAddress = sharedPref.getString("hub_address", "");
                    
                    Helper.sendAction(act, hubAddress + "/Msngr", v.getContext());
                    
                    Msg msg = new Msg();
                    msg.setMsg(message);
                    msg.setUser(toUser);
                    msg.setIsInbound(0);
                    msg.setSentAtTime(sentAt);
                    
                    db = new DatabaseHelper(v.getContext());
                    db.addMessage(msg);
                    Log.d(TAG, "Inserted a row: " + msg.getMsg());
                    db.closeDB();
                    EditText msgInput = (EditText) findViewById(R.id.msgTextToSend);
                    msgInput.setText("");
                    customAdapter.changeCursor(db.getMessagesForUser(actionBar.getTitle().toString()));
                    conversation.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            conversation.setSelection(conversation.getCount());
                            conversation.smoothScrollToPosition(conversation.getCount());
                        }
                    }, 100);
            	}
            }
        });
    }

    public void onResume() {
        super.onResume();
                
        final ActionBar actionBar = getActionBar();
      
    	final DatabaseHelper dbh = new DatabaseHelper(this);
    	new Handler().post(new Runnable() {
        	@Override
            public void run() {
        		customAdapter = new ConversationListCursorAdapter(MainActivity.this, dbh.getMessagesForUser(actionBar.getTitle().toString()));
                conversation.setAdapter(customAdapter);
            }
    	});
        
        conversation.postDelayed(new Runnable() {
            @Override
            public void run() {
                conversation.setSelection(conversation.getCount());
                conversation.smoothScrollToPosition(conversation.getCount());
            }
        }, 100);
    }
}
