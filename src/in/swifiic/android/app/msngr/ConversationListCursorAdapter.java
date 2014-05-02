package in.swifiic.android.app.msngr;

import android.content.Context;
import android.database.Cursor;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ConversationListCursorAdapter extends CursorAdapter {

	@SuppressWarnings("deprecation")
	public ConversationListCursorAdapter(Context context, Cursor c) {
		super(context, c, FLAG_AUTO_REQUERY);
	}
	
	@Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.chat_message, parent, false);
        return retView;
    }
 
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView userAlias = (TextView) view.findViewById(R.id.userAlias);
        TextView message = (TextView) view.findViewById(R.id.message);
        int isInbound = cursor.getInt(cursor.getColumnIndex("isInbound"));
        if(isInbound == 1) {
        	userAlias.setText(cursor.getString(cursor.getColumnIndex("user")));
        	userAlias.setGravity(Gravity.LEFT);
        	message.setGravity(Gravity.LEFT);
        } else {
        	userAlias.setGravity(Gravity.RIGHT);
        	message.setGravity(Gravity.RIGHT);
        	userAlias.setText("Me");
        }
        message.setText(cursor.getString(cursor.getColumnIndex("message")));    
    }
}
