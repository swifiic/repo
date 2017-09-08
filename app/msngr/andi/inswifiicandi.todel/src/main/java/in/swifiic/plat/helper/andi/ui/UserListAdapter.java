package in.swifiic.plat.helper.andi.ui;

import java.util.LinkedList;
import java.util.List;

import in.swifiic.plat.andi.R;
import in.swifiic.plat.helper.andi.Constants;
import in.swifiic.plat.helper.andi.User;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListAdapter extends BaseAdapter {
    private LayoutInflater mInflater = null;
    private List<User> mList = new LinkedList<User>();

    private class ViewHolder {
        public ImageView imageIcon; // TBD XXX integrate user PNG here - for their avatar / image
        public TextView textName;
        public TextView textAlias;
        public User user;
    }

    public UserListAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }
    
    public void swapList(List<User> data) {
    	mList = data;
    	notifyDataSetChanged();
    }

    public int getCount() {
        return mList.size();
    }

    public void add(User n) {
        mList.add(n);
    }

    public void clear() {
        mList.clear();
    }

	public void remove(int position) {
        mList.remove(position);
    }

    public Object getItem(int position) {
        return mList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.userlist_item_lib, null, true);
            holder = new ViewHolder();
            holder.imageIcon = (ImageView) convertView.findViewById(R.id.imageIcon);
            holder.textName = (TextView) convertView.findViewById(R.id.textName);
            holder.textAlias = (TextView) convertView.findViewById(R.id.textAlias);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.user = mList.get(position);
        
        Bitmap bm = BitmapFactory.decodeFile(Constants.PUBLIC_DIR_PATH + holder.user.userName + ".png");
        holder.imageIcon.setImageBitmap(bm);
        holder.textName.setText(holder.user.alias);
        holder.textAlias.setText(holder.user.userName);

        return convertView;
    }  
}
