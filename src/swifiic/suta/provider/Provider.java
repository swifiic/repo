package swifiic.suta.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

/***
 * 
 * @author abhishek
 * Note: 3rd Jan 2013 - This class needs significant rework - for now limiting it to 
 * Just provide list of users
 * 
 * Update to the schema will happen based on addition of users or addition of 
 *     apps / change of roles etc. It is expected that multicast blob will deliver this data
 */


// what we explose from Content provider is a query like
// swifiic.suta/users/<appName>/<user list>   - where <user list> entry has role, userId, user name, user alias
//   <appName> is variable

public class Provider extends ContentProvider {

	private static final String AUTHORITY = "swifiic.suta";
	private static final String USR_BASE_PATH = "users";
	private static final String APP_BASE_PATH = "apps";
	private static final int USERS = 10;
	public static Provider providerInstance=null;
	private DatabaseHelper dbHelper = null;

    //---for database use---
    private SQLiteDatabase sutaDB;
    private static final String DATABASE_NAME = "SUTA";
    private static final String DB_USR_TABLE = "Users";
    private static final String DB_APP_TABLE = "Apps";
    private static final String DB_MAP_TABLE = "UAMaps";
    
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE =
            "create table " + DB_USR_TABLE + 
            " (usr_id text primary key , name text not null, alias text not null);" +
            "create table " + DB_APP_TABLE +
            "(app_id text primary key, app_name text not null, role1 text, role2 text, role3 text);"+
            "create table " + DB_MAP_TABLE +
                    "(app_id text, role text not null, usr_id text not null);";
    
    		
    
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
            db.execSQL(DATABASE_CREATE);
            ContentValues v= new ContentValues();
            // refer PopulateTestData.txt
            v.put("user_id", "+919618258456"); v.put("name", "Operator");v.put("alias", "at");
            db.insert(DB_USR_TABLE, "", v);
            v.clear();
            v.put("user_id", "+919618258001"); v.put("name", "Test User");v.put("alias", "tu");
            db.insert(DB_USR_TABLE, "", v);
            v.clear();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
        int newVersion) {
            Log.w("Content provider database", 
                  "Upgrading database from version " + 
                  oldVersion + " to " + newVersion + 
                  ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DB_USR_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DB_APP_TABLE);
            // add devices and roles table as well
            
            onCreate(db);
        }
        
        protected void populateSchema(SQLiteDatabase db, String usrs, String apps){
        	
        }
    } 
    
    
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	  static {
	    sURIMatcher.addURI(AUTHORITY, APP_BASE_PATH + "/#", USERS);
	  }


	@Override
	public String getType(Uri arg0) {
		int uriType = sURIMatcher.match(arg0);
	    switch (uriType) {
	    case USERS:
	    	return "vnd.android.cursor.dir/";
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + arg0);
	    }
	}


	@Override
	public boolean onCreate() {
	    Context context = getContext();
        dbHelper = new DatabaseHelper(context);
        sutaDB = dbHelper.getWritableDatabase();
        providerInstance = this;
        return (sutaDB == null)? false:true;
	}

	public void loadSchema(String userSchema, String appSchema){
		dbHelper.populateSchema(sutaDB, userSchema, appSchema);
	}
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		int uriType = sURIMatcher.match(uri);
		if(USERS == uriType || USER_ID==uriType) {
			sqlBuilder.setTables(DB_USR_TABLE);
			if(USER_ID==uriType)
				sqlBuilder.appendWhere("user_id = " + uri.getPathSegments().get(1));
			if (sortOrder==null || sortOrder=="")
	            sortOrder = "alias";
			Cursor c = sqlBuilder.query(
	                sutaDB, 
	                projection, 
	                selection, 
	                selectionArgs, 
	                null, 
	                null, 
	                sortOrder);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		}
        return null;
	}

	// Updated / Deletes  - not needed - Internally the schema is dropped an recreated
	// as triggered from MainActivity so don't override delete, insert, update 
	//   unluckily update is abstract in base class 
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {return 0;}
	public int delete(Uri arg0, String arg1, String[] arg2) { return 0;	}
	public Uri insert(Uri arg0, ContentValues arg1) {return null;}

}