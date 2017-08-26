package in.swifiic.plat.app.suta.andi.provider;

import in.swifiic.plat.helper.andi.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import de.tubs.ibr.dtn.util.Base64;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/***
 * 
 * @author abhishek
 * Note: 3rd Jan r file generated 2013 - This class needs significant rework - for now limiting it to 
 * Just provide list of users
 * 
 * Update to the schema will happen based on addition of users or addition of 
 *     apps / change of roles etc. It is expected that multicast blob will deliver this data
 */


// what we expose from Content provider is a query like
// swifiic.suta/users/<appName>/<user list>   - where <user list> entry has role, userId, user name, user alias
// <appName> is variable

public class Provider extends ContentProvider {

	private static Context mContext;



	private static final String TAG = "Provider";

	private static final String AUTHORITY = "in.swifiic.plat.app.suta.andi";
	private static final String USER_BASE_PATH = "users";
	private static final int USERS = 10;
	public static Provider providerInstance = null;
	private DatabaseHelper dbHelper = null;

    //---for database use---
    private SQLiteDatabase sutaDB;
    private static final String DATABASE_NAME = "SUTA";
    private static final String DB_USR_TABLE = "users";
    private static final String DB_APP_TABLE = "apps";
    private static final String DB_MAP_TABLE = "uamaps";
    
    private static final int DATABASE_VERSION = 2;
    
    private static final String CREATE_TABLE_USER =
            "CREATE TABLE " + DB_USR_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                             + "user_id TEXT NOT NULL, "
            								 + "name TEXT NOT NULL, "
            								 + "alias TEXT NOT NULL UNIQUE); ";


    private static final String CREATE_TABLE_APP =
            "CREATE TABLE " + DB_APP_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            								 + "app_id TEXT NOT NULL, "
          									 + "app_name TEXT NOT NULL, "
          									 + "role1 TEXT, "
          									 + "role2 TEXT, "
          									 + "role3 TEXT); ";
    private static final String CREATE_TABLE_MAP = 
    		"CREATE TABLE " + DB_MAP_TABLE + " (app_id INTEGER NOT NULL, "
          									 + "role TEXT NOT NULL, "
          									 + "user_id INTEGER NOT NULL, "
          									 + "FOREIGN KEY (app_id) REFERENCES " + DB_APP_TABLE + "(_id), "
          									 + "FOREIGN KEY (user_id) REFERENCES " + DB_USR_TABLE + "(_id));";
   
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.d("onCreate Provider", "Creating table with: " + CREATE_TABLE_USER);
            db.execSQL(CREATE_TABLE_USER);

            Log.d("onCreate Provider", "Creating table with: " + CREATE_TABLE_APP);
            db.execSQL(CREATE_TABLE_APP);
            Log.d("onCreate Provider", "Creating table with: " + CREATE_TABLE_MAP);
            db.execSQL(CREATE_TABLE_MAP);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//Log.w("Content provider database", "Upgrading database from version " + oldVersion + " to " + newVersion +", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DB_USR_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DB_APP_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DB_MAP_TABLE);
            // TODO add devices and roles table as well
            
            onCreate(db);
        }
        
        // TODO FIXME
        @SuppressWarnings("unused")
		protected void populateSchema(SQLiteDatabase db, String usrs, String apps) {
        	// STUB
        }
    } // class DatabaseHelper
    
    
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
	    sURIMatcher.addURI(AUTHORITY, USER_BASE_PATH + "/*", USERS);
	}


	@Override
	public String getType(Uri arg0) {
		int uriType = sURIMatcher.match(arg0);
	    
		switch (uriType) {
	    case USERS:
	    	return "vnd.android.cursor.dir/in.swifiic.plat.app.suta.andi.users";
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
		mContext = context;
        return (sutaDB == null)? false: true;
	}

	// not loading just storing user details to local data base
	// userSchema format - "username|alias;username|alias;..."
	public void loadUserSchema(String userSchema) {
		sutaDB.execSQL("DELETE FROM users WHERE 1=1");
		String userInfo, username, alias, imageEncoded64;
		String dirPath = Constants.PUBLIC_DIR_PATH;
		File directory = new File(dirPath);
		directory.mkdirs();
		byte[] imageBytes = null;
		ContentValues v = new ContentValues();
		int i = 1;
		StringTokenizer st = new StringTokenizer(userSchema, ";");
		while(st.hasMoreTokens()) {
			userInfo = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(userInfo, "|");
		     while(st2.hasMoreTokens()) {
		    	 username = st2.nextToken();
		    	 alias = st2.nextToken();
		    	 imageEncoded64 = st2.nextToken();
		    	 File file = new File(dirPath, username + ".png");
		    	 
		    	 try {
		    		 imageBytes = Base64.decode(imageEncoded64);
		    		 Log.d(TAG, "Saving profile pic to: " + file.toString());
		    		 OutputStream out = new FileOutputStream(file);
		    		 Bitmap bm = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
		    		 bm.compress(Bitmap.CompressFormat.PNG, 90, out);
		    		 out.flush();
		    		 out.close();
		    	 } catch (IOException e) {
		    		 e.printStackTrace();
		    	 }
		    	 v.put("user_id", i);
		         v.put("name", username);
		         v.put("alias", alias);
		         if(sutaDB.insert(DB_USR_TABLE, "", v) < 0) {
		        	 Log.e("SUTA Provider", "Unable to insert values: " + v.toString());
		         } else {
		        	 Log.d("SUTA Provider", "Successfully to inserted values: " + v.toString());
		         }
		         v.clear();
		         ++i;
		     }
		 }
	}


	// stores the account details to local database
	// Format of accountDetails = accountDetails1$accountDetails2$accountDetails3
	// Format of accountDetails1 = macAddress|sTimeOfLastUpdateFromApp|sLastHubValueSutaReports|sLastHubUpdateSutaGotAT|remainingCredit|Details,Ammount:Details,Ammount:
	// transactionDetails = Details,Ammount:Details,Ammount:
	// revisedTransactionDetails = Details	Amount\nDetails	Amount
	public void storeAccountDetails(String accountdetails,String macAddress,String notifSentByHubAt,String notifRecievedBySutaAt){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = pref.edit();


		int flag = 0;
		String macAdd;
		String details;
		String amount;

		String sTimeOfLastUpdateFromApp = "";
		String sLastHubValueSutaReports = "";
		String sLastHubUpdateSutaGotAT = "";
		String remainingCredit="";
		String transactionDetails="";
		String revisedTransactionDetails="";
		String userInfo,singleTransaction;
		StringTokenizer st = new StringTokenizer(accountdetails, "$");
		while(st.hasMoreTokens()) {
			userInfo = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(userInfo, "|");
			macAdd = st2.nextToken();
			if (macAdd.equals(macAddress) ){
				flag = 1;
				sTimeOfLastUpdateFromApp=st2.nextToken();
				sLastHubValueSutaReports=st2.nextToken();
				sLastHubUpdateSutaGotAT=st2.nextToken();
				remainingCredit = st2.nextToken();
				transactionDetails = st2.nextToken();
				StringTokenizer st3 = new StringTokenizer(transactionDetails, ":");
				while (st3.hasMoreTokens()){
					singleTransaction = st3.nextToken();
					StringTokenizer st4 = new StringTokenizer(singleTransaction, ",");
					while (st4.hasMoreTokens()){
						amount = st4.nextToken();
						details = st4.nextToken();
						revisedTransactionDetails += amount+"	"+details+"\n";

					}


				}

				break;
			}
		}
		if (flag == 1){
			/*try{
				editor.remove("remainingCredit");
				editor.remove("currTime");
				editor.remove("revisedTransactionDetails");
			}
			catch (Exception e){
			}
			*/

			// here if values of sTimeOfLastUpdateFromApp,sLastHubValueSutaReports,sLastHubUpdateSutaGotAT are "-1"
			// it implies that hub sent the notification, before the msg sent by suta received at hub
			editor.putString("notifSentByHubAt",notifSentByHubAt);
			editor.putString("notifRecievedBySutaAt",notifRecievedBySutaAt);
			editor.putString("sTimeOfLastUpdateFromApp",sTimeOfLastUpdateFromApp);
			editor.putString("sLastHubValueSutaReports",sLastHubValueSutaReports);
			editor.putString("sLastHubUpdateSutaGotAT",sLastHubUpdateSutaGotAT);
			editor.putString("remainingCredit",remainingCredit);
			editor.putString("revisedTransactionDetails",revisedTransactionDetails);
			editor.commit();
		}

	}




	public void deletedB()
	{
		sutaDB.execSQL("DELETE FROM users WHERE 1=1");
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		
		int uriType = sURIMatcher.match(uri);
		String app = uri.getLastPathSegment();
		
		if(USERS == uriType) {	
			
//			sqlBuilder.setTables("apps");
//			projection = new String[]{"app_id"};
//			selection = "app_name=\'" + app +"\'";
//			Log.d("Provider query", "Querying for app_id of app: " + app);			
//			Cursor c1 = sqlBuilder.query(
//		                sutaDB, 
//		                projection, 
//		                selection, 
//		                selectionArgs, 
//		                null, 
//		                null, 
//		                null);
//			c1.moveToFirst();
//			String app_id = c1.getString(0);
//			
//			sqlBuilder.setTables("uamaps INNER JOIN users ON uamaps.user_id=users.user_id");			
//			projection = new String[]{"name", "alias"};
//			// TODO IMPORTANT - right now just return the whole user list... implement app roles in suta hub for this to work
//			// selection = "app_id=\'" + app_id +"\'";
//			selection = null;
//			Log.d("Provider query", "Querying for users of app: " + app);			
//			Cursor c = sqlBuilder.query(
//		                sutaDB, 
//		                projection, 
//		                selection, 
//		                selectionArgs, 
//		                null, 
//		                null, 
//		                sortOrder);
			sqlBuilder.setTables("users");
			projection = new String[]{"name","alias"};
			selection = null;
			Log.d("Provider query", "Querying for app_id of app: " + app);			
			Cursor c1 = sqlBuilder.query(
		                sutaDB, 
		                projection, 
		                selection, 
		                selectionArgs, 
		                null, 
		                null, 
		                null);
			Log.d("Provider query", "Dumping cursor: " + DatabaseUtils.dumpCursorToString(c1));

			c1.setNotificationUri(getContext().getContentResolver(), uri);
			return c1;
		}
        return null;
	}

	// Updates / Deletes  - not needed - Internally the schema is dropped an recreated
	// as triggered from MainActivity so don't override delete, insert, update 
	// unluckily update is abstract in base class 
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) { return 0; }
	public int delete(Uri arg0, String arg1, String[] arg2) { return 0;	}
	public Uri insert(Uri arg0, ContentValues arg1) { return null; }

}