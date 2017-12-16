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
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConversationListCursorAdapter extends CursorAdapter {

	@SuppressWarnings("deprecation")
	public ConversationListCursorAdapter(Context context, Cursor c) {
		super(context, c, FLAG_AUTO_REQUERY);
	}

	@Override
    public View newView(Context context, Cursor cursor, final ViewGroup parent) {
        // when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.chat_message, parent, false);
        //add onlongclicklistener
        //boolean flag =false;
        /*retView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LinearLayout timestamps = (LinearLayout)parent.findViewById(R.id.timestamps);
                if (timestamps.getVisibility() == View.VISIBLE)
                    timestamps.setVisibility(View.GONE);
                else
                    timestamps.setVisibility(View.VISIBLE);
                return false;
            }
        });*/
        return retView;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView userAlias = (TextView) view.findViewById(R.id.userAlias);
        TextView message = (TextView) view.findViewById(R.id.message);
        TextView sentAt = (TextView) view.findViewById(R.id.sentAt);
        TextView hubrelayedAt = (TextView) view.findViewById(R.id.hubrelayedAt);
        TextView receivedAt = (TextView) view.findViewById(R.id.receivedAt);
        //sentat
        Date sentDate = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_SENTAT))));

        java.text.DateFormat df = DateFormat.getTimeFormat(context);
        String timeStringSent = df.format(sentDate);
        //hubrelayedat



        int isInbound = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_IS_INBOUND));
        if(isInbound == 1) {
            Date hubrelayedatDate = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_RELAYEDAT))));

            java.text.DateFormat dfh = DateFormat.getTimeFormat(context);
            String timeStringHubRelayed = dfh.format(hubrelayedatDate);
            //receivedat
            Date receivedDate = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_RECEIVEDAT))));

            java.text.DateFormat dfr = DateFormat.getTimeFormat(context);
            String timeStringReceived = dfr.format(receivedDate);

        	userAlias.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_USER)));
        	userAlias.setGravity(Gravity.LEFT);
        	message.setGravity(Gravity.LEFT);
            sentAt.setGravity(Gravity.LEFT);
            hubrelayedAt.setGravity(Gravity.LEFT);
            receivedAt.setGravity(Gravity.LEFT);
        	view.setBackgroundColor(context.getResources().getColor(R.color.light_green));
            hubrelayedAt.setText(timeStringHubRelayed);
            receivedAt.setText(timeStringReceived);
        } else {
        	userAlias.setText("Me");
        	userAlias.setGravity(Gravity.RIGHT);
        	message.setGravity(Gravity.RIGHT);
            sentAt.setGravity(Gravity.RIGHT);
//            hubrelayedAt.setGravity(Gravity.RIGHT);
//            receivedAt.setGravity(Gravity.RIGHT);
        	view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
        message.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_MESSAGE)));
        sentAt.setText(timeStringSent);
    }
}
