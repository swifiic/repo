package in.swifiic.android.app.lib;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
	public String name;
	public String id;
	public byte[] imageArray;
	
	public int describeContents() {
		return 0;
	}


	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(id);
		dest.writeByteArray(imageArray);
	}
	
    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(final Parcel source) {
        	User n = new User();
        	n.name = new String(source.readString());
        	n.id = source.readString();
        	source.readByteArray(n.imageArray);
        	return n;
        }

        public User[] newArray(final int size) {
            return new User[size];
        }
    };
}