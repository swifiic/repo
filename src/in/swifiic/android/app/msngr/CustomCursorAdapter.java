package in.swifiic.android.app.msngr;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class CustomCursorAdapter extends CursorAdapter {

	public CustomCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}
	
	@Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.chat_message_received, parent, false);
 
        return retView;
    }
 
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView userAlias = (TextView) view.findViewById(R.id.userAlias);
        userAlias.setText(cursor.getString(cursor.getColumnIndex("fromUser")));
 
        TextView message = (TextView) view.findViewById(R.id.message);
        message.setText(cursor.getString(cursor.getColumnIndex("message")));
    }
}
