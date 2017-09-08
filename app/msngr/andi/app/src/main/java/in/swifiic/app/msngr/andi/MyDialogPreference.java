package in.swifiic.app.msngr.andi;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;

public class MyDialogPreference extends DialogPreference {

	private static final String TAG = "MyDialogPreference";

	public MyDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			Log.d(TAG, "Deleting all messages. POOF!!");
			DatabaseHelper db = new DatabaseHelper(this.getContext());
			db.deleteAll();
			db.closeDB();
		}
	}
} 
