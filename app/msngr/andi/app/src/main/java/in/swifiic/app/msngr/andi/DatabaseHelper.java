package in.swifiic.app.msngr.andi;

import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	 
    // Logcat tag
	public static DatabaseHelper dbHelper=null;
    private static final String TAG = "DatabaseHelper";
 
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "Msngr";
 
    // Table Names
    private static final String TABLE_MSGS = "messages";
 
    // Column names
    private static final String KEY_ID = "_id";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_USER = "user";
    private static final String KEY_IS_INBOUND = "isInbound";	// This key is: 1 for incoming message 0 for outgoing message    
    private static final String KEY_SENTAT = "sentAt";
  
    // Table Create Statement
    private static final String CREATE_TABLE_MSGS = "CREATE TABLE " + TABLE_MSGS + "(" 
    		+ KEY_ID + " INTEGER PRIMARY KEY, " 
    		+ KEY_MESSAGE + " TEXT, "
    		+ KEY_USER + " TEXT, "
            + KEY_IS_INBOUND + " INTEGER, " 
            + KEY_SENTAT + " TEXT)";
 
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_MSGS);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MSGS);
 
        // Create new tables
        onCreate(db);
    }
    
    protected void populateSampleMessages(SQLiteDatabase db) {
    	Log.d("PopulateMessages", "Populating table: " + TABLE_MSGS);
    	ContentValues v = new ContentValues();
        Date date = new Date();
        v.put(KEY_MESSAGE, "Sample message from abhishek to shivam");
        v.put(KEY_USER, "abhishek");
        v.put(KEY_IS_INBOUND, 1);
        v.put(KEY_SENTAT, date.getTime());
        db.insert(TABLE_MSGS, null, v);
        
        v = new ContentValues();
        v.put(KEY_MESSAGE, "Sample message from shivam to abhishek");
        v.put(KEY_USER, "test");
        v.put(KEY_IS_INBOUND, 0);
        v.put(KEY_SENTAT, date.getTime() + 1000);
        db.insert(TABLE_MSGS, null, v);
        
        v = new ContentValues();
        v.put(KEY_MESSAGE, "Sample message from shivam to abhishek");
        v.put(KEY_USER, "abhishek");
        v.put(KEY_IS_INBOUND, 0);
        v.put(KEY_SENTAT, date.getTime() + 1000);
        db.insert(TABLE_MSGS, null, v);
        
        v = new ContentValues();
        v.put(KEY_MESSAGE, "Sample message from shivam to abhishek");
        v.put(KEY_USER, "test");
        v.put(KEY_IS_INBOUND, 0);
        v.put(KEY_SENTAT, date.getTime() + 1000);
        db.insert(TABLE_MSGS, null, v);
    }
    
    // THE "CRUD"
    
    /*
     * Adding a message
     */
    public long addMessage(Msg msg) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE, msg.getMsg());
        values.put(KEY_USER, msg.getUser());
        values.put(KEY_IS_INBOUND, msg.getIsInbound());
        values.put(KEY_SENTAT, msg.getSentAtTime());
     
        // Insert row
        long msg_id = db.insert(TABLE_MSGS, null, values);
        if(msg_id==-1) {
    		Log.e(TAG, "Error inserting row!");
    	}
        db.close();
        Log.d(TAG, "Done adding message, message id: " + msg_id);
        return msg_id;
    }
    
    /*
     * Get messages for a particular contact
     */
    public Cursor getMessagesForUser(String userName) {        
        Log.d(TAG, "Getting messages for user: " + userName);
   	 
    	String selectQuery = "SELECT * FROM " + TABLE_MSGS + " WHERE "
    			+ KEY_USER + "=\'" + userName + "\' " 
    			+ "ORDER BY " + KEY_SENTAT;
     
        Log.d(TAG, selectQuery);
     
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        Log.d(TAG, "Got" + c.getCount() + " messages.");
        db.close();
        return c;
    }
    
    public void deleteAll() {    	
    	SQLiteDatabase db = this.getWritableDatabase();
    	Log.d(TAG, "Dropping table: " + TABLE_MSGS);
    	db.execSQL("DROP TABLE IF EXISTS " + TABLE_MSGS);
    	db.execSQL(CREATE_TABLE_MSGS);
    }
    
    // Closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

	public Cursor getFirstMessageForAllUsers() {
		SQLiteDatabase db = this.getReadableDatabase();
		String[] projection = {"_id","user", "message", "sentAt"};
		Cursor c = db.query("messages", projection, null, null, "user", null, "sentAt");
		DatabaseUtils.dumpCursor(c);
		String temp = "";
		String user, message, sentAt;
		int id;
		MatrixCursor mc = new MatrixCursor(projection);
		while(c.moveToNext()) {
			if(temp.equals(c.getString(c.getColumnIndex("user")))) {
				continue;
			}
			id = c.getInt(c.getColumnIndex("_id"));
			user = c.getString(c.getColumnIndex("user"));
			message = c.getString(c.getColumnIndex("message"));
			sentAt = c.getString(c.getColumnIndex("sentAt"));
			mc.addRow(new Object[]{id, user, message, sentAt});
			temp = c.getString(c.getColumnIndex("user"));		
		}
		return mc;
	}

	public void deleteMessagesForUserIds(List<String> selectedItems) {
		int size = selectedItems.size();
		for(int i=size-1; i >= 0; --i) {
			String query = "DELETE FROM " + TABLE_MSGS + " WHERE " + KEY_USER + "=\'" + selectedItems.get(i) + "\'";
			Log.d(TAG, "Deleting messages from user: " + selectedItems.get(i));
			SQLiteDatabase db = getWritableDatabase();
			db.execSQL(query);
			db.close();
		}
		
	}
	public void deleteForReset()
	{
		SQLiteDatabase db = getWritableDatabase();
		String query="DELETE FROM " + TABLE_MSGS + " WHERE 1=1";
		db.execSQL(query);
	}
	
}