package swifiic.soa;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

   public static final String DATABASE_NAME = "USERDATABASE.db";
   public static final String USERS_TABLE = "Users";
   public static final String USER_NAME = "UserName";
   public static final String PASSWORD = "Password";

   public DBHelper(Context context)
   {
      super(context, DATABASE_NAME , null, 1);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      // TODO Auto-generated method stub
      db.execSQL(
      "create table "+USERS_TABLE+
      " ( "+USER_NAME+" varchar(15) primary key , "+PASSWORD+" varchar(15))");
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // TODO Auto-generated method stub
      db.execSQL("DROP TABLE IF EXISTS "+USERS_TABLE);
      onCreate(db);
   }

   public void addUser(String user,String pass){
	   if (isUserPresent(user)) updatePassword(user, pass);
	   else insertUser(user, pass);
   }
   
   public boolean insertUser(String user, String pass)
   {
      SQLiteDatabase db = this.getWritableDatabase();
      ContentValues contentValues = new ContentValues();

      contentValues.put(USER_NAME, user);
      contentValues.put(PASSWORD, pass);

      db.insert(USERS_TABLE, null, contentValues);
      return true;
   }
   
  public boolean isUserPresent(String user){
	  SQLiteDatabase db = this.getReadableDatabase();
      Cursor res =  db.rawQuery( "select * from "+USERS_TABLE+" where "+USER_NAME+" = '"+user+"'", null );
      res.moveToFirst();  
      return !res.isAfterLast();
  }
   
   /*
   public int numberOfRows(){
      SQLiteDatabase db = this.getReadableDatabase();
      int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
      return numRows;
   }*/
   public boolean updatePassword (String user,String pass)
   {
      SQLiteDatabase db = this.getWritableDatabase();
      ContentValues contentValues = new ContentValues();
      contentValues.put(USER_NAME,user);
      contentValues.put(PASSWORD, pass);
      db.update(USERS_TABLE, contentValues,USER_NAME+" = ? ", new String[] { user } );
      return true;
   }

   public HashMap<String,String> getAllUsers()
   {
      HashMap<String,String> map = new HashMap<String,String>();
      //hp = new HashMap();
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor res =  db.rawQuery( "select * from "+USERS_TABLE, null );
      res.moveToFirst();
      while(res.isAfterLast() == false){
      map.put(res.getString(res.getColumnIndex(USER_NAME)),res.getString(res.getColumnIndex(PASSWORD)));
      res.moveToNext();
      }
   return map;
   }
}