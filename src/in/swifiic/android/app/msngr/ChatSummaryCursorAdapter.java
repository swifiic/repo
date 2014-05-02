package in.swifiic.android.app.msngr;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ChatSummaryCursorAdapter extends CursorAdapter {

	public ChatSummaryCursorAdapter(Context context, Cursor c) {
		super(context, c, false);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View retView = inflater.inflate(R.layout.chat_summary_list_item, parent, false);
		return retView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView alias = (TextView) view.findViewById(R.id.firstLine);
		TextView message = (TextView) view.findViewById(R.id.secondLine);
		// TODO ImageView userImage = (ImageView) view.findViewById(R.id.chatListIcon);
		alias.setText(cursor.getString(cursor.getColumnIndex("user")));
		message.setText(cursor.getString(cursor.getColumnIndex("message")));
	}

}
