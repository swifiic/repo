package in.swifiic.plat.helper.andi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import in.swifiic.plat.helper.andi.xml.Action;
import in.swifiic.plat.helper.andi.xml.Notification;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import de.tubs.ibr.dtn.util.Base64;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Helper {

	public static Notification parseNotification(String str) {
        Serializer serializer = new Persister();
        try {
        	Notification notif = serializer.read(Notification.class,str);
        	return notif;
        } catch(Exception e) {
        	// This should not happen unless APP tries a random string 
        	// String given from Generic Service was already tested for success
        }
        return null;
	}
	
	public static String serializeAction(Action act) {
        try {
        	 StringWriter writer = new StringWriter();
        	 Serializer serializer = new Persister();  
       	     serializer.write(act, writer);  

        	 return writer.getBuffer().toString();
        } catch(Exception e) {
        	// This should not happen since Action is a XML serializable object
        }
        return null;
	}
	public static String sendAction(Action act, String hubAddress, Context c) {
        try {
        	// Loading my identity from preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
            String myIdentity = sharedPref.getString("my_identity", "");
        	act.addArgument("fromUser", myIdentity);
        	
        	String msg = serializeAction(act);
            
        	Intent i = new Intent(c, GenericService.class);
            
            i.setAction(Constants.SEND_MSG_INTENT);
            i.putExtra("action", msg); // msgTextToSend
            i.putExtra("hub_address", hubAddress);
            
            Log.d("Helper", "Sending: " + msg + "To: " + hubAddress);
            
            c.startService(i);
        } catch(Exception e) {
        	Log.e("sendAction", "Something goofy:" + e.getMessage());
        }
        return null;
	}
	
	/**
	 * @param f
	 *            -> file type
	 * @return String -> Base64 encoded string of the file
	 */
	// TODO
	public static String fileToB64String(String fName) {
		StringBuffer fileBuf = new StringBuffer();
		File readFile = new File(fName);

		if (readFile.exists()) {
			FileInputStream fis;
			try {
				fis = new FileInputStream(readFile);

				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				byte[] buf = new byte[1024];
				try {
					for (int readNum; (readNum = fis.read(buf)) != -1;) {
						bos.write(buf, 0, readNum);
						// no doubt here is 0
						/*
						 * Writes len bytes from the specified byte array
						 * starting at offset off to this byte array output
						 * stream.
						 */
					}
				} catch (IOException ex) {
				}

				byte[] bytes = bos.toByteArray();

				fileBuf.append(Base64.encodeBytes(bytes));
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}
		}

		return fileBuf.toString();
	}

	public static void b64StringToFile(String contentB64, String fileName){
		File writeFile = new File(fileName);
		FileOutputStream str =null;
		try {
			str = new FileOutputStream(writeFile);
			str.write(Base64.decode(contentB64));
			
		} catch (Exception e) {
			Log.e("b64StringToFile", "Could not save file " + e.getLocalizedMessage());
		} finally {
			try {
				if(null != str) str.close();
			} catch (Exception e) {/* do nothing */	}
		}
		
		
		
	}


public static String sendSutaInfo(Action act, String hubAddress, Context c) {
    try {
    	// Loading my identity from preferences
    	 SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
         String myIdentity = sharedPref.getString("my_identity", "");
     	act.addArgument("fromUser", myIdentity);
    	Intent i = new Intent(c, GenericService.class);
    	String msg = serializeAction(act);
        i.setAction(Constants.SEND_INFO_INTENT);
        i.putExtra("action", msg); // msgTextToSend
        i.putExtra("hub_address", hubAddress);
     
        
        Log.d("Helper", "Sending: " + msg + "To: " + hubAddress);
        
        c.startService(i);
    } catch(Exception e) {
    	Log.e("sendAction", "Something goofy:" + e.getMessage());
    }
    return null;
}
}
