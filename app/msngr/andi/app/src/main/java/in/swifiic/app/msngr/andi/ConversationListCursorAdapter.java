package in.swifiic.app.msngr.andi;

import in.swifiic.app.msngr.andi.R;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
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
        TextView timestamp = (TextView) view.findViewById(R.id.timestamp);
        
        Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("sentAt"))));
        
        java.text.DateFormat df = DateFormat.getTimeFormat(context);
        String timeString = df.format(date);
        
        int isInbound = cursor.getInt(cursor.getColumnIndex("isInbound"));
        if(isInbound == 1) {
        	userAlias.setText(cursor.getString(cursor.getColumnIndex("user")));
        	userAlias.setGravity(Gravity.LEFT);
        	message.setGravity(Gravity.LEFT);
        	timestamp.setGravity(Gravity.LEFT);
        	view.setBackgroundColor(context.getResources().getColor(R.color.light_green));
        } else {
        	userAlias.setText("Me");
        	userAlias.setGravity(Gravity.RIGHT);
        	message.setGravity(Gravity.RIGHT);
        	timestamp.setGravity(Gravity.RIGHT);
        	view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
        message.setText(cursor.getString(cursor.getColumnIndex("message")));   
        timestamp.setText(timeString);
    }
}
