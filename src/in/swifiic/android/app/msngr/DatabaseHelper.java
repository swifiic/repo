package in.swifiic.android.app.msngr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	 
    // Logcat tag
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
    private static final String KEY_FROM = "fromUser";
    private static final String KEY_TO = "toUser";
    private static final String KEY_SENTAT = "sentat";
  
    // Table Create Statement
    private static final String CREATE_TABLE_MSGS = "CREATE TABLE " + TABLE_MSGS + "(" 
    		+ KEY_ID + " INTEGER PRIMARY KEY," 
    		+ KEY_MESSAGE + " TEXT," 
            + KEY_FROM + " TEXT," 
    		+ KEY_TO + " TEXT," 
            + KEY_SENTAT + " TEXT" + ")";
 
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_MSGS);
        populateSampleMessages(db);
    }
    
    protected void populateSampleMessages(SQLiteDatabase db) {
    	Log.d("PopulateMessages", "Populating table: " + TABLE_MSGS);
    	ContentValues v = new ContentValues();
        Date date = new Date();
        v.put(KEY_MESSAGE, "Sample message from abhishek to shivam");
        v.put(KEY_FROM, "abhishek");
        v.put(KEY_TO, "shivam");
        //v.put(KEY_ID, "1");
        v.put(KEY_SENTAT, date.getTime());
        db.insert(TABLE_MSGS, null, v);
        
        v = new ContentValues();
        v.put(KEY_MESSAGE, "Sample message from shivam to abhishek");
        v.put(KEY_FROM, "shivam");
        v.put(KEY_TO, "abhishek");
        //v.put(KEY_ID, "2");
        v.put(KEY_SENTAT, date.getTime());
        db.insert(TABLE_MSGS, null, v);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MSGS);
 
        // create new tables
        onCreate(db);
    }
    
    /*
     * Adding a message
     */
    public long addMessage(Msg msg) {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE, msg.getMsg());
        values.put(KEY_FROM, msg.getFrom());
        values.put(KEY_TO, msg.getTo());
        values.put(KEY_SENTAT, msg.getSentAtTime());
     
        // insert row
        long msg_id = db.insert(TABLE_MSGS, null, values);
     
        return msg_id;
    }
    
    /*
     * Get messages for a particular contact
     */
    public Cursor getMessagesForUser(String userName) {
    	Log.d(TAG, "Getting messages for user: " + userName);
    	 
    	String selectQuery = "SELECT * FROM " + TABLE_MSGS + " WHERE "
    			+ KEY_FROM + "=\'" + userName + "\'" 
    			+ " OR " 
    			+ KEY_TO + "=\'" + userName + "\' ORDER BY " + KEY_SENTAT;
     
        Log.d(TAG, selectQuery);
     
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        return c;
    }
    
    public void deleteAll() {    	
    	SQLiteDatabase db = this.getWritableDatabase();
    	Log.d("DeleteAllMessage", "Dropping table: " + TABLE_MSGS);
    	db.execSQL("DROP TABLE IF EXISTS " + TABLE_MSGS);
    	db.execSQL(CREATE_TABLE_MSGS);
    }
    
    // Closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}