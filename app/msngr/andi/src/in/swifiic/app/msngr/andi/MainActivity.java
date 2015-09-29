package in.swifiic.app.msngr.andi;

import in.swifiic.app.msngr.andi.R;
import in.swifiic.plat.helper.andi.AppEndpointContext;
import in.swifiic.plat.helper.andi.Constants;
import in.swifiic.plat.helper.andi.Helper;
import in.swifiic.plat.helper.andi.ui.SwifiicActivity;
import in.swifiic.plat.helper.andi.xml.Action;

import java.util.Date;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.view.inputmethod.InputMethodManager;

public class MainActivity extends SwifiicActivity {
    
    private static final String TAG = "MainActivity";
	
    private EditText messageToSend = null;
    private ListView conversation = null;
    private ImageButton b = null;
    private AppEndpointContext aeCtx = new AppEndpointContext("Msngr", "0.1", "1");
    
   public static SharedPreferences pref=null;
    
    ConversationListCursorAdapter customAdapter;
    BroadcastReceiver mBroadcastReceiver;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);            	        // TODO check in sharedPreference if the generic code mentioned resetRequired
        // TODO if yes - reset AND clear the value of resetRequired
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref=PreferenceManager.getDefaultSharedPreferences(this);
        if(pref.getString("reset_required", "no").equalsIgnoreCase("yes")){
        	DatabaseHelper db = new DatabaseHelper(this);
        	db.deleteForReset();
        	db.close();
        	pref.edit().putString("reset_required", "no").commit();
        }
        setContentView(R.layout.main_activity_msngr);
        messageToSend = (EditText)findViewById(R.id.msgTextToSend);
	messageToSend.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(messageToSend, InputMethodManager.SHOW_IMPLICIT);
        conversation = (ListView)findViewById(R.id.conversation);
        b = (ImageButton)findViewById(R.id.buttonSendMsg);
        
        Intent i = getIntent();
        String userName = i.getStringExtra("userName");
        Drawable icon = Drawable.createFromPath(Constants.PUBLIC_DIR_PATH + userName + ".png");
        Log.d(TAG, "Got username: " + userName);
        final ActionBar actionBar = getActionBar();
        actionBar.setTitle(userName);
        actionBar.setIcon(icon);
        
        mBroadcastReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    		DatabaseHelper db = new DatabaseHelper(context);
	    		ActionBar actionBar = getActionBar();
	    		customAdapter.changeCursor(db.getMessagesForUser(actionBar.getTitle().toString()));
	    		db.closeDB();
	    		Log.d(TAG, "Cancelling notification");
	    		NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
	    		SystemClock.sleep(2000);
	    		nm.cancel(intent.getIntExtra("notificationId", 1));
	    	}
	    };
        
        IntentFilter filter = new IntentFilter("newMessageReceived");
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
		Log.d(TAG, "Broadcast Receiver registered.");
        
        // Assigning an action to the send button        
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	if (messageToSend.getText().toString().equals("")){
            		// Do nothing if the message is empty
            	} else {
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
                    
                    DatabaseHelper db = new DatabaseHelper(v.getContext());
                    db.addMessage(msg);
                    Log.d(TAG, "Inserted a row: " + msg.getMsg());
                    EditText msgInput = (EditText) findViewById(R.id.msgTextToSend);
                    msgInput.setText("");
                    customAdapter.changeCursor(db.getMessagesForUser(actionBar.getTitle().toString()));
                    db.closeDB();
            	}
            }
        });
    }
    public static void restart(Context context, int delay) {
        if (delay == 0) {
            delay = 1;
        }
        Log.e("", "restarting app");
        Intent restartIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName() );
        PendingIntent intent = PendingIntent.getActivity(
                context, 0,
                restartIntent, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
        System.exit(2);
    }
    
    public void onResume() {
        super.onResume();
      
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref=PreferenceManager.getDefaultSharedPreferences(this);
        if(pref.getString("reset_required", "no").equalsIgnoreCase("yes")){
        	DatabaseHelper db = new DatabaseHelper(this);
        	db.deleteForReset();
        	db.close();
        	pref.edit().putString("reset_required", "no").commit();
        	restart(this,0);
        	
        	
        }
      
      
        final ActionBar actionBar = getActionBar();
    	new Handler().post(new Runnable() {
        	@Override
            public void run() {
        		String user = actionBar.getTitle().toString();
        		DatabaseHelper db = new DatabaseHelper(MainActivity.this);
        		customAdapter = new ConversationListCursorAdapter(MainActivity.this, db.getMessagesForUser(user));
        		db.closeDB();
        		if(customAdapter==null) {
        			Log.e(TAG, "Custom adapter is null?!?1");
        		}
                conversation.setAdapter(customAdapter);
            }
    	});
        
        conversation.postDelayed(new Runnable() {
            @Override
            public void run() {
                conversation.setSelection(conversation.getCount());
                conversation.smoothScrollToPosition(conversation.getCount());
            }
        }, 100);}
    
    
    /*
     * Menu Creation and handling
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
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
    
    public void onDestroy() {
    	super.onDestroy();
    	LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);    	
    }
}
